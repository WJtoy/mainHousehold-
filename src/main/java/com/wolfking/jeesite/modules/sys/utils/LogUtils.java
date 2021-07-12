/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.utils;

import cn.hutool.extra.servlet.ServletUtil;
import com.kkl.kklplus.entity.sys.SysLog;
import com.kkl.kklplus.entity.sys.SysLoginLog;
import com.kkl.kklplus.entity.sys.mq.MQSysLogMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.mq.sender.LogSender;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * 字典工具类
 *
 * @author ThinkGem
 * @version 2014-11-7
 */
@Lazy(false)
@Component
@Slf4j
public class LogUtils {

    public static final String CACHE_MENU_NAME_PATH_MAP = "menuNamePathMap";

    @Autowired
    private LogSender logSender;

    private static LogUtils logUtils ;


    public void setLogSender(LogSender logSender) {
        this.logSender = logSender;
    }

    //通过init方法，
    //1.注入bean(logSender)
    //2.赋值给static LogUtils logUtils
    //3.使用logSender的时候，logUtils
    @PostConstruct
    public void init(){
        logUtils = this ;
        logUtils.logSender = this.logSender;
    }

    //public static LogUtils getLogUtils() {
    //    return logUtils;
    //}

    /*
    public static String getLogProcessInfo(){
        return String.format("Queue Size:%s,Thread activeCount:%s",LogThread.interceptorLogQueue.size(),LogThread.activeCount());
    }

    public static String getLogThreadList(){
        ThreadGroup group = LogThread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slackList = new Thread[estimatedSize];
        int actualSize = topGroup.enumerate(slackList);
        Thread[] list = new Thread[actualSize];
        System.arraycopy(slackList, 0, list, 0, actualSize);
        //System.out.println("Thread list size == " + list.length);
        StringBuffer sbThread = new StringBuffer(1000);
        sbThread.append("Thread List size:" + list.length).append(System.lineSeparator());
        for (Thread thread : list) {
            sbThread.append(String.valueOf(thread.getId())).append(" - ").append(thread.getName()).append(System.lineSeparator());
        }
        return sbThread.toString();
    }
    */

    /**
     * 保存日志
     */
    public static void saveLog(HttpServletRequest request, String title) {
        saveLog(request, null, null, title);
    }

    /**
     * 保存日志
     */
    public static void saveLog(HttpServletRequest request, Object handler, Exception ex, String title) {
        try{
            String params = getRequestParameterString(request.getParameterMap());
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr(ServletUtil.getClientIP(request))
                    .setRequestUri(StringUtils.left(request.getRequestURI(),250))
                    .setMethod(request.getMethod())
                    .setParams(params)
                    .setUserAgent(StringUtils.left(request.getHeader("user-agent"),250))
                    .setException(ex==null?"": Exceptions.getStackTraceAsString(ex))
                    .setCreateBy(0)
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,uri:{} ",title,request.getRequestURI(),e);
        }
    }

    /**
     * 保存日志
     */
    public static void saveLog(HttpServletRequest request, Object handler, Exception ex, String title,String params) {
        try{
            if(StringUtils.isBlank(params)) {
                params = getRequestParameterString(request.getParameterMap());
            }
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr(ServletUtil.getClientIP(request))
                    .setRequestUri(StringUtils.left(request.getRequestURI(),250))
                    .setMethod(request.getMethod())
                    .setParams(params)
                    .setUserAgent(StringUtils.left(request.getHeader("user-agent"),250))
                    .setException(ex==null?"": Exceptions.getStackTraceAsString(ex))
                    .setCreateBy(0)
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,uri:{} ",title,request.getRequestURI(),e);
        }
    }

    /**
     * 保存日志
     */
    public static void saveLog(HttpServletRequest request, Object handler, Exception ex, String title,String params,User user) {
        long userId = (user==null || user.getId() == null)?0:user.getId();
        try{
            if(StringUtils.isBlank(params)) {
                params = getRequestParameterString(request.getParameterMap());
            }
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr(ServletUtil.getClientIP(request))
                    .setRequestUri(StringUtils.left(request.getRequestURI(),250))
                    .setMethod(request.getMethod())
                    .setParams(params)
                    .setUserAgent(StringUtils.left(request.getHeader("user-agent"),250))
                    .setException(ex==null?"": Exceptions.getStackTraceAsString(ex))
                    .setCreateBy(userId)
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,user:{} ,uri:{}",title,userId,request.getRequestURI(),e);
        }
    }

    /**
     * 保存日志
     */
    public static void saveLog(HttpServletRequest request, Object handler, Exception ex, String title,String method,String params,User user) {
        long userId = (user == null || user.getId() == null) ? 0 : user.getId();
        try {
            if (StringUtils.isBlank(params)) {
                params = getRequestParameterString(request.getParameterMap());
            }
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr(ServletUtil.getClientIP(request))
                    .setRequestUri(StringUtils.left(request.getRequestURI(),250))
                    .setMethod(method)
                    .setParams(params)
                    .setUserAgent(StringUtils.left(request.getHeader("user-agent"),250))
                    .setException(ex == null ? "" : Exceptions.getStackTraceAsString(ex))
                    .setCreateBy(userId)
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,user:{} ,uri:{} ",title,userId,request.getRequestURI(),e);
        }
    }

