package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.search.RPTSpecialChargeSearchCondition;
import com.wolfking.jeesite.ms.providerrpt.feign.CustomerNewOrderDailyRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSSpecialChargeAreaRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSSpecialChargeAreaRptFeignFallbackFactory implements FallbackFactory<MSSpecialChargeAreaRptFeign> {


    @Override
    public MSSpecialChargeAreaRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSSpecialChargeAreaRptFeignFallbackFactory:{}",throwable.getMessage());
        }


        return new MSSpecialChargeAreaRptFeign() {
            @Override
            public MSResponse<List<RPTSpecialChargeAreaEntity>> getSpecialChargeList(RPTSpecialChargeSearchCondition searchCondition) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }

}

