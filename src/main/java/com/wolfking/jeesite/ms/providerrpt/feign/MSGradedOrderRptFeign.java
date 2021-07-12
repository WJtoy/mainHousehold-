package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTDevelopAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.RPTGradedOrderEntity;

import com.kkl.kklplus.entity.rpt.RPTKefuCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;

import com.wolfking.jeesite.ms.providerrpt.fallback.MSGradedOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSGradedOrderRptFeignFallbackFactory.class)
public interface MSGradedOrderRptFeign {


    @GetMapping("/gradedOrder/orderServicePointFee")
    MSResponse<MSPage<RPTGradedOrderEntity>> getOrderServicePointFeeRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/gradedOrder/kefuDailyCompleted")
    MSResponse<List<RPTKefuCompletedDailyEntity>> getKefuCompletedOrderDailyRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/gradedOrder/provinceCompleteOrder")
    MSResponse<List<RPTAreaCompletedDailyEntity>> getProvinceCompletedOrderRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/gradedOrder/cityCompleteOrder")
    MSResponse<List<RPTAreaCompletedDailyEntity>> getCityCompletedOrderRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/gradedOrder/areaCompleteOrder")
    MSResponse<List<RPTAreaCompletedDailyEntity>> getAreaCompletedOrderRpt(@RequestBody RPTGradedOrderSearch searchCondition);

    @GetMapping("/gradedOrder/developAverageFee")
    MSResponse<List<RPTDevelopAverageOrderFeeEntity>> getDevelopAverageFeeRpt(@RequestBody RPTGradedOrderSearch searchCondition);



}
