package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTDispatchOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCompletedOrderDetailsSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSDispatchListRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSDispatchListRptFeignFallbackFactory implements FallbackFactory<MSDispatchListRptFeign> {
    @Override
    public MSDispatchListRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSDispatchListRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSDispatchListRptFeign(){

            @Override
            public MSResponse<List<RPTDispatchOrderEntity>> getDispatchListInformation(RPTCompletedOrderDetailsSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getDispatchListInforChart(RPTCompletedOrderDetailsSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
