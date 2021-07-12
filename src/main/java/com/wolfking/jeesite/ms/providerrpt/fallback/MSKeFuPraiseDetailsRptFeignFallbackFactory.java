package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuPraiseDetailsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSKeFuPraiseDetailsRptFeignFallbackFactory implements FallbackFactory<MSKeFuPraiseDetailsRptFeign> {
    @Override
    public MSKeFuPraiseDetailsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuPraiseDetailsRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKeFuPraiseDetailsRptFeign() {
            @Override
            public MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> getKeFuPraiseDetailsList(RPTKeFuCompleteTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
