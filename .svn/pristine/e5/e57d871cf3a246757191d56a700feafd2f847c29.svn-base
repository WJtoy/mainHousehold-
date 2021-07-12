package com.wolfking.jeesite.ms.providerrpt.servicepoint.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.fallback.SpServicePointPraiseDetailsRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = SpServicePointPraiseDetailsRptFeignFallbackFactory.class)
public interface SpServicePointPraiseDetailsRptFeign {

    @PostMapping("/spServicePointPraise/getSpServicePointPraiseDetailsList")
    MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> getServicePointPraiseDetailsList(@RequestBody RPTKeFuCompleteTimeSearch search);
}
