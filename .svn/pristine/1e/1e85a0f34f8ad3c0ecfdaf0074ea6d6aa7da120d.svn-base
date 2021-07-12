/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.common.security.shiro.session;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.IdGen;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.Servlets;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 系统安全认证实现类
 * @author ThinkGem
 * @version 2014-7-24
 */
@Component("sessionDAO")
@Slf4j
public class CacheSessionDAO extends AbstractSessionDAO implements SessionDAO,InitializingBean{

	public static long globExpire = 120;// 2小时

	@Autowired
	private IdGen idgen;
	@Autowired
	private CacheManager cacheManager;

	@Override
	public void afterPropertiesSet() throws Exception {
		setSessionIdGenerator(idgen);
//		setActiveSessionsCacheName("activeSessionsCache");
//		setCacheManager(cacheManager);
	}

	public CacheSessionDAO() {
        super();
    }

	// 创建session
	@Override
	protected Serializable doCreate(Session session) {
		HttpServletRequest request = Servlets.getRequest();
		if (request != null){
			String uri = request.getServletPath();
//			System.out.println("doCreate:" + uri);
			// 如果是静态文件，则不创建SESSION
			if (Servlets.isStaticFile(uri)){
				return null;
			}
            if (StringUtils.endsWith(uri,"/login") && request.getMethod().equalsIgnoreCase("post")){
                return null;
            }
//			if ((StringUtils.endsWith(uri,"/login")
//					|| StringUtils.endsWith(uri,"login/"))
//					&& request.getMethod().equalsIgnoreCase("post")){
//
//			}else{
////				System.out.println("not login post,not crete session");
//				return null;
//			}
		}
		Serializable sessionId = this.generateSessionId(session);
		this.assignSessionId(session, sessionId);
		cacheManager.getCache("activeSessionsCache").put(session.getId(),session);
//		super.doCreate(session);
		log.debug("doCreate {} {}", session, request != null ? request.getRequestURI() : "");
		return sessionId;
	}

	// 获取session
	@Override
	protected Session doReadSession(Serializable sessionId) {
//		return super.doReadSession(sessionId);
		return (Session)cacheManager.getCache("activeSessionsCache").get(sessionId);
	}

	@Override
	public Session readSession(Serializable sessionId) throws UnknownSessionException {
		try{
			Session s = null;
			HttpServletRequest request = Servlets.getRequest();
			if (request != null){
				String uri = request.getServletPath();
				// 如果是静态文件，则不获取SESSION
				if (Servlets.isStaticFile(uri)){
					return null;
				}
				s = (Session)request.getAttribute("session_"+sessionId);
			}
			if (s != null){
				return s;
			}

//			Session session = super.readSession(sessionId);
			Session session = (Session) cacheManager.getCache("activeSessionsCache").get(sessionId);
			log.debug("readSession {} {}", sessionId, request != null ? request.getRequestURI() : "");

			if (request != null && session != null){
				request.setAttribute("session_"+sessionId, session);
			}

			return session;
		}catch (UnknownSessionException e) {
			return null;
		}
	}


