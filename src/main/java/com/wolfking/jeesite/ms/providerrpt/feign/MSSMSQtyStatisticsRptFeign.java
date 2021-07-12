package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSMSQtyStatisticsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTSMSQtyStatisticsSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSSMSQtyStatisticsRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSSMSQtyStatisticsRptFeignFallbackFactory.class)
public interface MSSMSQtyStatisticsRptFeign {

    /**
     * 获取短信数量统计
     */
    @PostMapping("/smsQtyStatistics/getSMSQtyStatisticsRptList")
    MSResponse<List<RPTSMSQtyStatisticsEntity>> getSMSQtyStatisticsRptList(@RequestBody RPTSMSQtyStatisticsSearch search);

    /**
     * 获取短信数量统计图表数据
     */
    @PostMapping("/smsQtyStatistics/getSMSQtyStatisticsChartList")
    MSResponse<Map<String, Object>> getSMSQtyStatisticsChartList(@RequestBody RPTSMSQtyStatisticsSearch search);
}
