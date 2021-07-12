/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.LongIDTreeDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 区域DAO接口
 *
 * @author ThinkGem
 * @version 2014-05-16
 */
@Mapper
public interface AreaDao extends LongIDTreeDao<Area> {

    List<Area> findSpecList(Area area);  //mark on 2020-8-5

    /**
     * 通过用户id或区域id获取区域列表
     * @param paramMap
     * @return
     */
    // TODO: 去掉sys_user_area表  //add on 2019-12-16
    //List<Area> findListByUserIdOrAreaId(Map<String,Object> paramMap);  //mark on 2020-8-5

    /**
     * 根据用户id在sys_user_area中获取区域id列表  add on 2020-7-29
     * @param userId
     * @param page
     * @return
     */
    // 去掉sys_user_area add on2020-09-05
    //List<Long> findAreaIdListByUserId(@Param("userId") Long userId, @Param("page") Page<Area> page);


    List<Area> findListByType(@Param("type") Integer type);  //mark on 2020-8-5

    /**
     * 通过区域类型获取安维所覆盖的网点
     * @param type
     * @return
     */
    // TODO: 此方法已没有地方调用,记得删除此方法  //add on 2019-12-16
    //List<Area> findListByEngineerAndType(Integer type);  //mark on 2020-8-5

    /**
     * 获取所有网点的覆盖区域
     *
     * @param type
     * @return
     */
    //List<Area> getServicePointCoverAreasByAreaType(Integer type);  //mark on 2020-8-5

    /**
     * 获取区，市，省区域信息  add on 2019-9-20
     * @param id
     * @return
     */
    //Area getTripleAreaById(long id); //mark on 2020-8-5

    /**
     * 获取区，市，省区域列表信息  add on 2019-9-20
     * @param map
     * @return
     */
    //List<Area> findProvinceCityCountyList(Map<String,Object> map);  //mark on 2020-7-29

    /**
     * 根据ParentIds和type获取所有下级的数据  add on 2019-10-21
     * @param area
     * @return
     */
    List<Long> findIdByParentIdsAndType(Area area);  //mark on 2020-8-5


    /**
     * 获取所有网点街道
     * @return
     */
    //List<Area> getServicePointCoverTown(Integer limitValue);   //mark on 2020-1-20 web端去md_servicepoint_station

    /**
     * 获取所有网点街道个数
     * @return
     */
    //Integer getTownCount(); //mark on 2020-8-5

    List<Area> findAreasForServicePointOrEngineer(@Param("ids") List<Long> ids, @Param("page") Page<Area> page);   //mark on 2020-8-5


}
