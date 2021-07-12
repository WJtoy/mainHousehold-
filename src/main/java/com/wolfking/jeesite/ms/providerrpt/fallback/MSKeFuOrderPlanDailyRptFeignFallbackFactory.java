package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuOrderPlanDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSKeFuOrderPlanDailyRptFeignFallbackFactory implements FallbackFactory<MSKeFuOrderPlanDailyRptFeign> {
    @Override
    public MSKeFuOrderPlanDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuOrderPlanDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKeFuOrderPlanDailyRptFeign() {
            @Override
            public MSResponse<List<RPTKeFuOrderPlanDailyEntity>> getKeFuOrderPlanDailyList(RPTKeFuOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }
}
