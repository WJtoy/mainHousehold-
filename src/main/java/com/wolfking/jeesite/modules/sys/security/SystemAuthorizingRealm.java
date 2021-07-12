/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.security;

import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.sys.SysLoginLog;
import com.kkl.kklplus.entity.sys.SysUser;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.servlet.ValidateCodeServlet;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.sys.entity.Menu;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysUserWhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 系统安全认证实现类
 *
 * @author ThinkGem
 * @version 2014-7-5
 */
@Service
@Slf4j
public class SystemAuthorizingRealm extends AuthorizingRealm {

    // 外部用户类型
    private static Set<Integer> OUT_USER_TYPES = Sets.newHashSet(SysUser.USER_TYPE_CUSTOMER,SysUser.USER_TYPE_SUBCUSTOMER,SysUser.USER_TYPE_ENGINEER,SysUser.USER_TYPE_SEARCH_CUSTOMER);

    //@Autowired
    private SystemService systemService;

    @Autowired
    private RedisUtils redisUtils;

    //@Autowired
    //@Lazy
    //private ServicePointService servicePointService;

    @Autowired
    private MSSysUserWhiteListService msSysUserWhiteListService;

    //域名访问令牌
    @Value("${requestAccessToken.specialLine}")
    private String specialLineAccessToken;

    @Value("${requestAccessToken.domain}")
    private String domainAccessToken;

    /**
     * 认证信息.(身份验证) : Authentication 是用来验证用户身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
        if (!(authcToken instanceof UsernamePasswordToken)) {
            return null;
        }
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        //验证访问token
        if(StringUtils.isBlank(token.getAccessType())){
            throw new AuthenticationException("msg:非法请求.");
        }
        if(!specialLineAccessToken.equals(token.getAccessType()) && !domainAccessToken.equals(token.getAccessType())){
            throw new AuthenticationException("msg:非法请求.");
        }
		/*
		int activeSessionSize = getSystemService().getSessionDao().getActiveSessions(false).size();
		if (logger.isDebugEnabled()){
			logger.debug("login submit, active session size: {}, username: {}", activeSessionSize, token.getUsername());
		}
		*/
        // 校验登录验证码,如总是需要检验验证码，注释if
        if (UserUtils.isValidateCodeLogin(token.getUsername(), false, false)) {
            Session session = UserUtils.getSession();
            //String code = (String)session.getAttribute(ValidateCodeServlet.VALIDATE_CODE);
            String sessionId = session.getId().toString();
            String code = (String) redisUtils.get(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, String.format(ValidateCodeServlet.VALIDATE_CODE_KEY, sessionId), String.class);
            if (token.getCaptcha() == null || !token.getCaptcha().toUpperCase().equals(code)) {
                throw new AuthenticationException("msg:验证码错误.");
            }
        }
        String name = token.getUsername();
        String password = String.valueOf(token.getPassword());
        if (StringUtils.isBlank(password)) {
            throw new UnknownAccountException("msg:帐号或密码错误, 请重试.");
        }
        // 校验用户名密码
        User user = getSystemService().getUserByLoginName(token.getUsername());
        //User user = systemService.getUserByLoginNameFromDb(token.getUsername());
        if (user == null) {
            throw new UnknownAccountException("msg:帐号或密码错误, 请重试.");
        }
        //检查账号是否停用
        if(null != user.getStatusFlag() && 0 != user.getStatusFlag()){
            throw new AuthenticationException("msg:您的帐号已停用,请联系管理员！");
        }
        //禁止网点子帐号登录 2021-01-07
        //subFlag: 0-主账号 1-子账号 2-信息员
        if (user.getUserType() == User.USER_TYPE_ENGINEER && user.getSubFlag() == 1) {
            throw new AuthenticationException("msg:网点子帐号没有权限！");
        }
        //外部帐号，不能通过专线访问
        if(OUT_USER_TYPES.contains(user.getUserType()) && token.getAccessType().equals(specialLineAccessToken)){
            log.error("外部帐号[{}]  通过专线登录",user.getLoginName());
            throw new AuthenticationException("msg:您的帐号禁止使用专线登录.");
        }
        //内部帐号，通过外网访问，检测白名单
        if(!OUT_USER_TYPES.contains(user.getUserType()) && token.getAccessType().equals(domainAccessToken)) {
            boolean isWhite = msSysUserWhiteListService.IsWhiteUser(user.getId());
            if(!isWhite){
                log.error("内部非白名单帐号[{}]  通过域名登录",user.getLoginName());
                throw new AuthenticationException("msg:您的帐号禁止该域名登录, 请联系管理员.");
            }
        }
        if (!SystemService.validatePassword(password, user.getPassword())) {
            throw new UnknownAccountException("msg:帐号或密码错误, 请重试.");
        }

