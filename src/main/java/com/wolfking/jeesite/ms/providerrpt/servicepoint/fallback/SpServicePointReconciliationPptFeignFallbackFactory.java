package com.wolfking.jeesite.ms.providerrpt.servicepoint.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.feign.SpServicePointReconciliationPptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpServicePointReconciliationPptFeignFallbackFactory implements FallbackFactory<SpServicePointReconciliationPptFeign> {
    @Override
    public SpServicePointReconciliationPptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("SpServicePointReconciliationPptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new SpServicePointReconciliationPptFeign() {
            @Override
            public MSResponse<MSPage<ServicePointChargeRptEntity>> getNrPointWriteOff(RPTServicePointWriteOffSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
