package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.modules.md.dao.KeFuRegionDao;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sys.dao.RoleDao;
import com.wolfking.jeesite.modules.sys.dao.UserKeFuDao;
import com.wolfking.jeesite.modules.sys.dao.UserRegionDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSRegionPermissionNewService;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KeFuRegionService extends BaseService {
    @Resource
    private UserKeFuDao userKeFuDao;

    @Resource
    private UserRegionDao userRegionDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private KeFuRegionDao keFuRegionDao;
    @Autowired
    private MSSysOfficeService msSysOfficeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MSRegionPermissionNewService msRegionPermissionNewService;

    @Autowired
    private AreaService areaService;


    public List<User> findUser(User user,Long officeId) {
        List<Long> officeIdList;
        if (user != null && user.getOffice() != null && user.getOffice().getId() > 0) {
            officeIdList = msSysOfficeService.findIdListById(user.getOffice().getId());
            user.setOfficeIds(officeIdList);
        }else {
            if(user == null){
                user = new User();
            }
            User u = UserUtils.getUser();
            for(Role role : u.getRoleList()){
                if(role.getId() == 1){
                    officeId = 0L;
                }
            }
            officeIdList = msSysOfficeService.findIdListById(officeId);
            user.setOfficeIds(officeIdList);
        }
        List<User> userList = keFuRegionDao.findList(user);

        Set<Long> officeIds = userList.stream().map(User::getOfficeId).collect(Collectors.toSet());


        List<Long> ids = Lists.newArrayList();

        ids.addAll(officeIds);

        Set<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toSet());
        List<UserProductCategory> productCategory = Lists.newArrayList();
        if (userIds.size() > 0) {
            productCategory = userKeFuDao.getProductCategoryIds(Lists.newArrayList(userIds));
        }


        Map<Long, ProductCategory> allProductCategoryMap = ProductUtils.getAllProductCategoryMap();


        Map<Long, List<Long>> productCategoryMap = productCategory.stream().collect(Collectors.groupingBy(UserProductCategory::getUserId, Collectors.mapping(UserProductCategory::getProductCategoryId, Collectors.toList())));


        List<Office> officeList = Lists.newArrayList();
        if (ids.size() > 1) {
            officeList = msSysOfficeService.findSpecColumnListByIds(ids.stream().distinct().collect(Collectors.toList()));
        } else if (ids.size() == 1) {
            Office office = msSysOfficeService.getSpecColumnById(ids.get(0));
            Optional.ofNullable(office).ifPresent(officeList::add);
        }
        Map<Long, Office> officeMap;
        officeMap = ObjectUtils.isEmpty(officeList) ? Maps.newHashMap() : officeList.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, r -> r, (v2, v1) -> v1));
        List<UserRegion> userRegionList;
        List<Long> productCategoryIds;
        List<String> productCategoryNames;
        List<String> keFuRegionList;
        List<Role> roles;
        String productCategoryName;
        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_PROVINCE);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_CITY);
        Map<Long, Area> countyMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);
        int type;
        for (User newUser : userList) {
            if (newUser != null) {
                if (newUser.getOffice() != null && newUser.getOffice().getId() != null) {
                    Office office = officeMap.get(newUser.getOffice().getId());
                    if (office != null) {
                        newUser.setOffice(office);  //获取 name,parent_id,parent_ids
                    }
                }
                if (newUser.getSubFlag() == 3) {
                    type = 1;
                } else if (newUser.getSubFlag() == 4) {
                    type = 3;
                }else {
                    type = 0;
                }
                userRegionList = userRegionDao.getUserRegions(newUser.getId());
                keFuRegionList = selectUserRegionNames(userRegionList, type, provinceMap, cityMap, countyMap);
                if (keFuRegionList != null) {
                    newUser.setUserRegionNames(keFuRegionList);
                }

                roles = roleDao.getUserRoles(newUser.getId());
                newUser.setRoleList(roles);

                if (newUser.getSubFlag() == 1) {
                    newUser.setCustomerList(customerService.findVipListWithIdAndNameByKefu(newUser.getId()));
                }
                if (newUser.getId() != null) {
                    productCategoryIds = productCategoryMap.get(newUser.getId());
                    productCategoryNames = Lists.newArrayList();
                    if (productCategoryIds != null) {
                        for (Long productCategoryId : productCategoryIds) {
                            if (allProductCategoryMap.get(productCategoryId) != null) {
                                productCategoryName = allProductCategoryMap.get(productCategoryId).getName();
                                if (productCategoryName != null && !productCategoryName.equals("")) {
                                    productCategoryNames.add(productCategoryName);
                                }
                            }


                        }
                    }
                    newUser.setProductCategoryNames(StringUtils.join(productCategoryNames, ","));
                }
            }
        }

        return userList;
    }


    public List<String> selectUserRegionNames(List<UserRegion> userRegionList, Integer type, Map<Long, Area> provinceMap, Map<Long, Area> cityMap, Map<Long, Area> countyMap) {
        int groupType = 1;//组类型,1-工单流向,2-收费类型
        List<String> UserRegionNameList = Lists.newArrayList();
        Map<Long, List<UserRegion>> map = userRegionList.stream().collect(Collectors.groupingBy(UserRegion::getProvinceId, Collectors.toList()));
        String provinceName = "";
        String cityName = "";
        String areaName = "";
        String countyName = "";
        List<Area> areaList;
        Map<Long, List<MDRegionPermission>> listMap = new HashMap<>();
        if (type == 1 || type == 3) {
            List<MDRegionPermission> list = msRegionPermissionNewService.findAreaListByGroupTypeAndType(groupType, type);
            listMap = list.stream().collect(Collectors.groupingBy(MDRegionPermission::getProvinceId));
        }
        List<Long> collect = Lists.newArrayList();
        List<MDRegionPermission> cityList;
        List<String> cityNames;
        List<UserServiceRegion> countyNames;
        Area area;
        for (Map.Entry<Long, List<UserRegion>> entry : map.entrySet()) {
            if (entry.getKey() != 0) {
                if (provinceMap.get(entry.getKey()) != null) {
                    provinceName = provinceMap.get(entry.getKey()).getName();
                }
                cityNames = Lists.newArrayList();
                countyNames = Lists.newArrayList();
                areaList = Lists.newArrayList();
                for (UserRegion item : entry.getValue()) {
                    if (item.getAreaType().equals(Area.TYPE_VALUE_PROVINCE)) {
                        if (type == 3 || type == 1) {
                            cityList = listMap.get(item.getProvinceId());
                            if (cityList != null) {
                                collect = cityList.stream().map(MDRegionPermission::getCityId).distinct().collect(Collectors.toList());
                            }else {
                                collect = Lists.newArrayList();
                            }
                            for (Long cityId : collect) {
                                area = cityMap.get(cityId);
                                areaList.add(area);
                            }
                            cityName = areaList.stream().map(Area::getName).collect(Collectors.joining("，"));
                        } else {
                            areaList = areaService.findListByParent(Area.TYPE_VALUE_CITY, item.getProvinceId());// 市
                            cityName = areaList.stream().map(Area::getName).collect(Collectors.joining("，"));
                        }
                        areaName = "<p style='font-weight: bold;float: left;'>" + provinceName + "：</p>" + cityName;
                    } else if (item.getAreaType().equals(Area.TYPE_VALUE_CITY)) {
                        if (cityMap.get(item.getCityId()) != null) {
                            cityName = cityMap.get(item.getCityId()).getName();
                            cityNames.add(cityName);
                        }
                    } else if (item.getAreaType().equals(Area.TYPE_VALUE_COUNTY)) {
                        UserServiceRegion county = new UserServiceRegion();
                        if (cityMap.get(item.getCityId()) != null) {
                            cityName = cityMap.get(item.getCityId()).getName();
                            county.setCityId(item.getCityId());
                            county.setCityName(cityName);
                            if (countyMap.get(item.getAreaId()) != null) {
                                countyName = countyMap.get(item.getAreaId()).getName();
                                county.setAreaId(item.getAreaId());
                                county.setAreaName(countyName);
                            }
                            countyNames.add(county);
                            cityNames.add(cityName);
                        }

                    }
                }

                Map<String, List<UserServiceRegion>> countyNameMap = countyNames.stream().collect(Collectors.groupingBy(UserServiceRegion::getCityName));
                if (areaList.size() <= 0) {
                    List<String> countys;
                    String county = "";
                    cityNames = cityNames.stream().distinct().collect(Collectors.toList());
                    for (int i = 0; i < cityNames.size(); i++) {
                        if (countyNameMap.get(cityNames.get(i)) != null) {
                            countys = countyNameMap.get(cityNames.get(i)).stream().map(UserServiceRegion::getAreaName).collect(Collectors.toList());
                            county = String.join("，", countys);
                            cityNames.set(i, cityNames.get(i) + "(" + county + ")");
                        }
                    }
                    cityName = String.join("，", cityNames);
                    areaName = "<p style='font-weight: bold;float: left;'>" + provinceName + "：</p>" + cityName;
                }
            } else {
                areaName = "<p style='font-weight: bold;float: left;'>全国</p>";
            }
            UserRegionNameList.add(areaName);
        }
        return UserRegionNameList;
    }
}
