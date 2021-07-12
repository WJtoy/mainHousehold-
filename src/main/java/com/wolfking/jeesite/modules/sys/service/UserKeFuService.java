package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.md.GlobalMappingSyncTypeEnum;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionDto;
import com.kkl.kklplus.entity.sys.SyncTypeEnum;
import com.kkl.kklplus.entity.sys.mq.MQSysUserCustomerMessage;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.service.ServiceException;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.mq.sender.SysUserCustomerSender;
import com.wolfking.jeesite.modules.sd.entity.OrderKefuTypeRuleEnum;
import com.wolfking.jeesite.modules.sys.dao.RoleDao;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.dao.UserKeFuDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryUserMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSRegionPermissionNewService;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import com.wolfking.jeesite.ms.providersys.service.MSSysUserCustomerService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.common.utils.Collections3.distinctByKey;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserKeFuService extends BaseService {

    @Resource
    private UserKeFuDao userKeFuDao;

    @Resource
    private UserDao userDao;

    @Resource
    private RoleDao roleDao;

    @Autowired
    private MSUserService msUserService;

    @Autowired
    private MSSysOfficeService msSysOfficeService;

    @Autowired
    private UserRegionService userRegionService;

    @Autowired
    private MSSysUserCustomerService msSysUserCustomerService;

    @Autowired
    private SysUserCustomerSender sysUserCustomerSender;

    @Autowired
    private ProductCategoryUserMappingService productCategoryUserMappingService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SystemService systemService;

    @Autowired
    private MSRegionPermissionNewService msRegionPermissionNewService;

    @Autowired
    private AreaService areaService;

    public Page<User> findUser(Page<User> page, User user) {
        // 生成数据权限过滤条件（dsf为dataScopeFilter的简写，在xml中使用 ${sqlMap.dsf}调用权限SQL）
//        user.getSqlMap().put("dsf", dataScopeFilter(user.getCurrentUser(), "o", "a"));
        // 设置分页参数
        user.setPage(page);
        // 执行分页查询


        if (user != null && user.getOffice() != null && user.getOffice().getId() > 0) {
            List<Long> officeIdList = msSysOfficeService.findIdListById(user.getOffice().getId());
            user.setOfficeIds(officeIdList);
        }

        List<User> userList = userKeFuDao.findList(user);

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

        if (!ObjectUtils.isEmpty(userList)) {
            List<Long> productCategoryIds;
            List<String> productCategoryNames;
            String productCategoryName;
            for (User newUser : userList) {
                if (newUser != null) {
                    if (newUser.getOffice() != null && newUser.getOffice().getId() != null) {
                        Office office = officeMap.get(newUser.getOffice().getId());
                        if (office != null) {
                            newUser.setOffice(office);  //获取 name,parent_id,parent_ids
                        }
                    }
                    if (newUser.getRoleList() == null || newUser.getRoleList().size() == 0) {
                        List<Role> roles = roleDao.getUserRoles(newUser.getId());
                        newUser.setRoleList(roles);
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
        }
        page.setList(userList);
        return page;
    }

    /**
     * 根据部门名称和部门类型获取所有部门
     *
     * @param name
     * @param userType
     * @return
     */
    public List<Office> orderByOffice(String name, Integer userType) {
        List<Office> officeList = msSysOfficeService.findListByNameAndType(name, userType);

        List<Office> outputOfficeList = Lists.newArrayList();
        List<Long> officeParentIds = msSysOfficeService.findParentListByNameAndType(name, userType);
//        officeParentIds = officeParentIds.stream().filter(t->t != 38L).collect(Collectors.toList());
        for (Long parentId : officeParentIds) {
            Office office = officeList.stream().filter(r -> r.getId().longValue() == parentId).findFirst().orElse(null);
            if (office != null) {
                office.setType(0);
            }
            outputOfficeList.add(office);
            recursionOffice(officeList, office, outputOfficeList);
        }
        return outputOfficeList;
    }
    /**
     * 根据部门名称和部门类型获取所有部门
     *
     * @param name
     * @param userType
     * @return
     */
    public List<Office> orderByOfficeNew(String name, Long officeId,Integer userType) {
        List<Office> officeList = msSysOfficeService.findListByNameAndType(name, userType);

        List<Office> outputOfficeList = Lists.newArrayList();
        List<Long> officeParentIds = Lists.newArrayList();
        if(officeId != null){
            if(officeId != 0){
                officeParentIds.add(officeId);
            }else {
                officeParentIds = msSysOfficeService.findParentListByNameAndType(name, userType);
            }
        }
        for (Long parentId : officeParentIds) {
            Office office = officeList.stream().filter(r -> r.getId().longValue() == parentId).findFirst().orElse(null);
            if (office != null) {
                office.setType(0);
            }
            outputOfficeList.add(office);
            recursionOffice(officeList, office, outputOfficeList);
        }
        return outputOfficeList;
    }
    // 递归office
    private void recursionOffice(List<Office> sourceOfficeList, Office off, List<Office> offices) {
        String blank = "&nbsp;&nbsp;&nbsp;";
        List<Office> existsOfficeList = sourceOfficeList.stream().filter(x -> x.getParent().getId().longValue() == off.getId()).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(existsOfficeList) && existsOfficeList.size() > 0) {
            for (Office tempOffice : existsOfficeList) {
                if (offices.stream().filter(r -> r.getId().equals(tempOffice.getId())).findFirst().orElse(null) == null) {

                    tempOffice.setType(off.getType() + 1);
                    String strName = "";
                    for (int i = 0; i < tempOffice.getType(); i++) {
                        strName = strName.concat(blank);
                    }
                    tempOffice.setName(strName.concat(tempOffice.getName()));
                    offices.add(tempOffice);
                    recursionOffice(sourceOfficeList, tempOffice, offices);
                }
            }
        }
    }

    public void saveUser(User user) {
        boolean isNew = false;
        if (user.getId() == null || user.getId() <= 0) {
            isNew = true;
            user.preInsert();
            userKeFuDao.insert(user);
            MSUserUtils.addUserToRedis(user);//user微服务
            if (user.getUserRegion() != null) {
                user.getUserRegion().setUserId(user.getId());
                saveUserRegion(user.getUserRegion());//新建用户保存授权区域
            }

        } else {
            user.preUpdate();
            userKeFuDao.update(user);
            msUserService.refreshUserCacheByUserId(user.getId());//user微服务
            // 更新用户与角色关联
            userDao.deleteUserRole(user);
        }
        //region


        if (user.getRoleList() != null && user.getRoleList().size() > 0) {
            userDao.insertUserRole(user);
        } else {
            throw new ServiceException(user.getLoginName() + "没有设置角色！");
        }

        userDao.deleteUserCustomer(user);
        msSysUserCustomerService.deleteByUserId(user.getId());   //sysUserCustomer微服务化
        try {
            MQSysUserCustomerMessage.SysUserCustomerMessage message = MQSysUserCustomerMessage.SysUserCustomerMessage.newBuilder()
                    .setUserId(user.getId())
                    .setSyncType(SyncTypeEnum.DELETE.getValue())
                    .build();
            sysUserCustomerSender.send(message);
        } catch (Exception e) {
            log.error("发送消息队列删除表数据库失败.SystemService.saveUser:{}", user.getId(), e.getMessage());
        }
        //new
        if (user.getUserType().equals(User.USER_TYPE_SERVICE)) {
            if (user.getCustomerList() != null && user.getCustomerList().size() > 0) {
                //去重，前端传入有重复
                List<Customer> customers = user.getCustomerList();
                customers = customers.stream().filter(distinctByKey(LongIDBaseEntity::getId)).collect(Collectors.toList());
                user.setCustomerList(customers);
                userDao.insertUserCustomer(user);
                msSysUserCustomerService.batchInsert(user);  // add on 2020-9-12
            }
        }
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()
                || user.getUserType() == User.USER_TYPE_GROUP.intValue() || user.getUserType() == User.USER_TYPE_ENGINEER.intValue()) {
            productCategoryUserMappingService.saveProductCategoryUserMapping(isNew ? GlobalMappingSyncTypeEnum.ADD : GlobalMappingSyncTypeEnum.UPDATE,
                    user.getId(), user.getProductCategoryIds());
            if (!isNew) {
                try {
                    redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SYS_DB, RedisConstant.SYS_USER_PRODUCT_CATEGORY, user.getId().toString());
                } catch (Exception e) {
                    log.error("hdel user product category list error:{}", user.getId(), e);
                }
            }
        }
        // 清除用户缓存
        UserUtils.clearCache(user);
    }


    public void deleteUser(User user) {
        userKeFuDao.delete(user);
        userRegionService.removeUserRegionCash(user.getId());
        // 同步到Activiti
        // 清除用户缓存
        List<Long> productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
        if (productCategoryIds != null && !productCategoryIds.isEmpty()) {
            productCategoryUserMappingService.deleteProductCategoryUserMapping(user.getId());
        }
        UserUtils.clearCache(user);

    }

    /**
     * 根据角色名称查询
     *
     * @param name
     * @return
     */
    public List<Role> getUserRolesByName(String name) {
        List<Role> roles = roleDao.getUserRolesByName(name);
        if (roles != null) {
            roles = roles.stream().sorted(Comparator.comparing(Role::getName).reversed()).collect(Collectors.toList());
            roles = roles.stream().filter(t -> !t.getName().equals("客服-技术支持")).collect(Collectors.toList());
            return roles;
        } else {
            return Lists.newArrayList();
        }
    }

    /**
     * 根据角色名称查询
     *
     * @param officeId
     * @return
     */
    public List<Role> getUserRolesByNameNew(Long officeId) {
        List<Role> roles = roleDao.getUserRolesByOfficeId(officeId);
        if (roles != null) {
            roles = roles.stream().sorted(Comparator.comparing(Role::getName).reversed()).collect(Collectors.toList());
            roles = roles.stream().filter(t -> !t.getName().equals("客服-技术支持")).collect(Collectors.toList());
            return roles;
        } else {
            return Lists.newArrayList();
        }
    }

    public List<String> getUserRegionNameList(Long userId, Integer type) {
        List<String> UserRegionNameList;
        List<UserRegion> userRegionList = userRegionService.getUserRegionsFromDB(userId);
        UserRegionNameList = selectUserRegionNames(userRegionList, type);

        return UserRegionNameList;
    }

    public Integer getManagerFlag(Long userId) {
        return userKeFuDao.getManagerFlag(userId);
    }


    public List<MDRegionPermissionDto> getUserRegionAreaList(Integer subFlag) {
        int groupType = 1;//组类型,1-工单流向,2-收费类型

        List<MDRegionPermissionDto> list = Lists.newArrayList();
        MDRegionPermissionDto permissionDto;
        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_PROVINCE);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_CITY);
        Map<Long, Area> countyMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);

        String provinceName = "";
        String cityName = "";
        String areaName = "";

        MDRegionPermissionDto city;
        MDRegionPermissionDto county;
        if (subFlag == 3 || subFlag == 4) {
            int type;
            if (subFlag == 4) {
                type = 3;
            } else {
                type = 1;
            }
            list = msRegionPermissionNewService.findRegionPermissionDtoList(groupType, type);
        } else {
            list = areaConvertMDRegionPermissionDto();
        }
        permissionDto = new MDRegionPermissionDto();
        permissionDto.setAreaName("全国");
        permissionDto.setProvinceId(1L);
        list.add(permissionDto);
        for (MDRegionPermissionDto dto : list) {
            city = new MDRegionPermissionDto();
            if (provinceMap.get(dto.getProvinceId()) != null) {
                provinceName = provinceMap.get(dto.getProvinceId()).getName();

                dto.setAreaName(provinceName);
            }
            if (dto.getProvinceId() == 1) {
                permissionDto = new MDRegionPermissionDto();
                provinceName = "全国";
                permissionDto.setAreaName("全国");
                permissionDto.setProvinceId(1L);
                permissionDto.setCityId(1L);
                dto.getPermissionDtoList().add(permissionDto);
            } else {
                city.setProvinceId(dto.getProvinceId());
                city.setAreaName(provinceName);
                city.setCityId(dto.getProvinceId());
                dto.getPermissionDtoList().add(city);
            }

            for (MDRegionPermissionDto entity : dto.getPermissionDtoList()) {
                if (entity.getProvinceId() == 1 && entity.getCityId().equals(entity.getProvinceId())) {
                    permissionDto = new MDRegionPermissionDto();
                    permissionDto.setAreaName("全国");
                    permissionDto.setProvinceId(1L);
                    permissionDto.setCityId(1L);
                    permissionDto.setAreaId(1L);
                    entity.getPermissionDtoList().add(permissionDto);
                }
                if (entity.getProvinceId() != 1 && entity.getCityId().equals(entity.getProvinceId())) {
                    county = new MDRegionPermissionDto();
                    county.setProvinceId(entity.getProvinceId());
                    county.setCityId(entity.getProvinceId());
                    county.setAreaId(entity.getProvinceId());
                    county.setAreaName(entity.getAreaName());
                    entity.getPermissionDtoList().add(county);
                }
                if (cityMap.get(entity.getCityId()) != null) {
                    cityName = cityMap.get(entity.getCityId()).getName();
                    entity.setAreaName(cityName);
                }
                for (MDRegionPermissionDto item : entity.getPermissionDtoList()) {

                    if (countyMap.get(item.getAreaId()) != null) {
                        areaName = countyMap.get(item.getAreaId()).getName();
                        item.setAreaName(areaName);
                    }

                }

                entity.setPermissionDtoList(entity.getPermissionDtoList().stream().sorted(Comparator.comparing(MDRegionPermissionDto::getAreaId)).collect(Collectors.toList()));
            }
            dto.setPermissionDtoList(dto.getPermissionDtoList().stream().sorted(Comparator.comparing(MDRegionPermissionDto::getCityId)).collect(Collectors.toList()));
        }
        list = list.stream().sorted(Comparator.comparing(MDRegionPermissionDto::getProvinceId)).collect(Collectors.toList());


        return list;
    }

    public List<MDRegionPermissionDto> areaConvertMDRegionPermissionDto() {
        List<Area> provinceList = areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
        List<Area> cityList = areaService.findListByType(Area.TYPE_VALUE_CITY);
        List<Area> countyList = areaService.findListByType(Area.TYPE_VALUE_COUNTY);

        List<MDRegionPermissionDto> list = Lists.newArrayList();

        MDRegionPermissionDto provinceRegionPermission;
        MDRegionPermissionDto cityRegionPermission;
        MDRegionPermissionDto areaRegionPermission;
        //所有区域
        Map<Long, List<Area>> provinceMap = cityList.stream().collect(Collectors.groupingBy(t -> t.getParent().getId()));
        Map<Long, List<Area>> cityMap = countyList.stream().collect(Collectors.groupingBy(t -> t.getParent().getId()));
        List<Area> citys;
        List<Area> areas;
        for (Area province : provinceList) {
            provinceRegionPermission = new MDRegionPermissionDto();
            provinceRegionPermission.setProvinceId(province.getId());
            provinceRegionPermission.setAreaName(province.getName());
            list.add(provinceRegionPermission);
            citys = provinceMap.get(province.getId());
            if (citys != null) {
                for (Area city : citys) {
                    cityRegionPermission = new MDRegionPermissionDto();
                    cityRegionPermission.setProvinceId(province.getId());
                    cityRegionPermission.setCityId(city.getId());
                    cityRegionPermission.setAreaName(city.getName());
                    provinceRegionPermission.getPermissionDtoList().add(cityRegionPermission);
                    areas = cityMap.get(city.getId());
                    if (areas != null) {
                        for (Area area : areas) {
                            areaRegionPermission = new MDRegionPermissionDto();
                            areaRegionPermission.setProvinceId(province.getId());
                            areaRegionPermission.setCityId(city.getId());
                            areaRegionPermission.setAreaId(area.getId());
                            areaRegionPermission.setAreaName(area.getName());
                            cityRegionPermission.getPermissionDtoList().add(areaRegionPermission);
                        }
                    }

                }
            }

        }


        return list;
    }

    public List<UserRegion> contrastUserRegion(List<UserRegion> list, Integer type, Long userId) {

        List<UserRegion> items = Lists.newArrayList();
        if (list != null && list.size() > 0) {
            int groupType = 1;//组类型,1-工单流向,2-收费类型

//
            List<Area> cityList = areaService.findListByType(Area.TYPE_VALUE_CITY);
//        List<Area> countyList = areaService.findListByType(Area.TYPE_VALUE_COUNTY);
//
//        //所有区域
            Map<Long, List<Area>> provinceMap = cityList.stream().collect(Collectors.groupingBy(t -> t.getParent().getId()));
//        Map<Long, List<Area>> cityMap = countyList.stream().collect(Collectors.groupingBy(t -> t.getParent().getId()));


            //勾选全国
            List<UserRegion> nationwide = list.stream().filter(t -> t.getAreaId() == 1).collect(Collectors.toList());
            //勾选省
            List<UserRegion> choiceProvince = list.stream().filter(t -> t.getProvinceId() != 1 && t.getAreaId() == t.getCityId()).collect(Collectors.toList());
            //勾选市
            List<UserRegion> choiceCity = list.stream().filter(t -> t.getAreaId() == 0).collect(Collectors.toList());
            //勾选区
            List<UserRegion> choiceArea = list.stream().filter(t -> t.getAreaId() != 0 && t.getAreaId() != t.getCityId() && t.getAreaId() != 1).collect(Collectors.toList());

            Map<Long, List<UserRegion>> listMap = choiceCity.stream().collect(Collectors.groupingBy(UserRegion::getProvinceId));

            UserRegion userRegion;


            List<MDRegionPermission> warrantyCity;
            List<UserRegion> userRegionCity;
            List<Long> collect = Lists.newArrayList();
//            if (type == 1 || type == 3) {
//                //客服已授权的区域
//                List<MDRegionPermission> userRegionAll = msRegionPermissionNewService.findAreaListByGroupTypeAndType(groupType, type);
//
//                Map<Long, List<MDRegionPermission>> warrantyProvinceMap = userRegionAll.stream().collect(Collectors.groupingBy(MDRegionPermission::getProvinceId));
//
//                for (Long provinceId : warrantyProvinceMap.keySet()) {
//                    warrantyCity = warrantyProvinceMap.get(provinceId);
//                    if (warrantyCity != null) {
//                        collect = warrantyCity.stream().map(MDRegionPermission::getCityId).distinct().collect(Collectors.toList());
//                    }
//                    userRegionCity = listMap.get(provinceId);
//                    if (userRegionCity != null) {
//                        if (collect.size() == userRegionCity.size()) {
//                            userRegion = new UserRegion();
//                            userRegion.setAreaType(Area.TYPE_VALUE_PROVINCE);
//                            userRegion.setUserId(userId);
//                            userRegion.setProvinceId(provinceId);
//                            userRegion.setCityId(0);
//                            userRegion.setAreaId(0);
//                            items.add(userRegion);
//                            choiceCity = choiceCity.stream().filter(t -> t.getProvinceId() != provinceId).collect(Collectors.toList());
//                            choiceArea = choiceArea.stream().filter(t -> t.getProvinceId() != provinceId).collect(Collectors.toList());
//                        }
//                    }
//                }
//            } else {
            List<Area> city;
            for (Long provinceId : provinceMap.keySet()) {
                city = provinceMap.get(provinceId);
                userRegionCity = listMap.get(provinceId);
                if (userRegionCity != null) {
                    if (city.size() == userRegionCity.size()) {
                        userRegion = new UserRegion();
                        userRegion.setAreaType(Area.TYPE_VALUE_PROVINCE);
                        userRegion.setUserId(userId);
                        userRegion.setProvinceId(provinceId);
                        userRegion.setCityId(0);
                        userRegion.setAreaId(0);
                        items.add(userRegion);
                        choiceCity = choiceCity.stream().filter(t -> t.getProvinceId() != provinceId).collect(Collectors.toList());
                        choiceArea = choiceArea.stream().filter(t -> t.getProvinceId() != provinceId).collect(Collectors.toList());

                    }
                }

            }
            // }
//            Map<Long, List<MDRegionPermission>> warrantyCityMap = userRegionAll.stream().collect(Collectors.groupingBy(MDRegionPermission::getCityId));
//
//            List<Area> citys;
//            List<Area> areas;
//            List<MDRegionPermission> warrantyCity;
//            List<MDRegionPermission> warrantyArea;
//            List<Long> collect;
//            boolean discernProvince = false;
//            if (nationwide.size() > 0) {
//                //对比授权的区与实际区数量
//                if (countyList.size() == userRegionAll.size()) {
//                    userRegion = new UserRegion();
//                    userRegion.setAreaType(Area.TYPE_VALUE_COUNTRY);
//                    userRegion.setUserId(userId);
//                    userRegion.setProvinceId(0);
//                    userRegion.setCityId(0);
//                    userRegion.setAreaId(0);
//                    items.add(userRegion);
//                } else {
//                    for (Long provinceId : provinceMap.keySet()) {
//                        collect = Lists.newArrayList();
//                        citys = provinceMap.get(provinceId);
//                        warrantyCity = warrantyProvinceMap.get(provinceId);
//                        if (warrantyCity != null) {
//                            collect = warrantyCity.stream().map(MDRegionPermission::getCityId).distinct().collect(Collectors.toList());
//                        }
//                        //对比该省授权市
//                        if (citys.size() == collect.size()) {
//                            discernProvince = true;
//                        }
//                        for (Long cityId : collect) {
//                            areas = cityMap.get(cityId);
//                            warrantyArea = warrantyCityMap.get(cityId);
//                            if (areas.size() == warrantyArea.size()) {
//                                userRegion = new UserRegion();
//                                userRegion.setAreaType(Area.TYPE_VALUE_CITY);
//                                userRegion.setUserId(userId);
//                                userRegion.setProvinceId(provinceId);
//                                userRegion.setCityId(cityId);
//                                userRegion.setAreaId(0);
//                                items.add(userRegion);
//                            } else {
//                                for (MDRegionPermission area : warrantyArea) {
//                                    userRegion = new UserRegion();
//                                    userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
//                                    userRegion.setUserId(userId);
//                                    userRegion.setProvinceId(provinceId);
//                                    userRegion.setCityId(cityId);
//                                    userRegion.setAreaId(area.getAreaId());
//                                    discernProvince = false;
//                                    items.add(userRegion);
//                                }
//                            }
//                        }
//                        if (discernProvince) {
//                            items = items.stream().filter(t -> t.getProvinceId() != provinceId).collect(Collectors.toList());
//                            userRegion = new UserRegion();
//                            userRegion.setAreaType(Area.TYPE_VALUE_PROVINCE);
//                            userRegion.setUserId(userId);
//                            userRegion.setProvinceId(provinceId);
//                            userRegion.setCityId(0);
//                            userRegion.setAreaId(0);
//                            items.add(userRegion);
//                        }
//                    }
//                }
//
//            } else {
//                //遍历勾选全省
//                if (choiceProvince.size() > 0) {
//                    for (UserRegion region : choiceProvince) {
//                        collect = Lists.newArrayList();
//                        citys = provinceMap.get(region.getProvinceId());
//                        warrantyCity = warrantyProvinceMap.get(region.getProvinceId());
//                        if (warrantyCity != null) {
//                            collect = warrantyCity.stream().map(MDRegionPermission::getCityId).distinct().collect(Collectors.toList());
//                        }
//                        //对比该省下的市数量
//                        if (citys.size() == collect.size()) {
//                            discernProvince = true;
//                        }
//                        for (Long cityId : collect) {
//                            areas = cityMap.get(cityId);
//                            warrantyArea = warrantyCityMap.get(cityId);
//                            if (areas.size() == warrantyArea.size()) {
//                                userRegion = new UserRegion();
//                                userRegion.setAreaType(Area.TYPE_VALUE_CITY);
//                                userRegion.setUserId(userId);
//                                userRegion.setProvinceId(region.getProvinceId());
//                                userRegion.setCityId(cityId);
//                                userRegion.setAreaId(0);
//                                items.add(userRegion);
//                            } else {
//                                for (MDRegionPermission area : warrantyArea) {
//                                    userRegion = new UserRegion();
//                                    userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
//                                    userRegion.setUserId(userId);
//                                    userRegion.setProvinceId(region.getProvinceId());
//                                    userRegion.setCityId(cityId);
//                                    userRegion.setAreaId(area.getAreaId());
//                                    discernProvince = false;
//                                    items.add(userRegion);
//                                }
//                            }
//                        }
//                        if (discernProvince) {
//                            items = items.stream().filter(t -> t.getProvinceId() != region.getProvinceId()).collect(Collectors.toList());
//                            userRegion = new UserRegion();
//                            userRegion.setAreaType(Area.TYPE_VALUE_PROVINCE);
//                            userRegion.setUserId(userId);
//                            userRegion.setProvinceId(region.getProvinceId());
//                            userRegion.setCityId(0);
//                            userRegion.setAreaId(0);
//                            items.add(userRegion);
//                        }
//                    }
//                }
//
//                //遍历勾选全市
//                for (UserRegion region : choiceCity) {
//                    areas = cityMap.get(region.getCityId());
//                    warrantyArea = warrantyCityMap.get(region.getCityId());
//                    if (areas.size() == warrantyArea.size()) {
//                        userRegion = new UserRegion();
//                        userRegion.setAreaType(Area.TYPE_VALUE_CITY);
//                        userRegion.setUserId(userId);
//                        userRegion.setProvinceId(region.getProvinceId());
//                        userRegion.setCityId(region.getCityId());
//                        userRegion.setAreaId(0);
//                        items.add(userRegion);
//                        choiceArea = choiceArea.stream().filter(t -> t.getCityId() != region.getCityId()).collect(Collectors.toList());
//                    } else {
//                        for (MDRegionPermission area : warrantyArea) {
//                            userRegion = new UserRegion();
//                            userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
//                            userRegion.setUserId(userId);
//                            userRegion.setProvinceId(region.getProvinceId());
//                            userRegion.setCityId(region.getCityId());
//                            userRegion.setAreaId(area.getAreaId());
//                            items.add(userRegion);
//                            choiceArea = choiceArea.stream().filter(t -> t.getAreaId() != area.getAreaId()).collect(Collectors.toList());
//                        }
//                    }
//
//                }
//                //遍历勾选区
//                for (UserRegion region : choiceArea) {
//                    userRegion = new UserRegion();
//                    userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
//                    userRegion.setUserId(userId);
//                    userRegion.setProvinceId(region.getProvinceId());
//                    userRegion.setCityId(region.getCityId());
//                    userRegion.setAreaId(region.getAreaId());
//                    items.add(userRegion);
//                }
//            }
//        } else {
            if (nationwide.size() > 0) {
                userRegion = new UserRegion();
                userRegion.setAreaType(Area.TYPE_VALUE_COUNTRY);
                userRegion.setUserId(userId);
                userRegion.setProvinceId(0);
                userRegion.setCityId(0);
                userRegion.setAreaId(0);
                items.add(userRegion);
            } else {
                if (choiceProvince.size() > 0) {
                    for (UserRegion region : choiceProvince) {
                        userRegion = new UserRegion();
                        userRegion.setAreaType(Area.TYPE_VALUE_PROVINCE);
                        userRegion.setUserId(userId);
                        userRegion.setProvinceId(region.getProvinceId());
                        userRegion.setCityId(0);
                        userRegion.setAreaId(0);
                        items.add(userRegion);
                    }
                }
                if (choiceCity.size() > 0) {
                    for (UserRegion region : choiceCity) {
                        userRegion = new UserRegion();
                        userRegion.setAreaType(Area.TYPE_VALUE_CITY);
                        userRegion.setUserId(userId);
                        userRegion.setProvinceId(region.getProvinceId());
                        userRegion.setCityId(region.getCityId());
                        userRegion.setAreaId(0);
                        items.add(userRegion);
                        choiceArea = choiceArea.stream().filter(t -> t.getCityId() != region.getCityId()).collect(Collectors.toList());
                    }
                }
                if (choiceArea.size() > 0) {
                    for (UserRegion region : choiceArea) {
                        userRegion = new UserRegion();
                        userRegion.setAreaType(Area.TYPE_VALUE_COUNTY);
                        userRegion.setUserId(userId);
                        userRegion.setProvinceId(region.getProvinceId());
                        userRegion.setCityId(region.getCityId());
                        userRegion.setAreaId(region.getAreaId());
                        items.add(userRegion);
                    }
                }

            }
            // }
        }
        return items;
    }

    public boolean saveUserRegion(UserRegionViewModel userRegion) {
        List<UserRegion> list;
        List<UserRegion> userRegionList = Lists.newArrayList();
        Set<UserRegion> newRegions;
        Set<UserRegion> removeRegions = null;
        int type = 0;
        if (userRegion != null && userRegion.getUserId() != null) {
            try {
                if (userRegion.getRegionList() != null) {
                    list = userRegion.getRegionList();
                    if (userRegion.getSubFlag() == 3) {
                        type = 1;
                    }
                    if (userRegion.getSubFlag() == 4) {
                        type = 3;
                    }
                    userRegionList = contrastUserRegion(list, type, userRegion.getUserId());
                    Map<String, Set<UserRegion>> regionsMap = compareUserRegions(userRegionList, userRegion.getUserId());
                    newRegions = regionsMap.get("new");
                    removeRegions = regionsMap.get("remove");
                } else {
                    newRegions = new HashSet<>(userRegionList);
                }
                userRegionService.saveUserRegions(userRegion.getUserId(), newRegions, removeRegions);

                userKeFuDao.updateSubFlag(userRegion.getUserId(), userRegion.getSubFlag());

                msUserService.refreshUserCacheByUserId(userRegion.getUserId());//user微服务
                //再次移除用户-区域缓存
                userRegionService.removeUserRegionCash(userRegion.getUserId());

                //重新加载缓存
                List<UserRegion> userRegionsFromDB = userRegionService.getUserRegionsFromDB(userRegion.getUserId());
                userRegionService.writeUserRegionCache(userRegion.getUserId(), userRegionsFromDB);
                userRegion.setRegionList(userRegionsFromDB);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比对新提交区域与原有区域
     * 返回比对后结果
     *
     * @param regions
     * @param userId
     * @return Map<key, List < UserRegion>>
     * new: 新增集合
     * remove: 删除集合
     */
    private Map<String, Set<UserRegion>> compareUserRegions(List<UserRegion> regions, Long userId) {
        Set<UserRegion> regionSet = Sets.newHashSetWithExpectedSize(regions.size());
        regions.forEach(t -> {
            t.setUserId(userId);
            regionSet.add(t);
        });
        List<UserRegion> orgRegions = userRegionService.getUserRegionsFromDB(userId);
        Map<String, Set<UserRegion>> map = Maps.newHashMapWithExpectedSize(2);
        if (ObjectUtils.isEmpty(orgRegions)) {
            map.put("new", regionSet);
            map.put("remove", Sets.newHashSet());
            return map;
        }
        Set<UserRegion> orgSet = orgRegions.stream().collect(Collectors.toSet());
        Set<UserRegion> newSet = Sets.difference(regionSet, orgSet);
        Set<UserRegion> removeSet = Sets.difference(orgSet, regionSet);
        map.put("new", newSet);
        map.put("remove", removeSet);
        return map;
    }


    public List<String> selectUserRegionNames(List<UserRegion> userRegionList, Integer type) {
        int groupType = 1;//组类型,1-工单流向,2-收费类型

        List<String> UserRegionNameList = Lists.newArrayList();
        Map<Long, Area> provinceMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_PROVINCE);
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_CITY);
        Map<Long, Area> countyMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);

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


    public User getKefuSupervisor(Customer customer,Long areaId,Long productCategoryId,int kefuType,Long cityId,Long provinceId) {
        User user;
        //自动客服
        if(kefuType== OrderKefuTypeRuleEnum.ORDER_AUTO.getCode()){
            return userKeFuDao.getKefuSupervisor(areaId,productCategoryId,KefuTypeEnum.AutomaticKefu.getCode(),cityId,provinceId);
        }
        //突击区域订单
        if(kefuType == OrderKefuTypeRuleEnum.ORDER_RUSH.getCode()){//可突击，获取突击客服
            return userKeFuDao.getKefuSupervisor(areaId,productCategoryId,KefuTypeEnum.Rush.getCode(),cityId,provinceId);
        }
        //3.其他订单
        if(customer.getVipFlag()==1){ //先找KA客服主管
            user = userKeFuDao.getVIPKefuSupervisor(customer.getId(),areaId,productCategoryId,cityId,provinceId);
            if(user!=null){
                return user;
            }
        }else{
            user = userKeFuDao.getKefuSupervisor(areaId,productCategoryId,KefuTypeEnum.Kefu.getCode(),cityId,provinceId);
            if(user!=null){
                return user;
            }
        }
        user = userKeFuDao.getKefuSupervisor(areaId,productCategoryId,KefuTypeEnum.COMMON_KEFU.getCode(),cityId,provinceId);
        return user;
    }
}
