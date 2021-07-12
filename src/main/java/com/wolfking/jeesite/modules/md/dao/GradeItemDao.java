/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价标准DAO接口
 * @author
 * @version
 */
@Mapper
public interface GradeItemDao extends LongIDCrudDao<GradeItem> {
    /**
     * 获取评价明细
     * @param entity
     * @return
     */
    java.util.List<GradeItem> findListByGradeID(GradeItem entity);
}
