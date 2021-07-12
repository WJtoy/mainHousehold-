package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSign;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerSignSearch;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerSignRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCustomerSignRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSCustomerSignRptFeign msCustomerSignRptFeign;

    @Autowired
    private ReportExportTaskService reportExportTaskService;


    public Page<B2BSign> getCustomerSignList(Page<B2BSign> page, Long mallId, String mallName, String mobile,Integer status, Date beginDate, Date endDate) {
        Page<B2BSign> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        RPTCustomerSignSearch search = setSearch(mallId,mallName, mobile, status,beginDate,endDate);
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<B2BSign>> msResponse = msCustomerSignRptFeign.getCustomerSignList(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<B2BSign> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return  returnPage;

    }

    public RPTCustomerSignSearch setSearch(Long mallId, String mallName, String mobile, Integer status,Date beginDate, Date endDate){

        RPTCustomerSignSearch search = new RPTCustomerSignSearch();
        endDate = DateUtils.getEndOfDay(endDate);
        beginDate = DateUtils.getStartOfDay(beginDate);
        search.setMallId(mallId);
        search.setMallName(mallName);
        search.setMobile(mobile);
        search.setBeginDate(beginDate.getTime());
        search.setEndDate(endDate.getTime());
        search.setStatus(status);

        return  search;

    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask( Long mallId, String mallName, String mobile,Integer status, Date beginDate, Date endDate, User user) {

        RPTCustomerSignSearch searchCondition = setSearch(mallId,mallName, mobile, status,beginDate,endDate);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.checkRptExportTask(RPTReportEnum.CUSTOMER_SIGN_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long mallId, String mallName, String mobile,Integer status, Date beginDate, Date endDate, User user) {

        RPTCustomerSignSearch searchCondition = setSearch(mallId,mallName, mobile, status,beginDate,endDate);
        String reportTitle =  "客户签约（" + DateUtils.formatDate(beginDate, "yyyy年MM月dd日") + "~"
                + DateUtils.formatDate(endDate, "yyyy年MM月dd日") + "）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.CUSTOMER_SIGN_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }



}
