package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuCompleteTimeEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuCompleteTimeRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSKeFuCompleteTimeRptFeignFallbackFactory implements FallbackFactory<MSKeFuCompleteTimeRptFeign> {
    @Override
    public MSKeFuCompleteTimeRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuCompleteTimeRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKeFuCompleteTimeRptFeign(){

            @Override
            public MSResponse<List<RPTKeFuCompleteTimeEntity>> getKeFuCompleteTimeRptList(RPTKeFuCompleteTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getKeFuCompleteTimeChartList(RPTKeFuCompleteTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }
}
