package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProductSpec;
import com.kkl.kklplus.entity.md.dto.MDProductSpecDto;
import com.kkl.kklplus.entity.md.dto.MDProductSpecItemDto;
import com.kkl.kklplus.entity.md.dto.MDProductSpecTypeDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.providermd.feign.MSProductSpecFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSProductSpecService {

   @Autowired
   private MSProductSpecFeign msProductSpecFeign;


    /**
     * 根据id获取
     **/
    public MDProductSpec getById(Long id){
        MSResponse<MDProductSpec> msResponse = msProductSpecFeign.getById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }


    public MDProductSpecDto getDtoWithSpecId(Long id){
        MSResponse<MDProductSpecDto> msResponse = msProductSpecFeign.getDtoWithSpecId(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }



    /**
     * 分页查询
     **/
    public Page<MDProductSpecDto> findListForPage(Page<MDProductSpecDto> mdProductSpecDtoPage,MDProductSpecDto mdProductSpecDto){
        return MDUtils.findMDEntityListForPage(mdProductSpecDtoPage, mdProductSpecDto, msProductSpecFeign::findList);
    }

    /**
     * 判断产品分类名称是否存在
     **/
    public String checkName(Long id,String name) {
        Long resultId = null;
        MSResponse<Long> msResponse = msProductSpecFeign.getIdByName(name);
        if (MSResponse.isSuccess(msResponse)) {
            resultId = msResponse.getData();
        }
        if(id == null || id<=0){
            return resultId == null ? "true" : "false";
        }else{
            if(resultId !=null && resultId>0 && !resultId.equals(id)){
                return "false";
            }else{
                return "true";
            }
        }
    }


    /**
     * 添加/更新
     * @param mdProductSpecDto
     * @return
     */
    public void save(MDProductSpecDto mdProductSpecDto) {
        String specItemNames = mdProductSpecDto.getProductSpecItemNames().replace("，",",");
        String[] specItemNamArray = specItemNames.split(",");
        List<MDProductSpecItemDto> productSpecItems = Lists.newArrayList();
        mdProductSpecDto.preInsert();
        if(specItemNamArray !=null && specItemNamArray.length>0){
            List<String> specItemNameList = new ArrayList<>(Arrays.asList(specItemNamArray));
            //去重
            List<String> uniqueItemList = specItemNameList.stream().distinct().collect(Collectors.toList());
            MDProductSpecItemDto productSpecItem;
           for(int i=0;i<uniqueItemList.size();i++){
               productSpecItem = new MDProductSpecItemDto();
               productSpecItem.setName(uniqueItemList.get(i));
               productSpecItem.setSort(0);
               productSpecItem.setRemarks(mdProductSpecDto.getRemarks());
               productSpecItem.setCreateById(mdProductSpecDto.getCreateById());
               productSpecItem.setUpdateById(mdProductSpecDto.getUpdateById());
               productSpecItem.setCreateDate(mdProductSpecDto.getCreateDate());
               productSpecItem.setUpdateDate(mdProductSpecDto.getUpdateDate());
               productSpecItems.add(productSpecItem);
           }
        }
        mdProductSpecDto.setProductSpecItemDtoList(productSpecItems);

        String[] productTypeItemNameArray = mdProductSpecDto.getProductTypeItemNames().split(",");
        List<MDProductSpecTypeDto> productSpecTypeDtoList = Lists.newArrayList();
        if(productTypeItemNameArray !=null && productTypeItemNameArray.length>0){
            MDProductSpecTypeDto productSpecTypeDto;
            for(int i=0;i<productTypeItemNameArray.length;i++){
                productSpecTypeDto = new MDProductSpecTypeDto();
                productSpecTypeDto.setProductTypeItemId(Long.valueOf(productTypeItemNameArray[i]));
                productSpecTypeDto.setRemarks(mdProductSpecDto.getRemarks());
                productSpecTypeDto.setCreateById(mdProductSpecDto.getCreateById());
                productSpecTypeDto.setUpdateById(mdProductSpecDto.getUpdateById());
                productSpecTypeDto.setCreateDate(mdProductSpecDto.getCreateDate());
                productSpecTypeDto.setUpdateDate(mdProductSpecDto.getUpdateDate());
                productSpecTypeDtoList.add(productSpecTypeDto);
            }
        }
        mdProductSpecDto.setProductSpecTypeDtoList(productSpecTypeDtoList);
        MSResponse<Integer> msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(mdProductSpecDto.getId()!=null && mdProductSpecDto.getId()>0){
            msResponse = msProductSpecFeign.update(mdProductSpecDto);
        }else{
            msResponse = msProductSpecFeign.insert(mdProductSpecDto);
        }
        if(msResponse.getCode()>0){
            throw new RuntimeException("保存产品规格错误.错误原因:" + msResponse.getMsg());
        }
    }

    /**
     * 删除
     * @param mdProductSpecDto
     * @return
     */
     public void delete(MDProductSpecDto mdProductSpecDto){
         mdProductSpecDto.preUpdate();
         MSResponse<Integer> msResponse = msProductSpecFeign.delete(mdProductSpecDto);
         if(msResponse.getCode()>0){
             throw new RuntimeException("删除产品规格错误.错误原因:" + msResponse.getMsg());
         }
     }
}
