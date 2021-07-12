/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 字典DAO接口
 * 
 * @author KodyHuang
 * @version 2014-9-25
 */
@Mapper
public interface ServiceTypeDao extends LongIDCrudDao<ServiceType> {

	List<String> findTypeList();

	List<ServiceType> findListServiceName(String name);

	ServiceType findServiceTypeByName(String name);

	ServiceType findServiceTypeByCode(String name);
}
