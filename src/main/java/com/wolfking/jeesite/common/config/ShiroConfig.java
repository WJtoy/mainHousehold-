package com.wolfking.jeesite.common.config;

import com.wolfking.jeesite.common.security.shiro.cache.RedisCacheManager;
import com.wolfking.jeesite.common.security.shiro.session.CacheSessionDAO;
import com.wolfking.jeesite.common.security.shiro.session.SessionManager;
import com.wolfking.jeesite.modules.sys.security.SystemAuthorizingRealm;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.DispatcherType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro的控制类
 * 下面方法的顺序不能乱
 * Created by zhao.weiwei
 * create on 2017/1/11 10:59
 * the email is zhao.weiwei@jyall.com.
 */
@Component
//@Configuration
public class ShiroConfig {

    //public final static Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
    //public final static String anonPaths = "/userfiles/|/uploads/|/static/|/druid/|/api/|/fi/customercurrency/async|/error/|/servlet/validateCodeServlet|/td/";
    public final static String anonPaths = "/userfiles/|/uploads/|/static/|/api/|/fi/customercurrency/async|/error/|/servlet/validateCodeServlet|/td/|/ms/xyingyan/|/ms/b2bCenter/";
//    public final static String anonPaths = "/userfiles/|/uploads/|/static/|/api/|/fi/customercurrency/async|/error/|/servlet/validateCodeServlet|/td/";
    //static {
    //    // 配置退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
    //    filterChainDefinitionMap.put("/logout", "logout");
    //    // <!-- 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
    //    // <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
    //    filterChainDefinitionMap.put("/login", "anon");//anon 可以理解为不拦截
    //    filterChainDefinitionMap.put("/userfiles/**", "anon");//用户文件
    //    filterChainDefinitionMap.put("/uploads/**", "anon");//上传文件
    //    filterChainDefinitionMap.put("/static/**", "anon");//静态资源
    //    filterChainDefinitionMap.put("/druid/**", "anon");//druid
    //    filterChainDefinitionMap.put("/api/**", "anon");//rest api
    //    filterChainDefinitionMap.put("/fi/customercurrency/async","anon");//支付宝异步通知
    //    filterChainDefinitionMap.put("/error/**", "anon");
    //    filterChainDefinitionMap.put("/servlet/validateCodeServlet", "anon");//验证码
    //    filterChainDefinitionMap.put("/td/**", "anon");//第三方接口，如短信
    //    //测试时使用
    //    filterChainDefinitionMap.put("/sd/test/**","anon");
    //}

    /**
     * 全局的环境变量的设置
     * shiro的拦截

    @Bean(name = "shiroFilterChainDefinitions")
    public String shiroFilterChainDefinitions(Environment environment, @Value("${adminPath}") String adminPath) {
        // Global.resolver = new RelaxedPropertyResolver(environment);
        String string = "/static/** = anon\n";
        string += "/error/** = anon\n";
        string += "/userfiles/** = anon\n";
        string += "/uploads/** = anon\n";
        string += adminPath + "/druid/** = anon\n";//druid
        string += adminPath + "/api/** = anon\n";//rest api
        string += adminPath + "/basic = basic\n";
        string += adminPath + "/servlet/validateCodeServlet = anon\n";
        string += adminPath + "/td/** = anon\n";//第三方接口
        string += adminPath + "/login = authc\n";
        string += adminPath + "/logout = logout\n";
        string += adminPath + "/** = user";

        return string;
    }
     */

    @Bean(name = "basicHttpAuthenticationFilter")
    public BasicHttpAuthenticationFilter casFilter(@Value("${adminPath:/a}") String adminPath) {
        BasicHttpAuthenticationFilter basicHttpAuthenticationFilter = new BasicHttpAuthenticationFilter();
        basicHttpAuthenticationFilter.setLoginUrl(adminPath + "/login");
        return basicHttpAuthenticationFilter;
    }


    @Bean(name="shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, Environment environment) {

        Global.resolver = new RelaxedPropertyResolver(environment);

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        // 未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("error/403");
//        System.out.println("Shiro拦截器工厂类注入开始");

        // 拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

        // 配置退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
        // authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问


        //资源
        //filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/userfiles/**", "anon");
        filterChainDefinitionMap.put("/uploads/**", "anon");
        filterChainDefinitionMap.put("/static/**", "anon");

        //filterChainDefinitionMap.put("/druid/**", "anon");//druid
        filterChainDefinitionMap.put("/api/**", "anon");//rest api
        //spring cloud
        filterChainDefinitionMap.put("/info", "anon");
        filterChainDefinitionMap.put("/env/**", "anon");
        filterChainDefinitionMap.put("/error", "anon");
        filterChainDefinitionMap.put("/health", "anon");
        filterChainDefinitionMap.put("/hystrix.stream", "anon");
        filterChainDefinitionMap.put("/metrics/**", "anon");
        filterChainDefinitionMap.put("/trace/**", "anon");
        filterChainDefinitionMap.put("/threads/**", "anon");
        //
        filterChainDefinitionMap.put("/fi/customercurrency/async","anon");
        //filterChainDefinitionMap.put("/fi/customercurrency/test12356","anon");
        filterChainDefinitionMap.put("/sd/test/**","anon");

        filterChainDefinitionMap.put("/error/**", "anon");

        filterChainDefinitionMap.put("/servlet/validateCodeServlet", "anon");
        filterChainDefinitionMap.put("/td/**", "anon");
        // 退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
//        filterChainDefinitionMap.put("/logout", "logout");

        filterChainDefinitionMap.put("/ms/xyingyan/**", "anon");//供新迎燕微服务调用

        filterChainDefinitionMap.put("/ms/b2bCenter/**", "anon");//生成工单号

        filterChainDefinitionMap.put("/**", "authc"); // authc
//        filterChainDefinitionMap.put("admin/**", "authc, roles[admin]");
//        filterChainDefinitionMap.put("docs/**", "authc, perms[document:read]");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
//        System.out.println("Shiro拦截器工厂类注入成功");
        return shiroFilterFactoryBean;
    }

