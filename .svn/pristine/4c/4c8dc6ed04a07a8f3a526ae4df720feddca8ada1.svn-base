package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTServicePointCoverageEntity;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointCoverageRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSServicePointCoverageRptFeignFallbackFactory implements FallbackFactory<MSServicePointCoverageRptFeign> {
    @Override
    public MSServicePointCoverageRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointCoverageRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointCoverageRptFeign() {
            @Override
            public MSResponse<List<RPTServicePointCoverageEntity>> getServicePointCoverageList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<RPTServicePointCoverageEntity>> getServicePointNoCoverageList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
