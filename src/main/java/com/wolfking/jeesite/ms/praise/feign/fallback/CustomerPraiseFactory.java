package com.wolfking.jeesite.ms.praise.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.praise.PraiseListModel;
import com.kkl.kklplus.entity.praise.PraisePageSearchModel;
import com.wolfking.jeesite.ms.praise.feign.CustomerPraiseFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerPraiseFactory implements FallbackFactory<CustomerPraiseFeign> {

    @Override
    public CustomerPraiseFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("CustomerPraiseFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new CustomerPraiseFeign() {
            @Override
            public MSResponse<MSPage<PraiseListModel>> approvedList(PraisePageSearchModel praisePageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
