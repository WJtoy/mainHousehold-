package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerComplainEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerComplainSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerComplainRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSCustomerComplainRptFeignFallbackFactory implements FallbackFactory<MSCustomerComplainRptFeign> {
    @Override
    public MSCustomerComplainRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerComplainRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCustomerComplainRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerComplainEntity>> getCustomerComplainList(RPTCustomerComplainSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
