/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/wolfking/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.td.utils;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.service.BaseService;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.td.entity.TaskRequest;
import com.wolfking.jeesite.modules.td.entity.TaskResponse;
import com.wolfking.jeesite.modules.td.utils.HttpRequest;
import com.wolfking.jeesite.modules.td.utils.JacksonHelper;

import java.util.HashMap;

/**
 * 
 * @author Kody huang
 * @version 2015-10-13
 */
public class Kuaidi100Utils extends BaseService
{

	// Kuaidi100Utils.SendKuaidi("shentong", "268012345678");
	public static String SendKuaidi(String company, String number)
	{
		if(StringUtils.isBlank(company) || StringUtils.isBlank(number))
		{
			return "快递公司或者快递单号为空";
		}

		TaskRequest req = new TaskRequest();
		req.setCompany(company);
		req.setFrom("");
		req.setTo("");
		req.setNumber(number);
		req.getParameters().put("callbackurl", Global.getConfig("KDBackURL"));
		req.setKey(Global.getConfig("KDSign"));

		HashMap<String, String> p = new HashMap<String, String>();
		p.put("schema", "json");
		p.put("param", JacksonHelper.toJSON(req));
		try
		{
			String ret = HttpRequest.postData("http://www.kuaidi100.com/poll",
					p, "UTF-8");
			TaskResponse resp = JacksonHelper.fromJSON(ret, TaskResponse.class);
			if (resp.getResult() == true)
			{
				return "OK";
			} else
			{
				return "FAIL";
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return "EXCEPTION";
		}

	}

}
