package com.wolfking.jeesite.ms.praise.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.praise.PraiseAppListModel;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.wolfking.jeesite.ms.praise.feign.AppPraiseFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppPraiseFactory implements FallbackFactory<AppPraiseFeign> {

    @Override
    public AppPraiseFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("AppPraiseFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new AppPraiseFeign() {

            @Override
            public MSResponse<MSPage<PraiseAppListModel>> rejectAppList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<PraiseAppListModel>> findPraiseList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
