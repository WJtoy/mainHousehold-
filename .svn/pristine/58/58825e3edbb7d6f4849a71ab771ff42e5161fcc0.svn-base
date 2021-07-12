package com.wolfking.jeesite.ms.cc.service;

import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.cc.AbnormalFormEnum;
import com.kkl.kklplus.entity.cc.vm.AbnormalFormSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAppFeedbackType;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.kkl.kklplus.entity.praise.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.mq.dto.MQNoticeMessage;
import com.wolfking.jeesite.modules.mq.dto.MQWebSocketMessage;
import com.wolfking.jeesite.modules.mq.sender.NoticeMessageSender;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.NoticeMessageItemVM;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
import com.wolfking.jeesite.ms.cc.entity.SubAbnormalType;
import com.wolfking.jeesite.ms.cc.entity.mapper.AbnormalFormModelMapper;
import com.wolfking.jeesite.ms.cc.feign.CCAbnormalFormFeign;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import com.wolfking.jeesite.ms.providermd.service.MSAppFeedbackTypeService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;


/**
 * 异常单服务层
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AbnormalFormService {

    @Autowired
    private CCAbnormalFormFeign ccAbnormalFormFeign;

    @Autowired
    private AreaService areaService;

    @Autowired
    private UserRegionService userRegionService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private NoticeMessageSender noticeMessageSender;

    @Autowired
    private MSAppFeedbackTypeService appFeedbackTypeService;

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSCustomerService msCustomerService;


    //region 操作
    /**
     *  添加异常单
     *  @param abnormalForm
     */
    public void save(AbnormalForm abnormalForm){
        long id = sequenceIdService.nextId();//2020/05/24
        abnormalForm.setId(id);
        MSResponse<Integer> msResponse = ccAbnormalFormFeign.save(abnormalForm);
        if(msResponse.getCode()>0){
            throw new RuntimeException(msResponse.getMsg());
        }
    }

    /**
     *  关闭异常单
     *  @param abnormalForm
     */
    @Transactional()
    public void closeAbnormalForm(AbnormalForm abnormalForm,User user){
        //获取该订单的异常单数量,如果小于等于1,这要关闭工单的异常
        // 审单异常与其他异常已经在微服务端分开统计数量
        int count = getCountByOrderId(abnormalForm.getOrderId(),abnormalForm.getQuarter(),abnormalForm.getFormType());
        if(count<=1){
            String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, abnormalForm.getOrderId());
            //获得锁
            Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
            if (!locked) {
                throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
            }
            try {
                Order o = orderService.getOrderById(abnormalForm.getOrderId(), "", OrderUtils.OrderDataLevel.CONDITION, true);
                if (o == null || o.getOrderCondition() ==null) {
                    throw new OrderException("读取订单信息失败。");
                }
                OrderCondition condition = o.getOrderCondition();
                Date date = new Date();
                HashMap<String, Object> params = Maps.newHashMap();
                params.put("quarter", o.getQuarter());
                params.put("orderId", abnormalForm.getOrderId());
                params.put("updateBy", user);
                params.put("updateDate", date);
                if(abnormalForm.getFormType().equals(AbnormalFormEnum.FormType.REVIEW_ABNORMALY.getCode())){
                    //审单异常
                    condition.setPendingFlag(Order.ORDER_PENDDING_FLAG_RENEW);
                    params.put("pendingFlag", condition.getPendingFlag());
                    orderDao.updateCondition(params);

                    //log
                    Dict status = condition.getStatus();
                    OrderProcessLog processLog = new OrderProcessLog();
                    processLog.setQuarter(o.getQuarter());
                    processLog.setAction("异常处理完成");
                    processLog.setOrderId(abnormalForm.getOrderId());
                    processLog.setActionComment(String.format("订单异常处理完成,处理人:%s", user.getName()));

                    processLog.setStatus(status.getLabel());
                    processLog.setStatusValue(Integer.parseInt(status.getValue()));
                    processLog.setStatusFlag(OrderProcessLog.OPL_SF_PENDINGED);
                    processLog.setCloseFlag(0);
                    processLog.setCreateBy(user);
                    processLog.setCreateDate(date);
                    processLog.setCustomerId(condition.getCustomerId());
                    processLog.setDataSourceId(o.getDataSourceId());
                    orderService.saveOrderProcessLogNew(processLog);
                    //调用公共缓存
                    OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
                    builder.setOpType(OrderCacheOpType.UPDATE)
                            .setOrderId(abnormalForm.getOrderId())
                            .incrVersion(1L)
                            .setCondition(condition)
                            .setPendingFlag(Order.ORDER_PENDDING_FLAG_RENEW)
                            .setExpireSeconds(0L);
                    OrderCacheUtils.update(builder.build());
                }else{
                    // 非审单异常
                    if (condition.getAppAbnormalyFlag() == 1) {
                        //condition
                        condition.setAppAbnormalyFlag(0);
                        condition.setUpdateDate(date);
                        condition.setUpdateBy(user);
                        params.put("appAbnormalyFlag", 0);
                        orderDao.updateCondition(params);

                        //同步网点工单数据
                        Long spId = Optional.ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
                        servicePointOrderBusinessService.abnormalyFlag(
                                abnormalForm.getOrderId(),
                                o.getQuarter(),
                                spId,
                                0,
                                user.getId(),
                                date.getTime()
                        );

                        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
                        builder.setOpType(OrderCacheOpType.UPDATE)
                                .setOrderId(abnormalForm.getOrderId())
                                .incrVersion(1L)
                                .setSyncDate(new Date().getTime())
                                .setCondition(condition)
                                .setExpireSeconds(0L);
                        OrderCacheUtils.update(builder.build());

                        try {
                            MQNoticeMessage.NoticeMessage message = MQNoticeMessage.NoticeMessage.newBuilder()
                                    .setOrderId(condition.getOrderId())
                                    .setQuarter(condition.getQuarter())
                                    .setNoticeType(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY)
                                    .setCustomerId(condition.getCustomer().getId())
                                    .setKefuId(condition.getKefu() != null ? condition.getKefu().getId() : 0l)
                                    .setAreaId(condition.getArea().getId())
                                    .setTriggerBy(MQWebSocketMessage.User.newBuilder()
                                            .setId(user.getId())
                                            .setName(user.getName())
                                            .build()
                                    )
                                    .setTriggerDate(date.getTime())
                                    .setDelta(-1)
                                    .build();
                            try {
                                noticeMessageSender.send(message);
                            } catch (Exception e) {
                                //消息队列发送错误
                                log.error("[OrderService.dealAPPException] send MQNoticeMessage,orderId:{} ,user:{}", abnormalForm.getOrderId(), user.getId(), e);
                            }
                        } catch (Exception e) {
                            log.error("[OrderService.dealAPPException] send MQNoticeMessage,orderId:{} ,user:{}", abnormalForm.getOrderId(), user.getId(), e);
                        }
                    }
                }
            }catch (OrderException o){
                throw o;
            }catch (Exception e){
                throw new RuntimeException(e.getMessage(), e);
            }finally {
                if (locked && lockkey != null) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
                }
            }
        }
        MSResponse<Integer> msResponse = ccAbnormalFormFeign.closeAbnormalForm(abnormalForm);
        if(msResponse.getCode()>0){
            throw new RuntimeException(msResponse.getMsg());
        }
    }


    /**
     * 客服订单详情页面关闭订单异常，客服通过退单申请关闭异常
     * @param abnormalForm
     * @return
     */
    public void closeByOrderId(AbnormalForm abnormalForm){
        MSResponse<Integer> msResponse = ccAbnormalFormFeign.closeByOrderId(abnormalForm);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("根据订单关闭异常单失败.失败原因:" + msResponse.getMsg());
        }
    }

    /**
     * 批量添加异常单(财务对账批量标记异常)
     * @param abnormalFormList
     * @return
     */
    @Transactional()
    public void insertBatch(@RequestBody List<AbnormalForm> abnormalFormList){
        if(!ObjectUtils.isEmpty(abnormalFormList)){
            MSResponse<Integer> msResponse = new MSResponse<>(MSErrorCode.SUCCESS);
            if(abnormalFormList.size()>12){
                List<List<AbnormalForm>> list = Lists.partition(abnormalFormList,12);
                for (List<AbnormalForm> itemList: list){
                    //2020/05/24
                    for (AbnormalForm abnormalForm : itemList) {
                        long id = sequenceIdService.nextId();
                        abnormalForm.setId(id);
                    }
                    msResponse = ccAbnormalFormFeign.insertBatch(itemList);
                }
            }else{
                //2020/05/24
                for (AbnormalForm abnormalForm : abnormalFormList) {
                    long id = sequenceIdService.nextId();
                    abnormalForm.setId(id);
                }
                msResponse = ccAbnormalFormFeign.insertBatch(abnormalFormList);
            }
            if(!MSResponse.isSuccess(msResponse)){
                throw new RuntimeException("批量添加异常单失败.失败原因:" + msResponse.getMsg());
            }
        }
    }

    /**
     * 根据工单号关闭审单异常(客服异常处理列表关闭异常工单)
     * @param abnormalForm
     * @return
     */
    @Transactional()
    public void closeReviewAbnormal(AbnormalForm abnormalForm){
        MSResponse<Integer> msResponse = ccAbnormalFormFeign.closeReviewAbnormal(abnormalForm);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("关闭审单异常.失败原因:" + msResponse.getMsg());
        }
    }

    /**
     * 客户处理好评驳回-修改
     * @param praise
     * @param user
     */
    public void updatePraiseForKefu(Praise praise,User user){
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION,praise.getOrderId());
        //获得锁
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);//60秒
        if (!locked) {
            throw new RuntimeException("此好评单正在处理中，请稍候重试");
        }
        try {
            int currentStatus = praise.getStatus();
            Date date = new Date();
            PraiseLog praiseLog = new PraiseLog();
            praiseLog.setId(sequenceIdService.nextId());//2020/05/24
            if(praise.getStatus() == PraiseStatusEnum.REJECT.code){
                praise.setStatus(PraiseStatusEnum.PENDING_REVIEW.code);
            }
            praiseLog.setContent("【驳回后重新修改】好评费");
            praise.setUpdateBy(user.getName());
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setActionType(PraiseActionEnum.RESUBMIT.code);
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.KEFU.code);
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());
            praise.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.resubmit(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("修改好评费失败:" + msResponse.getMsg());
            }
            if(currentStatus == PraiseStatusEnum.REJECT.code){ //驳回后重新提交
                //更新好评单状态
                try {
                    servicePointOrderBusinessService.syncPraiseStatus(praise.getOrderId(),praise.getQuarter(),praise.getServicepointId(),
                            praise.getStatus(),praise.getUpdateById(),praise.getUpdateDt());
                }catch (Exception e){
                    log.error("发送消息队列更新好评单状态失败 form: {}", GsonUtils.toGsonString(praise),e);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }

    }

    /**
     * 客服取消好评单
     * @param praise
     * @param user
     */
    public void cancelled(Praise praise,User user){
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION,praise.getOrderId());
        //获得锁
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);//60秒
        if (!locked) {
            throw new RuntimeException("此好评单正在处理中，请稍候重试");
        }
        try {
            PraiseLog praiseLog = new PraiseLog();
            praiseLog.setId(sequenceIdService.nextId());//2020/05/24
            Date date = new Date();
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praise.setUpdateBy(user.getName());
            if(praise.getStatus()==PraiseStatusEnum.REJECT.code){
                praiseLog.setActionType(PraiseActionEnum.REJECT_TO_CANCELED.code);
            }else{
                praiseLog.setActionType(PraiseActionEnum.NEW_TO_CANCELED.code);
            }
            praise.setStatus(PraiseStatusEnum.CANCELED.code);
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setPraiseId(praise.getId());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            if(StringUtils.isNotBlank(praise.getRemarks())){
                praiseLog.setContent("【取消】好评单审核,描述:" + praise.getRemarks());
            }else {
                praiseLog.setContent("【取消】好评单审核");
            }
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.KEFU.code);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());
            praise.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.cancelled(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("取消好评单失败:" + msResponse.getMsg());
            }
            String content="您有好评单("+praise.getOrderNo()+")被取消,因为"+ praise.getRemarks();
            PushMessageUtils.pushPraiseMessage(praise.getServicepointId(),praise.getEngineerId(),"好评单取消",content);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    //endregion 操作

    //region 列表

    /**
     * 待处理列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> waitProcessList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.waitProcessList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                List<AbnormalFormModel> list = toList(data);
                abnormalFormModel(list);
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 已处理列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> processedList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.processedList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                List<AbnormalFormModel> list = toList(data);
                abnormalFormModel(list);
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * app异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> appAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.appAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = toList(data);
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                if(!ObjectUtils.isEmpty(list)){
                    List<MDAppFeedbackType> appFeedbackTypeList = appFeedbackTypeService.findAllList();
                    Map<Long,MDAppFeedbackType> appFeedbackTypeMap = Maps.newHashMap();
                    if(!ObjectUtils.isEmpty(appFeedbackTypeList)){
                        appFeedbackTypeList = appFeedbackTypeList.stream().filter(t->t.getParentId()==0L).collect(Collectors.toList());
                        appFeedbackTypeMap = appFeedbackTypeList.stream().collect(Collectors.toMap(MDAppFeedbackType::getId, a -> a,(k1,k2)->k1));
                    }
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(AbnormalFormModel entity:list){
                        MDAppFeedbackType appFeedbackType = appFeedbackTypeMap.get(entity.getSubType().longValue());
                        if(appFeedbackType!=null){
                            entity.setSubTypeName(appFeedbackType.getLabel());
                        }else{
                            entity.setSubTypeName("其他");
                        }
                        setFeedBackTimeliness(entity,date,startDt,endDt);
                    }
                }
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 审单异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> reviewAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.reviewAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = toList(data);
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                if(!ObjectUtils.isEmpty(list)){
                    Map<String,Dict> map = MSDictUtils.getDictMap("fi_charge_audit_type");
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(AbnormalFormModel entity:list){
                         Dict dict = map.get(String.valueOf(entity.getSubType()));
                         if(dict!=null){
                             entity.setSubTypeName(dict.getLabel());
                         }
                        setFeedBackTimeliness(entity,date,startDt,endDt);
                    }
                }
                //abnormalFormModel(list);
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * app完工异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> appCompleteAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.appCompleteAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = toList(data);
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                //abnormalFormModel(list);
                if(!ObjectUtils.isEmpty(list)){
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                     for(AbnormalFormModel entity:list){
                         AbnormalFormEnum.SubType subType = AbnormalFormEnum.SubType.fromCode(entity.getSubType());
                         if(subType!=null){
                             entity.setSubTypeName(subType.getMsg());
                         }
                         setFeedBackTimeliness(entity,date,startDt,endDt);
                     }
                }
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 短信异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> smsAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.smsAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = toList(data);
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                //abnormalFormModel(list);
                if(!ObjectUtils.isEmpty(list)){
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(AbnormalFormModel entity:list){
                        AbnormalFormEnum.SubType subType = AbnormalFormEnum.SubType.fromCode(entity.getSubType());
                        if(subType!=null){
                            entity.setSubTypeName(subType.getMsg());
                        }
                        setFeedBackTimeliness(entity,date,startDt,endDt);
                    }
                }
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }


    /**
     * 旧app异常列表
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> oldAppAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.oldAppAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = toList(data);
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                //abnormalFormModel(list);
                if(!ObjectUtils.isEmpty(list)){
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(AbnormalFormModel entity:list){
                        AbnormalFormEnum.SubType subType = AbnormalFormEnum.SubType.fromCode(entity.getSubType());
                        if(subType!=null){
                            entity.setSubTypeName(subType.getMsg());
                        }
                        setFeedBackTimeliness(entity,date,startDt,endDt);
                    }
                }
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 好评单异常
     * @param abnormalFormSearchModel
     * @return
     */
    public Page<AbnormalFormModel> praiseAbnormalList(Page<AbnormalFormModel> page,AbnormalFormSearchModel abnormalFormSearchModel){
        abnormalFormSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<AbnormalForm>> msResponse = ccAbnormalFormFeign.kefuPraiseRejectAbnormalList(abnormalFormSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<AbnormalForm> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<AbnormalFormModel> list = toList(data);
                //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList());
                if(list!=null && list.size()>0){
                    Map<String,Dict> dictList = MSDictUtils.getDictMap("praise_abnormal_type");
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for(AbnormalFormModel entity:list){
                        Dict dict = dictList.get(String.valueOf(entity.getSubType()));
                        if(dict!=null){
                            entity.setSubTypeName(dict.getLabel());
                        }
                        setFeedBackTimeliness(entity,date,startDt,endDt);
                    }
                }
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    //endregion 列表

    //region 其他

    /**
     * 根据订单id获取数量
     * @param orderId
     * @param quarter
     * @return
     */
    public Integer getCountByOrderId(Long orderId,String quarter,Integer formType){
        MSResponse<Integer> msResponse = ccAbnormalFormFeign.getCountByOrderId(orderId,quarter,formType);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return 0;
        }
    }

    /**
     * 客服处理好评驳回详情
     * @param orderId
     * @param quarter
     * @return
     */
    public ViewPraiseModel praiseInfoForKefu(Long orderId, String quarter,Long servicePointId){
        Praise praise = orderPraiseService.getByOrderId(quarter,orderId,servicePointId);
        if(praise!=null){
            ViewPraiseModel viewPraiseModel = Mappers.getMapper(PraiseModelMapper.class).PraiseToViewModel(praise);
            if(viewPraiseModel!=null){
                PraiseStatusEnum praiseStatusEnum = PraiseStatusEnum.fromCode(viewPraiseModel.getStatus());
                if(praiseStatusEnum!=null){
                    viewPraiseModel.setStrStatus(praiseStatusEnum.msg);
                }
                Customer customer = customerService.getFromCache(viewPraiseModel.getCustomerId());
                if(customer!=null){
                    viewPraiseModel.setCustomerName(customer.getName());
                }
                ServicePoint servicePoint = servicePointService.getFromCache(viewPraiseModel.getServicepointId());
                if(servicePoint!=null){
                    viewPraiseModel.setServicePointNo(servicePoint.getServicePointNo());
                    viewPraiseModel.setServicePointPhone(servicePoint.getContactInfo1());
                }
            }
            return viewPraiseModel;
        }else{
            return null;
        }
    }

    /**
     * 组装异常单信息
     * @param order
     * @param reason 异常原因
     * @param channel 来源
     * @param formType 异常类型
     * @param subType 子类型
     * @param remarks 描述
     * @return
     */
    public AbnormalForm handleAbnormalForm(Order order, String reason, User user,Integer channel,Integer formType,Integer subType,String remarks){
        OrderCondition orderCondition = order.getOrderCondition();
        if(orderCondition !=null){
            AbnormalForm abnormalForm = new AbnormalForm();
            long id = sequenceIdService.nextId();//2020/05/24
            abnormalForm.setId(id);
            String abnormalNo = SeqUtils.NextSequenceNo("abnormalNo",0,3);
            abnormalForm.setOrderId(orderCondition.getOrderId());
            abnormalForm.setOrderNo(orderCondition.getOrderNo());
            abnormalForm.setQuarter(orderCondition.getQuarter());
            abnormalForm.setAbnormalNo(abnormalNo);
            abnormalForm.setChannel(channel);
            abnormalForm.setFormType(formType);
            abnormalForm.setSubType(subType);
            abnormalForm.setCustomerId(orderCondition.getCustomer().getId());
            abnormalForm.setServicepointId(orderCondition.getServicePoint().getId());
            abnormalForm.setProductCategoryId(orderCondition.getProductCategoryId());
            abnormalForm.setKefuId(orderCondition.getKefu().getId());
            abnormalForm.setStatus(0);
            abnormalForm.setReason(StringUtils.left(reason,250));
            abnormalForm.setDescription(StringUtils.left(remarks,250));
            abnormalForm.setProvinceId(0L);
            abnormalForm.setCityId(0L);
            abnormalForm.setAreaId(orderCondition.getArea().getId());
            abnormalForm.setSubAreaId(orderCondition.getSubArea().getId());
            abnormalForm.setUserName(StringUtils.left(orderCondition.getUserName(),20));
            abnormalForm.setUserPhone(orderCondition.getServicePhone());
            abnormalForm.setUserAddress(orderCondition.getArea().getName() + orderCondition.getServiceAddress());
            if(orderCondition.getEngineer()!=null && orderCondition.getEngineer().getId()!=null){
                abnormalForm.setEngineerId(orderCondition.getEngineer().getId());
            }
            if(user !=null){
                abnormalForm.setCreateById(user.getId());
            }
            abnormalForm.setCreateDt(System.currentTimeMillis());
            Area area = areaService.getFromCache(orderCondition.getArea().getId());
            if (area != null) {
                List<String> ids = Splitter.onPattern(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(area.getParentIds());
                if (ids.size() >= 2) {
                    abnormalForm.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                    abnormalForm.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
                }
            }
            abnormalForm.setCanRush(orderCondition.getCanRush());
            abnormalForm.setKefuType(orderCondition.getKefuType());
            return abnormalForm;
        }else{
            return null;
        }
    }

    /**
     * 获取异常类型集合
     * @return
     */
    public List<AbnormalFormEnum.FormType> findFormTypeList(){
        List<AbnormalFormEnum.FormType> list = Lists.newArrayList();
        for(AbnormalFormEnum.FormType formType:AbnormalFormEnum.FormType.values()){
            list.add(formType);
        }
        return list;
    }

    /**
     * 获取异常分类集合
     * @return
     */
    public List<SubAbnormalType> findFormSubType(){
        List<SubAbnormalType> subAbnormalTypeList = Lists.newArrayList();
        SubAbnormalType subAbnormalType;
        List<MDAppFeedbackType> appFeedbackTypeList = appFeedbackTypeService.findAllList();
        if(!ObjectUtils.isEmpty(appFeedbackTypeList)){
            appFeedbackTypeList = appFeedbackTypeList.stream().filter(t->t.getParentId() == 0).collect(Collectors.toList());
            for(MDAppFeedbackType item:appFeedbackTypeList){
                subAbnormalType = new SubAbnormalType();
                subAbnormalType.setValue(item.getId().intValue());
                subAbnormalType.setLabel(item.getLabel());
                subAbnormalTypeList.add(subAbnormalType);
            }
        }
        List<Dict> list = MSDictUtils.getDictList("fi_charge_audit_type");
        if(!ObjectUtils.isEmpty(list)){
            for(Dict dict:list){
                subAbnormalType = new SubAbnormalType();
                subAbnormalType.setValue(Integer.valueOf(dict.getValue()));
                subAbnormalType.setLabel(dict.getLabel());
                subAbnormalTypeList.add(subAbnormalType);
            }
        }
        for (AbnormalFormEnum.SubType subType :AbnormalFormEnum.SubType.values()){
            if(subType.getCode()!=AbnormalFormEnum.SubType.NOT_CONTACT_USER.getCode() && subType.getCode()!=AbnormalFormEnum.SubType.FEE.getCode() &&
                    subType.getCode()!=AbnormalFormEnum.SubType.CANCEL.getCode() &&  subType.getCode()!=AbnormalFormEnum.SubType.MODIFY_PLAN.getCode() &&
                    subType.getCode()!=AbnormalFormEnum.SubType.OTHER.getCode()){
                subAbnormalType = new SubAbnormalType();
                subAbnormalType.setValue(subType.getCode());
                subAbnormalType.setLabel(subType.getMsg());
                subAbnormalTypeList.add(subAbnormalType);
            }
        }
        return subAbnormalTypeList;
    }

    /**
     * 获取异常分类集合
     * @return
     */
    public List<SubAbnormalType> findSubTypeByFormType(Integer formType){
        List<SubAbnormalType> subTypeList = Lists.newArrayList();
        SubAbnormalType subAbnormalType;
        if(formType.equals(AbnormalFormEnum.FormType.APP_ABNORMALY.getCode())){
            List<MDAppFeedbackType> list = appFeedbackTypeService.findAllList();
            if(!ObjectUtils.isEmpty(list)){
                list = list.stream().filter(t->t.getParentId()==0).collect(Collectors.toList());
                for(MDAppFeedbackType item:list){
                    subAbnormalType = new SubAbnormalType();
                    subAbnormalType.setValue(item.getId().intValue());
                    subAbnormalType.setLabel(item.getLabel());
                    subTypeList.add(subAbnormalType);
                }
            }
        }else if(formType.equals(AbnormalFormEnum.FormType.REVIEW_ABNORMALY.getCode())){
            List<Dict> list = MSDictUtils.getDictList("fi_charge_audit_type");
            if(!ObjectUtils.isEmpty(list)){
                for(Dict item:list){
                    subAbnormalType = new SubAbnormalType();
                    subAbnormalType.setValue(Integer.valueOf(item.getValue()));
                    subAbnormalType.setLabel(item.getLabel());
                    subTypeList.add(subAbnormalType);
                }
            }
        }
        return subTypeList;
    }

    /**
     * 获取类型,子类型名称
     * */
    public void abnormalFormModel(List<AbnormalFormModel> abnormalFormModels){
        if(abnormalFormModels!=null && abnormalFormModels.size()>0){
            Map<String,Dict> map = MSDictUtils.getDictMap("fi_charge_audit_type");
            Map<String,Dict> praiseType = MSDictUtils.getDictMap("praise_abnormal_type");
            List<MDAppFeedbackType> list = appFeedbackTypeService.findAllList();
            Map<Long,MDAppFeedbackType> appFeedbackTypeMap = Maps.newHashMap();
            if(!ObjectUtils.isEmpty(list)){
                list = list.stream().filter(t->t.getParentId()==0L && t.getIsAbnormaly()==1).collect(Collectors.toList());
                appFeedbackTypeMap = list.stream().collect(Collectors.toMap(MDAppFeedbackType::getId, a -> a,(k1,k2)->k1));
            }
            for(AbnormalFormModel item:abnormalFormModels){
                 AbnormalFormEnum.FormType formType = AbnormalFormEnum.FormType.fromCode(item.getFormType());
                 if(formType !=null){
                     item.setFromTypeName(formType.getMsg());
                     if(formType.getCode() == AbnormalFormEnum.FormType.REVIEW_ABNORMALY.getCode()){
                         Dict dict = map.get(item.getSubType().toString());
                         if(dict!=null){
                             item.setSubTypeName(dict.getLabel());
                         }
                     }else if(formType.getCode() == AbnormalFormEnum.FormType.APP_ABNORMALY.getCode()){
                         MDAppFeedbackType appFeedbackType = appFeedbackTypeMap.get(item.getSubType().longValue());
                         if(appFeedbackType!=null){
                             item.setSubTypeName(appFeedbackType.getLabel());
                         }else{
                             item.setSubTypeName("其他");
                         }
                     }else if(formType.getCode() == AbnormalFormEnum.FormType.PRAISE_ABNORMAL.getCode()){
                         Dict dict = praiseType.get(item.getSubType().toString());
                         item.setSubTypeName(dict.getLabel());
                     }else{
                         AbnormalFormEnum.SubType subType = AbnormalFormEnum.SubType.fromCode(item.getSubType());
                         if(subType!=null){
                             item.setSubTypeName(subType.getMsg());
                         }
                     }
                 }
                 //时效
                int minutes = (int)(60*item.getTimeLiness());
                item.setFeedBackTimeliness(DateUtils.minuteToTimeString(minutes,"小时","分钟"));

            }
        }
    }

    /**
     * 记载客服负责区域
     * */
    public String loadAreaForKefu(AbnormalFormSearchModel abnormalFormModel,User user){
        Date date;
        if (StringUtils.isBlank(abnormalFormModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            abnormalFormModel.setEndDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            abnormalFormModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            abnormalFormModel.setBeginDate(DateUtils.formatDate(date,"yyyy-MM-dd"));
            abnormalFormModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(abnormalFormModel.getBeginDate());
            abnormalFormModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(abnormalFormModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            abnormalFormModel.setEndDt(date.getTime());
        }
        boolean isKefu = user.isKefu();
        /*if(isKefu){
            if(user.getSubFlag().equals(KefuTypeEnum.VIPKefu.getCode())){
                if(CollectionUtils.isEmpty(user.getCustomerIds())) {
                    List<Long> customerIdList = systemService.findVipCustomerIdListByKefu(user.getId());
                    if (customerIdList == null || customerIdList.size() <= 0) {
                        return "错误：您的账号未设定VIP客户";
                    }
                    abnormalFormModel.setCustomerIdList(customerIdList);
                }else{
                    abnormalFormModel.setCustomerIdList(new ArrayList<Long>(user.getCustomerIds()));
                }
            }
        }*/
        //客服
        if(isKefu){
            if(user.getSubFlag().equals(KefuTypeEnum.VIPKefu.getCode())){
                if(CollectionUtils.isEmpty(user.getCustomerIds())) {
                    List<Long> customerIdList = systemService.findVipCustomerIdListByKefu(user.getId());
                    if (customerIdList == null || customerIdList.size() <= 0) {
                        return "错误：您的账号未设定VIP客户";
                    }
                    abnormalFormModel.setCustomerIdList(customerIdList);
                }else{
                    abnormalFormModel.setCustomerIdList(new ArrayList<Long>(user.getCustomerIds()));
                }
            }else if(user.getSubFlag().equals(KefuTypeEnum.Kefu.getCode())){
                abnormalFormModel.setCustomerType(0);//查询非vip客户的单据
            }
            List<UserRegion> regions = userRegionService.getUserRegions(user.getId());
            if(ObjectUtils.isEmpty(regions)){
                return "错误：您的账号未设定负责区域";
            }

            List<Long> userProductCategoryList = systemService.getAuthorizedProductCategoryIds(user.getId());
            if(ObjectUtils.isEmpty(userProductCategoryList)){
                return "错误:您未开通产品类目权限，请联系管理员";
            }
            //客服只能看到自己的品类
            if(!ObjectUtils.isEmpty(userProductCategoryList)){
                abnormalFormModel.setProductCategoryIds(userProductCategoryList);
            }

            Supplier<Stream<UserRegion>> streamSupplier = () -> regions.stream();
            long count = streamSupplier.get().filter(t->t.getAreaType() == 1).count();
            if(count>0){
                abnormalFormModel.setProvinceList(null);
                abnormalFormModel.setCityList(null);
                abnormalFormModel.setAreaList(null);
                return org.apache.commons.lang3.StringUtils.EMPTY;
            }
            List<Long> idList = null;
            //province
            idList = streamSupplier.get().filter(t->t.getAreaType() == 2).map(t->t.getProvinceId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                abnormalFormModel.setProvinceList(null);
            }else{
                abnormalFormModel.setRegionFilterCount(abnormalFormModel.getRegionFilterCount()+1);
                abnormalFormModel.setProvinceList(idList);
            }
            //city
            idList = streamSupplier.get().filter(t->t.getAreaType() == 3).map(t->t.getCityId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                abnormalFormModel.setCityList(null);
            }else{
                abnormalFormModel.setRegionFilterCount(abnormalFormModel.getRegionFilterCount()+1);
                abnormalFormModel.setCityList(idList);
            }
            //area
            idList = streamSupplier.get().filter(t->t.getAreaType() == 4).map(t->t.getAreaId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                abnormalFormModel.setAreaList(null);
            }else{
                abnormalFormModel.setRegionFilterCount(abnormalFormModel.getRegionFilterCount()+1);
                abnormalFormModel.setAreaList(idList);
            }
            //客服类型
            KefuTypeEnum kefuTypeEnum = KefuTypeEnum.fromCode(user.getSubFlag());
            if(kefuTypeEnum!=null){
                //abnormalFormModel.setCustomerType(kefuTypeEnum.getCustomerType());
                abnormalFormModel.setKefuType(kefuTypeEnum.getKefuType());
            }else{
                return "错误:读取客服类型错误";
            }
        }else{
            abnormalFormModel.setProvinceList(null);
            abnormalFormModel.setCityList(null);
            abnormalFormModel.setAreaList(null);

            abnormalFormModel.setCustomerType(null);
            abnormalFormModel.setKefuType(null);
        }

       /* if(isKefu) {
            if (user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
                //vip客服
                abnormalFormModel.setRushType(null);//忽略突击区域
            } else if (user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
                abnormalFormModel.setRushType(0);//排除突击区域订单
            } else if(user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
                //突击客服
                abnormalFormModel.setCustomerType(null);
                abnormalFormModel.setRushType(1);//查看突击区域订单
            } else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
                abnormalFormModel.setCustomerType(null);
                abnormalFormModel.setRushType(0);//排除突击区域订单
            }else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
                abnormalFormModel.setCustomerType(null);
                abnormalFormModel.setRushType(0);//排除突击区域订单
            }else {//超级客服
                abnormalFormModel.setRushType(null);//可查看突击区域订单
            }
        }else{
            //其他类型帐号，不限制客户
            abnormalFormModel.setRushType(null);//可查看突击区域订单
        }*/
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }

    /**
     * 记载网点负责区域
     * */
    public String loadAreaForServicePoint(AbnormalFormSearchModel abnormalFormModel,User user) {
        Date date;
        if (StringUtils.isBlank(abnormalFormModel.getBeginDate())) {
            date = DateUtils.getDateEnd(new Date());
            abnormalFormModel.setEndDate(DateUtils.formatDate(date, "yyyy-MM-dd"));
            abnormalFormModel.setEndDt(date.getTime());
            date = DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1));
            abnormalFormModel.setBeginDate(DateUtils.formatDate(date, "yyyy-MM-dd"));
            abnormalFormModel.setBeginDt(date.getTime());
        } else {
            date = DateUtils.parseDate(abnormalFormModel.getBeginDate());
            abnormalFormModel.setBeginDt(date.getTime());
            date = DateUtils.parseDate(abnormalFormModel.getEndDate());
            date = DateUtils.getDateEnd(date);
            abnormalFormModel.setEndDt(date.getTime());
        }
        //客服
        List<UserRegion> regions = userRegionService.getUserRegions(user.getId());
        if (ObjectUtils.isEmpty(regions)) {
            return "错误：您的账号未设定负责区域";
        }

        List<Long> userProductCategoryList = systemService.getAuthorizedProductCategoryIds(user.getId());
        if (ObjectUtils.isEmpty(userProductCategoryList)) {
            return "错误:您未开通产品类目权限，请联系管理员";
        }
        //客服只能看到自己的品类
        if (!ObjectUtils.isEmpty(userProductCategoryList)) {
            abnormalFormModel.setProductCategoryIds(userProductCategoryList);
        }

        Supplier<Stream<UserRegion>> streamSupplier = () -> regions.stream();
        long count = streamSupplier.get().filter(t -> t.getAreaType() == 1).count();
        if (count > 0) {
            abnormalFormModel.setProvinceList(null);
            abnormalFormModel.setCityList(null);
            abnormalFormModel.setAreaList(null);
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        List<Long> idList = null;
        //province
        idList = streamSupplier.get().filter(t -> t.getAreaType() == 2).map(t -> t.getProvinceId()).distinct().collect(Collectors.toList());
        if (ObjectUtils.isEmpty(idList)) {
            abnormalFormModel.setProvinceList(null);
        } else {
            abnormalFormModel.setRegionFilterCount(abnormalFormModel.getRegionFilterCount() + 1);
            abnormalFormModel.setProvinceList(idList);
        }
        //city
        idList = streamSupplier.get().filter(t -> t.getAreaType() == 3).map(t -> t.getCityId()).distinct().collect(Collectors.toList());
        if (ObjectUtils.isEmpty(idList)) {
            abnormalFormModel.setCityList(null);
        } else {
            abnormalFormModel.setRegionFilterCount(abnormalFormModel.getRegionFilterCount() + 1);
            abnormalFormModel.setCityList(idList);
        }
        //area
        idList = streamSupplier.get().filter(t -> t.getAreaType() == 4).map(t -> t.getAreaId()).distinct().collect(Collectors.toList());
        if (ObjectUtils.isEmpty(idList)) {
            abnormalFormModel.setAreaList(null);
        } else {
            abnormalFormModel.setRegionFilterCount(abnormalFormModel.getRegionFilterCount() + 1);
            abnormalFormModel.setAreaList(idList);
        }
        abnormalFormModel.setRushType(null);//可查看突击区域订单
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }

    /**
     * 几个时间相差多少小时
     * @param
     * @return
     */
    public double differTime(long beginDate,long endDate){
        double praiseTimeliness = (double)(endDate-beginDate)/(60*60*1000);
        praiseTimeliness = BigDecimal.valueOf(praiseTimeliness).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return praiseTimeliness;
    }

    /**
     * 根据好评单时效显示不同文本
     * @param timeliness    时效(小时)
     * @return
     */
    private String getCutOffTimelinessLabel(double timeliness){
        int minutes = (int)(60*timeliness);
        if(minutes > 0){
            return DateUtils.minuteToTimeString(minutes,"小时","分") +"后超时";
        }else{
            if(-minutes > 120){
                return "超时:" + DateUtils.minuteToTimeString(-minutes,"小时","分");
            }else{
                return "超时:"+-minutes + "分钟";
            }
        }
    }

    /**
     * 根据好评单时效显示不同文本
     * @param entity
     * @param date
     * @return
     */
    private void setFeedBackTimeliness(AbnormalFormModel entity,Date date,long startDt,long endDt){
        double praiseTimeliness = DateUtils.calculateTimeliness(date,entity.getTimeoutAt(),startDt,endDt);
        entity.setFeedBackTimeliness(getCutOffTimelinessLabel(praiseTimeliness));
        entity.setCutOffTimeliness(praiseTimeliness);
    }

    private List<AbnormalFormModel> toList(MSPage<AbnormalForm> data){
        if(CollectionUtils.isEmpty(data.getList())){
            return Lists.newArrayList();
        }
        int size = data.getList().size();
        List<Long> servicePointIds = data.getList().stream().map(AbnormalForm::getServicepointId).distinct().collect(Collectors.toList());
        List<Long> customerIds = data.getList().stream().map(AbnormalForm::getCustomerId).distinct().collect(Collectors.toList());
        List<String> fields = Lists.newArrayList("id","name","contactInfo1");
        List<MDServicePointViewModel> mdServicePointViewModels = msServicePointService.findBatchByIdsByCondition(servicePointIds,fields,0);
        Map<Long,MDServicePointViewModel> servicePointViewModelMap = mdServicePointViewModels.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, a -> a,(k1,k2)->k1));
        if(servicePointViewModelMap==null){
            servicePointViewModelMap = Maps.newHashMap();
        }
        List<Customer> customers = msCustomerService.findIdAndNameListByIds(customerIds);
        Map<Long,Customer> customerMap = customers.stream().collect(Collectors.toMap(Customer::getId, a -> a,(k1,k2)->k1));
        if(customerMap==null){
            customerMap = Maps.newHashMap();
        }

        Map<Long,User> userMap = Maps.newHashMapWithExpectedSize(size*2);
        List<AbnormalFormModel> list = Lists.newArrayListWithCapacity(size);
        AbnormalFormModelMapper mapper = Mappers.getMapper(AbnormalFormModelMapper.class);
        AbnormalFormModel model;
        MDServicePointViewModel mdServicePointViewModel;
        ServicePoint servicePoint;
        Customer customer;
        for(AbnormalForm form:data.getList()){
            model = mapper.toViewModel(form,userMap);
            mdServicePointViewModel = servicePointViewModelMap.get(form.getServicepointId());
            if(mdServicePointViewModel!=null){
                servicePoint = new ServicePoint(mdServicePointViewModel.getId());
                servicePoint.setName(mdServicePointViewModel.getName());
                servicePoint.setContactInfo1(mdServicePointViewModel.getContactInfo1());
                model.setServicePoint(servicePoint);
            }
            customer = customerMap.get(form.getCustomerId());
            if(customer!=null){
                model.setCustomer(customer);
            }
            list.add(model);
        }
        //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList(),spMap,customerMap,userMap,categoryMap);
        return list;
    }

    //endregion 其他

}
