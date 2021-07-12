package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSalesPerfomanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerPerformanceRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSCustomerPerformanceRptFeignFallbackFactory  implements FallbackFactory<MSCustomerPerformanceRptFeign> {
    @Override
    public MSCustomerPerformanceRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerPerformanceRptFeignFallbackFactory:{}",throwable.getMessage());
        }


        return new MSCustomerPerformanceRptFeign() {
            @Override
            public MSResponse<List<RPTSalesPerfomanceEntity>> getSalesPerformanceList(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTSalesPerfomanceEntity>> getCustomerPerformanceList(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
