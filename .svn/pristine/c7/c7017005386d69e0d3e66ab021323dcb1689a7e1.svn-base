package com.wolfking.jeesite.ms.providerrpt.servicepoint.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.feign.SpServicePointPraiseDetailsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class SpServicePointPraiseDetailsRptFeignFallbackFactory implements FallbackFactory<SpServicePointPraiseDetailsRptFeign> {


    @Override
    public SpServicePointPraiseDetailsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("SpServicePointPraiseDetailsRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new SpServicePointPraiseDetailsRptFeign() {
            @Override
            public MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> getServicePointPraiseDetailsList(RPTKeFuCompleteTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
