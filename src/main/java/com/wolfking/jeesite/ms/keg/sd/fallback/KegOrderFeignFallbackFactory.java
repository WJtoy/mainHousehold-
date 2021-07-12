package com.wolfking.jeesite.ms.keg.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.keg.sd.KegOrder;
import com.kkl.kklplus.entity.keg.sd.KegOrderCompleted;
import com.wolfking.jeesite.ms.keg.sd.feign.KegOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KegOrderFeignFallbackFactory implements FallbackFactory<KegOrderFeign> {

    @Override
    public KegOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("KegOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new KegOrderFeign() {

            @Override
            public MSResponse newOrder(KegOrder kegOrder) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse completed(KegOrderCompleted completed) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
