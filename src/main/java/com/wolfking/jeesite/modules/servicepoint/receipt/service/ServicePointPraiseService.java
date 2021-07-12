package com.wolfking.jeesite.modules.servicepoint.receipt.service;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.praise.*;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.servicepoint.ms.receipt.SpOrderPraiseService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.entity.mapper.PraiseModelMapper;
import com.wolfking.jeesite.ms.praise.feign.ServicePointPraiseFeign;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 网点好评费service
 * */
@Slf4j
@Service
public class ServicePointPraiseService {

    @Autowired
    private ServicePointPraiseFeign servicePointPraiseFeign;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SpOrderPraiseService spOrderPraiseService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;


    /**
     * 网点查询待处理好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> pendingReviewList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = servicePointPraiseFeign.pendingReviewList(praisePageSearchModel);
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

    /**
     * 网点查询已审核好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> approvedList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = servicePointPraiseFeign.approvedList(praisePageSearchModel);
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

    /**
     * 网点查询已驳回好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> rejectList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = servicePointPraiseFeign.rejectList(praisePageSearchModel);
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

    /**
     * 网点查询所有好评信息列表
     * @param praisePageSearchModel
     * @return
     */
    public Page<ViewPraiseModel> findPraiseList(Page<ViewPraiseModel> page, PraisePageSearchModel praisePageSearchModel){
        praisePageSearchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<PraiseListModel>> msResponse = servicePointPraiseFeign.findPraiseList(praisePageSearchModel);
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

    /**
     * 网点好评单详情
     * @param id
     * @param quarter
     */
    public ViewPraiseModel getPraiseInfoForServicePoint(Long id, String quarter){
        Praise praise = spOrderPraiseService.getByPraiseId(quarter,id);
        if(praise!=null){
            ViewPraiseModel viewPraiseModel = Mappers.getMapper(PraiseModelMapper.class).PraiseToViewModel(praise);
               if(viewPraiseModel!=null){
                   PraiseStatusEnum praiseStatusEnum = PraiseStatusEnum.fromCode(viewPraiseModel.getStatus());
                   if(praiseStatusEnum!=null){
                       viewPraiseModel.setStrStatus(praiseStatusEnum.msg);
                   }
               }
            return viewPraiseModel;
        }else{
            return null;
        }
    }

    /**
     * 网点修改好评单(新建,驳回状态修改)
     * @param praise
     * @param user
     */
    public void updatePraise(Praise praise,User user){
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
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.SERVICE_POINT.code);
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());
            praise.setPraiseLog(praiseLog);
        /*    MSResponse<Integer> msResponse = orderPraiseFeign.resubmit(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("修改好评费失败:" + msResponse.getMsg());
            }*/
            spOrderPraiseService.resubmit(praise);

            if(currentStatus == PraiseStatusEnum.REJECT.code){ //驳回后重新提交
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
     * 网点取消好评单
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
            Date date = new Date();
            PraiseLog praiseLog = new PraiseLog();
            praise.setUpdateById(user.getId());
            praise.setUpdateDt(date.getTime());
            praise.setUpdateBy(user.getName());
            if(praise.getStatus()==PraiseStatusEnum.REJECT.code){
                praiseLog.setActionType(PraiseActionEnum.REJECT_TO_CANCELED.code);
            }else {
                praiseLog.setActionType(PraiseActionEnum.NEW_TO_CANCELED.code);
            }
            praise.setStatus(PraiseStatusEnum.CANCELED.code);
            praiseLog.setStatus(praise.getStatus());
            praiseLog.setPraiseId(praise.getId());
            praiseLog.setQuarter(praise.getQuarter());
            praiseLog.setVisibilityFlag(ViewPraiseModel.VISIBILITY_FLAG_ALL);
            if(StringUtils.isNotBlank(praise.getRemarks())){
                praiseLog.setContent(praise.getRemarks());
            }else {
                praiseLog.setContent("【取消】好评单审核");
            }
            praiseLog.setCreatorType(PraiseCreatorTypeEnum.SERVICE_POINT.code);
            praiseLog.setCreateById(praise.getUpdateById());
            praiseLog.setCreateDt(date.getTime());
            praise.setPraiseLog(praiseLog);
          /*  MSResponse<Integer> msResponse = orderPraiseFeign.cancelled(praise);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException("审核通过好评单失败:" + msResponse.getMsg());
            }*/
            spOrderPraiseService.cancelled(praise);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            if (locked && lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    /**
     * 网点保存好评费申请
     * @param praise
     * @param order
     * @param user
     * @param createType 操作人类型 10-客服  40-网店
     */
    public void saveApplyPraise(Praise praise, Order order, User user, int createType){
        spOrderPraiseService.saveApplyPraise(praise,order,user,createType);
    }



    /**
     * 申请好评费页面修改好评费
     * @param praise
     * @param user
     * @param createType 操作人类型 10-客服  40-网店
     */
    public void updatePraise(Praise praise, User user,int createType){
        spOrderPraiseService.updatePraise(praise,user,createType);
    }



}
