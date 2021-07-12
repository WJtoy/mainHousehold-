package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTKeFuAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.RptCustomerMonthOrderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerMonthDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuAverageOrderFeeRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSKeFuAverageOrderFeeRptFallbackFactory implements FallbackFactory<MSKeFuAverageOrderFeeRptFeign> {

    @Override
    public MSKeFuAverageOrderFeeRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuAverageOrderFeeRptFallbackFactory:{}",throwable.getMessage());
        }

         return new MSKeFuAverageOrderFeeRptFeign() {
             @Override
             public MSResponse<List<RPTKeFuAverageOrderFeeEntity>> getKeFuAverageOrderFeeList(RPTComplainStatisticsDailySearch search) {
                 return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
             }

             @Override
             public MSResponse<List<RPTKeFuAverageOrderFeeEntity>> getVipKeFuAverageOrderFeeList(RPTComplainStatisticsDailySearch search) {
                 return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
             }
         };
    }
}
