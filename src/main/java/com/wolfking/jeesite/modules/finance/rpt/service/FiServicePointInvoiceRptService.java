package com.wolfking.jeesite.modules.finance.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointInvoiceRptFeign;
import com.wolfking.jeesite.ms.providerrpt.service.ReportExportTaskService;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class FiServicePointInvoiceRptService {

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private MSServicePointInvoiceRptFeign msServicePointInvoiceRptFeign;

    @Autowired
    private FiReportExportTaskService fiReportExportTaskService;


    public Page<RPTServicePointInvoiceEntity> getServicePointInvoiceRptDateNew(Page<RPTServicePointInvoiceEntity> page, Long servicePointId, String withdrawNo, Integer paymentType, Integer bank,
                                                                               Date beginDate, Date endDate, Date beginInvoiceDate, Date endInvoiceDate,Integer status) {
        Page<RPTServicePointInvoiceEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        RPTServicePointInvoiceSearch search = setSearch(servicePointId,withdrawNo,paymentType,bank,beginDate,endDate,beginInvoiceDate,endInvoiceDate,status);
        search.setPageNo(page.getPageNo());
        search.setPageSize(page.getPageSize());
        if (microServicesProperties.getReport().getEnabled()) {
            MSResponse<MSPage<RPTServicePointInvoiceEntity>> msResponse = msServicePointInvoiceRptFeign.getServicePointInvoiceList(search);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<RPTServicePointInvoiceEntity> data = msResponse.getData();
                returnPage.setCount(data.getRowCount());
                returnPage.setList(data.getList());
            }
        }

        return  returnPage;

    }

    public RPTServicePointInvoiceSearch setSearch(Long servicePointId, String withdrawNo, Integer paymentType, Integer bank,
                                                  Date beginDate, Date endDate, Date beginInvoiceDate, Date endInvoiceDate,Integer status){

        RPTServicePointInvoiceSearch search = new RPTServicePointInvoiceSearch();
        if(endDate !=null){
            endDate = DateUtils.getEndOfDay(endDate);
            search.setEndDate(endDate.getTime());
        }else {
            search.setEndDate(0L);
        }

        if(beginDate !=null){
            beginDate = DateUtils.getStartOfDay(beginDate);
            search.setBeginDate(beginDate.getTime());
        }else {
            search.setBeginDate(0L);
        }
        Date[] dates = OrderUtils.getQuarterDates(beginInvoiceDate,endInvoiceDate, 0, 0);
        List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
        if (quarters != null && quarters.size() > 0) {
            search.setQuarters(quarters);
        }
        endInvoiceDate = DateUtils.getEndOfDay(endInvoiceDate);
        beginInvoiceDate = DateUtils.getStartOfDay(beginInvoiceDate);
        search.setBank(bank);
        search.setPaymentType(paymentType);
        search.setWithdrawNo(withdrawNo);
        search.setServicePointId(servicePointId);
        search.setEndInvoiceDate(endInvoiceDate.getTime());
        search.setBeginInvoiceDate(beginInvoiceDate.getTime());
        search.setStatus(status);

        return  search;

    }

    /**
     * 检查报表导出
     */
    public void checkRptExportTask(Long servicePointId, String withdrawNo, Integer paymentType, Integer bank,
                                   Date beginDate, Date endDate, Date beginInvoiceDate, Date endInvoiceDate,Integer status, User user) {

        RPTServicePointInvoiceSearch searchCondition = setSearch(servicePointId,withdrawNo,paymentType,bank,beginDate,endDate,beginInvoiceDate,endInvoiceDate,status);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.checkRptExportTask(RPTReportEnum.FI_SERVICEPOINT_INVOICE_RPT, user, searchConditionJson);
    }

    /**
     * 创建报表导出任务
     */
    public void createRptExportTask(Long servicePointId, String withdrawNo, Integer paymentType, Integer bank,
                                    Date beginDate, Date endDate, Date beginInvoiceDate, Date endInvoiceDate,Integer status, User user) {

        RPTServicePointInvoiceSearch searchCondition = setSearch(servicePointId,withdrawNo,paymentType,bank,beginDate,endDate,beginInvoiceDate,endInvoiceDate,status);
        String reportTitle =  "网点付款清单（" + DateUtils.formatDate(beginInvoiceDate, "yyyy年MM月dd日") + "~"
                + DateUtils.formatDate(endInvoiceDate, "yyyy年MM月dd日") + "）";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        fiReportExportTaskService.createRptExportTask(RPTReportEnum.FI_SERVICEPOINT_INVOICE_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }



}
