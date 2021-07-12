package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerRevenueEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerRevenueSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerRevenueRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerRevenueRptFeignFallbackFactory.class)
public interface MSCustomerRevenueRptFeign {

    /**
     * 获取客户营收统计
     */
    @PostMapping("/customerRevenue/getCustomerRevenueRpt")
    MSResponse<List<RPTCustomerRevenueEntity>> getCustomerRevenueRptList(@RequestBody RPTCustomerRevenueSearch search);

    /**
     * 获取客户营收图表数据
     */
    @PostMapping("/customerRevenue/getCustomerRevenueChartList")
    MSResponse<Map<String, Object>> getCustomerRevenueChartList(@RequestBody RPTCustomerRevenueSearch search);
}
