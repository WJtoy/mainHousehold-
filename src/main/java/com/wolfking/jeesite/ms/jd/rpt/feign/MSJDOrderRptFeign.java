package com.wolfking.jeesite.ms.jd.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;

import com.kkl.kklplus.entity.common.MSPage;

import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.fallback.MSJDOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "kklplus-b2b-jd", fallbackFactory = MSJDOrderRptFeignFallbackFactory.class)
public interface MSJDOrderRptFeign {
        //region 状态数据查询 报表
        @PostMapping("/processlog/getProcessLogList")
        MSResponse<MSPage<B2BOrderProcesslog>> getList(@RequestBody JDSearchModel processlogSearchModel);

}
