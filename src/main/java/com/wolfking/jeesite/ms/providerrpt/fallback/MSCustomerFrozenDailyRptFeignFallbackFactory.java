package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCustomerFrozenDailyRptFeignFallbackFactory implements FallbackFactory<MSCustomerFrozenDailyRptFeign> {

    @Override
    public MSCustomerFrozenDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerFrozenDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerFrozenDailyRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> getCustomerFrozenDailyRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
