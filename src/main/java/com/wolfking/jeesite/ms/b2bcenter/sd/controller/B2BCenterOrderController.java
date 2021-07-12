package com.wolfking.jeesite.ms.b2bcenter.sd.controller;


import cn.hutool.core.util.StrUtil;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.validator.BeanValidators;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.ThreeTuple;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sd.service.OrderEditFormService;
import com.wolfking.jeesite.modules.sd.service.OrderMQService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderExistsException;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BButtonLabelUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderConvertVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderSearchVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.b2bcenter.sd.utils.B2BOrderUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * B2B订单Controller
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/b2b/b2bcenter/order/")
public class B2BCenterOrderController extends BaseController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private B2BOrderManualBaseService b2BOrderManualBaseService;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private OrderMQService orderMQService;

    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    @Autowired
    private OrderEditFormService orderEditFormService;

    @Autowired
    private CustomerService customerService;

    /**
     * 临时表缓存Key
     */
    private static final String JD_B2BORDER_TEMPDB_KEY = "B2B:%d:%S";

    private String getTempCacheKey(B2BDataSourceEnum dataSourceEnum, Session session) {
        if (dataSourceEnum == null || session == null) {
            return "";
        }
        return String.format(JD_B2BORDER_TEMPDB_KEY, dataSourceEnum.id, session.getId().toString());
    }

    private static final String VIEW_NAME_B2BORDER_ALL_LIST = "modules/b2bcenter/sd/b2bOrderAllList";
    private static final String VIEW_NAME_B2BORDER_NO_ROUTING_LIST = "modules/b2bcenter/sd/b2bOrderNoRoutingList";
    private static final String VIEW_NAME_MANUAL_FROM = "modules/b2bcenter/sd/manualForm";
    private static final String VIEW_NAME_CANCEL_FROM = "modules/b2bcenter/sd/cancelForm";

    //---------------------------------------------------------------------------------------------------------------列表

    //region 订单转换

    /**
     * 待转换的订单列表
     */
    @RequiresPermissions("b2b:order:transfer")
    @RequestMapping(value = "/b2bOrderAllList")
    public String getB2BOrderAllList(B2BOrderSearchVModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BOrderVModel> page = new Page<>();
        Date now = new Date();
        if (order.getBeginCreateDate() == null) {
            order.setBeginCreateDate(DateUtils.addMonth(now, -1));
        }
        if (order.getEndCreateDate() == null) {
            order.setEndCreateDate(now);
        }
        order.setBeginCreateDate(DateUtils.getStartOfDay(order.getBeginCreateDate()));
        order.setEndCreateDate(DateUtils.getEndOfDay(order.getEndCreateDate()));
        order.setBeginCreateDt(order.getBeginCreateDate().getTime());
        order.setEndCreateDt(order.getEndCreateDate().getTime());

        List<Dict> processFlags = Lists.newArrayListWithCapacity(3);
        processFlags.add(new Dict("0", "受理，还未转换"));
//        processFlags.add(new Dict("2", "拒绝，转换但业务数据不满足要求"));
        processFlags.add(new Dict("2", "快可立工单"));
        processFlags.add(new Dict("3", "失败，转换但发生错误"));

        List<Dict> abnormalOrderFlags = Lists.newArrayListWithCapacity(3);
        abnormalOrderFlags.add(new Dict("0", "全部"));
        abnormalOrderFlags.add(new Dict("1", "包安装"));
        abnormalOrderFlags.add(new Dict("2", "不包安装"));

        List<Dict> dataSourceList = B2BMDUtils.getEnabledDataSourceDicts();

        model.addAttribute("page", page);
        model.addAttribute("order", order);
        model.addAttribute("processFlags", processFlags);
        model.addAttribute("abnormalOrderFlags", abnormalOrderFlags);
        model.addAttribute("dataSourceList", dataSourceList);
        model.addAttribute("customers", Lists.newArrayList());
        model.addAttribute("canSearch", false);
        model.addAttribute("canDirectlyCancel", false);
        model.addAttribute("canIgnore", false);

        if (dataSourceList.isEmpty()) {
            addMessage(model, "错误：没有可用的B2B微服务！");
            return VIEW_NAME_B2BORDER_ALL_LIST;
        }

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(order.getDataSource());
        if (dataSourceEnum == null) {
            dataSourceEnum = B2BDataSourceEnum.valueOf(dataSourceList.get(0).getIntValue());
        }
        Dict currentDataSource = null;
        if (dataSourceEnum != null) {
            final Integer dataSourceId = dataSourceEnum.id;
            currentDataSource = dataSourceList.stream().filter(i -> i.getIntValue().equals(dataSourceId)).findFirst().orElse(null);
        }

        if (currentDataSource == null) {
            addMessage(model, "错误：数据源参数错误！");
            return VIEW_NAME_B2BORDER_ALL_LIST;
        }
        order.setDataSource(currentDataSource.getIntValue());
        model.addAttribute("currenctDataSource", currentDataSource);
        model.addAttribute("customers", B2BMDUtils.getCustomers(currentDataSource.getIntValue()));

        model.addAttribute("MANUAL_TRANSFER", B2BButtonLabelUtils.getButtonLabel(dataSourceEnum.id, B2BButtonLabelUtils.ButtonEnum.MANUAL_TRANSFER.buttonId));
        model.addAttribute("IGNORE_AND_HIDE", B2BButtonLabelUtils.getButtonLabel(dataSourceEnum.id, B2BButtonLabelUtils.ButtonEnum.IGNORE_AND_HIDE.buttonId));
        model.addAttribute("APPOINT_AND_CANCEL", B2BButtonLabelUtils.getButtonLabel(dataSourceEnum.id, B2BButtonLabelUtils.ButtonEnum.APPOINT_AND_CANCEL.buttonId));
        model.addAttribute("DIRECTLY_CANCEL", B2BButtonLabelUtils.getButtonLabel(dataSourceEnum.id, B2BButtonLabelUtils.ButtonEnum.DIRECTLY_CANCEL.buttonId));

        Session session = UserUtils.getSession();
        if (session == null) {
            addMessage(model, "错误：登录超时！");
            return VIEW_NAME_B2BORDER_ALL_LIST;
        }

        try {
            page = b2BOrderManualBaseService.findPageOfToTransfer(new Page(request, response), order, dataSourceEnum);
            model.addAttribute("page", page);
            model.addAttribute("canSearch", true);
            model.addAttribute("canDirectlyCancel", B2BMDUtils.canDirectlyCancel(dataSourceEnum.id));
            model.addAttribute("canIgnore", B2BMDUtils.canIgnoreOrder(dataSourceEnum.id));
            redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), page.getList(), 30 * 60);
        } catch (Exception e) {
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session));
            addMessage(model, "错误：" + e.getMessage());
        }

        return VIEW_NAME_B2BORDER_ALL_LIST;
    }

    //-----------------------------------------------------------------------------------------------------------手工转单

    /**
     * 人工处理
     */
    @RequiresPermissions("b2b:order:transfer")
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    public String manualForm(int dataSource, Long b2bOrderId, String b2bOrderNo, String quarter, Model model) {

        B2BOrderConvertVModel order = new B2BOrderConvertVModel();
        model.addAttribute("order", order);
        model.addAttribute("canCreateOrder", false);

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if (dataSourceEnum == null || StringUtils.isBlank(b2bOrderNo)) {
            addMessage(model, "错误：参数不全，请重新查询后重试！");
            return VIEW_NAME_MANUAL_FROM;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || user.getId() == null || session == null) {
            addMessage(model, "错误：登录超时！");
            return VIEW_NAME_MANUAL_FROM;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.size() == 0) {
            addMessage(model, "错误：长时间未操作，请重新查询后再转换！");
            return VIEW_NAME_MANUAL_FROM;
        }
        B2BOrderVModel orderVModel;
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            orderVModel = list.stream().filter(t -> t.getDataSource() == dataSource && t.getB2bOrderId().equals(b2bOrderId)).findFirst().orElse(null);
        } else {
            orderVModel = list.stream().filter(t -> t.getDataSource() == dataSource && t.getOrderNo().equalsIgnoreCase(b2bOrderNo)).findFirst().orElse(null);
        }
        if (orderVModel == null) {
            addMessage(model, "错误：订单缓存已过期，请重新查询后再转换！");
            return VIEW_NAME_MANUAL_FROM;
        }

        MSResponse msResponse = b2BCenterOrderService.checkB2BOrderProcessFlag(dataSourceEnum, orderVModel.getId(), b2bOrderNo);
        if (!MSResponse.isSuccessCode(msResponse)) {
            addMessage(model, "错误：" + msResponse.getMsg());
            return VIEW_NAME_MANUAL_FROM;
        }
        try {
            MSResponse<Order> responseEntity = b2BOrderManualBaseService.transferOrderManual(orderVModel, user);
            if (responseEntity.getCode() == MSErrorCode.SUCCESS.getCode()) {
                Order o = responseEntity.getData();
                //缓存转单前订单产品，用于提交时读取图片 2020-08-07
                String cache_key = MessageFormat.format("B2B:TRANS:OITEM:{0}",o.getParentBizOrderId());
                redisUtils.set(RedisConstant.RedisDBType.REDIS_TEMP_DB, cache_key, o.getItems(),60*30);
                order = mapperFacade.map(o, B2BOrderConvertVModel.class);
//                order.setB2bOrderId(orderVModel.getId());
                order.setB2bQuarter(orderVModel.getQuarter());
                CustomerFinance fi = order.getCustomer().getFinance();
                order.setCustomerBalance(fi.getBalance());
                if (fi.getCreditFlag() == 1) {
                    order.setCustomerCredit(fi.getCredit());
                }

                //models、brand、b2bProductCodes 2018/1/7
                List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).collect(Collectors.toList());
                Map<Long, ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>>> productPropertiesMap = orderEditFormService.getProductPropertyEntris(order.getCustomer().getId(), StringUtils.toInteger(order.getDataSource().getValue()), productIds);
                if (!productPropertiesMap.isEmpty()) {
                    ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>> productProperties;
                    for (OrderItemModel item : order.getItems()) {
                        productProperties = productPropertiesMap.get(item.getProductId());
                        if (productProperties != null) {
                            item.setBrands(productProperties.getAElement());
                            item.setModels(productProperties.getBElement());
                            item.setB2bProductCodes(productProperties.getCElement());
                            if (StrUtil.isNotBlank(item.getBrand())) {
                                item.setBrandId(B2BOrderUtils.getBrandId(item.getBrand(), productProperties.getAElement()));
                            }
                        }
                        if (StrUtil.isNotBlank(item.getBrand())) {
                            item.setHasBrand(true);
                        }
                    }
                }
                order.setExpresses(MSDictUtils.getDictList("express_type"));
                model.addAttribute("order", order);
                model.addAttribute("canCreateOrder", true);
                if (StringUtils.isNotBlank(responseEntity.getMsg())) {
                    addMessage(model, "错误：" + responseEntity.getMsg());
                }
                return VIEW_NAME_MANUAL_FROM;
            } else {
                long orderId = responseEntity.getData() == null ? 0 : responseEntity.getData().getId() == null ? 0 : responseEntity.getData().getId();
                String orderNo = responseEntity.getData() == null ? "" : StringUtils.toString(responseEntity.getData().getOrderNo());
                String errorMsg = responseEntity.getMsg() == null ? "" : responseEntity.getMsg();
                if (responseEntity.getCode() == B2BOrderVModel.ERROR_CODE_B2BORDER_IS_CONVERTED.getCode()) {
                    b2BCenterOrderService.updateB2BOrderConversionProgressNew(dataSourceEnum, b2bOrderNo, B2BProcessFlag.PROCESS_FLAG_SUCESS, orderId, orderNo, quarter, errorMsg, user, orderVModel.getB2bOrderId());
                } else {
                    b2BCenterOrderService.updateB2BOrderConversionProgressNew(dataSourceEnum, b2bOrderNo, B2BProcessFlag.PROCESS_FLAG_FAILURE, 0L, "", quarter, errorMsg, user, orderVModel.getB2bOrderId());
                }
                addMessage(model, "错误：" + responseEntity.getMsg());
                return VIEW_NAME_MANUAL_FROM;
            }

        } catch (Exception e) {
            log.error("[B2BCenterOrderController.manualForm] b2bOrderNo:{} ,dataSource:{}", orderVModel.getOrderNo(), orderVModel.getDataSource(), e);
        }
        return VIEW_NAME_MANUAL_FROM;
    }

    /**
     * 保存人工处理订单
     */

    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/manual", method = RequestMethod.POST)
    public AjaxJsonEntity manualSave(B2BOrderConvertVModel order) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            jsonEntity.setMessage("您的账号登录超时，请重新登录。");
            return jsonEntity;
        }
        try {
            beanValidator(order);
        } catch (ConstraintViolationException ex) {
            List<String> list = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
            jsonEntity.setMessage(StringUtils.join(list, System.getProperty("line.separator")));
            return jsonEntity;
        }
        //读取缓存转单前订单产品，用于提交时读取图片 2020-08-07
        String cache_key = MessageFormat.format("B2B:TRANS:OITEM:{0}",order.getParentBizOrderId());
        List<OrderItem> oitems = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, cache_key,OrderItem[].class);
        setDefaultProductPics(order,oitems);

        Order createdOrder = null;
        try {
            Customer customer = customerService.getFromCache(order.getCustomer().getId());
            if (customer == null) {
                jsonEntity.setMessage("确认客户类型失败，请重试！");
                return jsonEntity;
            }
            order.setCustomer(customer);
            createdOrder = b2BOrderManualBaseService.saveOrderToKKLForManual(order, user);
            jsonEntity.setSuccess(true);
            jsonEntity.setMessage("保存成功!");
            b2BCenterOrderService.updateB2BOrderConversionProgressNew(B2BDataSourceEnum.get(order.getDataSource().getIntValue()), order.getB2bOrderNo(), B2BProcessFlag.PROCESS_FLAG_SUCESS,
                    createdOrder.getId(), createdOrder.getOrderNo(), order.getB2bQuarter(), "转单成功(人工转单)", user, order.getB2bOrderId());
            if (createdOrder.getCreateBy() == null || createdOrder.getCreateBy().getId() == null || createdOrder.getCreateBy().getId() <= 0) {
                createdOrder.setCreateBy(user);
            }
            orderMQService.sendCreateOrderMessage(createdOrder, "B2BCenterOrderController.manual");
        } catch (B2BOrderExistsException e1) {
            String errorMsg = "已转入工单系统,快可立工单号：" + e1.getB2bOrderNo();
            b2BCenterOrderService.updateB2BOrderConversionProgressNew(B2BDataSourceEnum.get(order.getDataSource().getIntValue()), e1.getB2bOrderNo(), B2BProcessFlag.PROCESS_FLAG_SUCESS,
                    e1.getOrderId(), "", order.getB2bQuarter(), errorMsg, user, order.getB2bOrderId());
            jsonEntity.setMessage(errorMsg);
        } catch (Exception e2) {
            jsonEntity.setMessage(e2.getLocalizedMessage());
        }

        return jsonEntity;
    }

    private void setDefaultProductPics(B2BOrderConvertVModel order,List<OrderItem> b2bItems){
        if(order == null || CollectionUtils.isEmpty(order.getItems()) || CollectionUtils.isEmpty(b2bItems)){
            return;
        }
        List<OrderItemModel> items = order.getItems();
        Supplier<Stream<OrderItem>> streamSupplier = () -> b2bItems.stream();
        OrderItem bit;
        for(OrderItemModel item : items){
            bit = streamSupplier.get().filter(t->t.getProductId().equals(item.getProductId())).findAny().orElse(null);
            if(bit != null) {
                item.setPics(bit.getPics());
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------批量转单

    /**
     * 批量转换(ajax)
     */
    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/transfer")
    public AjaxJsonEntity transfer(@RequestBody List<B2BOrderTransferResult> orders, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);

        B2BDataSourceEnum dataSourceEnum = null;
        if (orders != null && !orders.isEmpty() && orders.get(0) != null) {
            dataSourceEnum = B2BDataSourceEnum.valueOf(orders.get(0).getDataSource());
        }
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("请重新选择要转换的订单");
            return jsonEntity;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || user.getId() == null || session == null) {
            jsonEntity.setMessage("登录超时,请重新登录");
            return jsonEntity;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.isEmpty()) {
            jsonEntity.setMessage("长时间未操作，请重新查询后再转换。");
            return jsonEntity;
        }

        /* 批量操作只操作同一数据源的工单 */
        int dataSourceId = dataSourceEnum.id;
        list = list.stream().filter(t -> t.getDataSource() == dataSourceId).collect(Collectors.toList());
        if (list.isEmpty()) {
            jsonEntity.setMessage("本地缓存数据无要转换的订单，请重新查询后再转换。");
            return jsonEntity;
        }
        List<B2BOrderVModel> orderVModelList = Lists.newArrayList();
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            Set<Long> b2bOrderIds = orders.stream().filter(i->i.getDataSource() == dataSourceId).map(B2BOrderTransferResult::getB2bOrderId).collect(Collectors.toSet());
            for (B2BOrderVModel item : list) {
                if (b2bOrderIds.contains(item.getB2bOrderId())) {
                    orderVModelList.add(item);
                }
            }
            if (orderVModelList.size() != b2bOrderIds.size()) {
                jsonEntity.setMessage("本地缓存数据数量与选择不一致，请重新查询后再转换。");
                return jsonEntity;
            }
        } else {
            Set<String> b2bOrderNos = orders.stream().filter(i -> i.getDataSource() == dataSourceId).map(B2BOrderTransferResult::getB2bOrderNo).collect(Collectors.toSet());
            for (B2BOrderVModel item : list) {
                if (b2bOrderNos.contains(item.getOrderNo())) {
                    orderVModelList.add(item);
                }
            }
            if (orderVModelList.size() != b2bOrderNos.size()) {
                jsonEntity.setMessage("本地缓存数据数量与选择不一致，请重新查询后再转换。");
                return jsonEntity;
            }
        }
        if (orderVModelList.isEmpty()) {
            jsonEntity.setMessage("本地缓存数据无要转换的订单，请重新查询后再转换。");
            return jsonEntity;
        }

        MSResponse checkResponse = b2BCenterOrderService.checkB2BOrderProcessFlag(orders, dataSourceEnum);
        if (!MSResponse.isSuccessCode(checkResponse)) {
            jsonEntity.setMessage(checkResponse.getMsg());
            return jsonEntity;
        }
        Boolean locked;
        List<B2BOrderTransferResult> progressList = Lists.newArrayList();
        List<B2BOrderTransferResult> conversionProgressList = Lists.newArrayList();
        try {
            Order createdOrder = null;
            B2BOrderTransferResult conversionProgress;
            Date date = new Date();
            String lockKey;
            for (B2BOrderVModel item : orderVModelList) {
                lockKey = String.format(RedisConstant.B2B_WORKCARD_TRANSFER_KEY, item.getDataSource(), item.getOrderNo());
                locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//1分钟
                conversionProgress = new B2BOrderTransferResult();
                conversionProgress.setId(item.getId());
                conversionProgress.setB2bOrderId(item.getB2bOrderId());
                conversionProgress.setDataSource(item.getDataSource());
                conversionProgress.setB2bOrderNo(item.getOrderNo());
                conversionProgress.setB2bQuarter(item.getQuarter());
                conversionProgress.setUpdater(user.getName());
                conversionProgress.setUpdateDt(date.getTime());
                progressList.add(conversionProgress);
                if (!locked) {
                    conversionProgress.setProcessComment("已在处理中...,请稍后重试");
                    conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    continue;
                }

                try {
                    createdOrder = b2BOrderManualBaseService.saveOrderToKKLForBatch(item, user);
                    conversionProgress.setOrderId(createdOrder.getId());
                    conversionProgress.setKklOrderNo(createdOrder.getOrderNo());
                    conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    conversionProgress.setProcessComment("转单成功(批量转单)");
                    if (createdOrder.getCreateBy() == null) {
                        createdOrder.setCreateBy(user);
                    }
                    orderMQService.sendCreateOrderMessage(createdOrder, "CanboOrderController.transfer");
                } catch (B2BOrderExistsException e1) {
                    conversionProgress.setOrderId(e1.getOrderId());
                    conversionProgress.setKklOrderNo(e1.getOrderNo());
                    conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    conversionProgress.setProcessComment(StringUtils.left(e1.getMessage(), 200));
                } catch (Exception e2) {
                    conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    conversionProgress.setProcessComment(StringUtils.left(e2.getMessage(), 200));
                }
                if (createdOrder == null) {
                    try {
                        redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
                    } catch (Exception e) {
                    }
                }
                conversionProgressList.add(conversionProgress);

                MSResponse progressResponse = b2BCenterOrderService.updateB2BOrderConversionProgress(dataSourceEnum, Lists.newArrayList(conversionProgress), user);
                if (!MSResponse.isSuccessCode(progressResponse)) {
                    conversionProgress.setProcessComment("更新B2B工单的转换进度失败");
                }
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(progressList);
        } catch (Exception e) {
            jsonEntity.setMessage("订单转换错误:" + StringUtils.left(e.getMessage(), 200));
            jsonEntity.setData(progressList);
        }
        return jsonEntity;
    }

    //-----------------------------------------------------------------------------------------------------------取消工单

    /**
     * 取消转单
     */
    @RequiresPermissions("b2b:order:transfer")
    @RequestMapping(value = "/cancelOrderTransitionForm", method = RequestMethod.GET)
    public String cancelOrderTransitionForm(Integer dataSource, Long b2bOrderId, String b2bOrderNo, String quarter, String comment, Model model) {
        B2BOrderTransferResult order = new B2BOrderTransferResult();
        model.addAttribute("order", order);
        model.addAttribute("canAction", false);

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if (dataSourceEnum == null || StringUtils.isBlank(b2bOrderNo)) {
            addMessage(model, "错误：参数不全，请重试！");
            return VIEW_NAME_CANCEL_FROM;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || session == null) {
            addMessage(model, "错误：登录超时！");
            return VIEW_NAME_CANCEL_FROM;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.isEmpty()) {
            addMessage(model, "错误：长时间未操作，请重新查询后再转换！");
            return VIEW_NAME_CANCEL_FROM;
        }
        B2BOrderVModel orderVModel;
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            orderVModel = list.stream().filter(t -> t.getDataSource() == dataSourceEnum.id && t.getB2bOrderId().equals(b2bOrderId)).findFirst().orElse(null);
        } else {
            orderVModel = list.stream().filter(t -> t.getDataSource() == dataSourceEnum.id && t.getOrderNo().equalsIgnoreCase(b2bOrderNo)).findFirst().orElse(null);
        }
        if (orderVModel == null) {
            addMessage(model, "错误：订单缓存已过期，请重新查询后再转换！");
            return VIEW_NAME_CANCEL_FROM;
        }
        order.setDataSource(dataSource);
        order.setB2bOrderId(b2bOrderId);
        order.setB2bOrderNo(b2bOrderNo);
        order.setB2bQuarter(quarter);
        order.setProcessComment(comment);
        model.addAttribute("order", order);
        model.addAttribute("canAction", true);
        return VIEW_NAME_CANCEL_FROM;
    }

    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/cancelOrderTransition", method = RequestMethod.POST)
    public AjaxJsonEntity cancelOrderTransition(B2BOrderTransferResult order, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);

        B2BDataSourceEnum dataSourceEnum = order != null ? B2BDataSourceEnum.valueOf(order.getDataSource()) : null;
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || user.getId() == null || session == null) {
            jsonEntity.setMessage("登录超时,请重新登录");
            return jsonEntity;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.isEmpty()) {
            jsonEntity.setMessage("长时间未操作，请重新查询后再取消。");
            return jsonEntity;
        }
        B2BOrderVModel orderVModel;
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            orderVModel = list.stream().filter(i -> i.getDataSource() == dataSourceEnum.id && i.getB2bOrderId().equals(order.getB2bOrderId())).findFirst().orElse(null);
        } else {
            orderVModel = list.stream().filter(i -> i.getDataSource() == dataSourceEnum.id && i.getOrderNo().equalsIgnoreCase(order.getB2bOrderNo())).findFirst().orElse(null);
        }
        if (orderVModel == null) {
            jsonEntity.setMessage("本地缓存数据无要取消的订单，请重新查询后再取消。");
            return jsonEntity;
        }

        order.setId(orderVModel.getId());
        order.setUpdater(String.valueOf(user.getId()));
        order.setUpdaterName(StringUtils.toString(user.getName()));
        order.setUpdateDt(System.currentTimeMillis());
        order.setProcessFlag(5);
        MSResponse msResponse = b2BCenterOrderService.cancelOrderTransition(order, dataSourceEnum);
        if (MSResponse.isSuccessCode(msResponse)) {
            jsonEntity.setSuccess(true);
        } else {
            jsonEntity.setMessage(StringUtils.toString(msResponse.getMsg()));
        }
        return jsonEntity;
    }


    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/directlyCancelOrderTransition")
    public AjaxJsonEntity directlyCancelOrderTransition(@RequestParam("dataSource") Integer dataSource, @RequestParam("b2bOrderId") Long b2bOrderId,
                                                        @RequestParam("b2bOrderNo") String b2bOrderNo, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);

        if (dataSource == null || StringUtils.isBlank(b2bOrderNo)) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || user.getId() == null || session == null) {
            jsonEntity.setMessage("登录超时,请重新登录");
            return jsonEntity;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.isEmpty()) {
            jsonEntity.setMessage("长时间未操作，请重新查询后再取消。");
            return jsonEntity;
        }

        B2BOrderVModel orderVModel;
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            orderVModel = list.stream().filter(i -> i.getDataSource() == dataSourceEnum.id && i.getB2bOrderId().equals(b2bOrderId)).findFirst().orElse(null);
        } else {
            orderVModel = list.stream().filter(i -> i.getDataSource() == dataSourceEnum.id && i.getOrderNo().equalsIgnoreCase(b2bOrderNo)).findFirst().orElse(null);
        }
        if (orderVModel == null) {
            jsonEntity.setMessage("本地缓存数据无要取消的订单，请重新查询后再取消。");
            return jsonEntity;
        }

        B2BOrderTransferResult order = new B2BOrderTransferResult();
        order.setId(orderVModel.getId());
        order.setB2bQuarter(orderVModel.getQuarter());
        order.setB2bOrderNo(b2bOrderNo);
        order.setUpdater(String.valueOf(user.getId()));
        order.setUpdateDt(System.currentTimeMillis());
        order.setProcessFlag(5);
        MSResponse msResponse = b2BCenterOrderService.directlyCancelOrderTransition(order, dataSourceEnum);
        if (MSResponse.isSuccessCode(msResponse)) {
            jsonEntity.setSuccess(true);
        } else {
            jsonEntity.setMessage(StringUtils.toString(msResponse.getMsg()));
        }
        return jsonEntity;
    }

    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/ignoreOrderTransition")
    public AjaxJsonEntity ignoreOrderTransition(@RequestParam("dataSource") Integer dataSource,
                                                @RequestParam("b2bOrderId") Long b2bOrderId,
                                                @RequestParam("b2bOrderNo") String b2bOrderNo, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);

        if (dataSource == null || StringUtils.isBlank(b2bOrderNo)) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || user.getId() == null || session == null) {
            jsonEntity.setMessage("登录超时,请重新登录");
            return jsonEntity;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.isEmpty()) {
            jsonEntity.setMessage("长时间未操作，请重新查询后再忽略。");
            return jsonEntity;
        }

        B2BOrderVModel orderVModel;
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            orderVModel = list.stream().filter(i -> i.getDataSource() == dataSourceEnum.id && i.getB2bOrderId().equals(b2bOrderId)).findFirst().orElse(null);
        } else {
            orderVModel = list.stream().filter(i -> i.getDataSource() == dataSourceEnum.id && i.getOrderNo().equalsIgnoreCase(b2bOrderNo)).findFirst().orElse(null);
        }
        if (orderVModel == null) {
            jsonEntity.setMessage("本地缓存数据无要忽略的订单，请重新查询后再忽略。");
            return jsonEntity;
        }

        B2BOrderTransferResult order = new B2BOrderTransferResult();
        order.setId(orderVModel.getId());
        order.setB2bQuarter(orderVModel.getQuarter());
        order.setB2bOrderNo(b2bOrderNo);
        order.setUpdater(String.valueOf(user.getId()));
        order.setUpdateDt(System.currentTimeMillis());
        order.setProcessFlag(5);
        order.setProcessComment("忽略隐藏");
        MSResponse msResponse = b2BCenterOrderService.ignoreOrderTransition(order, dataSourceEnum);
        if (MSResponse.isSuccessCode(msResponse)) {
            jsonEntity.setSuccess(true);
        } else {
            jsonEntity.setMessage(StringUtils.toString(msResponse.getMsg()));
        }
        return jsonEntity;
    }

    //-----------------------------------------------------------------------------------------------------------批量取消

    /**
     * 批量取消(ajax)
     */
    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/cancelOrderTransitionBatch")
    public AjaxJsonEntity cancelOrderTransitionBatch(@RequestBody List<B2BOrderTransferResult> orders, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);

        B2BDataSourceEnum dataSourceEnum = null;
        if (orders != null && !orders.isEmpty() && orders.get(0) != null) {
            dataSourceEnum = B2BDataSourceEnum.valueOf(orders.get(0).getDataSource());
        }
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("请重新选择要取消的订单");
            return jsonEntity;
        }

        User user = UserUtils.getUser();
        Session session = UserUtils.getSession();
        if (user == null || user.getId() == null || session == null) {
            jsonEntity.setMessage("登录超时,请重新登录");
            return jsonEntity;
        }

        List<B2BOrderVModel> list = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), B2BOrderVModel[].class);
        if (list == null || list.isEmpty()) {
            jsonEntity.setMessage("长时间未操作，请重新查询后再取消。");
            return jsonEntity;
        }

        /* 批量操作只操作同一数据源的工单 */
        int dataSourceId = dataSourceEnum.id;
        list = list.stream().filter(t -> t.getDataSource() == dataSourceId).collect(Collectors.toList());
        if (list.isEmpty()) {
            jsonEntity.setMessage("本地缓存数据无要取消的订单，请重新查询后再取消。");
            return jsonEntity;
        }
        List<B2BOrderVModel> orderVModelList = Lists.newArrayList();
        if (dataSourceEnum == B2BDataSourceEnum.VIOMI) {
            Set<Long> b2bOrderIds = orders.stream().filter(i -> i.getDataSource() == dataSourceId).map(B2BOrderTransferResult::getB2bOrderId).collect(Collectors.toSet());
            for (B2BOrderVModel item : list) {
                if (b2bOrderIds.contains(item.getB2bOrderId())) {
                    orderVModelList.add(item);
                }
            }
            if (orderVModelList.size() != b2bOrderIds.size()) {
                jsonEntity.setMessage("本地缓存数据无要取消的订单，请重新查询后再取消。");
                return jsonEntity;
            }
        } else {
            Set<String> b2bOrderNos = orders.stream().filter(i -> i.getDataSource() == dataSourceId).map(B2BOrderTransferResult::getB2bOrderNo).collect(Collectors.toSet());
            for (B2BOrderVModel item : list) {
                if (b2bOrderNos.contains(item.getOrderNo())) {
                    orderVModelList.add(item);
                }
            }
            if (orderVModelList.size() != b2bOrderNos.size()) {
                jsonEntity.setMessage("本地缓存数据无要取消的订单，请重新查询后再取消。");
                return jsonEntity;
            }
        }
        if (orderVModelList.isEmpty()) {
            jsonEntity.setMessage("本地缓存数据无要取消的订单，请重新查询后再取消。");
            return jsonEntity;
        }

        MSResponse checkResponse = b2BCenterOrderService.checkB2BOrderProcessFlag(orders, dataSourceEnum);
        if (!MSResponse.isSuccessCode(checkResponse)) {
            jsonEntity.setMessage(checkResponse.getMsg());
            return jsonEntity;
        }
        Boolean locked;
        List<B2BOrderTransferResult> progressList = Lists.newArrayList();
        MSResponse cancelResponse = null;
        B2BOrderTransferResult order = null;

        B2BOrderTransferResult conversionProgress;
        Date date = new Date();
        String lockKey;
        for (B2BOrderVModel item : orderVModelList) {
            lockKey = String.format(RedisConstant.B2B_WORKCARD_TRANSFER_KEY, item.getDataSource(), item.getOrderNo());
            locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);//1分钟
            conversionProgress = new B2BOrderTransferResult();
            conversionProgress.setDataSource(item.getDataSource());
            conversionProgress.setB2bOrderNo(item.getOrderNo());
            conversionProgress.setB2bQuarter(item.getQuarter());
            conversionProgress.setUpdater(user.getName());
            conversionProgress.setUpdateDt(date.getTime());
            progressList.add(conversionProgress);
            if (!locked) {
                conversionProgress.setProcessComment("已在处理中...,请稍后重试");
                conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                continue;
            }

            order = new B2BOrderTransferResult();
            order.setId(item.getId());
            order.setB2bOrderNo(item.getOrderNo());
            order.setUpdater(String.valueOf(user.getId()));
            order.setUpdaterName(StringUtils.toString(user.getName()));
            order.setUpdateDt(System.currentTimeMillis());
            order.setProcessFlag(5);
            order.setProcessComment("取消工单(批量)");
            cancelResponse = b2BCenterOrderService.cancelOrderTransition(order, dataSourceEnum);
            if (MSResponse.isSuccessCode(cancelResponse)) {
                conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                conversionProgress.setProcessComment("取消工单成功(批量)");
            } else {
                conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                conversionProgress.setProcessComment(StringUtils.toString(cancelResponse.getMsg()));
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
                } catch (Exception e) {
                }
            }
        }
        jsonEntity.setSuccess(true);
        jsonEntity.setData(progressList);
        return jsonEntity;
    }


    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/updateOrderAbnormalFlagBatch")
    public AjaxJsonEntity updateOrderAbnormalFlagBatch(@RequestParam Integer dataSource, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);
        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            jsonEntity.setMessage("您的账号登录超时，请重新登录。");
            return jsonEntity;
        }
        MSResponse msResponse = b2BCenterOrderService.updateOrderAbnormalFlagBatch(dataSourceEnum);
        if (MSResponse.isSuccessCode(msResponse)) {
            jsonEntity.setSuccess(true);
        } else {
            jsonEntity.setMessage(StringUtils.toString(msResponse.getMsg()));
        }
        return jsonEntity;
    }


    /**
     * 待转换的订单列表
     */
    @RequiresPermissions("b2b:order:transfer")
    @RequestMapping(value = "/b2bOrderNoRoutingList")
    public String getB2bOrderNoRoutingList(B2BOrderSearchVModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BOrderVModel> page = new Page<>();
        Date now = new Date();
        if (order.getBeginCreateDate() == null) {
            order.setBeginCreateDate(DateUtils.addMonth(now, -1));
        }
        if (order.getEndCreateDate() == null) {
            order.setEndCreateDate(now);
        }
        order.setBeginCreateDate(DateUtils.getStartOfDay(order.getBeginCreateDate()));
        order.setEndCreateDate(DateUtils.getEndOfDay(order.getEndCreateDate()));
        order.setBeginCreateDt(order.getBeginCreateDate().getTime());
        order.setEndCreateDt(order.getEndCreateDate().getTime());


        List<Dict> dataSourceList = B2BMDUtils.getRoutingEnabledDataSourceDicts();

        model.addAttribute("page", page);
        model.addAttribute("order", order);
        model.addAttribute("dataSourceList", dataSourceList);
        model.addAttribute("canSearch", false);
        model.addAttribute("canIgnore", false);

        if (dataSourceList.isEmpty()) {
            addMessage(model, "错误：没有可用的B2B微服务！");
            return VIEW_NAME_B2BORDER_NO_ROUTING_LIST;
        }

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(order.getDataSource());
        if (dataSourceEnum == null) {
            dataSourceEnum = B2BDataSourceEnum.valueOf(dataSourceList.get(0).getIntValue());
        }
        Dict currentDataSource = null;
        if (dataSourceEnum != null) {
            final Integer dataSourceId = dataSourceEnum.id;
            currentDataSource = dataSourceList.stream().filter(i -> i.getIntValue().equals(dataSourceId)).findFirst().orElse(null);
        }

        if (currentDataSource == null) {
            addMessage(model, "错误：数据源参数错误！");
            return VIEW_NAME_B2BORDER_NO_ROUTING_LIST;
        }
        order.setDataSource(currentDataSource.getIntValue());
        model.addAttribute("currenctDataSource", currentDataSource);
        model.addAttribute("APPOINT_AND_CANCEL", B2BButtonLabelUtils.getButtonLabel(dataSourceEnum.id, B2BButtonLabelUtils.ButtonEnum.APPOINT_AND_CANCEL.buttonId));
        model.addAttribute("IGNORE_AND_HIDE", B2BButtonLabelUtils.getButtonLabel(dataSourceEnum.id, B2BButtonLabelUtils.ButtonEnum.IGNORE_AND_HIDE.buttonId));

        Session session = UserUtils.getSession();
        if (session == null) {
            addMessage(model, "错误：登录超时！");
            return VIEW_NAME_B2BORDER_NO_ROUTING_LIST;
        }

        try {
            page = b2BOrderManualBaseService.findPageOfNoRoutingB2BOrders(new Page(request, response), order, dataSourceEnum);
            redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, getTempCacheKey(dataSourceEnum, session), page.getList(), 30 * 60);
            model.addAttribute("page", page);
            model.addAttribute("canIgnore", B2BMDUtils.canIgnoreOrder(dataSourceEnum.id));
            model.addAttribute("canSearch", true);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }

        return VIEW_NAME_B2BORDER_NO_ROUTING_LIST;
    }

    @RequiresPermissions("b2b:order:transfer")
    @ResponseBody
    @RequestMapping(value = "/updateOrderRoutingFlagBatch")
    public AjaxJsonEntity updateOrderRoutingFlagBatch(@RequestParam Integer dataSource, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);
        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if (dataSourceEnum == null) {
            jsonEntity.setMessage("参数不全，请重试！");
            return jsonEntity;
        }
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            jsonEntity.setMessage("您的账号登录超时，请重新登录。");
            return jsonEntity;
        }
        MSResponse msResponse = b2BCenterOrderService.updateOrderRoutingFlagBatch(dataSourceEnum);
        if (MSResponse.isSuccessCode(msResponse)) {
            jsonEntity.setSuccess(true);
        } else {
            jsonEntity.setMessage(StringUtils.toString(msResponse.getMsg()));
        }
        return jsonEntity;
    }

}

