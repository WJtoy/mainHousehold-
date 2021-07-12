package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSMSQtyStatisticsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTSMSQtyStatisticsSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSSMSQtyStatisticsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSSMSQtyStatisticsRptFeignFallbackFactory implements FallbackFactory<MSSMSQtyStatisticsRptFeign> {
    @Override
    public MSSMSQtyStatisticsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSSMSQtyStatisticsRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSSMSQtyStatisticsRptFeign(){

            @Override
            public MSResponse<List<RPTSMSQtyStatisticsEntity>> getSMSQtyStatisticsRptList(RPTSMSQtyStatisticsSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getSMSQtyStatisticsChartList(RPTSMSQtyStatisticsSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
