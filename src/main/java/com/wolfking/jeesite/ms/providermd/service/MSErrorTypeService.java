package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDErrorType;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.providermd.feign.MSErrorTypeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MSErrorTypeService {
    @Autowired
    private MSErrorTypeFeign msErrorTypeFeign;

    /**
     * 保存故障分类
     * @param mdErrorType
     * @return
     */
    public void save(MDErrorType mdErrorType) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msErrorTypeFeign.save(mdErrorType));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("保存故障分类到微服务出错.出错原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 分页查询数据
     * @param page
     * @param mdErrorType
     * @return
     */
    public Page<MDErrorType> findListForPage(Page<MDErrorType> page, MDErrorType mdErrorType) {
        Page<MDErrorType> errorTypePage = new Page<>();
        errorTypePage.setPageSize(page.getPageSize());
        errorTypePage.setPageNo(page.getPageNo());

        mdErrorType.setPage(new MSPage<>(errorTypePage.getPageNo(), errorTypePage.getPageSize()));
        MSResponse<MSPage<MDErrorType>> returnErrorType = msErrorTypeFeign.findList(mdErrorType);
        if (MSResponse.isSuccess(returnErrorType)) {
            MSPage<MDErrorType> data = returnErrorType.getData();
            errorTypePage.setCount(data.getRowCount());
            errorTypePage.setList(data.getList());
        } else {
            errorTypePage.setCount(0);
            errorTypePage.setList(new ArrayList<>());
        }
        return errorTypePage;
    }

    public void delete(MDErrorType mdErrorType) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msErrorTypeFeign.delete(mdErrorType));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除故障分类出错.出错原因:"+msErrorCode.getMsg());
        }
    }

    public String checkName(Long productId, String name,Long customerId) {
        Long id = null;
        MSResponse<Long> msResponse = msErrorTypeFeign.getByProductIdAndName(productId, name,customerId);
        if (MSResponse.isSuccess(msResponse)) {
            id = msResponse.getData();
        }
        return id==null?"true":"false";
    }

    /**
     * 产品id获取故障分类（旧）
     * customerId=0
     * */
    public List<MDErrorType> findErrorTypesByProductId(Long productId) {
        MSResponse<List<MDErrorType>> msResponse = msErrorTypeFeign.findErrorTypesByProductId(productId,0L);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return Lists.newArrayList();
    }


    public List<MDErrorType> findListByProductId(Long productId, Long id) {
        return MDUtils.findListUnnecessaryConvertType(()->msErrorTypeFeign.findListByProductId(productId, id));
    }

    /**
     * 按产品Id + id读取故障类型
     */
    public MDErrorType getByProductIdAndId(Long productId, Long id){
        if(id == null || id <=0){
            return null;
        }
        MSResponse<List<MDErrorType>> msResponse = msErrorTypeFeign.findListByProductId(productId,id);
        if(MSResponse.isSuccess(msResponse)){
            List<MDErrorType> list = msResponse.getData();
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            return list.get(0);
        }else{
            return null;
        }
    }

}
