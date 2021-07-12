package com.wolfking.jeesite.ms.konka.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.konka.sd.*;
import com.wolfking.jeesite.ms.konka.sd.fallback.KonkaOrderFeignFallbackFactory;
import com.wolfking.jeesite.ms.xyingyan.sd.fallback.XYingYanOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "kklplus-b2b-konka", fallbackFactory = KonkaOrderFeignFallbackFactory.class)
public interface KonkaOrderFeign {

    //---------------------------------------------------------------------------------------------------------------转单

    /**
     * 查询康佳B2B工单
     */
    @PostMapping("/konkaOrderInfo/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel);

    /**
     * 检查康佳B2B工单是否可以转成快可立工单
     */
    @PostMapping("/konkaOrderInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos);

    /**
     * 更新康宝B2B工单的转单状态
     */
    @PostMapping("/konkaOrderInfo/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    /**
     * 取消康宝B2B工单的转单操作
     */
    @PostMapping("/konkaOrderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    //-----------------------------------------------------------------------------------------------------变更B2B工单状态

    /**
     * 派单
     */
    @PostMapping("/konkaOrderPlanned/planned")
    MSResponse orderPlanned(@RequestBody KonkaOrderPlanned konkaOrderPlanned);

    /**
     * 预约
     */
    @PostMapping("/konkaOrderAppointed/appointed")
    MSResponse orderAppointed(@RequestBody KonKaOrderAppointed konKaOrderAppointed);

    /**
     * 上门
     */
    @PostMapping("/konkaOrderVisited/visited")
    MSResponse orderVisited(@RequestBody KonkaOrderVisited konkaOrderVisited);

    /**
     * 完成
     */
    @PostMapping("/konkaOrderCompleted/completed")
    MSResponse orderCompleted(@RequestBody KonkaOrderCompleted konkaOrderCompleted);

    /**
     * 取消
     */
    @PostMapping("/konkaOrderCancelled/cancelled")
    MSResponse orderCancelled(@RequestBody KonkaOrderCancelled konkaOrderCancelled);
}
