package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSUncompletedOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSUncompletedOrderRptFeignFallbackFactory implements FallbackFactory<MSUncompletedOrderRptFeign> {

    @Override
    public MSUncompletedOrderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSUncompletedOrderRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSUncompletedOrderRptFeign() {
            @Override
            public MSResponse<MSPage<RPTUncompletedOrderEntity>> getUnCompletedOrderList(RPTUncompletedOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
