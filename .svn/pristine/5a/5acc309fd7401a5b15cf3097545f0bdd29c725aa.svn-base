package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuPraiseDetailsEntity;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuCompleteTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerPraiseDetailsRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerPraiseDetailsRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtCustomerPraiseDetailsRptFeignFallbackFactory implements FallbackFactory<CtCustomerPraiseDetailsRptFeign> {
    @Override
    public CtCustomerPraiseDetailsRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerPraiseDetailsRptFeignFallbackFactory:{}",throwable.getMessage());
        }


        return new CtCustomerPraiseDetailsRptFeign() {
            @Override
            public MSResponse<MSPage<RPTKeFuPraiseDetailsEntity>> getCustomerPraiseDetailsList(RPTKeFuCompleteTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
