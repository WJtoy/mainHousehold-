package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCancelledOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCancelledOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCancelledOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCancelledOrderRptFeignFallbackFactory implements FallbackFactory<MSCancelledOrderRptFeign> {

    @Override
    public MSCancelledOrderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCancelledOrderRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCancelledOrderRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCancelledOrderEntity>> getCancelledOrderList(RPTCancelledOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
