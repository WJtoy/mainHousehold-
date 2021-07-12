package com.wolfking.jeesite.common.security.shiro;

import com.wolfking.jeesite.common.web.Servlets;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class URLPermissionsFilter extends PermissionsAuthorizationFilter {

//	@Autowired
//	private UserService userService;

	@Override
	public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws IOException {
		String curUrl = getRequestUrl(request);
		//System.out.println("perms url :" + curUrl);//请求地址
		Subject subject = SecurityUtils.getSubject();
		/*
		if (subject.getPrincipal() == null
				|| StringUtils.endsWithAny(curUrl, ".js", ".css", ".html")
				|| StringUtils.endsWithAny(curUrl, ".jpg", ".png", ".gif", ".jpeg")
				|| StringUtils.endsWithAny(curUrl,"validateCodeServlet")
				|| StringUtils.contains(curUrl, "/uploads/")
				|| StringUtils.contains(curUrl, "/error/")
				|| StringUtils.endsWith(curUrl, "/error")
				|| StringUtils.contains(curUrl, "/static/")
				|| StringUtils.contains(curUrl, "/userfiles/")
				|| StringUtils.contains(curUrl, "/td/")
				) {
			return true;
		}
		*/
		//Pattern p = Pattern.compile(".*?"+ ShiroConfig.anonPaths,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		if (subject.getPrincipal() == null || Servlets.isAnonRequest(curUrl)) {
			return true;
		}
		//System.out.println("需验证地址");
		//List<String> urls = userService.findPermissionUrl(subject.getPrincipal().toString());
		//return urls.contains(curUrl);
		return false;
	}

	/**
	 * 获取当前URL+Parameter
	 * 
	 * @author lance
	 * @since 2014年12月18日 下午3:09:26
	 * @param request 拦截请求request
	 * @return 返回完整URL
	 */
	private String getRequestUrl(ServletRequest request) {
		HttpServletRequest req = (HttpServletRequest) request;
		String queryString = req.getQueryString();

		queryString = StringUtils.isBlank(queryString) ? "" : "?" + queryString;
		return req.getRequestURI() + queryString;
	}
}
