package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFrozenDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFrozenMonthRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCustomerFrozenMonthRptFeignFallbackFactory implements FallbackFactory<MSCustomerFrozenMonthRptFeign> {

    @Override
    public MSCustomerFrozenMonthRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerFrozenMonthRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerFrozenMonthRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerFrozenDailyEntity>> getCustomerFrozenMonthRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}
