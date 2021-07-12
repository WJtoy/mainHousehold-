package com.wolfking.jeesite.ms.providermd.service;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProductTypeItem;
import com.wolfking.jeesite.ms.providermd.feign.MSProductTypeItemFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MSProductTypeItemService {

    @Autowired
    private MSProductTypeItemFeign msProductTypeItemFeign;


    /**
     * 获取所有产品分类项次
     **/
    public List<MDProductTypeItem> findAllList(){
        MSResponse<List<MDProductTypeItem>> msResponse = msProductTypeItemFeign.findAllList();
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 根据productTypeId获取数据productTypeItem集合
     * */
    public List<MDProductTypeItem> findListByProductTypeId(Long productTypeId){
        MSResponse<List<MDProductTypeItem>> msResponse = msProductTypeItemFeign.findListByProductTypeId(productTypeId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }
}
