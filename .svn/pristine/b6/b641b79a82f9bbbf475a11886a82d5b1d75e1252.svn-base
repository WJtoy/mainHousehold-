package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTOrderDailyWorkEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCreatedOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Component
@Slf4j
public class MSCreatedOrderRptFallbackFactory implements FallbackFactory<MSCreatedOrderRptFeign> {
    @Override
    public MSCreatedOrderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCreatedOrderRptFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCreatedOrderRptFeign(){
            @Override
            public MSResponse<List<RPTOrderDailyWorkEntity>> getCreatedOrderList(@RequestBody RPTCompletedOrderDetailsSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
