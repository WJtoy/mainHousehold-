package com.wolfking.jeesite.ms.praise.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.praise.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.praise.feign.SalesPraiseFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 业务好评service
 * */
@Slf4j
@Service
public class SalesPraiseService {

    @Autowired
    private SalesPraiseFeign salesPraiseFeign;

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SequenceIdService sequenceIdService;

    /**
     * 业务查询待处理好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> pendingReviewList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = salesPraiseFeign.pendingReviewList(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
                Date date = new Date();
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                for(ViewPraiseModel entity:list){
                    //double praiseTimeliness = DateUtils.differTime(entity.getTimeoutAt(),date);
                    double praiseTimeliness = DateUtils.calculateTimeliness(date,entity.getTimeoutAt(),startDt,endDt);
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
     * 业务查询已审核好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> approvedList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = salesPraiseFeign.approvedList(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
                for(ViewPraiseModel entity:list){
                    int minutes = (int)(60*entity.getApproveTimeLiness());
                    entity.setTimelinessLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
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
     * 业务查询所有好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> findPraiseList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = salesPraiseFeign.findPraiseList(praisePageSearchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<PraiseListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ViewPraiseModel> list = Mappers.getMapper(PraiseModelMapper.class).toViewModels(data.getList());
                Date date = new Date();
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                for (ViewPraiseModel entity:list){
                    if(entity.getStatus() == PraiseStatusEnum.PENDING_REVIEW.code){
                        double praiseTimeliness = DateUtils.calculateTimeliness(date,entity.getTimeoutAt(),startDt,endDt);
                        //double praiseTimeliness = DateUtils.differTime(entity.getTimeoutAt(),date);
                        entity.setTimelinessLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                        entity.setCutOffTimeliness(praiseTimeliness);
                    }else{
                        int minutes = (int)(60*entity.getApproveTimeLiness());
                        entity.setTimelinessLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
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
     * 业务好评单详情
     * @param id
     * @param quarter
     */
    public ViewPraiseModel getPraiseInfoForSale(Long id, String quarter){
        Praise praise = orderPraiseService.getPraiseInfo(quarter,id);
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
     * 业务审核通过好评单
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
             praise.setRemarks("【通过】好评单审核," + praise.getRemarks());

             PraiseLog praiseLog = new PraiseLog();
             praiseLog.setId(sequenceIdService.nextId());//2020/05/25
             praiseLog.setStatus(praise.getStatus());
             praiseLog.setPraiseId(praise.getId());
             praiseLog.setQuarter(praise.getQuarter());
             praiseLog.setActionType(PraiseActionEnum.APPROVE.code);
             praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
             praiseLog.setContent(praise.getRemarks());
             praiseLog.setCreatorType(PraiseCreatorTypeEnum.SALES.code);
             praiseLog.setCreateById(praise.getUpdateById());
             praiseLog.setCreateDt(date.getTime());

             praise.setPraiseLog(praiseLog);
             MSResponse<Integer> msResponse = orderPraiseFeign.approve(praise);
             if(!MSResponse.isSuccessCode(msResponse)){
                 throw new RuntimeException("审核通过好评单失败:" + msResponse.getMsg());
             }
             String content="您有好评单("+praise.getOrderNo()+")审核已通过,视为有效好评,已获得"+praise.getServicepointPraiseFee()+"块好评费";
             PushMessageUtils.pushPraiseMessage(praise.getServicepointId(),praise.getEngineerId(),"好评单审核通过",content);
         }catch (Exception e){
             throw new RuntimeException(e.getMessage());
         }finally {
             if (locked && lockKey != null) {
                 redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
             }
         }
     }

    /**
     * 业务驳回好评单
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
            //praise.setUpdateBy(user.getName());
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
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.SALES.code);
            praiseLog.setCreateById(user.getId());
            praiseLog.setCreateDt(date.getTime());

            praiseAbnormalMessage.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.reject(praiseAbnormalMessage);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("审核通过好评单失败:" + msResponse.getMsg());
            }
            String content="您有好评单("+praise.getOrderNo()+")被驳回,因为"+ praise.getRemarks();
            PushMessageUtils.pushPraiseMessage(praise.getServicepointId(),praise.getEngineerId(),"好评单驳回",content);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    /**
     * 业务取消好评单
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
            Dict dict = MSDictUtils.getDictByValue(praise.getRejectionCategory().toString(),"praise_abnormal_type");
            if(StringUtils.isBlank(praise.getRemarks())){
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
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.SALES.code);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());

            praise.setPraiseLog(praiseLog);
            MSResponse<Integer> msResponse = orderPraiseFeign.cancelled(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("审核通过好评单失败:" + msResponse.getMsg());
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
}
