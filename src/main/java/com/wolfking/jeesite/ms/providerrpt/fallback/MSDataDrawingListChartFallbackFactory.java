package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerComplainChartEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTDataDrawingListSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSDataDrawingListChartFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MSDataDrawingListChartFallbackFactory implements FallbackFactory<MSDataDrawingListChartFeign> {
    @Override
    public MSDataDrawingListChartFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSDataDrawingListChartFallbackFactory:{}",throwable.getMessage());
        }

        return new MSDataDrawingListChartFeign() {
            @Override
            public MSResponse<Map<String, Object>> getKeFuCompleteTimeChartList(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getOrderDataChartList(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getOrderQtyDailyChartData(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<RPTCustomerReminderEntity> getCustomerReminderChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<RPTCustomerComplainChartEntity> getCustomerComplainChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getServicePointQtyChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getServicePointStreetQtyChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<Double>> getIncurExpenseChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getOrderCrushQtyChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Object>> getOrderPlanDailyChart(RPTDataDrawingListSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
