package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtCustomerNewOrderDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtCustomerNewOrderDailyRptFeignFallbackFactory implements FallbackFactory<CtCustomerNewOrderDailyRptFeign> {


    @Override
    public CtCustomerNewOrderDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("CtCustomerNewOrderDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new CtCustomerNewOrderDailyRptFeign() {
            @Override
            public MSResponse<MSPage<RPTCustomerNewOrderDailyRptEntity>> getCustomerNewOrderDailyRptList(RPTSearchCondtion rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}

