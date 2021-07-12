package com.wolfking.jeesite.ms.lb.sb.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.lb.sd.LbOrderCancelApply;
import com.kkl.kklplus.entity.lb.sd.LbOrderCancelAudit;
import com.kkl.kklplus.entity.lb.sd.LbOrderCompleteApply;
import com.kkl.kklplus.entity.lb.sd.LbOrderStatus;
import com.wolfking.jeesite.ms.lb.sb.fallback.LbOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 乐邦B2B微服务接口调用
 */
@FeignClient(name = "kklplus-b2b-lb", fallbackFactory = LbOrderFeignFallbackFactory.class)
public interface LbOrderFeign {

    //region 工单转换

    /**
     * 获取B2B工单
     */
    @PostMapping("/orderInfo/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel orderSearchModel);

    /**
     * 检查工单是否可转换
     */
    @PostMapping("/orderInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> workcardIds);

    /**
     * 更新订单转换进度标志
     */
    @PostMapping("/orderInfo/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    /**
     * 取消工單轉換
     */
    @PostMapping("/orderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    //endregion


    //region 工单取消

    /**
     * 工单取消申请
     */
    @PostMapping("/orderCancel/apply")
    MSResponse orderCancelApply(@RequestBody LbOrderCancelApply orderCancelApply);

    /**
     * 处理申请消息处理标志
     */
    @PostMapping("/orderCancel/processApplyFlag/{id}")
    MSResponse processApplyFlag(@PathVariable("id") Long id);

    /**
     * 工单取消审核
     */
    @PostMapping("/orderCancel/audit")
    MSResponse orderCancelAudit(@RequestBody LbOrderCancelAudit orderCancelAudit);

    /**
     * 处理审核消息处理标志
     */
    @PostMapping("/orderCancel/processAuditFlag/{id}")
    MSResponse processAuditFlag(@PathVariable("id") Long id);


    //endregion 工单取消


    //region 工单状态变更

    @PostMapping("/orderStatus/status")
    MSResponse orderStatus(@RequestBody LbOrderStatus orderStatus);


    //endregion 工单状态变更

    //region 工单完成

    @PostMapping("/orderCompleteApply/completeApply")
    MSResponse orderCompleteApply(@RequestBody LbOrderCompleteApply orderCompleteApply);

    //endregion 工单完成

    /**
     * 申请配件
     */
    @PostMapping("material/new")
    MSResponse newMaterial(@RequestBody B2BMaterial material);


    @GetMapping("material/updateAuditFlag/{id}")
    MSResponse updateAuditFlag(@PathVariable("id") Long id);

    @GetMapping("material/updateDeliverFlag/{id}")
    MSResponse updateDeliverFlag(@PathVariable("id") Long id);
}
