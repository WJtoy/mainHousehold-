package com.wolfking.jeesite.ms.inse.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.inse.sd.*;
import com.wolfking.jeesite.ms.inse.sd.feign.InseOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InseOrderFeignFallbackFactory implements FallbackFactory<InseOrderFeign> {

    @Override
    public InseOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("InseOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new InseOrderFeign() {
            @Override
            public MSResponse<MSPage<B2BOrder>> getList(B2BOrderSearchModel workcardSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse checkWorkcardProcessFlag(List<B2BOrderTransferResult> orderNos) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateTransferResult(List<B2BOrderTransferResult> workcardTransferResults) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse saveLog(InseOrderRemark orderRemark) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderPlanned(InseOrderPlanned inseOrderPlanned) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderAppointed(InseOrderAppointed inseOrderAppointed) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderVisited(InseOrderVisited inseOrderVisited) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCompleted(InseOrderCompleted inseOrderCompleted) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCancelled(InseOrderCancelled inseOrderCancelled) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateProcessFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse apply(InseOrderCancelApply cancelApply) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
