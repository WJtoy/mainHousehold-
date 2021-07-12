package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserServicePointExploitDao {

    /**
     * 查找
     *
     * @param user
     * @return
     */
    List<User> findList(@Param("user") User user,@Param("roleIds") List<Long> roleIds,@Param("page") Page page);
}
