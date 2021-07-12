package com.wolfking.jeesite.modules.mq.receiver.sms;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.voiceservice.CallbackType;
import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderComplainService;
import com.wolfking.jeesite.modules.sd.service.OrderOpitionTraceService;
import com.wolfking.jeesite.modules.sd.service.OrderGradeService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.*;

/**
 * 订单客评服务
 * @autor Ryan Lu
 * @date 2019/1/18 2:07 PM
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SmsBusinessService extends LongIDBaseService {
    /*
    @Value("${sequence.workerid}")
    private int workerid;

    @Value("${sequence.datacenterid}")
    private int datacenterid;

    //id generator
    private static SequenceIdUtils sequenceIdUtils;

    @PostConstruct
    public void init() {
        SmsBusinessService.sequenceIdUtils = new SequenceIdUtils(workerid,datacenterid);
    }*/

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderComplainService orderComplainService;

    @Autowired
    private OrderGradeService orderGradeService;

    @Autowired
    private OrderOpitionTraceService orderOpitionTraceService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private AbnormalFormService abnormalFormService;

    @Autowired
    private AreaService areaService;



    private static final User user = new User(2l, "短信回访", "");

    /**
     * 自动短信客评处理逻辑
     */
    public void autoGradeAction(MQSmsCallbackMessage.SmsCallbackEntity callbackEntity){

        String json = new JsonFormat().printToString(callbackEntity);

        /*是否是本子系统负责的消息
        if(!callbackEntity.getSite().equalsIgnoreCase(siteCode)){
            log.warn("此系统为:{},接收到其他系统消息:{},msg:{}",siteCode,callbackEntity.getSite(),json);

            String msg = MessageFormat.format("此系统为:{0},接收到非本系统消息:{1},msg:{2}",siteCode,callbackEntity.getSite(),json);
            LogUtils.saveLog("语音回访:site错误","Order.CallbackReceiver",msg,null,null);
            return;
        }
        */

        Date date = DateUtils.longToDate(callbackEntity.getSendedAt());
        //region 业务处理部分
        try {
            //客评
            Order order = orderService.getOrderById(callbackEntity.getOrderId(), callbackEntity.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true,true,false,true);
            //检查是否可自动客评
            NameValuePair checkResult = orderGradeService.canAutoGrade(order);
            int checkResultValue = StringUtils.toInteger(checkResult.getValue());

            //日志
            OrderProcessLog processLog = new OrderProcessLog();
            Dict status = order.getOrderCondition().getStatus();
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            processLog.setQuarter(order.getQuarter());
            processLog.setAction("用户短信评价");
            processLog.setOrderId(callbackEntity.getOrderId());
            StringBuilder cmt = new StringBuilder(250);
            cmt.append("客评短信【").append(StringUtils.left(callbackEntity.getLabelling(),240)).append("】");
            processLog.setActionComment(cmt.toString());
            cmt.setLength(0);
            //processLog.setActionComment(callbackEntity.getLabelling());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);//跟踪进度
            processLog.setCloseFlag(0);
            if (callbackEntity.getScore() == 1 || callbackEntity.getScore() == 2) {
                //回复1，才厂商可见
                processLog.setRemarks(processLog.getActionComment());
            }
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            /* 2-已客评，已对账 不标记异常*/
            if (checkResultValue == 2) {
                return;
                /*
                log.warn("短信回访:不能自动客评-{},msg:{}", checkResult.getName(), json);
                // app abnormal
                HashMap<String, Object> map = Maps.newHashMap();
                map.put("quarter", order.getQuarter());
                map.put("orderId", callbackEntity.getOrderId());
                map.put("appAbnormalyFlag", 1);//异常
                map.put("updateBy", user);
                map.put("updateDate", date);
                orderService.updateOrderCondition(map);
                //异常消息统计
                try {
                    orderService.sendAppNoticeMessage(
                            callbackEntity.getOrderId(),
                            order.getQuarter(),
                            order.getOrderCondition().getCustomer().getId(),
                            order.getOrderCondition().getArea().getId(),
                            order.getOrderCondition().getKefu().getId(),
                            date,
                            user
                    );
                } catch (Exception e) {
                    log.error("[autoGradeAction] orderId:{}",callbackEntity.getOrderId(),e);
                }
                return;
                */
            }

            //转到回访失败列表标记
            Dict orderStatus = order.getOrderCondition().getStatus();
            //短信回访不成功（score>2）
            if(checkResult.getValue().equals("0") && callbackEntity.getScore() != 1 && callbackEntity.getScore() != 2){
                checkResultValue = 4;//*
                checkResult.setValue("4");
                checkResult.setName(MessageFormat.format("短信回访结果为:{0} ,不能自动客评",callbackEntity.getScore()));
            }

            int orderStatusValue = StringUtils.toInteger(orderStatus.getValue());
            // 回访失败
            // 检查不通过 + 工单状态 + 待回访状态
            //TODO: APP完工[55]
//            if (checkResultValue > 0
//                    && orderStatusValue >= Order.ORDER_STATUS_APPROVED
//                    && orderStatusValue <= Order.ORDER_STATUS_SERVICED
//                    && order.getOrderCondition().getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED ) {
            if (checkResultValue > 0
                    && orderStatusValue >= Order.ORDER_STATUS_APPROVED
                    && orderStatusValue <= Order.ORDER_STATUS_APP_COMPLETED
                    && order.getOrderCondition().getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED ) {
                orderGradeService.followUp(CallbackType.SMS,order.getOrderCondition(),checkResult,user,date);
                return;
            }
            //log.warn("checkResultValue:" + checkResultValue);
            //自动客评
//            if (checkResultValue == 0
//                    && orderStatusValue == Order.ORDER_STATUS_SERVICED
//                    && order.getOrderCondition().getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED ) {
            if (checkResultValue == 0
                    && (orderStatusValue == Order.ORDER_STATUS_SERVICED || orderStatusValue == Order.ORDER_STATUS_APP_COMPLETED)
                    && order.getOrderCondition().getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED ) {
                orderGradeService.autoGradeForCallback(order,user,json,OrderUtils.OrderGradeType.MESSAGE_GRADE);
            }else{
                orderGradeService.smsSaveFail(order,null,checkResultValue,callbackEntity.getLabelling(),callbackEntity.getScore(),user,date);
                //转投诉单
                if(callbackEntity.getScore() == 3){
                    smsToComplain(order.getOrderCondition(),callbackEntity.getLabelling());
                }
                log.error("短信回访:不退回访失败列表，也不自动客评,订单标记为APP异常,msg:{}",json);
            }

        } catch (Exception e){
            log.error("短信回访:回调处理失败,msg:{}",json,e);
            try {
                LogUtils.saveLog("短信回访:回调处理失败","SmsCallbackReceiver.onAction",json,e,user);
            }catch (Exception ex){}
        }

        //endregion
    }

    /**
     * 普通回复短信，记录订单日志并标志订单为App异常
     * @param orderId
     * @param quarter
     * @param statusValue
     * @param sms
     * @param user
     */
    @Transactional
    public void smsSaveOrderLogAndAppAbnormal(Long customerId,Long areaId,Long kefuId,Long orderId,String quarter,int statusValue,String sms,User user) throws Exception {
        if(orderId == null || StringUtils.isBlank(quarter) || statusValue <= 0
                || StringUtils.isBlank(sms)){
            return;
        }
        Date date = new Date();
        //日志
        OrderProcessLog processLog = new OrderProcessLog();
        Dict status = new Dict(statusValue,"");
        processLog.setStatus(status.getLabel());
        processLog.setStatusValue(Integer.parseInt(status.getValue()));
        processLog.setQuarter(quarter);
        processLog.setAction("用户短信评价");
        processLog.setOrderId(orderId);
        StringBuilder cmt = new StringBuilder(250);
        cmt.append("短信回复【").append(StringUtils.left(sms,240)).append("】");
        processLog.setActionComment(cmt.toString());
        cmt.setLength(0);
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);//跟踪进度
        processLog.setCloseFlag(0);
        processLog.setCreateBy(user);
        processLog.setCreateDate(date);
        processLog.setCustomerId(customerId);
        orderService.saveOrderProcessLogNew(processLog);

        // app abnormal
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("quarter", quarter);
        map.put("orderId", orderId);
        map.put("appAbnormalyFlag", 1);//异常
        map.put("updateBy", user);
        map.put("updateDate", date);
        orderService.updateOrderCondition(map);
        //意见跟踪日志
        OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                .channel(AppFeedbackEnum.Channel.SMS.getValue())
                .quarter(quarter)
                .orderId(orderId)
                .servicePointId(0L)
                .appointmentAt(0)
                .opinionId(0)
                .parentId(0)
                .opinionType(AppFeedbackEnum.FeedbackType.SMS.getValue())
                .opinionValue(0)
                .opinionLabel(StringUtils.left(processLog.getActionComment(),250))
                .isAbnormaly(1)
                .remark("非客评短信回复")
                .createAt(System.currentTimeMillis())
                .createBy(user)
                .times(1)
                .totalTimes(1)
                .build();
        orderOpitionTraceService.insert(opitionTrace);

        //异常单
        Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
        if(order!=null && order.getOrderCondition()!=null){
            AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order,processLog.getActionComment(),user,AppFeedbackEnum.Channel.SMS.getValue(),
                    AbnormalFormEnum.FormType.SMS.getCode(), AbnormalFormEnum.SubType.NOT_MSM_GRADE.getCode(),"");
            if(abnormalForm!=null){
                abnormalForm.setOpinionLogId(opitionTrace.getId());
                try {
                    abnormalFormService.save(abnormalForm);
                }catch (Exception e){
                    log.error("[smsSaveOrderLogAndAppAbnormal]普通回复短信保存异常单失败 form:{}",GsonUtils.getInstance().toGson(abnormalForm),e);
                }
            }
        }
        //同步网点工单数据
        if(statusValue < Order.ORDER_STATUS_COMPLETED) {
            servicePointOrderBusinessService.abnormalyFlag(
                    orderId,
                    quarter,
                    null,
                    1,
                    user.getId(),
                    date.getTime()
            );
        }
        //异常消息统计
        try {
            orderService.sendAppNoticeMessage(
                    orderId,
                    quarter,
                    customerId,
                    areaId,
                    kefuId,
                    date,
                    user
            );
        } catch (Exception e) {
            log.error("[smsSaveOrderLogAndAppAbnormal] orderId:{}",orderId,e);
        }
    }

    @Transactional
    public void smsSaveOrderLog(Long customerId,Long orderId,String quarter,int statusValue,String sms,User user) {
        if (orderId == null || StringUtils.isBlank(quarter) || statusValue <= 0
                || StringUtils.isBlank(sms)) {
            return;
        }
        Date date = new Date();
        //日志
        OrderProcessLog processLog = new OrderProcessLog();
        Dict status = new Dict(statusValue,"");
        processLog.setStatus(status.getLabel());
        processLog.setStatusValue(Integer.parseInt(status.getValue()));
        processLog.setQuarter(quarter);
        processLog.setAction("用户回复短信");
        processLog.setOrderId(orderId);
        StringBuilder cmt = new StringBuilder(250);
        cmt.append("短信回复【").append(StringUtils.left(sms,240)).append("】");
        processLog.setActionComment(cmt.toString());
        cmt.setLength(0);
        //processLog.setActionComment(StringUtils.left("用户短信回复："+sms,250));
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);//跟踪进度
        processLog.setCloseFlag(0);
        processLog.setCreateBy(user);
        processLog.setCreateDate(date);
        processLog.setCustomerId(customerId);
        orderService.saveOrderProcessLogNew(processLog);
    }

    /**
     * 短信回复 3:自动生成投诉单
     * @param orderCondition
     * @param msgContext  短信内容
     */
    public void smsToComplain(OrderCondition orderCondition,String msgContext){
        if(orderCondition == null || orderCondition.getOrderId() == null){
            log.error("用户短信回复自动转投诉单失败,传入订单null");
            return;
        }
        //check
        List<OrderComplain> complains = orderComplainService.getComplainListByOrder(orderCondition.getOrderId(),"",orderCondition.getQuarter(),false);
        if(!ObjectUtils.isEmpty(complains)){
            long cnt = complains.stream().filter(t->t.getStatus().getIntValue()<=OrderComplain.STATUS_PROCESSING.intValue()).count();
            if(cnt > 0){
                log.error("用户短信回复自动转投诉单失败:订单已有未处理完成的投诉单");
                return;
            }
        }
        User user = new User(2L,"用户回复短信","");
        Date date = new Date();
        OrderComplain complain = new OrderComplain();
        long id = sequenceIdService.nextId();
        complain.setId(id);
        String no =SeqUtils.NextSequenceNo("ComplainNo", 0, 3);
        if(StringUtils.isBlank(no)){
            log.error("用户短信回复自动转投诉单失败：生成投诉单号错误");
            return;
        }
        complain.setComplainNo(no);
        complain.setAction(0);
        complain.setStatus(new Dict(OrderComplain.STATUS_APPLIED.toString(),"待处理"));
        complain.setComplainType(new Dict("1","用户"));//投诉方
        complain.setComplainObjectsIds(Lists.newArrayList("1"));//投诉网点
        complain.setComplainItemsIds(Lists.newArrayList("1"));//中差评
        complain.setComplainBy(user.getName());
        complain.setComplainDate(date);
        complain.setComplainRemark(StringUtils.isBlank(msgContext)?"用户短信回复:对师傅的服务不满意":msgContext);
        complain.setAttachmentQty(0);
        complain.setCreateBy(user);
        complain.setCreateDate(date);
        complain.setAppointDate(date);//新建的时间把待跟进时间也设置为创建时间
        complain.setUpdateBy(user);
        complain.setUpdateDate(date);
        //from order
        complain.setQuarter(orderCondition.getQuarter());
        complain.setOrderId(orderCondition.getOrderId());
        complain.setOrderNo(orderCondition.getOrderNo());
        complain.setProductCategoryId(orderCondition.getProductCategoryId());
        complain.setArea(orderCondition.getArea());
        complain.setServicePoint(orderCondition.getServicePoint()==null?new ServicePoint(0L):orderCondition.getServicePoint());
        complain.setCustomer(orderCondition.getCustomer()==null?new Customer(0L):orderCondition.getCustomer());
        complain.setUserName(orderCondition.getUserName());
        complain.setUserPhone(orderCondition.getServicePhone());
        complain.setUserAddress(String.format("%s %s",orderCondition.getArea().getName() ,orderCondition.getServiceAddress()));
        complain.setKefu(orderCondition.getKefu()==null?new User(0L,"",""):orderCondition.getKefu());
        complain.setOrderStatus(orderCondition.getStatus());
        complain.setCanRush(orderCondition.getCanRush());
        complain.setCreateType(OrderComplain.CREATE_TYPE_MANUANL);
        complain.setKefuType(orderCondition.getKefuType());
        Area area = areaService.getFromCache(orderCondition.getArea().getId());
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                complain.setCity(new Area(Long.valueOf(ids.get(ids.size() - 1))));
                complain.setProvince(new Area(Long.valueOf(ids.get(ids.size() - 2))));
            }
        }
        //save
        try {
            orderComplainService.saveComplainApply(complain,null);
        }catch (Exception e){
            StringBuilder json = new StringBuilder();
            try {
                json.append(GsonUtils.getInstance().toGson(complain));
            }catch (Exception e1){}
            log.error("用户短信回复自动转投诉单失败,json:{}",json.toString(),e);
            json = null;
        }
    }

    /**
     * 根据短信内容转评分
     */
    public int smsContentToScore(String content){
        int score = 0;
        if(org.apache.commons.lang3.StringUtils.isBlank(content)){
            return score;
        }
        if(content.equalsIgnoreCase("1") || content.equalsIgnoreCase("非常满意")){
            score = 1;
        }else if(content.equalsIgnoreCase("2") || content.equalsIgnoreCase("满意") ){
            score = 2;
        }else{
            try {
                score = Integer.parseInt(content);
            }catch (Exception e){
                score = 0;
            }
        }
        return score;
    }

    /**
     * 短信评分 转 评价内容
     * @param score
     * @return
     */
    public String scoreToGradeContent(Integer score,String text){
        String content = new String("");
        switch (score){
            case 1:
                content= MessageFormat.format("用户回复: {0}，对师傅的服务满意",text);
                break;
            case 2:
                content= MessageFormat.format("用户回复：{0},对师傅的服务评价一般",text);
                break;
            case 3:
                content= MessageFormat.format("用户回复：{0}，对师傅的服务不满意",text);
                break;
            case 4:
                content= MessageFormat.format("用户回复：{0}，还有部分产品未完成",text);
                break;
            default:
                content= MessageFormat.format("用户回复：{0}",text);
                break;
        }
        return content;
    }
}