    /**
     * 保存日志
     */
    public static void saveLog(HttpServletRequest request, Object handler, Exception ex, String title,User user) {
        long userId = (user == null || user.getId() == null) ? 0 : user.getId();
        try {
            String params = getRequestParameterString(request.getParameterMap());
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr(ServletUtil.getClientIP(request))
                    .setRequestUri(StringUtils.left(request.getRequestURI(),250))
                    .setMethod(request.getMethod())
                    .setParams(params)
                    .setUserAgent(StringUtils.left(request.getHeader("user-agent"),250))
                    .setException(ex == null ? "" : Exceptions.getStackTraceAsString(ex))
                    .setCreateBy(userId)
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e) {
            log.error("[LogUtils.saveLog] title:{} ,uri:{} ,user:{}", title, request.getRequestURI(), userId, e);
        }
    }

    /**
     * 保存日志
     */
    public static void saveLog(String title,String method, String params, Exception ex,User user) {
        long userId = (user == null || user.getId() == null) ? 0 : user.getId();
        try {
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr("127.0.0.1")
                    .setRequestUri(StringUtils.left(method,250))
                    .setMethod("Log")
                    .setParams(params)
                    .setUserAgent("Web")
                    .setException(ex == null ? "" : Exceptions.getStackTraceAsString(ex))
                    .setCreateBy((user == null || user.getId() == null) ? 0 : user.getId())
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,method:{} ,user:{}",title,method,userId,e);
        }
    }

    /**
     * 保存日志
     */
    public static void saveLog(String title,String method, String params, Throwable ex,User user) {
        long userId = (user == null || user.getId() == null) ? 0 : user.getId();
        try {
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(ex == null ? SysLog.TYPE_ACCESS : SysLog.TYPE_EXCEPTION)
                    .setTitle(title)
                    .setRemoteAddr("127.0.0.1")
                    .setRequestUri(StringUtils.left(method,250))
                    .setMethod("Log")
                    .setParams(params)
                    .setUserAgent("Web")
                    .setException(ex == null ? "" : Exceptions.getStackTraceAsString(ex))
                    .setCreateBy((user == null || user.getId() == null) ? 0 : user.getId())
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,method:{} ,user:{}",title,method,userId,e);
        }
    }

    public static void saveLog(String title,String method, String params, Exception ex,User user,Integer type) {
        long userId = (user == null || user.getId() == null) ? 0 : user.getId();
        try {
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(type)
                    .setTitle(title)
                    .setRemoteAddr("127.0.0.1")
                    .setRequestUri(StringUtils.left(method,250))
                    .setMethod("Log")
                    .setParams(params)
                    .setUserAgent("Web")
                    .setException(ex == null ? "" : Exceptions.getStackTraceAsString(ex))
                    .setCreateBy(userId)
                    .setCreateDate(System.currentTimeMillis())
                    .setQuarter(QuarterUtils.getSeasonQuarter(new Date()))
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLog] title:{} ,method:{} ,user:{}",title,method,userId,e);
        }
    }

    /**
     * 登录日志
     * @param logType   日志类型
     * @param clientType    客户端类型
     * @param user      用户
     * @param ip        ip地址
     * @param content   日志内容
     */
    public static void saveLoginLog(SysLoginLog.LogType logType,SysLoginLog.ClientType clientType, User user, String ip, String content) {
        long userId = (user == null || user.getId() == null) ? 0 : user.getId();
        try {
            //用户类型
            Integer userType = SysLoginLog.UserType.INNER.getCode();
            if(user.isCustomer()){
                userType = SysLoginLog.UserType.CUSTOMER.getCode();
            }else if(user.isEngineer()){
                userType = SysLoginLog.UserType.SERVICE_POINT.getCode();
            }
            MQSysLogMessage.SysLogMessage logMessage = MQSysLogMessage.SysLogMessage.newBuilder()
                    .setType(userType)
                    .setTitle(StringUtils.EMPTY)
                    .setRemoteAddr(ip)
                    .setUserAgent(StringUtils.EMPTY)
                    .setMethod("Login")//固定
                    .setParams(content) // log content
                    .setException(StringUtils.EMPTY)
                    .setCreateBy(userId)
                    .setRequestUri(userId ==0?"":user.getName())//userName
                    .setCreateDate(System.currentTimeMillis())
                    .build();
            logUtils.logSender.send(logMessage);
        }catch (Exception e){
            log.error("[LogUtils.saveLoginLog] logType:{} ,clientType:{} ,user:{}",logType.getType(),clientType.getType(),userId,e);
        }
    }

    /**
     * 将web请求参数转成字符形式，如a=1&b=2&c=3
     * @param map
     * @return
     */
    public static String getRequestParameterString(Map<String, String[]> map){
        if(map == null || map.size() == 0){
            return "";
        }
        StringBuilder params = new StringBuilder();
        map.forEach((k,v)->{
            if(v.length>0) {
                params.append("&").append(k).append("=").append(URLEncoder.encode(v[0]));
            }else{
                params.append("&").append(k).append("=");
            }

        });
        return params.toString().substring(1);
    }

}
