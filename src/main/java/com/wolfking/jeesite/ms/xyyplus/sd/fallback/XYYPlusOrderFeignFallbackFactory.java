package com.wolfking.jeesite.ms.xyyplus.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderCancelApply;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderCancelAudit;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderCompleteApply;
import com.kkl.kklplus.entity.xyyplus.sd.XYYOrderStatus;
import com.wolfking.jeesite.ms.xyyplus.sd.feign.XYYPlusOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class XYYPlusOrderFeignFallbackFactory implements FallbackFactory<XYYPlusOrderFeign> {

    @Override
    public XYYPlusOrderFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("XYYPlusOrderFeignFallbackFactory:{}", throwable.getMessage());
        }

        return new XYYPlusOrderFeign() {
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
            public MSResponse orderCancelApply(XYYOrderCancelApply orderCancelApply) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse processApplyFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCancelAudit(XYYOrderCancelAudit orderCancelAudit) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse processAuditFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderStatus(XYYOrderStatus orderStatus) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCompleteApply(XYYOrderCompleteApply orderCompleteApply) {
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
