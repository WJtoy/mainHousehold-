/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sd.dao.KefuOrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderAttachmentDao;
import com.wolfking.jeesite.modules.sd.dao.OrderComplainDao;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客服订单Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KefuOrderService extends OrderRegionService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderDao dao;

    @Resource
    protected KefuOrderDao kefuOrderDao;

    @Resource
    protected CustomerFinanceDao customerFinanceDao;

    @Resource
    protected CustomerCurrencyDao customerCurrencyDao;

    @Resource
    protected ServicePointDao servicePointDao;

    @Resource
    protected OrderAttachmentDao attachmentDao;

    @Resource
    protected OrderComplainDao complainDao;

    @Resource
    protected UserDao userDao;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;

    //endregion 消息队列

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private MSEngineerService msEngineerService;


    /**
     * 分页查询客服负责的订单-处理中
     2019/08/29 ryan
    public Page<Order> findKefuProcessOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuProcessOrderList(entity);
        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    /**
     * 分页查询客服负责的订单-完成订单
     2019/08/29 ryan
    public Page<Order> findKefuFinishOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                               boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuFinishOrderList(entity);

        return getOrderItems(page, list, getComplainFormStatus, true);
    }*/

    /**
     * 分页查询客服负责的订单-取消订单
     2019/08/29 ryan
    public Page<Order> findKefuCancelOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                               boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuCancelOrderList(entity);

        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    /**
     * 分页查询客服负责的订单-退回订单
     2019/08/29 ryan
    public Page<Order> findKefuReturnOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                               boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuReturnOrderList(entity);

        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    /**
     * 分页查询客服负责的订单-所有订单
     2019/08/29 ryan
    public Page<Order> findKefuAllOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                            boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuAllOrderList(entity);

        return getOrderItems(page, list, getComplainFormStatus, true);
    }*/


    /**
     * 分页查询客服负责的订单-停滞订单
     2019/08/29 ryan
    public Page<Order> findKefuPendingOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuPendingOrderList(entity);
        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    /**
     * 分页查询客服负责的订单-已预约订单
     2019/08/29 ryan
    public Page<Order> findKefuAppointedOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                  boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuAppointedOrderList(entity);
        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    /**
     * 分页查询客服负责的订单-待派单
     */
    public Page<Order> findKefuPlaningOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = Lists.newArrayList();  //kefuOrderDao.findKefuPlaningOrderList(entity); //mark on 2020-7-31
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-突击中
     2019/08/29 ryan
    public Page<Order> findKefuRushingOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuRushingOrderList(entity);
        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    /**
     * 分页查询客服负责的订单-投诉单
     2019/08/29 ryan
    public Page<Order> findKefuComplainOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                 boolean getComplainFormStatus) {
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderDao.findKefuComplainOrderList(entity);
        return getOrderItems(page, list, getComplainFormStatus, false);
    }*/

    //region 公共方法

    private Page<Order> getOrderItems(Page<OrderSearchModel> page, List<Order> list, boolean readComplainFormStatus, boolean readServicePointFinance) {
        Page<Order> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (list != null && list.size() > 0) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            Map<String, ArrayList<Long>> compalinOrderIds = Maps.newHashMap();//有投诉订单id,按分片分开存储和查询
            Order order;
            Integer status;
            String quarter = new String("");
            ServicePoint servicePoint;
            Engineer engineer;
            User engineerAccount;
            //Set<Long> servicePointIds = Sets.newHashSet();
            Map<String, Dict> statusMaps = MSDictUtils.getDictMap("order_status");
            Map<String, Dict> pendingTypes = MSDictUtils.getDictMap("PendingType");
            Map<String, Dict> dataSources = MSDictUtils.getDictMap("order_data_source");
            Set<Long> servicePointIds = Sets.newHashSet();
            Dict dictStatus;
            Dict pendingType;
            OrderCondition orderCondition;
            Map<Long, UrgentLevel> urgentLeves = urgentLevelService.findAllMap();
            int statusValue;
            UrgentLevel urgentLevel;
            // add on 2019-10-31 begin
            List<Long> engineerIds = list.stream().filter(order1 -> order1.getOrderCondition().getEngineer() != null && order1.getOrderCondition().getEngineer().getId() != null)
                    .map(order1 -> order1.getOrderCondition().getEngineer().getId()).distinct().collect(Collectors.toList());
            Map<Long,String> engineerMap = MDUtils.getEngineerNamesByIds(engineerIds);
            // add on 2019-10-31 end
            for (int i = 0, len = list.size(); i < len; i++) {
                order = list.get(i);
                order.setItems(OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson()));
                orderItemList.addAll(order.getItems());
                quarter = order.getQuarter();
//                order.setItems(orderItems.get(order.getId()));
                order.setDataSource(dataSources.get(order.getDataSource().getValue()));
                //dictStatus = order.getOrderCondition().getStatus();
                orderCondition = order.getOrderCondition();
                dictStatus = statusMaps.get(orderCondition.getStatus().getValue());
                if (dictStatus != null) {
                    orderCondition.getStatus().setLabel(dictStatus.getLabel());
                }
                statusValue = orderCondition.getStatus().getIntValue();
                if (statusValue == Order.ORDER_STATUS_COMPLETED || statusValue == Order.ORDER_STATUS_CHARGED) {
                    if (order.getOrderCondition().getChargeFlag() == 1) {
                        //已生成对账单(厂商)
                        orderCondition.getStatus().setLabel("已入账");//2017-09-26 订单生成对账单就显示:已入账
                    } else if (orderCondition.getPendingFlag() == 1) {
                        //财务标记异常
                        orderCondition.getStatus().setLabel("异常");
                    }
                }
                if (orderCondition.getPendingType().getIntValue() > 0) {
                    pendingType = pendingTypes.get(orderCondition.getPendingType().getValue());
                    if (pendingType != null) {
                        orderCondition.setPendingType(pendingType);
                    }
                }
                //dataSource
                Dict dataSource;
                if (order.getDataSource() == null || order.getDataSource().getIntValue() == 0) {
                    dataSource = MSDictUtils.getDictByValue("1", Order.ORDER_DATA_SOURCE_TYPE);//快可立
                } else {
                    dataSource = MSDictUtils.getDictByValue(order.getDataSource().getValue(), Order.ORDER_DATA_SOURCE_TYPE);
                }
                if (dataSource != null) {
                    order.setDataSource(dataSource);
                } else {
                    order.setDataSource(new Dict("0", ""));
                }
                //shop/店铺
                if (order.getDataSource().getIntValue() > 1 && order.getB2bShop() != null && StringUtils.isNotBlank(order.getB2bShop().getShopId())) {
                    B2bCustomerMap b2bCustomerMap = b2bCustomerMapService.getShopInfo(order.getDataSource().getIntValue(), order.getB2bShop().getShopId());
                    if (b2bCustomerMap != null) {
                        order.setB2bShop(b2bCustomerMap);
                    }
                }

                //加急
                if (orderCondition.getUrgentLevel().getId().longValue() > 0) {
                    urgentLevel = urgentLeves.get(orderCondition.getUrgentLevel().getId());
                    if (urgentLevel != null) {
                        orderCondition.setUrgentLevel(urgentLevel);
                    }
                }
                //投诉转到 orderStatus
            /*    if (readComplainFormStatus && orderCondition.getIsComplained() > 0) {
                    if (compalinOrderIds.containsKey(quarter)) {
                        compalinOrderIds.get(quarter).add(order.getId());
                    } else {
                        compalinOrderIds.put(quarter, Lists.newArrayList(order.getId()));
                    }
                }*/
                if (readComplainFormStatus && order.getOrderStatus().getComplainFlag()>0) {
                    if (compalinOrderIds.containsKey(quarter)) {
                        compalinOrderIds.get(quarter).add(order.getId());
                    } else {
                        compalinOrderIds.put(quarter, Lists.newArrayList(order.getId()));
                    }
                }
                servicePoint = orderCondition.getServicePoint();
                if (servicePoint != null && servicePoint.getId() != null && servicePoint.getId() > 0) {
                    //完成单
                    //if(orderCondition.getStatus().getIntValue() == Order.ORDER_STATUS_COMPLETED){
                    servicePointIds.add(servicePoint.getId());
                    //}
                    servicePoint = servicePointService.getFromCache(servicePoint.getId());
                    if (servicePoint != null) {
                        orderCondition.setServicePoint(servicePoint);
                    }
                    engineerAccount = orderCondition.getEngineer();
                    if (engineerAccount != null && engineerAccount.getId() > 0) {
                        /*
                        // mark on 2019-10-31
                        engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), engineerAccount.getId());
                        if (engineer != null) {
                            orderCondition.getEngineer().setName(engineer.getName());
                        }
                        */
                        orderCondition.getEngineer().setName(engineerMap.get(engineerAccount.getId())); // add on 2019-10-31 //Engineer微服务
                    }
                }
            }

            OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));

            //投诉单状态查询
            try {
                getComplainFormStatus(list, compalinOrderIds);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            //网点余额
            if (readServicePointFinance && servicePointIds.size() > 0) {
                List<Map<String, Object>> balances = servicePointService.getServicePointBalances(Lists.newArrayList(servicePointIds));
                if (balances != null && balances.size() > 0) {
                    for (int i = 0, len = balances.size(); i < len; i++) {
                        final Long spId = (Long) balances.get(i).get("id");
                        final BigDecimal balance = (BigDecimal) balances.get(i).get("balance");
                        final Date lastPayDate = (Date) balances.get(i).get("last_pay_date");
                        final BigDecimal lastPayAmount = (BigDecimal) balances.get(i).get("last_pay_amount");
                        list.stream().filter(t -> t.getOrderCondition().getServicePoint() != null && t.getOrderCondition().getServicePoint().getId().equals(spId))
                                .forEach(o -> {
                                    o.getOrderCondition().getServicePoint().getFinance().setBalance(balance.doubleValue());
                                    o.getOrderCondition().getServicePoint().getFinance().setLastPayDate(lastPayDate);
                                    o.getOrderCondition().getServicePoint().getFinance().setLastPayAmount(lastPayAmount.doubleValue());
                                });
                    }
                }
            }
            rtnPage.setList(list);
        }
        return rtnPage;
    }

    /**
     * 根据分片及订单id获得投诉单状态
     *
     * @param orders
     * @param compalinOrderIds
     */
    private void getComplainFormStatus(List<Order> orders, Map<String, ArrayList<Long>> compalinOrderIds) {
        if (compalinOrderIds.size() > 0) {
            Integer status;
            Order order;
            List<Dict> statusList = MSDictUtils.getDictList("complain_status");//切换为微服务
            if (statusList != null || statusList.size() > 0) {
                Map<String, Dict> dictMaps = statusList.stream().collect(Collectors.toMap(Dict::getValue, item -> item));
                Dict dict;
                List<Map<String, Object>> complainStatus;
                Map<String, Object> map;
                for (Map.Entry<String, ArrayList<Long>> entry : compalinOrderIds.entrySet()) {
                    complainStatus = complainDao.getOrderComplainStatusByOrderIds(entry.getKey(), entry.getValue());
                    if (complainStatus != null && complainStatus.size() > 0) {
                        for (int i = 0, size = complainStatus.size(); i < size; i++) {
                            map = complainStatus.get(i);
                            final Long orderId = (Long) map.get("order_id");
                            status = (Integer) map.get("status");
                            //dict = dictMaps.get(status.toString());
                            order = orders.stream().filter(t -> t.getId().equals(orderId)).findFirst().orElse(null);
                            if (order != null) {
                                order.setComplainFormStatus(dictMaps.get(status.toString()));
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * 查询退单待审核列表
     */
    public Page<Order> getOrderReturnApproveList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        List<Order> orderList = kefuOrderDao.getOrderReturnApproveList(searchModel);
        List<Long> cancelApplyByIdList = orderList.stream().map(order -> order.getOrderStatus().getCancelApplyBy().getId()).collect(Collectors.toList());
        Map<Long, String> names = MSUserUtils.getNamesByUserIds(cancelApplyByIdList);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList != null && orderList.size() > 0) {
            Map<String, Dict> cancelResponsibleMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
            Dict cancelResponsibleDict = null;
            for (Order item : orderList) {
                if (item.getOrderStatus().getCancelResponsible() != null && item.getOrderStatus().getCancelResponsible().getValue() != null) {
                    cancelResponsibleDict = cancelResponsibleMap.get(item.getOrderStatus().getCancelResponsible().getValue());
                    if (cancelResponsibleDict != null) {
                        item.getOrderStatus().setCancelResponsible(cancelResponsibleDict);
                    }
                }
                if (names.get(item.getOrderStatus().getCancelApplyBy().getId()) != null){
                    item.getOrderStatus().getCancelApplyBy().setName(names.get(item.getOrderStatus().getCancelApplyBy().getId()));
                }
            }
            rtnPage.setList(orderList);
        }
        return rtnPage;
    }

    /**  合并到：getOrderReturnApproveList
     * 查询新迎燕B2B退单待审核列表

    public Page<Order> getXYYOrderReturnApproveList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        List<Order> orderList = kefuOrderDao.getXYYOrderReturnApproveList(searchModel);
        List<Long> cancelApplyByIdList = orderList.stream().map(order -> order.getOrderStatus().getCancelApplyBy().getId()).collect(Collectors.toList());
        Map<Long, String> names = MSUserUtils.getNamesByUserIds(cancelApplyByIdList);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (!orderList.isEmpty()) {
            Map<String, Dict> cancelResponsibleMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
            Dict cancelResponsibleDict;
            for (Order item : orderList) {
                if (item.getOrderStatus().getCancelResponsible() != null && item.getOrderStatus().getCancelResponsible().getValue() != null) {
                    cancelResponsibleDict = cancelResponsibleMap.get(item.getOrderStatus().getCancelResponsible().getValue());
                    if (cancelResponsibleDict != null) {
                        item.getOrderStatus().setCancelResponsible(cancelResponsibleDict);
                    }
                }
                if (names.get(item.getOrderStatus().getCancelApplyBy().getId()) != null){
                    item.getOrderStatus().getCancelApplyBy().setName(names.get(item.getOrderStatus().getCancelApplyBy().getId()));
                }
            }
            rtnPage.setList(orderList);
        }
        return rtnPage;
    }
     */
    /**
     * 异常处理列表
     */
    public Page<Order> getExceptionHandlingList(Page<OrderPendingSearchModel> page, OrderPendingSearchModel searchModel) {
        searchModel.setPage(page);

        List<Order> orderList = kefuOrderDao.getExceptionHandlingList(searchModel);
        List<Long> kefuIdList = orderList.stream().map(order -> order.getOrderCondition().getKefu().getId()).collect(Collectors.toList());
        Map<Long, String> names = MSUserUtils.getNamesByUserIds(kefuIdList);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        if (orderList != null && !orderList.isEmpty()) {
            Map<String, Dict> orderStatusMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_STATUS);
            Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_DATA_SOURCE);
            Map<Long, Customer> customerMap = Maps.newHashMap();
            Map<String, List<Long>> orderIdMap = Maps.newHashMap();
            Customer customer = null;
            Long customerId = null;
            Dict orderStatusDict = null;
            Dict dataSourceDict = null;
            for (Order item : orderList) {
                if (orderIdMap.containsKey(item.getQuarter())) {
                    orderIdMap.get(item.getQuarter()).add(item.getId());
                } else {
                    orderIdMap.put(item.getQuarter(), Lists.newArrayList(item.getId()));
                }
                customerId = item.getOrderCondition().getCustomer().getId();
                customer = customerMap.get(customerId);
                if (customer != null) {
                    item.getOrderCondition().setCustomer(customer);
                } else {
                    customer = CustomerUtils.getCustomer(customerId);
                    if (customer != null) {
                        item.getOrderCondition().setCustomer(customer);
                        customerMap.put(customer.getId(), customer);
                    }
                }
                orderStatusDict = orderStatusMap.get(item.getOrderCondition().getStatus().getValue());
                if (orderStatusDict != null) {
                    item.getOrderCondition().setStatus(orderStatusDict);
                }
                dataSourceDict = dataSourceMap.get(item.getDataSource().getValue());
                if (dataSourceDict != null) {
                    item.setDataSource(dataSourceDict);
                }
                if (names.get(item.getOrderCondition().getKefu().getId()) != null){
                    item.getOrderCondition().getKefu().setName(names.get(item.getOrderCondition().getKefu().getId()));
                }
            }

            Map<Long, List<OrderDetail>> detailMap = Maps.newHashMap();
            List<OrderDetail> subDetailList = null;
            Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
            Map<Long, Product> productMap = ProductUtils.getAllProductMap();
            Map<Long, Engineer> engineerMap = Maps.newHashMap();
            ServiceType serviceType = null;
            Product product = null;
            Engineer engineer = null;
            for (Map.Entry<String, List<Long>> entry : orderIdMap.entrySet()) {
                subDetailList = kefuOrderDao.getOrderDetailByOrderIds(entry.getKey(), entry.getValue());
                if (subDetailList != null && !subDetailList.isEmpty()) {
                    // add on 2019-11-1 begin
                    List<Long> engineerIds = subDetailList.stream().filter(r->r.getEngineer() != null && r.getEngineer().getId() != null)
                            .map(r->r.getEngineer().getId()).distinct().collect(Collectors.toList());
                    Map<Long,String> engineerNamesMap = MDUtils.getEngineerNamesByIds(engineerIds);
                    // add on 2019-11-1 end
                    for (OrderDetail detail : subDetailList) {
                        if (detailMap.containsKey(detail.getOrderId())) {
                            detailMap.get(detail.getOrderId()).add(detail);
                        } else {
                            detailMap.put(detail.getOrderId(), Lists.newArrayList(detail));
                        }

                        serviceType = serviceTypeMap.get(detail.getServiceType().getId());
                        if (serviceType != null) {
                            detail.setServiceType(serviceType);
                        }
                        product = productMap.get(detail.getProduct().getId());
                        if (product != null) {
                            detail.setProduct(product);
                        }
                        /*
                        // mark on 2019-11-1
                        engineer = engineerMap.get(detail.getEngineer().getId());
                        if (engineer != null) {
                            detail.setEngineer(engineer);
                        } else {
                            engineer = servicePointService.getEngineerFromCache(detail.getServicePoint().getId(), detail.getEngineer().getId());
                            if (engineer != null) {
                                detail.setEngineer(engineer);
                                engineerMap.put(engineer.getId(), engineer);
                            }
                        }
                        */
                        detail.getEngineer().setName(engineerNamesMap.get(detail.getEngineer().getId())); // add on 2019-11-1
                    }
                }
            }
            for (Order item : orderList) {
                subDetailList = detailMap.get(item.getId());
                if (subDetailList != null) {
                    item.setDetailList(subDetailList);
                }
            }
            rtnPage.setList(orderList);
        }

        return rtnPage;
    }

    //endregion 公共方法
}
