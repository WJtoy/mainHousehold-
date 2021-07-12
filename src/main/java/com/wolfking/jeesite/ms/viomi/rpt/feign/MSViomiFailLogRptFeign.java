package com.wolfking.jeesite.ms.viomi.rpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.viomi.sd.VioMiApiLog;
import com.kkl.kklplus.entity.viomi.sd.VioMiExceptionOrder;
import com.wolfking.jeesite.ms.viomi.rpt.entity.ViomiFailLogSearchModel;
import com.wolfking.jeesite.ms.viomi.rpt.fallback.MSViomiFailLogRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "kklplus-b2b-viomi", fallbackFactory = MSViomiFailLogRptFeignFallbackFactory.class)
public interface MSViomiFailLogRptFeign {

        //region 状态数据查询 报表
        @PostMapping("/apiLog/getExceptionOrderList")
        MSResponse<MSPage<VioMiExceptionOrder>> getFailLogList(@RequestBody ViomiFailLogSearchModel model);

        //根据Id查询信息
        @GetMapping("/apiLog/getExceptionApiList/{b2bOrderId}")
        MSResponse<List<VioMiApiLog>> getLogById(@PathVariable("b2bOrderId") Long b2bOrderId);

        //根据Id查询信息
        @GetMapping("/apiLog/getOrderById/{id}")
        MSResponse<VioMiExceptionOrder> getOrderInfo(@PathVariable("id") Long id);

        //重发
        @PostMapping("/orderReview/resend")
        MSResponse retryData(@RequestParam("apiLogId") Long apiLogId, @RequestParam("operator") String operator, @RequestParam("operatorId") Long operatorId);

}
