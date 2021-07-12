package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTExploitDetailEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTExploitDetailSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSExploitDetailRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSExploitDetailRptService {
    @Autowired
    private MSExploitDetailRptFeign msExploitDetailRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取开发明细报表数据
     */
    public Page<RPTExploitDetailEntity> getExploitDetailList(Page<RPTExploitDetailEntity> page, String orderNo, Long customerId,Long areaId,Integer areaType,String userPhone,
                                                                    Date createBeginDate, Date createEndDate,Date closeBeginDate,Date closeEndDate,List<Long> productCategoryIds,Integer orderNoSearchType,Integer isPhone) {
        Page<RPTExploitDetailEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (closeBeginDate != null && closeEndDate != null) {
            RPTExploitDetailSearch search = new RPTExploitDetailSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setCustomerId(customerId);
            if(createBeginDate!= null){
                search.setCreateBeginDt(createBeginDate.getTime());
            }
            if(createEndDate != null){
                search.setCreateEndDt(createEndDate.getTime());
            }
            search.setCloseBeginDt(closeBeginDate.getTime());
            search.setCloseEndDt(closeEndDate.getTime());
            search.setOrderNo(orderNo);
            search.setAreaId(areaId);
            search.setAreaType(areaType);
            search.setUserPhone(userPhone);
            search.setOrderNoSearchType(orderNoSearchType);
            search.setIsPhone(isPhone);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTExploitDetailEntity>> msResponse = msExploitDetailRptFeign.getExploitDetailRptList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTExploitDetailEntity> data = msResponse.getData();
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
    public void checkRptExportTask(String orderNo, Long customerId,Long areaId,Integer areaType,String userPhone,Date createBeginDate,
                                   Date createEndDate,Date closeBeginDate,Date closeEndDate,List<Long> productCategoryIds,Integer orderNoSearchType,Integer isPhone,User user) {

        RPTExploitDetailSearch searchCondition = setSearchCondition(orderNo,customerId,areaId,areaType,userPhone,createBeginDate,createEndDate,
                closeBeginDate,closeEndDate,productCategoryIds,orderNoSearchType,isPhone);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.EXPLOIT_DETAIL_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(String orderNo, Long customerId,Long areaId,Integer areaType,String userPhone,
                                    Date createBeginDate, Date createEndDate,Date closeBeginDate,Date closeEndDate,List<Long> productCategoryIds,Integer orderNoSearchType,Integer isPhone,User user) {

        RPTExploitDetailSearch searchCondition = setSearchCondition(orderNo,customerId,areaId,areaType,userPhone,createBeginDate,createEndDate,
                closeBeginDate,closeEndDate,productCategoryIds,orderNoSearchType,isPhone);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String beginDate = DateUtils.formatDate(closeBeginDate, "yyyy年MM月dd日");
        String endDate = DateUtils.formatDate(closeEndDate, "yyyy年MM月dd日");
        String reportTitle = customerName + beginDate  + "~" +  endDate + "开发明细表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.EXPLOIT_DETAIL_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTExploitDetailSearch setSearchCondition(String orderNo, Long customerId,Long areaId, Integer areaType,String userPhone,
                                                     Date createBeginDate, Date createEndDate,Date closeBeginDate,Date closeEndDate,List<Long> productCategoryIds,Integer orderNoSearchType,Integer isPhone){

        RPTExploitDetailSearch search = new RPTExploitDetailSearch();
        search.setCustomerId(customerId);
        if(search.getCreateBeginDate() != null){
            search.setCreateBeginDt(createBeginDate.getTime());
        }
        if(search.getCreateEndDate() != null){
            search.setCreateEndDt(createEndDate.getTime());
        }
        search.setCloseBeginDt(closeBeginDate.getTime());
        search.setCloseEndDt(closeEndDate.getTime());
        search.setOrderNo(orderNo);
        search.setAreaId(areaId);
        search.setAreaType(areaType);
        search.setUserPhone(userPhone);
        search.setOrderNoSearchType(orderNoSearchType);
        search.setIsPhone(isPhone);
        search.setProductCategoryIds(productCategoryIds);
        return search;
    }
}
