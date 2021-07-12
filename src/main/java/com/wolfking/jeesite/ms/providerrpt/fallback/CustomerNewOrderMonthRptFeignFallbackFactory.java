package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderMonthRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerNewOrderMonthRptFeignFallbackFactory implements FallbackFactory<CustomerNewOrderMonthRptFeign> {


    @Override
    public CustomerNewOrderMonthRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("CustomerNewOrderMonthRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CustomerNewOrderMonthRptFeign(){
            @Override
            public MSResponse<MSPage<RPTCustomerNewOrderDailyRptEntity>> getCustomerNewOrderMonthRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}

