/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.td.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.entity.md.RestGetVerifyCode;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestEnum;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.dto.MQNoticeMessage;
import com.wolfking.jeesite.modules.mq.dto.MQWebSocketMessage;
import com.wolfking.jeesite.modules.mq.sender.NoticeMessageSender;
import com.wolfking.jeesite.modules.mq.sender.ShortMessageSender;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.NoticeMessageItemVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.td.dao.Message2Dao;
import com.wolfking.jeesite.modules.td.entity.Message2;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.wolfking.jeesite.common.config.redis.RedisConstant.RedisDBType.REDIS_TEMP_DB;
import static com.wolfking.jeesite.common.config.redis.RedisConstant.VERCODE_KEY;

/**
 * 消息Service
 */
@Configurable
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class MessageService extends LongIDBaseService{

    @Value("${site.code}")
    private String siteCode;

    /**
     * 持久层对象
     */

    @Autowired
    private Message2Dao message2Dao;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderVoiceTaskService orderVoiceTaskService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private RedisUtils redisUtils;

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private NoticeMessageSender noticeMessageSender;

    @Autowired
    private SmsMQSender smsMQSender;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderGradeService  orderGradeService;

    @Autowired
    private OrderOpitionTraceService orderOpitionTraceService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private AbnormalFormService abnormalFormService;

    public void insert(Message2 message2){
        message2Dao.insert(message2);
    }

    public void update(Message2 message2){
        message2Dao.update(message2);
    }

    //待重送列表
    public List<Message2> getResendList(Integer number){
        return message2Dao.getResendList(number);
    }

    @Transactional(readOnly = false)
    public void autoGradeOneNew(Message2 savemessage, OrderGradeModel gradeModel) {
        // 默认：处理成功
        savemessage.setStatus(30);
        User user = savemessage.getCreateBy();
        Date date = savemessage.getCreateDate();
        Integer score = 0;
        String trackingContent = new String("");
        Dict status = new Dict("0", "");

        Order order = null;
        OrderCondition orderCondition = null;
        OrderProcessLog processLog = null;
        List<OrderCondition> orders;
        try {
            // 按电话号码查找订单(可能返回多个)
            orders = orderService.getToGradeOrdersByPhone(savemessage.getMobile());
            //没找到订单，忽略
            if (orders == null || orders.size() == 0) {
                savemessage.setRemarks("未找到订单");
                message2Dao.insert(savemessage);
                return;
            }

            //同用户多订单情况,只保存log
            if (orders.size() > 1) {
                savemessage.setRemarks("找到多个订单");
            }
            // 找到真正用到的订单
            orderCondition = getToGradeOrder(orders);

            if (null == orderCondition) {
                savemessage.setRemarks(savemessage.getRemarks().length() == 0 ? "未找到订单" : savemessage.getRemarks());
                message2Dao.insert(savemessage);//保存消息
                return;
            }

            final String quarter = orderCondition.getQuarter();

            //region 客评处理
            order = orderService.getOrderById(orderCondition.getOrderId(), orderCondition.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true,true,false,true);
            //检查是否可自动客评
            NameValuePair checkResult = orderGradeService.canAutoGrade(order);
            if(!checkResult.getValue().equals("0")){
                savemessage.setStatus(40);
                savemessage.setRemarks(checkResult.getName());
            }

            //短信内容
            String msg = savemessage.getContent().trim();
            if(msg.equalsIgnoreCase("1") || msg.equalsIgnoreCase("非常满意")){
                score = 1;
            }else if(msg.equalsIgnoreCase("2") || msg.equalsIgnoreCase("满意") ){
                score = 2;
            }else{
                try {
                    score = Integer.parseInt(msg);
                }catch (Exception e){
                    score = 0;
                }
            }

            trackingContent = gradeScoreToContent(score, savemessage.getContent());

            processLog = new OrderProcessLog();
            processLog.setQuarter(orderCondition.getQuarter());
            processLog.setAction("用户短信评价");
            processLog.setOrderId(orderCondition.getOrderId());
            StringBuilder cmt = new StringBuilder(250);
            cmt.append("短信回复【").append(StringUtils.left(trackingContent,240)).append("】");
            processLog.setActionComment(cmt.toString());
            cmt.setLength(0);
            //processLog.setActionComment(trackingContent);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);//跟踪进度
            processLog.setCloseFlag(0);
            processLog.setRemarks("");//终端用户名
            if (score == 1 || score == 2) {
                //回复1，才厂商可见
                processLog.setRemarks(processLog.getActionComment());
            }
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);

            // 异常处理
            // 回复内容不是：1 ,2 ,满意,非常满意
            if(checkResult.getValue().equals("0") && score != 1 && score != 2){
                savemessage.setStatus(40);
                savemessage.setRemarks("短信内容不是:1,2,非常满意,满意");
                checkResult.setValue("3");
            }

            if (!checkResult.getValue().equals("0")) {
                saveFail(savemessage,order,processLog,score,checkResult.getValue(),user,date);
                return;
            }

            //region 正常处理

            orderCondition = order.getOrderCondition();
            Engineer engineer = new Engineer();
            engineer.setId(order.getOrderCondition().getEngineer().getId());
            engineer.setName(order.getOrderCondition().getEngineer().getName());
            gradeModel.setEngineer(engineer);
            gradeModel.setOrder(order);
            gradeModel.setOrderId(orderCondition.getOrderId());
            gradeModel.setQuarter(orderCondition.getQuarter());
            gradeModel.getGradeList().stream().forEach(t -> {
                t.setQuarter(quarter);
            });

            status = orderCondition.getStatus();
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            //保存消息体
            try {
                message2Dao.insert(savemessage);//保存消息
            } catch (Exception e) {
                log.error("[MessageService.autoGradeOne] insert ,orderId:{}", orderCondition.getOrderId(), e);
                //LogUtils.saveLog("保存接收短信消息错误", "MessageService.autoGrade", orderCondition.getOrderId().toString(), e, null);
            }
            //记录订单日志
            processLog.setCustomerId(orderCondition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);
            OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(order.getId(),order.getQuarter());
            orderService.saveGrade(gradeModel,orderStatusFlag, user, null, null);
            //endregion 正常处理

        } catch (Exception e) {
            log.error("[autoGradeOne] orderId:{}",(order == null ? "" : order.getId().toString()),e);
            //由于捕捉了异常，需要使用该方法让事物回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            //使用异步线程，保存错误时，将订单标记异常
            if (null != orderCondition) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("quarter", orderCondition.getQuarter());
                map.put("orderId", orderCondition.getOrderId());
                map.put("appAbnormalyFlag", 1);//异常
                //map.put("autoGradeFlag", 2);//标记用户已回复 comment at 2019/01/23
                map.put("updateBy", user);
                map.put("updateDate", date);
                try {
                    //意见跟踪日志
                    OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                            .channel(AppFeedbackEnum.Channel.SMS.getValue())
                            .quarter(orderCondition.getQuarter())
                            .orderId(orderCondition.getOrderId())
                            .servicePointId(0L)
                            .appointmentAt(0)
                            .opinionId(0)
                            .parentId(0)
                            .opinionType(AppFeedbackEnum.FeedbackType.SMS_GRADE.getValue())
                            .opinionValue(0)
                            .opinionLabel(StringUtils.left(trackingContent,250))
                            .isAbnormaly(1)
                            .remark("用户短信客评处理失败，标记异常")
                            .createAt(System.currentTimeMillis())
                            .createBy(user)
                            .times(1)
                            .totalTimes(1)
                            .build();
                    OrderCondition finalOrderCondition = orderCondition;
                    Long spId = Optional.ofNullable(orderCondition.getServicePoint()).map(t->t.getId()).orElse(0L);

                    //异常单
                    final AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order,trackingContent,user,AppFeedbackEnum.Channel.SMS.getValue(),
                            AbnormalFormEnum.FormType.SMS.code,AbnormalFormEnum.SubType.MSM_GRADE.code,"用户短信客评处理失败");
                    //异常消息汇总
                    if(orderCondition.getAppAbnormalyFlag() == 0) {
                        try {
                            sendAppNoticeMessage(
                                    orderCondition.getOrderId(),
                                    orderCondition.getQuarter(),
                                    orderCondition.getCustomer().getId(),
                                    orderCondition.getArea().getId(),
                                    orderCondition.getKefu() != null ? orderCondition.getKefu().getId() : 0l,
                                    new Date(),
                                    user
                            );
                        } catch (Exception ex) {
                            log.error("[用户短信客评处理失败]sendAppNoticeMessage orderId:{}", orderCondition.getOrderId(), ex);
                        }
                    }
                    CompletableFuture.supplyAsync(() -> {
                        orderService.updateOrderCondition(map);
                        //同步网点工单数据
                        servicePointOrderBusinessService.abnormalyFlag(
                                finalOrderCondition.getOrderId(),
                                finalOrderCondition.getQuarter(),
                                spId,
                                1,
                                user.getId(),
                                date.getTime()
                        );
                        try {
                            orderOpitionTraceService.insert(opitionTrace);
                            if(abnormalForm !=null){
                                abnormalForm.setOpinionLogId(opitionTrace.getId());
                                abnormalFormService.save(abnormalForm);
                            }
                        } catch (Exception ex) {
                            log.error("[autoGradeOne] Async update order error:",ex);
                            return null;
                        }
                        return true;
                    })
                    .exceptionally(t -> {
                        log.error("[autoGradeOne] Async update order error:",t);
                        return null;
                    }).get();
                } catch (Exception e1) {
                    log.error("[autoGradeOne] Async update order error:",e1);
                }
            }
            if (null != savemessage) {
                savemessage.setStatus(40);
                savemessage.setRemarks(e.getMessage());
                try {
                    CompletableFuture.supplyAsync(() -> {
                        message2Dao.insert(savemessage);//保存消息
                        return true;
                    })
                            .exceptionally(t -> {
                                log.error("[autoGradeOne] Async save message error:",t);
                                //System.out.println("Unexpected error:" + t);
                                return null;
                            }).get();
                } catch (Exception e1) {
                    log.error("[autoGradeOne] Async save message error:",e1);
                }
            }
        }
    }

    @Transactional
    public void saveFail(Message2 savemessage,Order order,OrderProcessLog processLog,int score,String checkResult,User user,Date date) throws Exception {
        OrderCondition orderCondition = order.getOrderCondition();
        Dict status = orderCondition.getStatus();
        processLog.setStatus(status.getLabel());
        processLog.setStatusValue(Integer.parseInt(status.getValue()));
        if(!checkResult.equals("1") && !checkResult.equals("0")) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("quarter", order.getQuarter());
            map.put("orderId", order.getId());
            map.put("appAbnormalyFlag", 1);//异常
            map.put("updateBy", user);
            map.put("updateDate", date);
            orderService.updateOrderCondition(map);
            //同步网点工单数据
            Long spId = Optional.ofNullable(orderCondition.getServicePoint()).map(t->t.getId()).orElse(0L);
            servicePointOrderBusinessService.abnormalyFlag(
                    order.getId(),
                    order.getQuarter(),
                    spId,
                    1,
                    user.getId(),
                    date.getTime()
            );
            //意见跟踪日志
            OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                    .channel(AppFeedbackEnum.Channel.SMS.getValue())
                    .quarter(order.getQuarter())
                    .orderId(order.getId())
                    .servicePointId(0L)
                    .appointmentAt(0)
                    .opinionId(0)
                    .parentId(0)
                    .opinionType(AppFeedbackEnum.FeedbackType.SMS.getValue())
                    .opinionValue(0)
                    .opinionLabel(StringUtils.left(processLog.getActionComment(),250))
                    .isAbnormaly(1)
                    .remark("用户短信回复，标记异常")
                    .createAt(System.currentTimeMillis())
                    .createBy(user)
                    .times(1)
                    .totalTimes(1)
                    .build();
            orderOpitionTraceService.insert(opitionTrace);

            //异常消息汇总
            if(orderCondition.getAppAbnormalyFlag() == 0) {
                try {
                    sendAppNoticeMessage(
                            orderCondition.getOrderId(),
                            orderCondition.getQuarter(),
                            orderCondition.getCustomer().getId(),
                            orderCondition.getArea().getId(),
                            orderCondition.getKefu() != null ? orderCondition.getKefu().getId() : 0l,
                            date,
                            user
                    );
                } catch (Exception e) {
                    log.error("[MessageService.saveFail]sendAppNoticeMessage orderId:{}", orderCondition.getOrderId(), e);
                }
            }
            // 异常单
            AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order,processLog.getActionComment(),user,AppFeedbackEnum.Channel.SMS.getValue(),
                    AbnormalFormEnum.FormType.SMS.code,AbnormalFormEnum.SubType.MSM_GRADE.code,"");
            if(abnormalForm!=null){
                abnormalForm.setOpinionLogId(opitionTrace.getId());
                abnormalFormService.save(abnormalForm);
            }
        }
        message2Dao.insert(savemessage);//保存消息
        //记录订单日志
        processLog.setCustomerId(orderCondition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        orderService.saveOrderProcessLogNew(processLog);
        //取消智能回访
        if (StringUtils.isNoneBlank(siteCode)) {
            try {
                Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
                if (taskResult != null && taskResult == 0) {
                    try {
                        orderService.stopVoiceOperateMessage(siteCode, order.getId(), order.getQuarter(), user.getName(), date);
                    } catch (Exception e) {
                        log.error("收到短信-停滞智能回访错误:" + order.getId(), e);
                    }
                }//taskResult
            }catch (Exception e){
                log.error("收到短信-停滞智能回访错误:" + order.getId(), e);
            }
        }//site

        return;
    }

    /* commented at 2019/01/22 ryan
    @Transactional(readOnly = false)
    public void autoGradeOne (Message2 savemessage, OrderGradeModel gradeModel) {
        //Long orderId;
        savemessage.setStatus(30);
        User user = savemessage.getCreateBy();
        Date date = savemessage.getCreateDate();
        Integer score = 0;
        String trackingContent = new String("");
        Dict status = new Dict("0", "");
        //String orderKey = new String("");
        HashMap<String, Object> map = new HashMap<String, Object>();
        long servicedQty = 0;
        Order order = null;
        OrderCondition orderCondition = null;
        OrderProcessLog processLog = null;
        List<OrderCondition> orders;
        try {
            // 按电话号码查找订单(可能返回多个)
            orders = orderService.getToGradeOrdersByPhone(savemessage.getMobile());
            //没找到订单，忽略
            if (orders == null || orders.size() == 0) {
                savemessage.setRemarks("未找到订单");
                message2Dao.insert(savemessage);
                return;
            }

            //同用户多订单情况,只保存log
            if (orders.size() > 1) {
                savemessage.setRemarks("找到多个订单");
            }
            orderCondition = getToGradeOrder(orders);//找到真正用到的订单

            if (null == orderCondition) {
                savemessage.setRemarks(savemessage.getRemarks().length() == 0 ? "未找到订单" : savemessage.getRemarks());
                message2Dao.insert(savemessage);//保存消息
                return;
            }
            final String quarter = orderCondition.getQuarter();
            Boolean appFlag = false;//是否是app异常情况
            Boolean countAppAbnormalyMessage = true;//累计到app异常通知

            //region 客评处理
            order = orderService.getOrderById(orderCondition.getOrderId(), orderCondition.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            if (null == order || null == order.getOrderCondition()) {
                appFlag = true;
                savemessage.setStatus(40);
                savemessage.setRemarks("读取订单失败:" + orderCondition.getOrderId().toString());
            }else {
                orderCondition = order.getOrderCondition();
            }
            //短信内容
            boolean canAutoGrade = false;
            String msg = savemessage.getContent().trim();
            if(msg.equalsIgnoreCase("1") || msg.equalsIgnoreCase("非常满意")){
                canAutoGrade = true;
                score = 1;
            }else if(msg.equalsIgnoreCase("2") || msg.equalsIgnoreCase("满意") ){
                canAutoGrade = true;
                score = 2;
            }else{
                try {
                    score = Integer.parseInt(msg);
                }catch (Exception e){
                    score = 0;
                }
            }

            trackingContent = gradeScoreToContent(score, savemessage.getContent());

            processLog = new OrderProcessLog();
            processLog.setQuarter(orderCondition.getQuarter());
            processLog.setAction("用户短信评价");
            processLog.setOrderId(orderCondition.getOrderId());
            processLog.setActionComment(trackingContent);
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_TRACKING);//跟踪进度
            processLog.setCloseFlag(0);
            processLog.setRemarks("");//终端用户名
            if (score == 1 || score == 2) {
                //回复1，才厂商可见
                processLog.setRemarks(processLog.getActionComment());
            }
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);

            int orgAppAbnormalFlag = orderCondition.getAppAbnormalyFlag();
            //region 异常处理
            //以下几种情况不客评，但标记异常
            // 1.回复内容不是：1 ,2 ,满意,非常满意
            if(!appFlag && canAutoGrade == false){
                appFlag = true;
                savemessage.setRemarks("短信内容不是:1,2,非常满意,满意");
            }
            // 2.同手机多单情况
            //   1.1.多个已上门单
            //   1.2.没有已上门单
            // 3.订单状态不符,不是已上门
            // 4.非待客评(grade_flag !=2 )
            // 5.app异常单
            if(!appFlag) {
                servicedQty = orders.stream().filter(t -> Order.ORDER_STATUS_SERVICED.intValue() == t.getStatusValue()).count();
                if (servicedQty > 1
                        || (orders.size() > 1 && Order.ORDER_STATUS_SERVICED != orderCondition.getStatusValue())
                        || Order.ORDER_STATUS_SERVICED != orderCondition.getStatusValue()
                        || orderCondition.getGradeFlag() < 2
                        || 1 == orgAppAbnormalFlag) {
                    appFlag = true;
                    if(servicedQty>1){
                        savemessage.setRemarks("多个已上门单");
                    } else if (Order.ORDER_STATUS_SERVICED != orderCondition.getStatusValue()) {
                        savemessage.setRemarks("订单状态不符合");
                    } else if (orderCondition.getAppAbnormalyFlag() == 1) {
                        savemessage.setRemarks("已标记为app异常");
                    } else if (orderCondition.getGradeFlag() == 1) {
                        savemessage.setRemarks("已客评");
                    } else if (orderCondition.getGradeFlag() == 0) {
                        savemessage.setRemarks("非待客评状态");
                    }
                }
            }
            //6.检查配件是否都已处理
            if (!appFlag) {
                Integer qty = orderService.getNoApprovedMaterialMasterQty(orderCondition.getOrderId(), orderCondition.getQuarter());
                if (qty != null && qty > 0) {
                    appFlag = true;
                    savemessage.setRemarks("配件申请未审核或待发货");
                }
            }
            //7.检查完成照片
            if(!appFlag && null != order) {
                Customer customer = customerService.getFromCache(orderCondition.getCustomer().getId());
                if (null != customer && customer.getMinUploadNumber()>0 && orderCondition.getFinishPhotoQty()<customer.getMinUploadNumber()) {
                    appFlag = true;
                    savemessage.setRemarks("上传的服务效果图少于客户要求的最小数量");
                }
            }
            //8.无上门服务
            if (!appFlag) {
                if (order.getDetailList().size() == 0) {
                    appFlag = true;
                    savemessage.setRemarks("无上门服务");
                    orderCondition = order.getOrderCondition();
                }
            }
            //9.检查金额是否有问题,如这里不检查，
            // 后面gradeModel.setCheckCanAutoCharge(true)
            if (!appFlag) {
                Boolean checkFee = orderService.checkOrderFeeAndServiceAmountBeforeGrade(order, true);
                if (!checkFee) {
                    appFlag = true;
                    savemessage.setRemarks("此订单金额异常不能自动完工");
                }
            }
            //10.检查是否可自动客评,不能自动生成对账单的，不能自动客评
            if (!appFlag) {
                Boolean canGrade = orderService.compareOrderDetail(order);
                if (!canGrade) {
                    appFlag = true;
                    savemessage.setRemarks("此订单不符合自动客评要求");
                }
            }
            if (true == appFlag) {
                //region 异常处理
                if (null != order) {//已经读取订单详细资料
                    status = orderCondition.getStatus();
                } else {
                    status = MSDictUtils.getDictByValue(orderCondition.getStatus().getValue(), "order_status");//切换为微服务
                    if (status == null) {
                        status = new Dict(orderCondition.getStatus().getValue(), "读取状态错误");
                    }
                }
                processLog.setStatus(status.getLabel());
                processLog.setStatusValue(Integer.parseInt(status.getValue()));

                map.put("quarter", orderCondition.getQuarter());
                map.put("orderId", orderCondition.getOrderId());
                if(1 != score){
                    map.put("appAbnormalyFlag", 1);//异常
                }
                //map.put("autoGradeFlag", 2);//标记用户已回复
                //停滞
                //map.put("pendingFlag",2);//2:正常
                //map.put("pendingType",orderCondition.getPendingType());
                //map.put("pendingTypeDate",null);
                map.put("updateBy", user);
                map.put("updateDate", date);
                orderService.updateOrderCondition(map);
                //savemessage.setStatus(40);
                message2Dao.insert(savemessage);//保存消息
                //记录订单日志
                orderService.insertProcessLog(processLog);
                try {
                    if (1 != score && countAppAbnormalyMessage && 1 != orderCondition.getAppAbnormalyFlag()) {
                        sendAppNoticeMessage(
                                orderCondition.getOrderId(),
                                orderCondition.getQuarter(),
                                orderCondition.getCustomer().getId(),
                                orderCondition.getArea().getId(),
                                orderCondition.getKefu() != null ? orderCondition.getKefu().getId() : 0l,
                                date,
                                user
                        );
                    }
                } catch (Exception e) {
                    log.error("[MessageService.autoGrade]sendAppNoticeMessage orderId:{}",orderCondition.getOrderId(),e);
                    //LogUtils.saveLog("统计APP异常消息错误", "MessageService.autoGrade", orderCondition.getOrderId().toString(), e, null);
                }
                //取消智能回访
                String site = Global.getSiteCode();
                if (StringUtils.isNoneBlank(site)) {
                    try {
                        Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
                        if (taskResult != null && taskResult == 0) {
                            try {
                                orderService.stopVoiceOperateMessage(site, order.getId(), order.getQuarter(), user.getName(), date);
                            } catch (Exception e) {
                                log.error("收到短信-停滞智能回访错误:" + order.getId(), e);
                            }
                        }//taskResult
                    }catch (Exception e){
                        log.error("收到短信-停滞智能回访错误:" + order.getId(), e);
                    }
                }//site
                return;
                //endregion 异常处理
            }
            else {
                //region 正常处理

                orderCondition = order.getOrderCondition();
                Engineer engineer = new Engineer();
                engineer.setId(order.getOrderCondition().getEngineer().getId());
                engineer.setName(order.getOrderCondition().getEngineer().getName());
                gradeModel.setEngineer(engineer);
                gradeModel.setOrder(order);
                gradeModel.setOrderId(orderCondition.getOrderId());
                gradeModel.setQuarter(orderCondition.getQuarter());
                gradeModel.getGradeList().stream().forEach(t -> {
                    t.setQuarter(quarter);
                });

                status = orderCondition.getStatus();
                processLog.setStatus(status.getLabel());
                processLog.setStatusValue(Integer.parseInt(status.getValue()));
                //保存消息体
                try {
                    message2Dao.insert(savemessage);//保存消息
                } catch (Exception e) {
                    log.error("[MessageService.autoGradeOne] insert ,orderId:{}", orderCondition.getOrderId(), e);
                    //LogUtils.saveLog("保存接收短信消息错误", "MessageService.autoGrade", orderCondition.getOrderId().toString(), e, null);
                }
                //记录订单日志
                orderService.insertProcessLog(processLog);

                orderService.saveGrade(gradeModel, user, null, null);

                //endregion 正常处理
            }
        } catch (Exception e) {
            log.error("[autoGradeOne] orderId:{}",(order == null ? "" : order.getId().toString()),e);
            //由于捕捉了异常，需要使用该方法让事物回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            //使用异步线程，保存错误时，将订单标记异常
            if (null != orderCondition) {
                map.clear();
                map.put("quarter", orderCondition.getQuarter());
                map.put("orderId", orderCondition.getOrderId());
                map.put("appAbnormalyFlag", 1);//异常
                map.put("autoGradeFlag", 2);//标记用户已回复
                map.put("updateBy", user);
                map.put("updateDate", date);
                try {
                    CompletableFuture.supplyAsync(() -> {
                        orderService.updateOrderCondition(map);
                        return true;
                    })
                            .exceptionally(t -> {
                                log.error("[autoGradeOne] Async update order error:",t);
                                return null;
                            }).get();
                } catch (Exception e1) {
                    log.error("[autoGradeOne] Async update order error:",e1);
                }
            }
            if (null != savemessage) {
                savemessage.setStatus(40);
                savemessage.setRemarks(e.getMessage());
                try {
                    CompletableFuture.supplyAsync(() -> {
                        message2Dao.insert(savemessage);//保存消息
                        return true;
                    })
                            .exceptionally(t -> {
                                log.error("[autoGradeOne] Async save message error:",t);
                                //System.out.println("Unexpected error:" + t);
                                return null;
                            }).get();
                } catch (Exception e1) {
                    log.error("[autoGradeOne] Async save message error:",e1);
                }
            }
            //LogUtils.saveLog("短信平台错误", "MessageService.autoGrade", savemessage.toString() + "order id:" + (order == null ? "" : order.getId().toString()), e, user);
        }

    }

    */

    /**
     * 取得待客评订单
     * 1.先待客评(grade_flag=2) -> grade_flag =0 && sub_status = 70
     * 2.未客评且已上门(grade_flag=0)
     * 2.未客评(grade_flag=0)
     * 3.已客评
     * 按订单id倒序，优先最新的订单
     * @param orders
     * @return
     */
    private OrderCondition getToGradeOrder(List<OrderCondition> orders){
        if(orders==null || orders.size()==0){
            return null;
        }
        if(orders.size()==1){
            return orders.get(0);
        }
        /*grade_flag = 2
        OrderCondition order = orders.stream().filter(t->t.getGradeFlag()==2 && Integer.parseInt(t.getStatus().getValue())<Order.ORDER_STATUS_COMPLETED)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        */
        //待回访,sub_status=70
        OrderCondition order = orders.stream().filter(t->t.getGradeFlag()==0 && t.getSubStatus() == Order.ORDER_SUBSTATUS_APPCOMPLETED)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        if(order != null){
            return order;
        }
        //grade_flag = 0 且 上门服务
        //TODO: APP完工[55]
//        order = orders.stream().filter(t->t.getGradeFlag()==0 && t.getStatusValue() == Order.ORDER_STATUS_SERVICED)
//                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
//                .findFirst().orElse(null);
        order = orders.stream().filter(t->t.getGradeFlag()==0 && (t.getStatusValue() == Order.ORDER_STATUS_SERVICED || t.getStatusValue() == Order.ORDER_STATUS_APP_COMPLETED))
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        if(order != null){
            return order;
        }
        //grade_flag = 0
        order = orders.stream().filter(t->t.getGradeFlag()==0)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        if(order != null){
            return order;
        }
        order = orders.stream().filter(t->t.getGradeFlag() > 0)
                .sorted(Comparator.comparing(OrderCondition::getOrderId).reversed())
                .findFirst().orElse(null);
        return order;
    }
    /**
     * 短信评分 转 评价内容
     * @param score
     * @return
     */
    private String gradeScoreToContent(Integer score,String text){
        String content = new String("");
        switch (score){
            case 1:
                content= MessageFormat.format("{0}，对师傅的服务满意",text);
                break;
            case 2:
                content= MessageFormat.format("{0},对师傅的服务评价一般",text);
                break;
            case 3:
                content= MessageFormat.format("{0}，对师傅的服务不满意",text);
                break;
            case 4:
                content= MessageFormat.format("{0}，还有部分产品未完成",text);
                break;
            default:
                content= MessageFormat.format("{0}",text);
                break;
        }
        return content;
    }

    /**
     * 发送提醒消息(APP异常)
     * @param orderId
     * @param quarter
     * @param customerId
     * @param areaId
     * @param kefuId
     * @param date
     * @param user
     */
    private void sendAppNoticeMessage(Long orderId,String quarter,Long customerId,Long areaId,Long kefuId,Date date,User user){
        MQNoticeMessage.NoticeMessage noticeMessage = MQNoticeMessage.NoticeMessage.newBuilder()
                .setNoticeType(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY)
                .setOrderId(orderId)
                .setQuarter(quarter)
                .setCustomerId(customerId)
                .setKefuId(kefuId!=null?kefuId:0l)
                .setAreaId(areaId)
                .setTriggerDate(date.getTime())
                .setTriggerBy(MQWebSocketMessage.User.newBuilder()
                        .setId(user.getId())
                        .setName(user.getName())
                        .build()
                )
                .setDelta(1)
                .build();
        noticeMessageSender.send(noticeMessage);
    }

    //region api functions

    /**
     * 获取短信验证码
     * @param verifyCode 0：注册，1：重置密码
     * @return
     */
    public RestResult<Object> getVerifyCode(RestGetVerifyCode verifyCode){
        RestEnum.VerifyCodeType verifyCodeType = RestEnum.VerifyCodeType.valueOf(RestEnum.VerifyCodeTypeString[verifyCode.getType()]);
        if (verifyCodeType == RestEnum.VerifyCodeType.saveServicePointBankAccountInfo) {
            Random random = new Random();
            String strCode = String.valueOf(random.nextInt(999999) + 1000000).substring(1);
            String strContent = "您正在修改快可立全国联保网点的银行账号信息,验证码为:" + strCode + ",如非本人操作,请忽略";
            // 使用新的短信发送方法 2019/02/28
//                smsMQSender.send(verifyCode.getPhone(),strContent.toString(),"",0,System.currentTimeMillis());
            //TODO: 短信类型
            smsMQSender.sendNew(verifyCode.getPhone(), strContent.toString(), "", 0, System.currentTimeMillis(), SysSMSTypeEnum.VERIFICATION_CODE);
            //缓存验证码，修改网点的银行账号信息时验证
            String verifyCodeCacheKey = String.format(VERCODE_KEY, verifyCodeType.ordinal(), verifyCode.getPhone());
            if (redisUtils.exists(REDIS_TEMP_DB, verifyCodeCacheKey)) {
                redisUtils.remove(REDIS_TEMP_DB, verifyCodeCacheKey);
            }
            redisUtils.set(REDIS_TEMP_DB, verifyCodeCacheKey, strCode, 5 * 60);
        }
        else {
            Integer delFlag = userDao.getDelFlagByMobile(verifyCode.getPhone());
            //用户不存在
            if (delFlag == null) {
                if (verifyCodeType == RestEnum.VerifyCodeType.register) {
                    Random random = new Random();
                    String strCode = String.valueOf(random.nextInt(999999) + 1000000).substring(1);

                    StringBuilder strContent = new StringBuilder();
                    strContent.append("您正在注册快可立全国联保账号,验证码为:" + strCode + ",如非本人操作,请忽略");
                    // 使用新的短信发送方法 2019/02/28
//                smsMQSender.send(verifyCode.getPhone(),strContent.toString(),"",0,System.currentTimeMillis());
                    //TODO: 短信类型
                    smsMQSender.sendNew(verifyCode.getPhone(), strContent.toString(), "", 0, System.currentTimeMillis(), SysSMSTypeEnum.VERIFICATION_CODE);
                    //缓存验证码，注册时验证
                    String verifyCodeCacheKey = String.format(VERCODE_KEY, verifyCodeType.ordinal(), verifyCode.getPhone());
                    if (redisUtils.exists(REDIS_TEMP_DB, verifyCodeCacheKey)) {
                        redisUtils.remove(REDIS_TEMP_DB, verifyCodeCacheKey);
                    }
                    redisUtils.set(REDIS_TEMP_DB, verifyCodeCacheKey, strCode, 5 * 60);
                } else if (verifyCodeType == RestEnum.VerifyCodeType.resetPassword) {
                    return RestResultGenerator.custom(ErrorCode.MEMBER_PHONE_NOT_EXIST.code, ErrorCode.MEMBER_PHONE_NOT_EXIST.message);
                }
            }
            //用户存在
            else {
                if (verifyCodeType == RestEnum.VerifyCodeType.register) {
                    return RestResultGenerator.custom(ErrorCode.MEMBER_PHONE_REGISTERED.code, ErrorCode.MEMBER_PHONE_REGISTERED.message);
                } else if (verifyCodeType == RestEnum.VerifyCodeType.resetPassword) {
                    if (delFlag.equals(1)) {
                        return RestResultGenerator.custom(ErrorCode.MEMBER_ENGINEER_NO_EXSIT.code, ErrorCode.MEMBER_ENGINEER_NO_EXSIT.message);
                    }
                    Random random = new Random();
                    String strCode = String.valueOf(random.nextInt(99999) + 100000);

                    StringBuilder strContent = new StringBuilder();
                    strContent.append("您正在进行找回密码操作,验证码为:" + strCode + ",如非本人操作,请忽略");
                    // 使用新的短信发送方法 2019/02/28
//                smsMQSender.send(verifyCode.getPhone(),strContent.toString(),"",0,System.currentTimeMillis());
                    //TODO: 短信类型
                    smsMQSender.sendNew(verifyCode.getPhone(), strContent.toString(), "", 0, System.currentTimeMillis(), SysSMSTypeEnum.VERIFICATION_CODE);
                    //缓存验证码，重置密码时验证
                    String verifyCodeCacheKey = String.format(VERCODE_KEY, verifyCodeType.ordinal(), verifyCode.getPhone());
                    if (redisUtils.exists(REDIS_TEMP_DB, verifyCodeCacheKey)) {
                        redisUtils.remove(REDIS_TEMP_DB, verifyCodeCacheKey);
                    }
                    redisUtils.set(REDIS_TEMP_DB, verifyCodeCacheKey, strCode, 5 * 60);
                }
            }
        }
        return RestResultGenerator.success();
    }
    //endregion api functions
}
