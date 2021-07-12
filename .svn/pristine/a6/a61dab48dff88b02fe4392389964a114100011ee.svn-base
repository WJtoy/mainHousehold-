package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTDispatchOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSDispatchListRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSDispatchListRptFeignFallbackFactory.class)
public interface MSDispatchListRptFeign {

    /**
     * 获取接派单来源统计
     */
    @PostMapping("/dispatchList/dispatchListInformation")
    MSResponse<List<RPTDispatchOrderEntity>> getDispatchListInformation(@RequestBody RPTCompletedOrderDetailsSearch search);

    /**
     * 获取客户每月下单图表
     */
    @PostMapping("/dispatchList/getDispatchListInforChart")
    MSResponse<Map<String, Object>> getDispatchListInforChart(@RequestBody RPTCompletedOrderDetailsSearch search);
}
