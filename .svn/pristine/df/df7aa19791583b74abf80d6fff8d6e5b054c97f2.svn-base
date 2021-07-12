package com.wolfking.jeesite.ms.b2bcenter.mq.receiver;

import com.google.common.base.Splitter;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.mq.B2BMQQueueType;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderComplain;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.service.OrderComplainService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.config.B2BCenterOrderComplainConfig;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderComplainMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderComplainService;
import com.wolfking.jeesite.ms.utils.B2BFailureLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.assertj.core.util.Lists;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;


@Slf4j
@Component
public class B2BCenterOrderComplainMQReceiver implements ChannelAwareMessageListener {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderComplainService orderComplainService;
    @Autowired
    private B2BCenterOrderComplainMQSender sender;
    @Autowired
    private AreaService areaService;

    @Autowired
    private B2BOrderComplainService b2bOrderComplainService;


    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        User user = B2BOrderVModel.b2bUser;
        MQB2BOrderComplainMessage.B2BOrderComplainMessage msgObj = null;
        try {
            msgObj = MQB2BOrderComplainMessage.B2BOrderComplainMessage.parseFrom(message.getBody());
            if (msgObj == null) {
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_RETRY,null,B2BCenterOrderComplainConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"消息体错误",null);
                return;
            }
            if (msgObj.getOrderId() <= 0) {
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_RETRY,null,B2BCenterOrderComplainConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"参数错误:快可立订单Id内容错误",null);
                return;
            }
            int times = StringUtils.toInteger(message.getMessageProperties().getHeaders().get(B2BMQConstant.MESSAGE_PROPERTIES_HEADER_KEY_TIMES));
            //1.读取订单信息
            Order order = orderService.getOrderById(msgObj.getOrderId(),msgObj.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
            if(order == null || order.getOrderCondition() == null){
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_RETRY,msgObj,B2BCenterOrderComplainConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"读取订单信息错误",times);
                return;
            }
            //2.创建投诉单
            MSResponse response = createComplain(msgObj,order);
            if (!MSResponse.isSuccessCode(response)) {
                processFail(B2BMQQueueType.B2BCENTER_ORDER_COMPLAIN_RETRY,msgObj,B2BCenterOrderComplainConfig.B2B_UID,B2BProcessFlag.PROCESS_FLAG_FAILURE,1,"创建投诉单错误",times);
                return;
            }
        } catch (Exception e) {
            if (msgObj != null) {
                String msgJson = new JsonFormat().printToString(msgObj);
                log.error("重试消息失败, json:{}, errorMsg: {}", msgJson, e);
                LogUtils.saveLog("重试消息失败", "B2BCenterOrderComplainMQReceiver.onMessage", msgJson, e, user);
            } else {
                log.error("重试消息失败, errorMsg: {}", e);
                LogUtils.saveLog("重试消息失败", "B2BCenterOrderComplainMQReceiver.onMessage", "", e, user);
            }
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 处理失败
     * 1.记录日志
     * 2.重送处理
     * @param queueType         队列类型
     * @param message           消息体
     * @param createById
     * @param processFlag       状态
     * @param processTime       错误次数
     * @param processComment    错误内容
     * @param retryTimes        重试次数,null不重送，超过重送次数也不重送
     */
    private void processFail(B2BMQQueueType queueType, MQB2BOrderComplainMessage.B2BOrderComplainMessage message, Long createById,
                             B2BProcessFlag processFlag, int processTime, String processComment,Integer retryTimes){
        String msgJson = StringUtils.EMPTY;
        try {
            if(message != null){
                msgJson = new JsonFormat().printToString(message);
            }
            B2BFailureLogUtils.saveFailureLog(queueType, msgJson, createById, processFlag, processTime, processComment);
        }catch (Exception e){
            LogUtils.saveLog("失败处理错误", "B2BCenterOrderComplainMQReceiver", "", e, new User(createById));
        }
        //重试
        if(retryTimes != null && retryTimes < B2BCenterOrderComplainConfig.RETRY_TIMES){
            retryTimes++;
            sender.sendRetry(message, retryTimes);
        }else{
            log.error(retryTimes==null?"[B2B投诉-创建]初次处理失败,msg:{}":"[B2B投诉-创建]重试3次失败,msg:{}",msgJson);
            LogUtils.saveLog(retryTimes==null?"初次处理失败":"重试3次失败", "B2BCenterOrderComplainMQReceiver", msgJson, null, new User(createById));
        }
    }

    private MSResponse<OrderComplain> createComplain(MQB2BOrderComplainMessage.B2BOrderComplainMessage message,Order order){
        OrderComplain form = null;
        try {
            form = new OrderComplain();
            Long id = SeqUtils.NextID();
            form.setId(id);
            form.setAction(0);//new
            form.setOrderId(order.getId());
            form.setQuarter(order.getQuarter());
            OrderCondition orderCondition = order.getOrderCondition();
            form.setOrderNo(orderCondition.getOrderNo());
            form.setProductCategoryId(orderCondition.getProductCategoryId());

            form.setB2bComplainNo(message.getB2BComplainNo());
            form.setCustomer(orderCondition.getCustomer());
            //user
            Area area = areaService.getFromCache(orderCondition.getArea().getId());
            boolean parseAreaResult = parseProvinceAndCity(form,area);
            if(!parseAreaResult){
                return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, "读取投诉单区域所属省/市错误"));
            }
            //KKL投诉单号
            if (StringUtils.isBlank(message.getComplainNo())) {
                String no = generateFormNo();
                if (StringUtils.isBlank(no)) {
                    return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, "创建投诉单号失败"));
                }
                form.setComplainNo(no);
            } else {
                form.setComplainNo(message.getComplainNo());
            }
            form.setArea(orderCondition.getArea());
            form.setUserName(orderCondition.getUserName());
            form.setUserPhone(orderCondition.getServicePhone());
            form.setUserAddress(MessageFormat.format("{0} {1}",area.getFullName(),orderCondition.getServiceAddress()));
            form.setKefu(orderCondition.getKefu());
            //body
            form.setComplainBy("B2B账号");
            form.setComplainType(new Dict(0, "厂商"));//投诉类型 0-厂商 1-用户
            form.setStatus(new Dict(0, "待处理"));
            form.setServicePoint(orderCondition.getServicePoint() == null ? new ServicePoint(0L) : orderCondition.getServicePoint());
            //form.setComplainObject(2);//2-网点
            form.setComplainObjectsIds(Lists.newArrayList("1"));
            form.setComplainItem(2);//2-中差评
            form.setComplainItemsIds(Lists.newArrayList("1"));
            form.setApplyAttaches(Lists.newArrayList());
            form.setComplainRemark(StringUtils.left(message.getContent(), 500));
            form.setCreateBy(new User(B2BCenterOrderComplainConfig.B2B_UID, "B2B账号",""));
            form.setCreateDate(DateUtils.longToDate(message.getCreateAt()));
            form.setComplainDate(form.getCreateDate());
            form.setCreateType(OrderComplain.CREATE_TYPE_B2B);
            form.setCanRush(orderCondition.getCanRush());
            form.setOrderStatus(orderCondition.getStatus());
            form.setKefuType(orderCondition.getKefuType());
            orderComplainService.saveComplainApply(form,null);
            b2bOrderComplainService.updateFlag(message.getB2BComplainId(),form.getId(),order.getDataSourceId());
            return new MSResponse<>(form);
        }catch (Exception e){
            return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE, ExceptionUtils.getRootCauseMessage(e)));
        }
    }

    private boolean parseProvinceAndCity(OrderComplain complain,Area area){
        if(area == null) {
            return false;
        }
        List<String> ids = Splitter.onPattern(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(area.getParentIds());
        if (ids.size() >= 2) {
            complain.setCity(new Area(Long.valueOf(ids.get(ids.size() - 1))));
            complain.setProvince(new Area(Long.valueOf(ids.get(ids.size() - 2))));
        }else{
            return false;
        }
        return true;
    }

    private String generateFormNo(){
        int times = 0;
        String no = StringUtils.EMPTY;
        while(times<3) {
            no = SeqUtils.NextSequenceNo("ComplainNo");
            if(StringUtils.isNotBlank(no)){
                return no;
            }
            times++;
        }
        return no;
    }
}

