package com.wolfking.jeesite.ms.vatti.sd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderManualBaseService;
import com.wolfking.jeesite.ms.vatti.sd.feign.VattiOrderFeign;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configurable
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class VattiOrderService extends B2BOrderManualBaseService {

    @Autowired
    private VattiOrderFeign vattiOrderFeign;

    //region 工单转换

    /**
     * 批量检查工单是否可转换
     */
    public MSResponse checkB2BOrderProcessFlag(List<B2BOrderTransferResult> b2bOrderNos) {
        return vattiOrderFeign.checkWorkcardProcessFlag(b2bOrderNos);
    }

    /**
     * 调用微服务的B2B工单转换进度更新接口
     */
    public MSResponse sendB2BOrderConversionProgressUpdateCommandToB2B(List<B2BOrderTransferResult> progressList) {
        return vattiOrderFeign.updateTransferResult(Lists.newArrayList(progressList));
    }

    //endregion 工单转换


}
