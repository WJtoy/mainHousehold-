package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.UserSub;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserSubDao {

    List<UserSub> getAllUserSubList();

    List<UserSub> getUserSubList(Long userId);

    Integer saveUserUnderling(UserSub userSub);

    List<Long> findCustomerIdListByUserId(@Param("userId") Long userId, @Param("type") Integer type, @Param("page") Page<UserSub> page);

    Integer deleteUserUnderling(Long userId);


    Integer deleteUserSub(Long userSubId);
}
