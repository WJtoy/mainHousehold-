package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTOrderDailyWorkEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCreatedOrderRptFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSCreatedOrderRptFallbackFactory.class)
public interface MSCreatedOrderRptFeign {

    @PostMapping("/rptCreatedOrder/getCreatedOrderList")
    MSResponse<List<RPTOrderDailyWorkEntity>> getCreatedOrderList(@RequestBody RPTCompletedOrderDetailsSearch search);


}
