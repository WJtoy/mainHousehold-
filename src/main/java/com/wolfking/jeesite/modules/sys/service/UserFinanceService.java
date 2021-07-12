package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.GlobalMappingSyncTypeEnum;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.service.ServiceException;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sys.dao.RoleDao;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.dao.UserKeFuDao;
import com.wolfking.jeesite.modules.sys.dao.UserServicePointExploitDao;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryUserMappingService;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserFinanceService extends BaseService {

    @Resource
    private UserDao userDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private UserKeFuDao userKeFuDao;
    @Resource
    private UserServicePointExploitDao userServicePointExploitDao;
    @Autowired
    private MSUserService msUserService;

    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private MSSysOfficeService msSysOfficeService;

    @Autowired
    private ProductCategoryUserMappingService productCategoryUserMappingService;

    @Autowired
    private RedisUtils redisUtils;

    public Page<User> findUser(Page<User> page, User user) {

        user.setPage(page);

        List<Long> roleIds = Lists.newArrayList();

        List<Office> offices = userKeFuService.orderByOffice("财务", User.USER_TYPE_SERVICE);//获取网点开发部门
        List<Role> roleList;
        for (Office office : offices) {
            roleList = roleDao.getUserRolesByOfficeId(office.getId());
            if (roleList != null && roleList.size() > 0) {
                roleIds.addAll(roleList.stream().map(Role::getId).collect(Collectors.toList()));
            }
        }

        List<User> userList = userServicePointExploitDao.findList(user, roleIds, user.getPage());

        Set<Long> officeIds = userList.stream().map(User::getOfficeId).collect(Collectors.toSet());


        List<Long> ids = Lists.newArrayList();

        ids.addAll(officeIds);


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
                }
            }
        }
        page.setList(userList);
        return page;
    }

    public void saveUser(User user) {
        if (user.getId() == null || user.getId() <= 0) {
            user.preInsert();
            userKeFuDao.insert(user);
            MSUserUtils.addUserToRedis(user);//user微服务
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
            throw new ServiceException(user.getLoginName() + "没有设置角色权限！");
        }

        // 清除用户缓存
        UserUtils.clearCache(user);
    }
}
