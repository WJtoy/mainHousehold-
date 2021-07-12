package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCompletedOrderDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCompletedOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCompletedOrderRptFeignFallbackFactory implements FallbackFactory<MSCompletedOrderRptFeign> {

    @Override
    public MSCompletedOrderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCompletedOrderNewRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCompletedOrderRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCompletedOrderDetailsEntity>> getCompletedOrderDetailsList(RPTCompletedOrderDetailsSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
