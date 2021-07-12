package com.wolfking.jeesite.modules.sys.interceptor;

import com.wolfking.jeesite.common.web.FormTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by wolfking(赵伟伟)
 * Created on 2017/1/15 20:21
 * Mail zww199009@163.com
 */
@Component
public class WebInterceptorConfigurer extends WebMvcConfigurerAdapter{
    /*单独线程异步保存日志到数据库(sys_log)
    @Autowired
    private LogInterceptor logInterceptor;
    @Autowired
    private LogThread logThread;
    */
    /*防止表单多次提交
    @Bean
    public FormTokenInterceptor getFormTokenInterceptor() {
        return new FormTokenInterceptor();
    }
    */

    //添加拦截器
    public void addInterceptors(InterceptorRegistry registry) {
        /*日志
        registry.addInterceptor(logInterceptor).addPathPatterns("/**").excludePathPatterns("/login");
        super.addInterceptors(registry);
        logThread.start();
        */

        /*防止表单多次提交
        InterceptorRegistration formTokenInterceptor = registry.addInterceptor(getFormTokenInterceptor());
        // 排除配置
        formTokenInterceptor.excludePathPatterns("/error**");
        formTokenInterceptor.excludePathPatterns("/login**");
        formTokenInterceptor.excludePathPatterns("/api/**");
        formTokenInterceptor.excludePathPatterns("/static/**");
        // 拦截配置
        formTokenInterceptor.addPathPatterns("/sd/*");
        formTokenInterceptor.addPathPatterns("/md/*");
        formTokenInterceptor.addPathPatterns("/fi/*");

        //使用
        1.在Controller需要产生token的方法加注解：
        @FormToken(save = true)
	    public String plan(String orderId,String quarter, HttpServletRequest request, Model model)
        2.在需要验证的页面，如orderPlanForm.jsp：
        <form>
        <input type="hidden" name="formToken" value="${formToken}" />
        ...
        </form
        3.在Controller提交的方法加注解：
        @FormToken(remove = true)
        public AjaxJsonEntity plan(Order order, HttpServletResponse response)
        */
    }
}
