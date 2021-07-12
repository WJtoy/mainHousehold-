package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuAreaEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCrushCoverageRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuAreaRptFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSKeFuAreaRptService {

    @Autowired
    private MSKeFuAreaRptFeign msKeFuAreaRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTKeFuAreaEntity> getKefuAreasRptData() {
        List<RPTKeFuAreaEntity> list = new ArrayList<>();
        MSResponse<List<RPTKeFuAreaEntity>> msResponse = msKeFuAreaRptFeign.getKeFuAreaList();
        if (MSResponse.isSuccess(msResponse)) {
            list = msResponse.getData();
        }
        return list;
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(User user) {
        String reportTitle = "客服区域报表";
        reportExportTaskService.createRptExportTask(RPTReportEnum.KEFU_COVERAGE_RPT, RPTReportTypeEnum.OTHER_REPORT, user, reportTitle, "");
    }

}
