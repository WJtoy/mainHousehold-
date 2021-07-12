package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTServicePointBaseInfoEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointBaseInfoSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSServicePointBaseRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSServicePointBaseRptFeignFallbackFactory.class)
public interface MSServicePointBaseRptFeign {

    /**
     * 获取网点基础资料
     */
    @PostMapping("/servicePointBase/getServicePointBasePage")
    MSResponse<MSPage<RPTServicePointBaseInfoEntity>> getServicePointBasePage(@RequestBody RPTServicePointBaseInfoSearch search);
}
