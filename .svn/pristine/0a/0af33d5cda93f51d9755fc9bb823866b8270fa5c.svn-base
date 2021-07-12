package com.wolfking.jeesite.ms.suning.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.suning.sd.SuningOrderModify;
import com.kkl.kklplus.entity.suning.sd.SuningOrderModifySrvtime;
import com.kkl.kklplus.entity.suning.sd.SuningOrderWorkStatus;
import com.wolfking.jeesite.ms.suning.sd.feign.SuningOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
public class SuningOrderFeignFallbackFactory implements FallbackFactory<SuningOrderFeign> {

    @Override
    public SuningOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("SuningOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new SuningOrderFeign() {
            @Override
            public MSResponse<MSPage<B2BOrder>> getList(B2BOrderSearchModel workcardSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse checkWorkcardProcessFlag(List<B2BOrderTransferResult> workcardIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateOrderTransferResult(List<B2BOrderTransferResult> orderTransferResults) {
                return null;
            }

            @Override
            public MSResponse cancelOrderTransition(B2BOrderTransferResult orderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderModify(SuningOrderModify suningOrderModify) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse moditySrvtime(SuningOrderModifySrvtime suningOrderModifySrvtime) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse workStatus(SuningOrderWorkStatus suningOrderWorkStatus) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<B2BOrder>> getListUnknownOrder(B2BOrderSearchModel b2BOrderSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateSystemIdAll() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
