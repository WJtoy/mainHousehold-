/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.operation.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.kkl.kklplus.entity.cc.vm.ReminderTimeLinessModel;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.operation.sd.dao.OperationOrderListDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.HistoryPlanOrderServiceItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RegionSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import com.wolfking.jeesite.ms.cc.entity.mapper.OrderReminderVMMapper;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourseImage;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallServiceMonitorSearchVM;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 开发订单列表Service
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OperationOrderListService {

    //地址限制查询key生存周期(秒)
    public static final long ADDRESS_QUERY_LOCK_TIMEOUT = 3 * 60;

    @Resource
    private OperationOrderListDao operationOrderListDao;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderService orderService;

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
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private MSServicePointService msServicePointService;

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

        List<Order> list = operationOrderListDao.findKefuPlaningOrderList(entity);
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

        List<Order> list = operationOrderListDao.findKefuNoAppointmentOrderList(entity);
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

        List<Order> list = operationOrderListDao.findKefuArriveAppointmentOrderList(entity);
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

        List<Order> list = operationOrderListDao.findKefuPassAppointmentOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuPendingOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuServicedOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuFollowUpFailOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuUncompletedOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuAllOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuCompletedOrderList(entity);
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
        List<TmallAnomalyRecourse> list = operationOrderListDao.findKefuTmallAnomalyList(entity);
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
        List<TmallServiceMonitor> list = operationOrderListDao.findKefuTmallServiceMonitorList(entity);
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

        List<Order> list = operationOrderListDao.findKefuRushingOrderList(entity);
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
        List<Order> list = operationOrderListDao.findKefuComplainOrderList(entity);
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
        List<Order> list = operationOrderListDao.findReminderOrderLit(entity);
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
            if(!ObjectUtils.isEmpty(quarters)){
                searchModel.setQuarters(quarters);
            }
        }
        List<HistoryPlanOrderModel> list = operationOrderListDao.findHistoryPlaningOrderList(searchModel);
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
                if(!ObjectUtils.isEmpty(productCategoryList)){
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
                if(!ObjectUtils.isEmpty(models)){
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

    /**
     * 查询退单待审核列表
     */
    public Page<Order> getOrderReturnApproveList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);
        List<Order> orderList = operationOrderListDao.getOrderReturnApproveList(searchModel);
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
            List<ServicePointFinance> financeList = operationOrderListDao.getBalanceInfoByServicePointIds(Lists.newArrayList(servicePointIds));
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
                complainStatusList = operationOrderListDao.getComplainStatusByOrderIds(item.getKey(), item.getValue());
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

    /**
     * 装载并检查账号负责区域
     */
    public String loadAndCheckUserRegions(RegionSearchModel searchModel, User user) {
        //界面未选择区域
        if (searchModel.getArea() == null || searchModel.getArea().getId() == null) {
            searchModel.setArea(new Area(0L));
            searchModel.setAreaLevel(null);
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }

}
