package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSRechargeRecordRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSRechargeRecordRptFeignFallbackFactory.class)
public interface MSRechargeRecordRptFeign {

    @PostMapping("/getRechargeRecord/getRechargeRecordByPage")
    MSResponse<MSPage<RPTRechargeRecordEntity>> getRechargeRecordByPage(@RequestBody RPTCustomerOrderPlanDailySearch search);
}
