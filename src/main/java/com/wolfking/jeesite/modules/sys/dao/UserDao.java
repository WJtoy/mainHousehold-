/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户DAO接口
 *
 * @author ThinkGem
 * @version 2014-05-16
 */
@Mapper
public interface UserDao extends LongIDCrudDao<User> {

    /**
     * 根据id获取
     * @param id
     * @return
     */
    User getNew(@Param("id") Long id);  // add on 2020-12-3 Office微服务化

    /**
     * 根据id获得帐号基本信息(不关联其他表)
     * @param id
     * @return
     */
    User getBaseInfo(@Param("id") Long id);
    /**
     * 根据登录名称查询用户
     *
     * @param user
     * @return
     */
    //User getByLoginName(User user);

    /**
     * 根据登录名称查询用户
     *
     * @param user
     * @return
    */
    User getByLoginNameNew(User user);  // add on 2020-12-3 Office微服务化

    /**
     * 根据登录名及用户类型查询用户
     * @param loginName
     * @param userTypes
     * @return
     */
    //User getByLoginNameAndType(@Param("loginName") String loginName,@Param("customerId") Long customerId,@Param("userTypes") Integer[] userTypes);

    /**
     * 根据登录名及用户类型查询用户
     * @param loginName
     * @param userTypes
     * @return
     */
    //User getByLoginNameAndTypeWithoutCustomerAccountProfile(@Param("loginName") String loginName,@Param("userTypes") Integer[] userTypes);

    /**
     * 根据登录名及用户类型查询用户
     * @param loginName
     * @param userTypes
     * @return
     */
    User getByLoginNameAndTypeWithoutCustomerAccountProfileNew(@Param("loginName") String loginName,@Param("userTypes") Integer[] userTypes);  // add on 2020-12-3 office微服务化

    /**
     *
     * @param user
     * @return
     */
    //User getAppUserByLoginName(User user);

    /**
     *
     * @param user
     * @return
     */
    User getAppUserByLoginNameNew(User user);   // add on 2020-12-3 office微服务化


    /**
     * 检查系统帐号是否注册
     * @param id
     * @param loginName
     * @return
     */
    Long checkLoginName(@Param("id") Long id,@Param("loginName") String loginName);

    Long checkLoginCustomerId(@Param("name") String name,@Param("phone") String phone,@Param("customerId") Long customerId);
    /**
     * 按安维id获得登录帐号信息
     * @param engineerId
     * @return
     */
    User getByEngineerId(@Param("engineerId") Long engineerId);

    /**
     * 通过OfficeId获取用户列表，仅返回用户id和name（树查询用户时用）
     *
     * @param user
     * @return
     */
    List<User> findUserByOfficeId(User user);

    int userEnableDisable(@Param("userId") Long userId,@Param("statusFlag") Integer statusFlag);
    /**
     * 查找
     * @param user
     * @return
     */
    List<User> findListNew(User user);

    /**
     * 查询全部用户数目
     *
     * @return
     */
    long findAllCount(User user);

    /**
     * 更新用户密码
     *
     * @param user
     * @return
     */
    int updatePasswordById(User user);

    /**
     * 更新登录信息，如：登录IP、登录时间
     *
     * @param user
     */
    int updateLoginInfo(User user);

    /**
     * 读取账户登录信息，包含ip和日期
     * @param id
     * @return  Map:
     *      login_ip
     *      login_date
     */
    HashMap<String, Object> getLoginInfo(@Param("id") Long id);

    // 按帐号ID更新
    int updateUserByParms(HashMap<String,Object> maps);

    // 按安维id更新
    int updateUserByEngineerId(HashMap<String,Object> maps);

    /**
     * 根据用户id获取用户角色  2020-11-20
     * @param userId
     * @return
     */
    Long getUserRoleByUserId(@Param("userId") Long userId);

    /**
     * 删除用户角色关联数据
     *
     * @param user
     * @return
     */
    int deleteUserRole(User user);

    /**
     * 插入用户角色关联数据
     *
     * @param user
     * @return
     */
    int insertUserRole(User user);

    /**
     * 在新增网点并发调用时使用方法：insertUserRole，会出现 Lock wait timeout exceeded; try restarting transaction
     * 插入用户角色关联数据
     * 2020-11-18
     * @param user
     * @return
    */
    int insertSingleUserRole(User user);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    int updateUserInfo(User user);

    /**
     * 删除用户区域关联数据
     *
     * @param user
     * @return
     */
    //int deleteUserArea(User user);

    /**
     * 插入用户区域关联数据
     *
     * @param user
     * @return
     */
    //去掉sys_user_area 表
    //int insertUserArea(User user);

    /**
     * 删除用户客户关联数据
     *
     * @param user
     * @return
     */
    int deleteUserCustomer(User user);

