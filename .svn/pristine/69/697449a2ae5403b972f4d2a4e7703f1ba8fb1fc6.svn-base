package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTComplainStatisticsDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSComplainStatisticsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSComplainStatisticsRptFeignFallbackFactory implements FallbackFactory<MSComplainStatisticsRptFeign> {
    @Override
    public MSComplainStatisticsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSComplainStatisticsRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSComplainStatisticsRptFeign() {
            @Override
            public MSResponse<List<RPTComplainStatisticsDailyEntity>> getComplainStatisticsDailyList(RPTComplainStatisticsDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getComplainStatisticsDailyChart(RPTComplainStatisticsDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
