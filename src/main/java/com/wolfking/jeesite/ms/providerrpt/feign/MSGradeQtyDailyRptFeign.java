package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTGradeQtyDailyEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerOrderPlanDailySearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSGradeQtyDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSGradeQtyDailyRptFeignFallbackFactory.class)
public interface MSGradeQtyDailyRptFeign {


    /**
     * 获取客评统计
     */
    @PostMapping("/gradeQtyDaily/gradeQtyDailyByList")
    MSResponse<List<RPTGradeQtyDailyEntity>> getGradeQtyDailyList(@RequestBody RPTCustomerOrderPlanDailySearch search);

}
