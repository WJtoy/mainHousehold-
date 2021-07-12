package com.wolfking.jeesite.ms.keg.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.keg.sd.KegOrderCompletedData;
import com.wolfking.jeesite.ms.keg.rpt.entity.KegSearchModel;
import com.wolfking.jeesite.ms.keg.rpt.feign.MSKegFailLogRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSKegFailLogRptFeignFallbackFactory implements FallbackFactory<MSKegFailLogRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSKegFailLogRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSKegFailLogRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSKegFailLogRptFeign() {


            @Override
            public MSResponse<MSPage<KegOrderCompletedData>> getFailLogList(KegSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse<KegOrderCompletedData> getLogById(Long id) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

            @Override
            public MSResponse closeLog(Long id, Long updateBy) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }


            @Override
            public MSResponse retryData(KegOrderCompletedData kegOrderCompletedData) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

        };
    }


}
