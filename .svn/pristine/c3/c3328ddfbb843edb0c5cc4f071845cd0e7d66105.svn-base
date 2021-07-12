package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDBrand;
import com.kkl.kklplus.entity.md.MDErrorCode;
import com.kkl.kklplus.entity.md.MDProductType;
import com.kkl.kklplus.entity.md.MDProductTypeItem;
import com.kkl.kklplus.entity.md.dto.MDProductTypeDto;
import com.kkl.kklplus.entity.md.dto.TreeDTO;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.ms.providermd.feign.MSBrandFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSProductTypeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSProductTypeService {

    @Autowired
    private MSProductTypeFeign msProductTypeFeign;


    /**
     * 根据id获取
     **/
    public MDProductTypeDto getById(Long id){
        MSResponse<MDProductTypeDto> msResponse = msProductTypeFeign.getById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 分页查询
     **/
    public Page<MDProductTypeDto> findListForPage(Page<MDProductTypeDto> mdProductTypeDtoPage,MDProductTypeDto productTypeDto){
        return MDUtils.findMDEntityListForPage(mdProductTypeDtoPage, productTypeDto, msProductTypeFeign::findList);
    }

    /**
     * 判断产品分类名称是否存在
     **/
    public String checkName(Long id,String name) {
        Long resultId = null;
        MSResponse<Long> msResponse = msProductTypeFeign.getIdByName(name);
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
     * @param mdProductTypeDto
     * @return
     */
    public void save(MDProductTypeDto mdProductTypeDto) {
        String name = mdProductTypeDto.getItemNames().replace("，",",");
        mdProductTypeDto.setItemNames(name);
        String[] itemName = mdProductTypeDto.getItemNames().split(",");
        List<MDProductTypeItem> productTypeItems = Lists.newArrayList();
        mdProductTypeDto.preInsert();
        if(itemName !=null && itemName.length>0){
            List<String> itemNameList = new ArrayList<>(Arrays.asList(itemName));
            //去重
            List<String> uniqueItemList = itemNameList.stream().distinct().collect(Collectors.toList());
            MDProductTypeItem productTypeItem;
           for(int i=0;i<uniqueItemList.size();i++){
               productTypeItem = new MDProductTypeItem();
               productTypeItem.setName(uniqueItemList.get(i));
               productTypeItem.setRemarks(mdProductTypeDto.getRemarks());
               productTypeItem.setCreateById(mdProductTypeDto.getCreateById());
               productTypeItem.setUpdateById(mdProductTypeDto.getUpdateById());
               productTypeItem.setCreateDate(mdProductTypeDto.getCreateDate());
               productTypeItem.setUpdateDate(mdProductTypeDto.getUpdateDate());
               productTypeItems.add(productTypeItem);
           }
        }
        mdProductTypeDto.setProductTypeItemList(productTypeItems);
        MSResponse<Integer> msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(mdProductTypeDto.getId()!=null && mdProductTypeDto.getId()>0){
            msResponse = msProductTypeFeign.update(mdProductTypeDto);
        }else{
            msResponse = msProductTypeFeign.insert(mdProductTypeDto);
        }
        if(msResponse.getCode()>0){
            throw new RuntimeException("保存产品分类错误.错误原因:" + msResponse.getMsg());
        }
    }


    /**
     * 删除
     * @param mdProductTypeDto
     * @return
     */
     public void delete(MDProductTypeDto mdProductTypeDto){
         mdProductTypeDto.preUpdate();
         MSResponse<Integer> msResponse = msProductTypeFeign.delete(mdProductTypeDto);
         if(msResponse.getCode()>0){
             throw new RuntimeException("删除产品分类错误.错误原因:" + msResponse.getMsg());
         }
     }

    /**
     * 根据品类id获取
     * @param productCategoryId
     * */
    public List<MDProductType> findListByCategoryId(Long productCategoryId){
        MSResponse<List<MDProductType>> msResponse = msProductTypeFeign.findListByCategoryId(productCategoryId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 根据客户Id+多品类id获取产品分类
     * @param productCategoryIds
     *
    public MSResponse<List<TreeDTO>>  findTypeAndItemsByCategoryIds(List<Long> productCategoryIds){
        return msProductTypeFeign.findTypeAndItemsByCategoryIds(null,productCategoryIds);
    }*/

    /**
     * 根据客户Id+多品类id获取产品分类
     * @param customerId    客户id
     * @param productCategoryIds
     * */
    public MSResponse<List<TreeDTO>>  findTypeAndItemsByCategoryIds(Long customerId,List<Long> productCategoryIds){
        return msProductTypeFeign.findTypeAndItemsByCategoryIds(customerId,productCategoryIds);
    }

}
