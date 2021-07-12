package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCompletedOrderNewRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCompletedOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCompletedOrderNewRptFeignFallbackFactory.class)
public interface MSCompletedOrderNewRptFeign {

    /**
     * 分页获取完工单明细
     */
    @PostMapping("/completedOrderNew/getCompletedOrderNewDetailsList")
    MSResponse<MSPage<RPTCompletedOrderDetailsEntity>> getCompletedOrderNewDetailsList(@RequestBody RPTCompletedOrderDetailsSearch search);


}
