package com.wolfking.jeesite.ms.praise.service;

import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.kkl.kklplus.entity.md.ReceivablePayableItemEnum;
import com.kkl.kklplus.entity.praise.CustomerPraisePaymentTypeEnum;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.praise.dto.MQPraiseMessage;
import com.kkl.kklplus.entity.sys.SysUser;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.service.OrderAutoChargeCheckService;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 好评单服务层
 *
 * @author Ryan Lu
 * @date 2020/3/31 11:40 AM
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PraiseFormService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderFeeService orderFeeService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private OrderReceivableService receivableService;

    @Autowired
    private OrderPayableService payableService;

    @Autowired
    private OrderServicepointReceivableService servicepointReceivableService;

    @Autowired
    private OrderAutoChargeCheckService orderAutoChargeCheckService;

    @Autowired
    private OrderServicePointFeeService orderServicePointFeeService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    /**
     * 审核审核处理（包含自动和手动）
     * 1.审核通过
     *   1.更新好评单状态
     *   2.更新状态及费用(orderFee),并汇总到应收和应付合计
     *   3.写入应收应付表
     *   4.记录订单日志
     *   5.自动对账检查及处理
     * 2.取消(canceled)
     *  1.更新好评单状态
     *  2.记录订单日志
     *  3.自动对账检查及处理
     * 3.待审核
     *  1.更新好评单状态
     *  2.记录订单日志
     * @param message   好评单审核结果
     */
    @Transactional(readOnly = false)
    public void reviewResultHandle(MQPraiseMessage.PraiseActionMessage message,int orderStatus) {
        int status = message.getStatus();
        OrderProcessLog processLog = null;
        try {
            OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(message.getOrderId(), message.getQuarter(), true);
            if(orderStatusFlag == null){
                throw new RuntimeException("读取并确认订单好评状态错误");
            }
            if(orderStatusFlag.getPraiseStatus() == status){
                PraiseStatusEnum praiseStatusEnum = PraiseStatusEnum.fromCode(status);
                log.error("当前好评单状态与消息体中一致。订单: {} 好评单状态:{}",message.getOrderId(),praiseStatusEnum.msg);
                return;
            }
            StringBuffer comment = new StringBuffer(100);
            if (status == PraiseStatusEnum.APPROVE.code) {
                comment.append("【审核通过】好评单");
            } else if (status == PraiseStatusEnum.CANCELED.code) {
                comment.append("【取消】好评单");
            } else if (status == PraiseStatusEnum.PENDING_REVIEW.code) {
                comment.append("【待审核】好评单");
            }else{
                comment.append("【驳回】好评单");
            }
            DateTime dateTime = new DateTime(message.getTriggerAt());
            User user = new User(message.getTrigger().getId(), message.getTrigger().getName(), "");
            processLog = new OrderProcessLog();
            processLog.setQuarter(message.getQuarter());
            processLog.setAction("好评单审核");
            processLog.setOrderId(message.getOrderId());
            comment.append(" ").append(message.getTrigger().getName());//.append(" ").append(dateTime.toString("yyyy-MM-dd HH:mm"));
            if(StringUtils.isNotBlank(message.getRemark())){
                comment.append(" ").append(message.getRemark().trim());
            }
            processLog.setActionComment(StringUtils.left(comment.toString(),250));
            processLog.setStatus("已客评");
            processLog.setStatusValue(Order.ORDER_STATUS_COMPLETED.intValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(dateTime.toDate());
            int visibilityValue = VisibilityFlagEnum.or(Sets.newHashSet(VisibilityFlagEnum.KEFU, VisibilityFlagEnum.CUSTOMER,VisibilityFlagEnum.SERVICE_POINT));
            processLog.setVisibilityFlag(visibilityValue);
            processLog.setRemarks(processLog.getActionComment());//厂家可见
            processLog.setDataSourceId(message.getDataSourceId());//b2b发送日志用
            processLog.setCustomerId(message.getCustomerId());//b2b发送日志用
            int updateRow = orderStatusFlagService.updatePraiseStatus(message.getOrderId(), message.getQuarter(), status);
            //用于阻止重复消费
            if(updateRow == 0){
                log.error("更新好评单状态无效,orderId:{} status:{}",message.getOrderId(),status);
                return;
            }
            orderService.saveOrderProcessLogNew(processLog);
            //同步网点订单表
            servicePointOrderBusinessService.syncPraiseStatus(message.getOrderId(),message.getQuarter(),message.getServicePointId(),status,message.getTrigger().getId(),message.getTriggerAt());
            //驳回,待审核 不做其他处理
            if(status == PraiseStatusEnum.REJECT.code || status == PraiseStatusEnum.PENDING_REVIEW.code){
                return;
            }
            if(orderStatus != Order.ORDER_STATUS_COMPLETED.intValue()){
                log.error("订单状态:{} 。非[已客评]状态，不变更好评费用。orderId:{}",orderStatus, message.getOrderId());
                return;
            }
            //有费用，更新好评费（包含应收和应付）
            if (status == PraiseStatusEnum.APPROVE.code && (message.getReceivable() > 0 || message.getPayable() > 0)) {
                Double praiseFee = message.getReceivable() > 0? message.getReceivable() : null;
                //客户好评结算方式：线下，不写入工单费用相关表
                if(message.getCustomerPaymentType() == CustomerPraisePaymentTypeEnum.OUTLINE.code){
                    praiseFee = null;
                }
                Double engineerPriaseFee = message.getPayable()>0?message.getPayable():null;
                SysUser sysUser = new SysUser();
                sysUser.setId(message.getTrigger().getId());
                sysUser.setName(message.getTrigger().getName());
                //应收，费用大于0写入
                if (praiseFee != null) {
                    OrderReceivable receivable = OrderReceivable.builder()
                            .orderId(message.getOrderId())
                            .quarter(message.getQuarter())
                            .itemNo(ReceivablePayableItemEnum.Praise.code)
                            .formNo(message.getFormNo())
                            .amount(message.getReceivable())
                            .remark("")
                            .createBy(sysUser)
                            .createAt(message.getTriggerAt())
                            .build();
                    receivableService.insert(receivable);
                }
                //应付
                if (message.getPayable() > 0) {
                    //订单应付记录
                    OrderPayable payable = OrderPayable.builder()
                            .orderId(message.getOrderId())
                            .quarter(message.getQuarter())
                            .itemNo(ReceivablePayableItemEnum.Praise.code)
                            .formNo(message.getFormNo())
                            .amount(message.getPayable())
                            .remark("")
                            .createBy(sysUser)
                            .createAt(message.getTriggerAt())
                            .build();
                    payableService.insert(payable);
                    //网点应收应付记录
                    OrderServicepointReceivable servicepointReceivable = OrderServicepointReceivable.builder()
                            .orderId(message.getOrderId())
                            .quarter(message.getQuarter())
                            .servicePoint(new MDServicePoint(Long.valueOf(message.getServicePointId())))
                            .itemNo(ReceivablePayableItemEnum.Praise.code)
                            .formNo(message.getFormNo())
                            .amount(message.getPayable())
                            .remark("")
                            .createBy(sysUser)
                            .createAt(message.getTriggerAt())
                            .build();
                    servicepointReceivableService.insert(servicepointReceivable);
                }
                //订单费用表
                if(praiseFee != null || engineerPriaseFee != null){
                    //订单费用表
                    orderFeeService.updatePraiseFee(message.getOrderId(),message.getQuarter(),praiseFee,engineerPriaseFee);
                    //网点订单费用表
                    orderServicePointFeeService.updatePraiseFee(message.getOrderId(),message.getQuarter(),message.getServicePointId(),engineerPriaseFee);
                }
            }else if(status == PraiseStatusEnum.CANCELED.code && orderStatusFlag.getPraiseStatus() == PraiseStatusEnum.APPROVE.code  && (message.getReceivable() > 0 || message.getPayable() > 0)){
                //异常处理功能取消好评单,且好评单有费用产生
                //应付
                if(message.getPayable()>0){
                    orderServicePointFeeService.updatePraiseFee(message.getOrderId(),message.getQuarter(),message.getServicePointId(),0.00);
                    servicepointReceivableService.switchEnabled(message.getOrderId(),message.getQuarter(),message.getServicePointId(),ReceivablePayableItemEnum.Praise.code,1,user.getId(),message.getTriggerAt());
                    payableService.switchEnabled(message.getOrderId(),message.getQuarter(),String.valueOf(ReceivablePayableItemEnum.Praise.code),1,user.getId(),message.getTriggerAt());
                }
                //应收
                if(message.getReceivable()>0){
                    receivableService.switchEnabled(message.getOrderId(),message.getQuarter(),String.valueOf(ReceivablePayableItemEnum.Praise.code),1,user.getId(),message.getTriggerAt());
                }
                //订单费用汇总
                orderFeeService.updatePraiseFee(message.getOrderId(),message.getQuarter(),0.00,0.00);
            }

            //审核通过，或取消
            // 自动对账处理
            MSResponse msResponse = orderAutoChargeCheckService.autoCharge(null,message.getOrderId(),message.getQuarter(),user,message.getTriggerAt());
            if(!MSResponse.isSuccessCode(msResponse)){
                log.error("自动对账检查不通过，orderId:{} ,result:{}",message.getOrderId(),msResponse.getMsg());
            }
        }catch (Exception e){
            log.error("好评单审核消息处理失败,json:{}",new JsonFormat().printToString(message),e);
            throw new RuntimeException("好评单审核消息处理失败",e);
        }
    }

    /**
     * 判断用户的类型
     * @param user  帐号信息 10 - 客服 20 - 客户 30 - 业务 40 - 网点
     */
    public int getCreatorType(User user){
        if (user == null || user.getId() == null || user.getId()<=0){
            return 10;
        } else if(user.isCustomer()){
            return 20;
        } else if(user.isSaleman()){
            return 30;
        } else if(user.isEngineer()){
            return 40;
        }else {
            return 10;
        }
    }
}
