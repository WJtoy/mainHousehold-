package com.wolfking.jeesite.ms.praise.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.service.OrderRegionService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderStatusFlagService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.praise.entity.PraiseLogModel;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseLogModelMapper;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderPraiseService  extends OrderRegionService {


    @Autowired
    private AreaService areaService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private MSCustomerPraiseFeeService msCustomerPraiseFeeService;

    /**
     * 客服可取消没费用通过的好评单时间段(24小时内可取消)
     **/
    public static final int TIME_HOUR = 24;

    /**
     *  根据订单Id，网点Id获取好评费
     * */
    public Praise getByOrderId(String quarter,Long orderId,Long servicePointId){
        MSResponse<Praise> msResponse = orderPraiseFeign.getByOrderIdAndServicepointId(quarter,orderId,servicePointId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }


    /**
     * 保存好评费申请
     * @param praise
     * @param order
     * @param user
     * @param createType 创建人类型 10：客服 40：网点
     * */
    @Transactional()
    public void saveApplyPraise(Praise praise, Order order, User user,int createType){
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION,praise.getOrderId());
        //获得锁
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);//60秒
        if (!locked) {
            throw new RuntimeException("此好评单正在处理中，请稍候重试");
        }
        try {
            OrderCondition orderCondition = order.getOrderCondition();
            Long customerId = orderCondition.getCustomer().getId();
            MDCustomerPraiseFee customerPraiseFee = msCustomerPraiseFeeService.getByCustomerIdFromCacheNewForCP(customerId);
            if(customerPraiseFee == null){
                throw new RuntimeException("读取客户好评设定信息失败");
            }

            String praiseNo = SeqUtils.NextSequenceNo("praiseNo",0,3);
            if(StringUtils.isBlank(praiseNo)){
                throw new RuntimeException("生成好评单号失败");
            }
            //2020/05/25
            if(praise.getId() == null || praise.getId() <=0) {
                praise.setId(sequenceIdService.nextId());
            }
            Date date = new Date();

            praise.setPraiseNo(praiseNo);
            praise.setOrderNo(order.getOrderNo());
            praise.setDataSource(order.getDataSourceId());
            praise.setProductCategoryId(orderCondition.getProductCategoryId());
            praise.setStatus(PraiseStatusEnum.NEW.code);
            praise.setWorkcardId(order.getWorkCardId());
            praise.setParentBizOrderId(order.getParentBizOrderId());
            if(orderCondition.getSubArea()!=null && orderCondition.getSubArea().getId()!=null){
                praise.setSubAreaId(orderCondition.getSubArea().getId());
            }
            if(orderCondition.getArea()!=null && orderCondition.getArea().getId()!=null){
                praise.setAreaId(orderCondition.getArea().getId());
            }
            Area area = areaService.getFromCache(orderCondition.getArea().getId());
            if (area != null) {
                List<String> ids = Splitter.onPattern(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(area.getParentIds());
                if (ids.size() >= 2) {
                    praise.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                    praise.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
                }
            }
            praise.setCustomerId(orderCondition.getCustomer().getId());
            String shopId = Optional.ofNullable(order.getB2bShop()).map(t->t.getShopId()).orElse(StrUtil.EMPTY);
            praise.setShopId(shopId==null? StrUtil.EMPTY:shopId);
            praise.setCustomerPaymentType(customerPraiseFee.getOnlineFlag());//客户好评费结算方式
            if(orderCondition.getServicePoint()!=null && orderCondition.getServicePoint().getId()!=null){
                praise.setServicepointId(orderCondition.getServicePoint().getId());
            }

            if(orderCondition.getEngineer()!=null && orderCondition.getEngineer().getId()!=null){
                praise.setEngineerId(orderCondition.getEngineer().getId());
            }
            praise.setProductNames(StringUtils.left(praise.getProductNames(),100));
            praise.setUserName(orderCondition.getUserName());
            praise.setUserPhone(orderCondition.getServicePhone());
            praise.setUserAddress(orderCondition.getArea().getName() + orderCondition.getServiceAddress());
            //String[] picture = praise.getPicsJson().split(",");
            //praise.setPicsJson(GsonUtils.toGsonString(picture));
            praise.setKefuId(orderCondition.getKefu().getId());
            praise.setRemarks("");
            praise.setCreateById(user.getId());
            praise.setCreateDt(date.getTime());
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praise.setCanRush(orderCondition.getCanRush());
            praise.setKefuType(orderCondition.getKefuType());

            //跟踪进度
            PraiseLog praiseLog = new PraiseLog();
            praiseLog.setId(sequenceIdService.nextId());//2020/05/25
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setCreatorType(createType);
            praiseLog.setActionType(PraiseActionEnum.NEW.code);

            praiseLog.setContent("【新建】好评单申请");
            praiseLog.setCreateById(praise.getCreateById());
            praiseLog.setCreateDt(praise.getCreateDt());
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            praise.setPraiseLog(praiseLog);

            MSResponse<Praise> msResponse = orderPraiseFeign.saveApplyPraise(praise);
            if(!MSResponse.isSuccess(msResponse)){
                throw new RuntimeException("申请好评费失败:" + msResponse.getMsg());
            }

            //更新工单状态标记表
            orderStatusFlagService.updatePraiseStatus(praise.getOrderId(),praise.getQuarter(),PraiseStatusEnum.NEW.code);

            //更新好评单状态
            try {
                servicePointOrderBusinessService.syncPraiseStatus(praise.getOrderId(),praise.getQuarter(),praise.getServicepointId(),
                        praise.getStatus(),praise.getCreateById(),praise.getCreateDt());
            }catch (Exception e){
                log.error("发送消息队列更新好评单状态失败 form: {}", GsonUtils.toGsonString(praise),e);
            }

            OrderProcessLog processLog = new OrderProcessLog();
            try {
                //写入工单日志
                processLog.setAction("新建好评单");
                processLog.setOrderId(praise.getOrderId());
                processLog.setQuarter(praise.getQuarter());
                processLog.setActionComment("【新建】好评单");
                processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
                processLog.setCloseFlag(0);
                processLog.setStatus("");
                processLog.setStatusValue(0);
                processLog.setCreateDate(date);
                processLog.setCreateBy(user);
                processLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
                orderService.saveOrderProcessLogNew(processLog);
            }catch (Exception e){
                 log.error("保存工单日志失败 form: {}",GsonUtils.toGsonString(processLog),e);
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
     * 修改好评单
     * */
    public void updatePraise(Praise praise,User user,int createType){
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
            praiseLog.setId(sequenceIdService.nextId());//2020/05/25
            if(praise.getStatus() == PraiseStatusEnum.REJECT.code){
                praise.setStatus(PraiseStatusEnum.PENDING_REVIEW.code);
                praiseLog.setActionType(PraiseActionEnum.RESUBMIT.code);
                praiseLog.setContent("【驳回后重新修改】好评费");
            }else{
                praiseLog.setActionType(PraiseActionEnum.UPDATE_PIC.code);
                praiseLog.setContent("【修改】好评费");
            }
            praise.setUpdateBy(user.getName());
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            praiseLog.setCreatorType(createType);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());
            praise.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.resubmit(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("修改好评费失败:" + msResponse.getMsg());
            }
            if(currentStatus == PraiseStatusEnum.REJECT.code){
                //更新好评单状态
                try {
                    servicePointOrderBusinessService.syncPraiseStatus(praise.getOrderId(),praise.getQuarter(),praise.getServicepointId(),
                            praise.getStatus(),praise.getUpdateById(),praise.getUpdateDt());
                }catch (Exception e){
                    log.error("发送消息队列更新好评单状态失败 form: {}",GsonUtils.toGsonString(praise),e);
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
     * 获取好评单详情
     * */
    public Praise getPraiseInfo(String quarter,Long id){
        MSResponse<Praise> msResponse = orderPraiseFeign.getById(quarter,id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 获取待是审核好评单详情
     * @param id
     * @param quarter
     */
    public ViewPraiseModel getPraiseInfoForReview(Long id, String quarter){
        MSResponse<Praise> msResponse = orderPraiseFeign.getById(quarter,id);
        if(!MSResponse.isSuccess(msResponse)){
            return null;
        }
        Praise praise = msResponse.getData();
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
            }
            return viewPraiseModel;
        }else{
            return null;
        }
    }

    /**
     * 根据好评单id和分片获取跟踪进度列表
     * */
    public List<PraiseLogModel> finPraiseLogList(String quarter,Long praiseId){
         MSResponse<List<PraiseLog>> msResponse = orderPraiseFeign.finPraiseLogList(quarter,praiseId);
         if(MSResponse.isSuccess(msResponse)){
             List<PraiseLogModel> praiseLogModelList = Mappers.getMapper(PraiseLogModelMapper.class).toViewModels(msResponse.getData());
             return praiseLogModelList;
         }else {
             return Lists.newArrayList();
         }
    }

    //region 审核

    /**
     * 客服待审核好评单列表
     */
    public Page<ViewPraiseModel> pendingReviewList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = orderPraiseFeign.noFeesPendingReviewList(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
                long date = System.currentTimeMillis();
                for(ViewPraiseModel entity:list){
                    double praiseTimeliness = DateUtils.differTime(entity.getTimeoutAt(),date);
                    entity.setTimelinessLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                    entity.setCutOffTimeliness(praiseTimeliness);
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
     * 审核通过好评单
     * @param praise
     */
    public void approve(Praise praise,User user){
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION,praise.getOrderId());
        //获得锁
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);//60秒
        if (!locked) {
            throw new RuntimeException("此好评单正在处理中，请稍候重试");
        }
        try {
            Date date = new Date();
            praise.setStatus(PraiseStatusEnum.APPROVE.code);
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praise.setUpdateBy(user.getName());
            praise.setRemarks("通过好评单审核");

            PraiseLog praiseLog = new PraiseLog();
            praiseLog.setId(sequenceIdService.nextId());//2020/05/25
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setPraiseId(praise.getId());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setActionType(PraiseActionEnum.APPROVE.code);
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            praiseLog.setContent("【通过】好评单审核");
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.KEFU.code);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());

            praise.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.approve(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("审核通过好评单失败:" + msResponse.getMsg());
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
     * 驳回好评单
     * @param praise
     */
    public void reject(Praise praise,Order order,User user){
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION,praise.getOrderId());
        //获得锁
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);//60秒
        if (!locked) {
            throw new RuntimeException("此好评单正在处理中，请稍候重试");
        }
        try {
            String abnormalNo = SeqUtils.NextSequenceNo("abnormalNo",0,3);
            if(StringUtils.isBlank(abnormalNo)){
                throw new RuntimeException("生成异常单失败");
            }
            Date date = new Date();
            if(StringUtils.isBlank(praise.getRemarks())){
                Dict dict = MSDictUtils.getDictByValue(praise.getRejectionCategory().toString(),"praise_abnormal_type");
                if(dict!=null){
                    praise.setRemarks(dict.getLabel());
                }
            }
            PraiseAbnormalMessage praiseAbnormalMessage = new PraiseAbnormalMessage();
            praiseAbnormalMessage.setId(sequenceIdService.nextId());//2020/05/25
            praiseAbnormalMessage.setPraiseId(praise.getId());
            praiseAbnormalMessage.setQuarter(praise.getQuarter());
            praiseAbnormalMessage.setStatus(PraiseStatusEnum.REJECT.code);
            praiseAbnormalMessage.setAbnormalNo(abnormalNo);
            praiseAbnormalMessage.setChannel(AppFeedbackEnum.Channel.ORDER.getValue());
            praiseAbnormalMessage.setSubType(praise.getRejectionCategory());
            praiseAbnormalMessage.setReason(praise.getRemarks());
            praiseAbnormalMessage.setKefuId(order.getOrderCondition().getKefu().getId());
            praiseAbnormalMessage.setCreateDt(date.getTime());
            praiseAbnormalMessage.setCreateById(user.getId());
            praiseAbnormalMessage.setCreateByName(user.getName());
            praiseAbnormalMessage.setRemarks(praise.getRemarks());

            PraiseLog praiseLog = new PraiseLog();
            praiseLog.setId(sequenceIdService.nextId());//2020/05/25
            praiseLog.setStatus(praiseAbnormalMessage.getStatus());
            praiseLog.setPraiseId(praise.getId());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setActionType(PraiseActionEnum.REJECT.code);
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            if(StringUtils.isNotBlank(praise.getRemarks())){
                praiseLog.setContent("【驳回】好评单审核,描述:" + praise.getRemarks());
            }else {
                praiseLog.setContent("【驳回】好评单审核");
            }
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.KEFU.code);
            praiseLog.setCreateById(user.getId());
            praiseLog.setCreateDt(date.getTime());

            praiseAbnormalMessage.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.reject(praiseAbnormalMessage);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("驳回好评单失败:" + msResponse.getMsg());
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
     * 取消好评单
     * @param praise
     */
    public void cancelled(Praise praise,User user){
        String lockKey = String.format(PraiseConstrant.LOCK_PRAISE_WRITE_OPERATION,praise.getOrderId());
        //获得锁
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, PraiseConstrant.LOCK_EXPIRED_PRAISE_WRITE_OPERATION);//60秒
        if (!locked) {
            throw new RuntimeException("此好评单正在处理中，请稍候重试");
        }
        try {
            Date date = new Date();
            praise.setStatus(PraiseStatusEnum.CANCELED.code);
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praise.setUpdateBy(user.getName());
            if(StringUtils.isBlank(praise.getRemarks())){
                Dict dict = MSDictUtils.getDictByValue(praise.getRejectionCategory().toString(),"praise_abnormal_type");
                if(dict!=null){
                    praise.setRemarks(dict.getLabel());
                }
            }
            if(StringUtils.isBlank(praise.getRemarks())){
                PraiseStatusEnum praiseStatusEnum = PraiseStatusEnum.fromCode(praise.getRejectionCategory());
                if(praiseStatusEnum!=null){
                    praise.setRemarks(praiseStatusEnum.msg);
                }
            }
            PraiseLog praiseLog = new PraiseLog();
            praiseLog.setId(sequenceIdService.nextId());//2020/05/25
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setPraiseId(praise.getId());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setActionType(PraiseActionEnum.CANCELED.code);
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
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }


    /**
     * 客服查看无费用好评单列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> noFeesApprovedKefuList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = orderPraiseFeign.noFeesApprovedKefuList(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
           /*     long date = System.currentTimeMillis();
                for(ViewPraiseModel entity:list){
                    double timeDiffer = DateUtils.differTime(entity.getUpdateDt(),date);
                    if(timeDiffer>TIME_HOUR){ //大于24小时
                        entity.setOvertimeFlag(ViewPraiseModel.HAS_OVERTIME_FLAG);
                    }
                }*/
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
     * 客服查看无费用好评单列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> invalidationKefuList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = orderPraiseFeign.invalidationKefuLis(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
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


    public void invalidPraise(Praise praise,User user){
        if(StringUtils.isBlank(praise.getRemarks().trim())){
            Dict dict = MSDictUtils.getDictByValue(praise.getRejectionCategory().toString(),"praise_abnormal_type");
            if(dict!=null){
                praise.setRemarks(dict.getLabel());
            }
        }
        Date date = new Date();
        praise.setStatus(PraiseStatusEnum.INVALIDATION.code);
        praise.setUpdateById(user.getId());
        praise.setUpdateDt(date.getTime());

        PraiseLog praiseLog = new PraiseLog();
        praiseLog.setId(sequenceIdService.nextId());//2020/05/25
        praiseLog.setStatus(praise.getStatus());
        praiseLog.setPraiseId(praise.getId());
        praiseLog.setQuarter(praise.getQuarter());
        praiseLog.setActionType(PraiseActionEnum.INVALIDATION.code);
        praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
        if(StringUtils.isNotBlank(praise.getRemarks())){
            praiseLog.setContent("【无效】好评单,描述:" + praise.getRemarks());
        }else {
            praiseLog.setContent("【无效】好评单");
        }
        praiseLog.setCreatorType(PraiseCreatorTypeEnum.KEFU.code);
        praiseLog.setCreateById(praise.getUpdateById());
        praiseLog.setCreateDt(date.getTime());
        praise.setPraiseLog(praiseLog);
        MSResponse<Integer> msResponse = orderPraiseFeign.invalidation(praise);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("修改好评单为无效失败" + msResponse.getMsg());
        }
        String content="您有好评单("+praise.getOrderNo()+")被好评无效,因为"+ praise.getRemarks();
        PushMessageUtils.pushPraiseMessage(praise.getServicepointId(),praise.getEngineerId(),"好评单无效",content);
    }

    /**
     * 计算网点,厂商好评费用
     * @param praise
     * @param customerPraiseFee
     * @return
     */
    public static NameValuePair<Double,Double> getPraiseFee(Praise praise, MDCustomerPraiseFee customerPraiseFee){
        List<String> standardCodeList  = praise.getPicItems().stream().filter(t->StringUtils.isNotBlank(t.getUrl())).map(PraisePicItem::getCode).collect(Collectors.toList());
        Map<String, MDCustomerPraiseFeePraiseStandardItem> map = customerPraiseFee.getPraiseStandardItems().stream().collect(Collectors.toMap(MDCustomerPraiseFeePraiseStandardItem::getCode, a -> a,(k1, k2)->k1));
        List<NameValuePair<String,Double>> nameValuePraiseList = Lists.newArrayList();
        NameValuePair<String,Double> nameValuePraise = null;
        for (String standardCode:standardCodeList){
            MDCustomerPraiseFeePraiseStandardItem standardItem = map.get(standardCode);
            if(standardItem!=null){
                nameValuePraise = new NameValuePair<String, Double>(standardCode,standardItem.getFee());
                nameValuePraiseList.add(nameValuePraise);
            }
        }
        NameValuePair<Double,Double> praiseFee = PraiseUtils.calculatePraiseCost(customerPraiseFee.getPraiseFee(),customerPraiseFee.getMaxPraiseFee(),customerPraiseFee.getDiscount(),nameValuePraiseList);
        return praiseFee;
    }

    //endregion
}
