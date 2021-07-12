package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTFinancialReviewDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSFinancialReviewDetailsRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSFinancialReviewDetailsRptFeignFallbackFactory.class)
public interface MSFinancialReviewDetailsRptFeign {

    @PostMapping("/financialReviewDetails/getFinancialReviewDetailsList")
    MSResponse<MSPage<RPTFinancialReviewDetailsEntity>> getFinancialReviewDetailsList(@RequestBody RPTCustomerOrderPlanDailySearch search);
}