    /**
     * 安全认证过滤器

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            @Value("${adminPath:/a}") String adminPath,
            BasicHttpAuthenticationFilter basicHttpAuthenticationFilter,
            FormAuthenticationFilter formAuthenticationFilter,
            DefaultWebSecurityManager securityManager,
            @Qualifier("shiroFilterChainDefinitions") String shiroFilterChainDefinitions) {
        Map<String, Filter> filters = new HashMap<>();
//        filters.put("anon", new AnonymousFilter());
//        filters.put("perms", urlPermissionsFilter());
        filters.put("basic", basicHttpAuthenticationFilter);
        filters.put("authc", formAuthenticationFilter);

        //SystemLogoutFilter logoutFilter = new SystemLogoutFilter();
//        logoutFilter.setRedirectUrl("/login");
        //filters.put("logout", logoutFilter);

        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setFilters(filters);

        bean.setSecurityManager(securityManager);
        bean.setLoginUrl(adminPath + "/login");
        bean.setSuccessUrl(adminPath + "/index");
        bean.setUnauthorizedUrl(adminPath+"/error/403");
        bean.setFilterChainDefinitions(shiroFilterChainDefinitions);
        return bean;
    }
     */

    /*
    @Bean
    public URLPermissionsFilter urlPermissionsFilter() {
        return new URLPermissionsFilter();
    }
    */

    /*
    @Bean(name = "shiroCacheManager")
    public EhCacheManager shiroCacheManager(CacheManager manager) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(manager);
        return ehCacheManager;
    }
    */

    @Bean(name = "sessionManager")
    public SessionManager sessionManager(CacheSessionDAO dao) {
        SessionManager sessionManager = new SessionManager();
        sessionManager.setSessionDAO(dao);
        sessionManager.setGlobalSessionTimeout(4*3600000);//new:4h = 4x60mx60sx1000ms
        sessionManager.setSessionValidationInterval(900000);//15分钟检查一次session是否过期(SessionValidationSchedulerEnabled==true有效)
        sessionManager.setSessionValidationSchedulerEnabled(false);//是否开启检查,使用redis的过期机制
        sessionManager.setSessionIdCookie(getSimpleCookie());
        sessionManager.setSessionIdUrlRewritingEnabled(false);//去掉 JSESSIONID
//        sessionManager.setSessionIdCookie(new SimpleCookie("web.session.id"));
        sessionManager.setSessionIdCookieEnabled(true);
        return sessionManager;
    }

    /**
     * 指定本系统SESSIONID, 默认为: JSESSIONID 问题: 与SERVLET容器名冲突, 如JETTY, TOMCAT 等默认JSESSIONID,
     * 当跳出SHIRO SERVLET时如ERROR-PAGE容器会为JSESSIONID重新分配值导致登录会话丢失!
     * @return
     */
    @Bean
    public SimpleCookie getSimpleCookie() {
        SimpleCookie cookie = new SimpleCookie("web.session.id");
        cookie.setHttpOnly(true);
        return cookie;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(
            SystemAuthorizingRealm systemAuthorizingRealm,
            SessionManager sessionManager,
            RedisCacheManager redisCacheManager) {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setSessionManager(sessionManager);
        defaultWebSecurityManager.setCacheManager(redisCacheManager);
        defaultWebSecurityManager.setRealm(systemAuthorizingRealm);
        defaultWebSecurityManager.setRememberMeManager(rememberMeManager());
        return defaultWebSecurityManager;
    }

    /**
     * cookie对象;
     * @return
     */
    public SimpleCookie rememberMeCookie(){
        //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //<!-- 记住我cookie生效时间30天 ,单位秒;-->
        simpleCookie.setMaxAge(2592000);
        return simpleCookie;
    }

    @Bean
    public RememberMeManager rememberMeManager() {
        RememberMeManager rememberMe = new CookieRememberMeManager();
        return rememberMe;
    }

    /*
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
        cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }*/


    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            DefaultWebSecurityManager defaultWebSecurityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(defaultWebSecurityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setDispatcherTypes(DispatcherType.REQUEST,DispatcherType.ERROR,DispatcherType.FORWARD,DispatcherType.INCLUDE);
        return filterRegistration;
    }


    /**
     * 保证实现了Shiro内部lifecycle函数的bean执行
     * @return
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * AOP式方法级权限检查
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }
}
