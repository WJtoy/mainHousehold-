package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTTravelChargeRankEntity;
import com.kkl.kklplus.entity.rpt.search.RPTTravelChargeRankSearchCondition;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSTravelChargeRankRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * RPT微服务调用(远程费用排名)
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSTravelChargeRankRptFeignFallbackFactory.class)
public interface MSTravelChargeRankRptFeign {
    /**
     * 获取远程费用排名数据
     * @return
     */
    @GetMapping("/travelChargeRank/getList")
    MSResponse<MSPage<RPTTravelChargeRankEntity>> getTravelChargeRankList(@RequestBody RPTTravelChargeRankSearchCondition rptSearchCondtion);
}
