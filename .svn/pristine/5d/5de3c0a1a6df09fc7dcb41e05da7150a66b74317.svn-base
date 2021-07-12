package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTRebuildMiddleTableTaskEntity;
import com.kkl.kklplus.entity.rpt.common.RPTErrorCode;
import com.kkl.kklplus.entity.rpt.common.RPTMiddleTableEnum;
import com.kkl.kklplus.entity.rpt.common.RPTMiddleTableTypeEnum;
import com.kkl.kklplus.entity.rpt.common.RPTRebuildOperationTypeEnum;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.entity.rpt.search.RPTRebuildMiddleTableTaskSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.rpt.entity.RptSearchCondition;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.RebuildMiddleTableTaskFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSRebuildMiddleTableTaskService {

    @Autowired
    private RebuildMiddleTableTaskFeign rebuildMiddleTableTaskFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    /**
     * 创建报表中间表重建任务
     */
    public void createRebuildMiddleTableTask(RptSearchCondition params) {
        if (!microServicesProperties.getReport().getEnabled()) {
            throw new RPTBaseException("报表微服务未开启");
        }
        if (params != null && RPTMiddleTableEnum.isMiddleTableId(params.getMiddleTableId())
                && RPTMiddleTableTypeEnum.isMiddleTableType(params.getMiddleTableType())
                && RPTRebuildOperationTypeEnum.isOperationType(params.getRebuildOperationType())) {
            RPTRebuildMiddleTableTaskEntity task = new RPTRebuildMiddleTableTaskEntity();
            task.setMiddleTableId(params.getMiddleTableId());
            task.setMiddleTableType(params.getMiddleTableType());
            task.setOperationType(params.getRebuildOperationType());
            if (params.getMiddleTableType() == RPTMiddleTableTypeEnum.DAY.getValue()) {
                if (params.getBeginDate() == null || params.getEndDate() == null) {
                    throw new RPTBaseException(RPTErrorCode.RPT_OPERATE_FAILURE, "创建报表中间表重建任务失败：参数不全");
                }
                task.setBeginDate(params.getBeginDate().getTime());
                task.setEndDate(params.getEndDate().getTime());
            } else if (params.getMiddleTableType() == RPTMiddleTableTypeEnum.YEAR_MONTH.getValue()) {
                if (params.getSelectedYear() == null || params.getSelectedYear() == 0
                        || params.getSelectedMonth() == null || params.getSelectedMonth() == 0) {
                    throw new RPTBaseException(RPTErrorCode.RPT_OPERATE_FAILURE, "创建报表中间表重建任务失败：参数不全");
                }
                task.setSelectedYear(params.getSelectedYear());
                task.setSelectedMonth(params.getSelectedMonth());
            }
            MSResponse<String> msResponse = rebuildMiddleTableTaskFeign.createRebuildMiddleTableTask(task);
            if (!MSResponse.isSuccessCode(msResponse)) {
                throw new RPTBaseException(new MSErrorCode(msResponse.getCode(), msResponse.getMsg()));
            }
        } else {
            throw new RPTBaseException(RPTErrorCode.RPT_OPERATE_FAILURE, "创建报表中间表重建任务失败：参数不全");
        }
    }


    /**
     *
     */
    public Page<RPTRebuildMiddleTableTaskEntity> getTaskList(Page<RPTRebuildMiddleTableTaskEntity> page, Integer middleTableId) {
        if (!microServicesProperties.getReport().getEnabled()) {
            throw new RPTBaseException("报表微服务未开启");
        }
        RPTRebuildMiddleTableTaskSearch search = new RPTRebuildMiddleTableTaskSearch();
        search.setMiddleTableId(middleTableId);
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        Page<RPTRebuildMiddleTableTaskEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());

        MSResponse<MSPage<RPTRebuildMiddleTableTaskEntity>> taskPage = rebuildMiddleTableTaskFeign.getRebuildMiddleTableTaskList(search);
        if (MSResponse.isSuccessCode(taskPage)) {
            returnPage.setCount(taskPage.getData().getRowCount());
            returnPage.setList(taskPage.getData().getList());
        }

        return returnPage;
    }


}
