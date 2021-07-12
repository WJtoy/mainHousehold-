package com.wolfking.jeesite.ms.providermd.service;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDProduct;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.ms.providermd.feign.MSProductVerSecondFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MSProductVerSecondService {

    @Autowired
    private MSProductVerSecondFeign msProductVerSecondFeign;

    /**
     * 根据id 获取产品名称
     * @param productId
     * @return
     */
    public String getNameByIdForMD(Long productId){
        return MDUtils.getObjUnnecessaryConvertType(() -> msProductVerSecondFeign.getNameByIdForMD(productId));
    }

    /**
     * 根据id 获取产品
     * @param
     * @return id,name,product_category_id,model,pinYin,sort,flag,remarks
     */
    public Product getSpecColumnByIdForMD(Long productId){
        return MDUtils.getById(productId, Product.class, msProductVerSecondFeign::getSpecColumnByIdForMD);
    }

    /**
     * 分页获取产品信息
     * @param productPage
     * @param product
     * @return
     */
    public Page<Product> findListForMD(Page<Product> productPage, Product product) {
        return MDUtils.findListForPage(productPage, product, Product.class, MDProduct.class, msProductVerSecondFeign::findListForMD);
    }

    /**
     * 获取所有的已审核产品数据
     * @return
     */
    public List<Product> findAllProductListForMD() {
        return MDUtils.findAllList(Product.class, msProductVerSecondFeign::findAllProductListForMD);
    }

    /**
     * 添加/更新
     * @param product
     * @param isNew
     * @return
     */
    public MSErrorCode saveForMD(Product product, boolean isNew) {
        return MDUtils.genericSave(product, MDProduct.class, isNew, isNew?msProductVerSecondFeign::insertForMD:msProductVerSecondFeign::updateProductForMD);
    }

    /**
     * 更新产品排序
     * @param productList
     * @return
     */
    public MSErrorCode updateSortForMD(List<Product> productList) {
        return MDUtils.genericBatchSave(productList, MDProduct.class, msProductVerSecondFeign::updateSortForMD);
    }

    /**
     * 逻辑删除
     * @param product
     * @return
     */
    public MSErrorCode deleteForMD(Product product) {
        return MDUtils.genericSave(product, MDProduct.class, false, msProductVerSecondFeign::deleteForMD);
    }

    /**
     * 根据产品名称获取产品id
     * @param name
     * @return
     */
    public Long getIdByNameForMD(String name) {
        return MDUtils.getObjUnnecessaryConvertType(()->msProductVerSecondFeign.getIdByNameForMD(name));
    }

    /**
     * 根据产品id判断是否有套组产品包含此产品
     * @param productId
     * @return
     */
    public Product getSetProductByProductIdForMD(Long productId) {
        return MDUtils.getById(productId, Product.class, msProductVerSecondFeign::getSetProductByProductIdForMD);
    }
}
