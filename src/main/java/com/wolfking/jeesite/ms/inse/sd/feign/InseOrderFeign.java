package com.wolfking.jeesite.ms.inse.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.inse.sd.*;
import com.kkl.kklplus.entity.jd.sd.JdOrderAppointed;
import com.kkl.kklplus.entity.jd.sd.JdOrderCancelled;
import com.kkl.kklplus.entity.jd.sd.JdOrderCompleted;
import com.wolfking.jeesite.ms.inse.sd.fallback.InseOrderFeignFallbackFactory;
import com.wolfking.jeesite.ms.jd.sd.fallback.JdOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "kklplus-b2b-inse", fallbackFactory = InseOrderFeignFallbackFactory.class)
public interface InseOrderFeign {

    /**
     * 获取樱雪工单
     */
    @PostMapping("/inseOrderInfo/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel);

    @PostMapping("/inseOrderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    /**
     * 检查工单是否可以转换
     */
    @PostMapping("/inseOrderInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos);

    /**
     * 更新工单转换进度
     */
    @PostMapping("/inseOrderInfo/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    @PostMapping("/inseOrderInfo/saveLog")
    MSResponse saveLog(@RequestBody InseOrderRemark orderRemark);

    /**
     * 派单
     */
    @PostMapping("/inseOrderPlanned/planned")
    MSResponse orderPlanned(@RequestBody InseOrderPlanned inseOrderPlanned);

    /**
     * 预约
     */
    @PostMapping("/inseOrderAppointed/appointed")
    MSResponse orderAppointed(@RequestBody InseOrderAppointed inseOrderAppointed);

    /**
     * 上门
     */
    @PostMapping("/inseOrderVisited/visited")
    MSResponse orderVisited(@RequestBody InseOrderVisited inseOrderVisited);

    /**
     * 完成
     */
    @PostMapping("/inseOrderCompleted/completed")
    MSResponse orderCompleted(@RequestBody InseOrderCompleted inseOrderCompleted);

    /**
     * 取消
     */
    @PostMapping("/inseOrderCancelled/cancelled")
    MSResponse orderCancelled(@RequestBody InseOrderCancelled inseOrderCancelled);


    @GetMapping("/abnormalOrder/updateProcessFlag/{id}")
    MSResponse updateProcessFlag(@PathVariable("id") Long id);

    @PostMapping("/inseOrderCancelled/apply")
    MSResponse apply(@RequestBody InseOrderCancelApply cancelApply);
}
