package com.wolfking.jeesite.ms.otlan.sd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.otlan.sd.feign.OtlanOrderFeign;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OtlanOrderService extends B2BOrderManualBaseService {

    @Autowired
    private OtlanOrderFeign otlanOrderFeign;

    //region 工单状态检查

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return otlanOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    //endregion 工单状态检查

    /**
     * 取消工单转换
     */
    public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        return otlanOrderFeign.cancelOrderTransition(b2BOrderTransferResult);
    }
    //region 更新转单进度

    /**
     * 调用天猫微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return otlanOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 更新转单进度

}
