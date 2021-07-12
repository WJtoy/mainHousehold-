package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAbnormalFinancialAuditEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSAbnormalFinancialReviewRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSAbnormalFinancialReviewRptFeignFallbackFactory implements FallbackFactory<MSAbnormalFinancialReviewRptFeign> {


    @Override
    public MSAbnormalFinancialReviewRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSAbnormalFinancialReviewRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSAbnormalFinancialReviewRptFeign() {
            @Override
            public MSResponse<List<RPTAbnormalFinancialAuditEntity>> getAbnormalFinancialList(RPTCustomerOrderPlanDailySearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
