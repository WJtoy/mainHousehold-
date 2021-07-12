package com.wolfking.jeesite.ms.lb.sb.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.lb.sd.LbOrderCancelApply;
import com.kkl.kklplus.entity.lb.sd.LbOrderCancelAudit;
import com.kkl.kklplus.entity.lb.sd.LbOrderCompleteApply;
import com.kkl.kklplus.entity.lb.sd.LbOrderStatus;
import com.wolfking.jeesite.ms.lb.sb.feign.LbOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LbOrderFeignFallbackFactory implements FallbackFactory<LbOrderFeign> {

    @Override
    public LbOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("LbOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new LbOrderFeign() {
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
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            @Override
            public MSResponse orderCancelApply(LbOrderCancelApply orderCancelApply) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse processApplyFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCancelAudit(LbOrderCancelAudit orderCancelAudit) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse processAuditFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderStatus(LbOrderStatus orderStatus) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCompleteApply(LbOrderCompleteApply orderCompleteApply) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse newMaterial(B2BMaterial material) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateAuditFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateDeliverFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }

}
