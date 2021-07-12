package com.wolfking.jeesite.ms.providerrpt.fallback;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTRechargeRecordEntity;
import com.kkl.kklplus.entity.rpt.RPTServicePointBalanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSRechargeRecordRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSRechargeRecordRptFeignFallbackFactory implements FallbackFactory<MSRechargeRecordRptFeign> {
    @Override
    public MSRechargeRecordRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSRechargeRecordRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSRechargeRecordRptFeign() {
            @Override
            public MSResponse<MSPage<RPTRechargeRecordEntity>> getRechargeRecordByPage(RPTCustomerOrderPlanDailySearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
