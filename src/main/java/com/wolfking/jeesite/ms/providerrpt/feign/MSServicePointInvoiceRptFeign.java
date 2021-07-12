package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointInvoiceRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "provider-rpt", fallbackFactory = MSServicePointInvoiceRptFeignFallbackFactory.class)
public interface MSServicePointInvoiceRptFeign {

    /**
     * 获取网点付款清单
     */
    @PostMapping("/servicePointInvoice/getServicePointInvoiceList")
    MSResponse<MSPage<RPTServicePointInvoiceEntity>> getServicePointInvoiceList(@RequestBody RPTServicePointInvoiceSearch search);
}
