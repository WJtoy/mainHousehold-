/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.Grade;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评价项目DAO接口
 * @author ThinkGem
 * @version 2013-8-23
 */
@Mapper
public interface GradeDao extends LongIDCrudDao<Grade> {

    /**
     * 读取生效的客评项及评分
     */
    List<Grade> findAllEnabledGradeAndItems();
}
