package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.sys.SysOfficeAttributes;
import com.kkl.kklplus.entity.sys.SysOfficeRegion;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sys.dao.OfficeRegionDao;
import com.wolfking.jeesite.modules.sys.dao.UserRegionDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeAttributesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OfficeRegionService extends BaseService {
    @Autowired
    private MSSysOfficeAttributesService msSysOfficeAttributesService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private AreaService areaService;

    @Resource
    private UserRegionDao userRegionDao;

    @Resource
    private OfficeRegionDao officeRegionDao;

    public List<ProductCategory> getProductCategoryList(Long officeId) {
        long attributesId = 10; //获取部门负责品类
        List<ProductCategory> productCategoryList = Lists.newArrayList();
        List<ProductCategory> allList = productCategoryService.findAllList();
        Map<Long, ProductCategory> collect = allList.stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
        SysOfficeAttributes sysOfficeAttributes = msSysOfficeAttributesService.getByOfficeIdAndAttributeId(officeId, attributesId);
        if (sysOfficeAttributes != null) {
            String json = sysOfficeAttributes.getAttributeValueJson();
            productCategoryList = Arrays.asList(GsonUtils.fromJsonNew(json, ProductCategory[].class));
            for (ProductCategory entity : productCategoryList) {
                if (collect.get(entity.getId()) != null) {
                    entity.setName(collect.get(entity.getId()).getName());
                }
            }
        }
        return productCategoryList;
    }

    public List<UserServiceRegion> getOfficeRegion(Long officeId) {
        long attributesId = 20; //获取部门负责区域
        List<SysOfficeRegion> sysOfficeRegionList = Lists.newArrayList();
        SysOfficeAttributes sysOfficeAttributes = msSysOfficeAttributesService.getByOfficeIdAndAttributeId(officeId, attributesId);

        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_PROVINCE);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_CITY);
        Map<Long, Area> areaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);
        List<Area> cityList = areaService.findListByType(Area.TYPE_VALUE_CITY);
        List<Area> areaList = areaService.findListByType(Area.TYPE_VALUE_COUNTY);
        Map<Long, List<Area>> cityGroupingByMap = cityList.stream().collect(Collectors.groupingBy(t -> t.getParent().getId()));
        Map<Long, List<Area>> areaGroupingByMap = areaList.stream().collect(Collectors.groupingBy(t -> t.getParent().getId()));

        List<UserServiceRegion> provinceRegionList = Lists.newArrayList();

        if (sysOfficeAttributes != null) {
            String json = sysOfficeAttributes.getAttributeValueJson();
            sysOfficeRegionList = Arrays.asList(GsonUtils.fromJsonNew(json, SysOfficeRegion[].class));
            provinceRegionList = getUserServiceRegion(sysOfficeRegionList, provinceMap, cityMap, areaMap, cityGroupingByMap, areaGroupingByMap);

            List<ProductCategory> productCategoryList = getProductCategoryList(officeId);
            List<Long> userIds;
            List<User> userList;
            List<UserRegion> regionList;
            List<SysOfficeRegion> userRegionList;
            List<UserServiceRegion> userServiceRegion;
            Map<Long, UserServiceRegion> userServiceRegionMap;
            Map<Long, UserServiceRegion> cityServiceRegionMap;
            OfficeProductCategory officeProductCategory;
            List<Long> areaIds;
            List<Long> beGrantedAreaIds;
            List<Long> unauthorizedAreaIds;
            for (ProductCategory productCategory : productCategoryList) {
                userRegionList = Lists.newArrayList();
                regionList = Lists.newArrayList();
                userList = officeRegionDao.getUserIdsByOfficeIdProductCategoryId(officeId, productCategory.getId());
                userIds = userList.stream().map(User::getId).collect(Collectors.toList());
                for (Long userId : userIds) {
                    regionList.addAll(userRegionDao.getUserRegions(userId));
                }
                for (UserRegion userRegion : regionList) {
                    SysOfficeRegion sysOfficeRegion = new SysOfficeRegion();
                    sysOfficeRegion.setAreaType(userRegion.getAreaType());
                    sysOfficeRegion.setProvinceId(userRegion.getProvinceId());
                    sysOfficeRegion.setCityId(userRegion.getCityId());
                    sysOfficeRegion.setAreaId(userRegion.getAreaId());
                    sysOfficeRegion.setOfficeId(userRegion.getUserId());
                    userRegionList.add(sysOfficeRegion);
                }
                userServiceRegion = getUserServiceRegion(userRegionList, provinceMap, cityMap, areaMap, cityGroupingByMap, areaGroupingByMap);
                userServiceRegionMap = userServiceRegion.stream().collect(Collectors.toMap(UserServiceRegion::getProvinceId, Function.identity()));
                for (UserServiceRegion entity : provinceRegionList) {
                    if (userServiceRegionMap.get(entity.getProvinceId()) != null) {
                        cityServiceRegionMap = userServiceRegionMap.get(entity.getProvinceId()).getRegionList().stream().collect(Collectors.toMap(UserServiceRegion::getCityId, Function.identity()));
                        for (UserServiceRegion item : entity.getRegionList()) {
                            officeProductCategory = new OfficeProductCategory();
                            beGrantedAreaIds = Lists.newArrayList();
                            unauthorizedAreaIds = Lists.newArrayList();
                            if (cityServiceRegionMap.get(item.getCityId()) != null) {
                                if (item.getRegionList().size() <= cityServiceRegionMap.get(item.getCityId()).getRegionList().size()) {
                                    officeProductCategory.setIsCoverage(1);
                                    officeProductCategory.setId(productCategory.getId());
                                } else {
                                    areaIds = cityServiceRegionMap.get(item.getCityId()).getRegionList().stream().map(UserServiceRegion::getAreaId).collect(Collectors.toList());
                                    for (UserServiceRegion region : item.getRegionList()) {
                                        if (areaIds.contains(region.getAreaId())) {
                                            beGrantedAreaIds.add(region.getAreaId());
                                        } else {
                                            unauthorizedAreaIds.add(region.getAreaId());
                                        }
                                    }
                                    officeProductCategory.setBeGrantedAreaIds(StringUtils.join(beGrantedAreaIds, ","));
                                    officeProductCategory.setUnauthorizedAreaIds(StringUtils.join(unauthorizedAreaIds, ","));
                                    officeProductCategory.setIsCoverage(0);
                                    officeProductCategory.setId(productCategory.getId());
                                }
                            } else {
                                for (UserServiceRegion region : item.getRegionList()) {
                                    unauthorizedAreaIds.add(region.getAreaId());
                                }
                                officeProductCategory.setUnauthorizedAreaIds(StringUtils.join(unauthorizedAreaIds, ","));
                                officeProductCategory.setIsCoverage(0);
                                officeProductCategory.setId(productCategory.getId());
                            }
                            item.getOfficeProductCategories().add(officeProductCategory);
                        }
                    } else {
                        for (UserServiceRegion item : entity.getRegionList()) {
                            unauthorizedAreaIds = Lists.newArrayList();
                            officeProductCategory = new OfficeProductCategory();
                            for (UserServiceRegion region : item.getRegionList()) {
                                unauthorizedAreaIds.add(region.getAreaId());
                            }
                            officeProductCategory.setUnauthorizedAreaIds(StringUtils.join(unauthorizedAreaIds, ","));
                            officeProductCategory.setIsCoverage(0);
                            officeProductCategory.setId(productCategory.getId());
                            item.getOfficeProductCategories().add(officeProductCategory);
                        }
                    }
                }
            }
        }


        return provinceRegionList;
    }


    private List<UserServiceRegion> getUserServiceRegion(List<SysOfficeRegion> list, Map<Long, Area> provinceMap, Map<Long, Area> cityMap, Map<Long, Area> areaMap, Map<Long, List<Area>> cityGroupingByMap, Map<Long, List<Area>> areaGroupingByMap) {
        UserServiceRegion provinceRegion;
        List<UserServiceRegion> provinceRegionList = Lists.newArrayList();
        List<UserServiceRegion> cityRegionList = Lists.newArrayList();
        List<UserServiceRegion> areaRegionList = Lists.newArrayList();
        List<Area> citys;
        UserServiceRegion cityRegion;
        List<Area> areas;
        UserServiceRegion areaRegion;
        List<SysOfficeRegion> nationwide = list.stream().filter(t -> t.getAreaType() == 1).collect(Collectors.toList());
        List<SysOfficeRegion> wholeProvince = list.stream().filter(t -> t.getAreaType() == 2).collect(Collectors.toList());
        List<SysOfficeRegion> wholeCity = list.stream().filter(t -> t.getAreaType() == 3).collect(Collectors.toList());
        List<SysOfficeRegion> wholeArea = list.stream().filter(t -> t.getAreaType() == 4).collect(Collectors.toList());
        if (nationwide.size() > 0) {
            wholeProvince = Lists.newArrayList();
            wholeCity = Lists.newArrayList();
            wholeArea = Lists.newArrayList();
            for (Area province : provinceMap.values()) {
                provinceRegion = new UserServiceRegion();
                cityRegionList = Lists.newArrayList();
                provinceRegion.setProvinceId(province.getId());
                provinceRegion.setProvinceName(province.getName());
                citys = cityGroupingByMap.get(province.getId());
                if (citys != null) {
                    for (Area city : citys) {
                        cityRegion = new UserServiceRegion();
                        areaRegionList = Lists.newArrayList();
                        cityRegion.setProvinceId(province.getId());
                        cityRegion.setProvinceName(province.getName());
                        cityRegion.setCityId(city.getId());
                        cityRegion.setCityName(city.getName());
                        areas = areaGroupingByMap.get(city.getId());
                        if (areas != null) {
                            for (Area area : areas) {
                                areaRegion = new UserServiceRegion();
                                areaRegion.setProvinceId(province.getId());
                                areaRegion.setProvinceName(province.getName());
                                areaRegion.setCityId(city.getId());
                                areaRegion.setCityName(city.getName());
                                areaRegion.setAreaId(area.getId());
                                areaRegion.setAreaName(area.getName());
                                areaRegionList.add(areaRegion);
                            }
                        }
                        cityRegion.setRegionList(areaRegionList);
                        cityRegionList.add(cityRegion);
                    }
                }
                provinceRegion.setRegionList(cityRegionList);
                provinceRegionList.add(provinceRegion);
            }

        }
        if (wholeProvince.size() > 0) {
            for (SysOfficeRegion entity : wholeProvince) {
                provinceRegion = new UserServiceRegion();
                cityRegionList = Lists.newArrayList();
                wholeProvince = wholeProvince.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                        -> new TreeSet<>(Comparator.comparing(SysOfficeRegion::getProvinceId))), ArrayList::new));
                wholeCity = wholeCity.stream().filter(t -> !t.getProvinceId().equals(entity.getProvinceId())).collect(Collectors.toList());
                wholeArea = wholeArea.stream().filter(t -> !t.getProvinceId().equals(entity.getProvinceId())).collect(Collectors.toList());
                provinceRegion.setProvinceId(entity.getProvinceId());
                if (provinceMap.get(entity.getProvinceId()) != null) {
                    provinceRegion.setProvinceName(provinceMap.get(entity.getProvinceId()).getName());
                }
                citys = cityGroupingByMap.get(entity.getProvinceId());
                if (citys != null) {
                    for (Area city : citys) {
                        cityRegion = new UserServiceRegion();
                        areaRegionList = Lists.newArrayList();
                        cityRegion.setProvinceId(entity.getProvinceId());
                        if (provinceMap.get(entity.getProvinceId()) != null) {
                            cityRegion.setProvinceName(provinceMap.get(entity.getProvinceId()).getName());
                        }
                        cityRegion.setCityId(city.getId());
                        cityRegion.setCityName(city.getName());
                        areas = areaGroupingByMap.get(city.getId());
                        if (areas != null) {
                            for (Area area : areas) {
                                areaRegion = new UserServiceRegion();
                                areaRegion.setProvinceId(entity.getProvinceId());
                                if (provinceMap.get(entity.getProvinceId()) != null) {
                                    areaRegion.setProvinceName(provinceMap.get(entity.getProvinceId()).getName());
                                }
                                areaRegion.setCityId(city.getId());
                                areaRegion.setCityName(city.getName());
                                areaRegion.setAreaId(area.getId());
                                areaRegion.setAreaName(area.getName());
                                areaRegionList.add(areaRegion);
                            }
                        }
                        areaRegionList.sort(Comparator.comparing(UserServiceRegion::getAreaId));
                        cityRegion.setRegionList(areaRegionList);
                        cityRegionList.add(cityRegion);
                    }
                }
                cityRegionList.sort(Comparator.comparing(UserServiceRegion::getCityId));
                provinceRegion.setRegionList(cityRegionList);
                provinceRegionList.add(provinceRegion);
            }
        }
        List<UserServiceRegion> cityAreaRegionList = Lists.newArrayList();
        if (wholeCity.size() > 0) {
            cityAreaRegionList = Lists.newArrayList();
            for (SysOfficeRegion entity : wholeCity) {
                cityRegion = new UserServiceRegion();
                cityRegion.setProvinceId(entity.getProvinceId());
                wholeCity = wholeCity.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                        -> new TreeSet<>(Comparator.comparing(SysOfficeRegion::getCityId))), ArrayList::new));
                wholeArea = wholeArea.stream().filter(t -> !t.getCityId().equals(entity.getCityId())).collect(Collectors.toList());
                if (provinceMap.get(entity.getProvinceId()) != null) {
                    cityRegion.setProvinceName(provinceMap.get(entity.getProvinceId()).getName());
                }
                cityRegion.setCityId(entity.getCityId());
                if (cityMap.get(entity.getCityId()) != null) {
                    cityRegion.setCityName(cityMap.get(entity.getCityId()).getName());
                }

                areas = areaGroupingByMap.get(entity.getCityId());
                if (areas != null) {
                    for (Area area : areas) {
                        areaRegion = new UserServiceRegion();
                        areaRegion.setProvinceId(entity.getProvinceId());
                        if (provinceMap.get(entity.getProvinceId()) != null) {
                            areaRegion.setProvinceName(provinceMap.get(entity.getProvinceId()).getName());
                        }
                        areaRegion.setCityId(entity.getCityId());
                        if (cityMap.get(entity.getCityId()) != null) {
                            areaRegion.setCityName(cityMap.get(entity.getCityId()).getName());
                        }
                        areaRegion.setAreaId(area.getId());
                        areaRegion.setAreaName(area.getName());
                        cityAreaRegionList.add(areaRegion);
                    }
                }
                cityAreaRegionList.sort(Comparator.comparing(UserServiceRegion::getAreaId));
            }

        }
        areaRegionList = Lists.newArrayList();
        if (wholeArea.size() > 0) {
            for (SysOfficeRegion entity : wholeArea) {
                areaRegion = new UserServiceRegion();
                areaRegion.setProvinceId(entity.getProvinceId());
                if (provinceMap.get(entity.getProvinceId()) != null) {
                    areaRegion.setProvinceName(provinceMap.get(entity.getProvinceId()).getName());
                }
                areaRegion.setCityId(entity.getCityId());
                if (cityMap.get(entity.getCityId()) != null) {
                    areaRegion.setCityName(cityMap.get(entity.getCityId()).getName());
                }
                areaRegion.setAreaId(entity.getAreaId());
                if (areaMap.get(entity.getAreaId()) != null) {
                    areaRegion.setAreaName(areaMap.get(entity.getAreaId()).getName());
                }
                areaRegionList.add(areaRegion);
            }

        }
        if (wholeCity.size() > 0 || wholeArea.size() > 0) {
            cityRegionList = Lists.newArrayList();
            if (wholeCity.size() > 0) {
                areaRegionList.addAll(cityAreaRegionList);
            }

            Map<Long, List<UserServiceRegion>> areaRegionMap = areaRegionList.stream().collect(Collectors.groupingBy(UserServiceRegion::getCityId));
            for (Map.Entry<Long, List<UserServiceRegion>> entry : areaRegionMap.entrySet()) {
                cityRegion = new UserServiceRegion();
                cityRegion.setProvinceId(entry.getValue().get(0).getProvinceId());
                cityRegion.setCityId(entry.getKey());
                if (cityMap.get(entry.getKey()) != null) {
                    cityRegion.setCityName(cityMap.get(entry.getKey()).getName());
                }
                cityRegion.setRegionList(entry.getValue());
                cityRegionList.add(cityRegion);
            }

            Map<Long, List<UserServiceRegion>> cityRegionMap = cityRegionList.stream().collect(Collectors.groupingBy(UserServiceRegion::getProvinceId));
            for (Map.Entry<Long, List<UserServiceRegion>> entry : cityRegionMap.entrySet()) {
                provinceRegion = new UserServiceRegion();
                provinceRegion.setProvinceId(entry.getKey());
                if (provinceMap.get(entry.getKey()) != null) {
                    provinceRegion.setProvinceName(provinceMap.get(entry.getKey()).getName());
                }
                provinceRegion.setRegionList(entry.getValue());
                provinceRegionList.add(provinceRegion);
            }
        }
        provinceRegionList.sort(Comparator.comparing(UserServiceRegion::getProvinceId));
        return provinceRegionList;
    }

    public Map<String, List<Area>> getRegionArea(String beGrantedAreaIds, String unauthorizedAreaIds) {
        List<Area> beGrantedArea = Lists.newArrayList();
        List<Area> unauthorizedArea = Lists.newArrayList();
        Map<String, List<Area>> map = new HashMap<>();
        List<Long> areaIdList = Lists.newArrayList();
        Map<Long, Area> areaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);
        if (beGrantedAreaIds != null && !beGrantedAreaIds.equals("")) {
            areaIdList = Arrays.stream(beGrantedAreaIds.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            for (Long id : areaIdList) {
                beGrantedArea.add(areaMap.get(id));
            }
        }
        if (unauthorizedAreaIds != null && !unauthorizedAreaIds.equals("")) {
            areaIdList = Arrays.stream(unauthorizedAreaIds.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            for (Long id : areaIdList) {
                unauthorizedArea.add(areaMap.get(id));
            }
        }

        map.put("beGrantedArea", beGrantedArea);
        map.put("unauthorizedArea", unauthorizedArea);
        return map;
    }

    public void save(Long keFuId, Long provinceId, Long cityId, String areaRegion) {
        List<Long> areaIds = Lists.newArrayList();
        if (areaRegion != null && !areaRegion.equals("")) {
            areaIds = Arrays.stream(areaRegion.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        }
        List<UserRegion> userRegionList = userRegionDao.getUserRegions(keFuId);
        List<Area> areaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY, cityId);// 区县
        List<Area> cityList = areaService.findListByParent(Area.TYPE_VALUE_CITY, provinceId);// 市
        List<Area> provinceList = areaService.findListByType(Area.TYPE_VALUE_PROVINCE);//省

        List<UserRegion> wholeProvince = userRegionList.stream().filter(t -> t.getAreaType() == 2).collect(Collectors.toList());
        List<UserRegion> wholeCity = userRegionList.stream().filter(t -> t.getAreaType() == 3).collect(Collectors.toList());
        List<UserRegion> wholeArea = userRegionList.stream().filter(t -> t.getAreaType() == 4).collect(Collectors.toList());
        UserRegion userRegion;
        for (Long id : areaIds) {
            userRegion = new UserRegion();
            userRegion.setUserId(keFuId);
            userRegion.setProvinceId(provinceId);
            userRegion.setCityId(cityId);
            userRegion.setAreaId(id);
            userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
            wholeArea.add(userRegion);
        }
        if (wholeArea.size() > 0) {

            Map<Long, List<UserRegion>> listMap = wholeArea.stream().collect(Collectors.groupingBy(UserRegion::getCityId));
            if (listMap.get(cityId).size() == areaList.size()) {
                userRegion = new UserRegion();
                userRegion.setUserId(keFuId);
                userRegion.setAreaType(Area.TYPE_VALUE_CITY);
                userRegion.setProvinceId(provinceId);
                userRegion.setCityId(cityId);
                userRegion.setAreaId(0);
                wholeCity.add(userRegion);
                Map<Long, List<UserRegion>> cityMap = wholeCity.stream().collect(Collectors.groupingBy(UserRegion::getProvinceId));
                if (cityMap.get(provinceId).size() == cityList.size()) {
                    userRegion = new UserRegion();
                    userRegion.setUserId(keFuId);
                    userRegion.setAreaType(Area.TYPE_VALUE_PROVINCE);
                    userRegion.setProvinceId(provinceId);
                    userRegion.setCityId(0);
                    userRegion.setAreaId(0);
                    wholeProvince.add(userRegion);
                    if (wholeProvince.size() == provinceList.size()) {
                        userRegion = new UserRegion();
                        userRegion.setUserId(keFuId);
                        userRegion.setAreaType(Area.TYPE_VALUE_COUNTRY);
                        userRegion.setProvinceId(0);
                        userRegion.setCityId(0);
                        userRegion.setAreaId(0);
                        userRegionDao.deleteByUserId(keFuId);
                        userRegionDao.insert(userRegion);
                    } else {
                        userRegionDao.deleteByUserAndAreaAndAreaType(keFuId, provinceId, Area.TYPE_VALUE_PROVINCE);
                        userRegionDao.insert(userRegion);
                    }
                } else {
                    userRegionDao.deleteByUserAndAreaAndAreaType(keFuId, cityId, Area.TYPE_VALUE_CITY);
                    userRegionDao.insert(userRegion);
                }
            } else {
                for (Long id : areaIds) {
                    userRegion = new UserRegion();
                    userRegion.setUserId(keFuId);
                    userRegion.setProvinceId(provinceId);
                    userRegion.setCityId(cityId);
                    userRegion.setAreaId(id);
                    userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
                    userRegionDao.insert(userRegion);
                }
            }
        }
    }

    public List<User> getUserKeFuList(Long officeId, Long ProductCategoryId) {
        return officeRegionDao.getUserIdsByOfficeIdProductCategoryId(officeId, ProductCategoryId);
    }
}
