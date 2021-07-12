package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedQtyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSUncompletedOrderNewRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSUncompletedOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSUncompletedOrderNewRptFeignFallbackFactory.class)
public interface MSUncompletedOrderNewRptFeign {

    @PostMapping("/uncompletedOrderNew/getUncompletedOrderNewList")
    MSResponse<MSPage<RPTUncompletedQtyEntity>> getUnCompletedOrderNewList(@RequestBody RPTUncompletedOrderSearch search);
}
