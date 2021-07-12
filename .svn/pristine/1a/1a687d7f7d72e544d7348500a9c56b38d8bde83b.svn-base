package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuPraiseDetailsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointPraiseDetailsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSServicePointPraiseDetailsRptService {


    @Autowired
    private MSServicePointPraiseDetailsRptFeign msServicePointPraiseDetailsRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    public Page<RPTKeFuPraiseDetailsEntity> getPraiseOrderList(Page<RPTKeFuPraiseDetailsEntity> page, Integer status,Long servicePointId,Date beginDate, Date endDate) {
        Page<RPTKeFuPraiseDetailsEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if ( beginDate != null && endDate != null) {
            RPTKeFuCompleteTimeSearch search = new RPTKeFuCompleteTimeSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setBeginDate(beginDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setServicePointId(servicePointId);
            search.setStatus(status);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> msResponse = msServicePointPraiseDetailsRptFeign.getServicePointPraiseDetailsList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTKeFuPraiseDetailsEntity> data = msResponse.getData();
                    returnPage.setCount(data.getRowCount());
                    returnPage.setList(data.getList());

                }
            }
        }
        return returnPage;
    }



    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Integer status,Long servicePointId,Date beginDate, Date endDate, User user) {

        RPTKeFuCompleteTimeSearch searchCondition = setSearchCondition(status,servicePointId,beginDate,endDate);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.SERVICEPOINT_PRAISE_DETAILS_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Integer status, Long servicePointId,Date beginDate, Date endDate,User user) {

        RPTKeFuCompleteTimeSearch searchCondition = setSearchCondition(status,servicePointId,beginDate,endDate);
        String reportTitle = DateUtils.formatDate(beginDate, "yyyy年MM月dd日") + "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日")+ "网点好评明细表";;
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.SERVICEPOINT_PRAISE_DETAILS_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTKeFuCompleteTimeSearch setSearchCondition(Integer status,Long servicePointId,Date beginDate, Date endDate){

        RPTKeFuCompleteTimeSearch search = new RPTKeFuCompleteTimeSearch();
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setServicePointId(servicePointId);
        search.setStatus(status);
        return search;
    }
}
