package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.base.Splitter;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.feign.MSRegionPermissionFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.viewModel.AreaModel;
import com.wolfking.jeesite.ms.providermd.entity.CategoryOpenAuthority;
import com.wolfking.jeesite.ms.providermd.entity.RegionPermissionView;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 客户品牌服务
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class MSRegionPermissionService {

    @Autowired
    private MSRegionPermissionFeign regionPermissionFeign;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ProductCategoryService productCategoryService;

    /**
     * 根据省市区街道判断是否是突击区域
     * @param mdRegionPermission
     * @return
     */
    public int getSubAreaStatusFromCacheForSD(MDRegionPermission mdRegionPermission){
        int canRush=0;
        MSResponse<Integer> msResponse = regionPermissionFeign.getSubAreaStatusFromCacheForSD(mdRegionPermission);
        if(MSResponse.isSuccessCode(msResponse)){
            if(msResponse.getData()==1){
                canRush = 1; //是突击区域
            }
        }else{
            log.error("调用微服务获取突击区域标识失败.失败原因:{},品类categoryId:{},街道subAreaId:{}",msResponse.getMsg(),mdRegionPermission.getProductCategoryId(),mdRegionPermission.getSubAreaId());
        }
        return canRush;
    }

    public Map<String, Object> findRegionPermissionList(MDRegionPermission regionPermission){
        Map<String, Object> map = Maps.newHashMap();
        Area area = areaService.getFromCache(regionPermission.getCityId());// 市id获取area
        regionPermission.setAreaId(area.getParent().getId());
        regionPermission.setCityId(area.getId());
        map.put("regionPermission", regionPermission);
        map.put("fullName", area.getParent().getName() + area.getName());
        List<Area> areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,regionPermission.getCityId());// 区县

        // 根据城市id去查区域设置
        Map<Long, Map<Long, List<MDRegionPermission>>> resultMap = Maps.newHashMap();
        MSResponse<List<MDRegionPermission>> response = regionPermissionFeign.findListWithCategory(regionPermission);
        if (MSResponse.isSuccess(response)) {
            List<MDRegionPermission> regionPermissions = response.getData();
            // 进行区县/品类分组
            resultMap = regionPermissions.stream()
                    .collect(
                            Collectors.groupingBy(MDRegionPermission :: getAreaId,
                                    Collectors.groupingBy(MDRegionPermission :: getProductCategoryId)));
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
        List<RegionPermissionView> regionPermissionViewList = Lists.newArrayList();
        if(!ObjectUtils.isEmpty(areaList)){
            //获取区/县，街道
            AreaModel areaModel;
            for (Area areaCounty : areaList){
                RegionPermissionView regionPermissionView = new RegionPermissionView();
                areaModel = new AreaModel();
                BeanUtils.copyProperties(areaCounty,areaModel);
                List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());
                regionPermissionView.setAreaName(areaCounty.getName());
                regionPermissionView.setAreaId(areaCounty.getId());
                if(subAreaList != null){
                    regionPermissionView.setCount(subAreaList.size());
                    areaModel.setSubAreas(subAreaList);
                }else{
                    areaModel.setSubAreas(Lists.newArrayList());
                }
                Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(areaCounty.getId());
                List<ProductCategory> productCategories = productCategoryService.findAllList().stream().sorted(Comparator.comparing(ProductCategory::getId)).collect(Collectors.toList());
                List<CategoryOpenAuthority> categoryOpenAuthorities = Lists.newArrayList();
                CategoryOpenAuthority categoryOpenAuthority;
                for (ProductCategory productCategory:productCategories){
                    if (longListMap != null) {
                        List<MDRegionPermission> regionPermissions = longListMap.get(productCategory.getId());
                        categoryOpenAuthority = new CategoryOpenAuthority();
                        categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                        if(regionPermissions!=null && regionPermissions.size()>0){
                            int closeSize = regionPermissions.stream().filter(t -> t.getStatus() == 0).collect(Collectors.toList()).size();
                            int openSize = regionPermissions.stream().filter(t -> t.getStatus() == 1).collect(Collectors.toList()).size();
                            categoryOpenAuthority.setNoOpeningNum(closeSize);
                            categoryOpenAuthority.setOpeningNum(openSize);
                        }else{
                            categoryOpenAuthority.setNoOpeningNum(subAreaList.size());
                            categoryOpenAuthority.setOpeningNum(0);
                        }
                        categoryOpenAuthorities.add(categoryOpenAuthority);
                    } else {
                        categoryOpenAuthority = new CategoryOpenAuthority();
                        categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                        categoryOpenAuthority.setNoOpeningNum(subAreaList.size());
                        categoryOpenAuthority.setOpeningNum(0);
                        categoryOpenAuthorities.add(categoryOpenAuthority);
                    }

                }
                regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                regionPermissionViewList.add(regionPermissionView);
            }
        }
        map.put("regionPermissionViewList", regionPermissionViewList);

        return map;
    }

    /**
     * @param resultMap             DB结果集
     * @param productCategories     产品品类
     * @return
     */
    public List<RegionPermissionView> foreachData(Map<Long, Map<Long, List<MDRegionPermission>>> resultMap, List<ProductCategory> productCategories){
        List<RegionPermissionView> regionPermissionViewList = Lists.newArrayList();

        for (Long cityId : resultMap.keySet()) {
            Area entity = areaService.getFromCache(cityId);
            int subCount = 0;
            RegionPermissionView regionPermissionView = new RegionPermissionView();
            regionPermissionView.setAreaName(entity.getName());
            regionPermissionView.setAreaId(entity.getId());
            List<Area> areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,entity.getId());// 获取市下的所有区县

            if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                for (Area areaCounty : areaLists){// 遍历区县
                    List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
                    if(subAreaList != null){
                        subCount += subAreaList.size();// 统计街道数量
                    }
                }
                Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(entity.getId());
                List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessing(productCategories, longListMap);
                regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                regionPermissionView.setCount(subCount);
                regionPermissionViewList.add(regionPermissionView);
            }
        }
        return regionPermissionViewList;
    }

    public Map<String, Object> findRegionPermissionListNew(MDRegionPermission regionPermission){
        Integer type = 0;
        List<Area> cityList = Lists.newArrayList();

        List<RegionPermissionView> regionPermissionViewList = Lists.newArrayList();
        Map<String, Object> newMap = Maps.newHashMap();
        String typeName = regionPermission.getType() == 0?"远程街道":"突击街道";
        // 所以产品品类
        List<ProductCategory> productCategories = productCategoryService.findAllList();
        // 全国
        if (regionPermission.getCityId() == 0) {
            List<Area> areas = areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
            newMap = Maps.newHashMap();
            if (!areas.isEmpty()) {
                for (Area area : areas) {
                    regionPermissionViewList = Lists.newArrayList();

                    regionPermission.setProvinceId(area.getId());
                    regionPermission.setCityId(null);

                    Map<Long, Map<Long, List<MDRegionPermission>>> resultMap = dataGroupingProcessing(regionPermission);
                    if (regionPermission.getStatus() != null && regionPermission.getStatus() == 1) {
                        if (resultMap.size() > 0) {
                            List<RegionPermissionView> list = foreachData(resultMap, productCategories);
                            newMap.put(area.getName(),list);
                        }
                        regionPermission.setCityId(0l);
                    } else {
                        cityList = areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getId());// 市
                        if(!ObjectUtils.isEmpty(cityList)){
                            for (Area city : cityList) {
                                int subCount = 0;
                                RegionPermissionView regionPermissionView = new RegionPermissionView();
                                regionPermissionView.setAreaName(city.getName());
                                regionPermissionView.setAreaId(city.getId());
                                List<Area> areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,city.getId());// 获取市下的所有区县
                                for (Area areaCounty : areaLists){// 遍历区县
                                    List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
                                    if(subAreaList != null){
                                        subCount += subAreaList.size();// 统计街道数量
                                    }
                                }
                                regionPermissionView.setCount(subCount);

                                Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(city.getId());
                                List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessing(productCategories, longListMap);

                                regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                                regionPermissionViewList.add(regionPermissionView);
                            }
                        }
                        newMap.put(area.getName(),regionPermissionViewList);
                        regionPermission.setCityId(0l);
                    }
                }
            }
        } else {
            Area area = areaService.getFromCache(regionPermission.getCityId());// 市id获取area
            if (area != null) {
                type = area.getType();
                if (type == 2) {// 省
                    regionPermission.setProvinceId(area.getId());
                    regionPermission.setCityId(null);
                    regionPermissionViewList = Lists.newArrayList();

                    Map<Long, Map<Long, List<MDRegionPermission>>> resultMap = dataGroupingProcessing(regionPermission);
                    if (regionPermission.getStatus() != null && regionPermission.getStatus() == 1) {
                        if (resultMap.size() > 0) {
                            List<RegionPermissionView> list = foreachData(resultMap, productCategories);
                            newMap.put(area.getName(),list);
                        } else {
                            newMap.put("empty", "该省所有市区"+typeName+"均未开通");
                        }
                        regionPermission.setCityId(area.getId());
                    } else {
                        cityList = areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getId());// 市list
                        if(!ObjectUtils.isEmpty(cityList)){
                            for (Area city : cityList) {
                                int subCount = 0;
                                RegionPermissionView regionPermissionView = new RegionPermissionView();
                                regionPermissionView.setAreaName(city.getName());
                                regionPermissionView.setAreaId(city.getId());
                                List<Area> areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,city.getId());// 获取市下的所有区县

                                if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                                    for (Area areaCounty : areaLists){// 遍历区县
                                        List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
                                        if(subAreaList != null){
                                            subCount += subAreaList.size();// 统计街道数量
                                        }
                                    }
                                    Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(city.getId());
                                    List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessing(productCategories, longListMap);
                                    regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                                    regionPermissionView.setCount(subCount);
                                    regionPermissionViewList.add(regionPermissionView);
                                }
                            }
                        }
                        newMap.put(area.getName(),regionPermissionViewList);
                    }
                    regionPermission.setCityId(area.getId());
                } else if (type == 3) {
                    regionPermission.setCityId(area.getId());
                    regionPermissionViewList = Lists.newArrayList();
                    // 根据城市id去查区域设置
                    Map<Long, Map<Long, List<MDRegionPermission>>> resultMap = dataGroupingProcessing(regionPermission);

                    if (regionPermission.getStatus() != null && regionPermission.getStatus() == 1) {
                        if (resultMap.size() <= 0) {
                            newMap.put("empty", "该市所有区域"+typeName+"均未开通");
                            return newMap;
                        }
                    }
                    int subCount = 0;
                    RegionPermissionView regionPermissionView = new RegionPermissionView();
                    regionPermissionView.setAreaName(area.getName());
                    regionPermissionView.setAreaId(area.getId());
                    List<Area> areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,area.getId());// 获取市下的所有区县

                    if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                        for (Area areaCounty : areaLists){// 遍历区县
                            List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
                            if(subAreaList != null){
                                subCount += subAreaList.size();// 统计街道数量
                            }
                        }
                        regionPermissionView.setCount(subCount);
                        Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(area.getId());
                        List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessing(productCategories, longListMap);
                        regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                        regionPermissionViewList.add(regionPermissionView);
                    }
                    newMap.put(area.getName(),regionPermissionViewList);
                }
            }
        }

        return newMap;
    }

    /**
     * 1.通过省/市获取区域开通数量
     * 2.根据市、品类分组
     */
    public Map<Long, Map<Long, List<MDRegionPermission>>> dataGroupingProcessing(MDRegionPermission regionPermission){
        Map<Long, Map<Long, List<MDRegionPermission>>> resultMap = Maps.newHashMap();
        MSResponse<List<MDRegionPermission>> response = regionPermissionFeign.findListWithCategory(regionPermission);
        if (MSResponse.isSuccess(response)) {
            List<MDRegionPermission> regionPermissions = response.getData();
            // 进行区县/品类分组
            resultMap = regionPermissions.stream()
                    .collect(
                            Collectors.groupingBy(MDRegionPermission :: getCityId,
                                    Collectors.groupingBy(MDRegionPermission :: getProductCategoryId)));
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
        return resultMap;
    }

    /**
     * 处理产品品类下区域开通数
     * @param longListMap
     * @return
     */
    public List<CategoryOpenAuthority> productCategoryProcessing(List<ProductCategory> productCategories, Map<Long, List<MDRegionPermission>> longListMap){
        List<CategoryOpenAuthority> categoryOpenAuthorities = Lists.newArrayList();
        CategoryOpenAuthority categoryOpenAuthority;
        for (ProductCategory productCategory : productCategories){
            if (longListMap != null) {
                List<MDRegionPermission> regionPermissions = longListMap.get(productCategory.getId());
                categoryOpenAuthority = new CategoryOpenAuthority();
                categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                categoryOpenAuthority.setProductCategoryId(productCategory.getId());
                if(regionPermissions!=null && regionPermissions.size()>0){
                    int openSize = regionPermissions.size();
                    categoryOpenAuthority.setOpeningNum(openSize);
                }else{
                    categoryOpenAuthority.setOpeningNum(0);
                }
                categoryOpenAuthorities.add(categoryOpenAuthority);
            } else {
                categoryOpenAuthority = new CategoryOpenAuthority();
                categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                categoryOpenAuthority.setProductCategoryId(productCategory.getId());
                categoryOpenAuthority.setOpeningNum(0);
                categoryOpenAuthorities.add(categoryOpenAuthority);
            }
        }
        return categoryOpenAuthorities;
    }

    /**
     * 根据城市和产品品类获取启用区域
     * @param regionPermission
     * @return
     */
    public List<MDRegionPermission> findListByCategoryAndCityId(MDRegionPermission regionPermission){
        regionPermission.setStatus(1);
        MSResponse<List<MDRegionPermission>> response = regionPermissionFeign.findListByCategoryAndCityId(regionPermission);
        List<MDRegionPermission> data;
        if(MSResponse.isSuccessCode(response)){
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
//        List<Long> subAreaId =data.stream().map(i-> i.getSubAreaId()).collect(Collectors.toList());
        if (data.isEmpty()) {
            return Lists.newArrayList();
        }
        return data;

    }

    /**
     * 加载省市区街道
     * @param cityId
     * @return
     */
    public Map<String, Object> selectStreet(Long cityId){
        List<AreaModel> areaModelList = Lists.newArrayList();
        Area area = areaService.getFromCache(cityId);// 省市

        Map<String, Object> areaMap = Maps.newHashMap();
        areaMap.put("area", area);

        List<Area> areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,cityId);// 区县
        if(!ObjectUtils.isEmpty(areaList)){
            //获取区域区/县，街道
            AreaModel areaModel;
            for (Area areaCounty : areaList){
                areaModel = new AreaModel();
                BeanUtils.copyProperties(areaCounty,areaModel);
                List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下级街道
                if(subAreaList!=null){
                    areaModel.setSubAreas(subAreaList);
                }else{
                    areaModel.setSubAreas(Lists.newArrayList());
                }
                areaModelList.add(areaModel);
            }
        }
        areaMap.put("areaModelList", areaModelList);
        return areaMap;
    }

    @Transactional
    public void save(List<MDRegionPermission> list, User user) {
        if (!list.isEmpty()) {
            // 批量保存
            list.forEach(r -> {
                r.setUpdateById(user.getId());
                r.preUpdate();
            });
            MSResponse<Integer> response = regionPermissionFeign.batchSave(list);
            if (!MSResponse.isSuccess(response)) {
                throw new RuntimeException("调用微服务保存区域设置设定错误.错误原因:" + response.getMsg());
            }
        }
    }


    /**
     * 根据市区街道街道是否有远程费
     * @param productCategory
     * @param areaId
     * @param subAreaId
     * @return
     */
    public Integer getRemoteFeeStatusFromCacheForSD(long productCategory,long areaId,long subAreaId){
        Integer remoteFeeStatus=1;
        if(subAreaId<=3){
            return remoteFeeStatus;
        }
        MDRegionPermission regionPermission = new MDRegionPermission();
        regionPermission.setProductCategoryId(productCategory);
        regionPermission.setAreaId(areaId);
        regionPermission.setSubAreaId(subAreaId);
        Area area = areaService.getFromCache(areaId);
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                regionPermission.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
            }
        }
        MSResponse<Integer> msResponse = regionPermissionFeign.getRemoteFeeStatusFromCacheForSD(regionPermission);
        if(MSResponse.isSuccessCode(msResponse)){
            remoteFeeStatus = msResponse.getData();
        }else{
            log.error("调用微服务获取突击区域标识失败.失败原因:{},品类categoryId:{},街道subAreaId:{}",msResponse.getMsg(),regionPermission.getProductCategoryId(),regionPermission.getSubAreaId());
        }
        return remoteFeeStatus;
    }
}
