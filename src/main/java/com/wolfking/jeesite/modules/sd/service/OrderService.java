/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BTmallConstant;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardStatusUpdateMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.entity.lm.mq.MQLMExpress;
import com.kkl.kklplus.entity.md.*;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.kkl.kklplus.entity.md.dto.MDRegionAttributesDto;
import com.kkl.kklplus.entity.praise.*;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.rpt.common.RPTOrderProcessTypeEnum;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.kkl.kklplus.entity.sys.mq.MQSysShortMessage;
import com.kkl.kklplus.entity.voiceservice.OperateType;
import com.kkl.kklplus.entity.voiceservice.mq.MQVoiceSeviceMessage;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.*;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerBlockCurrencyService;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaTimelinessModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.TimelinessChargeModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.ServicePointUtils;
import com.wolfking.jeesite.modules.mq.conf.NoticeMessageConfig;
import com.wolfking.jeesite.modules.mq.dao.OrderAutoCompleteDao;
import com.wolfking.jeesite.modules.mq.dto.*;
import com.wolfking.jeesite.modules.mq.entity.OrderAutoComplete;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderProcessModel;
import com.wolfking.jeesite.modules.mq.sender.NoticeMessageSender;
import com.wolfking.jeesite.modules.mq.sender.OrderAutoCompleteDelaySender;
import com.wolfking.jeesite.modules.mq.sender.OrderGradeMessageSender;
import com.wolfking.jeesite.modules.mq.sender.OrderReportSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsCallbackTaskMQSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.mq.sender.voice.NewTaskMQSender;
import com.wolfking.jeesite.modules.mq.sender.voice.OperateTaskMQSender;
import com.wolfking.jeesite.modules.mq.service.OrderAutoChargeCheckService;
import com.wolfking.jeesite.modules.mq.service.RPTOrderProcessService;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.dao.*;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderGradeMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.viewModel.*;
import com.wolfking.jeesite.modules.sd.utils.*;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserKeFuService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.td.entity.Message2;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderModifyService;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderProcessLogService;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterPushOrderInfoToMsService;
import com.wolfking.jeesite.ms.cc.entity.ReminderAutoCloseTypeEnum;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.logistics.service.LogisticsBusinessService;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.praise.mq.sender.PraiseAutoReviewMessageSender;
import com.wolfking.jeesite.ms.praise.service.PraiseFormService;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import com.wolfking.jeesite.ms.providersys.service.MSSysUserCustomerService;
import com.wolfking.jeesite.ms.service.push.APPMessagePushService;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.um.sd.service.UMOrderService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;
import static java.util.Optional.ofNullable;

/**
 * 订单Service
 */
@Service
@Configurable
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Autowired
    protected OrderDao dao;

    @Autowired
    private OrderHeadDao orderHeadDao;

    @Autowired
    private OrderGradeDao gradeDao;

    @Autowired
    protected CustomerFinanceDao customerFinanceDao;

    @Autowired
    protected CustomerCurrencyDao customerCurrencyDao;

    @Autowired
    protected ServicePointDao servicePointDao;

    @Autowired
    protected OrderAttachmentDao attachmentDao;

    @Autowired
    protected OrderComplainDao complainDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected FeedbackDao feedbackDao;

    @Autowired
    private OrderAutoCompleteDao autoCompleteDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private OrderGradeMessageSender gradeMessageSender;

    @Autowired
    private SystemService systemService;

    @Autowired
    private CustomerTimelinessService customerTimelinessService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private TimelinessLevelService timelinessLevelService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private TimeLinessPriceService timeLinessPriceService;

    @Autowired
    private MSAreaTimeLinessService msAreaTimeLinessService;

    @Autowired
    private MSCustomerService msCustomerService;

    //region 消息队列

    @Autowired
    private OrderReportSender orderReportSender;

    @Autowired
    private SmsMQSender smsMQSender;

    @Autowired
    private NoticeMessageSender noticeMessageSender;

    //短信回访
    @Autowired
    private SmsCallbackTaskMQSender smsCallbackTaskMQSender;

    //endregion 消息队列

    @Autowired
    private APPMessagePushService appMessagePushService;

    @Autowired
    private InsurancePriceService insurancePriceService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private UrgentCustomerService urgentCustomerService;

    @Autowired
    private OrderVoiceTaskService orderVoiceTaskService;

    @Autowired
    private OrderGradeService orderGradeService;

    @Autowired
    private OrderAutoCompleteDelaySender orderAutoCompleteDelaySender;

    @Autowired
    B2BCenterOrderModifyService b2BCenterOrderModifyService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private UMOrderService umOrderService;
    @Autowired
    private B2BCenterPushOrderInfoToMsService b2BCenterPushOrderInfoToMsService;

    @Autowired
    private LogisticsBusinessService logisticsBusinessService;

    @Autowired
    private OrderMQService orderMQService;

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private AreaService areaService;

    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;

    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    @Resource
    private OrderItemCompleteDao orderItemCompleteDao;

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;

    @Autowired
    private OperateTaskMQSender operateTaskMQSender;

    @Autowired
    private NewTaskMQSender newTaskMQSender;

    @Autowired
    private ProductCategoryService productCategoryService;

    //不发短信的数据源设定
    @Value("${shortmessage.ignore-data-sources}")
    private String smIgnoreDataSources;

    @Autowired
    private OrderCacheReadService orderCacheReadService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private B2BCenterOrderProcessLogService b2BCenterOrderProcessLogService;

    @Autowired
    private OrderLocationService orderLocationService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private OrderAutoChargeCheckService orderAutoChargeCheckService;

    //region 好评单

    @Autowired
    private PraiseFormService praiseFormService;

    @Autowired
    private PraiseAutoReviewMessageSender praiseAutoReviewMessageSender;

    //endregion

    @Autowired
    private OrderServicePointFeeService orderServicePointFeeService;

    @Autowired
    private OrderServicepointReceivableService orderServicepointReceivableService;

    @Autowired
    private OrderCrushService crushService;

    @Autowired
    private OrderFeeService orderFeeService;

    @Value("${voiceService.enabled}")
    private boolean voiceEnabled;

    @Value("${site.code}")
    private String siteCode;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSGradeService msGradeService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private RPTOrderProcessService orderProcessService;

    @Autowired
    private SequenceIdService sequenceIdService;

    //region 完工维修
    @Autowired
    private MSErrorTypeService msErrorTypeService;
    @Autowired
    private MSErrorCodeService msErrorCodeService;

    @Autowired
    private MSActionCodeService msActionCodeService;

    @Autowired
    private OrderOpitionTraceService orderOpitionTraceService;

    @Autowired
    private AbnormalFormService abnormalFormService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;

    @Autowired
    private MSRegionPermissionService regionPermissionService;

    //endregion 完工维修
    @Autowired
    private MSSysUserCustomerService msSysUserCustomerService;

    @Autowired
    private OrderReturnCompleteService returnCompleteService;
    @Autowired
    private OrderAdditionalInfoService orderAdditionalInfoService;

    @Autowired
    private MSRegionPermissionNewService regionPermissionNewService;
    @Autowired
    private CustomerBlockCurrencyService customerBlockCurrencyService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private MSSysOfficeService sysOfficeService;


    @Autowired
    private MSRegionPermissionNewService msRegionPermissionNewService;

    @Autowired
    private MSServicePointPriceService msServicePointPriceService;

    //region 订单读取

    //切换为微服务
    public OrderStatus getOrderStatusById(Long orderId, String quarter, Boolean fromMasterDb) {
        OrderStatus orderStatus = dao.getOrderStatusById(orderId, quarter, fromMasterDb);
        if (orderStatus != null) {
            if (orderStatus.getCancelResponsible() != null && StringUtils.toInteger(orderStatus.getCancelResponsible().getValue()) > 0) {
                String cancelResponsibleLabel = MSDictUtils.getDictLabel(orderStatus.getCancelResponsible().getValue(), "cancel_responsible", "");
                orderStatus.getCancelResponsible().setLabel(cancelResponsibleLabel);
            }
            //user微服务
            List<Long> userIds = Lists.newArrayList();
            if (orderStatus.getCustomerApproveBy() != null && orderStatus.getCustomerApproveBy().getId() != null) {
                userIds.add(orderStatus.getCustomerApproveBy().getId());
            }
            if (orderStatus.getPlanBy() != null && orderStatus.getPlanBy().getId() != null) {
                userIds.add(orderStatus.getPlanBy().getId());
            }
            if (orderStatus.getCloseBy() != null && orderStatus.getCloseBy().getId() != null) {
                userIds.add(orderStatus.getCloseBy().getId());
            }
            if (orderStatus.getChargeBy() != null && orderStatus.getChargeBy().getId() != null) {
                userIds.add(orderStatus.getChargeBy().getId());
            }
            if (orderStatus.getCancelApplyBy() != null && orderStatus.getCancelApplyBy().getId() != null) {
                userIds.add(orderStatus.getCancelApplyBy().getId());
            }
            if (orderStatus.getCancelApproveBy() != null && orderStatus.getCancelApproveBy().getId() != null) {
                userIds.add(orderStatus.getCancelApproveBy().getId());
            }
            Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(userIds);
            String userName = null;
            if (!nameMap.isEmpty()) {
                if (orderStatus.getCustomerApproveBy() != null && orderStatus.getCustomerApproveBy().getId() != null) {
                    orderStatus.getCustomerApproveBy().setName(StringUtils.toString(nameMap.get(orderStatus.getCustomerApproveBy().getId())));
                }
                if (orderStatus.getPlanBy() != null && orderStatus.getPlanBy().getId() != null) {
                    orderStatus.getPlanBy().setName(StringUtils.toString(nameMap.get(orderStatus.getPlanBy().getId())));
                }
                if (orderStatus.getCloseBy() != null && orderStatus.getCloseBy().getId() != null) {
                    orderStatus.getCloseBy().setName(StringUtils.toString(nameMap.get(orderStatus.getCloseBy().getId())));
                }
                if (orderStatus.getChargeBy() != null && orderStatus.getChargeBy().getId() != null) {
                    orderStatus.getChargeBy().setName(StringUtils.toString(nameMap.get(orderStatus.getChargeBy().getId())));
                }
                if (orderStatus.getCancelApplyBy() != null && orderStatus.getCancelApplyBy().getId() != null) {
                    orderStatus.getCancelApplyBy().setName(StringUtils.toString(nameMap.get(orderStatus.getCancelApplyBy().getId())));
                }
                if (orderStatus.getCancelApproveBy() != null && orderStatus.getCancelApproveBy().getId() != null) {
                    orderStatus.getCancelApproveBy().setName(StringUtils.toString(nameMap.get(orderStatus.getCancelApproveBy().getId())));
                }
            }
        }
        return orderStatus;
    }

    //切换为微服务
    public Order getOrderByIdMS(Long orderId, String quarter) {
        Order order = orderHeadDao.getOrderById(orderId, quarter);//2020-12-17 sd_order -> sd_order_head
        if (order == null) {
            return order;
        }
        //2020-12-17 sd_order -> sd_order_head
        order.setItems(OrderItemUtils.pbToItems(order.getItemsPb()));
        order.setOrderAdditionalInfo(OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb()));
        //end
        OrderItemUtils.setOrderItemProperties(order.getItems(), Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT, CacheDataTypeEnum.EXPPRESSCOMPANY));
        order.setOrderInfo("");//必须设置为空，因为工单缓存读写用到该字段(该字段原来没有被从数据库读取)

        if (order.getOrderType() != null && Integer.parseInt(order.getOrderType().getValue()) > 0) {
            Dict orderTypeDict = MSDictUtils.getDictByValue(order.getOrderType().getValue(), "order_type");
            order.getOrderType().setLabel(orderTypeDict == null ? "" : orderTypeDict.getLabel());
        }
        Dict dataSource;
        if (order.getDataSource() == null || order.getDataSource().getIntValue() == 0) {
            dataSource = MSDictUtils.getDictByValue("1", Order.ORDER_DATA_SOURCE_TYPE);//快可立
        } else {
            dataSource = MSDictUtils.getDictByValue(order.getDataSource().getValue(), Order.ORDER_DATA_SOURCE_TYPE);
        }
        order.getDataSource().setLabel(dataSource == null ? "" : dataSource.getLabel());
        //shop/店铺
        if (order.getDataSource().getIntValue() >= B2BDataSourceEnum.KKL.id && order.getB2bShop() != null && StringUtils.isNotBlank(order.getB2bShop().getShopId())) {
            B2bCustomerMap b2bCustomerMap = b2bCustomerMapService.getShopInfo(order.getDataSource().getIntValue(), order.getB2bShop().getShopId());
            if (b2bCustomerMap != null) {
                order.setB2bShop(b2bCustomerMap);
            }
        }
        //销售渠道
        if (order.getOrderChannel() != null && order.getOrderChannel().getIntValue() >0){
            int channelValue = order.getOrderChannel().getIntValue();
            if(channelValue == 1){
                order.getOrderChannel().setLabel("线下");
            }else{
                Dict channel = MSDictUtils.getDictByValue(order.getOrderChannel().getValue(), "sale_channel");
                if(channel != null) {
                    order.setOrderChannel(channel);
                }
            }
        }
        return order;
    }

    /**
     * 检查订单缓存中基本信息
     * 单头：产品，服务
     * 无数据，读取并更新缓存
     * @param order
     */
    public void doubleCheckOrderCache(Order order){
        if(order == null){
            return;
        }
        List<OrderItem> items = order.getItems();
        if(CollectionUtils.isEmpty(items)){
            reReadOrderItemsAndSyncCache(order);
            return;
        }
        Supplier<Stream<OrderItem>> supplier = () -> items.stream();
        Set<CacheDataTypeEnum> dataTypeSet = Sets.newLinkedHashSet();
        long cnt = supplier.get().filter(t-> t.getProduct() == null).count();
        if(cnt > 0){
            reReadOrderItemsAndSyncCache(order);
            return;
        }

        cnt = supplier.get().filter(t-> t.getServiceType() == null).count();
        if(cnt > 0){
            reReadOrderItemsAndSyncCache(order);
            return;
        }
    }

    /**
     * 重新读取订单项目并更新订单单头缓存
     * @param order
     */
    private void reReadOrderItemsAndSyncCache(Order order){
        Order o = orderItemService.getOrderItems(order.getQuarter(), order.getId());
        if(o == null) {
            return;
        }
        List<OrderItem> items = o.getItems();
        if(CollectionUtils.isEmpty(items)){
            return;
        }
        OrderItemUtils.setOrderItemProperties(items, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT, CacheDataTypeEnum.EXPPRESSCOMPANY));
        order.setItems(items);
        //write cache
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(order.getId())
                .setInfo(order)
                .setSyncDate(System.currentTimeMillis())
                .setExpireSeconds(0L);
        OrderCacheUtils.update(builder.build());
    }

    /**
     * 订单费用
     */
    public OrderFee getOrderFeeById(Long orderId, String quarter, boolean fromMasterDb) {
        //切换为微服务
        OrderFee orderFee = dao.getOrderFeeById(orderId, quarter, fromMasterDb);
        if (orderFee != null) {
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            if (orderFee.getOrderPaymentType() != null && Integer.parseInt(orderFee.getOrderPaymentType().getValue()) > 0) {
                orderFee.setOrderPaymentType(paymentTypeMap.get(orderFee.getOrderPaymentType().getValue()));
            }
            if (orderFee.getEngineerPaymentType() != null && Integer.parseInt(orderFee.getEngineerPaymentType().getValue()) > 0) {
                orderFee.setEngineerPaymentType(paymentTypeMap.get(orderFee.getEngineerPaymentType().getValue()));
            }
        }
        return orderFee;
    }

    /**
     * 网点费用汇总
     *
     * @param orderId
     * @param quarter
     * @param servicePointId
     * @return
     */
    public OrderServicePointFee getOrderServicePointFee(Long orderId, String quarter, Long servicePointId) {
        return dao.getOrderServicePointFee(quarter, orderId, servicePointId);
    }

    /**
     * 网点费用汇总
     *
     * @param orderId
     * @param quarter
     * @return
     */
    public List<OrderServicePointFee> getOrderServicePointFees(Long orderId, String quarter, boolean fromMasterDb) {
        List<OrderServicePointFee> fees = dao.getOrderServicePointFees(quarter, orderId, fromMasterDb);
        OrderServicePointFee fee;
        ServicePoint servicePoint;
        if (fees != null && fees.size() > 0) {
            for (int i = 0, size = fees.size(); i < size; i++) {
                fee = fees.get(i);
                servicePoint = servicePointService.getFromCache(fee.getServicePoint().getId());
                fee.setServicePoint(servicePoint);
            }
        }
        return fees;
    }

    public Map<Long, OrderServicePointFee> getOrderServicePointFeeMapsForCharge(Long orderId, String quarter) {
        return dao.getOrderServicePointFeesForCharge(quarter, orderId);
    }

    /**
     * 从数据库读取订单信息
     */
    public Order getOrderById(Long orderId, String quarter) {
        return getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, false);
    }

    /**
     * 读取订单当前网点id
     * @param orderId
     * @param quarter
     * @return
     */
    public Long getCurrentServicePointId(Long orderId, String quarter){
        return dao.getCurrentServicePointId(orderId,quarter);
    }


    /*从主库读取 2017/11/19*/
    public OrderCondition getOrderConditionFromMasterById(Long orderId, String quarter) {
        //切换为微服务
        OrderCondition orderCondition = dao.getOrderConditionFromMasterById(orderId, quarter);
        // add on 2019-9-25 begin
        //ServicePoint微服务
        if (orderCondition != null && orderCondition.getServicePoint() != null && orderCondition.getServicePoint().getId() != null) {
            ServicePoint servicePoint = msServicePointService.getById( orderCondition.getServicePoint().getId());
            if (servicePoint != null) {
                orderCondition.getServicePoint().setServicePointNo(servicePoint.getServicePointNo());
                orderCondition.getServicePoint().setName(servicePoint.getName());
            }
        }
        // add on 2019-10-28 begin
        if (orderCondition != null && orderCondition.getEngineer() != null && orderCondition.getEngineer().getId() != null) {
            Engineer engineer = msEngineerService.getByIdFromCache(orderCondition.getEngineer().getId());
            if (engineer != null) {
                orderCondition.getEngineer().setName(engineer.getName());
                orderCondition.getEngineer().setMobile(engineer.getContactInfo());
            }
        }
        // add on 2019-10-28 end
        // add on 2019-9-25 end
        if (orderCondition.getPendingType() != null && Integer.parseInt(orderCondition.getPendingType().getValue()) > 0) {
            String pendingTypeLabel = MSDictUtils.getDictLabel(orderCondition.getPendingType().getValue(), "PendingType", "");
            orderCondition.getPendingType().setLabel(pendingTypeLabel);
        }
        if (orderCondition.getStatus() != null && Integer.parseInt(orderCondition.getStatus().getValue()) > 0) {
            String orderStatusLabel = MSDictUtils.getDictLabel(orderCondition.getStatus().getValue(), "order_status", "");
            orderCondition.getStatus().setLabel(orderStatusLabel);
        }
        if (orderCondition.getUrgentLevel() == null || orderCondition.getUrgentLevel().getId().longValue() == 0) {
            orderCondition.setUrgentLevel(new UrgentLevel(0l, "不加急"));
        } else {
            UrgentLevel urgentLevel = urgentLevelService.getFromCache(orderCondition.getUrgentLevel().getId());
            if (urgentLevel == null) {
                urgentLevel = orderCondition.getUrgentLevel();
                urgentLevel.setRemarks("读取错误");
            }
            orderCondition.setUrgentLevel(urgentLevel);
        }
        //客户微服务
        if (orderCondition.getCustomer() != null && orderCondition.getCustomer().getId() != null) {
            Customer customer = msCustomerService.getByIdToCustomer(orderCondition.getCustomer().getId());
            if(customer != null && customer.getId() != null && customer.getId()>0) {
                orderCondition.setCustomer(customer);
            }
        }
        //user微服务
        List<Long> userIds = Lists.newArrayList();
        if (orderCondition.getCustomer() != null && orderCondition.getCustomer().getSales() != null && orderCondition.getCustomer().getSales().getId() != null) {
            userIds.add(orderCondition.getCustomer().getSales().getId());
        }
        if (orderCondition.getCreateBy() != null && orderCondition.getCreateBy().getId() != null) {
            userIds.add(orderCondition.getCreateBy().getId());
        }
        if (orderCondition.getUpdateBy() != null && orderCondition.getUpdateBy().getId() != null) {
            userIds.add(orderCondition.getUpdateBy().getId());
        }
        if (orderCondition.getKefu() != null && orderCondition.getKefu().getId() != null) {
            userIds.add(orderCondition.getKefu().getId());
        }
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(userIds);
        User user = null;
        if (!userMap.isEmpty()) {
            if (orderCondition.getCustomer() != null && orderCondition.getCustomer().getSales() != null && orderCondition.getCustomer().getSales().getId() != null) {
                user = userMap.get(orderCondition.getCustomer().getSales().getId());
                if (user != null) {
                    orderCondition.getCustomer().setSales(user);
                }
            }
            if (orderCondition.getCreateBy() != null && orderCondition.getCreateBy().getId() != null) {
                user = userMap.get(orderCondition.getCreateBy().getId());
                if (user != null) {
                    orderCondition.setCreateBy(user);
                }
            }
            if (orderCondition.getUpdateBy() != null && orderCondition.getUpdateBy().getId() != null) {
                user = userMap.get(orderCondition.getUpdateBy().getId());
                if (user != null) {
                    orderCondition.setUpdateBy(user);
                }
            }
            if (orderCondition.getKefu() != null && orderCondition.getKefu().getId() != null) {
                user = userMap.get(orderCondition.getKefu().getId());
                if (user != null) {
                    orderCondition.setKefu(user);
                }
            }
        }

        return orderCondition;
    }

    /**
     * 从主库读个别特殊字段，主要给并发处理判断使用 2018/01/16
     * 状态，结帐标记，客评标记，配件标记
     */
    public Map<String, Object> getOrderConditionSpecialFromMasterById(Long orderId, String quarter){
        if(orderId == null || orderId.longValue() <= 0  || StringUtils.isBlank(quarter)){
            return null;
        }
        return dao.getOrderConditionSpecialFromMasterById(orderId,quarter);
    }

    /**
     * 从主库读取订单信息
     */
    @Transactional(readOnly = true)
    public Order getOrderFromMasterDb(Long orderId, String quarter) {
        Order order = null;
        try {
            order = orderHeadDao.getOrderFromMasterById(orderId, quarter);
            if (order != null) {
                //2020-12-17 sd_order -> sd_order_head
                order.setItems(OrderItemUtils.pbToItems(order.getItemsPb()));
                order.setOrderAdditionalInfo(OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb()));
                OrderItemUtils.setOrderItemProperties(order.getItems(), Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT, CacheDataTypeEnum.EXPPRESSCOMPANY));
                order.setOrderInfo("");//必须设置为空，因为工单缓存读写用到该字段(该字段原来没有被从数据库读取)
            }
            //切换为微服务
            if (order.getOrderType() != null && Integer.parseInt(order.getOrderType().getValue()) > 0) {
                Dict orderTypeDict = MSDictUtils.getDictByValue(order.getOrderType().getValue(), "order_type");
                order.getOrderType().setLabel(orderTypeDict == null ? "" : orderTypeDict.getLabel());
            }

            //切换为微服务
            OrderCondition condition = getOrderConditionFromMasterById(orderId, quarter);
            order.setOrderCondition(condition);

            OrderStatus status = getOrderStatusById(orderId, quarter, true);//切换为微服务
            order.setOrderStatus(status);

            OrderFee fee = dao.getOrderFeeFromMasterById(orderId, quarter);
            //切换为微服务
            if (fee != null) {
                Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
                if (fee.getOrderPaymentType() != null && Integer.parseInt(fee.getOrderPaymentType().getValue()) > 0) {
                    fee.setOrderPaymentType(paymentTypeMap.get(fee.getOrderPaymentType().getValue()));
                }
                if (fee.getEngineerPaymentType() != null && Integer.parseInt(fee.getEngineerPaymentType().getValue()) > 0) {
                    fee.setEngineerPaymentType(paymentTypeMap.get(fee.getEngineerPaymentType().getValue()));
                }
            }

            order.setOrderFee(fee);
            //网点费用汇总
            order.setServicePointFees(dao.getOrderServicePointFees(order.getQuarter(), orderId, true));
        } catch (Exception e) {
            LogUtils.saveLog("读取订单错误", "OrderService.getOrderFromMasterDb", String.format("id:%s,quarter:%s", orderId, quarter), e, null);
        }
        return order;
    }

    /**
     * 从主库读取派单时预设的费用
     * 因主从等原因，造成上门时从库数据不一定生效
     */
    public OrderFee getPresetFeeWhenPlanFromMasterDB(Long orderId,String quarter){
        return dao.getPresetFeeWhenPlanFromMasterDB(orderId,quarter);
    }
    /**
     * 读取订单信息
     *
     * @param orderId    订单id
     * @param level      读取订单内容类别（见OrderUtils.OrderDataLevel）
     * @param cacheFirst 缓存优先
     * @return
     */
    public Order getOrderById(Long orderId, String quarter, OrderUtils.OrderDataLevel level, boolean cacheFirst) {
        return getOrderById(orderId, quarter, level, cacheFirst, false);
    }

    /**
     * 读取订单信息
     *
     * @param orderId      订单id
     * @param level        读取订单内容类别（见OrderUtils.OrderDataLevel）
     * @param cacheFirst   缓存优先
     * @param fromMasterDb 数据读取主库(包含condition部分，fee，details)
     * @return
     */
    public Order getOrderById(Long orderId, String quarter, OrderUtils.OrderDataLevel level, boolean cacheFirst, boolean fromMasterDb) {
        return getOrderById(orderId, quarter, level, cacheFirst, fromMasterDb,false,false);
    }

    /**
     * 读取订单信息
     *
     * @param orderId      订单id
     * @param level        读取订单内容类别（见OrderUtils.OrderDataLevel）
     * @param cacheFirst   缓存优先
     * @param fromMasterDb 数据读取主库(包含condition部分，fee，details)
     * @param checkFixSpec 是否检查产品有无安装规范
     * @return
     */
    public Order getOrderById(Long orderId, String quarter, OrderUtils.OrderDataLevel level, boolean cacheFirst, boolean fromMasterDb,boolean checkFixSpec){
        return getOrderById(orderId, quarter, level, cacheFirst, fromMasterDb,checkFixSpec,false);
    }

    /**
     * 读取订单信息
     *
     * @param orderId      订单id
     * @param level        读取订单内容类别（见OrderUtils.OrderDataLevel）
     * @param cacheFirst   缓存优先
     * @param fromMasterDb 数据读取主库(包含condition部分，fee，details)
     * @param checkFixSpec 是否检查产品有无安装规范
     * @param loadCategory 是否读取产品品类信息
     * @return
     */
    public Order getOrderById(Long orderId, String quarter, OrderUtils.OrderDataLevel level, boolean cacheFirst, boolean fromMasterDb,boolean checkFixSpec,boolean loadCategory) {
        Order order = orderCacheReadService.getOrderById(orderId, quarter, level, cacheFirst, fromMasterDb);
        List<OrderItem> items = ofNullable(order).map(o -> o.getItems()).orElse(Lists.newArrayList());
        Long customerId= ofNullable(order).map(o->o.getOrderCondition())
                .map(o->o.getCustomer()).map(c->c.getId()).orElse(0L);
        //调用微服务判断各个产品是否有安装规范
        if(checkFixSpec && !CollectionUtils.isEmpty(items) && customerId.longValue() >0){
            Set<Long> productIds =items.stream().map(t->t.getProduct().getId()).collect(Collectors.toSet());
            Map<Long,Integer> fixSpecFlags = msCustomerProductService.findFixSpecByCustomerIdAndProductIdsFromCacheForSD(customerId,new ArrayList<Long>(productIds));
            for (OrderItem item : items) {
                Integer fixSpecFlag = fixSpecFlags.get(item.getProduct().getId());
                if(fixSpecFlag != null) {
                    item.setFixSpec(fixSpecFlag.intValue());
                }
            }
        }
        //产品品类
        long productCategoryId = Optional.ofNullable(order).map(o->o.getOrderCondition()).map(t->t.getProductCategoryId()).orElse(0L);
        if(loadCategory && productCategoryId >0){
            ProductCategory category = productCategoryService.getByIdForMD(productCategoryId);
            if(category != null){
                order.getOrderCondition().setProductCategory(category);
            }
        }
        return order;
    }

    public boolean removeOrderCache(String id) {
        return OrderCacheUtils.delete(Long.parseLong(id));
    }

    /**
     * 建立订单（v:2 2017/11/12）
     * 将短信及消息推送拆分到消息队列
     * <p>
     * @version 2.1
     * @date 2018/09/03
     * @author ryan
     * 1.去掉sd_orderitem读写
     * 2.sd_order_fee改为消息队列处理
     * 3.md_customer_finance读取栏位精简，
     * 按需读取(lock_flag,credit,credit_flag,block_amount,balance)
     * 4.冻结流水使用消息队列处理(移到service外处理)
     *
     * @version 2.2
     * @date 2019/04/24
     * @author ryan
     * 1.新增地理信息表，存储经纬度等信息
     */
    @Transactional(readOnly = false)
    public void createOrder_v2_1(Order order, Long tmpId) {
        OrderCondition condition = order.getOrderCondition();
        Long areaId = Optional.ofNullable(condition.getArea()).map(t->t.getId()).orElse(0L);
        if(areaId == null || areaId <= 0){
            throw new RuntimeException("地址解析错误：无区/县编码");
        }
        if(condition.getProvinceId() == null || condition.getProvinceId() <=0){
            throw new RuntimeException("地址解析错误：无省份编码");
        }
        if(condition.getCityId() == null || condition.getCityId() <=0){
            throw new RuntimeException("地址解析错误：无市编码");
        }
        User user = order.getCreateBy();
        Date date = order.getCreateDate();

        //保存单头
        //工单附加信息
        String infoJson = OrderAdditionalInfoUtils.toOrderAdditionalInfoJson(order.getOrderAdditionalInfo());
        order.setOrderInfo(StringUtils.toString(infoJson));
        order.setAdditionalInfoPb(OrderAdditionalInfoUtils.additionalInfoToPbBytes(order.getOrderAdditionalInfo()));//2020-12-03 sd_order -> sd_order_head
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            order.setOrderItemJson(OrderItemUtils.toOrderItemsJson(items)); //Add by Zhoucy 2018-6-23
            order.setItemsPb(OrderItemUtils.ItemsToPbBytes(items));//2020-12-03 sd_order -> sd_order_head
        }

        //condition

        //地址特殊处理 2020-05-12 Ryan
        condition.setServiceAddress(StringUtils.filterAddress(condition.getServiceAddress()));
        condition.setAddress(condition.getServiceAddress());

        condition.setPendingTypeDate(date); //Add By Zhoucy
        condition.setCreateBy(user);
        condition.setCreateDate(date);
        condition.setCustomerType(condition.getCustomer().getVipFlag());//客户类型 2019/12/11
        //status
        OrderStatus status = order.getOrderStatus();
        if (condition.getStatus().getValue().equalsIgnoreCase("20")) {
            status.setCustomerApproveBy(user);
            status.setCustomerApproveDate(date);
            status.setCustomerApproveFlag(1);
        } else {
            status.setCustomerApproveBy(new User(0l));
        }
        //fee
        OrderFee fee = order.getOrderFee();
        //save to db
        if(StringUtils.isNotBlank(order.getParentBizOrderId())){
            order.setParentBizOrderId(StringEscapeUtils.unescapeHtml4(order.getParentBizOrderId()).replaceAll("\"","").replaceAll("“","").replaceAll("”","")); //去掉双引号
        }
        orderHeadDao.insert(order);//2020-12-03 sd_order -> sd_order_head
        //productIds超长处理
        condition.setProductIds(StringUtils.left(condition.getProductIds(),50));
        dao.insertCondition(condition);
        dao.insertStatus(status);
        //dao.insertCustomer(customer); comment at 2019/08/17
        //地理信息表
        OrderLocation location = order.getOrderLocation();
        if(location.getOrderId()==null){
            location.setOrderId(order.getId());
            location.setQuarter(order.getQuarter());
            location.setArea(condition.getArea());
        }
        orderLocationService.insert(order.getOrderLocation());
        //更改客户的冻结金额(block_amount)
        customerFinanceDao.incBlockAmount(condition.getCustomer().getId(), fee.getBlockedCharge(), fee.getExpectCharge(), user.getId(), date);

        if (tmpId != null && tmpId > 0) {
            //临时订单表变更
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("id", tmpId);
            params.put("successFlag", 1);
            params.put("orderNo", order.getOrderNo());
            params.put("updateBy", order.getCreateBy());
            params.put("updateDate", order.getCreateDate());
            dao.updateTempOrder(params);
        }
    }

    /**
     * 物理删除订单主表
     *
     * @param id
    2019/08/28 ryan
     @Transactional(readOnly = false)
     public void deleteOrder(Long id) {
     dao.delete(id);
     }
     */

    /**
     * 修改订单
     * <p>
     * v2.1 2018/09/03 ryan
     * 1.去掉sd_orderitem读写
     * 2.修改订单时，加急不变更
     *
     * 2020/05-04 Ryan
     * 1.灯饰下单方式修改订单可修改销售渠道
     */
    @Transactional(readOnly = false)
    public void updateOrder(Order order, OrderFee fee, String orgItemsString) {
        OrderFee orderFee = order.getOrderFee();
        //切换为微服务
        if (fee == null) {
            fee = getOrderFeeById(order.getId(), order.getQuarter(), false);
        }
        // 保存前重新计算派单价，防止前台篡改
        // 先备份原来的金额,后面扣款使用
        Double oExpectCharge = fee.getExpectCharge();
        Double oBlockedCharge = fee.getBlockedCharge();

        // 计算新的金额
        Double charge = 0.0;
        Double blockedCharge = 0.0;
        int qty = 0;
        int itemno = 0;
        OrderCondition condition = order.getOrderCondition();
        condition.preUpdate();
        User user = condition.getUpdateBy();
        Date date = condition.getUpdateDate();
        Long oid = order.getId();
        for (OrderItem item : order.getItems()) {
            itemno = itemno + 10;
            item.setOrderId(oid);
            if (item.getId() == 0 || item.getId() == null) {
                item.setCreateBy(user);
                item.setCreateDate(date);
            }
            item.setUpdateBy(user);
            item.setUpdateDate(date);
            if (item.getDelFlag() != OrderItem.DEL_FLAG_DELETE) {
                charge = charge + item.getCharge();
                blockedCharge = blockedCharge + item.getBlockedCharge();
                qty = qty + item.getQty();
            }
        }
        //更改后的服务费，包含加急费
        fee.setExpectCharge(charge + orderFee.getCustomerUrgentCharge());
        fee.setBlockedCharge(blockedCharge);
        order.setTotalQty(qty);
        condition.setAddress(StringUtils.filterAddress(condition.getAddress()));

        HashMap<String, Object> params = Maps.newHashMap();
        // save order
        params.put("quarter", order.getQuarter());
        params.put("id", order.getId());//key
        params.put("description", order.getDescription());
        params.put("totalQty", order.getTotalQty());
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            params.put("orderItemJson", OrderItemUtils.toOrderItemsJson(order.getItems()));
            params.put("itemsPb",OrderItemUtils.ItemsToPbBytes(order.getItems()));//2020-12-03 sd_order -> sd_order_head
        } else {
            params.put("orderItemJson", null);
            params.put("itemsPb",null);//2020-12-03 sd_order -> sd_order_head
        }
        if(order.getVerificationCode().equals("sencond:order")){
            params.put("shopId",order.getB2bShop()==null?"":order.getB2bShop().getShopId());
            params.put("orderChannel",order.getOrderChannel().getIntValue());//销售渠道
            params.put("workCardId",order.getWorkCardId());//第三方单号
            params.put("parentBizOrderId",order.getParentBizOrderId());
        }
        orderHeadDao.updateOrder(params);//2020-12-03 sd_order -> sd_order_head
        //orderItemCompleteService.deleteOrderCompletePics(order.getId(), order.getQuarter());// 2021/04/05 修改订单不删除完成照片

        //condition
        Area sub_area = null;
        if ( !org.springframework.util.ObjectUtils.isEmpty(condition.getSubArea()) && !org.springframework.util.ObjectUtils.isEmpty(condition.getSubArea().getId())) {
            sub_area = areaService.getFromCache(condition.getSubArea().getId());
        }

        params.clear();
        params.put("quarter", order.getQuarter());
        params.put("orderId", order.getId());
        params.put("provinceId", condition.getProvinceId()); // 2019-09-26
        params.put("cityId", condition.getCityId());    // 2019-09-26
        params.put("area", condition.getArea());
        params.put("subArea",sub_area);  //add on 2019-5-21
        params.put("userName", condition.getUserName());
        params.put("phone1", condition.getPhone1());
        params.put("phone2", condition.getPhone2());
        params.put("servicePhone", condition.getServicePhone());
        params.put("address", condition.getAddress());
        params.put("serviceAddress", condition.getAddress());
        //params.put("totalQty",order.getTotalQty());
        params.put("updateBy", user);
        params.put("updateDate", date);

        params.put("productIds", condition.getProductIds());
        params.put("productCategoryId", condition.getProductCategoryId()); // 2019-09-26
        params.put("hasSet", condition.getHasSet());
        params.put("serviceTypes", condition.getServiceTypes());
        params.put("orderServiceType", condition.getOrderServiceType());//12-06
        params.put("kefuType",condition.getKefuType());
        params.put("canRush",condition.getCanRush());
        //加急等级,不变更
        //params.put("urgentLevel", condition.getUrgentLevel());
        dao.updateCondition(params);

        //fee
        params.clear();
        params.put("quarter", order.getQuarter());
        params.put("orderId", order.getId());
        params.put("expectCharge", fee.getExpectCharge());
        params.put("blockedCharge", fee.getBlockedCharge());
        //加急
        params.put("engineerTotalCharge", 0);//否则engineerTotalCharge会汇总engineerUrgentCharge
        params.put("orderCharge", 0);//同上
        dao.updateFee(params);

        //更改前获得balance
        Customer customer = customerService.getFromCache(condition.getCustomer().getId());
        CustomerFinance finance = customerFinanceDao.getAmounts(condition.getCustomer().getId());
        //金额差异
        //fee.expectCharge 为重新计算后的服务费，包含加急费
        double blockChargeDiff = fee.getExpectCharge() - oExpectCharge + fee.getBlockedCharge() - oBlockedCharge;
        //调整customer finance
        if (blockChargeDiff != 0) {
            //blockedCharge = blockedCharge - 原blockedCharge + 新blockedCharge - 原ExpectCharge + 新ExpectCharge
            customerFinanceDao.incBlockAmount(condition.getCustomer().getId(), fee.getBlockedCharge() - oBlockedCharge, fee.getExpectCharge() - oExpectCharge, user.getId(), date);
        }

        //log
        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setQuarter(order.getQuarter());
        processLog.setOrderId(order.getId());
        processLog.setAction("修改订单");
        processLog.setActionComment(
                StringUtils.left(
                        String.format(
                                "修改订单:%s,修改人:%s,修改前项目：%s %s",
                                order.getOrderNo(),
                                user.getName(),
                                System.getProperty("line.separator"),
                                orgItemsString
                        ),
                        250
                )
        );
        String status = MSDictUtils.getDictLabel(order.getOrderCondition().getStatus().getValue(), "order_status", "订单已审核");//切换为微服务
        processLog.setStatus(status);
        processLog.setStatusValue(Integer.parseInt((order.getOrderCondition().getStatus().getValue())));
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(user);
        processLog.setCreateDate(date);
        processLog.setCustomerId(condition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        saveOrderProcessLogNew(processLog);


        Order norder = getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, false);
        //如已派单，重新计算：预估服务费用
        long servicePointId = ofNullable(norder).map(t->t.getOrderCondition()).map(t->t.getServicePoint()).map(t->t.getId()).map(t->t.longValue()).orElse(0l);
        if(servicePointId > 0){
            long engineerId = ofNullable(norder).map(t->t.getOrderCondition()).map(t->t.getEngineer()).map(t->t.getId()).map(t->t.longValue()).orElse(0l);
            if(engineerId>0){
                OrderPlan orderPlan = dao.getOrderPlan(order.getId(), order.getQuarter(), servicePointId, engineerId);
                if(orderPlan != null) {
                    double estimatedServiceCost = calcServicePointCost(condition,norder.getOrderCondition().getServicePoint(), order.getItems());
                    HashMap<String, Object> planMaps = Maps.newHashMapWithExpectedSize(5);
                    planMaps.put("id", orderPlan.getId());
                    planMaps.put("estimatedServiceCost", estimatedServiceCost);//预估服务费用
                    planMaps.put("updateBy", user);
                    planMaps.put("updateDate", date);
                    dao.UpdateOrderPlan(planMaps);
                }
            }
        }
        //因订单当头或item变更，需要更新json
        //更新order.jsongetOrderById
        //调用公共缓存
        OrderCacheUtils.delete(order.getId());

        // 修改B2B工单
        b2BCenterOrderModifyService.modifyB2BOrder(order, false);

        if (blockChargeDiff != 0) {
            CustomerCurrency currency = new CustomerCurrency();
            //分片根据冻结记录创建时间计算
            currency.setQuarter(QuarterUtils.getSeasonQuarter(date));
            currency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency)); //Production
            currency.setCustomer(condition.getCustomer());
            //差异
            currency.setCurrencyType(blockChargeDiff > 0 ? CustomerCurrency.CURRENCY_TYPE_IN : CustomerCurrency.CURRENCY_TYPE_OUT);
            currency.setCurrencyNo(order.getOrderNo());
            currency.setBeforeBalance(finance.getBlockAmount());//变更前冻结金额
            currency.setBalance(finance.getBlockAmount() + blockChargeDiff);//变更后
            currency.setAmount(blockChargeDiff);
            currency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH);
            currency.setActionType(70);
            currency.setCreateBy(user);
            currency.setCreateDate(date);
            currency.setRemarks(
                    String.format("修改订单，冻结变更为 %.2f ，原冻结费用为%.2f 相关单号为:%s",
                            (fee.getExpectCharge() + fee.getBlockedCharge()),
                            (oExpectCharge + oBlockedCharge),
                            order.getOrderNo()
                    )
            );
            //切分冻结流水
            customerBlockCurrencyService.saveCustomerBlockCurrency(currency);
        }
    }

    /**
     * 下单消息队列消费
     * 增加下单日志及费用表记录
     * 增加新表:订单状态标记表 2020-04-01
     */
    @Transactional(readOnly = false)
    public void insertCreateLogAndFee(OrderFee fee, OrderProcessLog log) {
        dao.insertFee(fee);
        saveOrderProcessLogNew(log);
        OrderStatusFlag status = OrderStatusFlag.builder()
                .orderId(fee.getOrderId())
                .quarter(fee.getQuarter())
                .praiseStatus(0)
                .build();
        orderStatusFlagService.insert(status);
    }

    /**
     * 修改订单Condition
     * 使用hashmap传值，不存在的key在mybatis里为null
     */
    @Transactional(readOnly = false)
    public void updateOrderCondition(HashMap<String, Object> map) {
        dao.updateCondition(map);
    }

    /**
     * 更新催单信息
     */
    @Transactional
    public int updateReminderInfo(HashMap<String, Object> map) {
        dao.updateReminderInfo(map);
        int rtn = dao.updateConditionReminderFlag(map);
        //同步网点工单数据
        //有网点才同步
        Long spId = (Long)map.get("servicePointId");
        if(spId != null && spId > 0) {
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

    //切换为微服务
    public List<OrderDetail> getOrderDetails(Long orderId, String quarter, Boolean fromMasterDb) {
        List<OrderDetail> orderDetailList = dao.getOrderDetails(orderId, quarter, fromMasterDb);
        if (orderDetailList != null && orderDetailList.size() > 0) {
            // add on 2019-8-23 begin
            // product微服务
            Product product = new Product();
            List<Product> productList = msProductService.findListByConditions(product);
            Map<Long, Product> productMap = Maps.newHashMap();
            if (productList != null && productList.size() >0) {
                productMap = productList.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
            }
            // add on 2019-8-23 end

            // add on 2019-9-25 begin
            // 获取网点的数据
            List<Long> servicePointIds = orderDetailList.stream().map(r->r.getServicePoint().getId()).distinct().collect(Collectors.toList());
            // add on 2019-9-25 end

            // add on 2019-10-12 begin
            Map<Long, String> servicePointMap = MDUtils.getServicePointNamesByIds(servicePointIds);
            // add on 2019-10-12 end

            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");

            //获取服务类型调用微服务 add om 2019-10-15 key:服务类型id,value：服务名称
            Map<Long,String> serviceTypeMap = serviceTypeService.findAllIdsAndNames();
            // end


            // add on 2019-10-28 begin
            List<Long> engineerIds = orderDetailList.stream().map(r->r.getEngineer().getId()).distinct().collect(Collectors.toList());
            Map<Long,String> engineerMap =  MDUtils.getEngineerNamesByIds(engineerIds);
            // add on 2019-10-28 end
            for (OrderDetail orderDetail : orderDetailList) {
                if (orderDetail.getEngineerPaymentType() != null && Integer.parseInt(orderDetail.getEngineerPaymentType().getValue()) > 0) {
                    orderDetail.setEngineerPaymentType(paymentTypeMap.get(orderDetail.getEngineerPaymentType().getValue()));
                }
                // add on 2019-8-23 begin
                Product productEntity = productMap.get(orderDetail.getProduct().getId());
                if (productEntity != null) {
                    orderDetail.getProduct().setName(productEntity.getName());
                    orderDetail.getProduct().setCategory(productEntity.getCategory());
                    orderDetail.getProduct().setSetFlag(productEntity.getSetFlag());
                }
                // add on 2019-8-23 end
                orderDetail.getServicePoint().setName(servicePointMap.get(orderDetail.getServicePoint().getId()));  // add on 2019-9-25

                //服务类型调用服务 add on 2019-10-15
                String serviceName = serviceTypeMap.get(orderDetail.getServiceType().getId());
                if(StringUtils.isNotBlank(serviceName)){
                    orderDetail.getServiceType().setName(serviceName);
                }
                // end
                orderDetail.getEngineer().setName(engineerMap.get(orderDetail.getEngineer().getId()));  // add on 2019-10-28 //Engineer微服务
            }
            //维修故障信息
            getErrorInfoFromMS(orderId,orderDetailList);
        }
        return orderDetailList;
    }

    /**
     * 从微服务读取故障相关信息
     */
    private void getErrorInfoFromMS(Long orderId,List<OrderDetail> list){
        try{
            OrderDetail detail;
            Dict dict;
            // 故障分类
            MDErrorType errorType = null;
            // 故障现象
            MDErrorCode errorCode = null;
            // 故障分析&处理
            MDActionCodeDto actionCode = null;
            long pid;
            Map<String,Dict> orderServiceTypesMap = Maps.newHashMap();
            String strValue;
            Long errorId;
            for(int i=0,size=list.size();i<size;i++){
                detail = list.get(i);
                if(detail.getServiceCategory().getIntValue() > 0) {
                    if (orderServiceTypesMap.isEmpty()) {
                        orderServiceTypesMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_ORDER_SERVICE_TYPE);
                    }
                    dict = orderServiceTypesMap.get(detail.getServiceCategory().getValue());
                    if (dict != null) {
                        detail.setServiceCategory(dict);
                    } else {
                        detail.setServiceCategory(new Dict(detail.getServiceCategory().getIntValue(), ""));
                    }
                }
                errorId = ofNullable(detail.getErrorType()).map(t->t.getId()).orElse(0L);
                if(errorId <= 0){
                    continue;
                }
                pid = detail.getProduct().getId();
                errorType = msErrorTypeService.getByProductIdAndId(pid,errorId);
                if(errorType == null){
                    continue;
                }
                detail.setErrorType(errorType);

                errorId = ofNullable(detail.getErrorCode()).map(t->t.getId()).orElse(0L);
                if(errorId <= 0){
                    continue;
                }
                errorCode = msErrorCodeService.getByProductIdAndId(pid,errorId);
                if(errorCode == null){
                    continue;
                }
                detail.setErrorCode(errorCode);

                errorId = ofNullable(detail.getActionCode()).map(t->t.getId()).orElse(0L);
                if(errorId > 0){
                    actionCode = msActionCodeService.getByProductIdAndId(pid,errorId);
                    if(actionCode != null) {
                        detail.setActionCode(actionCode);
                    }
                }
            }
        }catch (Exception e){
            log.error("从微服务转载读取订单上门服务项目错误,id:{}",orderId,e);
        }
    }

    /**
     * 再次检查前端提交的上门服务内容
     * @param detail
     * @return
     */
    public AjaxJsonEntity checkServiceSubmitInfo(OrderDetail detail){
        //2020-09-24 接入云米，增加经纬度检查
        AjaxJsonEntity locationCheckResult = checkAddressLocation(detail.getDataSource(),detail.getOrderId(),detail.getQuarter());
        if(!locationCheckResult.getSuccess()){
            return locationCheckResult;
        }
        if (detail == null){
            return AjaxJsonEntity.fail("提交无数据",null);
        }
        if (detail.getServiceCategory() == null){
            return AjaxJsonEntity.fail("请选择服务类型",null);
        }
        int orderServiceType = StringUtils.toInteger(detail.getServiceCategory().getValue());
        if (orderServiceType == 0){
            return AjaxJsonEntity.fail("请选择服务类型",null);
        }
        long serviceTypeId = ofNullable(detail.getServiceType()).map(t->t.getId()).orElse(0L);
        if(serviceTypeId <= 0){
            return AjaxJsonEntity.fail("请选择服务项目",null);
        }
        ServiceType serviceType = serviceTypeService.getFromCache(serviceTypeId);
        //需要输入故障类型，现象及处理
        if(serviceType.getRelateErrorTypeFlag() != null && serviceType.getRelateErrorTypeFlag() == 1) {
            //检查非安装(1)类型
            if (orderServiceType > 1 && detail.getHasErrorType() == 1) {
                if (detail.getErrorType() == null || detail.getErrorType().getId() == null || detail.getErrorType().getId() == 0) {
                    return AjaxJsonEntity.fail("请选择故障分类", null);
                }
                if (detail.getErrorCode() == null || detail.getErrorCode().getId() == null || detail.getErrorCode().getId() == 0) {
                    return AjaxJsonEntity.fail("请选择故障现象", null);
                }
                if (detail.getActionCode() == null || org.apache.commons.lang3.StringUtils.isBlank(detail.getActionCode().getName())) {
                    return AjaxJsonEntity.fail("请选择或输入故障处理内容", null);
                }
            }
        }
        return AjaxJsonEntity.success("",null);
    }

    /**
     * 检查订单用户地址经纬度坐标
     * @param dataSource    数据源
     * @param orderId       订单ID
     * @param quarter       分片
     * @return
     */
    public AjaxJsonEntity checkAddressLocation(int dataSource,Long orderId,String quarter){
        if(B2BDataSourceEnum.VIOMI.id != dataSource){
            return AjaxJsonEntity.success("",null);
        }
        OrderLocation location = orderLocationService.getByOrderId(orderId,quarter);
        if(location == null || location.getLatitude()<=0 || location.getLongitude()<=0){
            return AjaxJsonEntity.fail("用户地址定位经纬度坐标缺失", null);
        }
        return AjaxJsonEntity.success("",null);
    }

    /**
     * 读取网点具体安维师傅的派单记录
     *
     * @param orderId        订单id
     * @param quarter        分片
     * @param servicePointId 网点id
     * @param engineerId     安维id
     * @return
     */
    public OrderPlan getOrderPlan(Long orderId, String quarter, Long servicePointId, Long engineerId) {
        return dao.getOrderPlan(orderId, quarter, servicePointId, engineerId);
    }

    public List<OrderPlan> getOrderPlanList(Long orderId, String quarter, Integer isMaster) {
        return dao.getOrderPlanList(orderId, quarter, isMaster);
    }

    /**
     * 获取派过单的网点列表
     * 2019-4-24
     */
    public List<ServicePoint> getPlannedServicePoints(Long orderId, String quarter) {
        List<ServicePoint> points = Lists.newArrayList();
        List<OrderPlan> orderPlans = dao.getOrderPlanList(orderId, quarter, 1);
        if (orderPlans != null && !orderPlans.isEmpty()) {
            Set<Long> ids = orderPlans.stream().filter(i->i.getServicePoint() !=null && i.getServicePoint().getId() != null)
                    .map(i -> i.getServicePoint().getId()).distinct().collect(Collectors.toSet());
            ServicePoint servicePoint;
            for (Long id : ids) {
                servicePoint = servicePointService.getFromCache(id);
                if (servicePoint != null) {
                    points.add(servicePoint);
                }
            }
        }

        return points;
    }

    /**
     * 获取对帐订单
     *
     * @param page
     * @param entity
     * @return
     */
    public Page<Order> findOrderForCharge(Page<OrderSearchModel> page, OrderSearchModel entity) {
        entity.setPage(page);
        List<Map<String, Object>> list;
        if (entity.getOrderNoSearchType() == 1) {
            list = dao.findIdListForChargeByOrderNo(entity);
        }
        else  {
            list = dao.findIdListForCharge(entity);
        }
        Page<Order> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (list != null && list.size() > 0) {
            List<Order> orders = Lists.newArrayListWithExpectedSize(list.size());
            Long id;
            Integer status;
            Integer chargeFlag;
            String quarter = new String("");
            boolean cacheFirst = true;
            for (int i = 0, len = list.size(); i < len; i++) {
                id = (Long) list.get(i).get("order_id");
                status = (Integer) list.get(i).get("status");
                chargeFlag = (Integer) list.get(i).get("charge_flag");
                quarter = (String) list.get(i).get("quarter");
                cacheFirst = chargeFlag == 0 ? true : false;
                if (!cacheFirst) {
                    cacheFirst = status < Order.ORDER_STATUS_COMPLETED ? true : false;
                }
                Order order = getOrderById(id, quarter, entity.getOrderDataLevel(), cacheFirst);
                if (order != null) {
                    if (order.getOrderCondition() != null && order.getOrderCondition().getCustomer() != null &&
                            order.getOrderCondition().getCustomer().getId() != null) {
                        Long customerId = order.getOrderCondition().getCustomer().getId();
                        Customer customer = customerService.getFromCache(customerId);
//                        Customer customer = (Customer) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_CUSTOMER_ALL,customerId,customerId, Customer.class);
                        if (customer != null) {
                            order.getOrderCondition().setCustomer(customer);
                        }
                    }
                    orders.add(order);
                }
            }
            rtnPage.setList(orders);
        }
        return rtnPage;
    }

    /**
     * 获取对帐订单
     *
     * @param page
     * @param entity
     * @return
     */
    public Page<OrderChargeViewModel> findOrderForChargeNew(Page<OrderSearchModel> page, OrderSearchModel entity) {
        entity.setPage(page);
        List<OrderChargeViewModel> list = dao.findConditionFeeForCharge(entity);
        Page<OrderChargeViewModel> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (list != null && list.size() > 0) {
            List<OrderChargeViewModel> orders = Lists.newArrayListWithExpectedSize(list.size());
            List<OrderDetailChargeViewModel> allDetails = Lists.newArrayList();

            List<Long> customerIds = list.stream().map(o -> o.getCustomerId()).distinct().collect(Collectors.toList());
            Map<Long, String> customerNamesMap = MDUtils.getCustomerNamesByIds(customerIds);
            List<Long> kefuIds = list.stream().map(o -> o.getKefuId()).distinct().collect(Collectors.toList());
            Map<Long, String> kefuNamesMap = MSUserUtils.getNamesByUserIds(kefuIds);

            for (OrderChargeViewModel order : list) {
                List<OrderDetailChargeViewModel> details = dao.findDetailForCharge(order.getOrderId(), order.getQuarter());
                // add on 2019-10-28 begin
                if (details != null && !details.isEmpty()) {
                    List<Long> engineerIds = details.stream().map(r->r.getEngineerId()).distinct().collect(Collectors.toList());
                    Map<Long, String> engineerMap = MDUtils.getEngineerNamesByIds(engineerIds);
                    // add on 2020-1-6 begin
                    Map<Long,String> serviceTypeMap = serviceTypeService.findAllIdsAndNames();
                    details.stream().forEach(r->{
                        r.setEngineerName(engineerMap.get(r.getEngineerId()));
                        r.setServiceTypeName(serviceTypeMap.get(r.getServiceTypeId()));
                    });
                    // add on 2020-1-6 end
                }
                // add on 2019-10-28 end
                order.setDetails(details);
                allDetails.addAll(details);

                if (!customerNamesMap.isEmpty() && order.getCustomerId() != null && order.getCustomerId() > 0) {
                    String customerName = customerNamesMap.get(order.getCustomerId());
                    if (customerName != null) {
                        order.setCustomerName(customerName);
                    }
                }
                if (!kefuNamesMap.isEmpty() && order.getKefuId() != null && order.getKefuId() > 0) {
                    String kefuName = kefuNamesMap.get(order.getKefuId());
                    if (kefuName != null) {
                        order.setKefuName(kefuName);
                    }
                }
                orders.add(order);
            }

            Map<Long, String> productsMap = MDUtils.getAllProductNames();
            List<Long> servicePointIds = allDetails.stream().map(d -> d.getServicePointId()).distinct().collect(Collectors.toList());
//            Map<Long, ServicePoint> servicePointsMap = MDUtils.getServicePointsByIds(servicePointIds);  // mark on 2019-10-14

            Map<Long, MDServicePointViewModel> servicePointsMap = MDUtils.getServicePointsByIds(servicePointIds);
            for ( OrderDetailChargeViewModel detail : allDetails) {
                if (!servicePointsMap.isEmpty() && detail.getServicePointId() != null && detail.getServicePointId() > 0) {
                    /*
                    // mark on 2019-10-14
                    ServicePoint sp = servicePointsMap.get(detail.getServicePointId());
                    if (sp != null) {
                        detail.setServicePointNo(sp.getServicePointNo());
                        detail.setServicePointName(sp.getName());
                    }
                    */
                    // add on 2019-10-14 begin
                    MDServicePointViewModel sp = servicePointsMap.get(detail.getServicePointId());
                    if (sp != null) {
                        detail.setServicePointNo(sp.getServicePointNo());
                        detail.setServicePointName(sp.getName());
                    }
                    // add on 2019-10-14 end
                }
                if (!productsMap.isEmpty() && detail.getProductId() != null && detail.getProductId() > 0) {
                    String productName = productsMap.get(detail.getProductId());
                    if (productName != null) {
                        detail.setProductName(productName);
                    }
                }
            }

            rtnPage.setList(orders);
        }
        return rtnPage;
    }

    //endregion 订单读取

    //region 订单处理

    /**
     * 按客户+品类+区域随机返回负责该区域的客服信息
     *
     * 1.自动客服
     * 1.1.先查找符合的自动客服(sub_flag=4)，无->1.2
     * 1.2.查找符合的超级客服(sub_flag=0),无，返回null
     *
     * 2.vip客户
     * 2.1.先查找符合的vip客服(sub_flag=1)，无,返回null
     *
     * 3.突击区域
     * 3.1.先查找符合的突击客服(sub_flag=3),无,返回null
     *
     * 4.大客服
     * 4.1.先查找符合的大客服(sub_flag=2)，无,返回null
     */
    public User getRandomKefu(Long customerId, Long areaId,Long productCategoryId,int kefuType,Long cityId,Long provinceId) {
        Customer customer = customerService.getFromCache(customerId);
        if (customer == null){
            log.error("确认客户是否是vip客户错误：读取客户错误");
            return null;
        }

        List<User> list;
        //自动客服
        if(kefuType==OrderKefuTypeRuleEnum.ORDER_AUTO.getCode()){
            list = dao.getKefuListNew(areaId,productCategoryId,KefuTypeEnum.AutomaticKefu.getCode(),cityId,provinceId);
            if (!CollectionUtils.isEmpty(list)) {
                if(list.size() ==1){
                    return list.get(0);
                }
                int rnd = RandomUtils.nextInt(0,list.size());
                return list.get(rnd);
            }
            return null;
        }
        //突击区域订单
        if(kefuType == OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){//可突击，获取突击客服
            //list = dao.getKefuList(areaId,productCategoryId,KefuTypeEnum.Rush.getCode());
            list = dao.getKefuListNew(areaId,productCategoryId,KefuTypeEnum.Rush.getCode(),cityId,provinceId);
            if (!CollectionUtils.isEmpty(list)) {
                if(list.size() ==1){
                    return list.get(0);
                }
                int rnd = RandomUtils.nextInt(0,list.size());
                return list.get(rnd);
            }
            return null;
        }
        //3.其他订单
        //list = dao.getKefuList(areaId,productCategoryId,KefuTypeEnum.Kefu.getCode());
        if(customer.getVipFlag()==1){ //先找KA客服
            list = dao.getKefuListOfVipCustomerNew(customerId,areaId,productCategoryId,cityId,provinceId);
            if (!CollectionUtils.isEmpty(list)) {
                if(list.size() ==1){
                    return list.get(0);
                }
                int rnd = RandomUtils.nextInt(0,list.size());
                return list.get(rnd);
            }
        }else{
            list = dao.getKefuListNew(areaId,productCategoryId,KefuTypeEnum.Kefu.getCode(),cityId,provinceId);
            if (!CollectionUtils.isEmpty(list)) {
                if(list.size() ==1){
                    return list.get(0);
                }
                int rnd = RandomUtils.nextInt(0,list.size());
                return list.get(rnd);
            }
        }
        list = dao.getKefuListNew(areaId,productCategoryId,KefuTypeEnum.COMMON_KEFU.getCode(),cityId,provinceId);
        if (!CollectionUtils.isEmpty(list)) {
            if(list.size() ==1){
                return list.get(0);
            }
            int rnd = RandomUtils.nextInt(0,list.size());
            return list.get(rnd);
        }
        //找超级客服
        /*list = dao.getKefuListNew(areaId,productCategoryId,KefuTypeEnum.SuperKefu.getCode(),cityId,provinceId);
        if (!CollectionUtils.isEmpty(list)) {
            if(list.size() ==1){
                return list.get(0);
            }
            int rnd = RandomUtils.nextInt(0,list.size());
            return list.get(rnd);
        }*/
        return null;
    }

    /**
     * 客服接单
     *
     * @param orderId 订单id
     * @param comment 说明
     */
    @Transactional(readOnly = false)
    public void acceptOrder(Long orderId, String quarter, User user, String comment) {
        //锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//1分钟
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null) {
                throw new OrderException("读取订单错误，请重试。");
            }
            if (!order.canAccept()) {
                throw new OrderException("此订单不能接单，请确认订单状态。");
            }

            // 检查是否有订单明细
            if (order.getItems() == null || order.getItems().size() == 0) {
                throw new OrderException("该订单不能接单，订单无服务内容。");
            }

            //回收单的客服不变，否则报表统计不准确，同一单出现累加多次情况
            //因此，增加是否已分配客户判断
            Boolean hasKefu = true;

            OrderCondition condition = order.getOrderCondition();
            User kefu = order.getOrderCondition().getKefu();
            if (kefu != null && kefu.getId() != 0) {
                hasKefu = true;
            } else {
                // 接单时设置客服
                if (user.isKefu()) {
                    condition.setKefu(user);
                } else {
                    Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(condition.getArea().getId());
                    Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
                    Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
                    kefu = getRandomKefu(condition.getCustomer().getId(), condition.getArea().getId(),condition.getProductCategoryId(),condition.getKefuType(),city.getId(),province.getId());
                    if (kefu != null) {
                        condition.setKefu(kefu);
                    } else {
                        //无客服
                        throw new OrderException("此区域暂未分配跟进客服，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
                    }
                }
                hasKefu = false;
            }
            Date date = new Date();
            //status
            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_ACCEPTED), "order_status", "已接单");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_ACCEPTED, label);
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("quarter", order.getQuarter());
            params.put("acceptDate", date);
            params.put("orderId", orderId);
            dao.updateStatus(params);

            //condition
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", status);
            if (!hasKefu) {
                params.put("kefu", condition.getKefu());
            }
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("客服接单");
            processLog.setOrderId(orderId);
            if (StringUtils.isNotBlank(comment)) {
                processLog.setActionComment(String.format("客服接单:%s,操作人:%s,备注:%s", order.getOrderNo(), user.getName(), comment));
            } else {
                processLog.setActionComment(String.format("客服接单:%s,操作人:%s", order.getOrderNo(), user.getName()));
            }
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_ACCEPTED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //调用公共缓存
            OrderStatus orderStatus = order.getOrderStatus();
            orderStatus.setAcceptDate(date);
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setCondition(condition)
                    .setOrderStatus(orderStatus)
                    .setStatus(status)
                    .incrVersion(1L)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 自动派单
     */
    @Transactional(readOnly = false)
    public void autoPlanOrder(Order order) {
        if (order == null || order.getId() == null) {
            throw new OrderException("派单失败：参数无值。");
        }
        User user = order.getCreateBy();
        try {

            OrderCondition condition = order.getOrderCondition();
            ServicePoint servicePoint = condition.getServicePoint();
            Engineer engineer = servicePoint.getPrimary();
            if (engineer.getServicePoint() == null) {
                engineer.setServicePoint(servicePoint);
            }
            //fee
            OrderFee fee = order.getOrderFee();
            Long prevServicePointId = null;
            String key = String.format(RedisConstant.SD_ORDER, order.getId());

            Integer prevStatus = order.getOrderCondition().getStatusValue();
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();

            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_PLANNED), "order_status", "已派单");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_PLANNED, label);
            //待接单，或已接单 更改状态
            int statusValue = condition.getStatusValue();
            if (statusValue == Order.ORDER_STATUS_APPROVED.intValue()
                    || statusValue == Order.ORDER_STATUS_ACCEPTED.intValue()) {
                condition.setStatus(status);
            }
            int pendingType = -1;
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("operationAppFlag", 0);
            params.put("servicePoint", servicePoint);
            params.put("engineer", servicePoint.getPrimary());
            params.put("status", condition.getStatus());
            params.put("subStatus", Order.ORDER_SUBSTATUS_PLANNED);
            params.put("pendingType", new Dict(0, ""));
            boolean resetAppointmentDate = true;
            params.put("resetAppointmentDate", resetAppointmentDate);
            params.put("reservationDate", date);
            params.put("pendingTypeDate", date);
            // 突击单关闭
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                params.put("rushOrderFlag", 2);
            }
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            //fee
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("engineerPaymentType", fee.getEngineerPaymentType());//安维付款方式
            //params.put("planTravelCharge", fee.getPlanTravelCharge());
            //params.put("planTravelNo", fee.getPlanTravelNo());
            params.put("planDistance", fee.getPlanDistance());
            //params.put("planOtherCharge", fee.getPlanOtherCharge());
            //params.put("customerPlanTravelCharge", fee.getCustomerPlanTravelCharge());
            //params.put("customerPlanOtherCharge", fee.getCustomerPlanOtherCharge());//2019/03/16
            dao.updateFee(params);

            //Status
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("planBy", user);
            params.put("planDate", date);
            params.put("planComment", "自动派单");
            //是否是接单+派单
            if (prevStatus == 20) {
                params.put("acceptDate", date);
            }
            dao.updateStatus(params);

            //Location,更新派单的网点与用户之间的上门距离
            params.clear();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("distance", fee.getPlanDistance());//单位公里
            orderLocationService.updateByMap(params);

            // log
            //接单
            if (prevStatus == 20) {
                OrderProcessLog logAccept = new OrderProcessLog();
                logAccept.setQuarter(order.getQuarter());
                logAccept.setAction("系统自动接单");
                logAccept.setOrderId(order.getId());
                logAccept.setActionComment(String.format("系统自动接单:%s,操作人:%s", order.getOrderNo(), user.getName()));
                logAccept.setStatus(status.getLabel());
                logAccept.setStatusValue(Order.ORDER_STATUS_ACCEPTED);
                logAccept.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                logAccept.setCloseFlag(0);
                logAccept.setCreateBy(user);
                logAccept.setCreateDate(date);
                saveOrderProcessLogNew(logAccept);
            }

            //派单
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("自动派单");
            processLog.setOrderId(order.getId());
            StringBuffer comment = new StringBuffer();
            comment.append(String.format("自动派单给网点:%s,操作人:%s,备注:%s", condition.getServicePoint().getName(), user.getName(), order.getRemarks()));
            processLog.setActionComment(StringUtils.left(comment.toString(), 250));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            saveOrderProcessLogNew(processLog);
            //更新接单数(网点,安维)
            params.clear();
            params.put("id", condition.getServicePoint().getId());
            params.put("planCount", 1);//派单数+1
            params.put("updateBy", user);
            params.put("updateDate", date);
            HashMap<String, Object> paramsForServicePoint = Maps.newHashMap();  //add on 2020-1-16
            paramsForServicePoint.put("id", condition.getServicePoint().getId()); //add on 2020-1-16
            paramsForServicePoint.put("planCount", 1);                            //add on 2020-1-16
            paramsForServicePoint.put("unfinishedOrderCount", 1);

            //安维
            params.remove("id");
            params.put("id", servicePoint.getPrimary().getId());//主帐号
            HashMap<String, Object> paramsForEngineer = Maps.newHashMap();  //add on 2020-1-16
            paramsForEngineer.put("planCount", 1);                           //add on 2020-1-16
            paramsForEngineer.put("id", servicePoint.getPrimary().getId());  //add on 2020-1-16

            //cache
            servicePoint.setPlanCount(servicePoint.getPlanCount() + 1);

            engineer.setPlanCount(engineer.getPlanCount() + 1);
            if (engineer.getServicePoint() == null) {
                engineer.setServicePoint(servicePoint);
            }

            condition.setServicePoint(servicePoint);//网点

            User engineerUser = new User();
            engineerUser.setId(engineer.getId());
            engineerUser.setName(engineer.getName());
            engineerUser.setMobile(engineer.getContactInfo());//2017/09/21
            condition.setEngineer(engineerUser);//安维

            condition.setUpdateBy(user);
            condition.setUpdateDate(date);

            OrderFee redisFee = order.getOrderFee();
            redisFee.setEngineerPaymentType(fee.getEngineerPaymentType());
            redisFee.setPlanTravelCharge(fee.getPlanTravelCharge());
            redisFee.setPlanTravelNo(fee.getPlanTravelNo());
            redisFee.setCustomerPlanTravelCharge(fee.getCustomerPlanTravelCharge());
            redisFee.setCustomerPlanOtherCharge(fee.getCustomerPlanOtherCharge());//2019/03/16

            //派单记录表&保险 2018/01/23
            Integer nextPlanTimes = dao.getOrderPlanMaxTimes(order.getId(), order.getQuarter());
            if (nextPlanTimes == null) {
                nextPlanTimes = 1;
            } else {
                //同网点派单不加1
                nextPlanTimes++;//+1
            }
            OrderPlan orderPlan = dao.getOrderPlan(order.getId(), order.getQuarter(), servicePoint.getId(), engineer.getId());
            if (orderPlan == null || orderPlan.getId() == null) {
                String insuranceNo = new String("");
                Double insuranceAmount = 0.0;
                if (ServicePointUtils.servicePointInsuranceEnabled(servicePoint)) {
                    //保险费
                    List<Long> categorids = order.getItems().stream().filter(t -> t.getDelFlag() == 0).map(t -> t.getProduct().getCategory().getId()).distinct().collect(Collectors.toList());
                    insuranceAmount = getOrderInsuranceAmount(categorids);
                    if (insuranceAmount == null) {
                        insuranceAmount = 0.00;
                    }
                    //保险费大于0，才生成保单
                    if (insuranceAmount > 0) {
                        //保险单号
                        insuranceNo = SeqUtils.NextSequenceNo("orderInsuranceNo");
                        if (StringUtils.isBlank(insuranceNo)) {
                            insuranceNo = SeqUtils.NextSequenceNo("orderInsuranceNo");
                            if (StringUtils.isBlank(insuranceNo)) {
                                throw new OrderException("生成工单保险单号错误");
                            }
                        }
                    }
                }
                orderPlan = new OrderPlan();
                orderPlan.setQuarter(order.getQuarter());
                orderPlan.setOrderId(order.getId());
                orderPlan.setServicePoint(servicePoint);
                orderPlan.setEngineer(engineer);
                orderPlan.setIsMaster(1);//*
                orderPlan.setPlanTimes(nextPlanTimes);//*
                orderPlan.setCreateBy(user);
                orderPlan.setCreateDate(date);
                orderPlan.setUpdateBy(new User(0l));
                orderPlan.setEstimatedOtherCost(order.getOrderFee().getPlanOtherCharge());//其它费用
                orderPlan.setEstimatedDistance(order.getOrderFee().getPlanDistance());//距离
                orderPlan.setEstimatedTravelCost(order.getOrderFee().getPlanTravelCharge());//远程费
                Double amount = calcServicePointCost(condition,servicePoint, order.getItems());//服务费
                orderPlan.setEstimatedServiceCost(amount);
                dao.insertOrderPlan(orderPlan);

                if (ServicePointUtils.servicePointInsuranceEnabled(servicePoint)  && insuranceAmount > 0) {
                    //保险单
                    OrderInsurance orderInsurance = new OrderInsurance();
                    orderInsurance.setAmount(insuranceAmount);
                    orderInsurance.setInsuranceNo(insuranceNo);
                    orderInsurance.setOrderId(order.getId());
                    orderInsurance.setOrderNo(order.getOrderNo());
                    orderInsurance.setQuarter(order.getQuarter());
                    orderInsurance.setServicePointId(condition.getServicePoint().getId());
                    orderInsurance.setAssured(engineer.getName());//主帐号
                    orderInsurance.setPhone(engineer.getContactInfo());
                    orderInsurance.setAddress(engineer.getAddress());
                    orderInsurance.setInsureDate(date);
                    orderInsurance.setInsuranceDuration(12);//投保期限12个月
                    orderInsurance.setCreateBy(user);
                    orderInsurance.setCreateDate(date);
                    orderInsurance.setDelFlag(LongIDDataEntity.DEL_FLAG_DELETE);
                    dao.insertOrderInsurance(orderInsurance);
                    //网点费用,默认无效
                    OrderServicePointFee servicePointFee = mapper.map(orderInsurance, OrderServicePointFee.class);
                    servicePointFee.setServicePoint(condition.getServicePoint());
                    dao.insertOrderServicePointFee(servicePointFee);
                    //不更新OrderFee.insuranceCharge
                } else {
                    //Service Point Fee
                    OrderServicePointFee servicePointFee = new OrderServicePointFee();
                    servicePointFee.setServicePoint(condition.getServicePoint());
                    servicePointFee.setOrderId(order.getId());
                    servicePointFee.setQuarter(order.getQuarter());
                    dao.insertOrderServicePointFee(servicePointFee);
                }
            } else {
                HashMap<String, Object> planMaps = Maps.newHashMap();
                planMaps.put("id", orderPlan.getId());
                planMaps.put("planTimes", nextPlanTimes);
                planMaps.put("estimatedDistance", order.getOrderFee().getPlanDistance());//距离
                //planMaps.put("estimatedTravelCost", order.getOrderFee().getPlanTravelCharge());//远程费
                //planMaps.put("estimatedOtherCost", order.getOrderFee().getPlanOtherCharge());//其它费用
                planMaps.put("updateBy", user);
                planMaps.put("updateDate", date);
                dao.UpdateOrderPlan(planMaps);
            }

            //关闭突击单
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                crushService.closeOrderCurshByOrderId(order.getId(),order.getQuarter(),1,null,user,date);
            }
            condition.setRushOrderFlag(2);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS)
                    .setDeleteField(OrderCacheField.CONDITION);
            OrderCacheUtils.update(builder.build());

            //派单时通知B2B
            b2BCenterOrderService.planOrder(order, engineer, user, date);

            //add on 2020-1-16 begin 集中调用MD微服务
            servicePointService.updateServicePointByMap(paramsForServicePoint);
            msEngineerService.updateEngineerByMap(paramsForEngineer);
            //add on 2020-1-16 end

            //region 消息队列
            User engieerAccount = null;
            //region 发送用户短信
            //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
            //派单发给用户的短信调整：师傅使用app和不使用app的短信格式统一
            List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
            if (!ignoreDataSources.contains(order.getDataSource().getValue()) && order.getSendUserMessageFlag() != null && order.getSendUserMessageFlag() == 1) {
                // 派单后给用户发送短信
                //检查客户短信发送开关，1:才发送
                Customer customer = null;
                try {
                    customer = customerService.getFromCache(condition.getCustomer().getId());
                } catch (Exception e) {
                    LogUtils.saveLog("客服派单:检查客户短信开关异常", "OrderService.planOrder", order.getId().toString(), e, user);
                }
                //发送短信 1.未取到客户信息 2.取到，且短信发送标记为：1 2018/04/12
                if ((customer == null || (customer != null && customer.getShortMessageFlag() == 1)) && servicePoint.getPlanContactFlag() == 0) {
                    // 网点联系人 为网点负责人(0)时此处发送短信;师傅(1)在网点派单或App派单时再发短信 2020-11-19
                    StringBuffer userContent = new StringBuffer(250);
                    try {
                        //用户电话号码
                        String mobile = StringUtils.isBlank(condition.getServicePhone()) ? condition.getPhone1() : condition.getServicePhone();
                        if (StringUtils.isBlank(mobile)) {
                            mobile = condition.getPhone2();
                        }
                        //有电话号码，才发送短信
                        if (StringUtils.isNotBlank(mobile)) {
                            userContent.append("您的");
                            OrderItem item;
                            for (int i = 0, size = order.getItems().size(); i < size; i++) {
                                item = order.getItems().get(i);
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
                            if (condition.getKefu() != null) {
                                userContent
                                        .append("客服")
                                        .append(condition.getKefu().getName().substring(0, 1)).append("小姐")
                                        .append(condition.getKefu().getPhone())
                                        .append("/");
                            }
                            userContent.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
                            // 使用新的短信发送方法 2019/02/28
                            smsMQSender.sendNew(mobile,
                                    userContent.toString(),
                                    "",
                                    user.getId(),
                                    date.getTime(),
                                    SysSMSTypeEnum.ORDER_PLANNED
                            );
                        }
                    } catch (Exception e) {
                        log.error(MessageFormat.format("[OrderService.autoPlanOrder]sms- mobile:{0},content:{1},triggerBy:{2},triggerDate:{3}", condition.getServicePhone(), userContent.toString(), user.getId(), date.getTime()), e);
                    }
                }
            }
            //endregion

            //region APP通知
            if (engieerAccount != null) {// && engieerAccount.getAppLoged() == 1
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
                    log.error("[OrderService.autoPlanOrder]app notice - uid:".concat(engieerAccount.getId().toString())
                                    .concat(",msg:").concat(engineer.getName().substring(0, 1).concat("师傅,有新单派给您，请及时打开APP进行查看处理"))
                            , e);
                }
            }
            //endregion

            //region 网点订单表更新
            OrderStatus orderStatus = order.getOrderStatus();
            int orderChannel = ofNullable(order.getOrderChannel()).map(Dict::getIntValue).orElse(0);
            MQOrderServicePointMessage.ServicePointMessage.Builder spMsgBuilder = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                    .setOperationType(MQOrderServicePointMessage.OperationType.Create)
                    .setOrderChannel(orderChannel)
                    .setDataSource(order.getDataSourceId())
                    .setOrderId(order.getId())
                    .setQuarter(order.getQuarter())
                    .setSubStatus(Order.ORDER_SUBSTATUS_PLANNED)
                    .setOperationAt(date.getTime())
                    .setOperationBy(user.getId())
                    .setResetAppointmentDate(resetAppointmentDate==true?1:0)//重置null
                    .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                            .setOrderNo(order.getOrderNo())
                            .setOrderServiceType(condition.getOrderServiceType())
                            .setAreaId(condition.getArea().getId())
                            .setAreaName(condition.getArea().getName())
                            .setStatus(condition.getStatus().getIntValue())
                            .build())
                    .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                            .setServicePointId(servicePoint.getId())
                            .setEngineerId(servicePoint.getPrimary().getId())
                            .setPrevServicePointId(Objects.isNull(prevServicePointId)?0:prevServicePointId)
                            .setPlanOrder(0)
                            .setPlanType(OrderServicePoint.PlanType.AUTO.ordinal())//2019/04/19
                            .build())
                    .setUserInfo(MQOrderServicePointMessage.UserInfo.newBuilder()
                            .setUserName(condition.getUserName())
                            .setPhone(condition.getServicePhone())
                            .setAddress(condition.getServiceAddress())
                            .build())
                    .setPlanDate(date.getTime())
                    .setReservationDate(date.getTime())
                    .setPendingType(pendingType)
                    .setMasterFlag(1)//主账号
                    .setAbnormalyFlag(condition.getAppAbnormalyFlag())
                    .setUrgentLevelId(condition.getUrgentLevel().getId().intValue())
                    .setReminderFlag(orderStatus.getReminderStatus())
                    .setComplainFlag(orderStatus.getComplainFlag());
            servicePointOrderBusinessService.planOrder(spMsgBuilder);
            //endregion

            //endregion 消息队列

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[autoPlanOrder] orderId:{}", order.getId(), e);
            throw new OrderException(e);
        }
    }


    /**
     * 客服派单
     * 派单给安维网点（注：旧版本：派单给安维）
     */
    @Transactional(readOnly = false)
    public void planOrder(PlanActionEntity planActionEntity) {
        Order order = planActionEntity.getOrder();
        if (order == null || order.getId() == null) {
            throw new OrderException("派单失败：参数无值。");
        }
        //锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = planActionEntity.getUser();
        try {
            OrderCondition condition = order.getOrderCondition();
            ServicePoint servicePoint = condition.getServicePoint();
            //fee
            OrderFee fee = order.getOrderFee();
            Boolean servicePointOrderUpdated = false;//是否更新网点订单表(sd_order_servicepoint)
            Long prevServicePointId = null;
            String key = String.format(RedisConstant.SD_ORDER, order.getId());
            Order o = planActionEntity.getO();
            Integer prevStatus = o.getOrderCondition().getStatusValue();
            Date date = planActionEntity.getDate();
            HashMap<String, Object> params = Maps.newHashMap();
            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_PLANNED), "order_status", "已派单");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_PLANNED, label);
            OrderCondition rediscondition = o.getOrderCondition();
            //待接单，或已接单 更改状态
            int statusValue = rediscondition.getStatusValue();
            Boolean rePlan = false;//是否是：重新派单且无上门服务
            if (statusValue == Order.ORDER_STATUS_SERVICED && o.getDetailList().size() == 0) {
                rePlan = true;
                servicePointOrderUpdated = true;
                rediscondition.setStatus(status);
            }
            //APP完工[55]
            if (statusValue == Order.ORDER_STATUS_APP_COMPLETED) {
                if (o.getDetailList().size() == 0) {
                    rePlan = true;
                    servicePointOrderUpdated = true;
                    rediscondition.setStatus(status);
                } else {
                    Dict servicedStatus = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_SERVICED), Dict.DICT_TYPE_ORDER_STATUS);
                    rediscondition.setStatus(servicedStatus);
                }
            }
            if (statusValue == Order.ORDER_STATUS_APPROVED.intValue() || statusValue == Order.ORDER_STATUS_ACCEPTED.intValue()) {
                rediscondition.setStatus(status);
            }

            int pendingType = -1;
            params.put("quarter", o.getQuarter());
            params.put("orderId", order.getId());
            params.put("operationAppFlag", 0);
            params.put("servicePoint", servicePoint);
            params.put("engineer", servicePoint.getPrimary());
            params.put("status", rediscondition.getStatus());
            params.put("updateBy", user);
            params.put("updateDate", date);
            //按品类强制订单完工，因App完工后客服重新派单，造成客服完工过不了
            params.put("appCompleteType", StringUtils.EMPTY);
            params.put("appCompleteDate", null);

            if (rePlan == true) {
                params.put("gradeFlag", 0);//重新派单且无上门服务，待客评->未客评
            }
            /* 重新派单，且已上门  --> 上面已重置完工时间和完工类型，此处注释
            if (rediscondition.getStatusValue() == Order.ORDER_STATUS_SERVICED && !rediscondition.getServicePoint().getId().equals(servicePoint.getId())) {
                //再次派单
                params.put("appCompleteType", "");//完工类型
            }*/
            boolean resetAppointmentDate = false;
            boolean isPlanSameServicePoint = true; //2021-6-2是否派同一个网点
            /* 跨网点派单时：appointment_date=null、pending_type = 0、reservation_date = pending_type_date = now、sub_status = 10， Add by Zhoucy */
            if (rediscondition.getServicePoint() == null || !servicePoint.getId().equals(rediscondition.getServicePoint().getId())) {
                params.put("subStatus", Order.ORDER_SUBSTATUS_PLANNED);
                params.put("pendingType", new Dict(0, ""));
                pendingType = 0;
                resetAppointmentDate = true;
                params.put("resetAppointmentDate", resetAppointmentDate);
                params.put("reservationDate", date);
                params.put("pendingTypeDate", date);
                servicePointOrderUpdated = true;
                if(rediscondition.getServicePoint() !=null && rediscondition.getServicePoint().getId() != null && rediscondition.getServicePoint().getId() > 0) {
                    prevServicePointId = rediscondition.getServicePoint().getId();
                }
                isPlanSameServicePoint = false;
            }
            // 突击单关闭 Add by Ryan
            if (rediscondition.getRushOrderFlag() == 1 || rediscondition.getRushOrderFlag() == 3) {
                params.put("rushOrderFlag", 2);
            }
            dao.updateCondition(params);

            //fee
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("engineerPaymentType", fee.getEngineerPaymentType());//安维付款方式
            params.put("planTravelCharge", fee.getPlanTravelCharge());
            params.put("planTravelNo", fee.getPlanTravelNo());
            params.put("planDistance", fee.getPlanDistance());
            params.put("planOtherCharge", fee.getPlanOtherCharge());
            params.put("customerPlanTravelCharge", fee.getCustomerPlanTravelCharge());
            params.put("customerPlanOtherCharge", fee.getCustomerPlanOtherCharge());//2019/03/16
            dao.updateFee(params);

            //Location,更新派单的网点与用户之间的上门距离
            params.clear();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("distance", fee.getPlanDistance());//单位公里
            orderLocationService.updateByMap(params);

            //Status
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("planBy", user);
            params.put("planDate", date);
            params.put("planComment", order.getRemarks());
            //是否是接单+派单
            if (prevStatus == 20) {
                params.put("acceptDate", date);
            }
            if (rePlan == true) {
                params.put("serviceFlag", 0);//重新派单且无上门服务，待客评->未客评
            }
            dao.updateStatus(params);
            int dataSourceId = o.getDataSourceId();
            // log
            //接单
            if (prevStatus == 20) {
                OrderProcessLog logAccept = new OrderProcessLog();
                logAccept.setQuarter(o.getQuarter());
                logAccept.setAction("客服接单");
                logAccept.setOrderId(order.getId());
                logAccept.setActionComment(String.format("客服接单:%s,操作人:%s", order.getOrderNo(), user.getName()));
                logAccept.setStatus(status.getLabel());
                logAccept.setStatusValue(Order.ORDER_STATUS_ACCEPTED);
                logAccept.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                logAccept.setCloseFlag(0);
                logAccept.setCreateBy(user);
                logAccept.setCreateDate(date);
                logAccept.setCustomerId(condition.getCustomerId());
                logAccept.setDataSourceId(dataSourceId);
                saveOrderProcessLogNew(logAccept);
            }

            //派单
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(o.getQuarter());
            processLog.setAction("派单");
            processLog.setOrderId(order.getId());
            StringBuffer comment = new StringBuffer();
            //突击单派单时，生成两条日志，将派单备注独立出来写入到跟踪进度中，并且客户不可见
            if (order.getCrushPlanFlag() == null || order.getCrushPlanFlag() == 0) {
                comment.append(String.format("派单给网点:%s,操作人:%s,备注:%s", condition.getServicePoint().getName(), user.getName(), order.getRemarks()));
            }
            else {
                comment.append(String.format("派单给网点:%s,操作人:%s", condition.getServicePoint().getName(), user.getName()));
            }
            // 如有预设远程费，记录在日志中
            if (fee.getCustomerPlanTravelCharge() > 0 || fee.getPlanTravelCharge() > 0 || fee.getCustomerPlanOtherCharge() > 0) {
//                comment.append(String.format("派单给网点:%s,操作人:%s,备注:%s", condition.getServicePoint().getName(), user.getName(), order.getRemarks()));
                if (fee.getPlanDistance() > 0) {
                    comment.append(String.format(",距离:%.0f 公里", fee.getPlanDistance()));
                }
                if (fee.getCustomerPlanTravelCharge() > 0) {
                    comment.append(String.format(",预设厂商远程费:%s", fee.getCustomerPlanTravelCharge()));
                }
                if (fee.getPlanTravelCharge() > 0) {
                    comment.append(String.format(",预设网点远程费:%s", fee.getPlanTravelCharge()));
                }
                if (fee.getPlanOtherCharge() > 0) {
                    comment.append(String.format(",网点其它费用:%s", fee.getPlanOtherCharge()));
                }
                //2019/03/16
                if (fee.getCustomerPlanOtherCharge() > 0) {
                    comment.append(String.format(",厂商其它费用:%s", fee.getCustomerPlanOtherCharge()));
                }
                processLog.setActionComment(StringUtils.left(comment.toString(), 250));
            } else {
                if (fee.getPlanDistance() > 0) {
                    comment.append(String.format(",距离:%.0f 公里", fee.getPlanDistance()));
                }
                processLog.setActionComment(StringUtils.left(comment.toString(), 250));
            }
            processLog.setStatus(rediscondition.getStatus().getLabel());
            processLog.setStatusValue(rediscondition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(dataSourceId);
            saveOrderProcessLogNew(processLog);

            //突击单派单时，生成两条日志，将派单备注独立出来写入到跟踪进度中，并且客户不可见
            if (order.getCrushPlanFlag() != null && order.getCrushPlanFlag() == 1 && StringUtils.isNotBlank(order.getRemarks())){
                OrderProcessLog trackingLog = new OrderProcessLog();
                trackingLog.setQuarter(o.getQuarter());
                trackingLog.setAction("跟踪进度");
                trackingLog.setOrderId(order.getId());
                String planRemarks = String.format("突击单派单备注:%s", order.getRemarks());
                trackingLog.setActionComment(StringUtils.left(planRemarks, 250));
                trackingLog.setStatus(rediscondition.getStatus().getLabel());
                trackingLog.setStatusValue(rediscondition.getStatusValue());
                trackingLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);
                trackingLog.setCloseFlag(0);
                trackingLog.setCreateBy(user);
                trackingLog.setCreateDate(date);
                int visibilityValue = OrderUtils.calcProcessLogVisibilityFlag(processLog);
                visibilityValue = VisibilityFlagEnum.subtract(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.CUSTOMER));
                trackingLog.setVisibilityFlag(visibilityValue);
                trackingLog.setDataSourceId(dataSourceId);
                trackingLog.setCustomerId(condition.getCustomerId());
                saveOrderProcessLogWithNoCalcVisibility(trackingLog);
            }

            //更新接单数(网点,安维)
            params.clear();
            params.put("id", condition.getServicePoint().getId());
            params.put("planCount", 1);//派单数+1
            params.put("updateBy", user);
            params.put("updateDate", date);
            //servicePointDao.updateServicePointByMap(params);  //mark on 2020-1-17   web端去md_servicepoint
            //servicePointService.updateServicePointByMap(params); // add on 2019-10-4  //mark on 2020-1-16 集中MD调用微服务
            HashMap<String,Object> paramsForServicePoint = Maps.newHashMap(); // add on 2020-1-16
            boolean unfinishedOrderCountFlag = false;
            paramsForServicePoint.put("id", condition.getServicePoint().getId()); //add on 2020-1-16
            paramsForServicePoint.put("planCount", 1);//派单数+1   //add on 2020-1-16
            if(isPlanSameServicePoint && statusValue == Order.ORDER_STATUS_APP_COMPLETED){//派给当前网点并且是app完工
                paramsForServicePoint.put("unfinishedOrderCount",1);//未完工数量+1
            }else if(!isPlanSameServicePoint){//跨网点派单
                paramsForServicePoint.put("unfinishedOrderCount",1);//新网点未完工数量+1
                if(statusValue!=Order.ORDER_STATUS_APP_COMPLETED && prevServicePointId!=null && prevServicePointId>0){//原网点未完工数量-1
                    unfinishedOrderCountFlag = true;
                }
            }
            //安维
            params.remove("id");
            params.put("id", servicePoint.getPrimary().getId());//主帐号
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            //msEngineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务  //mark on 2020-1-16 集中MD调用微服务
            HashMap<String,Object> paramsForEngineer = Maps.newHashMap();
            paramsForEngineer.put("id", servicePoint.getPrimary().getId());//主帐号
            paramsForEngineer.put("planCount", 1);//派单数+1 //add on 2020-1-16

            //cache
            //service point planCount+1
            servicePoint.setPlanCount(servicePoint.getPlanCount() + 1);
            //servicePointService.updateServicePointCache(servicePoint); //mark on 2020-1-17   web端去md_servicepoint

            //engineer planCount+1
            Engineer engineer = servicePoint.getPrimary();
            engineer.setPlanCount(engineer.getPlanCount() + 1);
            if (engineer.getServicePoint() == null) {
                engineer.setServicePoint(servicePoint);
            }
            //servicePointService.updateEngineerCache(engineer); //mark on 2020-1-17   web端去md_servicepoint

            rediscondition.setServicePoint(servicePoint);//网点
            if (prevStatus == 20) {
                rediscondition.setKefu(user);
            }
            User engineerUser = new User();
            engineerUser.setId(engineer.getId());
            engineerUser.setName(engineer.getName());
            engineerUser.setMobile(engineer.getContactInfo());//2017/09/21
            rediscondition.setEngineer(engineerUser);//安维

            rediscondition.setUpdateBy(user);
            rediscondition.setUpdateDate(date);

            OrderFee redisFee = o.getOrderFee();
            redisFee.setEngineerPaymentType(fee.getEngineerPaymentType());
            redisFee.setPlanTravelCharge(fee.getPlanTravelCharge());
            redisFee.setPlanTravelNo(fee.getPlanTravelNo());
            redisFee.setCustomerPlanTravelCharge(fee.getCustomerPlanTravelCharge());
            redisFee.setCustomerPlanOtherCharge(fee.getCustomerPlanOtherCharge());//2019/03/16
            OrderPlan orderPlan = planActionEntity.getOrderPlan();
            if(orderPlan != null && orderPlan.getId() == null){
                //无派单记录
                dao.insertOrderPlan(orderPlan);
            } else {
                //有派单记录
                HashMap<String, Object> planMaps = Maps.newHashMap();
                planMaps.put("id", orderPlan.getId());
                planMaps.put("planTimes", orderPlan.getPlanTimes());
                planMaps.put("estimatedDistance", orderPlan.getEstimatedDistance());//距离
                planMaps.put("estimatedTravelCost", orderPlan.getEstimatedTravelCost());//远程费
                planMaps.put("estimatedOtherCost", orderPlan.getEstimatedOtherCost());//其它费用
                planMaps.put("estimatedServiceCost", orderPlan.getEstimatedServiceCost());//其它费用
                planMaps.put("updateBy", orderPlan.getUpdateBy());
                planMaps.put("updateDate", orderPlan.getUpdateDate());
                dao.UpdateOrderPlan(planMaps);
            }
            //保险单
            if(planActionEntity.getOrderInsurance() != null) {
                dao.insertOrderInsurance(planActionEntity.getOrderInsurance());
            }
            //网点费用
            if(planActionEntity.getServicePointFee() != null) {
                dao.insertOrderServicePointFee(planActionEntity.getServicePointFee());
            }
            //关闭突击单
            if (rediscondition.getRushOrderFlag() == 1 || rediscondition.getRushOrderFlag() == 3) {
                crushService.closeOrderCurshByOrderId(o.getId(),o.getQuarter(),1,null,user,date);
            }
            condition.setRushOrderFlag(2);
            rediscondition.setRushOrderFlag(2);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS)
                    .setDeleteField(OrderCacheField.CONDITION);
            OrderCacheUtils.update(builder.build());

            //派单时通知B2B
            b2BCenterOrderService.planOrder(o, engineer, user, date);

            //add on 2020-1-16 begin  集中处理MD微服务调用
            servicePointService.updateServicePointByMap(paramsForServicePoint);
            msEngineerService.updateEngineerByMap(paramsForEngineer);
            //add on 2020-1-16 end
            if(unfinishedOrderCountFlag){
                updateServicePointUnfinishedOrderCount(prevServicePointId,-1,"客服派单更新网点未完工数量",order.getId(),user);
            }

            //region 消息队列

            //region 发送用户短信
            if(StringUtils.isNoneBlank(planActionEntity.getUserPhone()) && StringUtils.isNoneBlank(planActionEntity.getUserMsg())) {
                smsMQSender.sendNew(planActionEntity.getUserPhone(),
                        planActionEntity.getUserMsg().toString(),
                        "",
                        user.getId(),
                        date.getTime(),
                        SysSMSTypeEnum.ORDER_PLANNED
                );
            }
            //endregion

            //region 安维人员短息
            if( StringUtils.isNoneBlank(planActionEntity.getEngineerPhone()) && StringUtils.isNoneBlank(planActionEntity.getEngineerMsg())){
                smsMQSender.sendNew(planActionEntity.getEngineerPhone(),
                        planActionEntity.getEngineerMsg().toString(),
                        "",
                        user.getId(),
                        date.getTime(),
                        SysSMSTypeEnum.ORDER_PLANNED
                );
            }
            //endregion

            //region APP通知
            if(planActionEntity.getAppPushMessage() != null){
                appMessagePushService.sendMessage(planActionEntity.getAppPushMessage());
            }
            //endregion

            //region 网点订单表更新
            if(servicePointOrderUpdated == true){
                syncServicePointPlanInfo(o,servicePoint,user,date,resetAppointmentDate,prevServicePointId,pendingType);
            }
            //endregion

            //endregion 消息队列

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[planOrder] orderId:{}", order.getId(), e);
            throw new OrderException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    private void syncServicePointPlanInfo(Order order,ServicePoint servicePoint,User user,Date date,Boolean resetAppointmentDate,Long prevServicePointId,int pendingType){
        OrderStatus orderStatus = order.getOrderStatus();
        OrderCondition condition = order.getOrderCondition();
        int orderChannel = ofNullable(order.getOrderChannel()).map(Dict::getIntValue).orElse(0);
        MQOrderServicePointMessage.ServicePointMessage.Builder spMsgBuilder = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.Create)
                .setOrderChannel(orderChannel)
                .setDataSource(order.getDataSourceId())
                .setOrderId(order.getId())
                .setQuarter(order.getQuarter())
                .setSubStatus(Order.ORDER_SUBSTATUS_PLANNED)
                .setOperationAt(date.getTime())
                .setOperationBy(user.getId())
                .setResetAppointmentDate(resetAppointmentDate==true?1:0)//重置null
                .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                        .setOrderNo(order.getOrderNo())
                        .setOrderServiceType(condition.getOrderServiceType())
                        .setAreaId(condition.getArea().getId())
                        .setAreaName(condition.getArea().getName())
                        .setStatus(condition.getStatus().getIntValue())
                        .build())
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(servicePoint.getId())
                        .setEngineerId(servicePoint.getPrimary().getId())
                        .setPrevServicePointId(Objects.isNull(prevServicePointId)?0:prevServicePointId)
                        .setPlanOrder(0)
                        .setPlanType(OrderServicePoint.PlanType.AUTO.ordinal())//2019/04/19
                        .build())
                .setUserInfo(MQOrderServicePointMessage.UserInfo.newBuilder()
                        .setUserName(condition.getUserName())
                        .setPhone(condition.getServicePhone())
                        .setAddress(condition.getServiceAddress())
                        .build())
                .setPlanDate(date.getTime())
                .setReservationDate(date.getTime())
                .setPendingType(pendingType)
                .setMasterFlag(1)//主账号
                .setAbnormalyFlag(condition.getAppAbnormalyFlag())
                .setUrgentLevelId(condition.getUrgentLevel().getId().intValue())
                .setReminderFlag(orderStatus.getReminderStatus())
                .setComplainFlag(orderStatus.getComplainFlag());
        servicePointOrderBusinessService.planOrder(spMsgBuilder);
    }
    /**
     * 获得下个派单次序
     */
    public Integer getOrderPlanMaxTimes(long orderId, String quarter){
        return dao.getOrderPlanMaxTimes(orderId, quarter);
    }

    /**
     * 修改实际上门联系信息
     * 只能修改具体上门地址，不能修改省市区县
     * @param order
     */
    @Transactional(readOnly = false)
    public void updateServiceInfo(Order order) {
        if (order == null || order.getId() == null) {
            throw new OrderException("参数无值。");
        }
        //检查锁
        String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 30);//30秒
        if (!locked) {
            throw new OrderException("该单正在被其他人处理中，请稍候重试，或刷新页面。");
        }
        try {
            User user = UserUtils.getUser();
            Order o = getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (o == null || o.getOrderCondition() == null) {
                throw new OrderException("读取订单失败，请重试。");
            }
            OrderCondition condition = o.getOrderCondition();
            String prevServicePhone = condition.getServicePhone();
            String prevServiceAddress = condition.getServiceAddress();
            OrderCondition ucondition = order.getOrderCondition();
            String bServiceAddress = condition.getServiceAddress();
            Area bArea = condition.getArea();
            Area bSubArea = condition.getSubArea();
            condition.setServicePhone(ucondition.getServicePhone());
            condition.setServiceAddress(ucondition.getServiceAddress());
            o.setDescription(order.getDescription());
            //获取客服类型
            OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = getKefuType(condition.getProductCategoryId(),ucondition.getCityId(),ucondition.getArea().getId(),
                                                                      ucondition.getSubArea().getId(),condition.getCustomer().getVipFlag(),condition.getCustomer().getVip());

            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            //condition
            params.put("quarter", o.getQuarter());
            params.put("orderId", order.getId());
            params.put("servicePhone", condition.getServicePhone());
            params.put("serviceAddress", condition.getServiceAddress());
            if( ucondition.getArea() != null && ucondition.getArea().getId() != null
                    && ucondition.getArea().getId() > 0
                    && !condition.getArea().getName().equalsIgnoreCase(ucondition.getArea().getName())){
                params.put("area", ucondition.getArea());
                //区县变更了
                if(!condition.getArea().getId().equals(ucondition.getArea().getId())) {
                    params.put("provinceId", ucondition.getProvinceId());
                    params.put("cityId", ucondition.getCityId());
                    //判断区县是否有跟进客服
                    User kefu =getRandomKefu(condition.getCustomer().getId(),ucondition.getArea().getId(),condition.getProductCategoryId(),orderKefuTypeRuleEnum.getCode(),ucondition.getCityId(),ucondition.getProvinceId());
                    if(kefu==null){
                       /* User userSupervisor = userKeFuService.getKefuSupervisor(condition.getCustomer().getId(),ucondition.getArea().getId(),condition.getProductCategoryId(),orderKefuTypeRuleEnum.getType(),ucondition.getCityId(),ucondition.getProvinceId());
                        String supervisorName = "";
                        if(userSupervisor!=null){
                            supervisorName = userSupervisor.getName();
                        }*/
                       Customer customer = customerService.getFromCache(condition.getCustomer().getId());
                        String tip = noFindKefuTip(user,customer,condition.getProductCategoryId(),orderKefuTypeRuleEnum.getCode(),ucondition.getArea().getId(),ucondition.getCityId(),ucondition.getProvinceId());
                        throw new OrderException(tip);
                    }
                }
                condition.setArea(ucondition.getArea());
            }
            if( ucondition.getSubArea() != null && ucondition.getSubArea().getId() != null
                    && !condition.getSubArea().getId().equals(ucondition.getSubArea().getId())){
                params.put("subArea", ucondition.getSubArea());
                condition.setSubArea(ucondition.getSubArea());//2020-12-03 修复修改用户信息中街道未同步缓存问题
            }
            //2021-02-04 街道、区县、市、省有变更，增加日志记录
            if(null != params.get("subArea") || null != params.get("area")){
                StringJoiner stringJoiner = new StringJoiner("");
                stringJoiner.add("修改前地址: ").add(bArea.getName()).add(" ").add(bServiceAddress)
                        .add(" area: ").add(String.valueOf(bArea.getId()))
                        .add(" subArea: ").add(String.valueOf(bSubArea.getId()));
                stringJoiner.add(", 修改后地址: ").add(ucondition.getArea().getName()).add(" ").add(ucondition.getServiceAddress())
                        .add(" area: ").add(String.valueOf(ucondition.getArea().getId()))
                        .add(" subArea: ").add(String.valueOf(ucondition.getSubArea().getId()));
                LogUtils.saveLog("客服修改用户地址", "OrderService.updateServiceInfo", stringJoiner.toString(), null, user);
            }
            String kefuTypeInfo = "";
            if(!condition.getKefuType().equals(orderKefuTypeRuleEnum.getCode())){//客服类型改变了
                params.put("kefuType",orderKefuTypeRuleEnum.getCode());
                int canRush = 0;
                if(orderKefuTypeRuleEnum.getCode()==OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){ //突击
                    canRush = 1;
                }
                params.put("canRush",canRush);
                condition.setCanRush(canRush);
                kefuTypeInfo = "客服：【".concat(getKefuTypeName(condition.getKefuType(),condition.getCustomer().getVipFlag()))
                              .concat("->").concat(getKefuTypeName(orderKefuTypeRuleEnum.getCode(),condition.getCustomer().getVipFlag()))+"】";
                condition.setKefuType(orderKefuTypeRuleEnum.getCode());
            }
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateUserInfo(params);
            //order
            params.clear();
            params.put("quarter", o.getQuarter());
            params.put("id", order.getId());
            params.put("description", order.getDescription());
            orderHeadDao.updateOrder(params);//2020-12-03 sd_order -> sd_order_head
            // log
            StringBuffer addressStringJoiner = new StringBuffer("【");
            addressStringJoiner.append(bArea.getName());
            addressStringJoiner.append(bServiceAddress);
            addressStringJoiner.append("->");
            addressStringJoiner.append(ucondition.getArea().getName());
            addressStringJoiner.append(ucondition.getServiceAddress());
            addressStringJoiner.append("】");
            if(StringUtils.isNotBlank(kefuTypeInfo)){
                addressStringJoiner.append("，");
                addressStringJoiner.append(kefuTypeInfo);
            }
            String actionComment = addressStringJoiner.toString();
            OrderProcessLog log = new OrderProcessLog();
            log.setQuarter(o.getQuarter());
            log.setAction("修改联系信息");
            log.setOrderId(order.getId());
            log.setActionComment(String.format("地址:%s", actionComment));
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(Integer.parseInt(condition.getStatus().getValue()));
            log.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            log.setCloseFlag(0);
            log.setCreateBy(user);
            log.setCreateDate(date);
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(o.getDataSourceId());
            saveOrderProcessLogNew(log);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .incrVersion(1L)
                    .setCondition(condition)
                    .setInfo(o)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

            //region 网点订单数据更新 2019-03-25
            if(!(prevServicePhone.equalsIgnoreCase(ucondition.getServicePhone()) && prevServiceAddress.equalsIgnoreCase(ucondition.getServiceAddress()))) {
                servicePointOrderBusinessService.updateUserInfo(order.getId(), order.getQuarter(),
                        ucondition.getServicePhone(), ucondition.getServiceAddress(),
                        user.getId(), date.getTime());
            }

            // 修改B2B工单
            o.getOrderCondition().setServicePhone(condition.getServicePhone());
            o.getOrderCondition().setServiceAddress(condition.getServiceAddress());
            b2BCenterOrderModifyService.modifyB2BOrder(o, false);
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

    /**
     * 修改实际上门联系信息
     *
     * @param order
     */
    @Transactional(readOnly = false)
    public void updateArrivalDate(Order order) {
        if (order == null || order.getId() == null) {
            throw new OrderException("参数无值。");
        }
        //检查锁
        String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 30);//30秒
        if (!locked) {
            throw new OrderException("该单正在被其他人处理中，请稍候重试，或刷新页面。");
        }
        try {

            Order o = getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (o == null || o.getOrderCondition() == null) {
                throw new OrderException("订单读取失败，请稍候重试。");
            }
            OrderCondition condition = o.getOrderCondition();
            if (condition.getArrivalDate() != null) {
                throw new OrderException("该单已设置了到货日期，不允许再次设定。");
            }
            condition.setArrivalDate(order.getCreateDate());

            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            //condition
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("arrivalDate", order.getCreateDate());
            params.put("updateBy", order.getCurrentUser());
            params.put("updateDate", new Date());
            dao.updateCondition(params);

            // log
            OrderProcessLog log = new OrderProcessLog();
            log.setQuarter(o.getQuarter());
            log.setAction("修改到货日期信息");
            log.setOrderId(order.getId());
            log.setActionComment("修改到货日期信息:" + DateUtils.formatDate(order.getCreateDate(), "yyyy-MM-dd HH:mm"));
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(Integer.parseInt(condition.getStatus().getValue()));
            log.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);
            log.setCloseFlag(0);
            log.setCreateBy(order.getCurrentUser());
            log.setCreateDate(date);
            log.setRemarks(log.getActionComment());//厂家可见
//            dao.insertProcessLog(log);
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(o.getDataSourceId());
            saveOrderProcessLogNew(log);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .incrVersion(1L)
                    .setCondition(condition)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new OrderException(e);
        } finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    @Transactional
    public void updateDetail(HashMap<String,Object> params){
        dao.updateDetail(params);
    }

    /**
     * 修改网点费用
     */
    @Transactional
    public void updateOrderServicePointFeeByMaps(HashMap<String, Object> params){
        dao.updateOrderServicePointFeeByMaps(params);
    }

    @Transactional
    public void updateFee(HashMap<String,Object> params){
        dao.updateFee(params);
    }

    /**
     * 更新安维派单记录表上门标记
     */
    @Transactional
    public Integer updateServiceFlagOfOrderPlan(Long orderId,String quarter,Long servicePointId,Long engineerId,Integer serviceFlag,Long updateBy,Date updateDate){
        return dao.updateServiceFlagOfOrderPlan(orderId,quarter,servicePointId,engineerId,serviceFlag,updateBy,updateDate);
    }

    /**
     * 订单退回
     * 订单状态退回到：已接单
     *
     * @param orderId
     * @param quarter
     * @return
     */
    @Transactional(readOnly = false)
    public void orderBackToAccept(Long orderId, String quarter) {
        if (orderId == null || orderId <= 0) {
            throw new OrderException("订单退回失败：参数无值。");
        }

        Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("该订单不能退回：读取订单信息失败。");
        }
        if (order.getDetailList() != null && order.getDetailList().size() > 0) {
            throw new OrderException("该订单不能退回：已有上门服务记录。");
        }

        //检查锁
        String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//60秒
        if (!locked) {
            throw new OrderException("该单正在被其他人处理中，请稍候重试，或刷新页面。");
        }

        User user = UserUtils.getUser();
        try {
            Date date = new Date();
            OrderCondition condition = order.getOrderCondition();
            //status
            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_ACCEPTED), "order_status", "已接单");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_ACCEPTED, label);

            HashMap<String, Object> params = Maps.newHashMap();
            //order
            params.put("quarter", order.getQuarter());
            params.put("id", orderId);
            params.put("confirmDoor", 0);
            orderHeadDao.updateOrder(params);//2020-12-03 sd_order -> sd_order_head

            //order status
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            //params.put("firstContactDate", null);
            params.put("planBy", new User(0l));
            params.put("planDate", null);
            params.put("planComment", "");
            //params.put("firstPlanDate", null);
            params.put("serviceFlag", 0);
            params.put("serviceDate", null);
            params.put("serviceComment", "");
            dao.updateStatus(params);

            //condition
            params.clear();
            params.put("orderId", orderId);
            params.put("quarter", order.getQuarter());
            params.put("status", status);
            //订单退回到接单区时将subStatus为0，Added by Zhoucy 2018-8-1
            params.put("pendingType", new Dict(0, ""));
            params.put("resetAppointmentDate", true);
            params.put("subStatus", Order.ORDER_SUBSTATUS_NEW);
            params.put("gradeFlag", 0);
            params.put("serviceTimes", 0);//上门次数
            params.put("servicePoint", new ServicePoint(0l));
            params.put("engineer", new User(0l));
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            //fee
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            //customer
            params.put("serviceCharge", 0.0d);
            params.put("materialCharge", 0.0d);
            params.put("expressCharge", 0.0d);
            params.put("travelCharge", 0.0d);
            params.put("otherCharge", 0.0d);
            params.put("orderCharge", 0.0d);

            //servicepoint
            params.put("engineerServiceCharge", 0.0d);
            params.put("engineerTravelCharge", 0.0d);
            params.put("engineerExpressCharge", 0.0d);
            params.put("engineerMaterialCharge", 0.0d);
            params.put("engineerOtherCharge", 0.0d);
            params.put("engineerTotalCharge", 0.0d);

            params.put("engineerPaymentType", new Dict(0l));
            params.put("planTravelNo", "");
            params.put("planTravelCharge", 0.0d);
            params.put("customerPlanTravelCharge", 0.0d);

            dao.updateFee(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("订单退回");
            processLog.setOrderId(orderId);
            processLog.setActionComment("订单退回到派单区");
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_ACCEPTED);//已接单
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition != null ? condition.getCustomerId() : 0);
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //region servicepoint相关数据清除
            //1.订单保险单记录表
            dao.deleteOrderInsurance(order.getQuarter(),orderId);
            //2.订单网点费用统计表
            dao.deleteOrderServicePointFee(order.getQuarter(),orderId);
            //3.网点订单表(sd_order_servicepoint) 在后面以消息队列方式处理
            //4.网点派单记录 2019/05/30
            //dao.deleteOrderPlan(orderId,order.getQuarter());
            //endregion

            //缓存淘汰
            OrderCacheUtils.delete(orderId);
            // 暂停回访任务，后续再智能回访时，调用接口：继续任务
            //String site = Global.getSiteCode();
            if (StringUtils.isNoneBlank(siteCode)) {
                Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
                if (taskResult != null && taskResult == 0) {
                    try {
                        stopVoiceOperateMessage(siteCode, order.getId(), order.getQuarter(), user.getName(), date);
                    } catch (Exception e) {
                        log.error("订单退回到派单区-暂停智能回访错误:" + order.getId(), e);
                    }
                }
            }
            //region 网点订单更新  2019-03-25
            servicePointOrderBusinessService.goBackToAccept(order.getId(),order.getQuarter(),
                    status.getIntValue(),Order.ORDER_SUBSTATUS_NEW,
                    user.getId(),date.getTime());
            //endregion
            //网点未接单数递减
            ServicePoint servicePoint = condition.getServicePoint();
            if(servicePoint!=null && servicePoint.getId()!=null && servicePoint.getId()>0){
                updateServicePointUnfinishedOrderCount(servicePoint.getId(),-1,"工单退回派单区",orderId,user);
            }

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(String.format("订单退回失败：%s", e.getMessage()), e);
        } finally {
            if (locked && lockKey != null) {
                //redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 5l);//5秒后过期
            }
        }
    }

    /**
     * 客服停滞原因
     * 2018/12/18 客服预约（除 3:预约时间，7:待跟进）也发送短信给用户
     * 2018/12/21 客服预约 1:等通知 不发送短信
     * @param order
     */
    @Transactional(readOnly = false)
    public void pendingOrder(OrderCondition order) {
        //User user = UserUtils.getUser();
        User user = order.getCreateBy();
        String time = "";
        // 时间取整点时间
        if (order.getAppointmentDate() != null) {
            if (DateUtils.getYear(order.getAppointmentDate()) > 9999) {
                throw new OrderException("日期超出范围");
            }

            if (order.getAppointmentDate().getTime() <= new Date().getTime()) {
                throw new OrderException("预约时间不能小于当前时间");
            }
            time = DateUtils.formatDate(order.getAppointmentDate(), "yyyy-MM-dd HH:00");
            try {
                Date date = DateUtils.parse(time, "yyyy-MM-dd HH:00");
                if (order.getAppointmentDate().getTime() != date.getTime()) {
                    order.setAppointmentDate(DateUtils.addHour(date, 1));
                    time = DateUtils.formatDate(order.getAppointmentDate(), "yyyy-MM-dd HH:00");
                } else {
                    order.setAppointmentDate(date);
                }
            } catch (java.text.ParseException e) {
                throw new OrderException("日期格式错误");
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        Integer pendingType = StringUtils.toInteger(order.getPendingType().getValue());
        log.info("pendingType:{} ,date:{}", pendingType, order.getAppointmentDate());
        try {
            Order o = getOrderById(order.getOrderId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (null == o || null == o.getOrderCondition()) {
                throw new OrderException("读取订单失败，请重试。");
            }
            OrderCondition condition = o.getOrderCondition();

            //若当前预约时间与前一次预约时间一样，则不需要通知B2B
            boolean isNeedSendToB2B = true;
            if (condition.getAppointmentDate() != null && condition.getAppointmentDate().getTime() == order.getAppointmentDate().getTime()) {
                isNeedSendToB2B = false;
            }

            Date date = new Date();
            StringBuilder cmmt = new StringBuilder();
            //预约日期
            Boolean appoint = false;
            cmmt.append(appoint ? "" : order.getPendingType().getLabel());
            if (StringUtils.isNotBlank(order.getRemarks())) {
                cmmt.append(appoint ? "" : "：").append(StringUtils.substring(order.getRemarks().trim(), 0, 100));
            }
            cmmt.append(appoint ? "" : "，").append("下次跟进时间：").append(time);

            //log
            int statusFlag = OrderProcessLog.OPL_SF_CHANGED_STATUS;
            OrderProcessLog log = new OrderProcessLog();
            log.setQuarter(o.getQuarter());
            log.setAction("变更停滞原因");
            log.setOrderId(order.getOrderId());
            log.setActionComment(cmmt.toString());
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(condition.getStatusValue());
//            log.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            log.setCloseFlag(0);
            log.setCreateBy(user);
            log.setCreateDate(date);
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(o.getDataSourceId());


            HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(15);
            //condition
            params.put("quarter", o.getQuarter());
            params.put("orderId", order.getOrderId());
            //预约
            Boolean isReservation = order.getPendingType().getValue().equalsIgnoreCase("3");
            //设置订单的subStatus，Add by Zhoucy
            Date appointmentDate = order.getAppointmentDate();
            Date pendingTypeDate = order.getAppointmentDate();
            //如果预约时间在22点及以后，则将客服处理时间提前2小时（因为等工单预约到期时，客服已经下班了） Added by zhoucy
            if (DateUtils.getHourOfDay(pendingTypeDate) >= 22) {
                pendingTypeDate = DateUtils.addHour(pendingTypeDate, -2);
            }
            Date reservationDate = order.getAppointmentDate();
            Integer subStatus = Order.ORDER_SUBSTATUS_NEW;
            Dict pendingTypeDict = order.getPendingType();
            if (pendingType == Order.PENDINGTYPE_FOLLOWING) {
                subStatus = null;
                pendingTypeDict = null;
                appointmentDate = null;
                reservationDate = null;
                statusFlag = OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS;
            } else if (pendingType == Order.PENDINGTYPE_APPOINTED) {
                subStatus = Order.ORDER_SUBSTATUS_APPOINTED;
                params.put("reservationTimes", 1);
            } else if (pendingType == Order.PENDINGTYPE_WAITINGPARTS) {
                subStatus = Order.ORDER_SUBSTATUS_WAITINGPARTS;
                params.put("reservationTimes", 1);
            } else {
                subStatus = Order.ORDER_SUBSTATUS_PENDING;
                params.put("reservationTimes", 1);
            }

            params.put("subStatus", subStatus);
            params.put("pendingType", pendingTypeDict);
            params.put("pendingTypeDate", pendingTypeDate);//设置停滞的时间
            params.put("appointmentDate", appointmentDate);
            params.put("reservationDate", reservationDate);
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            log.setStatusFlag(statusFlag);
            saveOrderProcessLogNew(log);

            condition.setAppointmentDate(order.getAppointmentDate());
            condition.setPendingType(order.getPendingType());
            condition.setPendingTypeDate(order.getPendingTypeDate());
            condition.setUpdateBy(user);
            condition.setUpdateDate(date);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getOrderId())
                    .incrVersion(1L)
                    .setPendingType(order.getPendingType())
                    .setPendingTypeDate(pendingTypeDate)
                    .setSyncDate(new Date().getTime())
                    .setExpireSeconds(0L);
            if (pendingType != Order.PENDINGTYPE_FOLLOWING) {
                builder.setReservationTimes(condition.getReservationTimes() + 1)
                        .setAppointmentDate(order.getAppointmentDate());
            }
            if (isReservation) {
                builder.setReservationDate(order.getAppointmentDate());
            }
            OrderCacheUtils.update(builder.build());
            //region 短信
            //预约时间(3) 除外
            //未在配置中：shortmessage.ignore-data-sources
            List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
            if (!ignoreDataSources.contains(o.getDataSource().getValue()) && pendingType != 1 && pendingType != 3 && pendingType != 7) {
                StringBuffer strContent = new StringBuffer();
                switch (pendingType) {
                    //case 1://等通知
                    //    strContent.append("尊敬的用户，您好，您的售后工单，由于暂时无法上门，请您在时间方便时，联系师傅或客服预约上门时间");
                    //    break;
                    case 2://等配件
                        strContent.append("尊敬的用户，您好，您的售后工单，由于需要等待商家寄发配件，请您在收到配件后，及时联系师傅或客服预约上门时间");
                        break;
                    case 4://等到货
//                        strContent.append("尊敬的用户，您好，您的售后工单，由于您暂时还未收到货，请您在收到货后时，及时联系师傅或客服预约上门时间");
                        //京东单等到货短信内容调整 2019-04-16
                        /* 2020-05-12 停发 等到货 短信
                        if(o.getDataSource().getIntValue() == B2BDataSourceEnum.JD.getId()) {
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
                if (strContent.length() > 0) {
                    // 使用新的短信发送方法 2019/02/28
                    smsMQSender.sendNew(condition.getServicePhone(),
                            strContent.toString(),
                            "",
                            user.getId(),
                            date.getTime(),
                            SysSMSTypeEnum.ORDER_PENDING
                    );
                }
            }

            //endregion

            Long servicePointId = condition.getServicePoint() == null ? null : condition.getServicePoint().getId();
            //region B2B消息队列
            //从派单处移到此处
            if (o.getDataSource() != null && pendingType != Order.PENDINGTYPE_FOLLOWING && isNeedSendToB2B) {
                //status -> 3
                Long engineerId = condition.getEngineer() == null ? null : condition.getEngineer().getId();
                b2BCenterOrderService.pendingOrder(o, servicePointId, engineerId, pendingType, order.getAppointmentDate(), user, date, order.getRemarks());
            }
            //endregion B2B消息队列

            //region 网点订单数据更新 2019-03-25
            if(pendingTypeDict != null && servicePointId != null && servicePointId > 0 && subStatus != null && subStatus > 0) {
                servicePointOrderBusinessService.pending(o.getId(), o.getQuarter(),
                        servicePointId, subStatus, pendingTypeDict.getIntValue(),
                        appointmentDate.getTime(), reservationDate.getTime(),0,
                        user.getId(), date.getTime());
            }
            //endregion

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.pendingOrder]orderId:{} ,time:{}", order.getOrderId(), time, e);
            throw new OrderException("保存错误,请重试", e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }


    /**
     * 停滞原因
     */
    @Transactional(readOnly = false)
    public void appPendingOrder(Order o, String remarks, boolean isNeedSendToB2b) {
        OrderCondition order = o.getOrderCondition();
        User user = order.getCreateBy();
        String time = "";
        // 时间取整点时间
        if (order.getAppointmentDate() != null) {
            if (DateUtils.getYear(order.getAppointmentDate()) > 9999) {
                throw new OrderException("日期超出范围");
            }
            time = DateUtils.formatDate(order.getAppointmentDate(), "yyyy-MM-dd HH:00:00");
            try {
                Date date = DateUtils.parse(time, "yyyy-MM-dd HH:00:00");
                order.setAppointmentDate(date);
            } catch (java.text.ParseException e) {
                throw new OrderException("日期格式错误");
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getOrderId());
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
            log.setOrderId(order.getOrderId());
            log.setActionComment(StringUtils.left(order.getRemarks(), 250));
            log.setStatus(order.getStatus().getLabel());
            log.setStatusValue(order.getStatusValue());
            log.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            log.setCloseFlag(0);
            log.setCreateBy(user);
            log.setCreateDate(date);
            log.setCustomerId(order.getCustomerId());
            log.setDataSourceId(o.getDataSourceId());
            saveOrderProcessLogNew(log);

            HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(15);
            //condition
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getOrderId());
            //预约
            Boolean isReservation = order.getPendingType().getValue().equalsIgnoreCase("3");
            //设置订单的subStatus，Add by Zhoucy
            Integer iPendingType = StringUtils.toInteger(order.getPendingType().getValue());
            Date appointmentDate = order.getAppointmentDate();
            Date pendingTypeDate = order.getAppointmentDate();
            //如果预约时间在22点及以后，则将客服处理时间提前2小时（因为等工单预约到期时，客服已经下班了） Added by zhoucy
            if (DateUtils.getHourOfDay(pendingTypeDate) >= 22) {
                pendingTypeDate = DateUtils.addHour(pendingTypeDate, -2);
            }
            Date reservationDate = order.getAppointmentDate();
            Integer subStatus = Order.ORDER_SUBSTATUS_NEW;
            Dict pendingTypeDict = order.getPendingType();
            if (iPendingType == Order.PENDINGTYPE_FOLLOWING) {
                subStatus = null;
                pendingTypeDict = null;
                appointmentDate = null;
                reservationDate = null;
            } else if (iPendingType == Order.PENDINGTYPE_APPOINTED) {
                subStatus = Order.ORDER_SUBSTATUS_APPOINTED;
                params.put("reservationTimes", 1);
            } else if (iPendingType == Order.PENDINGTYPE_WAITINGPARTS) {
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
            int appAbnormalyFlag = order.getAppAbnormalyFlag();
            if(1 == appAbnormalyFlag){
                params.put("appAbnormalyFlag", appAbnormalyFlag);
            }
            dao.updateCondition(params);
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getOrderId())
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.PENDING_TYPE)
                    .setDeleteField(OrderCacheField.PENDING_TYPE_DATE);
            if (iPendingType != Order.PENDINGTYPE_FOLLOWING) {
                builder.setDeleteField(OrderCacheField.RESERVATION_TIMES)
                        .setDeleteField(OrderCacheField.APPOINTMENT_DATE);
                if (isReservation) {
                    builder.setDeleteField(OrderCacheField.RESERVATION_DATE);
                }
            }
            OrderCacheUtils.update(builder.build());

            Long servicePointId = order.getServicePoint() == null ? null : order.getServicePoint().getId();
            //region B2B消息队列
            //从派单处移到此处
            if (o.getDataSource() != null && iPendingType != Order.PENDINGTYPE_FOLLOWING && isNeedSendToB2b) {
                Date effectiveDate = order.getAppointmentDate();
                //APP的预约时间若在22点及以后，则发给B2B的预约时间增加18个小时
                //status -> 3
                Long engineerId = order.getEngineer() == null ? null : order.getEngineer().getId();
                b2BCenterOrderService.pendingOrder(o, servicePointId, engineerId, iPendingType, effectiveDate, user, date, remarks);
            }
            //endregion B2B消息队列

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.pending(order.getOrderId(),order.getQuarter(),
                    servicePointId,subStatus,pendingTypeDict.getIntValue(),
                    appointmentDate.getTime(),reservationDate.getTime(),appAbnormalyFlag,
                    user.getId(),date.getTime());
            //endregion

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.appPendingOrder]orderId:{} ,pendintType:{} ,date:{} ,remarks:{}", order.getOrderId(), order.getPendingType().getLabel(), time, order.getRemarks(), e);
            throw new OrderException("保存错误", e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 删除上门服务明细
     * 1.删除数据
     * 2.更新订单的相关数据：
     * 3.解除配件关联，不删除配件
     *
     * @param detail
     */
    @Transactional(readOnly = false)
    public void deleteDetail(OrderDetail detail) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, detail.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = detail.getCreateBy();
        try {
            //上门服务项目时候删除
            Order order = getOrderById(detail.getOrderId(), detail.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单信息失败");
            }

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
            Long engiId = model.getEngineer().getId();//2019/09/03
            OrderServicePointFee orderServicePointFee = getOrderServicePointFee(order.getId(), order.getQuarter(), servicePointId);
        /*远程费,绑定到已添加的同次上门的其它服务项上
			if(details.size()>1 && detail.getEngineerTravelCharge()>0){
				OrderDetail od = details.stream()
						.filter(t->t.getDelFlag().intValue()==0
								&& t.getServiceTimes()==detail.getServiceTimes()
								&& t.getId() != detail.getId())
						.findFirst().orElse(null);
				if(od !=null){
					od.setEngineerTravelCharge(detail.getEngineerTravelCharge());
					od.setTravelCharge(detail.getTravelCharge());
					od.setTravelNo(detail.getTravelNo());
				}
			}
		*/
            model.setDelFlag(OrderDetail.DEL_FLAG_DELETE);//*,添加删除标记，计算价格时忽略该笔项目，重要
            //重新计算价格
            details.remove(model);//移除要删除的上门服务
            rechargeOrder(details, model);

            Date date = detail.getCreateDate();
            HashMap<String, Object> params = Maps.newHashMap();
            Boolean hasDetailOfSamePoint = false;
            OrderDetail otherDetail = details.stream().filter(t -> !t.getId().equals(detail.getId()) && t.getServicePoint().getId().equals(model.getServicePoint().getId())).findFirst().orElse(null);
            if (otherDetail != null) {
                hasDetailOfSamePoint = true;
            }
            //保险费汇总(负数)
            Double insuranceCharge = 0.00;
            insuranceCharge = getTotalOrderInsurance(order.getId(), order.getQuarter());
            if (insuranceCharge == null) {
                insuranceCharge = 0.00;
            }
            //该网点无其他上门服务
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
            HashMap<String, Object> feeMap = recountFee(details);
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
            //异常处理，加急费,好评费 汇总到合计
            if (detail.getAddType() == 1) {
                orderFee.setOrderCharge(orderFee.getOrderCharge() + orderFee.getCustomerUrgentCharge()+orderFee.getPraiseFee());
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
                // 2020-04-13 好评费暂不处理，因好评费只挂到最后一个网点上
                // 在异常处理完成时再处理及汇总
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
            log.setRemarks(String.format("客服删除的上门服务ID：%s", detail.getId()));
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(log);

            //cache,淘汰
            OrderCacheUtils.setDetailActionFlag(order.getId());
            OrderCacheUtils.delete(detail.getOrderId());

            //region 网点订单数据更新 2019-03-25
            // 被删除上门服务的网点无其他上门服务，才更新
            long samePointServiceCnt = details.stream().filter(t->t.getServicePoint().getId().longValue() == servicePointId.longValue() && t.getId().longValue() != detail.getId().longValue() && t.getDelFlag() == 0).count();
            if(samePointServiceCnt == 0) {
                //无同网点上门记录，更新上门服务为:0
                servicePointOrderBusinessService.delOnSiteService(detail.getId(),1,1, order.getId(), order.getQuarter(), servicePointId, condition.getServicePoint().getId(), engiId, user.getId(), date.getTime());
            }else{
                //网点的service_flag不变更
                //判断是否有同安维师傅的上门记录
                OrderDetail otherDetai2 = details.stream()
                        .filter(t -> !t.getId().equals(detail.getId())
                                && t.getServicePoint().getId().equals(model.getServicePoint().getId())
                                && t.getEngineer().getId().equals(model.getEngineer().getId())
                        ).findFirst().orElse(null);
                if (otherDetai2 == null) {
                    //无此安维师傅的上门记录，更新上门服务为:0
                    //但不更新网点的上门标记
                    servicePointOrderBusinessService.delOnSiteService(detail.getId(),0,1, order.getId(), order.getQuarter(), servicePointId, condition.getServicePoint().getId(), engiId, user.getId(), date.getTime());
                }
            }
            //endregion
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.deleteDetail]orderId:{}", detail.getOrderId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
            lockkey = null;
        }
    }

    /**
     * 根据上门服务重新统计费用和上门次数
     *
     * @param details
     * @return
     */
    public HashMap<String, Object> recountFee(List<OrderDetail> details) {
        //应收
        Double serviceCharge = 0.0;
        Double materialCharge = 0.0;
        Double expressCharge = 0.0;
        Double travelCharge = 0.0;
        Double otherCharge = 0.0;
        Double orderCharge = 0.0;//合计
        //应付
        Double engineerServiceCharge = 0.0;
        Double engineerMaterialCharge = 0.0;
        Double engineerExpressCharge = 0.0;
        Double engineerTravelCharge = 0.0;
        Double engineerOtherCharge = 0.0;
        Double engineerTotalCharge = 0.0;//合计
        Integer serviceTimes = 0;
        OrderDetail d;
//        System.out.println(">>>detials count: " + details.size());
        for (int i = 0, size = details.size(); i < size; i++) {
            d = details.get(i);
            if (d.getDelFlag() == OrderDetail.DEL_FLAG_DELETE) {
                continue;
            }
            //取得有效记录中最大的上门服务次数
            if (serviceTimes < d.getServiceTimes()) {
                serviceTimes = d.getServiceTimes();
            }
            // 应收
            serviceCharge = serviceCharge + d.getCharge();//服务费
            materialCharge = materialCharge + d.getMaterialCharge();//配件费
            expressCharge = expressCharge + d.getExpressCharge();//快递费
            travelCharge = travelCharge + d.getTravelCharge();//远程费
            otherCharge = otherCharge + d.getOtherCharge();//其它

            // 应付
            engineerServiceCharge = engineerServiceCharge + d.getEngineerServiceCharge();//服务费
            engineerMaterialCharge = engineerMaterialCharge + d.getEngineerMaterialCharge();//配件费
            engineerExpressCharge = engineerExpressCharge + d.getEngineerExpressCharge();//快递费
            engineerTravelCharge = engineerTravelCharge + d.getEngineerTravelCharge();//远程费
            engineerOtherCharge = engineerOtherCharge + d.getEngineerOtherCharge();//其它

        }
        //应收合计
        orderCharge = serviceCharge
                + materialCharge
                + expressCharge
                + travelCharge
                + otherCharge;
        //应付合计
        engineerTotalCharge = engineerServiceCharge
                + engineerMaterialCharge
                + engineerExpressCharge
                + engineerTravelCharge
                + engineerOtherCharge;

        HashMap<String, Object> shResult = Maps.newHashMap();
        shResult.put("serviceTimes", serviceTimes);
        //应收
        shResult.put("serviceCharge", serviceCharge);
        shResult.put("materialCharge", materialCharge);
        shResult.put("expressCharge", expressCharge);
        shResult.put("travelCharge", travelCharge);
        shResult.put("otherCharge", otherCharge);
        shResult.put("orderCharge", orderCharge);
        //应付
        shResult.put("engineerServiceCharge", engineerServiceCharge);
        shResult.put("engineerMaterialCharge", engineerMaterialCharge);
        shResult.put("engineerExpressCharge", engineerExpressCharge);
        shResult.put("engineerTravelCharge", engineerTravelCharge);
        shResult.put("engineerOtherCharge", engineerOtherCharge);
        shResult.put("engineerTotalCharge", engineerTotalCharge);

//        System.out.println("serviceCharge:" + charge);
//        System.out.println("engineerTotalCharge:" + engineerTotalCharge);
        return shResult;
    }

    @Transactional(readOnly = false)
    public void addDetail(OrderDetail detail) {
        addDetail(detail, true);
    }

    /**
     * 自动计入应收（客户应付）远程费及其他费用
     * 数据字典:customer:auto:count:remotecharge:category 中配置的品类，
     * 自动将网点远程费及其他费用复制到应收属性
     * @param detail
     */
    @Transactional
    public void autoCountCustomerRemoteCharge(Long productCategoryId,OrderDetail detail){
        if(detail== null){
            return;
        }
        //网点无远程费及其他费用(都是0），忽略
        if(detail.getEngineerTravelCharge() <= 0 && detail.getEngineerOtherCharge() <= 0){
            return;
        }
        // 应收远程费及其他费用，已有费用，忽略
        if(detail.getTravelCharge() > 0 && detail.getOtherCharge() > 0){
            return;
        }
        // 检查品类是否是特殊处理的品类
        //Long categoryId = Optional.ofNullable(detail.getProduct()).map(t->t.getCategory()).map(c->c.getId()).orElse(0L);
        if(productCategoryId== null || productCategoryId <= 0){
            return;
        }
        try {
            //get config from dict
            Dict dict = MSDictUtils.getDictByValue(productCategoryId.toString(), OrderUtils.SYNC_CUSTOMER_CHARGE_DICT);
            if (dict == null || !dict.getValue().equals(productCategoryId.toString())) {
                return;
            }
            int tags = detail.getSyncChargeTags();
            //应收已有费用的忽略
            if(detail.getTravelCharge() == 0 && detail.getEngineerTravelCharge() > 0){
                detail.setTravelCharge(detail.getEngineerTravelCharge());//远程费
                tags = BitUtils.addTags(tags,BitUtils.positionToTag(OrderUtils.SyncCustomerCharge.TRAVEL.ordinal()));
            }
            if(detail.getOtherCharge() == 0 && detail.getEngineerOtherCharge() > 0) {
                detail.setOtherCharge(detail.getEngineerOtherCharge());//其他费用
                tags = BitUtils.addTags(tags,BitUtils.positionToTag(OrderUtils.SyncCustomerCharge.OTHER.ordinal()));
            }
            detail.setSyncChargeTags(tags);
        }catch (Exception e){
            log.error("读取客户自动计算远程费品类配置错误,category:{}",productCategoryId,e);
        }
    }

    /**
     * 远程费+其他费用总费用受控品类
     * (同次)上门服务中远程费与其他费用的合计金额超过设定金额，不允许保存
     * 费用不超过设定金额，应收为0
     * @param productCategoryId 品类id
     * @param list 之前的上门服务,不包含本次上门服务
     * @param detail 本次上门服务
     */
    @Transactional
    public void limitRemoteChargeCheck(Long productCategoryId,List<OrderDetail> list, OrderDetail detail) {
        long id = Optional.ofNullable(productCategoryId).orElse(0L);
        if(id <= 0){
            log.error("传入品类ID小于等于0,{}",id);
            return;
        }
        Dict limitRemoteDict = MSDictUtils.getDictByValue(productCategoryId.toString(), OrderUtils.LIMIT_REMOTECHARGE_CATEGORY_DICT);
        if (limitRemoteDict == null) {
            return;
        }
        double limitCharge = Double.valueOf(limitRemoteDict.getSort());
        if(limitCharge <0){
            limitCharge = 0;
        }
        int serviceTimes = detail.getServiceTimes();
        if(!CollectionUtils.isEmpty(list)) {
            TwoTuple<Double, Double> total = list.stream()
                    .filter(t -> t.getServiceTimes() == serviceTimes && t.getDelFlag() == 0)
                    .map(t -> new TwoTuple<Double, Double>(t.getEngineerTravelCharge(), t.getEngineerOtherCharge()))
                    .reduce(new TwoTuple<Double, Double>(0.00, 0.00), (d1, d2) -> {
                        return new TwoTuple<Double, Double>(d1.getAElement() + d2.getAElement(), d1.getBElement() + d2.getBElement());
                    });
            total.setAElement(total.getAElement() + detail.getEngineerTravelCharge());
            total.setBElement(total.getBElement() + detail.getEngineerOtherCharge());
            double charge = total.getAElement() + total.getBElement();
            if (charge > limitCharge) {
                throw new RuntimeException(MessageFormat.format("远程费用和其他费用合计{0,number,#.##}元，已超过上限{1,number,#.##}元，不能保存！请确认是否操作退单!", charge,limitCharge));
            }
        }else{
            double charge = detail.getEngineerTravelCharge() + detail.getEngineerOtherCharge();
            if (charge > limitCharge) {
                throw new RuntimeException(MessageFormat.format("远程费用和其他费用合计{0,number,#.##}元，已超过上限{1,number,#.##}元，不能保存！请确认是否操作退单!", charge,limitCharge));
            }
        }
        //应收清零
        detail.setTravelCharge(0.00);
        detail.setOtherCharge(0.00);
    }

    /**
     * 添加上门服务明细
     *
     * 2019-4-25 财务退回的异常处理时添加上门服务项不需要检查预约时间（工单管理 - 客服工单 - 异常处理）
     * @param detail
     */
    @Transactional(readOnly = false)
    public void addDetail(OrderDetail detail, boolean isCheckAppointmentDate) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, detail.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        Integer statusValue = null;
        Integer subStatusValue = null;
        User user = detail.getCreateBy();
        try {
            Order order = getOrderById(detail.getOrderId(), StringUtils.isBlank(detail.getQuarter())?"":detail.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单错误，请重试。");
            }

            Integer chargeFlag = order.getOrderCondition().getChargeFlag();
            if (chargeFlag != null && chargeFlag.intValue() == 1) {
                throw new OrderException("此订单已已经对账，不能删除上门服务。");
            }
            Date date = new Date();
            // 预约时间检查 2019/04/13
            if (isCheckAppointmentDate) {
                if (order.getOrderCondition().getAppointmentDate() == null) {
                    throw new OrderException("没有设置预约时间，不允许直接确认上门.");
                }

                if (order.getOrderCondition().getAppointmentDate().getTime() > DateUtils.getEndOfDay(date).getTime()) {
                    throw new OrderException("预约时间与当前不一致，请重新预约！");
                }
            }
            detail.setQuarter(order.getQuarter());//数据库分片
            OrderCondition condition = order.getOrderCondition();
            statusValue = condition.getStatusValue();//2019-03-25
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
            Long engiId = condition.getEngineer().getId();
            //增加网点确认 2019/08/01
            if(detail.getServicePoint() == null || detail.getServicePoint().getId() == null || detail.getServicePoint().getId() <= 0) {
                detail.setServicePoint(condition.getServicePoint());
            }
            //2021-05-19 偏远区域
            /*RestResult<Boolean> remoteCheckResult = checkServicePointRemoteArea(condition);
            if(remoteCheckResult.getCode() != ErrorCode.NO_ERROR.code){
                throw new OrderException(new StringJoiner("").add("判断区域是否为偏远区域错误:").add(remoteCheckResult.getMsg()).toString());
            }*/
            ServicePrice eprice = null;
           /* Boolean isRemoteArea = (Boolean)remoteCheckResult.getData();
            if(isRemoteArea) {
                // 偏远区域
                List<com.kkl.kklplus.entity.common.NameValuePair<Long, Long>> nameValuePairs = Lists.newArrayList(new com.kkl.kklplus.entity.common.NameValuePair<Long,Long>(product.getId(),detail.getServiceType().getId()));
                Map<String, ServicePrice> priceMap = servicePointService.getRemotePriceMapByProductsFromCache(servicePointId, nameValuePairs);
                if (priceMap == null) {
                    throw new OrderException("网点[偏远区域]价格读取失败，请重试");
                }
                if (CollectionUtils.isEmpty(priceMap)) {
                    throw new OrderException("网点[偏远区域]价格读取失败，未维护网点价格");
                }
                eprice = priceMap.get(new StringJoiner(":").add(product.getId().toString()).add(detail.getServiceType().getId().toString()).toString());
            }else{
                //标准价格
                eprice = servicePointService.getPriceByProductAndServiceTypeFromCache(servicePointId,product.getId(),detail.getServiceType().getId());
            }*/
            eprice = getPriceByProductAndServiceTypeFromCacheNew(condition,servicePointId,product.getId(),detail.getServiceType().getId());
            if (eprice == null) {
                throw new OrderException(String.format("未定义%s服务价格；网点：%s[%s] 产品:%s 服务：%s。",condition.getServicePoint().getServicePointNo(),condition.getServicePoint().getName(), product.getName(), detail.getServiceType().getName()));
            }
            //网点费用表
            OrderServicePointFee orderServicePointFee = getOrderServicePointFee(order.getId(), order.getQuarter(), servicePointId);
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
                long[] setIds = getSetProductIdIncludeMe(product.getId(), order.getItems());
                if (setIds != null && setIds.length > 0) {
                    subProducts = ArrayUtils.addAll(subProducts, setIds);
                }
            }
            final long[] sids = ArrayUtils.clone(subProducts);
            //切换为微服务,只读取单头
            List<MaterialMaster> materials = orderMaterialService.findMaterialMasterHeadsByOrderId(detail.getOrderId(), order.getQuarter());
            if (materials != null && materials.size() > 0) {
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
        /*
        boolean isAddFlag = false;//用于判断远程费用在上门时只支付一次
        Double travleCharge = details.stream().filter(t->t.getDelFlag()==0).mapToDouble(t->t.getEngineerTravelCharge()).sum();
        if(travleCharge==0){
            isAddFlag = true;
            detail.setEngineerTravelCharge(orderFee.getPlanTravelCharge());
            detail.setTravelNo(StringUtils.isBlank(orderFee.getPlanTravelNo())?"":orderFee.getPlanTravelNo());
            detail.setTravelCharge(orderFee.getCustomerPlanTravelCharge());
        }*/
            autoCountCustomerRemoteCharge(condition.getProductCategoryId(),detail);
            if(detail.getSyncChargeTags() == null){
                detail.setSyncChargeTags(0);
            }
            //2020-11-22 远程费+其他费用的总费用受控品类处理
            limitRemoteChargeCheck(condition.getProductCategoryId(),details,detail);

            details.add(detail);//*
            rechargeOrder(details, detail);
            HashMap<String, Object> params = Maps.newHashMap();

            //保险费汇总(负数)
            Double insuranceCharge = getTotalOrderInsurance(order.getId(), order.getQuarter());
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
            HashMap<String, Object> feeMap = recountFee(details);
            Integer serviceTimes = (Integer) feeMap.get("serviceTimes");
            //应收
            orderFee.setServiceCharge((Double) feeMap.get("serviceCharge"));
            orderFee.setMaterialCharge((Double) feeMap.get("materialCharge"));
            orderFee.setExpressCharge((Double) feeMap.get("expressCharge"));
            orderFee.setTravelCharge((Double) feeMap.get("travelCharge"));
            orderFee.setOtherCharge((Double) feeMap.get("otherCharge"));
            orderFee.setOrderCharge((Double) feeMap.get("orderCharge"));//以上5项合计
            //时效费,加急费,好评费
            if (condition.getPendingFlag() == 1 || detail.getAddType() == 1) {
                orderFee.setOrderCharge(orderFee.getOrderCharge() + orderFee.getCustomerTimeLinessCharge() + orderFee.getCustomerUrgentCharge() + orderFee.getPraiseFee());
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
                // 2020-04-13
                // 好评费不汇总，因好评费只加到最后的网点上
                // 在异常处理完成时再对应付好评费处理
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
                statusValue = Order.ORDER_STATUS_SERVICED;//2019-03-25 ryan
            }
            if (detail.getAddType() != 1) {
                subStatusValue = Order.ORDER_SUBSTATUS_SERVICED;//2019-03-25 ryan
                params.put("subStatus", Order.ORDER_SUBSTATUS_SERVICED);//Add by Zhoucy
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
                    //insert
                    model.setBrand(StringUtils.left(StringUtils.toString(model.getBrand()), 20));//实际上门服务项的产品品牌只保留前18个字符
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
                    isnull =false;
                    if(model.getActionCode() == null){
                        isnull = true;
                    }
                    if(isnull || model.getActionCode().getId() == null){
                        if(actionCode == null) {
                            actionCode = new MDActionCodeDto();
                            actionCode.setId(0L);
                            if(isnull) {
                                actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                            }else{
                                if(StringUtils.isBlank(model.getActionCode().getName())){
                                    actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                                }else{
                                    actionCode.setName(model.getActionCode().getName());
                                }
                            }

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
                            && ArrayUtils.contains(sids, t.getProduct().getId().longValue())
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
            //2019-12-27 统一上门服务格式
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
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);//2021-07-03 非第一次上门服务客户看不到
            //if (firstService) {
            //    processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            //} else {
            //    processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            //}
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

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

            //region 网点订单数据更新 2019-03-25
            //订单状态和子状态需要更新
            servicePointOrderBusinessService.onSiteService(
                    order.getId(), order.getQuarter(), servicePointId, engiId,
                    statusValue, subStatusValue, user.getId(), date.getTime());
            //endregion
        } catch (OrderException oe) {
            throw new OrderException(ExceptionUtils.getRootCauseMessage(oe),oe);
        } catch (Exception e) {
            log.error("[OrderService.addDetail]orderId:{}", detail.getOrderId(), e);
            throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 修改上门服务
     */
    @Transactional
    public void editDetail(HashMap<String, Object> params){
        dao.editDetail(params);
    }


    /**
     * 重新计算费用，只计算本次上门服务项目
     * 循环计价，厂商取最高价，网点取最低价
     */
    public void rechargeOrder(List<OrderDetail> list, OrderDetail detail) {
        if (list == null || list.size() == 0) {
            return;
        }
        //本次上门，删除标记不等于1
        List<OrderDetail> items;
        OrderDetail m;
        int size;
        //1.厂商,先去标准价最高，标准价相同，取折扣价最低的
        Double cprice = 0.0;
        items = list.stream()
                .filter(t -> t.getServiceTimes() == detail.getServiceTimes() && t.getDelFlag().intValue() != 1)
                .sorted(Comparator.comparingDouble(OrderDetail::getStandPrice).reversed()
                        .thenComparingDouble(OrderDetail::getDiscountPrice))
                .collect(Collectors.toList());
        if (items.size() == 0) {
            return;
        }
        size = items.size();
        for (int i = 0; i < size; i++) {
            m = items.get(i);
            if (i == 0) {
                m.setCharge(m.getStandPrice() + m.getDiscountPrice() * (m.getQty() - 1));
            } else {
                m.setCharge(m.getDiscountPrice() * m.getQty());
            }
            cprice = cprice + m.getCharge();
        }

        //2.网点取价，循环累计，取最低价
        items = list.stream()
                .filter(t -> t.getServiceTimes() == detail.getServiceTimes() && t.getDelFlag().intValue() != 1)
                .collect(Collectors.toList());
        size = items.size();
        Double sprice = 0.0;
        Map<Integer, Double> servprices = Maps.newHashMap();//网点
        //1.循环累计，当前行取标准加，其余取折扣加
        for (int i = 0; i < size; i++) {
            sprice = 0.0;

            for (int j = 0; j < size; j++) {
                m = items.get(j);
                if (i == j) {
                    sprice = sprice + m.getEngineerStandPrice() + m.getEngineerDiscountPrice() * (m.getQty() - 1);
                } else {
                    sprice = sprice + m.getEngineerDiscountPrice() * m.getQty();
                }
            }
            servprices.put(i, sprice);
        }
        //2.取最低价
        int sidx = 0;
        sprice = servprices.values().stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        for (int j = 0; j < size; j++) {
            if (servprices.get(j).equals(sprice)) {
                sidx = j;
                break;
            }
        }

        //计价费用
        for (int i = 0; i < size; i++) {
            m = items.get(i);
            //网点
            if (i == sidx) {
                m.setEngineerServiceCharge(m.getEngineerStandPrice() + m.getEngineerDiscountPrice() * (m.getQty() - 1));
            } else {
                m.setEngineerServiceCharge(m.getEngineerDiscountPrice() * m.getQty());
            }
        }
        items = null;
    }

    /**
     * 派单及接单时，计算网点预估服务费用
     * 循环计价，取最低价
     */
    public Double calcServicePointCost(OrderCondition orderCondition,ServicePoint servicePoint, List<OrderItem> list) {
        if (list == null || list.size() == 0) {
            return 0.0d;
        }
        Long servicePointId = servicePoint.getId();
        List<OrderDetail> items = mapper.mapAsList(list, OrderDetail.class);
        //使用新的网点价格读取方法 2020-03-07
        List<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>> nameValuePairs = getOrderDetailProductAndServiceTypePairs(items);
        if(CollectionUtils.isEmpty(nameValuePairs)){
            throw new OrderException("确认订单服务项目失败");
        }
        //Map<String,ServicePrice> priceMap = servicePointService.getPriceMapByProductsFromCache(servicePointId,nameValuePairs);
        Map<String,ServicePrice> priceMap = getServicePriceFromCacheNew(orderCondition,servicePointId,nameValuePairs);

        if(priceMap==null){
            throw new OrderException("网点价格读取失败，请重试");
        }
        if (CollectionUtils.isEmpty(priceMap)) {
            OrderDetail orderDetail = items.get(0);
            if(orderDetail!=null && orderDetail.getProduct()!=null && orderDetail.getServiceType()!=null){
                throw new OrderException(String.format("网点[%s] 产品[%s] 未维护服务[%s]的价格",servicePoint.getName(), orderDetail.getProduct().getName(), orderDetail.getServiceType().getName()));
            }else{
                throw new OrderException("网点价格读取失败，没维护网点价格");
            }
        }

        OrderDetail m;
        int size;
        Double sprice = 0.0;
        size = items.size();
        //网点取价，循环累计，取最低价
        Map<Integer, Double> servprices = Maps.newHashMap();//网点
        ServicePrice price;
        //1.循环累计，当前行取标准加，其余取折扣加
        for (int i = 0; i < size; i++) {
            sprice = 0.0;
            for (int j = 0; j < size; j++) {
                m = items.get(j);
                final Long productId = m.getProduct().getId();
                final Long serviceTypeId = m.getServiceType().getId();
                price = priceMap.get(String.format("%d:%d",productId,serviceTypeId));
                /*
                price = streamSupplier.get().filter(t -> t.getProduct().getId().equals(productId)
                        && t.getServiceType().getId().equals(serviceTypeId) && t.getDelFlag() == 0)
                        .findFirst().orElse(null);
                */
                if (price == null) {
                    throw new OrderException(String.format("网点:%s 未定义产品:%s 未定义：%s 的价格", servicePoint.getName(), m.getProduct().getName(), m.getServiceType().getName()));
                }
                if (i == j) {
                    sprice = sprice + price.getPrice() + price.getDiscountPrice() * (m.getQty() - 1);
                } else {
                    sprice = sprice + price.getDiscountPrice() * m.getQty();
                }
            }
            servprices.put(i, sprice);
        }
        //2.取最低价
        int sidx = 0;
        sprice = servprices.values().stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        return sprice;
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
    public void confirmDoorAuto(Long orderId, String quarter, User user, int confirmType) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("错误：此订单正在处理中，请稍候重试，或刷新订单。");
        }
        try {
            Order o = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
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
            AjaxJsonEntity locationCheckResult = checkAddressLocation(o.getDataSource().getIntValue(),orderId,o.getQuarter());
            if(!locationCheckResult.getSuccess()){
                throw new OrderException("因"+locationCheckResult.getMessage() + "，不能确认上门。");
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
                    processLog.setActionComment(String.format("%s%s", confirmType == 0 ? "客服" : "安维", "确认上门"));
                    processLog.setStatus(condition.getStatus().getLabel());
                    processLog.setStatusValue(condition.getStatusValue());
                    processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                    processLog.setCloseFlag(0);
                    processLog.setCreateBy(user);
                    processLog.setCreateDate(date);
                    processLog.setCustomerId(condition.getCustomerId());
                    processLog.setDataSourceId(dataSourceId);
                    saveOrderProcessLogNew(processLog);

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
                    dao.updateCondition(params);

                    // 2019-03-25 网点订单数据更新
                    servicePointOrderBusinessService.confirmOnSiteService(orderId,o.getQuarter(),servicePointId,engineerId,prevStatus,Order.ORDER_SUBSTATUS_SERVICED,user.getId(),date.getTime());

                    //region B2B消息队列
                    if (o.getDataSourceId() == B2BDataSourceEnum.VIOMI.id
                            || o.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                        Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                        b2BCenterOrderService.serviceOrder(o, pointId, engineerId, user, date);
                    }

                    return;
                }
            }

            OrderFee orderFee = o.getOrderFee();
            if(orderFee == null){
                throw new OrderException("错误：读取订单费用汇总数据失败！");
            }
            //2020-10-21 从主库读取派单时预设的费用和单号
            OrderFee feeMaster = getPresetFeeWhenPlanFromMasterDB(orderId,o.getQuarter());
            if(feeMaster == null){
                log.error("读取派单预设费用失败,orderId:{} , quarter:{}",orderId,o.getQuarter());
                throw new OrderException("错误：读取派单预设费用失败！");
            }
            orderFee.setPlanTravelCharge(feeMaster.getPlanTravelCharge());
            orderFee.setPlanTravelNo(feeMaster.getPlanTravelNo());
            orderFee.setPlanDistance(feeMaster.getPlanDistance());
            orderFee.setCustomerPlanTravelCharge(feeMaster.getCustomerPlanTravelCharge());
            orderFee.setPlanOtherCharge(feeMaster.getPlanOtherCharge());
            orderFee.setCustomerPlanOtherCharge(feeMaster.getCustomerPlanOtherCharge());

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
            //ServicePoint Price
            //使用新的网点价格读取方法 2020-03-07
            List<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>> nameValuePairs = getOrderItemProductAndServiceTypePairs(items);
            if(CollectionUtils.isEmpty(nameValuePairs)){
                throw new OrderException("确认订单服务项目失败");
            }
            Map<String, ServicePrice> priceMap = null;
            priceMap = getServicePriceFromCacheNew(condition,servicePointId,nameValuePairs);
            if (priceMap == null) {
                throw new OrderException(new StringJoiner("").add("网点").add("价格读取失败，请重试").toString());
            }
            if (CollectionUtils.isEmpty(priceMap)) {
                throw new OrderException(new StringJoiner("").add("网点").add("价格读取失败，未维护网点价格").toString());
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
            Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
            if(CollectionUtils.isEmpty(serviceTypeMap)){
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
                detail.setBrand(StringUtils.left(StringUtils.toString(item.getBrand()), 20));//实际上门服务项的品牌只保留前20个字符
                detail.setServiceTimes(serviceTimes);
                detail.setQty(item.getQty());
                detail.setServiceType(item.getServiceType());
                detail.setServiceCategory(new Dict(st.getOrderServiceType(),""));
                detail.setRemarks("自动添加下单的服务项目");
                detail.setSyncChargeTags(0);

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
                        autoCountCustomerRemoteCharge(condition.getProductCategoryId(),detail);
                    }
                    //2020-11-22 远程费+其他费用的总费用受控品类处理
                    limitRemoteChargeCheck(condition.getProductCategoryId(),null,detail);
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
            Double insuranceCharge = getTotalOrderInsurance(o.getId(),o.getQuarter());
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
            rechargeOrder(details, firstDetail);
            //重新汇总金额
            HashMap<String, Object> feeMap = recountFee(details);
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
            processLog.setDataSourceId(dataSourceId);
            saveOrderProcessLogNew(processLog);
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
                    processLog.setDataSourceId(dataSourceId);
                    saveOrderProcessLogNew(processLog);
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
                    isnull =false;
                    if(model.getActionCode() == null){
                        isnull = true;
                    }
                    if(isnull || model.getActionCode().getId() == null){
                        if(actionCode == null) {
                            actionCode = new MDActionCodeDto();
                            actionCode.setId(0L);
                            if(isnull) {
                                actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                            }else{
                                if(StringUtils.isBlank(model.getActionCode().getName())){
                                    actionCode.setName(org.apache.commons.lang3.StringUtils.EMPTY);
                                }else{
                                    actionCode.setName(model.getActionCode().getName());
                                }
                            }

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

            //cache
            OrderCacheUtils.setDetailActionFlag(orderId);
            OrderCacheUtils.delete(orderId);

            //region B2B消息队列
            if (prevStatus == Order.ORDER_STATUS_PLANNED || o.getDataSourceId() == B2BDataSourceEnum.VIOMI.id
                    || o.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                Long pointId = condition.getServicePoint() != null ? condition.getServicePoint().getId() : null;
                b2BCenterOrderService.serviceOrder(o, pointId, engineerId, user, date);
            }
            //endregion B2B消息队列

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.confirmOnSiteService(orderId,o.getQuarter(),servicePointId,engineerId,status.getIntValue(),Order.ORDER_SUBSTATUS_SERVICED,user.getId(),date.getTime());
            //endregion

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
     * 标记app异常
     * @param orderId
     * @param quarter
     * @param servicePointId    网点id
     * @param pendingType   停滞类型
     * @param user  反馈用户
     * @param remarks   备注
     */
    @Transactional(readOnly = false)
    public void saveAppAbnormaly(Long orderId, String quarter, Long servicePointId, Dict pendingType, User user, String remarks) {
        String lockkey = null;
        Boolean locked = false;
        try {
            lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
            //获得锁
            locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
            if (!locked) {
                throw new OrderException("此订单正在处理中，请稍候重试");
            }

            Date date = new Date();
            Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null || order.getItems() == null) {
                throw new OrderException("读取订单信息失败");
            }
            Integer statusValue = order.getOrderCondition().getStatusValue();
            if (statusValue != Order.ORDER_STATUS_SERVICED && statusValue != Order.ORDER_STATUS_PLANNED) {
                throw new OrderException("保存标记异常失败，订单状态错误");
            }

            OrderCondition condition = order.getOrderCondition();
            Integer orgAppAbnormalyFlag = condition.getAppAbnormalyFlag();
            if( 0 == orgAppAbnormalyFlag) {
                HashMap<String, Object> params = Maps.newHashMap();
                params.put("orderId", orderId);
                params.put("quarter", order.getQuarter());
                params.put("appAbnormalyFlag", 1);
                dao.updateCondition(params);
                //同步网点工单数据
                Long spId = ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
                servicePointOrderBusinessService.abnormalyFlag(
                        order.getId(),
                        order.getQuarter(),
                        spId,
                        1,
                        user.getId(),
                        date.getTime()
                );
            }

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("进度跟踪");
            processLog.setOrderId(orderId);
            processLog.setActionComment("异常原因："
                    .concat(pendingType.getLabel().replace(",", "").replace("，", ""))
                    .concat("，备注：")
                    .concat(remarks));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //异常单
            AbnormalForm abnormalForm = null;
            try {
                AbnormalFormEnum.SubType subType = AbnormalFormEnum.SubType.fromCode(Integer.valueOf(pendingType.getValue()));
                if(subType == null){
                    subType = AbnormalFormEnum.SubType.OLD_ABNORMAL_OPERATION;
                }
                abnormalForm = abnormalFormService.handleAbnormalForm(order,pendingType.getLabel(),user,AppFeedbackEnum.Channel.APP.getValue(),
                        AbnormalFormEnum.FormType.OLD_APP_ABNORMALY.getCode(),subType.getCode(),processLog.getActionComment());
                if(abnormalForm!=null){
                    abnormalForm.setOpinionLogId(0L);
                    abnormalFormService.save(abnormalForm);
                }
            }catch (Exception e){
                log.error("[orderService.saveAppAbnormaly]保存异常单失败 form:{}", GsonUtils.getInstance().toGson(abnormalForm),e);
            }

            //region Notice Message
            if (orgAppAbnormalyFlag == 0 && condition.getKefu() != null) {
                try {

                    MQNoticeMessage.NoticeMessage message = MQNoticeMessage.NoticeMessage.newBuilder()
                            .setOrderId(condition.getOrderId())
                            .setQuarter(condition.getQuarter())
                            .setNoticeType(NoticeMessageConfig.NOTICE_TYPE_APPABNORMALY)
                            .setCustomerId(condition.getCustomer().getId())
                            .setKefuId(condition.getKefu().getId())
                            .setAreaId(condition.getArea().getId())
                            .setTriggerBy(MQWebSocketMessage.User.newBuilder()
                                    .setId(user.getId())
                                    .setName(user.getName())
                                    .build()
                            )
                            .setTriggerDate(date.getTime())
                            .setDelta(1)
                            .build();

                    try {
                        noticeMessageSender.send(message);
                    } catch (Exception e) {
                        //消息队列发送错误
                        log.error("[OrderService.saveAppabnormaly] send MQNoticeMessage,orderId:{} ,user:{} ,pendingType:{}", orderId, user.getId(), pendingType.getValue(), e);
                    }

                } catch (Exception e) {
                    log.error("[OrderService.saveAppabnormaly] send MQNoticeMessage,orderId:{} ,user:{} ,pendingType:{}", orderId, user.getId(), pendingType.getValue(), e);
                }
            }
            //endregion Notice Message

            //cache,淘汰,调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setDeleteField(OrderCacheField.CONDITION);
            OrderCacheUtils.update(builder.build());

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
        }
    }

    /**
     * 取消app异常
     *
     * @param orderId 订单id
     * @param quarter 分片
     */
    @Transactional(readOnly = false)
    public void dealAPPException(Long orderId, String quarter) {
        if (orderId == null || orderId <= 0) {
            throw new OrderException("参数无值。");
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = UserUtils.getUser();
        try {
            Order o = getOrderById(orderId, "", OrderUtils.OrderDataLevel.CONDITION, true);
            if (orderId == null || orderId <= 0) {
                throw new OrderException("读取订单信息失败。");
            }
            OrderCondition condition = o.getOrderCondition();
            if (condition.getAppAbnormalyFlag() != 1) {
                return;
            }
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            //condition
            condition.setAppAbnormalyFlag(0);
            condition.setUpdateDate(date);
            condition.setUpdateBy(user);
            params.put("quarter", o.getQuarter());//*
            params.put("orderId", orderId);
            params.put("appAbnormalyFlag", 0);
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            //同步网点工单数据
            Long spId = ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
            servicePointOrderBusinessService.abnormalyFlag(
                    orderId,
                    o.getQuarter(),
                    spId,
                    0,
                    user.getId(),
                    date.getTime()
            );

            //关闭异常单 add on 2020-2-26
            AbnormalForm abnormalForm = new AbnormalForm();
            abnormalForm.setOrderId(orderId);
            abnormalForm.setCloseBy(user.getId());
            abnormalForm.setCloseAt(date.getTime());
            abnormalForm.setQuarter(o.getQuarter());
            abnormalFormService.closeByOrderId(abnormalForm);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .incrVersion(1L)
                    .setSyncDate(new Date().getTime())
                    .setCondition(condition)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());
            //region Notice Message
            try {
                MQNoticeMessage.NoticeMessage message = MQNoticeMessage.NoticeMessage.newBuilder()
                        .setOrderId(condition.getOrderId())
                        .setQuarter(condition.getQuarter())
                        .setNoticeType(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY)
                        .setCustomerId(condition.getCustomer().getId())
                        .setKefuId(condition.getKefu() != null ? condition.getKefu().getId() : 0l)
                        .setAreaId(condition.getArea().getId())
                        .setTriggerBy(MQWebSocketMessage.User.newBuilder()
                                .setId(user.getId())
                                .setName(user.getName())
                                .build()
                        )
                        .setTriggerDate(date.getTime())
                        .setDelta(-1)
                        .build();

                try {
                    noticeMessageSender.send(message);
                } catch (Exception e) {
                    //消息队列发送错误
                    log.error("[OrderService.dealAPPException] send MQNoticeMessage,orderId:{} ,user:{}", orderId, user.getId(), e);
                }
            } catch (Exception e) {

                log.error("[OrderService.dealAPPException] send MQNoticeMessage,orderId:{} ,user:{}", orderId, user.getId(), e);
            }
            //endregion Notice Message

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.dealAPPException] send MQNoticeMessage,orderId:{} ,user:{}", orderId, user.getId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }

    }

    /**
     * 工单完成(app)
     */
    @Transactional(readOnly = false)
    public RestResult saveOrderComplete(Order order, User user, Dict completeType, String remarks) {
        long index = 0;
        if (order == null || order.getOrderCondition() == null || completeType == null || user == null) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "参数错误：为空");
        }
        //检查sub_status 2019/01/14
        OrderCondition condition = order.getOrderCondition();
        if (condition.getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED.intValue()) {
            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.code, ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.message);
        }
        int orderStatusValue = condition.getStatusValue();
        if (orderStatusValue == Order.ORDER_STATUS_COMPLETED.intValue() || orderStatusValue == Order.ORDER_STATUS_CHARGED.intValue()) {
            return RestResultGenerator.custom(ErrorCode.ORDER_FINISH_SERVICE.code, ErrorCode.ORDER_FINISH_SERVICE.message);
        } else if (orderStatusValue > Order.ORDER_STATUS_CHARGED.intValue()) {
            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.code, "订单已取消或已退单");
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
                return RestResultGenerator.custom(ErrorCode.ORDER_REDIS_LOCKED.code, ErrorCode.ORDER_REDIS_LOCKED.message);
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append("orderNo:").append(order.getOrderNo());
            buffer.append(" completeType:").append(completeType.getValue());

            //region check

            //检查状态
            int statusValue = condition.getStatusValue();
            if (statusValue != Order.ORDER_STATUS_SERVICED) {
                return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.code, ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.message);
            } else {
                //APP完工[55]
                Dict appCompletedStatus =  MSDictUtils.getDictByValue(Order.ORDER_STATUS_APP_COMPLETED.toString(), Dict.DICT_TYPE_ORDER_STATUS);
                condition.setStatus(appCompletedStatus);
            }
            //检查未审核或未发货配件申请单
            // 根据订单配件状态检查是否可以客评 2019/06/13 22:56 at home
            MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),order.getId(),order.getQuarter());
            if(!MSResponse.isSuccessCode(msResponse)){
                return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.code, "订单完成失败，" + msResponse.getMsg());
            }
            //检查客户要求完成照片数量
            Customer customer = customerService.getFromCache(order.getOrderCondition().getCustomer().getId());
            if (customer == null) {
                return RestResultGenerator.custom(ErrorCode.RECORD_NOT_EXIST.code, "客户不存在，或读取客户信息失败");
            }
            if (customer.getMinUploadNumber() > 0 && order.getOrderCondition().getFinishPhotoQty() < customer.getMinUploadNumber()) {
                return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.code, "请先上传客户要求的最少服务效果图");
            }
            if (!checkOrderProductBarCode(order.getId(), order.getQuarter(), order.getOrderCondition().getCustomer().getId(), order.getDetailList())) {
                return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVEORDERCOMPLETE.code, "请先上传产品条码");
            }

            Integer orgAppAbnormaly = condition.getAppAbnormalyFlag();//如果原来异常，本次也异常，不需要累加异常数量
            buffer.append(" orgAppAbnormaly:").append(orgAppAbnormaly.toString());
            //完成类型
            Integer appAbnormalyFlag = 0;
            if (1 == orgAppAbnormaly) {//订单已经app异常，改成短信回复客评
                autoCompleteFlag = false;
            } else if (!"compeled_all".equalsIgnoreCase(completeType.getValue()) && !"compeled_all_notest".equalsIgnoreCase(completeType.getValue())) {
                //已完成工单全部内容但未试机 不标记异常  2020-01-07
                autoCompleteFlag = false;
                appAbnormalyFlag = 1;
            }
            buffer.append(" appAbnormalyFlag:").append(appAbnormalyFlag.toString());
            if (true == autoCompleteFlag) {
                String checkResult = checkAutoComplete(order);
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

            //region save to db
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
            dao.updateCondition(params);
            if(1 == appAbnormalyFlag){
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
                        .remark(StringUtils.left(completeType.getValue() + ": " + remarks,250))
                        .createAt(System.currentTimeMillis())
                        .createBy(user)
                        .times(1)
                        .totalTimes(1)
                        .build();
                orderOpitionTraceService.insert(opitionTrace);

                //异常单
                Integer subType = 0;
                if(condition.getOrderServiceType()==1){
                    subType = AbnormalFormEnum.SubType.INSTALL_ERROR.code;
                }else{
                    subType = AbnormalFormEnum.SubType.REPAIR_ERROR.code;
                }
                String reason = completeType.getLabel() + ": " + remarks;
                AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order,reason,user, AppFeedbackEnum.Channel.APP.getValue(),
                        AbnormalFormEnum.FormType.APP_COMPLETE.code,subType,"App完工不符合自动完工条件");
                try{
                    if(abnormalForm!=null){
                        abnormalForm.setOpinionLogId(opitionTrace.getId());
                        abnormalFormService.save(abnormalForm);
                    }
                }catch (Exception e){
                    log.error("[OrderService.SaveOrderComplete]app完工保存异常单失败 form:{}",GsonUtils.getInstance().toGson(abnormalForm), e);
                }
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("安维完成");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("%s,备注:%s", completeType.getLabel(), remarks));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(condition.getStatus().getLabel());
            processLog.setStatusValue(condition.getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setRemarks("");
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //endregion save to db

            //region 消息队列

            //region 短信回访
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
                smsCallbackTaskMQSender.send(condition.getOrderId(),order.getQuarter(),condition.getServicePhone(),strContent.toString(),"",null,"",user.getId(),date.getTime());
            }
            //endregion 短信

            //region 智能回访
            //2019/01/18 更改：只要app完成就发语音回访
            //2019/01/21 更改：不自动完成的才发语音回访
            //2019/04/13 更改：有重单的不发语音回访
            if (voiceEnabled && !autoCompleteFlag && StringUtils.isNoneBlank(siteCode)
                    && StringUtils.isBlank(order.getRepeateNo()) ) {
                sendNewVoiceTaskMessage(siteCode, order, user.getName(), date);
            }
            //endregion 智能回访

            //region 异常统计
            if (1 == appAbnormalyFlag && 0 == orgAppAbnormaly) {
                buffer.append(" 发送异常统计消息");
                sendNoticeMessage(
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
            //endregion 异常统计

            b2BCenterOrderService.appCompleteOrder(order, user, date);

            //region 调用公共缓存

            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.PENDING_TYPE)
                    .setDeleteField(OrderCacheField.PENDING_TYPE_DATE);
            OrderCacheUtils.update(builder.build());

            //endregion

            //region 自动完工
            if (true == autoCompleteFlag) {
                buffer.append(" 发送自动完工消息");
                //自动完工调用saveGrade，因此此处不需要发送B2B订单状态变更消息
                sendAutoCompleteMessage(order.getId(), order.getQuarter(), user, date);
            }
            //endregion 自动完工

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.appComplete(
                    order.getId(),
                    order.getQuarter(),
                    Order.ORDER_SUBSTATUS_APPCOMPLETED,
                    completeType.getValue(),
                    appAbnormalyFlag,
                    user.getId(),
                    date.getTime()
            );
            //endregion

            //region 消息队列

            log.info("app自动完工:{}", buffer.toString());
            if (false == autoCompleteFlag) {
                LogUtils.saveLog("app自动完工", "OrderService.SaveOrderComplete", buffer.toString(), null, null);
            }

            return RestResultGenerator.success();
        } catch (OrderException oe) {
            log.error("[OrderService.SaveOrderComplete]=={}== orderId:{}", index, order.getId(), oe);
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.SaveOrderComplete]=={}== orderId:{}", index, order.getId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
        }
    }


    //region 进度跟踪

    /**
     * 电话联系用户
     */
    @Transactional(readOnly = false)
    public RestResult saveCallUser(Order order, User user) {
        if (order == null || order.getOrderCondition() == null || order.getOrderStatus() == null || user == null) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "参数错误：为空");
        }
        String lockkey = null;//锁
        Boolean locked = false;
        try {
            Date date = new Date();
            lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
            //获得锁
            locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
            if (!locked) {
                return RestResultGenerator.custom(ErrorCode.ORDER_REDIS_LOCKED.code, ErrorCode.ORDER_REDIS_LOCKED.message);
            }
            if (order.getOrderStatus().getFirstContactDate() == null) {
                HashMap<String, Object> params = Maps.newHashMap();
                params.put("orderId", order.getId());
                params.put("quarter", order.getQuarter());
                params.put("firstContactDate", new Date());
                dao.updateStatus(params);
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setOrderId(order.getId());
            processLog.setAction("安维联系用户");
            processLog.setActionComment("安维人员APP联系用户");
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(order.getOrderCondition().getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setRemarks("安维人员APP联系用户");
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(order.getOrderCondition() != null ? order.getOrderCondition().getCustomerId() : 0);
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(builder.build());

            return RestResultGenerator.success();
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);//释放锁
            }
        }
    }


    /**
     * 保存进度跟踪记录
     *
     * @param order      订单
     * @param isEnginner 是否是安维提交
     */
    @Transactional(readOnly = false)
    public OrderProcessLog saveTracking(Order order, Boolean isEnginner) {
        return saveTracking(order, isEnginner, true);
    }

    /**
     * 保存进度跟踪记录
     *
     * @param order      订单
     * @param isEnginner 是否是安维提交
     * @param regetOrder 是否要重载订单内容
     */
    @Transactional(readOnly = false)
    public OrderProcessLog saveTracking(Order order, Boolean isEnginner, Boolean regetOrder) {
        User user = order.getCreateBy();
        Date date = order.getCreateDate();
        Long orderId = order.getId();
        String remarks = order.getRemarks();
        Integer isCustomerSame = order.getIsCustomerSame();
        if (regetOrder) {
            String quarter = order.getQuarter();
            order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单信息失败");
            }
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
        if (isEnginner) {
            processLog.setCloseFlag(2);
        } else {
            processLog.setCloseFlag(0);
        }
        /* 客户可见
        if (isCustomerSame == 1) {
            processLog.setRemarks(processLog.getActionComment());
        } else {
            processLog.setRemarks("");
        }*/
        processLog.setCreateBy(user);
        processLog.setCreateDate(date);

        //客服详情界面：跟踪进度，客户不可见时，网点同样不可见，2019-4-17
        //网点添加跟踪进度：网点可见
        int visibilityValue = OrderUtils.calcProcessLogVisibilityFlag(processLog);
        if (isCustomerSame == 1) {
            visibilityValue = VisibilityFlagEnum.add(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.CUSTOMER, VisibilityFlagEnum.SERVICE_POINT));
        }
        else {
            visibilityValue = VisibilityFlagEnum.subtract(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.CUSTOMER, VisibilityFlagEnum.SERVICE_POINT));
        }
        if (isEnginner) {
            visibilityValue = VisibilityFlagEnum.add(visibilityValue, Sets.newHashSet(VisibilityFlagEnum.SERVICE_POINT));
        }
        processLog.setVisibilityFlag(visibilityValue);
        processLog.setCustomerId(condition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        saveOrderProcessLogWithNoCalcVisibility(processLog);

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

        dao.updateCondition(params);

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

    //endregion 进度跟踪

    //endregion 订单处理

    //region 客户

    /**
     * 客户审核新开订单
     *
     * @param orderId 订单id
     * @param orderNo 订单号
     * @param user    审核人
     */
    @Transactional(readOnly = false)
    public void customerApproveOrder(Long orderId, String orderNo, String quarter, User user) {
        Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.STATUS, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单信息失败");
        }
        String key = String.format(RedisConstant.SD_ORDER, orderId);
        Date date = new Date();
        HashMap<String, Object> params = Maps.newHashMap();
        //condition
        Dict status = new Dict(String.valueOf(Order.ORDER_STATUS_APPROVED));
        String label = MSDictUtils.getDictLabel(status.getValue(), "order_status", "待接单");//切换为微服务
        status.setLabel(label);
        OrderCondition condition = order.getOrderCondition();
        condition.setStatus(status);
        condition.setUpdateBy(user);
        condition.setUpdateDate(date);

        params.put("quarter", order.getQuarter());
        params.put("orderId", orderId);//id
        params.put("status", status);
        params.put("updateBy", user);
        params.put("updateDate", date);
        dao.updateCondition(params);
        //status
        OrderStatus orderStatus = order.getOrderStatus();
        orderStatus.setCustomerApproveFlag(1);
        orderStatus.setCustomerApproveBy(user);
        orderStatus.setCustomerApproveDate(date);

        params.clear();
        params.put("quarter", order.getQuarter());
        params.put("orderId", orderId);//id
        params.put("customerApproveFlag", 1);
        params.put("customerApproveBy", user);
        params.put("customerApproveDate", date);
        dao.updateStatus(params);
        //log
        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setQuarter(order.getQuarter());
        processLog.setAction("订单审核");
        processLog.setOrderId(orderId);
        processLog.setActionComment(String.format("审核订单:%s,审核人:%s", orderNo, user.getName()));
        processLog.setStatus(status.getLabel());
        processLog.setStatusValue(Order.ORDER_STATUS_APPROVED);
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(user);
        processLog.setCreateDate(date);
        processLog.setCustomerId(condition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        saveOrderProcessLogNew(processLog);

        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(orderId)
                .incrVersion(1L)
                .setOrderStatus(orderStatus)
                .setStatus(status)
                .setCondition(condition);
        OrderCacheUtils.update(builder.build());

        //APP通知消息 & 短信
        List<User> engineers = servicePointService.getEngineerAccountsListByAreaAndProductCategory(condition.getArea().getId(),condition.getProductCategoryId());
        sendNewOrderMsg(order, engineers, user);

        //向优盟微服务抛工单原始数据
        umOrderService.sendOrderDataToMS(order);

        //自动派单 2019-04-15
        order.setCreateBy(user);//2019/09/16 传入审单人
        order.setCreateDate(date);//审核日期
        orderMQService.sendAutoPlanOrderMessage(order);

        //version:2.2 订阅物流推送
        logisticsBusinessService.subsLogisticsMessage(MQLMExpress.GoodsType.Goods, order.getId(), order.getOrderNo(), order.getQuarter(), order.getOrderCondition().getServicePhone(), order.getItems());
    }

    @Transactional(readOnly = false)
    public void cancelOrder(Long orderId, User user, String comment) {
        cancelOrderNew(orderId, user, comment, true);
    }

    /**
     * 客户取消订单
     * 自动审核
     *
     * @param orderId 订单id
     * @param comment 取消说明
     */
    @Transactional(readOnly = false)
    public void cancelOrderNew(Long orderId, User user, String comment, boolean isCallB2BInterface) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Order order = getOrderById(orderId, "", OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null) {
                throw new OrderException("读取订单信息失败");
            }
            OrderCondition condition = order.getOrderCondition();
            boolean isSaleman = user.isSaleman();
            if(isSaleman){
                if(condition.getStatusValue() > Order.ORDER_STATUS_PLANNED) {
                    throw new OrderException("该订单不能取消，请刷新页面查看订单订单状态。");
                }
            }else if (condition.getStatusValue() > Order.ORDER_STATUS_ACCEPTED){
                throw new OrderException("该订单不能取消，请刷新页面查看订单订单状态。");
            }
            //检查配件单
            if(condition.getPartsFlag() == 1){
                MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),order.getId(),order.getQuarter());
                if(!MSResponse.isSuccessCode(msResponse)){
                    throw new OrderException(msResponse.getMsg());
                }
            }
            //切换为微服务
            CustomerFinance cfi = customerFinanceDao.get(condition.getCustomer().getId());
            if (cfi.getPaymentType() != null && Integer.parseInt(cfi.getPaymentType().getValue()) > 0) {
                String paymentTypeLabel = MSDictUtils.getDictLabel(cfi.getPaymentType().getValue(), "PaymentType", "");
                cfi.getPaymentType().setLabel(paymentTypeLabel);
            }

            // 检查客户是否已经锁定
            if (cfi != null && cfi.getLockFlag() == 1) {
                throw new OrderException("客户结账锁打开，系统正在结账，无法取消订单！ 请稍候再试");
            }
            //好评单
            OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(orderId, condition.getQuarter());

            OrderFee fee = order.getOrderFee();
            Date date = new Date();
            //status
            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_CANCELED), "order_status", "已取消");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_CANCELED, label);
            Integer cancelResponsible = OrderStatus.CANCEL_RESPONSIBLE_CUSTOMER; //厂商
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", status);// 取消
            params.put("cancelSponsor", 1);//发起方
            params.put("cancelResponsible", cancelResponsible);
            params.put("cancelApplyBy", user);
            params.put("cancelApplyDate", date);
            params.put("cancelApplyComment", comment);
            params.put("cancelApproveFlag", 1);
            params.put("cancelApproveBy", user);
            params.put("cancelApproveDate", date);
            //close
            params.put("closeFlag", 1);
            params.put("closeBy", user);
            params.put("updateBy", user);
            params.put("updateDate", date);

            dao.updateStatus(params);

            //condition
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", status);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("closeDate", date);//订单关闭日期
            params.put("subStatus", Order.ORDER_SUBSTATUS_CANCELED);//Add by Zhoucy
            // 突击单关闭 Add by Ryan
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                params.put("rushOrderFlag", 2);
            }
            dao.updateCondition(params);

            //关闭突击单
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                crushService.closeOrderCurshByOrderId(orderId,order.getQuarter(),1,null,user,date);
            }
            condition.setRushOrderFlag(2);

            Boolean hasRemindered = false;
            if(order.getOrderStatus() != null && order.getOrderStatus().getReminderStatus() != null && order.getOrderStatus().getReminderStatus() > 0) {
                hasRemindered = true;
            }
            // 关闭催单 2019/08/15
            if (hasRemindered && order.getOrderStatus().getReminderStatus() < ReminderStatus.Completed.getCode()){
                Long servicePointId = ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
                reminderService.completeReminder(orderId,order.getQuarter(),user,date,comment, ReminderAutoCloseTypeEnum.OrderCancel,Order.ORDER_STATUS_CANCELED,servicePointId);
            } else if(hasRemindered){
                reminderService.updateOrderCloseInfo(orderId,order.getQuarter(),date,Order.ORDER_STATUS_CANCELED);
            }
            // 关闭未发件配件单(驳回，包含未发件的返件单)
            if(condition.getPartsFlag() == 1){
                orderMaterialService.closeMaterialMasterWhenCancel(order.getDataSourceId(),orderId,order.getQuarter(),user,date,StringUtils.left("订单取消自动关闭：" + comment,148));
            }
            //更新网点未完工数量
            ServicePoint servicePoint = condition.getServicePoint();
            if(servicePoint!=null && servicePoint.getId()!=null && servicePoint.getId()>0){
                updateServicePointUnfinishedOrderCount(servicePoint.getId(),-1,"工单取消",orderId,user);
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("客户取消订单");
            processLog.setOrderId(orderId);
            if (StringUtils.isNotBlank(comment)) {
                processLog.setActionComment(String.format("客户取消订单:%s,操作人:%s,备注:%s", order.getOrderNo(), user.getName(), comment));
            } else {
                processLog.setActionComment(String.format("客户取消订单:%s,操作人:%s", order.getOrderNo(), user.getName()));
            }
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_CANCELED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //更改前获得balance
            CustomerFinance finance = customerFinanceDao.getAmounts(condition.getCustomer().getId());
            //customer currency log
            CustomerCurrency currency = new CustomerCurrency();
            //分片根据冻结记录创建时间计算
            currency.setQuarter(QuarterUtils.getSeasonQuarter(date));
            currency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency)); //Production
            currency.setCustomer(condition.getCustomer());
            currency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_OUT);
            currency.setCurrencyNo(order.getOrderNo());
            currency.setBeforeBalance(finance.getBlockAmount());//冻结金额
            currency.setBalance(finance.getBlockAmount() - fee.getBlockedCharge() - fee.getExpectCharge());
            currency.setAmount(0 - fee.getBlockedCharge() - fee.getExpectCharge());
            currency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH);
            currency.setActionType(70);
            currency.setCreateBy(user);
            currency.setCreateDate(date);
            currency.setRemarks(
                    String.format(
                            "订单取消解冻 %.2f元 相关单号为 %s",
                            (fee.getBlockedCharge() + fee.getExpectCharge()),
                            order.getOrderNo()
                    )
            );
//            customerCurrencyDao.insert(currency);


            //fee,退费-扣减冻结金额
            customerFinanceDao.incBlockAmount(condition.getCustomer().getId(), 0l - fee.getBlockedCharge(), 0l - fee.getExpectCharge(), user.getId(), date);

            //调用公共缓存
            OrderCacheUtils.delete(orderId);

            //消息队列
            // 1.异步更新历史资料库
            //      改由job定时将订单转成历史订单 ryan at 2018/01/09
            // 2. 业务绩效报表
            Customer customer = customerService.getFromCache(condition.getCustomer().getId());
            if (customer == null) {
                customer = condition.getCustomer();
            }
            //region B2B消息队列
            if (isCallB2BInterface) {
                //status -> 4
                b2BCenterOrderService.cancelOrder(order, cancelResponsible, date, comment, user, date);
            }
            //endregion B2B消息队列

            //region 网点订单数据更新 2019-03-25
            Long servicePointId = null;
            if(condition.getServicePoint() != null){
                servicePointId = condition.getServicePoint().getId();
            }
            if(servicePointId == null || servicePointId <=0){
                //有过上门服务
                if (condition.getServiceTimes()>0){
                    servicePointId = 1L;
                }
            }
            servicePointOrderBusinessService.orderStatusUpdate(MQOrderServicePointMessage.OperationType.CancelOrder_VALUE,
                    orderId,order.getQuarter(),null,
                    status.getIntValue(),Order.ORDER_SUBSTATUS_CANCELED,-1,
                    false,null,
                    user.getId(),date.getTime());
            //endregion

            // 业务取消订单，如已派单，需发送app通知
            if(isSaleman && condition.getStatusValue() == Order.ORDER_STATUS_PLANNED.intValue()){
                if(condition.getEngineer() != null && condition.getEngineer().getId() != null && condition.getEngineer().getId() >0){
                    //send app notice
                    User engineerAccount = systemService.getUserByEngineerId(condition.getEngineer().getId());
                    if (engineerAccount != null && engineerAccount.getAppLoged() == 1) {
                        orderMQService.noticeEngineerOrderCanceled(user,engineerAccount.getId(),engineerAccount.getName(),engineerAccount.getMobile(),condition.getOrderNo(),(condition.getKefu()==null?"":condition.getKefu().getPhone()));
                    }
                }
            }

            //region 报表微服务消息队列
            try{
                RPTOrderProcessModel orderProcessModel = new RPTOrderProcessModel();
                orderProcessModel.setProcessType(RPTOrderProcessTypeEnum.CANCEL.getValue());
                orderProcessModel.setOrderId(orderId);
                if(condition.getProductCategoryId()!=null && condition.getProductCategoryId()>0){
                    orderProcessModel.setProductCategoryId(condition.getProductCategoryId());
                }
                orderProcessModel.setCustomerId(customer.getId());
                orderProcessModel.setKeFuId(condition.getKefu().getId());
                orderProcessModel.setOrderStatus(Order.ORDER_STATUS_CANCELED);
                if(condition.getCreateDate()!=null){
                    orderProcessModel.setOrderCreateDate(condition.getCreateDate().getTime());
                }
                orderProcessModel.setOrderCloseDate(date.getTime());
                if(order.getDataSourceId()>0){
                    orderProcessModel.setDataSource(order.getDataSourceId());
                }
                if(condition.getServicePoint()!=null && condition.getServicePoint().getId()!=null && condition.getServicePoint().getId()>0){
                    orderProcessModel.setServicePointId(condition.getServicePoint().getId());
                }
                if(condition.getArea() !=null && condition.getArea().getId()!= null && condition.getArea().getId()>0){
                    orderProcessModel.setCountId(condition.getArea().getId());
                }
                orderProcessService.sendRPTOrderProcess(orderProcessModel);
            }catch (Exception e){
                log.error("orderProcessService.sendRPTOrderProcess取消订单发送报表微服务消息队列失败"+e.getMessage());
            }
            //endregion
            //好评单自动取消
            cancelPraseForm(orderStatusFlag,user,1);

            //切分冻结流水
            customerBlockCurrencyService.saveCustomerBlockCurrency(currency);
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            LogUtils.saveLog("取消订单", "OrderService.cancelOrder", orderId.toString(), e, user);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                //redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey,10);
            }
        }
    }

    /**
     * 判断并自动取消好评单
     * @param orderStatusFlag
     */
    private void cancelPraseForm(OrderStatusFlag orderStatusFlag,User user,int cancelOrder){
        if(orderStatusFlag == null){
            return;
        }
        if(orderStatusFlag.getPraiseStatus() >= PraiseStatusEnum.NEW.code && orderStatusFlag.getPraiseStatus() <PraiseStatusEnum.APPROVE.code){
            try {
                Praise praise = new Praise();
                praise.setOrderId(orderStatusFlag.getOrderId());
                praise.setQuarter(orderStatusFlag.getQuarter());
                praise.setStatus(PraiseStatusEnum.CANCELED.code);
                praise.setUpdateById(user.getId());
                praise.setUpdateBy(user.getName());
                praise.setUpdateDt(System.currentTimeMillis());

                PraiseLog praiseLog = new PraiseLog();
                praiseLog.setId(sequenceIdService.nextId());//2020/05/25
                praiseLog.setStatus(praise.getStatus());
                praiseLog.setActionType(PraiseActionEnum.REJECT_TO_CANCELED.code);
                praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
                praiseLog.setContent(cancelOrder==1?"订单取消，[取消]好评单":"订单退单，[取消]好评单");
                praiseLog.setCreatorType(PraiseCreatorTypeEnum.KEFU.code);
                praise.setPraiseLog(praiseLog);
                praise.setRemarks(praiseLog.getContent());
                MSResponse<Integer> msResponse = orderPraiseFeign.cancelled(praise);
                if (!MSResponse.isSuccessCode(msResponse)) {
                    log.error("取消好评单失败:{},orderId:{}", msResponse.getMsg(), orderStatusFlag.getOrderId());
                }
            }catch (Exception pe){
                log.error("取消好评单错误,orderId:{}",orderStatusFlag.getOrderId(),pe);
            }
        }
    }

    //endregion 客户

    //region 订单导入

    /**
     * 客户取消导入订单
     *
     * @param orderId 订单id
     * @param comment 取消说明
     */
    @Transactional(readOnly = false)
    public void cancelTempOrder(Long orderId, User user, String comment) {
        TempOrder o = dao.getTempOrderStatus(orderId);
        if (o.getSuccessFlag() == 1 || o.getDelFlag() == TempOrder.DEL_FLAG_DELETE) {
            throw new OrderException(String.format("该订单已%s", o.getSuccessFlag() == 1 ? "转成正式订单" : "取消"));
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.clear();
        params.put("id", orderId);
        params.put("delFlag", TempOrder.DEL_FLAG_DELETE);
        params.put("errorMsg", comment);
        params.put("updateBy", user);
        params.put("updateDate", new Date());
        dao.updateTempOrder(params);
    }

    //endregion

    //region 问题反馈

    /**
     * 同步订单消息提醒统计数量
     */
    public void reloadNoticeMessage() {
        log.info("服务启动任务：问题反馈 ");
        StringBuffer sb = new StringBuffer(200);

        //1.kefu
        log.info("服务启动任务：客服问题反馈 ");
        //clear db
        redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                connection.flushDb();
                return null;
            }
        });


        //1.1.by customer
        List<NoticeMessageItemVM> items = groupFeedbackByKefuOfCustomer();
        log.info("1.1.For Kefu by Customer:{}", items == null ? 0 : items.size());
        if (items != null && items.size() > 0) {
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    final byte[] feedbackbycustomer = RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] feedbackbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbycustomer = RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);

                    items.forEach(t -> {
                        if (t.getNoticeType() == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
                            connection.hSet(feedbackbycustomer, String.valueOf(t.getCustomerId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        } else if (t.getNoticeType() == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
                            connection.hSet(pendingbycustomer, String.valueOf(t.getCustomerId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }
                    });
                    return null;
                }
            });
            items.clear();
        }
        //1.2.by area
        List<NoticeMessageItemVM> items1 = groupFeedbackByKefuOfArea();
        log.info("1.2.For Kefu by Area:{}", items1 == null ? 0 : items1.size());
        if (items1 != null && items1.size() > 0) {
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    final byte[] feedbackbycustomer = RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] feedbackbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbycustomer = RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbyarea = RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);

                    items1.forEach(t -> {
                        if (t.getNoticeType() == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
                            //System.out.println("feedback area:" + String.valueOf(t.getAreaId()) + " qty:" + String.valueOf(t.getQty()));
                            log.info("feedback area:{} ,qty:{}", t.getAreaId(), t.getQty());
                            connection.hSet(feedbackbyarea, String.valueOf(t.getAreaId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        } else if (t.getNoticeType() == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
                            //System.out.println("pending qty:" + String.valueOf(t.getAreaId()) + " qty:" + String.valueOf(t.getQty()));
                            log.info("pending area:{} ,qty:{}", t.getAreaId(), t.getQty());
                            connection.hSet(pendingbyarea, String.valueOf(t.getAreaId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }
                    });
                    return null;
                }
            });
            items1.clear();
        }

        //2.For customer and sales
        List<NoticeMessageItemVM> items2 = groupFeedbackByCustomer();
        log.info("2.For customer:{}", items2 == null ? 0 : items2.size());
        if (items2 != null && items2.size() > 0) {
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    final byte[] btotal = "total".getBytes(StandardCharsets.UTF_8);
                    items2.forEach(t -> {
                        if (t.getNoticeType() == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
                            final byte[] bkey = String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, t.getCustomerId()).getBytes(StandardCharsets.UTF_8);
                            //total
                            connection.hSet(bkey, String.valueOf(t.getCreateBy()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                            connection.hIncrBy(bkey, btotal, t.getQty());
                        } else if (t.getNoticeType() == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
                            final byte[] bkey = String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, t.getCustomerId()).getBytes(StandardCharsets.UTF_8);
                            connection.hSet(bkey, String.valueOf(t.getCreateBy()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                            //total
                            connection.hIncrBy(bkey, btotal, t.getQty());
                        }
                    });
                    return null;
                }
            });
            items2.clear();
        }


        log.info("服务启动任务：APP异常 ");
        //只有客服和客服主管使用
        //1.by customer
        List<NoticeMessageItemVM> items3 = groupAppAbnormalyByKefuOfCustomer();
        log.info("1.By customer:{}", items3 == null ? 0 : items3.size());
        if (items3 != null && items3.size() > 0) {
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    final byte[] bkey = RedisConstant.MS_APP_ABNORMALY_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    items3.forEach(t -> {
                        connection.hSet(bkey, String.valueOf(t.getCustomerId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                    });
                    return null;
                }
            });
        }
        //2.by area
        List<NoticeMessageItemVM> items4 = groupAppAbnormalyByKefuOfArea();
        log.info("2.By area:{}", items4 == null ? 0 : items4.size());
        if (items4 != null && items4.size() > 0) {
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    final byte[] bkey = RedisConstant.MS_APP_ABNORMALY_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    items4.forEach(t -> {
                        connection.hSet(bkey, String.valueOf(t.getAreaId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                    });
                    return null;
                }
            });
            items4.clear();
        }
    }

    /**
     * 处理反馈异常
     *
     * @param orderId 订单id
     */
    @Transactional(readOnly = false)
    public void feedbackHandled(Long orderId, String quarter, User user) {
        //if (StringUtils.isBlank(quarter)) {
        //    quarter = getOrderQuarterFromCache(orderId);
        //}
        Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单失败");
        }
        int orgReplayKefu = order.getOrderCondition().getReplyFlagKefu();
        int orgReplyCustomer = order.getOrderCondition().getReplyFlagCustomer();
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isNoneBlank(quarter)) {
            map.put("quarter", quarter);
        }
        map.put("orderId", orderId);
        map.put("updateBy", user);
        map.put("updateDate", new Date());
        if (user.isCustomer() || user.isSaleman()) {
            map.put("replyFlagKefu", 0);
        } else {
            map.put("replyFlagCustomer", 0);
        }
        dao.updateCondition(map);
        //cache
        map.clear();
        if (user.isCustomer()) {
            map.put("replyFlagKefu", 0);
        } else {
            map.put("replyFlagCustomer", 0);
        }
        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(orderId)
                .setExpireSeconds(0L);
        if (user.isCustomer()) {
            builder.setReplyFlagKefu(0);
        } else {
            builder.setReplyFlagCustomer(0);
        }
        OrderCacheUtils.update(builder.build());


        // message
        OrderCondition condition = order.getOrderCondition();
        Long cid = condition.getCustomer().getId();
        try {
            if (user.isCustomer() || user.isSaleman()) {
                if (orgReplayKefu == 1) {
                    //total -1
                    //createby -1
                    redisUtils.hIncrByFields(
                            RedisConstant.RedisDBType.REDIS_MS_DB,
                            String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, cid),
                            new String[]{"total", condition.getCreateBy().getId().toString()},
                            -1l
                    );
                }
            } else {
                //客服问题反馈处理，递减
                User kefu = condition.getKefu();
                if (kefu == null) {
                    return;
                }
                if (!user.isKefu() && !user.isKefuLeader()) {
                    return;
                }
                if (orgReplyCustomer == 1) {
                    //当前帐号是客服或客服主管
                    if (user.getCustomerIds() != null && user.getCustomerIds().contains(cid)) {//by customer
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,//key
                                cid.toString(), //field
                                -1l //-1
                        );
                    } else { // by area
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA, //key
                                condition.getArea().getId().toString(),//field
                                -1l // -1
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OrderService.feedbackHandled] orderId:{}", orderId, e);
        }
    }

    //endregion

    //region 安维

    /**
     * 安维人员接单操作
     * 接单+派单，
     * 回收单，再次接单，不变更客服及报表统计数据
     *
     * @param orderId
     */
    @Transactional(readOnly = false)
    public void engineerAcceptOrder(Long orderId, String quarter) {
        User user = UserUtils.getUser();
        if (!user.isEngineer()) {
            throw new OrderException("登录用户不是安维人员，不能在安维接单列表接单");
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }

        try {
            //判断是否可以接单
            Engineer engineer = servicePointService.getEngineerFromCache(user.getCompany().getId(), user.getId());
            if (engineer.getAppFlag() == 0) {
                throw new OrderException("安维没有独立接单的权限");
            }

            Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null) {
                throw new OrderException("读取订单信息失败");
            }
            if (!order.canAccept()) {
                throw new OrderException("该订单不能接单，请刷新页面查看订单是否已取消或其他人员已接单。");
            }

            // 检查是否有订单明细
            if (order.getItems() == null || order.getItems().size() == 0) {
                throw new OrderException("该订单不能接单，订单无服务内容。");
            }

            //回收单的客服不变，否则报表统计不准确，同一单出现累加多次情况
            //因此，增加客服判断逻辑
            Boolean hasKefu = true;
            User kefu = order.getOrderCondition().getKefu();
            OrderCondition condition = order.getOrderCondition();
            int dataSourceId = order.getDataSourceId();
            // 因下单时分配客服，因此此处逻辑可忽略
            if (kefu == null || kefu.getId() == null || Objects.equals(kefu.getId(), 0l)) {
                hasKefu = false;
                // 接单时设置客服
                // 随机读
                Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(condition.getArea().getId());
                Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
                Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
                kefu = getRandomKefu(condition.getCustomer().getId(), condition.getArea().getId(),condition.getProductCategoryId(),condition.getKefuType(),city.getId(),province.getId());
                if (kefu != null) {
                    condition.setKefu(kefu);
                } else {//无客服
                    throw new OrderException("此区域暂未分配跟进客服，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
                }
            }
            // END

            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_PLANNED), "order_status", "已派单");//切换为微服务
            Dict nstatus = new Dict(Order.ORDER_STATUS_PLANNED, label);
            ServicePoint servicePoint = servicePointService.getFromCache(engineer.getServicePoint().getId());

            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            //status
            OrderStatus orderStatus = order.getOrderStatus();
            orderStatus.setAcceptDate(date);
            orderStatus.setPlanBy(user);
            orderStatus.setPlanDate(date);
            orderStatus.setPlanComment("");
            params.put("quarter", order.getQuarter());
            params.put("acceptDate", date);
            params.put("planBy", user);
            params.put("planDate", date);
            params.put("planComment", "");
            params.put("orderId", orderId);
            dao.updateStatus(params);
            //condition
            User euser = new User(engineer.getId());
            euser.setName(engineer.getName());
            euser.setMobile(engineer.getContactInfo());
            condition.setEngineer(euser);
            condition.setServicePoint(engineer.getServicePoint());
            condition.setStatus(nstatus);
            condition.setUpdateBy(user);
            condition.setUpdateDate(date);

            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", nstatus);
            if (!hasKefu) {
                params.put("kefu", condition.getKefu());
            }
            params.put("servicepoint", condition.getServicePoint());
            params.put("engineer", euser);
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);
            //fee
            OrderFee fee = order.getOrderFee();
            fee.setEngineerPaymentType(servicePoint.getFinance().getPaymentType());

            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("engineerPaymentType", fee.getEngineerPaymentType());
            dao.updateFee(params);

            //log,接单

            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("安维接单");
            processLog.setOrderId(orderId);
            processLog.setActionComment(String.format("安维接单:%s,操作人:%s", order.getOrderNo(), engineer.getName()));
            processLog.setStatus("已接单");
            processLog.setStatusValue(Order.ORDER_STATUS_ACCEPTED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(dataSourceId);
            saveOrderProcessLogNew(processLog);
            //log,派单
            processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("派单-安维接单自动派单");
            processLog.setOrderId(orderId);
            processLog.setActionComment(String.format("派单-安维接单自动派单:%s,操作人:%s", order.getOrderNo(), engineer.getName()));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(nstatus.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_PLANNED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(DateUtils.addMilliseconds(date, 10));
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(dataSourceId);
            saveOrderProcessLogNew(processLog);
            //更新接单数(网点,安维)
            params.clear();
            params.put("id", engineer.getServicePoint().getId());
            params.put("planCount", 1);//派单数+1
            params.put("updateBy", user);
            params.put("updateDate", date);
            //servicePointDao.updateServicePointByMap(params);  //mark on 2020-1-17   web端去md_servicepoint
            servicePointService.updateServicePointByMap(params); // add on 2019-10-4
            //安维
            params.remove("id");
            params.put("id", engineer.getId());
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            msEngineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务

            //service point planCount+1
            servicePoint.setPlanCount(servicePoint.getPlanCount() + 1);
            //servicePointService.updateServicePointCache(servicePoint); //mark on 2020-1-17   web端去md_servicepoint
            //redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, servicePoint, servicePoint.getId(), 0l);

            //engineer planCount+1
            engineer.setPlanCount(engineer.getPlanCount() + 1);
            if (engineer.getServicePoint() == null) {
                engineer.setServicePoint(servicePoint);
            }
            //servicePointService.updateEngineerCache(engineer);//mark on 2020-1-17   web端去md_servicepoint

            //调用公共缓存
            OrderCacheParam.Builder cacheParam = new OrderCacheParam.Builder();
            cacheParam.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .incrVersion(1L)
                    .setCondition(condition)
                    .setOrderStatus(orderStatus)
                    .setStatus(nstatus)
                    .setFee(fee)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(cacheParam.build());
            //mq
//            if (!hasKefu) {
//                //客服每日接单报表统计数据
//                MQOrderReport.OrderReport message = null;
//                try {
//                    MQOrderReport.OrderReport.Builder builder = MQOrderReport.OrderReport.newBuilder()
//                            .setOrderId(order.getId())
//                            .setOrderType(Order.ORDER_STATUS_ACCEPTED)
//                            .setQty(1)
//                            .setAmount(order.getOrderFee().getExpectCharge())
//                            .setTriggerDate(date.getTime())
//                            .setTriggerBy(user.getId());
//                    MQOrderReport.Kefu mqkefu = MQOrderReport.Kefu.newBuilder()
//                            .setId(condition.getKefu().getId())
//                            .setName(condition.getKefu().getName()).build();
//                    builder.setKefu(mqkefu);
//                    message = builder.build();
//                    orderReportSender.send(message);
//                } catch (Exception e) {
//                    if (message != null) {
//                        OrderReport report = new OrderReport();
//                        report.setOrderId(message.getOrderId());
//                        report.setRetryTimes(0);
//                        report.setStatus(40);
//                        report.setQty(message.getQty());
//                        report.setAmount(message.getAmount());
//                        report.setOrderType(message.getOrderType());
//                        report.setTriggerBy(message.getKefu().getId());
//                        report.setTriggerDate(new Date(message.getTriggerDate()));
//                        try {
//                            mqOrderReportService.insert(report);
//                        } catch (Exception e1) {
//                            log.error("[OrderService.engineerAcceptOrder]save Message fail:{}", e1.getMessage());
//                        }
//                    }
//                }
//            }
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            //异常
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 申请退单（客服操作）
     *
     * @param orderId
     */
    @Transactional(readOnly = false)
    public void returnOrder(Long orderId, Dict responsible, String comment) {
        User user = UserUtils.getUser();
        returnOrderNew(orderId, responsible, "", comment, user);
    }

    @Transactional(readOnly = false)
    public void returnOrderNew(Long orderId, Dict responsible, String verifyCode, String comment, User user) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Order order = getOrderById(orderId, "", OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单信息失败");
            }
            if (!OrderUtils.canReturn(order.getOrderCondition().getStatusValue())) {
                throw new OrderException("该订单不能退单，请刷新页面查看订单订单状态。");
            }
            if(order.getOrderCondition().getPartsFlag() == 1) {
                MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),order.getId(), order.getQuarter());
                if (!MSResponse.isSuccessCode(msResponse)) {
                    throw new OrderException(msResponse.getMsg());
                }
            }
            Date date = new Date();
            StringBuffer commentBuffer = new StringBuffer();
            if (StringUtils.isNotBlank(comment)) {
                commentBuffer.append(comment);
            }
            Dict prevStatus = MSDictUtils.getDictByValue(String.valueOf(order.getOrderCondition().getStatusValue()), "order_status");
            if (prevStatus != null) {
                commentBuffer.append(String.format(",%s%s-%s]", OrderStatus.STATUS_TITLE_IN_CANCEL_APPLY_RESPONSIBLE, prevStatus.getLabel(), prevStatus.getValue()));
            }
            //status
            String label = MSDictUtils.getDictLabel(String.valueOf(Order.ORDER_STATUS_RETURNING), "order_status", "退单申请");//切换为微服务
            Dict status = new Dict(Order.ORDER_STATUS_RETURNING, label);
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("quarter", order.getQuarter());
            params.put("status", status);// 取消
            params.put("cancelSponsor", 2);//发起方，kkl
            params.put("cancelResponsible", Integer.parseInt(responsible.getValue()));//退单类型
            params.put("cancelApplyBy", user);
            params.put("cancelApplyDate", date);
            params.put("cancelApplyComment", commentBuffer.toString());
            params.put("cancelApproveFlag", 0);
            params.put("cancelApproveBy", new User(0l));
            params.put("cancelApproveDate", null);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("orderId", orderId);
            dao.updateStatus(params);

            //condition
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", status);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("subStatus", Order.ORDER_SUBSTATUS_RETURNNING);//Add by Zhoucy
            dao.updateCondition(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("退单申请");
            processLog.setOrderId(orderId);
            StringBuffer actionCommentBuffer = new StringBuffer();
            actionCommentBuffer.append(String.format("退单申请:%s,申请人:%s,退单类型:%s", order.getOrderNo(), user.getName(),responsible.getLabel()));
            if (StringUtils.isNotBlank(comment)) {
                actionCommentBuffer.append(String.format(",备注:%s", comment));
            }
            if (StringUtils.isNotBlank(verifyCode)) {
                actionCommentBuffer.append(String.format(",退单验证码:[%s]", verifyCode));
            }
            processLog.setActionComment(actionCommentBuffer.toString());
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_RETURNING);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //调用公共缓存
            OrderStatus orderStatus = order.getOrderStatus();
            orderStatus.setCancelSponsor(1);//发起人
            orderStatus.setCancelResponsible(responsible);//退单类型
            orderStatus.setCancelApplyBy(user);
            orderStatus.setCancelApplyDate(date);
            orderStatus.setCancelApplyComment(comment);
            orderStatus.setCancelApproveBy(new User(0L));
            orderStatus.setCancelApproveDate(null);
            orderStatus.setCancelApproveFlag(0);//waiting confirm
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .incrVersion(1L)
                    .setStatus(status)
                    .setOrderStatus(orderStatus)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

//            if (order.getDataSource() != null) {
//                b2BCenterOrderService.applyForCancelB2BOrder(order.getDataSource().getIntValue(), order.getWorkCardId(), order.getId(), order.getOrderNo(), user, date, date, comment);
//            }
            if (order.getDataSourceId() != B2BDataSourceEnum.VIOMI.id
                    || (order.getDataSourceId() == B2BDataSourceEnum.VIOMI.id && OrderUtils.checkOrderServiceType(order, OrderUtils.OrderTypeEnum.REPAIRE))) {
                b2BCenterOrderService.applyReturnOrder(order, responsible.getIntValue(), date, comment, verifyCode, user, date);
            }
            //region 网点订单数据更新 2019-03-25
            // pendingType=-1,不变更
            servicePointOrderBusinessService.orderStatusUpdate(MQOrderServicePointMessage.OperationType.OrderReturnRequest_VALUE,
                    orderId,order.getQuarter(),null,
                    status.getIntValue(),Order.ORDER_SUBSTATUS_RETURNNING,-1,false,null,
                    user.getId(),date.getTime());
            //endregion

            //网点未完工数量 -1
            ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
            if(servicePoint!=null && servicePoint.getId()!=null && servicePoint.getId()>0 &&
                    prevStatus!=null && !prevStatus.getIntValue().equals(Order.ORDER_STATUS_APP_COMPLETED)){
                updateServicePointUnfinishedOrderCount(servicePoint.getId(),-1,"退单申请",orderId,user);
            }
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.returnOrder] orderId：{}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    //endregion 安维

    //region 客服回访

    /**
     * 回访失败（客服操作）
     *
     * @param orderId 订单id
     * @param orderNo 订单号
     * @param quarter 订单号
     * @param comment 备注
     * @param status  订单状态
     * @param user    操作人
     */
    @Transactional(readOnly = false)
    public void saveFollowUpFail(Long orderId,String orderNo,String quarter, String comment,Dict status,User user,Long servicePointId) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        boolean isSuccess = true;
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        Date date = new Date();
        try {
            //condition
            HashMap<String, Object> params = Maps.newHashMap();
            params.clear();
            params.put("quarter", quarter);
            params.put("orderId", orderId);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("subStatus", Order.ORDER_SUBSTATUS_FOLLOWUP_FAIL);
            dao.updateCondition(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(quarter);
            processLog.setAction("回访失败");
            processLog.setOrderId(orderId);
            if (StringUtils.isNotBlank(comment)) {
                processLog.setActionComment(String.format("回访失败:%s,操作人:%s,备注:%s", orderNo, user.getName(), comment));
            } else {
                processLog.setActionComment(String.format("回访失败:%s,操作人:%s", orderNo, user.getName()));
            }
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(status.getIntValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            saveOrderProcessLogNew(processLog);
        } catch (OrderException oe) {
            isSuccess = false;
            throw oe;
        } catch (Exception e) {
            isSuccess = false;
            log.error("[saveFollowUpFail] orderId：{}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            //region 网点订单数据更新 2019-03-25
            if(true == isSuccess) {
                servicePointOrderBusinessService.orderStatusUpdate(MQOrderServicePointMessage.OperationType.FollowUpFail_VALUE,
                        orderId, quarter, null,
                        null, Order.ORDER_SUBSTATUS_FOLLOWUP_FAIL, -1,
                        false, null,
                        user.getId(), date.getTime());
            }
            //endregion
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    //endregion 客服回访

    //region 退单

    @Transactional(readOnly = false)
    public void approveReturnOrderNew(Long orderId,String quarter, String comment, User user) {
        approveReturnOrderOperation(orderId, quarter, comment, user, false);
    }

    @Transactional(readOnly = false)
    public void kklApproveReturnB2bOrder(Long orderId,String quarter, String comment, User user) {
        approveReturnOrderOperation(orderId, quarter, comment, user, true);
    }
    /**
     * 客户同意退单申请
     *
     * @param orderId
     * @param comment 说明

     @Transactional(readOnly = false)
     public void approveReturnOrder(Long orderId, String comment) {
     User user = UserUtils.getUser();
     approveReturnOrderNew(orderId, comment, user);
     }*/

    @Transactional(readOnly = false)
    public void approveReturnOrderOperation(Long orderId,String quarter, String comment, User user, boolean isKklApproveReturnB2bOrder) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        Boolean locked = false;
        try {
            Order order = getOrderById(orderId, quarter==null?"":quarter.trim(), OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null || order.getOrderCondition() == null || order.getOrderFee() == null || order.getOrderStatus() == null) {
                throw new OrderException("读取订单失败，请确认订单时候存在.");
            }
            //配件单检查
            MSResponse<String> msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),orderId,order.getQuarter());
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new OrderException(msResponse.getData() + ":" + msResponse.getMsg());
            }
            OrderCondition condition = getOrderConditionFromMasterById(orderId, order.getQuarter());
            if (condition == null) {
                throw new OrderException("读取订单失败，请确认订单时候存在.");
            }
            order.setOrderCondition(condition);
            //检查订单是否已经审核
            if (!order.canApproveReturn()) {
                throw new OrderException(String.format("订单:%s 已通过审核或驳回。", order.getOrderNo()));
            }
            if (isKklApproveReturnB2bOrder && (order.getDataSourceId() == B2BDataSourceEnum.VIOMI.id && order.canApproveReturnB2bOrder() == 0)) {
                throw new OrderException(String.format("云米B2B订单[%s]: 非安装单不允许进行退单审核操作。", order.getOrderNo()));
            }
            String cancelApplyComment = "";
            cancelApplyComment = order.getOrderStatus() != null ? order.getOrderStatus().getCancelApplyComment() : "";
            if (StringUtils.isBlank(cancelApplyComment)) {
                cancelApplyComment = order.getOrderStatus() != null && order.getOrderStatus().getCancelResponsible() != null
                        ? StringUtils.toString(order.getOrderStatus().getCancelResponsible().getLabel()) : "";
            }

            //获得锁
            locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
            if (!locked) {
                throw new OrderException(String.format("订单:%s 正在处理中，请稍候重试，或刷新页面。", order.getOrderNo()));
            }
            //好评单
            OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(orderId, condition.getQuarter());

            OrderFee fee = order.getOrderFee();
            //操作
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(10);
            Dict status = new Dict(String.valueOf(Order.ORDER_STATUS_RETURNED));
            String label = new String("");
            label = MSDictUtils.getDictLabel(status.getValue(), "order_status", "已退单");//切换为微服务
            status.setLabel(label);
            //order
            //condition
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", status);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("closeDate", date);
            params.put("subStatus", Order.ORDER_SUBSTATUS_RETURNED);//Add by Zhoucy
            // 突击单关闭 Add by Ryan
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                params.put("rushOrderFlag", 2);
            }
            dao.updateCondition(params);

            //order status
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("cancelApproveFlag", 1);
            params.put("cancelApproveBy", user);
            params.put("cancelApproveDate", date);
            //close
            params.put("closeFlag", 1);
            params.put("closeBy", user);
            dao.updateStatus(params);

            //关闭突击单
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                crushService.closeOrderCurshByOrderId(orderId,order.getQuarter(),1,null,user,date);
            }
            condition.setRushOrderFlag(2);
            // 关闭催单 2019/08/15
            Boolean hasRemindered = false;
            if(order.getOrderStatus() != null && order.getOrderStatus().getReminderStatus() != null && order.getOrderStatus().getReminderStatus() > 0) {
                hasRemindered = true;
            }
            if(hasRemindered && order.getOrderStatus().getReminderStatus() < ReminderStatus.Completed.getCode()) {
                Long servicePointId = ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
                reminderService.completeReminder(orderId ,order.getQuarter() ,user ,date ,comment, ReminderAutoCloseTypeEnum.OrderReturn,Order.ORDER_STATUS_RETURNED,servicePointId);
            }else if(hasRemindered){
                reminderService.updateOrderCloseInfo(orderId,order.getQuarter(),date,Order.ORDER_STATUS_RETURNED);
            }
            // 关闭未发件配件单(驳回，包含未发件的返件单)
            if(condition.getPartsFlag() == 1){
                orderMaterialService.closeMaterialMasterWhenCancel(order.getDataSourceId(),orderId,order.getQuarter(),user,date,"订单退单审核后自动关闭");
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setOrderId(order.getId());
            processLog.setAction("同意退单申请");
            processLog.setActionComment(String.format("同意退单申请:%s,审核人:%s", order.getOrderNo(), user.getName()));
            processLog.setStatus(label);
            processLog.setStatusValue(Order.ORDER_STATUS_RETURNED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(1);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //更改前获得冻结金额
            CustomerFinance finance = customerFinanceDao.getAmounts(condition.getCustomer().getId());

            //customer currency log
            CustomerCurrency currency = new CustomerCurrency();
            //分片根据冻结记录创建时间计算
            currency.setQuarter(QuarterUtils.getSeasonQuarter(date));
            currency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency)); //Production
            currency.setCustomer(condition.getCustomer());
            currency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_OUT);
            currency.setCurrencyNo(order.getOrderNo());
            currency.setBeforeBalance(finance.getBlockAmount());//冻结金额
            currency.setBalance(finance.getBlockAmount() - fee.getBlockedCharge() - fee.getExpectCharge());
            currency.setAmount(0 - fee.getBlockedCharge() - fee.getExpectCharge());//负数
            currency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH);
            currency.setActionType(70);//冻结
            currency.setCreateBy(user);
            currency.setCreateDate(date);
            currency.setRemarks(
                    String.format(
                            "订单退单解冻 %.2f元 相关单号为 %s",
                            (fee.getBlockedCharge() + fee.getExpectCharge()),
                            order.getOrderNo()
                    )
            );
            //更改客户的冻结金额(block_amount)
            customerFinanceDao.incBlockAmount(condition.getCustomer().getId(), 0 - fee.getBlockedCharge(), 0 - fee.getExpectCharge(), user.getId(), date);

            //调用公共缓存
            OrderCacheUtils.delete(orderId);
            //消息处理
            // 1.取消智能回访
            if (StringUtils.isNoneBlank(siteCode)) {
                Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
                if (taskResult != null && taskResult == 0) {
                    try {
                        stopVoiceOperateMessage(siteCode, order.getId(), order.getQuarter(), user.getName(), date);
                    } catch (Exception e) {
                        log.error("退单审核-停滞智能回访错误:" + order.getId(), e);
                    }
                }//taskResult
            }//site
            // 2. 业务绩效报表+客服日退单报表
            Customer customer = customerService.getFromCache(condition.getCustomer().getId());
            if (customer == null) {
                customer = condition.getCustomer();
            }

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.orderStatusUpdate(MQOrderServicePointMessage.OperationType.ApproveReturnRequest_VALUE,
                    order.getId(),order.getQuarter(),null,
                    status.getIntValue(),Order.ORDER_SUBSTATUS_RETURNED,-1,
                    false,null,
                    user.getId(),date.getTime());
            //endregion

            //region 报表微服务消息队列
            try{
                RPTOrderProcessModel orderProcessModel = new RPTOrderProcessModel();
                orderProcessModel.setProcessType(RPTOrderProcessTypeEnum.RETURN.getValue());
                orderProcessModel.setOrderId(orderId);
                if(condition.getProductCategoryId()!=null && condition.getProductCategoryId()>0){
                    orderProcessModel.setProductCategoryId(condition.getProductCategoryId());
                }
                orderProcessModel.setCustomerId(customer.getId());
                orderProcessModel.setKeFuId(condition.getKefu().getId());
                orderProcessModel.setOrderStatus(Order.ORDER_STATUS_RETURNED);
                if(condition.getCreateDate()!=null){
                    orderProcessModel.setOrderCreateDate(condition.getCreateDate().getTime());
                }
                orderProcessModel.setOrderCloseDate(date.getTime());
                if(order.getDataSourceId()>0){
                    orderProcessModel.setDataSource(order.getDataSourceId());
                }
                if(condition.getServicePoint()!=null && condition.getServicePoint().getId()!=null && condition.getServicePoint().getId()>0){
                    orderProcessModel.setServicePointId(condition.getServicePoint().getId());
                }
                if(condition.getArea() !=null && condition.getArea().getId()!= null && condition.getArea().getId()>0){
                    orderProcessModel.setCountId(condition.getArea().getId());
                }
                orderProcessService.sendRPTOrderProcess(orderProcessModel);
            }catch (Exception e){
                log.error("OrderService.approveReturnOrder退单单发送报表微服务消息队列失败"+e.getMessage());
            }
            //endregion

            //好评单自动取消
            cancelPraseForm(orderStatusFlag,user,0);

            //关闭异常单单
            AbnormalForm abnormalForm = new AbnormalForm();
            abnormalForm.setOrderId(orderId);
            abnormalForm.setCloseBy(user.getId());
            abnormalForm.setCloseAt(date.getTime());
            abnormalForm.setQuarter(order.getQuarter());
            abnormalFormService.closeByOrderId(abnormalForm);

            //切分冻结流水
            customerBlockCurrencyService.saveCustomerBlockCurrency(currency);
            //region B2B消息队列
            if (order.getDataSourceId() == B2BDataSourceEnum.VIOMI.id && OrderUtils.checkOrderServiceType(order, OrderUtils.OrderTypeEnum.REPAIRE)) {
                //云米维修单不需要手动审核
            } else {
                Integer cancelResponsible = order.getOrderStatus().getCancelResponsible() == null || order.getOrderStatus().getCancelResponsible().getValue() == null ? null : StringUtils.toInteger(order.getOrderStatus().getCancelResponsible().getValue());
                b2BCenterOrderService.approveReturnOrder(order, cancelResponsible, date, cancelApplyComment, user, date);
            }
            //endregion B2B消息队列
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.approveReturnOrder] orderId:{}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 5l);//延时5秒
            }
        }

    }

    @Transactional(readOnly = false)
    public void rejectReturnOrderNew(Long orderId,String quarter, String comment, User user) {
        rejectReturnOrderOperation(orderId, quarter, comment, user, false);
    }

    @Transactional(readOnly = false)
    public void kklRejectReturnB2bOrder(Long orderId,String quarter, String comment, User user) {
        rejectReturnOrderOperation(orderId, quarter, comment, user, true);
    }
    /**
     * 客户驳回退单申请
     *
     * @param orderId
     * @param comment 说明

     @Transactional(readOnly = false)
     public void rejectReturnOrder(Long orderId, String comment) {
     User user = UserUtils.getUser();
     rejectReturnOrderNew(orderId, comment, user);
     }*/

    @Transactional(readOnly = false)
    private void rejectReturnOrderOperation(Long orderId,String quarter, String comment, User user, boolean isKklRejectReturnB2bOrder) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Order order = getOrderById(orderId, quarter ==null?"":quarter.trim(), OrderUtils.OrderDataLevel.DETAIL, false);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单失败，请确认订单时候存在.");
            }

            //检查订单是否已审核
            if (!order.canApproveReturn()) {
                throw new OrderException(String.format("订单:%s 不能驳回申请，请查看订单是否已被同意或驳回申请。", order.getOrderNo()));
            }
            if (isKklRejectReturnB2bOrder && !(order.getDataSourceId() == B2BDataSourceEnum.VIOMI.id && order.canApproveReturnB2bOrder() == 1)) {
                throw new OrderException(String.format("云米B2B订单[%s]: 非安装单不允许进行退单驳回操作。", order.getOrderNo()));
            }
            Dict prvStatus = getLastOrderLog(orderId, order.getQuarter(), order.getOrderCondition().getStatus().getValue());
            if (prvStatus == null) {
                throw new OrderException(String.format("查找订单:" + order.getOrderNo() + "申请前状态错误。"));
            }

            OrderCondition condition = order.getOrderCondition();

            //操作
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            boolean resetAppointmentDate = false;
            //Add by Zhoucy
            Integer subStatus = Order.ORDER_SUBSTATUS_NEW;
            //没有服务网点
            if (condition.getServicePoint() == null || condition.getServicePoint().getId() == null || condition.getServicePoint().getId() == 0) {
                subStatus = Order.ORDER_SUBSTATUS_NEW;
            }
            //有服务网点，并且有上门服务项
            else if (order.getDetailList() != null && order.getDetailList().size() > 0) {
                subStatus = Order.ORDER_SUBSTATUS_SERVICED;
                params.put("pendingType", new Dict(0, ""));
            } else {
                subStatus = Order.ORDER_SUBSTATUS_PLANNED;
                params.put("pendingType", new Dict(0, ""));
                resetAppointmentDate = true;
                params.put("resetAppointmentDate", resetAppointmentDate);
            }

            //order
            //condition
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", prvStatus);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("subStatus", subStatus); //Add by Zhoucy
            params.put("pendingTypeDate", date); //Add by Zhoucy
            params.put("reservationDate", date); //Add by Zhoucy
            dao.updateCondition(params);

            //order status
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("updateBy", user);
            dao.rejectCancel(params);

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setOrderId(order.getId());
            processLog.setAction("驳回退单申请");
            processLog.setActionComment(String.format("驳回退单申请:%s,审核人:%s", order.getOrderNo(), user.getName()));
            processLog.setStatus(prvStatus.getLabel());
            processLog.setStatusValue(Integer.parseInt(prvStatus.getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //调用公共缓存
            OrderStatus orderStatus = order.getOrderStatus();
            orderStatus.setCancelSponsor(0);//发起人
            orderStatus.setCancelResponsible(new Dict("0"));//责任方/退单类型
            orderStatus.setCancelApplyBy(new User(0l));
            orderStatus.setCancelApplyDate(null);
            orderStatus.setCancelApplyComment("");
            orderStatus.setCancelApproveBy(new User(0l));
            orderStatus.setCancelApproveDate(null);
            orderStatus.setCancelApproveFlag(2);//reject
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setStatus(prvStatus)
                    .setOrderStatus(orderStatus)
                    .incrVersion(1L)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.orderStatusUpdate(MQOrderServicePointMessage.OperationType.RejectReturnRequest_VALUE,
                    orderId,order.getQuarter(),null,
                    prvStatus.getIntValue(),subStatus, 0,
                    resetAppointmentDate,date.getTime(),
                    user.getId(),date.getTime());
            //endregion

            //网点未完工数量+1
            ServicePoint servicePoint = condition.getServicePoint();
            if(servicePoint!=null && servicePoint.getId()!=null && servicePoint.getId()>0 && !prvStatus.getIntValue().equals(Order.ORDER_STATUS_APP_COMPLETED)){
                updateServicePointUnfinishedOrderCount(servicePoint.getId(),1,"驳回退单申请",orderId,user);
            }
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.rejectReturnOrder] orderId:{}", orderId, e);
            //LogUtils.saveLog("驳回退单申请", "OrderService.rejectReturnOrder", orderId.toString(), e, user);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    //endregion 退单

    //region 客评

    /**
     * 读取客评项目 (for 订单)
     *
     * @return
     */
    public List<OrderGrade> getToOrderGrade() {
        /*
        // mark on 2020-1-7  begin
        List<OrderGrade> gradeList;
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_ORDER_GRADE)) {
            gradeList = redisUtils.getList(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_ORDER_GRADE, OrderGrade[].class);
        } else {
            //gradeList = dao.getToOrderGrade(); mark on 2019-10-8
            //调用微服务 start 2019-10-8
            gradeList = msGradeService.getToOrderGrade();
            // end
            redisUtils.set(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_ORDER_GRADE, gradeList, 0l);
        }
        return gradeList;
        // mark on 2020-1-7  end
        */
        return msGradeService.getToOrderGrade(); // add on 2020-1-7
    }

    public OrderGradeModel getOrderGrade(OrderCondition orderCondition) {
        OrderGradeModel gradeModel = new OrderGradeModel();
        if (orderCondition == null || orderCondition.getOrderId() == null) {
            return gradeModel;
        }
        List<OrderGrade> gradeList;
        if (orderCondition.getGradeFlag() > 0) {
            //已评
            //gradeList = dao.getOrderGradeByOrderId(orderCondition.getOrderId(), orderCondition.getQuarter()); //mark on 2020-1-7
            gradeList = getOrderGradeByOrderId(orderCondition.getOrderId(), orderCondition.getQuarter());   //add on 2020-1-7
        } else {
            gradeList = getToOrderGrade();
        }
        gradeModel.setOrderNo(orderCondition.getOrderNo());
        gradeModel.setOrderId(orderCondition.getOrderId());
        gradeModel.setQuarter(orderCondition.getQuarter());
        Engineer engineer = new Engineer();
        engineer.setId(orderCondition.getEngineer().getId());
        engineer.setName(orderCondition.getEngineer().getName());
        gradeModel.setEngineer(engineer);
        gradeModel.setServicePoint(orderCondition.getServicePoint());
        gradeModel.setGradeList(gradeList == null ? Lists.newArrayList() : gradeList);
        return gradeModel;
    }

    public List<OrderGrade> getOrderGradeByOrderId(Long orderId,  String quarter) {
        // add on 2020-1-7 //去除md_grade_item
        List<OrderGrade> orderGradeList = dao.getOrderGradeByOrderId(orderId, quarter);
        if (org.springframework.util.ObjectUtils.isEmpty(orderGradeList)) {
            return orderGradeList;
        }

        List<Grade> gradeList = msGradeService.findAllGradeListFromCache();
        List<GradeItem> gradeItemList = Lists.newArrayList();
        if (!org.springframework.util.ObjectUtils.isEmpty(gradeList)) {
            gradeList.stream().forEach(r->gradeItemList.addAll(r.getItemList()));
        }

        if (!org.springframework.util.ObjectUtils.isEmpty(gradeItemList)) {
            orderGradeList.stream().forEach(r->{
                if (!org.springframework.util.ObjectUtils.isEmpty(r.getItems())) {
                    List<Long> itemIds = r.getItems().stream().map(GradeItem::getId).collect(Collectors.toList());
                    r.setItems(gradeItemList.stream().filter(x->itemIds.contains(x.getId())).collect(Collectors.toList()));
                }
            });
        }
        return orderGradeList;
    }


    /**
     * 保存客评，并更改订单状态为完成
     * gradeModel.order尽量在调用方传递，避免此方法读取数据库
     *
     * Modify: ryan ,at 2018-07-07
     * 只要设定加急等级，就计费，不按服务类型区分
     * Modify: ryan 2019-06-13 23:08
     * 有未关闭的配件单，同时关闭
     * Modify: ryan 2019-08-15
     * 自动关闭未完成催单,在"订单客评消息消费者/OrderGradeMessageReceiver"中处理
     * Modify: ryan 2020-03-30
     * 1.客评不检查是否自动对账
     * 2.发送好评自动审核消息队列(延迟5秒)
     * Modify: ryan 2021-05-20
     * 增加快可立时效文本日志输出
     */
    @Transactional(readOnly = false)
    public void saveGrade(OrderGradeModel gradeModel,OrderStatusFlag orderStatusFlag, User user, String title, Integer manuTimeLinessLevel) {
        Boolean saveSuccess = false;
        if (gradeModel == null || gradeModel.getOrderId() == null) {
            throw new OrderException("参数错误");
        }
        Long orderId = gradeModel.getOrderId();
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        Order order = null;
        Date date = new Date();
        boolean autoReviewPraise = false;
        try {
            if (null == gradeModel.getOrder()) {
                order = getOrderById(orderId, StringUtils.isBlank(gradeModel.getQuarter()) ? "" : gradeModel.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true, true,false,true);
            } else {
                order = gradeModel.getOrder();
            }
            if (order == null || order.getOrderCondition() == null || order.getOrderFee() == null) {
                throw new OrderException("读取客评订单信息失败。");
            }
            if (!order.canGrade()) {
                throw new OrderException("不能保存客评，请刷新订单列表。订单可能已完成或退单。");
            }
            OrderCondition condition = order.getOrderCondition();
            int dataSource = order.getDataSourceId();
            int preStatus = condition.getStatusValue();
            if (dataSource == B2BDataSourceEnum.VIOMI.getId()) {
                if (StringUtils.isEmpty(condition.getAppCompleteType())) {
                    throw new OrderException("此订单需先[完工]，然后方可[客评]。");
                }
            } else {
                //其他数据源,按品类检查App完工开关
                int appCompleteFlag = Optional.ofNullable(condition.getProductCategory()).map(p->p.getAppCompleteFlag()).orElse(0);
                int status = condition.getStatusValue();
                if(appCompleteFlag == 1 && status != Order.ORDER_STATUS_APP_COMPLETED ){
                    throw new OrderException("此订单需先[完工]，然后方可[客评]。");
                }
            }
            //判断预约日期不能超过客评日期
            Date startData = DateUtils.getEndOfDay(date); //获取当天的结束时间
            if(order.getOrderCondition().getAppointmentDate()!=null && order.getOrderCondition().getAppointmentDate().getTime()>startData.getTime()){
                throw new OrderException("预约日期不能超过客评日期,请重新预约");
            }
            // 根据订单配件状态检查是否可以客评 2019/06/13 22:52 at home
            MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),orderId,order.getQuarter());
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new OrderException(msResponse.getMsg());
            }
            //检查当前网点是否有上门服务 2018/06/11
            List<OrderDetail> details = order.getDetailList();
            if (CollectionUtils.isEmpty(details)) {
                throw new OrderException("此订单无上门服务，不能客评。");
            }

            Date appCompleteDate = new Date();
            Long sid = condition.getServicePoint().getId();
            ServicePoint servicePoint = condition.getServicePoint();

            long cnt = details.stream().filter(t -> t.getServicePoint().getId().longValue() == sid.longValue() && t.getDelFlag() == OrderDetail.DEL_FLAG_NORMAL)
                    .count();
            if (cnt == 0) {
                throw new OrderException("当前网点无上门服务，不能客评，请确认！");
            }
            if (gradeModel.getCheckOrderFee()) {
                Boolean checkFee = checkOrderFeeAndServiceAmountBeforeGrade(order, true);
                if (!checkFee) {
                    throw new OrderException("此订单金额异常不能客评，请联系管理员。");
                }
            }
            //engineer
            Engineer engineer = gradeModel.getEngineer();
            gradeModel.setEngineer(engineer);
            gradeModel.setServicePoint(servicePoint);

            //根据上门服务来判断是否是安装，只要上门服务有安装，就算安装
            boolean installService = details.stream().filter(t -> t.getDelFlag() == 0 && t.getServiceType().getId().longValue() == 1).count() > 0;
            //region 时效

            //1.网点时效(应付，快可立补贴)
            double timeLinessCharge = 0.0;
            Date timeLinessStartDate = getServicePointTimeLinessStartDate(order, orderId, order.getQuarter(), sid);
            if (timeLinessStartDate == null) {
                throw new OrderException("读取网点派单日期失败，请重试。");
            }
            Order.TimeLinessType timeLinessType = Order.TimeLinessType.HOURS;
            Dict timeLinessInfo = null;
            Double hours = 0.0;
            long productCategoryId = Optional.ofNullable(condition.getProductCategoryId()).orElse(0L);
            // (网点开关打开，且是保内安装单)
            Boolean repeateTimelinessFee = false;//重复时效费检查
            Date beginDate = null;
            Boolean recordTimelinessLog = false;//是否记录时效计算日志
            if (servicePoint.getTimeLinessFlag() == 1 && installService) {
                timeLinessType = Order.TimeLinessType.ALL;
                //重复时效费检查
                beginDate = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
                Date endDate = new DateTime().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999).toDate();
                repeateTimelinessFee = checkRepeateTimeLinessFee(beginDate,endDate,condition.getCustomer().getId(),sid,productCategoryId,condition.getPhone1());
                if(repeateTimelinessFee){
                    recordTimelinessLog = true;
                    timeLinessType = Order.TimeLinessType.HOURS;
                }
            }
            try {
                timeLinessInfo = getServicePointTimeLinessInfo(timeLinessType, condition, timeLinessStartDate, productCategoryId, manuTimeLinessLevel);
            } catch (Exception e) {
                log.error("计算网点时效费用错误 - orderId:{}, timeLinessType:{} , timeLinessStartDate:{} ,productCategoryId:{}, manuTimeLinessLevel:{}"
                        ,orderId
                        ,timeLinessType.name
                        ,DateUtils.formatDate(timeLinessStartDate,"yyyy-MM-dd HH:mm:ss")
                        ,productCategoryId
                        ,manuTimeLinessLevel
                );
                throw new OrderException("计算网点时效费用错误：" + e.getMessage(), e);
            }
            if (timeLinessInfo != null) {
                hours = Double.valueOf(timeLinessInfo.getType());
                timeLinessCharge = Double.valueOf(timeLinessInfo.getLabel());
            }
            //增加时效启用时间判断
            Date timelinessEffectiveDate = null;
            String timelinessEffectiveDateDict = MSDictUtils.getDictSingleValue("order_timeliness_effective_date", "2021-05-19");
            if(StrUtil.isNotEmpty(timelinessEffectiveDateDict)){
                timelinessEffectiveDate = DateUtils.parseDate(timelinessEffectiveDateDict);
            }
            boolean resetTimeLineessCharge = false;//不支付网点时效费
            if(servicePoint.getTimeLinessFlag() == 0 || !installService){
                recordTimelinessLog = true;
            }
            //如果时效开始时间早于数据字典中设定的时效补贴启用日期，不补贴网点费用
            if(timelinessEffectiveDate != null && timelinessEffectiveDate.getTime() > timeLinessStartDate.getTime()){
                timeLinessCharge = 0.0;
                resetTimeLineessCharge = true;
                recordTimelinessLog = true;
            }
            if(recordTimelinessLog) {
                StringBuffer timelinessCheck = new StringBuffer(256);
                timelinessCheck.append("OrderId:").append(orderId).append(" ,timeLinessStartDate:").append(DateUtils.formatDate(timeLinessStartDate, "yyyy-MM-dd HH:mm:ss"));
                timelinessCheck.append(" ,时效生效日期：").append(StrUtil.trimToEmpty(timelinessEffectiveDateDict));
                timelinessCheck.append(" ,订单创建时间:").append(DateUtils.formatDate(condition.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
                if (servicePoint.getTimeLinessFlag() == 0) {
                    timelinessCheck.append(" ,网点快可立时效开关:关闭");
                }
                if (!installService) {
                    timelinessCheck.append(" ,工单类型:非安装单");
                }
                timelinessCheck.append(" ,timeLinessType:").append(timeLinessType.name).append(" ,客服选择的时效：").append(manuTimeLinessLevel);
                if(repeateTimelinessFee) {
                    timelinessCheck.append(" ,重复时效费检查[结果: 重复, 检查日期:")
                            .append(DateUtils.formatDate(beginDate, "yyyy-MM-dd HH:mm:ss"))
                            .append(", cid:").append(condition.getCustomer().getId())
                            .append(", sid:").append(sid)
                            .append(", categoryId：").append(productCategoryId)
                            .append("phone：").append(condition.getPhone1()).append(" ]");
                }
                if (timeLinessInfo != null) {
                    timelinessCheck.append(" ,timeLinessInfo [ 用时:").append(timeLinessInfo.getType()).append(", 等级:").append(timeLinessInfo.getValue()).append(",金额:").append(timeLinessInfo.getLabel()).append(" ]");
                }

                if(resetTimeLineessCharge) {
                    timelinessCheck.append(" ,时效开始时间早于生效日期，时效费用清零");
                }
                log.error(timelinessCheck.toString());
                timelinessCheck.setLength(0);
            }
            //时效
            gradeModel.setTimeLiness(hours);//*

            //2.客户时效(应收),网点时效(应付，客户出费用)
            Dict customerTimeLinessInfo = null;
            double customerTimeLinessCharge = 0.0;//客户时效补贴(快可立应收)
            double subsidyTimeLinessCharge = 0.0;//客户时效补贴(网点应收)
            Double customerHours = 0.0;//客户时效(计时)(单位：小时)
            Order.TimeLinessType customerTimeLinessType = Order.TimeLinessType.HOURS;
            //客户时效开关开启，网点客户时效开关也开启，且是保内安装单，计算费用
            if (!repeateTimelinessFee && installService && condition.getCustomer().getTimeLinessFlag() == 1 && servicePoint.getCustomerTimeLinessFlag() == 1) {
                customerTimeLinessType = Order.TimeLinessType.ALL;
            }

            try {
                customerTimeLinessInfo = getCustomerTimeLinessInfo(customerTimeLinessType, condition);
            } catch (Exception e) {
                throw new OrderException("计算客户时效费用错误：" + e.getMessage(), e);
            }
            if (customerTimeLinessInfo != null) {
                customerHours = Double.valueOf(customerTimeLinessInfo.getType());
                customerTimeLinessCharge = Double.valueOf(customerTimeLinessInfo.getLabel());//应收
                subsidyTimeLinessCharge = Double.valueOf(customerTimeLinessInfo.getRemarks());//应付
            }
            //3.费用处理规则
            com.kkl.kklplus.entity.common.NameValuePair<Double,Double> timelineFinalCharge = getServicePointTimeLinessCharge(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge);
            timeLinessCharge = timelineFinalCharge.getName();
            subsidyTimeLinessCharge = timelineFinalCharge.getValue();

            //endregion 时效

            OrderStatus orderStatus = order.getOrderStatus();
            OrderFee orderFee = order.getOrderFee();

            //region 加急费,只要设定加急就计费
            // 客户选择了加急等级才计算费用
            // 用时= now/appCompleteDate - (加急日期和到货日期两者日期最大的)
            UrgentChargeModel urgentChargeModel = new UrgentChargeModel();
            Double urgentHours = 0.0;
            Date urgentDate = orderStatus.getUrgentDate();
            if (condition.getUrgentLevel().getId().longValue() > 0) {
                Date arrivalDate = order.getOrderCondition().getArrivalDate();
                //如到货日期比加急日期晚，已到货日期为准
                if (arrivalDate != null && DateUtils.pastMinutes(urgentDate, arrivalDate) > 0) {
                    urgentDate = arrivalDate;
                }
                //如果是app完成(compeled_all,compeled_all_notest,compeled_maintain)，结束时间已app完成为准
                //否则已当前时间为准
                Set<String> appCompletSet = Sets.newHashSet("compeled_all", "compeled_all_notest", "compeled_maintain");
                if (condition.getAppCompleteDate() != null && appCompletSet.contains(condition.getAppCompleteType().toLowerCase().trim())) {
                    appCompleteDate = condition.getAppCompleteDate();
                }
                Long minutes = DateUtils.pastMinutes(urgentDate, appCompleteDate);
                if (minutes > 0) {
                    urgentHours = minutes / 60.0;
                }
                //根据选择的加急等级进行判断
                List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
                if (urgentLevels == null || urgentLevels.size() == 0) {
                    throw new OrderException("未设定加急费标准!");
                }

                UrgentLevel urgentLevel = urgentLevels.stream().filter(t -> t.getId().longValue() == condition.getUrgentLevel().getId().longValue()).findFirst().orElse(null);
                if (urgentLevel == null) {
                    throw new OrderException("加急标准不存在!");
                } else {
                    List<String> ranges = Splitter.onPattern("[~|-]")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(urgentLevel.getLabel());
                    if (ranges.size() == 2) {
                        if (urgentHours <= Double.valueOf(ranges.get(1))) {
                            //小于等于上限,符合加急条件，取下单/修改订单/修改加急时的费用，不需要重新取值
                            urgentChargeModel = new UrgentChargeModel();
                            urgentChargeModel.setChargeIn(orderFee.getCustomerUrgentCharge());
                            urgentChargeModel.setChargeOut(orderFee.getEngineerUrgentCharge());
                        }
                    } else {
                        throw new OrderException("加急等级描述格式错误!");
                    }
                }
            }
            //endregion 加急费

            //汇总总分
            List<OrderGrade> gradeList = gradeModel.getGradeList();
            Integer point = 0;
            if (gradeList != null && gradeList.size() > 0) {
                point = gradeList.stream().map(t -> t.getPoint()).reduce(0, Integer::sum);
            }
            gradeModel.setQuarter(order.getQuarter());
            gradeModel.setOrderNo(order.getOrderNo());
            gradeModel.setCreateBy(user);
            gradeModel.setCreateDate(date);
            gradeModel.setPoint(point);//* 2018/10/09

            //统计订单上门服务中数据，包含产品，产品类别，服务，套组
            String statusLabel = MSDictUtils.getDictLabel("80", "order_status", "完成 ");//切换为微服务
            Dict status = new Dict("80", statusLabel);

            //应收加急费汇总到orderCharge与expectCharge比较，用于判断是否能自动对账
            //时效费不计入
            if (urgentChargeModel.getChargeIn() > 0) {
                orderFee.setOrderCharge(orderFee.getOrderCharge() + urgentChargeModel.getChargeIn());
            }
            /*判断是否可以自动产生客户对账单
            canAutoCharge = gradeModel.getCanAutoCharge();
            if (gradeModel.getCheckCanAutoCharge()) {
                canAutoCharge = compareOrderDetail(order);
            }*/

            //region entry

            //condition
            HashMap<String, Object> conditionParams = Maps.newHashMap();
            condition.setCloseDate(date);
            // 客评类型 2019/01/22
            if (gradeModel.getAutoGradeFlag() == null || gradeModel.getAutoGradeFlag() == OrderUtils.OrderGradeType.NO_GRADE.getValue()) {
                condition.setGradeFlag(OrderUtils.OrderGradeType.MANUAL_GRADE.getValue());//人工客评
            } else {
                condition.setGradeFlag(gradeModel.getAutoGradeFlag());//自动客评 *
            }
            condition.setStatus(status);
            condition.setUpdateBy(user);
            condition.setUpdateDate(date);
            condition.setPendingFlag(2);
            condition.setPendingType(new Dict("0", ""));
            /*
            if (canAutoCharge) {
                condition.setAutoChargeFlag(3);//自动对账中
                conditionParams.put("autoChargeFlag", 3);
            }*/
            conditionParams.put("quarter", order.getQuarter());
            conditionParams.put("orderId", orderId);
            conditionParams.put("status", status);
            conditionParams.put("closeDate", condition.getCloseDate());
            conditionParams.put("gradeFlag", condition.getGradeFlag());// 客评类型
            conditionParams.put("pendingFlag", condition.getPendingFlag());
            conditionParams.put("pendingType", condition.getPendingType());
            conditionParams.put("updateBy", user);
            conditionParams.put("updateDate", date);
            //时效-网点(应付)
            conditionParams.put("timeLiness", hours);
            //网点金额汇总
            if (timeLinessCharge != 0) {
                conditionParams.put("timeLinessCharge", timeLinessCharge);//update engineerTotalCharge
            }
            if (subsidyTimeLinessCharge != 0) {
                conditionParams.put("subsidyTimeLinessCharge", subsidyTimeLinessCharge);//update engineerTotalCharge
            }
            //时效-客户(应收)金额汇总
            if (customerTimeLinessCharge != 0) {
                conditionParams.put("customerTimeLinessCharge", customerTimeLinessCharge);//update orderCharge
            }
            //加急
            //应收
            if (urgentChargeModel.getChargeIn() > 0) {
                conditionParams.put("customerUrgentCharge", urgentChargeModel.getChargeIn());//update orderCharge
            }
            //应付
            if (urgentChargeModel.getChargeOut() > 0) {
                conditionParams.put("engineerUrgentCharge", urgentChargeModel.getChargeOut());//update engineerTotalCharge
            }
            conditionParams.put("subStatus", Order.ORDER_SUBSTATUS_COMPLETED);//Add by Zhoucy
            condition.setSubStatus(Order.ORDER_SUBSTATUS_COMPLETED);//2019/02/13
            // 突击单关闭 Add by Ryan
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                conditionParams.put("rushOrderFlag", 2);
            }

            //OrderFee.timeLinessCharge and engineerTotalCharge
            HashMap<String, Object> feeParams = Maps.newHashMap();
            if (timeLinessCharge != 0 || customerTimeLinessCharge != 0 || subsidyTimeLinessCharge != 0 || customerHours != 0) {
                feeParams.put("quarter", order.getQuarter());
                feeParams.put("orderId", orderId);
                //时效-网点
                if (timeLinessCharge != 0) {
                    feeParams.put("timeLinessCharge", timeLinessCharge);//时效奖励(快可立补贴)
                    orderFee.setTimeLinessCharge(timeLinessCharge);
                    orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge()+timeLinessCharge);
                }
                if (subsidyTimeLinessCharge != 0) {
                    feeParams.put("subsidyTimeLinessCharge", subsidyTimeLinessCharge);//时效费(客户补贴)
                    orderFee.setSubsidyTimeLinessCharge(subsidyTimeLinessCharge);
                    orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge()+subsidyTimeLinessCharge);
                }
                //时效-客户(应收)
                if (customerHours != 0) {
                    feeParams.put("customerTimeLiness", customerHours);//应收 18/06/04
                    feeParams.put("customerTimeLinessCharge", customerTimeLinessCharge);//应收 18/06/04
                    orderFee.setCustomerTimeLinessCharge(customerTimeLinessCharge);
                    orderFee.setOrderCharge(orderFee.getOrderCharge()+customerTimeLinessCharge);
                }
                //加急费
                feeParams.put("customerUrgentCharge", urgentChargeModel.getChargeIn());//应收
                if (urgentChargeModel.getChargeIn() > 0) {
                    orderFee.setCustomerUrgentCharge(urgentChargeModel.getChargeIn());
                    orderFee.setOrderCharge(orderFee.getOrderCharge() + urgentChargeModel.getChargeIn());
                }
                feeParams.put("engineerUrgentCharge", urgentChargeModel.getChargeOut());//应付
                if (urgentChargeModel.getChargeOut() > 0) {
                    orderFee.setEngineerUrgentCharge(urgentChargeModel.getChargeOut());
                    orderFee.setEngineerTotalCharge(orderFee.getEngineerTotalCharge()+urgentChargeModel.getChargeOut());
                }
            }

            //Service Point Fee
            HashMap<String, Object> servicePointFeeParams = Maps.newHashMap();
            servicePointFeeParams.put("quarter", order.getQuarter());
            servicePointFeeParams.put("orderId", orderId);
            servicePointFeeParams.put("servicePointId", sid);
            if (hours > 0 || customerHours > 0 || timeLinessCharge != 0) {
                servicePointFeeParams.put("timeLiness", hours);
                servicePointFeeParams.put("timeLinessCharge", timeLinessCharge);
                servicePointFeeParams.put("customerTimeLiness", customerHours);
                servicePointFeeParams.put("customerTimeLinessCharge", subsidyTimeLinessCharge);//客户补贴时效费
                servicePointFeeParams.put("customerTimeLinessCharge", subsidyTimeLinessCharge);//客户补贴时效费
            }
            servicePointFeeParams.put("urgentCharge", urgentChargeModel.getChargeOut());//加急费

            //status
            orderStatus.setCloseFlag(1);
            orderStatus.setCloseBy(user);

            HashMap<String, Object> statusParams = Maps.newHashMap();
            statusParams.put("quarter", order.getQuarter());
            statusParams.put("orderId", orderId);
            statusParams.put("closeFlag", 1);
            statusParams.put("closeBy", user);
            statusParams.put("closeDate", date);

            // 关闭突击单 移到队列处理
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                gradeModel.setRushCloseFlag(1);//关闭
            }
            condition.setRushOrderFlag(2);

            //log
            //sd_orderprocesslog合并成一条，数据为0时不产生相关描述

            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setId(sequenceIdService.nextId());
            processLog.setQuarter(order.getQuarter());//*
            processLog.setAction(StringUtils.isBlank(title) ? "客评" : title);
            processLog.setOrderId(orderId);
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_COMPLETED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            StringBuilder sbLog = new StringBuilder(200);
            sbLog.append(StringUtils.isBlank(title) ? "客评" : title);
            if(user.getUserType()==User.USER_TYPE_ENGINEER){
                sbLog.append("【系统客评】");
            }else{
                sbLog.append("【"+ user.getName()+"】");
            }
            processLog.setActionComment(sbLog.toString());
            gradeModel.setProcessLog(processLog);
            //客服网点可见费用
            sbLog.setLength(0);
            List<OrderProcessLog> logs = Lists.newArrayList();
            Double insuranceCharge = order.getOrderFee().getInsuranceCharge();
            if (timeLinessCharge != 0 || insuranceCharge != 0 || subsidyTimeLinessCharge > 0 || urgentChargeModel.getChargeOut() > 0) {
                OrderProcessLog kefuProcessLog = new OrderProcessLog();
                kefuProcessLog.setId(sequenceIdService.nextId());
                kefuProcessLog.setQuarter(order.getQuarter());//*
                kefuProcessLog.setAction(StringUtils.isBlank(title) ? "客评" : title);
                kefuProcessLog.setOrderId(orderId);
                kefuProcessLog.setStatus(status.getLabel());
                kefuProcessLog.setStatusValue(Order.ORDER_STATUS_COMPLETED);
                kefuProcessLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                kefuProcessLog.setCloseFlag(0);
                kefuProcessLog.setCreateBy(user);
                kefuProcessLog.setCreateDate(date);
                Set<VisibilityFlagEnum> visibilityFlags = Sets.newHashSet();
                visibilityFlags.add(VisibilityFlagEnum.KEFU);
                visibilityFlags.add(VisibilityFlagEnum.SERVICE_POINT);
                kefuProcessLog.setVisibilityFlag(VisibilityFlagEnum.or(visibilityFlags));
                sbLog.append(String.format("完工时效【%.2f小时】", hours));
                if (urgentChargeModel.getChargeOut() > 0) {
                    sbLog.append(String.format(",网点加急费【%.2f元】", urgentChargeModel.getChargeOut()));
                }
                if (subsidyTimeLinessCharge > 0) {
                    sbLog.append(String.format(",网点时效奖励【%.2f元】", subsidyTimeLinessCharge));
                }
                if (timeLinessCharge > 0) {
                    sbLog.append(String.format(",时效补贴【%.2f元】", timeLinessCharge));
                }
                if (insuranceCharge > 0) {
                    sbLog.append(String.format(",互助基金【%.2f元】", insuranceCharge));
                }
                kefuProcessLog.setActionComment(StringUtils.left(sbLog.toString(),255));
                logs.add(kefuProcessLog);
            }
            //客户可看费用
            sbLog.setLength(0);
            if (customerTimeLinessCharge > 0 || urgentChargeModel.getChargeIn() > 0) {
                OrderProcessLog customerProcessLog = new OrderProcessLog();
                customerProcessLog.setId(sequenceIdService.nextId());
                customerProcessLog.setQuarter(order.getQuarter());//*
                customerProcessLog.setAction(StringUtils.isBlank(title) ? "客评" : title);
                customerProcessLog.setOrderId(orderId);
                customerProcessLog.setStatus(status.getLabel());
                customerProcessLog.setStatusValue(Order.ORDER_STATUS_COMPLETED);
                customerProcessLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
                customerProcessLog.setCloseFlag(0);
                customerProcessLog.setCreateBy(user);
                customerProcessLog.setCreateDate(date);
                sbLog.append(String.format("完工时效【%.2f小时】", hours));
                if (urgentChargeModel.getChargeIn() > 0) {
                    sbLog.append(String.format(",客户加急费【%.2f元】", urgentChargeModel.getChargeIn()));
                }
                if (customerTimeLinessCharge > 0) {
                    sbLog.append(String.format(",客户时效奖励【%.2f元】", customerTimeLinessCharge));
                }
                customerProcessLog.setActionComment(StringUtils.left(sbLog.toString(),255));
                Set<VisibilityFlagEnum> visibilityFlags = Sets.newHashSet();
                visibilityFlags.add(VisibilityFlagEnum.CUSTOMER);
                customerProcessLog.setVisibilityFlag(VisibilityFlagEnum.or(visibilityFlags));
                logs.add(customerProcessLog);
            }
            if(logs!=null && logs.size()>0){
                gradeModel.setFeeProcessLogs(logs);
            }
            //region save to db
            dao.updateCondition(conditionParams);
            if (!feeParams.isEmpty()) {
                dao.updateFee(feeParams);
            }
            //Service Point Fee
            dao.updateOrderServicePointFeeByMaps(servicePointFeeParams);
            //status
            dao.updateStatus(statusParams);
            //关闭配件单 2019/06/13 23:13 home
            orderMaterialService.closeMaterialMasterWhenGrade(order.getDataSourceId(),orderId,order.getQuarter(),user,date,"客评自动关闭");
            //关闭突击单 移到队列处理
            //log 移到队列：OrderGradeMessageReceiver 中处理
            b2BCenterOrderService.completeOrder(order, date, user, date);
            b2BCenterPushOrderInfoToMsService.completeOrder(order, date, user, date);
            //自动对账处理
            //无好评单
            if(orderStatusFlag == null || orderStatusFlag.getPraiseStatus() == 0) {
                orderAutoChargeCheckService.autoCharge(order,orderId,order.getQuarter(),user,date.getTime());
            }else if(orderStatusFlag != null && orderStatusFlag.getPraiseStatus() == PraiseStatusEnum.NEW.code) {
                //好评单自动审核
                autoReviewPraise = true;
            }
            //更新网点未完工数
            if(preStatus!=Order.ORDER_STATUS_APP_COMPLETED){
                updateServicePointUnfinishedOrderCount(sid,-1,"客评",orderId,user);
            }
            saveSuccess = true;//*
            //endregion save to db

            //region cache
            //更新close信息，如自动对账，在队列处理完成后再从缓存从移除，并产生历史数据json
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setCondition(condition)
                    .setOrderStatus(orderStatus)
                    .setSyncDate(new Date().getTime())
                    .setExpireSeconds(OrderUtils.ORDER_EXPIRED)
                    .setDeleteField(OrderCacheField.PENDING_FLAG)
                    .setDeleteField(OrderCacheField.PENDING_TYPE);
            OrderCacheUtils.update(builder.build());

            //endregion cache

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.orderGrade(
                    orderId,order.getQuarter(), status.getIntValue(),Order.ORDER_SUBSTATUS_COMPLETED,
                    user.getId(),date.getTime());
            //endregion
        } catch (OrderException oe) {
            saveSuccess = false;
            log.error("[OrderService.saveGrade] orderId:{}", gradeModel.getOrderId(), oe);
            throw oe;
        } catch (Exception e) {
            saveSuccess = false;
            log.error("[OrderService.saveGrade] orderId:{}", gradeModel.getOrderId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (saveSuccess) {
                sendGradeMessage(gradeModel,order,autoReviewPraise, user, date);
                //非语音回访客评，发送停止智能回访
                if (gradeModel.getAutoGradeFlag() != OrderUtils.OrderGradeType.VOICE_GRADE.getValue() && StringUtils.isNoneBlank(siteCode)) {
                    Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), orderId);
                    if (taskResult != null && taskResult == 0) {
                        stopVoiceOperateMessage(siteCode, order.getId(), order.getQuarter(), user.getName(), date);
                    }//taskResult
                }//autoGradeFlag
            }
            if (lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 客服完工（云米）
     */
    @Transactional(readOnly = false)
    public void kefuComplete(KefuCompleteModel completeModel) {
        Boolean saveSuccess = false;
        if (completeModel == null || completeModel.getOrderId() == null) {
            throw new OrderException("参数错误");
        }
        Long orderId = completeModel.getOrderId();
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        Date date = new Date();
        Order order = completeModel.getOrder();
        OrderCondition condition = order.getOrderCondition();
        try {
            HashMap<String, Object> params = Maps.newHashMap();
            condition.setPendingFlag(2);//正常
            condition.setPendingType(new Dict(0, ""));
            params.put("orderId", condition.getOrderId());
            params.put("quarter", condition.getQuarter());
            params.put("appCompleteType", completeModel.getCompleteType().getValue());//完工类型-客服完工
            params.put("appCompleteDate", date);//完工日期
            params.put("pendingFlag", condition.getPendingFlag());
            params.put("pendingType", 0);
            params.put("status", Order.ORDER_STATUS_APP_COMPLETED);
            params.put("subStatus", Order.ORDER_SUBSTATUS_APPCOMPLETED);
            params.put("updateDate", date);
            params.put("updateBy", completeModel.getUser().getId());
            int cnt = dao.kefuComplete(params);
            if(cnt == 0) {
                throw new OrderException("完工失败，无数据更新。");
            }
            if(completeModel.getDataSourceId() == B2BDataSourceEnum.VIOMI.getId()) {
                orderAdditionalInfoService.updateBuyDate(condition.getOrderId(), condition.getQuarter(), completeModel.getBuyDate().getTime());
            }
            //log
            String statusLabel = MSDictUtils.getDictLabel(Order.ORDER_STATUS_APP_COMPLETED.toString(), "order_status", "客服完工");//切换为微服务
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(condition.getQuarter());
            processLog.setAction("客服完成");
            processLog.setOrderId(condition.getOrderId());
            processLog.setActionComment(String.format("%s,%s", completeModel.getCompleteType().getLabel(), StringUtils.isBlank(completeModel.getRemarks()) ? "" : "备注:" + completeModel.getRemarks()));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(), 255));
            processLog.setStatus(statusLabel);
            processLog.setStatusValue(Order.ORDER_STATUS_APP_COMPLETED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(completeModel.getUser());
            processLog.setCreateDate(date);
            processLog.setRemarks("");
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(completeModel.getDataSourceId());
            saveOrderProcessLogNew(processLog);
            saveSuccess = true;//*
            //region cache
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(condition.getOrderId())
                    .setDeleteField(OrderCacheField.INFO)
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.PENDING_TYPE)
                    .setDeleteField(OrderCacheField.PENDING_TYPE_DATE);
            OrderCacheUtils.update(builder.build());
            //endregion cache
            //region 网点订单数据更新
            servicePointOrderBusinessService.appComplete(
                    condition.getOrderId(),
                    condition.getQuarter(),
                    Order.ORDER_SUBSTATUS_APPCOMPLETED,
                    completeModel.getCompleteType().getValue(),
                    0,
                    completeModel.getUser().getId(),
                    date.getTime()
            );
            //endregion
            //更新未完工单数
            ServicePoint servicepoint = condition.getServicePoint();
            if(servicepoint!=null && servicepoint.getId()!=null && servicepoint.getId()>0){
                updateServicePointUnfinishedOrderCount(servicepoint.getId(),-1,"客服完工",condition.getOrderId(),completeModel.getUser());
            }

            //region B2B
            // 调用云米：处理完成
            b2BCenterOrderService.appCompleteOrder(order,completeModel.getUser(),date);
            //endregion B2B

        } catch (OrderException oe) {
            saveSuccess = false;
            log.error("[OrderService.kefuComplete] orderId:{}", condition.getOrderId(), oe);
            throw oe;
        } catch (Exception e) {
            saveSuccess = false;
            log.error("[OrderService.kefuComplete] orderId:{}", condition.getOrderId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }


    /**
     * @date 2018/10/10
     * 接单客评消息队列后对数据库操作
     * 1.保存客评记录
     * 2.记录跟踪进度(日志)
     * 3.关闭突击单
     * 4.更新网点评分及完成单数，并同步缓存
     * 5.更新安维评分及完成单数，并同步缓存
     * @date 2019/08/15
     * 6.关闭催单
     */
    @Transactional(readOnly = false)
    public void saveGradeRecordAndServicePoint(OrderGradeModel gradeModel) {
        ServicePoint servicePoint = gradeModel.getServicePoint();
        if (servicePoint == null) {
            throw new OrderException("订单:" + gradeModel.getOrderNo() + " 客评队列无:网点信息");
        }
        Long sid = servicePoint.getId();
        servicePoint = servicePointService.getFromCache(sid);
        if (servicePoint == null) {
            throw new OrderException("订单:" + gradeModel.getOrderNo() + " 客评队列无:网点信息");
        }
        /* 切换为微服务
        if (servicePoint.getFinance().getPaymentType() != null && Integer.parseInt(servicePoint.getFinance().getPaymentType().getValue()) > 0) {
            String paymentTypeLabel = MSDictUtils.getDictLabel(servicePoint.getFinance().getPaymentType().getValue(), "PaymentType", "");
            servicePoint.getFinance().getPaymentType().setLabel(paymentTypeLabel);
        }
        if (servicePoint.getFinance().getBank() != null && Integer.parseInt(servicePoint.getFinance().getBank().getValue()) > 0) {
            String bankName = MSDictUtils.getDictLabel(servicePoint.getFinance().getBank().getValue(), "banktype", "");
            servicePoint.getFinance().getBank().setLabel(bankName);
        }
        if (servicePoint.getLevel() != null && Integer.parseInt(servicePoint.getLevel().getValue()) > 0) {
            String levelName = MSDictUtils.getDictLabel(servicePoint.getLevel().getValue(), "ServicePointLevel", "");
            servicePoint.getLevel().setLabel(levelName);
        }
        if (servicePoint.getFinance() != null && servicePoint.getFinance().getUnit() != null && StringUtils.isNotBlank(servicePoint.getFinance().getUnit().getValue())) {
            String unitName = MSDictUtils.getDictLabel(servicePoint.getFinance().getUnit().getValue(), "unit", "");
            servicePoint.getFinance().getUnit().setLabel(unitName);
        }
        if (servicePoint.getFinance() != null) {
            Dict bankIssue = servicePoint.getFinance().getBankIssue();
            if (bankIssue != null && StringUtils.isNotBlank(bankIssue.getValue()) && !bankIssue.getValue().equalsIgnoreCase("0")) {
                Dict nbankIssue = MSDictUtils.getDictByValue(bankIssue.getValue(), "BankIssueType");//切换为微服务
                if (nbankIssue == null) {
                    nbankIssue = new Dict(bankIssue.getValue(), "付款异常");
                }
                servicePoint.getFinance().setBankIssue(nbankIssue);
            }
        }*/

        Engineer engineer = gradeModel.getEngineer();
        if (engineer == null) {
            throw new OrderException("订单:" + gradeModel.getOrderNo() + " 客评队列无:安维信息");
        }
        Long eid = engineer.getId();
        engineer = servicePointService.getEngineerFromCache(sid, eid);
        if (engineer == null) {
            throw new OrderException("订单:" + gradeModel.getOrderNo() + " 客评队列无:安维信息");
        }

        //1.跟踪进度(日志)
        if (gradeModel.getProcessLog() != null) {
//            dao.insertProcessLog(gradeModel.getProcessLog());
            saveOrderProcessLogNew(gradeModel.getProcessLog());
        }
        if(gradeModel.getFeeProcessLogs()!=null && gradeModel.getFeeProcessLogs().size()>0){
            for (OrderProcessLog item:gradeModel.getFeeProcessLogs()){
                if(item!=null){
                    dao.insertProcessLog(item);
                }
                //saveOrderProcessLogWithNoCalcVisibility(item);
            }
        }
        //gradeModel.setProcessLog(null);//此处不序列化此属性
        String json = OrderGradeModelAdapter.getInstance().toJson(gradeModel);
        json = GsonUtils.MyCatJsonFormat(json);//mycat json处理
        gradeModel.setContent(json);

        //2.save grade
        gradeDao.insertOrderGrade(gradeModel);

        //3.关闭突击单
        if (gradeModel.getRushCloseFlag() == 1) {
            crushService.closeOrderCurshByOrderId(gradeModel.getOrderId(),gradeModel.getQuarter(),1,null,gradeModel.getCreateBy(),gradeModel.getCreateDate());
        }

        //region 4.servicePoint

        HashMap<String, Object> servicePointParams = Maps.newHashMap();
        servicePointParams.put("id", sid);
        if (gradeModel.getPoint() > 0) {
            servicePointParams.put("grade", gradeModel.getPoint());//与原来值取平均值
        }
        servicePointParams.put("orderCount", 1);
        //网点
        //servicePointDao.updateServicePointByMap(servicePointParams);//mark on 2020-1-17   web端去md_servicepoint
        //servicePointService.updateServicePointByMap(servicePointParams); // add on 2019-10-4 //mark on 2020-1-16 集中调用MD微服务

        //cache
        /*
        //mark on 2020-1-17  begin  web端去md_servicepoint
        Double score = Double.valueOf(servicePoint.getId().toString());
        try {
            servicePoint.setOrderCount(servicePoint.getOrderCount() + 1);
            if (gradeModel.getPoint() > 0) {
                if (servicePoint.getGrade() > 0) {
                    int point = (servicePoint.getGrade() + gradeModel.getPoint()) / 2;
                    servicePoint.setGrade(point);
                } else {
                    servicePoint.setGrade(gradeModel.getPoint());
                }
            }
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICEPOINT_ALL, servicePoint, score, 0l);
        } catch (Exception e) {
            log.error("同步网点缓存异常,orderNo:{},servicePoint:{}", gradeModel.getOrderNo(), sid, e);
        }
        //mark on 2020-1-17  end
        */

        //end servicePoint

        //region 5.engineer

        //更新安维人员完成订单数及评价分数
        HashMap<String, Object> engineerParams = Maps.newHashMap();
        engineerParams.put("id", eid);
        if (gradeModel.getPoint() > 0) {
            engineerParams.put("grade", gradeModel.getPoint());//与原来值取平均值
        }
        engineerParams.put("orderCount", 1);
        //servicePointDao.updateEngineerByMap(engineerParams); //mark on 2020-1-13 web端去除md_engineer
        //msEngineerService.updateEngineerByMap(engineerParams);  // add on 2019-10-18 //Engineer微服务  //mark on 2020-1-16 集中调用MD微服务

        //cache
        /*
        // Engineer缓存改从微服务获取  // mark on 2019-11-12
        score = Double.valueOf(eid.toString());
        String cacheKey = String.format(RedisConstant.MD_SERVICEPOINT_ENGINEER, sid);
        try {
            engineer.setOrderCount(engineer.getOrderCount() + 1);
            if (gradeModel.getPoint() > 0) {
                if (engineer.getGrade() > 0) {
                    double dpoint = (engineer.getGrade() + gradeModel.getPoint()) / 2;
                    engineer.setGrade(dpoint);
                } else {
                    engineer.setGrade(gradeModel.getPoint().doubleValue());
                }
            }
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, cacheKey, engineer, score, 0l); //mark on 2019-11-12
        } catch (Exception e) {
            log.error("同步安维缓存异常,orderNo:{},engineer:{}", gradeModel.getOrderNo(), eid, e);
        }
         */
        //endregion 3.engineer

        //region 6.关闭催单
        // 2019/08/15
        Order order = gradeModel.getOrder();
        Boolean hasRemindered = false;
        if(order != null && order.getOrderStatus() != null && order.getOrderStatus().getReminderStatus() != null && order.getOrderStatus().getReminderStatus() > 0) {
            hasRemindered = true;
        }
        if (hasRemindered && order.getOrderStatus().getReminderStatus() < ReminderStatus.Completed.getCode()){
            reminderService.completeReminder(order.getId(),order.getQuarter(),gradeModel.getCreateBy(),gradeModel.getCreateDate(),"客评", ReminderAutoCloseTypeEnum.OrderComplete,Order.ORDER_STATUS_COMPLETED,sid);
            //淘汰订单orderStatus缓存
            OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
            cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(cacheBuilder.build());
        }else if(hasRemindered){
            reminderService.updateOrderCloseInfo(order.getId(),order.getQuarter(),gradeModel.getCreateDate(),Order.ORDER_STATUS_COMPLETED);
        }
        //endregion

        //add on 2020-1-16 begin  集中处理MD微服务调用
        servicePointService.updateServicePointByMap(servicePointParams);
        msEngineerService.updateEngineerByMap(engineerParams);
        //add on 2020-1-16 end
    }

    /**
     * 客评后消息队列
     * 1.客服完工日报表
     * 2.客评消息中增加自动关闭催单处理 2019-08-16
     * 3.增加：发送好判定自动审核消息队列送（延迟5秒) 2020-04-03
     *
     * @param grade 客评实体
     * @param order 订单
     * @param autoReviewPraise 好评单自动审核标记
     * @param user 操作人
     * @param date 操作日期
     */
    private void sendGradeMessage(OrderGradeModel grade, Order order, boolean autoReviewPraise,User user, Date date) {
        OrderCondition condition = order.getOrderCondition();
        Long orderId = order.getId();

        //region 1.客服完成日报表
        MQOrderReport.OrderReport message = null;
        try {
            MQOrderReport.Kefu kefu;
            if (user.isKefu()) {
                //客评人为客服
                kefu = MQOrderReport.Kefu.newBuilder()
                        .setId(user.getId())
                        .setName(user.getName())
                        .build();
            } else {
                kefu = MQOrderReport.Kefu.newBuilder()
                        .setId(condition.getKefu().getId())
                        .setName(condition.getKefu().getName())
                        .build();
            }
            message = MQOrderReport.OrderReport.newBuilder()
                    .setOrderId(orderId)
                    .setOrderType(Order.ORDER_STATUS_COMPLETED)
                    .setQty(1)
                    .setAmount(order.getOrderFee().getExpectCharge())
                    .setTriggerDate(date.getTime())
                    .setTriggerBy(user.getId())
                    .setKefu(kefu)
                    .setQty(1)
                    .build();

            orderReportSender.send(message);
        } catch (Exception e) {
            if (message != null) {
                log.error("[OrderService.sendGradeMessage]message:{}",new JsonFormat().printToString(message),e);
                //OrderReport report = new OrderReport();
                //report.setOrderId(message.getOrderId());
                //report.setRetryTimes(0);
                //report.setStatus(40);
                //report.setQty(message.getQty());
                //report.setAmount(message.getAmount());
                //report.setOrderType(message.getOrderType());
                //report.setTriggerBy(message.getKefu().getId());
                //report.setTriggerDate(new Date(message.getTriggerDate()));
                //report.setCustomer(message.getCustomer());
                //report.setKefu(message.getKefu());
                //report.setRemarks(e.getMessage().length() > 250 ? e.getMessage().substring(0, 250) : e.getMessage());
                //try {
                //    mqOrderReportService.insert(report);
                //} catch (Exception e1) {
                //    log.error("[OrderService.saveGrade]save to db fail - orderId:{} ,message:{}", orderId, e1.getMessage());
                //}
            } else {
                log.error("[OrderService.sendGradeMessage]客服完成日报表", e);
            }
        }

        //endregion

        //2.好评单自动审核 2020-03-31
        long servicePointId = Optional.ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
        if (autoReviewPraise){
            int userType = praiseFormService.getCreatorType(user);
            int visibilityFlag = VisibilityFlagEnum.or(Sets.newHashSet(VisibilityFlagEnum.KEFU, VisibilityFlagEnum.CUSTOMER,VisibilityFlagEnum.SERVICE_POINT));
            praiseAutoReviewMessageSender.sendRetry(orderId, order.getQuarter(),servicePointId, user, userType, date.getTime(), visibilityFlag, 5000, 1);
        }
        //3.客评消息
        try {
            MQOrderGradeMessage.OrderGradeMessage gradeMessage = Mappers.getMapper(OrderGradeMessageMapper.class).modelToMq(grade);
            gradeMessageSender.sendRetry(gradeMessage, 5000, 0);
        } catch (Exception e) {
            log.error("发送客评消息失败:{}", OrderGradeModelAdapter.getInstance().toJson(grade), e);
        }
    }

    //region 网点时效

    /**
     * 检查网点时效是否重复计费
     * @param beginDate         客评日期-开始
     * @param endDate           客评日期-结束
     * @param customerId        客户
     * @param productCategoryId 品类
     * @param userPhone         用户电话
     * @param servicePointId    网点
     * @return  true：有符合条件的订单已计算时效费
     */
    public Boolean checkRepeateTimeLinessFee(Date beginDate,Date endDate,long customerId,long servicePointId,long productCategoryId,String userPhone) {
        DateTime start = new DateTime(beginDate.getTime());
        start = start.minusMonths(6);
        Date startDate = OrderUtils.getGoLiveDate();
        if(start.toDate().after(startDate)){
            startDate = start.toDate();
        }
        //半年内
        List<String> quarters = QuarterUtils.getQuarters(startDate, endDate);
        List<Map<String, Object>> list = dao.getRepeateTimelinessFeeInfo(beginDate, endDate, customerId, servicePointId, productCategoryId, userPhone, quarters);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        Map<String, Object> map;
        BigDecimal fee;
        BigDecimal zeroFee = new BigDecimal("0.0");
        for (int i = 0, size = list.size(); i < size; i++) {
            map = list.get(i);
            fee = (BigDecimal) map.get("customer_time_liness_charge");
            if (fee.compareTo(zeroFee) == 1) {
                return true;
            }
            fee = (BigDecimal) map.get("time_liness_charge");
            if (fee.compareTo(zeroFee) == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得网点时效计费开始时间
     * 1.按网点获得最早的派单日期
     * 2.与到货日期比较，取最晚的日期
     *
     * @param order
     * @param orderId
     * @param quarter
     * @param servicePointId
     * @return
     */
    public Date getServicePointTimeLinessStartDate(Order order, Long orderId, String quarter, Long servicePointId) {
        if (orderId == null || servicePointId == null || order == null || order.getOrderStatus() == null || order.getOrderCondition() == null) {
            return null;
        }
        //plandate
        Date planDate = dao.getOrderStartPlanDateOfServicePoint(orderId, quarter, servicePointId, null);
        if (planDate == null) {
            planDate = order.getOrderStatus().getPlanDate();
        }
        if (planDate == null) {
            planDate = order.getOrderCondition().getArrivalDate();
        } else {
            Date arrivalDate = order.getOrderCondition().getArrivalDate();
            //如到货日期比派单日期晚，已到货日期为准
            if (arrivalDate != null && DateUtils.pastMinutes(planDate, arrivalDate) > 0) {
                planDate = arrivalDate;
            }
        }
        return planDate;
    }


    /**
     * 计算网点时效等级及金额
     *
     * @param timeLinessType      要返回时效的内容类型
     * @param condition           订单实例
     * @param timeLinessStartDate 时效开始计时时间
     * @param productCategoryId   产品类别
     * @param manuTimeLinessLevel 客服选择的时效区间
     * @return Dict
     * type:用时
     * value:等级
     * label:金额
     */
    public Dict getServicePointTimeLinessInfo(Order.TimeLinessType timeLinessType, OrderCondition condition, Date timeLinessStartDate, long productCategoryId, Integer manuTimeLinessLevel) throws RuntimeException {
        Dict level = null;
        //用时
        Date appCompleteDate = new Date();
        //如果是app完成(compeled_all,compeled_all_notest,compeled_maintain)，结束时间已app完成为准
        //否则已当前时间为准
        Set<String> appCompletSet = Sets.newHashSet("compeled_all", "compeled_all_notest", "compeled_maintain");
        if (condition.getAppCompleteDate() != null && appCompletSet.contains(condition.getAppCompleteType().toLowerCase().trim())) {
            appCompleteDate = condition.getAppCompleteDate();
        }
        //Long minutes = DateUtils.pastMinutes(timeLinessStartDate, appCompleteDate);
        double hours = pastHours(timeLinessStartDate, appCompleteDate);
        if (hours <= 0 && (manuTimeLinessLevel == null || manuTimeLinessLevel <= 0)) {
            return level;
        }
        if(productCategoryId <= 0){
            throw new OrderException("读取订单品类错误，清重试。");
        }
        level = new Dict();
        level.setType(String.format("%.2f", hours));
        level.setLabel("0.0");
        if (timeLinessType.type >= Order.TimeLinessType.LEVEL.type) {//等级
            if (manuTimeLinessLevel != null && manuTimeLinessLevel > 0) {
                level.setValue(manuTimeLinessLevel.toString());//客服选择的等级
            } else {
                List<Dict> timeLinessTypes = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
                if (CollectionUtils.isEmpty(timeLinessTypes)) {
                    throw new OrderException("请检查是否设定网点时效配置。");
                }
                //排序
                timeLinessTypes.stream().sorted(Comparator.comparing(Dict::getIntValue));
                Dict dict;
                List<String> ranges;
                for (int i = 0, size = timeLinessTypes.size(); i < size; i++) {
                    dict = timeLinessTypes.get(i);
                    ranges = Splitter.onPattern("[~|-]")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(dict.getLabel());
                    if (ranges.size() == 2) {
                        if (hours > Double.valueOf(ranges.get(0)) && hours <= Double.valueOf(ranges.get(1))) {
                            level.setValue(dict.getValue());
                            break;
                        }
                    }
                }
            }
        }
        if (timeLinessType.type == Order.TimeLinessType.ALL.type) {
            //增加时效区域开关判断 2019-05-18
            Area area = condition.getArea();
            if(area==null || area.getId() == null || area.getId() <=0){
                throw new OrderException("读取订单区域错误，请重试。");
            }
            area = areaService.getFromCache(area.getId());
            if(area==null || area.getId() == null || area.getId() <=0
                    || area.getParent() == null || area.getParent().getId() == null
                    || area.getParent().getId() <= 0){
                throw new OrderException("读取订单区域错误，请重试。");
            }
            AreaTimeLiness areaTimeLiness = msAreaTimeLinessService.getFromCacheForSD(area.getParent().getId(),productCategoryId); //2020-06-24
            if(areaTimeLiness == null){
                throw new OrderException("区域时效开关未设定，请联系管理员。");
            }
            //订单只能有一个品类，按品类请求微服务
            //奖金
            if (areaTimeLiness.getIsOpen() == 1 && level != null && StringUtils.isNotBlank(level.getValue())) {
                List<TimeLinessPrice> prices = timeLinessPriceService.getTimeLinessPrices(productCategoryId);
                if (prices == null || prices.size() == 0) {
                    throw new OrderException("请检查产品类别是否设定网点时效费用。");
                }
                final int intLevel = level.getIntValue();
                Double timeLinessCharge = prices.stream().filter(t -> t.getTimeLinessLevel().getIntValue() == intLevel).map(t -> t.getAmount()).min(Double::compareTo).orElse(0.00);
                level.setLabel(String.format("%.2f", timeLinessCharge));
            }
        }
        return level;
    }

    private double pastHours(Date startDate,Date toDate){
        long t = toDate.getTime() - startDate.getTime();
        double hours =  t / (3600d * 1000d);
        return hours;
    }

    /**
     * 计算客户时效，包含用时，等级及客户应付金额级网点应收金额
     *
     * @param timeLinessType 要返回时效的内容类型
     * @param condition      订单实例
     * @return Dict
     * type:用时
     * value:等级
     * label:应收金额
     * remarks:应付金额
     */
    public Dict getCustomerTimeLinessInfo(Order.TimeLinessType timeLinessType, OrderCondition condition) throws RuntimeException {
        Dict level = null;
        if (condition == null || condition.getCreateDate() == null) {
            return level;
        }
        //用时
        //开始日期
        Date startDate = condition.getCreateDate();
        //如果有到货日期，以到货日期为准
        //否则已下单日期为准
        if (condition.getArrivalDate() != null && DateUtils.pastMinutes(condition.getCreateDate(), condition.getArrivalDate()) > 0) {
            startDate = condition.getArrivalDate();
        }
        //结束日期
        Date appCompleteDate = new Date();
        //如果是app完成(compeled_all,compeled_all_notest,compeled_maintain)，结束时间已app完成为准
        //否则已当前时间为准
        Set<String> appCompletSet = Sets.newHashSet("compeled_all", "compeled_all_notest", "compeled_maintain");
        if (condition.getAppCompleteDate() != null && appCompletSet.contains(condition.getAppCompleteType().toLowerCase().trim())) {
            appCompleteDate = condition.getAppCompleteDate();
        }
        Long minutes = DateUtils.pastMinutes(startDate, appCompleteDate);
        Double hours = 0.0;
        if (minutes > 0) {
            hours = minutes / 60.0;
        }
        if (hours <= 0) {
            return level;
        }
        level = new Dict();
        level.setType(String.format("%.2f", hours));
        level.setLabel("0.0");
        level.setRemarks("0.0");
        level.setValue("");//等级
        //getOrderById中已经从缓存中取了一次customer及网点信息
        Customer customer = condition.getCustomer();
        //客户时效开关开启，才计算等级和费用
        if (customer != null && customer.getTimeLinessFlag() == 1 && timeLinessType.type >= Order.TimeLinessType.LEVEL.type) {
            //等级
            List<TimelinessLevel> timeLinessLevels = timelinessLevelService.findAllList();
            if (timeLinessLevels == null || timeLinessLevels.size() == 0) {
                throw new OrderException("请检查是否设定客户时效等级。");
            }
            //排序
            timeLinessLevels.stream().sorted(Comparator.comparing(TimelinessLevel::getSort));
            TimelinessLevel timelinessLevel;
            List<String> ranges;
            for (int i = 0, size = timeLinessLevels.size(); i < size; i++) {
                timelinessLevel = timeLinessLevels.get(i);
                ranges = Splitter.onPattern("[~|-]")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(timelinessLevel.getName());
                if (ranges.size() == 2) {
                    if (hours > Double.valueOf(ranges.get(0)) && hours <= Double.valueOf(ranges.get(1))) {
                        level.setValue(timelinessLevel.getId().toString());//id
                        break;
                    }
                }
            }
        }
        //奖金
        if (customer != null && customer.getTimeLinessFlag() == 1
                && timeLinessType.type == Order.TimeLinessType.ALL.type
                && level != null && StringUtils.isNotBlank(level.getValue())) {
            Long customerId = null;
            Long areaId = null;
            Long provinceId = null;
            if (condition.getCustomer() == null || condition.getCustomer().getId() <= 0) {
                return level;
            }
            customerId = condition.getCustomer().getId();

            if (condition.getArea() == null || condition.getArea().getId() <= 0) {
                return level;
            }
            areaId = condition.getArea().getId();
            Area area = areaService.getFromCache(areaId, Area.TYPE_VALUE_COUNTY);
            if (area != null) {
                String[] ids = StringUtils.split(area.getParentIds(), ",");
                if (ids != null && ids.length == 4) {
                    provinceId = Long.valueOf(ids[2]);//0:0 1:国家 2:省 3:市
                } else {
                    return level;
                }
            }
            AreaTimelinessModel areaTimeliness = customerTimelinessService.getAreaTimelinessModel(customerId, provinceId);
            if (areaTimeliness != null && areaTimeliness.getList() != null && areaTimeliness.getList().size() > 0) {
                final long levelId = Long.valueOf(level.getValue());
                TimelinessChargeModel timelinessChargeModel = areaTimeliness.getList().stream().filter(t -> t.getTimelinessLevel().getId().longValue() == levelId).findFirst().orElse(null);
                if (timelinessChargeModel != null) {
                    level.setLabel(String.format("%.2f", timelinessChargeModel.getChargeIn()));//应收
                    level.setRemarks(String.format("%.2f", timelinessChargeModel.getChargeOut()));//应付
                }
            }
        }
        return level;
    }

    /**
     * 网点时效最终费用计算
     * 两个开关都开启，且产生费用时，才判断规则
     *
     * @param timeLinessType    快可立补贴开关
     * @param customerTimeLinessType    客户时效开关
     * @param timeLinessCharge  快可立补贴费用
     * @param subsidyTimeLinessCharge   客户时效补贴费用
     * @return  NameValuePair<快可立补贴费用,客户时效补贴费用>
     */
    private com.kkl.kklplus.entity.common.NameValuePair<Double,Double> getServicePointTimeLinessCharge(Order.TimeLinessType timeLinessType, Order.TimeLinessType customerTimeLinessType,
                                                                                                       Double timeLinessCharge, Double subsidyTimeLinessCharge){
        com.kkl.kklplus.entity.common.NameValuePair<Double,Double> pair = new com.kkl.kklplus.entity.common.NameValuePair<Double,Double>(0.0,0.0);
        //3.费用处理规则，两个开关都开启，且有费用时，才判断规则
        if( timeLinessType == Order.TimeLinessType.ALL && customerTimeLinessType == Order.TimeLinessType.ALL
                && (timeLinessCharge > 0 || subsidyTimeLinessCharge > 0) ) {
            int logicStatus = 1;//客户时效优先
            try {
                logicStatus = StringUtils.toInteger(MSDictUtils.getDictSingleValue("timeline_charge_logic", "1"));
            }catch (Exception e){
                log.error("读取网点时效费用规则(timeline_charge_logic)错误",e);
            }
            switch(logicStatus){
                case 1:
                    //客户时效优先
                    if(subsidyTimeLinessCharge > 0) {
                        timeLinessCharge = 0.0;//快可立补贴
                    }
                    break;
                case 2:
                    //快可立补贴优先
                    if(timeLinessCharge > 0) {
                        subsidyTimeLinessCharge = 0.0;//客户补贴
                    }
                    break;
                default:
                    //叠加，不做特殊处理
                    break;
            }
        }
        pair.setName(timeLinessCharge);
        pair.setValue(subsidyTimeLinessCharge);
        return pair;
    }

    //endregion 网点时效

    /**
     * @param order       订单
     * @param checkDetail 是否检查订单明细
     * @version 0.1
     * 客评前检查 fee与detail汇总金额是否一致
     * @version 0.2
     * @date 2018/05/15
     * @by Ryan
     * 新增网点保险费(负数)，检查逻辑做变更
     * @version 0.3
     * @date 2018/09/11
     * @by Ryan
     * orderCondtion 移除 orderCharge，engineerTotalCharge
     * @version 0.4
     * @date 2018/10/09
     * @by Ryan
     * 去除order_detail 5个费用与order_fee 5个汇总的对比
     */
    public boolean checkOrderFeeAndServiceAmountBeforeGrade(Order order, Boolean checkDetail) {
        if (order == null) {
            return false;
        }
        //1.检查orderFee
        OrderFee fee = order.getOrderFee();
        if (fee == null) {
            return false;
        }
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        Double detailAmount = 0.0;
        //应收(时效，加急费 未汇总)
        detailAmount = fee.getServiceCharge()
                + fee.getOtherCharge()
                + fee.getMaterialCharge()
                + fee.getExpressCharge()
                + fee.getTravelCharge()
                + fee.getPraiseFee();
        if (!NumberUtils.formatNum(fee.getOrderCharge()).equals(NumberUtils.formatNum(detailAmount))) {
            return false;
        }
        //应付(保险已汇总到total)
        Double engineerAmount = 0.0;
        engineerAmount = fee.getEngineerServiceCharge()
                + fee.getEngineerOtherCharge()
                + fee.getEngineerMaterialCharge()
                + fee.getEngineerExpressCharge()
                + fee.getEngineerTravelCharge()
                + fee.getInsuranceCharge()
                + fee.getEngineerPraiseFee();
        if (!NumberUtils.formatNum(fee.getEngineerTotalCharge()).equals(NumberUtils.formatNum(engineerAmount))) {
            return false;
        }

        return true;// ok
    }

    /**
     * @param order       订单
     * @param checkDetail 是否检查订单明细
     * @return
     * @version 0.1
     * 对账时检查 fee与detail汇总金额是否一致
     * @version 0.2
     * @date 2018/05/15
     * @by Ryan
     * 新增网点保险费(负数)，检查逻辑做变更
     * @version 0.3
     * @date 2018/05/18
     * @by Ryan
     * 新增网点时效费，检查逻辑做变更
     * @version 0.4
     * @date 2018/06/22
     * @by Ryan
     * 新增加急费
     * @version 0.5
     * @date 2018/09/11
     * @by Ryan
     * orderCondition 移除 orderCharge，engineerTotalCharge
     */
    public boolean checkOrderFeeAndServiceAmount(Order order, Boolean checkDetail) {
        if (order == null) {
            return false;
        }
        //1.检查orderFee
        OrderFee fee = order.getOrderFee();
        if (fee == null) {
            return false;
        }
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        Double detailAmount = 0.0;
        //应收
        detailAmount = fee.getServiceCharge()
                + fee.getOtherCharge()
                + fee.getMaterialCharge()
                + fee.getExpressCharge()
                + fee.getTravelCharge()
                + fee.getCustomerTimeLinessCharge()
                + fee.getCustomerUrgentCharge()
                + fee.getPraiseFee();
        if (!NumberUtils.formatNum(fee.getOrderCharge()).equals(NumberUtils.formatNum(detailAmount))) {
            return false;
        }
        //应付
        Double engineerAmount = 0.0;
        engineerAmount = fee.getEngineerServiceCharge()
                + fee.getEngineerOtherCharge()
                + fee.getEngineerMaterialCharge()
                + fee.getEngineerExpressCharge()
                + fee.getEngineerTravelCharge()
                + fee.getInsuranceCharge()
                + fee.getTimeLinessCharge()
                + fee.getSubsidyTimeLinessCharge()
                + fee.getEngineerUrgentCharge()
                + fee.getEngineerPraiseFee();
        if (!NumberUtils.formatNum(fee.getEngineerTotalCharge()).equals(NumberUtils.formatNum(engineerAmount))) {
            return false;
        }

        List<OrderDetail> details = order.getDetailList();
        if (details == null) {
            details = Lists.newArrayList();
        }

        if (checkDetail != null && true == checkDetail && details.size() == 0) {
            return false;
        }
        detailAmount = 0.0;
        engineerAmount = 0.0;
        if (details.size() > 0) {
            OrderDetail detail;
            for (int i = 0, size = details.size(); i < size; i++) {
                detail = details.get(i);
                //应收无 时效费，加急金额
                detailAmount = detailAmount
                        + detail.getCharge()
                        + detail.getExpressCharge()
                        + detail.getTravelCharge()
                        + detail.getMaterialCharge()
                        + detail.getOtherCharge();
                //应付无 保险费，时效奖励(快可立补贴)，时效费(客户补贴)，加急费
                engineerAmount = engineerAmount
                        + detail.getEngineerServiceCharge()
                        + detail.getEngineerExpressCharge()
                        + detail.getEngineerTravelCharge()
                        + detail.getEngineerMaterialCharge()
                        + detail.getEngineerOtherCharge();
            }
        }
        //应收上门服务表中应收无：时效费，加急费，好评费
        if (!NumberUtils.formatNum(fee.getOrderCharge() - fee.getCustomerTimeLinessCharge() - fee.getCustomerUrgentCharge() - fee.getPraiseFee()).equals(NumberUtils.formatNum(detailAmount))) {
            return false;
        }
        //应付上门服务中表中应付无：保险费，时效奖励(快可立补贴)，时效费(客户补贴)，加急费,好评费
        if (!NumberUtils.formatNum(fee.getEngineerTotalCharge() - fee.getInsuranceCharge() - fee.getTimeLinessCharge() - fee.getSubsidyTimeLinessCharge() - fee.getEngineerUrgentCharge() - fee.getEngineerPraiseFee()).equals(NumberUtils.formatNum(engineerAmount))) {
            return false;
        }
        /*2.check orderCondition.orderCharege <-> orderFee.orderCharge
        //  orderCondition.engineerTotalCharege <-> orderFee.engineerTotalCharge
        if (order.getOrderCondition() != null) {
            OrderCondition condition = order.getOrderCondition();
            if (condition.getOrderCharge() > 0.0 && condition.getEngineerTotalCharge() > 0.0
                    && (!StringUtils.formatNum(condition.getOrderCharge()).equals(StringUtils.formatNum(fee.getOrderCharge()))
                    || !StringUtils.formatNum(condition.getEngineerTotalCharge()).equals(StringUtils.formatNum(fee.getEngineerTotalCharge())))
                    ) {
                return false;
            }
        }
        */
        return true;
    }

    /**
     * 获得指定手机号用户的待客评的订单id列表
     *
     * @param phone
     * @return
     */
    public List<OrderCondition> getToGradeOrdersByPhone(String phone) {
        return dao.getToGradeOrdersByPhone(phone, null);
    }

    /**
     * 获得指定手机号用户的待客评的订单id列表
     *
     * @param phone
     * @return
     */
    public List<OrderCondition> getToGradeOrdersByPhoneAndDate(String phone, Date date) {
        List<String> quarters = null;
        if (date != null) {
            Date startDate = DateUtils.addMonth(date, 3);
            quarters = QuarterUtils.getQuarters(startDate, date);
        }
        if (quarters != null && quarters.isEmpty()) {
            quarters = null;
        }
        return dao.getToGradeOrdersByPhone(phone, quarters);
    }

    /**
     * 自动完工(自动完工消息队列调用)
     */
    //@Transactional(readOnly = false)
    public void autoComplete(OrderAutoComplete message) {
        long index = 0;
        if (message == null || message.getOrderId() == null || message.getOrderId() <= 0) {
            return;
        }
        message.setStatus(30);//成功
        message.setCreateDate(new Date());
        message.setUpdateDate(new Date());

        OrderCondition orderCondition = null;
        User user = null;
        Order order = null;
        try {
            user = UserUtils.get(message.getTriggerBy());
            if (null == user || null == user.getId()) {
                user = new User(message.getTriggerBy());
            }

            order = getOrderById(message.getOrderId(), message.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true, true,false,true);
            orderCondition = order.getOrderCondition();
            //检查是否可以自动客评
            NameValuePair checkResult = orderGradeService.canAutoGrade(order);
            int checkResultValue = StringUtils.toInteger(checkResult.getValue());
            if (checkResultValue == 0) {
                // 根据订单配件状态检查是否可以客评 2019/06/26
                MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),message.getOrderId(),order.getQuarter());
                if(!MSResponse.isSuccessCode(msResponse)){
                    checkResultValue = 3;//*
                    checkResult.setValue("3");
                    checkResult.setName(msResponse.getMsg());
                }else {
                    // ** 检查是否可自动客评,不能自动生成对账单的，不能自动客评 **
                    String msg = checkAutoComplete(order);
                    if (StringUtils.isNoneBlank(msg)) {
                        checkResultValue = 3;//*
                        checkResult.setValue("3");
                        checkResult.setName(msg);
                    }
                }
            }

            if (checkResultValue > 0) {
                message.setStatus(40);
                message.setRemarks(checkResult.getName());
                LogUtils.saveLog("订单自动完工检查-异常", "OrderService.autoComplete", message.getRemarks().concat(",id").concat(message.getOrderId().toString()), null, user, Log.TYPE_EXCEPTION);
            } else {
                message.setRemarks("ok");
                message.setStatus(30);
            }
            //保存消息记录
            try {
                if (message.getId() == null) {
                    message.setId(SeqUtils.NextID());
                    autoCompleteDao.insert(message);//保存消息
                } else {
                    message.setRetryTimes(1);
                    autoCompleteDao.update(message);
                }
            } catch (Exception e) {
                log.error("[OrderService.autoComplete]=={}== orderId:{},message:{}", index, message.getOrderId(), GsonUtils.getInstance().toGson(message), e);
            }
            OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(message.getOrderId(), message.getQuarter());
            if (checkResultValue > 0) {
                HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(3);
                params.put("orderId", order.getId());
                params.put("quarter", order.getQuarter());
                params.put("appAbnormalyFlag", 1);
                dao.updateCondition(params);
                //意见跟踪日志
                OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                        .channel(AppFeedbackEnum.Channel.APP.getValue())
                        .quarter(order.getQuarter())
                        .orderId(order.getId())
                        .servicePointId(0L)
                        .appointmentAt(0)
                        .opinionId(0)
                        .parentId(0)
                        .opinionType(AppFeedbackEnum.FeedbackType.APP_COMPLETE.getValue())
                        .opinionValue(0)
                        .opinionLabel("订单自动完工不符合条件，标记异常")
                        .isAbnormaly(1)
                        .remark("")
                        .createAt(System.currentTimeMillis())
                        .createBy(user)
                        .times(1)
                        .totalTimes(1)
                        .build();
                orderOpitionTraceService.insert(opitionTrace);

                //异常消息汇总
                if(orderCondition.getAppAbnormalyFlag() == 0) {
                    try {
                        sendAppNoticeMessage(
                                orderCondition.getOrderId(),
                                orderCondition.getQuarter(),
                                orderCondition.getCustomer().getId(),
                                orderCondition.getArea().getId(),
                                orderCondition.getKefu() != null ? orderCondition.getKefu().getId() : 0l,
                                new Date(),
                                user
                        );
                    } catch (Exception e) {
                        log.error("[自动完工:回调处理失败]sendAppNoticeMessage orderId:{}", orderCondition.getOrderId(), e);
                    }
                }
                //异常单
                AbnormalForm abnormalForm=null;
                Integer subType = 0;
                try {
                    if(order.getOrderCondition()!=null){
                        if(order.getOrderCondition().getOrderServiceType()==1){
                            subType = AbnormalFormEnum.SubType.INSTALL_ERROR.code;
                        }else{
                            subType = AbnormalFormEnum.SubType.REPAIR_ERROR.code;
                        }
                        String reason = "订单自动完工不符合条件:" + checkResult.getName();
                        abnormalForm = abnormalFormService.handleAbnormalForm(order,reason,user, AppFeedbackEnum.Channel.APP.getValue(),
                                AbnormalFormEnum.FormType.APP_COMPLETE.code,subType,"");
                        if(abnormalForm!=null){
                            abnormalForm.setOpinionLogId(opitionTrace.getId());
                            abnormalFormService.save(abnormalForm);
                        }
                    }
                }catch (Exception e){
                    log.error("[OrderService.SaveOrderComplete]app完工保存异常单失败 form:{}",GsonUtils.getInstance().toGson(abnormalForm), e);
                }
                return;
            }

            //自动客评

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(message.getQuarter());
            processLog.setAction("系统自动完成");
            processLog.setOrderId(message.getOrderId());
            processLog.setActionComment(String.format("系统自动完成,操作人:%s", user.getName()));
            processLog.setStatusValue(Order.ORDER_STATUS_SERVICED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(message.getUpdateDate());
            processLog.setRemarks("");
            processLog.setDataSourceId(order.getDataSourceId());

            //grade
            OrderGradeModel gradeModel = new OrderGradeModel();
            gradeModel.setOrder(order);//减少数据库操作
            gradeModel.setCheckOrderFee(false);//已经检查过了
            //gradeModel.setCheckCanAutoCharge(true);//不检查能否自动生成对账单
            //gradeModel.setCanAutoCharge(false);//自动生成对账单
            gradeModel.setOrderId(orderCondition.getOrderId());
            gradeModel.setQuarter(orderCondition.getQuarter());
            //servicePoint
            gradeModel.setServicePoint(orderCondition.getServicePoint());
            //engineer
            Engineer engineer = new Engineer();
            engineer.setId(order.getOrderCondition().getEngineer().getId());
            engineer.setName(order.getOrderCondition().getEngineer().getName());
            gradeModel.setEngineer(engineer);

            HashMap<Grade, GradeItem> grades = new HashMap<>();//客评项
            List<OrderGrade> gradeList = Lists.newArrayList();
            List<Grade> gradeitems = gradeService.findAllListCache();//all
            if (gradeitems != null && gradeitems.size() > 0) {
                gradeitems.stream().forEach(grade -> {
                    GradeItem item = grade.getItemList().stream().sorted(Comparator.comparing(GradeItem::getPoint).reversed())
                            .findFirst().orElse(null);
                    if (item != null) {
                        grades.put(grade, item);
                    }
                });
            }
            if (grades.size() > 0) {
                grades.forEach((k, v) -> {
                    OrderGrade orderGrade = new OrderGrade();
                    orderGrade.setGradeId(k.getId());
                    orderGrade.setGradeName(k.getName());
                    orderGrade.setGradeItemId(v.getId());
                    orderGrade.setGradeItemName(v.getRemarks());
                    orderGrade.setPoint(v.getPoint());
                    orderGrade.setSort(k.getSort());
                    gradeList.add(orderGrade);
                });
            }
            gradeModel.setGradeList(gradeList);
            gradeModel.setAutoGradeFlag(OrderUtils.OrderGradeType.APP_GRADE.getValue());
            autoCompleteTransaction(gradeModel, processLog, orderStatusFlag,user);
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.autoComplete]=={}== orderId:{}", index, message.getOrderId(), e);
            message.setStatus(40);
            message.setRemarks(e.getMessage());
            try {
                if (message.getId() == null) {
                    message.setId(SeqUtils.NextID());
                    autoCompleteDao.insert(message);//保存消息
                } else {
                    message.setRetryTimes(1);
                    autoCompleteDao.update(message);
                }
            } catch (Exception e1) {
                //do something other
            }
        }
    }

    /**
     * 保存自动完工(自动完工消息队列调用)
     *
     * @param gradeModel
     * @param processLog
     * @param user
     */
    @Transactional(readOnly = false)
    public void autoCompleteTransaction(OrderGradeModel gradeModel, OrderProcessLog processLog,OrderStatusFlag orderStatusFlag, User user) {
        saveOrderProcessLogNew(processLog);
        saveGrade(gradeModel, orderStatusFlag,user, null, null);
    }


    /**
     * 检查是否可以自动完工
     *
     * @param order
     * @return String, 检查结果，空字符串代表：可以
     * <p>
     * (x)2018/06/04 Ryan (2018/06/06 cancel)
     * 有保险费，时效奖金，不能自动完工
     */
    public String checkAutoComplete(Order order) {
        //1.单服务项目
        if (order.getDetailList().stream().filter(t -> t.getDelFlag() == 0).count() > 1) {
            return "此订单不符合自动完工要求:多个上门服务项";
        }

        OrderDetail detail = order.getDetailList().stream().filter(t -> t.getDelFlag() == 0).findFirst().orElse(null);
        if (detail == null) {
            return "此订单不符合自动完工要求:无上门服务项";
        }
        //2.数量1
        if (detail.getQty() > 1) {
            return "此订单不符合自动完工要求:上门服务项数量大于1";
        }
        //3.下单和上门是否一致
        StringBuilder itemstring = new StringBuilder(100);
        order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getProductId))
                .forEach(t -> {
                    itemstring.append("S#").append(t.getServiceType().getId()).append("#S")
                            .append("P#").append(t.getProduct().getId()).append("#P")
                            .append("Q#").append(t.getQty()).append("#Q");
                });
        //实际上门明细,只读取安装项目
        StringBuilder detailstring = new StringBuilder(200);
        detailstring.append("S#").append(detail.getServiceType().getId()).append("#S")
                .append("P#").append(detail.getProduct().getId()).append("#P")
                .append("Q#").append(detail.getQty()).append("#Q");
        if (!itemstring.toString().equalsIgnoreCase(detailstring.toString())) {
            return "下单的项目与实际上门不一致";
        }

        //4.安装单
        String azCode = new String("II");
        //mark on 2019-10-11
       /* List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        ServiceType azServiceType = serviceTypes.stream().filter(t -> t.getCode().equalsIgnoreCase(azCode)).findFirst().orElse(null);
        if (azServiceType == null) {
            return "此订单不符合自动完工要求:非安装单";
        }*/
        // 调用微服务获取服务类型,只返回id和code start on 2019-10-11
        Map<Long,String> map = serviceTypeService.findIdsAndCodes();
        Long serviceTypeId = map.entrySet().stream().filter(t->t.getValue().equalsIgnoreCase(azCode)).map(t->t.getKey()).findFirst().orElse(null);
        if(serviceTypeId==null || serviceTypeId<=0){
            return "此订单不符合自动完工要求:非安装单";
        }
        //end
        //final Long azServiceTypeId = azServiceType.getId();
        final Long azServiceTypeId = serviceTypeId;
        if (!detail.getServiceType().getId().equals(azServiceTypeId)) {
            return "此订单不符合自动完工要求:非安装单";
        }
        //5.单品
        Product product = productService.getProductByIdFromCache(detail.getProductId());
        if (product == null || product.getSetFlag() == 1) {
            return "此订单不符合自动完工要求:非单品";
        }
        //6.应收金额一致,项次数量一致 才可自动生成对帐单（也有可能出现安装单->维修单）
        OrderFee orderFee = order.getOrderFee();
        if (!Objects.equals(orderFee.getExpectCharge() - orderFee.getCustomerUrgentCharge(), orderFee.getOrderCharge())
                || order.getDetailList().size() != order.getItems().size()) {
            return "此订单不符合自动完工要求:下单项目与实际上门服务项目记录数不一致";
        }
        //7.实际上门明细费用判断，有配件费、其它、远程费、快递费的不能自动对账
        //应收
        if (detail.getMaterialCharge() > 0 || detail.getOtherCharge() > 0
                || detail.getTravelCharge() > 0 || detail.getExpressCharge() > 0) {
            return "此订单不符合自动完工要求:应收有服务费外的其他费用";
        }
        //应付
        if (detail.getEngineerMaterialCharge() > 0 || detail.getEngineerOtherCharge() > 0 ||
                detail.getEngineerTravelCharge() > 0 || detail.getEngineerExpressCharge() > 0) {
            return "此订单不符合自动完工要求:应付有服务费外的其他费用";
        }
        /*8.有保险费，时效奖金，加急费，不能自动完工
        OrderFee fee = order.getOrderFee();
        if(fee != null){
            //保险
            if(fee.getInsuranceCharge() != 0){
                return "此订单不符合自动完工要求:网点有购买保险";
            }
            //网点时效费
            if(fee.getTimeLinessCharge() != 0){
                return "此订单不符合自动完工要求:网点有时效费用";
            }
            //客户应收时效费
            if(fee.getCustomerTimeLinessCharge() != 0){
                return "此订单不符合自动完工要求:客户有应收时效费用";
            }
            //加急费
        }*/
        return "";
    }

    /**
     * 订单标记为自动对账中
     * @param orderId
     * @param quarter
     */
    @Transactional
    public int signAutoChargeing(long orderId,String quarter){
        HashMap<String, Object> conditionParams = Maps.newHashMap();
        conditionParams.put("orderId", orderId);
        conditionParams.put("quarter", quarter);
        conditionParams.put("autoChargeFlag", 3);
        return dao.signAutoChargeing(conditionParams);
    }

    //endregion 客评

    //region 异常处理列表

    @Transactional
    public void saveOrderPending(Long orderId,String quarter, User user) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try{
            //读取订单
            Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单信息错误。");
            }
            OrderCondition condition = order.getOrderCondition();
            long servicePointId = Optional.ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
            if(servicePointId == 0){
                throw new OrderException("读取订单网点信息错误。");
            }
            long cnt = 0;
            List<OrderDetail> details = order.getDetailList();
            if(!CollectionUtils.isEmpty(details)){
                cnt = details.stream().filter(t -> t.getDelFlag() == 0 && t.getServicePoint().getId().longValue() == servicePointId).count();
            }
            //好评单
            OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(orderId, order.getQuarter());
            if (orderStatusFlag !=null && orderStatusFlag.getPraiseStatus() == PraiseStatusEnum.APPROVE.code && cnt == 0) {
                throw new OrderException("当前网点无上门服务,请先取消好评");
            }
            //当前网点是否有上门服务
            boolean hasServiceItems = false;
            if(orderStatusFlag !=null && orderStatusFlag.getPraiseStatus() > 0 && cnt > 0) {
                hasServiceItems = true;
            }
            saveOrderPendingToDb(order,orderStatusFlag,servicePointId,hasServiceItems,user);
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 异常处理提交前，读取及处理当前网点好评费
     * @param orderId
     * @param quarter
     * @param servicePointId   当前网点
     * @param hasServiceItems  当前网点有无上门服务
     * @return  好评费
     */
    @Transactional
    public double processPraiseFee(Long orderId,String quarter,Long servicePointId,boolean hasServiceItems,long updateBy,long updateAt){
        Double praiseFee = 0.00;
        //当前网点无上门服务,好评费无效
        if(!hasServiceItems){
            orderServicepointReceivableService.switchEnabled(
                    orderId,
                    quarter,
                    servicePointId,
                    ReceivablePayableItemEnum.Praise.code,
                    1,
                    updateBy,
                    updateAt
            );
            return praiseFee;
        }
        //1.读取当前网点好评费
        List<OrderServicepointReceivable> spReceivables = orderServicepointReceivableService.getByOrderId(orderId,quarter, ReceivablePayableItemEnum.Praise.code,servicePointId);
        if(!CollectionUtils.isEmpty(spReceivables)){
            OrderServicepointReceivable spReceivable = spReceivables.get(0);
            praiseFee = spReceivable.getAmount();
            spReceivable = null;
            spReceivables.clear();
        }
        spReceivables = null;
        return praiseFee;
    }

    /**
     * 完成异常处理
     * pendingFlag标记为：3
     */
    @Transactional(readOnly = false)
    public void saveOrderPendingToDb(Order order,OrderStatusFlag orderStatusFlag,long servicePointId,boolean hasServiceItems, User user) {
        try {
            Date date = new Date();
            //有好评单
            if(orderStatusFlag != null && orderStatusFlag.getPraiseStatus() == PraiseStatusEnum.APPROVE.code) {
                OrderFee orderFee = order.getOrderFee();
                Double prePriaseFee = orderFee.getEngineerPraiseFee();
                //好评费重新读取并汇总
                Double praiseFee = processPraiseFee(order.getId(), order.getQuarter(), servicePointId, hasServiceItems, user.getId(), date.getTime());
                if (praiseFee != 0 || prePriaseFee != 0) {
                    orderFeeService.updatePraiseFee(order.getId(), order.getQuarter(), null, praiseFee);
                    orderServicePointFeeService.updatePraiseFee(order.getId(), order.getQuarter(), servicePointId, praiseFee);
                }
            }
            OrderCondition condition = order.getOrderCondition();
            condition.setPendingFlag(Order.ORDER_PENDDING_FLAG_RENEW);
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("pendingFlag", condition.getPendingFlag());
            params.put("updateBy", user);
            params.put("updateDate", date);
            dao.updateCondition(params);

            //log
            Dict status = condition.getStatus();
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("异常处理完成");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("订单异常处理完成,处理人:%s", user.getName()));
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_PENDINGED);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //关闭异常单
            AbnormalForm abnormalForm = new AbnormalForm();
            abnormalForm.setOrderId(order.getId());
            abnormalForm.setQuarter(order.getQuarter());
            abnormalForm.setCloseBy(user.getId());
            abnormalForm.setCloseAt(date.getTime());
            abnormalFormService.closeReviewAbnormal(abnormalForm);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .incrVersion(1L)
                    .setCondition(condition)
                    .setPendingFlag(Order.ORDER_PENDDING_FLAG_RENEW)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            //LogUtils.saveLog("完成异常处理", "OrderService.saveOrderPending", orderId.toString(), e, user);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    //endregion 异常处理

    //region 公共部分

    /**
     * 从redis中获得订单号
     * 加入重试机制
     */
    public String getNewOrderNo() {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(4);//4次，包含本次调用
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        //重试补偿
        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(1000);
        exponentialBackOffPolicy.setMultiplier(2);
        exponentialBackOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
        String result = retryTemplate.execute(new RetryCallback<String, RuntimeException>() {
            // 重试操作
            @Override
            public String doWithRetry(RetryContext retryContext) throws RuntimeException {
                //log.info("retry {} times.", retryContext.getRetryCount());
                return SeqUtils.NextOrderNo();
            }
        }, new RecoveryCallback<String>() { //兜底回调
            @Override
            public String recover(RetryContext retryContext) throws RuntimeException {
                Throwable t = retryContext.getLastThrowable();
                if (t != null) {
                    log.error("after retry {} times, recovery method called!", retryContext.getRetryCount(), t);
                }
                return "";
            }
        });
        return result;
    }

    /**
     * 获得订单产品列表
     *
     * @param items      订单项
     * @param spliteSet  套组是否拆分
     * @param includeSet 是否返回套组id
     * @return
     */
    public Set<Product> getOrderProducts(List<OrderItem> items, Boolean spliteSet, Boolean includeSet) {
        Set<Product> ids = Sets.newHashSet();
        if (items == null || items.size() == 0) {
            return ids;
        }
        StringBuffer sb = new StringBuffer();
        items.stream().map(t -> t.getProduct().getId()).distinct().forEach(
                t -> {
                    Product p = productService.getProductByIdFromCache(t);
                    if (p != null) {
                        if (p.getSetFlag() == 1) {//set
                            if (includeSet) {
                                ids.add(p);
                            }
                            if (spliteSet) {
                                final String[] sids = p.getProductIds().split(",");
                                for (String id : sids) {
                                    if (StringUtils.isNoneBlank(id)) {
                                        Product p1 = productService.getProductByIdFromCache(Long.valueOf(id));
                                        if (p1 != null) {
                                            ids.add(p1);
                                        }
                                    }
                                }
                            }
                        } else {
                            ids.add(p);
                        }
                    }
                }
        );
        return ids;
    }

    /**
     * 获得下单时订单项产品是套组，且包含指定产品
     *
     * @param productId 组成产品
     * @param items     订单项
     * @return
     */
    public long[] getSetProductIdIncludeMe(Long productId, List<OrderItem> items) {
        List<Long> ids = Lists.newArrayList();
        if (productId == null || productId <= 0 || items == null || items.size() == 0) {
            return new long[]{};
        }
        StringBuffer sb = new StringBuffer();
        items.stream().map(t -> t.getProduct().getId()).distinct().forEach(
                t -> {
                    Product p = productService.getProductByIdFromCache(t);
                    if (p.getSetFlag() == 1) {
                        sb.setLength(0);
                        sb.append(",".concat(p.getProductIds()).concat(","));
                        if (sb.toString().contains(String.format(",%s,", productId.toString()))) {
                            ids.add(t);
                        }
                    }
                }
        );
        return ids.stream().mapToLong(i -> i).toArray();
    }

    /**
     * 返回变更前的订单状态
     */
    public Dict getLastOrderLog(Long orderId, String quarter, String value) {
        List<Dict> status = dao.getLastOrderLog(orderId, quarter);
        if (status == null || status.size() == 0) {
            return null;
        }
        Dict dict = status.stream().sorted(Comparator.comparingLong(Dict::getId))
                .filter(t -> t.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
        if (dict == null) {
            return null;
        }
        final Long id = dict.getId();
        dict = status.stream()
                .sorted(Comparator.comparing(Dict::getId).reversed())
                .filter(t -> t.getId() < id && t.getValue() != value)
                .findFirst()
                .orElse(null);
        if (dict == null) {
            return null;
        }
        return dict;
    }

    /**
     * 检查订单版本是否正确
     * 如传递的值与缓存中不一致，代表订单已被修改或操作
     *
     * @param orderId 订单id
     * @param version 版本
     * @return
     */
    public Boolean orderVersionValid(Long orderId, Long version) {
        String key = String.format(RedisConstant.SD_ORDER, orderId);
        Long current = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SD_DB, key, "version", Long.class);
        return current != null && Objects.equals(version, current);
    }

    /**
     * 返回APP需要的日志
     */
    public List<OrderProcessLog> getAppOrderLogs(Long orderId, String quarter) {
        List<OrderProcessLog> list = dao.getAppOrderLogs(orderId, quarter);
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

    /**
     * 按订单id及标志列表(多个)返回日志
     *
     * @param orderId
     * @param statusFlags
     * @return
     */
    public List<OrderProcessLog> getOrderLogsByFlags(Long orderId, String
            quarter, List<Integer> statusFlags, Integer closeFlag) {
        if (orderId == null || orderId <= 0) {
            return Lists.newArrayList();
        }
        List<OrderProcessLog> list = dao.getOrderLogsByFlags(orderId, quarter, statusFlags, closeFlag);
        //user微服务
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

    /**
     * 读取订单分片
     *
     * @param orderId
     * @return
     */
    public String getOrderQuarterFromCache(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return "";
        }
        String key = String.format(RedisConstant.SD_ORDER, orderId);
        String quarter = new String("");
        try {
            if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB, key)) {
                quarter = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SD_DB, key, "quarter", String.class);
            }
            if (StringUtils.isBlank(quarter)) {
                quarter = orderHeadDao.getOrderQuarter(orderId);//2020-12-17 sd_order -> sd_order_head
            }

            return StringUtils.isBlank(quarter) ? "" : quarter;

        } catch (Exception e) {
        }
        return quarter;
    }

    /**
     * 保存工单日志（计算日志可见性标记值）
     */
    @Transactional(readOnly = false)
    public void saveOrderProcessLogNew(OrderProcessLog processLog) {
        if (processLog.getVisibilityFlag() == null || processLog.getVisibilityFlag() == VisibilityFlagEnum.NONE.getValue()) {
            int visiblityValue = OrderUtils.calcProcessLogVisibilityFlag(processLog);
            processLog.setVisibilityFlag(visiblityValue);
        }
        saveOrderProcessLogWithNoCalcVisibility(processLog);
    }

    /**
     * 保存工单日志（不计算日志可见性标记值）
     */
    @Transactional(readOnly = false)
    public void saveOrderProcessLogWithNoCalcVisibility(OrderProcessLog processLog) {
        saveProcessLogToDB(processLog);
        b2BCenterOrderProcessLogService.pushOrderProcessLogToMS(processLog);
    }

    /**
     * 将日志写入数据库
     */
    @Transactional(readOnly = false)
    public void saveProcessLogToDB(OrderProcessLog processLog) {
        dao.insertProcessLog(processLog);
    }

    /**
     * 根据客户id+电话号码从缓存中读取重复订单号
     *
     * @param customerId
     * @param phone
     * @return Sting
     */
    public String getRepeateOrderNo(long customerId, String phone) {
        if (customerId == 0 || StringUtils.isBlank(phone)) {
            return null;
        }
        String key = MessageFormat.format(OrderTaskService.CHECK_REPEATE_ORDER_KEY, String.valueOf(customerId));
        String orderNo = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, phone.trim(), String.class);
        return orderNo;
    }

    /**
     * 下单时更新重单检查缓存为最新订单号
     * 取消单时，暂不更新
     * 在CreateOrderPushMessageRetryReceiver及Retry中消费成功后调用此方法
     *
     * @param customerId
     * @param phone
     * @param newOrderNo
     */
    public void setNewRepeateOrderNo(long customerId, String phone, String newOrderNo) {
        if (customerId == 0 || StringUtils.isBlank(phone)) {
            return;
        }
        try {
            String key = MessageFormat.format(OrderTaskService.CHECK_REPEATE_ORDER_KEY, String.valueOf(customerId));
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, phone.trim(), newOrderNo, 0l);
        } catch (Exception e) {
            log.error("更新重单检查缓存错误,customer id:{},phone:{},orderNo:{}", customerId, phone, newOrderNo, e);
        }
    }

    /**
     * 返回产品及其服务项目组成的键值对列表
     * @param items 服务项目列表
     * @return
     */
    public List<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>> getOrderDetailProductAndServiceTypePairs(List<OrderDetail> items){
        if(CollectionUtils.isEmpty(items)){
            return null;
        }
        Set<com.kkl.kklplus.entity.common.NameValuePair<Long, Long>> valuePairs = items.stream().map(t -> {
            return new com.kkl.kklplus.entity.common.NameValuePair<Long, Long>(t.getProductId(), t.getServiceType().getId());
        }).collect(Collectors.toSet());
        return new ArrayList<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>>(valuePairs);
    }

    /**
     * 返回产品及其服务项目组成的键值对列表
     * @param items 服务项目列表
     * @return
     */
    public List<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>> getOrderItemProductAndServiceTypePairs(List<OrderItem> items){
        if(CollectionUtils.isEmpty(items)){
            return null;
        }
        Set<com.kkl.kklplus.entity.common.NameValuePair<Long, Long>> valuePairs = items.stream().map(t -> {
            return new com.kkl.kklplus.entity.common.NameValuePair<Long, Long>(t.getProductId(), t.getServiceType().getId());
        }).collect(Collectors.toSet());
        return new ArrayList<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>>(valuePairs);
    }

    //endregion

    //region 财务

    /**
     * 对帐标记订单异常
     *
     * @param orderIds
     * @param comment
     */
    @Transactional(readOnly = false)
    public void setPending(String orderIds, String comment,Integer auditType) {
        User updateBy = UserUtils.getUser();
        Date updateDate = new Date();
        String key = new String("");
        List<AbnormalForm> abnormalFormList = Lists.newArrayList();
        Dict dict = MSDictUtils.getDictByValue(String.valueOf(auditType),"fi_charge_audit_type");
        String actionComment = "";
        if(dict!=null){
            if(StringUtils.isNotBlank(comment)){
                actionComment = dict.getLabel() + ","+comment;
            }else {
                actionComment = dict.getLabel();
            }
        }
        for (String orderId : orderIds.split(",")) {
            Order order = getOrderById(Long.parseLong(orderId), "", OrderUtils.OrderDataLevel.CONDITION, true);
            //检查是否已经生成对账单
            if (order != null && order.getOrderCondition() != null && 1 == order.getOrderCondition().getChargeFlag()) {
                continue;
            }
            // jeff 检查订单是否修改，如果修改如何抛出异常回滚
            dao.setPending(Long.parseLong(orderId), updateBy.getId(), updateDate);
            //log
            if (order != null && order.getOrderCondition() != null) {
                OrderProcessLog processLog = new OrderProcessLog();
                processLog.setQuarter(order.getQuarter());
                processLog.setAction("标记异常单");
                processLog.setOrderId(Long.parseLong(orderId));
                processLog.setActionComment(StringUtils.left(actionComment,255));
                processLog.setStatus(MSDictUtils.getDictLabel(Order.ORDER_STATUS_COMPLETED.toString(), "order_status", "完成"));//切换为微服务
                processLog.setStatusValue(Order.ORDER_STATUS_COMPLETED);
                processLog.setStatusFlag(OrderProcessLog.OPL_SF_PENDDING);
                processLog.setCloseFlag(1);
                processLog.setCreateBy(updateBy);
                processLog.setCreateDate(updateDate);
//                dao.insertProcessLog(processLog);
                processLog.setCustomerId(order.getOrderCondition().getCustomerId());
                processLog.setDataSourceId(order.getDataSourceId());
                saveOrderProcessLogNew(processLog);

                AbnormalForm abnormalForm =null;
                AppFeedbackEnum.Channel channel = AppFeedbackEnum.Channel.ORDER;
                AbnormalFormEnum.FormType formType = AbnormalFormEnum.FormType.REVIEW_ABNORMALY;
                String reason = "";
                if(dict!=null){
                    reason = dict.getLabel();
                }
                abnormalForm = abnormalFormService.handleAbnormalForm(order,comment,updateBy,channel.getValue(),formType.getCode(), auditType,"");
                if(abnormalForm!=null){
                    abnormalFormList.add(abnormalForm);
                }
            }
            // jeff 更新订单缓存 pending_flag
            key = String.format(RedisConstant.SD_ORDER, orderId);
            //订单缓存失效
            //redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SD_DB, key, "condition");//ryan
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(Long.parseLong(orderId))
                    .setDeleteField(OrderCacheField.CONDITION);
            OrderCacheUtils.update(builder.build());

        }
        if(abnormalFormList!=null && abnormalFormList.size()>0){
            try {
                abnormalFormService.insertBatch(abnormalFormList);
            }catch (Exception e){
                log.error("财务对账标记异常保存异常单失败 form{}",GsonUtils.getInstance().toGson(abnormalFormList),e);
                throw new RuntimeException("财务对账标记异常保存异常单失败:" + e.getMessage());
            }
        }
    }

    //endregion 财务

    //region 网点

    /**
     * 网点派单给安维人员
     *
     * @param order
     */
    @Transactional(readOnly = false)
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
            o = getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        } catch (Exception e) {
            //LogUtils.saveLog("网点派单错误", "OrderService.servicePointPlanOrder.getOrderById", "order id:" + order.getId().toString(), e, user);
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
            dao.updateCondition(params);

            //log,派单
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(o.getQuarter());
            processLog.setAction("派单");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("安维网点派单给安维人员:%s,操作人:%s,备注:%s", engineer.getName(), user.getName(), order.getRemarks()));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(o.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);
            //更新接单数安维
            //新师傅
            params.put("id", engineer.getId());//新安维帐号
            params.put("planCount", 1);//派单数+1
            params.put("updateBy", user);
            params.put("updateDate", date);
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            msEngineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务

            //原师傅
            if (oldEngineerId != null) {
                params.remove("id");
                params.put("id", oldEngineerId);//原安维帐号
                params.remove("planCount");
                params.put("planCount", -1);//派单数-1
            }
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            msEngineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务

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
            Integer nextPlanTimes = dao.getOrderPlanMaxTimes(o.getId(), o.getQuarter());
            if (nextPlanTimes == null) {
                nextPlanTimes = 1;
            } else {
                nextPlanTimes++;//+1
            }
            //prev
            OrderPlan preOrderPlan = dao.getOrderPlan(o.getId(), o.getQuarter(), servicePointId, oldEngineerId);
            Double serviceCost = 0.0;
            if (preOrderPlan == null) {
                //throw new RuntimeException("读取派单记录错误，请重试");
                serviceCost = calcServicePointCost(rediscondition,order.getOrderCondition().getServicePoint(), o.getItems());
            } else {
                serviceCost = preOrderPlan.getEstimatedServiceCost();
            }
            OrderPlan orderPlan = dao.getOrderPlan(o.getId(), o.getQuarter(), servicePointId, engineer.getId());
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

                dao.insertOrderPlan(orderPlan);
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
                dao.UpdateOrderPlan(planMaps);
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

            //region 消息队列
            // 短信通知
            // 发送用户短信
            //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
            List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
            if (!ignoreDataSources.contains(o.getDataSource().getValue()) && order.getSendUserMessageFlag() != null && order.getSendUserMessageFlag() == 1) {
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
                        for(int i=0,size=o.getItems().size();i<size;i++){
                            item = o.getItems().get(i);
                            userContent
                                    .append(item.getBrand())
                                    .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                    .append(item.getQty())
                                    .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                    .append(item.getServiceType().getName())
                                    .append((i==(size-1))?"":" ");
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
                        for(int i=0,size=o.getItems().size();i<size;i++){
                            item = o.getItems().get(i);
                            userContent
                                    .append(item.getBrand())
                                    .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                    .append(item.getQty())
                                    .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                    .append(item.getServiceType().getName())
                                    .append((i==(size-1))?"":" ");
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
                    LogUtils.saveLog(
                            "网点派单-发送短信失败",
                            "OrderService.servicePointPlanOrder",
                            MessageFormat.format("mobile:{0},content:{1},triggerBy:{2},triggerDate:{3}", rediscondition.getServicePhone(), userContent.toString(), user.getId(), date.getTime()),
                            e,
                            user
                    );
                    //log.error("[OrderService.servicePointPlanOrder] send engineer short message,orderId:{} ,engineerId:{}", order.getId(), engineer.getId(), e);
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

            //region 网点订单更新  2019-03-25
            servicePointOrderBusinessService.changeEngineer(o.getId(),o.getQuarter(),servicePointId,engineerId,engineer.getMasterFlag(),user.getId(),date.getTime());
            //endregion

            //endregion 消息队列


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
     * app网点接单
     * order.orderCondition.engineer,servicePoint,customer 在传入前，需从缓存中获得完整内容
     * <p>
     * 主帐号才能接单，且appFlag=1
     *
     * @param order
     */
    @Transactional(readOnly = false)
    public void grabOrder(Order order, User user, Engineer engineer) {
        if (order == null || order.getId() == null) {
            throw new OrderException("接单失败：参数无值。");
        }
        if (user == null || engineer == null) {
            throw new OrderException("接单失败：无帐号信息。");
        }
        if (engineer.getServicePoint() == null) {
            throw new OrderException("接单失败：无网点信息。");
        }
        //锁
        long releaseLockTime = 5;//5秒后锁过期
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新订单。");
        }

        try {
            //再取订单状态
            Map<String, Object> conMap = dao.getOrderConditionSpecialFromMasterById(order.getId(), order.getQuarter());
            Integer statusValue = (Integer) conMap.get("status_value");
            if (statusValue >= Order.ORDER_STATUS_PLANNED) {
                throw new OrderException("接单失败,订单已被其他网点接单");
            }
            Order o = getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.STATUS,true);
            if (o == null || o.getOrderCondition() == null){
                throw new OrderException("接单失败,读取订单错误，请重试");
            }
            OrderCondition condition = order.getOrderCondition();
            String key = String.format(RedisConstant.SD_ORDER, order.getId());
            Integer prevStatus = order.getOrderCondition().getStatusValue();
            Date date = new Date();
            HashMap<String, Object> params = Maps.newHashMap();

            Dict status = MSDictUtils.getDictByValue(Order.ORDER_STATUS_PLANNED.toString(), "order_status");//切换为微服务
            if (status == null) {
                status = new Dict(Order.ORDER_STATUS_PLANNED.toString(), "已派单");
            }
            ServicePoint servicePoint = engineer.getServicePoint();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("operationAppFlag", 1);//app接单
            params.put("servicePoint", servicePoint);
            params.put("engineer", engineer);
            params.put("status", status);
            params.put("updateBy", user);
            params.put("updateDate", date);
            //网点接单时，appointment_date=null、pending_type = 0、reservation_date = pending_type_date = now、sub_status = 10
            params.put("resetAppointmentDate", true);
            params.put("pendingType", new Dict(0, ""));
            params.put("reservationDate", date);
            params.put("pendingTypeDate", date);
            params.put("subStatus", Order.ORDER_SUBSTATUS_PLANNED);//Add by Zhoucy
            // 突击单关闭 Add by Ryan
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                params.put("rushOrderFlag", 2);
            }
            dao.updateCondition(params);
            //fee
            params.clear();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("engineerPaymentType", servicePoint.getFinance().getPaymentType());//安维付款方式
            dao.updateFee(params);

            //Status
            params.clear();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("acceptDate", date);
            params.put("planBy", user);
            params.put("planDate", date);
            params.put("planComment", "安维接单派单");
            dao.updateStatus(params);

            // log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setOrderId(order.getId());
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("安维接单派单");
            processLog.setActionComment(String.format("安维接单派单:%s,操作人:%s", order.getOrderNo(), user.getName()));
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Order.ORDER_STATUS_PLANNED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //更新接单数(网点,安维)
            params.clear();
            params.put("id", servicePoint.getId());
            params.put("planCount", 1);//派单数+1
            params.put("updateBy", user);
            params.put("updateDate", date);
            //servicePointDao.updateServicePointByMap(params);//mark on 2020-1-17   web端去md_servicepoint
            //servicePointService.updateServicePointByMap(params); // add on 2019-10-4  //mark on 2020-1-16 集中调用MD微服务
            HashMap<String,Object> paramsForServicePoint = Maps.newHashMap();  // add on 2020-1-16
            paramsForServicePoint.put("id", servicePoint.getId()); // add on 2020-1-16
            paramsForServicePoint.put("planCount", 1);//派单数+1    // add on 2020-1-16

            //安维
            params.remove("id");
            params.put("id", engineer.getId());
            //servicePointDao.updateEngineerByMap(params);  //mark on 2020-1-13 web端去除md_engineer
            //msEngineerService.updateEngineerByMap(params);  // add on 2019-10-18 //Engineer微服务
            HashMap<String,Object> paramsForEngineer = Maps.newHashMap();  // add on 2020-1-16
            paramsForEngineer.put("planCount", 1);//派单数+1    // add on 2020-1-16
            paramsForEngineer.put("id", engineer.getId());     // add on 2020-1-16

            //cache
            servicePoint.setPlanCount(servicePoint.getPlanCount() + 1);
            //servicePointService.updateServicePointCache(servicePoint);//mark on 2020-1-17   web端去md_servicepoint

            //engineer planCount+1
            engineer.setPlanCount(engineer.getPlanCount() + 1);
            //servicePointService.updateEngineerCache(engineer);//mark on 2020-1-17   web端去md_servicepoint

            //派单记录表 2018/01/24
            Integer nextPlanTimes = dao.getOrderPlanMaxTimes(order.getId(), order.getQuarter());
            if (nextPlanTimes == null) {
                nextPlanTimes = 1;
            } else {
                nextPlanTimes++;//+1
            }
            OrderPlan orderPlan = dao.getOrderPlan(order.getId(), order.getQuarter(), servicePoint.getId(), engineer.getId());
            if (orderPlan == null || orderPlan.getId() == null) {
                String insuranceNo = new String("");
                Double insuranceAmount = 0.0;
//                if (servicePoint.getInsuranceFlag() == 1) {
                if (ServicePointUtils.servicePointInsuranceEnabled(servicePoint)) {
                    //保险费
                    List<Long> categorids = order.getItems().stream().filter(t -> t.getDelFlag() == 0).map(t -> t.getProduct().getCategory().getId()).distinct().collect(Collectors.toList());
                    insuranceAmount = getOrderInsuranceAmount(categorids);
                    if (insuranceAmount == null) {
                        //throw new RuntimeException("请确认产品类别是否设置了保险费！");
                        insuranceAmount = 0.0;
                    }
                    //保险费大于0，才生成保单
                    if (insuranceAmount > 0) {
                        //保险单号
                        insuranceNo = SeqUtils.NextSequenceNo("orderInsuranceNo");
                        if (StringUtils.isBlank(insuranceNo)) {
                            insuranceNo = SeqUtils.NextSequenceNo("orderInsuranceNo");
                            if (StringUtils.isBlank(insuranceNo)) {
                                throw new RuntimeException("生成工单保险单号错误");
                            }
                        }
                    }
                }
                orderPlan = new OrderPlan();
                //orderPlan.setId(SeqUtils.NextID());
                orderPlan.setQuarter(order.getQuarter());
                orderPlan.setOrderId(order.getId());
                orderPlan.setServicePoint(servicePoint);
                orderPlan.setEngineer(engineer);
                orderPlan.setIsMaster(engineer.getMasterFlag());//*
                orderPlan.setPlanTimes(nextPlanTimes);//*
                orderPlan.setCreateBy(user);
                orderPlan.setCreateDate(date);
                orderPlan.setUpdateBy(new User(0l));
                //距离
                orderPlan.setEstimatedDistance(0.00d);
                //服务费
                Double amount = calcServicePointCost(condition,servicePoint, order.getItems());
                orderPlan.setEstimatedServiceCost(amount);
                dao.insertOrderPlan(orderPlan);
//                if (servicePoint.getInsuranceFlag() == 1 && insuranceAmount > 0) {
                if (ServicePointUtils.servicePointInsuranceEnabled(servicePoint) && insuranceAmount > 0) {
                    //保险单
                    OrderInsurance orderInsurance = new OrderInsurance();
                    orderInsurance.setAmount(insuranceAmount);
                    orderInsurance.setInsuranceNo(insuranceNo);
                    orderInsurance.setOrderId(order.getId());
                    orderInsurance.setOrderNo(order.getOrderNo());
                    orderInsurance.setQuarter(order.getQuarter());
                    orderInsurance.setServicePointId(servicePoint.getId());
                    Engineer primary = servicePoint.getPrimary();
                    orderInsurance.setAssured(primary.getName());
                    orderInsurance.setPhone(primary.getContactInfo());
                    orderInsurance.setAddress(primary.getAddress());
                    orderInsurance.setInsureDate(date);
                    orderInsurance.setInsuranceDuration(12);//投保期限12个月
                    orderInsurance.setCreateBy(user);
                    orderInsurance.setCreateDate(date);
                    dao.insertOrderInsurance(orderInsurance);
                    //网点费用,默认无效
                    OrderServicePointFee servicePointFee = mapper.map(orderInsurance, OrderServicePointFee.class);
                    servicePointFee.setServicePoint(servicePoint);
                    dao.insertOrderServicePointFee(servicePointFee);
                    //不更新OrderFee.insuranceCharge
                } else {
                    //Service Point Fee
                    OrderServicePointFee servicePointFee = new OrderServicePointFee();
                    servicePointFee.setServicePoint(servicePoint);
                    servicePointFee.setOrderId(order.getId());
                    servicePointFee.setQuarter(order.getQuarter());
                    dao.insertOrderServicePointFee(servicePointFee);
                }
            } else {
                HashMap<String, Object> planMaps = Maps.newHashMap();
                planMaps.put("id", orderPlan.getId());
                planMaps.put("planTimes", nextPlanTimes);
                planMaps.put("updateBy", user);
                planMaps.put("updateDate", date);
                dao.UpdateOrderPlan(planMaps);
            }

            //关闭突击单
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                crushService.closeOrderCurshByOrderId(order.getId(),order.getQuarter(),1,null,user,date);
            }
            condition.setRushOrderFlag(2);

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(builder.build());

            //add on 2020-1-16 begin  集中处理MD微服务调用
            servicePointService.updateServicePointByMap(paramsForServicePoint);
            msEngineerService.updateEngineerByMap(paramsForEngineer);
            //add on 2020-1-16 end

//            if (order.getDataSource() != null) {
            //派单时通知B2B
//                b2BCenterOrderService.planB2BOrder(order.getDataSource().getIntValue(), order.getWorkCardId(),order.getId(), order.getOrderNo(), engineer.getId(), engineer.getName(), engineer.getContactInfo(), user, date);
            b2BCenterOrderService.planOrder(order, engineer, user, date);
//            }

            //region 短信通知
            //检查客户短信发送开关，1:才发送
            Customer customer = null;
            try {
                customer = customerService.getFromCache(condition.getCustomer().getId());
            } catch (Exception e) {
                LogUtils.saveLog("网点接单:检查客户短信开关异常", "OrderService.grabOrder", order.getId().toString(), e, user);
            }
            //发送短信 1.未取到客户信息 2.取到，且短信发送标记为：1
            //未在配置中：shortmessage.ignore-data-sources  //2018-12-05
            List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
            if (!ignoreDataSources.contains(order.getDataSource().getValue()) && (customer == null || (customer != null && customer.getShortMessageFlag() == 1))) {
                StringBuffer userContent = new StringBuffer(250);
                try {

                    userContent.append("您的");
                    OrderItem item;
                    for(int i=0,size=o.getItems().size();i<size;i++){
                        item = o.getItems().get(i);
                        userContent
                                .append(item.getBrand())
                                .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                .append(item.getQty())
                                .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                .append(item.getServiceType().getName())
                                .append((i==(size-1))?"":" ");
                    }
                    userContent.append("，");
                    userContent.append(engineer.getName().substring(0, 1));
                    userContent.append("师傅").append(engineer.getContactInfo()).append("已接单,");
                    if (condition.getKefu() != null) {
                        userContent
                                .append("客服")
                                .append(condition.getKefu().getName().substring(0, 1)).append("小姐")
                                .append(condition.getKefu().getPhone())
                                .append("/");
                    }
                    userContent.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
                    // 使用新的短信发送方法 2019/02/28
                    smsMQSender.sendNew(condition.getServicePhone(),
                            userContent.toString(),
                            "",
                            user.getId(),
                            date.getTime(),
                            SysSMSTypeEnum.ORDER_ACCEPTED_APP
                    );
                } catch (Exception e) {
                    LogUtils.saveLog(
                            "网点接单-发送短信失败",
                            "OrderService.grabOrder",
                            MessageFormat.format("mobile:{0},content:{1},triggerBy:{2},triggerDate:{3}", condition.getServicePhone(), userContent.toString(), user.getId(), date.getTime()),
                            e,
                            user
                    );
                }
            }

            //region 网点订单数据更新 2019-03-25
            OrderCondition oc = o.getOrderCondition();
            OrderStatus orderStatus = o.getOrderStatus();
            int orderChannel = ofNullable(o.getOrderChannel()).map(Dict::getIntValue).orElse(0);
            MQOrderServicePointMessage.ServicePointMessage.Builder spMsgBuilder = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                    .setOperationType(MQOrderServicePointMessage.OperationType.Create)
                    .setOrderChannel(orderChannel)
                    .setDataSource(o.getDataSourceId())
                    .setOrderId(order.getId())
                    .setQuarter(order.getQuarter())
                    .setSubStatus(Order.ORDER_SUBSTATUS_PLANNED)
                    .setOperationAt(date.getTime())
                    .setOperationBy(user.getId())
                    .setResetAppointmentDate(1)//重置null
                    .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                            .setOrderNo(o.getOrderNo())
                            .setOrderServiceType(oc.getOrderServiceType())
                            .setAreaId(oc.getArea().getId())
                            .setAreaName(oc.getArea().getName())
                            .setStatus(status.getIntValue())
                            .build())
                    .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                            .setServicePointId(servicePoint.getId())
                            .setEngineerId(servicePoint.getPrimary().getId())
                            .setPrevServicePointId(0)
                            .setPlanOrder(1)
                            .setPlanType(OrderServicePoint.PlanType.APP.ordinal())
                            .build())
                    .setUserInfo(MQOrderServicePointMessage.UserInfo.newBuilder()
                            .setUserName(oc.getUserName())
                            .setPhone(oc.getServicePhone())
                            .setAddress(oc.getServiceAddress())
                            .build())
                    .setPlanDate(date.getTime())
                    .setReservationDate(date.getTime())
                    .setPendingType(0)
                    .setMasterFlag(1) // 主账号
                    .setAbnormalyFlag(oc.getAppAbnormalyFlag())
                    .setUrgentLevelId(oc.getUrgentLevel().getId().intValue())
                    .setReminderFlag(orderStatus.getReminderStatus())
                    .setComplainFlag(orderStatus.getComplainFlag());
            servicePointOrderBusinessService.planOrder(spMsgBuilder);
            //endregion

            //endregion 消息队列


        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.grabOrder] orderId:{}", order.getId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                if (releaseLockTime > 0) {
                    redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, releaseLockTime);
                } else {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
                }
            }
        }
    }

    //endregion 网点

    //region 投诉

    @Transactional
    public void updateComplainInfo(HashMap<String,Object> parmas){
        dao.updateComplainInfo(parmas);
    }

    //endregion 投诉

    //region 短信&APP Notic

    /**
     * 下单后，订单通过审核后触发
     * 1.按区域id获得网点主帐号信息（User）
     * 2.发送短信 (取消，2017/09/01)
     * 3.发送APP Notice
     *
     * @date 2020-01-01
     * 京东优易+不自动派单
     *
     * @param order 订单
     * @param user  触发者
     */
    private void sendNewOrderMsg(Order order, List<User> engineers, User user) {
        //京东优易+不自动派单 2021-01-01
        if(order.getDataSourceId() == B2BDataSourceEnum.JDUEPLUS.getId()) {
            return;
        }
        Date date = new Date();

        if (engineers != null && engineers.size() > 0) {
            // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
            StringBuffer content = new StringBuffer();
            content.append("师傅，在您附近有一张  ");
            for (OrderItem item : order.getItems()) {
                content.append(item.getServiceType().getName())
                        .append(item.getBrand())
                        .append(item.getProduct().getName());
            }
            content.append("的工单，请尽快登陆APP接单~");

            MQSysShortMessage.SysShortMessage.Builder shortMessageBuilder = MQSysShortMessage.SysShortMessage.newBuilder();
            shortMessageBuilder
                    .setSendTime(date.getTime())
                    .setId(0)
                    .setExtNo(Message2.EXTNO_DEFAULT)
                    .setTriggerBy(user.getId())
                    .setTriggerDate(date.getTime())
                    .setType("pt")
                    .setSmsType(SysSMSTypeEnum.ORDER_CREATED.getValue());// 短信类型

            MQPushMessage.PushMessage.Builder pushBuilder = MQPushMessage.PushMessage.newBuilder();
            pushBuilder.setPushMessageType(MQPushMessage.PushMessageType.Notification)
                    .setMessageType(0)
                    .setSubject("")
                    .setContent("")
                    .setTimestamp(System.currentTimeMillis());
            MQSysShortMessage.SysShortMessage shortMessage;
            for (User engineer : engineers) {
                //手机接单权限
                if (engineer.getAppFlag() == 0) {
                    continue;
                }
                /*短信 */
                if (engineer.getShortMessageFlag() == 1) {
                    shortMessageBuilder.setMobile(engineer.getMobile());
                    shortMessageBuilder.setContent(engineer.getName().substring(0, 1).concat(content.toString()));
                    shortMessage = shortMessageBuilder.build();
                    try {
                        smsMQSender.send(shortMessage);// 使用新的短信发送方法 2019/02/28
                    } catch (Exception e) {
                        String json = GsonUtils.getInstance().toGson(shortMessage);
                        log.error("[OrderService.senNewOrderMsg] engineer sms - ", json, e);
                    }
                }

                // 发送APP消息
                // 张三师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~
                try {
                    //将推送切换为微服务
                    AppPushMessage pushMessage = new AppPushMessage();
                    pushMessage.setPassThroughType(AppPushMessage.PassThroughType.NOTIFICATION);
                    pushMessage.setMessageType(AppMessageType.ACCEPTORDER);
                    pushMessage.setSubject("");
                    pushMessage.setContent("");
                    pushMessage.setTimestamp(System.currentTimeMillis());
                    pushMessage.setUserId(engineer.getId());
                    pushMessage.setDescription(engineer.getName().substring(0, 1).concat(content.toString()));
                    appMessagePushService.sendMessage(pushMessage);

                } catch (Exception e) {
                    log.error("[OrderService.senNewOrderMsg]app notice - uid:{} ,msg:{}{}", engineer.getId(), engineer.getName(), content.toString(), e);
                }
            }
        }
    }


    /**
     * 发送统计消息
     * 包含：问题反馈，反馈处理,app异常
     */
    public void sendNoticeMessage(Integer noticeType, Long orderId, String quarter, Customer customer, User
            kefu, Long areaId, User user, Date date) {
        MQNoticeMessage.NoticeMessage message = null;
        try {
            message = MQNoticeMessage.NoticeMessage.newBuilder()
                    .setOrderId(orderId)
                    .setQuarter(quarter)
                    .setNoticeType(noticeType)
                    .setCustomerId(customer.getId())
                    .setKefuId(kefu == null ? 0 : kefu.getId())
                    .setAreaId(areaId)
                    .setTriggerBy(MQWebSocketMessage.User.newBuilder()
                            .setId(user.getId())
                            .setName(user.getName())
                            .build()
                    )
                    .setTriggerDate(date.getTime())
                    .setDelta(1)
                    .build();

            try {
                noticeMessageSender.send(message);
            } catch (Exception e) {
                //消息队列发送错误
                log.error("[OrderService.sendNoticeMessage] content:{} ", GsonUtils.getInstance().toGson(message), e);
                //LogUtils.saveLog("发送统计消息错误", "sendNoticeMessage", GsonUtils.getInstance().toGson(message), e, user);
            }

        } catch (Exception e) {
            log.error("[OrderService.sendNoticeMessage] noticeType:{} ,orderId:{} ,customer:{} ,kefu:{} ,area:{}"
                    , noticeType
                    , orderId
                    , customer.getId()
                    , kefu == null ? "0" : kefu.getId()
                    , areaId
                    , e
            );
        }

    }

    /**
     * 发送自动完工消息(app触发)
     */
    public void sendAutoCompleteMessage(Long orderId, String quarter, User user, Date date) {
        Long index = 0l;
        MQOrderAutoComplete.OrderAutoComplete message = null;
        try {
            message = MQOrderAutoComplete.OrderAutoComplete.newBuilder()
                    .setOrderId(orderId)
                    .setQuarter(quarter)
                    .setTriggerBy(user.getId())
                    .setTriggerDate(date.getTime())
                    .build();
            orderAutoCompleteDelaySender.send(message);
        } catch (Exception e) {
            OrderAutoComplete entry = new OrderAutoComplete();
            entry.setOrderId(orderId);
            entry.setQuarter(quarter);
            entry.setTriggerBy(user.getId());
            entry.setTriggerDate(date);
            entry.setCreateBy(user);
            entry.setCreateDate(date);
            log.error("[OrderService.sendAutoCompleteMessage]=={}== orderId:{} ,entry:{}", index, orderId, GsonUtils.getInstance().toGson(entry), e);
            try {
                entry.setId(SeqUtils.NextID());
                autoCompleteDao.insert(entry);
            } catch (Exception e1) {
                log.error("[OrderService.sendAutoCompleteMessage]=={}== orderId:{}", index, orderId, e1);
            }
        }
    }


    //endregion 短信&APP Notic

    //region 提醒消息

    /**
     * 发送提醒消息(APP异常)
     *
     * @param orderId
     * @param quarter
     * @param customerId
     * @param areaId
     * @param kefuId
     * @param date
     * @param user
     */
    public void sendAppNoticeMessage(Long orderId, String quarter, Long customerId, Long areaId, Long kefuId, Date
            date, User user) {
        MQNoticeMessage.NoticeMessage noticeMessage = MQNoticeMessage.NoticeMessage.newBuilder()
                .setNoticeType(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setCustomerId(customerId)
                .setKefuId(kefuId != null ? kefuId : 0l)
                .setAreaId(areaId)
                .setTriggerDate(date.getTime())
                .setTriggerBy(MQWebSocketMessage.User.newBuilder()
                        .setId(user.getId())
                        .setName(user.getName())
                        .build()
                )
                .setDelta(1)
                .build();
        noticeMessageSender.send(noticeMessage);
    }

    /**
     * 统计客户问题反馈(by customer_id,create_by)
     */
    public List<NoticeMessageItemVM> groupFeedbackByCustomer() {
        //只统计最近2个分片
        List<String> quarters = QuarterUtils.getLatestQuarters(2);
        List<Map<String, Object>> datas = feedbackDao.groupByCustomer(quarters);
        List<NoticeMessageItemVM> list = Lists.newArrayList();
        if (datas == null || datas.size() == 0) {
            return list;
        }
        HashMap<Integer, NoticeMessageItemVM> maps = NoticeMessageItemVM.getDefaultMessages(3);
        NoticeMessageItemVM feedback = maps.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
        NoticeMessageItemVM pending = maps.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
        long cid;
        long createBy = 0l;
        long feedbackQty = 0l;
        long pendingQty = 0;
        NoticeMessageItemVM msgFeedback;
        NoticeMessageItemVM msgPending;
        for (int i = 0, size = datas.size(); i < size; i++) {
            cid = (Long) datas.get(i).get("customer_id");
            createBy = (Long) datas.get(i).get("create_by");
            feedbackQty = new BigDecimal(datas.get(i).get("feedback_qty").toString()).longValue();
            pendingQty = new BigDecimal(datas.get(i).get("pending_qty").toString()).longValue();

            //if(feedbackQty>0){
            msgFeedback = new NoticeMessageItemVM();
            BeanUtils.copyProperties(feedback, msgFeedback);
            msgFeedback.setCustomerId(cid);
            msgFeedback.setCreateBy(createBy);
            msgFeedback.setQty(feedbackQty);
            list.add(msgFeedback);
            //}
            //if(pendingQty>0){
            msgPending = new NoticeMessageItemVM();
            BeanUtils.copyProperties(pending, msgPending);
            msgPending.setCustomerId(cid);
            msgPending.setCreateBy(createBy);
            msgPending.setQty(pendingQty);
            list.add(msgPending);
            //}
        }
        return list;
    }

    /**
     * 统计已分配客户的客服的数据(by customer)
     */
    public List<NoticeMessageItemVM> groupFeedbackByKefuOfCustomer() {
        List<String> quarters = QuarterUtils.getLatestQuarters(2);
        List<Map<String, Object>> datas = feedbackDao.groupByKefuOfCustomer(quarters);
        List<NoticeMessageItemVM> list = Lists.newArrayList();
        if (datas == null || datas.size() == 0) {
            return list;
        }
        HashMap<Integer, NoticeMessageItemVM> maps = NoticeMessageItemVM.getDefaultMessages(2);
        NoticeMessageItemVM feedback = maps.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
        NoticeMessageItemVM pending = maps.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
        long cid;
        long feedbackQty = 0l;
        long pendingQty = 0;
        NoticeMessageItemVM msgFeedback;
        NoticeMessageItemVM msgPending;
        for (int i = 0, size = datas.size(); i < size; i++) {
            cid = (Long) datas.get(i).get("customer_id");
            //feedbackQty = ((BigDecimal)datas.get(i).get("feedback_qty")).longValue();
            feedbackQty = new BigDecimal(datas.get(i).get("feedback_qty").toString()).longValue();
            pendingQty = new BigDecimal(datas.get(i).get("pending_qty").toString()).longValue();

            //if(feedbackQty>0){
            msgFeedback = new NoticeMessageItemVM();
            BeanUtils.copyProperties(feedback, msgFeedback);
            msgFeedback.setCustomerId(cid);
            msgFeedback.setQty(feedbackQty);
            list.add(msgFeedback);
            //}
            //if(pendingQty>0){
            msgPending = new NoticeMessageItemVM();
            BeanUtils.copyProperties(pending, msgPending);
            msgPending.setCustomerId(cid);
            msgPending.setQty(pendingQty);
            list.add(msgPending);
            //}
        }
        return list;

    }

    /**
     * 统计未分配客户的客服的数据(by area)
     */
    public List<NoticeMessageItemVM> groupFeedbackByKefuOfArea() {
        //只统计最近2个分片
        List<String> quarters = QuarterUtils.getLatestQuarters(2);
        List<Map<String, Object>> datas = feedbackDao.groupByKefuOfArea(quarters);
        List<NoticeMessageItemVM> list = Lists.newArrayList();
        if (datas == null || datas.size() == 0) {
            return list;
        }
        HashMap<Integer, NoticeMessageItemVM> maps = NoticeMessageItemVM.getDefaultMessages(2);
        NoticeMessageItemVM feedback = maps.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
        NoticeMessageItemVM pending = maps.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
        long areaId;
        long feedbackQty = 0l;
        long pendingQty = 0;
        NoticeMessageItemVM msgFeedback;
        NoticeMessageItemVM msgPending;
        for (int i = 0, size = datas.size(); i < size; i++) {
            areaId = (Long) datas.get(i).get("area_id");
            feedbackQty = new BigDecimal(datas.get(i).get("feedback_qty").toString()).longValue();
            pendingQty = new BigDecimal(datas.get(i).get("pending_qty").toString()).longValue();
            //if(feedbackQty>0){
            msgFeedback = new NoticeMessageItemVM();
            BeanUtils.copyProperties(feedback, msgFeedback);
            msgFeedback.setAreaId(areaId);
            msgFeedback.setQty(feedbackQty);
            list.add(msgFeedback);
            //}
            //if(pendingQty>0){
            msgPending = new NoticeMessageItemVM();
            BeanUtils.copyProperties(pending, msgPending);
            msgPending.setAreaId(areaId);
            msgPending.setQty(pendingQty);
            list.add(msgPending);
            //}
        }
        return list;
    }

    /**
     * 统计已分配客户的客服的app异常数据(by customer)
     */
    public List<NoticeMessageItemVM> groupAppAbnormalyByKefuOfCustomer() {
        //只统计最近2个分片
        List<String> quarters = QuarterUtils.getLatestQuarters(2);
        List<Map<String, Object>> datas = dao.groupAppAbnormalyByKefuOfCustomer(quarters);
        List<NoticeMessageItemVM> list = Lists.newArrayList();
        if (datas == null || datas.size() == 0) {
            return list;
        }
        //HashMap<Integer,NoticeMessageItemVM> maps = NoticeMessageItemVM.getDefaultMessages(2);
        //NoticeMessageItemVM app = maps.get(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY);
        long cid;
        long qty = 0l;
        NoticeMessageItemVM msgApp;
        for (int i = 0, size = datas.size(); i < size; i++) {
            cid = (Long) datas.get(i).get("customer_id");
            qty = new BigDecimal(datas.get(i).get("qty").toString()).longValue();

            msgApp = new NoticeMessageItemVM(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY);
            //BeanUtils.copyProperties(app,msgApp);
            msgApp.setCustomerId(cid);
            msgApp.setQty(qty);
            list.add(msgApp);
        }
        return list;
    }

    /**
     * 统计未分配客户的客服的app异常(by area)
     */
    public List<NoticeMessageItemVM> groupAppAbnormalyByKefuOfArea() {
        //只统计最近2个分片
        List<String> quarters = QuarterUtils.getLatestQuarters(2);
        List<Map<String, Object>> datas = dao.groupAppAbnormalyByKefuOfArea(quarters);
        List<NoticeMessageItemVM> list = Lists.newArrayList();
        if (datas == null || datas.size() == 0) {
            return list;
        }
        //HashMap<Integer,NoticeMessageItemVM> maps = NoticeMessageItemVM.getDefaultMessages(2);
        //NoticeMessageItemVM app = maps.get(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY);
        long areaId;
        long qty = 0l;
        NoticeMessageItemVM msgApp;

        for (int i = 0, size = datas.size(); i < size; i++) {
            areaId = (Long) datas.get(i).get("area_id");
            qty = new BigDecimal(datas.get(i).get("qty").toString()).longValue();
            msgApp = new NoticeMessageItemVM(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY);
            //BeanUtils.copyProperties(app, msgApp);
            msgApp.setAreaId(areaId);
            msgApp.setQty(qty);
            list.add(msgApp);
        }
        return list;
    }

    //endregion 提醒消息

    //region 工单保险单

    /**
     * 根据订单项计算保险费
     *
     * @param categorids 产品类别
     * @return 保险费(null代表未设定保险费)
     */
    public Double getOrderInsuranceAmount(List<Long> categorids) {
        if (categorids == null || categorids.size() == 0) {
            return 0D;
        }
        List<InsurancePrice> prices = insurancePriceService.findAllList();
        Optional<Double> insurance = prices.stream().filter(t -> categorids.contains(t.getCategory().getId())).map(t -> t.getInsurance()).max(Double::compareTo);
        if (insurance.isPresent()) {
            return insurance.get();
        } else {
            return null;
        }
    }

    /**
     * 按B2B工单id+分片+数据源读取已转换工单系统工单信息(id,order_no)
     *
     * @param workcardId
     * @param quarter
     * @param dataSource
     * @return
     */
    public HashMap<String, Object> getB2BOrderNo(int dataSource, String workcardId, String quarter) {
        return orderHeadDao.getB2BOrderNo(dataSource, workcardId, quarter);//2020-12-17 sd_order -> sd_order_head
    }

    /**
     * 订单保险费合计(返回Null或负数)
     */
    public Double getTotalOrderInsurance(long orderId, String quarter){
        if(orderId <=0 || StringUtils.isBlank(quarter)){
            return null;
        }
        return dao.getTotalOrderInsurance(orderId,quarter);
    }

    /**
     * 获得网点保险单
     */
    public OrderInsurance getOrderInsuranceByServicePoint(Long orderId, String quarter, Long servicePointId){
        return dao.getOrderInsuranceByServicePoint(quarter, orderId, servicePointId);
    }

    /**
     * 更新保险单
     */
    @Transactional(readOnly = false)
    public void updateOrderInsurance(OrderInsurance insurance){
        dao.updateOrderInsurance(insurance);
    }
    //endregion

    //region B2B

    /**
     * B2B通知状态变更
     */
    @Transactional(readOnly = false)
    public void tmallWorkcardStatusUpdate(MQTmallPushWorkcardStatusUpdateMessage.TmallPushWorkcardStatusUpdateMessage message) {
        if (message == null) {
            return;
        }
        //status 0-退货 1-签收 2-拒签
        if (message.getStatus() == 1) {
            //签收不处理
            return;
        }
        String workCardId = String.valueOf(message.getWorkcardId());
        HashMap<String, Object> maps = orderHeadDao.getB2BOrderNo(B2BTmallConstant.DATA_SOURCE, workCardId, null);//2020-12-17 sd_order -> sd_order_head
        if (maps == null || maps.size() == 0) {
            throw new OrderException("未找到天猫workCardId:" + workCardId + " 对应的订单号");
        }
        if (!maps.containsKey("id")) {
            throw new OrderException("未找到天猫workCardId:" + workCardId + " 对应的订单号");
        }
        Long id = new BigDecimal(maps.get("id").toString()).longValue();
        String quarter = (String) maps.get("quarter");
        Order order = getOrderById(id, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getId() == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单：" + id.toString() + " 错误");
        }
        User user = new User(3L, "b2b");
        if (order.getOrderCondition().getStatusValue() == Order.ORDER_STATUS_APPROVED) {
            //未接单，取消
            cancelOrder(id, user, StringUtils.isBlank(message.getComment()) ? "天猫推送消息通知取消" : message.getComment());
        } else {
            //已接单，退单
            returnOrder(id, new Dict("51", "厂家(电商)通知取消"), StringUtils.isBlank(message.getComment()) ? "天猫推送消息通知取消" : message.getComment());
        }
    }

    //endregion

    //region 加急

    /**
     * 计算加急费(客评)
     *
     * @param customer      客户(要有urgentFlag)
     * @param areaId        区域id (要转换成省的id)
     * @param urgentLevelId 加急等级
     * @param hours         用时
     */
    public UrgentChargeModel getGradeCustomerUrgentCharge(Customer customer, Long areaId, Long urgentLevelId, Double hours) {
        UrgentChargeModel urgentChargeModel = null;
        if (customer == null || areaId.longValue() == 0) {
            return urgentChargeModel;
        }

        Area province = AreaUtils.getProvinceByArea(areaId, 4);
        if (province == null) {
            throw new OrderException("读取省份错误!");
        }
        UrgentChargeModel urgentCharge = null;
        AreaUrgentModel areaUrgentModel = urgentCustomerService.getAreaUrgentModel(customer.getId(), province.getId());
        if (areaUrgentModel != null && areaUrgentModel.getList() != null && areaUrgentModel.getList().size() > 0) {
            urgentCharge = areaUrgentModel.getList().stream().filter(t -> t.getUrgentLevel().getId().longValue() == urgentLevelId).findFirst().orElse(null);
        }
        //get default
        if (urgentCharge == null) {
            List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
            if (urgentLevels == null || urgentLevels.size() == 0) {
                throw new OrderException("未设定加急费标准!");
            } else {
                UrgentLevel urgentLevel = urgentLevels.stream().filter(t -> t.getId().longValue() == urgentLevelId).findFirst().orElse(null);
                if (urgentLevel == null) {
                    throw new OrderException("加急标准不存在!");
                } else {
                    List<String> ranges = Splitter.onPattern("[~|-]")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(urgentLevel.getLabel());
                    if (ranges.size() == 2) {
                        if (hours <= Double.valueOf(ranges.get(1))) {
                            urgentCharge = new UrgentChargeModel();
                            urgentCharge.setChargeIn(urgentLevel.getChargeIn());
                            urgentCharge.setChargeOut(urgentLevel.getChargeOut());
                            return urgentCharge;
                        }
                    } else {
                        throw new OrderException("加急等级描述格式错误!");
                    }
                }
            }
        } else {
            List<String> ranges = Splitter.onPattern("[~|-]")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(urgentCharge.getUrgentLevel().getLabel());
            if (ranges.size() == 2) {
                if (hours <= Double.valueOf(ranges.get(1))) {
                    return urgentCharge;
                }
            } else {
                throw new OrderException("加急等级描述格式错误!");
            }
        }
        return new UrgentChargeModel();
    }

    /**
     * 计算加急费(下单)
     *
     * @param customerId    客户id
     * @param areaId        区域id (要转换成省的id)
     * @param urgentLevelId 加急等级
     * @param checkCustomer 是否检查客户加急开关(未使用)
     *                      1.先从客户加急费用表取价格(md_customer_urgent)
     *                      2.如1中未取道，从默认加急等级中取价格(md_urgent_level)
     *                      3.如都未取道价格，返回空hashmap
     */
    public HashMap<String, Object> getCustomerUrgentCharge(Long customerId, Long areaId, Long urgentLevelId, String checkCustomer) {
        HashMap<String, Object> maps = Maps.newHashMap();
        //maps.put("chargeIn", 0.00d);
        //maps.put("chargeOut", 0.00d);
        if (areaId.longValue() == 0 || urgentLevelId.longValue() == 0 || customerId.longValue() == 0) {
            return maps;
        }
        Area province = AreaUtils.getProvinceByArea(areaId, 4);
        if (province == null) {
            throw new OrderException("确认加急费失败： 读取省份错误!");
        }
        UrgentChargeModel urgentCharge = null;
        AreaUrgentModel areaUrgentModel = urgentCustomerService.getAreaUrgentModel(customerId, province.getId());
        if (areaUrgentModel != null && areaUrgentModel.getList() != null && areaUrgentModel.getList().size() > 0) {
            urgentCharge = areaUrgentModel.getList().stream().filter(t -> t.getUrgentLevel().getId().longValue() == urgentLevelId).findFirst().orElse(null);
        }
        //get default
        if (urgentCharge == null) {
            List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
            if (urgentLevels == null || urgentLevels.size() == 0) {
                return maps;
            } else {
                UrgentLevel urgentLevel = urgentLevels.stream().filter(t -> t.getId().longValue() == urgentLevelId).findFirst().orElse(null);
                if (urgentLevel == null) {
                    return maps;
                } else {
                    urgentCharge = new UrgentChargeModel();
                    urgentCharge.setChargeIn(urgentLevel.getChargeIn());
                    urgentCharge.setChargeOut(urgentLevel.getChargeOut());
                }
            }
        }
        maps.put("chargeIn", urgentCharge.getChargeIn());
        maps.put("chargeOut", urgentCharge.getChargeOut());
        return maps;
    }

    /**
     * 设置加急
     */
    @Transactional(readOnly = false)
    public void setUrgent(UrgentModel urgent) {
        //锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, urgent.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//1分钟
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Order order = getOrderById(urgent.getOrderId(), urgent.getQuarter(), OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null) {
                throw new OrderException("读取订单错误，请重试。");
            }
            Dict status = order.getOrderCondition().getStatus();
            //退单申请之后的单不能更改加急
            if (status.getIntValue() >= Order.ORDER_STATUS_RETURNING) {
                throw new OrderException("此订单不能加急，请确认订单状态。");
            }
            OrderCondition condition = order.getOrderCondition();

            if (urgent.getUrgentLevel().getId().longValue() > 0) {
                List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
                UrgentLevel urgentLevel = urgentLevels.stream().filter(t -> t.getId().longValue() == urgent.getUrgentLevel().getId().longValue()).findFirst().orElse(null);
                if (urgentLevel == null) {
                    throw new OrderException("加急等级不存在。");
                }
                urgent.getUrgentLevel().setRemarks(urgentLevel.getRemarks());
            }
            condition.setUrgentLevel(urgent.getUrgentLevel());
            Date date = new Date();
            //status

            HashMap<String, Object> params = Maps.newHashMap();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("urgentDate", date);
            dao.updateStatus(params);

            //condition
            params.clear();
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("urgentLevel", urgent.getUrgentLevel());
            params.put("updateBy", urgent.getCreateBy());
            params.put("updateDate", date);
            params.put("pendingTypeDate", date); //Added by zhoucy，设置加急，工单回到预约到期(客服)
            dao.updateCondition(params);

            //fee
            dao.changeOrderUrgentCharge(order.getId(), order.getQuarter(), urgent.getChargeIn(), urgent.getChargeOut());

            //log
            OrderProcessLog orderProcessLog = new OrderProcessLog();
            orderProcessLog.setQuarter(order.getQuarter());
            orderProcessLog.setAction(urgent.getUrgentLevel().getId().longValue() == 0 ? "取消加急" : "加急");
            orderProcessLog.setOrderId(order.getId());
            if (urgent.getUrgentLevel().getId().longValue() == 0) {
                if (StringUtils.isNotBlank(urgent.getRemarks())) {
                    orderProcessLog.setActionComment(String.format("取消加急,操作人:%s,备注:%s", urgent.getCreateBy().getName(), urgent.getRemarks().trim()));
                } else {
                    orderProcessLog.setActionComment(String.format("取消加急,操作人:%s", urgent.getCreateBy().getName()));
                }
            } else {
                if (StringUtils.isNotBlank(urgent.getRemarks())) {
                    orderProcessLog.setActionComment(String.format("设定加急:%s,操作人:%s,备注:%s", urgent.getUrgentLevel().getRemarks(), urgent.getCreateBy().getName(), urgent.getRemarks().trim()));
                } else {
                    orderProcessLog.setActionComment(String.format("设定加急:%s,操作人:%s", urgent.getUrgentLevel().getLabel(), urgent.getCreateBy().getName()));
                }
            }
            orderProcessLog.setActionComment(StringUtils.left(orderProcessLog.getActionComment(),255));
            orderProcessLog.setStatus(status.getLabel());
            orderProcessLog.setStatusValue(status.getIntValue());
            orderProcessLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            orderProcessLog.setCloseFlag(0);
            orderProcessLog.setCreateBy(urgent.getCreateBy());
            orderProcessLog.setCreateDate(date);
            orderProcessLog.setCustomerId(condition.getCustomerId());
            orderProcessLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(orderProcessLog);

            //cache,更新version,订单状态
            condition.setPendingTypeDate(date);
            OrderStatus orderStatus = order.getOrderStatus();
            orderStatus.setUrgentDate(date);
            //同步网点工单数据
            servicePointOrderBusinessService.relatedForm(
                    order.getId(),
                    order.getQuarter(),
                    0,
                    0,
                    urgent.getUrgentLevel().getId().intValue(),
                    urgent.getCreateBy().getId(),
                    date.getTime()
            );

            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(urgent.getOrderId())
                    .setCondition(condition)
                    .setOrderStatus(orderStatus)
                    .setPendingTypeDate(date)
                    .incrVersion(1L)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

            if(condition.getServicePoint() != null && condition.getEngineer() != null) {
                Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
                if (engineer != null && engineer.getAccountId() > 0) {
                    String content = "您有加急工单，请及时处理!";
                    PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.SYSTEM, "加急单通知", content, engineer.getAccountId());
                }
            }

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }


    //endregion 加急

    public boolean checkOrderProductBarCode(Long orderId, String quarter, Long customerId, List<OrderDetail> orderDetails) {
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && customerId != null && customerId > 0) {
            if (orderDetails != null && !orderDetails.isEmpty()) {
                List<OrderItemComplete> uploadedCompletePics = orderItemCompleteDao.getByOrderId(orderId, quarter);

                //拆分套组，获取最终的产品id
                List<Long> tempProductIds = orderDetails.stream()
                        .filter(i -> i.getProduct() != null && i.getProduct().getId() != null)
                        .map(OrderDetail::getProductId).collect(Collectors.toList());
                Map<Long, Product> productMap = productService.getProductMap(tempProductIds);
                Set<Long> prodcutIdSet = Sets.newHashSet();
                Product product = null;
                Long productId = null;
                for (Long idLong : tempProductIds) {
                    product = productMap.get(idLong);
                    if (product != null) {
                        if (product.getSetFlag() == 1) {
                            final String[] setIds = product.getProductIds().split(",");
                            for (String idString : setIds) {
                                productId = StringUtils.toLong(idString);
                                if (productId > 0) {
                                    prodcutIdSet.add(productId);
                                }
                            }
                        } else {
                            prodcutIdSet.add(idLong);
                        }
                    }
                }

                uploadedCompletePics = uploadedCompletePics.stream().filter(i -> prodcutIdSet.contains(i.getProduct().getId())).collect(Collectors.toList());//只检查OrderDetail中的产品
                if (!uploadedCompletePics.isEmpty()) {
                    List<Long> productIds = uploadedCompletePics.stream().map(i -> i.getProduct().getId()).distinct().collect(Collectors.toList());
                    Map<Long, ProductCompletePic> completePicRuleMap = OrderUtils.getCustomerProductCompletePicMap(productIds, customerId);
                    ProductCompletePic picRule = null;
                    for (OrderItemComplete item : uploadedCompletePics) {
                        picRule = completePicRuleMap.get(item.getProduct().getId());
                        if (picRule != null && picRule.getBarcodeMustFlag() != null && picRule.getBarcodeMustFlag() == 1
                                && StringUtils.isBlank(item.getUnitBarcode())) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 查询工单附件列表
     *
     * @param orderId 订单id
     * @param quarter 分片季度
     * @return
     */
    private List<OrderAttachment> getByOrderId(Long orderId, String quarter) {
        List<OrderAttachment> orderAttachmentList = attachmentDao.getByOrderId(orderId, quarter);
        List<Long> createByIdList = orderAttachmentList.stream().map(attachment -> attachment.getCreateBy().getId()).collect(Collectors.toList());
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(createByIdList);
        orderAttachmentList.forEach(attachment -> {
            if (userMap.get(attachment.getCreateBy().getId()) != null) {
                attachment.setCreateBy(userMap.get(attachment.getCreateBy().getId()));
            }
        });
        return orderAttachmentList;
    }

    //region 智能回访

    /**
     * 客评后发送消息通知智能客服停止自动回访
     *
     * @param orderId
     * @param user
     * @param date
     */
    public void stopVoiceOperateMessage(String site, Long orderId, String quarter, String user, Date date) {
        if (orderId == null || orderId <= 0 || StringUtils.isBlank(site)) {
            return;
        }
        if (StringUtils.isBlank(user)) {
            return;
        }
        try {
            MQVoiceSeviceMessage.OperateCommand operateCommand = MQVoiceSeviceMessage.OperateCommand.newBuilder()
                    .setSite(site)
                    .setOrderId(orderId)
                    .setCommand(OperateType.STOP.code)
                    .setCreateBy(user)
                    .setCreateDate(date == null ? System.currentTimeMillis() : date.getTime())
                    .build();
            operateTaskMQSender.send(operateCommand);
            orderVoiceTaskService.cancel(quarter, orderId, date.getTime());
        } catch (Exception e) {
            log.error("[停止智能回访错误]orderId:{},user:{},date:{}", orderId, user, date, e);
        }
    }

    /**
     * 继续任务
     *
     * @param orderId
     * @param user
     * @param date
     */
    public void keepOnVoiceOperateMessage(String site, Long orderId, String quarter, String user, Date date) {
        if (orderId == null || orderId <= 0 || StringUtils.isBlank(site)) {
            return;
        }
        if (StringUtils.isBlank(user)) {
            return;
        }
        try {
            MQVoiceSeviceMessage.OperateCommand operateCommand = MQVoiceSeviceMessage.OperateCommand.newBuilder()
                    .setSite(site)
                    .setOrderId(orderId)
                    .setCommand(OperateType.KEEP_ON.code)
                    .setCreateBy(user)
                    .setCreateDate(date == null ? System.currentTimeMillis() : date.getTime())
                    .build();
            operateTaskMQSender.send(operateCommand);
            orderVoiceTaskService.cancel(quarter, orderId, date.getTime());
        } catch (Exception e) {
            log.error("[停止智能回访错误]orderId:{},user:{},date:{}", orderId, user, date, e);
        }
    }

    /**
     * 客评后发送消息通知智能客服停止自动回访 (预留)
     *
     * @param orderId
     * @param user
     * @param date
     */
    public void removeVoiceOperateMessage(String site, Long orderId, String quarter, String user, Date date) {
        if (orderId == null || orderId <= 0 || StringUtils.isBlank(site)) {
            return;
        }
        if (StringUtils.isBlank(user)) {
            return;
        }
        try {
            MQVoiceSeviceMessage.OperateCommand operateCommand = MQVoiceSeviceMessage.OperateCommand.newBuilder()
                    .setSite(site)
                    .setOrderId(orderId)
                    .setCommand(OperateType.DELETE.code)
                    .setCreateBy(user)
                    .setCreateDate(date == null ? System.currentTimeMillis() : date.getTime())
                    .build();
            operateTaskMQSender.send(operateCommand);
            orderVoiceTaskService.cancel(quarter, orderId, date.getTime());
        } catch (Exception e) {
            log.error("[停止智能回访错误]orderId:{},user:{},date:{}", orderId, user, date, e);
        }
    }


    /**
     * 新建智能回访任务
     *
     * @param order 订单
     * @param kefu  客服
     * @param date
     */
    public void sendNewVoiceTaskMessage(String site, Order order, String kefu, Date date) {
        if (order == null || StringUtils.isBlank(site)) {
            return;
        }
        OrderCondition orderCondition = order.getOrderCondition();
        if (orderCondition == null) {
            return;
        }
        String quarter = order.getQuarter();
        if (StringUtils.isBlank(quarter)) {
            quarter = orderCondition.getQuarter();
        }
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        if (StringUtils.isBlank(orderCondition.getUserName()) || StringUtils.isBlank(kefu)
                || StringUtils.isBlank(orderCondition.getServicePhone())) {
            return;
        }
        //voice task
        OrderVoiceTask task = orderVoiceTaskService.getBaseInfoByOrderId(quarter, order.getId());
        //已存在的任务，继续任务
        if (task != null) {
            keepOnVoiceOperateMessage(site, order.getId(), quarter, kefu, date);
            return;
        }
        OrderItem firstItem = null;
        if (items.size() == 1) {
            firstItem = items.get(0);
        } else {
            //items.stream().sorted(Comparator.comparing(OrderItem::getServiceType))
            firstItem = items.stream().sorted(
                    Comparator.comparing(OrderItem::getProduct, (x, y) -> {
                        //再按套组邮箱
                        if (x == null && y != null) {
                            return 1;
                        } else if (x != null && y == null) {
                            return -1;
                        } else if (x == null && y == null) {
                            return -1;
                        } else {
                            if (x.getId().equals(y.getId())) {
                                return 0;
                            } else if (x.getSetFlag() == 1) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }).thenComparing(OrderItem::getServiceType, (x, y) -> {
                        //先按照服务排序,安装优先
                        if (x == null && y != null) {
                            return 1;
                        } else if (x != null && y == null) {
                            return -1;
                        } else if (x == null && y == null) {
                            return -1;
                        } else {
                            if (x.getId().equals(y.getId())) {
                                return 0;
                            } else if (x.getName().contains("安装")) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }))
                    .findFirst().orElse(null);
        }
        Product product = productService.getProductByIdFromCache(firstItem.getProduct().getId());
        if (product == null) {
            product = firstItem.getProduct();
        }
        //无开场白的产品不自动回访
        if (StringUtils.isBlank(product.getPinYin())) {
            return;
        }
        String productName = product.getName();
        ServiceType serviceType = firstItem.getServiceType();
        String openingSpeech = product.getPinYin() + (serviceType.getName().contains("安装") ? "1" : "2");
        try {
            //用户名长度超长要截取，保留20个字符
            MQVoiceSeviceMessage.Task message = MQVoiceSeviceMessage.Task.newBuilder()
                    .setVoiceType(1)
                    .setSite(site)
                    .setOrderId(orderCondition.getOrderId())
                    .setQuarter(quarter)
                    .setUserName(StringUtils.left(orderCondition.getUserName(), 20))
                    .setPhone(orderCondition.getServicePhone())
                    .setProducts(productName)
                    .setOpeningSpeech(openingSpeech) //开场白（产品+服务） 1：安装 2：其他
                    .setCaption(orderCondition.getOrderNo() + "自动回访")
                    .setCreateBy(kefu)
                    .setCreateDate(date == null ? System.currentTimeMillis() : date.getTime())
                    .build();

            task = new OrderVoiceTask();
            task.setOrderId(message.getOrderId());
            task.setQuarter(message.getQuarter());
            task.setCreateBy(message.getCreateBy());
            task.setCreateDate(message.getCreateDate());
            task.setProjectCaption(message.getCaption());
            task.setRemark(message.getProducts());
            task.setUserName(message.getUserName());
            task.setPhone(message.getPhone());
            task.setVoiceType(message.getVoiceType());
            orderVoiceTaskService.insert(task);

            newTaskMQSender.send(message);
        } catch (Exception e) {
            log.error("[发送智能回访消息错误]orderId:{},userName:{},phone:{},product:{},openingSpeech:{},kefu:{}",
                    orderCondition.getOrderId(), orderCondition.getUserName(), orderCondition.getServicePhone(), product, openingSpeech, kefu,
                    e);
        }
    }


    //endregion 智能回访

    /**
     * 从数据库获取工单的服务品类
     */
    public Long getOrderProductCategoryId(String quarter, Long orderId) {
        Long productCategoryId = 0L;
        if (orderId != null && orderId > 0) {
            productCategoryId = dao.getOrderProductCategoryId(quarter, orderId);
        }
        return productCategoryId == null ? 0 : productCategoryId;
    }

    public List<OrderCondition> getConditionForUpdateSubArea(int pageNo, String quarter, String status) {
        Page<OrderSearchModel> page = new Page<>(pageNo, 100);
        OrderSearchModel orderSearchModel = new OrderSearchModel();
        orderSearchModel.setPage(page);
        orderSearchModel.setQuarter(quarter);
        orderSearchModel.setStatus(new Dict(status));
        List<OrderCondition> result = dao.getConditionForUpdateSubArea(orderSearchModel);
        return result;
    }

    public void updateSubAreaId(String quarter, String status) {
        int pageNo = 1;
        List<OrderCondition> conditions = getConditionForUpdateSubArea(pageNo, quarter, status);
        while (conditions != null && conditions.size() > 0) {
            for (OrderCondition condition : conditions) {
                Long subAreaId = 0L;
                try {
                    //String subAreaIdInfo[] = AreaUtils.decodeAddressGaode(condition.getAreaName().concat(" ").concat(condition.getServiceAddress())); //mark on 2020-8-5
                    String subAreaIdInfo[] = AreaUtils.decodeAddressGaodeFromMS(condition.getAreaName().concat(" ").concat(condition.getServiceAddress()));  //add on 2020-8-5
                    subAreaId = Long.parseLong(subAreaIdInfo[1]);
                } catch (Exception e) {}
                if (subAreaId != 0) {
                    HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(3);
                    params.put("quarter", condition.getQuarter());
                    params.put("orderId", condition.getOrderId());
                    params.put("subAreaId", subAreaId);
                    dao.updateSubAreaId(params);
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            pageNo++;
            conditions = getConditionForUpdateSubArea(pageNo, quarter, status);
        }
    }

    //region B2B退单

    /**
     * 樱雪直接通知取消status>30的工单
     */
    @Transactional(readOnly = false)
    public void b2bCancelOrder(Long orderId,String quarter,Dict responsible, String comment, User user) {
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            Order order = getOrderById(orderId, quarter==null?"":quarter.trim(), OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null || order.getOrderCondition() == null || order.getOrderFee() == null || order.getOrderStatus() == null) {
                throw new OrderException("读取订单失败，请确认订单时候存在.");
            }
            if (!OrderUtils.canReturn(order.getOrderCondition().getStatusValue())) {
                throw new OrderException("该订单不能退单，请刷新页面查看订单订单状态。");
            }
            //配件单检查
            MSResponse<String> msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(),orderId,order.getQuarter());
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new OrderException(msResponse.getData() + ":" + msResponse.getMsg());
            }
            OrderCondition condition = getOrderConditionFromMasterById(orderId, order.getQuarter());
            if (condition == null) {
                throw new OrderException("读取订单失败，请确认订单时候存在.");
            }
            order.setOrderCondition(condition);


            OrderFee fee = order.getOrderFee();

            Date date = new Date();
            Dict status = new Dict(String.valueOf(Order.ORDER_STATUS_RETURNED));
            String label = MSDictUtils.getDictLabel(status.getValue(), "order_status", "已退单");
            status.setLabel(label);
            HashMap<String, Object> params = Maps.newHashMap();

            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("status", status);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("closeDate", date);
            params.put("subStatus", Order.ORDER_SUBSTATUS_RETURNED);
            // 突击单关闭 Add by Ryan
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                params.put("rushOrderFlag", 2);
            }
            dao.updateCondition(params);

            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("status", status);// 取消
            params.put("cancelSponsor", 2);//发起方，kkl
            params.put("cancelResponsible", Integer.parseInt(responsible.getValue()));//退单类型
            params.put("cancelApplyBy", user);
            params.put("cancelApplyDate", date);
            params.put("cancelApplyComment", comment);
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("orderId", orderId);
            params.put("cancelApproveFlag", 1);
            params.put("cancelApproveBy", user);
            params.put("cancelApproveDate", date);
            params.put("closeFlag", 1);
            params.put("closeBy", user);
            dao.updateStatus(params);

            //关闭突击单
            if (condition.getRushOrderFlag() == 1 || condition.getRushOrderFlag() == 3) {
                crushService.closeOrderCurshByOrderId(orderId,order.getQuarter(),1,null,user,date);
            }
            condition.setRushOrderFlag(2);
            // 关闭催单 2019/08/15
            Boolean hasRemindered = false;
            if(order.getOrderStatus() != null && order.getOrderStatus().getReminderStatus() != null && order.getOrderStatus().getReminderStatus() > 0) {
                hasRemindered = true;
            }
            if(hasRemindered && order.getOrderStatus().getReminderStatus() < ReminderStatus.Completed.getCode()) {
                Long servicePointId = ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
                reminderService.completeReminder(orderId ,order.getQuarter() ,user ,date ,comment, ReminderAutoCloseTypeEnum.OrderReturn,Order.ORDER_STATUS_RETURNED,servicePointId);
            }else if(hasRemindered){
                reminderService.updateOrderCloseInfo(orderId,order.getQuarter(),date,Order.ORDER_STATUS_RETURNED);
            }
            // 关闭未发件配件单(驳回，包含未发件的返件单)
            if(condition.getPartsFlag() == 1){
                orderMaterialService.closeMaterialMasterWhenCancel(order.getDataSourceId(),orderId,order.getQuarter(),user,date,"订单退单审核后自动关闭");
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("退单申请");
            processLog.setOrderId(orderId);
            if (StringUtils.isNotBlank(comment)) {
                processLog.setActionComment(String.format("退单申请:%s,申请人:%s,备注:%s", order.getOrderNo(), user.getName(), comment));
            } else {
                processLog.setActionComment(String.format("退单申请:%s,申请人:%s", order.getOrderNo(), user.getName()));
            }
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(),255));
            processLog.setStatus("退单申请");
            processLog.setStatusValue(Order.ORDER_STATUS_RETURNING);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());
            processLog.setOrderId(order.getId());
            processLog.setAction("同意退单申请");
            processLog.setActionComment(String.format("同意退单申请:%s,审核人:%s", order.getOrderNo(), user.getName()));
            processLog.setStatus(label);
            processLog.setStatusValue(Order.ORDER_STATUS_RETURNED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(1);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(condition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(processLog);

            //更改前获得冻结金额
            CustomerFinance finance = customerFinanceDao.getAmounts(condition.getCustomer().getId());

            //customer currency log
            CustomerCurrency currency = new CustomerCurrency();
            //分片根据冻结记录创建时间计算
            currency.setQuarter(QuarterUtils.getSeasonQuarter(date));
            currency.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency)); //Production
            currency.setCustomer(condition.getCustomer());
            currency.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_OUT);
            currency.setCurrencyNo(order.getOrderNo());
            currency.setBeforeBalance(finance.getBlockAmount());//冻结金额
            currency.setBalance(finance.getBlockAmount() - fee.getBlockedCharge() - fee.getExpectCharge());
            currency.setAmount(0 - fee.getBlockedCharge() - fee.getExpectCharge());//负数
            currency.setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH);
            currency.setActionType(70);//冻结
            currency.setCreateBy(user);
            currency.setCreateDate(date);
            currency.setRemarks(
                    String.format(
                            "订单退单解冻 %.2f元 相关单号为 %s",
                            (fee.getBlockedCharge() + fee.getExpectCharge()),
                            order.getOrderNo()
                    )
            );
//            currency.setQuarter(order.getQuarter());//*
//            customerCurrencyDao.insert(currency);
            //更改客户的冻结金额(block_amount)
            customerFinanceDao.incBlockAmount(condition.getCustomer().getId(), 0 - fee.getBlockedCharge(), 0 - fee.getExpectCharge(), user.getId(), date);

            //更新网点未完工数量
            Dict prvStatus = condition.getStatus();
            ServicePoint servicePoint = condition.getServicePoint();
            if(servicePoint!=null && servicePoint.getId()!=null && servicePoint.getId()>0 && prvStatus!=null && prvStatus.getIntValue()!=Order.ORDER_STATUS_APP_COMPLETED){
                updateServicePointUnfinishedOrderCount(servicePoint.getId(),-1,"B2B退单",orderId,user);

            }
            //调用公共缓存
            OrderCacheUtils.delete(orderId);
            //消息处理
            // 1.取消智能回访
            //String site = Global.getSiteCode();
            if (StringUtils.isNoneBlank(siteCode)) {
                Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
                if (taskResult != null && taskResult == 0) {
                    try {
                        stopVoiceOperateMessage(siteCode, order.getId(), order.getQuarter(), user.getName(), date);
                    } catch (Exception e) {
                        log.error("退单审核-停滞智能回访错误:" + order.getId(), e);
                    }
                }//taskResult
            }//site
            // 2. 业务绩效报表+客服日退单报表
            Customer customer = customerService.getFromCache(condition.getCustomer().getId());
            if (customer == null) {
                customer = condition.getCustomer();
            }

            //region 网点订单数据更新 2019-03-25
            servicePointOrderBusinessService.orderStatusUpdate(MQOrderServicePointMessage.OperationType.ApproveReturnRequest_VALUE,
                    order.getId(),order.getQuarter(),null,
                    status.getIntValue(),Order.ORDER_SUBSTATUS_RETURNED,-1,
                    false,null,
                    user.getId(),date.getTime());
            //endregion

            //region 报表微服务消息队列
            try{
                RPTOrderProcessModel orderProcessModel = new RPTOrderProcessModel();
                orderProcessModel.setProcessType(RPTOrderProcessTypeEnum.RETURN.getValue());
                orderProcessModel.setOrderId(orderId);
                if(condition.getProductCategoryId()!=null && condition.getProductCategoryId()>0){
                    orderProcessModel.setProductCategoryId(condition.getProductCategoryId());
                }
                orderProcessModel.setCustomerId(customer.getId());
                orderProcessModel.setKeFuId(condition.getKefu().getId());
                orderProcessModel.setOrderStatus(Order.ORDER_STATUS_RETURNED);
                if(condition.getCreateDate()!=null){
                    orderProcessModel.setOrderCreateDate(condition.getCreateDate().getTime());
                }
                orderProcessModel.setOrderCloseDate(date.getTime());
                if(order.getDataSourceId()>0){
                    orderProcessModel.setDataSource(order.getDataSourceId());
                }
                if(condition.getServicePoint()!=null && condition.getServicePoint().getId()!=null && condition.getServicePoint().getId()>0){
                    orderProcessModel.setServicePointId(condition.getServicePoint().getId());
                }
                if(condition.getArea() !=null && condition.getArea().getId()!= null && condition.getArea().getId()>0){
                    orderProcessModel.setCountId(condition.getArea().getId());
                }
                orderProcessService.sendRPTOrderProcess(orderProcessModel);
            }catch (Exception e){
                log.error("OrderService.approveReturnOrder樱雪退单单发送报表微服务消息队列失败"+e.getMessage());
            }
            //endregion

            //切分冻结流水
            customerBlockCurrencyService.saveCustomerBlockCurrency(currency);
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[OrderService.b2bCancelOrder] orderId:{}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 5l);//延时5秒
            }
        }

    }

    //endregion B2B退单

    //region 突击

    public List<HistoryPlanOrderModel> getOrderServiceItemList(String quarter,  List<Long> orderIds){
        return dao.getOrderServiceItemList(quarter,orderIds);
    }

    /**
     * 根据订单读取街道id(突击用)
     */
    public Long getSubAreaIdByOrderId(Long orderId, String quarter){
        return dao.getSubAreaIdByOrderId(orderId,quarter);
    }

    /**
     * 根据突击单区县或街道ID读取同区域以往派单记录
     */
    public List<HistoryPlanOrderModel> findOrderListOfCrush(OrderCrushSearchVM searchModel){
        return dao.findOrderListOfCrush(searchModel);
    }

    /**
     * 根据市Id,区id,街道id,品类id获取是否可突击
     */
    public int isCanRush(long categoryId,long cityId,long areaId,long subAreaId){
        if(subAreaId<=3){
            return 0;
        }
        MDRegionPermission regionPermission = new MDRegionPermission();
        regionPermission.setProductCategoryId(categoryId);
        regionPermission.setCityId(cityId);
        regionPermission.setAreaId(areaId);
        regionPermission.setSubAreaId(subAreaId);
        return regionPermissionService.getSubAreaStatusFromCacheForSD(regionPermission);
    }


    /**
     * 按订单id及标志列表(多个)返回日志(分页获取)
     *
     * @param page
     * @param orderTrackingSearchModel
     * @return
     */
    public Page<OrderProcessLog> getOrderLogsByFlagsNew(Page<OrderTrackingSearchModel> page,OrderTrackingSearchModel orderTrackingSearchModel) {
        Page<OrderProcessLog> returnPage = new Page<>();
        if (orderTrackingSearchModel.getOrderId() == null || orderTrackingSearchModel.getOrderId() <= 0) {
            return returnPage;
        }
        orderTrackingSearchModel.setPage(page);
        List<OrderProcessLog> list = dao.getOrderLogsByFlagsNew(orderTrackingSearchModel);
        returnPage.setCount(page.getCount());
        returnPage.setList(list);

        //user微服务
        if (!returnPage.getList().isEmpty()) {
            List<Long> userIds = returnPage.getList().stream().filter(i -> i.getCreateBy() != null && i.getCreateBy().getId() != null)
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
        return returnPage;
    }

    //endregion

    //region 退换货

    /**
     * 退货-确认收货
     */
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void confirmReceived(KefuCompleteModel requestModel,StringBuilder logContent) {
        if (requestModel == null || requestModel.getOrderId() == null || requestModel.getOrder() == null) {
            throw new OrderException("参数错误");
        }
        OrderCondition condition = requestModel.getOrder().getOrderCondition();
        if (condition.getArrivalDate() != null) {
            throw new OrderException("该单已收货，不允许操作[确认收货]。");
        }
        Long orderId = requestModel.getOrderId();
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        if(requestModel.getRemarks() == null){
            requestModel.setRemarks(StringUtils.EMPTY);
        }
        if(requestModel.getOperateDate() == null){
            requestModel.setOperateDate(new Date());
        }
        Date date = requestModel.getOperateDate();
        Order order = requestModel.getOrder();
        try {
            HashMap<String, Object> params = Maps.newHashMap();
            //condition
            params.put("orderId", order.getId());
            params.put("quarter", order.getQuarter());
            params.put("arrivalDate", date);
            params.put("updateBy", order.getCurrentUser());
            params.put("updateDate", date);
            dao.updateCondition(params);
            condition.setArrivalDate(date);
            //log
            OrderProcessLog log = new OrderProcessLog();
            log.setQuarter(order.getQuarter());
            log.setAction("换货确认收货");
            log.setOrderId(order.getId());
            logContent.append("换货确认收货，确认时间:").append(DateUtils.formatDate(date, "yyyy-MM-dd HH:mm"));
            log.setActionComment(logContent.toString());
            log.setStatus(condition.getStatus().getLabel());
            log.setStatusValue(Integer.parseInt(condition.getStatus().getValue()));
            log.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);
            log.setCloseFlag(0);
            log.setCreateBy(order.getCurrentUser());
            log.setCreateDate(date);
            log.setRemarks(log.getActionComment());//厂家可见
            log.setCustomerId(condition.getCustomerId());
            log.setDataSourceId(order.getDataSourceId());
            saveOrderProcessLogNew(log);

            //region B2B
            // 确认收货：网点确认收新品
            MSResponse response = b2BCenterOrderService.confirmReceived(order,requestModel.getRemarks(),requestModel.getUser(),date);
            if(!MSResponse.isSuccessCode(response)){
                throw new OrderException(response.getMsg());
            }
            //endregion B2B

            //region cache
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(condition.getOrderId())
                    .incrVersion(1L)
                    .setCondition(condition)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());
            //endregion cache
        } finally {
            if (lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 保存退货/换货完工
     * 通过更改:appCompleteType 来控制是否完工；完工后才能客评
     */
    @Transactional(readOnly = false)
    public void saveCompleteItemsForReturn(ReturnCompleteModel completeModel){
        if (completeModel == null || completeModel.getOrderId() == null || StringUtils.isBlank(completeModel.getQuarter())) {
            throw new OrderException("参数错误");
        }
        if(CollectionUtils.isEmpty(completeModel.getItems())){
            throw new OrderException("无完工项目");
        }
        if(completeModel.getItems().size() < 2){
            throw new OrderException("完工项目不完整");
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, completeModel.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = completeModel.getUser();
        Date date = new Date();
        try {
            //1.保存拆装和物流
            List<OrderReturnComplete> items = completeModel.getItems();
            for (OrderReturnComplete item : items) {
                if (item.getId() == null || item.getId() <= 0) {
                    item.setOrderId(completeModel.getOrderId());
                    item.setQuarter(completeModel.getQuarter());
                    item.setDataSource(completeModel.getDataSource());
                    item.setProductId(completeModel.getProductId());
                    item.setProductName(completeModel.getProductName());
                    item.setCreateBy(user.getName());
                    item.setCreateDate(date);
                    removePrefixFromPhotoUrl(item.getJsonItem().getPhotos());
                    item.setJson(gsonRedisSerializer.toJson(item.getJsonItem()));
                } else {
                    item.setOrderId(completeModel.getOrderId());
                    item.setQuarter(completeModel.getQuarter());
                    item.setUpdateBy(user.getName());
                    item.setUpdateDate(date);
                    removePrefixFromPhotoUrl(item.getJsonItem().getPhotos());
                    item.setJson(gsonRedisSerializer.toJson(item.getJsonItem()));
                }
            }
            returnCompleteService.save(completeModel);
        }catch (Exception e) {
            log.error("[OrderService.saveCompleteReturn] {}", completeModel, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }

    }

    /**
     * 保存数据库前，移除路径中前缀
     * @param photos
     */
    private void removePrefixFromPhotoUrl(List<OrderReturnComplete.PicSubItem> photos){
        if(CollectionUtils.isEmpty(photos)){
            return;
        }
        StringBuilder url = new StringBuilder();
        for(OrderReturnComplete.PicSubItem photo:photos){
            if(StringUtils.isBlank(photo.getUrl())){
                continue;
            }
            if(photo.getUrl().startsWith("/uploads/")){
                url.setLength(0);
                url.append(StringUtils.substring(photo.getUrl(),9));
                photo.setUrl(url.toString());
            }else if(photo.getUrl().startsWith("uploads/")){
                url.setLength(0);
                url.append(StringUtils.substring(photo.getUrl(),8));
                photo.setUrl(url.toString());
            }else if(photo.getUrl().startsWith("/")){
                photo.setUrl(StringUtils.substring(photo.getUrl(),1));
            }
        }
    }

    /**
     * 发送退换货完工数据
     */
    @Transactional(readOnly = false)
    public MSResponse sendB2BCompleteReturnMessage(ReturnCompleteModel completeModel){
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        Supplier<Stream<OrderReturnComplete>> streamSupplier = () -> completeModel.getItems().stream();
        if(completeModel.getDataSource() == null || completeModel.getDataSource() <= 1){
            streamSupplier.get().forEach(t->{
                returnCompleteService.uploadSuccess(t.getId(),t.getQuarter(),completeModel.getUser().getName(),completeModel.getOperateDate());
            });
            return response;
        }

        //1.send
        //1.1.dismout
        OrderReturnComplete dismountItem = streamSupplier.get().filter(t->t.getItemType() == OrderReturnComplete.ItemTypeEnum.DISMOUNT.getId()).findFirst().orElse(null);
        if(dismountItem != null && dismountItem.getUploadFlag() == 0){
            response = b2BCenterOrderService.orderDismounting(completeModel.getDataSource(),completeModel.getOrderServiceType(),completeModel.getB2bOrderId(),"",dismountItem,completeModel.getUser(),completeModel.getOperateDate());
            if(!MSResponse.isSuccessCode(response)){
                return response;
            }else{
                int row = returnCompleteService.uploadSuccess(dismountItem.getId(),dismountItem.getQuarter(),completeModel.getUser().getName(),completeModel.getOperateDate());
                dismountItem.setUploadFlag(1);
            }
        }
        //1.2.logistics
        OrderReturnComplete logisticsItem = streamSupplier.get().filter(t->t.getItemType() == OrderReturnComplete.ItemTypeEnum.LOGISTICS.getId()).findFirst().orElse(null);
        if(logisticsItem != null && logisticsItem.getUploadFlag() == 0){
            response = b2BCenterOrderService.backLogistics(completeModel.getDataSource(),completeModel.getOrderServiceType(),completeModel.getB2bOrderId(),"",logisticsItem,completeModel.getUser(),completeModel.getOperateDate());
            if(!MSResponse.isSuccessCode(response)){
                return response;
            }else{
                int row = returnCompleteService.uploadSuccess(logisticsItem.getId(),logisticsItem.getQuarter(),completeModel.getUser().getName(),completeModel.getOperateDate());
                logisticsItem.setUploadFlag(1);
            }
        }
        return response;
    }

    /**
     * 退换货-客服完工
     * 更新订单信息，不发送B2B接口信息
     */
    @Transactional(readOnly = false)
    public void kefuCompleteReturn(KefuCompleteModel completeModel) {
        if (completeModel == null || completeModel.getOrderId() == null) {
            throw new OrderException("参数错误");
        }
        Long orderId = completeModel.getOrderId();
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        Boolean saveSuccess = false;
        Date date = new Date();
        try {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("orderId", orderId);
            params.put("quarter", completeModel.getQuarter());
            params.put("appCompleteType", completeModel.getCompleteType().getValue());//完工类型-客服完工
            params.put("appCompleteDate", date);//完工日期
            params.put("pendingFlag", 2);//正常
            params.put("pendingType", 0);
            params.put("status", Order.ORDER_STATUS_APP_COMPLETED);
            params.put("subStatus", Order.ORDER_SUBSTATUS_APPCOMPLETED);
            params.put("updateDate", date);
            params.put("updateBy", completeModel.getUser().getId());
            int cnt = dao.kefuComplete(params);
            //if(cnt == 0) {
            //    throw new OrderException("完工失败，无数据更新。");
            //}
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(completeModel.getQuarter());
            processLog.setAction("客服完成");
            processLog.setOrderId(orderId);
            processLog.setActionComment(String.format("%s,%s", completeModel.getCompleteType().getLabel(), StringUtils.isBlank(completeModel.getRemarks()) ? "" : "备注:" + completeModel.getRemarks()));
            processLog.setActionComment(StringUtils.left(processLog.getActionComment(), 255));
            processLog.setStatus("已上门");
            processLog.setStatusValue(Order.ORDER_STATUS_SERVICED);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(completeModel.getUser());
            processLog.setCreateDate(date);
            processLog.setRemarks("");
            processLog.setCustomerId(0L);
            processLog.setDataSourceId(completeModel.getDataSourceId());
            saveOrderProcessLogNew(processLog);
            saveSuccess = true;//*
            //region cache
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setDeleteField(OrderCacheField.CONDITION)
                    .setDeleteField(OrderCacheField.PENDING_TYPE)
                    .setDeleteField(OrderCacheField.PENDING_TYPE_DATE);
            OrderCacheUtils.update(builder.build());
            //endregion cache
            //region 网点订单数据更新
            servicePointOrderBusinessService.appComplete(
                    orderId,
                    completeModel.getQuarter(),
                    Order.ORDER_SUBSTATUS_APPCOMPLETED,
                    completeModel.getCompleteType().getValue(),
                    0,
                    completeModel.getUser().getId(),
                    date.getTime()
            );
            //endregion
        } catch (OrderException oe) {
            saveSuccess = false;
            log.error("[OrderService.kefuCompleteReturn] {}", completeModel, oe);
            throw oe;
        } catch (Exception e) {
            saveSuccess = false;
            log.error("[OrderService.kefuCompleteReturn] {}", completeModel, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }
    //endregion 退换货

    //region 挂起

    @Transactional(readOnly = false)
    public void suspendOrder(Long orderId, String quarter, OrderSuspendTypeEnum type, OrderSuspendFlagEnum flag) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("orderId", orderId);
        params.put("quarter", quarter);
        params.put("suspendType", type.getValue());
        params.put("suspendFlag", flag.getValue());
        updateOrderCondition(params);
    }

    //endregion 挂起

    @Transactional(readOnly = false)
    public void sendReturnVerifyCode(Long orderId, String quarter, Dict responsible, String comment, User user) {
        Order order = getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.STATUS, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单信息失败");
        }
        if (!B2BDataSourceEnum.isB2BDataSource(order.getDataSourceId())) {
            throw new OrderException("错误：仅B2B工单允许进行此操作");
        }
        String b2bOrderNo = order.getWorkCardId();
        String servicePhone = order.getOrderCondition().getServicePhone();
        Dict responsibleDict = MSDictUtils.getDictByValue(responsible.getValue(), Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
        if (responsibleDict == null) {
            throw new OrderException("错误：退单类型不能为空");
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(responsible.getLabel());
        if (StringUtils.isNotBlank(comment)) {
            stringBuffer.append(";").append(comment);
        }
        MSResponse response = b2BCenterOrderService.sendCancelVerifyCode(order.getDataSourceId(), b2bOrderNo, servicePhone, stringBuffer.toString(), user);
        if (!MSResponse.isSuccessCode(response)) {
            throw new OrderException("错误：" + response.getMsg());
        }
    }


    /**
     * 根据市Id,区id,街道id,品类id获取客服类型
     * regionAttributesDto(subAreaType:街道属性(1-突击,2-大客服,3-自动),areaType:区/县属性(1-突击,2-大客服,3-自动),categoryType:品类vip标识(1:是,0否(不区分vip客户)))
     * @param customerVipFlag 客户vip标识
     * @param vipLevel 客户vip等级
     * @return OrderKefuTypeRuleEnum
     */
    public OrderKefuTypeRuleEnum getKefuType(long categoryId, long cityId, long areaId, long subAreaId, long customerVipFlag,int vipLevel){
        OrderKefuTypeRuleEnum orderKefuTypeRuleEnum =OrderKefuTypeRuleEnum.ORDER_COMMON;
        MDRegionPermission regionPermission = new MDRegionPermission();
        regionPermission.setProductCategoryId(categoryId);
        regionPermission.setCityId(cityId);
        regionPermission.setAreaId(areaId);
        regionPermission.setSubAreaId(subAreaId);
        regionPermission.setVip(vipLevel);
        MDRegionAttributesDto regionAttributesDto = regionPermissionNewService.getKeFuTypeFromCacheForSD(regionPermission);
        if(customerVipFlag==1){//vip客服
            if(regionAttributesDto.getSubAreaType().equals(3)){ //如果是自动街道
                orderKefuTypeRuleEnum = OrderKefuTypeRuleEnum.ORDER_AUTO;
            }else if(regionAttributesDto.getSubAreaType().equals(1) && regionAttributesDto.getAreaType().equals(3)){//vip客服街道是突击,区县是自动的区域,属于突击
                orderKefuTypeRuleEnum = OrderKefuTypeRuleEnum.ORDER_RUSH;
            }else if(regionAttributesDto.getSubAreaType().equals(2) && regionAttributesDto.getAreaType().equals(3)){ //街道是大客服,区县是自动
                orderKefuTypeRuleEnum = OrderKefuTypeRuleEnum.ORDER_AUTO; //属于自动客服
            }
        }else{
            if(regionAttributesDto.getSubAreaType().equals(3)){//自动客服
                orderKefuTypeRuleEnum = OrderKefuTypeRuleEnum.ORDER_AUTO;
            }else if(regionAttributesDto.getSubAreaType().equals(1)){//突击客服
                orderKefuTypeRuleEnum = OrderKefuTypeRuleEnum.ORDER_RUSH;
            }else if(regionAttributesDto.getSubAreaType().equals(2) && regionAttributesDto.getAreaType().equals(3)){
                orderKefuTypeRuleEnum = OrderKefuTypeRuleEnum.ORDER_AUTO; //属于自动客服
            }
        }
        return orderKefuTypeRuleEnum;
    }

    /**
     * 根据工单区域属性返回客服类型
     */
    public String getKefuTypeName(int kefuType,long customerVipFlag){
        String kefuTypeName = "";
        if(kefuType==OrderKefuTypeRuleEnum.ORDER_COMMON.getCode()){
            if(customerVipFlag==0){
                kefuTypeName = "普通";
            }else{
                kefuTypeName = "KA";
            }
        }
        if(kefuType==OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){
            kefuTypeName = "突击";
        }
        if(kefuType==OrderKefuTypeRuleEnum.ORDER_AUTO.getCode()){
            kefuTypeName = "自动";
        }
        return kefuTypeName;
    }

    /**
     * 返回下单找不到客服消息提示tip
     * 业务下单:【西藏自治区 拉萨市 当雄县】暂无负债【厨电类】客服，请联系二部主管配置区域后下单。
     * 客户下单:【广东省 深圳市 罗湖区】暂无负债【厨电类】客服，请联系业务员【杜燕贞，电话：15919036009】配置区域后下单
     */
    public String noFindKefuTip(User user,Customer customer,long categoryId,int kefuType,long areaId,long cityId,long provinceId){
        ProductCategory productCategory =  productCategoryService.getFromCache(categoryId);
        Area area = areaService.get(areaId);
        StringBuffer sb = new StringBuffer("<div><font style=\"color:#2FA2DE\">【");
        sb.append(area==null?"":area.getFullName());
        sb.append("】</font>暂无负责<font style=\"color:#2FA2DE\">【");
        sb.append(productCategory==null?"":productCategory.getName());
        String tip ="】</font>客服，请联系";
        if(user.isCustomer()){
            tip=tip+"业务员【"+customer.getSales().getName()+"，电话："+customer.getSales().getMobile()+"】配置区域后下单";
        }else{
            User userSupervisor = userKeFuService.getKefuSupervisor(customer,areaId,categoryId,kefuType,cityId,provinceId);
            if(userSupervisor!=null){
                Office office = sysOfficeService.get(userSupervisor.getOfficeId());
                if(office!=null){
                    tip = tip+office.getName()+"主管配置区域后下单。";
                }else{
                    tip = tip+"客服主管配置区域后下单。";
                }
            }else{
                tip = tip+"客服主管配置区域后下单。";
            }
        }
        sb.append(tip);
        sb.append("</div>");
        return sb.toString();
    }

    /**
     * 根据客户id查询一条工单id(用于判断删除客户时,客户是否有下过工单)
     * @param customerId
     * @return
     */
    public Long getOrderIdByCustomerId(Long customerId){
        return dao.getOrderIdByCustomerId(customerId);
    }

    //region 偏远区域

    /**
     * 检查街道是否是偏远区域
     * @param orderCondition    订单信息表
     * @return  data: true 是；false 不是
     */
    public RestResult<Boolean> checkServicePointRemoteArea(OrderCondition orderCondition){
        if(orderCondition == null){
            return RestResultGenerator.success(false);
        }
        long categroyId= Optional.ofNullable(orderCondition.getProductCategoryId()).orElse(0L);
        long areaId = Optional.ofNullable(orderCondition.getArea()).map(t->t.getId()).orElse(0L);
        long subAreaId = Optional.ofNullable(orderCondition.getSubArea()).map(t->t.getId()).orElse(0L);
        if(categroyId <=0){
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code,"品类参数无值");
        }
        if(areaId <= 0){
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code,"区域参数无值");
        }
        //判断街道是否属于：偏远区域
        MSResponse<Integer> msResponse = msRegionPermissionNewService.getRemoteAreaStatusFromCacheForSD(categroyId, areaId, subAreaId);
        if(MSResponse.isSuccessCode(msResponse)){
            if(msResponse.getData()==1){
                return RestResultGenerator.success(true);
            }else{
                return RestResultGenerator.success(false);
            }
        }
        return RestResultGenerator.custom(msResponse.getCode(),msResponse.getMsg());
    }


    /**
     * 检查街道是否是偏远区域，并检查网点是否设定了偏远区域价格
     * @param servicePointId    网点
     * @param orderCondition    订单信息表
     * @param items             订单项目
     * @return  code=0: 通过验证：1.不是偏远区域；2.是偏远区域，且已设定服务价格；
     *          code>0: 验证不通过：1.参数错误；2.运行时异常；3.价格未设定
     */
    public RestResult<Object> checkServicePointRemoteAreaAndPrice(long servicePointId,OrderCondition orderCondition,List<OrderItem> items){
        if(servicePointId <= 0){
            return RestResultGenerator.custom(ErrorCode.READ_ORDER_FAIL.code,"参数：网点编码无内容。");
        }
        RestResult<Boolean> remoteCheckResult = checkServicePointRemoteArea(orderCondition);
        if(remoteCheckResult.getCode() != ErrorCode.NO_ERROR.code){
            return RestResultGenerator.custom(ErrorCode.READ_ORDER_FAIL.code,new StringJoiner("").add("偏远区域处理:").add(remoteCheckResult.getMsg()).toString());
        }
        Boolean isRemoteArea = (Boolean)remoteCheckResult.getData();
        if(isRemoteArea != null && isRemoteArea){
            //检查是否有设定价格
            if (CollectionUtils.isEmpty(items)) {
                return RestResultGenerator.custom(ErrorCode.READ_ORDER_FAIL.code,"读取订单详细服务项目失败。");
            }
            Set<com.kkl.kklplus.entity.common.NameValuePair<Long, Long>> valuePairs = items.stream().map(t -> {
                return new com.kkl.kklplus.entity.common.NameValuePair<Long, Long>(t.getProductId(), t.getServiceType().getId());
            }).collect(Collectors.toSet());
            List<com.kkl.kklplus.entity.common.NameValuePair<Long, Long>> nameValuePairs = new ArrayList<com.kkl.kklplus.entity.common.NameValuePair<Long, Long>>(valuePairs);
            List<ServicePrice> prices = msServicePointPriceService.findPricesListByRemotePriceFlagFromCacheForSD(servicePointId, nameValuePairs);
            if(CollectionUtils.isEmpty(prices)){
                return RestResultGenerator.custom(ErrorCode.READ_ORDER_FAIL.code,"网点未定义偏远区域价格。");
            }else if(prices.size() < valuePairs.size()){
                return RestResultGenerator.custom(ErrorCode.READ_ORDER_FAIL.code,"网点未定义此单中部分产品的偏远区域价格。");
            }
            return RestResultGenerator.success();
        }
        return RestResultGenerator.success();
    }

    /**
     * 更新网点未完工单数量
     * @param servicePointId 网点id
     * @param count 数量
     *
     */
    public void updateServicePointUnfinishedOrderCount(Long servicePointId,int count,String method,Long orderId,User user){
        Map<String,Object> servicePointParams = Maps.newHashMap();
        servicePointParams.put("id",servicePointId);
        servicePointParams.put("unfinishedOrderCount",count);
        try {
            msServicePointService.updateUnfinishedOrderCountByMapForSD(servicePointParams);
        }catch (Exception e){
            String param = "servicePointId:"+servicePointId+",orderId:"+orderId;
            LogUtils.saveLog("更新网点未完工单数量失败",method,param,e,user);
        }
    }
    //endregion

    /**
     * 根据工单id获取店铺,数据源,第三方单号
     * @param orderId
     * @param quarter
     * @return
     */
    public Order getB2BInfoById(Long orderId,String quarter){
          return dao.getB2BInfoById(orderId,quarter);
    }

    /**
     * 批量获取网点价格(偏远价格或服务价格)
     * @param orderCondition    订单信息表
     * @param servicePointId     网点
     * @param products           产品id和服务类型id键值对
     * @return  d
     */
    public Map<String,ServicePrice> getServicePriceFromCacheNew(OrderCondition orderCondition,Long servicePointId,List<com.kkl.kklplus.entity.common.NameValuePair<Long,Long>> products){
        long categroyId= Optional.ofNullable(orderCondition.getProductCategoryId()).orElse(0L);
        long areaId = Optional.ofNullable(orderCondition.getArea()).map(t->t.getId()).orElse(0L);
        long subAreaId = Optional.ofNullable(orderCondition.getSubArea()).map(t->t.getId()).orElse(0L);
        if(categroyId <=0){
            //return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code,"品类参数无值");
             throw new RuntimeException("品类参数无值");
        }
        long cityId = 0;
        Area area = areaService.getFromCache(areaId);
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                cityId = Long.valueOf(ids.get(ids.size() - 1));
            }
        }
        if(cityId <= 0){
            //return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code,"区域参数无值");
            throw new RuntimeException("区域参数无值");
        }
        List<ServicePrice> prices = msServicePointPriceService.findListByCategoryAndAreaAndServiceTypeAndProductFromCacheForSD(products, categroyId, cityId,  subAreaId, servicePointId);
        if(prices==null){
            return null;
        }
        if(CollectionUtils.isEmpty(prices)){
            return Maps.newHashMapWithExpectedSize(0);
        }
        return prices.stream().collect(Collectors.toMap(
                e-> String.format("%d:%d",e.getProduct().getId(),e.getServiceType().getId()),
                e-> e
        ));
    }

    /**
     *  按需读取网点价格（偏远价格或服务价格）
     * @param orderCondition    工单信息
     * @param servicePointId    网点id
     * @param productId         产品id
     * @param serviceTypeId     服务项目id
     * @return
     */
    public ServicePrice getPriceByProductAndServiceTypeFromCacheNew(OrderCondition orderCondition,long servicePointId,long productId,long serviceTypeId){
        if(servicePointId <= 0 || productId <= 0 || serviceTypeId <= 0 ||orderCondition==null){
            return null;
        }
        long categroyId= Optional.ofNullable(orderCondition.getProductCategoryId()).orElse(0L);
        long areaId = Optional.ofNullable(orderCondition.getArea()).map(t->t.getId()).orElse(0L);
        long subAreaId = Optional.ofNullable(orderCondition.getSubArea()).map(t->t.getId()).orElse(0L);
        long cityId = 0;
        Area area = areaService.getFromCache(areaId);
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                cityId = Long.valueOf(ids.get(ids.size() - 1));
            }
        }
        if(cityId <= 0){
            //return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code,"区域参数无值");
            throw new RuntimeException("区域参数无值");
        }
        List<ServicePrice> prices =  msServicePointPriceService.findListByCategoryAndAreaAndServiceTypeAndProductFromCacheForSD(Lists.newArrayList(new com.kkl.kklplus.entity.common.NameValuePair<Long,Long>(productId,serviceTypeId)),categroyId,
                                                                                                                                 cityId,subAreaId,servicePointId);
        if(CollectionUtils.isEmpty(prices)){
            return null;
        }
        return prices.get(0);
    }

}
