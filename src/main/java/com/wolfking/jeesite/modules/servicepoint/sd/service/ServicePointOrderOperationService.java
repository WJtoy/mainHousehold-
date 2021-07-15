/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.servicepoint.sd.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.mq.conf.NoticeMessageConfig;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderHeadDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.md.*;
import com.wolfking.jeesite.modules.servicepoint.ms.mq.SpSmsCallbackTaskMQSender;
import com.wolfking.jeesite.modules.servicepoint.ms.mq.SpSmsMQSender;
import com.wolfking.jeesite.modules.servicepoint.ms.receipt.SpAbnormalFormService;
import com.wolfking.jeesite.modules.servicepoint.ms.receipt.SpOrderMaterialService;
import com.wolfking.jeesite.modules.servicepoint.ms.receipt.SpOrderOpitionTraceService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpB2BCenterOrderService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderCacheReadService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.servicepoint.ms.sys.SpAPPMessagePushService;
import com.wolfking.jeesite.modules.servicepoint.ms.sys.SpSystemService;
import com.wolfking.jeesite.modules.servicepoint.ms.utils.SpRedisUtils;
import com.wolfking.jeesite.modules.servicepoint.sd.dao.ServicePointOrderOperationDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

/**
 * 网点工单操作
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointOrderOperationService extends LongIDBaseService {

    @Value("${shortmessage.ignore-data-sources}")
    private String smIgnoreDataSources;
    @Value("${voiceService.enabled}")
    private boolean voiceEnabled;
    @Value("${site.code}")
    private String siteCode;

    @Resource
    OrderDao orderDao;

    @Autowired
    OrderHeadDao orderHeadDao;

    @Autowired
    private SpRedisUtils redisUtils;

    @Autowired
    private SpSmsMQSender smsMQSender;
    @Autowired
    private SpSmsCallbackTaskMQSender smsCallbackTaskMQSender;

    @Autowired
    private SpOrderCacheReadService orderCacheReadService;

    @Autowired
    private SpServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private SpB2BCenterOrderService b2BCenterOrderService;

    @Autowired
    private SpSystemService systemService;
    @Autowired
    private SpCustomerService customerService;
    @Autowired
    private SpServicePointService servicePointService;
    @Autowired
    private SpEngineerService engineerService;

    @Autowired
    private SpServiceTypeService serviceTypeService;

    @Autowired
    private SpProductService productService;

    @Autowired
    private SpOrderMaterialService orderMaterialService;
    @Autowired
    private SpAbnormalFormService abnormalFormService;
    @Autowired
    private SpOrderOpitionTraceService orderOpitionTraceService;

    @Autowired
    private SpAPPMessagePushService appMessagePushService;

    @Autowired
    private SpOrderService orderService;

    @Autowired
    private OrderService kefuOrderService;

    @Autowired
    private ServicePointOrderOperationDao servicePointOrderOperationDao;

    @Autowired
    private MSServicePointService msServicePointService;


    /**
     * 网点派单给安维人员
     */
    @Transactional()
    public void servicePointPlanOrder(Order order) {
        if (order == null || order.getId() == null) {
            throw new OrderException("派单失败：参数无值。");
        }

        Long servicePointId = order.getOrderCondition().getServicePoint().getId();
        //新派师傅
        Long engineerId = order.getOrderCondition().getEngineer().getId();
        Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
        if (engineer == null) {
            throw new OrderException(String.format("未找到安维:%s的信息", engineer.getName()));
        }
        //User user = UserUtils.getUser();
        User user = order.getCreateBy();
        Order o = null;
        try {
            o = orderCacheReadService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        } catch (Exception e) {
            log.error("[网点分配订单]读取订单缓存错误,orderId:{}",order.getId(),e);
        }

        if (o == null || o.getOrderCondition() == null) {
            throw new OrderException("确认订单信息错误。");
        }

        if (!o.canPlanOrder()) {
            throw new OrderException("该订单不能派单，请刷新页面查看订单是否已取消。");
        }

        //已派师傅
        Long oldEngineerId = o.getOrderCondition().getEngineer().getId();

        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }

        try {
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_PLANNED), "order_status", "已派单");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_PLANNED, label);
            params.put("quarter", o.getQuarter());
            params.put("orderId", order.getId());
            //params.put("operationAppFlag",0);
            params.put("engineer", engineer);
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderDao.updateCondition(params);

            //log,派单
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(o.getQuarter());
            processLog.setAction("派单");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("安维网点派单给安维人员:%s,操作人:%s,备注:%s", engineer.getName(), user.getName(), order.getRemarks()));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(), 255));
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(o.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLog(processLog);
            //更新接单数安维
            //新师傅
            params.put("id", engineer.getId());//新安维帐号
            params.put("planCount", 1);//派单数+1
            params.put("updateBy", user);
            params.put("updateDate", date);
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            engineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务

            //原师傅
            if (oldEngineerId != null) {
                params.remove("id");
                params.put("id", oldEngineerId);//原安维帐号
                params.remove("planCount");
                params.put("planCount", -1);//派单数-1
            }
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            engineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务

            //update order condition
            OrderCondition rediscondition = o.getOrderCondition();
            User engineerUser = new User();
            engineerUser.setId(engineer.getId());
            engineerUser.setName(engineer.getName());
            engineerUser.setMobile(engineer.getContactInfo());//2017/09/21

            rediscondition.setEngineer(engineerUser);
            rediscondition.setUpdateBy(user);
            rediscondition.setUpdateDate(date);

            // 原来的安维人员派单量-1，新的+1
            engineer.setPlanCount(engineer.getPlanCount() + 1);

            //派单记录表 2018/01/24
            Integer nextPlanTimes = orderDao.getOrderPlanMaxTimes(o.getId(), o.getQuarter());
            if (nextPlanTimes == null) {
                nextPlanTimes = 1;
            } else {
                nextPlanTimes++;//+1
            }
            //prev
            OrderPlan preOrderPlan = orderDao.getOrderPlan(o.getId(), o.getQuarter(), servicePointId, oldEngineerId);
            Double serviceCost = 0.0;
            if (preOrderPlan == null) {
                //throw new RuntimeException("读取派单记录错误，请重试");
                serviceCost = orderService.calcServicePointCost(rediscondition,order.getOrderCondition().getServicePoint(), o.getItems());
            } else {
                serviceCost = preOrderPlan.getEstimatedServiceCost();
            }
            OrderPlan orderPlan = orderDao.getOrderPlan(o.getId(), o.getQuarter(), servicePointId, engineer.getId());
            if (orderPlan == null || orderPlan.getId() == null) {
                orderPlan = new OrderPlan();
                //orderPlan.setId(SeqUtils.NextID());
                orderPlan.setQuarter(o.getQuarter());
                orderPlan.setOrderId(o.getId());
                orderPlan.setServicePoint(order.getOrderCondition().getServicePoint());
                orderPlan.setEngineer(engineer);
                orderPlan.setIsMaster(0);//*
                orderPlan.setPlanTimes(nextPlanTimes);//*
                orderPlan.setCreateBy(user);
                orderPlan.setCreateDate(date);
                orderPlan.setUpdateBy(new User(0l));
                //同网点,与前次相同
                if (preOrderPlan != null) {
                    orderPlan.setEstimatedServiceCost(preOrderPlan.getEstimatedServiceCost());
                    orderPlan.setEstimatedDistance(preOrderPlan.getEstimatedDistance());
                    orderPlan.setEstimatedOtherCost(preOrderPlan.getEstimatedOtherCost());
                } else {
                    orderPlan.setEstimatedServiceCost(serviceCost);
                    orderPlan.setEstimatedDistance(0.0);
                    orderPlan.setEstimatedOtherCost(0.0);
                }

                orderDao.insertOrderPlan(orderPlan);
            } else {
                HashMap<String, Object> planMaps = Maps.newHashMap();
                planMaps.put("id", orderPlan.getId());
                planMaps.put("planTimes", nextPlanTimes);
                if (preOrderPlan != null) {
                    planMaps.put("estimatedServiceCost", preOrderPlan.getEstimatedServiceCost());//服务费
                    planMaps.put("estimatedDistance", preOrderPlan.getEstimatedDistance());//距离
                    planMaps.put("estimatedOtherCost", preOrderPlan.getEstimatedOtherCost());//其它费用
                } else {
                    planMaps.put("estimatedServiceCost", serviceCost);//服务费
                    planMaps.put("estimatedDistance", 0.0);//距离
                    planMaps.put("estimatedOtherCost", 0.0);//其它费用
                }
                planMaps.put("updateBy", user);
                planMaps.put("updateDate", date);
                orderDao.UpdateOrderPlan(planMaps);
            }

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .incrVersion(1L)
                    .setCondition(rediscondition)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

            //派单时通知B2B
            b2BCenterOrderService.planOrder(o, engineer, user, date);
            b2BCenterOrderService.servicePointPlanOrder(o, engineer, user, date);

            // 短信通知
            // 发送用户短信
            //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
            List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
            if (!ignoreDataSources.contains(o.getDataSource().getValue()) ) {
                // && order.getSendUserMessageFlag() != null && order.getSendUserMessageFlag() == 1
                if(CollectionUtils.isEmpty(o.getItems())){
                    log.error("[网点分配订单]发送派单短信错误：无订单项,orderId:{}",order.getId());
                }
                StringBuffer userContent = new StringBuffer();
                // 派单后给用户发送短信
                try {
                    if (engineer.getAppFlag() == 0)// 无APP的师傅人工派单给用户短信
                    {
                        // 您好！您有华帝吸油烟机1台需要維修，已为您安排何师傅13396963302。如48小时内未接到电话或在服务过程中有疑问，
                        // 请致电客服黄小姐0757-26169178/400-666-3653（9:00～18:00）。
                        // 祝您生活愉快！
                        // 2019-07-18
                        //您的优盟燃气热水器1台安装，罗师傅18962284455已接单,客服李小姐0757-29235638/4006663653
                        userContent.append("您的");
                        OrderItem item;
                        for (int i = 0, size = o.getItems().size(); i < size; i++) {
                            item = o.getItems().get(i);
                            userContent
                                    .append(item.getBrand())
                                    .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                    .append(item.getQty())
                                    .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                    .append(item.getServiceType().getName())
                                    .append((i == (size - 1)) ? "" : " ");
                        }
                        userContent.append("，");
                        userContent.append(engineer.getName().substring(0, 1));
                        userContent.append("师傅").append(engineer.getContactInfo())
                                .append("已接单，");
                        if (rediscondition.getKefu() != null) {
                            userContent
                                    .append("客服")
                                    .append(rediscondition.getKefu().getName().substring(0, 1)).append("小姐")
                                    .append(rediscondition.getKefu().getPhone())
                                    .append("/");
                        }
                        userContent.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
                        if(StringUtils.isBlank(rediscondition.getServicePhone())){
                            log.error("[网点分配订单]发送派单短信错误：无用户服务电话,orderId:{}",order.getId());
                        }
                        // 使用新的短信发送方法 2019/02/28
                        smsMQSender.sendNew(rediscondition.getServicePhone(),
                                userContent.toString(),
                                "",
                                user.getId(),
                                date.getTime(),
                                SysSMSTypeEnum.ORDER_PLANNED_SERVICE_POINT
                        );
                    } else {
                        //使用过APP的师傅短信  09-27 by kody
                        // 2019-07-18
                        //您的优盟燃气热水器1台安装，罗师傅18962284455已接单,客服李小姐0757-29235638/4006663653
                        userContent.append("您的");
                        OrderItem item;
                        for (int i = 0, size = o.getItems().size(); i < size; i++) {
                            item = o.getItems().get(i);
                            userContent
                                    .append(item.getBrand())
                                    .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                    .append(item.getQty())
                                    .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                    .append(item.getServiceType().getName())
                                    .append((i == (size - 1)) ? "" : " ");
                        }
                        userContent.append("，");
                        userContent.append(engineer.getName().substring(0, 1));
                        userContent.append("师傅").append(engineer.getContactInfo()).append("已接单,");
                        if (rediscondition.getKefu() != null) {
                            userContent
                                    .append("客服")
                                    .append(rediscondition.getKefu().getName().substring(0, 1)).append("小姐")
                                    .append(rediscondition.getKefu().getPhone())
                                    .append("/");
                        }
                        userContent.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
                        if(StringUtils.isBlank(rediscondition.getServicePhone())){
                            log.error("[网点分配订单]发送派单短信错误：无用户服务电话,orderId:{}",order.getId());
                        }
                        // 使用新的短信发送方法 2019/02/28
                        smsMQSender.sendNew(rediscondition.getServicePhone(),
                                userContent.toString(),
                                "",
                                user.getId(),
                                date.getTime(),
                                SysSMSTypeEnum.ORDER_PLANNED_SERVICE_POINT
                        );
                    }
                } catch (Exception e) {
                    log.error("[网点分配订单]发送派单短信错误,mobile:{},content:{}",rediscondition.getServicePhone(),userContent.toString());
                    LogUtils.saveLog(
                            "网点派单-发送短信失败",
                            "OrderService.servicePointPlanOrder",
                            MessageFormat.format("mobile:{0},content:{1},triggerBy:{2},triggerDate:{3}", rediscondition.getServicePhone(), userContent.toString(), user.getId(), date.getTime()),
                            e,
                            user
                    );
                }
            }

            //APP通知 2018/01/10
            try {
                User engieerAccount = systemService.getUserByEngineerId(engineer.getId());// 变更从cashe中取
                if (engieerAccount != null && engieerAccount.getAppLoged() == 1) {
                    //if(engineer != null && engineer.getAppLoged() == 1){
                    // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
                    try {
                        //将推送切换为微服务
                        AppPushMessage pushMessage = new AppPushMessage();
                        pushMessage.setPassThroughType(AppPushMessage.PassThroughType.NOTIFICATION);
                        pushMessage.setMessageType(AppMessageType.PLANORDER);
                        pushMessage.setSubject("");
                        pushMessage.setContent("");
                        pushMessage.setTimestamp(System.currentTimeMillis());
                        pushMessage.setUserId(engieerAccount.getId());
                        pushMessage.setDescription(engieerAccount.getName().substring(0, 1).concat("师傅,有新单派给您，请及时打开APP进行查看处理"));
                        appMessagePushService.sendMessage(pushMessage);

                    } catch (Exception e) {
                        log.error("[OrderService.servicePointPlanOrder]app notice - uid:{}", engineer.getId(), e);
                    }
                }
            } catch (Exception e) {
                log.error("[OrderService.servicePointPlanOrder]app notice - uid:{} ,msg:{}{}", engineer.getId(), engineer.getName().substring(0, 1), "师傅,有新单派给您，请及时打开APP进行查看处理", e);
            }


            servicePointOrderBusinessService.changeEngineer(o.getId(), o.getQuarter(), servicePointId, engineerId, engineer.getMasterFlag(), user.getId(), date.getTime());


        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.servicePointPlanOrder] orderId:{} ,servicePointId:{} ,engineerId:{}", order.getId(), servicePointId, engineerId, e);
            throw new RuntimeException("网点派单错误:" + e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }


    /**
     * 确认上门 （订单必须已经派单或接单）
     * 客服操作，直接自动添加所有的上门服务
     *
     * @param orderId 订单id
     * @param quarter 分片
     * @param user    操作人
     */
    @Transactional()
    public void confirmDoorAuto(Long orderId, String quarter, User user) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("错误：此订单正在处理中，请稍候重试，或刷新订单。");
        }
        try {
            Order o = orderCacheReadService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (o == null) {
                throw new OrderException("错误：读取订单信息失败");
            }
            if (!o.canService()) {
                throw new OrderException("错误：不能确认上门，请确认订单状态.");
            }
            if (o.getOrderCondition().getAppointmentDate() == null) {
                throw new OrderException("错误：没有设置预约时间，不允许直接确认上门.");
            }
            Date date = new Date();
            if (o.getOrderCondition().getAppointmentDate().getTime() > DateUtils.getEndOfDay(date).getTime()) {
                throw new OrderException("预约时间与当前不一致，请重新预约！");
            }
            //2020-09-24 接入云米，增加经纬度检查
            AjaxJsonEntity locationCheckResult = kefuOrderService.checkAddressLocation(o.getDataSourceId(),o.getId(),o.getQuarter());
            if(!locationCheckResult.getSuccess()){
                throw new OrderException(locationCheckResult.getMessage());
            }
            OrderCondition condition = o.getOrderCondition();
            //网点费用表
            Long servicePointId = condition.getServicePoint().getId();
            Long engineerId = condition.getEngineer() == null ? null : condition.getEngineer().getId();
            OrderServicePointFee orderServicePointFee = getOrderServicePointFee(orderId, o.getQuarter(), servicePointId);
            int dataSourceId = o.getDataSourceId();
            int prevStatus = condition.getStatusValue();
            List<OrderDetail> details = o.getDetailList();
            if (details == null) {
                details = Lists.newArrayList();
            }
            //有效的上门服务
            details = details.stream()
                    .filter(t -> t.getDelFlag() == 0)
                    .collect(Collectors.toList());
            HashMap<String, Object> params = Maps.newHashMap();

            // 如果订单中已经有添加当前安维网点的上门服务就不再添加
            // 只记录log
            if (details.size() > 0) {
                OrderDetail detail = details.stream()
                        .filter(t -> t.getDelFlag() == 0
                                && Objects.equals(t.getServicePoint().getId(), condition.getServicePoint().getId())
                        )
                        .findFirst()
                        .orElse(null);
                if (detail != null) {
                    //log
                    OrderProcessLog processLog = new OrderProcessLog();
                    processLog.setQuarter(o.getQuarter());
                    processLog.setAction("确认上门");
                    processLog.setOrderId(orderId);
                    processLog.setActionComment(String.format("%s%s", "安维", "确认上门"));
                    processLog.setStatus(condition.getStatus().getLabel());
                    processLog.setStatusValue(condition.getStatusValue());
                    processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                    processLog.setCloseFlag(0);
                    processLog.setCreateBy(user);
                    processLog.setCreateDate(date);
                    processLog.setCustomerId(condition.getCustomerId());
                    processLog.setDataSourceId(dataSourceId);
                    orderService.saveOrderProcessLog(processLog);

                    //cache,淘汰
                    OrderCacheUtils.delete(orderId);

                    //add by Zhoucy 2018-9-3 19:50 重复确认上门也修改子状态及相关时间
                    params.clear();
                    params.put("quarter", o.getQuarter());
                    params.put("orderId", orderId);
                    /* 一键添加上门服务时：sub_status=50、pending_type_date = reservation_date = now、pending_type = 0， Add by Zhoucy*/
                    params.put("pendingType", new Dict("0", ""));
                    params.put("subStatus", Order.ORDER_SUBSTATUS_SERVICED);
                    params.put("pendingTypeDate", date);
                    params.put("reservationDate", date);
                    params.put("updateBy", user);
                    params.put("updateDate", date);
                    orderDao.updateCondition(params);

                    // 2019-03-25 网点订单数据更新
                    servicePointOrderBusinessService.confirmOnSiteService(orderId, o.getQuarter(), servicePointId, engineerId, prevStatus, Order.ORDER_SUBSTATUS_SERVICED, user.getId(), date.getTime());

                    if (o.getDataSourceId() == B2BDataSourceEnum.VIOMI.id
                            || o.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                        Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                        b2BCenterOrderService.serviceOrder(o, pointId, engineerId, user, date);
                    }
                    return;
                }
            }

            OrderFee orderFee = o.getOrderFee();
            // 确认上门改变订单的状态
            Dict status = new Dict();
            status.setValue(Order.ORDER_STATUS_SERVICED.toString());
            status.setLabel(MSDictUtils.getDictLabel(status.getValue(), "order_status", "已上门"));//切换为微服务

            Boolean firstService = true;//首次上门

            if (details.size() > 0) {
                firstService = false;
            }

            //以下代码，当前网点没有上门过，自动添加上门服务，有可能是二次上门
            //检查当前安维的付款方式
            Dict engineerPaymentType = orderFee.getEngineerPaymentType();
            if (engineerPaymentType == null || engineerPaymentType.getIntValue() <= 0) {
                //throw new OrderException(String.format("订单中安维网点：%s 的付款方式未设定", condition.getServicePoint().getName()));
                ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
                if (servicePoint != null && servicePoint.getFinance() != null
                        && servicePoint.getFinance().getPaymentType() != null
                        && servicePoint.getFinance().getPaymentType().getIntValue() > 0) {
                    engineerPaymentType = servicePoint.getFinance().getPaymentType();
                } else {
                    throw new OrderException(String.format("确认网点：%s 结算方式失败", condition.getServicePoint().getName()));
                }
            }
            Dict orderPaymentType = orderFee.getOrderPaymentType();
            if (orderPaymentType == null || StringUtils.isBlank(orderPaymentType.getValue())) {
                throw new OrderException(String.format("订单中客户：%s 的付款方式未设定", condition.getCustomer().getName()));
            }
            //Customer Price
            List<CustomerPrice> customerPrices = customerService.getPricesFromCache(condition.getCustomer().getId());
            if (customerPrices == null || customerPrices.size() == 0) {
                throw new OrderException(String.format("读取客户：%s价格失败", condition.getCustomer().getName()));
            }
            List<OrderItem> items = o.getItems();
            // ServicePoint Price
            // 2021-05-19 偏远区域处理
            Map<String, ServicePrice> priceMap = null;
           /* RestResult<Boolean> remoteCheckResult = orderService.checkServicePointRemoteArea(condition);
            if(remoteCheckResult.getCode() != ErrorCode.NO_ERROR.code){
                throw new OrderException(new StringJoiner("").add("判断区域是否为偏远区域错误:").add(remoteCheckResult.getMsg()).toString());
            }*/
            List<NameValuePair<Long,Long>> nameValuePairs = getOrderItemProductAndServiceTypePairs(o.getItems());
            if(CollectionUtils.isEmpty(nameValuePairs)){
                throw new OrderException("确认订单服务项目失败");
            }
            /*Boolean isRemoteArea = (Boolean)remoteCheckResult.getData();
            if(isRemoteArea) {
                priceMap = servicePointService.getRemotePriceMapByProductsFromCache(servicePointId, nameValuePairs);
            }else{
                priceMap = servicePointService.getPriceMapByProductsFromCache(servicePointId, nameValuePairs);
            }*/
            priceMap = orderService.getServicePriceFromCacheNew(condition,servicePointId, nameValuePairs);
            if (priceMap == null) {
                throw new OrderException(new StringJoiner("").add("读取网点").add("价格失败,请重试").toString());
            }
            if (CollectionUtils.isEmpty(priceMap)) {
                throw new OrderException(new StringJoiner("").add("读取网点").add("价格失败,未维护网点价格").toString());
            }

            //配件
            //只读取单头
            List<MaterialMaster> materials = orderMaterialService.findMaterialMasterHeadsByOrderId(orderId, o.getQuarter());
            if (materials == null) {
                materials = Lists.newArrayList();
            }

            CustomerPrice cprice;
            ServicePrice eprice;
            List<MaterialMaster> materialMasters = Lists.newArrayList();

            int serviceTimes = condition.getServiceTimes() + 1;//上门次数
            boolean isAddFlag = false;//是否远程费已计费过
            User u = condition.getEngineer();//类型是User,值是md_engineer.id
            Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), u.getId());
            if (engineer == null) {
                throw new OrderException(String.format("读取安维师傅失败，id:%s", u.getId()));
            }
            OrderDetail firstDetail = null;//本次上门服务的第一笔记录
            Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
            if (CollectionUtils.isEmpty(serviceTypeMap)) {
                throw new OrderException("读取服务项目失败。");
            }
            ServiceType st = null;
            int idx = 0;
            for (OrderItem item : o.getItems()) {
                final Product product = item.getProduct();
                final ServiceType serviceType = item.getServiceType();
                cprice = customerPrices.stream()
                        .filter(m -> Objects.equals(m.getProduct().getId(), product.getId()) && Objects.equals(m.getServiceType().getId(), serviceType.getId()))
                        .findFirst().orElse(null);
                if (cprice == null) {
                    throw new OrderException(String.format("未定义产品价格。客户：%s 产品:%s 服务：%s", condition.getCustomer().getName(), product.getName(), serviceType.getName()));
                }
                eprice = priceMap.get(String.format("%d:%d", product.getId(), serviceType.getId()));
                if (eprice == null) {
                    throw new OrderException(String.format("未定义产品价格。网点：%s 产品：%s 服务：%s", condition.getServicePoint().getName(), product.getName(), serviceType.getName()));
                }
                st = serviceTypeMap.get(serviceType.getId());
                if (st == null) {
                    throw new OrderException(String.format("服务项目【%s】读取失败，或不存在", serviceType.getId()));
                }
                OrderDetail detail = new OrderDetail();
                detail.setQuarter(o.getQuarter());
                detail.setEngineerStandPrice(eprice.getPrice());
                detail.setEngineerDiscountPrice(eprice.getDiscountPrice());
                detail.setStandPrice(cprice.getPrice());
                detail.setDiscountPrice(cprice.getDiscountPrice());
                detail.setOrderId(orderId);
                detail.setProduct(item.getProduct());
                detail.setProductSpec(item.getProductSpec());
                detail.setBrand(StringUtils.left(StringUtils.toString(item.getBrand()), 20));//实际上门服务项的品牌只保留前20个字符
                detail.setServiceTimes(serviceTimes);
                detail.setQty(item.getQty());
                detail.setServiceType(item.getServiceType());
                detail.setServiceCategory(new Dict(st.getOrderServiceType(), ""));
                detail.setRemarks("自动添加下单的服务项目");

                //engineer
                detail.setServicePoint(condition.getServicePoint());
                detail.setEngineerPaymentType(engineerPaymentType);
                detail.setEngineer(engineer);

                detail.setCreateBy(user);
                detail.setCreateDate(date);
                detail.setTravelNo("");
                detail.setDelFlag(50 + idx);//new,important,配件使用该值与上门服务关联

                //配件（分两部分 1-已审核，2-未审核） 套组要分拆
                //1.已审核,未关联上门明细的,统计配件费
                //2.未审核的，先关联，再审核时重新计算配件费
                int[] materialStatus = new int[]{2, 3, 4};//2：待发货 3：已发货 4：已完成
                long[] subProducts = new long[]{};//产品
                subProducts = ArrayUtils.add(subProducts, detail.getProduct().getId().longValue());
                //套组，拆分产品
                Product p = productService.getProductByIdFromCache(product.getId());
                if (p.getSetFlag() == 1) {
                    List<Product> products = productService.getProductListOfSet(p.getId());
                    if (products != null && products.size() > 0) {
                        for (Product sp : products) {
                            subProducts = ArrayUtils.add(subProducts, sp.getId().longValue());
                        }
                    }
                }
                final long[] sids = ArrayUtils.clone(subProducts);
                List<MaterialMaster> relateMaterials = null;
                if (materials.size() > 0) {
                    relateMaterials = materials.stream()
                            .filter(
                                    t -> ArrayUtils.contains(materialStatus, Integer.parseInt(t.getStatus().getValue()))
                                            && Objects.equals(t.getOrderDetailId(), 0l)
                                            && ArrayUtils.contains(sids, t.getProductId().longValue())
                            )
                            .collect(Collectors.toList());
                    if (relateMaterials != null && relateMaterials.size() > 0) {
                        for (MaterialMaster m : relateMaterials) {
                            //id,这时候还未产生id,使用delFlag关联,值>=50
                            m.setOrderDetailId(Long.valueOf(detail.getDelFlag().toString()));
                            //应付，+
                            detail.setEngineerMaterialCharge(detail.getEngineerMaterialCharge() + m.getTotalPrice());
                            detail.setEngineerTotalCharge(detail.getEngineerChage());
                            //应收，+
                            detail.setMaterialCharge(detail.getMaterialCharge() + m.getTotalPrice());
                        }
                    }
                }
                //远程费
                if (!isAddFlag) {//预设的远程费用只记入一次
                    isAddFlag = true;
                    //网点
                    detail.setEngineerTravelCharge(orderFee.getPlanTravelCharge());//预设远程费
                    detail.setEngineerOtherCharge(orderFee.getPlanOtherCharge());//预设其他费用
                    detail.setTravelNo(StringUtils.isBlank(orderFee.getPlanTravelNo()) ? "" : orderFee.getPlanTravelNo());//审批单号
                    //厂商
                    detail.setTravelCharge(orderFee.getCustomerPlanTravelCharge());//厂商远程费
                    detail.setOtherCharge(orderFee.getCustomerPlanOtherCharge());//厂商其他费用 2019/03/17
                }
                details.add(detail);
                //配件
                if (relateMaterials != null && relateMaterials.size() > 0) {
                    for (MaterialMaster m : relateMaterials) {
                        m.setOrderDetailId(Long.valueOf(detail.getDelFlag().toString()));//这时候还未产生id,使用delFlag关联,值>=50
                    }
                }

                if (idx == 0) {
                    firstDetail = detail;
                }
                idx++;
            }

            //保险费汇总(负数)
            Double insuranceCharge = orderDao.getTotalOrderInsurance(o.getId(),o.getQuarter());
            if (insuranceCharge == null) {
                insuranceCharge = 0.00;
            }

            //保险单号生效
            OrderInsurance orderInsurance = null;
            boolean insuranceFormEnabled = false;
            orderInsurance = orderDao.getOrderInsuranceByServicePoint(o.getQuarter(), o.getId(), servicePointId);
            if (orderInsurance != null && orderInsurance.getDelFlag() == OrderInsurance.DEL_FLAG_DELETE) {
                insuranceFormEnabled = true;
                orderInsurance.setUpdateBy(user);
                orderInsurance.setUpdateDate(date);
                orderInsurance.setDelFlag(0);
                orderDao.updateOrderInsurance(orderInsurance);
                insuranceCharge = insuranceCharge - orderInsurance.getAmount();//保险启用
            }

            //OrderFee
            orderService.rechargeOrder(details, firstDetail);
            //重新汇总金额
            HashMap<String, Object> feeMap = orderService.recountFee(details);
            //应收
            orderFee.setServiceCharge((Double) feeMap.get("serviceCharge"));
            orderFee.setMaterialCharge((Double) feeMap.get("materialCharge"));
            orderFee.setExpressCharge((Double) feeMap.get("expressCharge"));
            orderFee.setTravelCharge((Double) feeMap.get("travelCharge"));
            orderFee.setOtherCharge((Double) feeMap.get("otherCharge"));
            orderFee.setOrderCharge((Double) feeMap.get("orderCharge"));//以上5项的合计
            //时效费
            //加急费，时效费(快可立补贴&客户补贴) 不需统计，因确认上门只能在客评前操作，因此在对账异常订单处理时不做此操作
            //orderFee.setOrderCharge(orderFee.getOrderCharge()+orderFee.getCustomerTimeLinessCharge());

            //应付
            orderFee.setEngineerServiceCharge((Double) feeMap.get("engineerServiceCharge"));
            orderFee.setEngineerMaterialCharge((Double) feeMap.get("engineerMaterialCharge"));
            orderFee.setEngineerExpressCharge((Double) feeMap.get("engineerExpressCharge"));
            orderFee.setEngineerTravelCharge((Double) feeMap.get("engineerTravelCharge"));
            orderFee.setEngineerOtherCharge((Double) feeMap.get("engineerOtherCharge"));
            orderFee.setEngineerTotalCharge((Double) feeMap.get("engineerTotalCharge"));//合计
            //保险费
            orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + insuranceCharge);
            //加急费，时效费(快可立补贴&客户补贴) 不需统计，因确认上门只能在客评前操作，因此在对账异常订单处理时不做此操作
            //orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + timeLinessCharge + subsidyTimeLinessCharge);//合计

            params.clear();
            //fee
            params.put("orderId", o.getId());
            params.put("quarter", o.getQuarter());
            //应收(客户)
            params.put("serviceCharge", orderFee.getServiceCharge()); //服务费
            params.put("materialCharge", orderFee.getMaterialCharge());// 配件费
            params.put("expressCharge", orderFee.getExpressCharge()); // 快递费
            params.put("travelCharge", orderFee.getTravelCharge()); //远程费
            params.put("otherCharge", orderFee.getOtherCharge());//其他費用
            params.put("orderCharge", orderFee.getOrderCharge());//合计

            //应付(安维)
            params.put("engineerServiceCharge", orderFee.getEngineerServiceCharge());//服务费
            params.put("engineerMaterialCharge", orderFee.getEngineerMaterialCharge());//配件费
            params.put("engineerExpressCharge", orderFee.getEngineerExpressCharge());//快递费
            params.put("engineerTravelCharge", orderFee.getEngineerTravelCharge());//远程费
            params.put("engineerOtherCharge", orderFee.getEngineerOtherCharge());//其它费用
            params.put("insuranceCharge", insuranceCharge);//保险费用(负数，扣减)
            //合计=其他费用合计-保险费
            params.put("engineerTotalCharge", orderFee.getEngineerTotalCharge());
            orderDao.updateFee(params);

            //condition
            condition.setServiceTimes(serviceTimes);
            //已派单 -> 已上门
            if (condition.getStatusValue() == Order.ORDER_STATUS_PLANNED) {
                condition.setStatus(status);
            } else {
                status = condition.getStatus();
            }

            params.clear();
            params.put("quarter", o.getQuarter());
            params.put("status", status);
            params.put("orderId", orderId);
            params.put("serviceTimes", serviceTimes);
            /* 一键添加上门服务时：sub_status=50、pending_type_date = reservation_date = now、pending_type = 0， Add by Zhoucy*/
            params.put("pendingType", new Dict("0", ""));
            params.put("subStatus", Order.ORDER_SUBSTATUS_SERVICED);
            params.put("pendingTypeDate", date);
            params.put("reservationDate", date);
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderDao.updateCondition(params);

            //status
            if (firstService) {
                OrderStatus orderStatus = o.getOrderStatus();
                orderStatus.setServiceFlag(1);
                orderStatus.setServiceDate(date);
                orderStatus.setServiceTimes(serviceTimes);
                params.clear();
                params.put("quarter", o.getQuarter());
                params.put("orderId", o.getId());
                params.put("serviceFlag", 1);
                params.put("serviceDate", date);
                params.put("serviceTimes", serviceTimes);
                orderDao.updateStatus(params);
            }

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(o.getQuarter());
            processLog.setAction("确认上门");
            processLog.setOrderId(orderId);
            processLog.setActionComment(String.format("%s%s", "安维", "确认上门"));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(dataSourceId);
            orderService.saveOrderProcessLog(processLog);
            //details
            OrderDetail model;
            MDErrorType errorType = null;
            MDErrorCode errorCode = null;
            MDActionCodeDto actionCode = null;
            boolean isnull;
            for (int i = 0, size = details.size(); i < size; i++) {
                model = details.get(i);
                if (model.getDelFlag() == OrderDetail.DEL_FLAG_DELETE) {
                    continue;
                }
                if (model.getId() == null || model.getId() <= 0) {
                    //log
                    processLog = new OrderProcessLog();
                    processLog.setQuarter(o.getQuarter());
                    processLog.setAction("上门服务:添加订单具体服务项目");
                    processLog.setOrderId(orderId);
                    // 2019-12-27 统一上门服务跟踪进度格式
                    //processLog.setActionComment(String.format("上门服务:添加订单具体服务项目:%s,产品:%s", model.getServiceType().getName(), model.getProduct().getName()));
                    if (StringUtils.isBlank(model.getOtherActionRemark())) {
                        processLog.setActionComment(String.format("%s【%s】", model.getServiceType().getName(), model.getProduct().getName()));
                    } else {
                        processLog.setActionComment(String.format("%s【%s】其他故障:【%s】", model.getServiceType().getName(), model.getProduct().getName(), model.getOtherActionRemark()));
                    }
                    processLog.setActionComment(StringUtils.left(processLog.getActionComment(), 255));
                    processLog.setStatus(condition.getStatus().getLabel());
                    processLog.setStatusValue(condition.getStatusValue());
                    if (firstService) {
                        processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                    } else {
                        processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
                    }
                    processLog.setCloseFlag(0);
                    processLog.setCreateBy(user);
                    processLog.setCreateDate(date);
                    processLog.setCustomerId(condition.getCustomerId());
                    processLog.setDataSourceId(dataSourceId);
                    orderService.saveOrderProcessLog(processLog);
                    //insert
                    model.setQuarter(o.getQuarter());
                    Long delFalg = Long.valueOf(model.getDelFlag().toString());//*
                    model.setDelFlag(0);//* 还原
                    if (model.getServiceCategory() == null || model.getServiceCategory().getIntValue() == 0) {
                        //调用方未设定，以下单时的工单类型为准
                        model.setServiceCategory(new Dict(condition.getOrderServiceType(), ""));
                    }
                    if (model.getErrorType() == null || model.getErrorType().getId() == null) {
                        if (errorType == null) {
                            errorType = new MDErrorType();
                            errorType.setId(0L);
                        }
                        model.setErrorType(errorType);
                    }
                    if (model.getErrorCode() == null || model.getErrorCode().getId() == null) {
                        if (errorCode == null) {
                            errorCode = new MDErrorCode();
                            errorCode.setId(0L);
                        }
                        model.setErrorCode(errorCode);
                    }
                    isnull = false;
                    if (model.getActionCode() == null) {
                        isnull = true;
                    }
                    if (isnull || model.getActionCode().getId() == null) {
                        if (actionCode == null) {
                            actionCode = new MDActionCodeDto();
                            actionCode.setId(0L);
                            if (isnull) {
                                actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                            } else {
                                if (StringUtils.isBlank(model.getActionCode().getName())) {
                                    actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                                } else {
                                    actionCode.setName(model.getActionCode().getName());
                                }
                            }

                        }
                        model.setActionCode(actionCode);
                    }
                    model.setOtherActionRemark(StringUtils.trimToEmpty(model.getOtherActionRemark()));
                    orderDao.insertDetail(model);
                    //配件
                    materialMasters = materials.stream().filter(t -> Objects.equals(t.getOrderDetailId(), delFalg)).collect(Collectors.toList());
                    for (MaterialMaster m : materialMasters) {
                        m.setOrderDetailId(model.getId());
                        params.clear();
                        params.put("id", m.getId());
                        params.put("quarter", o.getQuarter());//*
                        params.put("orderDetailId", model.getId());//*
                        //以下两个字段只有状态变更才更新
                        //params.put("updateBy", user);
                        //params.put("updateDate", date);
                        orderMaterialService.updateMaterialMaster(params);
                    }
                }
            }

            /* 安维确认上门 */
            params.clear();
            params.put("quarter", o.getQuarter());
            params.put("id", orderId);
            params.put("confirmDoor", 1);
            orderHeadDao.updateOrder(params);//2020-12-03 sd_order -> sd_order_head

            //OrderServicePointFee 生效并汇总
            OrderDetail servicePointFeeSum = null;
            if (orderServicePointFee != null) {
                servicePointFeeSum = details.stream().filter(t -> t.getServicePoint().getId().longValue() == servicePointId.longValue() && t.getDelFlag() != OrderDetail.DEL_FLAG_DELETE)
                        .reduce(new OrderDetail(), (item1, item2) -> {
                            return new OrderDetail(
                                    item1.getEngineerServiceCharge() + item2.getEngineerServiceCharge(),
                                    item1.getEngineerTravelCharge() + item2.getEngineerTravelCharge(),
                                    item1.getEngineerExpressCharge() + item2.getEngineerExpressCharge(),
                                    item1.getEngineerMaterialCharge() + item2.getEngineerMaterialCharge(),
                                    item1.getEngineerOtherCharge() + item2.getEngineerOtherCharge()
                            );
                        });
            }
            params.clear();
            params.put("orderId", o.getId());
            params.put("quarter", o.getQuarter());
            params.put("servicePointId", servicePointId);
            params.put("delFlag", 0);
            //费用汇总
            if (orderServicePointFee != null && servicePointFeeSum != null) {
                params.put("serviceCharge", servicePointFeeSum.getEngineerServiceCharge());
                params.put("travelCharge", servicePointFeeSum.getEngineerTravelCharge());
                params.put("expressCharge", servicePointFeeSum.getEngineerExpressCharge());
                params.put("materialCharge", servicePointFeeSum.getEngineerMaterialCharge());
                params.put("otherCharge", servicePointFeeSum.getEngineerOtherCharge());
                //2021-03-04 首次派单，网点保险开关关闭，再次派单时，网点保险开关开启情况，上门服务时补偿处理
                if(insuranceFormEnabled && orderServicePointFee.getInsuranceCharge() == 0.00){
                    params.put("insuranceCharge",0-orderInsurance.getAmount());
                    orderServicePointFee.setInsuranceCharge(0-orderInsurance.getAmount());//保证后面计算没有问题
                    params.put("insuranceNo",orderInsurance.getInsuranceNo());
                }else {
                    params.put("insuranceCharge", orderServicePointFee.getInsuranceCharge());
                }
                params.put("timeLinessCharge", orderServicePointFee.getTimeLinessCharge());
                params.put("customerTimeLinessCharge", orderServicePointFee.getCustomerTimeLinessCharge());
                params.put("urgentCharge", orderServicePointFee.getUrgentCharge());
                //汇总
                Double engineerTotalCharge = servicePointFeeSum.getEngineerServiceCharge()
                        + servicePointFeeSum.getEngineerTravelCharge()
                        + servicePointFeeSum.getEngineerExpressCharge()
                        + servicePointFeeSum.getEngineerMaterialCharge()
                        + servicePointFeeSum.getEngineerOtherCharge()
                        + orderServicePointFee.getInsuranceCharge()
                        + orderServicePointFee.getTimeLinessCharge()
                        + orderServicePointFee.getCustomerTimeLinessCharge()
                        + orderServicePointFee.getUrgentCharge();
                params.put("orderCharge", engineerTotalCharge);
            }
            orderDao.updateOrderServicePointFeeByMaps(params);

            //cache
            OrderCacheUtils.setDetailActionFlag(orderId);
            OrderCacheUtils.delete(orderId);


            if (prevStatus == Order.ORDER_STATUS_PLANNED  || o.getDataSourceId() == B2BDataSourceEnum.VIOMI.id
                    || o.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                b2BCenterOrderService.serviceOrder(o, pointId, engineerId, user, date);
            }

            servicePointOrderBusinessService.confirmOnSiteService(orderId, o.getQuarter(), servicePointId, engineerId, status.getIntValue(), Order.ORDER_SUBSTATUS_SERVICED, user.getId(), date.getTime());


        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.confirmDoorAuto] orderId:{}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 网点停滞
     */
    @Transactional()
    public void servicePointPendingOrder(Long orderId, String quarter, Dict pendingType, Date appointmentDate, String remarks, User user) {
        Order order = orderCacheReadService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取工单信息失败");
        }
        OrderCondition condition = order.getOrderCondition();
        if (condition.getServicePoint() == null || StringUtils.toLong(condition.getServicePoint().getId()) <= 0
                || condition.getEngineer() == null || StringUtils.toLong(condition.getEngineer().getId()) <= 0) {
            throw new RuntimeException("读取网点及师傅信息失败");
        }
        Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
        if (null == engineer) {
            throw new RuntimeException("读取师傅信息失败");
        }
        //若当前预约时间与前一次预约时间一样，则不需要通知B2B
        boolean isNeedSendToB2B = true;
        if (condition.getAppointmentDate() != null && condition.getAppointmentDate().getTime() == appointmentDate.getTime()) {
            isNeedSendToB2B = false;
        }

        StringBuffer sbAppointDate = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(appointmentDate.getTime());
        sbAppointDate.append(calendar.get(Calendar.MONTH) + 1).append("月").append(calendar.get(Calendar.DAY_OF_MONTH)).append("日");
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 13) {
            sbAppointDate.append(" 上午");
        } else if (hour < 18) {
            sbAppointDate.append(" 下午");
        } else {
            sbAppointDate.append(" 晚上");
        }
        StringBuilder remarksBuffer = new StringBuilder();
        remarksBuffer.append("安维预约上门:")
                .append(sbAppointDate.toString())
                .append(",")
                .append(pendingType.getLabel());
        if (StringUtils.isNotBlank(remarks)) {
            remarksBuffer.append(",").append(StringUtils.substring(order.getRemarks().trim(), 0, 100));
        }

        int pendingTypeValue = pendingType.getIntValue();
        String time = "";
        // 时间取整点时间
        if (DateUtils.getYear(appointmentDate) > 9999) {
            throw new OrderException("日期超出范围");
        }
        time = DateUtils.formatDate(appointmentDate, "yyyy-MM-dd HH:00:00");
        try {
            Date date = DateUtils.parse(time, "yyyy-MM-dd HH:00:00");
            condition.setAppointmentDate(date);
        } catch (java.text.ParseException e) {
            throw new OrderException("日期格式错误");
        }

        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, condition.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Date date = new Date();
            //log
            OrderProcessLog log = new OrderProcessLog();
            log.setQuarter(order.getQuarter());
            log.setAction("预约上门");
            log.setOrderId(condition.getOrderId());
            log.setActionComment(StringUtils.left(remarksBuffer.toString(), 250));
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(condition.getStatusValue());
            log.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            log.setCloseFlag(0);
            log.setCreateBy(user);
            log.setCreateDate(date);
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLog(log);

            HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(15);
            //condition
            params.put("quarter", order.getQuarter());
            params.put("orderId", condition.getOrderId());
            //预约
            boolean isReservation = (pendingTypeValue == Order.PENDINGTYPE_APPOINTED);
            Date pendingTypeDate = appointmentDate;
            //如果预约时间在22点及以后，则将客服处理时间提前2小时（因为等工单预约到期时，客服已经下班了） Added by zhoucy
            if (DateUtils.getHourOfDay(pendingTypeDate) >= 22) {
                pendingTypeDate = DateUtils.addHour(pendingTypeDate, -2);
            }
            Date reservationDate = appointmentDate;
            Integer subStatus = Order.ORDER_SUBSTATUS_NEW;
            Dict pendingTypeDict = pendingType;
            if (pendingTypeValue == Order.PENDINGTYPE_FOLLOWING) {
                subStatus = null;
                pendingTypeDict = null;
                appointmentDate = null;
                reservationDate = null;
            } else if (pendingTypeValue == Order.PENDINGTYPE_APPOINTED) {
                subStatus = Order.ORDER_SUBSTATUS_APPOINTED;
                params.put("reservationTimes", 1);
            } else if (pendingTypeValue == Order.PENDINGTYPE_WAITINGPARTS) {
                subStatus = Order.ORDER_SUBSTATUS_WAITINGPARTS;
                params.put("reservationTimes", 1);
            } else {
                subStatus = Order.ORDER_SUBSTATUS_PENDING;
                params.put("reservationTimes", 1);
            }

            params.put("subStatus", subStatus);
            params.put("pendingType", pendingTypeDict);
            params.put("pendingTypeDate", pendingTypeDate);
            params.put("appointmentDate", appointmentDate);
            params.put("reservationDate", reservationDate);
            params.put("updateBy", user);
            params.put("updateDate", date);
            int appAbnormalyFlag = condition.getAppAbnormalyFlag();
            if (1 == appAbnormalyFlag) {
                params.put("appAbnormalyFlag", appAbnormalyFlag);
            }
            orderDao.updateCondition(params);
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(condition.getOrderId())
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.PENDING_TYPE)
                    .setDeleteField(OrderCacheField.PENDING_TYPE_DATE);
            if (pendingTypeValue != Order.PENDINGTYPE_FOLLOWING) {
                builder.setDeleteField(OrderCacheField.RESERVATION_TIMES)
                        .setDeleteField(OrderCacheField.APPOINTMENT_DATE);
                if (isReservation) {
                    builder.setDeleteField(OrderCacheField.RESERVATION_DATE);
                }
            }
            OrderCacheUtils.update(builder.build());

            Long servicePointId = condition.getServicePoint() == null ? null : condition.getServicePoint().getId();

            //从派单处移到此处
            if (order.getDataSource() != null && pendingTypeValue != Order.PENDINGTYPE_FOLLOWING && isNeedSendToB2B) {
                Date effectiveDate = appointmentDate;
                //APP的预约时间若在22点及以后，则发给B2B的预约时间增加18个小时
                //status -> 3
                Long engineerId = condition.getEngineer() == null ? null : condition.getEngineer().getId();
                b2BCenterOrderService.pendingOrder(order, servicePointId, engineerId, pendingTypeValue, effectiveDate, user, date, remarks);
            }

            servicePointOrderBusinessService.pending(condition.getOrderId(), order.getQuarter(),
                    servicePointId, subStatus, pendingTypeDict.getIntValue(),
                    appointmentDate.getTime(), reservationDate.getTime(), appAbnormalyFlag,
                    user.getId(), date.getTime());

            //TODO: 预约日期&等通知，不发短信
