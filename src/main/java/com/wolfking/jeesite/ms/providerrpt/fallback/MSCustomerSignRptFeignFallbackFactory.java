package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSign;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerSignSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerSignRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCustomerSignRptFeignFallbackFactory implements FallbackFactory<MSCustomerSignRptFeign> {
    @Override
    public MSCustomerSignRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerSignRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerSignRptFeign() {
            @Override
            public MSResponse<MSPage<B2BSign>> getCustomerSignList(RPTCustomerSignSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
