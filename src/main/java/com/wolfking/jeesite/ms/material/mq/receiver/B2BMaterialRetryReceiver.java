package com.wolfking.jeesite.ms.material.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.mq.message.MQB2BMaterialFormMessage;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.sys.SysLog;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.material.mq.sender.B2BMateiralMQSender;
import com.wolfking.jeesite.ms.joyoung.sd.service.JoyoungOrderService;
import com.wolfking.jeesite.ms.material.service.B2BMaterialExecutor;
import com.wolfking.jeesite.ms.material.service.B2BMaterialExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * B2B微服务通知消息消费端
 * 包括：审核，发货通知
 */
@Slf4j
@Component
public class B2BMaterialRetryReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private B2BMateiralMQSender sender;

    @Autowired
    private OrderMaterialService materialService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private JoyoungOrderService joyoungOrderService;

    @Autowired
    private B2BMaterialExecutorFactory b2bMaterialExecutorFactory;

    @Autowired
    private AreaService areaService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //处理完成再回ack
        //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQB2BMaterialFormMessage.B2BMaterialFormMessage materialFormMessage = null;
        User user = new User(0L);
        String lockKey = new String("");
        Boolean locked = false;
        StringBuilder json = new StringBuilder(500);
        int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
        try{
            materialFormMessage = MQB2BMaterialFormMessage.B2BMaterialFormMessage.parseFrom(message.getBody());
            if(materialFormMessage == null || materialFormMessage.getId() <=0){
                log.error("B2B配件通知处理失败:消息体解析错误");
                LogUtils.saveLog("B2B配件通知处理失败-消息体解析错误:" +  materialFormMessage.getKklMasterId(), "B2BMaterialRetryReceiver", json.toString(),null,user);
                return;
            }
            json.append(new JsonFormat().printToString(materialFormMessage));
            MaterialMaster materialMaster = materialService.getMaterialMasterById(materialFormMessage.getKklMasterId(),materialFormMessage.getQuarter());
            if(materialMaster == null){
                log.error("B2B配件通知处理失败:无此配件单，message:{} ",json.toString());
                LogUtils.saveLog("B2B配件通知处理失败:无此配件单:" +  materialFormMessage.getKklMasterId(), "B2BMaterialRetryReceiver", json.toString(),null,user);
                return;
            }
            boolean isSuccess = true;
            int status = materialMaster.getStatus().getIntValue();
            switch (materialFormMessage.getNotifyType()){
                case Audit://审核
                    if(status != 1){
                        isSuccess = false;
                        log.error("B2B配件通知处理失败:配件单已审核，message:{}, material:{}",json.toString(),materialMaster.toString());
                        //LogUtils.saveLog("九阳配件通知处理失败:配件单已审核", "JoyoungMaterialRetryReceiver", json.toString(),null,user);
                        return;
                    }
                    //update
                    updateAuditInfo(materialMaster,materialFormMessage);
                    break;
                case Delivery://发货
                    if(status != 2){
                        isSuccess = false;
                        log.error("B2B配件通知处理失败:配件单已发货，message:{}, material:{}",json.toString(),materialMaster.toString());
                        //LogUtils.saveLog("九阳配件通知处理失败:配件单已发货", "JoyoungMaterialRetryReceiver", json.toString(),null,user);
                        return;
                    }
                    //update
                    updateDeliveryInfo(materialMaster,materialFormMessage);
                    break;
                default:
                    isSuccess = false;
                    log.error("B2B配件通知处理失败:通知类型错误，message:{}",json.toString());
                    //LogUtils.saveLog("九阳配件通知处理失败:通知类型错误", "JoyoungMaterialRetryReceiver", json.toString(),null,user);
                    break;
            }
            if(!isSuccess){
                return;
            }

        }catch (Exception ex){
            log.error("B2B配件通知消费错误,message:{}",json.toString(),ex);
            if(materialFormMessage != null && materialFormMessage.getId() > 0){
                sendRetry(times,materialFormMessage,json,ex);
            }
        }
        finally {
            //ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 更新发货信息
     * @param materialMaster        配件单
     * @param materialFormMessage   消息体
     */
    private void updateDeliveryInfo(MaterialMaster materialMaster,MQB2BMaterialFormMessage.B2BMaterialFormMessage materialFormMessage){
        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(materialFormMessage.getDataSource());
        User user = new User(3L,"B2B","");
        if(dataSourceEnum != null){
            user.setName(dataSourceEnum.name);
        }
        Order order = orderService.getOrderById(materialMaster.getOrderId(), materialMaster.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getOrderCondition() == null) {
            insertMQLog(materialFormMessage,"读取配件单的订单信息失败："+  materialFormMessage.getKklMasterId(),user);
            return;
        }
        materialMaster.setExpressNo(materialFormMessage.getExpressNo());
        materialMaster.setExpressCompany(new Dict(materialFormMessage.getExpressCompany(),materialFormMessage.getExpressCompany()));
        materialMaster.setUpdateBy(user);
        materialMaster.setUpdateDate(new Date());
        materialService.updateMaterialApplyExpress(materialMaster,order);
        //message
        try {
            OrderCondition condition = order.getOrderCondition();
            Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
            if (engineer != null && engineer.getAccountId() > 0) {
                String content = String.format("%s %s 工单申请的配件已发货，请及时打开APP进行查看", condition.getOrderNo(), condition.getUserName());
                PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.MDELIVER, "", content, engineer.getAccountId());
            }
        }
        catch (Exception e){
            LogUtils.saveLog("配件发货APP推送失败:" + materialMaster.getId().toString(),"OrderController.editMaterialMaster",materialMaster.getOrderId().toString(),e,user);
        }
        //notify B2B

        if(dataSourceEnum != null) {
            B2BMaterialExecutor b2BMaterialExecutor = b2bMaterialExecutorFactory.getExecutor(dataSourceEnum);
            if(b2BMaterialExecutor != null){
                try {
                    MSResponse msResponse = b2BMaterialExecutor.notifyDeliverFlag(materialFormMessage.getId());
                    if (!MSResponse.isSuccessCode(msResponse)) {
                        log.error("厂商通知配件已发货，订单处理完成，同步回调B2B微服失败:{}", msResponse.getMsg());
                        //throw new RuntimeException(msResponse.getMsg());
                    }
                }catch (Exception e){
                    log.error("厂商通知配件已发货，订单处理完成，同步回调B2B微服异常",e);
                }
            }
        }
    }

    /**
     * 更新审核结果
     */
    private void updateAuditInfo(MaterialMaster materialMaster,MQB2BMaterialFormMessage.B2BMaterialFormMessage materialFormMessage){
        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(materialFormMessage.getDataSource());
        User user = new User(3L,"B2B","");
        if(dataSourceEnum != null){
            user.setName(dataSourceEnum.name);
        }
        Order order = orderService.getOrderById(materialMaster.getOrderId(), materialMaster.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            insertMQLog(materialFormMessage,"读取配件单的订单信息失败",user);
            return;
        }
        OrderCondition condition = order.getOrderCondition();

        //reject
        if(materialFormMessage.getAuditStatus() == 0){
            materialService.b2bRejectMaterialApply(materialMaster.getId(),materialMaster.getOrderId(),materialMaster.getQuarter(),user,StringUtils.isBlank(materialFormMessage.getRemark())?"":materialFormMessage.getRemark(),order);
            if(dataSourceEnum != null) {
                B2BMaterialExecutor b2BMaterialExecutor = b2bMaterialExecutorFactory.getExecutor(dataSourceEnum);
                if(b2BMaterialExecutor != null){
                    try {
                        MSResponse msResponse = b2BMaterialExecutor.notifyApplyFlag(materialFormMessage.getId());
                        if (!MSResponse.isSuccessCode(msResponse)) {
                            log.error("厂商驳回配件申请，订单处理完成，回调B2B微服失败:{}", msResponse.getMsg());
                            //throw new RuntimeException(msResponse.getMsg());
                        }
                    }catch (Exception e){
                        log.error("厂商驳回配件申请，订单处理完成，回调B2B微服异常",e);
                    }
                }
            }
            //push message
            if(condition.getServicePoint() != null && condition.getEngineer() != null) {
                Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
                if (engineer != null && engineer.getAccountId() > 0) {
                    String content = String.format("%s %s 工单的配件申请被厂家或客服驳回，请及时打开APP进行查看", condition.getOrderNo(), condition.getUserName());
                    PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.MREJECT, "", content, engineer.getAccountId());
                }
            }
            return;
        }
        //approve
        int isMaterialReturn = 0;
        if(dataSourceEnum!=null && dataSourceEnum==B2BDataSourceEnum.XYINGYAN){ //如果是新迎燕,从微服务获取是否返件
            isMaterialReturn = materialFormMessage.getReturnFlag();
        }else{
            isMaterialReturn = materialMaster.getReturnFlag();
        }
        String returnNo = org.apache.commons.lang3.StringUtils.EMPTY;
        if(isMaterialReturn == 1){
            returnNo = SeqUtils.NextSequenceNo("ReturnMaterialFormNo",0,3);
            if(StringUtils.isBlank(returnNo)){
                insertMQLog(materialFormMessage,"产生返件单号错误",null);
                return;
            }
        }
        String[] itemIds = materialMaster.getItems().stream()
                .filter(t->t.getReturnFlag()==1)
                .map(t->t.getId().toString())
                .toArray(String[]::new);
        if(dataSourceEnum!=null && dataSourceEnum==B2BDataSourceEnum.XYINGYAN && isMaterialReturn==1){
            MDCustomerAddress customerAddress = new MDCustomerAddress();
            String[] areaParseResult = AreaUtils.decodeAddressGaode(materialFormMessage.getReceiverAddress().replace(" ", ""));
            if(areaParseResult != null && areaParseResult.length > 0){
                customerAddress.setAreaId(Long.valueOf(areaParseResult[0]));
                Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(customerAddress.getAreaId());
                Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
                Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
                customerAddress.setProvinceId(province.getId());
                customerAddress.setCityId(city.getId());
            }
            customerAddress.setUserName(materialFormMessage.getReceiver());
            customerAddress.setContactInfo(materialFormMessage.getReceiverPhone());
            customerAddress.setAddress(materialFormMessage.getReceiverAddress());

            itemIds = materialMaster.getItems().stream().map(t->t.getId().toString()).toArray(String[]::new);
            materialService.approveMaterialApply(materialMaster,order,isMaterialReturn,returnNo,itemIds,user,customerAddress,0,null,"");
        }else{
            materialService.approveMaterialApply(materialMaster,order,isMaterialReturn,returnNo,itemIds,user,null,0,null,"");
        }
        if(dataSourceEnum != null) {
            B2BMaterialExecutor b2BMaterialExecutor = b2bMaterialExecutorFactory.getExecutor(dataSourceEnum);
            if(b2BMaterialExecutor != null){
                try {
                    MSResponse msResponse = b2BMaterialExecutor.notifyApplyFlag(materialFormMessage.getId());
                    if (!MSResponse.isSuccessCode(msResponse)) {
                        log.error("厂商审核通过配件申请，订单处理完成，回调B2B微服失败:{}", msResponse.getMsg());
                        //throw new RuntimeException(msResponse.getMsg());
                    }
                }catch (Exception e){
                    log.error("厂商审核通过配件申请，订单处理完成，回调B2B微服异常",e);
                }
            }
        }
        //message
        if(condition.getServicePoint() != null && condition.getServicePoint().getId() != null && condition.getEngineer() != null) {
            Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
            if (isMaterialReturn == 1 && engineer != null && engineer.getAccountId() > 0) {
                String content = String.format("%s %s 工单的配件申请已通过，请注意订单配件需返厂，请及时打开APP进行查看", condition.getOrderNo(), condition.getUserName());
                PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.MPASS, "", content, engineer.getAccountId());
            }
        }
    }


    /**
     * 发送延迟队列，重试
     */
    private void sendRetry(int times,MQB2BMaterialFormMessage.B2BMaterialFormMessage materialFormMessage,StringBuilder json,Exception ex){
        if (times < getMaxAttempts()) {
            times++;
            sender.sendDelay(materialFormMessage, getDelaySeconds(times), times);
        }else {
            log.error("B2B配件通知消费失败：达到重试上限,message:{}", json.toString(), ex);
            insertMQLog(materialFormMessage,"达到重试上限",null);
        }
    }

    /**
     * 消息消费失败记录
     * 供定时任务处理
     */
    private void insertMQLog(MQB2BMaterialFormMessage.B2BMaterialFormMessage message,String msg,User user){

        if(message == null){
            return;
        }
        StringBuilder json = new StringBuilder();
        json.append(new JsonFormat().printToString(message));
        LogUtils.saveLog(StringUtils.left("B2B配件通知处理:"+msg,250), "B2BMaterialFormMessage", json.toString(), null, user, SysLog.TYPE_EXCEPTION);
    }

    private int getMaxAttempts() {
        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }
}
