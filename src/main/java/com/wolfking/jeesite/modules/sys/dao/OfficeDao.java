/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.LongIDTreeDao;
import com.wolfking.jeesite.common.persistence.TreeDao;
import com.wolfking.jeesite.modules.sys.entity.Office;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机构DAO接口
 *
 * @author ThinkGem
 * @version 2014-05-16
 */
@Mapper
public interface OfficeDao extends LongIDTreeDao<Office> {

    /**
     * 按编码获得机构信息
     * @param code
     * @return
     */
    public Office getByCode(@Param("code") String code);

    /**
     * 按编码获得下属机构列表
     * @param code
     */
    public List<Office> getSubListByParentCode(@Param("code") String code);
}
