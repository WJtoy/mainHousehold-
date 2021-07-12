package com.wolfking.jeesite.ms.providerrpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTReminderResponseTimeEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTReminderResponseTimeSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerReminderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSReminderResponseTimeRptService {
    @Autowired
    private MSCustomerReminderRptFeign msCustomerReminderRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    /**
     * 从rpt微服务中获取催单回复时效明细数据
     */
    public Page<RPTReminderResponseTimeEntity> getReminderResponseTimeList(Page<RPTReminderResponseTimeEntity> page, String orderNo, Long areaId, String reminderNo,
                                                                           Integer reminderTimes, Date beginDate, Date endDate) {
        Page<RPTReminderResponseTimeEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if (beginDate != null && endDate != null) {
            RPTReminderResponseTimeSearch search = new RPTReminderResponseTimeSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            if(orderNo != null && !orderNo.equals("")){
                search.setOrderNo(orderNo);
            }
            if(areaId != null && areaId != 0){
                search.setAreaId(areaId);
            }
            if(reminderNo != null && !reminderNo.equals("")){
                search.setReminderNo(reminderNo);
            }
            if(reminderTimes != null && reminderTimes != 0){
                search.setReminderTimes(reminderTimes);
            }
            search.setBeginDate(beginDate.getTime());
            search.setEndDate(endDate.getTime());

            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTReminderResponseTimeEntity>> msResponse = msCustomerReminderRptFeign.getReminderResponseTimeList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTReminderResponseTimeEntity> data = msResponse.getData();
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
    public void checkRptExportTask(String orderNo,Long areaId,String reminderNo,
                                   Integer reminderTimes,Date beginDate,Date endDate,User user) {

        RPTReminderResponseTimeSearch searchCondition = setSearchCondition(orderNo,areaId,reminderNo,reminderTimes,beginDate,endDate);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);

        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.REMINDER_RESPONSETIME_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(String orderNo,Long areaId,String reminderNo,
                                    Integer reminderTimes,Date beginDate,Date endDate,User user) {

        RPTReminderResponseTimeSearch searchCondition = setSearchCondition(orderNo,areaId,reminderNo,reminderTimes,beginDate,endDate);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String reportTitle = DateUtils.formatDate(beginDate, "yyyy年MM月dd日") + "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日")+ "催单回复时效报表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.REMINDER_RESPONSETIME_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTReminderResponseTimeSearch setSearchCondition(String orderNo,Long areaId,String reminderNo,
                                                            Integer reminderTimes,Date beginDate,Date endDate){

        RPTReminderResponseTimeSearch searchCondition = new RPTReminderResponseTimeSearch();

        if(orderNo != null && !orderNo.equals("")){
            searchCondition.setOrderNo(orderNo);
        }
        if(areaId != null && areaId != 0){
            searchCondition.setAreaId(areaId);
        }
        if(reminderNo != null && !reminderNo.equals("")){
            searchCondition.setReminderNo(reminderNo);
        }
        if(reminderTimes != null && reminderTimes != 0){
            searchCondition.setReminderTimes(reminderTimes);
        }
        searchCondition.setBeginDate(beginDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        return searchCondition;
    }
}
