package com.wolfking.jeesite.ms.xyingyan.sd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.xyingyan.sd.PutB2BOrderErrorCode;
import com.kkl.kklplus.entity.xyingyan.sd.PutB2BOrderResult;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderMQService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderInsufficientBalanceException;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BProductNotSupportedException;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderAutoBaseService;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Slf4j
//@Service
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class XYingYanOrderService extends B2BOrderManualBaseService {
//
//    private static final User USER_B2B = new User(3L, "b2b");
//
//    @Autowired
//    OrderMQService orderMQService;

    /**
     * 下单
     */
//    public PutB2BOrderResult createXYingYanOrder(B2BOrder b2BOrder) {
//        PutB2BOrderResult result = new PutB2BOrderResult();
//        result.setB2bOrderNo(b2BOrder.getOrderNo());
//        PutB2BOrderErrorCode errorCode = PutB2BOrderErrorCode.SUCCESS;
//        try {
//            List<B2BOrderVModel> orderVModels = toB2BOrderVModels(Lists.newArrayList(b2BOrder));
//            if (!orderVModels.isEmpty()) {
//                Order order = toOrderAuto(orderVModels.get(0), USER_B2B);
//                String repeatedOrderNo = orderService.getRepeateOrderNo(order.getOrderCondition().getCustomer().getId(), order.getOrderCondition().getPhone1());
//                if (StringUtils.isNotBlank(repeatedOrderNo)) {
//                    order.setRepeateNo(repeatedOrderNo);
//                }
//                orderService.createOrder_v2_1(order, null);
//                if (order.getCreateBy() == null || order.getCreateBy().getId() == null || order.getCreateBy().getId() <= 0) {
//                    order.setCreateBy(USER_B2B);
//                }
//                orderMQService.sendCreateOrderMessage(order, "XYingYanOrderService.createXYingYanOrder");
//                result.setKklOrderNo(order.getOrderNo());
//                result.setKklOrderId(order.getId());
//            } else {
//                errorCode = PutB2BOrderErrorCode.OTHER_ERROR;
//            }
//        } catch (B2BProductNotSupportedException e1) {
//            errorCode = PutB2BOrderErrorCode.PRODUCT_NOT_SUPPORTED;
//            LogUtils.saveLog("新迎燕自动下单失败：产品不支持", "XYingYanOrderService.createXYingYanOrder", e1.getLocalizedMessage(), null, null);
////            log.error("新迎燕自动下单失败：产品不支持 - {}", e1.getLocalizedMessage());
//        } catch (B2BOrderInsufficientBalanceException e2) {
//            errorCode = PutB2BOrderErrorCode.BALANCE_INSUFFICIENT;
//            LogUtils.saveLog("新迎燕自动下单失败：余额不足", "XYingYanOrderService.createXYingYanOrder", e2.getLocalizedMessage(), null, null);
////            log.error("新迎燕自动下单失败：余额不足 - {}", e2.getLocalizedMessage());
//        } catch (Exception e3) {
//            errorCode = PutB2BOrderErrorCode.OTHER_ERROR;
//            LogUtils.saveLog("新迎燕自动下单失败:其他错误", "XYingYanOrderService.createXYingYanOrder", e3.getLocalizedMessage(), null, null);
////            log.error("新迎燕自动下单失败:其他错误 - {}", e3.getLocalizedMessage());
//        }
//        result.setErrorCode(errorCode.code);
//
//        return result;
//    }

}
