package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTExploitDetailEntity;
import com.kkl.kklplus.entity.rpt.search.RPTExploitDetailSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSExploitDetailRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSExploitDetailRptFeignFallbackFactory implements FallbackFactory<MSExploitDetailRptFeign> {
    @Override
    public MSExploitDetailRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSExploitDetailRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSExploitDetailRptFeign() {
            @Override
            public MSResponse<MSPage<RPTExploitDetailEntity>> getExploitDetailRptList(RPTExploitDetailSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
