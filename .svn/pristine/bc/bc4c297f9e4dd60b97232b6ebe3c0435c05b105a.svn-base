package com.wolfking.jeesite.ms.keg.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.keg.sd.KegOrderCompletedData;
import com.wolfking.jeesite.ms.keg.rpt.entity.KegSearchModel;
import com.wolfking.jeesite.ms.keg.rpt.fallback.MSKegFailLogRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kklplus-b2b-keg", fallbackFactory = MSKegFailLogRptFeignFallbackFactory.class)
public interface MSKegFailLogRptFeign {

        //region 状态数据查询 报表
        @PostMapping("/completed/getAbnormalList")
        MSResponse<MSPage<KegOrderCompletedData>> getFailLogList(@RequestBody KegSearchModel processlogSearchModel);

        //根据Id查询信息
        @GetMapping("/completed/getCompletedMsg/{id}")
        MSResponse<KegOrderCompletedData> getLogById(@PathVariable("id") Long id);

        //忽略信息
        @GetMapping("/completed/close/{id}/{updateBy}")
        MSResponse closeLog(@PathVariable("id") Long id, @PathVariable("updateBy") Long updateBy);

        //重发
        @PostMapping("/completed/retryCompleted")
        MSResponse retryData(@RequestBody KegOrderCompletedData kegOrderCompletedData);

}
