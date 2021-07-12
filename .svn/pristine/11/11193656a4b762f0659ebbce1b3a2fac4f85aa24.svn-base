package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAbnormalFinancialAuditEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSAbnormalFinancialReviewRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSAbnormalFinancialReviewRptFeignFallbackFactory.class)
public interface MSAbnormalFinancialReviewRptFeign {

    @GetMapping("/abnormalFinancial/getAbnormalFinancialList")
    MSResponse<List<RPTAbnormalFinancialAuditEntity>> getAbnormalFinancialList(@RequestBody RPTCustomerOrderPlanDailySearch searchCondition);

}
