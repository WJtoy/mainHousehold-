package com.wolfking.jeesite.ms.tmall.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;

import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.fallback.MSCanboOrderRptFeignFallbackFactory;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "kklplus-b2b-canbo", fallbackFactory = MSCanboOrderRptFeignFallbackFactory.class)
public interface MSCanboOrderRptFeign {

    //region 状态数据查询 报表
    @PostMapping("/failedLog/getList")
    MSResponse<MSPage<B2BOrderProcesslog>> getList(@RequestBody B2BRptSearchModel processlogSearchModel);

    //根据Id查询信息
    @GetMapping("/failedLog/getLog/{id}")
    MSResponse<B2BOrderProcesslog> getLogById(@PathVariable("id") Long id);

    //忽略信息
    @PutMapping("/failedLog/close")
    MSResponse closeLog(@RequestBody B2BRetryOperationData retryOperationData);

    //重发
    @PostMapping("/failedLog/retryData")
    MSResponse retryData(@RequestBody B2BRetryOperationData retryOperationData);

}
