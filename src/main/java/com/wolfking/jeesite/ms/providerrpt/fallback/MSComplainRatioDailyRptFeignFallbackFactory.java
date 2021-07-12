package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSComplainRatioDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSComplainRatioDailyRptFeignFallbackFactory implements FallbackFactory<MSComplainRatioDailyRptFeign> {
    @Override
    public MSComplainRatioDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSComplainRatioDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSComplainRatioDailyRptFeign() {
            @Override
            public MSResponse<List<RPTAreaCompletedDailyEntity>> getProvinceComplainCompletedOrderRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTAreaCompletedDailyEntity>> getCityComplainCompletedOrderRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTAreaCompletedDailyEntity>> getAreaComplainCompletedOrderRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
