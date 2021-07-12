package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTMasterApplyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKeFuOrderPlanDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSMasterApplyRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSMasterApplyRptFeignFallbackFactory implements FallbackFactory<MSMasterApplyRptFeign> {
    @Override
    public MSMasterApplyRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSMasterApplyRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSMasterApplyRptFeign() {
            @Override
            public MSResponse<MSPage<RPTMasterApplyEntity>> getMasterApplyList(RPTComplainStatisticsDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
