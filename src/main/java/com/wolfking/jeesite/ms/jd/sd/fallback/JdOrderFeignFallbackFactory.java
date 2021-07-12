package com.wolfking.jeesite.ms.jd.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.jd.sd.JdOrderAppointed;
import com.kkl.kklplus.entity.jd.sd.JdOrderCancelled;
import com.kkl.kklplus.entity.jd.sd.JdOrderCompleted;
import com.wolfking.jeesite.ms.jd.sd.feign.JdOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JdOrderFeignFallbackFactory implements FallbackFactory<JdOrderFeign> {

    @Override
    public JdOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("JdOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new JdOrderFeign() {
            @Override
            public MSResponse<MSPage<B2BOrder>> getList(B2BOrderSearchModel workcardSearchModel) {
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
            public MSResponse appointmentPush(JdOrderAppointed orderAppointmentPush) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse planned(JdOrderAppointed orderAppointed) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse appointJDOrder(JdOrderAppointed orderAppointed) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse installPush(JdOrderCompleted orderInstallPush) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse completeOrder(JdOrderCompleted orderCompleted) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse uninstallPush(JdOrderCancelled orderUninstallPush) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
            @Override
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateSystemIdAll() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<B2BOrder>> getListUnknownOrder(B2BOrderSearchModel b2BOrderSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse ignoreCancel(B2BOrderTransferResult result) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
