/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sys.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色DAO接口
 *
 * @author ThinkGem
 * @version 2013-12-05
 */
@Mapper
public interface RoleDao extends LongIDCrudDao<Role> {

    Role getNew(@Param("id") Long id);   // add on 2020-12-1 office微服务化

    //Role getById(@Param("id") Long id);  //mark on 2020-12-19

    Role getByIdNew(@Param("id") Long id);  // add on 2020-12-1 office微服务化

    //Role getByName(Role role);   //mark on 2020-12-19

    Role getByNameNew(Role role);  // add on 2020-12-2 office微服务化

    //Role getByEnname(Role role);  //mark on 2020-12-19

    Role getByEnnameNew(Role role);  // add on 2020-12-2 office微服务化

    List<Role> findListNew(Role role); // add on 2020-12-2 office微服务化

    List<Role> findAllListNew(); // add on 2020-12-2 office微服务化

    /**
     * 维护角色与菜单权限关系
     *
     * @param role
     * @return
     */
    int deleteRoleMenu(Role role);

    int insertRoleMenu(Role role);

    /**
     * 维护角色与公司部门关系
     *
     * @param role
     * @return
     */
    int deleteRoleOffice(Role role);

    int insertRoleOffice(Role role);

    /**
     * 按用户ID获得角色列表
     */
    List<Role> getUserRoles(@Param("userId") Long userId);

    List<Long> getUserIdList(Role role);


    List<Role> getUserRolesByName(@Param("name") String name);

    List<Role> getUserRolesByOfficeId(@Param("officeId") Long officeId);
}
