package com.wolfking.jeesite.ms.um.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.um.sd.UmOrderAuditCharged;
import com.kkl.kklplus.entity.um.sd.UmOrderProcessLog;
import com.kkl.kklplus.entity.um.sd.UmOrderStatusUpdate;
import com.wolfking.jeesite.ms.um.sd.fallback.UmOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "kklplus-b2b-um", fallbackFactory = UmOrderFeignFallbackFactory.class)
public interface UmOrderFeign {

    //-----------------------------------------------------------------------------------------------------工单处理日志

    /**
     * 优盟工单处理日志
     */
    @PostMapping("/umOrderProcesslog/saveProcesslog")
    MSResponse saveProcesslog(@RequestBody UmOrderProcessLog umOrderProcessLog);


    /**
     * 获取工单(分页)
     */
    @PostMapping("/order/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel);

    /**
     * 检查工单是否可以转换
     */
    @PostMapping("/order/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos);

    @PostMapping("/order/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    @PostMapping("/order/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    @PostMapping("/order/statusUpdate")
    MSResponse statusUpdate(@RequestBody UmOrderStatusUpdate orderStatusUpdate);

    @PostMapping("/order/charged")
    MSResponse auditCharged(@RequestBody UmOrderAuditCharged auditCharged);
}