//            if (pendingTypeValue != Order.PENDINGTYPE_APPOINTED && pendingTypeValue != Order.PENDINGTYPE_WATTINGNOTICE) {
//                //检查客户短信发送开关，1:才发送 2020-01-06
//                Customer customer = customerService.getFromCache(order.getOrderCondition().getCustomer().getId());
//                //发送短信 1.未取道客户信息 2.取道，且短信发送标记为：1
//                //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
//                List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
//                if (!ignoreDataSources.contains(order.getDataSource().getValue()) && (customer == null || (customer != null && customer.getShortMessageFlag() == 1))) {
//                    StringBuffer strContent = new StringBuffer();
//                    strContent.append("您的售后工单,");
//                    strContent.append("将由");
//                    strContent.append(engineer.getName().substring(0, 1) + "师傅 ")
//                            .append(engineer.getContactInfo())
//                            .append("上门服务");
//                    strContent.append(",预约时间为" + sbAppointDate.toString());
//                    strContent.append(",如有疑问,请致电客服");
//                    // 使用新的短信发送方法 2019/02/28
//                    smsMQSender.sendNew(condition.getServicePhone(), strContent.toString(), "", user.getId(), date.getTime(), SysSMSTypeEnum.ORDER_PENDING_APP);
//                }
//            }
            //短信 原代码提炼为方法
            sendAppPendingSMS(order.getDataSource().getIntValue(), pendingType.getIntValue(), condition.getServicePhone(), user.getId(), date.getTime());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.appPendingOrder]orderId:{} ,pendintType:{} ,date:{} ,remarks:{}", order.getId(), pendingType.getLabel(), time, remarksBuffer.toString(), e);
            throw new OrderException("保存错误", e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 工单完成(网点)
     */
    @Transactional()
    public void servicePointCompleteOrder(OrderCondition pCondition, User user, Dict completeType, String remarks) {
        Order order = orderCacheReadService.getOrderById(pCondition.getOrderId(), pCondition.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("错误：读取工单信息失败");
        }
        OrderCondition condition = order.getOrderCondition();
        if (condition.getServicePoint() == null || condition.getEngineer() == null) {
            throw new RuntimeException("读取师傅信息失败");
        }
        Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
        if (null == engineer) {
            throw new RuntimeException("读取师傅信息失败");
        }
        final Long engineerId = engineer.getId();
        List<OrderDetail> details = order.getDetailList();
        if (details == null || details.size() == 0) {
            throw new RuntimeException(ErrorCode.ORDER_CAN_NOT_COMPLETE.message);
        }
        OrderDetail detail = details.stream().filter(t -> t.getEngineer().getId().equals(engineerId)).findFirst().orElse(null);
        if (detail == null) {
            throw new RuntimeException(ErrorCode.ORDER_CAN_NOT_COMPLETE.message);
        }
        if (condition.getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED.intValue()) {
            throw new RuntimeException(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.message);
        }
        int orderStatusValue = condition.getStatusValue();
        if (orderStatusValue == Order.ORDER_STATUS_APP_COMPLETED || orderStatusValue == Order.ORDER_STATUS_COMPLETED || orderStatusValue == Order.ORDER_STATUS_CHARGED) {
            throw new RuntimeException(ErrorCode.ORDER_FINISH_SERVICE.message);
        } else if (orderStatusValue > Order.ORDER_STATUS_CHARGED) {
            throw new RuntimeException("订单已取消或已退单");
        }
        String lockkey = null;//锁
        Boolean locked = false;
        Boolean autoCompleteFlag = true;//自动完工标记
        try {
            Date date = new Date();
            lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
            //获得锁
            locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
            if (!locked) {
                throw new RuntimeException(ErrorCode.ORDER_REDIS_LOCKED.message);
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append("orderNo:").append(order.getOrderNo());
            buffer.append(" completeType:").append(completeType.getValue());

            //检查状态
            int statusValue = condition.getStatusValue();
            if (statusValue != Order.ORDER_STATUS_SERVICED) {
                throw new RuntimeException(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.message);
            } else {
                //TODO: APP完工[55]
                Dict appCompletedStatus =  MSDictUtils.getDictByValue(Order.ORDER_STATUS_APP_COMPLETED.toString(), Dict.DICT_TYPE_ORDER_STATUS);
                condition.setStatus(appCompletedStatus);
            }
            //检查未审核或未发货配件申请单
            // 根据订单配件状态检查是否可以客评 2019/06/13 22:56 at home
            MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(), order.getId(), order.getQuarter());
            if (!MSResponse.isSuccessCode(msResponse)) {
                throw new RuntimeException("订单完成失败，" + msResponse.getMsg());
            }
            //检查客户要求完成照片数量
            Customer customer = customerService.getFromCache(order.getOrderCondition().getCustomer().getId());
            if (customer == null) {
                throw new RuntimeException("客户不存在，或读取客户信息失败");
            }
            if (customer.getMinUploadNumber() > 0 && order.getOrderCondition().getFinishPhotoQty() < customer.getMinUploadNumber()) {
                throw new RuntimeException("请先上传客户要求的最少服务效果图");
            }
            if (!orderService.checkOrderProductBarCode(order.getId(), order.getQuarter(), order.getOrderCondition().getCustomer().getId(), order.getDetailList())) {
                throw new RuntimeException("请先上传产品条码");
            }

            Integer orgAppAbnormaly = condition.getAppAbnormalyFlag();//如果原来异常，本次也异常，不需要累加异常数量
            buffer.append(" orgAppAbnormaly:").append(orgAppAbnormaly.toString());
            //完成类型
            Integer appAbnormalyFlag = 0;
            //2021/05/25 增加客户自动完工开关判断
            int customerAutoCompleteOrderFlag = Optional.ofNullable(customer).map(t->t.getAutoCompleteOrder()).orElse(0);
            if(customerAutoCompleteOrderFlag == 0){
                //客户自动完工开关未开启
                autoCompleteFlag = false;
                buffer.append(" 客户自动完工开关:关闭");
            } else if (1 == orgAppAbnormaly) {//订单已经app异常，改成短信回复客评
                autoCompleteFlag = false;
            } else if (!"compeled_all".equalsIgnoreCase(completeType.getValue()) && !"compeled_all_notest".equalsIgnoreCase(completeType.getValue())) {
                //已完成工单全部内容但未试机 不标记异常  2020-01-07
                autoCompleteFlag = false;
                appAbnormalyFlag = 1;
            }
            buffer.append(" appAbnormalyFlag:").append(appAbnormalyFlag.toString());
            if (true == autoCompleteFlag) {
                String checkResult = orderService.checkAutoComplete(order);
                buffer.append(" checkResult:").append(checkResult);
                if (StringUtils.isBlank(checkResult)) {
                    //网点自动完工检查
                    List<Long> points = order.getDetailList().stream()
                            .map(t -> t.getServicePoint().getId())
                            .distinct()
                            .collect(Collectors.toList());
                    ServicePoint servicePoint;
                    buffer.append(" servicepoint ids:");
                    if (null == points || 0 == points.size()) {
                        autoCompleteFlag = false;
                        buffer.append("[]{}");
                    } else {
                        buffer.append("[").append(StringUtils.join(points, ",")).append("]{");
                        for (int i = 0, size = points.size(); i < size; i++) {
                            servicePoint = servicePointService.getFromCache(points.get(i));
                            if (servicePoint == null) {
                                autoCompleteFlag = false;
                                buffer.append(" servicepointId:").append(points.get(i).toString()).append(" null get from redis");
                                buffer.append(" ,autoCompleteFlag:").append(autoCompleteFlag.toString());
                                break;
                            } else if (servicePoint.getAutoCompleteOrder() == 0) {
                                autoCompleteFlag = false;
                                buffer.append(" servicepointId:").append(points.get(i).toString()).append(" ,AutoCompleteOrder:").append(new Integer(servicePoint.getAutoCompleteOrder()).toString());
                                buffer.append(" ,autoCompleteFlag:").append(autoCompleteFlag.toString());
                                break;
                            }
                        }
                        buffer.append("}");
                    }
                } else {
                    autoCompleteFlag = false;
                }
            }

            //endregion check

            HashMap<String, Object> params = Maps.newHashMap();
            condition.setPendingFlag(2);//正常
            condition.setPendingType(new Dict(0, ""));
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("appCompleteType", completeType.getValue().trim());//完工类型
            params.put("appCompleteDate", date);//完工日期

            params.put("pendingFlag", condition.getPendingFlag());
            params.put("pendingType", condition.getPendingType());
            if (1 == appAbnormalyFlag) {
                condition.setAppAbnormalyFlag(appAbnormalyFlag);//app异常
                params.put("appAbnormalyFlag", appAbnormalyFlag);
            }
            params.put("subStatus", Order.ORDER_SUBSTATUS_APPCOMPLETED);//Add by Zhoucy
            params.put("status", condition.getStatus());
            orderDao.updateCondition(params);
            if (1 == appAbnormalyFlag) {
                //意见跟踪日志
                OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                        .channel(AppFeedbackEnum.Channel.APP.getValue())
                        .quarter(order.getQuarter())
                        .orderId(order.getId())
                        .servicePointId(condition.getServicePoint().getId())
                        .appointmentAt(0)
                        .opinionId(0)
                        .parentId(0)
                        .opinionType(AppFeedbackEnum.FeedbackType.APP_COMPLETE.getValue())
                        .opinionValue(0)
                        .opinionLabel("App完工不符合自动完工条件，标记为异常")
                        .isAbnormaly(1)
                        .remark(StringUtils.left(completeType.getValue() + ": " + remarks, 250))
                        .createAt(System.currentTimeMillis())
                        .createBy(user)
                        .times(1)
                        .totalTimes(1)
                        .build();
                orderOpitionTraceService.insert(opitionTrace);

                //异常单
                Integer subType = 0;
                if (condition.getOrderServiceType() == 1) {
                    subType = AbnormalFormEnum.SubType.INSTALL_ERROR.code;
                } else {
                    subType = AbnormalFormEnum.SubType.REPAIR_ERROR.code;
                }
                String reason = completeType.getLabel() + ": " + remarks;
                AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order, reason, user, AppFeedbackEnum.Channel.APP.getValue(),
                        AbnormalFormEnum.FormType.APP_COMPLETE.code, subType, "App完工不符合自动完工条件");
                try {
                    if (abnormalForm != null) {
                        abnormalForm.setOpinionLogId(opitionTrace.getId());
                        abnormalFormService.save(abnormalForm);
                    }
                } catch (Exception e) {
                    log.error("[OrderService.SaveOrderComplete]app完工保存异常单失败 form:{}", GsonUtils.getInstance().toGson(abnormalForm), e);
                }
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("安维完成");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("%s,备注:%s", completeType.getLabel(), remarks));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(), 255));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setRemarks("");
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLog(processLog);

            //更新未完工单数
            ServicePoint servicepoint = condition.getServicePoint();
            if(servicepoint!=null && servicepoint.getId()!=null && servicepoint.getId()>0){
                kefuOrderService.updateServicePointUnfinishedOrderCount(servicepoint.getId(),-1,"网点完工",order.getId(),user);
            }

            //增加检查客户短信发送开关，1:才发送 2018/04/12
            //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
            List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
            if (1 == customer.getShortMessageFlag()
                    && 0 == appAbnormalyFlag
                    && false == autoCompleteFlag
                    && completeType.getValue().equalsIgnoreCase("compeled_all")
                    && !ignoreDataSources.contains(order.getDataSource().getValue())
            ) {
                buffer.append(" 发送客户客评短信");
                // 尊敬的用户，您的售后维修工单已经由张三师傅完成，请您直接回复数字对师傅的服务进行评价：
                // 1 非常满意 2 一般 3 不满意，谢谢您的支持！祝您生活愉快！
                StringBuffer strContent = new StringBuffer();
                //strContent.append("您的售后工单已完成，请回复数字对师傅的服务进行评价：1非常满意 ,2一般, 3不满意,4还有产品未完成，谢谢您的支持！");//old
                strContent.append("您的服务已完成，请回复数字对师傅评价：1满意 2一般 3不满意。您的差评，我们将考核师傅500元并停单培训一周，感谢您对服务的监督");//2019/06/03
                smsCallbackTaskMQSender.send(condition.getOrderId(), order.getQuarter(), condition.getServicePhone(), strContent.toString(), "", null, "", user.getId(), date.getTime());
            }

            //2019/01/18 更改：只要app完成就发语音回访
            //2019/01/21 更改：不自动完成的才发语音回访
            //2019/04/13 更改：有重单的不发语音回访
            if (voiceEnabled && !autoCompleteFlag && StringUtils.isNoneBlank(siteCode)
                    && StringUtils.isBlank(order.getRepeateNo())) {
                orderService.sendNewVoiceTaskMessage(siteCode, order, user.getName(), date);
            }

            if (1 == appAbnormalyFlag && 0 == orgAppAbnormaly) {
                buffer.append(" 发送异常统计消息");
                orderService.sendNoticeMessage(
                        NoticeMessageConfig.NOTICE_TYPE_APPABNORMALY,
                        order.getId(),
                        order.getQuarter(),
                        customer,
                        condition.getKefu(),
                        condition.getArea().getId(),
                        user,
                        date
                );
            }


            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.PENDING_TYPE)
                    .setDeleteField(OrderCacheField.PENDING_TYPE_DATE);
            OrderCacheUtils.update(builder.build());

            if (true == autoCompleteFlag) {
                buffer.append(" 发送自动完工消息");
                //自动完工调用saveGrade，因此此处不需要发送B2B订单状态变更消息
                orderService.sendAutoCompleteMessage(order.getId(), order.getQuarter(), user, date);
            }

            servicePointOrderBusinessService.appComplete(
                    order.getId(),
                    order.getQuarter(),
                    Order.ORDER_SUBSTATUS_APPCOMPLETED,
                    completeType.getValue(),
                    appAbnormalyFlag,
                    user.getId(),
                    date.getTime()
            );

            log.info("app自动完工:{}", buffer.toString());
            if (false == autoCompleteFlag) {
                LogUtils.saveLog("app自动完工", "OrderService.SaveOrderComplete", buffer.toString(), null, null);
            }
        } catch (OrderException oe) {
            log.error("[OrderService.SaveOrderComplete] orderId:{}", order.getId(), oe);
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.SaveOrderComplete] orderId:{}", order.getId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
        }
    }

    /**
     * 返回APP需要的日志
     */
    public List<OrderProcessLog> getAppOrderLogs(Long orderId, String quarter) {
        List<OrderProcessLog> list = orderDao.getAppOrderLogs(orderId, quarter);
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().filter(i -> i.getCreateBy() != null && i.getCreateBy().getId() != null)
                    .map(i -> i.getCreateBy().getId()).distinct().collect(Collectors.toList());
            Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(userIds);
            if (!nameMap.isEmpty()) {
                list.stream().forEach(i -> {
                    if (i.getCreateBy() != null && i.getCreateBy().getId() != null) {
                        i.getCreateBy().setName(StringUtils.toString(nameMap.get(i.getCreateBy().getId())));
                    }
                });
            }
        }
        return list;
    }

    public List<OrderPlan> getOrderPlanList(Long orderId, String quarter, Integer isMaster) {
        return orderDao.getOrderPlanList(orderId, quarter, isMaster);
    }

    /**
     * 保存进度跟踪记录
     */
    @Transactional()
    public OrderProcessLog saveTracking(Order order) {
        User user = order.getCreateBy();
        Date date = order.getCreateDate();
        Long orderId = order.getId();
        String remarks = order.getRemarks();
        Integer isCustomerSame = order.getIsCustomerSame();

        String quarter = order.getQuarter();
        order = orderCacheReadService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单信息失败");
        }
        if (!order.canTracking()) {
            throw new OrderException("该订单不能进度跟踪，请刷新页面查看订单订单状态。");
        }
        OrderCondition condition = order.getOrderCondition();
        //log,派单
        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setQuarter(order.getQuarter());
        processLog.setAction("进度跟踪");
        processLog.setOrderId(orderId);
        processLog.setActionComment(String.format("%s %s", DateUtils.formatDate(new Date(), "MM-dd"), remarks));
        processLog.setStatus(condition.getStatus().getLabel());
        processLog.setStatusValue(condition.getStatusValue());
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);
        processLog.setCloseFlag(2);
        processLog.setCreateBy(user);
        processLog.setCreateDate(date);

        //客服详情界面：跟踪进度，客户不可见时，网点同样不可见，2019-4-17
        //网点添加跟踪进度：网点可见
        int visibilityValue = OrderUtils.calcProcessLogVisibilityFlag(processLog);
        if (isCustomerSame == 1) {
            visibilityValue = VisibilityFlagEnum.add(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.CUSTOMER, VisibilityFlagEnum.SERVICE_POINT));
        } else {
            visibilityValue = VisibilityFlagEnum.subtract(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.CUSTOMER, VisibilityFlagEnum.SERVICE_POINT));
        }
        visibilityValue = VisibilityFlagEnum.add(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.SERVICE_POINT));
        processLog.setVisibilityFlag(visibilityValue);
        processLog.setCustomerId(condition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);

