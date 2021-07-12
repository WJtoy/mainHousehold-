package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTServicePointCoverageEntity;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointCoverageRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSServicePointCoverageRptFeignFallbackFactory.class)
public interface MSServicePointCoverageRptFeign {

    /**
     * 获取网点覆盖
     */
    @PostMapping("/ServicePointCoverage/getServicePointCoverageList")
    MSResponse<List<RPTServicePointCoverageEntity>> getServicePointCoverageList();

    /**
     * 获取网点无覆盖
     */
    @PostMapping("/ServicePointCoverage/getServicePointNoCoverageList")
    MSResponse<List<RPTServicePointCoverageEntity>> getServicePointNoCoverageList();
}
