/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.mapper.JsonMapper;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.dao.DictDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 字典工具类
 * @author ThinkGem
 * @version 2013-5-29
 */
public class DictUtils {

	private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);

	private static DictDao dictDao = SpringContextHolder.getBean(DictDao.class);

	private static final int expireSeconds = 24 * 60 * 60;

//	public static final String CACHE_DICT_MAP = "dictMap";

	/**
	 * 获得标题
	 *
	 * @param value        字典值
	 * @param type         字典类型
	 * @param defaultValue 默认值
	 * @return
	 */
	public static String getDictLabel(String value, String type, String defaultValue) {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value)) {
			for (Dict dict : getDictList(type)) {
				if (value.equals(dict.getValue())) {
					return dict.getLabel();
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 获得描述
	 *
	 * @param value        字典值
	 * @param type         字典类型
	 * @param defaultValue 默认值
	 * @return
	 */
	public static String getDictDescription(String value, String type, String defaultValue) {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value)) {
			for (Dict dict : getDictList(type)) {
				if (value.equals(dict.getValue())) {
					return dict.getDescription();
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 获得字典
	 *
	 * @param value        字典值
	 * @param type         字典类型
	 * @return
	 */
	public static Dict getDictByValue(String value, String type) {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value)) {
			List<Dict> dicts = getDictList(type);
			if(dicts == null || dicts.size()==0){
				return null;
			}
			return dicts.stream().filter(t->t.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
			/*if(dict.isPresent()){
				return dict.get();
			}else{
				return null;
			}*/
		}
		return null;
	}

	/**
	 * 获得多个标题，已逗号分隔
	 *
	 * @param values       值（逗号分隔）
	 * @param type         类型
	 * @param defaultValue 默认值
	 * @return
	 */
	public static String getDictLabels(String values, String type, String defaultValue) {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(values)) {
			List<String> valueList = Lists.newArrayList();
			for (String value : StringUtils.split(values, ",")) {
				valueList.add(getDictLabel(value, type, defaultValue));
			}
			return StringUtils.join(valueList, ",");
		}
		return defaultValue;
	}

	/**
	 * 按类型和标题获得值
	 *
	 * @param label        标题
	 * @param type         类型
	 * @param defaultLabel 默认值
	 * @return
	 */
	public static String getDictValue(String label, String type, String defaultLabel) {
		if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(label)) {
			for (Dict dict : getDictList(type)) {
				if (label.equalsIgnoreCase(dict.getLabel())) {
					return dict.getValue();
				}
			}
		}
		return defaultLabel;
	}

	/**
	 * 获得某类型下所有的字典
	 *
	 * @param typePrex 类型前缀，如judge_item_
	 * @param keys key列表
	 * @return List
	 */
	public static List<Dict> getDictList(String typePrex,String[] keys) {
		List<Dict> values = Lists.newArrayList();
		if(keys == null || keys.length==0){
			return values;
		}
		String type;
		List<Dict> list;
		for(String key:keys){
			type = typePrex+key;
			list = getDictList(type);
			if(list != null && list.size()>0){
				values.addAll(list);
			}
		}
		return values.stream().sorted(Comparator.comparing(Dict::getSort)).collect(Collectors.toList());
	}

	/**
	 * 获得某类型下所有的字典
	 *
	 * @param type 类型
	 * @return List
	 */
	public static List<Dict> getDictList(String type) {
//		String key = "dict:" + type;
		String key = String.format(RedisConstant.SYS_DICT_TYPE, type);
		List<Dict> values = Lists.newArrayList();
		@SuppressWarnings("unchecked")
//		Map<String, byte[]> maps = redisUtils.hGetAll("dict:"+type);
		Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key);
		if (maps != null && maps.size() > 0) {
			for (Entry<String, byte[]> entry : maps.entrySet()) {
				Dict dict = (Dict) redisUtils.gsonRedisSerializer.fromJson(StringUtils.toString(entry.getValue()), Dict.class);
				values.add(dict);
			}
		}
		if (values == null || values.size() == 0) {
			Dict dict = new Dict();
			dict.setType(type);
			values = dictDao.findList(dict);
			if (values != null) {
				Map<String, Object> hashmaps = Maps.newHashMap();
				for (Dict d : values) {
					hashmaps.put(d.getValue(), d);
				}
				redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key, hashmaps, 0l);
			}
		}
		return values.stream().sorted(Comparator.comparing(Dict::getSort)).collect(Collectors.toList());
//		return values.stream().sorted((s1,s2)-> Integer.compare(s1.getSort(), s2.getSort())).collect(Collectors.toList());
	}

    /**
     * 获得某类型下所有的字典
     *
     * @param type 类型
     * @return Map
     */
    public static Map<String,Dict> getDictMaps(String type) {
        String key = String.format(RedisConstant.SYS_DICT_TYPE, type);
        Map<String,Dict> values = Maps.newHashMap();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key);
        if (maps != null && maps.size() > 0) {
            for (Entry<String, byte[]> entry : maps.entrySet()) {
                Dict dict = (Dict) redisUtils.gsonRedisSerializer.fromJson(StringUtils.toString(entry.getValue()), Dict.class);
                values.put(entry.getKey(),dict);
            }
        }
        if (values.size() == 0) {
            Dict dict = new Dict();
            dict.setType(type);
            List<Dict> list = dictDao.findList(dict);
            if (list != null) {
                Map<String, Object> hashmaps = Maps.newHashMap();
                for (Dict d : list) {
                    hashmaps.put(d.getValue(), d);
                    values.put(d.getValue(),d);
                }
                redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key, hashmaps, 0l);
            }
        }
        return values;
    }

	/**
	 * 按类型返回数据字典列表,并排除except指定的列表
	 * @param type
	 * @param except
	 * @return
	 */
	public static List<Dict> getDictExceptList(String type,String except){
		boolean checkExcept = false;
		if(StringUtils.isNotBlank(except)){
			if(!StringUtils.startsWith(except, ",")){
				except = ","+except;
			}
			if(!StringUtils.endsWith(except, ",")){
				except = except.concat(",");
			}
			checkExcept = true;
		}
		List<Dict> dictList = getDictList(type);
		if(dictList.size() == 0 || checkExcept == false){
			return dictList;
		}
		else{
			List<Dict> list = Lists.newArrayList();
			for(Dict dict : dictList){
				if(!except.contains(","+dict.getValue()+",")){
					list.add(dict);
				}
			}
			return list;
		}

	}

	/**
	 * 按类型返回数据字典列表（只返回指定值）
	 * @param type	数据字典类型
	 * @param values 指定的值列表，用逗号分隔
	 * @return
	 */
	public static List<Dict> getDictInclueList(String type,String values){
		boolean checkInclude = false;
		if(StringUtils.isNotBlank(values)){
			if(!StringUtils.startsWith(values, ",")){
				values = ","+values;
			}
			if(!StringUtils.endsWith(values, ",")){
				values = values.concat(",");
			}
			checkInclude = true;
		}
		List<Dict> dictList = getDictList(type);
		if(dictList.size() == 0 || checkInclude == false){
			return dictList;
		}
		else{
			List<Dict> list = Lists.newArrayList();
			for(Dict dict : dictList){
				if(values.contains(","+dict.getValue()+",")){
					list.add(dict);
				}
			}
			return list;
		}

	}

	/**
	 * 按类型返回数据字典列表（只返回指定值）
	 * @param type	数据字典类型
	 * @param start 开始值
	 * @param end   结束值
	 * 	start =null and end =null	： 	返回所有
	 *  start !=null and end !=null	:	返回之间的列表
	 *  start  =null and end !=null	: 	返回>=end的列表
	 *  start !=null and end =null	:	返回<=start的列表
	 * @return
	 */
	public static List<Dict> getDictListOfRange(String type,Integer start,Integer end){
		List<Dict> dictList = getDictList(type);
		if(dictList== null || dictList.size()==0){
			return dictList;
		}
		if(start == null && end == null){
			return dictList;
		}
		//返回之间的列表
		if(start != null && end != null){
			return dictList.stream().filter(t->{
						return Integer.parseInt(t.getValue())>=start && Integer.parseInt(t.getValue())<=end;
					})
					.collect(Collectors.toList());
		}
		//返回<=start的列表
		if(start!=null && end==null){
			return dictList.stream().filter(t->Integer.parseInt(t.getValue())<=start)
					.collect(Collectors.toList());
		}
		//返回>=end的列表
		if(start==null && end!=null){
			return dictList.stream().filter(t->Integer.parseInt(t.getValue())>=end)
					.collect(Collectors.toList());
		}
		return dictList;
	}

	/**
	 * 装载字典
	public static void CacheAllDict() {
		System.out.println("装载数据字典");

		Map<String, List<Dict>> dictMap = Maps.newHashMap();
		for (Dict dict : dictDao.findAllList(new Dict())) {
			List<Dict> dictList = dictMap.get(dict.getType());
			if (dictList != null) {
				dictList.add(dict);
			} else {
				dictMap.put(dict.getType(), Lists.newArrayList(dict));
			}
		}
		List<Dict> list;
		String key;
		Dict dict;
		for (Entry<String, List<Dict>> entry : dictMap.entrySet()) {
			key = String.format(RedisConstant.SYS_DICT_TYPE, entry.getKey());
			//淘汰原缓存
			redisUtils.remove(key);
			list = (List<Dict>) entry.getValue();
			if (list != null && list.size() > 0) {
				int size = list.size();
				for (int i = 0; i < size; i++) {
					dict = list.get(i);
					redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_DB, key, dict.getValue(), dict, 0l);
				}
			}
		}
	}
	*/
	/**
	 * 返回字典列表（JSON）
	 *
	 * @param type
	 * @return
	 */
	public static String getDictListJson(String type) {
		return JsonMapper.toJsonString(getDictList(type));
	}

	/**
	 * 获取字典单一值
	 *
	 * @param type
	 * @param defaultValue
	 * @return
	 */
	public static String getDictSingleValue(String type, String defaultValue) {
		if (StringUtils.isNotBlank(type)) {
			List<Dict> dictList = DictUtils.getDictList(type);
			if (dictList.size() > 0) {
				return dictList.get(0).getValue();
			}
		}
		return defaultValue;
	}


	/**
	 * 获得某类型下所有的字典
	 *
	 * @param type 类型
	 * @return List
	 */
	public static Map<String, Dict> getDictMap(String type) {
//		String key = "dict:" + type;
		String key = String.format(RedisConstant.SYS_DICT_TYPE, type);
		List<Dict> values = Lists.newArrayList();
		@SuppressWarnings("unchecked")
//		Map<String, byte[]> maps = redisUtils.hGetAll("dict:"+type);
		Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key);
		if (maps != null && maps.size() > 0) {
			for (Entry<String, byte[]> entry : maps.entrySet()) {
				Dict dict = (Dict) redisUtils.gsonRedisSerializer.fromJson(StringUtils.toString(entry.getValue()), Dict.class);
				values.add(dict);
			}
		}
		if (values == null || values.size() == 0) {
			Dict dict = new Dict();
			dict.setType(type);
			values = dictDao.findList(dict);
			if (values != null) {
				Map<String, Object> hashmaps = Maps.newHashMap();
				for (Dict d : values) {
					hashmaps.put(d.getValue(), d);
				}
				redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key, hashmaps, 0l);
			}
		}

		Map<String, Dict> result = Maps.newHashMap();
		if (values != null && values.size() > 0) {
			for (Dict item : values) {
				if (item != null && item.getValue() != null) {
					result.put(item.getValue(), item);
				}
			}
		}

		return result;
	}

}
