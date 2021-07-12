package com.wolfking.jeesite.ms.joyoung.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDFailLogRptFeign;
import com.wolfking.jeesite.ms.joyoung.rpt.entity.JoyoungSearchModel;
import com.wolfking.jeesite.ms.joyoung.rpt.feign.MSJoyoungFailLogRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSJoyoungFailLogRptFeignFallbackFactory implements FallbackFactory<MSJoyoungFailLogRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSJoyoungFailLogRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSJoyoungFailLogRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSJoyoungFailLogRptFeign() {

            @Override
            public MSResponse<MSPage<B2BOrderProcesslog>> getFailLogList(JoyoungSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse<B2BOrderProcesslog> getLogById(Long id) {
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
