/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.interceptor;

import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 日志拦截器
 *
 * @author ThinkGem
 * @version 2014-8-19
 */
@Component("controllerLogInterceptor")
@Slf4j
public class LogInterceptor extends BaseService implements HandlerInterceptor {

    //private static final ThreadLocal<Long> startTimeThreadLocal =
    //        new NamedThreadLocal<Long>("ThreadLocal StartTime");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        //startTimeThreadLocal.set(System.currentTimeMillis());
        /* 输出请求参数
        if(request instanceof ShiroHttpServletRequest){
            ShiroHttpServletRequest httpRequest = (ShiroHttpServletRequest)request;
            log.info("shiro url:{}",request.getRequestURI());
            httpRequest.getParameterMap().forEach((k,v) ->{
                log.info("key:{} ,value:{}",k,v);
            });
        }else{
            log.info("url:{}",request.getRequestURI());
            request.getParameterMap().forEach((k,v) ->{
                log.info("key:{} ,value:{}",k,v);
            });
        }*/
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        //logger.info("URI: {},耗时：{}   ",
        //        request.getRequestURI(), DateUtils.formatDateTime(System.currentTimeMillis() - startTimeThreadLocal.get()));
        //删除线程变量中的数据，防止内存泄漏
        //startTimeThreadLocal.remove();
        //只有异常的日志保存
        if(ex != null){
            // 保存日志
            LogUtils.saveLog(request, handler, ex, "LogInterceptor捕获异常");
        }
    }
}