//        condition.setTrackingFlag(1);
        condition.setTrackingFlag(visibilityValue);
        condition.setTrackingDate(processLog.getCreateDate());
        condition.setTrackingMessage(processLog.getActionComment());
        condition.setUpdateBy(processLog.getCreateBy());
        condition.setUpdateDate(processLog.getCreateDate());
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("quarter", order.getQuarter());
        params.put("orderId", orderId);
        params.put("trackingFlag", visibilityValue);
        params.put("trackingDate", processLog.getCreateDate());
        params.put("trackingMessage", processLog.getActionComment());

        orderDao.updateCondition(params);
        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(orderId)
                .incrVersion(1L)
                .setCondition(condition)
                .setTrackingFlag(1)
                .setTrackingDate(processLog.getCreateDate())
                .setTrackingMessage(processLog.getActionComment())
                .setExpireSeconds(0L);
        OrderCacheUtils.update(builder.build());
        return processLog;
    }


    /**
     * 更新催单信息
     */
    @Transactional
    public int updateReminderInfo(HashMap<String, Object> map) {
        orderDao.updateReminderInfo(map);
        int rtn = orderDao.updateConditionReminderFlag(map);
        //同步网点工单数据
        //有网点才同步
        Long spId = (Long) map.get("servicePointId");
        if (spId != null && spId > 0) {
            servicePointOrderBusinessService.relatedForm(
                    (long) map.get("orderId"),
                    (String) map.get("quarter"),
                    (int) map.get("reminderStatus"),
                    0,
                    0,
                    (long) map.get("reminderCreateBy"),
                    (long) map.get("reminderCreateAt")
            );
        }
        return rtn;
    }

    /**
     * 网点费用汇总
     */
    private OrderServicePointFee getOrderServicePointFee(Long orderId, String quarter, Long servicePointId) {
        return orderDao.getOrderServicePointFee(quarter, orderId, servicePointId);
    }

    /**
     * 返回产品及其服务项目组成的键值对列表
     */
    private List<NameValuePair<Long, Long>> getOrderItemProductAndServiceTypePairs(List<OrderItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        Set<NameValuePair<Long, Long>> valuePairs = items.stream().map(t -> new NameValuePair<>(t.getProductId(), t.getServiceType().getId())).collect(Collectors.toSet());
        return new ArrayList<>(valuePairs);
    }

    /**
     * 发送停滞短信
     *
     * @param dataSource  数据源
     * @param pendingType 停滞类型
     */
    private void sendAppPendingSMS(Integer dataSource, int pendingType, String phone, long userId, long sendAt) {
        if (StringUtils.isBlank(phone) || userId <= 0) {
            return;
        }
        if (sendAt <= 0) {
            sendAt = System.currentTimeMillis();
        }
        //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
        List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
        if (!ignoreDataSources.contains(dataSource.toString())) {
            StringBuffer strContent = new StringBuffer();
            switch (pendingType) {
                    /*
                    case 1://等通知
                        strContent.append("尊敬的用户，您好，您的售后工单，由于暂时无法上门，请您在时间方便时，联系师傅或客服预约上门时间");
                        break;
                    */
                case 2://等配件
                    strContent.append("尊敬的用户，您好，您的售后工单，由于需要等待商家寄发配件，请您在收到配件后，及时联系师傅或客服预约上门时间");
                    break;
                    /*
                    case 3://预约时间
                        strContent.append("您的售后工单,将由")
                                .append(engineer.getName().substring(0, 1) + "师傅 ")
                                .append(engineer.getContactInfo())
                                .append("上门服务,预约时间为" + sbAppointDate.toString())
                                .append(",如有疑问,请致电客服");
                        break;
                    */
                case 4://等到货
                    //京东单等到货短信内容调整 2019-04-16
                    /* 2020-05-12 停发 等到货 短信
                    if(dataSource == B2BDataSourceEnum.JD.getId()) {
                        strContent.append("您好，您的售后工单，由于您暂时还未收到货，请在收到货后，及时联系师傅或客服预约时间，咨询投诉热线：0757-29235666");
                    }else {
                        strContent.append("尊敬的用户，您好，您的售后工单，由于您暂时还未收到货，请您在收到货后时，及时联系师傅或客服预约上门时间");
                    }*/
                    break;
                case 5://等装修
                    strContent.append("尊敬的用户，您好，您的售后工单，由于您家需要等待装修，请您在装修完成后，及时联系师傅或客服预约上门时间");
                    break;
                case 6://不确定时间
                    strContent.append("尊敬的用户，您好，您的售后工单，由于您的原因暂时无法上门，请您在时间方便时，自行联系师傅或客服预约上门时间");
                    break;
            }
            // 使用新的短信发送方法 2019/02/28
            if (strContent.length() > 0) {
                smsMQSender.sendNew(phone, strContent.toString(), "", userId, sendAt, SysSMSTypeEnum.ORDER_PENDING_APP);
            }
        }
    }

    /**
     * 获取互助基金信息
     * @param orderId 工单id
     * @param servicePointId 网点id
     * @param quarter
     * @return
     */
    public List<OrderInsurance> getInsuranceByServicePoint(Long orderId,String quarter,Long servicePointId){
        List<OrderInsurance> orderInsurances = servicePointOrderOperationDao.getInsurancesByServicePointId(quarter,orderId,servicePointId);
        return orderInsurances;
    }
}
