package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.dto.MDErrorCodeDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.providermd.feign.MSErrorCodeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class MSErrorCodeService {
    @Autowired
    private MSErrorCodeFeign msErrorCodeFeign;

    public void save(MDErrorCode mdErrorCode) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msErrorCodeFeign.save(mdErrorCode));
        if (msErrorCode.getCode()>0){
            throw new RuntimeException("保存故障现象出错，出错原因:"+msErrorCode.getMsg());
        }
    }

    public void delete(MDErrorCode mdErrorCode) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msErrorCodeFeign.delete(mdErrorCode));
        if (msErrorCode.getCode()>0){
            throw new RuntimeException("删除故障现象出错，出错原因:"+msErrorCode.getMsg());
        }
    }

    public String checkName(MDErrorCode mdErrorCode) {
        Long id = null;
        MSResponse<Long> msResponse = msErrorCodeFeign.getByProductAndErrorType(mdErrorCode);
        if (MSResponse.isSuccess(msResponse)) {
            id = msResponse.getData();
        }
        return id==null?"true":"false";
    }

    public Page<MDErrorCodeDto> findListForPage(Page<MDErrorCodeDto> page, MDErrorCode mdErrorCode) {
        return MDUtils.findMDEntityListForPage(page, mdErrorCode, msErrorCodeFeign::findListReturnErrorCodeDto);
    }

    /**
     * 根据客户id+产品id+故障分类id获取故障现象(旧)
     * 获取客户无配置的故障现象(customerId=0)
     * */
    public List<MDErrorCode> findListByProductAndErrorType(Long errorTypeId,Long productId) {
        return MDUtils.findListUnnecessaryConvertType(()->msErrorCodeFeign.findListByProductAndErrorType(errorTypeId,productId,0L));
    }

    public Long getIdByProductAndErrorType(Long errorTypeId, Long productId) {
        Long id = 0L;
        MSResponse<Long> msResponse = msErrorCodeFeign.getIdByProductAndErrorType(errorTypeId, productId);
        if (MSResponse.isSuccess(msResponse)) {
            id = msResponse.getData();
        }
        return id;
    }

    public List<MDErrorCodeDto> findListByProductId(Long id, Long productId) {
        return MDUtils.findListUnnecessaryConvertType(()->msErrorCodeFeign.findListByProductId(id, productId));
    }

    /**
     * 按产品id + id读取故障现象
     */
    public MDErrorCode getByProductIdAndId(Long productId,Long id){
        if(id == null || id <=0){
            return null;
        }
        MSResponse<List<MDErrorCodeDto>> msResponse = msErrorCodeFeign.findListByProductId(id,productId);
        if(MSResponse.isSuccess(msResponse)){
            List<MDErrorCodeDto> list = msResponse.getData();
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            return list.get(0);
        }else{
            return null;
        }
    }

}
