package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerRevenueEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerRevenueSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerRevenueRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSCustomerRevenueRptFeignFallbackFactory implements FallbackFactory<MSCustomerRevenueRptFeign> {
    @Override
    public MSCustomerRevenueRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerRevenueRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerRevenueRptFeign() {
            @Override
            public MSResponse<List<RPTCustomerRevenueEntity>> getCustomerRevenueRptList(RPTCustomerRevenueSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getCustomerRevenueChartList(RPTCustomerRevenueSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
