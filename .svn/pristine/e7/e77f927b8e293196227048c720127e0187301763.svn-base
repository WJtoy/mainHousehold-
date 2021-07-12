package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpServicePointOrderBusinessService {


    @Autowired
    private ServicePointOrderBusinessService businessService;


    /**
     * 网点派单
     */
    public void changeEngineer(long orderId, String quarter, long servicePointId, long engineerId, int masterFlag, long updateBy, long updateAt) {
        businessService.changeEngineer(orderId, quarter, servicePointId, engineerId, masterFlag, updateBy, updateAt);
    }

    /**
     * 确认上门
     */
    public void confirmOnSiteService(long orderId, String quarter, Long servicePointId, Long engineerId, Integer status, int subStatus, long updateBy, long updateAt) {
        businessService.confirmOnSiteService(orderId, quarter, servicePointId, engineerId, status, subStatus, updateBy, updateAt);
    }

    /**
     * 预约时间
     *
     * @param servicePointId   网点id，用于判断：已派单的才更新
     * @param pendingType      停滞原因
     * @param appAbnormalyFlag 工单异常标记
     */
    public void pending(long orderId, String quarter, long servicePointId, int subStatus, int pendingType, long appointmentDate, long reservationDate, int appAbnormalyFlag, long updateBy, long updateAt) {
        businessService.pending(orderId, quarter, servicePointId, subStatus, pendingType, appointmentDate, reservationDate, appAbnormalyFlag, updateBy, updateAt);
    }

    /**
     * app完工
     *
     * @param subStatus       子状态
     * @param appCompleteType 完工类型(字符)
     * @param abnormalyFlag   工单标记异常标志
     */
    public void appComplete(long orderId, String quarter, Integer subStatus, String appCompleteType, int abnormalyFlag, long updateBy, long updateAt) {
        businessService.appComplete(orderId, quarter, subStatus, appCompleteType, abnormalyFlag, updateBy, updateAt);
    }

    /**
     * 工单关联单据处理
     * 包含加急，催单，投诉单等等
     *
     * @param reminderStatus 催单状态
     * @param complainFlag   投诉
     * @param urgentLevelId  加急
     */
    public void relatedForm(long orderId, String quarter, Integer reminderStatus, Integer complainFlag, Integer urgentLevelId, long updateBy, long updateAt) {
        businessService.relatedForm(orderId, quarter, reminderStatus, complainFlag, urgentLevelId, updateBy, updateAt);
    }

}
