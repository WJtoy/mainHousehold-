package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSUncompletedOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSUncompletedOrderRptFeignFallbackFactory.class)
public interface MSUncompletedOrderRptFeign {
    /**
     * 分页获取未完成工单明细
     */
    @PostMapping("/uncompletedOrder/getUncompletedOrderList")
    MSResponse<MSPage<RPTUncompletedOrderEntity>> getUnCompletedOrderList(@RequestBody RPTUncompletedOrderSearch search);
}
