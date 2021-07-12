package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTAreaOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSAreaOrderPlanDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSAreaOrderPlanDailyRptFeignFallbackFactory.class)
public interface MSAreaOrderPlanDailyRptFeign {

    /**
     * 获取省市区每日下单明细
     */
    @PostMapping("/areaOrderPlan/areaOrderPlanDaily")
    MSResponse<Map<String,List<RPTAreaOrderPlanDailyEntity>>> getAreaOrderPlanDailyList(@RequestBody RPTAreaOrderPlanDailySearch search);
}
