package com.wolfking.jeesite.ms.konka.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.feign.MSJDOrderRptFeign;
import com.wolfking.jeesite.ms.konka.rpt.entity.KonkaSearchModel;
import com.wolfking.jeesite.ms.konka.rpt.feign.MSKonkaOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSKonkaOrderRptFeignFallbackFactory implements FallbackFactory<MSKonkaOrderRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSKonkaOrderRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSKonkaOrderRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSKonkaOrderRptFeign() {
            @Override
            public MSResponse<MSPage<B2BOrderProcesslog>> getList(KonkaSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

        };
    }


}
