package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuAreaEntity;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCrushCoverageRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuAreaRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSKeFuAreaRptFeignFallbackFactory implements FallbackFactory<MSKeFuAreaRptFeign> {
    @Override
    public MSKeFuAreaRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKeFuAreaRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKeFuAreaRptFeign() {
            @Override
            public MSResponse<List<RPTKeFuAreaEntity>> getKeFuAreaList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

}
