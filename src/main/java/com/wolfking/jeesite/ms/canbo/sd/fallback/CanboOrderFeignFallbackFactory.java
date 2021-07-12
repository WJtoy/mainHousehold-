package com.wolfking.jeesite.ms.canbo.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.canbo.sd.*;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.canbo.sd.feign.CanboOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CanboOrderFeignFallbackFactory implements FallbackFactory<CanboOrderFeign> {

    @Override
    public CanboOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("CanboOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new CanboOrderFeign() {
            @Override
            public MSResponse<MSPage<B2BOrder>> getList(B2BOrderSearchModel orderSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse checkWorkcardProcessFlag(List<B2BOrderTransferResult> workcardIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateTransferResult(List<B2BOrderTransferResult> workcardTransferResults) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderPlanned(CanboOrderPlanned canboOrderPlanned) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderAppointed(CanboOrderAppointed canboOrderAppointed) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCompleted(CanboOrderCompleted canboOrderCompleted) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCancelled(CanboOrderCancelled canboOrderCancelled) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse saveLog(CanboOrderLog canboOrderLog) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

}
