package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTComplainStatisticsDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSComplainStatisticsRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSComplainStatisticsRptFeignFallbackFactory.class)
public interface MSComplainStatisticsRptFeign {

    /**
     * 每日投诉统计
     */
    @PostMapping("/complainStatistics/getComplainStatisticsDailyList")
    MSResponse<List<RPTComplainStatisticsDailyEntity>> getComplainStatisticsDailyList(@RequestBody RPTComplainStatisticsDailySearch search);

    /**
     * 每日投诉统计图表
     */
    @PostMapping("/complainStatistics/getComplainStatisticsDailyChart")
    MSResponse<Map<String, Object>> getComplainStatisticsDailyChart(@RequestBody RPTComplainStatisticsDailySearch search);
}
