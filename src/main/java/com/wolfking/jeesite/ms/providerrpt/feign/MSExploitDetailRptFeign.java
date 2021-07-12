package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTExploitDetailEntity;
import com.kkl.kklplus.entity.rpt.search.RPTExploitDetailSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSExploitDetailRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-rpt", fallbackFactory = MSExploitDetailRptFeignFallbackFactory.class)
public interface MSExploitDetailRptFeign {

    /**
     * 分页获取开发明细数据
     */
    @PostMapping("/exploitDetail/getExploitDetailRptList")
    MSResponse<MSPage<RPTExploitDetailEntity>> getExploitDetailRptList(@RequestBody RPTExploitDetailSearch search);
}
