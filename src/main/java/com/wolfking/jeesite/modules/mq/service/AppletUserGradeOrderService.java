package com.wolfking.jeesite.modules.mq.service;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.google.gson.JsonObject;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.applet.mq.MQAppletUserGradeOrderMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerProductCompletePicService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductCompletePicService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.mq.sender.RPTOrderProcessSender;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.Log;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class AppletUserGradeOrderService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMaterialService orderMaterialService;
    @Autowired
    private OrderItemCompleteService orderItemCompleteService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CustomerProductCompletePicService customerPicService;
    @Autowired
    private ProductCompletePicService productCompletePicService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    public void processMessage(MQAppletUserGradeOrderMessage.AppletUserGradeOrderMessage message) {
        try {
            Order order = orderService.getOrderById(message.getOrderId(), message.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true,true,false,true);
            MSResponse response = canGradeOrder(order);
            if (MSResponse.isSuccessCode(response)) {
                OrderGradeModel gradeModel = createGradeModel(order, message.getServiceAttitudeGradeItemId(), message.getTechniqueLevelGradeItemId(), message.getChargeSituationGradeItemId());
                saveGrade(gradeModel, order);
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("orderId", message.getOrderId());
                jsonObject.addProperty("errorMsg", response.getMsg());
                LogUtils.saveLog("小程序客评工单-异常", "AppletUserGradeOrderService#processMessage", jsonObject.toString(), null, null);
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("orderId", message.getOrderId());
            jsonObject.addProperty("errorMsg", ExceptionUtil.getMessage(e));
            LogUtils.saveLog("小程序客评工单-异常", "AppletUserGradeOrderService#processMessage", jsonObject.toString(), null, null);
        }

    }

    private MSResponse canGradeOrder(Order order) {
        MSResponse response = new MSResponse(MSErrorCode.FAILURE);
        if (order == null || order.getOrderCondition() == null) {
            response.setMsg("错误：读取订单失败");
            return response;
        }
        if (!order.canGrade()) {
            response.setMsg("错误：请确认及检查订单状态及是否有异常未处理，或是有已客评。");
            return response;
        }
        OrderCondition condition = order.getOrderCondition();
//        MSResponse msResponse = orderMaterialService.canGradeOfMaterialForm(order.getDataSourceId(), order.getId(), order.getQuarter());
//        if (!MSResponse.isSuccessCode(msResponse)) {
//            return false;
//        }
//        boolean chargeError = orderService.checkOrderFeeAndServiceAmountBeforeGrade(order, true);
//        if (!chargeError) {
//            return false;
//        }
        if (condition.getOrderServiceType() == OrderUtils.OrderTypeEnum.EXCHANGE.getId()
                || condition.getOrderServiceType() == OrderUtils.OrderTypeEnum.BACK.getId()) {
            response.setMsg("错误：退/换货工单，请人工处理。");
            return response;
        }
        // 2021-04-23 云米：增加[完工]的判断
        int dataSource = order.getDataSourceId();
        if (dataSource == B2BDataSourceEnum.VIOMI.getId()) {
            if (StringUtils.isEmpty(condition.getAppCompleteType())) {
                response.setMsg("错误：此订单需先[完工]，然后方可[客评]。");
                return response;
            }
        } else {
            //其他数据源,按品类检查App完工开关
            int appCompleteFlag = Optional.ofNullable(condition.getProductCategory()).map(p->p.getAppCompleteFlag()).orElse(0);
            int status = condition.getStatusValue();
            if(appCompleteFlag == 1 && status != Order.ORDER_STATUS_APP_COMPLETED ){
                response.setMsg("错误：此订单需先[完工]，然后方可[客评]。");
                return response;
            }
        }
        Long customerId = condition.getCustomerId();
        List<OrderItemComplete> itemCompleteList = orderItemCompleteService.findItemCompleteByOrderId(order.getId(), order.getQuarter());
        Map<Long, List<OrderItemComplete>> itemCompleteMap = itemCompleteList.stream().collect(Collectors.groupingBy(t -> t.getProduct().getId()));
        for (OrderDetail item : order.getDetailList()) {
            Product entity = productService.getProductByIdFromCache(item.getProductId());
            if (entity.getSetFlag() == 1) {
                String[] productIds = entity.getProductIds().split(",");
                for (int i = 0; i < productIds.length; i++) {
                    List<OrderItemComplete> itemCompletes = itemCompleteMap.get(Long.valueOf(productIds[i]));
                    if (itemCompletes == null || itemCompletes.size() <= 0) {
                        ProductCompletePic completePic = customerPicService.getFromCache(Long.valueOf(productIds[i]), customerId);
                        if (completePic == null) {
                            completePic = productCompletePicService.getFromCache(Long.valueOf(productIds[i]));
                        }
                        if (completePic != null) {
                            completePic.parseItemsFromJson();
                            if (completePic.getItems() != null && completePic.getItems().size() > 0) {
                                List<ProductCompletePicItem> completePicItemList = completePic.getItems().stream().filter(t -> t.getMustFlag() == 1).collect(Collectors.toList());
                                if (completePicItemList.size() > 0) {
                                    Product product = productService.getProductByIdFromCache(item.getProductId());
                                    response.setMsg("错误：产品[" + product.getName() + "] 未上传完工图片");
                                    return response;
                                }
                            }
                        }
                    }
                }
            } else {
                List<OrderItemComplete> itemCompletes = itemCompleteMap.get(item.getProductId());
                if (itemCompletes == null || itemCompletes.size() <= 0) {
                    ProductCompletePic completePic = customerPicService.getFromCache(item.getProductId(), customerId);
                    if (completePic == null) {
                        completePic = productCompletePicService.getFromCache(item.getProductId());
                    }
                    if (completePic != null) {
                        completePic.parseItemsFromJson();
                        if (completePic.getItems() != null && completePic.getItems().size() > 0) {
                            List<ProductCompletePicItem> completePicItemList = completePic.getItems().stream().filter(t -> t.getMustFlag() == 1).collect(Collectors.toList());
                            if (completePicItemList.size() > 0) {
                                Product product = productService.getProductByIdFromCache(item.getProductId());
                                response.setMsg("错误：产品[" + product.getName() + "] 未上传完工图片");
                                return response;
                            }
                        }
                    }
                }
            }
        }
        Customer customer = customerService.getFromCache(customerId);
        int uploadPicCount = orderItemCompleteService.getUploadCountByOrderId(order.getId(), order.getQuarter());
        int min = customer.getMinUploadNumber();
        int max = customer.getMaxUploadNumber();
        if (min > 0 && uploadPicCount < min) {
            response.setMsg("错误：此订单的客户[ " + customer.getName() + " ]已设置必须上传"
                    + min + "~" + max + "张图片,请在上门服务界面去添加附件图片");
            return response;
        }

        if (condition.getChargeFlag() != null && condition.getChargeFlag() == 1) {
            response.setMsg("错误：此工单已生成对账单");
            return response;
        }
        boolean checkSNResult = orderService.checkOrderProductBarCode(order.getId(), order.getQuarter(), customerId, order.getDetailList());
        if (!checkSNResult) {
            response.setMsg("错误：该厂商要求上传产品序列号，请检查是否已上传！");
            return response;
        }
        response.setCode(MSErrorCode.SUCCESS.getCode());
        return response;
    }

    private OrderGradeModel createGradeModel(Order order, Long serviceAttitudeGradeItemId, Long techniqueLevelGradeItemId, Long chargeSituationGradeItemId) {
        OrderGradeModel gradeModel = orderService.getOrderGrade(order.getOrderCondition());
        if (!gradeModel.getGradeList().isEmpty()) {
            gradeModel.setQuarter(order.getQuarter());
            List<OrderGrade> grades = gradeModel.getGradeList();
            Date timeLinessStartDate = orderService.getServicePointTimeLinessStartDate(order, order.getId(), order.getQuarter(), order.getOrderCondition().getServicePoint().getId());
            if (timeLinessStartDate != null) {
                long productCategoryId = Optional.ofNullable(order.getOrderCondition()).map(t -> t.getProductCategoryId()).orElse(0L);
                Dict timeLinessInfo = null;
                try {
                    timeLinessInfo = orderService.getServicePointTimeLinessInfo(Order.TimeLinessType.ALL, order.getOrderCondition(), timeLinessStartDate, productCategoryId, null);
                } catch (Exception e) {

                }
                OrderGrade grade;
                GradeItem gradeItem;
                for (int i = 0, size = grades.size(); i < size; i++) {
                    grade = grades.get(i);
                    if (CollectionUtil.isNotEmpty(grade.getItems())) {
                        List<GradeItem> sortedGradeItems = grade.getItems().stream().sorted(Comparator.comparing(GradeItem::getPoint).reversed()).collect(Collectors.toList());
                        if (StringUtils.isNotBlank(grade.getDictType()) && TimeLinessPrice.TIME_LINESS_LEVEL.equalsIgnoreCase(grade.getDictType())) {
                            gradeItem = sortedGradeItems.get(0);
                            grade.setGradeItemId(gradeItem.getId());
                            if (timeLinessInfo != null) {
                                grade.setRemarks(grade.getRemarks() + String.format("(<font color='red'>实际用时：%s 小时</font>)", timeLinessInfo.getType()));
                                int level = timeLinessInfo.getIntValue();
                                if (level > 0) {
                                    gradeItem = sortedGradeItems.stream().filter(t -> t.getDictValue().equalsIgnoreCase(String.valueOf(level)))
                                            .findFirst().orElse(null);
                                    if (gradeItem != null) {
                                        grade.setGradeItemId(gradeItem.getId());
                                    }
                                }
                            }
                        } else {
                            gradeItem = sortedGradeItems.stream().filter(t -> t.getId().equals(serviceAttitudeGradeItemId) || t.getId().equals(techniqueLevelGradeItemId) || t.getId().equals(chargeSituationGradeItemId))
                                    .findFirst().orElse(null);
                            if (gradeItem == null) {
                                gradeItem = sortedGradeItems.get(0);
                            }
                            grade.setGradeItemId(gradeItem.getId());
                        }
                    }
                }
            }
        }
        return gradeModel;
    }


    private void saveGrade(OrderGradeModel gradeModel, Order order) {
        gradeModel.setAutoGradeFlag(OrderUtils.OrderGradeType.MANUAL_GRADE.getValue());
        OrderGrade timelinessGradeItem = gradeModel.getGradeList().stream().filter(t -> t.getDictType().equalsIgnoreCase(TimeLinessPrice.TIME_LINESS_LEVEL)).findFirst().orElse(null);
        Integer level = null;
        if (timelinessGradeItem != null) {
            level = StringUtils.toInteger(timelinessGradeItem.getDictValue());
            if (level == 0) {
                level = null;
            }
        }
        gradeModel.setOrder(order);
        OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(order.getId(), order.getQuarter());
        orderService.saveGrade(gradeModel, orderStatusFlag, User.APPLET_USER, null, level);
    }
}
