/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDTreeService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.dao.OfficeDao;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * 机构Service
 * @author ThinkGem
 * @version 2014-05-16
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OfficeService extends LongIDTreeService<OfficeDao, Office> {
	@Autowired
	private AreaService areaService;

	@Autowired
	private MSSysAreaService msSysAreaService;

//	@Autowired
//	private RedisUtils redisUtils;

	@Autowired
	private MSSysOfficeService msSysOfficeService;

	public List<Office> findAll(){
		return UserUtils.getOfficeList();
	}

	public List<Office> findList(Boolean isAll){
		if (isAll != null && isAll){
			return UserUtils.getOfficeAllList();
		}else{
			return UserUtils.getOfficeList();
		}
	}

	public List<Office> findList(Office office){
		if(office != null){
			office.setParentIds(office.getParentIds()+"%");
			//List<Office> officeList =  dao.findByParentIdsLike(office);  //mark on 2020-12-19
			// add on 2020-11-28 begin
			List<Office> officeList = msSysOfficeService.findByParentIdsLike(office.getParentIds());
			//msSysOfficeService.compareListOffice("", officeList, officeListFromMS, "OfficeService.findList");  //mark on 2020-12-19
			// add on 2020-11-28 end
			// add on 2020-8-4 begin
			if (!officeList.isEmpty()) {
				List<Long> areaIds = officeList.stream().filter(r->r.getArea()!= null).map(r->r.getArea().getId()).distinct().collect(Collectors.toList());
				List<Area> areaList = areaIds!= null && !areaIds.isEmpty()?msSysAreaService.findSpecListByIds(areaIds):Lists.newArrayList();
				Map<Long,Area> areaMap = areaList!=null && !areaList.isEmpty()?areaList.stream().collect(Collectors.toMap(r->r.getId(),r->r)): Maps.newHashMap();
				officeList.stream().forEach(r->{
					Area area = areaMap.get(r.getArea().getId());
					if (area != null) {
						r.getArea().setName(area.getName());
						r.getArea().setFullName(area.getFullName());
						r.getArea().setParentIds(area.getParentIds());
					}
				});
			}
			return officeList;
			// add on 2020-8-4 end
		}
		return  new ArrayList<Office>();
	}

	/**
	 * 获取单条数据
	 *
	 * @param id
	 * @return
	 */
	@Override
	public Office get(long id) {
		//Office office = dao.get(id);  //mark on 2020-12-19
		// add on 2020-11-28 begin
		Office office = msSysOfficeService.get(id);
		//msSysOfficeService.compareSingleOffice(String.format("id=%s", id), office, officeFromMS, "OfficeService.get");   //mark on 2020-12-19
		// add on 2020-11-28 end
		if (office != null && office.getArea() != null && office.getArea().getId() != null) {
			Area area = areaService.get(office.getArea().getId());
			office.getArea().setName(area.getName());
			office.getArea().setFullName(area.getFullName());
			office.getArea().setParentIds(area.getParentIds());
		}
		return office;
	}

	/**
	 * 按编码获得机构信息
	 * @param code
	 * @return
	 */
	/*
	public Office getByCode(String code){
		//
		// 此方法除OfficeTest中调用外，无其他地方调用 无用代码  2020-11-28
		//
		if(StringUtils.isBlank(code)){
			return null;
		}
		Office office =  dao.getByCode(code);

		// add on 2020-7-31 begin
		if (office != null && office.getArea() != null && office.getArea().getId() != null) {
			Area area = areaService.get(office.getArea().getId());
			office.getArea().setName(area.getName());
			office.getArea().setParentIds(area.getParentIds());
		}
		// add on 2020-7-31 end
		return office;
	}
	*/

	/**
	 * 按编码获得下属机构列表
	 * @param code
	 */
//	public List<Office> getSubListByParentCode(String code){
//		//
//		// 此方法除OfficeTest中调用外，无其他地方调用 无用代码  2020-11-28
//		//
//		if(StringUtils.isBlank(code)){
//			return Lists.newArrayList();
//		}
//		//return dao.getSubListByParentCode(code);
//		List<Office> officeList = dao.getSubListByParentCode(code);
//		// add on 2020-11-28 begin
//		//List<Office> officeListFromMS = msSysOfficeService.findSubListByParentCode(code);
//		//msSysOfficeService.compareListOffice(String.format("code=%s", code), officeList, officeListFromMS, "OfficeService.getSubListByParentCode");
//		// add on 2020-11-28 end
//		return officeList;
//	}

	@Transactional(readOnly = false)
	public void save(Office office) {
		boolean isNew = office.getIsNewRecord();
		//super.save(office);  //mark on 2020-12-19
		// add on 2020-11-28 begin
		office.setIsNewRecord(isNew);

		Office parentOffice = msSysOfficeService.get(office.getParent().getId());
		office.setParentIds(parentOffice.getParentIds()+parentOffice.getId()+",");
		msSysOfficeService.save(office);
		// add on 2020-11-28 end
		//redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_OFFICE_ALL_LIST);
//		UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_SYS_DB,UserUtils.CACHE_OFFICE_LIST);
	}
	
	@Transactional(readOnly = false)
	public void delete(Office office) {
		//super.delete(office);  //mark on 2020-12-19
		msSysOfficeService.delete(office); // add on 2020-11-28
		//redisUtils.remove(RedisConstant.RedisDBType.REDIS_SYS_DB,RedisConstant.SYS_OFFICE_ALL_LIST);
//		UserUtils.removeCache(RedisConstant.RedisDBType.REDIS_SYS_DB,UserUtils.CACHE_OFFICE_LIST);
	}

	public List<Office>  orderByOffice() {
		List<Office> officeList = UserUtils.getOfficeAllList();

		List<Office> outputOfficeList = Lists.newArrayList();
		//Office office = officeList.stream().filter(r->r.getParent().getId().longValue()==0).findFirst().orElse(null);
		Office office = officeList.stream().filter(r->r.getId().longValue()==38).findFirst().orElse(null);
		office.setType(1);
		outputOfficeList.add(office);
		log.warn("office's name={},type={}", office.getName(),office.getType());

		recursionOffice(officeList, office, outputOfficeList);

		log.warn("OK!!!");

		return null;
	}

	// 递归office
	private void recursionOffice(List<Office> sourceOfficeList, Office off, List<Office> offices) {
		List<Office> existsOfficeList = sourceOfficeList.stream().filter(x->x.getParent().getId().longValue() == off.getId()).collect(Collectors.toList());
		if (!ObjectUtils.isEmpty(existsOfficeList) && existsOfficeList.size() >0) {
			for(int i=0; i< existsOfficeList.size(); i++) {
				Office tempOffice = existsOfficeList.get(i);
				if (offices.stream().filter(r->r.getId().equals(tempOffice.getId())).findFirst().orElse(null) == null) {
					tempOffice.setType(off.getType()+1);
					offices.add(tempOffice);
					log.warn("office's name={},type={}", tempOffice.getName(),tempOffice.getType());
					recursionOffice(sourceOfficeList, tempOffice, offices);
				} else {
					continue;
				}
			}
		} else {
			return;
		}
	}
}
