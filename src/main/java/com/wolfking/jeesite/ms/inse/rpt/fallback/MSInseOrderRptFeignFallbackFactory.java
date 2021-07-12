package com.wolfking.jeesite.ms.inse.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.inse.rpt.entity.InseSearchModel;
import com.wolfking.jeesite.ms.inse.rpt.feign.MSInseOrderRptFeign;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSInseOrderRptFeignFallbackFactory implements FallbackFactory<MSInseOrderRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSInseOrderRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSInseOrderRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSInseOrderRptFeign() {
            @Override
            public MSResponse<MSPage<B2BOrderProcesslog>> getList(InseSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

        };
    }


}
