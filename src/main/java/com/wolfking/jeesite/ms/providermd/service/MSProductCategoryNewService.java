package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDProductCategory;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.ms.providermd.feign.MSProductCategoryNewFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSProductCategoryNewService {
    @Autowired
    private MSProductCategoryNewFeign msProductCategoryNewFeign;
    /**
     * 获取全部产品类别-->基础资料
     *
     * @return id, name
     */
    public List<NameValuePair<Long, String>> findAllListForMD() {
        return MDUtils.findListUnnecessaryConvertType(()->msProductCategoryNewFeign.findAllListForMD());
    }

    /**
     * 用实体类形式获取全部产品类别-->基础资料
     * @return id,name
     */
    public List<ProductCategory> findAllListForMDWithEntity() {
        return convertToProductCategoryList(findAllListForMD());
    }

    /**
     * 获取全部产品类别-->报表
     *
     * @return id, name
    */
    public List<ProductCategory> findAllListForRPTWithEntity() {
        return convertToProductCategoryList(findAllListForRPT());
    }

    /**
     * 获取全部产品类别-->报表
     *
     * @return id, name
     */
    public List<NameValuePair<Long, String>> findAllListForRPT() {
        return MDUtils.findListUnnecessaryConvertType(()->msProductCategoryNewFeign.findAllListForRPT());
    }

    /**
     * 获取全部产品类别-->工单
     *
     * @return id, name
     */
    public List<NameValuePair<Long, String>> findAllListForSD() {
        return MDUtils.findListUnnecessaryConvertType(()->msProductCategoryNewFeign.findAllListForSD());
    }

    /**
     * 批量获取获取产品类别-->基础资料
     *
     * @param ids
     * @return id, name
     */
    public List<NameValuePair<Long, String>> findListByIdsForMD(List<Long> ids) {
        return MDUtils.findListUnnecessaryConvertType(()->msProductCategoryNewFeign.findListByIdsForMD(ids));
    }

    /**
     * 批量获取获取产品类别-->基础资料
     *
     * @param ids
     * @return id, name
     */
    public List<ProductCategory> findListByIdsForMDWithEntity(List<Long> ids) {
        return convertToProductCategoryList(findListByIdsForMD(ids));
    }

    /**
     * 分页&条件(code,name)获取产品类别-->基础资料
     *
     * @param
     * @return
     */
    public Page<ProductCategory> findListForMD(Page<ProductCategory> page, ProductCategory productCategory) {
        return MDUtils.findListForPage(page,productCategory, ProductCategory.class, MDProductCategory.class, msProductCategoryNewFeign::findListForMD);
    }

    /**
     * 获取所有有效的品类id
     * @return
     */
    public List<Long> findIdListForMD() {
        return MDUtils.findListUnnecessaryConvertType(()->msProductCategoryNewFeign.findIdListForMD());
    }

    /**
     * 根据ID获取产品类别-->基础资料
     *
     * @param id
     * @return id，code,name,del_flag,remarks
     */
    public ProductCategory getByIdForMD(Long id) {
        return MDUtils.getObjNecessaryConvertType(ProductCategory.class, ()->msProductCategoryNewFeign.getByIdForMD(id));
    }

    /**
     * 根据ID从缓存读取-->基础资料
     *
     * @param id
     * @return id, name
     */
    public String getFromCacheForMD(Long id) {
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryNewFeign.getFromCacheForMD(id));
    }

    /**
     * 用实体类形式根据ID从缓存读取-->基础资料
     * @param id
     * @return  id,name
     */
    public ProductCategory getFromCacheForMDWithEntity(Long id) {
        String name = getFromCacheForMD(id);
        ProductCategory productCategory = new ProductCategory(id);
        productCategory.setName(name);
        return productCategory;
    }

    /**
     * 根据ID从缓存读取->工单
     *
     * @param id
     * @return name
     */
    public String getFromCacheForSD(Long id) {
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryNewFeign.getFromCacheForSD(id));
    }

    /**
     * 根据产品Code获取ID-->基础资料
     *
     * @param code
     * @return id
     */
    public Long getIdByCodeForMD(String code) {
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryNewFeign.getIdByCodeForMD(code));
    }

    /**
     * 根据产品Code获取ID-->基础资料
     *
     * @param name
     * @return id
     */
    public Long getIdByNameForMD(String name) {
        return MDUtils.getObjUnnecessaryConvertType(()->msProductCategoryNewFeign.getIdByNameForMD(name));
    }

    /**
     * 保存产品类别
     * @param productCategory
     * @param isNew
     * @return
     */
    public void save(ProductCategory productCategory, boolean isNew) {
        MSErrorCode msErrorCode =  MDUtils.genericSave(productCategory, MDProductCategory.class, isNew, isNew?msProductCategoryNewFeign::insert:msProductCategoryNewFeign::update);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("保存产品类别失败.失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 删除产品类别
     * @param productCategory
     * @return
     */
    public void delete(ProductCategory productCategory) {
        MSErrorCode msErrorCode = MDUtils.genericSave(productCategory, MDProductCategory.class, false, msProductCategoryNewFeign::delete);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除产品类别失败.失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 将NameValue列表转换为品类列表
     * @param nameValuePairList
     * @return
     */
    private List<ProductCategory> convertToProductCategoryList(List<NameValuePair<Long,String>> nameValuePairList) {
        if (nameValuePairList != null && !nameValuePairList.isEmpty()) {
            return nameValuePairList.stream().map(nv->{
                ProductCategory productCategory = new ProductCategory();
                productCategory.setId(nv.getName());
                productCategory.setName(nv.getValue());
                return productCategory;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 根据客户获取品类
     * @param customerId
     * @return
     */
    public List<NameValuePair<Long, String>> findIdAndNameListByCustomerIdForMD(Long customerId) {
        return MDUtils.findListUnnecessaryConvertType(()->msProductCategoryNewFeign.findIdAndNameListByCustomerIdForMD(customerId));
    }


    public List<ProductCategory> findIdAndNameListByCustomerIdWithEntity(Long customerId) {
        return convertToProductCategoryList(findIdAndNameListByCustomerIdForMD(customerId));
    }
}
