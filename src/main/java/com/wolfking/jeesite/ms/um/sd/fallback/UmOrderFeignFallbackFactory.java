package com.wolfking.jeesite.ms.um.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.um.sd.UmOrderAuditCharged;
import com.kkl.kklplus.entity.um.sd.UmOrderProcessLog;
import com.kkl.kklplus.entity.um.sd.UmOrderStatusUpdate;
import com.wolfking.jeesite.ms.um.sd.feign.UmOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UmOrderFeignFallbackFactory implements FallbackFactory<UmOrderFeign> {

    @Override
    public UmOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("UmOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new UmOrderFeign() {
            @Override
            public MSResponse saveProcesslog(UmOrderProcessLog umOrderProcessLog) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

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
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse statusUpdate(UmOrderStatusUpdate orderStatusUpdate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse auditCharged(UmOrderAuditCharged auditCharged) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