    /**
     * 插入用户客户关联数据
     *
     * @param user
     * @return
     */
    int insertUserCustomer(User user);

    /**
     * 根据业务员姓名获得qq
     * @param name
     * @return
     */
    String getSalesQq(String name);

    List<User> getSaleList(@Param("subFlag") Integer subFlag, @Param("delFlag") Integer delFlag);

    List<User> getSaleListBySubFlagList(@Param("subFlagList") List<Integer> subFlag, @Param("delFlag") Integer delFlag);


    List<User> getAllSales();
//    /**
//     * 获得所有客户下的所有帐号
//     */
//    List<User> findAllCustomerAccountList();

    /**
     * 根据客户id获得所有帐号列表
     * 只返回:id,user_type
     * @param companyId 客户id
     */
    List<User> findCustomerAccountIdList(@Param("companyId") Long companyId);

    /**
     * 按组织id获得管理的人员帐号
     * @param officeId
     * @param userType
     * @return
     */
    List<User> findOfficeAccountList(@Param("officeId") Long officeId,@Param("userType") Integer userType);

    /**
     * 按组织id和类型，角色名获得人员帐号
     * @param officeId  组织id
     * @param userType  用户类型
     * @param roleName  角色名称
     * @return
     */
    List<User> findOfficeLeaderList(@Param("officeId") Long officeId ,@Param("userType") Integer userType,@Param("roleName") String roleName,@Param("enName") String enName);

    /**
     * 按客户获得负责的客服列表
     */
    List<User> getKefuListByCustomer(@Param("customerId") Long customerId);

    /**
     * 按区域获得负责的客服列表
     */
    //List<User> getKefuListByArea(@Param("areaId") Long areaId);

    /**
     * 按手机号返回帐号信息(id,name,mobile)
     * @param mobile
     * @param expectType 排除id类型:user,engineer
     * @param expectId   排除id
     */
    List<Map<String, Object>> getByMobile(@Param("mobile") String mobile,@Param("expectType") String expectType,@Param("expectId") Long expectId);

    void enableUser(@Param("id") Long id);

    void updateNew(User user);
    /**
     * 按手机号返回帐号delflag
     * @param mobile
     * @return
     */
    Integer getDelFlagByMobile(@Param("mobile") String mobile);

    /**
     * 按手机号返回帐号Id
     * @param mobile
     * @return
     */
    Long getIdByMobile(@Param("mobile") String mobile);

    /**
     * 按ID返回密码
     * @param id
     * @return
     */
    String getPasswordById(@Param("id") Long id);

    /**
     * 查找指定的用户列表：每个User对象只有id、name属性有值
     */
    List<User> getListByUserType(@Param("userType") Integer userType);

    User getBaseInfoByEngineerId(@Param("engineerId") Long engineerId);

    /**
     * 根据customerId或userId获取sys_user_customer中customerId列表
     */
    List<Long> findCustomerIdList(Map<String, Object> paramMap);

    /**
     * 根据客服id获取vip客户id列表
     */
    List<Long> findVipCustomerIdListByKefu(@Param("userId") Long userId);

    /**
     * 获取用户被授权的产品类目
     */
    List<Long> getAuthorizedProductCategoryIds(@Param("userId") Long userId);

    /**
     * 查询所有为主账号的安维人员列表信息
     * @return
     */
    List<User> findEngineerAccountsList(@Param("engineerIds") List<Long> engineerIds, @Param("subFlag") Integer SubFlag); // TODO: 此方法在ServicePoint，Engineer，EngineerArea微服务化时来取代ServicePointDao中的 getEngineerAccountsListByAreaAndProductCategory方法 //2019-11-8

    /**
     * 查询用户账户信息
     * @param entity
     * @return
     */
    // 来自CustomerAccountProfileDao  // 2020-1-11
    List<User> findListWithOutCustomerAccountProfile(User entity);

    List<Role> findListUserRoleName(@Param("userIds") List<Long> userIds);
    /**
     * 重置密码（默认是手机号mobile后六位）
     * @param entity
     */
    // 来自CustomerAccountProfileDao // 2020-1-11
    void resetPassword(User entity);

    /**
     * 检查用户的删除(del_flag)状态是否等于0(正常)
     * @param userIds
     * @return
     */
    List<Long> checkDelFlagOfUser(@Param("userIds") List<Long> userIds);

    /**
     * 根据客户id获取客户账号(正常状态)
     * @param id
     * @return
     */
    List<User> findCustomerAccountListByCustomerId(@Param("customerId") Long id);

    /**
     * 根据客户id获取客户账号(正常状态)
     * @param ids
     * @return
     */
    List<User> findUserByIds(@Param("ids") List<Long> ids);

    /**
     * 获取非主管用户
     */
    List<User> findUserByUserTypeSubFlag(@Param("userType") Integer userType, @Param("subFlag") Integer subFlag);
}
