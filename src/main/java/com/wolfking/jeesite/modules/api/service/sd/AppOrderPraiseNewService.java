/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.api.service.sd;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeeExamplePicItem;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.api.entity.common.AppDict;
import com.wolfking.jeesite.modules.api.entity.receipt.praise.AppGetOrderPraiseDetailInfoResponse;
import com.wolfking.jeesite.modules.api.entity.receipt.praise.AppGetOrderPraiseListItemNewResponse;
import com.wolfking.jeesite.modules.api.entity.receipt.praise.AppOrderPraiseFeeStandard;
import com.wolfking.jeesite.modules.api.entity.receipt.praise.AppPraisePicItem;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.dao.AppOrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderItemCompleteDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.feign.AppPraiseFeign;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * App工单好评
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AppOrderPraiseNewService extends LongIDBaseService {

    /**
     * 好评费计算的误差
     */
    private static final double PRAISE_FEE_DIFF = 0.0001;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSCustomerPraiseFeeService customerPraiseFeeService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;

    @Autowired
    private AreaService areaService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Resource
    private AppOrderDao appOrderDao;

    @Autowired
    private AppPraiseFeign appPraiseFeign;

    @Autowired
    private SequenceIdService sequenceIdService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;
    @Resource
    private OrderItemCompleteDao orderItemCompleteDao;
    @Autowired
    private ProductService productService;


    /**
     * 获取好评单的图片
     */
    public List<AppPraisePicItem> getUploadedPraisePics(Long orderId, String quarter, Long servicePointId) {
        List<AppPraisePicItem> result = Lists.newArrayList();
        Praise praise = getPraise(orderId, quarter, servicePointId);
        if (praise != null && praise.getPicItems() != null && !praise.getPicItems().isEmpty()) {
            AppPraisePicItem picItem;
            for (PraisePicItem item : praise.getPicItems()) {
                picItem = new AppPraisePicItem(item.getCode(), item.getName(), OrderPicUtils.getPraisePicUrl(item.getUrl()));
                result.add(picItem);
            }
        }
        return result;
    }

    public Map<String, MDCustomerPraiseFeePraiseStandardItem> getAppPraisePicItemStandard(Long customerId) {
        MDCustomerPraiseFee fee = getCustomerPraiseFee(customerId);
        Map<String, MDCustomerPraiseFeePraiseStandardItem> result = Maps.newHashMap();
        if (fee.getPraiseStandardItems() != null && !fee.getPraiseStandardItems().isEmpty()) {
            for (MDCustomerPraiseFeePraiseStandardItem standardItem : fee.getPraiseStandardItems()) {
                result.put(standardItem.getCode(), standardItem);
            }
        }
        return result;
    }

    /**
     * 获取App的好评费标准
     */
    public AppOrderPraiseFeeStandard getAppPraiseFeeStandard(Long customerId) {
        MDCustomerPraiseFee fee = getCustomerPraiseFee(customerId);
        AppOrderPraiseFeeStandard standard = new AppOrderPraiseFeeStandard();
        standard.setPraiseRequirement(fee.getPraiseRequirement());
        if (fee.getPraiseFeeFlag() == 1) {
            standard.setMinCustomerPraiseFee(fee.getPraiseFee());
            standard.setMaxCustomerPraiseFee(fee.getMaxPraiseFee());
            standard.setDiscount(fee.getDiscount());
        }

        //好评图片标准
        if (fee.getPraiseStandardItems() != null && !fee.getPraiseStandardItems().isEmpty()) {
            List<AppOrderPraiseFeeStandard.PicItem> picItems = Lists.newArrayList();
            AppOrderPraiseFeeStandard.PicItem picItem;
            for (MDCustomerPraiseFeePraiseStandardItem item : fee.getPraiseStandardItems()) {
                picItem = new AppOrderPraiseFeeStandard.PicItem();
                picItem.setCode(item.getCode());
                picItem.setName(item.getName());
                picItem.setFee(item.getFee());
                picItem.setMustFlag(item.getMustFlag());
                picItems.add(picItem);
            }
            standard.setPics(picItems);
        }

        //示例图片
        if (fee.getExamplePicItems() != null && !fee.getExamplePicItems().isEmpty()) {
            List<AppOrderPraiseFeeStandard.ExamplePicItem> examplePicItems = Lists.newArrayList();
            AppOrderPraiseFeeStandard.ExamplePicItem picItem;
            for (MDCustomerPraiseFeeExamplePicItem item : fee.getExamplePicItems()) {
                picItem = new AppOrderPraiseFeeStandard.ExamplePicItem();
                picItem.setCode(item.getCode());
                picItem.setName(item.getName());
                picItem.setUrl(OrderPicUtils.getPraiseExamplePicUrl(item.getUrl()));
                examplePicItems.add(picItem);
            }
            standard.setExamplePics(examplePicItems);
        }

        return standard;
    }


    /**
     * 获取好评单信息
     */
    public AppGetOrderPraiseDetailInfoResponse getOrderPraiseDetailInfo(Order order, Long servicePointId) {
        AppOrderPraiseFeeStandard standard = getAppPraiseFeeStandard(order.getOrderCondition().getCustomerId());
        AppGetOrderPraiseDetailInfoResponse result = new AppGetOrderPraiseDetailInfoResponse();
        result.setStandard(standard);
        Praise praise = getPraise(order.getId(), order.getQuarter(), servicePointId);
        if (praise != null) {
            PraiseStatusEnum statusEnum = PraiseStatusEnum.fromCode(praise.getStatus());
            result.setPraiseStatus(new AppDict(String.valueOf(statusEnum.code), statusEnum.msg));
            result.setPraiseFee(praise.getStatus() >= PraiseStatusEnum.APPROVE.code ?
                    praise.getServicepointPraiseFee() : praise.getApplyServicepointPraiseFee());
            if (praise.getStatus() == PraiseStatusEnum.APPROVE.code) {
                result.setRejectionCategory(new AppDict("0", "有效好评"));
            } else {
                int rejectionCategory = praise.getRejectionCategory() == null ? 0 : praise.getRejectionCategory();
                Dict praiseAbnormalDict = MSDictUtils.getDictByValue(String.valueOf(rejectionCategory), "praise_abnormal_type");
                if (praiseAbnormalDict != null) {
                    result.setRejectionCategory(new AppDict(praiseAbnormalDict.getValue(), praiseAbnormalDict.getLabel()));
                }
            }
            //装配已上传的好评图片
            Map<String, String> praisePicMap = Maps.newHashMap();
            if (praise.getPicItems() != null && !praise.getPicItems().isEmpty()) {
                for (PraisePicItem item : praise.getPicItems()) {
                    if (StringUtils.isNotBlank(item.getCode()) && StringUtils.isNotBlank(item.getUrl())) {
                        praisePicMap.put(item.getCode(), item.getUrl());
                    }
                }
                String uploadedPicUrl;
                for (AppOrderPraiseFeeStandard.PicItem standardPicItem : result.getStandard().getPics()) {
                    uploadedPicUrl = praisePicMap.get(standardPicItem.getCode());
                    if (StringUtils.isNotBlank(uploadedPicUrl)) {
                        standardPicItem.setUploadFlag(1);
                        standardPicItem.setUrl(OrderPicUtils.getPraisePicUrl(uploadedPicUrl));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 保存APP好评单
     */
    @Transactional()
    public void saveOrderPraiseInfo(Order order, Long servicePointId, Long engineerId, List<PraisePicItem> praisePicItems,
                                    Double customerApplyPraiseFee, Double servicePointApplyPraiseFee, User user) {
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION, order.getId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        List<String> picCodeList = praisePicItems.stream().map(PraisePicItem::getCode).collect(Collectors.toList());
        //重新计算好评费
        NameValuePair<Double, Double> calculatedPraiseFee = calculatePraiseFee(order.getOrderCondition().getCustomerId(), picCodeList);
        //检查APP提交的好评费是否正确
        checkPraiseFee(calculatedPraiseFee, customerApplyPraiseFee, servicePointApplyPraiseFee);
        Date now = new Date();
        try {
            Praise praise = getPraise(order.getId(), order.getQuarter(), servicePointId);
            if (praise != null && praise.getStatus() != PraiseStatusEnum.NEW.code) {
                throw new OrderException("此好评单当前不允许修改");
            }
            if (praise == null) {
                createOrderPraise(order, servicePointId, engineerId, praisePicItems, calculatedPraiseFee.getName(), calculatedPraiseFee.getValue(), user, now);
            } else {
                updateOrderPraise(praise, praisePicItems, calculatedPraiseFee.getName(), calculatedPraiseFee.getValue(), user, now);
            }
        } finally {
            if (lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    /**
     * 获取驳回的好评单列表
     */
    public Page<AppGetOrderPraiseListItemNewResponse> getOrderPraiseList(Long servicePointId, Long engineerId, Boolean isPrimaryAccount, Integer status, Integer pageNo, Integer pageSize) {

        List<AppGetOrderPraiseListItemNewResponse> itemList = Lists.newArrayList();
        PraisePageSearchModel searchModel = new PraisePageSearchModel();
        searchModel.setPage(new MSPage<>(pageNo, pageSize));
        searchModel.setStatus(status);
        searchModel.setServicepointId(servicePointId);
        searchModel.setEngineerId(isPrimaryAccount ? null : engineerId);
        MSResponse<MSPage<PraiseAppListModel>> msResponse = appPraiseFeign.findPraiseList(searchModel);
        Page<AppGetOrderPraiseListItemNewResponse> returnPage = new Page<>(pageNo, pageSize);
        if (MSResponse.isSuccess(msResponse)) {
            List<PraiseAppListModel> models = msResponse.getData().getList();
            if (models != null && !models.isEmpty()) {
                Map<String, Dict> praiseAbnormalTypeMap = MSDictUtils.getDictMap("praise_abnormal_type");
                AppGetOrderPraiseListItemNewResponse item;
                Dict praiseAbnormalDict;
                for (PraiseAppListModel model : models) {
                    item = new AppGetOrderPraiseListItemNewResponse();
                    item.setPraiseId(model.getId());
                    item.setOrderNo(model.getOrderNo());
                    item.setUserName(model.getUserName());
                    item.setServicePhone(model.getUserPhone());
                    int rejectionCategory = model.getRejectionCategory() == null ? 0 : model.getRejectionCategory();
                    praiseAbnormalDict = praiseAbnormalTypeMap.get(String.valueOf(rejectionCategory));
                    if (praiseAbnormalDict != null) {
                        item.setRejectionCategory(new AppDict(praiseAbnormalDict.getValue(), praiseAbnormalDict.getLabel()));
                    }
                    if (model.getPicItems() != null && !model.getPicItems().isEmpty()) {
                        for (PraisePicItem praisePicItem : model.getPicItems()) {
                            item.getPics().add(new AppGetOrderPraiseListItemNewResponse.PicItem(praisePicItem.getCode(), praisePicItem.getName(), OrderPicUtils.getPraisePicUrl(praisePicItem.getUrl())));
                        }
                    }
                    item.setCreateDate(model.getCreateDt());
                    itemList.add(item);
                }
            }
            returnPage.setCount(msResponse.getData().getRowCount());
            returnPage.setList(itemList);
        }
        return returnPage;
    }


    /**
     * 检查是否允许好评
     */
    public NameValuePair<Boolean, String> checkPraiseCondition(Order order, User user) {
        NameValuePair<Boolean, String> result = new NameValuePair<>(false, "");
        OrderCondition condition = order.getOrderCondition();
        //检查图片数量
        Customer customer = customerService.getFromCache(condition.getCustomerId());
        if (null != customer && customer.getMinUploadNumber()>0 && condition.getFinishPhotoQty() < customer.getMinUploadNumber()) {
            result.setValue(MessageFormat.format("此工单客户要求必须上传 {0}~{1} 张图片,请上传完工照片后再上传好评照片！", customer.getMinUploadNumber(),customer.getMaxUploadNumber()));
            return result;
        }
        //检查是否上传条码
        boolean checkSNResult = checkCompletePicAndProductBarCode(order.getId(), order.getQuarter(), condition.getCustomerId(), order.getItems());
        if(!checkSNResult){
            result.setValue("此工单客户要求上传产品条码，请上传产品条码后再上传好评照片！");
            return result;
        } else {
            result.setName(true);
            Date now = DateUtil.date();
            b2BCenterOrderService.completeOrder(order, now, user, now);
        }
        return result;
    }


    private boolean checkCompletePicAndProductBarCode(Long orderId, String quarter, Long customerId, List<OrderItem> orderItems) {
        boolean result = false;
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter) && customerId != null && customerId > 0 && orderItems != null && !orderItems.isEmpty()) {
            result = true;
            List<OrderItemComplete> uploadedCompletePics = orderItemCompleteDao.getByOrderId(orderId, quarter);
            //拆分套组，获取最终的产品id
            List<Long> tempProductIds = orderItems.stream().filter(i -> i.getProduct() != null && i.getProduct().getId() != null).map(OrderItem::getProductId).collect(Collectors.toList());
            Map<Long, Product> productMap = productService.getProductMap(tempProductIds);
            Set<Long> productIdSet = Sets.newHashSet();
            Product product;
            Long productId;
            for (Long idLong : tempProductIds) {
                product = productMap.get(idLong);
                if (product != null) {
                    if (product.getSetFlag() == 1) {
                        final String[] setIds = product.getProductIds().split(",");
                        for (String idString : setIds) {
                            productId = StringUtils.toLong(idString);
                            if (productId > 0) {
                                productIdSet.add(productId);
                            }
                        }
                    } else {
                        productIdSet.add(idLong);
                    }
                }
            }
            uploadedCompletePics = uploadedCompletePics.stream().filter(i -> productIdSet.contains(i.getProduct().getId())).collect(Collectors.toList());
            if (!uploadedCompletePics.isEmpty()) {
                List<Long> productIds = uploadedCompletePics.stream().map(i -> i.getProduct().getId()).distinct().collect(Collectors.toList());
                Map<Long, ProductCompletePic> completePicRuleMap = OrderUtils.getCustomerProductCompletePicMap(productIds, customerId);
                ProductCompletePic picRule;
                for (OrderItemComplete item : uploadedCompletePics) {
                    picRule = completePicRuleMap.get(item.getProduct().getId());
                    if (picRule != null && picRule.getBarcodeMustFlag() != null && picRule.getBarcodeMustFlag() == 1
                            && StringUtils.isBlank(item.getUnitBarcode())) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 创建工单好评单
     */
    private void createOrderPraise(Order order, Long servicePointId, Long engineerId,
                                   List<PraisePicItem> picItems,
                                   Double customerApplyPraiseFee, Double servicePointApplyPraiseFee,
                                   User user, Date createDate) {
        OrderCondition condition = order.getOrderCondition();
        Long customerId = condition.getCustomer().getId();
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(customerId);
        if(customerPraiseFee == null){
            throw new RuntimeException("读取客户好评设定信息失败");
        }
        String praiseNo = SeqUtils.NextSequenceNo("praiseNo", 0, 3);
        if (StringUtils.isBlank(praiseNo)) {
            throw new RuntimeException("生成好评单号失败");
        }

        List<String> productNames = order.getItems().stream().map(t -> t.getProduct().getName()).distinct().collect(Collectors.toList());
        Praise praise = new Praise();
        praise.setId(sequenceIdService.nextId());
        praise.setOrderId(order.getId());
        praise.setOrderNo(order.getOrderNo());
        praise.setQuarter(order.getQuarter());
        praise.setProductNames(StringUtils.left(StringUtils.join(productNames, ","), 100));
        praise.setProductCategoryId(condition.getProductCategoryId());
        praise.setDataSource(order.getDataSourceId());
        praise.setWorkcardId(order.getWorkCardId());
        praise.setParentBizOrderId(order.getParentBizOrderId());
        praise.setAreaId(condition.getArea().getId());
        praise.setSubAreaId(condition.getSubArea().getId());
        Area area = areaService.getFromCache(condition.getArea().getId());
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                praise.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                praise.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
            }
        }
        praise.setCustomerId(condition.getCustomer().getId());
        String shopId = Optional.ofNullable(order.getB2bShop()).map(t->t.getShopId()).orElse(StrUtil.EMPTY);
        praise.setShopId(shopId==null? StrUtil.EMPTY:shopId);
        praise.setCustomerPaymentType(customerPraiseFee.getOnlineFlag());//客户好评费结算方式
        praise.setServicepointId(servicePointId);
        praise.setEngineerId(engineerId);
        praise.setKefuId(condition.getKefu() != null && condition.getKefu().getId() != null ? condition.getKefu().getId() : 0);
        praise.setUserName(condition.getUserName());
        praise.setUserPhone(condition.getServicePhone());
        praise.setUserAddress(condition.getArea().getName() + condition.getServiceAddress());

        praise.setPraiseNo(praiseNo);
        praise.setStatus(PraiseStatusEnum.NEW.code);
        praise.setApplyCustomerPraiseFee(customerApplyPraiseFee);
        praise.setApplyServicepointPraiseFee(servicePointApplyPraiseFee);
        praise.setPicItems(picItems);
        praise.setRemarks("");
        praise.setCreateById(user.getId());
        praise.setCreateDt(createDate.getTime());
        praise.setCanRush(condition.getCanRush());
        praise.setKefuType(condition.getKefuType());

        PraiseLog praiseLog = new PraiseLog();
        praiseLog.setId(sequenceIdService.nextId());//2020/05/25
        praiseLog.setStatus(PraiseStatusEnum.NEW.code);
        praiseLog.setActionType(PraiseStatusEnum.NEW.code);
        praiseLog.setCreatorType(PraiseCreatorTypeEnum.SERVICE_POINT.code);
        praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
        praiseLog.setContent("[新建]好评单");

        praise.setPraiseLog(praiseLog);
        MSResponse<Praise> msResponse = orderPraiseFeign.saveApplyPraise(praise);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("创建好评单失败失败:" + msResponse.getMsg());
        }

        orderStatusFlagService.updatePraiseStatus(order.getId(), order.getQuarter(), PraiseStatusEnum.NEW.code);

        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setOrderId(order.getId());
        processLog.setQuarter(order.getQuarter());
        processLog.setAction("新建好评单");
        processLog.setActionComment("APP创建好评单：" + praiseNo);
        processLog.setStatus(condition.getStatus().getLabel());
        processLog.setStatusValue(condition.getStatusValue());
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(user);
        processLog.setCreateDate(createDate);
        processLog.setCustomerId(condition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        processLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
        orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);
        try {
            servicePointOrderBusinessService.syncPraiseStatus(praise.getOrderId(), praise.getQuarter(), praise.getServicepointId(),
                    praise.getStatus(), user.getId(), createDate.getTime());
        } catch (Exception e) {
            log.error("发送消息队列更新好评单状态失败 form: {}", GsonUtils.toGsonString(praise), e);
        }
    }

    /**
     * 修改工单好评单
     */
    private void updateOrderPraise(Praise praise, List<PraisePicItem> picItems,
                                   Double customerApplyPraiseFee, Double servicePointApplyPraiseFee, User user, Date updateDate) {
        praise.setPicItems(picItems);
        praise.setApplyCustomerPraiseFee(customerApplyPraiseFee);
        praise.setApplyServicepointPraiseFee(servicePointApplyPraiseFee);
        praise.setUpdateById(user.getId());
        praise.setUpdateBy(user.getName());
        praise.setUpdateDt(updateDate.getTime());

        PraiseLog praiseLog = new PraiseLog();
        praiseLog.setId(sequenceIdService.nextId());//2020/05/25
        praiseLog.setStatus(praise.getStatus());
        praiseLog.setActionType(PraiseActionEnum.UPDATE_PIC.code);
        praiseLog.setCreatorType(PraiseCreatorTypeEnum.SERVICE_POINT.code);
        praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
        praiseLog.setContent("修改好评单");

        praise.setPraiseLog(praiseLog);
        MSResponse<Integer> msResponse = orderPraiseFeign.resubmit(praise);
        if (!MSResponse.isSuccessCode(msResponse)) {
            throw new RuntimeException("修改好评单失败:" + msResponse.getMsg());
        }
    }

    /**
     * 获取好评费用标准
     */
    private MDCustomerPraiseFee getCustomerPraiseFee(Long customerId) {
        MDCustomerPraiseFee customerPraiseFee = customerPraiseFeeService.getByCustomerIdFromCacheNewForCP(customerId);
        if (customerPraiseFee == null) {
            throw new RuntimeException("读取好评费配置失败，请稍候重试");
        }
        return customerPraiseFee;
    }

    /**
     * 根据订单Id获取工单好评信息
     */
    private Praise getPraise(Long orderId, String quarter, Long servicePointId) {
        Praise praise = null;
        MSResponse<Praise> msResponse = orderPraiseFeign.getByOrderIdAndServicepointId(quarter, orderId, servicePointId);
        if (MSResponse.isSuccess(msResponse)) {
            praise = msResponse.getData();
        }

        return praise;
    }

    /**
     * 计算客户好评费、网点好评费
     */
    private NameValuePair<Double, Double> calculatePraiseFee(Long customerId, List<String> picCodes) {
        NameValuePair<Double, Double> result = new NameValuePair<>(0.0, 0.0);
        MDCustomerPraiseFee fee = getCustomerPraiseFee(customerId);
        if (fee.getPraiseFeeFlag() == 1) {
            Map<String, MDCustomerPraiseFeePraiseStandardItem> map = fee.getPraiseStandardItems().stream()
                    .collect(Collectors.toMap(MDCustomerPraiseFeePraiseStandardItem::getCode, a -> a, (k1, k2) -> k1));
            List<NameValuePair<String, Double>> nameValuePraiseList = Lists.newArrayList();
            MDCustomerPraiseFeePraiseStandardItem standardItem;
            NameValuePair<String, Double> nameValuePraise;
            for (String standardCode : picCodes) {
                standardItem = map.get(standardCode);
                if (standardItem != null) {
                    nameValuePraise = new NameValuePair<>(standardCode, standardItem.getFee());
                    nameValuePraiseList.add(nameValuePraise);
                }
            }
            result = PraiseUtils.calculatePraiseCost(fee.getPraiseFee(), fee.getMaxPraiseFee(), fee.getDiscount(), nameValuePraiseList);
        }
        return result;
    }

    /**
     * 检查客户好评费、网点好评费是否计算正确
     */
    private void checkPraiseFee(NameValuePair<Double, Double> calculatedPraiseFee, Double customerApplyPraiseFee, Double servicePointApplyPraiseFee) {
        if (!(Math.abs(calculatedPraiseFee.getName() - customerApplyPraiseFee) < PRAISE_FEE_DIFF
                && Math.abs(calculatedPraiseFee.getValue() - servicePointApplyPraiseFee) < PRAISE_FEE_DIFF)) {
            throw new RuntimeException("好评费用计算失败，请稍后重试");
        }
    }
}
