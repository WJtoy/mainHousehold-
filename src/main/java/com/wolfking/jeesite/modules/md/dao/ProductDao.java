package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface ProductDao extends LongIDCrudDao<Product> {

    /**
     * 新增产品与配件关联关系
     * @param map
     */
    void insertProductMaterial(HashMap<String, Object> map);

    /**
     * 更改产品排序
     * @param product
     */
    void updateSort(Product product);

    /**
     * 审核产品
     * @param product
     */
    void approveProduct(Product product);

    /**
     * 删除产品与配件关联关系
     * @param productId
     */
    void deleteProductMaterialByProductId(Long productId);

    /**
     * 获取产品配件关系
     * @return
     */
    List<ProductMaterial> getProductMaterialIdList();

    /**
     * 根据产品名称获取产品，最多一条，用于判断产品名称是否存在于数据库中
     * @param product
     * @return
     */
    Long getIdByName(Product product);

    /**
     * 根据产品分类ID获取产品ID，最多一条，用于判断产品分类下是否有产品
     * @param productCategoryId
     * @return
     */
    Long getIdByProductCategoryId(Long productCategoryId);

    /**
     * 获取所有非套组产品
     * @return
     */
    List<Product> getSingleProductList();

    /**
     * 获取所有产品--用于产品价格展示
     * @return
     */
    List<Product> findListForPrice(Product product);

    /**
     * 根据配件ID获取产品信息，最多一条，用于判断配件是否被产品使用
     * @param materialId
     * @return
     */
    HashMap<String, Object> getProductByMaterialId(Long materialId);

    /**
     * 通过配件id来获取ProductMaterial中的productId(为配合Product微服务而新增,取代getProductByMaterialId方法）
     * @param materialId
     * @return
     */
    Long getProductMaterialByMaterialId(Long materialId);

    /**
     * 根据产品ID获取用此产品组合套组的产品信息
     * @param productId
     * @return
     */
    HashMap<String, Object> getSetProductByProductId(Long productId);

    /**
     * 获取客户下面的所有产品
     * @param customerId
     * @return
     */
    List<HashMap<String, Object>> getIdsByCustomerId(@Param("customerId") Long customerId);

    /**
     * 获取服务网点下的所有产品
     * @param servicePointId
     * @return
     */
    List<HashMap<String, Object>> getIdsByServicePointId(@Param("servicePointId") Long servicePointId);
}
