package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSalesPerfomanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerPerformanceRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerPerformanceRptFeignFallbackFactory.class)
public interface MSCustomerPerformanceRptFeign {

    /**
     * 获取业务员业绩
     */
    @PostMapping("/salesPerformanceReport/getSalesPerformanceList")
    MSResponse<List<RPTSalesPerfomanceEntity>> getSalesPerformanceList(@RequestBody RPTCustomerOrderPlanDailySearch search);

    /**
     * 获取业务员业绩明細
     */
    @PostMapping("/salesPerformanceReport/getCustomerPerformanceList")
    MSResponse<List<RPTSalesPerfomanceEntity>> getCustomerPerformanceList(@RequestBody RPTCustomerOrderPlanDailySearch search);
}
