package com.wolfking.jeesite.ms.inse.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.inse.rpt.entity.InseSearchModel;
import com.wolfking.jeesite.ms.inse.rpt.fallback.MSInseOrderRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.fallback.MSJDOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kklplus-b2b-inse", fallbackFactory = MSInseOrderRptFeignFallbackFactory.class)
public interface MSInseOrderRptFeign {
        //region 状态数据查询 报表
        @PostMapping("/processlog/getProcessLogList")
        MSResponse<MSPage<B2BOrderProcesslog>> getList(@RequestBody InseSearchModel processlogSearchModel);

}
