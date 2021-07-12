package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.google.common.base.Splitter;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2b.pb.MQTmallAnomalyRecourseMessage;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterAnomalyRecourseMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.tmall.mq.service.MqB2bTmallLogService;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourseImage;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallAnomalyRecouseService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 天猫一键求助消费者
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ms.b2bcenter.mq.order.consumer.enabled", matchIfMissing = false)
public class B2BCenterAnomalyRecourseRetryMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private TmallAnomalyRecouseService anomalyRecouseService;

    @Autowired
    private MqB2bTmallLogService mqB2bTmallLogService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private B2BCenterAnomalyRecourseMQSender anomalyRecourseMQSender;

    @Autowired
    private AreaService areaService;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        User user = B2BOrderVModel.b2bUser;
        MQTmallAnomalyRecourseMessage.TmallAnomalyRecourseMessage recourseMessage = null;
        int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));

        try {
            recourseMessage = MQTmallAnomalyRecourseMessage.TmallAnomalyRecourseMessage.parseFrom(message.getBody());
            if (recourseMessage != null && recourseMessage.getOrderId() > 0) {
                Order order = orderService.getOrderById(recourseMessage.getOrderId(),recourseMessage.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                //B2B分片与订单分片可能出现跨季度情况
                if(order == null){
                    order = orderService.getOrderById(recourseMessage.getOrderId(),"", OrderUtils.OrderDataLevel.CONDITION,true);
                }
                if(order == null){
                    //delay & retry
                    if (times < getMaxAttempts()) {
                        times++;
                        anomalyRecourseMQSender.sendDelay(recourseMessage, getDelaySeconds(times), times);
                    }else{
                        String msgJson = new JsonFormat().printToString(recourseMessage);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_DELAY_ANOMALYRECOURSE, msgJson,
                                user.getId(),
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                "消费B2B一键求助失败:读取订单失败");
                    }
                    return;
                }
                TmallAnomalyRecourse anomalyRecourse = new TmallAnomalyRecourse();
                anomalyRecourse.setAnomalyRecourseId(recourseMessage.getAnomalyRecourseId());
                anomalyRecourse.setOrderId(recourseMessage.getOrderId());
                anomalyRecourse.setOrderNo(order.getOrderNo());
                anomalyRecourse.setQuarter(order.getQuarter());
                anomalyRecourse.setServiceCode(recourseMessage.getServiceCode());
                anomalyRecourse.setQuestionType(new Dict(recourseMessage.getQuestionType()));
                anomalyRecourse.setStatus(recourseMessage.getStatus());
                anomalyRecourse.setSubmitDate(DateUtils.longToDate(recourseMessage.getSubmitTime()));
                for(MQTmallAnomalyRecourseMessage.RecourseMessage imageMsg : recourseMessage.getRecourseMessageListList()){
                    TmallAnomalyRecourseImage image = new TmallAnomalyRecourseImage();
                    image.setRecourseText(imageMsg.getRecourseText());
                    image.setImageUrls(imageMsg.getImageUrlsList());
                    image.setSubmitTime(DateUtils.longToDate(imageMsg.getSubmitTime()));
                    anomalyRecourse.getRecourseList().add(image);
                }
                anomalyRecourse.setRecourseJson(GsonUtils.getInstance().toGson(anomalyRecourse.getRecourseList()));
                anomalyRecourse.setCreateDate(new Date());
                anomalyRecourse.setAreaId(order.getOrderCondition().getArea().getId());//2019/03/15
                anomalyRecourse.setAreaName(order.getOrderCondition().getArea().getName());//2019/03/15
                anomalyRecourse.setProductCategoryId(order.getOrderCondition().getProductCategoryId());
                anomalyRecourse.setCustomerId(order.getOrderCondition().getCustomerId());
                anomalyRecourse.setCanRush(order.getOrderCondition().getCanRush());
                anomalyRecourse.setKefuType(order.getOrderCondition().getKefuType());
                Area area = areaService.getFromCache(order.getOrderCondition().getArea().getId());
                if (area != null) {
                    List<String> ids = Splitter.onPattern(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(area.getParentIds());
                    if (ids.size() >= 2) {
                        anomalyRecourse.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                        anomalyRecourse.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
                    }
                }
                try{
                    anomalyRecouseService.insert(anomalyRecourse);
                }catch (Exception e){
                    log.error("[B2BAnomalyRecourseRetryMQ]消费B2B一键求助失败,ordeId:{}",anomalyRecourse.getOrderId() ,e);
                    //delay & retry
                    if (times < getMaxAttempts()) {
                        times++;
                        anomalyRecourseMQSender.sendDelay(recourseMessage, getDelaySeconds(times), times);
                    }else{
                        String msgJson = new JsonFormat().printToString(recourseMessage);
                        B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_DELAY_ANOMALYRECOURSE, msgJson,
                                user.getId(),
                                B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                                "消费B2B一键求助失败:读取订单失败");
                    }
                }
            }else{
                log.error("消费B2B一键求助失败:消息体解析错误");
            }
        }
        catch (UnexpectedRollbackException ur){
            //delay & retry
            if(recourseMessage != null && recourseMessage.getOrderId() > 0){
                if (times < getMaxAttempts()) {
                    times++;
                    anomalyRecourseMQSender.sendDelay(recourseMessage, getDelaySeconds(times), times);
                }else{
                    String msgJson = new JsonFormat().printToString(recourseMessage);
                    B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_DELAY_ANOMALYRECOURSE, msgJson,
                            user.getId(),
                            B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                            "消费B2B一键求助失败:读取订单失败");
                }
            }

        } catch (Exception e){
            if(recourseMessage != null && recourseMessage.getOrderId() > 0) {
                if (times < getMaxAttempts()) {
                    times++;
                    anomalyRecourseMQSender.sendDelay(recourseMessage, getDelaySeconds(times), times);
                }else{
                    String msgJson = new JsonFormat().printToString(recourseMessage);
                    B2BFailureLogUtils.saveFailureLog(B2BMQQueueType.TMALLPUSH_DELAY_ANOMALYRECOURSE, msgJson,
                            user.getId(),
                            B2BProcessFlag.PROCESS_FLAG_FAILURE, times + 1,
                            "消费B2B一键求助失败:读取订单失败");
                }
                log.error("工单:{} 消费B2B一键求助失败",recourseMessage.getOrderId(), e);
            }else{
                LogUtils.saveLog("消费B2B一键求助失败", "B2BAnomalyRecourseRetryMQ", "", e, user);
                log.error("消费B2B一键求助失败", e);
            }
        }
    }

    private int getMaxAttempts() {
        return rabbitProperties.getTemplate().getRetry().getMaxAttempts();
    }

    private int getDelaySeconds(int times) {
        return (int) (rabbitProperties.getTemplate().getRetry().getInitialInterval() * rabbitProperties.getTemplate().getRetry().getMultiplier() * times);
    }


}
