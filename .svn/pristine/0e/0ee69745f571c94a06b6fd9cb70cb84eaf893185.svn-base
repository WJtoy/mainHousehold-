package com.wolfking.jeesite.ms.tmall.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.order.WorkcardInfo;
import com.kkl.kklplus.entity.b2b.order.WorkcardSearchModel;
import com.kkl.kklplus.entity.b2b.order.WorkcardStatusUpdate;
import com.kkl.kklplus.entity.b2b.order.WorkcardTransferResult;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.tmall.sd.AnomalyRecourseRemarkUpdate;
import com.kkl.kklplus.entity.tmall.sd.ServiceMonitorMessageUpdate;
import com.wolfking.jeesite.ms.tmall.sd.fallback.WorkcardFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 天猫B2B微服务接口调用
 * 服务名:b2b
 */
@FeignClient(name = "kklplus-b2b", fallbackFactory = WorkcardFeignFallbackFactory.class)
public interface WorkcardFeign {

    //region 查询

    /**
     * 查询待转换订单列表
     *
     * @param workcardSearchModel
     */
    @PostMapping("/workcardInfo/getList")
    MSResponse<MSPage<WorkcardInfo>> getList(@RequestBody WorkcardSearchModel workcardSearchModel);

    /**
     * 查询待转换订单列表(新版本)
     *
     * @param orderSearchModel
     */
    @PostMapping("/workcardInfo/getListOrder")
    MSResponse<MSPage<B2BOrder>> getListOrder(B2BOrderSearchModel orderSearchModel);

    /**
     * 批量检查工单是否可转换
     */
    @PostMapping("/workcardInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> workcardIds);

    //endregion

    //region 更新

    /**
     * 更新订单转换进度标志
     */
    @PostMapping("/workcardInfo/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<WorkcardTransferResult> workcardTransferResults);

    /**
     * 更新订单转换进度标志(新版本)
     */
    @PostMapping("/workcardInfo/updateOrderTransferResult")
    MSResponse updateOrderTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    /**
     * 工单系统发起取消(预约失败：10)
     *
     @PostMapping("/workcardInfo/cancel") MSResponse cancel(@RequestBody WorkcardTransferResult workcardTransferResult);
     */

    //endregion

    //region 处理进度

    /**
     * 更新订单的处理进度
     */
    @PostMapping("/workcardProcessStatus/updateState")
    MSResponse<String> updateWorkcardProcessStatus(@RequestBody WorkcardStatusUpdate workcardStatusUpdate);

    //endregion

    //region 一键求助

    /**
     * 反馈一键求助
     */
    @PostMapping("/anomalyRecourse/anomalyRecourseFeedback")
    MSResponse anomalyRecourseFeedback(@RequestBody AnomalyRecourseRemarkUpdate feedback);


    //endregion

    /**
     * 反馈天猫预警
     */
    @PostMapping("/serviceMonitorMessage/updateServiceMonitorMessageStatus")
    MSResponse updateServiceMonitorMessageStatus(@RequestBody ServiceMonitorMessageUpdate serviceMonitorMessageUpdate);

    /**
     * 取消工单转换
     */
    @PostMapping("/workcardInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    /**
     * 直接取消工单（仅调用上门服务失败，不调用预约）
     */
    @PostMapping("/workcardInfo/directCancel")
    MSResponse directCancel(@RequestBody B2BOrderTransferResult workcardTransferResults);

    /**
     * 忽略（直接关闭工单）
     */
    @PostMapping("/workcardInfo/ignore")
    MSResponse ignoreCancel(@RequestBody B2BOrderTransferResult result);

    @GetMapping("/workcardInfo/updateAbnormalOrderFlagAll")
    MSResponse updateAbnormalOrderFlagAll();


    @PostMapping("/workcardInfo/getListUnknownOrder")
    MSResponse<MSPage<B2BOrder>> getListUnknownOrder(@RequestBody B2BOrderSearchModel b2BOrderSearchModel);

    @GetMapping("/workcardInfo/updateSystemIdAll")
    MSResponse updateSystemIdAll();
}
