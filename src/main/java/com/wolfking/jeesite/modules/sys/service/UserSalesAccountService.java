package com.wolfking.jeesite.modules.sys.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.sys.SyncTypeEnum;
import com.kkl.kklplus.entity.sys.mq.MQSysUserSubMessage;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.common.service.ServiceException;
import com.wolfking.jeesite.modules.mq.sender.SysUserSubSender;
import com.wolfking.jeesite.modules.sys.dao.RoleDao;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.dao.UserKeFuDao;
import com.wolfking.jeesite.modules.sys.dao.UserSubDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserSalesAccountService extends BaseService {

    @Resource
    private RoleDao roleDao;

    @Resource
    private UserDao userDao;

    @Resource
    private UserKeFuDao userKeFuDao;

    @Resource
    private UserSubDao userSubDao;
    @Autowired
    private MSUserService msUserService;

    @Autowired
    private SysUserSubSender sysUserSubSender;


    /**
     * 根据部门查询角色权限
     *
     * @param officeId
     * @return
     */
    public List<Role> getUserRolesByOfficeId(Long officeId) {
        return roleDao.getUserRolesByOfficeId(officeId);
    }


    public void saveUser(User user) {
        if (user.getId() == null || user.getId() <= 0) {
            user.preInsert();
            userKeFuDao.insert(user);
            MSUserUtils.addUserToRedis(user);//user微服务

            if(user.getSubFlag() == 3){
                List<Long> userSubIds = Lists.newArrayList();
                if(user.getUserSubIds().length() > 0){
                    userSubIds = Arrays.stream(user.getUserSubIds().split(",")).map(s -> Long.valueOf(s.trim())).collect(Collectors.toList());
                }
                userSubIds.add(user.getId());   //主管也有客户,由此也加在列表中
                UserSub userSub;
                for(Long subUserId : userSubIds){
                    userSub = new UserSub();
                    userSub.setUserId(user.getId());
                    userSub.setType(User.USER_TYPE_SALES);
                    userSub.setSubUserId(subUserId);
                    userSubDao.deleteUserSub(subUserId);//删除之前主管的关联
                    userSubDao.saveUserUnderling(userSub);//保存现在关联的主管
                }
                try {
                    MQSysUserSubMessage.SysUserSubMessage message = MQSysUserSubMessage.SysUserSubMessage.newBuilder()
                            .setUserId(user.getId())
                            .setSyncType(SyncTypeEnum.ADD.getValue())
                            .setType(User.USER_TYPE_SALES)
                            .addAllSubUserIds(userSubIds)
                            .build();
                    sysUserSubSender.send(message);
                }catch (Exception e){
                    log.error("发送消息队列删除表数据库失败.UserSalesAccountService.saveUser:{}",user.getId(),e.getMessage());
                }
            }

        } else {
            User pastUser = UserUtils.get(user.getId());
            user.preUpdate();
            userKeFuDao.update(user);
            msUserService.refreshUserCacheByUserId(user.getId());//user微服务
            // 更新用户与角色关联
            userDao.deleteUserRole(user);

            if (pastUser.getSubFlag() == 3 && user.getSubFlag() == 1 || user.getSubFlag() == 2){
                userSubDao.deleteUserUnderling(user.getId());
                MQSysUserSubMessage.SysUserSubMessage message = MQSysUserSubMessage.SysUserSubMessage.newBuilder()
                        .setUserId(user.getId())
                        .setSyncType(SyncTypeEnum.DELETE.getValue())
                        .setType(User.USER_TYPE_SALES)
                        .addAllSubUserIds(Lists.newArrayList())
                        .build();
                sysUserSubSender.send(message);
            } else if (pastUser.getSubFlag() == 1 && user.getSubFlag() == 3){
                userSubDao.deleteUserSub(user.getId());//删除之前主管的关联
                MQSysUserSubMessage.SysUserSubMessage message = MQSysUserSubMessage.SysUserSubMessage.newBuilder()
                        .setUserId(user.getId())
                        .setSyncType(SyncTypeEnum.UPDATE.getValue())
                        .setType(User.USER_TYPE_SALES)
                        .addAllSubUserIds(Lists.newArrayList())
                        .build();
                sysUserSubSender.send(message);
            }
        }


        if (user.getRoleList() != null && user.getRoleList().size() > 0) {
            userDao.insertUserRole(user);
        } else {
            throw new ServiceException(user.getLoginName() + "没有设置角色！");
        }
        // 清除用户缓存
        UserUtils.clearCache(user);
    }

    public List<User> getUserSales(Long userId,Integer userType,Integer subFlag){
        if(subFlag == 3){
            subFlag = 1;
        }//转换为非主管

        List<User> userSalesList = userDao.findUserByUserTypeSubFlag(userType, subFlag);//获取非主管用户
        List<UserSub> userSubList = userSubDao.getAllUserSubList();
        if(userId != null){
            userSubList = userSubList.stream().filter(t-> !t.getUserId().equals(userId)).collect(Collectors.toList());
            userSalesList = userSalesList.stream().filter(t-> !t.getId().equals(userId)).collect(Collectors.toList());
        }
        Map<Long,Long> userSubMap = userSubList.stream().collect(Collectors.toMap(UserSub::getSubUserId, UserSub::getUserId,(key1, key2) -> key2));
        Long id;
        Office office;
        User user;
        for(User userSales:userSalesList){
            id = userSubMap.get(userSales.getId());
            if(id != null){
                user = UserUtils.get(id);
                office = new Office(user.getId(),user.getName());
                userSales.setOffice(office);
            }
        }
        return userSalesList;
    }

    public List<UserSub> getUserSubList(Long userId){
        List<UserSub> list = userSubDao.getUserSubList(userId);
        User user;
        List<UserSub> filterList = Lists.newArrayList();
        for(UserSub entity:list){
            if(entity.getSubUserId() != null){
                if (entity.getSubUserId().equals(userId)) {
                    continue;
                }
                user = UserUtils.get(entity.getSubUserId());
                if(user != null && user.getName() != null){
                    entity.setSubUserName(user.getName());
                }
                filterList.add(entity);
            }
        }
        //return list;
        return filterList;
    }

    public void saveUserUnderling(UserUnderling userUnderling){
        UserSub userSub;
        userSubDao.deleteUserUnderling(userUnderling.getUserId());
        userSubDao.deleteUserSub(userUnderling.getUserId());//删除之前主管的关联
        if(userUnderling.getSubUserIds() != null) {
            if (!userUnderling.getSubUserIds().contains(userUnderling.getUserId())) {
                userUnderling.getSubUserIds().add(userUnderling.getUserId());  //主管也有客户，所以要加上他自己
            }
            for (Long subUserId : userUnderling.getSubUserIds()) {
                userSub = new UserSub();
                userSub.setUserId(userUnderling.getUserId());
                userSub.setType(User.USER_TYPE_SALES);
                userSub.setSubUserId(subUserId);
                userSubDao.deleteUserSub(subUserId);//删除之前主管的关联
                userSubDao.saveUserUnderling(userSub);//保存现在关联的主管
            }
        } else {
            userUnderling.setSubUserIds(Lists.newArrayList(userUnderling.getUserId()));
        }
        userKeFuDao.updateSubFlag(userUnderling.getUserId(),3);//修改用户为业务主管
        try {
            MQSysUserSubMessage.SysUserSubMessage message = MQSysUserSubMessage.SysUserSubMessage.newBuilder()
                    .setUserId(userUnderling.getUserId())
                    .setSyncType(SyncTypeEnum.UPDATE.getValue())
                    .setType(User.USER_TYPE_SALES)
                    .addAllSubUserIds(userUnderling.getSubUserIds())
                    .build();
            sysUserSubSender.send(message);
        }catch (Exception e){
            log.error("发送消息队列删除表数据库失败.UserSalesAccountService.saveUserUnderling:{}",userUnderling.getUserId(),e.getMessage());
        }
    }

    public String getSubUserNames(UserUnderling userUnderling){
        List<String> userNames = Lists.newArrayList();
        User user;
        for(Long subUserId: userUnderling.getSubUserIds()){
            if (subUserId.equals(userUnderling.getUserId())) {
                continue;
            }
            user = UserUtils.get(subUserId);
            if(user != null && user.getName() != null){
                userNames.add(user.getName());
            }

        }

        return userNames.stream().map(String::valueOf).collect(Collectors.joining("，"));
    }

    public String getSubUserNamesByUserId(Long userId){
        List<String> userNames = Lists.newArrayList();
        List<UserSub> userSubList = userSubDao.getUserSubList(userId);
        User user;
        for(UserSub userSub :userSubList){
            if (userSub.getSubUserId().equals(userId)) {
                continue;
            }
            user = UserUtils.get(userSub.getSubUserId());
            if(user != null && user.getName() != null){
                userNames.add(user.getName());
            }
           // userNames.add(UserUtils.get(userSub.getSubUserId()).getName());
        }

        return userNames.stream().map(String::valueOf).collect(Collectors.joining("，"));
    }
}
