package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTChargeDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTComplainStatisticsDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSChargeDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSComplainStatisticsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSChargeDailyRptFeignFallbackFactory  implements FallbackFactory<MSChargeDailyRptFeign> {
    @Override
    public MSChargeDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSChargeDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSChargeDailyRptFeign() {
            @Override
            public MSResponse<List<RPTChargeDailyEntity>> getChargeDailyList(RPTServicePointWriteOffSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
