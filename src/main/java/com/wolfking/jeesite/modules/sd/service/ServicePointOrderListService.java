/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.kkl.kklplus.entity.cc.vm.ReminderTimeLinessModel;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sd.dao.OrderComplainDao;
import com.wolfking.jeesite.modules.sd.dao.ServicePointOrderListDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import com.wolfking.jeesite.ms.cc.entity.mapper.OrderReminderVMMapper;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网点工单列表
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointOrderListService extends LongIDBaseService {

    @Resource
    private ServicePointOrderListDao servicePointOrderListDao;

    @Resource
    protected OrderComplainDao complainDao;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private KefuOrderListService kefuOrderListService;

    private OrderServicePointSearchModel setSearchModelProperties(OrderServicePointSearchModel searchModel) {
        //计算分片
        Date now = new Date();
        Date startDate = OrderUtils.getGoLiveDate();
        Date endDate = (searchModel.getEndAcceptDate() == null ? now : searchModel.getEndAcceptDate());
        Date tempDate;
        if (searchModel.getBeginAcceptDate() != null) {
            tempDate = DateUtils.addMonth(searchModel.getBeginAcceptDate(), -1);//查询日期是派单日期，分片是按下单分片，所以在此时间上加一个月
            if (tempDate.getTime() > startDate.getTime()) {
                startDate = tempDate;
            }
        } else {
            tempDate = DateUtils.addMonth(endDate, -9);//9个月
            if (tempDate.getTime() > startDate.getTime()) {
                startDate = tempDate;
            }
            searchModel.setBeginAcceptDate(startDate);
        }
        List<String> quarters = QuarterUtils.getQuarters(startDate, endDate);
        if (quarters != null && quarters.size() > 0) {
            searchModel.setQuarters(quarters);
        }

        searchModel.setStartOfToday(DateUtils.getStartOfDay(now));
        searchModel.setEndOfToday(DateUtils.getEndOfDay(now));
        return searchModel;
    }

    /**
     * 获取网点未预约的工单
     */
    public Page<Order> findServicePointNoAppointmentOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointNoAppointmentOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 催单
     */
    public Page<OrderReminderVM> findReminderOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findReminderOrderList(searchModel);
        Page<OrderReminderVM> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            // 催单时效
            List<OrderReminderVM> vmList = kefuOrderListService.getReminderTimeliness(orderList);
            rtnPage.setList(vmList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点预约到期的工单
     */
    public Page<Order> findServicePointArriveAppointmentOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointArriveAppointmentOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点预约超期的工单
     */
    public Page<Order> findServicePointPassAppointmentOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointPassAppointmentOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点停滞的工单
     */
    public Page<Order> findServicePointPendingOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointPendingOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点待完成的工单
     */
    public Page<Order> findServicePointServicedOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointServicedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点待回访的工单
     */
    public Page<Order> findServicePointAppCompletedOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointAppCompletedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点未完成的工单
     */
    public Page<Order> findServicePointUncompletedOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointUncompletedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点所有的工单
     */
    public Page<Order> findServicePointAllOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);
        List<Order> orderList = servicePointOrderListDao.findServicePointAllOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }
        return rtnPage;
    }

    /**
     * 获取网点完成的工单
     */
    public Page<Order> findServicePointCompletedOrderList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel) {
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);

        List<Order> orderList = servicePointOrderListDao.findServicePointCompletedOrderList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && orderList.size() > 0) {
            List<Long> orderIdList = orderList.stream().map(LongIDBaseEntity::getId).collect(Collectors.toList());
            List<OrderDetail> orderDetailList = servicePointOrderListDao.getOrderDetailListByOrderIds(orderIdList, searchModel.getServicePointId(), searchModel.getEngineerId());
            Map<Long, List<OrderDetail>> orderDetailMap = Maps.newHashMap();
            if (orderDetailList != null && orderDetailList.size() > 0) {
                Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
                Map<Long, Product> productMap = ProductUtils.getAllProductMap();
                for (OrderDetail item : orderDetailList) {
                    item.setServiceType(serviceTypeMap.get(item.getServiceType().getId()));
                    item.setProduct(productMap.get(item.getProductId()));
                    if (orderDetailMap.containsKey(item.getOrderId())) {
                        orderDetailMap.get(item.getOrderId()).add(item);
                    } else {
                        orderDetailMap.put(item.getOrderId(), Lists.newArrayList(item));
                    }
                }
            }

            Map<String, Dict> statusMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_STATUS);
            Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_PENDING_TYPE);
            Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
            List<Long> kefuIds = orderList.stream().filter(i->i.getOrderCondition()!=null&&i.getOrderCondition().getKefu()!=null&&i.getOrderCondition().getKefu().getId()!=null)
                    .map(i->i.getOrderCondition().getKefu().getId()).collect(Collectors.toList());
            Map<Long, User> kefuMap = MSUserUtils.getMapByUserIds(kefuIds);

            // add on 2019-11-1 begin
            List<Long> engineerIds = orderList.stream().filter(order -> order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId()!=null)
                    .map(order -> order.getOrderCondition().getEngineer().getId()).distinct().collect(Collectors.toList());
            Map<Long, Engineer> engineerMap = msEngineerService.findEngineersByIdsToMap(engineerIds, Arrays.asList("id","name","contactInfo"));
            // add on 2019-11-1 end
            for (Order item : orderList) {
                if (item.getOrderCondition() != null && item.getOrderCondition().getServicePoint() != null &&
                        item.getOrderCondition().getServicePoint().getId() != null) {
                    ServicePoint servicePoint = servicePointService.getFromCache(item.getOrderCondition().getServicePoint().getId());
                    if (servicePoint != null) {
                        item.getOrderCondition().setServicePoint(servicePoint);
                    }
                    if (item.getOrderCondition().getEngineer() != null && item.getOrderCondition().getEngineer().getId() != null) {
                        //Engineer engineer = servicePointService.getEngineerFromCache(item.getOrderCondition().getServicePoint().getId(), item.getOrderCondition().getEngineer().getId()); //mark on 2019-11-1
                        Engineer engineer = engineerMap.get(item.getOrderCondition().getEngineer().getId());  // add on 2019-11-1
                        if (engineer != null) {
                            item.getOrderCondition().setEngineer(new User(engineer.getId(), engineer.getName(), engineer.getContactInfo()));
                        }
                    }
                }
                if (item.getOrderCondition() != null && item.getOrderCondition().getKefu() != null && item.getOrderCondition().getKefu().getId() != null) {
                    User kefu = kefuMap.get(item.getOrderCondition().getKefu().getId());
                    if (kefu != null) {
                        item.getOrderCondition().setKefu(kefu);
                    }
                }
                Dict statusDict = statusMap.get(item.getOrderCondition().getStatus().getValue());
                if (statusDict != null) {
                    if (item.getOrderCondition().getChargeFlag() == 1) {
                        statusDict = Dict.copyDict(statusDict);
                        statusDict.setLabel("已入账");
                    } else if (Integer.valueOf(item.getOrderCondition().getStatus().getValue()) == 80) {
                        statusDict = Dict.copyDict(statusDict);
                        statusDict.setLabel("待审核");
                    }
                    item.getOrderCondition().setStatus(statusDict);
                }
                Dict pendingTypeDict = pendingTypeMap.get(item.getOrderCondition().getPendingType().getValue());
                if (pendingTypeDict != null) {
                    item.getOrderCondition().setPendingType(pendingTypeDict);
                }
                UrgentLevel urgentLevel = urgentLevelMap.get(item.getOrderCondition().getUrgentLevel().getId());
                if (urgentLevel != null) {
                    item.getOrderCondition().setUrgentLevel(urgentLevel);
                }
                List<OrderDetail> orderDetails = orderDetailMap.get(item.getId());
                if (orderDetails != null) {
                    item.setDetailList(orderDetails);
                }
            }
            setServicePointComplainFlags(searchModel.getServicePointId(), orderList);

            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }

        return rtnPage;
    }

    /**
     * 获取网点等配件工单列表
     */
    public Page<Order> findServicePointWaitingAccessoryList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel){
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);

        List<Order> orderList = servicePointOrderListDao.findServicePointWaitingAccessoryList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }
        return rtnPage;
    }

    /**
     * 获取网点退单工单列表
     */
    public Page<Order> findServicePointReturnedList(Page<OrderServicePointSearchModel> page, OrderServicePointSearchModel searchModel){
        searchModel = setSearchModelProperties(searchModel);
        searchModel.setPage(page);

        List<Order> orderList = servicePointOrderListDao.findServicePointReturnedList(searchModel);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (orderList != null && orderList.size() > 0) {
            setOrderProperties(orderList, searchModel.getServicePointId());
            rtnPage.setList(orderList);
        } else {
            rtnPage.setList(Lists.newArrayList());
        }
        return rtnPage;
    }

    //endregion 网点

    /**
     * 设置工单对象的部分属性
     */
    private void setOrderProperties(List<Order> orderList, Long servicePointId) {
        if (orderList != null && orderList.size() > 0) {
            Map<String, Dict> statusMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_STATUS);
            Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_PENDING_TYPE);
            Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
            List<Long> kefuIds = orderList.stream().filter(i -> i.getOrderCondition() != null && i.getOrderCondition().getKefu() != null && i.getOrderCondition().getKefu().getId() != null)
                    .map(i -> i.getOrderCondition().getKefu().getId()).collect(Collectors.toList());
            Map<Long, User> kefuMap = MSUserUtils.getMapByUserIds(kefuIds);
            List<OrderItem> orderItemList = Lists.newArrayList();
            List<Long> engineerIds = orderList.stream().filter(order -> order.getOrderCondition().getEngineer() != null && order.getOrderCondition().getEngineer().getId()!=null)
                    .map(order -> order.getOrderCondition().getEngineer().getId()).distinct().collect(Collectors.toList());
            Map<Long, Engineer> engineerMap = msEngineerService.findEngineersByIdsToMap(engineerIds, Arrays.asList("id","name","contactInfo"));
            for (Order item : orderList) {
                item.setItems(OrderItemUtils.pbToItems(item.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
                orderItemList.addAll(item.getItems());
                if (item.getOrderCondition() != null) {
                    if (item.getOrderCondition().getServicePoint() != null && item.getOrderCondition().getServicePoint().getId() != null) {
                        ServicePoint servicePoint = servicePointService.getFromCache(item.getOrderCondition().getServicePoint().getId());
                        if (servicePoint != null) {
                            item.getOrderCondition().setServicePoint(servicePoint);
                        }
                        if (item.getOrderCondition().getEngineer() != null && item.getOrderCondition().getEngineer().getId() != null) {
                            Engineer engineer = engineerMap.get(item.getOrderCondition().getEngineer().getId());  // add on 2019-11-1
                            if (engineer != null) {
                                item.getOrderCondition().setEngineer(new User(engineer.getId(), engineer.getName(), engineer.getContactInfo()));
                            }
                        }
                    }
                    if (item.getOrderCondition().getKefu() != null && item.getOrderCondition().getKefu().getId() != null) {
                        User kefu = kefuMap.get(item.getOrderCondition().getKefu().getId());
                        if (kefu != null) {
                            item.getOrderCondition().setKefu(kefu);
                        }
                    }
                }
                Dict statusDict = statusMap.get(item.getOrderCondition().getStatus().getValue());
                if (statusDict != null) {
                    item.getOrderCondition().setStatus(statusDict);
                }
                Dict pendingTypeDict = pendingTypeMap.get(item.getOrderCondition().getPendingType().getValue());
                if (pendingTypeDict != null) {
                    item.getOrderCondition().setPendingType(pendingTypeDict);
                }
                UrgentLevel urgentLevel = urgentLevelMap.get(item.getOrderCondition().getUrgentLevel().getId());
                if (urgentLevel != null) {
                    item.getOrderCondition().setUrgentLevel(urgentLevel);
                }
            }
            OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
            setServicePointComplainFlags(servicePointId, orderList);
        }
    }

    /**
     * 读取网点订单投诉标记
     */
    private void setServicePointComplainFlags(Long servicePointId, List<Order> orderList) {
        if (servicePointId != null && servicePointId > 0 && orderList != null && !orderList.isEmpty()) {
            List<Long> orderIds = orderList.stream().filter(i -> i.getId() != null).map(LongIDBaseEntity::getId).collect(Collectors.toList());
            if (!orderIds.isEmpty()) {
                List<Long> resultOrderIds = complainDao.getOrderIdList(servicePointId, orderIds);
                if (!resultOrderIds.isEmpty()) {
                    Map<Long, Order> orderMap = orderList.stream().filter(i -> i.getId() != null).collect(Collectors.toMap(LongIDBaseEntity::getId, i -> i));
                    Order order;
                    for (Long orderId : resultOrderIds) {
                        order = orderMap.get(orderId);
                        if (order != null) {
                            //order.getOrderCondition().setIsComplained(2);
                            order.getOrderStatus().setComplainFlag(2);
                        }
                    }
                }
            }
        }
    }
}
