package com.wolfking.jeesite.ms.jd.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.fallback.MSJDFailLogRptFeignFallbackFactory;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "kklplus-b2b-jdue", fallbackFactory = MSJDFailLogRptFeignFallbackFactory.class)
public interface MSJDFailLogRptFeign {

        //region 状态数据查询 报表
        @PostMapping("/failedLog/getList")
        MSResponse<MSPage<B2BOrderProcesslog>> getFailLogList(@RequestBody JDSearchModel processlogSearchModel);

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
