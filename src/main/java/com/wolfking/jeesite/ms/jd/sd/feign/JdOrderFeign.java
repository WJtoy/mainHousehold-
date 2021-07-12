package com.wolfking.jeesite.ms.jd.sd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.jd.sd.JdOrderAppointed;
import com.kkl.kklplus.entity.jd.sd.JdOrderCancelled;
import com.kkl.kklplus.entity.jd.sd.JdOrderCompleted;
import com.wolfking.jeesite.ms.jd.sd.fallback.JdOrderFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "kklplus-b2b-jd", fallbackFactory = JdOrderFeignFallbackFactory.class)
public interface JdOrderFeign {

    /**
     * 获取京东工单
     */
    @PostMapping("/jdOrder/getList")
    MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel);

    /**
     * 检查工单是否可以转换
     */
    @PostMapping("/jdOrder/checkWorkcardProcessFlag")
    MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos);

    /**
     * 检查工单是否可以转换
     */
    @PostMapping("/jdOrder/updateTransferResult")
    MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults);

    /**
     * 预约/改约/派单/改派
     */
    @RequestMapping("/orderAppointmentPush/appointmentPush")
    MSResponse appointmentPush(@RequestBody JdOrderAppointed orderAppointmentPush);

    /**
     * 派单接口
     */
    @RequestMapping("/orderAppointmentPush/planned")
    MSResponse planned(@RequestBody JdOrderAppointed orderAppointed);

    /**
     * 退单申请时需要调用该接口进行预约
     */
    @RequestMapping("/orderAppointmentPush/appointJDOrder")
    MSResponse appointJDOrder(@RequestBody JdOrderAppointed orderAppointed);

    /**
     * 完成
     */
    @RequestMapping("/jDOrderInstallPush/installPush")
    MSResponse installPush(@RequestBody JdOrderCompleted orderInstallPush);

    @RequestMapping("/jDOrderInstallPush/completeOrder")
    MSResponse completeOrder(@RequestBody JdOrderCompleted orderCompleted);

    /**
     * 取消工单
     */
    @RequestMapping("/jDOrderUninstallPush/uninstallPush")
    MSResponse uninstallPush(@RequestBody JdOrderCancelled orderUninstallPush);

    /**
     * 取消工单转换
     *
     * @param b2BOrderTransferResult
     * @return
     */
    @PostMapping("/jdOrder/cancelOrderTransition")
    MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult);

    @GetMapping("/jdOrder/updateSystemIdAll")
    MSResponse updateSystemIdAll();

    @PostMapping("/jdOrder/getListUnknownOrder")
    MSResponse<MSPage<B2BOrder>> getListUnknownOrder(@RequestBody B2BOrderSearchModel b2BOrderSearchModel);

    @PostMapping("/jdOrder/ignore")
    MSResponse ignoreCancel(@RequestBody B2BOrderTransferResult result);
}
