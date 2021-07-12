package com.wolfking.jeesite.ms.viomi.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderComplainProcess;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.viomi.sd.*;
import com.wolfking.jeesite.ms.viomi.sd.feign.VioMiOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VioMiOrderFeignFallbackFactory implements FallbackFactory<VioMiOrderFeign> {

    @Override
    public VioMiOrderFeign create(Throwable throwable) {

        if (throwable != null) {
            log.error("VioMiOrderFeignFallbackFactory:{}", throwable.getMessage());
        }

        return new VioMiOrderFeign() {
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
            public MSResponse<Integer> planing(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> appointment(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> clockInHome(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> processComplete(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> applyFinished(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> orderReturnVisit(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse cancel(VioMiOrderCancel cancel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse saveLog(VioMiOrderRemark remark) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> orderConfirm(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> orderDismounting(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> orderServicePointSend(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateProcessFlag(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<ProductParts>> getProductParts(String product69Code, Long createById) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<FaultType>> getFaultType() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse getGradeSn(VioMiOrderSnCode vioMiOrderSnCode) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse complainCompleted(B2BOrderComplainProcess complainProcess) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> orderNeedValidate(VioMiOrderHandle vioMiOrderHandle) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse cancelValidateCode(VioMiOrderSendSms vioMiOrderSendSms) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

}
