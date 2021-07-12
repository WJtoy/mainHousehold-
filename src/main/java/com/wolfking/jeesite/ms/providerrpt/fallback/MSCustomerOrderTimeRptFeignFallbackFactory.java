package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderTimeEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerOrderTimeRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSCustomerOrderTimeRptFeignFallbackFactory implements FallbackFactory<MSCustomerOrderTimeRptFeign> {
    @Override
    public MSCustomerOrderTimeRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerOrderTimeRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerOrderTimeRptFeign(){

            @Override
            public MSResponse<List<RPTCustomerOrderTimeEntity>> getCustomerOrderTimeRptList(RPTCustomerOrderTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getCustomerOrderTimeChartList(RPTCustomerOrderTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }
}
