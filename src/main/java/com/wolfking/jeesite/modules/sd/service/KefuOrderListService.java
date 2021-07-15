/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.kkl.kklplus.entity.cc.vm.ReminderTimeLinessModel;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.dao.KefuOrderListDao;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderHeadDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderServiceItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import com.wolfking.jeesite.ms.cc.entity.mapper.OrderReminderVMMapper;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourseImage;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallServiceMonitorSearchVM;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

/**
 * 客服订单列表Service
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KefuOrderListService extends OrderRegionService {

    //地址限制查询key生存周期(秒)
    public static final long ADDRESS_QUERY_LOCK_TIMEOUT = 3 * 60;

    @Resource
    private KefuOrderListDao kefuOrderListDao;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao dao;

    @Autowired
    private OrderHeadDao orderHeadDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Value("${queryLimit.address}")
    private long addressQueryLimit;

    @Autowired
    private EngineerService engineerService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private MSServicePointService msServicePointService;

    //region 普通列表

    /**
     * 限制按地址查询处理
     *
     * @return Key
     * false: 不允许查询
     * "key": 本次可以查询，已加锁
     * ""：本次可以查询，不按地址查询
     */
    private String canQueryByAddress(OrderSearchModel searchModel) {
        //不限制
        if (addressQueryLimit <= 0) {
            return "";
        }
        //未按地址查询
        if (StringUtils.isBlank(searchModel.getServiceAddress())) {
            return "";
        }
        Set<byte[]> keys = redisUtils.scan(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, "*", addressQueryLimit);
        if (keys == null || keys.size() < addressQueryLimit) {
            String key = UUID.randomUUID().toString();
            redisUtils.set(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key, System.currentTimeMillis(), ADDRESS_QUERY_LOCK_TIMEOUT);
            return key;
        }
        return "false";
    }

    /**
     * 分页查询客服负责的订单-待接单/待派单
     */
    public Page<Order> findKefuPlaningOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderListDao.findKefuPlaningOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-未预约
     */
    public Page<Order> findKefuNoAppointmentOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                      boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        Date now = new Date();
        entity.setPage(page);
        entity.setNow(now);
        entity.setPlanDateBegin(DateUtils.addHour(now, -2));
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderListDao.findKefuNoAppointmentOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-预约到期
     */
    public Page<Order> findKefuArriveAppointmentOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                          boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        Date now = new Date();
        Date beginAppointmentDate = null;
        //允许改约规则： 预约下午17点前（含） ， 当天23点前改约； 预约下午17点以后的， 第二天23点前完成改约。
        if (now.getTime() < DateUtils.getDate(now, 23, 0, 0).getTime()) {
            beginAppointmentDate = DateUtils.getDate(DateUtils.addDays(now, -1), 17, 0, 0);
        } else {
            beginAppointmentDate = DateUtils.getDate(now, 17, 0, 0);
        }
        entity.setStartOfToday(beginAppointmentDate);
        entity.setNow(now);
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderListDao.findKefuArriveAppointmentOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-预约超期(不允许改约)
     */
    public Page<Order> findKefuPassAppointmentOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                        boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        Date now = new Date();
        Date endAppointmentDate = null;
        //允许改约规则： 预约下午17点前（含） ， 当天23点前改约； 预约下午17点以后的， 第二天23点前完成改约。
        if (now.getTime() < DateUtils.getDate(now, 23, 0, 0).getTime()) {
            endAppointmentDate = DateUtils.getDate(DateUtils.addDays(now, -1), 17, 0, 0);
        } else {
            endAppointmentDate = DateUtils.getDate(now, 17, 0, 0);
        }
        entity.setStartOfToday(endAppointmentDate);
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }

        List<Order> list = kefuOrderListDao.findKefuPassAppointmentOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-停滞订单
     */
    public Page<Order> findKefuPendingOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        Date now = new Date();
        entity.setPage(page);
        entity.setNow(now);
        entity.setStartOfToday(DateUtils.getStartOfDay(now));
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuPendingOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-待回访
     */
    public Page<Order> findKefuServicedOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                 boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        Date now = new Date();
        entity.setPage(page);
        entity.setNow(now);
        entity.setStartOfToday(DateUtils.getStartOfDay(now));
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuServicedOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-回访失败
     */
    public Page<Order> findKefuFollowUpFailOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                     boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        Date now = new Date();
        entity.setPage(page);
        entity.setNow(now);
        entity.setStartOfToday(DateUtils.getStartOfDay(now));
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuFollowUpFailOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-未完成
     */
    public Page<Order> findKefuUncompletedOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                    boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        entity.setStartOfToday(DateUtils.getStartOfDay(new Date()));
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuUncompletedOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-所有订单
     */
    public Page<Order> findKefuAllOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                            boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuAllOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, true);
    }

    /**
     * 分页查询客服负责的订单-完成订单
     */
    public Page<Order> findKefuCompletedOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                  boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuCompletedOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, true);
    }

    //endregion 普通列表

    //region 特殊列表

    /**
     * 分页查询客服负责的订单 - 天猫求助列表
     */
    public Page<TmallAnomalyRecourse> findKefuTmallAnomalyList(Page<TmallAnomalyRecourseSearchVM> page, TmallAnomalyRecourseSearchVM entity) {
        entity.setPage(page);
        List<TmallAnomalyRecourse> list = kefuOrderListDao.findKefuTmallAnomalyList(entity);
        Page<TmallAnomalyRecourse> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (list != null && !list.isEmpty()) {
            List<TmallAnomalyRecourseImage> images;
            Map<String, Dict> questionTypeMaps = MSDictUtils.getDictMap("AnomalyQuestionType");
            for (TmallAnomalyRecourse m : list) {
                images = GsonUtils.getInstance().getGson().fromJson(m.getRecourseJson(), new TypeToken<List<TmallAnomalyRecourseImage>>() {
                }.getType());
                m.setRecourseList(images);
                if (questionTypeMaps != null && !questionTypeMaps.isEmpty() && m.getQuestionType() != null && StringUtils.isNotBlank(m.getQuestionType().getValue())) {
                    m.setQuestionType(questionTypeMaps.get(m.getQuestionType().getValue()));
                }
            }
            rtnPage.setList(list);
        }
        return rtnPage;
    }

    /**
     * 分页查询客服负责的订单 - 天猫预警列表
     */
    public Page<TmallServiceMonitor> findKefuTmallServiceMonitorList(Page<TmallServiceMonitor> page, TmallServiceMonitorSearchVM entity) {
        //entity.setPage(new Page<>(page.getPageNo(),page.getPageSize(),page.getCount()));
        Page<TmallServiceMonitorSearchVM> searchPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        entity.setPage(searchPage);
        List<TmallServiceMonitor> list = kefuOrderListDao.findKefuTmallServiceMonitorList(entity);
        Page<TmallServiceMonitor> rtnPage = new Page<>();
        rtnPage.setPageNo(searchPage.getPageNo());
        rtnPage.setPageSize(searchPage.getPageSize());
        rtnPage.setCount(searchPage.getCount());
        rtnPage.setOrderBy(searchPage.getOrderBy());
        if (list != null && !list.isEmpty()) {
            rtnPage.setList(list);
        }
        return rtnPage;
    }

    /**
     * 分页查询客服负责的订单-突击中
     */
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

        List<Order> list = kefuOrderListDao.findKefuRushingOrderList(entity);
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-投诉单
     */
    public Page<Order> findKefuComplainOrderList(Page<OrderSearchModel> page, OrderSearchModel entity,
                                                 boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findKefuComplainOrderList(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        return getOrderItems(page, list, getComplainFormStatus, false);
    }

    /**
     * 分页查询客服负责的订单-催单
     */
    public Page findReminderOrderLit(Page<OrderSearchModel> page, OrderSearchModel entity,
                                     boolean getComplainFormStatus) {
        String key = canQueryByAddress(entity);
        if (key.equalsIgnoreCase("false")) {
            throw new RuntimeException("已有很多按地址查询任务，请稍后查询；或去除地址重新查询");
        }
        entity.setPage(page);
        entity.setNow(new Date());
        if (entity.getBeginDate() != null) {
            Date[] dates = OrderUtils.getQuarterDates(entity.getBeginDate(), entity.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                entity.setQuarters(quarters);
            }
        }
        List<Order> list = kefuOrderListDao.findReminderOrderLit(entity);
        if (StringUtils.isNoneBlank(key)) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_ADDRESS_QUERY_LOCK_DB, key);
        }
        Page<Order> rtnPage = getOrderItems(page, list, getComplainFormStatus, false);
        // 催单时效
        List vmList = getReminderTimeliness(rtnPage.getList());
        return new Page<>(rtnPage.getPageNo(),rtnPage.getPageSize(),rtnPage.getCount(),vmList);
    }

    /**
     * 根据区县或街道ID读取同区域以往派单记录
     * for客服
     */
    public Page getHistoryPlanListForKefu(Page page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        //quarter
        if(searchModel.getBeginDate() != null && searchModel.getEndDate() != null){
            Date[] dates = OrderUtils.getQuarterDates(searchModel.getBeginDate(), searchModel.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if(!org.springframework.util.ObjectUtils.isEmpty(quarters)){
                searchModel.setQuarters(quarters);
            }
        }
        List<HistoryPlanOrderModel> list = kefuOrderListDao.findHistoryPlaningOrderList(searchModel);
        if(list == null){
            list = Lists.newArrayList();
        }else{
            Map<Long,String> productCategoryMap = Maps.newHashMapWithExpectedSize(page.getPageSize());
            if(searchModel.getProductCategoryId() != null && searchModel.getProductCategoryId()>0){
                ProductCategory productCategory = productCategoryService.getFromCache(searchModel.getProductCategoryId());
                productCategoryMap.put(searchModel.getProductCategoryId(),productCategory==null?"":productCategory.getName());
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
            Map<Long,ServicePoint> servicePointMap = Maps.newHashMapWithExpectedSize(page.getPageSize());
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
                if(model.getStatusValue()==Order.ORDER_STATUS_COMPLETED || model.getStatusValue()==Order.ORDER_STATUS_CHARGED) {
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
            Map<Long,Product> productMap = Maps.newHashMap();
            Product product;
            ServiceType serviceType;
            Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
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
            serviceTypeMap = null;
            serviceDetailMap = null;
            text = null;
            productMap = null;
            models = null;
            itemList = null;
            orderStatusMap = null;
        }
        page.setList(list);
        list = null;
        return page;
    }

    //endregion 特殊列表

    //region 公用方法

    /**
     * 读取催单时效
     */
    public List<OrderReminderVM> getReminderTimeliness(List<Order> list){
        List<OrderReminderVM> vmList = Lists.newArrayList();
        try {
            List<ReminderOrderModel> orderModelList = Lists.newArrayList();
            list.stream().forEach(t -> {
                orderModelList.add(ReminderOrderModel.builder()
                        .orderId(t.getId())
                        .quarter(t.getQuarter()).build()
                );
            });
            OrderReminderVM vm;
            if (!orderModelList.isEmpty()) {
                BulkRereminderCheckModel bulkRereminderCheckModel = new BulkRereminderCheckModel(orderModelList);
                Map<Long, ReminderTimeLinessModel> reminderModels = reminderService.bulkGetReminderTimeLinessByOrders(bulkRereminderCheckModel);
                if (reminderModels != null && reminderModels.size() > 0) {
                    Order order;
                    ReminderTimeLinessModel model;
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(int i=0,size=list.size();i<size;i++){
                        order = list.get(i);
                        vm = OrderReminderVMMapper.INSTANCE.toReminderModel(order);
                        if(reminderModels.containsKey(order.getId())){
                            model = reminderModels.get(order.getId());
                            if(model != null){
                                vm.setReminderId(model.getId());
                                if(model.getCreateAt()>0) {
                                    vm.setReminderDate(DateFormatUtils.format(model.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
                                }
                                double praiseTimeliness = DateUtils.calculateTimeliness(date,model.getTimeoutAt(),startDt,endDt);
                                vm.setCutOffTimeLiness(model.getCutOffTimeLiness());
                                vm.setCutOffLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                            }
                        }
                        vmList.add(vm);
                    }
                }
            }
        }catch (Exception e){
            log.error("读取催单时效错误",e);
        }
        return vmList;
    }

    /**
     * 根据催单时效显示不同文本
     * @param timeliness    时效(小时)
     * @return
     */
    private String getCutOffTimelinessLabel(double timeliness){
        int minutes = (int)(60*timeliness);
        if(minutes<40){
            return (60-minutes) + "分钟后超时";
        }
        if(minutes < 60){
            return (60-minutes) + "分钟后超时";
        }
        return "超时:" + DateUtils.minuteToTimeString(minutes-60,"小时","分");
    }


    private Page<Order> getOrderItems(Page<OrderSearchModel> page, List<Order> list, boolean readComplainFormStatus, boolean readServicePointFinance) {
        Page<Order> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());

        if (!CollectionUtils.isEmpty(list)) {
            Map<String, Dict> orderStatusMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_STATUS);
            Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_PENDING_TYPE);
            Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_DATA_SOURCE);
            TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> allCustomerMappingMaps = B2BMDUtils.getAllCustomerMappingMaps();
            Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
            List<OrderItem> orderItemList = Lists.newArrayList();
            Map<String, List<Long>> complainOrderIds = Maps.newHashMap();
            Set<Long> servicePointIds = Sets.newHashSet();
            String quarter;
            OrderCondition orderCondition;

            int dataSourceValue;
            int statusValue;
            Dict statusDict;
            Dict pendingType;
            UrgentLevel urgentLevel;

            ServicePoint servicePoint;
            Engineer engineer;
            User engineerAccount;
            OrderStatusFlag orderStatusFlag;
            // add on 2019-10-31 begin
            Supplier<Stream<Order>> streamSupplier = () -> list.stream();
            List<Long> engineerIds = streamSupplier.get().filter(order->order.getOrderCondition().getEngineer()!= null && order.getOrderCondition().getEngineer().getId()!=null)
                    .map(order->order.getOrderCondition().getEngineer().getId()).distinct().collect(Collectors.toList());
            Map<Long,Engineer> engineerMap = engineerService.findEngineerListFromCacheToMap(engineerIds);
            // add on 2019-10-31 end
            // add on 2020-11-13 begin  批量获取网点信息
            List<Long> servicePointIdList = streamSupplier.get().filter(order->order.getOrderCondition().getServicePoint() != null && order.getOrderCondition().getServicePoint().getId()!=null && order.getOrderCondition().getServicePoint().getId() !=0 )
                    .map(order->order.getOrderCondition().getServicePoint().getId()).distinct().collect(Collectors.toList());
            Map<Long, ServicePoint> servicePointMap = msServicePointService.findListByIdsFromCacheToMap(servicePointIdList);
            // add on 2020-11-13 end
            Map<String,List<Long>> orderIds = streamSupplier.get()
                    .collect(Collectors.groupingBy(
                            p -> p.getQuarter(),
                            Collectors.mapping(
                                    p -> p.getId(),
                                    Collectors.toList()
                            )
                    ));
            Map<Long, OrderStatusFlag> orderStatusFlagMap = Maps.newHashMapWithExpectedSize(list.size());
            for(Map.Entry<String,List<Long>> entry:orderIds.entrySet()) {
                Map<Long,OrderStatusFlag> subMap = orderStatusFlagService.getStatusFlagMapByOrderIds(entry.getKey(),entry.getValue());
                orderStatusFlagMap.putAll(subMap);
            }
            for (Order order : list) {
                quarter = order.getQuarter();
                orderCondition = order.getOrderCondition();

                //order.setItems(OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson()));
                order.setItems(OrderItemUtils.pbToItems(order.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
                orderItemList.addAll(order.getItems());

                dataSourceValue = StringUtils.toInteger(order.getDataSource().getValue());
                if (dataSourceValue == 0) {
                    dataSourceValue = Order.ORDER_DATA_SOURCE_VALUE_KKL;
                }
                order.setDataSource(dataSourceMap.get(String.format("%d", dataSourceValue)));
                if (B2BDataSourceEnum.isDataSource(dataSourceValue)
                        && order.getB2bShop() != null && StringUtils.isNotBlank(order.getB2bShop().getShopId())) {
                    order.getB2bShop().setDataSource(dataSourceValue);
                    B2BCustomerMapping customerMapping = null;
                    if (B2BDataSourceEnum.isB2BDataSource(dataSourceValue)) {
                        customerMapping = allCustomerMappingMaps.getBElement().get(String.format("%s:%s", dataSourceValue, order.getB2bShop().getShopId()));
                    } else {
                        customerMapping = allCustomerMappingMaps.getAElement().get(order.getB2bShop().getShopId());
                    }
                    if (customerMapping != null && StringUtils.isNotBlank(customerMapping.getShopName())) {
                        order.getB2bShop().setShopName(customerMapping.getShopName());
                    }
                }

                statusValue = StringUtils.toInteger(orderCondition.getStatus().getValue());
                statusDict = orderStatusMap.get(String.format("%d", statusValue));
                if (statusDict != null) {
//                    if (statusValue == Order.ORDER_STATUS_COMPLETED || statusValue == Order.ORDER_STATUS_CHARGED) {
//                        /* 改变对象的值，必须创建一个新的对象，否则会影响到其它引用该对象的地方 */
//                        if (orderCondition.getChargeFlag() != null && orderCondition.getChargeFlag() == 1) {
//                            statusDict = Dict.copyDict(statusDict);
//                            statusDict.setLabel("已入账");
//                        } else if (orderCondition.getPendingFlag() != null && orderCondition.getPendingFlag() == 1) {
//                            statusDict = Dict.copyDict(statusDict);
//                            statusDict.setLabel("异常");
//                        }
//                    }
                    if (statusValue == Order.ORDER_STATUS_COMPLETED && orderCondition.getPendingFlag() != null && orderCondition.getPendingFlag() == 1) {
                        statusDict = Dict.copyDict(statusDict);
                        statusDict.setLabel("异常");
                    }
                    orderCondition.setStatus(statusDict);
                }

                if (orderCondition.getPendingType().getIntValue() > 0) {
                    pendingType = pendingTypeMap.get(orderCondition.getPendingType().getValue());
                    if (pendingType != null) {
                        orderCondition.setPendingType(pendingType);
                    }
                }

                if (orderCondition.getUrgentLevel().getId() > 0) {
                    urgentLevel = urgentLevelMap.get(orderCondition.getUrgentLevel().getId());
                    if (urgentLevel != null) {
                        orderCondition.setUrgentLevel(urgentLevel);
                    }
                }

                //投诉由orderCondition 转移到 orderStatus
               /* if (readComplainFormStatus && orderCondition.getIsComplained() > 0) {
                    if (complainOrderIds.containsKey(quarter)) {
                        complainOrderIds.get(quarter).add(order.getId());
                    } else {
                        complainOrderIds.put(quarter, Lists.newArrayList(order.getId()));
                    }
                }*/
                if (readComplainFormStatus && order.getOrderStatus().getComplainFlag() > 0) {
                    if (complainOrderIds.containsKey(quarter)) {
                        complainOrderIds.get(quarter).add(order.getId());
                    } else {
                        complainOrderIds.put(quarter, Lists.newArrayList(order.getId()));
                    }
                }

                servicePoint = orderCondition.getServicePoint();
                if (servicePoint != null && servicePoint.getId() != null && servicePoint.getId() > 0) {
                    servicePointIds.add(servicePoint.getId());
                    //servicePoint = servicePointService.getFromCacheAsRequired(servicePoint.getId(),null);  // mark on 2020-11-13
                    servicePoint = servicePointMap.get(servicePoint.getId());   // add on 2020-11-13
                    if (servicePoint != null) {
                        orderCondition.setServicePoint(servicePoint);
                        engineerAccount = orderCondition.getEngineer();
                        if (engineerAccount != null && engineerAccount.getId() > 0) {
                            engineer = engineerMap.get(engineerAccount.getId());  //add on 2019-10-31  //Engineer微服务
                            if (engineer != null) {
                                User engineerUser = new User(engineer.getId());
                                engineerUser.setName(engineer.getName());
                                engineerUser.setMobile(engineer.getContactInfo());
                                engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
                                engineerUser.setAppLoged(engineer.getAppLoged());
                                engineerUser.setAppFlag(engineer.getAppFlag());
                                orderCondition.setEngineer(engineerUser);
                            }
                        }
                    }
                }
                //获取好评标识
                orderStatusFlag = orderStatusFlagMap.get(order.getId());
                if(orderStatusFlag!=null){
                    order.setOrderStatusFlag(orderStatusFlag);
                }
            }
            OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
            if (readComplainFormStatus) {
                setComplainFormStatus(list, complainOrderIds);
            }
            if (readServicePointFinance) {
                setServicePointBalance(list, servicePointIds);
            }
            rtnPage.setList(list);
        }
        return rtnPage;
    }

    /**
     * 设置工单的网点余额信息
     */
    private void setServicePointBalance(List<Order> orderList, Set<Long> servicePointIds) {
        if (!orderList.isEmpty() && !servicePointIds.isEmpty()) {
            List<ServicePointFinance> financeList = kefuOrderListDao.getBalanceInfoByServicePointIds(Lists.newArrayList(servicePointIds));
            if (financeList != null && !financeList.isEmpty()) {
                Map<Long, ServicePointFinance> financeMap = financeList.stream().collect(Collectors.toMap(ServicePointFinance::getId, i -> i));
                ServicePointFinance finance;
                for (Order order : orderList) {
                    if (order.getOrderCondition() != null && order.getOrderCondition().getServicePoint() != null
                            && order.getOrderCondition().getServicePoint().getId() != null) {
                        finance = financeMap.get(order.getOrderCondition().getServicePoint().getId());
                        if (finance != null) {
                            order.getOrderCondition().getServicePoint().setFinance(finance);
                        }
                    }
                }
            }
        }
    }


    /**
     * 设置工单的投诉单状态
     */
    private void setComplainFormStatus(List<Order> orderList, Map<String, List<Long>> complainOrderIdMap) {
        if (!orderList.isEmpty() && !complainOrderIdMap.isEmpty()) {
            List<LongTwoTuple> complainStatusList;
            Map<Long, Long> complainStatusMap = Maps.newHashMap();
            for (Map.Entry<String, List<Long>> item : complainOrderIdMap.entrySet()) {
                complainStatusList = kefuOrderListDao.getComplainStatusByOrderIds(item.getKey(), item.getValue());
                if (complainStatusList != null && !complainStatusList.isEmpty()) {
                    for (LongTwoTuple statusItem : complainStatusList) {
                        if (statusItem.getAElement() != null && statusItem.getBElement() != null) {
                            complainStatusMap.put(statusItem.getAElement(), statusItem.getBElement());
                        }
                    }
                }
            }
            if (!complainStatusMap.isEmpty()) {
                Map<String, Dict> complainStatusDictMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_COMPLAIN_STATUS);
                if (complainStatusDictMap != null && !complainStatusDictMap.isEmpty()) {
                    for (Order order : orderList) {
                        Long statusValue = complainStatusMap.get(order.getId());
                        if (statusValue != null) {
                            Dict statusDict = complainStatusDictMap.get(statusValue.toString());
                            if (statusDict != null) {
                                order.setComplainFormStatus(statusDict);
                            }
                        }
                    }
                }
            }
        }
    }

    //endregion 公用方法

    //region 上门服务

    /**
     * 删除上门服务明细
     * 1.删除数据
     * 2.更新订单的相关数据：
     * 3.解除配件关联，不删除配件
     *
     * @param detail
     */
    @Transactional(readOnly = false)
    public void deleteDetailForFollowUp(OrderDetail detail) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, detail.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = detail.getCreateBy();
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        try {
            //上门服务项目时候删除
            Order order = orderService.getOrderById(detail.getOrderId(), detail.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单信息失败");
            }

            //Integer chargeFlag = getChargeFlag(detail.getOrderId(), detail.getQuarter());//getOrderById()中已改成从数据库读取
            Integer chargeFlag = order.getOrderCondition().getChargeFlag();
            if (chargeFlag != null && chargeFlag.intValue() == 1) {
                throw new OrderException("此订单已已经对账，不能删除上门服务。");
            }

            List<OrderDetail> details = order.getDetailList();
            if (details == null) {
                details = Lists.newArrayList();
            }
            OrderDetail model = details.stream()
                    .filter(t -> Objects.equals(detail.getId(), t.getId()))
                    .findFirst()
                    .orElse(null);
            if (model == null || model.getDelFlag().equals(OrderDetail.DEL_FLAG_DELETE)) {
                throw new OrderException("上门服务项目已经被删除。");
            }
            //异常操作，不检查订单状态
            if (detail.getAddType() == 0 && !order.canService()) {
                throw new OrderException("不能删除上门服务项目，请刷新订单列查看订单处理状态。");
            }
            Long servicePointId = model.getServicePoint().getId();
            OrderServicePointFee orderServicePointFee = orderService.getOrderServicePointFee(order.getId(), order.getQuarter(), servicePointId);

            model.setDelFlag(OrderDetail.DEL_FLAG_DELETE);//*,添加删除标记，计算价格时忽略该笔项目，重要
            //重新计算价格
            details.remove(model);//移除要删除的上门服务
            orderService.rechargeOrder(details, model);

            Date date = detail.getCreateDate();
            Boolean hasDetailOfSamePoint = false;
            OrderDetail otherDetail = details.stream().filter(t -> !t.getServicePoint().getId().equals(detail.getId()) && t.getServicePoint().getId().equals(model.getServicePoint().getId())).findFirst().orElse(null);
            if (otherDetail != null) {
                hasDetailOfSamePoint = true;
            }
            //保险费汇总(负数)
            Double insuranceCharge = 0.00;
            insuranceCharge = orderService.getTotalOrderInsurance(order.getId(),order.getQuarter());
            if (insuranceCharge == null) {
                insuranceCharge = 0.00;
            }
            //保险单失效
            if (!hasDetailOfSamePoint) {
                //保险单
                OrderInsurance orderInsurance = dao.getOrderInsuranceByServicePoint(order.getQuarter(), order.getId(), servicePointId);
                if (orderInsurance != null && orderInsurance.getDelFlag() == OrderInsurance.DEL_FLAG_NORMAL) {
                    orderInsurance.setUpdateBy(user);
                    orderInsurance.setUpdateDate(date);
                    orderInsurance.setDelFlag(1);
                    dao.updateOrderInsurance(orderInsurance);
                    insuranceCharge = insuranceCharge + orderInsurance.getAmount();//保险失效,保险单的费用是正数，fee的是负数所以要用+处理
                }
            }

            // 计算合计
            OrderFee orderFee = order.getOrderFee();
            //时效奖励(快可立补贴)
            Double timeLinessCharge = orderFee.getTimeLinessCharge();
            //时效费(客户补贴)
            Double subsidyTimeLinessCharge = orderFee.getSubsidyTimeLinessCharge();

            //重新汇总金额
            HashMap<String, Object> feeMap = orderService.recountFee(details);
            Integer serviceTimes = (Integer) feeMap.get("serviceTimes");
            //应收
            orderFee.setServiceCharge((Double) feeMap.get("serviceCharge"));
            orderFee.setMaterialCharge((Double) feeMap.get("materialCharge"));
            orderFee.setExpressCharge((Double) feeMap.get("expressCharge"));
            orderFee.setTravelCharge((Double) feeMap.get("travelCharge"));
            orderFee.setOtherCharge((Double) feeMap.get("otherCharge"));
            orderFee.setOrderCharge((Double) feeMap.get("orderCharge"));//以上5项合计
            //时效费
            orderFee.setOrderCharge(orderFee.getOrderCharge() + orderFee.getCustomerTimeLinessCharge());
            //异常处理，加急费汇总到合计
            if (detail.getAddType() == 1) {
                orderFee.setOrderCharge(orderFee.getOrderCharge() + orderFee.getCustomerUrgentCharge());
            }
            //应付
            orderFee.setEngineerServiceCharge((Double) feeMap.get("engineerServiceCharge"));
            orderFee.setEngineerMaterialCharge((Double) feeMap.get("engineerMaterialCharge"));
            orderFee.setEngineerExpressCharge((Double) feeMap.get("engineerExpressCharge"));
            orderFee.setEngineerTravelCharge((Double) feeMap.get("engineerTravelCharge"));
            orderFee.setEngineerOtherCharge((Double) feeMap.get("engineerOtherCharge"));
            orderFee.setEngineerTotalCharge((Double) feeMap.get("engineerTotalCharge"));//以上5项合计
            //保险费，时效奖励(快可立补贴)，时效费(客户补贴)
            orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + insuranceCharge + timeLinessCharge + subsidyTimeLinessCharge);//合计
            //异常处理，加急费汇总到合计
            if (detail.getAddType() == 1) {
                orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + orderFee.getEngineerUrgentCharge());//合计
            }
            //fee
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            //应收(客户)
            params.put("serviceCharge", orderFee.getServiceCharge()); //服务费(应收)
            params.put("materialCharge", orderFee.getMaterialCharge());// 配件费(应收)
            params.put("expressCharge", orderFee.getExpressCharge()); // 快递费(应收)
            params.put("travelCharge", orderFee.getTravelCharge()); //远程费(应收)
            params.put("otherCharge", orderFee.getOtherCharge());// 其他費用(应收)
            params.put("orderCharge", orderFee.getOrderCharge());// 合计订单金额(应收)

            //应付(安维)
            params.put("engineerServiceCharge", orderFee.getEngineerServiceCharge());//服务费
            params.put("engineerMaterialCharge", orderFee.getEngineerMaterialCharge());//配件费
            params.put("engineerExpressCharge", orderFee.getEngineerExpressCharge());//快递费
            params.put("engineerTravelCharge", orderFee.getEngineerTravelCharge());//远程费
            params.put("engineerOtherCharge", orderFee.getEngineerOtherCharge());//其它费用
            params.put("insuranceCharge", insuranceCharge);//保险费
            //合计=其他费用合计-保险费
            params.put("engineerTotalCharge", orderFee.getEngineerTotalCharge());//合计
            dao.updateFee(params);

            //condition
            params.clear();
            OrderCondition condition = order.getOrderCondition();
            condition.setServiceTimes(serviceTimes);
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("serviceTimes", serviceTimes);//*
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            //删除此笔上门服务
            dao.deleteDetail(detail.getId(), order.getQuarter());

            OrderDetail d;
            for (int i = 0, size = details.size(); i < size; i++) {
                d = details.get(i);
                if (d.getDelFlag() == OrderDetail.DEL_FLAG_DELETE) {
                    continue;
                }

                //update
                params.clear();
                params.put("quarter", order.getQuarter());
                params.put("id", d.getId());
                params.put("itemNo", d.getItemNo());

                params.put("materialCharge", d.getMaterialCharge());
                params.put("travelCharge", d.getTravelCharge());
                params.put("charge", d.getCharge());

                params.put("engineerMaterialCharge", d.getEngineerMaterialCharge());
                params.put("engineerTravelCharge", d.getEngineerTravelCharge());
                params.put("engineerServiceCharge", d.getEngineerServiceCharge());

                params.put("updateBy", user);
                params.put("updateDate", date);
                dao.updateDetail(params);
            }
            //配件，不删除，更改为不与上门服务项目关联
            orderMaterialService.cancelRelationOfServiceAndMaterial(order.getId(), order.getQuarter(), detail.getId());
            //OrderServicePointFee,失效并汇总
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
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("servicePointId", servicePointId);
            if (!hasDetailOfSamePoint) {
                params.put("delFlag", 1);
            }
            //网点费用汇总
            if (orderServicePointFee != null && servicePointFeeSum != null) {
                params.put("serviceCharge", servicePointFeeSum.getEngineerServiceCharge());
                params.put("travelCharge", servicePointFeeSum.getEngineerTravelCharge());
                params.put("expressCharge", servicePointFeeSum.getEngineerExpressCharge());
                params.put("materialCharge", servicePointFeeSum.getEngineerMaterialCharge());
                params.put("otherCharge", servicePointFeeSum.getEngineerOtherCharge());
                params.put("insuranceCharge", orderServicePointFee.getInsuranceCharge());
                params.put("timeLinessCharge", orderServicePointFee.getTimeLinessCharge());
                params.put("customerTimeLinessCharge", orderServicePointFee.getCustomerTimeLinessCharge());
                params.put("urgentCharge", orderServicePointFee.getUrgentCharge());//下单时，未写入此列
                //汇总
                Double engineerTotalCharge = servicePointFeeSum.getEngineerServiceCharge()
                        + servicePointFeeSum.getEngineerTravelCharge()
                        + servicePointFeeSum.getEngineerExpressCharge()
                        + servicePointFeeSum.getEngineerMaterialCharge()
                        + servicePointFeeSum.getEngineerOtherCharge()
                        + orderServicePointFee.getInsuranceCharge()
                        + orderServicePointFee.getTimeLinessCharge()
                        + orderServicePointFee.getCustomerTimeLinessCharge();
                //异常处理情况
                if (detail.getAddType() == 1) {
                    engineerTotalCharge = engineerTotalCharge + orderServicePointFee.getUrgentCharge();
                }
                params.put("orderCharge", engineerTotalCharge);
            }
            dao.updateOrderServicePointFeeByMaps(params);

            //log
            OrderProcessLog log = new OrderProcessLog();
            log.setQuarter(order.getQuarter());
            log.setAction("删除上门服务具体服务项目");
            log.setOrderId(order.getId());
            log.setActionComment(String.format("删除上门服务具体服务项目:%s,产品:%s", model.getServiceType().getName(), model.getProduct().getName()));
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(condition.getStatusValue());
            log.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            log.setCloseFlag(0);
            log.setCreateBy(user);
            log.setCreateDate(date);
            //log.setRemarks(String.format("客服删除的上门服务ID：%s", detail.getId()));
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(log);

            //cache,淘汰
            OrderCacheUtils.setDetailActionFlag(order.getId());
            OrderCacheUtils.delete(detail.getOrderId());
            //手动释放变量
            user = null;
            details = null;
            orderFee = null;
            condition = null;
            order = null;
            params = null;
            log = null;
        } catch (OrderException oe) {
            throw new OrderException(oe.getMessage());
        } catch (Exception e) {
            log.error("[OrderService.deleteDetail]orderId:{}", detail.getOrderId(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
            params = null;
        }
    }

    /**
     * 添加上门服务明细(for待回访)
     *
     * @param detail
     */
    @Transactional(readOnly = false)
    public void addDetailForFollowUp(OrderDetail detail) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, detail.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = detail.getCreateBy();
        try {
            Order order = orderService.getOrderById(detail.getOrderId(), "", OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单错误，请重试。");
            }

            Integer chargeFlag = order.getOrderCondition().getChargeFlag();
            if (chargeFlag != null && chargeFlag.intValue() == 1) {
                throw new OrderException("此订单已已经对账，不能删除上门服务。");
            }

            detail.setQuarter(order.getQuarter());//数据库分片
            OrderCondition condition = order.getOrderCondition();
            //2020-09-24 接入云米，增加经纬度检查
            AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(order.getDataSourceId(),order.getId(),order.getQuarter());
            if(!locationCheckResult.getSuccess()){
                throw new OrderException(locationCheckResult.getMessage());
            }
            //Customer Price
            List<CustomerPrice> customerPrices = customerService.getPricesFromCache(condition.getCustomer().getId());
            if (customerPrices == null || customerPrices.size() == 0) {
                throw new OrderException(String.format("读取客户：%s价格失败", condition.getCustomer().getName()));
            }
            Product product = detail.getProduct();
            CustomerPrice cprice = customerPrices.stream()
                    .filter(t -> Objects.equals(t.getProduct().getId(), product.getId()) && Objects.equals(t.getServiceType().getId(), detail.getServiceType().getId()))
                    .findFirst().orElse(null);
            if (cprice == null) {
                throw new OrderException(String.format("未定义服务价格；客户：%s 产品:%s 服务：%s。", condition.getCustomer().getName(), product.getName(), detail.getServiceType().getName()));
            }
            detail.setStandPrice(cprice.getPrice());
            detail.setDiscountPrice(cprice.getDiscountPrice());

            //ServicePoint Price
            Long servicePointId = condition.getServicePoint().getId();
            //增加网点确认 2019/08/01
            if (detail.getServicePoint() == null || detail.getServicePoint().getId() == null || detail.getServicePoint().getId() <= 0) {
                detail.setServicePoint(condition.getServicePoint());
            }
            ServicePrice eprice = null;
            eprice = orderService.getPriceByProductAndServiceTypeFromCacheNew(condition,servicePointId,product.getId(),detail.getServiceType().getId());
            if (eprice == null) {
                throw new OrderException(String.format("未定义%s服务价格；网点：%s[%s] 产品:%s 服务：%s。",condition.getServicePoint().getServicePointNo(),condition.getServicePoint().getName(), product.getName(), detail.getServiceType().getName()));
            }
            //网点费用表
            OrderServicePointFee orderServicePointFee = orderService.getOrderServicePointFee(order.getId(), order.getQuarter(), servicePointId);
            detail.setEngineerStandPrice(eprice.getPrice());
            detail.setEngineerDiscountPrice(eprice.getDiscountPrice());
            //因sd_orderFee表中网点付款方式出现为0的情况，此处做特殊处理
            Dict engineerPaymentType = order.getOrderFee().getEngineerPaymentType();
            if (engineerPaymentType != null && engineerPaymentType.getIntValue() > 0) {
                detail.setEngineerPaymentType(engineerPaymentType);
            } else {
                ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
                if (servicePoint != null && servicePoint.getFinance() != null
                        && servicePoint.getFinance().getPaymentType() != null
                        && servicePoint.getFinance().getPaymentType().getIntValue() > 0) {
                    detail.setEngineerPaymentType(servicePoint.getFinance().getPaymentType());
                } else {
                    throw new OrderException(String.format("确认网点：%s 结算方式失败", condition.getServicePoint().getName()));
                }
            }
            Date date = new Date();

            //统计未关联上门明细的配件费(通过审核的)
            Double materailAmount = 0.0d;
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
            } else {
                //单品，判断订单项中套组
                long[] setIds = orderService.getSetProductIdIncludeMe(product.getId(), order.getItems());
                if (setIds != null && setIds.length > 0) {
                    subProducts = ArrayUtils.addAll(subProducts, setIds);
                }
            }
            final long[] sids = ArrayUtils.clone(subProducts);
            //只读取单头
            List<MaterialMaster> materials = orderMaterialService.findMaterialMasterHeadsByOrderId(detail.getOrderId(), order.getQuarter());
            if (materials != null && materials.size() > 0) {
                //&& Objects.equals(t.getProductId(), detail.getProduct().getId())
                materailAmount = materials.stream().filter(
                        t -> ArrayUtils.contains(materialStatus, Integer.parseInt(t.getStatus().getValue()))
                                && Objects.equals(t.getOrderDetailId(), 0l)
                                && ArrayUtils.contains(sids, t.getProductId().longValue())
                )
                        .collect(Collectors.summingDouble(MaterialMaster::getTotalPrice));
                if (materailAmount > 0) {
                    //应付，+
                    detail.setEngineerMaterialCharge(detail.getEngineerMaterialCharge() + materailAmount);
                    detail.setEngineerTotalCharge(detail.getEngineerChage());
                    //应收，+
                    detail.setMaterialCharge(detail.getMaterialCharge() + materailAmount);
                }
            }

            List<OrderDetail> details = order.getDetailList();
            OrderFee orderFee = order.getOrderFee();
            //时效奖励(快可立补贴)
            Double timeLinessCharge = orderFee.getTimeLinessCharge();
            //时效费(客户补贴)
            Double subsidyTimeLinessCharge = orderFee.getSubsidyTimeLinessCharge();
            //自动同步应收远程及其他费用
            orderService.autoCountCustomerRemoteCharge(condition.getProductCategoryId(),detail);
            if(detail.getSyncChargeTags() == null){
                detail.setSyncChargeTags(0);
            }
            //2020-11-22 远程费+其他费用的总费用受控品类处理
            orderService.limitRemoteChargeCheck(condition.getProductCategoryId(),details,detail);

            details.add(detail);//*
            orderService.rechargeOrder(details, detail);
            HashMap<String, Object> params = Maps.newHashMap();

            //保险费汇总(负数)
            Double insuranceCharge = orderService.getTotalOrderInsurance(order.getId(),order.getQuarter());
            if (insuranceCharge == null) {
                insuranceCharge = 0.00;
            }

            //保险单号生效
            OrderInsurance orderInsurance = null;
            boolean insuranceFormEnabled = false;
            orderInsurance = dao.getOrderInsuranceByServicePoint(order.getQuarter(), order.getId(), servicePointId);
            if (orderInsurance != null && orderInsurance.getDelFlag() == OrderInsurance.DEL_FLAG_DELETE) {
                insuranceFormEnabled = true;
                orderInsurance.setUpdateBy(user);
                orderInsurance.setUpdateDate(date);
                orderInsurance.setDelFlag(0);
                dao.updateOrderInsurance(orderInsurance);
                insuranceCharge = insuranceCharge - orderInsurance.getAmount();//保险启用
            }

            //重新汇总金额
            HashMap<String, Object> feeMap = orderService.recountFee(details);
            Integer serviceTimes = (Integer) feeMap.get("serviceTimes");
            //应收
            orderFee.setServiceCharge((Double) feeMap.get("serviceCharge"));
            orderFee.setMaterialCharge((Double) feeMap.get("materialCharge"));
            orderFee.setExpressCharge((Double) feeMap.get("expressCharge"));
            orderFee.setTravelCharge((Double) feeMap.get("travelCharge"));
            orderFee.setOtherCharge((Double) feeMap.get("otherCharge"));
            orderFee.setOrderCharge((Double) feeMap.get("orderCharge"));//以上5项合计
            //时效费,加急费
            if (condition.getPendingFlag() == 1 || detail.getAddType() == 1) {
                orderFee.setOrderCharge(orderFee.getOrderCharge() + orderFee.getCustomerTimeLinessCharge() + orderFee.getCustomerUrgentCharge());
            }

            //应付
            orderFee.setEngineerServiceCharge((Double) feeMap.get("engineerServiceCharge"));
            orderFee.setEngineerMaterialCharge((Double) feeMap.get("engineerMaterialCharge"));
            orderFee.setEngineerExpressCharge((Double) feeMap.get("engineerExpressCharge"));
            orderFee.setEngineerTravelCharge((Double) feeMap.get("engineerTravelCharge"));
            orderFee.setEngineerOtherCharge((Double) feeMap.get("engineerOtherCharge"));
            orderFee.setEngineerTotalCharge((Double) feeMap.get("engineerTotalCharge"));//以上5项合计
            //保险费
            orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + insuranceCharge);
            //时效奖励(快可立补贴)，时效费(客户补贴)
            if (condition.getPendingFlag() == 1 || detail.getAddType() == 1) {
                orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + timeLinessCharge + subsidyTimeLinessCharge + orderFee.getEngineerUrgentCharge());//合计
            }

            //fee
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            //应收(客户)
            params.put("serviceCharge", orderFee.getServiceCharge()); //服务费
            params.put("materialCharge", orderFee.getMaterialCharge());// 配件费
            params.put("expressCharge", orderFee.getExpressCharge()); // 快递费
            params.put("travelCharge", orderFee.getTravelCharge()); //远程费
            params.put("otherCharge", orderFee.getOtherCharge());// 其他費用
            params.put("orderCharge", orderFee.getOrderCharge());// 合计

            //应付(安维)
            params.put("engineerServiceCharge", orderFee.getEngineerServiceCharge());//服务费
            params.put("engineerMaterialCharge", orderFee.getEngineerMaterialCharge());//配件费
            params.put("engineerExpressCharge", orderFee.getEngineerExpressCharge());//快递费
            params.put("engineerTravelCharge", orderFee.getEngineerTravelCharge());//远程费
            params.put("engineerOtherCharge", orderFee.getEngineerOtherCharge());//其它费用
            params.put("insuranceCharge", insuranceCharge);//保险费用(负数，扣减)
            //合计=其他费用合计-保险费
            params.put("engineerTotalCharge", orderFee.getEngineerTotalCharge());
            dao.updateFee(params);

            //condition
            params.clear();
            params.put("quarter", order.getQuarter());//*
            params.put("orderId", order.getId());
            Boolean firstService = false;//首次上门
            Dict status = condition.getStatus();
            condition.setServiceTimes(serviceTimes);

            //异常处理不变更订单状态
            if (detail.getAddType() != 1 && Integer.parseInt(status.getValue()) == Order.ORDER_STATUS_PLANNED.intValue()) {
                firstService = true;
                status.setValue(String.valueOf(Order.ORDER_STATUS_SERVICED));
                status.setLabel("已上门");
                params.put("status", status);
            }
            if (detail.getAddType() != 1) {
                //params.put("subStatus", Order.ORDER_SUBSTATUS_SERVICED);//Add by Zhoucy
                params.put("pendingType", new Dict(0, ""));
                params.put("pendingTypeDate", date);//Add by Zhoucy
            }

            params.put("serviceTimes", serviceTimes);
            params.put("updateBy", user);
            params.put("updateDate", date);

            dao.updateCondition(params);

            //status
            if (firstService) {
                OrderStatus orderStatus = order.getOrderStatus();
                orderStatus.setServiceFlag(1);
                orderStatus.setServiceDate(date);
                orderStatus.setServiceTimes(serviceTimes);
                params.clear();
                params.put("quarter", order.getQuarter());
                params.put("orderId", order.getId());
                params.put("serviceFlag", 1);
                params.put("serviceDate", date);
                params.put("serviceTimes", serviceTimes);
                dao.updateStatus(params);
            }
            //details
            MDErrorType errorType = null;
            MDErrorCode errorCode = null;
            MDActionCodeDto actionCode = null;
            OrderDetail model;
            for (int i = 0, size = details.size(); i < size; i++) {
                model = details.get(i);
                if (model.getDelFlag() == OrderDetail.DEL_FLAG_DELETE) {
                    continue;
                }
                if (model.getId() == null || model.getId() <= 0) {
                    //insert
                    if(model.getServiceCategory() == null || model.getServiceCategory().getIntValue() == 0){
                        //调用方未设定，以下单时的工单类型为准
                        model.setServiceCategory(new Dict(order.getOrderCondition().getOrderServiceType(),""));
                    }
                    if(model.getErrorType() == null || model.getErrorType().getId() == null){
                        if(errorType == null) {
                            errorType = new MDErrorType();
                            errorType.setId(0L);
                        }
                        model.setErrorType(errorType);
                    }
                    if(model.getErrorCode() == null || model.getErrorCode().getId() == null){
                        if(errorCode == null) {
                            errorCode = new MDErrorCode();
                            errorCode.setId(0L);
                        }
                        model.setErrorCode(errorCode);
                    }
                    if(model.getActionCode() == null || model.getActionCode().getId() == null){
                        if(actionCode == null) {
                            actionCode = new MDActionCodeDto();
                            actionCode.setId(0L);
                            actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                        }
                        model.setActionCode(actionCode);
                    }
                    model.setOtherActionRemark(StringUtils.trimToEmpty(model.getOtherActionRemark()));
                    dao.insertDetail(model);
                } else {
                    //update
                    params.clear();
                    params.put("quarter", order.getQuarter());
                    params.put("id", model.getId());
                    params.put("itemNo", model.getItemNo());

                    params.put("materialCharge", model.getMaterialCharge());
                    params.put("travelCharge", model.getTravelCharge());
                    params.put("charge", model.getCharge());

                    params.put("engineerMaterialCharge", model.getEngineerMaterialCharge());
                    params.put("engineerTravelCharge", model.getEngineerTravelCharge());
                    params.put("engineerServiceCharge", model.getEngineerServiceCharge());

                    params.put("updateBy", user);
                    params.put("updateDate", date);
                    dao.updateDetail(params);
                }
            }
            //关联配件,包含审核和未审核的
            //未审核的: 在审核时再计费
            List<MaterialMaster> relateMaterials = materials.stream().filter(
                    t -> ArrayUtils.contains(materialStatus, Integer.parseInt(t.getStatus().getValue()))
                            && Objects.equals(t.getOrderDetailId(), 0l)
                            && ArrayUtils.contains(sids, t.getProductId().longValue())
            ).collect(Collectors.toList());
            if (relateMaterials != null && relateMaterials.size() > 0) {
                for (MaterialMaster m : relateMaterials) {
                    m.setOrderDetailId(detail.getId());//关联的订单上门明细
                    orderMaterialService.addRelationOfServiceAndMaterial(m);
                }
            }
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
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
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
            dao.updateOrderServicePointFeeByMaps(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("上门服务:添加订单具体服务项目");
            processLog.setOrderId(order.getId());
            // 2019-12-27 统一上门服务跟踪进度格式
            //processLog.setActionComment(String.format("上门服务:添加订单具体服务项目:%s,产品:%s", detail.getServiceType().getName(), detail.getProduct().getName()));
            if (detail.getErrorType() != null && detail.getErrorType().getId() > 0){
                if(StringUtils.isBlank(detail.getOtherActionRemark())){
                    processLog.setActionComment(String.format("%s【%s】现象:【%s】处理措施:【%s】",
                            detail.getServiceType().getName(),
                            detail.getProduct().getName(),
                            detail.getErrorCode().getName(),
                            detail.getActionCode().getName()
                    ));
                }else {
                    processLog.setActionComment(String.format("%s【%s】现象:【%s】处理措施:【%s】其他故障:【%s】",
                            detail.getServiceType().getName(),
                            detail.getProduct().getName(),
                            detail.getErrorCode().getName(),
                            detail.getActionCode().getName(),
                            detail.getOtherActionRemark()
                    ));
                }
            }else{
                if(StringUtils.isBlank(detail.getOtherActionRemark())){
                    processLog.setActionComment(String.format("%s【%s】", detail.getServiceType().getName(), detail.getProduct().getName()));
                }else{
                    processLog.setActionComment(String.format("%s【%s】其他故障:【%s】", detail.getServiceType().getName(), detail.getProduct().getName(),detail.getOtherActionRemark()));
                }
            }
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
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
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            //cache,淘汰
            OrderCacheUtils.setDetailActionFlag(order.getId());
            OrderCacheUtils.delete(detail.getOrderId());

            //region B2B消息队列
            if (firstService || order.getDataSourceId() == B2BDataSourceEnum.VIOMI.id
                    || order.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                //status -> 4
                Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                Long engineerId = condition.getEngineer() == null ? null : condition.getEngineer().getId();
                b2BCenterOrderService.serviceOrder(order, pointId, engineerId, user, date);
            }
            //endregion B2B消息队列
        } catch (OrderException oe) {
            throw new OrderException(oe.getMessage());
        } catch (Exception e) {
            log.error("[OrderService.addDetail]orderId:{}", detail.getOrderId(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 修改上门服务明细(for待回访)
     *
     * @param detail
     */
    @Transactional(readOnly = false)
    public void editDetailForFollowUp(OrderDetail detail) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, detail.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = detail.getUpdateBy();
        try {
            Order order = orderService.getOrderById(detail.getOrderId(), "", OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单错误，请重试。");
            }

            Integer chargeFlag = order.getOrderCondition().getChargeFlag();
            if (chargeFlag != null && chargeFlag.intValue() == 1) {
                throw new OrderException("此订单已已经对账，不能删除上门服务。");
            }
            List<OrderDetail> details = order.getDetailList();
            if (details == null || details.isEmpty()) {
                throw new OrderException("此订单无上门服务项目。");
            }
            Long detailId = detail.getId();
            OrderDetail d = details.stream().filter(t -> t.getId().equals(detailId)).findFirst().orElse(null);
            if (d == null) {
                throw new OrderException("此订单无此上门服务项目。");
            }
            if (d.getDelFlag() == 1) {
                throw new OrderException("此上门服务项目已删除，请刷新上门服务列表。");
            }
            //2020-09-24 接入云米，增加经纬度检查
            AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(order.getDataSourceId(),order.getId(),order.getQuarter());
            if(!locationCheckResult.getSuccess()){
                throw new OrderException(locationCheckResult.getMessage());
            }
            //remove from details
            details.remove(d);//*

            detail.setQuarter(order.getQuarter());//数据库分片
            OrderCondition condition = order.getOrderCondition();
            //Customer Price
            List<CustomerPrice> customerPrices = customerService.getPricesFromCache(condition.getCustomer().getId());
            if (customerPrices == null || customerPrices.size() == 0) {
                throw new OrderException(String.format("读取客户：%s价格失败", condition.getCustomer().getName()));
            }
            Product product = detail.getProduct();
            CustomerPrice cprice = customerPrices.stream()
                    .filter(t -> Objects.equals(t.getProduct().getId(), product.getId()) && Objects.equals(t.getServiceType().getId(), detail.getServiceType().getId()))
                    .findFirst().orElse(null);
            if (cprice == null) {
                throw new OrderException(String.format("未定义服务价格；客户：%s 产品:%s 服务：%s。", condition.getCustomer().getName(), product.getName(), detail.getServiceType().getName()));
            }
            detail.setStandPrice(cprice.getPrice());
            detail.setDiscountPrice(cprice.getDiscountPrice());

            //ServicePoint Price
            Long servicePointId = condition.getServicePoint().getId();
            //使用新的网点价格读取方法 2020-03-07
            //ServicePrice eprice = servicePointService.getPriceByProductAndServiceTypeFromCache(servicePointId,product.getId(),detail.getServiceType().getId());
            ServicePrice eprice = orderService.getPriceByProductAndServiceTypeFromCacheNew(condition,servicePointId,product.getId(),detail.getServiceType().getId());
            if (eprice == null) {
                throw new OrderException(String.format("未定义服务价格；网点：%s[%s] 产品:%s 服务：%s。", condition.getServicePoint().getServicePointNo(),condition.getServicePoint().getName(), product.getName(), detail.getServiceType().getName()));
            }
            //网点费用表
            OrderServicePointFee orderServicePointFee = orderService.getOrderServicePointFee(order.getId(), order.getQuarter(), servicePointId);
            detail.setEngineerStandPrice(eprice.getPrice());
            detail.setEngineerDiscountPrice(eprice.getDiscountPrice());
            //ryan at 2018/10/31
            //因sd_orderFee表中网点付款方式出现为0的情况，此处做特殊处理
            Dict engineerPaymentType = order.getOrderFee().getEngineerPaymentType();
            if (engineerPaymentType != null && engineerPaymentType.getIntValue() > 0) {
                detail.setEngineerPaymentType(engineerPaymentType);
            } else {
                ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
                if (servicePoint != null && servicePoint.getFinance() != null
                        && servicePoint.getFinance().getPaymentType() != null
                        && servicePoint.getFinance().getPaymentType().getIntValue() > 0) {
                    detail.setEngineerPaymentType(servicePoint.getFinance().getPaymentType());
                } else {
                    throw new OrderException(String.format("确认网点：%s 结算方式失败", condition.getServicePoint().getName()));
                }
            }
            //end
            Date date = new Date();

            //统计未关联上门明细的配件费(通过审核的)
            Double materailAmount = 0.0d;
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
            } else {
                //单品，判断订单项中套组
                long[] setIds = orderService.getSetProductIdIncludeMe(product.getId(), order.getItems());
                if (setIds != null && setIds.length > 0) {
                    subProducts = ArrayUtils.addAll(subProducts, setIds);
                }
            }
            final long[] sids = ArrayUtils.clone(subProducts);
            //只读取单头
            List<MaterialMaster> materials = orderMaterialService.findMaterialMasterHeadsByOrderId(detail.getOrderId(), order.getQuarter());
            if (materials != null && materials.size() > 0) {
                //&& Objects.equals(t.getProductId(), detail.getProduct().getId())
                materailAmount = materials.stream().filter(
                        t -> ArrayUtils.contains(materialStatus, Integer.parseInt(t.getStatus().getValue()))
                                && Objects.equals(t.getOrderDetailId(), 0l)
                                && ArrayUtils.contains(sids, t.getProductId().longValue())
                )
                        .collect(Collectors.summingDouble(MaterialMaster::getTotalPrice));
                if (materailAmount > 0) {
                    //应付，+
                    detail.setEngineerMaterialCharge(detail.getEngineerMaterialCharge() + materailAmount);
                    detail.setEngineerTotalCharge(detail.getEngineerChage());
                    //应收，+
                    detail.setMaterialCharge(detail.getMaterialCharge() + materailAmount);
                }
            }

            OrderFee orderFee = order.getOrderFee();
            //时效奖励(快可立补贴)
            Double timeLinessCharge = orderFee.getTimeLinessCharge();
            //时效费(客户补贴)
            Double subsidyTimeLinessCharge = orderFee.getSubsidyTimeLinessCharge();
            //自动同步应收远程及其他费用
            orderService.autoCountCustomerRemoteCharge(condition.getProductCategoryId(),detail);
            if(detail.getSyncChargeTags() == null){
                detail.setSyncChargeTags(0);
            }
            //2020-11-22 远程费+其他费用的总费用受控品类处理
            orderService.limitRemoteChargeCheck(condition.getProductCategoryId(),details,detail);
            details.add(detail);//*
            orderService.rechargeOrder(details, detail);
            HashMap<String, Object> params = Maps.newHashMap();

            //保险费汇总(负数)
            //Double insuranceCharge = orderFee.getInsuranceCharge();

            //重新汇总金额
            HashMap<String, Object> feeMap = orderService.recountFee(details);
            //Integer serviceTimes = (Integer) feeMap.get("serviceTimes");
            //应收
            orderFee.setServiceCharge((Double) feeMap.get("serviceCharge"));
            orderFee.setMaterialCharge((Double) feeMap.get("materialCharge"));
            orderFee.setExpressCharge((Double) feeMap.get("expressCharge"));
            orderFee.setTravelCharge((Double) feeMap.get("travelCharge"));
            orderFee.setOtherCharge((Double) feeMap.get("otherCharge"));
            orderFee.setOrderCharge((Double) feeMap.get("orderCharge"));//以上5项合计
            //时效费,加急费
            if (condition.getPendingFlag() == 1 || detail.getAddType() == 1) {
                orderFee.setOrderCharge(orderFee.getOrderCharge() + orderFee.getCustomerTimeLinessCharge() + orderFee.getCustomerUrgentCharge());
            }

            //应付
            orderFee.setEngineerServiceCharge((Double) feeMap.get("engineerServiceCharge"));
            orderFee.setEngineerMaterialCharge((Double) feeMap.get("engineerMaterialCharge"));
            orderFee.setEngineerExpressCharge((Double) feeMap.get("engineerExpressCharge"));
            orderFee.setEngineerTravelCharge((Double) feeMap.get("engineerTravelCharge"));
            orderFee.setEngineerOtherCharge((Double) feeMap.get("engineerOtherCharge"));
            orderFee.setEngineerTotalCharge((Double) feeMap.get("engineerTotalCharge"));//以上5项合计
            //保险费
            orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + orderFee.getInsuranceCharge());
            //时效奖励(快可立补贴)，时效费(客户补贴)
            if (condition.getPendingFlag() == 1 || detail.getAddType() == 1) {
                orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge() + timeLinessCharge + subsidyTimeLinessCharge + orderFee.getEngineerUrgentCharge());//合计
            }

            //fee
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            //应收(客户)
            params.put("serviceCharge", orderFee.getServiceCharge()); //服务费
            params.put("materialCharge", orderFee.getMaterialCharge());// 配件费
            params.put("expressCharge", orderFee.getExpressCharge()); // 快递费
            params.put("travelCharge", orderFee.getTravelCharge()); //远程费
            params.put("otherCharge", orderFee.getOtherCharge());// 其他費用
            params.put("orderCharge", orderFee.getOrderCharge());// 合计

            //应付(安维)
            params.put("engineerServiceCharge", orderFee.getEngineerServiceCharge());//服务费
            params.put("engineerMaterialCharge", orderFee.getEngineerMaterialCharge());//配件费
            params.put("engineerExpressCharge", orderFee.getEngineerExpressCharge());//快递费
            params.put("engineerTravelCharge", orderFee.getEngineerTravelCharge());//远程费
            params.put("engineerOtherCharge", orderFee.getEngineerOtherCharge());//其它费用
            //params.put("insuranceCharge", insuranceCharge);//保险费用(负数，扣减)
            //合计=其他费用合计-保险费
            params.put("engineerTotalCharge", orderFee.getEngineerTotalCharge());
            dao.updateFee(params);

            //details
            OrderDetail model;
            for (int i = 0, size = details.size(); i < size; i++) {
                model = details.get(i);
                if (model.getDelFlag() == OrderDetail.DEL_FLAG_DELETE) {
                    continue;
                }
                //update
                params.clear();
                params.put("quarter", order.getQuarter());
                params.put("id", model.getId());
                params.put("itemNo", model.getItemNo());

                params.put("materialCharge", model.getMaterialCharge());
                params.put("travelCharge", model.getTravelCharge());
                params.put("charge", model.getCharge());

                params.put("engineerMaterialCharge", model.getEngineerMaterialCharge());
                params.put("engineerTravelCharge", model.getEngineerTravelCharge());
                params.put("engineerServiceCharge", model.getEngineerServiceCharge());

                params.put("updateBy", user);
                params.put("updateDate", date);

                if (model.getId().longValue() == detailId.longValue()) {
                    //本次修改服务内容
                    params.put("brand", detail.getBrand());
                    params.put("productSpec", detail.getProductSpec());
                    params.put("serviceType", detail.getServiceType());
                    params.put("product", detail.getProduct());
                    params.put("product", detail.getProduct());
                    params.put("qty", detail.getQty());
                    params.put("remarks", detail.getRemarks());
                    //engineer
                    params.put("engineerStandPrice", detail.getEngineerStandPrice());
                    params.put("engineerDiscountPrice", detail.getEngineerDiscountPrice());
                    params.put("engineerExpressCharge", detail.getEngineerExpressCharge());
                    params.put("engineerOtherCharge", detail.getEngineerOtherCharge());
                    params.put("engineerDiscountPrice", detail.getEngineerDiscountPrice());
                    //customer
                    params.put("standPrice", detail.getStandPrice());
                    params.put("discountPrice", detail.getDiscountPrice());
                    params.put("expressCharge", detail.getExpressCharge());
                    params.put("travelNo", detail.getTravelNo());
                    params.put("otherCharge", detail.getOtherCharge());
                    //repair
                    int serviceCategoryId = detail.getServiceCategory().getIntValue();
                    params.put("serviceCategoryId", serviceCategoryId);
                    if (detail.getErrorType() != null && detail.getErrorType().getId() != null) {
                        params.put("errorTypeId", detail.getErrorType().getId());
                        params.put("errorCodeId", detail.getErrorCode().getId());
                        if (detail.getActionCode() == null || detail.getActionCode().getId() == null) {
                            params.put("actionCodeId", 0L);
                            params.put("actionCodeName", "");
                        } else {
                            params.put("actionCodeId", detail.getActionCode().getId());
                            params.put("actionCodeName", StringUtils.trimToEmpty(detail.getActionCode().getName()));
                        }
                        params.put("otherActionRemark", StringUtils.trimToEmpty(detail.getOtherActionRemark()));
                    }else{
                        params.put("errorTypeId", 0);
                        params.put("errorCodeId", 0);
                        params.put("actionCodeId", 0L);
                        params.put("actionCodeName", "");
                        params.put("otherActionRemark", StringUtils.trimToEmpty(detail.getOtherActionRemark()));
                    }
                    dao.editDetail(params);
                } else {
                    //update
                    dao.updateDetail(params);
                }
            }

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
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("servicePointId", servicePointId);
            params.put("delFlag", 0);
            //费用汇总
            if (orderServicePointFee != null && servicePointFeeSum != null) {
                params.put("serviceCharge", servicePointFeeSum.getEngineerServiceCharge());
                params.put("travelCharge", servicePointFeeSum.getEngineerTravelCharge());
                params.put("expressCharge", servicePointFeeSum.getEngineerExpressCharge());
                params.put("materialCharge", servicePointFeeSum.getEngineerMaterialCharge());
                params.put("otherCharge", servicePointFeeSum.getEngineerOtherCharge());
                params.put("insuranceCharge", orderServicePointFee.getInsuranceCharge());
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
            dao.updateOrderServicePointFeeByMaps(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("上门服务:修改订单具体服务项目");
            processLog.setOrderId(order.getId());
            //2019-12-27 统一上门服务格式
            if (detail.getErrorType() != null && detail.getErrorType().getId() != null && detail.getErrorType().getId() > 0){
                if(StringUtils.isBlank(detail.getOtherActionRemark())){
                    processLog.setActionComment(String.format("修改: %s【%s】现象:【%s】处理措施:【%s】",
                            detail.getServiceType().getName(),
                            detail.getProduct().getName(),
                            detail.getErrorCode().getName(),
                            detail.getActionCode().getName()
                    ));
                }else {
                    processLog.setActionComment(String.format("修改: %s【%s】现象:【%s】处理措施:【%s】其他故障:【%s】",
                            detail.getServiceType().getName(),
                            detail.getProduct().getName(),
                            detail.getErrorCode().getName(),
                            detail.getActionCode().getName(),
                            detail.getOtherActionRemark()
                    ));
                }
            }else{
                if(StringUtils.isBlank(detail.getOtherActionRemark())){
                    processLog.setActionComment(String.format("修改: %s【%s】", detail.getServiceType().getName(), detail.getProduct().getName()));
                }else{
                    processLog.setActionComment(String.format("修改服务:%s【%s】其他故障:【%s】", detail.getServiceType().getName(), detail.getProduct().getName(),detail.getOtherActionRemark()));
                }
            }
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            //cache,淘汰
            OrderCacheUtils.setDetailActionFlag(order.getId());
            OrderCacheUtils.delete(detail.getOrderId());

        } catch (OrderException oe) {
            throw new OrderException(oe.getMessage());
        } catch (Exception e) {
            log.error("[OrderService.addDetail]orderId:{}", detail.getOrderId(), e);
            throw new RuntimeException(e.getMessage());
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
     * @param orderId     订单id
     * @param quarter     分片
     * @param user        操作人
     * @param confirmType 确认上门类型 0-客服 1-安维
     */
    @Transactional(readOnly = false)
    public void confirmDoorAutoForFollowUp(Long orderId, String quarter, User user, int confirmType) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("错误：此订单正在处理中，请稍候重试，或刷新订单。");
        }
        try {
            Order o = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (o == null) {
                throw new OrderException("错误：读取订单信息失败");
            }
            if (!o.canService()) {
                throw new OrderException("错误：不能确认上门，请确认订单状态.");
            }
            OrderCondition condition = o.getOrderCondition();
            //2020-09-24 接入云米，增加经纬度检查
            AjaxJsonEntity locationCheckResult = orderService.checkAddressLocation(o.getDataSource().getIntValue(),orderId,o.getQuarter());
            if(!locationCheckResult.getSuccess()){
                throw new OrderException("因"+locationCheckResult.getMessage() + "，不能确认上门。");
            }
            //网点费用表
            Long servicePointId = condition.getServicePoint().getId();
            OrderServicePointFee orderServicePointFee = orderService.getOrderServicePointFee(orderId, o.getQuarter(), servicePointId);

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
            Date date = new Date();

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
                    processLog.setActionComment(String.format("%s%s", confirmType == 0 ? "客服" : "安维", "确认上门"));
                    processLog.setStatus(condition.getStatus().getLabel());
                    processLog.setStatusValue(condition.getStatusValue());
                    processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                    processLog.setCloseFlag(0);
                    processLog.setCreateBy(user);
                    processLog.setCreateDate(date);
                    processLog.setCustomerId(condition.getCustomerId());
                    processLog.setDataSourceId(o.getDataSourceId());
                    orderService.saveOrderProcessLogNew(processLog);

                    //cache,淘汰
                    OrderCacheUtils.delete(orderId);

                    //add by Zhoucy 2018-9-3 19:50 重复确认上门也修改子状态及相关时间
                    params.clear();
                    params.put("quarter", o.getQuarter());
                    params.put("orderId", orderId);
                    /* 一键添加上门服务时：sub_status=50、pending_type_date = reservation_date = now、pending_type = 0， Add by Zhoucy*/
                    params.put("pendingType", new Dict("0", ""));
                    params.put("pendingTypeDate", date);
                    params.put("reservationDate", date);
                    params.put("updateBy", user);
                    params.put("updateDate", date);
                    dao.updateCondition(params);

                    //region B2B消息队列
                    if (o.getDataSourceId() == B2BDataSourceEnum.VIOMI.id || o.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                        Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                        Long engineerId = condition.getEngineer() != null ? condition.getEngineer().getId() : null;
                        b2BCenterOrderService.serviceOrder(o, pointId, engineerId, user, date);
                    }

                    return;
                }
            }

            OrderFee orderFee = o.getOrderFee();
            //2020-10-21 从主库读取派单时预设的费用和单号
            OrderFee feeMaster = orderService.getPresetFeeWhenPlanFromMasterDB(o.getId(),o.getQuarter());
            if(feeMaster != null){
                if(orderFee != null){
                    orderFee.setPlanTravelCharge(feeMaster.getPlanTravelCharge());
                    orderFee.setPlanTravelNo(feeMaster.getPlanTravelNo());
                    orderFee.setPlanDistance(feeMaster.getPlanDistance());
                    orderFee.setCustomerPlanTravelCharge(feeMaster.getCustomerPlanTravelCharge());
                    orderFee.setPlanOtherCharge(feeMaster.getPlanOtherCharge());
                    orderFee.setCustomerPlanOtherCharge(feeMaster.getCustomerPlanOtherCharge());
                }
            }
            //时效奖励(快可立补贴)
            //Double timeLinessCharge = orderFee.getTimeLinessCharge();
            //时效费(客户补贴)
            //Double subsidyTimeLinessCharge = orderFee.getSubsidyTimeLinessCharge();

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
            //ServicePoint Price
            Map<String, ServicePrice> priceMap = null;
            List<NameValuePair<Long,Long>> nameValuePairs = getItemProductAndServiceTypePairs(o.getItems());
            if(CollectionUtils.isEmpty(nameValuePairs)){
                throw new OrderException("确认订单服务项目失败");
            }
            priceMap = orderService.getServicePriceFromCacheNew(condition,servicePointId,nameValuePairs);
            if (CollectionUtils.isEmpty(priceMap)) {
                throw new OrderException(new StringJoiner("").add("读取安维网点:").add(condition.getServicePoint().getName()).add("价格失败,请检查该网点是否维护了服务价格").toString());
            }
            //配件，只读取单头
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
            Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
            if(CollectionUtils.isEmpty(serviceTypeMap)){
                throw new OrderException("读取服务项目失败。");
            }
            ServiceType st = null;
            OrderDetail firstDetail = null;//本次上门服务的第一笔记录
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
                eprice = priceMap.get(String.format("%d:%d",product.getId(),serviceType.getId()));
                if (eprice == null) {
                    throw new OrderException(String.format("未定义产品价格。网点：%s 产品：%s 服务：%s", condition.getServicePoint().getName(), product.getName(), serviceType.getName()));
                }
                st = serviceTypeMap.get(serviceType.getId());
                if(st == null){
                    throw new OrderException(String.format("服务项目【%s】读取失败，或不存在",serviceType.getId()));
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
                detail.setBrand(item.getBrand());
                detail.setServiceTimes(serviceTimes);
                detail.setQty(item.getQty());
                detail.setServiceType(item.getServiceType());
                detail.setServiceCategory(new Dict(st.getOrderServiceType(),""));
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
                    if(detail.getTravelCharge() <= 0 || detail.getOtherCharge() <=0){
                        orderService.autoCountCustomerRemoteCharge(condition.getProductCategoryId(),detail);
                    }
                    //2020-11-22 远程费+其他费用的总费用受控品类处理
                    orderService.limitRemoteChargeCheck(condition.getProductCategoryId(),null,detail);
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
            Double insuranceCharge = orderService.getTotalOrderInsurance(o.getId(), o.getQuarter());
            if (insuranceCharge == null) {
                insuranceCharge = 0.00;
            }

            //保险单号生效
            OrderInsurance orderInsurance = null;
            boolean insuranceFormEnabled = false;
            orderInsurance = dao.getOrderInsuranceByServicePoint(o.getQuarter(), o.getId(), servicePointId);
            if (orderInsurance != null && orderInsurance.getDelFlag() == OrderInsurance.DEL_FLAG_DELETE) {
                insuranceFormEnabled = true;
                orderInsurance.setUpdateBy(user);
                orderInsurance.setUpdateDate(date);
                orderInsurance.setDelFlag(0);
                dao.updateOrderInsurance(orderInsurance);
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
            dao.updateFee(params);

            //condition
            condition.setServiceTimes(serviceTimes);
            //comment by ryan at 2018/09/11 移除 orderCharge，engineerTotalCharge
            //condition.setOrderCharge(orderFee.getOrderCharge());
            //condition.setEngineerTotalCharge(orderFee.getEngineerTotalCharge());
            //end
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
            //comment by ryan at 2018/09/11 移除 orderCharge，engineerTotalCharge
            //params.put("orderCharge", orderFee.getOrderCharge());
            //params.put("engineerTotalCharge", orderFee.getEngineerTotalCharge());
            //end
            params.put("serviceTimes", serviceTimes);
            /* 一键添加上门服务时：sub_status=50、pending_type_date = reservation_date = now、pending_type = 0， Add by Zhoucy*/
            params.put("pendingType", new Dict("0", ""));
            //params.put("subStatus", Order.ORDER_SUBSTATUS_SERVICED);
            params.put("pendingTypeDate", date);
            params.put("reservationDate", date);
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

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
                dao.updateStatus(params);
            }

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(o.getQuarter());
            processLog.setAction("确认上门");
            processLog.setOrderId(orderId);
            processLog.setActionComment(String.format("%s%s", confirmType == 0 ? "客服" : "安维", "确认上门"));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(o.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);
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
                    //2019-12-27 统一上门服务跟踪进度格式
                    //processLog.setActionComment(String.format("上门服务:添加订单具体服务项目:%s,产品:%s", model.getServiceType().getName(), model.getProduct().getName()));
                    if(StringUtils.isBlank(model.getOtherActionRemark())){
                        processLog.setActionComment(String.format("%s【%s】", model.getServiceType().getName(), model.getProduct().getName()));
                    }else{
                        processLog.setActionComment(String.format("%s【%s】其他故障:【%s】", model.getServiceType().getName(), model.getProduct().getName(),model.getOtherActionRemark()));
                    }
                    processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
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
                    processLog.setDataSourceId(o.getDataSourceId());
                    orderService.saveOrderProcessLogNew(processLog);
                    //insert
                    model.setQuarter(o.getQuarter());
                    Long delFalg = Long.valueOf(model.getDelFlag().toString());//*
                    model.setDelFlag(0);//* 还原
                    if(model.getServiceCategory() == null || model.getServiceCategory().getIntValue() == 0){
                        //调用方未设定，以下单时的工单类型为准
                        model.setServiceCategory(new Dict(condition.getOrderServiceType(),""));
                    }
                    if(model.getErrorType() == null || model.getErrorType().getId() == null){
                        if(errorType == null) {
                            errorType = new MDErrorType();
                            errorType.setId(0L);
                        }
                        model.setErrorType(errorType);
                    }
                    if(model.getErrorCode() == null || model.getErrorCode().getId() == null){
                        if(errorCode == null) {
                            errorCode = new MDErrorCode();
                            errorCode.setId(0L);
                        }
                        model.setErrorCode(errorCode);
                    }
                    if(model.getActionCode() == null || model.getActionCode().getId() == null){
                        if(actionCode == null) {
                            actionCode = new MDActionCodeDto();
                            actionCode.setId(0L);
                            actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                        }
                        model.setActionCode(actionCode);
                    }
                    model.setOtherActionRemark(StringUtils.trimToEmpty(model.getOtherActionRemark()));
                    dao.insertDetail(model);
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
            if (1 == confirmType) {
                params.clear();
                params.put("quarter", o.getQuarter());
                params.put("id", orderId);
                params.put("confirmDoor", 1);
                orderHeadDao.updateOrder(params);//2020-12-03 sd_order -> sd_order_head
            }

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
            dao.updateOrderServicePointFeeByMaps(params);

            // 2020-02-15 网点订单数据更新
            servicePointOrderBusinessService.confirmOnSiteService(orderId, o.getQuarter(), servicePointId, engineer.getId(), status.getIntValue(), -1, user.getId(), date.getTime());

            //cache
            OrderCacheUtils.setDetailActionFlag(orderId);
            OrderCacheUtils.delete(orderId);

            //region B2B消息队列
            if (prevStatus == Order.ORDER_STATUS_PLANNED || o.getDataSourceId() == B2BDataSourceEnum.VIOMI.id
                    || o.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                Long engineerId = condition.getEngineer() == null ? null : condition.getEngineer().getId();
                b2BCenterOrderService.serviceOrder(o, pointId, engineerId, user, date);
            }
            //endregion B2B消息队列

        } catch (OrderException oe) {
            throw new OrderException(oe.getMessage());
        } catch (Exception e) {
            log.error("[OrderService.confirmDoorAuto] orderId:{}", orderId, e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    //endregion 上门服务

    //region 待回访

    /**
     * 转到预约超期列表
     * 变更subStatus: 50
     * pendingTypeDate:now()
     *
     * @param order
     */
    @Transactional(readOnly = false)
    public void toArriveAppointment(Order order) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = order.getCreateBy();
        try {
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("subStatus", Order.ORDER_SUBSTATUS_SERVICED);
            params.put("pendingTypeDate", date);
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

        } catch (OrderException oe) {
            throw new OrderException(oe.getMessage());
        } catch (Exception e) {
            log.error("[deleteDetail]orderId:{}", order.getId(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
        }
    }

    //endregion

    //region 公共方法

    /**
     * 返回产品及其服务项目组成的键值对列表
     * @param items 服务项目列表
     * @return
     */
    public List<NameValuePair<Long,Long>> getItemProductAndServiceTypePairs(List<OrderItem> items){
        if(CollectionUtils.isEmpty(items)){
            return null;
        }
        Set<NameValuePair<Long, Long>> valuePairs = items.stream().map(t -> {
            return new NameValuePair<Long, Long>(t.getProductId(), t.getServiceType().getId());
        }).collect(Collectors.toSet());
        return new ArrayList<NameValuePair<Long,Long>>(valuePairs);
    }

    /**
     * 获取互助基金信息
     * @param orderId 工单id
     * @param quarter
     * @return
     */
    public List<OrderInsurance> getOrderInsurance(Long orderId,String quarter){
        List<OrderInsurance> orderInsurances = dao.getOrderInsurances(quarter,orderId);
        if(orderInsurances!=null && orderInsurances.size()>0){
            List<Long> servicePointId = orderInsurances.stream().map(OrderInsurance::getServicePointId).collect(Collectors.toList());
            Map<Long,String> servicePointMap = MDUtils.getServicePointNamesByIds(servicePointId);
            String servicePointName = "";
            for (OrderInsurance item:orderInsurances){
                servicePointName = servicePointMap.get(item.getServicePointId());
                if(StringUtils.isNotBlank(servicePointName)){
                    item.setServicePointName(servicePointName);
                }
            }
        }else {
            return Lists.newArrayList();
        }
        return orderInsurances;
    }

    //endregion
}
