package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserKeFuDao {

    /**
     * 查找
     *
     * @param user
     * @return
     */
    List<User> findList(User user);


    List<UserProductCategory> getProductCategoryIds(@Param("userIds") List<Long> userIds);

    int insert(User user);

    int update(User user);

    int delete(User user);

    Integer getManagerFlag(Long userId);

    int updateSubFlag(@Param("userId") Long userId,@Param("subFlag") Integer subFlag);

    /**
     * 按品类+区县+客服类型+市+省读取客服主管
     * @param productCategoryId 品类
     * @param areaId    区县id
     * @param subFlag 客服类型
     * @param cityId    市id
     * @param provinceId  省id
     * @return
     */
     User getKefuSupervisor(@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId,@Param("subFlag") Integer subFlag,
                              @Param("cityId") Long cityId,@Param("provinceId") Long provinceId);

    /**
     * 按客户+品类+区县读取+市+省读取KA客服主管
     * @param customerId    客户id
     * @param productCategoryId 品类
     * @param areaId    区县id
     * @param cityId    市id
     * @param provinceId  省id
     * @return
     */
    User getVIPKefuSupervisor(@Param("customerId") Long customerId,@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId,
                                           @Param("cityId") Long cityId,@Param("provinceId") Long provinceId);


}
