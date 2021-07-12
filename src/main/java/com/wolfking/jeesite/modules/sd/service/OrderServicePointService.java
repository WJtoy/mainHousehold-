package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sd.dao.OrderServicePointDao;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 网点订单数据服务
 * @autor Ryan Lu
 * @date 2019/3/22 17:07 PM
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderServicePointService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Autowired
    protected OrderServicePointDao dao;

    @Autowired
    private OrderService orderService;

    //region 方法

    public OrderServicePoint findByOrderAndServicePoint(Long orderId,String quarter,Long servicePointId){
        return dao.findByOrderAndServicePoint(orderId,quarter,servicePointId);
    }

    /**
     * 获得下个派单次序
     */
    public int getNextPlanOrder(Long orderId,String quarter){
        Integer nextPlanOrder = dao.getNextPlanOrder(orderId,quarter);
        if(nextPlanOrder == null || nextPlanOrder <= 0){
            return 1;
        }
        return nextPlanOrder;
    }

    /**
     * 新派单/抢单
     */
    @Transactional(readOnly = false)
    public void assignNewServicePoint(OrderServicePoint entity) {
        dao.insert(entity);
    }

    /**
     * 标记为当前网点
     * 并同步订单的status,sub_status
     */
    @Transactional(readOnly = false)
    public void setCurrentServicePoint(OrderServicePoint entity) {
        if(entity.getDelFlag()==1){
            entity.setDelFlag(0);
        }
        dao.activeServicePoint(entity);
    }

    /**
     * 在新增网点记录前执行
     * 标记为非当前网点
     * 如无上门服务,del_flag = 1
     */
    @Transactional(readOnly = false)
    public void unSetCurrentServicePoint(long orderId,String quarter,Long exceptId,Long updator,Date updateDate,
                                         Integer status,Integer subStatus,Integer complainFlag,Integer abnormalyFlag,
                                         Integer reminderFlag,Integer urgentLevelId) {
        Integer reminderSort = null;
        if(reminderFlag != null) {
            reminderSort = reminderFlag == 1 ? 2 : (reminderFlag > 0 ? 1 : 0);
        }
        dao.unActiveServicePointByOrder(orderId,quarter,updator,updateDate,exceptId,status,subStatus,complainFlag,abnormalyFlag,reminderFlag,reminderSort,urgentLevelId);
        //dao.logicDeleteRecordByOrder(orderId,quarter,updator,updateDate,exceptId);
    }

    /**
     * byMap更新
     */
    @Transactional(readOnly = false)
    public void updateData(Map<String,Object> params) {
        dao.updateData(params);
    }

    /**
     * byMap更新非当前网点
     */
    @Transactional(readOnly = false)
    public void updateNotActiveServiePoint(Map<String,Object> params) {
        dao.updateNotActiveServiePoint(params);
    }

    /**
     * 更新网点好评单状态
     */
    @Transactional(readOnly = false)
    public void updatePriaseStatus(Long orderId,String quarter,Integer praiseStatus,Long servicePointId,Long updateBy,Long updateAt) {
        if(praiseStatus == null || praiseStatus<=0){
            return;
        }
        Date date = DateUtils.longToDate(updateAt);

        if(servicePointId == 0) {
            //更新所有
            dao.updatePraiseStatusByServiePoint(orderId, quarter, null, praiseStatus, updateBy, date);
        }else{
            // 非当前网点，取消：-> 待审核，通过，取消
            if (praiseStatus == PraiseStatusEnum.PENDING_REVIEW.code || praiseStatus == PraiseStatusEnum.APPROVE.code
                    || praiseStatus == PraiseStatusEnum.CANCELED.code) {
                dao.updatePraiseStatusByExceptServiePoint(orderId, quarter, servicePointId, PraiseStatusEnum.CANCELED.code, updateBy, date);
            }
            // 当前网点,新建，驳回
            dao.updatePraiseStatusByServiePoint(orderId, quarter, servicePointId, praiseStatus, updateBy, date);
        }
    }

    /**
     * 按订单批量更新
     * 如客评，对账时已上门的网点都更新
     */
    @Transactional(readOnly = false)
    public void updateByOrder(Map<String,Object> params) {
        dao.updateByOrder(params);
    }

    /**
     * 回退到派单区
     */
    @Transactional
    public int goBackToAccept(OrderServicePoint entity){
        return dao.goBackToAccept(entity);
    }

    //endregion 方法

    //region 安维派单记录

    /**
     * 更新
     */
    @Transactional(readOnly = false)
    public Integer updateServiceFlagOfOrderPlan(Long orderId, String quarter, Long servicePointId, Long engineerId, Integer serviceFlag, Long updateBy, Date updateDate){
        if(servicePointId != null && servicePointId>0 && engineerId != null && engineerId>0) {
            return orderService.updateServiceFlagOfOrderPlan(orderId, quarter, servicePointId, engineerId, serviceFlag, updateBy, updateDate);
        }else{
            return 0;
        }
    }

    //endregion
}
