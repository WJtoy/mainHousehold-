package com.wolfking.jeesite.ms.otlan.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.jd.sd.fallback.JdOrderFeignFallbackFactory;
import com.wolfking.jeesite.ms.otlan.sd.fallback.OtlanOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "kklplus-b2b-otlan", fallbackFactory = OtlanOrderFeignFallbackFactory.class)
public interface OtlanOrderFeign {


    /**
     * 获取奥特朗工单(分页)
     */
    @PostMapping("/otlanOrderInfo/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel searchModel);


    /**
     * 检查工单是否可以转换
     */
    @PostMapping("/otlanOrderInfo/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> results);

    /**
     * 处理工单转换结果
     */
    @PostMapping("/otlanOrderInfo/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    /**
     * 取消转单
     */
    @PostMapping("/otlanOrderInfo/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);


}
