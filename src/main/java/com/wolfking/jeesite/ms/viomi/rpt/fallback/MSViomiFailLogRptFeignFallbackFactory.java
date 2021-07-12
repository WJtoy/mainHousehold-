package com.wolfking.jeesite.ms.viomi.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.viomi.sd.VioMiApiLog;
import com.kkl.kklplus.entity.viomi.sd.VioMiExceptionOrder;
import com.wolfking.jeesite.ms.viomi.rpt.entity.ViomiFailLogSearchModel;
import com.wolfking.jeesite.ms.viomi.rpt.feign.MSViomiFailLogRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MSViomiFailLogRptFeignFallbackFactory implements FallbackFactory<MSViomiFailLogRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSViomiFailLogRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSViomiFailLogRptFeignFallbackFactory:{}", throwable.getMessage());
        }

        return new MSViomiFailLogRptFeign() {


            @Override
            public MSResponse<MSPage<VioMiExceptionOrder>> getFailLogList(ViomiFailLogSearchModel model) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse<List<VioMiApiLog>> getLogById(Long id) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse<VioMiExceptionOrder> getOrderInfo(Long id) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse retryData(Long apiLogId, String operator, Long operatorId) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }



        };
    }


}
