package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerFrozenMonthRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenMonthRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtCustomerFrozenMonthRptFeignFallbackFactory implements FallbackFactory<CtCustomerFrozenMonthRptFeign> {

    @Override
    public CtCustomerFrozenMonthRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerFrozenMonthRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CtCustomerFrozenMonthRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> getCustomerFrozenMonthRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}
