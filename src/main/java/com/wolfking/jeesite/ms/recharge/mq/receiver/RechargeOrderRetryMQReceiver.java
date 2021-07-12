package com.wolfking.jeesite.ms.recharge.mq.receiver;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.fi.mq.MQRechargeOrderMessage;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.entity.MQRecharge;
import com.wolfking.jeesite.modules.mq.service.MQRechargeService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.recharge.controller.CustomerRechargeController;
import com.wolfking.jeesite.ms.recharge.entity.mapper.RechargeModelMapper;
import com.wolfking.jeesite.ms.recharge.mq.sender.RechargeOrderMQSender;
import com.wolfking.jeesite.ms.recharge.service.RechargeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 充值微服务通知充值成功
 */
@Slf4j
@Component
public class RechargeOrderRetryMQReceiver implements ChannelAwareMessageListener {
    /*
    @Value("${sequence.workerid}")
    private int workerid;

    @Value("${sequence.datacenterid}")
    private intatacenterid;

    private static SequenceIdUtils sequenceIdUtils;

    @PostConstruct
    public void init() {
        RechargeOrderRetryMQReceiver.sequenceIdUtils = new SequenceIdUtils(workerid,datacenterid);
    }
    */
    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private RechargeOrderService rechargeOrderService;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private RechargeOrderMQSender rechargeOrderMQSender;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MQRechargeService mqRechargeService;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //处理完成再回ack
        //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        MQRechargeOrderMessage.RechargeOrderMessage rechargeOrderMessage = null;
        User user = new User(0L);
        String lockKey = new String("");
        Boolean locked = false;
        StringBuilder json = new StringBuilder(500);
        int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
        try{
            rechargeOrderMessage = MQRechargeOrderMessage.RechargeOrderMessage.parseFrom(message.getBody());
            if(rechargeOrderMessage == null || rechargeOrderMessage.getId() <=0){
                log.error("[RechargeOrderRetryMQReceiver]充值通知处理失败:消息体解析错误");
                LogUtils.saveLog((rechargeOrderMessage==null?"":rechargeOrderMessage.getTradeNo()) + ":充值通知处理失败:消息体解析错误", "RechargeOrderRetryMQReceiver", json.toString(),null,user);
                return;
            }
            json.append(new JsonFormat().printToString(rechargeOrderMessage));
            lockKey = String.format(RedisConstant.LOCK_ALIPAY_SYNC,rechargeOrderMessage.getId());
            locked = redisUtils.getLock(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockKey,rechargeOrderMessage.getTradeNo(),60);
            if (!locked) {
                LogUtils.saveLog(rechargeOrderMessage.getTradeNo()+"充值通知处理失败-其他进程正在处理", "RechargeOrderRetryMQReceiver", json.toString(),null,user);
                sendRetry(times,rechargeOrderMessage,json,null);
                return;
            }
            CustomerCurrency customerCurrency = customerCurrencyService.get(rechargeOrderMessage.getId());
            if(customerCurrency!=null){
                LogUtils.saveLog(rechargeOrderMessage.getTradeNo()+":充值通知处理失败-流水单已经存在", "RechargeOrderRetryMQReceiver", json.toString(),null,user);
                //消息重试重复
                if(rechargeOrderMessage.getIsTaskRetry() == 1){
                    mqRechargeService.mqConsumeSuccess(rechargeOrderMessage.getReferId(),rechargeOrderMessage.getId());
                }
                return;
            }
            customerCurrency = Mappers.getMapper(RechargeModelMapper.class).mqToCustomerCurrency(rechargeOrderMessage);
            if(customerCurrency == null){
                LogUtils.saveLog(rechargeOrderMessage.getTradeNo()+":充值通知处理失败-转换错误", "RechargeOrderRetryMQReceiver", json.toString(),null,user);
                sendRetry(times,rechargeOrderMessage,json,null);
                return;
            }
            //get customer finance
            CustomerFinance customerFinance = customerService.getFinance(customerCurrency.getCustomer().getId());
            if(customerFinance == null){
                LogUtils.saveLog(rechargeOrderMessage.getTradeNo()+":充值通知处理失败-读取账户余额错输", "RechargeOrderRetryMQReceiver", json.toString(),null,user);
                sendRetry(times,rechargeOrderMessage,json,null);
                return;
            }
            customerCurrency.setBeforeBalance(customerFinance.getBalance());
            customerCurrency.setBalance(customerFinance.getBalance() + customerCurrency.getAmount());
            rechargeOrderService.rechargeProcess(customerCurrency,rechargeOrderMessage.getIsTaskRetry());
            LogUtils.saveLog(rechargeOrderMessage.getTradeNo()+"充值通知处理成功", "RechargeOrderRetryMQReceiver", json.toString(),null,user);
        }catch (DuplicateKeyException dup) {
            //重复
            try{
                mqRechargeService.mqConsumeSuccess(rechargeOrderMessage.getReferId(), rechargeOrderMessage.getId());
                log.error("充值消费错误:记录重复,body:{}",json.toString());
            }catch (Exception e){
                log.error("充值消费错误：更新mq_recharge成功标记错误,customerId:{} ,rechargeId:{}",rechargeOrderMessage.getReferId(),rechargeOrderMessage.getId(),e);
            }
        }catch (Exception ex){
            log.error("充值消费错误,body:{}",json.toString(),ex);
            if(rechargeOrderMessage != null && rechargeOrderMessage.getId() > 0){
                sendRetry(times,rechargeOrderMessage,json,ex);
            }
        }
        finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 发送延迟队列，重试
     */
    private void sendRetry(int times,MQRechargeOrderMessage.RechargeOrderMessage rechargeOrderMessage,StringBuilder json,Exception ex){
        if (times < getMaxAttempts()) {
            times++;
            rechargeOrderMQSender.sendDelay(rechargeOrderMessage, getDelaySeconds(times), times);
        }else{
            LogUtils.saveLog("充值消费失败：达到重试上限","RechargeOrderRetryMQReceiver",json.toString(),ex,null);
            log.error("充值消费失败：达到重试上限,message:{}",json.toString() ,ex);
            if(rechargeOrderMessage.getIsTaskRetry() == 0) {
                //保存日志记录
                insertMQLog(rechargeOrderMessage, json);
            }else{
                //重试失败
                try {
                    mqRechargeService.mqConsumeFail(rechargeOrderMessage.getReferId(), rechargeOrderMessage.getId(), (ex == null ? "重试失败" : StringUtils.left(ExceptionUtils.getRootCauseMessage(ex), 250)));
                }catch (Exception e){
                    log.error("充值消费错误：更新mq_recharge成功标记错误,customerId:{} ,rechargeId:{}",rechargeOrderMessage.getReferId(),rechargeOrderMessage.getId(),e);
                }
            }
        }
    }

