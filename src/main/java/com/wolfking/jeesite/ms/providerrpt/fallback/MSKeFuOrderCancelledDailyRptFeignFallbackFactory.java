package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderCancelledDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderCancelledDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuOrderCancelledDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSKeFuOrderCancelledDailyRptFeignFallbackFactory implements FallbackFactory<MSKeFuOrderCancelledDailyRptFeign> {
    @Override
    public MSKeFuOrderCancelledDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuOrderCancelledDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKeFuOrderCancelledDailyRptFeign() {
            @Override
            public MSResponse<List<RPTKeFuOrderCancelledDailyEntity>> getKeFuOrderCancelledDailyList(RPTKeFuOrderCancelledDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
