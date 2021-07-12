package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSPointWriteNewRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MSPointWriteOffNewRptFallbackFactory implements FallbackFactory<MSPointWriteNewRptFeign> {
    @Override
    public MSPointWriteNewRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSPointWriteOffNewRptFallbackFactory:{}",throwable.getMessage());
        }

        return new MSPointWriteNewRptFeign() {
            @Override
            public MSResponse<MSPage<ServicePointChargeRptEntity>> getNrPointWriteOffNew(RPTServicePointWriteOffSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
