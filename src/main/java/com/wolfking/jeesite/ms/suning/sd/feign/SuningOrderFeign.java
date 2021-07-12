package com.wolfking.jeesite.ms.suning.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.suning.sd.SuningOrderModify;
import com.kkl.kklplus.entity.suning.sd.SuningOrderModifySrvtime;
import com.kkl.kklplus.entity.suning.sd.SuningOrderWorkStatus;
import com.wolfking.jeesite.ms.suning.sd.fallback.SuningOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "kklplus-b2b-suning", fallbackFactory = SuningOrderFeignFallbackFactory.class)
public interface SuningOrderFeign {

    //-----------------------------------------------------------------------------------------------------工单处理日志

    /**
     * 获取苏宁工单
     */
    @PostMapping("/suningOrderInfo/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel);

    /**
     * 检查工单是否可以转换
     */
    @PostMapping("/suningOrderInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> workcardIds);

    /**
     * 更新转单进度
     */
    @PostMapping("/suningOrderInfo/updateOrderTransferResult")
    MSResponse updateOrderTransferResult(@RequestBody List<B2BOrderTransferResult> orderTransferResults);

    /**
     * 取消工单
     */
    @PostMapping("/suningOrderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult orderTransferResult);

    /**
     * 通知B2B工单信息被修改
     */
    @RequestMapping("/suningOrderModify/orderModify")
    MSResponse orderModify(@RequestBody SuningOrderModify suningOrderModify);

    /**
     * 工单预约
     */
    @PostMapping("/suningOrderModifySrvtime/moditySrvtime")
    MSResponse moditySrvtime(@RequestBody SuningOrderModifySrvtime suningOrderModifySrvtime);

    /**
     * 工单派单、完成、取消
     */
    @PostMapping("/suningOrderWorkStatus/workStatus")
    MSResponse workStatus(@RequestBody SuningOrderWorkStatus suningOrderWorkStatus);



    @PostMapping("/suningOrderInfo/getListUnknownOrder")
    MSResponse<MSPage<B2BOrder>> getListUnknownOrder(@RequestBody B2BOrderSearchModel b2BOrderSearchModel);

    @GetMapping("/suningOrderInfo/updateSystemIdAll")
    MSResponse updateSystemIdAll();
}
