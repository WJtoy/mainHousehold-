/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wolfking.jeesite.common.config.redis.GsonIgnoreStrategy;
import com.wolfking.jeesite.common.mapper.adapters.StringConverter;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;

/**
 * Gson字符串工具类
 * @author Ryan Lu
 */
public class RestGsonUtils {

	private static final Gson gson = new GsonBuilder()
			.addSerializationExclusionStrategy(new GsonIgnoreStrategy())
			//序列化null
			//.serializeNulls()
			//null <-> String
			.registerTypeAdapter(String.class, new StringConverter())
			//禁止转义html标签
			.disableHtmlEscaping()
			//.excludeFieldsWithoutExposeAnnotation() // <---
			.addSerializationExclusionStrategy(new GsonIgnoreStrategy())
			//.setLongSerializationPolicy(LongSerializationPolicy.STRING)//由于js精度不够(2的53次方)，返回json时将Long转成字符
			.create();

	private static RestGsonUtils gsonUtils;

	public RestGsonUtils() {}

	/**
	 * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
	 */
	public static RestGsonUtils getInstance() {
		if (gsonUtils == null){
			gsonUtils = new RestGsonUtils();
		}
		return gsonUtils;
	}

	/**
	 * Object可以是POJO，也可以是Collection或数组。
	 * 如果对象为Null, 返回"null".
	 * 如果集合为空集合, 返回"[]".
	 */
	public String toGson(Object object) {
		return gson.toJson(object);
	}

	/**
	 * 对象转换为JSON字符串
	 * @param object
	 * @return
	 */
	public static String toGsonString(Object object){
		try {
			return RestGsonUtils.getInstance().toGson(object);
		}catch (Exception e){
			LogUtils.saveLog("RestGsonUtils", "toGsonString", object.toString(), e, new User(0l, ""));
			return "";
		}
	}

    
}
