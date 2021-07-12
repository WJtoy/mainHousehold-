package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedQtyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSUncompletedOrderNewRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MSUncompletedOrderNewRptFeignFallbackFactory implements FallbackFactory<MSUncompletedOrderNewRptFeign> {
    @Override
    public MSUncompletedOrderNewRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSUncompletedOrderNewRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSUncompletedOrderNewRptFeign() {
            @Override
            public MSResponse<MSPage<RPTUncompletedQtyEntity>> getUnCompletedOrderNewList(RPTUncompletedOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
