package com.wolfking.jeesite.ms.tmall.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.rpt.B2BProcesslog;
import com.kkl.kklplus.entity.b2b.rpt.ProcesslogSearchModel;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderFailureLog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.feign.MSTmallOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSOrderRptFeignFallbackFactory implements FallbackFactory<MSTmallOrderRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSTmallOrderRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSOrderRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSTmallOrderRptFeign() {
            @Override
            public MSResponse<MSPage<B2BProcesslog>> getList(ProcesslogSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse<MSPage<B2BOrderFailureLog>> getFailLogList(B2BRptSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse<B2BOrderFailureLog> getLogById(Long id) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse closeLog(B2BRetryOperationData retryOperationData) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse retryData(B2BRetryOperationData retryOperationData) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

        };
    }


}
