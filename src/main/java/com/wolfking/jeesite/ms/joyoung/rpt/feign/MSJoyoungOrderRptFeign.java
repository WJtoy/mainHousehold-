package com.wolfking.jeesite.ms.joyoung.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.joyoung.rpt.entity.JoyoungSearchModel;
import com.wolfking.jeesite.ms.joyoung.rpt.fallback.MSJoyoungOrderRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.konka.rpt.entity.KonkaSearchModel;
import com.wolfking.jeesite.ms.konka.rpt.fallback.MSKonkaOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kklplus-b2b-joyoung", fallbackFactory = MSJoyoungOrderRptFeignFallbackFactory.class)
public interface MSJoyoungOrderRptFeign {
        //region 状态数据查询 报表
        @PostMapping("/processlog/getProcessLogList")
        MSResponse<MSPage<B2BOrderProcesslog>> getList(@RequestBody JoyoungSearchModel processlogSearchModel);

}
