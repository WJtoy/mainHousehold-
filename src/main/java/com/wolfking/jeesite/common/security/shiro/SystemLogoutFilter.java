package com.wolfking.jeesite.common.security.shiro;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 登出
 * Created by Ryan
 */
public class SystemLogoutFilter extends LogoutFilter {

    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        //在这里执行退出系统前需要清空的数据
//        System.out.println("logout filter");
        Subject subject = getSubject(request, response);
        String redirectUrl = getRedirectUrl(request, response, subject);
//        System.out.println("redirectUrl:" + redirectUrl);
        try {
            SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal)subject.getPrincipal();
            Long id = null;
            if (principal!=null) {
                id = Long.valueOf(principal.getId());
            }
            String key = String.format("shiro-cache:activeSessionsCache:%s",subject.getSession().getId());
            subject.logout();
            //clear cache
            if(id !=null) {
//                System.out.println("clear shiro cache:"+id);
                User user = UserUtils.get(id,subject.getSession().getId().toString());
                UserUtils.clearCache(user);
                /*remove session
                if(StringUtils.isNoneBlank(key)) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key);
                }*/
            }
        } catch (SessionException ise) {
            ise.printStackTrace();
        }

        issueRedirect(request, response, redirectUrl);

        // 返回false表示不执行后续的过滤器，直接返回跳转到登录页面

        return false;

    }
}
