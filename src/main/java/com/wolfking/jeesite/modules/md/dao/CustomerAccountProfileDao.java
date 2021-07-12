package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2017-05-03.
 */
@Mapper
public interface CustomerAccountProfileDao extends LongIDCrudDao<CustomerAccountProfile> {

    User getUser(Long id);

    CustomerAccountProfile get(Long id);

    CustomerAccountProfile getByUserId(Long userId);

    /**
     * 查询数据列表，如果需要分页，请设置分页对象，如：entity.setPage(new Page<T>());
     * @param entity
     * @return
     */
    List<User> findList(User entity);

    /**
     * 查询用户账户信息
     * @param entity
     * @return
     */
    List<User> findListWithOutCustomerAccountProfile(User entity);

    Integer hasOtherPrimaryAccount(@Param("customerId") Long customerId,@Param("expectId") Long expectId);

    /**
     * 重置密码（默认是手机号mobile后六位）
     * @param entity
     */
    void resetPassword(User entity);
}
