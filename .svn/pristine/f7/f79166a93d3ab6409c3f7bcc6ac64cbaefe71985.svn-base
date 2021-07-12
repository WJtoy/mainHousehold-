package com.wolfking.jeesite.ms.tmall.md.service;

import com.google.gson.Gson;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.ms.tmall.md.dao.B2BServicePointBatchLogDao;
import com.wolfking.jeesite.ms.tmall.md.entity.B2BServicePointBatchLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MdB2bTmallService {

    @Resource
    private B2BServicePointBatchLogDao b2BServicePointBatchLogDao;

    //region 公共方法

    /**
     * 创建或更新批量上传日志
     *
     * @param cityId
     * @param servicePointSuccessCount
     * @param servicePointFailureCount
     * @param coverServiceSuccessCount
     * @param coverServiceFailureCount
     * @param capacitySuccessCount
     * @param capacityFailureCount
     * @param workerSuccessCount
     * @param workerFailureCount
     * @param processCommentList
     */
    public void updateServicePointBatchLog(long cityId, int servicePointSuccessCount, int servicePointFailureCount,
                                           int coverServiceSuccessCount, int coverServiceFailureCount,
                                           int capacitySuccessCount, int capacityFailureCount,
                                           int workerSuccessCount, int workerFailureCount,
                                           List<B2BServicePointBatchLog.BatchProcessComment> processCommentList) {
        B2BServicePointBatchLog batchLog = new B2BServicePointBatchLog();
        batchLog.setCity(new Area(cityId));
        batchLog.setServicePointSuccessCount(servicePointSuccessCount);
        batchLog.setServicePointFailureCount(servicePointFailureCount);
        batchLog.setCoverServiceSuccessCount(coverServiceSuccessCount);
        batchLog.setCoverServiceFailureCount(coverServiceFailureCount);
        batchLog.setCapacitySuccessCount(capacitySuccessCount);
        batchLog.setCapacityFailureCount(capacityFailureCount);
        batchLog.setWorkerSuccessCount(workerSuccessCount);
        batchLog.setWorkerFailureCount(workerFailureCount);
        if (processCommentList != null && processCommentList.size() > 0) {
            Gson gson = new Gson();
            String processComment = gson.toJson(processCommentList, List.class);
            batchLog.setProcessComment(processComment);
        }

        B2BServicePointBatchLog oldBatchLog = b2BServicePointBatchLogDao.getServicePointBatchLogByCityId(cityId);
        if (oldBatchLog != null) {
            batchLog.setId(oldBatchLog.getId());
            batchLog.preUpdate();
            b2BServicePointBatchLogDao.update(batchLog);
        } else {
            batchLog.preInsert();
            b2BServicePointBatchLogDao.insert(batchLog);
        }
    }

    //endregion 公共方法
}
