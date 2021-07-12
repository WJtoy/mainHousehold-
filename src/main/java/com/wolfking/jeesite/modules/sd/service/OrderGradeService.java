package com.wolfking.jeesite.modules.sd.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.entity.voiceservice.CallbackType;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.GradeService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.service.AbnormalFormService;
import com.wolfking.jeesite.ms.validate.service.MSOrderValidateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单客评服务
 * @autor Ryan Lu
 * @date 2019/1/18 2:07 PM
 */
@Configurable
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderGradeService extends LongIDBaseService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private OrderVoiceTaskService orderVoiceTaskService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private OrderMaterialReturnService orderMaterialReturnService;

    @Autowired
    private OrderOpitionTraceService orderOpitionTraceService;

    @Autowired
    private AbnormalFormService abnormalFormService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private MSOrderValidateService msOrderValidateService;

    @Value("${site.code}")
    private String siteCode;

    //region 公共方法

    private final List<OrderGrade> gradeItems = Lists.newArrayList();

    /**
     * 检查是否可以自动客评
     * @return NameValuePair
     *  0:可自动客评
     *  1:不标记异常，不统计异常：已标记异常
     *  2:不标记异常，不统计异常：订单读取错误，已客评,或已对账
     *  3:标记异常，并统计异常：不可客评
     *
     *  2019-09-19 增加处理：有远程费和其他费用不能自动客评，返回：3
     *  2021-04-22 增加判断：品类的autoGradeFlag属性判断，1：可客评
     */
    public NameValuePair canAutoGrade(Order order){
        NameValuePair checkResult;
        // 0. 判断工单是否挂起：挂起工单不允许客评
        if (order.getOrderCondition().getSuspendFlag() == OrderSuspendFlagEnum.SUSPENDED.getValue()) {
            checkResult = new NameValuePair("挂起工单不允许自动对帐","2");
            return checkResult;
        }
        // 1.是否可以人工客评
        checkResult = canGrade(order);
        if(!checkResult.getValue().equals("0")){
            return checkResult;
        }

        OrderCondition orderCondition = order.getOrderCondition();
        //判断品类：autoGradeFlag
        ProductCategory category = orderCondition.getProductCategory();
        if(category == null){
            checkResult = new NameValuePair("该工单产品品类自动客评开关：未开启","3");
            return checkResult;
        }else if(category.getAutoGradeFlag() == 0){
            checkResult = new NameValuePair(StrUtil.format("该工单产品品类[{}]自动客评开关：未开启",category.getName()),"3");
            return checkResult;
        }
        // 非待客评状态(待客评状态:2)
        /*
        if (orderCondition.getGradeFlag() == 0) {
            checkResult.setName("非待客评状态");
            return checkResult;
        }
        */
        // 2.更改为判断subStatus是否是待回访/70
        if (orderCondition.getSubStatus() != Order.ORDER_SUBSTATUS_APPCOMPLETED) {
            checkResult.setValue("3");
            checkResult.setName("非待回访状态");
            return checkResult;
        }

        /* cancel at 2019/01/18 ryan
        检查是否可自动客评,不能自动生成对账单的，不能自动客评
        Boolean canGrade = orderService.compareOrderDetail(order);
        if (!canGrade) {
            appFlag = true;
            checkResult.setName("此订单不符合自动客评要求");
            return checkResult;
        }*/

        //2.订单项目数=0
        List<OrderDetail> details = order.getDetailList();
        if(ObjectUtils.isEmpty(details)){
            checkResult.setValue("3");
            checkResult.setName("无上门服务单，不能自动完工");
            return checkResult;
        }

        // 3.检查安装服务类型
        Map<Long, ServiceType> azServiceTypeMap = null;
        List<ServiceType> azServiceTypes = serviceTypeService.findListOfOrderType(1);
        if(!CollectionUtils.isEmpty(azServiceTypes)) {
            azServiceTypeMap = azServiceTypes.stream().collect(Collectors.toMap(ServiceType::getId, item -> item));
        }
        if(azServiceTypeMap == null){
            checkResult.setValue("3");
            checkResult.setName("非安装上门服务单，不能自动完工");
            return checkResult;
        }
        OrderDetail detail;
        //2019-09-19 有远程费和其他费用
        for (int i = 0, size = details.size(); i < size; i++) {
            detail = details.get(i);
            if (detail.getMaterialCharge() > 0 || detail.getOtherCharge() > 0
                    || detail.getTravelCharge() > 0 || detail.getExpressCharge() > 0) {
                checkResult.setValue("3");
                checkResult.setName("实际服务有配件费/其他费用/远程费/快递费，不能自动完工");
                return checkResult;
            }
            if (detail.getEngineerMaterialCharge() > 0 || detail.getEngineerOtherCharge() > 0 ||
                    detail.getEngineerTravelCharge() > 0 || detail.getEngineerExpressCharge() > 0) {
                checkResult.setValue("3");
                checkResult.setName("实际服务有配件费/其他费用/远程费/快递费，不能自动完工");
                return checkResult;
            }
        }
        // 是否有非安装上门服务
        // 非安装单，其中有一项不是安装(II,OI)就是非安装单，不能自动结账
        boolean hasExceptInstallService = false;
        for(int j=0,size=details.size();j<size;j++){
            detail = details.get(j);
            if(!azServiceTypeMap.containsKey(detail.getServiceType().getId())){
                hasExceptInstallService = true;
                break;
            }
        }
        //1.非安装单
        if (hasExceptInstallService) {
            checkResult.setValue("3");
            checkResult.setName("非安装上门服务单，不能自动完工");
            return checkResult;
        }

        checkResult.setValue("0");
        return checkResult;
    }

    /**
     * 检查是否可以客评
     * @return NameValuePair
     *  0:可以客评
     *  1:不标记异常，不统计异常：已标记异常
     *  2:不标记异常，不统计异常：订单读取错误，已客评,或已对账
     *  3:标记异常，并统计异常：不可客评
     *
     *  2020-09-25 云米：增加[完工]的判断
     *  2021-01-25 好评单未审核：不能客评 2  2021/03/06 commented
     *  2021-04-23 增加：品类强制App完工检查
     */
    public NameValuePair canGrade(Order order){
        NameValuePair checkResult = new NameValuePair("","3");
        // 1.工单检查
        if (null == order || null == order.getOrderCondition()) {
            checkResult.setValue("2");
            checkResult.setName("读取工单失败");
            return checkResult;
        }

        OrderCondition orderCondition = order.getOrderCondition();

        // 2.已客评
        if (orderCondition.getGradeFlag() > 0) {
            checkResult.setValue("2");
            checkResult.setName("此工单已客评");
            return checkResult;
        }
        // 3.工单状态是否是：已上门/50
        // APP完工[55]
        int status = orderCondition.getStatusValue();
        if (status != Order.ORDER_STATUS_SERVICED && status != Order.ORDER_STATUS_APP_COMPLETED){
            checkResult.setValue("1");
            checkResult.setName("此工单不是：已上门 状态，不能客评");
            return checkResult;
        }

        // 4.订单已标记为：app异常单
        if (orderCondition.getAppAbnormalyFlag() == 1) {
            checkResult.setValue("1");
            checkResult.setName("APP异常还未处理，不能客评");
            return checkResult;
        }

        // 5.是否已对账
        if(orderCondition.getChargeFlag() == 1){
            checkResult.setValue("2");
            checkResult.setName("此工单已生成对账单，请联系管理员");
            return checkResult;
        }

        // 6.无上门服务
        if (order.getDetailList().size() == 0) {
            checkResult.setName("无上门服务");
            return checkResult;
        }
        // 6.1 挂起类型为鉴定的工单必须存在鉴定上门服务项目，否则不允许客评
        if (orderCondition.getSuspendFlag() == OrderSuspendFlagEnum.SUSPENDED.getValue() && orderCondition.getSuspendType() == OrderSuspendTypeEnum.VALIDATE.getValue()) {
            if (!msOrderValidateService.hasValidateServiceType(orderCondition.getOrderServiceType(), order.getDetailList())) {
                checkResult.setValue("2");
                checkResult.setName("因鉴定操作而挂起的工单缺少鉴定服务项目");
                return checkResult;
            }
        }

        /*检查好评单状态，有好评单且未审核，不能客评 2021-01-25
        OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(orderCondition.getOrderId(), orderCondition.getQuarter(), true);
        if (orderStatusFlag == null) {
            checkResult.setValue("2");
            checkResult.setName("读取并确认好评信息时失败");
            return checkResult;
        }
        PraiseStatusEnum praiseStatusEnum = PraiseStatusEnum.fromCode(orderStatusFlag.getPraiseStatus());
        if (praiseStatusEnum != null && (praiseStatusEnum == PraiseStatusEnum.NEW || praiseStatusEnum == PraiseStatusEnum.PENDING_REVIEW) ) {
            checkResult.setValue("2");
            checkResult.setName("订单已申请了好评单，且状态是: " + praiseStatusEnum.msg+",不能客评.");
            return checkResult;
        }*/

        // 7.检查金额是否有问题
        Boolean checkFee = orderService.checkOrderFeeAndServiceAmountBeforeGrade(order, true);
        if (!checkFee) {
            checkResult.setName("此订单金额异常不能自动完工");
            return checkResult;
        }

        // 2020-09-25 云米：增加[完工]的判断
        int dataSource = order.getDataSourceId();
        if(dataSource == B2BDataSourceEnum.VIOMI.getId() && StringUtils.isBlank(orderCondition.getAppCompleteType())) {
            checkResult.setValue("2");
            checkResult.setName("此订单需先[完工]，然后方可[客评]");
            return checkResult;
        }

        //品类App完工要求 2021-04-23
        int appCompleteFlag = Optional.ofNullable(orderCondition.getProductCategory()).map(p->p.getAppCompleteFlag()).orElse(0);
        if(appCompleteFlag == 1 && status != Order.ORDER_STATUS_APP_COMPLETED ){
            checkResult.setValue("2");
            checkResult.setName("此订单需先[完工]，然后方可[客评]");
            return checkResult;
        }

        // 8.检查完成照片
        Customer customer = customerService.getFromCache(orderCondition.getCustomer().getId());
        if (null != customer && customer.getMinUploadNumber()>0 && orderCondition.getFinishPhotoQty()<customer.getMinUploadNumber()) {
            checkResult.setName(MessageFormat.format("此订单客户已设置必须上传 {0}~{1} 张图片,请在上门服务界面去添加附件图片", customer.getMinUploadNumber(),customer.getMinUploadNumber()));
            return checkResult;
        }

        // 9.检查是否上传条码
        Boolean checkSNResult = orderService.checkOrderProductBarCode(order.getId(), order.getQuarter(), customer.getId(), order.getDetailList());
        if(!checkSNResult){
            checkResult.setName("该厂商要求上传产品序列号，请检查是否已上传");
            return checkResult;
        }

        // 10.1.检查配件是否都已处理
        Integer qty = orderMaterialService.getNoApprovedMaterialMasterQty(order.getId(), order.getQuarter());
        if (qty != null && qty > 0) {
            checkResult.setName("此单还有配件申请未通过审核或未返件");
            return checkResult;
        }
        // 10.2.检查返件申请单：未发货
        qty = orderMaterialReturnService.getNoApprovedMaterialReturnQty(orderCondition.getOrderId(), orderCondition.getQuarter());
        if (qty != null && qty > 0) {
            checkResult.setName("此单还有返件申请待发货");
            return checkResult;
        }
        checkResult.setValue("0");
        return checkResult;
    }

    /**
     * 回访失败-转到回访失败列表
     * 并根据情况 统计异常消息
     */
    //@Transactional(readOnly = true)
    public void followUp(CallbackType callbackType, OrderCondition orderCondition, NameValuePair checkResult, User user, Date date){
        String callbackTypeName = "语音";
        if(callbackType == CallbackType.SMS){
            callbackTypeName = "短信";
        }
        boolean isSuccess = true;
        //日志
        OrderProcessLog followUpLog = new OrderProcessLog();
        followUpLog.setOrderId(orderCondition.getOrderId());
        followUpLog.setQuarter(orderCondition.getQuarter());
        followUpLog.setStatus(orderCondition.getStatus().getLabel());
        followUpLog.setStatusValue(orderCondition.getStatusValue());
        followUpLog.setAction(MessageFormat.format("{0}回访-标记为回访失败",callbackTypeName));
        followUpLog.setActionComment(StringUtils.left(MessageFormat.format("{0}回访-标记为回访失败，原因:{1}", callbackTypeName,checkResult.getName()), 200));
        followUpLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
        followUpLog.setCloseFlag(0);
        followUpLog.setCreateBy(user);
        followUpLog.setCreateDate(date);
        followUpLog.setCustomerId(orderCondition.getCustomerId());
        try {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("subStatus", Order.ORDER_SUBSTATUS_FOLLOWUP_FAIL);
            params.put("orderId", orderCondition.getOrderId());
            params.put("quarter", orderCondition.getQuarter());
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderService.updateOrderCondition(params);

            orderService.saveOrderProcessLogNew(followUpLog);
        }catch (Exception e){
            isSuccess = false;
            log.error("[{}回访:回调处理失败]回访失败 orderId:{} ,checkReulst:{}", callbackTypeName,orderCondition.getOrderId(),checkResult.getName(), e);
        }

        if(isSuccess) {
            //意见跟踪日志
            try {
                AppFeedbackEnum.Channel channel = callbackType == CallbackType.SMS ? AppFeedbackEnum.Channel.SMS : AppFeedbackEnum.Channel.VOICE;
                OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                        .channel(channel.getValue())
                        .quarter(orderCondition.getQuarter())
                        .orderId(orderCondition.getOrderId())
                        .servicePointId(orderCondition.getServicePoint().getId())
                        .appointmentAt(0)
                        .opinionId(0)
                        .parentId(0)
                        .opinionType(AppFeedbackEnum.FeedbackType.SMS_GRADE.getValue())
                        .opinionValue(0)
                        .opinionLabel(followUpLog.getActionComment())
                        .isAbnormaly(1)
                        .remark("短信客评，订单标记为APP异常")
                        .createAt(System.currentTimeMillis())
                        .createBy(user)
                        .times(1)
                        .totalTimes(1)
                        .build();
                orderOpitionTraceService.insert(opitionTrace);
                Order order = new Order();
                order.setOrderCondition(orderCondition);
                AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order, followUpLog.getActionComment(), user, channel.getValue(),
                        AbnormalFormEnum.FormType.SMS.code, AbnormalFormEnum.SubType.MSM_GRADE.code, "");
                if (abnormalForm != null) {
                    abnormalForm.setOpinionLogId(opitionTrace.getId());
                    abnormalFormService.save(abnormalForm);
                }
            } catch (Exception e) {
                log.error("[{}回访:回调处理失败]记录异常单错误 orderId:{} ,label:{}", callbackTypeName, orderCondition.getOrderId(), followUpLog.getActionComment(), e);
            }
        }
        //异常消息统计
        if (checkResult.getValue().equals("4")) {
            try {
                orderService.sendAppNoticeMessage(
                        orderCondition.getOrderId(),
                        orderCondition.getQuarter(),
                        orderCondition.getCustomer().getId(),
                        orderCondition.getArea().getId(),
                        orderCondition.getKefu() != null ? orderCondition.getKefu().getId() : 0l,
                        date,
                        user
                );
            } catch (Exception e) {
                log.error("[{}回访:回调处理失败]sendAppNoticeMessage orderId:{}", callbackTypeName, orderCondition.getOrderId(), e);
            }
        }
        //region 网点订单数据更新 2019-03-25
        int statusValue = orderCondition.getStatus().getIntValue();
        //状态是上门服务之前的才同步
        if(isSuccess && statusValue <= Order.ORDER_SUBSTATUS_SERVICED.intValue()){
            servicePointOrderBusinessService.orderStatusUpdate(
                    MQOrderServicePointMessage.OperationType.FollowUpFail_VALUE,
                    orderCondition.getOrderId(),
                    orderCondition.getQuarter(),
                    orderCondition.getServicePoint().getId(),
                    statusValue,
                    Order.ORDER_SUBSTATUS_FOLLOWUP_FAIL,
                    -1,
                    false,
                    null,
                    user.getId(),
                    date.getTime()
            );
        }
        //endregion
    }

    //endregion

    //region 语音回访

    /**
     * 短信/语音回访自动客评
     * @param order 订单
     * @param msgJson 消息json内容
     */
    //@Transactional(readOnly = true)
    public void autoGradeForCallback(Order order, User user, String msgJson, OrderUtils.OrderGradeType gradeType){
        if(order == null || order.getOrderCondition() == null){
            return;
        }
        OrderCondition orderCondition = null;
        if(user == null) {
            user = new User(2l, "语音回访", "");
        }
        try {
            orderCondition = order.getOrderCondition();
            final String quarter = orderCondition.getQuarter();
            if(gradeItems.isEmpty()) {
                List<Grade> gradeList = gradeService.findAllListCache();//all
                if (gradeList == null || gradeList.isEmpty()) {
                    log.error("[语音/客评回访:回调处理失败-无客评项]，msg:{}", msgJson);
                    return;
                }

                //List<OrderGrade> gradeItems = Lists.newArrayList();
                gradeList.stream().forEach(grade -> {
                    GradeItem item = grade.getItemList().stream().sorted(Comparator.comparing(GradeItem::getPoint).reversed())
                            .findFirst().orElse(null);
                    if (item != null) {
                        OrderGrade orderGrade = new OrderGrade();
                        orderGrade.setGradeId(grade.getId());
                        orderGrade.setGradeName(grade.getName());
                        orderGrade.setGradeItemId(item.getId());
                        orderGrade.setGradeItemName(item.getRemarks());
                        orderGrade.setPoint(item.getPoint());
                        orderGrade.setSort(grade.getSort());
                        gradeItems.add(orderGrade);
                    }
                });
            }
            OrderGradeModel gradeModel = new OrderGradeModel();
            gradeModel.setCheckOrderFee(false);//已经检查过了
            //gradeModel.setCheckCanAutoCharge(true);//检查能否自动生成对账单 2020-03-16
            //gradeModel.setCanAutoCharge(true);//自动生成对账单
            List<OrderGrade> gItems = Lists.newArrayList();
            BeanUtils.copyProperties(gradeItems,gItems);//copy
            gItems.stream().forEach(t -> {
                t.setQuarter(quarter);
            });
            gradeModel.setGradeList(gItems);
            gradeModel.setAutoGradeFlag(gradeType.getValue());//回访类型

            Engineer engineer = new Engineer();
            engineer.setId(order.getOrderCondition().getEngineer().getId());
            engineer.setName(order.getOrderCondition().getEngineer().getName());
            gradeModel.setEngineer(engineer);
            gradeModel.setOrder(order);//*
            gradeModel.setOrderNo(order.getOrderNo());
            gradeModel.setOrderId(orderCondition.getOrderId());
            gradeModel.setQuarter(orderCondition.getQuarter());
            gradeModel.setCreateBy(user);

            try {
                OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(order.getId(),order.getQuarter());
                orderService.saveGrade(gradeModel, orderStatusFlag,user, null, null);
            }catch (Exception e){
                log.error("[语音回访:回调处理失败-自动客评错误]，orderId:{}", order.getId(),e);
            }

        } catch (Exception e) {
            log.error("[语音回访:回调处理失败] orderId:{}",(order == null ? "" : order.getId().toString()),e);
        }
    }

    //endregion 语音回访

    //region 短信回访

    /**
     * 短信回访处理失败
     * @param order
     * @param processLog
     * @param checkResult
     * @param smsLabel
     * @param score
     * @param user
     * @param date
     * @throws Exception
     */
    @Transactional
    public void smsSaveFail(Order order, OrderProcessLog processLog, int checkResult, String smsLabel, int score, User user, Date date) throws Exception {

        OrderCondition orderCondition = order.getOrderCondition();
        Dict status = orderCondition.getStatus();
        StringBuilder actionComment = new StringBuilder();
        //记录订单日志
        if (processLog != null) {
            processLog.setStatus(status.getLabel());
            processLog.setStatusValue(Integer.parseInt(status.getValue()));
            processLog.setCustomerId(orderCondition.getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            actionComment.append(StringUtils.left(processLog.getActionComment(),250));
            processLog.setActionComment(actionComment.toString());
            orderService.saveOrderProcessLogNew(processLog);
        }else{
            actionComment.append(StringUtils.left(smsLabel,250));
        }
        // 回复：1，2，满意，非常满意 ，不标记异常 2019-11-12
        if (checkResult >1 && score > 2) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("quarter", order.getQuarter());
            map.put("orderId", order.getId());
            map.put("appAbnormalyFlag", 1);//异常
            map.put("updateBy", user);
            map.put("updateDate", date);
            orderService.updateOrderCondition(map);
            //意见跟踪日志
            OrderOpitionTrace opitionTrace = OrderOpitionTrace.builder()
                    .channel(AppFeedbackEnum.Channel.SMS.getValue())
                    .quarter(order.getQuarter())
                    .orderId(order.getId())
                    .servicePointId(orderCondition.getServicePoint().getId())
                    .appointmentAt(0)
                    .opinionId(0)
                    .parentId(0)
                    .opinionType(AppFeedbackEnum.FeedbackType.SMS.getValue())
                    .opinionValue(0)
                    .opinionLabel(actionComment.toString())
                    .isAbnormaly(1)
                    .remark("短信客评，订单标记为APP异常")
                    .createAt(System.currentTimeMillis())
                    .createBy(user)
                    .times(1)
                    .totalTimes(1)
                    .build();
            orderOpitionTraceService.insert(opitionTrace);
            AbnormalForm abnormalForm = abnormalFormService.handleAbnormalForm(order,actionComment.toString(),user,AppFeedbackEnum.Channel.SMS.getValue(),
                                                   AbnormalFormEnum.FormType.SMS.code,AbnormalFormEnum.SubType.MSM_GRADE.code,"");
            if(abnormalForm!=null){
                abnormalForm.setOpinionLogId(opitionTrace.getId());
                abnormalFormService.save(abnormalForm);
            }
            //异常消息统计
            try {
                orderService.sendAppNoticeMessage(
                        orderCondition.getOrderId(),
                        orderCondition.getQuarter(),
                        orderCondition.getCustomer().getId(),
                        orderCondition.getArea().getId(),
                        orderCondition.getKefu() != null ? orderCondition.getKefu().getId() : 0l,
                        date,
                        user
                );
            } catch (Exception e) {
                log.error("[smsSaveFail]sendAppNoticeMessage orderId:{}", orderCondition.getOrderId(), e);
            }
        }

        //取消智能回访
        if (StringUtils.isBlank(siteCode)) {
            return;
        }
        try {
            Integer taskResult = orderVoiceTaskService.getVoiceTaskResult(order.getQuarter(), order.getId());
            if (taskResult != null && taskResult == 0) {
                try {
                    orderService.stopVoiceOperateMessage(siteCode, order.getId(), order.getQuarter(), user.getName(), date);
                } catch (Exception e) {
                    log.error("收到短信-停止智能回访错误:" + order.getId(), e);
                }
            }//taskResult
        } catch (Exception e) {
            log.error("收到短信-停止智能回访错误:" + order.getId(), e);
        }
    }

    //endregion 短信回访
}
