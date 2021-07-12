package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.ServicePointChargeRptEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSPointWriteOffRptFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSPointWriteOffRptFallbackFactory.class)
public interface MSPointWriteRptFeign {

    @PostMapping("/rptServicePointWrite/getNrPointWriteOff")
    MSResponse<MSPage<ServicePointChargeRptEntity>> getNrPointWriteOff(@RequestBody RPTServicePointWriteOffSearch search);


}
