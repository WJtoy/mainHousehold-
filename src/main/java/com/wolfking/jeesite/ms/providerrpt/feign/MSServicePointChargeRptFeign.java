package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointPaySummaryEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointPaySummarySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointChargeRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSServicePointChargeRptFeignFallbackFactory.class)
public interface MSServicePointChargeRptFeign {

    /**
     * 分页获取网点应付汇总数据
     */
    @PostMapping("/servicePointCharge/getServicePointPaySummaryRptList")
    MSResponse<MSPage<RPTServicePointPaySummaryEntity>> getServicePointPaySummaryRptList(@RequestBody RPTServicePointPaySummarySearch search);


    /**
     * 分页获取网点成本排名数据
     */
    @PostMapping("/servicePointCharge/getServicePointCostPerRptList")
    MSResponse<MSPage<RPTServicePointPaySummaryEntity>> getServicePointCostPerRptList(@RequestBody RPTServicePointPaySummarySearch search);
}
