package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerFrozenDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtCustomerFrozenDailyRptFeignFallbackFactory implements FallbackFactory<CtCustomerFrozenDailyRptFeign> {

    @Override
    public CtCustomerFrozenDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("CtCustomerFrozenDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CtCustomerFrozenDailyRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> getCustomerFrozenDailyRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
