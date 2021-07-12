package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BModifyOperationEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderHeadDao;
import com.wolfking.jeesite.modules.sd.dao.OrderLocationDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sd.service.OrderAuxiliaryMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.utils.OrderAdditionalInfoUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.Log;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderExistsException;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderTranserFailureException;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderConvertVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderModifyEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderSearchVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.canbo.sd.feign.CanboOrderFeign;
import com.wolfking.jeesite.ms.inse.sd.feign.InseOrderFeign;
import com.wolfking.jeesite.ms.jd.sd.feign.JdOrderFeign;
import com.wolfking.jeesite.ms.jdue.sd.feign.JDUEOrderFeign;
import com.wolfking.jeesite.ms.jdueplus.sd.feign.JDUEPlusOrderFeign;
import com.wolfking.jeesite.ms.jinjing.feign.JinJingOrderFeign;
import com.wolfking.jeesite.ms.jinran.sd.feign.JinRanOrderFeign;
import com.wolfking.jeesite.ms.joyoung.sd.feign.JoyoungOrderFeign;
import com.wolfking.jeesite.ms.konka.sd.feign.KonkaOrderFeign;
import com.wolfking.jeesite.ms.lb.sb.feign.LbOrderFeign;
import com.wolfking.jeesite.ms.mbo.feign.MBOOrderFeign;
import com.wolfking.jeesite.ms.mqi.sd.feign.MqiOrderFeign;
import com.wolfking.jeesite.ms.otlan.sd.feign.OtlanOrderFeign;
import com.wolfking.jeesite.ms.pdd.sd.feign.PddOrderFeign;
import com.wolfking.jeesite.ms.philips.sd.feign.PhilipsOrderFeign;
import com.wolfking.jeesite.ms.sf.sd.feign.SFOrderFeign;
import com.wolfking.jeesite.ms.suning.sd.feign.SuningOrderFeign;
import com.wolfking.jeesite.ms.supor.sd.feign.SuporOrderFeign;
import com.wolfking.jeesite.ms.tmall.sd.feign.WorkcardFeign;
import com.wolfking.jeesite.ms.um.sd.feign.UmOrderFeign;
import com.wolfking.jeesite.ms.usatonga.feign.UsatonGaOrderFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import com.wolfking.jeesite.ms.vatti.sd.feign.VattiOrderFeign;
import com.wolfking.jeesite.ms.viomi.sd.feign.VioMiOrderFeign;
import com.wolfking.jeesite.ms.weber.feign.WeberOrderFeign;
import com.wolfking.jeesite.ms.xyyplus.sd.feign.XYYPlusOrderFeign;
import org.apache.commons.lang3.StringEscapeUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2BOrderManualBaseService extends B2BOrderAutoBaseService {

    @Autowired
    private JdOrderFeign jdOrderFeign;
    @Autowired
    private CanboOrderFeign canboOrderFeign;
    @Autowired
    private WeberOrderFeign weberOrderFeign;
    @Autowired
    private MBOOrderFeign mboOrderFeign;
    @Autowired
    private SuporOrderFeign suporOrderFeign;
    @Autowired
    private JinJingOrderFeign jinJingOrderFeign;
    @Autowired
    private UsatonGaOrderFeign usatonGaOrderFeign;
    @Autowired
    private MqiOrderFeign mqiOrderFeign;
    @Autowired
    private JinRanOrderFeign jinRanOrderFeign;
    @Autowired
    private WorkcardFeign workcardFeign;
    @Autowired
    private InseOrderFeign inseOrderFeign;
    @Autowired
    private KonkaOrderFeign konkaOrderFeign;
    @Autowired
    private JoyoungOrderFeign joyoungOrderFeign;
    @Autowired
    private JDUEOrderFeign jdueOrderFeign;
    @Autowired
    private JDUEPlusOrderFeign jduePlusOrderFeign;
    @Autowired
    private XYYPlusOrderFeign xyyPlusOrderFeign;
    @Autowired
    private LbOrderFeign lbOrderFeign;
    @Autowired
    private SuningOrderFeign suningOrderFeign;
    @Autowired
    private UmOrderFeign umOrderFeign;
    @Autowired
    private OtlanOrderFeign otlanOrderFeign;
    @Autowired
    private PddOrderFeign pddOrderFeign;
    @Autowired
    private VioMiOrderFeign vioMiOrderFeign;
    @Autowired
    private SFOrderFeign sfOrderFeign;
    @Autowired
    private VattiOrderFeign vattiOrderFeign;
    @Autowired
    private PhilipsOrderFeign philipsOrderFeign;
//    @Autowired
//    private PhilipsOrderNewFeign philipsOrderNewFeign;

    /**
     * 厂家(电商)通知取消
     */
    private static final String CANCEL_RESPONSIBLE_VALUE_CUSTOMER_NOTIFY = "51";

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    OrderCacheReadService orderCacheReadService;
    @Autowired
    OrderItemCompleteService orderItemCompleteService;

    @Resource
    OrderDao orderDao;
    @Autowired
    OrderHeadDao orderHeadDao;
    @Resource
    OrderLocationDao orderLocationDao;

    //region 获取B2B工单

    /**
     * 从B2B微服务分页获取工单列表
     */
    public Page<B2BOrderVModel> findPageOfToTransfer(Page page, B2BOrderSearchVModel searchVModel, B2BDataSourceEnum dataSource) {

        MSPage<B2BOrderSearchModel> msPage = new MSPage<>(page.getPageNo(), page.getPageSize());
        searchVModel.setPage(msPage);
        if (searchVModel.getProcessFlag() == null) {
            searchVModel.setProcessFlags(B2BProcessFlag.getAllProcessFlags().stream().map(i -> i.value).collect(Collectors.toList()));
        } else {
            searchVModel.setProcessFlags(Lists.newArrayList(searchVModel.getProcessFlag()));
        }
        if (StringUtils.isNotBlank(searchVModel.getShopIdsString())) {
            String[] shopIds = searchVModel.getShopIdsString().split(",");
            if (shopIds.length > 0) {
                searchVModel.setShopIds(Lists.newArrayList(shopIds));
            }
        }
        MSResponse<MSPage<B2BOrder>> responseEntity = getB2bOrderList(searchVModel, dataSource);
        Page<B2BOrderVModel> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (MSResponse.isSuccess(responseEntity)) {
            MSPage<B2BOrder> responePage = responseEntity.getData();
            returnPage.setCount(responePage.getRowCount());
            List<B2BOrderVModel> rtnList = toB2BOrderVModels(responePage.getList());
            returnPage.setList(rtnList);
        } else {
            throw new RuntimeException(responseEntity.getMsg());
        }

        return returnPage;
    }


    /**
     * 从B2B微服务分页获取工单列表
     */
    public Page<B2BOrderVModel> findPageOfNoRoutingB2BOrders(Page page, B2BOrderSearchVModel searchVModel, B2BDataSourceEnum dataSource) {

        MSPage<B2BOrderSearchModel> msPage = new MSPage<>(page.getPageNo(), page.getPageSize());
        searchVModel.setPage(msPage);
        if (searchVModel.getProcessFlag() == null) {
            searchVModel.setProcessFlags(B2BProcessFlag.getAllProcessFlags().stream().map(i -> i.value).collect(Collectors.toList()));
        } else {
            searchVModel.setProcessFlags(Lists.newArrayList(searchVModel.getProcessFlag()));
        }
        MSResponse<MSPage<B2BOrder>> responseEntity = getB2bOrderNoRoutingList(searchVModel, dataSource);
        Page<B2BOrderVModel> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (MSResponse.isSuccess(responseEntity)) {
            MSPage<B2BOrder> responePage = responseEntity.getData();
            returnPage.setCount(responePage.getRowCount());
            List<B2BOrderVModel> rtnList = toB2BOrderVModels(responePage.getList());
            returnPage.setList(rtnList);
        } else {
            throw new RuntimeException(responseEntity.getMsg());
        }

        return returnPage;
    }

    /**
     * 调用不同的B2B微服务
     */
    private MSResponse<MSPage<B2BOrder>> getB2bOrderList(B2BOrderSearchVModel searchVModel, B2BDataSourceEnum dataSource) {
        MSResponse<MSPage<B2BOrder>> responseEntity = null;
        if (dataSource == B2BDataSourceEnum.JD) {
            responseEntity = jdOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.TMALL) {
            responseEntity = workcardFeign.getListOrder(searchVModel);
        } else if (B2BDataSourceEnum.isTooneDataSource(dataSource)) {
            responseEntity = canboOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.WEBER) {
            responseEntity = weberOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.MBO) {
            responseEntity = mboOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.SUPOR) {
            responseEntity = suporOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JINJING) {
            responseEntity = jinJingOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.USATON_GA) {
            responseEntity = usatonGaOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.MQI) {
            responseEntity = mqiOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JINRAN) {
            responseEntity = jinRanOrderFeign.getList(searchVModel);
        }  else if (dataSource == B2BDataSourceEnum.INSE) {
            responseEntity = inseOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.XYINGYAN) {
            responseEntity = xyyPlusOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.LB) {
            responseEntity = lbOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.KONKA) {
            responseEntity = konkaOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JOYOUNG) {
            responseEntity = joyoungOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.SUNING) {
            responseEntity = suningOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JDUE) {
            responseEntity = jdueOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JDUEPLUS) {
            responseEntity = jduePlusOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.UM) {
            responseEntity = umOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.OTLAN) {
            responseEntity = otlanOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            responseEntity = pddOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.VIOMI) {
            responseEntity = vioMiOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.SF) {
            responseEntity = sfOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.VATTI) {
            responseEntity = vattiOrderFeign.getList(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.PHILIPS) {
            responseEntity = philipsOrderFeign.getList(searchVModel);
//            responseEntity = philipsOrderNewFeign.getList(searchVModel);
        }
        return responseEntity;
    }


    //endregion

    //region 检查

    /**
     * 检查服务区域是否有效
     *
     * @throws B2BOrderTranserFailureException
     */
    protected void validateServiceArea(B2BOrderConvertVModel order) {
        if (order.getArea() == null || order.getArea().getId() == null || order.getArea().getId() <= 0) {
            throw new B2BOrderTranserFailureException("找不到指定的区域,请重新选择。");
        }
        Area area = areaService.getFromCache(order.getArea().getId());
        if (area == null) {
            throw new B2BOrderTranserFailureException("找不到指定的区域,请重新选择。");
        }
        if (!area.getType().equals(Area.TYPE_VALUE_COUNTY)) {
            throw new B2BOrderTranserFailureException("区域请选择至区/县,请重新选择。");
        }
    }

    /**
     * 检查订单中的客户信息，并进行补充下单必须的属性值
     *
     * @throws B2BOrderTranserFailureException
     */
    protected void validateAndSetCustomerInfo(B2BOrderConvertVModel order, User user) {
        if (order.getCustomer() == null || order.getCustomer().getId() == null || order.getCustomer().getId() <= 0) {
            throw new B2BOrderTranserFailureException("请选择客户。");
        }
        Customer customer = customerService.getFromCache(order.getCustomer().getId());
        if (customer == null) {
            throw new B2BOrderTranserFailureException("请选择客户。");
        }
        if (user.isCustomer() && customer.getEffectFlag() != 1) {
            throw new B2BOrderTranserFailureException("您的账户基本信息不完整，请完善基本信息。");
        }

        CustomerFinance finance = customerService.getFinanceForAddOrder(order.getCustomer().getId());
        if (finance == null || finance.getPaymentType() == null || StringUtils.isBlank(finance.getPaymentType().getValue())) {
            throw new B2BOrderTranserFailureException("客户未设置结算方式。");
        }

        customer.setFinance(finance);
        order.setOrderPaymentType(finance.getPaymentType());
        order.setCustomer(customer);
    }

    /**
     * 检查订单中订单项，并补充下单必须的属性值
     *
     * @throws B2BOrderTranserFailureException
     */
    protected void validateAndSetOrderItem(OrderItem item, int itemNo, Map<Long, ServiceType> serviceTypeMap,
                                           Map<String, CustomerPrice> priceMap, Map<String, Dict> expressCompanyMap) {
        item.setItemNo(itemNo);
        if (item.getServiceType() == null || item.getServiceType().getId() == null || item.getServiceType().getId() <= 0) {
            throw new B2BOrderTranserFailureException("服务类型不存在");
        }
        ServiceType serviceType = serviceTypeMap.get(item.getServiceType().getId());
        if (serviceType == null) {
            throw new B2BOrderTranserFailureException("服务类型不存在");
        }
        item.setServiceType(serviceType);
        if (item.getProduct() == null || item.getProduct().getId() == null || item.getProduct().getId() <= 0) {
            throw new B2BOrderTranserFailureException("产品不存在");
        }
        Product product = productService.getProductByIdFromCache(item.getProduct().getId());
        if (product == null) {
            throw new B2BOrderTranserFailureException("产品不存在");
        }
        item.setProduct(product);
        CustomerPrice price = priceMap.get(String.format("%d:%d", item.getProduct().getId(), item.getServiceType().getId()));
        if (price == null) {
            throw new B2BOrderTranserFailureException(String.format("产品:%s 未定义服务项目:%s 的服务价格", item.getProduct().getName(), item.getServiceType().getName()));
        }
        item.setStandPrice(price.getPrice());
        item.setDiscountPrice(price.getDiscountPrice());
        if (item.getExpressCompany() != null && StringUtils.isNotBlank(item.getExpressCompany().getValue())) {
            Dict expressCompany = expressCompanyMap.get(item.getExpressCompany().getValue());
            if (expressCompany != null) {
                item.setExpressCompany(expressCompany);
            }
        }
    }

    //endregion 检查

    //region 手动转单

    /**
     * 调用不同的B2B微服务
     */
    private MSResponse<MSPage<B2BOrder>> getB2bOrderNoRoutingList(B2BOrderSearchVModel searchVModel, B2BDataSourceEnum dataSource) {
        MSResponse<MSPage<B2BOrder>> responseEntity = null;
        if (dataSource == B2BDataSourceEnum.TMALL) {
            responseEntity = workcardFeign.getListUnknownOrder(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JD) {
            responseEntity = jdOrderFeign.getListUnknownOrder(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JDUE) {
            responseEntity = jdueOrderFeign.getListUnknownOrder(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.JDUEPLUS) {
            responseEntity = jduePlusOrderFeign.getListUnknownOrder(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.SUNING) {
            responseEntity = suningOrderFeign.getListUnknownOrder(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.PDD) {
            responseEntity = pddOrderFeign.getListUnknownOrder(searchVModel);
        } else if (dataSource == B2BDataSourceEnum.SF) {
            responseEntity = sfOrderFeign.getListUnknownOrder(searchVModel);
        }
        return responseEntity;
    }

    /**
     * 
     * 将B2B工单转成快可立工单(手工转单)
     * 地址解析失败、余额检查失败、随机分配客服失败也允许下单
     */
    public MSResponse<Order> toOrderManual(B2BOrderVModel orderVModel, User user) {
        Assert.notNull(orderVModel, "没有需要转换的工单");
        Assert.isTrue(user != null && user.getId() != null && user.getId() > 0, "操作人为空，请检查是否登录超时");

        List<String> errorMsgList = Lists.newArrayList();

        validateBasicPropertiesForManual(orderVModel);
//        validateB2BOrderIsExists(orderVModel.getOrderNo(), orderVModel.getDataSource());
        validateB2BOrderIsExistsNew(orderVModel.getB2bOrderId(), orderVModel.getOrderNo(), orderVModel.getDataSource());
        String[] areaParseResult = new String[]{"0"};
        try {
            areaParseResult = parseAddressNew(orderVModel.getUserAddress());    // add on 2019-5-21
        } catch (Exception e) {
            errorMsgList.add(e.getLocalizedMessage());
            areaParseResult = new String[]{"0"};
        }
//        StringBuilder errorMsgBuilder = new StringBuilder();
//        if (areaParseResult != null && areaParseResult.length > 0) {
//            for (String item : areaParseResult) {
//                errorMsgBuilder.append(item == null ? "null" : item);
//            }
//        }
//        //TODO: 打印日志
//        LogUtils.saveLog("地址解析错误调试", "B2BOrderManualBaseService#toOrderManual", errorMsgBuilder.toString(), null, null);
        Customer customer = getCustomer(orderVModel);

        //创建OrderItems
        Map<String, Object> map = createOrderItems(orderVModel, customer.getId());/* 创建工单的OrderItem */
        int orderServiceType = (Integer) map.get("orderServiceType");
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("items");

        StringBuilder content = new StringBuilder();
        Double totalCharge = 0.00;
        Double blockedCharge = 0.00;
        content.append("师傅，在您附近有一张  ");
        Long categoryId = null;
        for (OrderItem item : orderItemList) {
            if (categoryId == null) {
                categoryId = item.getProduct().getCategory().getId();
            }
            totalCharge = totalCharge + item.getCharge();
            blockedCharge = blockedCharge + item.getBlockedCharge();
            content.append(item.getServiceType().getName()).append(item.getBrand()).append(item.getProduct().getName());
        }
        content.append("的工单，请尽快登陆APP接单~");

        try {
            validateCustomerBalance(orderVModel, totalCharge, blockedCharge);
        } catch (Exception e) {
            errorMsgList.add(e.getLocalizedMessage());
        }

        Area area = new Area(StringUtils.toLong(areaParseResult[0]));
 /*       int canRush = 0;
        if (areaParseResult.length > 1) {
            if (StringUtils.isNotEmpty(areaParseResult[1])) {
                Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(area.getId());
                Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
                Area subArea = new Area(Long.valueOf(areaParseResult[1]));  // 获取4级街道   //2019-5-21
                canRush = orderService.isCanRush(categoryId==null?0L:categoryId,city.getId(),area.getId(),subArea.getId());
            }
        }*/
        User kefu = null;
//        String orderNo = generateOrderNo();
        String orderNo = generateOrderNoNew(orderVModel.getDataSource(), orderVModel.getKklOrderNo());

        //Order
//        Date createDate = new Date();
        Date createDate = generateOrderCreateDate(orderVModel.getDataSource(), orderVModel.getCreateDt());
        Order order = createOrder(orderVModel, totalCharge, blockedCharge, createDate); /* 创建工单的Order、OrderStatus、OrderFee */
        order.setAppMessage(content.toString());
        order.setOrderNo(orderNo);
        User createBy = MSUserUtils.get(orderVModel.getCreateById());
        if (createBy != null && createBy.getId() != null && createBy.getId() > 0) {
            order.setCreateBy(createBy);
        } else {
            order.setCreateBy(user);
        }

        //OrderCondition
        OrderCondition condition = createOrderCondition(orderVModel, orderItemList, areaParseResult, user, createDate);/* 创建工单的OrderCondition */
        int canRush = 0;
        int kefuType = 0;
       /* if(condition.getCustomer().getVipFlag()==1){
            kefuType = OrderCondition.VIP_KEFU_TYPE;
        }else if(condition.getSubArea()!=null && condition.getSubArea().getId()!=null){
            canRush = orderService.isCanRush(categoryId==null?0L:categoryId,condition.getCityId(),area.getId(),condition.getSubArea().getId());
            kefuType = orderService.getKefuType(categoryId==null?0L:categoryId,condition.getCityId(),area.getId(),condition.getSubArea().getId());
        }*/
        OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(categoryId==null?0L:categoryId,condition.getCityId(),area.getId(),condition.getSubArea().getId(),condition.getCustomer().getVipFlag(),condition.getCustomer().getVip());
        kefuType = orderKefuTypeRuleEnum.getCode();
        try {
            kefu = getRandomKefu(orderVModel.getCustomer().getId(), area.getId(), categoryId == null ? 0L : categoryId,kefuType,condition.getCityId(),condition.getProvinceId());
        } catch (Exception e) {
            kefu = new User(0L);
        }
        condition.setKefu(kefu);
        condition.setOrderServiceType(orderServiceType);
        condition.setOrderId(order.getId());
        condition.setOrderNo(orderNo);
        condition.setQuarter(order.getQuarter());
        if (StringUtils.isBlank(condition.getAddress())) {
            condition.setAddress(orderVModel.getUserAddress());
        }
        order.setOrderCondition(condition);
        //地理信息 2019-04-15
        OrderLocation location = new OrderLocation(order.getId(), order.getQuarter());
        location.setArea(condition.getArea());
        // add on 2019-5-21 begin
        if (areaParseResult != null && areaParseResult.length == 9 && "1".equals(areaParseResult[4])) {
            location.setLongitude(StringUtils.toDouble(areaParseResult[7]));
            location.setLatitude(StringUtils.toDouble(areaParseResult[8]));
        }
        // add on 2019-5-21 end
        order.setOrderLocation(location);
        //OrderItem
        for (OrderItem item : orderItemList) {
            item.setOrderId(order.getId());
            item.setQuarter(order.getQuarter());
        }
        order.setItems(orderItemList);

        MSResponse<Order> responseEntity = new MSResponse<>(MSErrorCode.SUCCESS);
        responseEntity.setData(order);
        if (errorMsgList.size() > 0) {
            String errorMsg = String.join("；", errorMsgList);
            responseEntity.setMsg(errorMsg);
        } else {
            responseEntity.setMsg("");
        }
        return responseEntity;
    }

    //endregion 手动转单

    //region 往KKL系统下单

    /**
     * 批量转单 - 下单
     */
    @Transactional()
    public Order saveOrderToKKLForBatch(B2BOrderVModel orderModel, User user) {
        Order order = toOrderAuto(orderModel, user);
        String repeatedOrderNo = orderService.getRepeateOrderNo(order.getOrderCondition().getCustomer().getId(), order.getOrderCondition().getPhone1());
        if (StringUtils.isNotBlank(repeatedOrderNo)) {
            order.setRepeateNo(repeatedOrderNo);
        }
        String orderNo = order.getOrderNo();
        try {
            orderService.createOrder_v2_1(order, null);
        } catch (Exception e) {
            try {
                if (StringUtils.isNoneBlank(orderNo)) {
                    SeqUtils.reputSequenceNo("OrderNO", order.getCreateDate(), orderNo);
                }
            } catch (Exception e1) {
                LogUtils.saveLog("订单号返还失败,", "B2BOrderManualBaseService.saveOrderToKKLForBatch", orderNo, e1, user);
            }
            LogUtils.saveLog("B2B:批量转单保存失败", "B2BOrderManualBaseService.saveOrderToKKLForBatch", GsonUtils.getInstance().toGson(order), e, user);
            throw new B2BOrderTranserFailureException("下单失败,请重试。");
        }
        return order;
    }

    /**
     * 手动转单-基本检查
     */
    public MSResponse<Order> transferOrderManual(B2BOrderVModel orderVModel, User user) {
        MSResponse<Order> responseEntity = new MSResponse<>(MSErrorCode.SUCCESS);

        try {
            responseEntity = toOrderManual(orderVModel, user);
        } catch (B2BOrderExistsException e1) {
            responseEntity.setCode(B2BOrderVModel.ERROR_CODE_B2BORDER_IS_CONVERTED.getCode());
            responseEntity.setMsg(StringUtils.left(e1.getMessage(), 200));
            Order order = new Order(e1.getOrderId());
            order.setDataSource(new Dict(e1.getDataSourceId().toString()));
            order.setWorkCardId(e1.getB2bOrderNo());
            order.setOrderNo(e1.getOrderNo());
            responseEntity.setData(order);
        } catch (Exception e) {
            responseEntity.setErrorCode(MSErrorCode.FAILURE);
            responseEntity.setMsg(String.format("转换系统工单错误:%s", e.getMessage()));
            LogUtils.saveLog("B2B工单转换", orderVModel.getOrderNo(), "", e, user);
        }
        return responseEntity;
    }

    /**
     * 手动保存B2B工单
     */
    public Order saveOrderToKKLForManual(B2BOrderConvertVModel orderConvertVModel, User user) {
        String b2bOrderNo = orderConvertVModel.getB2bOrderNo();
        int dataSourceId = orderConvertVModel.getDataSource() != null ? orderConvertVModel.getDataSource().getIntValue() : 0;
        if (!B2BDataSourceEnum.isB2BDataSource(dataSourceId) || StringUtils.isBlank(b2bOrderNo)) {
            throw new B2BOrderTranserFailureException("找不到工单来源，请重新选择。");
        }

//        validateB2BOrderIsExists(b2bOrderNo, dataSourceId);
        validateB2BOrderIsExistsNew(orderConvertVModel.getB2bOrderId(), b2bOrderNo, dataSourceId);

        String lockKey = String.format(RedisConstant.SD_TMP_ORDER_TRANSFER, b2bOrderNo);
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 30);
        if (!locked) {
            throw new B2BOrderTranserFailureException("此订单在处理中，请稍候重试，或刷新页面。");
        }

        try {
            Order order = toOrderManual(orderConvertVModel, user);
            //检查重单
            String repeatedOrderNo = orderService.getRepeateOrderNo(order.getOrderCondition().getCustomer().getId(), order.getOrderCondition().getPhone1());
            if (StringUtils.isNotBlank(repeatedOrderNo)) {
                order.setRepeateNo(repeatedOrderNo);
            }
            String orderNo = order.getOrderNo();
            boolean saveSuccess = true;
            String dailyLogKey = String.format(RedisConstant.SD_CREATE_ORDER_LOG, DateUtils.getDate());
            try {
                orderService.createOrder_v2_1(order, null);
            } catch (Exception e) {
                saveSuccess = false;
                try {
                    if (StringUtils.isNoneBlank(orderNo)) {
                        SeqUtils.reputSequenceNo("OrderNO", order.getCreateDate(), orderNo);
                    }
                } catch (Exception e1) {
                    LogUtils.saveLog("订单号返还失败,", "B2BOrderManualBaseService.saveOrderToKKLForManual", orderNo, e1, user);
                }
                LogUtils.saveLog("B2B:人工处理单保存失败", "B2BOrderManualBaseService.saveOrderToKKLForManual", GsonUtils.getInstance().toGson(orderConvertVModel), e, user);
                throw new B2BOrderTranserFailureException("下单失败,请重试。");
            } finally {
                try {
                    double score = Double.valueOf(orderNo.substring(1));
                    if (saveSuccess) {
                        redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, orderNo, score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
                    } else {
                        redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, GsonUtils.getInstance().toGson(orderConvertVModel), score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
                    }
                } catch (Exception e) {
                    LogUtils.saveLog("每日下单日志", "B2BOrderManualBaseService.saveOrderToKKLForManual", orderNo, null, user, Log.TYPE_ACCESS);
                }
            }
            return order;
        } finally {
            if (lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    //endregion  往KKL系统下单

    //region 修改KKL工单

    public Order toOrderManual(B2BOrderConvertVModel orderConvertVModel, User user) {
        validateServiceArea(orderConvertVModel);//检查区域设置
        validateAndSetCustomerInfo(orderConvertVModel, user);//检查与配置客户设置

        orderConvertVModel.setAddress(StringEscapeUtils.unescapeHtml4(orderConvertVModel.getAddress().replace("null", "")).replace("\"", "").replace(":", "|").trim());
        if (orderConvertVModel.getAddress().length() > B2BOrderConvertVModel.ADDRESS_MAX_LENGTH) {
            throw new B2BOrderTranserFailureException("详细地址长度超过数据库设定:" + String.valueOf(B2BOrderConvertVModel.ADDRESS_MAX_LENGTH));
        }

        List<OrderItemModel> list = orderConvertVModel.getItems();

        if (list == null || list.size() == 0) {
            throw new B2BOrderTranserFailureException("订单无明细项目，请添加。");
        }
        if (list.size() > 10) {
            throw new B2BOrderTranserFailureException("订单的明细项目不允许超过10个");
        }
        int productQty = list.stream().map(OrderItem::getQty).reduce(0, Integer::sum);
        if (productQty == 0) {
            throw new B2BOrderTranserFailureException("订单无明细项目，请添加。");
        }
        Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
        ServiceType serviceType;
        int orderServiceType = 0;
        Map<String, CustomerPrice> priceMap = customerService.getCustomerPriceMap(orderConvertVModel.getCustomer().getId());
        // 已客户现有上架产品为准
        List<Product> productList = productService.getCustomerProductList(orderConvertVModel.getCustomer().getId());
        if (CollectionUtils.isEmpty(productList)) {
            throw new B2BOrderTranserFailureException("读取客户现有产品列表失败");
        }
        Set<Long> productIdSet = productList.stream().map(t -> t.getId()).collect(Collectors.toSet());
        Map<String, Dict> expressCompanyMap = MSDictUtils.getDictMap("express_type");
        int hasSet = 0;
        int itemNo = 0;
        Set<String> productIds = Sets.newHashSet();//产品
        Long categoryId = null;
        Set<String> serviceTypeIds = Sets.newHashSet();//服务项目
        StringBuilder content = new StringBuilder();
        content.append("师傅，在您附近有一张  ");
        for (OrderItem item : list) {
            if (item.getProduct() == null) {
                continue;
            }
            // 以客户现有上架产品为准
            if (!productIdSet.contains(item.getProduct().getId())) {
                throw new B2BOrderTranserFailureException("客户未配置产品: " + item.getProduct().getName());
            }
            itemNo = itemNo + 10;
            validateAndSetOrderItem(item, itemNo, serviceTypeMap, priceMap, expressCompanyMap);
            if (item.getProduct().getSetFlag() == 1) {
                hasSet = 1;
            }
            //工单类型按服务项目设定为准
            serviceType = serviceTypeMap.get(item.getServiceType().getId());
            if (serviceType == null) {
                throw new B2BOrderTranserFailureException("确认服务项目的工单类型错误，请重试。");
            }
            //除维修(2)外，值最大的优先
            if (orderServiceType == 0) {
                orderServiceType = serviceType.getOrderServiceType();
            } else if (serviceType.getOrderServiceType() == 2) {
                orderServiceType = serviceType.getOrderServiceType();
            } else if (orderServiceType < serviceType.getOrderServiceType()) {
                orderServiceType = serviceType.getOrderServiceType();
            }
            serviceTypeIds.add(String.format(",%s,", item.getServiceType().getId()));
            productIds.add(String.format(",%s,", item.getProduct().getId()));
            if (categoryId == null) {
                categoryId = item.getProduct().getCategory().getId();
            }
            content.append(item.getServiceType().getName()).append(item.getBrand()).append(item.getProduct().getName());
        }
        content.append("的工单，请尽快登陆APP接单~");
        int canRush = 0;
        int kefuType = 0;
        //vip客户，不检查突击区域 ， 街道id小于等于3也不检查突击区域 2020-06-20 Ryan
        long subAreaId = Optional.ofNullable(orderConvertVModel.getSubArea()).map(t->t.getId()).orElse(0l);
        int vipFlag= Optional.ofNullable(orderConvertVModel.getCustomer()).map(t->t.getVipFlag()).orElse(0);
        int vipLevel = Optional.ofNullable(orderConvertVModel.getCustomer()).map(t->t.getVip()).orElse(0);
        Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(orderConvertVModel.getArea().getId());
        Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
        Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
        /*if(vipFlag==1){
            kefuType = OrderCondition.VIP_KEFU_TYPE;
        }else{ //有街道
            //Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(orderConvertVModel.getArea().getId());
            //Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
            canRush = orderService.isCanRush(categoryId == null ? 0L : categoryId,city.getId(),orderConvertVModel.getArea().getId(),orderConvertVModel.getSubArea().getId());
            kefuType = orderService.getKefuType(categoryId == null ? 0L : categoryId,city.getId(),orderConvertVModel.getArea().getId(),orderConvertVModel.getSubArea().getId());
        }*/
        OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(categoryId == null ? 0L : categoryId,city.getId(),orderConvertVModel.getArea().getId(),orderConvertVModel.getSubArea().getId(),vipFlag,vipLevel);
        kefuType = orderKefuTypeRuleEnum.getCode();
        if(kefuType==OrderCondition.RUSH_KEFU_TYPE){
            canRush = 1;
        }
        //检查并配置客服
        User kefu = orderConvertVModel.getKefu();
        if (kefu == null || kefu.getId() == null || kefu.getId() <= 0) {
            kefu = getRandomKefu(orderConvertVModel.getCustomer().getId(), orderConvertVModel.getArea().getId(), categoryId == null ? 0L : categoryId,kefuType,city.getId(),province.getId());
        }
        orderConvertVModel.setKefu(kefu);

        //重新计算价格
        OrderUtils.rechargeOrder(list);
        Double totalCharge = 0.00;
        Double blockedCharge = 0.00;
        for (OrderItemModel item : list) {
            totalCharge = totalCharge + item.getCharge();
            blockedCharge = blockedCharge + item.getBlockedCharge();
        }
        orderConvertVModel.setExpectCharge(totalCharge);
        orderConvertVModel.setBlockedCharge(blockedCharge);
        orderConvertVModel.setTotalQty(productQty);
        orderConvertVModel.setCreateBy(user);
//        Date date = new Date();
        Date date = generateOrderCreateDate(orderConvertVModel.getDataSource().getIntValue(), orderConvertVModel.getCreateDt());
        orderConvertVModel.setCreateDate(date);
        //经纬度 2019-04-15
        if (orderConvertVModel.getLatitude() == 0 || orderConvertVModel.getLongitude() == 0) {
            String[] areaParseResult = AreaUtils.getLocation(orderConvertVModel.getArea().getName() + orderConvertVModel.getAddress());
            if (areaParseResult != null && areaParseResult.length == 2) {
                orderConvertVModel.setLongitude(StringUtils.toDouble(areaParseResult[0]));
                orderConvertVModel.setLatitude(StringUtils.toDouble(areaParseResult[1]));
            }
        }
        Order order = null;
        String orderNo = null;

        if (StringUtils.isBlank(orderConvertVModel.getOrderNo())) {
//            orderNo = generateOrderNo();
            orderNo = generateOrderNoNew(orderConvertVModel.getDataSource().getIntValue(), orderConvertVModel.getOrderNo());
            orderConvertVModel.setOrderNo(orderNo);
        }
        String quarter = QuarterUtils.getSeasonQuarter(orderConvertVModel.getCreateDate());
        orderConvertVModel.setQuarter(quarter);
        Long orderId = orderConvertVModel.getId();
        if (orderId == null || orderId <= 0) {
            orderId = SeqUtils.NextIDValue(SeqUtils.TableName.Order);
        }
        orderConvertVModel.setId(orderId);
        if (orderServiceType == 0) {
            orderServiceType = 2;//维修
        }
        orderConvertVModel.setOrderServiceType(orderServiceType);
        order = mapperFacade.map(orderConvertVModel, Order.class);
        order.setAppMessage(content.toString());

        Dict status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), Dict.DICT_TYPE_ORDER_STATUS);
        OrderCondition orderCondition = order.getOrderCondition();
        orderCondition.setStatus(status);
        orderCondition.setHasSet(hasSet);
        // 移到OrderToB2BOrderConvertVModelMapper,并在item中处理和判断
        orderCondition.setProductIds(String.join(",", productIds).replace(",,,", ",,"));
        orderCondition.setProductCategoryId(categoryId == null ? 0L : categoryId);
        orderCondition.setServiceTypes(String.join(",", serviceTypeIds).replace(",,,", ",,"));
        orderCondition.setCanRush(canRush);
        orderCondition.setKefuType(kefuType);
        return order;
    }

    /**
     * 编辑工单
     */
    @Transactional()
    protected boolean modifyKKLOrder(B2BOrderModifyEntity entity) {
        boolean result = false;
        String lockKey = String.format(RedisConstant.SD_ORDER_LOCK, entity.getKklOrderId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, ORDER_LOCK_EXPIRED);
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        boolean logEnabled = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            HashMap<String, Object> params = Maps.newHashMap();
            List<String> changeLogs = Lists.newArrayList();
            String changeLog;
            String originalParam;
            if (StringUtils.isNotBlank(entity.getUserName())) {
                params.put("userName", entity.getUserName());
                originalParam = StringUtils.isNotBlank(order.getOrderCondition().getUserName()) ? order.getOrderCondition().getUserName() : "无";
                changeLog = "用户名：" + originalParam + " => " + entity.getUserName();
                changeLogs.add(changeLog);
            }
            if (StringUtils.isNotBlank(entity.getUserMobile())) {
                params.put("servicePhone", StringUtils.left(entity.getUserMobile(), 11));
                originalParam = StringUtils.isNotBlank(order.getOrderCondition().getServicePhone()) ? order.getOrderCondition().getServicePhone() : "无";
                changeLog = "实际联络电话：" + originalParam + " => " + entity.getUserMobile();
                changeLogs.add(changeLog);
            }
            if (StringUtils.isNotBlank(entity.getUserPhone())) {
                params.put("phone2", entity.getUserPhone());
                originalParam = StringUtils.isNotBlank(order.getOrderCondition().getPhone2()) ? order.getOrderCondition().getPhone2() : "无";
                changeLog = "用户座机：" + originalParam + " => " + entity.getUserPhone();
                changeLogs.add(changeLog);
            }
            if (StringUtils.isNotBlank(entity.getUserAddress())) {
                String[] areaParseResult = null;
                try {
                    areaParseResult = parseAddressNew(entity.getUserAddress());
                    String serviceAddress = StringUtils.toString(order.getOrderCondition().getArea().getName()) + StringUtils.toString(order.getOrderCondition().getServiceAddress());
                    originalParam = StringUtils.isNotBlank(serviceAddress) ? serviceAddress : "无";
                    changeLog = "实际上门地址：" + originalParam + " => " + entity.getUserAddress();
                    changeLogs.add(changeLog);
                } catch (Exception e) {
                    B2BOrderModifyEntity.saveFailureLog(entity, "地址解析失败", "B2BOrderManualBaseService.modifyKKLOrder", e);
                }
                if (areaParseResult != null) {
                    Area area = new Area();
                    if (areaParseResult.length > 3) {
                        Long areaId = StringUtils.toLong(areaParseResult[0]);
                        Long subAreaId = StringUtils.toLong(areaParseResult[1]);
                        String areaName = areaParseResult[2].trim();
                        String serviceAddress = areaParseResult[3].trim();
                        if (areaId > 0 && StringUtils.isNotBlank(areaName) && StringUtils.isNotBlank(serviceAddress)) {
                            originalParam = StringUtils.isNotBlank(order.getOrderCondition().getArea().getName()) ? order.getOrderCondition().getArea().getName() : "无";
                            changeLog = "区域：" + originalParam + " => " + areaName;
                            changeLogs.add(changeLog);
                            originalParam = order.getOrderCondition().getSubArea().getId() != null && order.getOrderCondition().getSubArea().getId() > 0 ?
                                    order.getOrderCondition().getSubArea().getId().toString() : "无";
                            changeLog = "4级街道：" + originalParam + " => " + subAreaId.toString();
                            changeLogs.add(changeLog);
                            area.setId(areaId);
                            area.setName(areaName);
                            params.put("area", area);
                            Area subArea = new Area();
                            subArea.setId(subAreaId);
                            params.put("subArea", subArea);
                            params.put("serviceAddress", serviceAddress);
                            params.put("address", serviceAddress);
                            if (areaParseResult.length == 9 && areaParseResult[4].equals("1")) {
                                Double longitude = StringUtils.toDouble(areaParseResult[7]);
                                Double latitude = StringUtils.toDouble(areaParseResult[8]);
                                Map<String, Object> locationParams = Maps.newHashMap();
                                locationParams.put("longitude", longitude);
                                locationParams.put("latitude", latitude);
                                locationParams.put("area", area);
                                locationParams.put("orderId", order.getId());
                                locationParams.put("quarter", order.getQuarter());
                                orderLocationDao.updateByMap(locationParams);
                                logEnabled = true;
                            }
                        }
                    }
                }
            }
            if (entity.getHopeArrivalTime() > 0) {
                Date date = new Date(entity.getHopeArrivalTime());
                if (entity.getOperationType() == B2BModifyOperationEnum.EXPRESS_SIGN) {
                    params.put("arrivalDate", date);
                    String arrivalDateStr = DateUtils.formatDate(order.getOrderCondition().getArrivalDate(), "yyyy-MM-dd HH:mm:ss");
                    originalParam = StringUtils.isNotBlank(arrivalDateStr) ? arrivalDateStr : "无";
                    changeLog = "到货时间：" + originalParam + " => " + DateUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss");
                    changeLogs.add(changeLog);
                } else if (entity.getOperationType() == B2BModifyOperationEnum.EXPRESS_RESCHEDULE) {
                    String estimatedReceivedDateStr = DateUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss");
                    originalParam = StringUtils.isNotBlank(order.getOrderAdditionalInfo().getEstimatedReceiveDate()) ? order.getOrderAdditionalInfo().getEstimatedReceiveDate() : "无";
                    changeLog = "预计到货时间：" + originalParam + " => " + estimatedReceivedDateStr;
                    changeLogs.add(changeLog);
                    OrderAdditionalInfo info = order.getOrderAdditionalInfo();
                    info.setEstimatedReceiveDate(estimatedReceivedDateStr);
                    byte[] addtionInfoPb = OrderAdditionalInfoUtils.additionalInfoToPbBytes(info);
                    orderHeadDao.updateOrderAdditionalInfo(order.getId(), order.getQuarter(),addtionInfoPb);//2020-12-03 sd_order -> sd_order_head
                    logEnabled = true;
                }
            }
            if (!params.isEmpty()) {
                params.put("orderId", order.getId());
                params.put("quarter", order.getQuarter());
                params.put("updateBy", B2BOrderVModel.b2bUser);
                orderDao.updateCondition(params);
                logEnabled = true;
            }
            if (logEnabled) {
                //B2B编辑工单操作记入工单日志
                String changeLogsStr = StringUtils.join(changeLogs, "；");
                OrderProcessLog processLog = new OrderProcessLog();
                processLog.setQuarter(order.getQuarter());
                processLog.setOrderId(order.getId());
                processLog.setAction("B2B客户修改订单");
                processLog.setActionComment(StringUtils.left(String.format("【修改工单】%s", changeLogsStr), 250));
                String status = MSDictUtils.getDictLabel(order.getOrderCondition().getStatus().getValue(), "order_status", "订单已审核");
                processLog.setStatus(status);
                processLog.setStatusValue(Integer.parseInt((order.getOrderCondition().getStatus().getValue())));
                processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
                processLog.setCloseFlag(0);
                processLog.setCreateBy(B2BOrderVModel.b2bUser);
                processLog.setCreateDate(new Date());
                processLog.setCustomerId(order.getOrderCondition().getCustomerId());
                processLog.setDataSourceId(order.getDataSourceId());
                orderService.saveOrderProcessLogNew(processLog);
                result = true;
                OrderCacheUtils.delete(order.getId());
            }
        } else {
            B2BOrderModifyEntity.saveFailureLog(entity, "读取工单失败", "B2BOrderManualBaseService.modifyKKLOrder", null);
        }
        return result;
    }

    /**
     * 取消工单
     */
    @Transactional()
    protected boolean cancelKKLOrder(B2BOrderModifyEntity entity) {
        boolean result = false;
        Order order = orderCacheReadService.getOrderById(entity.getKklOrderId(), null, OrderUtils.OrderDataLevel.CONDITION, true, true);
        if (order != null && order.getOrderCondition() != null) {
            String remarks = StringUtils.toString(entity.getRemarks()) + "（" + B2BDataSourceEnum.SUNING.name + "平台通知取消B2B工单）";
            if (order.canCanceled()) {
                orderService.cancelOrderNew(order.getId(), B2BOrderVModel.b2bUser, remarks, false);
                result = true;
            } else {
                Dict cancelResponsibleDict = MSDictUtils.getDictByValue(CANCEL_RESPONSIBLE_VALUE_CUSTOMER_NOTIFY, Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
                orderService.returnOrderNew(order.getId(), cancelResponsibleDict, "", remarks, B2BOrderVModel.b2bUser);
                result = true;
            }
        } else {
            B2BOrderModifyEntity.saveFailureLog(entity, "读取工单失败", "B2BOrderManualBaseService.cancelKKLOrder", null);
        }
        return result;
    }

    //endregion 修改KKL工单


    //region 获取完工图片与条码

    protected Map<Long, List<String>> getProductIdToB2BProductCodeMapping(List<OrderItem> orderItems) {
        Map<Long, List<String>> result = Maps.newHashMap();
        if (orderItems != null && !orderItems.isEmpty()) {
            List<Long> pIds = orderItems.stream().filter(i -> i.getProduct() != null && i.getProduct().getId() != null).map(OrderItem::getProductId).distinct().collect(Collectors.toList());
            Map<Long, Product> productMap = productService.getProductMap(pIds);
            for (OrderItem item : orderItems) {
                Product product = productMap.get(item.getProductId());
                if (product != null && product.getId() != null) {
                    ThreeTuple<Set<Long>, String, Integer> tuple = new ThreeTuple<>();
                    tuple.setBElement(StringUtils.toString(item.getB2bProductCode()));
                    tuple.setCElement(item.getQty());
                    if (product.getSetFlag() == 1) {
                        String[] setIds = product.getProductIds().split(",");
                        for (String id : setIds) {
                            Long productId = StringUtils.toLong(id);
                            if (productId > 0) {
                                if (result.containsKey(productId)) {
                                    if (StringUtils.isNotBlank(item.getB2bProductCode()) && !result.get(productId).contains(item.getB2bProductCode())) {
                                        result.get(productId).add(item.getB2bProductCode());
                                    }
                                } else {
                                    result.put(productId, Lists.newArrayList(item.getB2bProductCode()));
                                }
                            }
                        }

                    } else {
                        if (result.containsKey(item.getProductId())) {
                            if (StringUtils.isNotBlank(item.getB2bProductCode()) && !result.get(item.getProductId()).contains(item.getB2bProductCode())) {
                                result.get(item.getProductId()).add(item.getB2bProductCode());
                            }
                        } else {
                            result.put(item.getProductId(), Lists.newArrayList(item.getB2bProductCode()));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Autowired
    private OrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

    protected double getActualTotalSurcharge(Long orderId, String quarter) {
        double actualTotalSurcharge = 0;
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            AuxiliaryMaterialMaster master = orderAuxiliaryMaterialService.getAuxiliaryMaterialMaster(orderId, quarter);
            if (master != null && master.getActualTotalCharge() != null) {
                actualTotalSurcharge = master.getActualTotalCharge();
            }
        }
        return actualTotalSurcharge;
    }


    protected List<CanboOrderCompleted.CompletedItem> getOrderCompletedItems(Long orderId, String quarter, List<OrderItem> orderItems) {
        List<CanboOrderCompleted.CompletedItem> result = Lists.newArrayList();
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemComplete> completeList = orderItemCompleteService.getByOrderId(orderId, quarter);
            CanboOrderCompleted.CompletedItem completedItem;
            ProductCompletePicItem picItem;
            if (completeList != null && !completeList.isEmpty()) {
                Map<Long, List<String>> b2bProductCodeMap = getProductIdToB2BProductCodeMapping(orderItems);

                List<String> b2bProductCodeList;
                for (OrderItemComplete item : completeList) {
                    item.setItemList(OrderUtils.fromProductCompletePicItemsJson(item.getPicJson()));
                    Map<String, ProductCompletePicItem> picItemMap = Maps.newHashMap();
                    for (ProductCompletePicItem innerItem : item.getItemList()) {
                        picItemMap.put(innerItem.getPictureCode(), innerItem);
                    }
                    completedItem = new CanboOrderCompleted.CompletedItem();

                    //获取B2B产品编码
                    b2bProductCodeList = b2bProductCodeMap.get(item.getProduct().getId());
                    if (b2bProductCodeList != null && !b2bProductCodeList.isEmpty()) {
                        completedItem.setItemCode(b2bProductCodeList.get(0));
                        if (b2bProductCodeList.size() > 1) {
                            b2bProductCodeList.remove(0);
                        }
                    } else {
                        completedItem.setItemCode("");
                    }

                    completedItem.setBarcode(StringUtils.toString(item.getUnitBarcode()));
                    completedItem.setOutBarcode(StringUtils.toString(item.getOutBarcode()));
                    //条码图片
                    picItem = picItemMap.get("pic4");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic4(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    //现场图片
                    picItem = picItemMap.get("pic1");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic1(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    picItem = picItemMap.get("pic2");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic2(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    picItem = picItemMap.get("pic3");
                    if (picItem != null && StringUtils.isNotBlank(picItem.getUrl())) {
                        completedItem.setPic3(OrderPicUtils.getOrderPicHostDir() + picItem.getUrl());
                    }
                    result.add(completedItem);
                }
            }
        }
        return result;
    }


    //endregion
}
