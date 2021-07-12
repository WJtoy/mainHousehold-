package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.CacheDataTypeEnum;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderCrushDao;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderServiceItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderCrushSearchVM;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderModifyService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 突击单Service
 *
 * @author RyanLu
 * @version 2020-04-22
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderCrushService extends OrderRegionService {

    @Autowired
    private OrderCrushDao crushDao;

    @Autowired
    private AreaService areaService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private B2BCenterOrderModifyService b2BCenterOrderModifyService;
    //region CUD

    /**
     * 保存突击单
     */
    @Transactional(readOnly = false)
    public void saveOrderCrush(OrderCrush orderCrush) {
        if (orderCrush.getId() != null && orderCrush.getId() != 0) {
            crushDao.updateOrderCrush(orderCrush);
        } else {
            crushDao.insertOrderCrush(orderCrush);
        }
        crushDao.updateOrderCrushLastFlagToZero(orderCrush.getQuarter(), orderCrush.getOrderId(), orderCrush.getId());
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        map.put("orderId", orderCrush.getOrderId());
        map.put("quarter", orderCrush.getQuarter());
        map.put("rushOrderFlag", 1);
        orderService.updateOrderCondition(map);
    }

    /**
     * 暂存突击单
     */
    @Transactional(readOnly = false)
    public void tempSaveOrderCrush(OrderCrush orderCrush) {
        if (orderCrush.getId() != null && orderCrush.getId() != 0) {
            crushDao.updateOrderCrush(orderCrush);
        } else {
            crushDao.insertOrderCrush(orderCrush);
        }

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        map.put("orderId", orderCrush.getOrderId());
        map.put("quarter", orderCrush.getQuarter());
        map.put("rushOrderFlag", 3);
        orderService.updateOrderCondition(map);
    }


    /**
     * 新建突击单
     */
    @Transactional
    public void insertOrderCrush(OrderCrush entity){
        crushDao.insertOrderCrush(entity);
    }

    /**
     * 更新突击单
     */
    public int updateOrderCrush(OrderCrush entity){
        return crushDao.updateOrderCrush(entity);
    }

    /**
     * 查询订单 是否 有已提交但未处理的突击单（突击单状态为0-待处理或2-暂存）
     *
     * @param orderId
     * @param quarter
     * @return
     */
    public boolean hasOpenOrderCrush(Long orderId, String quarter) {
        Long id = crushDao.getOpenOrderCrushId(orderId, quarter);
        if (id != null && id > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 按订单关闭突击单
     * @param orderId   订单id
     * @param quarter   分片
     * @param status    更新后状态
     * @param closeRemark   备注
     * @param closeBy   更新人
     * @param closeDate 更新日期
     * @return
     */
    @Transactional
    public int closeOrderCurshByOrderId(long orderId, String quarter, int status, String closeRemark, User closeBy,Date closeDate){
        OrderCrush orderCrush = crushDao.getOrderCrushByOrderId(orderId,quarter);
        double timeliness=0.0;
        if(orderCrush!=null && orderCrush.getCreateDate()!=null){
            timeliness = DateUtils.differTime(orderCrush.getCreateDate().getTime(),closeDate.getTime());
        }
        return crushDao.closeOrderCurshByOrderId(orderId,quarter,status,closeRemark,closeBy,closeDate,timeliness);
    }

    @Transactional
    public int updateOrderCrushLastFlagToZero(String quarter,Long orderId,Long exceptId){
        return crushDao.updateOrderCrushLastFlagToZero(quarter,orderId,exceptId);
    }

    /**
     * 完成突击单
     */
    @Transactional
    public int closeOrderCursh(OrderCrush entity){
        int row =  crushDao.closeOrderCursh(entity);
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        map.put("orderId", entity.getOrderId());
        map.put("quarter", entity.getQuarter());
        map.put("rushOrderFlag", 2);
        orderService.updateOrderCondition(map);
        return row;
    }

    //endregion CUD


    //region 查询

    /**
     * 分页查询突击单
     */
    public Page<OrderCrush> findPageList(Page<OrderCrushSearchVM> page, OrderCrushSearchVM entity) {
        entity.setPage(page);
        
        User user = entity.getCreateBy();
        if(user == null){
            user = UserUtils.getUser();
        } 
        int userType = user.getUserType();
        if (userType == User.USER_TYPE_INNER || userType == User.USER_TYPE_KEFU) {
            entity.setCreateBy(user);
        }

        //处理查询对象为投诉对象和投诉项目
        List<OrderCrush> list = crushDao.findPageList(entity);
        if(!CollectionUtils.isEmpty(list)) {
            Map<Long, String> productCategoryMap = Maps.newHashMapWithExpectedSize(20);
            if (entity.getProductCategoryId() != null && entity.getProductCategoryId() > 0) {
                ProductCategory productCategory = productCategoryService.getFromCache(entity.getProductCategoryId());
                productCategoryMap.put(entity.getProductCategoryId(), productCategory == null ? "" : productCategory.getName());
                productCategory = null;
            } else {
                List<ProductCategory> productCategoryList = productCategoryService.findAllList();
                if (!org.springframework.util.ObjectUtils.isEmpty(productCategoryList)) {
                    productCategoryMap.putAll(
                            productCategoryList.stream()
                                    .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName))
                    );
                }
                productCategoryList = null;
            }
            Map<String, Dict> orderServieTypes = MSDictUtils.getDictMap("order_service_type");
            if(orderServieTypes ==null){
                orderServieTypes = Maps.newHashMap();
            }else{
                orderServieTypes.remove("0");
            }
            OrderCrush model;
            String productCategory;
            Dict orderServiceType;
            for (int i = 0, size = list.size(); i < size; i++) {
                model = list.get(i);
                productCategory = productCategoryMap.get(model.getProductCategoryId());
                if(productCategory != null){
                    model.setProductCategoryName(productCategory);
                }
                //order service type
                orderServiceType = orderServieTypes.get(String.valueOf(model.getOrderServiceType()));
                if(orderServiceType != null){
                    model.setOrderServiceTypeName(orderServiceType.getLabel());
                }
                int minutes = (int) (60*model.getTimeLiness());
                model.setTimeLinessLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
            }
        }
        Page<OrderCrush> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        return rtnPage;
    }

    
    /**
     * 根据突击单区县或街道ID读取同区域以往派单记录
     *
     */
    public Page getHistoryPlanListForCrush(Page page, OrderCrushSearchVM orderCrush) {
        orderCrush.setPage(page);
        //quarter
        if(orderCrush.getBeginDate() != null && orderCrush.getEndDate() != null){
            Date[] dates = OrderUtils.getQuarterDates(orderCrush.getBeginDate(), orderCrush.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if(!org.springframework.util.ObjectUtils.isEmpty(quarters)){
                orderCrush.setQuarters(quarters);
            }
        }
        List<HistoryPlanOrderModel> list = orderService.findOrderListOfCrush(orderCrush);
        if(list == null){
            list = Lists.newArrayList();
        }else{
            Map<Long,String> productCategoryMap = Maps.newHashMapWithExpectedSize(page.getPageSize());
            if(orderCrush.getProductCategoryId() != null && orderCrush.getProductCategoryId()>0){
                ProductCategory productCategory = productCategoryService.getFromCache(orderCrush.getProductCategoryId());
                productCategoryMap.put(orderCrush.getProductCategoryId(),productCategory==null?"":productCategory.getName());
                productCategory = null;
            }else{
                List<ProductCategory> productCategoryList = productCategoryService.findAllList();
                if(!org.springframework.util.ObjectUtils.isEmpty(productCategoryList)){
                    productCategoryMap.putAll(
                            productCategoryList.stream()
                                    .collect(Collectors.toMap(ProductCategory::getId,ProductCategory::getName))
                    );
                }
                productCategoryList = null;
            }
            Map<Long, ServicePoint> servicePointMap = Maps.newHashMapWithExpectedSize(page.getPageSize());
            Map<String, Dict> orderStatusMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_STATUS);
            HistoryPlanOrderModel model;
            Map<Long, HistoryPlanOrderModel> modelMap = Maps.newHashMapWithExpectedSize(list.size());
            Map<String,List<Long>> serviceDetailMap = Maps.newHashMapWithExpectedSize(list.size());
            Dict status;
            ServicePoint servicePoint;
            List<OrderItem> itemList = Lists.newArrayList();
            for(int i=0,size=list.size();i<size;i++){
                model = list.get(i);
                status = orderStatusMap.get(String.valueOf(model.getStatusValue()));
                if(status != null){
                    model.setStatusName(status.getLabel());
                }
                //model.setItems(OrderItemUtils.fromOrderItemsJson(model.getItemJson()));
                model.setItems(OrderItemUtils.pbToItems(model.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
                itemList.addAll(model.getItems());
                if(model.getProductCategoryId()>0 && productCategoryMap.containsKey(model.getProductCategoryId())){
                    model.setProductCategoryName(productCategoryMap.get(model.getProductCategoryId()));
                }
                if(model.getStatusValue()== Order.ORDER_STATUS_COMPLETED || model.getStatusValue()==Order.ORDER_STATUS_CHARGED) {
                    if (serviceDetailMap.containsKey(model.getQuarter())) {
                        serviceDetailMap.get(model.getQuarter()).add(model.getOrderId());
                    }else{
                        serviceDetailMap.put(model.getQuarter(), Lists.newArrayList(model.getOrderId()));
                    }
                }
                //servicepoint
                servicePoint = servicePointMap.get(model.getServicePointId());
                if(servicePoint == null) {
                    servicePoint = servicePointService.getFromCache(model.getServicePointId());
                    if (servicePoint != null) {
                        servicePointMap.put(servicePoint.getId(),servicePoint);
                        model.setServicePointName(servicePoint.getName());
                    }
                }else{
                    model.setServicePointName(servicePoint.getName());
                }
                modelMap.put(model.getOrderId(),model);
            }
            OrderItemUtils.setOrderItemProperties(itemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
            //上门服务
            List<HistoryPlanOrderModel> models;
            HistoryPlanOrderModel entity;
            double serviceCharge,totalCharge,travelCharge,expressCharge,materialCharge,otherCharge;
            HistoryPlanOrderServiceItem item;
            StringBuilder text = new StringBuilder(300);
            for(Map.Entry<String,List<Long>> entry :serviceDetailMap.entrySet()) {
                models = orderService.getOrderServiceItemList(entry.getKey(),entry.getValue());
                if(!org.springframework.util.ObjectUtils.isEmpty(models)){
                    for(int i=0,size=models.size();i<size;i++){
                        entity = models.get(i);
                        serviceCharge = 0;
                        totalCharge = 0;
                        travelCharge=0;
                        expressCharge = 0;
                        materialCharge = 0;
                        otherCharge = 0;
                        text.setLength(0);
                        for(int j=0,jsize=entity.getServiceItems().size();j<jsize;j++){
                            item = entity.getServiceItems().get(j);
                            serviceCharge = serviceCharge + item.getEngineerServiceCharge();
                            travelCharge = travelCharge + item.getEngineerTravelCharge();
                            expressCharge= expressCharge + item.getEngineerExpressCharge();
                            materialCharge = materialCharge + item.getEngineerMaterialCharge();
                            otherCharge = otherCharge + item.getEngineerOtherCharge();
                            totalCharge = totalCharge + item.getEngineerTotalCharge();
                        }

                        model = modelMap.get(entity.getOrderId());
                        text.setLength(0);
                        text.append(NumberUtils.formatDouble(totalCharge,1)).append("元");
                        text.append("<br/>").append("服务费：").append(NumberUtils.formatDouble(serviceCharge,1));
                        if(materialCharge>0){
                            text.append("<br/>").append("配件费：").append(NumberUtils.formatDouble(materialCharge,1));
                        }
                        if(expressCharge>0){
                            text.append("<br/>").append("快递费：").append(NumberUtils.formatDouble(expressCharge,1));
                        }
                        if(travelCharge>0){
                            text.append("<br/>").append("远程费：").append(NumberUtils.formatDouble(travelCharge,1));
                        }
                        if(otherCharge>0){
                            text.append("<br/>").append("其他：").append(NumberUtils.formatDouble(otherCharge,1));
                        }
                        model.setChargeText(text.toString());
                    }
                }
            }
            //clear
            productCategoryMap = null;
            modelMap = null;
            serviceDetailMap = null;
            text = null;
            models = null;
            itemList = null;
            orderStatusMap = null;
        }
        page.setList(list);
        list = null;
        return page;
    }


    /**
     * 查询突击单列表
     *
     * @param page
     * @param orderCrush
     * @return

    public Page<OrderCrush> findOrderCrushList(Page<OrderCrushSearchVM> page, OrderCrushSearchVM orderCrush) {
        orderCrush.setPage(page);
        //quarter
        if(orderCrush.getBeginDate() != null && orderCrush.getEndDate() != null){
            Date startDate = DateUtils.addMonth(orderCrush.getBeginDate(),-1);
            List<String> quarters = QuarterUtils.getQuarters(startDate,orderCrush.getEndDate());
            if(!org.springframework.util.ObjectUtils.isEmpty(quarters)){
                orderCrush.setQuarters(quarters);
            }
        }
        List<OrderCrush> list = crushDao.findOrderCrushList(orderCrush);
        if(list == null){
            list = Lists.newArrayList();
        }else{
            Map<Long,String> productCategoryMap = Maps.newHashMapWithExpectedSize(20);
            if(orderCrush.getProductCategoryId() != null && orderCrush.getProductCategoryId()>0){
                ProductCategory productCategory = productCategoryService.getFromCache(orderCrush.getProductCategoryId());
                productCategoryMap.put(orderCrush.getProductCategoryId(),productCategory==null?"":productCategory.getName());
                productCategory = null;
            }else{
                List<ProductCategory> productCategoryList = productCategoryService.findAllList();
                if(!org.springframework.util.ObjectUtils.isEmpty(productCategoryList)){
                    productCategoryMap.putAll(
                            productCategoryList.stream()
                                    .collect(Collectors.toMap(ProductCategory::getId,ProductCategory::getName))
                    );
                }
                productCategoryList = null;
            }

            OrderCrush model;
            for(int i=0,size=list.size();i<size;i++){
                model = list.get(i);
                if(model.getProductCategoryId()>0 && productCategoryMap.containsKey(model.getProductCategoryId())){
                    model.setProductCategoryName(productCategoryMap.get(model.getProductCategoryId()));
                }
            }
        }
        Page<OrderCrush> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        page = null;
        orderCrush = null;
        return rtnPage;
    }
     */

    /**
     * 根据订单ID获取突击单列表
     *
     * @param orderId 工单Id
     * @param crushNo 突击单号
     * @param quarter 分片
     */
    public List<OrderCrush> findCrushListOfOrder(Long orderId, String crushNo, String quarter) {
        OrderCrush orderCrush = new OrderCrush();
        orderCrush.setOrderId(orderId);
        orderCrush.setCrushNo(crushNo);
        orderCrush.setQuarter(quarter);
        return crushDao.findCrushListOfOrder(orderCrush);
    }

    /**
     * 根据突击单区县或街道ID读取同区域以往派单记录

    public List<HistoryPlanOrderModel> findOrderListOfCrush(OrderCrushSearchVM searchModel){
        return crushDao.findOrderListOfCrush(searchModel);
    }

    public List<HistoryPlanOrderModel> getOrderServiceItemList(String quarter, List<Long> orderIds){
        return crushDao.getOrderServiceItemList(quarter,orderIds);
    }*/

    /**
     * 获取完整突击单
     */
    public OrderCrush getOrderCrush(Long id, String quarter,boolean isLoadSubArea) {
        OrderCrush crush = crushDao.getOrderCrushById(id, quarter);
        crush.setSubArea(new Area(0L));
        if(isLoadSubArea) {
            try {
                Long subAreaId = orderService.getSubAreaIdByOrderId(crush.getOrderId(), crush.getQuarter());
                if(subAreaId != null && subAreaId > 3){
                    Area subArea = areaService.getTownFromCache(crush.getArea().getId(),subAreaId);
                    if(subArea != null){
                        crush.setSubArea(subArea);
                    }
                }
            } catch (Exception e) {
                log.error("读取工单街道id错误,order id:{}", id, e);
            }
        }
        return crush;
    }

    /**
     * 获取完整突击单
     */
    public OrderCrush getOrderCrushById(Long id, String quarter){
        return crushDao.getOrderCrushById(id,quarter);
    }

    /**
     * 获取突击单(不包含网点信息)
     */
    public OrderCrush getOrderCrushNoDetailById(Long id, String quarter){
        return crushDao.getOrderCrushNoDetailById(id,quarter);
    }

    /**
     * 读取最大序号
     */
    public Integer getMaxTimes(Long orderId, String quarter){
        return crushDao.getMaxTimes(orderId,quarter);
    }

    /**
     * 查询未完成的突击单ID
     */
    public Long getOpenOrderCrushId(Long orderId, String quarter){
        return crushDao.getOpenOrderCrushId(orderId,quarter);
    }

    /**
     * 活动订单当前暂存的突击单
     */
    public OrderCrush getTempSaveOrderCrush(Long orderId, String quarter){
        return crushDao.getTempSaveOrderCrush(orderId,quarter);
    }

    /**
     * 修改实际上门联系信息
     *
     * @param orderCondition
     */
    @Transactional(readOnly = false)
    public void updateAddress(OrderCondition orderCondition,Long crushId) {
        //检查锁
        String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, orderCondition.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 30);//30秒
        if (!locked) {
            throw new OrderException("该单正在被其他人处理中，请稍候重试，或刷新页面。");
        }
        try {
            User user = UserUtils.getUser();
            Order o = orderService.getOrderById(orderCondition.getOrderId(), orderCondition.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (o == null || o.getOrderCondition() == null) {
                throw new OrderException("读取订单失败，请重试。");
            }
            OrderCondition condition = o.getOrderCondition();
            if(condition.getArea()==null && condition.getArea().getId()==null || condition.getArea().getId()<=0){
                throw new OrderException("读取工单区/县失败，请重试。");
            }
            Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(condition.getArea().getId());
            Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
            OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(condition.getProductCategoryId(),city.getId(),condition.getArea().getId(),
                                                                                   orderCondition.getSubArea().getId(),condition.getCustomer().getVipFlag(),condition.getCustomer().getVip());
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            //condition
            params.put("quarter", o.getQuarter());
            params.put("orderId", orderCondition.getOrderId());
            params.put("areaName", orderCondition.getAreaName());
            params.put("subArea",orderCondition.getSubArea());
            params.put("serviceAddress", orderCondition.getServiceAddress());
            params.put("updateBy", user);
            params.put("updateDate", date);
            String kefuTypeInfo = "";
            if(!condition.getKefuType().equals(orderKefuTypeRuleEnum.getCode())){
                params.put("kefuType", orderKefuTypeRuleEnum.getCode());
                int canRush = 0;
                if(orderKefuTypeRuleEnum.getCode()==OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){
                    canRush = 1;
                }
                params.put("canRush", canRush);
                kefuTypeInfo = "客服：【".concat(orderService.getKefuTypeName(condition.getKefuType(),condition.getCustomer().getVipFlag()))
                        .concat("->").concat(orderService.getKefuTypeName(orderKefuTypeRuleEnum.getCode(),condition.getCustomer().getVipFlag()))+"】";
            }
            orderDao.updateAddress(params);
            // log
            OrderProcessLog log = new OrderProcessLog();
            StringBuffer addressStringBuffer = new StringBuffer("【");
            if(condition.getArea()!=null){
                addressStringBuffer.append(condition.getArea().getName());
            }
            addressStringBuffer.append(condition.getServiceAddress());
            addressStringBuffer.append("->");
            addressStringBuffer.append(orderCondition.getAreaName());

            addressStringBuffer.append(orderCondition.getServiceAddress());
            addressStringBuffer.append("】");
            if(StringUtils.isNotBlank(kefuTypeInfo)){
                addressStringBuffer.append("，");
                addressStringBuffer.append(kefuTypeInfo);
            }
            log.setQuarter(o.getQuarter());
            log.setAction("修改地址");
            log.setOrderId(o.getId());
            log.setActionComment(String.format("地址:%s", addressStringBuffer.toString()));
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(Integer.parseInt(condition.getStatus().getValue()));
            log.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            log.setCloseFlag(0);
            log.setCreateBy(user);
            log.setCreateDate(date);
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(o.getDataSourceId());
            orderService.saveOrderProcessLogNew(log);

            OrderCacheUtils.delete(o.getId());


            // region 网点订单数据更新 2019-03-25 (网点工单表无街道id栏位暂时不需更新网点订单数据)
           /* if(!(prevServicePhone.equalsIgnoreCase(ucondition.getServicePhone()) && prevServiceAddress.equalsIgnoreCase(ucondition.getServiceAddress()))) {
                servicePointOrderBusinessService.updateUserInfo(order.getId(), order.getQuarter(),
                        ucondition.getServicePhone(), ucondition.getServiceAddress(),
                        user.getId(), date.getTime());
            }*/
            // 修改B2B工单
            o.getOrderCondition().setServiceAddress(orderCondition.getServiceAddress());
            b2BCenterOrderModifyService.modifyB2BOrder(o, false);

            //修改突击单地址
            params.clear();
            params.put("userAddress",orderCondition.getAreaName()+orderCondition.getServiceAddress());
            params.put("id",crushId);
            crushDao.updateAddress(params);

            //endregion
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    //endregion 查询

}
