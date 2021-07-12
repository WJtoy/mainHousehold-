/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.sys.SysArea;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDTreeService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.dao.AreaDao;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.UserRegion;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointAreaService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointStationService;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域Service
 *
 * @author ThinkGem
 * @version 2014-05-16
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AreaService extends LongIDTreeService<AreaDao, Area> {
    @Autowired
    private MSSysAreaService msSysAreaService;

    @Autowired
    private RedisUtils redisUtils;

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private UserRegionService userRegionService;

    /**
     * 获取单条数据
     *
     * @param id
     * @return
     */
    @Override
    public Area get(long id) {
        Area areaFromMS =  msSysAreaService.get(id);
        return areaFromMS;
    }

    public List<Area> findAll() {
        return findAll(1);
    }


    public List<Area> findProvinceAndCityListFromCache() {
        // add on 2020-8-5  从缓存中所有的省和市区域
        List<Area> list =  msSysAreaService.findProvinceAndCityListFromCache();
        if (list != null && !list.isEmpty()) {
            return list.stream().sorted(Comparator.comparingInt(Area::getSort)).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 读取区域
     *
     * @param fromType 开始区域层级
     * @return
     */
    public List<Area> findAll(Integer fromType) {
        List<Area> list = msSysAreaService.findAllListExcludeTown();  // add on 2020-11-13
        if (fromType == null) {
            return list;
        } else {
            return list.stream().filter(t -> t.getType() >= fromType)
                    .sorted(Comparator.comparingInt(Area::getSort)).collect(Collectors.toList());
        }
    }

    /**
     * 获取区，市及省区域名称(用于B2BServicePoint中获取网点信息) // add on 2019-9-20
     * @param id
     * @return
     */
    public Area getThreeLevelAreaById(Long id) {
        // add on 2020-7-29
        Area area = msSysAreaService.getThreeLevelAreaById(id);
        if (area == null) {
            area = new Area(id);
        }
        return area;
    }

    public Area getThreeLevelAreaByIdFromCache(Long id) {
        Area area = msSysAreaService.getThreeLevelAreaByIdFromCache(id);
        if (area == null) {
            area = new Area(id);
        }
        return area;
    }

    public Area getFromCache(Long id) {
        if (id == null || id == 0) {
            return null;
        }

        Area areaFromMS = msSysAreaService.getFromCache(id);  // add on 2020-7-20
        return areaFromMS;
    }

    public Area getFromCache(Long id, int type) {
        if (id == null || id == 0) {
            return null;
        }

        Area areaFromMS = msSysAreaService.getFromCache(id, type);  //add on 2020-7-20

        return areaFromMS;
    }

    public Area getTownFromCache(Long areaId, Long townId) {
        Area areaFromMS = msSysAreaService.getTownFromCache(areaId, townId);  // add on 2020-7-29

        return areaFromMS;
    }

    public List<Area> getSelfAndParents(Long id) {
        List<Area> list = Lists.newArrayList();
        Area area = getFromCache(id);
        if (area == null) {
            return list;
        }
        list.add(area);
        Area city = getFromCache(area.getParentId());
        if (city == null) {
            return list;
        }
        list.add(city);
        Area province = getFromCache(city.getParentId());
        if (province == null) {
            return list;
        }
        list.add(province);
        return list;
    }

    /**
     * 根据id获得其所有父区域,包含自己
     * 读取:area.getParent().getParent()
     * @param id 区域id
     */
    public Area getSelfAndParentList(Long id,Long parentId,int areaType) {
        Area subArea = null;
        //街道
        if(areaType == Area.TYPE_VALUE_TOWN){
            subArea = getTownFromCache(parentId,id);
            if(subArea == null){
                return subArea;
            }
        }

        //area
        Area area = getFromCache(areaType == Area.TYPE_VALUE_TOWN?parentId:id);
        if (area == null) {
            return subArea;
        }
        if(areaType == Area.TYPE_VALUE_TOWN) {
            subArea.setParent(area);
        }
        //city
        Area parent = null;
        if(area.getParentId() > 0){
            parent = getFromCache(area.getParentId());
            if (parent == null) {
                return area;
            }
            area.setParent(parent);
        }
        //province
        if(parent != null && parent.getParentId() > 0){
            Area preParent = getFromCache(parent.getParentId());
            if (preParent == null) {
                return area;
            }
            parent.setParent(preParent);
        }
        return areaType == Area.TYPE_VALUE_TOWN ? subArea : area;
    }

    /**
     * 根据区县id获得其所有父区域,包含自己
     * Map<区域类型,区域>
     * @param id 区/县 id
     */
    public Map<Integer,Area> getAllParentsWithDistrict(Long id) {
        Map<Integer,Area> map = Maps.newHashMapWithExpectedSize(5);
        Area area = getFromCache(id);
        if (area == null) {
            return map;
        }
        map.put(area.getType(),area);
        Area city = getFromCache(area.getParentId());
        if (city == null) {
            return map;
        }
        map.put(city.getType(),city);
        Area province = getFromCache(city.getParentId());
        if (province == null) {
            return map;
        }
        map.put(province.getType(),province);
        return map;
    }

    /**
     * 获取客户管辖的区域（所有阶层）
     *
     * @param id 客服id
     */
    public List<Area> getFullAreaListOfKefu(Long id) {
        List<Area> areas = getAreaListOfKefu(id);
        if (areas == null) {
            return Lists.newArrayList();
        } else if (areas.size() == 0) {
            return areas;
        }
        //包含所有阶层
        if (areas.stream().filter(t -> t.getType() < 4).count() > 0) {
            return areas;
        }
        //从下往上取

        List<Area> list = findProvinceAndCityListFromCache();
        for (int i = 4; i > 1; i--) {
            int type = i;
            Set<Long> pids = areas.stream().filter(t -> t.getType() == type).map(t -> t.getParent().getId()).collect(Collectors.toSet());
            list.stream().filter(t -> pids.contains(t.getId())).forEach(t -> {
                areas.add(t);
            });
        }

        return areas;
    }

    /**
     * 获取客户管辖的区域(数据库中列表)
     *
     * @param id 客服id
     */
    public List<Area> getAreaListOfKefu(Long id) {
        String key = String.format(RedisConstant.SHIRO_KEFU_AREA, id);
        return redisUtils.getList(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB, key, Area[].class);
    }

    public Page<Area> find(Page<Area> page, Map<String, Object> paramMap) {
        Area area = new Area();
        String type = ObjectUtils.toString(paramMap.get("type"));
        if (StringUtils.isNotEmpty(type)) {
            area.setType(Integer.valueOf(type));
        } else {
            area.setType(0);
        }
        String name = ObjectUtils.toString(paramMap.get("name"));
        if (StringUtils.isNotEmpty(name)) {
            area.setName(name);
        } else {
            area.setName("");
        }
        Area parent = new Area();
        String parentName = ObjectUtils.toString(paramMap.get("parentName"));
        if (StringUtils.isNotEmpty(parentName)) {
            parent.setName(parentName);
        } else {
            parent.setName("");
        }
        area.setParent(parent);

        // 生成数据权限过滤条件（dsf为dataScopeFilter的简写，在xml中使用 ${sqlMap.dsf}调用权限SQL）
        //area.getSqlMap().put("dsf", dataScopeFilter(area.getCurrentUser(), "o", "a"));
        // 设置分页参数
        area.setPage(page);
        // 执行分页查询
        //page.setList(areaDao.findList(area));

        List<Area> areaList = findSpecList(area);  //add on 2020-11-13

        //切换为微服务
        Map<String, Dict> areaTypeMap = MSDictUtils.getDictMap("sys_area_type");
        for (Area item : areaList) {
            if (item.getType() != null && item.getType() > 0) {
                Dict areaTypeDict = areaTypeMap.get(item.getType().toString());
                item.setTypeName(areaTypeDict != null ? areaTypeDict.getLabel() : "");
            }
        }

        page.setList(areaList);
        return page;
    }

    private List<Area> findSpecList(Area area) {
        // add on 2020-7-20
        return msSysAreaService.findSpecList(area);
    }

    /**
     * 按区域类型返回所有区域清单
     *
     * @param type
     * @return
     */
    public List<Area> findListByType(Integer type) {
        List<Area> areaListFromMS = msSysAreaService.findListByTypeFromCache(type);
        return areaListFromMS;
    }

    /**
     * 按区域类型返回所有区域Map<id,area>
     * @param type
     * @return
     */
    public Map<Long,Area> findMapByType(Integer type){
        List<Area> areas = findListByType(type);
        if(areas == null || areas.size() == 0){
            return Maps.newHashMap();
        }
        return areas.stream().collect(Collectors.toMap(
           e->e.getId(),
           e->e
        ));
    }

    /**
     * 按区域返回下属区域
     *
     * @param type
     * @param pid
     * @return
     */
    public List<Area> findListByParent(Integer type, Long pid) {
        List<Area> areaListFromMS = msSysAreaService.findListByTypeAndParentFromCache(type, pid);   // add on 2020-7-23
        return areaListFromMS;
    }

    @Transactional(readOnly = false)
    public void save(Area area) {
        boolean isNew = area.getIsNewRecord();
        area.setIsNewRecord(isNew);  // add on 2020-8-7
        msSysAreaService.save(area); // add on 2020-7-20
    }

    /**
     * 查找所有下级区域数据  //add on 2019-9-30
     * (此方法初衷主要为网点付款报表用)
     * @param parentIds
     * @return
     */
    public List<Area> findByParentIdsLike(String parentIds) {
        List<Area> areaListFromMS = msSysAreaService.findByParentIdsLike(parentIds);

        return areaListFromMS;
    }

    @Transactional(readOnly = false)
    public void delete(Area area) {
        msSysAreaService.delete(area.getId());
    }

    /**
     * 按用户id或者区域id查询区域列表
     *
     * @return
     */
    public List<Area> findListByUserIdOrAreaId(java.util.Map<String, Object> paramMap) {
        String strUserId = Optional.ofNullable(paramMap.get("userId")).map(Object::toString).orElse("");
        List<Area> userAreaList = findUserRegionListByUserId(Long.valueOf(strUserId));
        List<Area> areaList = Lists.newArrayList();
        if (!org.springframework.util.ObjectUtils.isEmpty(userAreaList)) {
            areaList = msSysAreaService.findDistrictListByAreas(userAreaList);
        }

        return areaList;
    }

    /**
     * 根据用户id从表sys_user_area获取区域id列表
     * @param userId
     * @return
     */
    public List<Area> findUserRegionListByUserId(Long userId) {
        //
        //  根据用户id从表sys_user_region获取区域id列表
        //
        List<Area> areaList = Lists.newArrayList();

        //List<UserRegion> userRegionList =  userRegionService.getUserRegions(userId);
        List<UserRegion> userRegionList =  userRegionService.getUserRegionsFromDB(userId);
        if (userRegionList != null && !userRegionList.isEmpty()) {
            for(int i=0; i< userRegionList.size();i++) {
                UserRegion userRegion = userRegionList.get(i);
                Area area = new Area();
                if (userRegion.getAreaType().equals(Area.TYPE_VALUE_COUNTRY)) {
                    area.setType(Area.TYPE_VALUE_COUNTRY);
                    area.setId(0L);
                } else if (userRegion.getAreaType().equals(Area.TYPE_VALUE_PROVINCE)) {
                    area.setType(Area.TYPE_VALUE_PROVINCE);
                    area.setId(userRegion.getProvinceId());
                } else if (userRegion.getAreaType().equals(Area.TYPE_VALUE_CITY)) {
                    area.setType(Area.TYPE_VALUE_CITY);
                    area.setId(userRegion.getCityId());
                } else if (userRegion.getAreaType().equals(Area.TYPE_VALUE_COUNTY)) {
                    area.setType(Area.TYPE_VALUE_COUNTY);
                    area.setId(userRegion.getAreaId());
                }
                areaList.add(area);
            }
        }

        return areaList;
    }

    /**
     * 根据ParentIds和type获取所有下级的数据  add on 2019-10-21
     * @param area
     * @return
     */
    public List<Long> findIdByParentIdsAndType(Area area){
        List<Long> longListFromMS = msSysAreaService.findIdByParentIdsAndType(area);  //add on 2020-7-29
        return longListFromMS;
    }

    /**
     * 通过区域id列表查找区域的详细信息(为安维人员获取区域信息) add on 2019-11-7
     * @param areaIds
     * @return
     */
    public List<Area> findEngineerAreas(List<Long> areaIds) {
        List<Area> areaList = Lists.newArrayList();
        if (areaIds != null && !areaIds.isEmpty()) {
            return findAreasForServicePointOrEngineer(areaIds);
        }
        return Lists.newArrayList();
    }

    public List<Area> findAreasForServicePointOrEngineer(List<Long> areaIds) {
        List<Area> areaListFromMS = msSysAreaService.findAreasForServicePointOrEngineer(areaIds);  // add on 2020-7-29
        return areaListFromMS;
    }

    public List<Area> findServicePointAreas(List<Long> areaIds) {
        List<Area> areaList = Lists.newArrayList();
        if (areaIds != null && !areaIds.isEmpty()) {
            if (areaIds.size() >200) {
                List<Area> finalAreaList = Lists.newArrayList();
                Lists.partition(areaIds, 200).forEach(ids->{
                    List<Area> areaListFromDb = findAreasForServicePointOrEngineer(ids);
                    if (areaListFromDb != null && !areaListFromDb.isEmpty()) {
                        finalAreaList.addAll(areaListFromDb);
                    }
                });
                if (finalAreaList != null && !finalAreaList.isEmpty()) {
                    areaList.addAll(finalAreaList);
                }
            } else {
                areaList = findAreasForServicePointOrEngineer(areaIds);
            }
        }
        return areaList;
    }

    public NameValuePair<Integer,Integer> findAllAreaCountForRPT() {
        NameValuePair<Integer,Integer> nameValuePair = msSysAreaService.findAllAreaCountForRPT();
        return nameValuePair;
    }
}