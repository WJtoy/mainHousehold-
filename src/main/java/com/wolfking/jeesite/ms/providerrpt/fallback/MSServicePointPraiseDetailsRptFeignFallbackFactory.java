package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointPraiseDetailsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MSServicePointPraiseDetailsRptFeignFallbackFactory  implements FallbackFactory<MSServicePointPraiseDetailsRptFeign> {

    @Override
    public MSServicePointPraiseDetailsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointPraiseDetailsRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointPraiseDetailsRptFeign() {
            @Override
            public MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> getServicePointPraiseDetailsList(RPTKeFuCompleteTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
