package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCrushCoverageRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSTravelCoverageRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSTravelCoverageRptService {

    @Autowired
    private MSTravelCoverageRptFeign msTravelCoverageRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    public List<RPTCrushCoverageEntity> getTravelCoverAreasRptData(List<Long> productCategoryIds) {
        RPTGradedOrderSearch rptGradedOrderSearch = new RPTGradedOrderSearch();
        rptGradedOrderSearch.setProductCategoryIds(productCategoryIds);
        List<RPTCrushCoverageEntity> list = new ArrayList<>();
        MSResponse<List<RPTCrushCoverageEntity>> msResponse = msTravelCoverageRptFeign.getTravelCoverageList(rptGradedOrderSearch);
        if (MSResponse.isSuccess(msResponse)) {
            list = msResponse.getData();
        }
        return list;
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(List<Long> productCategoryIds,User user) {
        String reportTitle = "远程区域报表";
        RPTGradedOrderSearch rptGradedOrderSearch = new RPTGradedOrderSearch();
        rptGradedOrderSearch.setProductCategoryIds(productCategoryIds);
        String searchConditionJson = RedisGsonUtils.toJson(rptGradedOrderSearch);
        reportExportTaskService.createRptExportTask(RPTReportEnum.TRAVEL_COVERAGE_RPT, RPTReportTypeEnum.OTHER_REPORT, user, reportTitle, searchConditionJson);
    }

}
