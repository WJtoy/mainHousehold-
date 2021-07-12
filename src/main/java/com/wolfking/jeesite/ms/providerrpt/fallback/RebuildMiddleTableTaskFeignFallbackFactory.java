package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTRebuildMiddleTableTaskEntity;
import com.kkl.kklplus.entity.rpt.search.RPTRebuildMiddleTableTaskSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.RebuildMiddleTableTaskFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RebuildMiddleTableTaskFeignFallbackFactory implements FallbackFactory<RebuildMiddleTableTaskFeign> {


    @Override
    public RebuildMiddleTableTaskFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("RebuildMiddleTableTaskFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new RebuildMiddleTableTaskFeign() {

            @Override
            public MSResponse<String> createRebuildMiddleTableTask(RPTRebuildMiddleTableTaskEntity taskEntity) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<RPTRebuildMiddleTableTaskEntity>> getRebuildMiddleTableTaskList(RPTRebuildMiddleTableTaskSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };

    }
}

