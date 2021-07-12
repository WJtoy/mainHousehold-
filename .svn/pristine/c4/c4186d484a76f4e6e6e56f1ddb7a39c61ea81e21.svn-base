package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.entity.md.MDErrorAction;
import com.kkl.kklplus.entity.md.dto.MDErrorActionDto;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.providermd.feign.MSActionCodeFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSErrorActionFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MSErrorActionService {
    @Autowired
    private MSErrorActionFeign msErrorActionFeign;

    @Autowired
    private MSActionCodeFeign msActionCodeFeign;

    public Page<MDErrorActionDto>  findPage(Page<MDErrorActionDto> page, MDErrorActionDto mdErrorActionDto) {
        return MDUtils.findMDEntityListForPage(page, mdErrorActionDto, msErrorActionFeign::findListWithProduct);
    }

    public Page<MDErrorActionDto>  findPageForActionCode(Page<MDErrorActionDto> page, MDErrorActionDto mdErrorActionDto) {
        return MDUtils.findMDEntityListForPage(page, mdErrorActionDto, msErrorActionFeign::findList);
    }

    public void save(MDErrorActionDto mdErrorActionDto) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msActionCodeFeign.save(mdErrorActionDto));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("保存处理代码失败，失败原因:"+ msErrorCode.getMsg());
        }
    }

    public void delete(MDErrorAction mdErrorAction) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msErrorActionFeign.delete(mdErrorAction));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("保存故障处理失败，失败原因:"+ msErrorCode.getMsg());
        }
    }

    public Long getIdByProductAndErrorCode(Long errorCodeId, Long productId) {
        Long id = 0L;
        MSResponse<Long> msResponse = msErrorActionFeign.getIdByProductAndErrorCode(errorCodeId, productId);
        if (MSResponse.isSuccess(msResponse)) {
            id = msResponse.getData();
        }
        return id;
    }

    public MDErrorActionDto getAssociatedDataById(Long errorActionId) {
        MSResponse<MDErrorActionDto> msResponse = msErrorActionFeign.getAssociatedDataById(errorActionId);
        MDErrorActionDto errorActionDto = new MDErrorActionDto();
        if (MSResponse.isSuccess(msResponse)) {
            errorActionDto = msResponse.getData();
        }
        return errorActionDto;
    }

    public void updateActionCodeNameAndAnalysis(MDActionCode mdActionCode) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msErrorActionFeign.updateActionCodeNameAndAnalysis(mdActionCode));
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("更新故障分析出错了。原因:"+msErrorCode.getMsg());
        }
    }

}
