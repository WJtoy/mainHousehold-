package com.wolfking.jeesite.ms.providerrpt.customer.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTUncompletedOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTUncompletedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.customer.feign.CtUncompletedOrderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSUncompletedOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CtUncompletedOrderRptFeignFallbackFactory implements FallbackFactory<CtUncompletedOrderRptFeign> {

    @Override
    public CtUncompletedOrderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("CtUncompletedOrderRptFeignFallbackFactory:{}",throwable.getMessage());
        }

       return new CtUncompletedOrderRptFeign() {
           @Override
           public MSResponse<MSPage<RPTUncompletedOrderEntity>> getUnCompletedOrderList(RPTUncompletedOrderSearch search) {
               return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
           }
       };
    }
}
