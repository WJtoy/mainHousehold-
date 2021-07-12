package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSDepositRechargFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSRechargeRecordRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSDepositRechargFeignFallbackFactory.class)
public interface MSDepositRechargeRptFeign {

    /**
     * 客戶充值
     */
    @PostMapping("/depositRecharge/getDepositRechargeSummary")
    MSResponse<List<RPTCustomerRechargeSummaryEntity>> getDepositRechargeSummary(@RequestBody RPTCustomerOrderPlanDailySearch search);


    @PostMapping("/depositRecharge/getDepositRechargeDetails")
    MSResponse<MSPage<RPTRechargeRecordEntity>> getDepositRechargeDetails(@RequestBody RPTCustomerOrderPlanDailySearch search);
}
