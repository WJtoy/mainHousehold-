package com.wolfking.jeesite.ms.jd.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSJDOrderRptFeignFallbackFactory implements FallbackFactory<MSJDOrderRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSJDOrderRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSJDOrderRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSJDOrderRptFeign() {
            @Override
            public MSResponse<MSPage<B2BOrderProcesslog>> getList(JDSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

        };
    }


}
