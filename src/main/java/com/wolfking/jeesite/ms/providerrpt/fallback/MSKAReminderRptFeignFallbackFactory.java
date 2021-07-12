package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.kkl.kklplus.entity.rpt.RPTReminderResponseTimeEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerReminderSearch;
import com.kkl.kklplus.entity.rpt.search.RPTReminderResponseTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSCustomerReminderRptFeign;
import com.wolfking.jeesite.ms.providerrpt.feign.MSKAReminderRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSKAReminderRptFeignFallbackFactory implements FallbackFactory<MSKAReminderRptFeign> {

    @Override
    public MSKAReminderRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSKAReminderRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSKAReminderRptFeign() {
            @Override
            public MSResponse<MSPage<RPTReminderResponseTimeEntity>> getReminderResponseTimeList(RPTReminderResponseTimeSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

}
