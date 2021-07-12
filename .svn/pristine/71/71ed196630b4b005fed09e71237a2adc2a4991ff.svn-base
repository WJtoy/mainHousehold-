package com.wolfking.jeesite.ms.providermd.service;


import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProduct;
import com.kkl.kklplus.entity.md.MDProductMaterial;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductMaterial;
import com.wolfking.jeesite.ms.providermd.feign.MSProductFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MSProductService {
    @Autowired
    private MSProductFeign msProductFeign;

    @Autowired
    private MapperFacade mapper;
    /**
     * 根据id获取产品信息
     * @param id
     * @return
     */
    public Product getById(Long id) {
        return MDUtils.getById(id, Product.class, msProductFeign::getById);
    }

    /**
     * 根据产品分类id获取产品id
     * @param productCategoryId
     * @return
     */
    public Long getIdByProductCategoryId(Long productCategoryId) {
        //return MDUtils.getByCustomCondition(productCategoryId, msProductFeign::getIdByProductCategoryId);
        return MDUtils.getObjUnnecessaryConvertType(()->msProductFeign.getIdByProductCategoryId(productCategoryId));
    }

    /**
     * 根据产品id判断是否有套组产品包含此产品
     * @param productId
     * @return
     */
    public Product getSetProductByProductId(Long productId) {
        return MDUtils.getById(productId, Product.class, msProductFeign::getSetProductByProductId);
    }

    /**
     * 根据产品名称获取产品id
     * @param name
     * @return
     */
    public Long getIdByName(String name) {
//        return MDUtils.getByCustomCondition(name, msProductFeign::getIdByName);
        return MDUtils.getObjUnnecessaryConvertType(()->msProductFeign.getIdByName(name));
    }

    /**
     * 获取所有的产品数据
     * @return
     */
    public List<Product> findAllList() {
        return MDUtils.findAllList(Product.class, msProductFeign::findAllList);
    }

    /**
     * 根据产品类别id获取产品列表
     * @param productCategoryId
     * @return
     */
    public List<Product> findListByProductCategoryId(Long productCategoryId) {
        //return MDUtils.findListByCustomCondition(productCategoryId, Product.class, msProductFeign::findListByProductCategoryId);
        return MDUtils.findAllList(Product.class,()->msProductFeign.findListByProductCategoryId(productCategoryId));
    }

    /**
     * 根据产品类别id获取单品产品列表
     * @param productCategoryId
     * @return
     */
    public List<Product> findSingleListByProductCategoryId(Long productCategoryId) {
        //return MDUtils.findListByCustomCondition(productCategoryId, Product.class, msProductFeign::findListByProductCategoryId);
        return MDUtils.findAllList(Product.class,()->msProductFeign.findSingleListByProductCategoryId(productCategoryId));
    }

    /**
     * 分页获取产品信息
     * @param productPage
     * @param product
     * @return
     */
    public Page<Product> findList(Page<Product> productPage, Product product) {
        return MDUtils.findListForPage(productPage, product, Product.class, MDProduct.class, msProductFeign::findList);
    }

    /**
     * 根据条件获取产品列表数据
     * @param product
     * @return
     * id,name,set_flag,sort,product_category_id
     */
    public List<Product> findListByConditions(Product product) {
        return MDUtils.findList(product, Product.class, MDProduct.class, msProductFeign::findListByConditions);
    }

    /**
     * 分页获取产品信息
     * @param productPage
     * @param product
     * @return
     */
    public Page<Product> findListForPrice(Page<Product> productPage, Product product) {
        return MDUtils.findListForPage(productPage, product, Product.class, MDProduct.class, msProductFeign::findListForPrice);
    }

    /**
     * 添加/更新
     * @param product
     * @param isNew
     * @return
     */
    public MSErrorCode save(Product product, boolean isNew) {
        return MDUtils.genericSave(product, MDProduct.class, isNew, isNew?msProductFeign::insert:msProductFeign::updateProduct);
    }

    /**
     * 更新产品排序
     * @param productList
     * @return
     */
    public MSErrorCode updatSort(List<Product> productList) {
        return MDUtils.genericBatchSave(productList, MDProduct.class, msProductFeign::updateSort);
    }

    /**
     * 产品审核
     * @param product
     * @return
     */
    public MSErrorCode approveProduct(Product product) {
        return MDUtils.genericSave(product, MDProduct.class, false, msProductFeign::approveProduct);
    }

    /**
     * 逻辑删除
     * @param product
     * @return
     */
    public MSErrorCode delete(Product product) {
        return MDUtils.genericSave(product, MDProduct.class, false, msProductFeign::delete);
    }


    /**
     * 根据配件获取产品配件,用于删除配件是判断配件是否绑定产品
     * @param materialId
     * @return
     */
    public HashMap<String,Object> getProductByMaterialId(Long materialId){
        MSResponse<HashMap<String,Object>> mapMSResponse =  msProductFeign.getProductByMaterialId(materialId);
        if(MSResponse.isSuccess(mapMSResponse)){
            return mapMSResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 获取所有的产品配件,用于web端redis缓存产品配件
     * @return
     */
    public List<ProductMaterial> findAllProductMaterial(){
        MSResponse<List<MDProductMaterial>> msResponse = msProductFeign.findAllProductMaterial();
        if(MSResponse.isSuccess(msResponse)){
            List<ProductMaterial> productMaterialList = mapper.mapAsList(msResponse.getData(),ProductMaterial.class);
            return productMaterialList;
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 根据id从缓存获取产品信息
     * @param id
     * @return
     */
    public Product getProductByIdFromCache(Long id){
        return MDUtils.getById(id, Product.class, msProductFeign::getProductByIdFromCache);
    }


    /**
     * 根据Id集合从缓存获取产品集合
     * @param ids
     * @return
     */
    public List<Product> findProductByIdListFromCache(List<Long> ids){
        return MDUtils.findAllList(Product.class,()->msProductFeign.findProductByIdListFromCache(ids));
    }

    /**
     * 根据客户与品类获取产品
     * @param productCategoryId
     * @return
     */
    public List<Product> findListByCustomerIdAndCategoryId(Long customerId,Long productCategoryId) {
        return MDUtils.findAllList(Product.class,()->msProductFeign.findListByCustomerIdAndCategoryId(customerId,productCategoryId));
    }
}