        byte[] salt = Encodes.decodeHex(user.getPassword().substring(0, 16));
        // 记录登录日志
        user.setLoginIp(token.getHost());
        saveLoginLog(user);
        return new SimpleAuthenticationInfo(
                new Principal(user, token.isMobileLogin()), //用户
                user.getPassword().substring(16), //密码
                ByteSource.Util.bytes(salt), //salt
                getName() //realm name
        );

    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(User user) {
        try {
            String ip = StringUtils.isBlank(user.getLoginIp())?"127.0.0.1":user.getLoginIp();
            user.setLoginIp(ip);
            LogUtils.saveLoginLog(SysLoginLog.LogType.LOGIN, SysLoginLog.ClientType.WEB,user,ip,"登录系统");
        } catch (Exception e) {
            log.error("[记录登录日志] 错误 - account:{} name:{} date:{}", user.getLoginName(), user.getName(),System.currentTimeMillis(), e);
        }
    }


    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Principal principal = (Principal) getAvailablePrincipal(principals);
        User user = getSystemService().getUser(Long.valueOf(principal.getId()));
        if (user != null) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            List<Menu> list = UserUtils.getMenuList();
            for (Menu menu : list) {
                if (StringUtils.isNotBlank(menu.getPermission())) {
                    // 添加基于Permission的权限信息
                    for (String permission : StringUtils.split(menu.getPermission(), ",")) {
                        info.addStringPermission(permission);
                    }
                }
            }
            // 添加用户权限
            info.addStringPermission("user");
            // 添加用户角色信息
            for (Role role : user.getRoleList()) {
                info.addRole(role.getEnname());
            }
            // 更新登录IP和时间
            // getSystemService().updateUserLoginInfo(user);
            // 记录登录日志
            //LogUtils.saveLog(Servlets.getRequest(), "系统登录");
            return info;
        } else {
            return null;
        }
    }

    @Override
    protected void checkPermission(Permission permission, AuthorizationInfo info) {
        authorizationValidate(permission);
        super.checkPermission(permission, info);
    }

    @Override
    protected boolean[] isPermitted(List<Permission> permissions, AuthorizationInfo info) {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
                authorizationValidate(permission);
            }
        }
        return super.isPermitted(permissions, info);
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        authorizationValidate(permission);
        return super.isPermitted(principals, permission);
    }

    @Override
    protected boolean isPermittedAll(Collection<Permission> permissions, AuthorizationInfo info) {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
                authorizationValidate(permission);
            }
        }
        return super.isPermittedAll(permissions, info);
    }

    /**
     * 授权验证方法
     *
     * @param permission
     */
    private void authorizationValidate(Permission permission) {
        // 模块授权预留接口
    }

    /**
     * 设定密码校验的Hash算法与迭代次数
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(SystemService.HASH_ALGORITHM);
        matcher.setHashIterations(SystemService.HASH_INTERATIONS);
        setCredentialsMatcher(matcher);
    }

    /**
     * 获取系统业务对象
     */
    public SystemService getSystemService() {
        if (systemService == null) {
            systemService = SpringContextHolder.getBean(SystemService.class);
        }
        return systemService;
    }

    /**
     * 授权用户信息
     */
    public static class Principal implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id; // 编号
        private String loginName; // 登录名
        private String name; // 姓名
        private boolean mobileLogin; // 是否手机登录

        public Principal(User user, boolean mobileLogin) {
            this.id = user.getId().toString();
            this.loginName = user.getLoginName();
            this.name = user.getName();
            this.mobileLogin = mobileLogin;
        }

        public String getId() {
            return id;
        }

        public String getLoginName() {
            return loginName;
        }

        public String getName() {
            return name;
        }

        public boolean isMobileLogin() {
            return mobileLogin;
        }

        /**
         * 获取SESSIONID
         */
        public String getSessionid() {
            try {
                return (String) UserUtils.getSession().getId();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        public String toString() {
            return id;
        }

    }
}