    private void insertMQLog(MQRechargeOrderMessage.RechargeOrderMessage message,StringBuilder json){
        if(message == null){
            return;
        }
        MQRecharge recharge = null;
        if(json == null || json.length()==0){
            json = new StringBuilder(300);
            json.append(new JsonFormat().printToString(message));
        }
        try {
            recharge = MQRecharge.builder()
                    .customerId(message.getReferId())
                    .rechargeId(message.getId())
                    .createAt(System.currentTimeMillis())
                    .triggerAt(message.getCreateAt())
                    .triggerBy(message.getCreateBy())
                    .retryTimes(0)
                    .status(40)
                    .messageContent(json.toString())
                    .build();
            recharge.setId(sequenceIdService.nextId());
            recharge.setQuarter(QuarterUtils.getSeasonQuarter(message.getCreateAt()));
            recharge.setRemarks(StringUtils.left(message.getRemarks(), 250));
            mqRechargeService.insertRechargeMessage(recharge);
        }catch (Exception e){
            log.error("[RechargeOrderRetryMQReceiver]保存消息记录失败,message:{}",json.toString() ,e);
            LogUtils.saveLog("充值消费错误：保存消息记录失败", "RechargeOrderRetryMQReceiver", json.toString(), e, null);
        }
    }

    private int getMaxAttempts() {
        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }
}
