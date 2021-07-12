/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.kkl.kklplus.entity.cc.vm.ReminderTimeLinessModel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.CacheDataTypeEnum;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.sd.dao.SalesOrderDao;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.SalesOrderSearchResultVM;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 业务订单Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class SalesOrderService extends LongIDBaseService {

    @Autowired
    public SalesOrderService(SalesOrderDao salesOrderDao, UrgentLevelService urgentLevelService) {
        this.salesOrderDao = salesOrderDao;
        this.urgentLevelService = urgentLevelService;
    }

    private final SalesOrderDao salesOrderDao;
    private final UrgentLevelService urgentLevelService;

    @Autowired
    private ReminderService reminderService;


    //region 业务

    /**
     * 是否需要单独根据帐号id去读取姓名
     * 1.按订单号或用户电话查询，不关联sys_user，需读取
     * 2.不按下单人查询，不关联sys_user,需读取
     * @param searchModel  查询条件
     * @return
     */
    private boolean isLoadUser(OrderSearchModel searchModel){
        if(searchModel.getOrderNoSearchType() == 1 || searchModel.getIsPhone() == 1){
            return true;
        }
        return StringUtils.isBlank(searchModel.getCreator());
    }

    /**
     * 从微服或其他方法读取订单其他信息
     * @param orderList
     * @param loadUser
     */
    private void loadOrderPropertiesFromMS(List<Order> orderList,boolean loadUser) {
        if (ObjectUtils.isEmpty(orderList)) {
            return;
        }
        Map<String, Dict> statusMap = MSDictUtils.getDictMap("order_status");
        Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap("PendingType");
        Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap("order_data_source");
        Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
        TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> allCustomerMappingMaps = B2BMDUtils.getAllCustomerMappingMaps();
        List<OrderItem> orderItemList = Lists.newArrayList();
        Map<String, List<Long>> orderIdMap = Maps.newHashMap();
        List<Long> userIdList = null;
        Map<Long,String> userNames = Maps.newHashMap();
        if(loadUser){
            userIdList = orderList.stream().filter(t->t.getOrderCondition().getCreateBy().getId()>0).map(t -> t.getOrderCondition().getCreateBy().getId()).distinct().collect(Collectors.toList());
            userNames =  MSUserUtils.getNamesByUserIds(userIdList);
        }

        Long userId;
        for (Order item : orderList) {
            item.setItems(OrderItemUtils.pbToItems(item.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
            orderItemList.addAll(item.getItems());
            //读取帐号姓名
            if(loadUser) {
                userId = item.getOrderCondition().getCreateBy().getId();
                if (userId > 0 && userNames.containsKey(userId)) {
                    item.getOrderCondition().getCreateBy().setName(userNames.get(userId));
                }
            }
            //订单状态
            Dict statusDict = statusMap.get(item.getOrderCondition().getStatus().getValue());
            if (statusDict != null) {
                item.getOrderCondition().setStatus(statusDict);
            }
            //停滞原因
            Dict pendingTypeDict = pendingTypeMap.get(item.getOrderCondition().getPendingType().getValue());
            if (pendingTypeDict != null) {
                item.getOrderCondition().setPendingType(pendingTypeDict);
            }
            //b2b
            Dict dataSourceDict = dataSourceMap.get(item.getDataSource().getValue());
            if(dataSourceDict != null){
                item.setDataSource(dataSourceDict);
            }
            if (dataSourceDict != null && item.getB2bShop() != null && StringUtils.isNotBlank(item.getB2bShop().getShopId())) {
                item.getB2bShop().setDataSource(dataSourceDict.getIntValue());
                B2BCustomerMapping customerMapping = null;
                if (B2BDataSourceEnum.isB2BDataSource(dataSourceDict.getIntValue())) {
                    customerMapping = allCustomerMappingMaps.getBElement().get(String.format("%s:%s", item.getDataSource().getValue(), item.getB2bShop().getShopId()));
                } else {
                    customerMapping = allCustomerMappingMaps.getAElement().get(item.getB2bShop().getShopId());
                }
                if (customerMapping != null && StringUtils.isNotBlank(customerMapping.getShopName())) {
                    item.getB2bShop().setShopName(customerMapping.getShopName());
                }
            }
            //加急等级
            UrgentLevel urgentLevel = urgentLevelMap.get(item.getOrderCondition().getUrgentLevel().getId());
            if (urgentLevel != null) {
                item.getOrderCondition().setUrgentLevel(urgentLevel);
            }
            if (item.getOrderStatus().getComplainFlag() > 0) {
                if (orderIdMap.containsKey(item.getQuarter())) {
                    orderIdMap.get(item.getQuarter()).add(item.getId());
                } else {
                    orderIdMap.put(item.getQuarter(), Lists.newArrayList(item.getId()));
                }
            }
        }
        // 以下按订单批量处理
        OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
        //读取订单最新的投诉单状态
        List<LongTwoTuple> complainStatusList = null;
        Map<Long, Long> allComplainStatusMap = Maps.newHashMap();
        Long orderId,status;
        for (Map.Entry<String, List<Long>> item : orderIdMap.entrySet()) {
            complainStatusList = salesOrderDao.getComplainStatusByOrderIds(item.getKey(), item.getValue());
            if (complainStatusList != null && !complainStatusList.isEmpty()) {
                for (LongTwoTuple complainStatus : complainStatusList) {
                    orderId = complainStatus.getAElement();
                    status = complainStatus.getBElement();
                    if (orderId != null && status != null) {
                        allComplainStatusMap.put(orderId, status);
                    }
                }
            }
        }
        if (allComplainStatusMap.size() > 0) {
            Map<String, Dict> complainStatusDict = MSDictUtils.getDictMap("complain_status");//切换为微服务
            if (complainStatusDict != null && complainStatusDict.size() > 0) {
                for (Order item : orderList) {
                    status = allComplainStatusMap.get(item.getId());
                    if (status != null) {
                        Dict statusDict = complainStatusDict.get(status.toString());
                        if (statusDict != null) {
                            item.setComplainFormStatus(statusDict);
                        }
                    }
                }
            }
        }

    }

    /**
     * 业务处理中工单列表
     */
    public Page<Order> getProcessingOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = salesOrderDao.getProcessingOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 业务已完成工单列表
     */
    public Page<Order> getCompletedOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = salesOrderDao.getCompletedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            //setOrderProperties(orderList);
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 业务已取消工单列表
     */
    public Page<Order> getCanceledOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = salesOrderDao.getCanceledOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            //setOrderProperties(orderList);
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 业务已退单工单列表
     */
    public Page<Order> getReturnedOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        //TODO: 写操作日志
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("userId", searchModel.getUserId());
            for (int i = 0; i< quarters.size(); i++) {
                jo.addProperty("quarter"+ i, quarters.get(i));
            }
            String logContent = GsonUtils.toGsonString(jo);
            LogUtils.saveLog("SalesOrderService.getReturnedOrderLit", "业务已退单工单列表", logContent, null, null);
        } catch (Exception e) {
            log.error("SalesOrderService.getReturnedOrderLit", e);
        }
        List<Order> orderList = salesOrderDao.getReturnedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            //setOrderProperties(orderList);
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 业务所有工单列表
     */
    public Page<Order> getAllOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = salesOrderDao.getAllOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && !orderList.isEmpty()) {
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            loadReminderTimeliness(orderList);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }
        return rtnPage;
    }

    /**
     * 业务投诉工单列表
     */
    public Page<Order> getComplainedOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        searchModel.setBeginAt(searchModel.getCompleteBegin().getTime());
        searchModel.setEndAt(searchModel.getCompleteEnd().getTime());
        List<Order> orderList = salesOrderDao.getComplainedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            //setOrderProperties(orderList);
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 业务催单工单列表
     */
    public Page<Order> getReminderOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        //催单创建日期
        searchModel.setBeginAt(searchModel.getCompleteBegin().getTime());
        searchModel.setEndAt(searchModel.getCompleteEnd().getTime());
        //分片，已下单日期为准
        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        /*取分片，将开始日期提前3个月，因下单日期比催单日期早
        Date[] dates = OrderUtils.getQuarterDates(DateUtils.addMonth(searchModel.getCompleteBegin(),-3), searchModel.getCompleteEnd(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        //下单日期,设定了要重新确认分片范围
        if(searchModel.getBeginDate() != null || searchModel.getEndDate() != null){
            Set<String> reminderQuaters = quarters.stream().collect(Collectors.toSet());
            dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
            List<String> orderQuarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (!ObjectUtils.isEmpty(quarters)) {
                reminderQuaters.addAll(orderQuarters.stream().collect(Collectors.toSet()));
                searchModel.setQuarters(reminderQuaters.stream().collect(Collectors.toList()));
            }
        }*/
        List<Order> orderList = salesOrderDao.getReminderOrderLit(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            //setOrderProperties(orderList);
            loadOrderPropertiesFromMS(orderList,isLoadUser(searchModel));
            loadReminderTimeliness(orderList);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 业务待发配件工单列表
     */
    public Page<Order> getWaitingPartsOrderList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<SalesOrderSearchResultVM> orderIdList = salesOrderDao.getWaitingPartsOrderIdList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderIdList != null && !orderIdList.isEmpty()) {
            List<Order> orderList = getOrderInfoByIdList(orderIdList);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }
        return rtnPage;
    }

    /**
     * 按订单分片及id列表读取订单列表基本信息
     */
    private List<Order> getOrderInfoByIdList(List<SalesOrderSearchResultVM> orderIdList){
        if(ObjectUtils.isEmpty(orderIdList)){
            return Lists.newArrayList();
        }
        List<Order> orders = Lists.newArrayListWithCapacity(orderIdList.size());
        Map<String,List<Long>> orderIdMap = orderIdList.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getQuarter(),
                        Collectors.mapping(
                                p -> p.getOrderId(),
                                Collectors.toList()
                        )
                ));
        List<Order> list;
        for(Map.Entry<String,List<Long>> entry:orderIdMap.entrySet()){
            list = salesOrderDao.getWaitingPartsOrderList(entry.getKey(),entry.getValue());
            if(!ObjectUtils.isEmpty(list)){
                orders.addAll(list);
            }
        }
        loadOrderPropertiesFromMS(orders,true);
        return orders;
    }

    private void loadReminderTimeliness(List<Order> orderList){
        //催单时效
        List<ReminderOrderModel> orderModelList = Lists.newArrayList();
        List<Order> reminderOrderList = orderList.stream().filter(t->t.getOrderStatus().getReminderStatus()>0).collect(Collectors.toList());
        reminderOrderList.stream().forEach(t -> {
            orderModelList.add(ReminderOrderModel.builder()
                    .orderId(t.getId())
                    .quarter(t.getQuarter()).build()
            );
        });
        Map<Long, ReminderTimeLinessModel> reminderModels = Maps.newHashMap();
        if(orderModelList!=null && orderModelList.size()>0){
            BulkRereminderCheckModel bulkRereminderCheckModel = new BulkRereminderCheckModel(orderModelList);
            reminderModels = reminderService.findReminderTimelinessByOrderIds(bulkRereminderCheckModel);
            Date date = new Date();
            TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
            long startDt = twoTuple.getAElement();
            long endDt = twoTuple.getBElement();
            ReminderTimeLinessModel model;
            for (Order order:orderList){
                if(reminderModels.containsKey(order.getId())){
                    model = reminderModels.get(order.getId());
                    if(model!=null){
                        if(order.getOrderStatus().getReminderStatus()==1){
                            double praiseTimeliness = DateUtils.calculateTimeliness(date,model.getTimeoutAt(),startDt,endDt);
                            order.setRemiderCutOffTimeliness(model.getCutOffTimeLiness());
                            order.setReminderCutOffLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                        }else if(order.getOrderStatus().getReminderStatus()>1){
                            order.setRemiderCutOffTimeliness(model.getCutOffTimeLiness());
                            int minute = (int) (model.getCutOffTimeLiness()*60);
                            order.setReminderCutOffLabel(DateUtils.minuteToTimeString(minute,"小时","分"));
                        }
                    }
                }
            }
        }
    }
    //endregion 业务




}
