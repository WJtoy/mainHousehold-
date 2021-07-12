package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RptCustomerMonthOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerMonthDailyRptFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerMonthDailyRptFallbackFactory.class)
public interface MSCustomerMonthDailyRptFeign {

    /**
     * 获取客户每月下单明细
     */
    @PostMapping("/customerMonthPlan/customerMonthPlanDaily")
    MSResponse<List<RptCustomerMonthOrderEntity>> getCustomerMonthPlanDailyList(@RequestBody RPTCustomerOrderPlanDailySearch search);

    /**
     * 获取客户每月下单图表
     */
    @PostMapping("/customerMonthPlan/getCustomerMonthPlanChartList")
    MSResponse<Map<String, Object>> getCustomerMonthPlanChartList(@RequestBody RPTCustomerOrderPlanDailySearch search);

}
