package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointBalanceEntity;
import com.kkl.kklplus.entity.rpt.RPTServicePointBaseInfoEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointBaseInfoSearch;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointBaseRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSServicePointBaseRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private MSServicePointBaseRptFeign msServicePointBaseRptFeign;

    public Page<RPTServicePointBaseInfoEntity> getServicePointBaseRpt(Page<RPTServicePointBaseInfoEntity> page, Long servicePointId, Integer type, Long areaId ) {
        Page<RPTServicePointBaseInfoEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
            RPTServicePointBaseInfoSearch search = new RPTServicePointBaseInfoSearch();
            search.setAreaId(areaId);
            search.setAreaType(type);
            search.setServicePointId(servicePointId);
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTServicePointBaseInfoEntity>> msResponse = msServicePointBaseRptFeign.getServicePointBasePage(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTServicePointBaseInfoEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());
                }
            }


        return returnPage;
    }


    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long servicePointId,Integer areaType, Long areaId, User user) {

        RPTServicePointBaseInfoSearch searchCondition = setSearchCondition(servicePointId,areaType,areaId );
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.SERVICEPOINT_BASE_INFO_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long servicePointId,Integer areaType,Long areaId,User user) {

        RPTServicePointBaseInfoSearch searchCondition = setSearchCondition(servicePointId,areaType,areaId);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = "网点基础资料";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SERVICEPOINT_BASE_INFO_RPT, RPTReportTypeEnum.FINANCE_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTServicePointBaseInfoSearch setSearchCondition(Long servicePointId,Integer areaType,Long areaId){

        RPTServicePointBaseInfoSearch servicePointBaseInfoSearch = new RPTServicePointBaseInfoSearch();
        servicePointBaseInfoSearch.setServicePointId(servicePointId);
        servicePointBaseInfoSearch.setAreaType(areaType);
        servicePointBaseInfoSearch.setAreaId(areaId);

        return  servicePointBaseInfoSearch;

    }

}
