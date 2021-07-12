package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.dto.MDProductSpecDto;
import com.wolfking.jeesite.ms.providermd.feign.MSProductSpecTypeFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
;import java.util.List;

@Service
public class MSProductSpecTypeService {

   @Autowired
   private MSProductSpecTypeFeign msProductSpecTypeFeign;

    /**
     * 根据产品一级分类和二级分类获取产品规格以及参数
     * @param productTypeId
     * @param productTypeItemId
     * */
    public List<MDProductSpecDto> findListByTypeIdAndItemId(Long productTypeId, Long productTypeItemId){
        MSResponse<List<MDProductSpecDto>> msResponse = msProductSpecTypeFeign.findListByTypeIdAndItemId(productTypeId,productTypeItemId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }
}
