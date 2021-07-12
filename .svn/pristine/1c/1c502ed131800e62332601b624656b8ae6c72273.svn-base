package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSComplainRatioDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSComplainRatioDailyRptFeignFallbackFactory.class)
public interface MSComplainRatioDailyRptFeign {

    @GetMapping("/complainOrder/provinceComplainCompleteOrder")
    MSResponse<List<RPTAreaCompletedDailyEntity>> getProvinceComplainCompletedOrderRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/complainOrder/cityComplainCompleteOrder")
    MSResponse<List<RPTAreaCompletedDailyEntity>> getCityComplainCompletedOrderRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/complainOrder/areaComplainCompleteOrder")
    MSResponse<List<RPTAreaCompletedDailyEntity>> getAreaComplainCompletedOrderRpt(@RequestBody RPTGradedOrderSearch searchCondition);
}
