package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerFrozenDailyRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerFrozenMonthRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerFrozenMonthRptFeignFallbackFactory.class)
public interface MSCustomerFrozenMonthRptFeign {

    @GetMapping("/customerFrozenMonth/getCustomerFrozenMonthRptList")
    MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> getCustomerFrozenMonthRptList(@RequestBody RPTSearchCondtion rptSearchCondtion);
}
