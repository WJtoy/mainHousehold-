package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDAreaTypeEnum;
import com.kkl.kklplus.entity.md.MDEngineerArea;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.utils.ObjectUtils;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EngineerForSPService {
    @Autowired
    private MSEngineerForSPService msEngineerForSPService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSEngineerAreaService msEngineerAreaService;


    @Autowired
    private SystemService systemService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ServicePointService servicePointService;

    @Resource
    private ServicePointDao servicePointDao;

    @Resource
    private UserDao userDao;

    @Autowired
    private MSUserService msUserService;

    @Autowired
    private MSServicePointAreaService msServicePointAreaService;

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public Engineer getEngineer(Long id) {
        Engineer engineer = msEngineerForSPService.getById(id);
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

    /**
     * 分页查询
     * 先从数据库返回id,再根据id从缓存中读取，缓存不存在则再从数据库读取并更新至缓存
     */
    public Page<Engineer> findPage(Page<Engineer> page, Engineer entity) {
        entity.setPage(page);
        return findEngineerListForPage(page, entity);
    }

    public Page<Engineer> findEngineerListForPage(Page<Engineer> page, Engineer engineer) {
        // add 2019-11-9
        Page<Engineer> engineerPage = msEngineerForSPService.findEngineerList(page, engineer);
        List<Engineer> engineerList = engineerPage.getList();

        if (engineerList != null && engineerList.size() > 0) {
            List<Long> engineerIds = engineerList.stream().map(engineer1 -> engineer1.getId()).distinct().collect(Collectors.toList());
            List<User> userList = systemService.findEngineerAccountList(engineerIds, null); //subFlag 1：是子帐号　0：不是子帐号
            Map<Long,User> userMap = userList!= null&& !userList.isEmpty()?userList.stream().collect(Collectors.toMap(User::getEngineerId, Function.identity())): Maps.newHashMap();
            Map<Long, String> serviceAreaMap = getEngineerServiceAreas(engineerIds); // add on 2019-11-8

            Map<String, Dict> levelMap = MSDictUtils.getDictMap("EngineerLevel");
            for (Engineer eng : engineerList) {
                if (eng.getLevel() != null && Integer.parseInt(eng.getLevel().getValue()) > 0) {
                    eng.setLevel(levelMap.get(eng.getLevel().getValue()));
                }
                if (eng.getArea()!= null && eng.getArea().getId() != null) {
                    Area area = areaService.getFromCache(eng.getArea().getId());
                    eng.setArea(area);
                }

                User user = userMap.get(eng.getId());
                if (user != null) {
                    eng.setAccountId(user.getId());
                    eng.setAppLoged(user.getAppLoged());
                }
                eng.setAreas(serviceAreaMap.get(eng.getId()));
            }
        }

        engineerPage.setList(engineerList);
        return engineerPage;
    }

    public Map<Long, String> getEngineerServiceAreas(List<Long> engineerIds) {
        List<MDEngineerArea> mdEngineerAreaList = msEngineerAreaService.findEngineerAreasWithIds(engineerIds);

        Map<Long, String> serviceAreaMap = Maps.newHashMap();
        if (mdEngineerAreaList != null && !mdEngineerAreaList.isEmpty()) {
            // 获取区域id列表，再通过区域id列表获取区域哈希对象
            List<Long> areaIds = mdEngineerAreaList.stream().map(MDEngineerArea::getAreaId).distinct().collect(Collectors.toList());
            Map<Long,String> areaMap = Maps.newHashMap();
            if (areaIds != null && !areaIds.isEmpty()) {
                areaIds.stream().forEach(r->{
                    Area area = areaService.getFromCache(r,Area.TYPE_VALUE_COUNTY);
                    if (area != null) {
                        areaMap.put(r, area.getName());
                    }
                });
            }
            // 生成key为安维id,value为区域名称哈希对象
            Map<Long, List<MDEngineerArea>> engineerAreaMap = mdEngineerAreaList.stream().collect(Collectors.groupingBy(MDEngineerArea::getEngineerId));
            engineerAreaMap.forEach((k,v)->{
                if (v != null && !v.isEmpty()) {
                    String areaNames = v.stream().filter(mdEngineerArea -> areaMap.get(mdEngineerArea.getAreaId()) != null).map(mdEngineerArea -> areaMap.get(mdEngineerArea.getAreaId())).collect(Collectors.joining(","));
                    serviceAreaMap.put(k, areaNames);
                }
            });
        }
        return serviceAreaMap;
    }

    /**
     * 保存
     */
    @Transactional(readOnly = false)
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
            Integer masterEngineerCount = msEngineerForSPService.checkMasterEngineer(engineer.getServicePoint().getId(), null);
            if (masterEngineerCount != null && masterEngineerCount == 0 && engineer.getMasterFlag() == 0) {
                throw new RuntimeException("网点:" + servicePoint.getServicePointNo()+"没有主账号,请设置主账号!" );
            }
            // add on 2020-12-26 end

            engineer.preInsert();
            MSErrorCode msErrorCode = msEngineerForSPService.save(engineer,true);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("保存师傅信息到微服务中出错。出错原因:" + msErrorCode.getMsg());
            }
            List<Long> engineerIds = Lists.newArrayList();
            if (engineer.getMasterFlag() == 1) {
                servicePoint.setUpdateBy(engineer.getCreateBy());
                servicePoint.setUpdateDate(engineer.getCreateDate());
                servicePoint.setPrimary(engineer);
                engineerIds = msEngineerForSPService.findSubEngineerIds(engineer.getId(), servicePoint.getId());
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
                    MSErrorCode msErrorCode1 = msEngineerForSPService.resetEngineerMasterFlag(engineerIds);
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
            Integer masterEngineerCount = msEngineerForSPService.checkMasterEngineer(engineer.getServicePoint().getId(), engineer.getId());
            if (masterEngineerCount != null && masterEngineerCount == 0 && engineer.getMasterFlag() == 0) {
                throw new RuntimeException("网点:" + servicePoint.getServicePointNo()+"没有主账号,请设置主账号!" );
            }
            // add on 2020-12-26 end

            engineer.preUpdate();
            // add on 2019-10-17 begin
            // 集中调用微服务
            MSErrorCode msErrorCode = msEngineerForSPService.save(engineer,false);
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
                    List<Long> engineerIds = msEngineerForSPService.findSubEngineerIds(engineer.getId(), servicePoint.getId()); //add on 2019-10-17
                    if (engineerIds != null && engineerIds.size() > 0) {
                        servicePointDao.resetUserEngineerSubFlag(engineerIds);
                        // add on 2019-10-17 begin
                        // Engineer微服务
                        MSErrorCode msErrorCode1 = msEngineerForSPService.resetEngineerMasterFlag(engineerIds);
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

    public void upgrade(Engineer engineer) {
        // TODO： 当前代码段没有地方调用，如果后续要用此处的话，不要忘记发送MQServcicePointEngineerMessage  // 2019-10-24
        //String key = String.format(RedisConstant.MD_SERVICEPOINT, engineer.getServicePoint().getId());
        Engineer orgEngineer = getEngineer(engineer.getId());//原始信息 //切换为微服务
        //切换为微服务 ServicePoint servicePoint = dao.get(engineer.getServicePoint().getId());//新网点
        ServicePoint servicePoint = servicePointService.get(engineer.getServicePoint().getId());//新网点
        engineer.setMasterFlag(1);//主帐号
        engineer.preUpdate();
        //dao.upgradeEngineer(engineer);  //mark on 2020-1-13 md_engineer
        msEngineerForSPService.upgradeEngineer(engineer);  // add on 2019-10-18 // Engineer微服务

        //原网点的接单数等不变更

        servicePoint.setPrimary(engineer);
        servicePoint.setOrderCount(servicePoint.getOrderCount() + engineer.getOrderCount());
        servicePoint.setPlanCount(servicePoint.getPlanCount() + engineer.getPlanCount());
        servicePoint.setBreakCount(servicePoint.getBreakCount() + engineer.getBreakCount());
        servicePoint.setUpdateBy(engineer.getUpdateBy());
        servicePoint.setUpdateDate(engineer.getUpdateDate());
        //dao.upgradeServicePoint(servicePoint);//mark on 2020-1-14  web端去servicePoint
        // add on 2019-10-4 begin
        MSErrorCode msErrorCode = msServicePointService.upgradeServicePoint(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务升级网点失败，失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 更新网点的主账号信息(微服务调用)// add on 2019-9-17
     * @param servicePoint
     */
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

    /**
     * 读取网点负责的区域列表
     *
     * @param id 网点id
     * @return
     */
    public List<Area> getAreas(Long id) {
        List<Long> areaIdList = msServicePointAreaService.findAreaIds(id);
        List<Area> areaList = areaService.findServicePointAreas(areaIdList);
        return areaList;
    }

    /**
     * 安维负责的区域列表
     *
     * @param id
     * @return
     */
    public List<Area> getEngineerAreaList(Long id) {
        // return dao.getEngineerAreas(id); // mark on 2019-11-7
        // add on 2019-11-7 begin  // EngineerArea微服务
        List<Long> areaIds = getEngineerAreaIds(id);
        List<Area> areaListFromMS = areaService.findEngineerAreas(areaIds);
        return areaListFromMS;
    }

    /**
     * 读取安维负责的区域ID列表
     *
     * @param id 安维id
     * @return
     */
    public List<Long> getEngineerAreaIds(Long id) {
        return msEngineerAreaService.findEngineerAreaIds(id); //add on 2019-11-7 //EngineerArea微服务
    }

    /**
     * 停用安维人员
     * 逻辑删除
     */
    @Transactional(readOnly = false)
    public void delete(Engineer engineer) {
        if (engineer.getServicePoint() == null || engineer.getServicePoint().getId() == 0) {
            engineer = getEngineer(engineer.getId());//切换为微服务
        }
        engineer.preUpdate();
        engineer.setDelFlag(Engineer.DEL_FLAG_DELETE);
        //dao.deleteEngineer(engineer);       //mark on 2020-1-13  md_engineer
        msEngineerForSPService.delete(engineer);   // add on 2019-10-18
        //禁止安维登录
        servicePointDao.deleteUser(engineer);

        //被删除的安维是主帐号,变更网点主帐号为0
        if (engineer.getMasterFlag() == 1) {
            ServicePoint point = servicePointService.getFromCache(engineer.getServicePoint().getId());
            if (point == null) {
                //切换为微服务 point = dao.get(engineer.getServicePoint().getId());
                point = servicePointService.get(engineer.getServicePoint().getId());
            }
            point.setPrimary(new Engineer(0L));
            point.setUpdateBy(engineer.getUpdateBy());
            point.setUpdateDate(engineer.getUpdateDate());
            updatePrimaryAccount(point);  //add on 2019-10-4
        }
    }

    /**
     * 启用安维人员
     */
    @Transactional(readOnly = false)
    public void enable(Engineer engineer) {
        if (engineer.getServicePoint() == null || engineer.getServicePoint().getId() == 0) {
            engineer = getEngineer(engineer.getId());//切换为微服务
        }
        engineer.preUpdate();
        engineer.setDelFlag(Engineer.DEL_FLAG_NORMAL);
        //dao.deleteEngineer(engineer);  // mark on 2020-1-13  md_engineer
        msEngineerForSPService.delete(engineer);  //add on 2019-10-18
        //禁止安维登录
        servicePointDao.deleteUser(engineer);
        ServicePoint point = servicePointService.getFromCache(engineer.getServicePoint().getId());
        //启用的是主帐号,变更网点主帐号
        if (engineer.getMasterFlag() == 1 && point != null && (point.getPrimary() == null || point.getPrimary().getId() == null || point.getPrimary().getId() == 0l)) {
            if (point == null) {
                //切换为微服务 point = dao.get(engineer.getServicePoint().getId());
                point = servicePointService.get(engineer.getServicePoint().getId());
            }
            point.setPrimary(engineer);
            point.setUpdateBy(engineer.getUpdateBy());
            point.setUpdateDate(engineer.getUpdateDate());
            updatePrimaryAccount(point);
        }
    }

    /**
     * 检查网点下是否存在其他的主帐号
     *
     * @param id       网点id
     * @param expectId 排除安维人员id
     * @return
     */
    public int checkMasterEngineer(Long id, Long expectId) {
        return msEngineerForSPService.checkMasterEngineer(id, expectId);  // add on 2019-10-22 //Engineer微服务
    }

    /**
     * 按手机号返回安维帐号
     *
     * @param phone    手机号
     * @param expectId 排除安维id
     */
    public User getEngineerByPhoneExpect(String phone, Long expectId) {
        return servicePointDao.getEngineerByPhoneExpect(phone, expectId);
    }

    /**
     * 重置密码
     * 手机号后6位
     *
     * @param engineer
     */
    @Transactional(readOnly = false)
    public void resetPassword(User engineer) {
        servicePointDao.resetPassword(engineer);
    }

    public Page<Engineer> getEngineersForKefu(Page<Engineer> page, Engineer engineer) {
        engineer.setPage(page);
        // add on 2019-10-26 begin
        Page<Engineer> engineerPage = msEngineerForSPService.findEngineerForKeFu(page, engineer);
        return engineerPage;
        // add on 2019-10-26 end
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
}
