package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerComplainEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerComplainSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerComplainRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerComplainRptFeignFallbackFactory.class)
public interface MSCustomerComplainRptFeign {

    /**
     * 分页获取开发明细数据
     */
    @PostMapping("/customerComplain/getCustomerComplainList")
    MSResponse<MSPage<RPTCustomerComplainEntity>> getCustomerComplainList(@RequestBody RPTCustomerComplainSearch search);
}
