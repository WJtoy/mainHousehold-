package com.wolfking.jeesite.ms.providerrpt.customer.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerChargeSummaryMonthlyEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerWriteOffEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCancelledOrderSearch;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderSearch;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerChargeSearch;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.customer.fallback.CtCustomerChargeRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerChargeRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = CtCustomerChargeRptFeignFallbackFactory.class)
public interface CtCustomerChargeRptFeign {

    /**
     * 获取客户的工单数量与消费金额信息
     */
    @PostMapping("/customer/customerCharge/getCustomerChargeSummaryMonthly")
    MSResponse<RPTCustomerChargeSummaryMonthlyEntity> getCustomerChargeSummaryMonthly(@RequestBody RPTCustomerChargeSearch search);

    /**
     * 分页获取退单或取消工单明细
     */
    @PostMapping("/customer/customerCharge/getCompletedOrderList")
    MSResponse<MSPage<RPTCompletedOrderEntity>> getCompletedOrderList(@RequestBody RPTCompletedOrderSearch search);

    /**
     * 分页获取退单或取消工单明细
     */
    @PostMapping("/customer/customerCharge/getCancelledOrderList")
    MSResponse<MSPage<RPTCancelledOrderEntity>> getCancelledOrderList(@RequestBody RPTCancelledOrderSearch search);


    /**
     * 分页获取退单或取消工单明细
     */
    @PostMapping("/customer/customerCharge/getCustomerWriteOffList")
    MSResponse<MSPage<RPTCustomerWriteOffEntity>> getCustomerWriteOffList(@RequestBody RPTCustomerWriteOffSearch search);


}
