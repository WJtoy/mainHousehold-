package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerOrderPlanDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSCustomerOrderPlanDailyRptFeignFallbackFactory implements FallbackFactory<MSCustomerOrderPlanDailyRptFeign> {
    @Override
    public MSCustomerOrderPlanDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerOrderPlanDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerOrderPlanDailyRptFeign() {
            @Override
            public MSResponse<List<RPTCustomerOrderPlanDailyEntity>> getCustomerOrderPlanDailyList(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getCustomerOrderPlanChartList(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}
