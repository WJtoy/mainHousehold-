package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSDepositRechargeRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSRechargeRecordRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSDepositRechargFeignFallbackFactory implements FallbackFactory<MSDepositRechargeRptFeign> {
    @Override
    public MSDepositRechargeRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSDepositRechargFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSDepositRechargeRptFeign() {

            @Override
            public MSResponse<List<RPTCustomerRechargeSummaryEntity>> getDepositRechargeSummary(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<RPTRechargeRecordEntity>> getDepositRechargeDetails(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
