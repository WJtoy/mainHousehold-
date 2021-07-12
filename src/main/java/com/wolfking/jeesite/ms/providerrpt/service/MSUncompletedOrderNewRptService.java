package com.wolfking.jeesite.ms.providerrpt.service;

import cn.hutool.core.util.StrUtil;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTUncompletedQtyEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSUncompletedOrderNewRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSUncompletedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSUncompletedOrderNewRptService {
        @Autowired
        private MSUncompletedOrderNewRptFeign msUncompletedOrderNewRptFeign;

        @Autowired
        private MicroServicesProperties microServicesProperties;

        @Autowired
        private ReportExportTaskService reportExportTaskService;

        /**
         * 从rpt微服务中获取未完工单明细数据
         */
        public Page<RPTUncompletedQtyEntity> getUnCompletedOrderNewList(Page<RPTUncompletedQtyEntity> page, Date endDate, String quarter) {
            Page<RPTUncompletedQtyEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
            if (endDate != null) {
                RPTUncompletedOrderSearch search = new RPTUncompletedOrderSearch();
                search.setPageNo(page.getPageNo());
                search.setPageSize(page.getPageSize());
                endDate = DateUtils.getEndOfDay(endDate);
                search.setEndDate(endDate);
                search.setEndDt(endDate.getTime());
                if (StrUtil.isNotEmpty(quarter)) {
                    search.setQuarter(quarter);
                }
                if (microServicesProperties.getReport().getEnabled()) {
                    MSResponse<MSPage<RPTUncompletedQtyEntity>> msResponse = msUncompletedOrderNewRptFeign.getUnCompletedOrderNewList(search);
                    if (MSResponse.isSuccess(msResponse)) {
                        MSPage<RPTUncompletedQtyEntity> data = msResponse.getData();
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
        public void checkRptExportTask(Date endDate,String quarter, User user) {

            RPTUncompletedOrderSearch searchCondition = setSearchCondition(endDate, quarter);
            String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

            reportExportTaskService.checkRptExportTask(RPTReportEnum.UNCOMPLETED_QTY_RPT, user, searchConditionJson);
        }

        /**
         *创建报表导出任务
         */
        public void createRptExportTask(Date endDate, String quarter, User user) {

            RPTUncompletedOrderSearch searchCondition = setSearchCondition(endDate, quarter);
            String reportTitle = "未完工单汇总（截止到" + DateUtils.formatDate(endDate, "yyyy年MM月dd日") + "）";
            String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
            reportExportTaskService.createRptExportTask(RPTReportEnum.UNCOMPLETED_QTY_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
        }


        /**
         *设置筛选项的值
         */
        public RPTUncompletedOrderSearch setSearchCondition(Date endDate, String quarter){

            RPTUncompletedOrderSearch searchCondition = new RPTUncompletedOrderSearch();
            endDate = DateUtils.getEndOfDay(endDate);
            searchCondition.setEndDate(endDate);
            searchCondition.setEndDt(endDate.getTime());
            if (StrUtil.isNotEmpty(quarter)) {
                searchCondition.setQuarter(quarter);
            }

            return searchCondition;
        }
}
