package com.wolfking.jeesite.ms.providerrpt.customer.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.customer.fallback.CtCustomerPraiseDetailsRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerPraiseDetailsRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = CtCustomerPraiseDetailsRptFeignFallbackFactory.class)
public interface CtCustomerPraiseDetailsRptFeign {

    @PostMapping("/customer/customerPraise/getCustomerPraiseDetailsList")
    MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> getCustomerPraiseDetailsList(@RequestBody RPTKeFuCompleteTimeSearch search);
}
