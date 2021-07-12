package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompleteTimeEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuCompleteTimeRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSKeFuCompleteTimeRptFeignFallbackFactory.class)
public interface MSKeFuCompleteTimeRptFeign {

    /**
     * 获取客服完工时效
     */
    @PostMapping("/keFuCompleteTime/getKeFuCompleteTimeRptList")
    MSResponse<List<RPTKeFuCompleteTimeEntity>> getKeFuCompleteTimeRptList(@RequestBody RPTKeFuCompleteTimeSearch search);

    /**
     * 获取客服完工时效图表数据
     */
    @PostMapping("/keFuCompleteTime/getKeFuCompleteTimeChartList")
    MSResponse<Map<String, Object>> getKeFuCompleteTimeChartList(@RequestBody RPTKeFuCompleteTimeSearch search);
}
