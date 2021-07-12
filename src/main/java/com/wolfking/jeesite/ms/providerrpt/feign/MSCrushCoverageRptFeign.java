package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCrushCoverageRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCrushCoverageRptFeignFallbackFactory.class)
public interface MSCrushCoverageRptFeign {

    /**
     * 突击区域
     */
    @PostMapping("/crushCoverage/getCrushCoverageList")
    MSResponse<List<RPTCrushCoverageEntity>> getCrushCoverageList(@RequestBody RPTGradedOrderSearch rptGradedOrderSearch);
}


