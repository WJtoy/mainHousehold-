package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
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
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerChargeRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MSCustomerChargeRptFeignFallbackFactory implements FallbackFactory<MSCustomerChargeRptFeign> {

    @Override
    public MSCustomerChargeRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerChargeRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerChargeRptFeign() {

            @Override
            public MSResponse<RPTCustomerChargeSummaryMonthlyEntity> getCustomerChargeSummaryMonthly(RPTCustomerChargeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<RPTCompletedOrderEntity>> getCompletedOrderList(RPTCompletedOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<RPTCancelledOrderEntity>> getCancelledOrderList(RPTCancelledOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            @Override
            public MSResponse<MSPage<RPTCustomerWriteOffEntity>> getCustomerWriteOffList(RPTCustomerWriteOffSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}
