package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;

import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerNewOrderDailyRptFeignFallbackFactory implements FallbackFactory<CustomerNewOrderDailyRptFeign> {


    @Override
    public CustomerNewOrderDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("CustomerNewOrderDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CustomerNewOrderDailyRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerNewOrderDailyRptEntity>> getCustomerNewOrderDailyRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}

