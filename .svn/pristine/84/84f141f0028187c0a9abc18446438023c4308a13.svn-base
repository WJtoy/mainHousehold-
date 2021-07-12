package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointInvoiceSummaryRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSServicePointInvoiceSummaryRptFeignFallbackFactory implements FallbackFactory<MSServicePointInvoiceSummaryRptFeign> {

    @Override
    public MSServicePointInvoiceSummaryRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointInvoiceSummaryRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointInvoiceSummaryRptFeign() {
            @Override
            public MSResponse<List<RPTServicePointInvoiceEntity>> getServicePointPaymentSummary(RPTServicePointInvoiceSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
