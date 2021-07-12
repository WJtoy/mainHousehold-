package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDMaterialCategory;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.MaterialCategory;
import com.wolfking.jeesite.ms.providermd.feign.MSMaterialCategoryFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSMaterialCategoryService {

    @Autowired
    private MSMaterialCategoryFeign msMaterialCategoryFeign;

    /**
     * 根据id获取配件类别信息
     * @param id
     * @return
     */
    public MaterialCategory getById(Long id) {
        return MDUtils.getById(id, MaterialCategory.class, msMaterialCategoryFeign::getById);
    }

    /**
     * 根据name获取id
     * @param name
     * @return
     */
    public Long getIdByName(String name) {
        MSResponse<Long> msResponse = msMaterialCategoryFeign.getIdByName(name);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取所有数据
     * @return
     */
    public List<MaterialCategory> findAllList() {
        return MDUtils.findAllList(MaterialCategory.class, msMaterialCategoryFeign::findAllList);
    }

    /**
     * 获取所有的配件分类  2020-5-16
     * @return
     */
    public List<MaterialCategory> findAllListWithIdAndName() {
        List<NameValuePair<Long, String>> nameValuePairList = MDUtils.findListUnnecessaryConvertType(()->msMaterialCategoryFeign.findAllListWithIdAndName());
        if (!ObjectUtils.isEmpty(nameValuePairList)) {
            return nameValuePairList.stream().map(r->{
                MaterialCategory materialCategory = new MaterialCategory();
                materialCategory.setId(r.getName());
                materialCategory.setName(r.getValue());
                return materialCategory;
            }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 获取分页数据
     * @param materialCategoryPage
     * @param materialCategory
     * @return
     */
    public Page<MaterialCategory> findList(Page<MaterialCategory> materialCategoryPage, MaterialCategory materialCategory) {
        return MDUtils.findListForPage(materialCategoryPage, materialCategory, MaterialCategory.class, MDMaterialCategory.class, msMaterialCategoryFeign::findList);
    }


    /**
     * 添加/更新
     * @param materialCategory
     * @param isNew
     * @return
     */
    public MSErrorCode save(MaterialCategory materialCategory, boolean isNew) {
        return MDUtils.genericSave(materialCategory, MDMaterialCategory.class, isNew, isNew?msMaterialCategoryFeign::insert:msMaterialCategoryFeign::update);
    }

    /**
     * 删除
     * @param materialCategory
     * @return
     */
    public MSErrorCode delete(MaterialCategory materialCategory) {
        return MDUtils.genericSave(materialCategory, MDMaterialCategory.class, false, msMaterialCategoryFeign::delete);
    }
}
