package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointPaySummaryEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointPaySummarySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointChargeRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSServicePointChargeRptFeignFallbackFactory implements FallbackFactory<MSServicePointChargeRptFeign> {
    @Override
    public MSServicePointChargeRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointChargeRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointChargeRptFeign() {
            @Override
            public MSResponse<MSPage<RPTServicePointPaySummaryEntity>> getServicePointPaySummaryRptList(RPTServicePointPaySummarySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<RPTServicePointPaySummaryEntity>> getServicePointCostPerRptList(RPTServicePointPaySummarySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
