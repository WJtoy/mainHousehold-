package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.kkl.kklplus.entity.md.MDRegionPermissionEnum;
import com.kkl.kklplus.entity.md.dto.MDRegionAttributesDto;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionDto;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionSummaryDto;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.viewModel.AreaModel;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.entity.AreaProductcategoryModel;
import com.wolfking.jeesite.ms.providermd.entity.CategoryOpenAuthority;
import com.wolfking.jeesite.ms.providermd.entity.RegionPermissionView;
import com.wolfking.jeesite.ms.providermd.feign.MSRegionPermissionFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 客户品牌服务
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class MSRegionPermissionNewService {

    @Autowired
    private MSRegionPermissionFeign regionPermissionFeign;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ProductCategoryService productCategoryService;

    /**
     * 根据省市区街道判断是否是突击区域
     *
     * @param mdRegionPermission
     * @return
     */
    public MDRegionAttributesDto getKeFuTypeFromCacheForSD(MDRegionPermission mdRegionPermission) {
        MDRegionAttributesDto regionAttributesDto = null;
        MSResponse<MDRegionAttributesDto> msResponse = regionPermissionFeign.getAreaTypeFromCacheForSD(mdRegionPermission);
        if (MSResponse.isSuccess(msResponse)) {
            regionAttributesDto = msResponse.getData();
        } else {
            log.error("调用微服务获取客服类型失败.失败原因:{},品类categoryId:{},街道subAreaId:{}", msResponse.getMsg(), mdRegionPermission.getProductCategoryId(), mdRegionPermission.getSubAreaId());
        }
        return regionAttributesDto;
    }

    /**
     * @param resultMap         DB结果集
     * @param productCategories 产品品类
     * @return
     */
    public List<RegionPermissionView> foreachData(Map<Long, Map<Long, List<MDRegionPermission>>> resultMap, List<ProductCategory> productCategories) {
        List<RegionPermissionView> regionPermissionViewList = Lists.newArrayList();

        for (Long cityId : resultMap.keySet()) {
            Area entity = areaService.getFromCache(cityId);
            int subCount = 0;
            RegionPermissionView regionPermissionView = new RegionPermissionView();
            regionPermissionView.setAreaName(entity.getName());
            regionPermissionView.setAreaId(entity.getId());
            List<Area> areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, entity.getId());// 获取市下的所有区县

            if (!ObjectUtils.isEmpty(areaLists)) {// 如果区县不为空

                for (Area areaCounty : areaLists) {// 遍历区县
                    List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaCounty.getId());// 获取区县下的街道集合
                    if (subAreaList != null) {
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

    public Map<String, Object> findRegionPermissionListNew(MDRegionPermission regionPermission) {
        Integer type = 0;
        List<Area> cityList = Lists.newArrayList();
        List<RegionPermissionView> regionPermissionViewList = Lists.newArrayList();
        Map<String, Object> newMap = Maps.newHashMap();
        String typeName = regionPermission.getGroupType() == 2?"远程街道":"突击街道";

        // 所以产品品类
        List<ProductCategory> productCategories = productCategoryService.findAllList();
        List<Area> areaLists = Lists.newArrayList();
        List<Area> subAreaList = Lists.newArrayList();
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
                        regionPermission.setCityId(0L);
                    } else {
                        cityList = areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getId());// 市
                        if (!ObjectUtils.isEmpty(cityList)) {
                            for (Area city : cityList) {
                                int subCount = 0;
                                RegionPermissionView regionPermissionView = new RegionPermissionView();
                                regionPermissionView.setAreaName(city.getName());
                                regionPermissionView.setAreaId(city.getId());
                                areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, city.getId());// 获取市下的所有区县
                                for (Area areaCounty : areaLists) {// 遍历区县
                                    subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaCounty.getId());// 获取区县下的街道集合
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
                        newMap.put(area.getName(), regionPermissionViewList);
                        regionPermission.setCityId(0L);
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
                                areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,city.getId());// 获取市下的所有区县

                                if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                                    for (Area areaCounty : areaLists){// 遍历区县
                                        subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
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
                    areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,area.getId());// 获取市下的所有区县

                    if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                        for (Area areaCounty : areaLists){// 遍历区县
                            subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
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
                    newMap.put(area.getParent().getName(),regionPermissionViewList);
                }
            }
        }

        return newMap;
    }


    public Map<String, Object> findRegionPermissionAreaListNew(MDRegionPermission regionPermission) {
        Integer type = 0;
        List<Area> cityList = Lists.newArrayList();
        List<RegionPermissionView> regionPermissionViewList = Lists.newArrayList();
        Map<String, Object> newMap = Maps.newHashMap();
        String typeName = regionPermission.getGroupType() == 3?"远程街道":"突击街道";

        // 所以产品品类
        List<ProductCategory> productCategories = productCategoryService.findAllList();
        List<Area> areaLists = Lists.newArrayList();
        List<Area> subAreaList = Lists.newArrayList();
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
                        regionPermission.setCityId(0L);
                    } else {
                        cityList = areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getId());// 市
                        if (!ObjectUtils.isEmpty(cityList)) {
                            for (Area city : cityList) {
                                int subCount = 0;
                                RegionPermissionView regionPermissionView = new RegionPermissionView();
                                regionPermissionView.setAreaName(city.getName());
                                regionPermissionView.setAreaId(city.getId());
                                areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, city.getId());// 获取市下的所有区县
                                for (Area areaCounty : areaLists) {// 遍历区县
                                    subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaCounty.getId());// 获取区县下的街道集合
                                    if(subAreaList != null){
                                        subCount += subAreaList.size();// 统计街道数量
                                    }
                                }
                                regionPermissionView.setCount(subCount);

                                Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(city.getId());
                                List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessingNew(productCategories, longListMap);

                                regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                                regionPermissionViewList.add(regionPermissionView);
                            }
                        }
                        newMap.put(area.getName(), regionPermissionViewList);
                        regionPermission.setCityId(0L);
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
                                areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,city.getId());// 获取市下的所有区县

                                if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                                    for (Area areaCounty : areaLists){// 遍历区县
                                        subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
                                        if(subAreaList != null){
                                            subCount += subAreaList.size();// 统计街道数量
                                        }
                                    }
                                    Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(city.getId());
                                    List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessingNew(productCategories, longListMap);
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
                    areaLists = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,area.getId());// 获取市下的所有区县

                    if(!ObjectUtils.isEmpty(areaLists)){// 如果区县不为空

                        for (Area areaCounty : areaLists){// 遍历区县
                            subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaCounty.getId());// 获取区县下的街道集合
                            if(subAreaList != null){
                                subCount += subAreaList.size();// 统计街道数量
                            }
                        }
                        regionPermissionView.setCount(subCount);
                        Map<Long, List<MDRegionPermission>> longListMap = resultMap.get(area.getId());
                        List<CategoryOpenAuthority> categoryOpenAuthorities = productCategoryProcessingNew(productCategories, longListMap);
                        regionPermissionView.setProductCategoryList(categoryOpenAuthorities);
                        regionPermissionViewList.add(regionPermissionView);
                    }
                    newMap.put(area.getParent().getName(),regionPermissionViewList);
                }
            }
        }

        return newMap;
    }

    /**
     * 1.通过省/市获取区域开通数量
     * 2.根据市、品类分组
     */
    public Map<Long, Map<Long, List<MDRegionPermission>>> dataGroupingProcessing(MDRegionPermission regionPermission) {
        Map<Long, Map<Long, List<MDRegionPermission>>> resultMap = Maps.newHashMap();
        MSResponse<List<MDRegionPermission>> response = regionPermissionFeign.findListWithCategoryNew(regionPermission);
        if (MSResponse.isSuccess(response)) {
            List<MDRegionPermission> regionPermissions = response.getData();
            // 进行区县/品类分组
            resultMap = regionPermissions.stream()
                    .collect(
                            Collectors.groupingBy(MDRegionPermission::getCityId,
                                    Collectors.groupingBy(MDRegionPermission::getProductCategoryId)));
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
    public List<CategoryOpenAuthority> productCategoryProcessing(List<ProductCategory> productCategories, Map<Long, List<MDRegionPermission>> longListMap) {
        List<CategoryOpenAuthority> categoryOpenAuthorities = Lists.newArrayList();
        CategoryOpenAuthority categoryOpenAuthority;
        int rushNum;
        int autoNum;
        int keFuNum;
        for (ProductCategory productCategory : productCategories) {
            if (longListMap != null) {
                List<MDRegionPermission> regionPermissions = longListMap.get(productCategory.getId());
                categoryOpenAuthority = new CategoryOpenAuthority();
                categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                categoryOpenAuthority.setProductCategoryId(productCategory.getId());
                if (regionPermissions != null && regionPermissions.size() > 0) {
                    rushNum = 0;
                    autoNum = 0;
                    keFuNum = 0;
                    if(regionPermissions.get(0).getGroupType() == 1){
                        for (MDRegionPermission entity : regionPermissions) {
                            if (entity.getGroupType() == 1) {
                                if(entity.getSubAreaId() != 0){
                                    if (entity.getType() == 1) {
                                        rushNum++;
                                    } else if (entity.getType() == 2) {
                                        keFuNum++;
                                    } else if (entity.getType() == 3) {
                                        autoNum++;
                                    }
                                }

                            }
                        }
                    }
                    int openSize = regionPermissions.size();
                    categoryOpenAuthority.setOpeningNum(openSize);
                    categoryOpenAuthority.setRushNum(rushNum);
                    categoryOpenAuthority.setKeFuNum(keFuNum);
                    categoryOpenAuthority.setAutoNum(autoNum);
                } else {
                    categoryOpenAuthority.setOpeningNum(0);
                    categoryOpenAuthority.setRushNum(0);
                    categoryOpenAuthority.setKeFuNum(0);
                    categoryOpenAuthority.setAutoNum(0);
                }
                categoryOpenAuthorities.add(categoryOpenAuthority);
            } else {
                categoryOpenAuthority = new CategoryOpenAuthority();
                categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                categoryOpenAuthority.setProductCategoryId(productCategory.getId());
                categoryOpenAuthority.setOpeningNum(0);
                categoryOpenAuthority.setRushNum(0);
                categoryOpenAuthority.setKeFuNum(0);
                categoryOpenAuthority.setAutoNum(0);
                categoryOpenAuthorities.add(categoryOpenAuthority);
            }
        }
        return categoryOpenAuthorities;
    }


    public List<CategoryOpenAuthority> productCategoryProcessingNew(List<ProductCategory> productCategories, Map<Long, List<MDRegionPermission>> longListMap) {
        List<CategoryOpenAuthority> categoryOpenAuthorities = Lists.newArrayList();
        CategoryOpenAuthority categoryOpenAuthority;
        int rushNum;
        int autoNum;
        int keFuNum;
        for (ProductCategory productCategory : productCategories) {
            if (longListMap != null) {
                List<MDRegionPermission> regionPermissions = longListMap.get(productCategory.getId());
                categoryOpenAuthority = new CategoryOpenAuthority();
                categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                categoryOpenAuthority.setProductCategoryId(productCategory.getId());
                if (regionPermissions != null && regionPermissions.size() > 0) {
                    rushNum = 0;
                    autoNum = 0;
                    keFuNum = 0;
                    if(regionPermissions.get(0).getGroupType() == 3){
                        for (MDRegionPermission entity : regionPermissions) {
                            if (entity.getGroupType() == 3) {
                                if(entity.getSubAreaId() != 0){
                                    if (entity.getType() == 1) {
                                        rushNum++;
                                    } else if (entity.getType() == 2) {
                                        keFuNum++;
                                    } else if (entity.getType() == 3) {
                                        autoNum++;
                                    }
                                }

                            }
                        }
                    }
                    int openSize = regionPermissions.size();
                    categoryOpenAuthority.setOpeningNum(openSize);
                    categoryOpenAuthority.setRushNum(rushNum);
                    categoryOpenAuthority.setKeFuNum(keFuNum);
                    categoryOpenAuthority.setAutoNum(autoNum);
                } else {
                    categoryOpenAuthority.setOpeningNum(0);
                    categoryOpenAuthority.setRushNum(0);
                    categoryOpenAuthority.setKeFuNum(0);
                    categoryOpenAuthority.setAutoNum(0);
                }
                categoryOpenAuthorities.add(categoryOpenAuthority);
            } else {
                categoryOpenAuthority = new CategoryOpenAuthority();
                categoryOpenAuthority.setProductCategoryName(productCategory.getName());
                categoryOpenAuthority.setProductCategoryId(productCategory.getId());
                categoryOpenAuthority.setOpeningNum(0);
                categoryOpenAuthority.setRushNum(0);
                categoryOpenAuthority.setKeFuNum(0);
                categoryOpenAuthority.setAutoNum(0);
                categoryOpenAuthorities.add(categoryOpenAuthority);
            }
        }
        return categoryOpenAuthorities;
    }


    /**
     * 根据城市和产品品类获取启用区域
     *
     * @param regionPermission
     * @return
     */
    public List<MDRegionPermission> findListByCategoryAndCityId(MDRegionPermission regionPermission) {

        MSResponse<List<MDRegionPermission>> response = regionPermissionFeign.findListByCategoryAndCityIdNew(regionPermission);
        List<MDRegionPermission> data;
        if (MSResponse.isSuccessCode(response)) {
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
        if (data.isEmpty()) {
            return Lists.newArrayList();
        }
        return data;

    }

    /**
     * 加载省市区街道
     *
     * @param cityId
     * @return
     */
    public Map<String, Object> selectStreet(Integer groupType,Long cityId) {
        List<AreaModel> areaModelList = Lists.newArrayList();
        Area area = areaService.getFromCache(cityId);// 省市

        Map<String, Object> areaMap = Maps.newHashMap();
        areaMap.put("area", area);

        List<Area> areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, cityId);// 区县
        List<Area> subAreaList = Lists.newArrayList();
        Area subArea;
        if (!ObjectUtils.isEmpty(areaList)) {
            //获取区域区/县，街道
            AreaModel areaModel;
            for (Area areaCounty : areaList) {
                areaModel = new AreaModel();
                BeanUtils.copyProperties(areaCounty, areaModel);
                subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaCounty.getId());// 获取区县下级街道
                if(groupType == 2){
                    if (subAreaList != null) {
                        areaModel.setSubAreas(subAreaList);
                    } else {
                        areaModel.setSubAreas(Lists.newArrayList());
                    }
                }else {
                    if (subAreaList != null && subAreaList.size() > 0) {
                        areaModel.setSubAreas(subAreaList);
                    } else {
                        subArea = new Area();
                        subArea.setId(0L);
                        subArea.setName("-");
                        subAreaList = Lists.newArrayList();
                        subAreaList.add(subArea);
                        areaModel.setSubAreas(subAreaList);
                    }
                }

                areaModelList.add(areaModel);
            }
        }
        areaMap.put("areaModelList", areaModelList);
        return areaMap;
    }

    /**
     * 加载省市区街道
     *
     * @param cityId
     * @return
     */
    public Map<String, Object> selectStreetNew(Integer groupType,Long cityId) {
        List<AreaModel> areaModelList = Lists.newArrayList();
        Area area = areaService.getFromCache(cityId);// 省市

        Map<String, Object> areaMap = Maps.newHashMap();
        areaMap.put("area", area);

        List<Area> areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, cityId);// 区县
        List<Area> subAreaList = Lists.newArrayList();
        Area subArea;
        if (!ObjectUtils.isEmpty(areaList)) {
            //获取区域区/县，街道
            AreaModel areaModel;
            for (Area areaCounty : areaList) {
                areaModel = new AreaModel();
                BeanUtils.copyProperties(areaCounty, areaModel);
                subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaCounty.getId());// 获取区县下级街道
                if(groupType == 3){
                    if (subAreaList != null) {
                        areaModel.setSubAreas(subAreaList);
                    } else {
                        areaModel.setSubAreas(Lists.newArrayList());
                    }
                }else {
                    if (subAreaList != null && subAreaList.size() > 0) {
                        areaModel.setSubAreas(subAreaList);
                    } else {
                        subArea = new Area();
                        subArea.setId(0L);
                        subArea.setName("-");
                        subAreaList = Lists.newArrayList();
                        subAreaList.add(subArea);
                        areaModel.setSubAreas(subAreaList);
                    }
                }

                areaModelList.add(areaModel);
            }
        }
        areaMap.put("areaModelList", areaModelList);
        return areaMap;
    }


    public void save(List<MDRegionPermission> list, User user) {
        if (!list.isEmpty()) {
            // 批量保存
            list.forEach(r -> {
                r.setUpdateById(user.getId());
                r.preUpdate();
            });
            MSResponse<Integer> response = regionPermissionFeign.batchSaveNew(list);
            if (!MSResponse.isSuccess(response)) {
                throw new RuntimeException("调用微服务保存区域设置设定错误.错误原因:" + response.getMsg());
            }
        }
    }

    public void saveNew(List<MDRegionPermission> list, User user) {
        if (!list.isEmpty()) {
            // 批量保存
            list.forEach(r -> {
                r.setUpdateById(user.getId());
                r.preUpdate();
            });
            MSResponse<Integer> response = regionPermissionFeign.batchSaveNew(list);
            if (!MSResponse.isSuccess(response)) {
                throw new RuntimeException("调用微服务保存区域设置设定错误.错误原因:" + response.getMsg());
            }
        }
    }

    public void batchSave(Long cityId, Long areaId, Long productCategoryId, Integer groupType, Integer type, User user) {
        List<MDRegionPermission> list = Lists.newArrayList();
        List<AreaModel> areaModelList = Lists.newArrayList();
        List<Area> areaList;
        AreaModel areaModel;
        Area area = areaService.get(cityId);
        if (areaId == null) {
            areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, cityId);// 区县
            if (!ObjectUtils.isEmpty(areaList)) {
                //获取区域区/县，街道
                for (Area areaCounty : areaList) {
                    areaModel = new AreaModel();
                    BeanUtils.copyProperties(areaCounty, areaModel);
                    List<Area> subAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaCounty.getId());// 获取区县下级街道
                    if (subAreaList != null) {
                        areaModel.setSubAreas(subAreaList);
                    } else {
                        areaModel.setSubAreas(Lists.newArrayList());
                    }
                    areaModelList.add(areaModel);
                }
            }
        } else {
            areaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, areaId);// 区县
            areaModel = new AreaModel();
            areaModel.setId(areaId);
            areaModel.setSubAreas(areaList);
            areaModelList.add(areaModel);
        }

        for (AreaModel model : areaModelList) {
            for (Area entity : model.getSubAreas()) {
                MDRegionPermission regionPermission = new MDRegionPermission();
                regionPermission.setCityId(cityId);
                regionPermission.setType(type);
                regionPermission.setGroupType(groupType);
                regionPermission.setProvinceId(area.getParentId());
                regionPermission.setAreaId(model.getId());
                regionPermission.setSubAreaId(entity.getId());
                regionPermission.setProductCategoryId(productCategoryId);
                regionPermission.setStatus(1);
                regionPermission.setUpdateById(user.getId());
                regionPermission.preUpdate();
                list.add(regionPermission);
            }
        }

        MSResponse<Integer> response = regionPermissionFeign.batchSaveNew(list);
        if (!MSResponse.isSuccess(response)) {
            throw new RuntimeException("调用微服务批量保存区域设置设定错误.错误原因:" + response.getMsg());
        }

    }

    /**
     * 根据市区街道街道是否有远程费
     *
     * @param productCategory
     * @param areaId
     * @param subAreaId
     * @return
     */
    public Integer getRemoteFeeStatusFromCacheForSD(long productCategory, long areaId, long subAreaId) {
        Integer remoteFeeStatus = 1;
        if (subAreaId <= 3) {
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
        if (MSResponse.isSuccessCode(msResponse)) {
            remoteFeeStatus = msResponse.getData();
        } else {
            log.error("调用微服务获取突击区域标识失败.失败原因:{},品类categoryId:{},街道subAreaId:{}", msResponse.getMsg(), regionPermission.getProductCategoryId(), regionPermission.getSubAreaId());
        }
        return remoteFeeStatus;
    }

    /**
     * 根据区,街道判断是否为远程区域
     *
     * @param productCategory
     * @param areaId
     * @param subAreaId
     * @return
     *      1.code =0
     *       data=1:远程区域;data=0:非远程区域
     *      2.code >0,错误
     */
    public MSResponse<Integer> getRemoteAreaStatusFromCacheForSD(long productCategory, long areaId, long subAreaId) {
        Integer remoteAreaStatus = 0;
        if (subAreaId <= 3) {
            return new MSResponse<>(remoteAreaStatus);
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
        MSResponse<Integer> msResponse = regionPermissionFeign.getRemoteAreaStatusFromCacheForSD(regionPermission);
        if (!MSResponse.isSuccessCode(msResponse)) {
            log.error("调用微服务获取远程区域失败.失败原因:{},品类categoryId:{},街道subAreaId:{}", msResponse.getMsg(), regionPermission.getProductCategoryId(), regionPermission.getSubAreaId());
        }
        return msResponse;
    }

    /**
     * 根据用户类型与客服类型获取市区Dto列表
     *
     * @param groupType,type
     * @return
     */
    public List<MDRegionPermissionDto> findDtoListByGroupTypeAndType(Integer groupType,Integer type) {

        MSResponse<List<MDRegionPermissionDto>> response = regionPermissionFeign.findDtoListByGroupTypeAndType(groupType,type);
        List<MDRegionPermissionDto> data;
        if (MSResponse.isSuccessCode(response)) {
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
        if (data.isEmpty()) {
            return Lists.newArrayList();
        }
        return data;

    }


    /**
     * 根据用户类型与客服类型获取市区列表
     *
     * @param groupType,type
     * @return
     */
    public List<MDRegionPermission> findAreaListByGroupTypeAndType(Integer groupType,Integer type) {

        MSResponse<List<MDRegionPermission>> response = regionPermissionFeign.findAreaListByGroupTypeAndType(groupType,type);
        List<MDRegionPermission> data;
        if (MSResponse.isSuccessCode(response)) {
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
        if (data.isEmpty()) {
            return Lists.newArrayList();
        }
        return data;

    }

    /**
     * 根据用户类型与客服类型获取市区列表
     *
     * @param groupType,type
     * @return
     */
    public List<MDRegionPermissionDto> findRegionPermissionDtoList(Integer groupType,Integer type) {

        MSResponse<List<MDRegionPermissionDto>> response = regionPermissionFeign.findRegionPermissionDtoList(groupType,type);
        List<MDRegionPermissionDto> data;
        if (MSResponse.isSuccessCode(response)) {
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取区域权限失败.失败原因" + response.getMsg());
        }
        if (data.isEmpty()) {
            return Lists.newArrayList();
        }
        return data;

    }

    public List<AreaProductcategoryModel> findAreaList() {
         List<AreaProductcategoryModel> list = Lists.newArrayList();

        MSResponse<List<MDRegionPermissionSummaryDto>> response = regionPermissionFeign.getAreaCountByProductCategoryForRPT();


         List<MDRegionPermissionSummaryDto> data;
        if (MSResponse.isSuccessCode(response)) {
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取品类区域失败.失败原因" + response.getMsg());
        }

        AreaProductcategoryModel entity;
        Map<Long, List<MDRegionPermissionSummaryDto>> groupBy = data.stream().collect(Collectors.groupingBy(MDRegionPermissionSummaryDto::getProductCategoryId));
        for(List<MDRegionPermissionSummaryDto> mdRegionPermissionSummaryDtoList : groupBy.values()){
            entity = new AreaProductcategoryModel();
            for(MDRegionPermissionSummaryDto regionEntity : mdRegionPermissionSummaryDtoList){
                entity.setProductcategoryName(regionEntity.getProductCategoryName());
                if(regionEntity.getType() == 1){
                    entity.setCrushStreetSum(regionEntity.getCount());
                    entity.setCrushAreaSum(regionEntity.getDistrictCount());
                }
                if(regionEntity.getType() == 3){
                    entity.setAutomaticAreaSum(regionEntity.getDistrictCount());
                }
            }
            list.add(entity);
        }


        return list;
    }

    public AreaProductcategoryModel findCountyList() {
        NameValuePair<Integer,Integer> nameValuePair = areaService.findAllAreaCountForRPT();
        AreaProductcategoryModel entity = new AreaProductcategoryModel();
        if(nameValuePair !=null){
            entity.setAreaSum(Integer.valueOf(nameValuePair.getName().toString()));
            entity.setStreetSum(Integer.valueOf(nameValuePair.getValue().toString()));
        }

        return entity;
    }


    public MDCustomerVipLevel findCustomerLevel() {

        MSResponse<MDCustomerVipLevel> response =  regionPermissionFeign.getMinStartVipLevel();
        MDCustomerVipLevel data;
        if (MSResponse.isSuccessCode(response)) {
            data = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取最小启用等级失败.失败原因" + response.getMsg());
        }

        if(data == null){
            List<MDCustomerVipLevel>  mdCustomerVipLevels= findCustomerLevelList();
            mdCustomerVipLevels = mdCustomerVipLevels.stream().sorted(Comparator.comparing(MDCustomerVipLevel::getValue)).collect(Collectors.toList());
            if(mdCustomerVipLevels != null && !mdCustomerVipLevels.isEmpty()){
                data =  mdCustomerVipLevels.get(0);
            }
        }else{
            if(data.getValue() == 0){
                data.setName("无");
            }
        }

        return data;
    }


    public List<MDCustomerVipLevel> findCustomerLevelList() {
        List<MDCustomerVipLevel> list;
        MSResponse<List<MDCustomerVipLevel>> response =  regionPermissionFeign.findAllIdAndNameList();
        if (MSResponse.isSuccessCode(response)) {
            list = response.getData();
        } else {
            throw new RuntimeException("调用微服务获取所有客服vip等级列表失败.失败原因" + response.getMsg());
        }
        for(MDCustomerVipLevel mdCustomerVipLevel : list){
            if(mdCustomerVipLevel.getValue()==0){
                mdCustomerVipLevel.setName("无");
            }
        }
        if(list != null && !list.isEmpty()){
            list = list.stream().sorted(Comparator.comparing(MDCustomerVipLevel::getValue)).collect(Collectors.toList());
        }

        return list;
    }


    public void saveCustomerVip(Long id, User user) {
        MDCustomerVipLevel mdCustomerVipLevel = new MDCustomerVipLevel();
        mdCustomerVipLevel.setUpdateById(user.getId());
        mdCustomerVipLevel.preUpdate();
        mdCustomerVipLevel.setId(id);
        MSResponse<Integer> response = regionPermissionFeign.updateStatusFlag(mdCustomerVipLevel);
        if (!MSResponse.isSuccess(response)) {
            throw new RuntimeException("调用微服务保存区域设置设定错误.错误原因:" + response.getMsg());
        }

    }
}
