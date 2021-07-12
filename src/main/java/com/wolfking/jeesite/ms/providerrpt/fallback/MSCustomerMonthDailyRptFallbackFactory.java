package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RptCustomerMonthOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerMonthDailyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSCustomerMonthDailyRptFallbackFactory implements FallbackFactory<MSCustomerMonthDailyRptFeign> {

    @Override
    public MSCustomerMonthDailyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCustomerMonthDailyRptFallbackFactory:{}",throwable.getMessage());
        }

         return new MSCustomerMonthDailyRptFeign() {
             @Override
             public MSResponse<List<RptCustomerMonthOrderEntity>> getCustomerMonthPlanDailyList(RPTCustomerOrderPlanDailySearch search) {
                 return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
             }

             @Override
             public MSResponse<Map<String, Object>> getCustomerMonthPlanChartList(RPTCustomerOrderPlanDailySearch search) {
                 return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
             }
         };
    }
}
