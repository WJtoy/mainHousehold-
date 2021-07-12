package com.wolfking.jeesite.ms.joyoung.rpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.joyoung.rpt.entity.JoyoungSearchModel;
import com.wolfking.jeesite.ms.joyoung.rpt.feign.MSJoyoungOrderRptFeign;
import com.wolfking.jeesite.ms.konka.rpt.entity.KonkaSearchModel;
import com.wolfking.jeesite.ms.konka.rpt.feign.MSKonkaOrderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSJoyoungOrderRptFeignFallbackFactory implements FallbackFactory<MSJoyoungOrderRptFeign> {
    private static String errorMsg = "操作超时";


    @Override
    public MSJoyoungOrderRptFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSJoyoungOrderRptFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSJoyoungOrderRptFeign() {
            @Override
            public MSResponse<MSPage<B2BOrderProcesslog>> getList(JoyoungSearchModel processlogSearchModel) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }

        };
    }


}
