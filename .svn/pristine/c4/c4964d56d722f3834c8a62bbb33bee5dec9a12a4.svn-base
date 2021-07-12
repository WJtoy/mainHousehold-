package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompletedMonthEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuCompletedMonthRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSKeFuCompletedMonthRptFeignFallbackFactory.class)
public interface MSKeFuCompletedMonthRptFeign {

    /**
     * 获取客服每月完工单明细
     */
    @PostMapping("/KeFuCompletedMonth/getKeFuCompletedMonthInfo")
    MSResponse<List<RPTKeFuCompletedMonthEntity>> getKeFuCompletedMonthInfo(@RequestBody RPTGradedOrderSearch search);

    /**
     * 获取客户每月下单图表
     */
    @PostMapping("/KeFuCompletedMonth/getKeFuCompletedMonthChartList")
    MSResponse<Map<String, Object>> getKeFuCompletedMonthChartList(@RequestBody RPTGradedOrderSearch search);
}
