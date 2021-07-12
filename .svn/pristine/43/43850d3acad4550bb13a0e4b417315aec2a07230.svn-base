package com.wolfking.jeesite.ms.vatti.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.vatti.sd.fallback.VattiOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 华帝B2B微服务接口调用
 */
@FeignClient(name = "kklplus-b2b-vatti", fallbackFactory = VattiOrderFeignFallbackFactory.class)
public interface VattiOrderFeign {

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

    //endregion 工单转换


}
