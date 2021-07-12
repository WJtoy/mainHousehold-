package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.kkl.kklplus.entity.rpt.RPTTravelChargeRankEntity;
import com.kkl.kklplus.entity.rpt.search.RPTTravelChargeRankSearchCondition;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSTravelChargeRankRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSTravelChargeRankRptFeignFallbackFactory implements FallbackFactory<MSTravelChargeRankRptFeign> {


    @Override
    public MSTravelChargeRankRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSTravelChargeRankRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSTravelChargeRankRptFeign() {
            @Override
            public MSResponse<MSPage<RPTTravelChargeRankEntity>> getTravelChargeRankList(RPTTravelChargeRankSearchCondition rptSearchCondtion) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };

    }
}

