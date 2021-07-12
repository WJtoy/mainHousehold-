package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.BrandCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌 和产品分类DAO
 * Created on 2017-04-12.
 */
@Mapper
public interface BrandCategoryDao extends LongIDCrudDao<BrandCategory> {

    void deteleByCategoryId(Long categoryId);

    List<Long> getBrandIdsByCategoryId(@Param("categoryId") Long categoryId);
}
