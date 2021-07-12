package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTAreaOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSAreaOrderPlanDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSAreaOrderPlanDailyRptFeignFallbackFactory implements FallbackFactory<MSAreaOrderPlanDailyRptFeign> {


    @Override
    public MSAreaOrderPlanDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSAreaOrderPlanDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSAreaOrderPlanDailyRptFeign() {
            @Override
            public MSResponse<Map<String,List<RPTAreaOrderPlanDailyEntity>>> getAreaOrderPlanDailyList(RPTAreaOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
