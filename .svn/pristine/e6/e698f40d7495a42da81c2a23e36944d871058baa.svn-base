package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointBaseInfoEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointBaseInfoSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointBaseRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSServicePointBaseRptFeignFallbackFactory  implements FallbackFactory<MSServicePointBaseRptFeign> {
    @Override
    public MSServicePointBaseRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointBaseRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointBaseRptFeign() {
            @Override
            public MSResponse<MSPage<RPTServicePointBaseInfoEntity>> getServicePointBasePage(RPTServicePointBaseInfoSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
