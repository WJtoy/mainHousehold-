package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerRechargeSummaryEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerRechargeSummaryRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSCustomerRechargeSummaryRptFeignFallbackFactory  implements FallbackFactory<MSCustomerRechargeSummaryRptFeign> {
    @Override
    public MSCustomerRechargeSummaryRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerRechargeSummaryRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerRechargeSummaryRptFeign() {
            @Override
            public MSResponse<List<RPTCustomerRechargeSummaryEntity>> getCustomerRechargeSummarys(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
