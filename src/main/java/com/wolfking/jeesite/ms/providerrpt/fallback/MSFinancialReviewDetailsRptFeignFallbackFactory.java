package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTFinancialReviewDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSFinancialReviewDetailsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSFinancialReviewDetailsRptFeignFallbackFactory implements FallbackFactory<MSFinancialReviewDetailsRptFeign> {
    @Override
    public MSFinancialReviewDetailsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSFinancialReviewDetailsRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSFinancialReviewDetailsRptFeign() {
            @Override
            public MSResponse<MSPage<RPTFinancialReviewDetailsEntity>> getFinancialReviewDetailsList(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
