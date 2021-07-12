package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSPointWriteRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MSPointWriteOffRptFallbackFactory implements FallbackFactory<MSPointWriteRptFeign> {
    @Override
    public MSPointWriteRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSPointWriteOffRptFallbackFactory:{}",throwable.getMessage());
        }

        return new MSPointWriteRptFeign() {
            @Override
            public MSResponse<MSPage<ServicePointChargeRptEntity>> getNrPointWriteOff(RPTServicePointWriteOffSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
