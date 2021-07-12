package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerReceivableSummaryEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerReceivableSummaryRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCustomerReceivableSummaryRptFeignFallbackFactory implements FallbackFactory<MSCustomerReceivableSummaryRptFeign> {
    @Override
    public MSCustomerReceivableSummaryRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerReceivableSummaryRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerReceivableSummaryRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerReceivableSummaryEntity>> getCustomerReceivableSummaryByPage(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
