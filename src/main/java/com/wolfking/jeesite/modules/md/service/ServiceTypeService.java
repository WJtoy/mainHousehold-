/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.ServiceTypeDao;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSServiceTypeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 服务类型
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServiceTypeService extends LongIDCrudService<ServiceTypeDao,ServiceType> {

	@Autowired
	private MSServiceTypeService msServiceTypeService;

	@Resource(name = "gsonRedisSerializer")
	public GsonRedisSerializer gsonRedisSerializer;

	public ServiceType get(Long id) {
		return msServiceTypeService.getById(id);
	}

	/**
	 * 按id查询，优先缓存
	 * @param id
	 * @return
	 */
	public ServiceType getFromCache(Long id) {
		if(id == null || id <=0){
			return null;
		}
		return msServiceTypeService.getFromCache(id);
	}

	public Page<ServiceType> find(Page<ServiceType> page, ServiceType serviceType) {
		// 设置分页参数
		serviceType.setPage(page);
		page = msServiceTypeService.findList(page,serviceType);
		List<ServiceType> list = page.getList();
		Map<String, Dict> warrantyStatusMap = MSDictUtils.getDictMap("warrantyStatus");//切换为微服务
		Map<String,Dict> orderTypes = MSDictUtils.getDictMap("order_service_type");
		Dict warrantyStatusDict;
		Dict dict;
		for (ServiceType item : list) {
			warrantyStatusDict = item.getWarrantyStatus();
			if (warrantyStatusDict != null && warrantyStatusDict.getValue() != null) {
				item.setWarrantyStatus(warrantyStatusMap.get(warrantyStatusDict.getValue()));
			}
			dict = orderTypes.get(String.valueOf(item.getOrderServiceType()));
			if(Objects.isNull(dict)){
				item.setOrderServiceTypeDict(new Dict(item.getOrderServiceType(),""));
			}else{
				item.setOrderServiceTypeDict(dict);
			}
		}
		// 执行分页查询
		page.setList(list);
		warrantyStatusMap.clear();
		orderTypes.clear();
		return page;
	}

	@Transactional
	public void save(ServiceType serviceType) {
		boolean isNew = serviceType.getIsNewRecord();
		MSErrorCode msErrorCode = msServiceTypeService.save(serviceType,isNew);
		if(msErrorCode.getCode()>0){
			throw new RuntimeException("保存服务类型失败.失败原因:" + msErrorCode.getMsg());
		}
	}

	@Transactional(readOnly = false)
	public void delete(ServiceType serviceType) {
		MSErrorCode msErrorCode = msServiceTypeService.delete(serviceType);
		if(msErrorCode.getCode()>0){
			throw new RuntimeException("删除服务类型失败.失败原因:" + msErrorCode.getMsg());
		}
	}

	public boolean existServiceName(String name) {
		List<ServiceType> list = findAllList();
		if(list==null || list.size()==0){
			return false;
		}
		return null != list.stream()
				.filter(t->t.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 读取所有服务类型
	 * 优先缓存读取
	 * @return
	 */
	@Override
	public List<ServiceType> findAllList() {
		List<ServiceType> serviceTypesList = msServiceTypeService.findAllList();
		if(serviceTypesList!=null && serviceTypesList.size()>0){
			return serviceTypesList.stream().sorted(Comparator.comparing(ServiceType::getSort).thenComparing(ServiceType::getName))
					.collect(Collectors.toList());
		}
		return serviceTypesList;
	}

	/**
	 * 重载缓存
	 */
	public void reloadServiceTypeCache() {
		/*
		// mark on 2020-1-6
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_SERVICE_TYPE);
		findAllList();
		*/
	}

	/**
	 * 获取所有服务类型
	 * @return
	 */
	public Map<Long, ServiceType> getAllServiceTypeMap() {
		List<ServiceType> serviceTypeList = findAllList();
		if(CollectionUtils.isEmpty(serviceTypeList)) {
			return Maps.newHashMapWithExpectedSize(0);
		}
		Map<Long, ServiceType> serviceTypeMap = Maps.newHashMap();
		for (ServiceType item : serviceTypeList) {
			serviceTypeMap.put(item.getId(), item);
		}
		return serviceTypeMap;
	}

	/**
	 * 获取所有的服务类型
	 * @return map<Long,String> key为id,value为服务类型名称</>
	 */
	public Map<Long,String> findAllIdsAndNames(){
		return msServiceTypeService.findAllIdsAndNames();
	}


	/**
	 * 获取所有的服务类型
	 * @return map<Long,String> key为id,value为服务类型编码(code)</>
	 */
	public Map<Long,String> findIdsAndCodes(){
		return msServiceTypeService.findIdsAndCodes();
	}


	/**
	 * 获取所有的服务类型
	 * @Parm id,name 为MDServiceType 的属性名
	 * @return list  对象只有id跟服务类型名称有值
	 */
	public List<ServiceType> findAllListIdsAndNames() {
		List<String> list = Lists.newArrayList();
		list.add("id");
		list.add("name");
		return msServiceTypeService.findAllListWithCondition(list);
	}

	public List<ServiceType> findListIdAndNameAndWarrantyStatus() {
		List<String> list = Lists.newArrayList();
		list.add("id");
		list.add("name");
		list.add("warrantyStatus");
		return msServiceTypeService.findAllListWithCondition(list);
	}

	/**
	 * 获取所有的服务类型
	 * @Parm id,name,code,warrantyStatus 为MDServiceType 属性名
	 * @return 对象只返回id 和服务名称,code,warrantyStatus
	 */
	public List<ServiceType> findAllListIdsAndNamesAndCodes(){
		List<String> list = Lists.newArrayList();
		list.add("id");
		list.add("name");
		list.add("code");
		list.add("warrantyStatus");
		return msServiceTypeService.findAllListWithCondition(list);
	}

	/**
	 * 获取所有的服务类型
	 * @Parm id,name,code,warrantyStatus sort 为MDServiceType 属性名
	 * @return 对象只返回id 和服务名称,code,warrantyStatus,sort
	 */
	public List<ServiceType> findAllListFields(){
		List<String> list = Lists.newArrayList();
		list.add("id");
		list.add("name");
		list.add("code");
		list.add("warrantyStatus");
		list.add("sort");
		return msServiceTypeService.findAllListWithCondition(list);
	}

	/**
	 * 按订单类型读取服务类型列表
	 */
	public List<ServiceType> findListOfOrderType(Integer orderType){
		if(orderType == null || orderType <0){
			return Lists.newArrayList();
		}
		List<ServiceType>  serviceTypes = findAllList();
		if(!org.springframework.util.CollectionUtils.isEmpty(serviceTypes)){
			serviceTypes = serviceTypes.stream().filter(t->t.getOrderServiceType().equals(orderType) && t.getDelFlag() == 0).collect(Collectors.toList());
		}
		return serviceTypes==null?Lists.newArrayList():serviceTypes;
	}
}
