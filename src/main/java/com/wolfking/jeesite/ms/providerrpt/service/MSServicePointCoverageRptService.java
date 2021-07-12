package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTServicePointCoverageEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointCoverageRptFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSServicePointCoverageRptService {
    @Autowired
    private MSServicePointCoverageRptFeign msServicePointCoverageRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTServicePointCoverageEntity> getServicePointCoverAreasRptData() {
        List<RPTServicePointCoverageEntity> list = new ArrayList<>();
        MSResponse<List<RPTServicePointCoverageEntity>> msResponse = msServicePointCoverageRptFeign.getServicePointCoverageList();
        if (MSResponse.isSuccess(msResponse)) {
            list = msResponse.getData();
        }
        return list;
    }

    public List<RPTServicePointCoverageEntity> getServicePointNoCoverAreasRptData() {
        List<RPTServicePointCoverageEntity> list = new ArrayList<>();
        MSResponse<List<RPTServicePointCoverageEntity>> msResponse = msServicePointCoverageRptFeign.getServicePointNoCoverageList();
        if (MSResponse.isSuccess(msResponse)) {
            list = msResponse.getData();
        }
        return list;
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(User user) {
        String reportTitle = "覆盖网点报表";
        reportExportTaskService.createRptExportTask(RPTReportEnum.SERVICE_POINT_COVERAGE_RPT, RPTReportTypeEnum.OTHER_REPORT, user, reportTitle, "");
    }
    /**
     * 创建报表导出任务
     */
    public void createServicePointNoCoverAreasRptExportTask(User user) {
        String reportTitle = "网点未覆盖区域报表";
        reportExportTaskService.createRptExportTask(RPTReportEnum.SERVICE_POINT_NOCOVERAGE_RPT, RPTReportTypeEnum.OTHER_REPORT, user, reportTitle, "");
    }
}
