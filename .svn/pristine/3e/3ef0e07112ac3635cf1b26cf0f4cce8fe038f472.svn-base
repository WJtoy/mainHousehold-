package com.wolfking.jeesite.ms.providerrpt.customer.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.customer.fallback.CtCustomerFrozenDailyRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerFrozenDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = CtCustomerFrozenDailyRptFeignFallbackFactory.class)
public interface CtCustomerFrozenDailyRptFeign {

    @GetMapping("/customer/customerFrozenDaily/getCustomerFrozenDailyRptList")
    MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> getCustomerFrozenDailyRptList(@RequestBody RPTSearchCondtion rptSearchCondtion);
}
