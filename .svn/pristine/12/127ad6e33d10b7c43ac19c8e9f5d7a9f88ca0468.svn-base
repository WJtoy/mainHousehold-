package com.wolfking.jeesite.ms.xyingyan.handler;

import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class XYingYanSecurityHandler extends HandlerInterceptorAdapter {

    private final static String HEADER_KEY_APPKEY = "appKey";
    private final static String HEADER_KEY_APPSECRET = "appSecret";

    @Autowired
    private MicroServicesProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (properties.getXYingYan().getEnabled()) {
            String appKey = "";
            String appSecret = "";
            try {
                appKey = StringUtils.toString(request.getHeader(HEADER_KEY_APPKEY));
                appSecret = StringUtils.toString(request.getHeader(HEADER_KEY_APPSECRET));
            } catch (Exception e) {
                log.error("新迎燕下单模块权限验证", e);
            }
            if (appKey.equals(properties.getXYingYan().getAppKey()) &&
                    appSecret.equals(properties.getXYingYan().getAppSecret())) {
                return super.preHandle(request, response, handler);
            }
        }
        throw new Exception("非法请求,身份验证失败.");
    }
}
