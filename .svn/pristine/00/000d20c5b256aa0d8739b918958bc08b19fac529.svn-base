/**
 * Copyright &copy; 2012-2013 <a href="httparamMap://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.dao.Log2Dao;
import com.wolfking.jeesite.modules.sys.entity.Log;
import com.wolfking.jeesite.modules.sys.entity.Log2;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wolfking.jeesite.common.persistence.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * 日志Service
 * @author ThinkGem
 * @version 2014-05-16
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class LogService extends LongIDCrudService<Log2Dao, Log2> {

	public Page<Log2> findPage(Page<Log2> page, Log2 log2) {
		
		// 设置默认时间范围，默认当前月
		if (log2.getBeginDate() == null){
			log2.setBeginDate(DateUtils.setDays(DateUtils.parseDate(DateUtils.getDate()), 1));
		}
		if (log2.getEndDate() == null){
			log2.setEndDate(DateUtils.addMonths(log2.getBeginDate(), 1));
		}
		
		// return super.findPage(page, log2);  //mark on 2020-7-11
		return null;
	}


	/**
	 * 保存日志
	 */
	@Async
	public Future<String> saveLog(HttpServletRequest request, Exception ex, String title, User user) {
		Log2 log2 = new Log2();
		Date createDate = new Date();
		log2.setTitle(title);
		log2.setType(ex == null ? Log.TYPE_ACCESS : Log.TYPE_EXCEPTION);
		//log2.setRemoteAddr(StringUtils.getRemoteAddr(request));
		log2.setRemoteAddr(ServletUtil.getClientIP(request));
		log2.setUserAgent(request.getHeader("user-agent"));
		log2.setRequestUri(request.getRequestURI());
		log2.setParams(request.getParameterMap());
		log2.setMethod(request.getMethod());
		log2.setCreateBy(user);
		log2.setCreateDate(createDate);
		log2.setQuarter(QuarterUtils.getSeasonQuarter(createDate));
		// 异步保存日志
		try {
			//super.insert(log2);  //mark on 2020-7-11 sys_log2微服务化
			return new AsyncResult<>("ok");
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return new AsyncResult<>(e.getMessage());
		}
	}

}
