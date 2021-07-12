/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Log2;
import com.wolfking.jeesite.modules.sys.service.LogService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 日志Controller
 * @author ThinkGem
 * @version 2013-6-2
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/log")
public class LogController extends BaseController {

	@Autowired
	private LogService logService;
	
	@RequiresPermissions("sys:log:view")
	//@RequestMapping(value = {"list", ""})  //mark on 2020-7-11
	public String list(Log2 log2, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Log2> page = logService.findPage(new Page<>(request, response), log2);
        model.addAttribute("page", page);
		return "modules/sys/logList";
	}

    /**
     * 列印当前待处理日志信息
     * Queue Size:待处理日志数,Thread activeCount:tomcat进程数
     * @param response
     * @throws IOException
     */
	@RequestMapping(value = "processInfo")
	public void processInfo(HttpServletResponse response) throws IOException {
		response.reset();
		response.setContentType("text/html; charset=UTF-8");
		BufferedOutputStream out = null;
		out = new BufferedOutputStream(response.getOutputStream());
		//out.write(LogUtils.getLogProcessInfo().getBytes(StandardCharsets.UTF_8));
		out.flush();
		out.close();
	}

    /**
     * 列印当前进程名程

	@RequestMapping(value = "threadList")
	public void threadList(HttpServletResponse response) throws IOException {
		response.reset();
		response.setContentType("text/html; charset=UTF-8");
		BufferedOutputStream out = null;
		out = new BufferedOutputStream(response.getOutputStream());
		out.write(LogUtils.getLogThreadList().getBytes(StandardCharsets.UTF_8));
		out.flush();
		out.close();
	}
     */
}
