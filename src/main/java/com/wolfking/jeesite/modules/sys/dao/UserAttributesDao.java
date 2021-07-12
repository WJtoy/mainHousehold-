package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.modules.sys.entity.UserAttributes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAttributesDao {

    UserAttributes getUserAttributesList(@Param("userId") Long userId, @Param("type") Integer type);

    Integer saveUserAttributes(UserAttributes userAttributes);

    Integer deleteUserAttributes(@Param("userId") Long userId, @Param("type") Integer type);
}
