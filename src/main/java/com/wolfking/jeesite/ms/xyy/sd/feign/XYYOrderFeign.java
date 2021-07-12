package com.wolfking.jeesite.ms.xyy.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.*;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.xyy.sd.fallback.XYYOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 新迎燕B2B微服务接口调用
 */
@FeignClient(name = "kklplus-b2b-xyy", fallbackFactory = XYYOrderFeignFallbackFactory.class)
public interface XYYOrderFeign {

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

    //endregion


    //region 工单状态变更

    /**
     * 派单
     */
    @PostMapping("/orderPlanned/planned")
    MSResponse orderPlanned(@RequestBody B2BOrderPlanned orderPlanned);

    /**
     * 预约服务时间
     */
    @PostMapping("/orderAppointed/appointed")
    MSResponse orderAppointed(@RequestBody B2BOrderAppointed orderAppointed);

    /**
     * 完成服务
     */
    @PostMapping("/orderCompleted/completed")
    MSResponse orderCompleted(@RequestBody B2BOrderCompleted orderCompleted);

    /**
     * 取消工单
     */
    @PostMapping("/orderCancelled/cancelled")
    MSResponse orderCancelled(@RequestBody B2BOrderCancelled orderCancelled);

    /**
     * 取消工單轉換
     */
    @PostMapping("/orderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);
    //endregion

}
