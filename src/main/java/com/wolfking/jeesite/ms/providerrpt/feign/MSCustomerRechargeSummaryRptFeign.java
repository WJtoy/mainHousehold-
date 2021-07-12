package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerRechargeSummaryRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerRechargeSummaryRptFeignFallbackFactory.class)
public interface MSCustomerRechargeSummaryRptFeign {


    /**
     * 客戶充值
     */
    @PostMapping("/customerRechargeSummary/getCustomerRechargeSummary")
    MSResponse<List<RPTCustomerRechargeSummaryEntity>> getCustomerRechargeSummarys(@RequestBody RPTCustomerOrderPlanDailySearch search);
}
