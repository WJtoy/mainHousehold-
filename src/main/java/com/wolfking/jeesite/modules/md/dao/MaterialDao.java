package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.Material;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface MaterialDao extends LongIDCrudDao<Material> {

    /**
     * 根据配件名称获取配件ID，最多一条，用于判断配件名称是否存在于数据库中
     * @param material
     * @return
     */
    Long getIdByName(Material material);

    /**
     * 根据产品ID查找配件列表(包含价格)
     * @param productId
     * @return
     */
    List<Material> getMaterialListByProductId(Long productId);

    /**
     * 根据产品ID查找配件ID列表
     * @param productId
     * @return
     */
    List<Long> getMaterialIdListByProductId(Long productId);


    /**
     * 获取分组价格列表
     * @param productIds
     * @return
     */
    List<Long> getMaterialIdListByProductIdList(List<Long> productIds);

    /**
     * 获取分组价格列表
     * @param productIds
     * @return
     */
    List<Material> getMaterialListByProductIdList(List<Long> productIds);

    /**
     * 根据配件类别获取配件ID，最多一条，用于判断删除配件类别是是否已经绑定配件
     * @param materialCategoryId
     * @return
     */
    Long getByMaterialCategoryId(Long materialCategoryId);


    /**
     * 根据产品id获取客户列表(待product_material转到微服务后删除)
     * @param productId
     * @return
     */
    List<Long> getMaterialIdByProductId(Long productId);

}
