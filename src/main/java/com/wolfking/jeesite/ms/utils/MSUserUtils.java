package com.wolfking.jeesite.ms.utils;

import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.service.sys.MSUserService;

import java.util.List;
import java.util.Map;

/**
 * @author Zhoucy
 * @date 2018/9/25 11:17
 **/
public class MSUserUtils {

    private static MSUserService msUserService = SpringContextHolder.getBean(MSUserService.class);

    public static User get(Long id) {
        return msUserService.get(id);
    }

    public static String getName(Long id) {
        return msUserService.getName(id);
    }

    public static List<User> getListByUserType(Integer userType) {
        return msUserService.getListByUserType(userType);
    }

    public static Map<Long, User> getMapByUserType(Integer userType) {
        return msUserService.getMapByUserType(userType);
    }

    public static Map<Long, String> getNamesByUserType(Integer userType) {
        return msUserService.getNameByUserType(userType);
    }

    public static List<User> getListByUserIds(List<Long> userIds) {
        return msUserService.getListByUserIds(userIds);
    }

    public static Map<Long, User> getMapByUserIds(List<Long> userIds) {
        return msUserService.getMapByUserIds(userIds);
    }

    public static Map<Long, String> getNamesByUserIds(List<Long> userIds) {
        return msUserService.getNamesByUserIds(userIds);
    }

    public static void reloadUserToRedis(User user) {
        msUserService.reloadUserToRedis(user);
    }

    public static void addUserToRedis(User user) {
        msUserService.addUserToRedis(user);
    }

}
