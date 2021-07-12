/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.DataEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.dao.GradeDao;
import com.wolfking.jeesite.modules.md.dao.GradeItemDao;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.modules.md.utils.GradeAdapter;
import com.wolfking.jeesite.ms.providermd.service.MSGradeItemService;
import com.wolfking.jeesite.ms.providermd.service.MSGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客评项目Service
 * @author
 * @version
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class GradeService extends LongIDCrudService<GradeDao,Grade> {
	/*@Autowired
	private GradeItemDao gradeItemDao;*/

	//public static final String CACHE_GRADE = "grade:id:";

	/*
	// mark on 2020-1-7
	@Autowired
	private RedisUtils redisUtils;
	*/

	@Autowired
	private MSGradeService msGradeService;

	@Autowired
	private MSGradeItemService msGradeItemService;
	
	//Grade
	public Grade get(Long id) {
		//return gradeDao.get(id);

		//Grade entity = super.get(id); //mark on 2019-9-29
        Grade entity = msGradeService.getById(id);
		//GradeItem item = new GradeItem();  //mark on 2020-1-7
		//item.setGrade(entity);             //mark on 2020-1-7
		//entity.setItemList(gradeItemDao.findListByGradeID(item)); mark on 2019-9-30
		entity.setItemList(msGradeItemService.findListByGradeId(id));

		return entity;
	}
	
	public List<Grade> findAllList(){
		//return super.findAllList(new Grade()); //mark on 2019-9-29
		return msGradeService.findAllList();
	}

	/**
	 * 读取生效的客评项及评分
	 */
	/*
	// mark on 2020-1-7
	public List<Grade> findAllEnabledGradeAndItems(){
		return dao.findAllEnabledGradeAndItems();
	}
	 */

	public Page<Grade> find(Page<Grade> page, Grade grade)
	{
		grade.setPage(page);
		//List<Grade> gradeList = super.findList(grade); //marl on 2019-9-29
		//调用微服务 start 2019-9-29
		page = msGradeService.findList(page,grade);
		List<Grade> gradeList = page.getList();
		// end
		for(Grade entity:gradeList)
		{
			//GradeItem item = new GradeItem();  //mark on 2020-1-7
			//item.setGrade(entity);             //mark on 2020-1-7
			//entity.setItemList(gradeItemDao.findListByGradeID(item)); //mark on 2019-9-30
			//调用微服务 start 2019-9-30
			entity.setItemList(msGradeItemService.findListByGradeId(entity.getId()));
			//end
			/*
			if (redisUtils.exists(CACHE_GRADE+entity.getId()))
			{
				redisUtils.remove(CACHE_GRADE+entity.getId());
			}
			redisUtils.set(CACHE_GRADE+entity.getId(),entity,0l);
			*/
		}
		return page.setList(gradeList);
	}
	
	@Transactional(readOnly = false)
	public void save(Grade grade) {
		boolean isNew = grade.getIsNewRecord();
		//super.save(grade);   //mark on 2020-1-7
		MSErrorCode msErrorCode = msGradeService.save(grade,isNew);
		if(msErrorCode.getCode()>0){
			throw new RuntimeException("保存客评失败.失败原因:" + msErrorCode.getMsg());
		}
		// 清除缓存
		//delGradeCache(); //mark on 2020-1-7

		/*
		if (redisUtils.exists(CACHE_GRADE+grade.getId())) {
			redisUtils.remove(CACHE_GRADE + grade.getId());
		}
		redisUtils.set(CACHE_GRADE+grade.getId(),grade,0l);
		*/
	}
	
	@Transactional(readOnly = false)
	public void deleteGrade(Long id) {
		Grade grade = get(id);
		//逻辑删除评价标准
		/*
		// mark on 2020-1-7
		if(grade!=null && grade.getItemList().size()>0){
			for(GradeItem g:grade.getItemList()) {
				gradeItemDao.delete(g);  //mark on 2020-1-7
			}
		}
		*/
		//super.delete(grade);  //mark on 2020-1-7
		//调用微服务 2019-9-28 start
		MSErrorCode msErrorCode = msGradeService.delete(grade);  // 此方法已包含删除grade及gradeItem数据  2020-1-7
		if(msErrorCode.getCode()>0){
			throw new RuntimeException("删除客评失败.失败原因:" + msErrorCode.getMsg());
		}
		// end 2019-9-29
		// 清除缓存
		//delGradeCache(); //mark on 2020-1-7

		/*if (redisUtils.exists(CACHE_GRADE+grade.getId())) {
			redisUtils.remove(CACHE_GRADE + grade.getId());
		}*/
	}
	
	//Item评价标准
	public GradeItem getItem(Long id) {
		//return gradeItemDao.get(id); mark on 2019-10-9

		//调用微服务 start 2019-10-9
		GradeItem gradeItem = msGradeItemService.getById(id);
		Grade entity = msGradeService.getById(gradeItem.getGrade().getId());
		if(entity!=null){
			gradeItem.setGrade(entity);
		}
		return gradeItem;
		// end 2019-10-9
	}
	
	@Transactional(readOnly = false)
	public void save(GradeItem item) {
		boolean isNew = item.getIsNewRecord();
		/*
		// mark on 2020-1-7
		if (item.getId() == null || item.getId() <=0)
		{
			item.preInsert();
			gradeItemDao.insert(item);
		}
		else{
			item.preUpdate();
			gradeItemDao.update(item);
		}
		*/

		//调用微服务 strat 2019-9-30

		MSErrorCode errorCode = msGradeItemService.save(item,isNew);
		if(errorCode.getCode()>0){
			throw new RuntimeException("保存评价标准失败.失败原因:" + errorCode.getMsg());
		}


		// 清除缓存
		//delGradeCache();  //mark on 2020-1-7

		/*// add on 2017-4-28 begin
		Grade grade = get(item.getGrade().getId());
		if (redisUtils.exists(CACHE_GRADE+grade.getId())) {
			redisUtils.remove(CACHE_GRADE + grade.getId());
		}
		redisUtils.set(CACHE_GRADE+grade.getId(),grade,0l);
		// add on 2017-4-28 end*/
	}
	
	@Transactional(readOnly = false)
	public void deleteGradeItem(Long gradeId,Long id) {
		Grade grade = get(gradeId);
		List<Long> itemIds = grade.getItemIdList();
		List<GradeItem> items = grade.getItemList();

		if (itemIds.contains(id)) {
			for(GradeItem g:items) {
				if(g.getId().equals(id)){
					g.setDelFlag(DataEntity.DEL_FLAG_DELETE);
					//gradeItemDao.delete(g);  //mark on 2020-1-7
					// 调用微服务 start 2019-9-30
					MSErrorCode msErrorCode = msGradeItemService.delete(g);
					if(msErrorCode.getCode()>0){
						throw new RuntimeException("删除客评标准失败.失败原因:" + msErrorCode.getMsg());
					}
					break;
				}
			}
			// 清除缓存
			//delGradeCache(); // mark on 2020-1-7
			/*// add on 2017-4-28 begin
			Grade updatedGrade = get(gradeId);
			if (redisUtils.exists(CACHE_GRADE+updatedGrade.getId())) {
				redisUtils.remove(CACHE_GRADE + updatedGrade.getId());
			}
			redisUtils.set(CACHE_GRADE+updatedGrade.getId(),updatedGrade,0l);
			// add on 2017-4-28 end*/
		}
	}

	/****************************************************************************
	 * redis操作
	 ****************************************************************************/
	/**
	 * 加载客评项目，当缓存未命中则从数据库装载至缓存
	 * @return
	 */
	public List<Grade> findAllListCache(){
		List<Grade> list =  Lists.newArrayList();
		// 调用微服务 start 2019-10-8
		list = msGradeService.findAllGradeListFromCache();
		/*
		// mark on 2020-1-7
		if(list !=null && list.size()>0){
			return list;
		}
		*/
		return list;
		// end
		/*
		// mark on 2020-1-7 begin
		try {
			Set<byte[]> sets = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_GRADE, 0, -1);
			if(sets != null && sets.size()>0) {
				String json = new String();
				Grade grade;
				try{
				for (byte[] bytes:sets) {
					json = StringUtils.toString(bytes);
					grade = GradeAdapter.getInstance().fromJson(json);
					list.add(grade);
				}
				}catch(Exception e) {
					list = Lists.newArrayList();
				}
			}
		}catch (Exception e){}
		if(list != null && list.size()>0){
			return list;
		}
		// mark on 2020-1-7 end
		*/

		/*list = dao.findAllEnabledGradeAndItems();*/  //mark on 2019-10-8

		// 调用微服务 start 2019-10-8
		//list = msGradeService.findAllGradeListToLoadCache();  //mark on 2020-1-7
		// end 2019-10-8

		/*
		List<Grade> list =  null;
		try{
			list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_GRADE,0,-1,Grade.class);
		}catch (Exception e){}

		if(list != null && list.size()>0){
			return list;
		}
		list = dao.findAllEnabledGradeAndItems();
		*/
		/*
		list = super.findAllList();
		if(list != null && list.size()>0){
			for(int index = 0; index < list.size(); index++){
				Grade grade = list.get(index);
				GradeItem item = new GradeItem();
				item.setGrade(grade);
				grade.setItemList(gradeItemDao.findListByGradeID(item));//grad items
				//redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_GRADE, grade, index+1,0);
			}
			//cache
			Set<RedisZSetCommands.Tuple> sets =  list.stream()
					.map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
					.collect(Collectors.toSet());

			redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,RedisConstant.MD_GRADE,sets,0l);
		}*/
		//if(list != null && list.size()>0){  // mark on 2020-1-7
			//cache
			/*
			Set<RedisZSetCommands.Tuple> sets =  list.stream()
					.map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
					.collect(Collectors.toSet());
			*/
			/*
			// mark on 2020-1-7 begin
			Set<RedisZSetCommands.Tuple> sets =  list.stream()
					.map(t -> new RedisTuple(GradeAdapter.getInstance().toJson(t).getBytes(StandardCharsets.UTF_8), t.getId().doubleValue()))
					.collect(Collectors.toSet());

			redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB,RedisConstant.MD_GRADE,sets,0l);
		}

		return list;
		// mark on 2020-1-7 end
		*/
	}



	//删除客评项目缓存
	/*
	// mark on 2020-1-7
	public void delGradeCache(){
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_GRADE);
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_ORDER_GRADE);
	}
	*/
}
