package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuAreaEntity;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCrushCoverageRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuAreaRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSKeFuAreaRptFeignFallbackFactory.class)
public interface MSKeFuAreaRptFeign {

    /**
     * 客服区域
     */
    @PostMapping("/keFuArea/getKeFuAreaList")
    MSResponse<List<RPTKeFuAreaEntity>> getKeFuAreaList();
}


