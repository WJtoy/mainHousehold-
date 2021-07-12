package com.wolfking.jeesite.ms.providermd.service;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDProductCategory;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.ms.providermd.feign.MSProductCategoryFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class MSProductCategoryService {
    @Autowired
    private MSProductCategoryFeign msProductCategoryFeign;
    /**
     * 根据id获取产品类别
     * @param id
     * @return
     */
    public ProductCategory getById(Long id) {
        return MDUtils.getObjNecessaryConvertType(ProductCategory.class, ()->msProductCategoryFeign.getById(id));
    }

    /**
     * 根据id获取产品类别对象
     * @param id
     * @return
     */
    public ProductCategory getNameByIdToEntity(Long id) {
        String strName =  getNameById(id);
        ProductCategory productCategory = new ProductCategory();
        productCategory.setId(id);
        productCategory.setName(strName);
        return productCategory;
    }

    /**
     * 根据id获取产品类别名称
     * @param id
     * @return
     */
    public String getNameById(Long id) {
//        String strName =  MDUtils.getByCustomCondition(()->msProductCategoryFeign.getNameById(id));
//        return strName;
        String strName =  MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryFeign.getNameById(id));
        return strName;
    }

    /**
     * 根据id获取产品类别
     * @param id
     * @return
     */
    public ProductCategory getFromCache(Long id) {
        return MDUtils.getObjNecessaryConvertType(ProductCategory.class, ()->msProductCategoryFeign.getFromCache(id));
    }

    /**
     * 根据id获取产品类别
     * @param id
     * @return
     */
    public String getNameFromCache(Long id) {
        //return MDUtils.getByCustomCondition(()->msProductCategoryFeign.getNameFromCache(id));
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryFeign.getNameFromCache(id));
    }

    /**
     * 根据code获取产品类别id
     * @param code
     * @return
     */
    public Long getIdByCode(String code) {
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryFeign.getIdByCode(code));
    }

    /**
     * 根据name获取产品类别id
     * @param name
     * @return
     */
    public Long getIdByName(String name) {
//        return MDUtils.getByCustomCondition(name,msProductCategoryFeign::getIdByName);
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryFeign.getIdByName(name));
    }

    /**
     * 获取所有的产品类别列表
     * @return
     */
    public List<ProductCategory> findAllList() {
        return MDUtils.findAllList(ProductCategory.class, msProductCategoryFeign::findAllList);
    }

    /**
     * 根据id列表获取所有的产品类别列表
     * @param ids
     * @return
     * 返回三个字段： id, code ,name
     */
    public List<ProductCategory> findListByIds(List<Long> ids) {
        return MDUtils.findListNecessaryConvertType(ProductCategory.class, () -> msProductCategoryFeign.findListByIds(ids));
    }

    /**
     * 产品类别列表
     * @param page
     * @param productCategory
     * @return
     */
    public Page<ProductCategory> findList(Page<ProductCategory> page, ProductCategory productCategory) {
        return MDUtils.findListForPage(page, productCategory, ProductCategory.class, MDProductCategory.class, msProductCategoryFeign::findList);
    }

    /**
     * 保存产品类别
     * @param productCategory
     * @param isNew
     * @return
     */
    public MSErrorCode save(ProductCategory productCategory, boolean isNew) {
        return MDUtils.genericSave(productCategory, MDProductCategory.class, isNew, isNew?msProductCategoryFeign::insert:msProductCategoryFeign::update);
    }

    /**
     * 删除产品类别
     * @param productCategory
     * @return
     */
    public MSErrorCode delete(ProductCategory productCategory) {
        return MDUtils.genericSave(productCategory, MDProductCategory.class, false, msProductCategoryFeign::delete);
    }
}
