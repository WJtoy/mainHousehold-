package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTGradeQtyDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSGradeQtyDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSGradeQtyDailyRptFeignFallbackFactory implements FallbackFactory<MSGradeQtyDailyRptFeign> {
    @Override
    public MSGradeQtyDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSGradeQtyDailyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSGradeQtyDailyRptFeign() {
            @Override
            public MSResponse<List<RPTGradeQtyDailyEntity>> getGradeQtyDailyList(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
