package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTKeFuOrderPlanDailyEntity;
import com.kkl.kklplus.entity.rpt.RPTMasterApplyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTComplainStatisticsDailySearch;
import com.kkl.kklplus.entity.rpt.search.RPTKeFuOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSKeFuOrderPlanDailyRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSMasterApplyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSMasterApplyRptFeignFallbackFactory.class)
public interface MSMasterApplyRptFeign {

    /*
     * 配件报表
     */
    @PostMapping("/masterApply/masterApplyList")
    MSResponse<MSPage<RPTMasterApplyEntity>> getMasterApplyList(@RequestBody RPTComplainStatisticsDailySearch search);
}
