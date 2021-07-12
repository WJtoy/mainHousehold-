package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTAreaOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteEntity;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteSearch;
import com.kkl.kklplus.entity.rpt.search.RPTAreaOrderPlanDailySearch;

import com.wolfking.jeesite.ms.providerrpt.feign.MSAreaOrderPlanDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSEveryDayCompleteRPTFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Auther wj
 * @Date 2021/5/27 10:24
 */
@Component
@Slf4j
public class MSEveryDayCompleteRptFeignFallbackFactory implements FallbackFactory<MSEveryDayCompleteRPTFeign> {


    @Override
    public MSEveryDayCompleteRPTFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSAreaOrderPlanDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSEveryDayCompleteRPTFeign() {
            @Override
            public MSResponse<Map<String, List<RPTEveryDayCompleteEntity>>> getAreaOrderCompleteRateList(RPTEveryDayCompleteSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
