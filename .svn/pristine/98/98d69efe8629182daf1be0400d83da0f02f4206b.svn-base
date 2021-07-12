package com.wolfking.jeesite.modules.sys.interceptor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.dao.Log2Dao;
import com.wolfking.jeesite.modules.sys.dao.MenuDao;
import com.wolfking.jeesite.modules.sys.entity.Log2;
import com.wolfking.jeesite.modules.sys.entity.Menu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by wolfking(赵伟伟)
 * Created on 2017/1/15 20:55
 * Mail zww199009@163.com
 */
@Component
@Slf4j
public class LogThread extends Thread {

    public static LinkedBlockingQueue<InterceptorLogEntity> interceptorLogQueue = new LinkedBlockingQueue<>();
    private static final String CACHE_MENU_NAME_PATH_MAP = "menuNamePathMap";

//    @Autowired
//    private Log2Dao log2Dao;  // mark on 2020-7-11

    @Autowired
    private MenuDao menuDao;
    @Autowired
    private RedisUtils redisUtils;

    public LogThread() {
        setDaemon(true); // 设置为后台线程,防止throw RuntimeExecption进程仍然存在的问题
    }

    public void run() {
        log.info("start the InterceptorLog  thread");
        while (true) {
            try {
                InterceptorLogEntity entiry = interceptorLogQueue.take();
                Log2 log2 = entiry.getLog();
                Exception ex = entiry.getEx();
                Object handler = entiry.getHandler();
                // 获取日志标题
                if (StringUtils.isBlank(log2.getTitle())) {
                    String permission = "";
                    if (handler instanceof HandlerMethod) {
                        Method m = ((HandlerMethod) handler).getMethod();
                        RequiresPermissions rp = m.getAnnotation(RequiresPermissions.class);
                        permission = (rp != null ? StringUtils.join(rp.value(), ",") : "");
                    }
                    log2.setTitle(getMenuNamePath(log2.getRequestUri(), permission));
                }
                // 如果有异常，设置异常信息
                log2.setException(Exceptions.getStackTraceAsString(ex));
                // 如果无标题并无异常日志，则不保存信息
                if (StringUtils.isBlank(log2.getTitle()) && StringUtils.isBlank(log2.getException()))
                    continue;
//                log.setId(UUID.randomUUID().toString());
                // log2Dao.insert(log2); //mark on 2020-7-11
            } catch (Exception e) {
                log.error("LogThread:{}", ExceptionUtils.getRootCauseMessage(e));
                //e.printStackTrace(System.out);
            }
        }
    }


    /**
     * 获取菜单名称路径（如：系统设置-机构用户-用户管理-编辑）
     */
    private String getMenuNamePath(String requestUri, String permission) {
        String href = StringUtils.substringAfter(requestUri, Global.getAdminPath());
        @SuppressWarnings("unchecked")
        //Map<String, String> menuMap = (Map<String, String>) CacheUtils.get(CACHE_MENU_NAME_PATH_MAP);
        //hash
        Map<String, byte[]> menuMaps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,CACHE_MENU_NAME_PATH_MAP);
        Map<String, Object> menuMap;
        if (menuMaps == null || menuMaps.size()==0) {
            menuMap = Maps.newHashMap();
            List<Menu> menuList = menuDao.findAllList(new Menu());
            for (Menu menu : menuList) {
                // 获取菜单名称路径（如：系统设置-机构用户-用户管理-编辑）
                String namePath = "";
                if (menu.getParentIds() != null) {
                    List<String> namePathList = Lists.newArrayList();
                    for (String id : StringUtils.split(menu.getParentIds(), ",")) {
                        if (Menu.getRootId().equals(id)) {
                            continue; // 过滤跟节点
                        }
                        for (Menu m : menuList) {
                            if (m.getId().equals(id)) {
                                namePathList.add(m.getName());
                                break;
                            }
                        }
                    }
                    namePathList.add(menu.getName());
                    namePath = StringUtils.join(namePathList, "-");
                }
                // 设置菜单名称路径
                if (StringUtils.isNotBlank(menu.getHref())) {
                    menuMap.put(menu.getHref(), namePath);
                } else if (StringUtils.isNotBlank(menu.getPermission())) {
                    for (String p : StringUtils.split(menu.getPermission())) {
                        menuMap.put(p, namePath);
                    }
                }

            }
            //CacheUtils.put(CACHE_MENU_NAME_PATH_MAP, menuMap);
            redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,CACHE_MENU_NAME_PATH_MAP,menuMap,0l);
        }else {
            menuMap = menuMaps.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> e.getValue().toString()
                    ));
        }
        Object menuNamePath = menuMap.get(href);
        if (menuNamePath == null) {
            for (String p : StringUtils.split(permission)) {
                menuNamePath = menuMap.get(p);
                if (StringUtils.isNotBlank(menuNamePath.toString()))
                    break;
            }
            if (menuNamePath == null)
                return "";
        }
        return menuNamePath.toString();
    }
}
