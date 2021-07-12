package com.wolfking.jeesite.ms.tmall.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.rpt.B2BProcesslog;
import com.kkl.kklplus.entity.b2b.rpt.Processlog;
import com.kkl.kklplus.entity.b2b.rpt.ProcesslogSearchModel;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderFailureLog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.fallback.MSOrderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "kklplus-b2b", fallbackFactory = MSOrderRptFeignFallbackFactory.class)
public interface MSTmallOrderRptFeign {

    //region 状态数据查询 报表
    @PostMapping("/processlog/getProcessLogList")
    MSResponse<MSPage<B2BProcesslog>> getList(@RequestBody ProcesslogSearchModel processlogSearchModel);

    //天猫失败日志
    @PostMapping("/failedLog/getList")
    MSResponse<MSPage<B2BOrderFailureLog>> getFailLogList(@RequestBody B2BRptSearchModel processlogSearchModel);


    //根据Id查询信息
    @GetMapping("/failedLog/getLog/{id}")
    MSResponse<B2BOrderFailureLog> getLogById(@PathVariable("id") Long id);

    //忽略信息
    @PutMapping("/failedLog/close")
    MSResponse closeLog(@RequestBody B2BRetryOperationData retryOperationData);

    //重发
    @PostMapping("/failedLog/retryData")
    MSResponse retryData(@RequestBody B2BRetryOperationData retryOperationData);
}
