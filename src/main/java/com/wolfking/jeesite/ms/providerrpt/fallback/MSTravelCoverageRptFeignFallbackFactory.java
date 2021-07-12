package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.search.RPTGradedOrderSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSTravelCoverageRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSTravelCoverageRptFeignFallbackFactory implements FallbackFactory<MSTravelCoverageRptFeign> {
    @Override
    public MSTravelCoverageRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSTravelCoverageRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSTravelCoverageRptFeign() {
            @Override
            public MSResponse<List<RPTCrushCoverageEntity>> getTravelCoverageList(RPTGradedOrderSearch rptGradedOrderSearch) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
