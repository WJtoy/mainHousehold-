package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户产品完成图片dao
 * Created on 2018-08-17
 */
@Mapper
public interface CustomerProductCompletePicDao extends LongIDCrudDao<ProductCompletePic> {

    /**
     * 物理删除
     * @param id
     */
     void deleteById(@Param("id") Long id);

     List<ProductCompletePic> getAllList(@Param("customerId") Long customerId);

}
