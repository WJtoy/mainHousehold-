package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSign;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerSignSearch;
import com.kkl.kklplus.entity.rpt.RPTServicePointInvoiceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointInvoiceSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerSignRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointInvoiceRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerSignRptFeignFallbackFactory.class)
public interface MSCustomerSignRptFeign {

    /**
     * 获取网点付款清单
     */
    @PostMapping("/customerSign/getCustomerSignList")
    MSResponse<MSPage<B2BSign>> getCustomerSignList(@RequestBody RPTCustomerSignSearch search);
}
