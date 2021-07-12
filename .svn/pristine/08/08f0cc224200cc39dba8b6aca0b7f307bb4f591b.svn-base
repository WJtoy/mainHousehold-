package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface ProductCategoryDao extends LongIDCrudDao<ProductCategory> {

    /**
     * 根据产品分类编码获取产品分类ID，最多一条，用于判断产品分类编码是否存在于数据库中
     * @param productCategory
     * @return
     */
    Long getIdByCode(ProductCategory productCategory);


    /**
     * 根据产品分类名称获取配件ID，最多一条，用于判断产品分类名称是否存在于数据库中
     * @param productCategory
     * @return
     */
    Long getIdByName(ProductCategory productCategory);
}
