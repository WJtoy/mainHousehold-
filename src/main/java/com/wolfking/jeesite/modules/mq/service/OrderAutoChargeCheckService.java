package com.wolfking.jeesite.modules.mq.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.mq.entity.OrderCharge;
import com.wolfking.jeesite.modules.mq.sender.OrderChargeSender;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 订单自动对账检查
 * 检查通过发送自动对账队列
 * @author Ryan Lu
 * @version 1.0.0
 * @date 2020-03-30 14:01
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderAutoChargeCheckService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderChargeSender orderChargeSender;

    @Autowired
    private OrderChargeService orderChargeService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    /**
     * 检查订单是否可以自动对账
     * @param orderId
     * @param quarter
     */
    private String checkOrderCanAutoCharge(Order order,long orderId,String quarter){
        if(order == null) {
            if (orderId <= 0 || StringUtils.isBlank(quarter)) {
                return "参数错误";
            }
        }
        if(order == null) {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        }
        if (order == null || order.getOrderCondition() == null || order.getOrderFee() == null) {
            return "读取订单错误";
        }
        OrderCondition condition = order.getOrderCondition();
        //1.检查状态
        int statusValue = condition.getStatusValue();
        if(statusValue != Order.ORDER_STATUS_COMPLETED.intValue()){
            return "订单当前状态不是：已客评，不能对账";
        }
        //2.订单项目
        if(order.getItems().size() == 0){
            return "订单无订单项";
        }
        if(order.getDetailList().size() == 0){
            return "订单无上门服务项目";
        }
        //3.保内安装服务
        String azCode = new String("II");
        Map<Long,String> map = serviceTypeService.findIdsAndCodes();
        Long serviceId = map.entrySet().stream().filter(t->t.getValue().equalsIgnoreCase(azCode)).map(t->t.getKey()).findFirst().orElse(null);
        if(serviceId == null || serviceId<=0){
            return "系统无保内安装服务项目";
        }
        final Long azServiceTypeId = serviceId;
        //非安装单，排除，如果订单中一安装，一维修，不能自动结账
        long nonazcnt = order.getDetailList().stream().filter(t -> !Objects.equals(t.getServiceType().getId(), azServiceTypeId)).count();
        //非安装单 nonazcnt >0
        if (nonazcnt > 0) {
            return "订单有非保内安装项目";
        }

        //4.应收金额一致, 才可自动生成对帐单（
        OrderFee orderFee = order.getOrderFee();
        if (!Objects.equals(orderFee.getExpectCharge(), orderFee.getOrderCharge())) {
            return "下单应收金额与实际金额不一致";
        }
        //5.项次数量一致,也有可能出现安装单->维修单
        if(order.getDetailList().size() != order.getItems().size()){
            return "订单项目数量与实际服务项目数量不一致";
        }
        /*判断应收时效费，应付时效费，网点保险费，加急费
        if(orderFee.getCustomerTimeLinessCharge() != 0 || orderFee.getTimeLinessCharge() != 0 || orderFee.getInsuranceCharge() != 0){
            return false;
        }*/
        //6.实际上门明细费用判断，有配件费、其它、远程费、快递费的不能自动对账
        OrderDetail detail;
        List<OrderDetail> details = order.getDetailList();
        for (int i = 0, size = order.getDetailList().size(); i < size; i++) {
            detail = details.get(i);
            if (detail.getMaterialCharge() > 0 || detail.getOtherCharge() > 0
                    || detail.getTravelCharge() > 0 || detail.getExpressCharge() > 0) {
                return "实际服务有配件费/其他费用/远程费/快递费";
            }
            if (detail.getEngineerMaterialCharge() > 0 || detail.getEngineerOtherCharge() > 0 ||
                    detail.getEngineerTravelCharge() > 0 || detail.getEngineerExpressCharge() > 0) {

                return "实际服务有配件费/其他费用/远程费/快递费";
            }
        }
        //7.下单项目与实际项目比对
        //下单明细
        StringBuffer itemString = new StringBuffer(200);
        order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getProductId))
                .forEach(t -> {
                    itemString.append("S#").append(t.getServiceType().getId()).append("#S")
                            .append("P#").append(t.getProduct().getId()).append("#P")
                            .append("Q#").append(t.getQty()).append("#Q");
                });
        //实际上门明细,只读取安装项目（5已过滤上门多项或少项情况）
        StringBuffer detailString = new StringBuffer(200);
        order.getDetailList().stream()
                .filter(t -> Objects.equals(t.getServiceType().getId(), azServiceTypeId))
                .sorted(Comparator.comparing(OrderDetail::getProductId))
                .forEach(t -> {
                    detailString.append("S#").append(t.getServiceType().getId()).append("#S")
                            .append("P#").append(t.getProduct().getId()).append("#P")
                            .append("Q#").append(t.getQty()).append("#Q");
                });
        if(!itemString.toString().equalsIgnoreCase(detailString.toString())){
            return  "订单项目与实际服务项目不一致";
        }
        return "success";
   }

    /**
     * 自动对账处理
     * 1.检查是否可以自动对账
     * 2.可自动对账，发送自动对账队列
     *
     * @return MSResponse {
     *  code : 0-检查通过 1-检查不通过
     * }
     */
    @Transactional
    public MSResponse autoCharge(Order order,long orderId,String quarter,User user,long triggerAt) {
        // 1.工单完工状态变更 ->1
        orderStatusFlagService.UpdateOrderCompleteStatus(orderId,quarter,1);
        //2.自动对账要求检查
        String checkResult = checkOrderCanAutoCharge(order,orderId,quarter);
        if("success".equalsIgnoreCase(checkResult)){
            MQOrderCharge.OrderCharge orderCharge = MQOrderCharge.OrderCharge.newBuilder()
                    .setOrderId(orderId)
                    .setTriggerBy(user.getId())
                    .setTriggerDate(triggerAt)
                    .build();
            try {
                //订单标记为：自动对账中
                int updateRow = orderService.signAutoChargeing(orderId,quarter);
                if(updateRow == 0){
                    log.error("更改自动客评标记失败，当前工单自动对账状态非0。orderId:{}",orderId);
                    //saveOrderChargeFailLog(orderId,quarter,user,triggerAt,"当前工单已对账或对账中",null);
                    return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE,"当前工单已对账或对账中"),null);
                }
                orderChargeSender.send(orderCharge);
                return new MSResponse<>(MSErrorCode.SUCCESS,null);
            } catch (Exception e) {
                log.error("发送对账单消息失败,orderId:{}", orderId, e);
                saveOrderChargeFailLog(orderId,quarter,user,triggerAt,null,e);
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE,"数据处理失败"),null);
            }
        }else{
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE,checkResult),null);
        }
    }

    private void saveOrderChargeFailLog(long orderId, String quarter, User user, long triggerAt,String errorMsg,Exception e){
        OrderCharge chargeEntity = new OrderCharge();
        chargeEntity.setOrderId(orderId);
        chargeEntity.setQuarter(quarter);//*
        chargeEntity.setId(SeqUtils.NextID());
        chargeEntity.setTriggerBy(user.getId());
        chargeEntity.setTriggerDate(new DateTime(triggerAt).toDate());
        chargeEntity.setDescription("");
        chargeEntity.setCreateBy(user);
        chargeEntity.setCreateDate(new Date());
        chargeEntity.setStatus(40);
        chargeEntity.setRetryTimes(1);
        if(e != null) {
            chargeEntity.setDescription(StringUtils.left(e.getMessage(),250));
        }else{
            chargeEntity.setDescription(StringUtils.left(errorMsg,250));
        }
        try {
            orderChargeService.insert(chargeEntity);
        } catch (Exception e1) {
            log.error("保存自动对账检查记录错误，orderId:{}", orderId, e1);
        }
    }

}
