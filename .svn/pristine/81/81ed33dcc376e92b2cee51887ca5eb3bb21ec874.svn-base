package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderTimeEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerOrderTimeRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerOrderTimeRptFeignFallbackFactory.class)
public interface MSCustomerOrderTimeRptFeign {

    /**
     * 获取客户工单时效
     */
    @PostMapping("/customerOrderTime/getCustomerOrderTimeRptList")
    MSResponse<List<RPTCustomerOrderTimeEntity>> getCustomerOrderTimeRptList(@RequestBody RPTCustomerOrderTimeSearch search);

    /**
     * 获取客户工单时效图表数据
     */
    @PostMapping("/customerOrderTime/getCustomerOrderTimeChartList")
    MSResponse<Map<String, Object>> getCustomerOrderTimeChartList(@RequestBody RPTCustomerOrderTimeSearch search);
}
