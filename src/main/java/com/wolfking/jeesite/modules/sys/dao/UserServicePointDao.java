package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserServicePointDao {
    /**
     * 查找
     *
     * @param user
     * @return
     */
    List<User> findList(User user);
}
