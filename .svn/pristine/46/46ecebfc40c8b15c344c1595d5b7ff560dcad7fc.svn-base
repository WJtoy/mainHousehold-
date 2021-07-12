package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuOrderPlanDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSKeFuOrderPlanDailyRptFeignFallbackFactory.class)
public interface MSKeFuOrderPlanDailyRptFeign {

    /**
     * 获取客服每日接单明细
     */
    @PostMapping("/keFuOrderPlan/keFuOrderPlanDaily")
    MSResponse<List<RPTKeFuOrderPlanDailyEntity>> getKeFuOrderPlanDailyList(@RequestBody RPTKeFuOrderPlanDailySearch search);
}
