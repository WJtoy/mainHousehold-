package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDAttachment;
import com.wolfking.jeesite.ms.providermd.feign.MSAttachmentFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class MSAttachmentFeignFallbackFactory implements FallbackFactory<MSAttachmentFeign> {

    @Override
    public MSAttachmentFeign create(Throwable throwable) {
        return new MSAttachmentFeign() {

            @Override
            public MSResponse<Integer> insert(MDAttachment mdAttachment) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDAttachment>> findListByAttachmentIdsForMD(List<Long> ids) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
