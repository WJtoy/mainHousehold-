package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTRebuildMiddleTableTaskEntity;
import com.kkl.kklplus.entity.rpt.search.RPTRebuildMiddleTableTaskSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.RebuildMiddleTableTaskFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = RebuildMiddleTableTaskFeignFallbackFactory.class)
public interface RebuildMiddleTableTaskFeign {

    @PostMapping("/rebuildMiddleTableTask/createRebuildMiddleTableTask")
    MSResponse<String> createRebuildMiddleTableTask(@RequestBody RPTRebuildMiddleTableTaskEntity taskEntity);

    @PostMapping("/rebuildMiddleTableTask/getRebuildMiddleTableTaskList")
    MSResponse<MSPage<RPTRebuildMiddleTableTaskEntity>> getRebuildMiddleTableTaskList(@RequestBody RPTRebuildMiddleTableTaskSearch search);
}
