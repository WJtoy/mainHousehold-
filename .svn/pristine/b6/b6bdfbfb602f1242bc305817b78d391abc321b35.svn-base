package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointBalanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointBalanceRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSServicePointBalanceRptFeignFallbackFactory.class)
public interface MSServicePointBalanceRptFeign {

    @PostMapping("/servicePointBalanceRpt/getServicePointBalanceByPage")
    MSResponse<MSPage<RPTServicePointBalanceEntity>> getServicePointBalanceByPage(@RequestBody RPTServicePointWriteOffSearch search);
}
