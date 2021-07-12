package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderEnum;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.entity.cc.ReminderType;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.exception.OrderReminderException;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderReminderCloseMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.utils.B2BOrderUtils;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.joyoung.sd.service.JoyoungOrderService;
import com.wolfking.jeesite.ms.mqi.sd.service.MqiOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;


import static java.util.Optional.ofNullable;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BOrderReminderService {

    private static final NameValuePair<Integer, String> REMINDER_REASON_OTHER = new NameValuePair<Integer, String>(30, "其他");

    @Autowired
    private OrderService orderService;
    @Autowired
    private AreaService areaService;
    @Autowired
    ReminderService reminderService;

    @Autowired
    private JoyoungOrderService joyoungOrderService;

    @Autowired
    private MqiOrderService mqiOrderService;

    @Autowired
    private B2BCenterOrderReminderCloseMQSender sender;

    public MSResponse processReminderB2BOrderMessage(MQB2BOrderReminderMessage.B2BOrderReminderMessage message) {
        MSResponse response = new MSResponse<>(MSErrorCode.SUCCESS);
        if (B2BDataSourceEnum.isB2BDataSource(message.getDataSource()) && B2BOrderUtils.canReminderOrder(message.getDataSource())
                && message.getKklOrderId() > 0) {
            try {
                if (message.getDataSource() == B2BDataSourceEnum.XYINGYAN.id || message.getDataSource() == B2BDataSourceEnum.LB.id || message.getDataSource() == B2BDataSourceEnum.VIOMI.id || message.getDataSource() == B2BDataSourceEnum.JOYOUNG.id
                        || message.getDataSource()==B2BDataSourceEnum.MQI.id) {
                    reminderOrder(message.getDataSource(), message.getKklOrderId(), message.getB2BReminderId(), message.getB2BReminderNo(), message.getContent(), B2BOrderVModel.b2bUser);
                }
            } catch (OrderReminderException e) {
                String logJson = GsonUtils.toGsonString(message);
                LogUtils.saveLog("B2B催单【OrderReminderException】", "B2BOrderReminderService.processReminderB2BOrderMessage", logJson, e, null);
                if (e.getErrorCode() == OrderReminderException.ERROR_CODE_GENERATE_REMINDER_NO_FAILURE) {
                    response.setCode(MSErrorCode.FAILURE.getCode());
                }
            } catch (Exception e2) {
                String logJson = GsonUtils.toGsonString(message);
                LogUtils.saveLog("B2B催单【Exception】", "B2BOrderReminderService.processReminderB2BOrderMessage", logJson, e2, null);
            }
        }
        return response;
    }

    @Transactional()
    public void reminderOrder(Integer dataSourceId, Long orderId, Long b2bReminderId, String b2bReminderNo, String remarks, User user) {
        Order order = orderService.getOrderById(orderId, null, OrderUtils.OrderDataLevel.STATUS, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderReminderException(OrderReminderException.ERROR_CODE_ORDER_NOT_FOUND, "读取工单信息失败");
        }
        OrderCondition orderCondition = order.getOrderCondition();
        int status = orderCondition.getStatusValue();
        //TODO: APP完工[55]
//        if (status == 0 || status > 50) {
        if (status == 0 || status > 55) {
            throw new OrderReminderException(OrderReminderException.ERROR_CODE_ORDER_STATUS_ERROR, "当前订单状态:" + orderCondition.getStatus().getLabel() + ",不能催单");
        }
        Integer reminderFlag = order.getOrderStatus().getReminderStatus();
        if (reminderFlag == null) {
            reminderFlag = 0;
        }
        Date date = new Date();
        //云米催单：多次催单，首先关闭以前的催单，然后创建新的催单
        if (dataSourceId == B2BDataSourceEnum.VIOMI.id) {
            /*if (reminderFlag == ReminderStatus.All.getCode()) {
                newReminder(order, orderCondition, b2bReminderId, b2bReminderNo, remarks, user);
            } else if (reminderFlag > ReminderStatus.All.getCode() && reminderFlag < ReminderStatus.Replied.getCode()) {
                Reminder reminder = reminderService.getLastReminderByOrderId(orderId, order.getQuarter());
                if (reminder != null) {
                    Long servicePointId = ofNullable(order.getOrderCondition().getServicePoint()).map(t -> t.getId()).orElse(0L);
                    reminderService.replyReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), "云米客户发起新的催单操作，自动回复以前的催单", user, servicePointId,reminder.getItemId());
                    reminderService.rejectReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), remarks, user, ReminderStatus.Replied.getCode(), servicePointId, REMINDER_REASON_OTHER);
                }
            }*/
            //支持多个催单
            if (reminderFlag == ReminderStatus.All.getCode()) {
                newReminder(order, orderCondition, b2bReminderId, b2bReminderNo, remarks, user);
            } else {
                newReminderItem(order, orderCondition, b2bReminderId, b2bReminderNo, remarks, user);
            }
        }else if(dataSourceId==B2BDataSourceEnum.JOYOUNG.id || dataSourceId==B2BDataSourceEnum.MQI.id){//九阳
            Long itemId=0L;
            if(reminderFlag == ReminderStatus.All.getCode()){//首次催单
                itemId = newReminder(order, orderCondition, b2bReminderId, b2bReminderNo, remarks, user);
            }else{
                itemId = newReminderItem(order, orderCondition, b2bReminderId, b2bReminderNo, remarks, user);
            }
            updateFlag(b2bReminderId,itemId,dataSourceId);
        } else {
            if (reminderFlag > ReminderStatus.All.getCode() && reminderFlag < ReminderStatus.Completed.getCode()) {
                ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderFlag);
                throw new OrderReminderException(OrderReminderException.ERROR_CODE_DO_NOT_REMINDER, "当前订单已催单，状态为:" + reminderStatus != null ? reminderStatus.getMsg() : "" + ",不能催单");
            }
            newReminder(order, orderCondition, b2bReminderId, b2bReminderNo, remarks, user);
        }
//        String reminderNo = SeqUtils.NextSequenceNo("ReminderNo", 0, 3);
//        if (StringUtils.isBlank(reminderNo)) {
//            throw new OrderReminderException(OrderReminderException.ERROR_CODE_GENERATE_REMINDER_NO_FAILURE, "生成单据编号失败，请重新提交");
//        }
//        long servicePointId = orderCondition.getServicePoint() == null || orderCondition.getServicePoint().getId() == null || orderCondition.getServicePoint().getId() <= 0 ? 0 : orderCondition.getServicePoint().getId();
//        Reminder.ReminderBuilder builder = Reminder.builder()
//                .reminderNo(reminderNo)
//                .b2bReminderId(b2bReminderId != null ? b2bReminderId : 0)
//                .b2bReminderNo(StringUtils.toString(b2bReminderNo))
//                .reminderType(ReminderType.B2B.getCode())
//                .dataSource(order.getDataSourceId())
//                .orderId(order.getId())
//                .quarter(order.getQuarter())
//                .orderCreateAt(orderCondition.getCreateDate().getTime())
//                .customerId(orderCondition.getCustomer().getId())
//                .servicepointId(servicePointId)
//                .productCategoryId(orderCondition.getProductCategoryId() == null ? 0L : orderCondition.getProductCategoryId()) //2019-10-08
//                .userName(orderCondition.getUserName())
//                .userPhone(orderCondition.getServicePhone())
//                .userAddress(orderCondition.getArea().getName() + order.getOrderCondition().getServiceAddress())
//                .orderNo(order.getOrderNo())
//                .cityId(0L)
//                .provinceId(0L)
//                .areaId(orderCondition.getArea().getId())
//                .subAreaId(orderCondition.getSubArea().getId())
//                .status(ReminderStatus.WaitReply.getCode())
//                .reminderRemark(StringUtils.toString(remarks))
//                .canRush(orderCondition.getCanRush());
//        Area area = areaService.getFromCache(orderCondition.getArea().getId());
//        if (area != null) {
//            List<String> ids = Splitter.onPattern(",")
//                    .omitEmptyStrings()
//                    .trimResults()
//                    .splitToList(area.getParentIds());
//            if (ids.size() >= 2) {
//                builder.cityId(Long.valueOf(ids.get(ids.size() - 1)))
//                        .provinceId(Long.valueOf(ids.get(ids.size() - 2)));
//            }
//        }
//        Reminder reminderForm = builder.build();
//        reminderForm.setCreateById(user.getId());
//        reminderForm.setOperatorType(reminderService.getReminderCreatorType(user).getCode());
//        reminderForm.setCreateBy(user.getName());//user name
//        reminderForm.setCreateDt(System.currentTimeMillis());
//        reminderService.newReminder(reminderForm);
        //淘汰订单orderStatus缓存
        OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
        cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(order.getId())
                .setDeleteField(OrderCacheField.ORDER_STATUS);
        OrderCacheUtils.update(cacheBuilder.build());
        PushMessageUtils.pushReminderMessage(orderCondition.getServicePoint(), orderCondition.getEngineer(), order.getOrderNo());
    }

    private Long newReminder(Order order, OrderCondition orderCondition, Long b2bReminderId, String b2bReminderNo, String remarks, User user) {
        String reminderNo = SeqUtils.NextSequenceNo("ReminderNo", 0, 3);
        if (StringUtils.isBlank(reminderNo)) {
            throw new OrderReminderException(OrderReminderException.ERROR_CODE_GENERATE_REMINDER_NO_FAILURE, "生成单据编号失败，请重新提交");
        }
        long servicePointId = orderCondition.getServicePoint() == null || orderCondition.getServicePoint().getId() == null || orderCondition.getServicePoint().getId() <= 0 ? 0 : orderCondition.getServicePoint().getId();
        long kefuId = orderCondition.getKefu() == null || orderCondition.getKefu().getId() == null || orderCondition.getKefu().getId() <= 0 ? 0 : orderCondition.getKefu().getId();
        long engineerId = orderCondition.getEngineer() == null || orderCondition.getEngineer().getId() == null || orderCondition.getEngineer().getId() <= 0 ? 0 : orderCondition.getEngineer().getId();
        String shopId = Optional.ofNullable(order.getB2bShop()).map(t->t.getShopId()).orElse(StrUtil.EMPTY);
        Reminder.ReminderBuilder builder = Reminder.builder()
                .reminderNo(reminderNo)
                .b2bReminderId(b2bReminderId != null ? b2bReminderId : 0)
                .b2bReminderNo(StringUtils.toString(b2bReminderNo))
                .reminderType(ReminderType.B2B.getCode())
                .dataSource(order.getDataSourceId())
                .orderId(order.getId())
                .quarter(order.getQuarter())
                .orderCreateAt(orderCondition.getCreateDate().getTime())
                .customerId(orderCondition.getCustomer().getId())
                .shopId(shopId==null?StrUtil.EMPTY:shopId)
                .servicepointId(servicePointId)
                .productCategoryId(orderCondition.getProductCategoryId() == null ? 0L : orderCondition.getProductCategoryId()) //2019-10-08
                .userName(orderCondition.getUserName())
                .userPhone(orderCondition.getServicePhone())
                .userAddress(orderCondition.getArea().getName() + order.getOrderCondition().getServiceAddress())
                .orderNo(order.getOrderNo())
                .cityId(0L)
                .provinceId(0L)
                .areaId(orderCondition.getArea().getId())
                .subAreaId(orderCondition.getSubArea().getId())
                .kefuId(kefuId)
                .kefuType(orderCondition.getKefuType())
                .engineerId(engineerId)
                .status(ReminderStatus.WaitReply.getCode())
                .reminderRemark(StringUtils.toString(remarks))
                .canRush(orderCondition.getCanRush())
                .reminderReason(REMINDER_REASON_OTHER);
        Area area = areaService.getFromCache(orderCondition.getArea().getId());
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                builder.cityId(Long.valueOf(ids.get(ids.size() - 1)))
                        .provinceId(Long.valueOf(ids.get(ids.size() - 2)));
            }
        }
        Reminder reminderForm = builder.build();
        reminderForm.setCreateById(user.getId());
        reminderForm.setOperatorType(reminderService.getReminderCreatorType(user).getCode());
        reminderForm.setCreateBy(user.getName());//user name
        reminderForm.setCreateDt(System.currentTimeMillis());
        Long reminderItemId = reminderService.newReminder(reminderForm);
        return reminderItemId;
    }

    public Long newReminderItem(Order order, OrderCondition orderCondition,Long b2bReminderId, String b2bReminderNo, String remarks, User user){
        long servicePointId = orderCondition.getServicePoint() == null || orderCondition.getServicePoint().getId() == null || orderCondition.getServicePoint().getId() <= 0 ? 0 : orderCondition.getServicePoint().getId();
        Reminder.ReminderBuilder builder = Reminder.builder()
                .b2bReminderId(b2bReminderId != null ? b2bReminderId : 0)
                .b2bReminderNo(StringUtils.toString(b2bReminderNo))
                .dataSource(order.getDataSourceId())
                .servicepointId(servicePointId)
                .orderId(order.getId())
                .quarter(order.getQuarter())
                .status(ReminderStatus.WaitReply.getCode())
                .reminderRemark(StringUtils.toString(remarks))
                .reminderReason(REMINDER_REASON_OTHER);
        Reminder reminderForm = builder.build();
        reminderForm.setCreateById(user.getId());
        reminderForm.setOperatorType(reminderService.getReminderCreatorType(user).getCode());
        reminderForm.setCreateBy(user.getName());//user name
        reminderForm.setCreateDt(System.currentTimeMillis());
        return reminderService.newReminderItem(reminderForm);
    }


    /**
     * kkl的催单id绑定到b2b催单的原始数据上
     */
    public MSResponse updateFlag(Long b2bConsultingId,Long reminderItemId,Integer dataSource){
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(dataSource!=null && dataSource== B2BDataSourceEnum.JOYOUNG.id){
            msResponse = joyoungOrderService.updateFlag(b2bConsultingId,reminderItemId);
        }
        if(dataSource!=null && dataSource== B2BDataSourceEnum.MQI.id){
            msResponse = mqiOrderService.updateFlag(b2bConsultingId,reminderItemId);
        }
        return msResponse;
    }

    /**
     * 调用消息队列发送催单回复
     */
    public MSResponse sendReminderProcess(MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        int dataSourceId = message.getDataSource();
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && message.getKklReminderId() > 0
                 && StringUtils.isNotBlank(message.getB2BReminderNo())
                && StringUtils.isNotBlank(message.getContent())) {
         if(dataSourceId == B2BDataSourceEnum.JOYOUNG.id || dataSourceId==B2BDataSourceEnum.MQI.id){
             sender.sendRetry(message,1);
            }
        }
        return response;
    }

    /**
     * 调用消息队列接收催单回复
     */
    public MSResponse receiverReminderProcess(MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        if(message.getDataSource() == B2BDataSourceEnum.JOYOUNG.id){
            response = joyoungOrderService.reminderProcess(message);
        }
        if(message.getDataSource() == B2BDataSourceEnum.MQI.id){
            response = mqiOrderService.reminderProcess(message);
        }
        return response;
    }

    /**
     * 第三方系统回调处理催单跟踪信息
     */
    public MSResponse reminderProcessCallback(MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message){
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        try {
            Order order = orderService.getOrderById(message.getKklOrderId(), null, OrderUtils.OrderDataLevel.STATUS, true);
            if (order == null || order.getOrderCondition() == null) {
                return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE,"读取工单信息失败"));
            }
            OrderCondition orderCondition = order.getOrderCondition();
            long servicePointId = orderCondition.getServicePoint() == null || orderCondition.getServicePoint().getId() == null || orderCondition.getServicePoint().getId() <= 0 ? 0 : orderCondition.getServicePoint().getId();
            Reminder reminder = new Reminder();
            reminder.setQuarter(order.getQuarter());
            reminder.setReminderRemark(message.getContent());
            reminder.setItemId(message.getKklReminderId());
            reminder.setServicepointId(servicePointId);
            reminder.setOrderId(order.getId());
            reminder.setCreateDt(message.getCreateDate());
            reminder.setCreateById(B2BOrderVModel.b2bUser.getId());
            reminder.setCreateBy(B2BOrderVModel.b2bUser.getName());
            reminder.setUpdateDt(message.getCreateDate());
            reminder.setUpdateById(B2BOrderVModel.b2bUser.getId());
            reminder.setDataSource(order.getDataSourceId());
            if(order.getDataSourceId()==B2BDataSourceEnum.JOYOUNG.id || order.getDataSourceId()==B2BDataSourceEnum.MQI.id){
                if(message.getOperationType()== B2BOrderEnum.ComplainOperationTypeEnum.LOG.value){
                    reminderService.insertReminderLogByB2B(reminder);
                }else if(message.getOperationType()== B2BOrderEnum.ComplainOperationTypeEnum.EXCEPTION_LOG.value){
                    reminderService.rejectByB2B(reminder);
                }else if(message.getOperationType()== B2BOrderEnum.ComplainOperationTypeEnum.CLOSE.value){
                    reminderService.closeReminderItemByB2B(reminder);
                }
            }
        }catch (Exception e){
            return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE,e.getMessage()));
        }
        return msResponse;
    }

}
