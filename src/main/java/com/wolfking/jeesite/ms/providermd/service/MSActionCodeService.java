package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.entity.md.dto.MDActionCodeDto;
import com.wolfking.jeesite.ms.providermd.feign.MSActionCodeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class MSActionCodeService {
    @Autowired
    private MSActionCodeFeign msActionCodeFeign;

    // TODO: 此方法现在没有用到，后续可以考虑删除
    public String checkAnalysis(MDActionCode mdActionCode) {
        Long id = null;
        MSResponse<Long> msResponse = msActionCodeFeign.getByProductAndServiceTypeAndAnalysis(mdActionCode);
        if (MSResponse.isSuccess(msResponse)) {
            id = msResponse.getData();
        }
        return id==null?"true":"false";
    }

    public List<MDActionCodeDto> findListByProductAndErrorCode(Long errorCodeId, Long productId) {
        return MDUtils.findListUnnecessaryConvertType(()->msActionCodeFeign.findListByProductAndErrorCode(errorCodeId,productId));
    }

    public List<MDActionCodeDto> findListByProductId(Long id, Long productId) {
        return MDUtils.findListUnnecessaryConvertType(()->msActionCodeFeign.findListByProductId(id, productId));
    }

    /**
     * 按产品id + id读取故障处理
     */
    public MDActionCodeDto getByProductIdAndId(Long productId,Long id){
        if(id == null || id <=0){
            return null;
        }
        MSResponse<List<MDActionCodeDto>> msResponse = msActionCodeFeign.findListByProductId(id,productId);
        if(MSResponse.isSuccess(msResponse)){
            List<MDActionCodeDto> list = msResponse.getData();
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            return list.get(0);
        }else{
            return null;
        }
    }
}
