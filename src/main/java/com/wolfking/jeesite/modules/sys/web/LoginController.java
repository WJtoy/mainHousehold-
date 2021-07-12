/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.web;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.servlet.ValidateCodeServlet;
import com.wolfking.jeesite.common.utils.CookieUtils;
import com.wolfking.jeesite.common.utils.IdGen;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.api.config.Constant;
import com.wolfking.jeesite.modules.api.util.JwtUtil;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.security.FormAuthenticationFilter;
import com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm;
import com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm.Principal;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录Controller
 *
 * @author ThinkGem
 * @version 2013-5-31
 */
@Controller
public class LoginController extends BaseController {

    /*
    @Autowired
    private SessionDAO sessionDAO;
    */

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 管理登录
     */
    @RequestMapping(value = "${adminPath}/login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
        Principal principal = UserUtils.getPrincipal();
        // 默认关闭页签模式
        String tabmode = CookieUtils.getCookie(request, "tabmode");
        if (tabmode == null) {
            CookieUtils.setCookie(response, "tabmode", "0");
        }
//        if (logger.isDebugEnabled()) {
//            logger.debug("login, active session size: {}", sessionDAO.getActiveSessions(false).size());
//        }
        // 如果已经登录，则跳转到管理首页
        if(principal !=null && !principal.isMobileLogin()){
            String key = String.format(RedisConstant.SYS_USER_ID,principal.getId());
            if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key)){
                return "redirect:" + adminPath + "/index";
            }
        }
        // 如果已经登录，则跳转到管理首页
//        if (principal != null && !principal.isMobileLogin())
//            return "redirect:" + adminPath + "/index";
        return "modules/sys/sysLogin";
    }

    /**
     * 登出
     */
    @RequestMapping(value = "${adminPath}/exit", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
        Subject subject = SecurityUtils.getSubject();
        try {
            SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal)subject.getPrincipal();
            Long id = null;
            if (principal!=null) {
                id = Long.valueOf(principal.getId());
            }
            String sessionKey = String.format("shiro-cache:activeSessionsCache:%s",subject.getSession().getId());
            //subject.logout();
            try {
                //clear cookie & session
                CookieUtils.setCookie(response,"web.session.id","",0);//cookie清除
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,sessionKey);
                //clear user cache
                if (id != null) {
                    User user = UserUtils.get(id,subject.getSession().getId().toString());
                    UserUtils.clearCache(user);
                }
//                subject.logout();
            }catch (Exception e){
                try{
                    CookieUtils.setCookie(response,"web.session.id","",0);//cookie清除
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,sessionKey);
                }catch (Exception e1){

                }
            }

        } catch (SessionException ise) {
            ise.printStackTrace();
        }
            return "redirect:" + adminPath + "/login#";

    }

    /**
     * 登录失败，真正登录的POST请求由Filter完成
     */
    @RequestMapping(value = "${adminPath}/login", method = RequestMethod.POST)
    public String loginFail(HttpServletRequest request, HttpServletResponse response, Model model) {
        Principal principal = UserUtils.getPrincipal();

        // 如果已经登录，则跳转到管理首页
        if (principal != null) {
            return "redirect:" + adminPath + "/index";
        }

        String username = WebUtils.getCleanParam(request, FormAuthenticationFilter.DEFAULT_USERNAME_PARAM);
        boolean rememberMe = WebUtils.isTrue(request, FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM);
        boolean mobile = WebUtils.isTrue(request, FormAuthenticationFilter.DEFAULT_MOBILE_PARAM);
        String exception = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        String message = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM);

        if (StringUtils.isBlank(message) || StringUtils.equals(message, "null")) {
            message = "用户或密码错误, 请重试.";
        }

        model.addAttribute(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM, username);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM, rememberMe);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_MOBILE_PARAM, mobile);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME, exception);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM, message);

//        if (logger.isDebugEnabled()) {
//            logger.debug("login fail, active session size: {}, message: {}, exception: {}",
//                    sessionDAO.getActiveSessions(false).size(), message, exception);
//        }
        /*始终开启登录验证码
        model.addAttribute("isValidateCodeLogin",true);
        */
        /* 非授权异常，登录失败，验证码加1。*/
        //if (!UnauthorizedException.class.getName().equals(exception) && !UnknownAccountException.class.getName().equals(exception)) {
        if (!UnauthorizedException.class.getName().equals(exception)) {
            model.addAttribute("isValidateCodeLogin", UserUtils.isValidateCodeLogin(username, true, false));
        }

        // 验证失败清空验证码
        request.getSession().setAttribute(ValidateCodeServlet.VALIDATE_CODE, IdGen.uuid());

        // 如果是手机登录，则返回JSON字符串
        if (mobile) {
            return renderString(response, model);
        }

        return "modules/sys/sysLogin";
    }

    /**
     * 登录成功，进入管理首页
     */
    @RequiresPermissions("user")
//    @RequestMapping(value = "${adminPath}/")
    @RequestMapping(value = {"${adminPath}/index", "${adminPath}"})
    public String index(HttpServletRequest request, HttpServletResponse response) {
        Principal principal = UserUtils.getPrincipal();

        /* 登录成功后，验证码计算器清零 */
        UserUtils.isValidateCodeLogin(principal.getLoginName(), false, true);
        // 2019-05-09 for im站内即时消息
        User user = UserUtils.getUser();
        String session = UserUtils.getSessionId();
        String token = UserUtils.getIMToken(user.getId(),session,user.getUserType());
        request.setAttribute("token", token);

        // 如果是手机登录，则返回JSON字符串
        if (principal.isMobileLogin()) {
            if (request.getParameter("login") != null) {
                return renderString(response, principal);
            }
            if (request.getParameter("index") != null) {
                return "modules/sys/sysIndex";
            }
            return "redirect:" + adminPath + "/login#";
        }
        return "modules/sys/sysIndex";
    }

    /**
     * 获取主题方案
     */
    @RequestMapping(value = "/theme/{theme}")
    public String getThemeInCookie(@PathVariable String theme, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isNotBlank(theme)) {
            CookieUtils.setCookie(response, "theme", theme);
        } else {
            theme = CookieUtils.getCookie(request, "theme");
        }
        return "redirect:" + request.getParameter("url");
    }


     
}
