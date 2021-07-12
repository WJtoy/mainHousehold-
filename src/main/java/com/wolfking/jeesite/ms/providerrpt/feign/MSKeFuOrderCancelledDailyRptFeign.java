package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderCancelledDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderCancelledDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuOrderCancelledDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "provider-rpt", fallbackFactory = MSKeFuOrderCancelledDailyRptFeignFallbackFactory.class)
public interface MSKeFuOrderCancelledDailyRptFeign {

    /**
     * 获取省市区每日下单明细
     */
    @PostMapping("/keFuOrderCancelled/keFuOrderCancelledDaily")
    MSResponse<List<RPTKeFuOrderCancelledDailyEntity>> getKeFuOrderCancelledDailyList(@RequestBody RPTKeFuOrderCancelledDailySearch search);
}
