package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDDisableWord;
import com.wolfking.jeesite.ms.providermd.feign.MSDisableWordFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSDisableWordFeignFallbackFactory implements FallbackFactory<MSDisableWordFeign> {
    @Override
    public MSDisableWordFeign create(Throwable cause) {
        return new MSDisableWordFeign() {
            @Override
            public MSResponse<MSPage<MDDisableWord>> findList(MDDisableWord mdDisableWord) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> batchInsert(List<MDDisableWord> disableWords) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(MDDisableWord mdDisableWord) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
