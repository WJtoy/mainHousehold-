package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCancelledOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCancelledOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCancelledOrderRptFeignFallbackFactory.class)
public interface MSCancelledOrderRptFeign {

    /**
     * 分页获取退单或取消工单明细
     */
    @PostMapping("/cancelledOrder/getCancelledOrderList")
    MSResponse<MSPage<RPTCancelledOrderEntity>> getCancelledOrderList(@RequestBody RPTCancelledOrderSearch search);
}
