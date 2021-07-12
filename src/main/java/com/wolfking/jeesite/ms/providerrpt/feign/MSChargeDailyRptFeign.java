package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTChargeDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTServicePointWriteOffSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSChargeDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSChargeDailyRptFeignFallbackFactory.class )
public interface MSChargeDailyRptFeign {

    /**
     * 每日对账统计
     */
    @PostMapping("/chargeDailyList/getChargeDailyList")
    MSResponse<List<RPTChargeDailyEntity>> getChargeDailyList(@RequestBody RPTServicePointWriteOffSearch search);

}
