package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerReceivableSummaryEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerReceivableSummaryRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerReceivableSummaryRptFeignFallbackFactory.class)
public interface MSCustomerReceivableSummaryRptFeign {

    /**
     * 获取客户应收汇总
     */
    @PostMapping("/customerReceivableSummary/getCustomerReceivableSummaryByPage")
    MSResponse<MSPage<RPTCustomerReceivableSummaryEntity>> getCustomerReceivableSummaryByPage(@RequestBody RPTCustomerOrderPlanDailySearch search);
}
