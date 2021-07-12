package com.wolfking.jeesite.modules.servicepoint.rpt.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTExportTaskEntity;
import com.kkl.kklplus.entity.rpt.RPTExportTaskSearch;
import com.kkl.kklplus.entity.rpt.common.RPTErrorCode;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.RptExportTaskFeign;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.feign.SpRptExportTaskFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class SpReportExportTaskService {

    @Autowired
    private SpRptExportTaskFeign spRptExportTaskFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    /**
     * 检查报表导出任务
     */
    public void checkRptExportTask(RPTReportEnum report, User createBy, String searchConditionJson) {
        if (!microServicesProperties.getReport().getEnabled()) {
            throw new RPTBaseException("报表微服务未开启");
        }
        if (report != null && createBy != null && createBy.getId() != null && createBy.getId() > 0) {
            RPTExportTaskEntity task = new RPTExportTaskEntity();
            task.setReportId(report.value);
            task.setTaskCreateBy(createBy.getId());
            task.setSearchConditionJson(StringUtils.toString(searchConditionJson));
            MSResponse<String> msResponse = spRptExportTaskFeign.checkRptExportTask(task);
            if (!MSResponse.isSuccessCode(msResponse)) {
                throw new RPTBaseException(new MSErrorCode(msResponse.getCode(), msResponse.getMsg()));
            }
        } else {
            throw new RPTBaseException(RPTErrorCode.RPT_OPERATE_FAILURE, "导出失败：报表参数不全");
        }
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(RPTReportEnum report, RPTReportTypeEnum type, User createBy,
                                    String reportTitle, String searchConditionJson) {
        if (!microServicesProperties.getReport().getEnabled()) {
            throw new RPTBaseException("报表微服务未开启");
        }
        if (report != null && type != null
                && createBy != null && createBy.getId() != null && createBy.getId() > 0
                && StringUtils.isNotBlank(reportTitle)) {
            RPTExportTaskEntity task = new RPTExportTaskEntity();
            task.setReportId(report.value);
            task.setReportType(type.value);
            task.setTaskCreateBy(createBy.getId());
            task.setTaskCreateByName(StringUtils.toString(createBy.getName()));
            task.setReportTitle(reportTitle);
            task.setSearchConditionJson(StringUtils.toString(searchConditionJson));
            MSResponse<String> msResponse = spRptExportTaskFeign.createRptExportTask(task);
            if (!MSResponse.isSuccessCode(msResponse)) {
                throw new RPTBaseException(new MSErrorCode(msResponse.getCode(), msResponse.getMsg()));
            }
        } else {
            throw new RPTBaseException(RPTErrorCode.RPT_OPERATE_FAILURE, "创建报表导出任务失败：参数不全");
        }
    }


    /**
     *
     */
    public Page<RPTExportTaskEntity> getRptExportTaskList(Page<RPTExportTaskEntity> page,
                                                          Integer reportId, Integer reportType,
                                                          User createBy, Date beginDate, Date endDate) {
        if (!microServicesProperties.getReport().getEnabled()) {
            throw new RPTBaseException("报表微服务未开启");
        }
        RPTExportTaskSearch search = new RPTExportTaskSearch();
        search.setReportId(reportId);
        search.setReportType(reportType);
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        if (createBy != null && createBy.getId() != null) {
            search.setTaskCreateBy(createBy.getId());
        }
        if (beginDate != null) {
            search.setBeginTaskCreateDate(beginDate.getTime());
        }
        if (endDate != null) {
            search.setEndTaskCreateDate(endDate.getTime());
        }
        Page<RPTExportTaskEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());

        MSResponse<MSPage<RPTExportTaskEntity>> taskPage = spRptExportTaskFeign.getRptExportTaskList(search);
        if (MSResponse.isSuccessCode(taskPage)) {
            returnPage.setCount(taskPage.getData().getRowCount());
            returnPage.setList(taskPage.getData().getList());
        }

        return returnPage;
    }

    public String getReportExcelDownloadUrl(Long taskId, Integer reportId,User user) {
        String downloadUrl = "";
        if (taskId != null && user != null && user.getId() != null && user.getId() > 0) {
            RPTExportTaskEntity params = new RPTExportTaskEntity();
            params.setReportId(reportId);
            params.setId(taskId);
            params.setLastDownloadBy(user.getId());
            params.setLastDownloadByName(user.getName());
            MSResponse<String> msResponse = spRptExportTaskFeign.getRptExcelDownloadUrl(params);
            if (MSResponse.isSuccessCode(msResponse)) {
                downloadUrl = msResponse.getData();
            }
        }
        return downloadUrl;
    }
}
