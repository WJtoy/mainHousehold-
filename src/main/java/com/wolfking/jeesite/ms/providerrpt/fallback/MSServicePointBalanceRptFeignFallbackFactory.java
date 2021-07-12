package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointBalanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointBalanceRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSServicePointBalanceRptFeignFallbackFactory implements FallbackFactory<MSServicePointBalanceRptFeign> {
    @Override
    public MSServicePointBalanceRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointBalanceRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointBalanceRptFeign() {
            @Override
            public MSResponse<MSPage<RPTServicePointBalanceEntity>> getServicePointBalanceByPage(RPTServicePointWriteOffSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