	// 更新session的最后一次访问时间
	@Override
    public void update(Session session) {
    	if (session == null || session.getId() == null) {  
            return;
        }
    	
    	HttpServletRequest request = Servlets.getRequest();
		if (request != null){
			String uri = request.getServletPath();
//			System.out.println("cacheSessionDao uri:" + uri);
			// ajax访问，不更新SESSION
			if(!uri.contains("/servlet/validateCodeServlet") && Servlets.isAjaxRequest(request)){
//				System.out.println("[ajax]");
				return;
			}

			//匿名访问
			if(Servlets.isAnonRequest(uri)){
				return;
			}

			/* 如果是静态文件，则不更新SESSION */
			if (Servlets.isStaticFile(uri)){
				return;
			}
			// 如果是视图文件，则不更新SESSION
			if (StringUtils.startsWith(uri, Global.getConfig("spring.mvc.view.prefix"))
					&& StringUtils.endsWith(uri, Global.getConfig("spring.mvc.view.suffix"))){
				return;
			}

			/* 匿名访问，不更新session
			if( StringUtils.contains(uri, "/uploads/")
					|| StringUtils.contains(uri, "/error/")
					|| StringUtils.endsWith(uri, "/error")
					|| StringUtils.contains(uri, "/static/")
					|| StringUtils.contains(uri, "/userfiles/")
					|| StringUtils.contains(uri, "/td/")){
				return;
			} */
			// request & 和异步
			if(!request.getDispatcherType().equals(DispatcherType.REQUEST) && !request.getDispatcherType().equals(DispatcherType.ASYNC)){
//				System.out.println("not:request,ansy,ignor");
				return;
			}
			/* 只有login 非post方式 忽略 */
			if(StringUtils.endsWith(uri,"/login") && !request.getMethod().equalsIgnoreCase("post")){
				return;
			}
			// 手动控制不更新SESSION
			String updateSession = request.getParameter("updateSession");
			if (Global.FALSE.equals(updateSession) || Global.NO.equals(updateSession)){
				return;
			}
			if(uri.contains("/servlet/validateCodeServlet")){
				cacheManager.getCache("activeSessionsCache").put(session.getId(),session);
				return;
			}
		}

		Object obj = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
		if(obj == null){
//			cacheManager.getCache("activeSessionsCache").remove(session.getId());
//			super.doDelete(session);
			return;
		}
		//System.out.print(obj);
//    	super.doUpdate(session);
		cacheManager.getCache("activeSessionsCache").put(session.getId(),session);
//		System.out.println("[doUpdate]:" + session.getId());
    	//log.debug("update {} {}", session.getId(), request != null ? request.getRequestURI() : "");

    }

    //删除session

    @Override
    public void delete(Session session) {
    	if (session == null || session.getId() == null) {  
            return;
        }

    	//super.doDelete(session);
		cacheManager.getCache("activeSessionsCache").remove(session.getId());
    	log.debug("delete {} ", session.getId());
    }

	@Override
	public Collection<Session> getActiveSessions() {
		return Collections.emptySet();
	}

    /**
	 * 获取活动会话
	 * @param includeLeave 是否包括离线（最后访问时间大于3分钟为离线会话）
	 * @return
	 */
	@Override
	public Collection<Session> getActiveSessions(boolean includeLeave) {
		return getActiveSessions(includeLeave, null, null);
	}

    /*
	@Override
	*/
	public Collection<Session> getActiveSessions(boolean includeLeave, Object principal, Session filterSession) {
//		return Sets.newHashSet();
//		return null;
		return Collections.emptySet();
	}

    /**
	 * 获取活动会话
	 * @param includeLeave 是否包括离线（最后访问时间大于3分钟为离线会话）
	 * @param principal 根据登录者对象获取活动会话
	 * @param filterSession 不为空，则过滤掉（不包含）这个会话。
	 * @return
	@Override
	public Collection<Session> getActiveSessions(boolean includeLeave, Object principal, Session filterSession) {
		// 如果包括离线，并无登录者条件。
		if (includeLeave && principal == null){
			return getActiveSessions();
		}
		Set<Session> sessions = Sets.newHashSet();
		for (Session session : getActiveSessions()){
			if(session == null) continue;
			boolean isActiveSession = false;
			// 不包括离线并符合最后访问时间小于等于3分钟条件。
			if (includeLeave || DateUtils.pastMinutes(session.getLastAccessTime()) <= 3){
				isActiveSession = true;
			}
			// 符合登陆者条件。
			if (principal != null){
				PrincipalCollection pc = (PrincipalCollection)session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
				if (principal.toString().equals(pc != null ? pc.getPrimaryPrincipal().toString() : StringUtils.EMPTY)){
					isActiveSession = true;
				}
			}
			// 过滤掉的SESSION
			if (filterSession != null && filterSession.getId().equals(session.getId())){
				isActiveSession = false;
			}
			if (isActiveSession){
				sessions.add(session);
			}
		}
		return sessions;
	}*/
	
}
