/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.finance.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.finance.sd.dao.FiCustomerOrderDao;
import com.wolfking.jeesite.modules.md.entity.CacheDataTypeEnum;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.sd.dao.CustomerOrderDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 财务部门客户工单Service
 * @author wangshoujiang
 * @date 2021-3-23
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class FiCustomerOrderService extends LongIDBaseService {

    @Autowired
    public FiCustomerOrderService(FiCustomerOrderDao fiCustomerOrderDao, UrgentLevelService urgentLevelService) {
        this.fiCustomerOrderDao = fiCustomerOrderDao;
        this.urgentLevelService = urgentLevelService;
    }

    private final FiCustomerOrderDao fiCustomerOrderDao;
    private final UrgentLevelService urgentLevelService;


    //region 客户

    private void setOrderProperties(List<Order> orderList,boolean isLoadItemInfo) {
        if (orderList != null && !orderList.isEmpty()) {
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("order_status");
            Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap("PendingType");
            Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap("order_data_source");
            Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
            TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> allCustomerMappingMaps = B2BMDUtils.getAllCustomerMappingMaps();
            List<OrderItem> orderItemList = Lists.newArrayList();
            Map<String, List<Long>> orderIdMap = Maps.newHashMap();
            for (Order item : orderList) {
                item.setItems(OrderItemUtils.pbToItems(item.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
                orderItemList.addAll(item.getItems());

                Dict statusDict = statusMap.get(item.getOrderCondition().getStatus().getValue());
                if (statusDict != null) {
                    item.getOrderCondition().setStatus(statusDict);
                }
                Dict pendingTypeDict = pendingTypeMap.get(item.getOrderCondition().getPendingType().getValue());
                if (pendingTypeDict != null) {
                    item.getOrderCondition().setPendingType(pendingTypeDict);
                }
                Dict dataSourceDict = dataSourceMap.get(item.getDataSource().getValue());
                if (dataSourceDict != null && item.getB2bShop() != null && StringUtils.isNotBlank(item.getB2bShop().getShopId())) {
                    item.setDataSource(dataSourceDict);
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
            if(isLoadItemInfo) {
                OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
            }
            List<LongTwoTuple> complainStatusList = null;
            Map<Long, Long> allComplainStatusMap = Maps.newHashMap();
            for (Map.Entry<String, List<Long>> item : orderIdMap.entrySet()) {
                complainStatusList = fiCustomerOrderDao.getComplainStatusByOrderIds(item.getKey(), item.getValue());
                if (complainStatusList != null && !complainStatusList.isEmpty()) {
                    for (LongTwoTuple complainStatus : complainStatusList) {
                        Long orderId = complainStatus.getAElement();
                        Long status = complainStatus.getBElement();
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
                        Long status = allComplainStatusMap.get(item.getId());
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
    }

    /**
     * 客户处理中工单列表
     */
    public Page<Order> getProcessingOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getProcessingOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,true);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 客户已完成工单列表
     */
    public Page<Order> getCompletedOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getCompletedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,true);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 客户已取消工单列表
     */
    public Page<Order> getCanceledOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getCanceledOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,false);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 客户已退单工单列表
     */
    public Page<Order> getReturnedOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getReturnedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,false);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }


    /**
     * 客户所有工单列表
     * 2020-11-27
     */
    public Page<Order> getAllOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getAllOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,true);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    //region new All List

    /**
     * 客户所有工单列表
     * 2020-11-27
     */
    public Page<Order> getNewAllOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getNewAllOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && !orderList.isEmpty()) {
            setNewOrderProperties(orderList);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }


    private void setNewOrderProperties(List<Order> orderList) {
        if (orderList != null && !orderList.isEmpty()) {
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("order_status");
            Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap("PendingType");
            Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap("order_data_source");
            Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
            TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> allCustomerMappingMaps = B2BMDUtils.getAllCustomerMappingMaps();
            List<OrderItem> orderItemList = Lists.newArrayList();
            Map<String, List<Long>> orderIdMap = Maps.newHashMap();
            for (Order item : orderList) {
                //item.setItems(OrderItemUtils.fromOrderItemsJson(item.getOrderItemJson()));
                item.setItems(OrderItemUtils.pbToItems(item.getItemsPb()));
                orderItemList.addAll(item.getItems());

                Dict statusDict = statusMap.get(item.getOrderCondition().getStatus().getValue());
                if (statusDict != null) {
                    item.getOrderCondition().setStatus(statusDict);
                }
                Dict pendingTypeDict = pendingTypeMap.get(item.getOrderCondition().getPendingType().getValue());
                if (pendingTypeDict != null) {
                    item.getOrderCondition().setPendingType(pendingTypeDict);
                }
                Dict dataSourceDict = dataSourceMap.get(item.getDataSource().getValue());
                if (dataSourceDict != null && item.getB2bShop() != null && StringUtils.isNotBlank(item.getB2bShop().getShopId())) {
                    item.setDataSource(dataSourceDict);
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
            OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));

            List<LongTwoTuple> complainStatusList = null;
            Map<Long, Long> allComplainStatusMap = Maps.newHashMap();
            for (Map.Entry<String, List<Long>> item : orderIdMap.entrySet()) {
                complainStatusList = fiCustomerOrderDao.getComplainStatusByOrderIds(item.getKey(), item.getValue());
                if (complainStatusList != null && !complainStatusList.isEmpty()) {
                    for (LongTwoTuple complainStatus : complainStatusList) {
                        Long orderId = complainStatus.getAElement();
                        Long status = complainStatus.getBElement();
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
                        Long status = allComplainStatusMap.get(item.getId());
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
    }

    //endregion new All List

    /**
     * 客户投诉工单列表
     */
    public Page<Order> getComplainedOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        searchModel.setBeginAt(searchModel.getBeginDate().getTime());
        searchModel.setEndAt(searchModel.getEndDate().getTime());
        List<Order> orderList = fiCustomerOrderDao.getComplainedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,true);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 客户待发配件工单列表
     */
    public Page<Order> getWaitingPartsOrderList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getWaitingPartsOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,true);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }


    /**
     * 客户催单工单列表
     */
    public Page<Order> getReminderOrderLit(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        searchModel.setBeginAt(searchModel.getCompleteBegin().getTime());
        searchModel.setEndAt(searchModel.getCompleteEnd().getTime());
        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && !quarters.isEmpty()) {
            searchModel.setQuarters(quarters);
        }
        List<Order> orderList = fiCustomerOrderDao.getReminderOrderLit(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList,true);
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }
        return rtnPage;
    }


    //endregion 客户


    @Autowired
    private OrderService orderService;


    /**
     * 待审核工单列表

    public Page<Order> getWaitingApproveOrderList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            searchModel.setQuarters(quarters);
        }

        List<OrderCondition> list = fiCustomerOrderDao.getWaitingApproveOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (list != null && !list.isEmpty()) {
            Order order;
            boolean cacheFirst;
            List<Order> orderList = Lists.newArrayListWithExpectedSize(list.size());
            for (OrderCondition condition : list) {
                cacheFirst = condition.getChargeFlag() == 0;
                if (!cacheFirst) {
                    cacheFirst = condition.getStatus().getIntValue() < Order.ORDER_STATUS_COMPLETED;
                }
                order = orderService.getOrderById(condition.getOrderId(), condition.getQuarter(), searchModel.getOrderDataLevel(), cacheFirst);
                if (order != null) {
                    orderList.add(order);
                }
            }
            rtnPage.setList(orderList);
        }

        return rtnPage;
    }*/


}
