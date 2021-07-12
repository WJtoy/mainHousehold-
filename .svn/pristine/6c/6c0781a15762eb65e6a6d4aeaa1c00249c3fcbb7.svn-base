package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.RptCustomerMonthOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerMonthDailyRptFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuAverageOrderFeeRptFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSKeFuAverageOrderFeeRptFallbackFactory.class)
public interface MSKeFuAverageOrderFeeRptFeign {

    /**
     * 获取客服特殊费用(非KA)
     */
    @PostMapping("/keFuAverageOrderFee/keFuAverageOrderFeeList")
    MSResponse<List<RPTKeFuAverageOrderFeeEntity>> getKeFuAverageOrderFeeList(@RequestBody RPTComplainStatisticsDailySearch search);


    /**
     * 获取客服特殊费用（KA）
     */
    @PostMapping("/keFuAverageOrderFee/vipKeFuAverageOrderFeeList")
    MSResponse<List<RPTKeFuAverageOrderFeeEntity>> getVipKeFuAverageOrderFeeList(@RequestBody RPTComplainStatisticsDailySearch search);

}
