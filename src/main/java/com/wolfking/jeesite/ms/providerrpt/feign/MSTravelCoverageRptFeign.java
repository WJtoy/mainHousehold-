package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSTravelCoverageRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSTravelCoverageRptFeignFallbackFactory.class)
public interface MSTravelCoverageRptFeign {

    /**
     * 远程区域
     */
    @PostMapping("/travelCoverage/getTravelCoverageList")
    MSResponse<List<RPTCrushCoverageEntity>> getTravelCoverageList(@RequestBody RPTGradedOrderSearch rptGradedOrderSearch);
}


