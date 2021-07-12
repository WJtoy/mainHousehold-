package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.LongIDTreeEntity;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.AreaTimeLinessDao;
import com.wolfking.jeesite.modules.md.entity.AreaTimeLiness;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSAreaTimeLinessService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 区域产品时效奖励开关表服务
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AreaTimeLinessService extends LongIDCrudService<AreaTimeLinessDao, AreaTimeLiness> {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AreaService areaService;

    @Autowired
    private MSAreaTimeLinessService msAreaTimeLinessService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;

    /**
     * 查询列表数据
     *
     * @param entity
     * @return
     */
    @Override
    public List<AreaTimeLiness> findList(AreaTimeLiness entity) {
        List<Long> areaIdList = Lists.newArrayList();
        Map<Long, String> areaMap = Maps.newHashMap();
        List<Area> areaList = Lists.newArrayList();
        Area area = Optional.ofNullable(entity).map(AreaTimeLiness::getArea).orElse(null);
        if (area != null) {
            areaList =  areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getParent().getId());
            if (!ObjectUtils.isEmpty(areaList)) {
                areaIdList = areaList.stream().map(Area::getId).collect(Collectors.toList());
                areaMap = areaList.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, LongIDTreeEntity::getName));
            }
        }
        Map<Long, String> finalAreaMap = areaMap;
        List<Area> lostAreaList = Lists.newArrayList();
        List<AreaTimeLiness> areaTimeLinessListFromMS = msAreaTimeLinessService.findListByAreaIdsForMD(areaIdList);
        if (!ObjectUtils.isEmpty(areaTimeLinessListFromMS)) {
            List<Long> existAreaIdList = Lists.newArrayList();
            areaTimeLinessListFromMS.stream().forEach(a->{
                a.getArea().setName(finalAreaMap.get(a.getArea().getId()));
                existAreaIdList.add(a.getArea().getId());
            });
            lostAreaList = areaList.stream().filter(a->!existAreaIdList.contains(a.getId())).collect(Collectors.toList());
        } else {  //没有数据则返回缺省数据
            if (!ObjectUtils.isEmpty(areaList)) {
                lostAreaList.addAll(areaList);
            }
        }
        if (!ObjectUtils.isEmpty(lostAreaList) ) {
            for(Area areaEntity : lostAreaList) {
                AreaTimeLiness areaTimeLiness = new AreaTimeLiness();
                areaTimeLiness.setArea(areaEntity);
                areaTimeLinessListFromMS.add(areaTimeLiness);
            }
        }
        return areaTimeLinessListFromMS;
    }

    /**
     * 查询列表数据
     *
     * @param entity
     * @return
     */

    public List<AreaTimeLiness> findListNew(AreaTimeLiness entity) {
        List<Long> areaIdList = Lists.newArrayList();
        Map<Long, String> areaMap = Maps.newHashMap();
        List<Area> areaList = Lists.newArrayList();
        Area area = Optional.ofNullable(entity).map(AreaTimeLiness::getArea).orElse(null);
        if (area != null) {
            areaList =  areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getId());
            if (!ObjectUtils.isEmpty(areaList)) {
                areaIdList = areaList.stream().map(Area::getId).collect(Collectors.toList());
                areaMap = areaList.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, LongIDTreeEntity::getName));
            }
        }
        Map<Long, String> finalAreaMap = areaMap;
        List<Area> lostAreaList = Lists.newArrayList();
        List<AreaTimeLiness> areaTimeLinessListFromMS = msAreaTimeLinessService.findListByAreaIdsForMD(areaIdList);
        if (!ObjectUtils.isEmpty(areaTimeLinessListFromMS)) {
            List<Long> existAreaIdList = Lists.newArrayList();
            areaTimeLinessListFromMS.stream().forEach(a->{
                a.getArea().setName(finalAreaMap.get(a.getArea().getId()));
                existAreaIdList.add(a.getArea().getId());
            });
            lostAreaList = areaList.stream().filter(a->!existAreaIdList.contains(a.getId())).collect(Collectors.toList());
        } else {  //没有数据则返回缺省数据
            if (!ObjectUtils.isEmpty(areaList)) {
                lostAreaList.addAll(areaList);
            }
        }
        if (!ObjectUtils.isEmpty(lostAreaList) ) {
            for(Area areaEntity : lostAreaList) {
                AreaTimeLiness areaTimeLiness = new AreaTimeLiness();
                areaTimeLiness.setArea(areaEntity);
                areaTimeLinessListFromMS.add(areaTimeLiness);
            }
        }
        return areaTimeLinessListFromMS;
    }

    public List<Map<String,Object>>  findAllProductCategoryList(AreaTimeLiness entity) {
        List<Map<String,Object>> returnList = Lists.newArrayList();
        List<Long> areaIdList = Lists.newArrayList();
        List<Area> areaList = Lists.newArrayList();
        Area area = Optional.ofNullable(entity).map(AreaTimeLiness::getArea).orElse(null);
        if (area != null) {
            areaList =  areaService.findListByParent(Area.TYPE_VALUE_CITY, area.getId());
            if (!ObjectUtils.isEmpty(areaList)) {
                areaIdList = areaList.stream().map(Area::getId).collect(Collectors.toList());
            }
        }

        if (ObjectUtils.isEmpty(areaList)) {
            return Lists.newArrayList();
        }

        List<AreaTimeLiness> areaTimeLinessListFromMS = msAreaTimeLinessService.findListByAreaIdsAndProductCategoryForMD(areaIdList);
        Map<Long, List<AreaTimeLiness>>  listMap = Maps.newHashMap();
        if (!ObjectUtils.isEmpty(areaTimeLinessListFromMS)) {
            listMap = areaTimeLinessListFromMS.stream().collect(Collectors.groupingBy(a->a.getArea().getId()));
        }

        // 获取所有的有效
        List<Long> proudctCategoryIds = msProductCategoryNewService.findIdListForMD();
        if (!ObjectUtils.isEmpty(proudctCategoryIds)) {
            proudctCategoryIds = proudctCategoryIds.stream().sorted().collect(Collectors.toList());
        } else {
            proudctCategoryIds = Lists.newArrayList();
        }


        for(int i=0; i< areaList.size(); i++) {
            Area currentArea = areaList.get(i);
            Map<String, Object> objectMap = Maps.newHashMap();
            objectMap.put("areaId", currentArea.getId());
            objectMap.put("areaName", currentArea.getName());

            List<Map<String,Object>> productCategoryMapList = Lists.newArrayList();

            List<AreaTimeLiness> currentAreaTimeLinessList = listMap.get(currentArea.getId());
            Map<Long,AreaTimeLiness> currentAreaTimeLinessMap = ObjectUtils.isEmpty(currentAreaTimeLinessList)?Maps.newHashMap():currentAreaTimeLinessList.stream().collect(Collectors.toMap(r->r.getProductCategoryId(),r->r));
            for(int j=0; j< proudctCategoryIds.size(); j++) {
                Map<String,Object> categoryMap = Maps.newHashMap();
                Long productCategoryId = proudctCategoryIds.get(j);
                AreaTimeLiness areaTimeLiness = currentAreaTimeLinessMap.get(productCategoryId);
                int isOpen = 0;
                if (areaTimeLiness == null) {
                    isOpen =0;
                } else {
                    isOpen = areaTimeLiness.getIsOpen();
                    categoryMap.put("id", areaTimeLiness.getId());
                }
                categoryMap.put("productCategoryId", productCategoryId);
                categoryMap.put("isOpen", isOpen);

                productCategoryMapList.add(categoryMap);
            }
            objectMap.put("itemList", productCategoryMapList);
            returnList.add(objectMap);
        }

        return returnList;
    }

// mark on 2020-4-11 begin
//    @Override
//    @Transactional()
//    public void save(AreaTimeLiness model){
//        super.save(model);
//        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_AREA_TIMELINESS_ALL)){
//            Area area = areaService.get(model.getArea().getId());
//            model.setArea(area);
//            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_AREA_TIMELINESS_ALL, model, model.getArea().getId(), 0);
//        }
//    }
    // mark on 2020-4-11 end

    @Transactional()
    public void saveBatch(List<AreaTimeLiness> list){
        if(list!=null && list.size()>0){
            msAreaTimeLinessService.batchSaveForMD(list);
        }
    }

}
