package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderDetailsEntity;
import com.kkl.kklplus.entity.rpt.common.RPTReportEnum;
import com.kkl.kklplus.entity.rpt.common.RPTReportTypeEnum;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.kkl.kklplus.entity.rpt.web.RPTArea;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCompletedOrderNewRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCompletedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.utils.RedisGsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSCompletedOrderNewRptService {

    @Autowired
    private MSCompletedOrderNewRptFeign msCompletedOrderNewRptFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    @Autowired
    private ReportExportTaskService reportExportTaskService;

    @Autowired
    private ProductCategoryService productCategoryService;

    /**
     * 从rpt微服务中获取订单完工明细数据
     */
    public Page<RPTCompletedOrderDetailsEntity> getCompletedOrderList(Page<RPTCompletedOrderDetailsEntity> page, Long customerId, Integer paymentType, List<Long> productCategoryIds, Date beginDate, Date endDate) {
        Page<RPTCompletedOrderDetailsEntity> returnPage = new Page<>(page.getPageNo(), page.getPageSize());
        if ( beginDate != null && endDate != null) {
            RPTCompletedOrderDetailsSearch search = new RPTCompletedOrderDetailsSearch();
            search.setPageNo(page.getPageNo());
            search.setPageSize(page.getPageSize());
            search.setCustomerId(customerId);
            search.setBeginDate(beginDate.getTime());
            search.setEndDate(endDate.getTime());
            search.setPaymentType(paymentType);
            search.setProductCategoryIds(productCategoryIds);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<MSPage<RPTCompletedOrderDetailsEntity>> msResponse = msCompletedOrderNewRptFeign.getCompletedOrderNewDetailsList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    MSPage<RPTCompletedOrderDetailsEntity> data = msResponse.getData();
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
    public void checkRptExportTask(Long customerId, Integer paymentType, List<Long> productCategoryIds,Date beginDate, Date endDate,User user) {

        RPTCompletedOrderDetailsSearch searchCondition = setSearchCondition(customerId,paymentType,productCategoryIds,beginDate,endDate);
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);

        reportExportTaskService.checkRptExportTask(RPTReportEnum.FINANCE_COMPLETED_ORDER_RPT, user, searchConditionJson);
    }

    /**
     *创建报表导出任务
     */
    public void createRptExportTask(Long customerId, Integer paymentType,List<Long> productCategoryIds,Date beginDate, Date endDate,User user) {

        RPTCompletedOrderDetailsSearch searchCondition = setSearchCondition(customerId,paymentType,productCategoryIds,beginDate,endDate);
        Customer customer = CustomerUtils.getCustomer(customerId);
        String customerName = customer == null ? "" : customer.getName();
        searchCondition.setPageNo(1);
        searchCondition.setPageSize(200000);
        List<ProductCategory> productCategoryLists= productCategoryService.findAllList();
        Map<Long, String> totalMap = productCategoryLists.stream().collect(Collectors.toMap(ProductCategory::getId,ProductCategory::getName));
        List<String> productCategoryName = Lists.newArrayList();
        String productCategoryNames;
        if(productCategoryIds.size() > 0){
            for(Long id : productCategoryIds){
               String name =  totalMap.get(id);
               productCategoryName.add(name);
            }
            productCategoryNames =  getListString(productCategoryName);

        }else {
            productCategoryName.addAll(totalMap.values());
            productCategoryNames = getListString(productCategoryName);
        }

        String reportTitle = customerName +productCategoryNames+ DateUtils.formatDate(beginDate, "yyyy年MM月dd日") + "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日")+ "订单完工明细表";
        String searchConditionJson = RedisGsonUtils.toJson(searchCondition);
        reportExportTaskService.createRptExportTask(RPTReportEnum.FINANCE_COMPLETED_ORDER_RPT, RPTReportTypeEnum.ORDER_REPORT, user, reportTitle, searchConditionJson);
    }


    /**
     *设置筛选项的值
     */
    public RPTCompletedOrderDetailsSearch setSearchCondition(Long customerId, Integer paymentType,List<Long> productCategoryIds,Date beginDate, Date endDate){

        RPTCompletedOrderDetailsSearch searchCondition = new RPTCompletedOrderDetailsSearch();
        searchCondition.setCustomerId(customerId);
        searchCondition.setBeginDate(beginDate.getTime());
        searchCondition.setEndDate(endDate.getTime());
        searchCondition.setPaymentType(paymentType);
        searchCondition.setProductCategoryIds(productCategoryIds);

        return searchCondition;
    }

    public String getListString(List<String> temp) {
        StringBuilder stringBuilder = new StringBuilder();
        String name;
        if(temp.size() == 1){
            name =  temp.get(0);
            stringBuilder.append("("+name + ")");

        }else{
            stringBuilder.append("(");
            for(int i = 0; i<=temp.size()-1 ; i++){
                name  = temp.get(i);
                if(i <temp.size()-1){
                    stringBuilder.append(name+"+");

                }else {
                    stringBuilder.append(name+")");
                }

            }
        }

        return stringBuilder.toString();
    }
}
