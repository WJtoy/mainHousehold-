/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.LongIDTreeDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserRegion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 帐号区域
 *
 * @author Ryan
 * @version 2019-10-11
 */
@Mapper
public interface UserRegionDao extends LongIDCrudDao<UserRegion> {

    /**
     * 获取所有网点的覆盖区域
     */
    List<UserRegion> getUserRegions(Long userId);

    /**
     * 按用户及区域类型删除
     * @param userId
     * @param areaType
     * @return
     */
    int deleteByUserAndAreaType(@Param("userId") long userId,@Param("areaType") int areaType);

    /**
     * 批量删除用户区域
     * @param userId
     * @return
     */
    int deleteByUserId(@Param("userId") long userId);

    /**
     * 查询所有数据列表
     * @see public List<T> findAllList(T entity)
     * @return
     */
    List<UserRegion> findAllList(@Param("page") Page<UserRegion> page);

    /**
     * 按用户及区域类型删除
     * @param userId
     * @param areaType
     * @param areaType
     * @return
     */
    int deleteByUserAndAreaAndAreaType(@Param("userId") long userId,@Param("areaId")long areaId,@Param("areaType") int areaType);

}
