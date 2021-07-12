package com.wolfking.jeesite.ms.joyoung.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BBase;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.common.material.B2BMaterialArrival;
import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import com.kkl.kklplus.entity.joyoung.sd.*;
import com.wolfking.jeesite.ms.joyoung.sd.fallback.JoyoungOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "kklplus-b2b-joyoung", fallbackFactory = JoyoungOrderFeignFallbackFactory.class)
public interface JoyoungOrderFeign {

    //---------------------------------------------------------------------------------------------------------------转单

    /**
     * 查询九阳B2B工单
     */
    @PostMapping("/joyoungOrderInfo/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel);

    /**
     * 检查九阳B2B工单是否可以转成快可立工单
     */
    @PostMapping("/joyoungOrderInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos);

    /**
     * 更新九阳B2B工单的转单状态
     */
    @PostMapping("/joyoungOrderInfo/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    /**
     * 取消九阳B2B工单的转单操作
     */
    @PostMapping("/joyoungOrderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    //-----------------------------------------------------------------------------------------------------变更B2B工单状态

    /**
     * 派单
     */
    @PostMapping("/joyoungOrderPlanned/planned")
    MSResponse orderPlanned(@RequestBody JoyoungOrderPlanned joyoungOrderPlanned);

    /**
     * 预约
     */
    @PostMapping("/joyoungOrderAppointed/appointed")
    MSResponse orderAppointed(@RequestBody JoyoungOrderAppointed joyoungOrderAppointed);

    /**
     * 上门
     */
    @PostMapping("/joyoungOrderVisited/visited")
    MSResponse orderVisited(@RequestBody JoyoungOrderVisited joyoungOrderVisited);

    /**
     * 完成
     */
    @PostMapping("/joyoungOrderCompleted/completed")
    MSResponse orderCompleted(@RequestBody JoyoungOrderCompleted joyoungOrderCompleted);

    /**
     * 取消
     */
    @PostMapping("/joyoungOrderCancelled/cancelled")
    MSResponse orderCancelled(@RequestBody JoyoungOrderCancelled joyoungOrderCancelled);

    //-----------------------------------------------------------------------------------------------------工单处理日志

    /**
     * 九阳工单处理日志
     */
    @RequestMapping("/joyoungOrderProcesslog/saveOrderProcesslog")
    MSResponse saveOrderProcesslog(@RequestBody JoyoungOrderProcessLog orderProcessLog);

    //region 配件

    /**
     * 申请配件单
     */
    @PostMapping("/material/newMaterial")
    MSResponse newMaterialForm(@RequestBody B2BMaterial joyoungMaterial);

    /**
     * by配件单关闭
     * 包含正常关闭，异常签收，取消(订单退单/取消)
     */
    @PostMapping("/material/close")
    MSResponse materialClose(@RequestBody B2BMaterialClose joyoungMaterialClose);

    /**
     * by订单关闭配件单
     * 包含正常关闭，异常签收，取消(订单退单/取消)
     */
    @PostMapping("/material/closeByOrderId")
    MSResponse materialCloseByOrder(@RequestBody B2BMaterialClose joyoungMaterialClose);

    /**
     * 到货
     */
    @PostMapping("/material/arrival")
    MSResponse materialArrival(@RequestBody B2BMaterialArrival joyoungMaterialArrival);

    /**
     * 审核
     * 消息队列处理成功后，同步更新微服务
     */
    @PostMapping("/materialApply/updateFlag/{id}")
    MSResponse updateApplyFlag(@PathVariable("id") Long id);

    /**
     * 发货回调
     * 消息队列处理成功后，同步更新微服务
     */
    @PostMapping("/materialDeliver/updateFlag/{id}")
    MSResponse updateDeliverFlag(@PathVariable("id") Long id);

    //endregion 配件


    //---------------------------------------------------------------------------------------------------------------投诉/催单

    /**
     * 更新九阳投诉单的kkl投诉单Id
     */
    @PostMapping("/consultingOrder/updateFlag/{id}/{kklConsultingId}")
    MSResponse<String> updateFlag(@PathVariable("id") Long id,
                                         @PathVariable("kklConsultingId")Long kklConsultingId);

    /**
     * 更新处理日志(催单/投诉)
     */
    @PostMapping("/consultingOrder/process")
    MSResponse process(@RequestBody JoyoungConsultingOrderProcess consultingOrderProcess);

    /**
     * 条码检查
     */
    @GetMapping("/product/barCodeVerify/{productBarCode}")
    MSResponse getProductData(@PathVariable("productBarCode") String productBarCode);
}
