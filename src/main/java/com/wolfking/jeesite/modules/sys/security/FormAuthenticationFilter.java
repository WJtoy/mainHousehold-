/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.security;

import cn.hutool.extra.servlet.ServletUtil;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 表单验证（包含验证码）过滤类
 * @author ThinkGem
 * @version 2014-5-19
 */
@Service
public class FormAuthenticationFilter extends org.apache.shiro.web.filter.authc.FormAuthenticationFilter {

	public static final String DEFAULT_CAPTCHA_PARAM = "validateCode";
	public static final String DEFAULT_MOBILE_PARAM = "mobileLogin";
	public static final String DEFAULT_MESSAGE_PARAM = "message";

	private String captchaParam = DEFAULT_CAPTCHA_PARAM;
	private String mobileLoginParam = DEFAULT_MOBILE_PARAM;
	private String messageParam = DEFAULT_MESSAGE_PARAM;

	@Autowired
	RedisUtils redisUtils;

	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
		String username = getUsername(request);
		String password = getPassword(request);
		if (password==null){
			password = "";
		}
		boolean rememberMe = isRememberMe(request);
		//String host = StringUtils.getRemoteAddr((HttpServletRequest)request);
		String host = ServletUtil.getClientIP((HttpServletRequest)request);
		String captcha = getCaptcha(request);
		boolean mobile = isMobileLogin(request);
		String accessType = ((HttpServletRequest) request).getHeader("kkl");
		accessType = "www";
		return new UsernamePasswordToken(username, password.toCharArray(), rememberMe, host, captcha, mobile,StringUtils.trimToEmpty(accessType));
	}

	public String getCaptchaParam() {
		return captchaParam;
	}

	protected String getCaptcha(ServletRequest request) {
		return WebUtils.getCleanParam(request, getCaptchaParam());
	}

	public String getMobileLoginParam() {
		return mobileLoginParam;
	}
	
	protected boolean isMobileLogin(ServletRequest request) {
        return WebUtils.isTrue(request, getMobileLoginParam());
    }
	
	public String getMessageParam() {
		return messageParam;
	}
	
	/**
	 * 登录成功之后跳转URL
	 */
	public String getSuccessUrl() {
		return super.getSuccessUrl();
	}

	/**
	 * 登录成功后调用
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Override
	protected void issueSuccessRedirect(ServletRequest request,
			ServletResponse response) throws Exception {

		Session session = UserUtils.getSession();
		if(session==null || session.getId()==null){
			return;
		}
			User user = UserUtils.getUser();
		if (user !=null && user.getId() !=null){
			//String key = String.format(RedisConstant.SHIRO_USER_SESSION,user.getId());
			//redisUtils.set(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,key,session.getId().toString(), 60* RedisValueCache.globExpire);
			UserUtils.loadUserInfo(user,session.getId().toString(),null);//装载customer,区域等
		}
		// 更新登录IP和时间
//		String ip = StringUtils.getIp((HttpServletRequest) request);
//		user.setLoginIp(ip);
//		systemService.updateUserLoginInfo(user);

		//login log
//		LogUtils.saveLog((HttpServletRequest) request, null, null,"登录成功",  user.getLoginName(),user);
//		if (p != null && !p.isMobileLogin()){
			 WebUtils.issueRedirect(request, response, getSuccessUrl(), null, true);
//		}else{
//			super.issueSuccessRedirect(request, response);
//		}
	}

	/**
	 * 登录失败调用事件
	 */
	@Override
	protected boolean onLoginFailure(AuthenticationToken token,
			AuthenticationException e, ServletRequest request, ServletResponse response) {
		String className = e.getClass().getName(), message = "";
		if (IncorrectCredentialsException.class.getName().equals(className)
				|| UnknownAccountException.class.getName().equals(className)){
			message = "用户或密码错误, 请重试.";
		} else if (e.getMessage() != null && StringUtils.startsWith(e.getMessage(), "msg:")){
			message = StringUtils.replace(e.getMessage(), "msg:", "");
		} else{
			message = "系统出现点问题，请稍后再试！";
			e.printStackTrace(); // 输出到控制台
		}
        //request.setAttribute(getFailureKeyAttribute(), className);
        request.setAttribute(getMessageParam(), message);
		/*
        if(token instanceof  UsernamePasswordToken) {
			LogUtils.saveLog((HttpServletRequest) request, null, null,"登录失败",  ((UsernamePasswordToken) token).getUsername(),null);
		}
		*/
        return true;
	}

    /*
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                return executeLogin(request, response);
            } else {
                // allow them to see the login page ;)
                return true;
                ////判断session里是否有用户信息,去掉注释，出现：ERR_TOO_MANY_REDIRECTS localhost 将您重定向的次数过多
                //if(Servlets.isAjax(request)){
                //    httpResponse.sendError(HttpStatus.UNAUTHORIZED.value());
                //    httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                //} else {
                //    redirectToLogin(request, response);
                //}
                //return false;

            }

        } else {
            // 判断session里是否有用户信息
            if(Servlets.isAjax(request)){
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            } else {
                redirectToLogin(request, response);
            }
            return false;
        }

        //if(Servlets.isAjax(request)){
		//	((HttpServletResponse)response).sendError(HttpStatus.UNAUTHORIZED.value());//401
		//}else{
		//	issueSuccessRedirect(request,response);
		//}
		//return false;
	}
    */
}