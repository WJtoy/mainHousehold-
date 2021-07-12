package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerNewOrderMonthRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderMonthRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtCustomerNewOrderMonthRptFeignFallbackFactory implements FallbackFactory<CtCustomerNewOrderMonthRptFeign> {


    @Override
    public CtCustomerNewOrderMonthRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("CustomerNewOrderMonthRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CtCustomerNewOrderMonthRptFeign(){
            @Override
            public MSResponse<MSPage<RPTCustomerNewOrderDailyRptEntity>> getCustomerNewOrderMonthRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}

