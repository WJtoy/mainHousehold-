package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteEntity;
import com.kkl.kklplus.entity.rpt.RPTEveryDayCompleteSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSEveryDayCompleteRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @Auther wj
 * @Date 2021/5/27 10:18
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSEveryDayCompleteRptFeignFallbackFactory.class)
public interface MSEveryDayCompleteRPTFeign {


    /**
     *
     */
    @PostMapping("/everyDayComplete/everyDayCompleteRate")
    MSResponse<Map<String, List<RPTEveryDayCompleteEntity>>> getAreaOrderCompleteRateList(@RequestBody RPTEveryDayCompleteSearch search);

}
