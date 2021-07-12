package com.wolfking.jeesite.ms.providerrpt.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.MSServicePointInvoiceRptFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSServicePointInvoiceRptFeignFallbackFactory implements FallbackFactory<MSServicePointInvoiceRptFeign> {
    @Override
    public MSServicePointInvoiceRptFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("MSServicePointInvoiceRptFeignFallbackFactory:{}",throwable.getMessage());
        }

        return new MSServicePointInvoiceRptFeign() {
            @Override
            public MSResponse<MSPage<RPTServicePointInvoiceEntity>> getServicePointInvoiceList(RPTServicePointInvoiceSearch search) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
