package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointInvoiceSummaryRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSServicePointInvoiceSummaryRptFeignFallbackFactory.class)
public interface MSServicePointInvoiceSummaryRptFeign {

    /**
     * 网点付款汇总
     */
    @PostMapping("/servicePointPayment/servicePointPaymentSummary")
        MSResponse<List<RPTServicePointInvoiceEntity>> getServicePointPaymentSummary(@RequestBody RPTServicePointInvoiceSearch search);
}
