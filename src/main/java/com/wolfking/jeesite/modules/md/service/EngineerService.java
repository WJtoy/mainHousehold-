package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDAreaTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.persistence.LongIDTreeEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.EngineerDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerAreaService;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 服务网点
 * Ryan Lu
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class EngineerService extends LongIDCrudService<EngineerDao, Engineer> {

    @Resource
    private EngineerDao engineerDao;

    @Resource
    private ServicePointDao servicePointDao;

    @Resource
    private UserDao userDao;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSEngineerAreaService msEngineerAreaService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSUserService msUserService;

    public Page<Engineer> getEngineersForKefu(Page<Engineer> page, Engineer engineer) {
        engineer.setPage(page);
        /*
        // mark on 2019-10-26
        // engineer微服务
        List<Engineer> engineers = engineerDao.getEngineersForKefu(engineer);
        page.setList(engineers);
        return page;
        */

        // add on 2019-10-26 begin
        Page<Engineer> engineerPage = msEngineerService.findEngineerForKeFu(page, engineer);
        return engineerPage;
        // add on 2019-10-26 end
    }
    /**
     * 获取所有能手机派单的主账号安维列表  // add on 2019-9-30
     * @param engineer
     * @return
     */
    public List<Long> findMasterEngineer(Engineer engineer) {
        //return engineerDao.findMasterEngineer(engineer);  //mark on 2019-10-26

        List<Long> list = Lists.newArrayList();

        Page<Engineer> engineerPage = new Page<>();
        engineerPage.setPageSize(1000);
        engineer.setPage(engineerPage);
        Page<Long> engineerIdPage = msEngineerService.findAppFlagEngineer(engineerPage, engineer);
        //log.warn("总页数：{}",engineerIdPage.getTotalPage());
        //log.warn("记录：{}",engineerIdPage.getList());
        if (engineerIdPage.getList()!= null && !engineerIdPage.getList().isEmpty()) {
            list.addAll(engineerIdPage.getList());
        }

        for(int i=2; i< engineerIdPage.getTotalPage()+1;i++) {
            Page<Engineer> engineerPage1 = new Page<>();
            engineerPage1.setPageSize(1000);
            engineerPage1.setPageNo(i);
            engineer.setPage(engineerPage1);

            Page<Long> engineerIdPage1 = msEngineerService.findAppFlagEngineer(engineerPage1, engineer);
            //log.warn("记录数{}",engineerIdPage1.getList());
            if (engineerIdPage1.getList()!= null && !engineerIdPage1.getList().isEmpty()) {
                list.addAll(engineerIdPage1.getList());
            }
        }
        //log.warn("{}", list);

        return list;
    }

    public List<Long> findPagingIdWithNameOrPhone(Engineer engineer) {
        List<Long> list = Lists.newArrayList();

        Page<Engineer> engineerPage = new Page<>();
        engineerPage.setPageSize(200);
        engineer.setPage(engineerPage);
        Page<Long> engineerIdPage = msEngineerService.findPagingIdWithNameOrPhone(engineerPage, engineer);
        if (engineerIdPage.getList()!= null && !engineerIdPage.getList().isEmpty()) {
            list.addAll(engineerIdPage.getList());
        }

        for(int i=2; i< engineerIdPage.getTotalPage()+1;i++) {
            Page<Engineer> engineerPage1 = new Page<>();
            engineerPage1.setPageSize(200);
            engineerPage1.setPageNo(i);
            engineer.setPage(engineerPage1);

            Page<Long> engineerIdPage1 = msEngineerService.findPagingIdWithNameOrPhone(engineerPage1, engineer);
            if (engineerIdPage1.getList()!= null && !engineerIdPage1.getList().isEmpty()) {
                list.addAll(engineerIdPage1.getList());
            }
        }

        return list;
    }

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public Engineer getEngineer(Long id) {
        Engineer engineer = msEngineerService.getById(id);
        engineer = getExtraInfoForEngineer(engineer);
        return engineer;
    }

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public Engineer getEngineerFromCache(Long id) {
        Engineer engineer = msEngineerService.getByIdFromCache(id);
        engineer = getExtraInfoForEngineer(engineer);
        return engineer;
    }

    /**
     * 获取安维人员的附加信息
     * @param engineer
     * @return
     */
    public Engineer getExtraInfoForEngineer(Engineer engineer) {
        Long servicePointId = null;
        Long engineerId = null;
        Long areaId = null;
        if (engineer != null) {
            servicePointId = engineer.getServicePoint() != null && engineer.getServicePoint().getId() !=null? engineer.getServicePoint().getId():null;
            engineerId = engineer.getId();
            areaId = engineer.getArea() != null && engineer.getArea().getId() != null? engineer.getArea().getId():null;
        }
        ServicePoint servicePoint = msServicePointService.getCacheById(servicePointId);
        if (servicePoint != null) {
            if (engineer.getServicePoint() == null) {
                engineer.setServicePoint(new ServicePoint(servicePointId));
            }
            engineer.getServicePoint().setName(servicePoint.getName());
            engineer.getServicePoint().setServicePointNo(servicePoint.getServicePointNo());
        }

        User user = systemService.getUserByEngineerId(engineerId);
        if (user != null) {
            engineer.setAccountId(user.getId());
            engineer.setAppLoged(user.getAppLoged());
        }

        Area area = areaId==null?null:areaService.getFromCache(areaId);
        if (area != null) {
            if (engineer.getArea()==null) {
                engineer.setArea(new Area(area.getId()));
            }
            engineer.getArea().setName(area.getName());
            engineer.getArea().setFullName(area.getFullName());
        }
        return engineer;
    }

    public List<Engineer> findEngineerListFromCache(List<Long> engineerIds) {
        List<Engineer> engineerList = msEngineerService.findEngineersByIds(engineerIds, Arrays.asList("id","name","masterFlag","appFlag"));
        if (engineerList != null && !engineerList.isEmpty()) {
            engineerList.stream().forEach(engineer -> {
                User user = systemService.getUserByEngineerId(engineer.getId());
                if (user != null) {
                    engineer.setAppLoged(user.getAppLoged());
                }
            });
        }
        return engineerList;
    }

    public Map<Long,Engineer> findEngineerListFromCacheToMap(List<Long> engineerIds) {
        List<Engineer> engineerList =  findEngineerListFromCache(engineerIds);
        return engineerList != null && !engineerList.isEmpty()?engineerList.stream().collect(Collectors.toMap(Engineer::getId, Function.identity())): Maps.newHashMap();
    }

    public List<String> getServiceAreaNames(List<Long> areaIds){
        Map<Long, Area> areaMap = AreaUtils.getAreaMap(MDAreaTypeEnum.COUNTY.getValue());
        Map<Long, Area> cityMap = AreaUtils.getAreaMap(MDAreaTypeEnum.CITY.getValue());

        String cityName;
        String areaName;
        List<String> areaNames = Lists.newArrayList();
        List<Area> areaList = Lists.newArrayList();
        for (Long id : areaIds) {
            areaList.add(areaMap.get(id));
        }
        List<Long> list = areaList.stream().sorted(Comparator.comparing(Area::getParentId)).map(Area::getParentId).distinct().collect(Collectors.toList());
        Map<Long, List<Area>> citys = areaList.stream().collect(Collectors.groupingBy(Area::getParentId));

        for(Long parentId : list){
            cityName = cityMap.get(parentId).getName();
            areaName = citys.get(parentId).stream().map(Area::getName).collect(Collectors.joining(","));
            areaNames.add("<p style='font-weight: bold;float: left;'>" + cityName + "：</p>" + areaName);
        }
        return areaNames;
    }

    public void save(Engineer engineer) {
        boolean isNew = engineer.getIsNewRecord();

        ServicePoint servicePoint = servicePointService.getFromCache(engineer.getServicePoint().getId());
        if (servicePoint == null) {
            servicePoint = servicePointService.get(engineer.getServicePoint().getId());
        }
        if (engineer.getEngineerAddress() != null && engineer.getEngineerAddress().getAreaName() != null) {
            String address = engineer.getEngineerAddress().getAreaName() + engineer.getEngineerAddress().getAddress();
            engineer.getEngineerAddress().setAddress(address);
        }
        if(engineer.getEngineerAddress() != null && engineer.getEngineerAddress().getAreaId() != null){
            Area city = areaService.getFromCache(engineer.getEngineerAddress().getAreaId());
            if(city != null){
                engineer.getEngineerAddress().setCityId(city.getParentId());
                Area province = areaService.getFromCache(city.getParentId());
                if(province != null){
                    engineer.getEngineerAddress().setProvinceId(province.getParentId());
                }
            }
        }
        if(engineer.getArea() != null && engineer.getArea().getId() != null){
            engineer.setAddress(engineer.getAddress().replace(engineer.getArea().getFullName(),""));
            engineer.setAddress(engineer.getArea().getFullName() + " " + engineer.getAddress());
        }
        if (isNew) {
            servicePoint.setSubEngineerCount(servicePoint.getSubEngineerCount() + 1);
            // add on 2020-12-26 begin
            // 先查询该网点是否有主账号
            Integer masterEngineerCount = msEngineerService.checkMasterEngineer(engineer.getServicePoint().getId(), null);
            if (masterEngineerCount != null && masterEngineerCount == 0 && engineer.getMasterFlag() == 0) {
                throw new RuntimeException("网点:" + servicePoint.getServicePointNo()+"没有主账号,请设置主账号!" );
            }
            // add on 2020-12-26 end

            engineer.preInsert();
            MSErrorCode msErrorCode = msEngineerService.save(engineer,true);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("保存师傅信息到微服务中出错。出错原因:" + msErrorCode.getMsg());
            }
            List<Long> engineerIds = Lists.newArrayList();
            if (engineer.getMasterFlag() == 1) {
                servicePoint.setUpdateBy(engineer.getCreateBy());
                servicePoint.setUpdateDate(engineer.getCreateDate());
                servicePoint.setPrimary(engineer);
                engineerIds = msEngineerService.findSubEngineerIds(engineer.getId(), servicePoint.getId());
                if (engineerIds != null && engineerIds.size() > 0) {
                    servicePointDao.resetUserEngineerSubFlag(engineerIds);
                }

            }
            //add sys_user,帐号:手机号 密码:手机号
            User user = new User();
            user.setCompany(new Office(engineer.getServicePoint().getId()));//网点
            user.setLoginName(engineer.getContactInfo());
            user.setName(engineer.getName());
            user.setMobile(engineer.getContactInfo());
            user.setUserType(User.USER_TYPE_ENGINEER);
            user.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
            user.getRoleList().add(new Role(6l));
            user.setPassword(SystemService.entryptPassword(StringUtils.right(engineer.getContactInfo().trim(), 6)));//手机号后6位
            user.setCreateBy(engineer.getCreateBy());
            user.setCreateDate(engineer.getCreateDate());
            user.setEngineerId(engineer.getId());
            userDao.insert(user);
            // add on 2020-10-14 begin
            if (engineer.getMasterFlag() == 1) {
                userDao.insertUserRole(user);//角色
            }
            // add on 2020-10-14 end
            MSUserUtils.addUserToRedis(user);//user微服务
            engineer.setAccountId(user.getId());// 18/01/16
            //区域
            List<Long> areas = engineer.getAreaIds();

            HashMap<String, Object> maps = Maps.newHashMap();
            if (engineer.getMasterFlag() == 0) {
                //更新网点数量
                maps.put("subEngineerCount", 1);
                maps.put("id", servicePoint.getId());
            }
            if (engineer.getMasterFlag() == 1) {
                updatePrimaryAccount(servicePoint);                  //add on 2019-9-17
                if (engineerIds != null && engineerIds.size() > 0) {
                    MSErrorCode msErrorCode1 = msEngineerService.resetEngineerMasterFlag(engineerIds);
                    if (msErrorCode1.getCode() > 0) {
                        throw new RuntimeException("重置网点下师傅主账号信息出错。出错原因:" + msErrorCode1.getMsg());
                    }
                }
            } else if (engineer.getMasterFlag() == 0) {
                updateServicePointByMap(maps);    // add on 2019-10-4
            }
            // add on 2019-10-17 end
            // add on 2019-11-7 begin  //EngineerArea微服务
            if (areas != null && !areas.isEmpty()) {
                msEngineerAreaService.assignEngineerAreas(areas, engineer.getId());
            }
            // add on 2019-11-7 end
        } else {
            // add on 2020-12-26 begin
            // 先查询该网点是否有主账号
            Integer masterEngineerCount = msEngineerService.checkMasterEngineer(engineer.getServicePoint().getId(), engineer.getId());
            if (masterEngineerCount != null && masterEngineerCount == 0 && engineer.getMasterFlag() == 0) {
                throw new RuntimeException("网点:" + servicePoint.getServicePointNo()+"没有主账号,请设置主账号!" );
            }
            // add on 2020-12-26 end

            engineer.preUpdate();
            // add on 2019-10-17 begin
            // 集中调用微服务
            MSErrorCode msErrorCode = msEngineerService.save(engineer,false);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("更新师傅信息到微服务中出错。出错原因:" + msErrorCode.getMsg());
            }
            // add on 2019-10-17 end
            if (engineer.getMasterFlag() != engineer.getOrgMasterFlag()) {
                //主帐号->子帐号
                if (engineer.getMasterFlag() == 0) {
                    servicePoint.setPrimary(new Engineer(0l));
                } else {
                    //子帐号 -> 主帐号
                    servicePoint.setPrimary(engineer);
                }
                servicePoint.setUpdateDate(engineer.getUpdateDate());
                servicePoint.setUpdateBy(engineer.getUpdateBy());
                updatePrimaryAccount(servicePoint);  //add on 2019-10-4
                if (servicePoint.getPrimary().getId() > 0l) {
                    List<Long> engineerIds = msEngineerService.findSubEngineerIds(engineer.getId(), servicePoint.getId()); //add on 2019-10-17
                    if (engineerIds != null && engineerIds.size() > 0) {
                        servicePointDao.resetUserEngineerSubFlag(engineerIds);
                        // add on 2019-10-17 begin
                        // Engineer微服务
                        MSErrorCode msErrorCode1 = msEngineerService.resetEngineerMasterFlag(engineerIds);
                        if (msErrorCode1.getCode() > 0) {
                            throw new RuntimeException("重置网点下师傅主账号信息出错。出错原因:" + msErrorCode1.getMsg());
                        }
                        // add on 2019-10-17 end
                    }
                }
                // add on 2020-10-14 begin
                // 当子师傅转为主账号后，给其添加角色,不然登录Web系统会因没有权限出现一片空白
                if (engineer.getMasterFlag() == 1) {
                    User user = userDao.getByEngineerId(engineer.getId());
                    user.getRoleList().add(new Role(6L));
                    Long userId  = userDao.getUserRoleByUserId(user.getId()); // 查询当前用户是否有角色  add on 2020-11-21
                    if (userId != null) {
                        userDao.deleteUserRole(user);
                    }
                    userDao.insertUserRole(user);
                }
                // add on 2020-10-14 begin
            }
            //修改登录信息
            servicePointDao.updateUser(engineer);

            msUserService.refreshUserCacheByEngineerId(engineer.getId());//user微服务
        }


    }

    public void updatePrimaryAccount(ServicePoint servicePoint) {
        MSErrorCode msErrorCode = msServicePointService.updatePrimaryAccount(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新网点的主账号信息失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 按需更改信息：订单相关统计数量只做增减,客评：平均运算，四舍五入
     * @param maps
     */
    public void updateServicePointByMap(HashMap<String, Object> maps) {
        MSErrorCode msErrorCode = msServicePointService.updateServicePointByMap(maps);
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("调用微服务按需更新网点信息失败.失败原因:" + msErrorCode.getMsg());
        }
    }
}
