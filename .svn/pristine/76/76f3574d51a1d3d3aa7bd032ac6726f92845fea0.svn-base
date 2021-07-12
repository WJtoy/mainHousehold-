package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProductCategoryBrand;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.BrandCategory;
import com.wolfking.jeesite.ms.providermd.feign.MSProductCategoryBrandFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.tmall.md.entity.B2BCategoryBrand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSProductCategoryBrandService {

    @Autowired
    private MSProductCategoryBrandFeign msProductCategoryBrandFeign;

    @Deprecated  //此方法后续停用  2020-1-7
    public List<B2BCategoryBrand> getCategoryBrandMap(String categoryIds) {
        MSResponse<List<B2BCategoryBrand>> msResponse = msProductCategoryBrandFeign.getCategoryBrandMap(categoryIds);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取产品的类型与品牌的对应关系
     * @param categoryIds
     * @return
     */
    public List<B2BCategoryBrand> findCategoryBrandMap(List<Long> categoryIds) {
        MSResponse<List<B2BCategoryBrand>> msResponse = msProductCategoryBrandFeign.findCategoryBrandMap(categoryIds);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 根据产品类别获取品牌id列表
     * @param categoryId
     * @return
     */
    public List<Long> getBrandIdsByCategoryId(Long categoryId) {
        MSResponse<List<Long>> msResponse = msProductCategoryBrandFeign.getBrandIdsByCategoryId(categoryId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取所有产品品牌
     * @return
     */
    public List<BrandCategory> findAllList() {
        return MDUtils.findAllList(BrandCategory.class, msProductCategoryBrandFeign::findAllList);
    }

    /**
     * 获取分页产品品牌
     * @param brandCategoryPage
     * @param brandCategory
     * @return
     */
    public Page<BrandCategory> findList(Page<BrandCategory> brandCategoryPage, BrandCategory brandCategory) {
        return MDUtils.findListForPage(brandCategoryPage, brandCategory, BrandCategory.class, MDProductCategoryBrand.class, msProductCategoryBrandFeign::findList);
    }

    /**
     * 批量插入品牌类别数据
     * @param brandCategoryList
     * @return
     */
    public MSErrorCode batchInsert(List<BrandCategory> brandCategoryList) {
        return MDUtils.genericBatchSave(brandCategoryList, MDProductCategoryBrand.class, msProductCategoryBrandFeign::batchInsert);
    }

    /**
     * 删除
     * @param id
     * @return
     */
    public MSErrorCode delete(Long id) {
        return MDUtils.genericCustomConditionSave(id, msProductCategoryBrandFeign::delete);
    }

    /**
     * 根据产品类别删除
     * @param categoryId
     * @return
     */
    public MSErrorCode deleteByCategoryId(Long categoryId) {
        return MDUtils.genericCustomConditionSave(categoryId, msProductCategoryBrandFeign::deleteByCategoryId);
    }
}
