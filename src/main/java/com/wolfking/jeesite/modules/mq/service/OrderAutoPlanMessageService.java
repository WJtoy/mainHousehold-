package com.wolfking.jeesite.modules.mq.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.es.ServicePointStation;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.kkl.kklplus.entity.sys.SysLog;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.PlanRadiusService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServicePointStationService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderAutoPlanMessage;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoPlanMessageSender;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.service.OrderMQService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.provideres.feign.OrderFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointProductFeign;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryServicePointService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointProductService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

/**
 * 自动派单业务实现层
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderAutoPlanMessageService {

    private static final int DELAY_TIME = 60000; //延迟60秒
    private static final int PAGE_SIZE = 10;//分页大小
    // 服务点检索总数
    // 如此值设定为10，第一半径返回5个服务点，都不符合，需要再检索第二半径，
    // 如果返回10个服务点，又没有符合要求的服务点，这时总检索数量是15，已超过10，就不检索下一页或下一半径
    private static final int QUERY_STATION_QTY = 10;
    private static final User PLAN_BY = new User(4l,"自动派单","");

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderFeign orderESFeign;
    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ServicePointStationService stationService;

//    @Autowired
//    private MSServicePointProductService msServicePointProductService;

    @Autowired
    private MSProductCategoryServicePointService productCategoryServicePointService;

    @Autowired
    private MSServicePointService msServicePointService;
    //region 消费方法

    /**
     * 处理收到的消息
     * @param message

    public void processMessage_old(MQOrderAutoPlanMessage.OrderAutoPlan message){
        if(message == null){
            log.error("message is null");
            LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService","message is null",new RuntimeException("message is null"),null, SysLog.TYPE_EXCEPTION);
            return;
        }
        // 测试代码
        //System.out.println("[OrderAutoPlanMessage]get message:" + new JsonFormat().printToString(message));
        // end
        StringBuilder msg = new StringBuilder(200);
        if(!OrderMQService.canAutoPlan(message)){
            msg.append(MessageFormat.format("不符合自动派单条件，请检查消息体:{0}",new JsonFormat().printToString(message)));
            log.error(msg.toString());
            LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
            return;
        }

        int[] radiuses = IntStream.of(message.getAreaRadius().getRadius1(),message.getAreaRadius().getRadius2(),message.getAreaRadius().getRadius3()).toArray();
        //锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, message.getOrderId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            msg.append(MessageFormat.format("订单：{0} 自动派单失败 - 订单正在处理中",message.getOrderNo()));
            log.error(msg.toString());
            LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
            return;
        }

        try {
            // 检查订单是否已派单
            Order order = orderService.getOrderById(message.getOrderId(),message.getQuarter(), OrderUtils.OrderDataLevel.STATUS,true);
            if(order == null || order.getOrderCondition() == null){
                msg.append(MessageFormat.format("读取订单:{0} 失败，无订单数据",message.getOrderNo()));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            OrderCondition orderCondition = order.getOrderCondition();
            int statusValue = Optional.ofNullable(orderCondition).map(t->t.getStatus()).map(t->t.getIntValue()).orElse(0);
            //int statusValue = orderCondition.getStatusValue();
            if(statusValue == 0){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败 - 读取当前状态：{1} 错误",message.getOrderNo(),statusValue));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            if( statusValue >= Order.ORDER_STATUS_PLANNED){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败 - 当前状态：{1} 不能自动派单",message.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            if(orderCondition.getServicePoint() != null && orderCondition.getServicePoint().getId() != null && orderCondition.getServicePoint().getId()>0){
                String servicePointName = Optional.ofNullable(orderCondition.getServicePoint()).map(t->t.getName()).orElse("");
                msg.append(MessageFormat.format("订单:{0} 自动派单失败 - 订单已分配网点：{1} 不能自动派单",message.getOrderNo(),servicePointName));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }

            //自动个派单帐号
            order.setCreateBy(PLAN_BY);

            //读取可派单网点
            ServicePointStation servicePointStation = getNearServicePoint(message.getAreaId(),radiuses,message.getLongitude(),message.getLatitude());
            //无网点
            if(servicePointStation == null){
                msg.append(MessageFormat.format("无可自动派单网点,orderNo:{0} ,areaId:{1} , longtitude:{2}, latitude:{3}",message.getOrderNo(),message.getAreaId(),message.getLongitude(),message.getLatitude()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            //log.warn("自动派单网点:{}",GsonUtils.getInstance().toGson(servicePointStation));

            ServicePoint servicePoint = servicePointService.getFromCache(servicePointStation.getServicePointId());
            if(servicePoint == null){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：读取网点:{1} 信息错误",message.getOrderNo(),servicePointStation.getServicePointId()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            Dict engineerPaymentType = servicePoint.getFinance().getPaymentType();
            if (engineerPaymentType == null || StringUtils.isBlank(engineerPaymentType.getValue()) || engineerPaymentType.getIntValue() <= 0) {
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：网点:{1} 付款方式未设定",message.getOrderNo(),servicePoint.getName()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), servicePoint.getPrimary().getId());
            if (engineer == null) {
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：读取网点:{1}({2}) 主帐号信息错误",message.getOrderNo(),servicePoint.getServicePointNo(),servicePoint.getName()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            servicePoint.setPrimary(engineer);
            try {
                orderCondition.setServicePoint(servicePoint);
                OrderFee orderFee = order.getOrderFee();
                orderFee.setEngineerPaymentType(engineerPaymentType);
                double distance = servicePointStation.getDistance()/1000d;
                distance = new BigDecimal(distance).setScale(2,   RoundingMode.HALF_UP).doubleValue();
                orderFee.setPlanDistance(distance);//上门距离(公里)
                order.setSendEngineerMessageFlag(1);//发送短信给网点
                order.setSendUserMessageFlag(1);//发送短信给用户
                orderService.autoPlanOrder(order);
            }catch (Exception e){
                log.error("订单:{} 自动派单失败",message.getOrderNo(),e);
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),e,null, SysLog.TYPE_EXCEPTION);
            }
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }
     */

    /**
     * 处理收到的消息
     * @param message
     */
    public void processMessage(MQOrderAutoPlanMessage.OrderAutoPlan message){
        // add on 2019-5-30
        if(message == null){
            log.error("message is null");
            LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService","message is null",new RuntimeException("message is null"),null, SysLog.TYPE_EXCEPTION);
            return;
        }
        // 测试代码
        //System.out.println("[OrderAutoPlanMessage]get message:" + new JsonFormat().printToString(message));
        // end
        StringBuilder msg = new StringBuilder(200);
        if(!OrderMQService.canAutoPlan(message)){
            msg.append(MessageFormat.format("不符合自动派单条件，请检查消息体:{0}",new JsonFormat().printToString(message)));
            log.error(msg.toString());
            LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
            return;
        }

        //int[] radiuses = IntStream.of(message.getAreaRadius().getRadius1(),message.getAreaRadius().getRadius2(),message.getAreaRadius().getRadius3()).toArray();
        //锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, message.getOrderId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            msg.append(MessageFormat.format("订单：{0} 自动派单失败 - 订单正在处理中",message.getOrderNo()));
            log.error(msg.toString());
            LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
            return;
        }

        try {
            // 检查订单是否已派单
            Order order = orderService.getOrderById(message.getOrderId(),message.getQuarter(), OrderUtils.OrderDataLevel.STATUS,true);
            if(order == null || order.getOrderCondition() == null){
                msg.append(MessageFormat.format("读取订单:{0} 失败，无订单数据",message.getOrderNo()));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            OrderCondition orderCondition = order.getOrderCondition();
            int statusValue = Optional.ofNullable(orderCondition).map(t->t.getStatus()).map(t->t.getIntValue()).orElse(0);
            //int statusValue = orderCondition.getStatusValue();
            if(statusValue == 0){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败 - 读取当前状态：{1} 错误",message.getOrderNo(),statusValue));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            if( statusValue >= Order.ORDER_STATUS_PLANNED){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败 - 当前状态：{1} 不能自动派单",message.getOrderNo(),order.getOrderCondition().getStatus().getLabel()));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            if(orderCondition.getServicePoint() != null && orderCondition.getServicePoint().getId() != null && orderCondition.getServicePoint().getId()>0){
                String servicePointName = Optional.ofNullable(orderCondition.getServicePoint()).map(t->t.getName()).orElse("");
                msg.append(MessageFormat.format("订单:{0} 自动派单失败 - 订单已分配网点：{1} 不能自动派单",message.getOrderNo(),servicePointName));
                log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }

            //自动个派单帐号
            order.setCreateBy(PLAN_BY);
            Long productCategoryId = orderCondition.getProductCategoryId();  //获取订单品类  // add on 2020-5-12

            //读取可派单网点
            //ServicePointStation servicePointStation = getNearServicePointBySubAreaId(message.getAreaId(), productCategoryId);  // mark on 2020-8-24
            // add on 2020-8-24 begin
            ServicePointStation servicePointStation = getNearServicePointBySubAreaIdNew(message.getAreaId(), productCategoryId);
            // add on 2020-8-24 end
            //无网点
            if(servicePointStation == null){
                msg.append(MessageFormat.format("无可自动派单网点,orderNo:{0} ,areaId:{1} , longtitude:{2}, latitude:{3}",message.getOrderNo(),message.getAreaId(),message.getLongitude(),message.getLatitude()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            //log.warn("自动派单网点:{}",GsonUtils.getInstance().toGson(servicePointStation));

            ServicePoint servicePoint = servicePointService.getFromCache(servicePointStation.getServicePointId());
            if(servicePoint == null){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：读取网点:{1} 信息错误",message.getOrderNo(),servicePointStation.getServicePointId()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            Dict engineerPaymentType = servicePoint.getFinance().getPaymentType();
            if (engineerPaymentType == null || StringUtils.isBlank(engineerPaymentType.getValue()) || engineerPaymentType.getIntValue() <= 0) {
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：网点:{1} 付款方式未设定",message.getOrderNo(),servicePoint.getName()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), servicePoint.getPrimary().getId());
            if (engineer == null) {
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：读取网点:{1}({2}) 主帐号信息错误",message.getOrderNo(),servicePoint.getServicePointNo(),servicePoint.getName()));
                //log.error(msg.toString());
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            //2021/05/18 偏远区域判断是否维护网点服务价格
            RestResult<Object> remoteCheckResult = orderService.checkServicePointRemoteAreaAndPrice(servicePoint.getId(), order.getOrderCondition(), order.getItems());
            if(remoteCheckResult.getCode() != ErrorCode.NO_ERROR.code){
                msg.append(MessageFormat.format("订单:{0} 自动派单失败：网点:{1}({2}) {3}",message.getOrderNo(),servicePoint.getServicePointNo(),servicePoint.getName(),remoteCheckResult.getMsg()));
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),new RuntimeException(msg.toString()),null, SysLog.TYPE_EXCEPTION);
                return;
            }
            servicePoint.setPrimary(engineer);
            try {
                orderCondition.setServicePoint(servicePoint);
                OrderFee orderFee = order.getOrderFee();
                orderFee.setEngineerPaymentType(engineerPaymentType);
                double distance = servicePointStation.getDistance() == null? 0:servicePointStation.getDistance()/1000d;
                distance = new BigDecimal(distance).setScale(2,   RoundingMode.HALF_UP).doubleValue();
                orderFee.setPlanDistance(distance);//上门距离(公里)
                order.setSendEngineerMessageFlag(1);//发送短信给网点
                order.setSendUserMessageFlag(1);//发送短信给用户
                orderService.autoPlanOrder(order);
            }catch (Exception e){
                log.error("订单:{} 自动派单失败",message.getOrderNo(),e);
                LogUtils.saveLog("自动派单失败","OrderAutoPlanMessageService",message.getOrderNo(),e,null, SysLog.TYPE_EXCEPTION);
            }
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    //endregion

    /**
     * 自动派单时读取可派单网点列表
     * @param areaId
     * @param distances 半径列表
     * @param longitude
     * @param latitude
     * @return
     */
    public ServicePointStation getNearServicePoint(long areaId, int[] distances,double longitude,double latitude){
        //不同半径返回的服务点有重复，重复服务点不重复计数
        Map<Long,Double> stationIdMaps = Maps.newHashMap();
        int remain = QUERY_STATION_QTY;
        return getNearServicePointRecursion(stationIdMaps,remain,areaId,0,distances,1,longitude,latitude);
    }

    /**
     * 递归获得网点
     * @param remain    剩余检索服务点数量
     * @param areaId    区县id
     * @param index     当前半径索引
     * @param distances 半径列表
     * @param longitude 经度
     * @param latitude  维度
     * @return
     */
    private ServicePointStation getNearServicePointRecursion(Map<Long,Double> stationIdMaps,int remain,long areaId, int index, int[] distances,int pageNo,double longitude,double latitude) {
        if(index >= distances.length || remain <= 0){
            return null;
        }
        int distance = 0;
        log.warn("====================================================================================");
        log.warn("index:{} , distance: {} ,remain: {}",index,distance,remain);
        MSResponse<MSPage<ServicePointStation>> msResponse = orderESFeign.getNearByServicePoint(areaId, distance,
                longitude, latitude,
                "", "", 1,"", "", "",pageNo, PAGE_SIZE
                );
        if (!MSResponse.isSuccessCode(msResponse)) {
            log.error("[getNearServicePoint]获取自动派单网点列表错误：{}", msResponse.getMsg());
            return null;
        }
        MSPage<ServicePointStation> msPage = msResponse.getData();
        log.warn("ES返回列表数量:{} , distance: {}",msPage.getList().size(),distance);
        if (msPage == null || msPage.getList() == null || msPage.getList().size() == 0) {
            // 下个半径检索
            if(index+1 >= distances.length){
                return null;
            }
            int nextDistance = distances[index+1];
            log.warn("下一半径： index:{} , distance: {}",index+1,nextDistance);
            //下个半径为0 或范围比当前小，中断递归
            if(nextDistance <= 0 || nextDistance <= distance){
                return null;
            }
            //从当前页开始查询
            return getNearServicePointRecursion(stationIdMaps,remain,areaId,index+1,distances,pageNo,longitude, latitude);
        } else {
            log.warn("返回列表:{}", GsonUtils.getInstance().toGson(msPage));
            List<ServicePointStation> list = msPage.getList();
            //距离相同随机处理
            list.stream().forEach(t -> t.setDistance(t.getDistance() + ThreadLocalRandom.current().nextDouble(1,10)));
            //重新排序
            //1.等级(倒序)
            //2.距离(正序)
            Collections.sort(list, Comparator.comparing(ServicePointStation::getLevel).reversed()
                    .thenComparing(ServicePointStation::getDistance));
            ServicePointStation station;
            com.wolfking.jeesite.modules.md.entity.ServicePointStation cacheStation = null;
            int rowCount = list.size();
            for(int i=0;i<rowCount;i++){
                station = list.get(i);
                if(stationIdMaps.containsKey(station.getStationId())){
                    continue;
                }
                remain--;
                stationIdMaps.put(station.getStationId(),station.getDistance());
                // 从缓存读取服务点的服务范围(半径)
                cacheStation = stationService.getServicePointStationCache(station.getServicePointId(),station.getStationId());
                if(cacheStation != null && station.getDistance() <= cacheStation.getRadius()){
                    log.warn("remain: {}",remain);
                    log.warn("符合的服务点 - index:{} serviePoint Id:{} level:{} station:{} distance:{} radius:{}",i,station.getServicePointId(),station.getLevel(),station.getStationName(),station.getDistance(),cacheStation.getRadius());
                    return station;
                }else{
                    if(cacheStation == null){
                        log.error("缓存中无服务点,serviceId:{} stationId:{}",station.getServicePointId(),station.getStationId());
                    }else{
                        log.warn("服务点:{} 服务范围：{}, 实际上门距离:{} ,超出服务范围",station.getStationId(),cacheStation.getRadius(),station.getDistance());
                    }
                    continue;
                }
            }
            //本次查询没有符合服务点
            //剩余检索次数
            if(remain<=0){
                return null;
            }
            //下一页
            log.warn("pageCount:{} ,pageNo: {} ,remain: {}",msPage.getPageCount(),pageNo,remain);
            if(msPage.getPageCount() > pageNo){
                return getNearServicePointRecursion(stationIdMaps,remain,areaId,index,distances,pageNo+1,longitude, latitude);
            }
            // 下个半径检索
            if(index+1 >= distances.length){
                return null;
            }
            int nextDistance = distances[index+1];
            log.warn("下一半径：index:{} , distance: {}",index+1,nextDistance);
            //下个半径为0 或范围比当前小，中断递归
            if(nextDistance <= 0 || nextDistance <= distance){
                return null;
            }
            //因下个半径比当前半径大，所以查询数据会重复
            int nextPageNo = pageNo;
            if(rowCount == msPage.getPageSize()){
                nextPageNo++;
            }
            return getNearServicePointRecursion(stationIdMaps,remain,areaId,index+1,distances,nextPageNo,longitude, latitude);
        }
    }

    /**
     * 自动派单时读取可派单网点列表
     * @param areaId 区域id(区/县)
     * @param  productCategoryId 订单的产品id
     * @Date  2019-5-30
     * @return
     */
    public ServicePointStation getNearServicePointBySubAreaId(long areaId, Long productCategoryId) {
        int remain = QUERY_STATION_QTY;

        log.warn("====================================================================================");
        log.warn("areaId:{} ",areaId);
        // 为兼容以前的版本,定义如下的变量
        int pageNo = 1;
        MSResponse<MSPage<ServicePointStation>> msResponse = orderESFeign.getNearByMatchSubAreaId(areaId,
                "", "", 1,"", "", "",pageNo, PAGE_SIZE
        );
        if (!MSResponse.isSuccessCode(msResponse)) {
            log.error("[getNearServicePoint]获取自动派单网点列表错误：{}", msResponse.getMsg());
            return null;
        }
        MSPage<ServicePointStation> msPage = msResponse.getData();
        if (msPage == null || msPage.getList() == null || msPage.getList().size() == 0) {
            return null;
        } else {
            log.warn("返回列表:{}", GsonUtils.getInstance().toGson(msPage));
            List<ServicePointStation> list = msPage.getList();
            // add on 2020-4-21  begin
            // 在返回的网点中检查其所有产品是否包含在其中
            List<ServicePointStation> conditionalList = Lists.newArrayList();
            for(ServicePointStation servicePointStation: list) {
                //List<Long> productCategoryList = productCategoryServicePointService.findListByServicePiontIdFromCacheForSD(servicePointStation.getServicePointId());
                boolean bAllExists =  productCategoryServicePointService.existByPointIdAndCategoryIdFromCacheForSD(servicePointStation.getServicePointId(), productCategoryId);
                if (bAllExists) {
                    conditionalList.add(servicePointStation);
                }
            }
            if (ObjectUtils.isEmpty(conditionalList)) {
                return null;  // 没有符合条件的网点
            }
            if (conditionalList.size() < remain) {
                remain = conditionalList.size();
            }
            // add on 2020-4-21  end



            // mark on 2020-4-21 begin
//            if (list.size() < remain) {
//                remain = list.size();
//            }
            // mark on 2020-4-21 end
            //int iRandom = ThreadLocalRandom.current().nextInt(1,remain);
            int iRandom = ThreadLocalRandom.current().nextInt(remain);

            // 重新排序
            // 1.等级(倒序)
            // mark on 2020-4-21 begin
//            Collections.sort(list, Comparator.comparing(ServicePointStation::getLevel).reversed());
//            ServicePointStation station = list.get(iRandom);
            // mark on 2020-4-21 end
            Collections.sort(conditionalList, Comparator.comparing(ServicePointStation::getLevel).reversed());  //add on 2020-4-21
            ServicePointStation station = conditionalList.get(iRandom);   //add on 2020-4-21

            log.warn("remain: {}",remain);
            log.warn("符合条件的服务点 - index:{} serviePoint Id:{} level:{} station:{}.",iRandom,station.getServicePointId(),station.getLevel(),station.getStationName());

            return station;
        }
    }

    /**
     * 自动派单时读取可派单网点列表
     * @param subAreaId 街道/乡/镇区域id
     * @param  productCategoryId 订单的产品id
     * @Date  2020-8-24
     * @return
     */
    public ServicePointStation getNearServicePointBySubAreaIdNew(long subAreaId, Long productCategoryId) {
        int remain = QUERY_STATION_QTY;

        log.warn("====================================================================================");
        log.warn("subAreaId:{} ", subAreaId);
        int pageNo = 1;
        MSResponse<MSPage<ServicePointStation>> msResponse = orderESFeign.getNearByServicePointMatchSubAreaId(productCategoryId, subAreaId,
                "", "", 1,"", "", "",pageNo, PAGE_SIZE
        );
        if (!MSResponse.isSuccessCode(msResponse)) {
            log.error("[getNearServicePointBySubAreaIdNew]获取自动派单网点列表错误：{}", msResponse.getMsg());
            return null;
        }
        MSPage<ServicePointStation> msPage = msResponse.getData();
        if (msPage == null || msPage.getList() == null || msPage.getList().size() == 0) {
            return null;
        } else {
            log.warn("返回列表:{}", GsonUtils.getInstance().toGson(msPage));
            List<ServicePointStation> list = msPage.getList();

            List<Long> servicePointIdList = list.stream().distinct().map(ServicePointStation::getServicePointId).collect(Collectors.toList());
            List<MDServicePointViewModel> servicePointViewModels =  msServicePointService.findBatchByIdsByCondition(servicePointIdList,Arrays.asList("id","capacity","unfinishedOrderCount"),0);
            List<MDServicePointViewModel> canPlanServicePointViewModels = Lists.newArrayList();
            ServicePointStation station=null;
            if(servicePointViewModels!=null && servicePointViewModels.size()>0){
                canPlanServicePointViewModels = servicePointViewModels.stream().filter(t->t.getCapacity()>t.getUnfinishedOrderCount()).collect(Collectors.toList());
            }
            int iRandom=0;
            if(canPlanServicePointViewModels!=null && canPlanServicePointViewModels.size()>0){
                if (canPlanServicePointViewModels.size() < remain) {
                    remain = canPlanServicePointViewModels.size();
                }
                iRandom = ThreadLocalRandom.current().nextInt(remain);
                MDServicePointViewModel mdServicePointViewModel = canPlanServicePointViewModels.get(iRandom);
                if(mdServicePointViewModel!=null){
                    station = list.stream().filter(t->t.getServicePointId().equals(mdServicePointViewModel.getId())).findFirst().orElse(null);
                }
            }else{
                log.error("无符合网点,获取网点的未完工数大于网点工单容量,servicePointStatus:{}",GsonUtils.toGsonString(list));
            }
            // 重新排序
            // 1.等级(倒序)
            //Collections.sort(list, Comparator.comparing(ServicePointStation::getLevel).reversed());
            //ServicePointStation station = list.get(iRandom);

            log.warn("remain: {}", remain);
            log.warn("符合条件的服务点 - index:{} serviePoint Id:{} level:{} station:{}.",iRandom,station.getServicePointId(),station.getLevel(),station.getStationName());

            return station;
        }
    }

}
