package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFinanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerFinanceSearch;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerFinanceRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerFinanceRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtCustomerFinanceRptFeignFallbackFactory implements FallbackFactory<CtCustomerFinanceRptFeign> {
    @Override
    public CtCustomerFinanceRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerFinanceRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CtCustomerFinanceRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerFinanceEntity>> getCustomerFinanceRptList(RPTCustomerFinanceSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
