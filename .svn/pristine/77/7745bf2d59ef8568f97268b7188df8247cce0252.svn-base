/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.CacheDataTypeEnum;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.EngineerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.ServiceLeaderOrderDao;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
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
 * 客服主管订单Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServiceLeaderOrderService extends LongIDBaseService {

    @Resource
    protected ServiceLeaderOrderDao serviceLeaderOrderDao;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private EngineerService engineerService;

    @Autowired
    private MSEngineerService msEngineerService;
    

    /**
     * 爽约工单列表
     */
    public Page<Order> getBrokeAppointmentOrderlist(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        if (searchModel.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                searchModel.setQuarters(quarters);
            }
        }
        List<Order> orderList;
        if (searchModel.getOrderNoSearchType() == 1 || searchModel.getIsPhone() == 1) {
            orderList = serviceLeaderOrderDao.getBrokeAppointmentOrderlistNew(searchModel);
        } else {
            searchModel = getEngineerNameAndContactInfo(searchModel);  // add on 2019-10-29
            orderList = serviceLeaderOrderDao.getBrokeAppointmentOrderlist(searchModel);
        }
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        if (orderList != null && !orderList.isEmpty()) {
            setOrderProperties(orderList);
            setComplainFormStatus(orderList);
            rtnPage.setList(orderList);
        }

        return rtnPage;
    }

    private OrderSearchModel getEngineerNameAndContactInfo(OrderSearchModel searchModel) {
        // add on 2019-10-29
        if (searchModel.getEngineer() != null) {
            if (StringUtils.isNotBlank(searchModel.getEngineer().getName()) || StringUtils.isNotBlank(searchModel.getEngineer().getPhone())) {
                Engineer engineer = new Engineer();
                engineer.setName(searchModel.getEngineer().getName());
                engineer.setContactInfo(searchModel.getEngineer().getPhone());
                List<Long> engineerIds = engineerService.findPagingIdWithNameOrPhone(engineer);
                if (engineerIds != null && !engineerIds.isEmpty()) {
                    searchModel.setEngineerIds(engineerIds);
                }
            }
        }
        return searchModel;
    }

    private void setComplainFormStatus(List<Order> orderList) {
        if (orderList != null && !orderList.isEmpty()) {
            Map<String, List<Long>> orderIdMap = Maps.newHashMap();
            for (Order item : orderList) {
                // 2019-08-29 //投诉标识转移到orderStatus
                /*if (item.getOrderCondition().getIsComplained() > 0) {
                    if (orderIdMap.containsKey(item.getQuarter())) {
                        orderIdMap.get(item.getQuarter()).add(item.getId());
                    } else {
                        orderIdMap.put(item.getQuarter(), Lists.newArrayList(item.getId()));
                    }
                }*/
                if (item.getOrderStatus().getComplainFlag() > 0) {
                    if (orderIdMap.containsKey(item.getQuarter())) {
                        orderIdMap.get(item.getQuarter()).add(item.getId());
                    } else {
                        orderIdMap.put(item.getQuarter(), Lists.newArrayList(item.getId()));
                    }
                }
            }
            List<LongTwoTuple> complainStatusList = null;
            Map<Long, Long> allComplainStatusMap = Maps.newHashMap();
            for (Map.Entry<String, List<Long>> item : orderIdMap.entrySet()) {
                complainStatusList = serviceLeaderOrderDao.getComplainStatusByOrderIds(item.getKey(), item.getValue());
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
                Map<String, Dict> complainStatusDict = MSDictUtils.getDictMap(Dict.DICT_TYPE_COMPLAIN_STATUS);
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

    private void setOrderProperties(List<Order> orderList) {
        if (orderList != null && !orderList.isEmpty()) {
            Map<String, Dict> orderStatusMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_STATUS);
            Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_DATA_SOURCE);
            List<Long> kefuIds = orderList.stream().filter(i -> i.getOrderCondition() != null && i.getOrderCondition().getKefu() != null && i.getOrderCondition().getKefu().getId() != null)
                    .map(i -> i.getOrderCondition().getKefu().getId()).collect(Collectors.toList());
            Map<Long, String> kefuNameMap = MSUserUtils.getNamesByUserIds(kefuIds);
            Map<Long, ServicePoint> servicePointMap = Maps.newHashMap();
            Map<Long, Engineer> engineerMap = Maps.newHashMap();
            Dict orderStatusDict = null;
            Dict dataSourceDict = null;
            ServicePoint servicePoint = null;
            Engineer engineer = null;
            Long servicePointId = null;
            Long engineerId = null;
            String kefuName = null;
            List<OrderItem> orderItemList = Lists.newArrayList();

            if (orderList != null && orderList.size() >0) {
                List<Long> engineerIds = orderList.stream().filter(r->r.getOrderCondition().getEngineer()!=null && r.getOrderCondition().getEngineer().getId()!=null)
                        .map(r->r.getOrderCondition().getEngineer().getId()).distinct().collect(Collectors.toList());
                engineerMap = msEngineerService.findEngineersByIdsToMap(engineerIds, Arrays.asList("id","name","contactInfo"));
            }
            for (Order item : orderList) {
                orderStatusDict = orderStatusMap.get(item.getOrderCondition().getStatus().getValue());
                if (orderStatusDict != null) {
                    item.getOrderCondition().setStatus(orderStatusDict);
                }
                dataSourceDict = dataSourceMap.get(item.getDataSource().getValue());
                if (dataSourceDict != null) {
                    item.setDataSource(dataSourceDict);
                }
                if (item.getOrderCondition() != null && item.getOrderCondition().getKefu() != null && item.getOrderCondition().getKefu().getId() != null) {
                    kefuName = kefuNameMap.get(item.getOrderCondition().getKefu().getId());
                    if (kefuName != null) {
                        item.getOrderCondition().getKefu().setName(kefuName);
                    }
                }
                item.setItems(OrderItemUtils.pbToItems(item.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
                orderItemList.addAll(item.getItems());

                servicePointId = item.getOrderCondition().getServicePoint() == null ? null : item.getOrderCondition().getServicePoint().getId();
                engineerId = item.getOrderCondition().getEngineer() == null ? null : item.getOrderCondition().getEngineer().getId();
                if (servicePointId != null && servicePointId > 0) {
                    servicePoint = servicePointMap.get(servicePointId);
                    if (servicePoint != null) {
                        item.getOrderCondition().setServicePoint(servicePoint);
                    } else {
                        servicePoint = servicePointService.getFromCache(servicePointId);
                        if (servicePoint != null) {
                            item.getOrderCondition().setServicePoint(servicePoint);
                            servicePointMap.put(servicePointId, servicePoint);
                        }
                    }
                    if (engineerId != null && engineerId > 0) {
                        engineer = engineerMap.get(engineerId);
                        if (engineer != null) {
                            item.getOrderCondition().setEngineer(new User(engineer.getId(), engineer.getName(), engineer.getContactInfo()));
                        }
                    }
                }
            }
            OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
        }
    }

}
