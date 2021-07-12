package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌DAO
 * Created on 2017-04-12.
 */
@Mapper
public interface BrandDao extends LongIDCrudDao<Brand> {

    Long getIdByName(Brand brand);

    Long getIdByCode(Brand brand);
}
