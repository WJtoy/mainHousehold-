package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerOrderPlanDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerOrderPlanDailyRptFeignFallbackFactory.class)
public interface MSCustomerOrderPlanDailyRptFeign {

    /**
     * 获取客户每日下单明细
     */
    @PostMapping("/customerOrderPlan/customerOrderPlanDaily")
    MSResponse<List<RPTCustomerOrderPlanDailyEntity>> getCustomerOrderPlanDailyList(@RequestBody RPTCustomerOrderPlanDailySearch search);

    /**
     * 获取客户每日下单图表
     */
    @PostMapping("/customerOrderPlan/customerOrderPlanDailyChart")
    MSResponse<Map<String, Object>> getCustomerOrderPlanChartList(@RequestBody RPTCustomerOrderPlanDailySearch search);


}
