package com.wolfking.jeesite.ms.providerrpt.servicepoint.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.fallback.SpServicePointReconciliationNewRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.fallback.SpServicePointReconciliationPptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = SpServicePointReconciliationNewRptFeignFallbackFactory.class)
public interface SpServicePointReconciliationNewRptFeign {

    @PostMapping("/NewServciePointReconciliation/getNewServciePointReconciliation")
    MSResponse<MSPage<ServicePointChargeRptEntity>> getNewNrPointWriteOff(@RequestBody RPTServicePointWriteOffSearch search);
}
