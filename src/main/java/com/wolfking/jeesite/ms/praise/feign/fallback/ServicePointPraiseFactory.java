package com.wolfking.jeesite.ms.praise.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.praise.PraiseListModel;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.wolfking.jeesite.ms.praise.feign.ServicePointPraiseFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServicePointPraiseFactory implements FallbackFactory<ServicePointPraiseFeign> {

    @Override
    public ServicePointPraiseFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("ServicePointPraiseFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new ServicePointPraiseFeign() {

            @Override
            public MSResponse<MSPage<PraiseListModel>> pendingReviewList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<PraiseListModel>> approvedList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<PraiseListModel>> rejectList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<PraiseListModel>> findPraiseList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
