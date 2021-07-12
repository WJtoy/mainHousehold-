package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTAreaCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTDevelopAverageOrderFeeEntity;
import com.kkl.kklplus.entity.rpt.RPTGradedOrderEntity;

import com.kkl.kklplus.entity.rpt.RPTKefuCompletedDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSGradedOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSGradedOrderRptFeignFallbackFactory implements FallbackFactory<MSGradedOrderRptFeign> {


    @Override
    public MSGradedOrderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSGradedOrderRptFeignFallbackFactory:{}",throwable.getMessage());
        }


        return new MSGradedOrderRptFeign() {
            @Override
            public MSResponse<MSPage<RPTGradedOrderEntity>> getOrderServicePointFeeRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTKefuCompletedDailyEntity>> getKefuCompletedOrderDailyRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTAreaCompletedDailyEntity>> getProvinceCompletedOrderRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTAreaCompletedDailyEntity>> getCityCompletedOrderRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTAreaCompletedDailyEntity>> getAreaCompletedOrderRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
            @Override
            public MSResponse<List<RPTDevelopAverageOrderFeeEntity>> getDevelopAverageFeeRpt(RPTGradedOrderSearch searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

}

