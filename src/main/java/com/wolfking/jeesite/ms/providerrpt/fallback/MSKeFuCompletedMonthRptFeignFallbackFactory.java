package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompletedMonthEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuCompletedMonthRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSKeFuCompletedMonthRptFeignFallbackFactory implements FallbackFactory<MSKeFuCompletedMonthRptFeign> {
    @Override
    public MSKeFuCompletedMonthRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuCompletedMonthRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKeFuCompletedMonthRptFeign() {
            @Override
            public MSResponse<List<RPTKeFuCompletedMonthEntity>> getKeFuCompletedMonthInfo(RPTGradedOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getKeFuCompletedMonthChartList(RPTGradedOrderSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
