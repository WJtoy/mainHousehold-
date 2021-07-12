package com.wolfking.jeesite.ms.tmall.md.dao;

import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.ms.tmall.md.entity.B2BCategoryBrand;
import com.wolfking.jeesite.ms.tmall.md.entity.B2BProductCategory;
import com.wolfking.jeesite.ms.tmall.md.entity.ServicePointProductCategoryIdsMap;
import com.wolfking.jeesite.ms.tmall.md.entity.ServicePointProvinceBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface B2BServicePointDao {

   ServicePoint getServicePoint(Long servicePointId);

   List<B2BProductCategory> getB2BProductCategoryMap(Long servicePointId);

   //List<B2BCategoryBrand> getCategoryBrandMap(Long servicePointId);   // mark on 2019-8-22
   List<B2BCategoryBrand> getCategoryBrandMap(@Param("categoryIds") String categoryIds);      // add on 2019-8-22

   Long getServicePointCapacity(Long servicePointId);

   List<Area> getServicePointServiceAreas(Long servicePointId);

   Engineer getEngineer(Long engineer);

   ServicePoint getOldServicePointInfo(Long servicePointId);
   List<Engineer> getEngineerlList(Long servicePointId);

   List<Long> getServicePointIds(@Param("forTmall") Integer forTmall);
   List<Engineer> getEngineers(@Param("forTmall") Integer forTmall);

   List<ServicePointProductCategoryIdsMap> getServicePointProductCategoryIdsMap();
   List<Long> getLastModifiedProductCategoryIds(@Param("startModifyDate")Date startModifyDate);

   /**
    * 查询每个市的网点数量
    * @return
    */
   List<ServicePointProvinceBatch> getCityServicePointCount(@Param("provinceId") Long provinceId);

   /**
    * 查询每个市的师傅数量
    * @return
    */
   List<ServicePointProvinceBatch> getCityEngineerCount(@Param("provinceId") Long provinceId);

   /**
    * 查询指定市的所有网点ID列表
    * @param cityId
    * @return
    */
   List<Long> getServicePointIdsByCityId(@Param("cityId") Long cityId);

   /**
    * 查询指定市的所有师傅ID列表
    * @param cityId
    * @return
    */
   List<Engineer> getEngineerIdsByCityId(@Param("cityId") Long cityId);
}

