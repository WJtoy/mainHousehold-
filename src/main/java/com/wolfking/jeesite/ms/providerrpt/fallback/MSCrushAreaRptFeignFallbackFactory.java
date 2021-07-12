package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushAreaEntity;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.search.RPTSpecialChargeSearchCondition;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCrushAreaRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSSpecialChargeAreaRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSCrushAreaRptFeignFallbackFactory implements FallbackFactory<MSCrushAreaRptFeign> {


    @Override
    public MSCrushAreaRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSCrushAreaRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSCrushAreaRptFeign() {
            @Override
            public MSResponse<List<RPTCrushAreaEntity>> getCrushList(RPTSpecialChargeSearchCondition searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }

}

