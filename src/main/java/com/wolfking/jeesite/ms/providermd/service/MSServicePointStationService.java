package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDServicePointStation;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointStationFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MSServicePointStationService {

    @Autowired
    private MSServicePointStationFeign msServicePointStationFeign;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private AreaService areaService;




    /**
     * 根据id获取数据
     * @param id
     * @return
     */
    public ServicePointStation getById(Long id){
//        return MDUtils.getById(id,ServicePointStation.class,msServicePointStationFeign::getById);
        return null;
    }

    /**
     * 根据网点Id和服务区域id从缓存中获取网点区域信息
     * @param servicePointId
     * @param stationId
     * @return
     */
    public ServicePointStation getFromCacheByPointIdAndStationId(Long servicePointId, Long stationId) {
        //return MDUtils.getEntity(ServicePointStation.class,()->msServicePointStationFeign.getFromCacheByPointIdAndStationId(servicePointId, stationId));
        return MDUtils.getObjNecessaryConvertType(ServicePointStation.class,()->msServicePointStationFeign.getFromCacheByPointIdAndStationId(servicePointId, stationId));
    }

    /**
     * 根据服务网点获取数据
     * @param subAreaIdList
     * @return
     */
    public List<ServicePointStation> findAutoPlanList(List<Long> subAreaIdList){
        List<ServicePointStation> servicePointStationList = Lists.newArrayList();
        int pageNo = 1;
        MSPage<MDServicePointStation> mdServicePointStationMSPage = new MSPage<>();
        mdServicePointStationMSPage.setPageNo(pageNo);
        mdServicePointStationMSPage.setPageSize(100);

        MSResponse<MSPage<MDServicePointStation>> msResponse = msServicePointStationFeign.findAutoPlanList(subAreaIdList, mdServicePointStationMSPage.getPageNo(), mdServicePointStationMSPage.getPageSize());
        if(MSResponse.isSuccess(msResponse)){
            MSPage<MDServicePointStation> returnPage = msResponse.getData();
            if (returnPage != null && returnPage.getList() != null) {
                servicePointStationList.addAll(mapper.mapAsList(returnPage.getList(), ServicePointStation.class));
            }
            while (pageNo < returnPage.getPageCount()) {
                pageNo++;
                mdServicePointStationMSPage.setPageNo(pageNo);
                MSResponse<MSPage<MDServicePointStation>> whileMSResponse = msServicePointStationFeign.findAutoPlanList(subAreaIdList, mdServicePointStationMSPage.getPageNo(), mdServicePointStationMSPage.getPageSize());
                if(MSResponse.isSuccess(whileMSResponse)) {
                    MSPage<MDServicePointStation> whileReturnPage = whileMSResponse.getData();
                    if (whileReturnPage != null && whileReturnPage.getList() != null) {
                        servicePointStationList.addAll(mapper.mapAsList(whileReturnPage.getList(), ServicePointStation.class));
                    }
                }
            }
        }
        return servicePointStationList;
    }

    /**
     * 查找网点覆盖的四级区域列表
     *
     * @return
     */
    public List<Long> findCoverAreaList(){
        List<Long> subAreaIdList = Lists.newArrayList();
        int pageNo = 1;
        MSPage<MDServicePointStation> mdServicePointStationMSPage = new MSPage<>();
        mdServicePointStationMSPage.setPageNo(pageNo);
        mdServicePointStationMSPage.setPageSize(500);

        MSResponse<MSPage<Long>> msResponse = msServicePointStationFeign.findCoverAreaList( mdServicePointStationMSPage.getPageNo(), mdServicePointStationMSPage.getPageSize());
        if(MSResponse.isSuccess(msResponse)){
            MSPage<Long> returnPage = msResponse.getData();
            if (returnPage != null && returnPage.getList() != null) {
                subAreaIdList.addAll(returnPage.getList());
            }
            while (pageNo < returnPage.getPageCount()) {
                pageNo++;
                mdServicePointStationMSPage.setPageNo(pageNo);
                MSResponse<MSPage<Long>> whileMSResponse = msServicePointStationFeign.findCoverAreaList(mdServicePointStationMSPage.getPageNo(), mdServicePointStationMSPage.getPageSize());
                if(MSResponse.isSuccess(whileMSResponse)) {
                    MSPage<Long> whileReturnPage = whileMSResponse.getData();
                    if (whileReturnPage != null && whileReturnPage.getList() != null) {
                        subAreaIdList.addAll(whileReturnPage.getList());
                    }
                }
            }
        }
        return subAreaIdList;
    }

    /**
     * 通过网点id和区域id查询服务点
     * @param servicePointId
     * @param subAreaId
     * @return
     */
    public ServicePointStation getByServicePointIdAndAreaId(Long servicePointId,Long subAreaId){
        /*
        MDServicePointStation mdServicePointStation = new MDServicePointStation();
        mdServicePointStation.setServicePointId(servicePointId);
        mdServicePointStation.setSubAreaId(subAreaId);
        MSResponse<MDServicePointStation> response = msServicePointStationFeign.getByServicePointIdAndAreaId(mdServicePointStation);
        if(MSResponse.isSuccess(response)){
            ServicePointStation servicePointStation = mapper.map(response.getData(),ServicePointStation.class);
            return servicePointStation;
        }else{
            return null;
        }
        */
        // TODO: 本方法作废
        return null;
    }

    /**
     * 根据服务网点获取数据(用于web加载缓存)
     * @param servicePointStation
     * @return
     */
    public List<ServicePointStation> findList(ServicePointStation servicePointStation){
        // 改成分页从DB中获取数据
        List<ServicePointStation> returnList = Lists.newArrayList();
        int pageNo = 1;
        Page<ServicePointStation> servicePointStationPage = new Page<>();
        servicePointStationPage.setPageSize(500);
        servicePointStationPage.setPageNo(pageNo);

        Page<ServicePointStation> returnPage = MDUtils.findListForPage(servicePointStationPage, servicePointStation, ServicePointStation.class, MDServicePointStation.class, msServicePointStationFeign::findList);
        if (returnPage != null && returnPage.getList() != null) {
            returnList.addAll(returnPage.getList());
        }

        while (pageNo < returnPage.getPageCount()) {
            pageNo++;
            servicePointStationPage.setPageNo(pageNo);
            Page<ServicePointStation> whileReturnPage = MDUtils.findListForPage(servicePointStationPage, servicePointStation, ServicePointStation.class, MDServicePointStation.class, msServicePointStationFeign::findList);
            if (whileReturnPage != null && whileReturnPage.getList() != null) {
                returnList.addAll(whileReturnPage.getList());
            }
        }

       /*
       // mark on 2019-12-25  //导致获取数据太慢
       if(list!=null && list.size()>0){
            Map<Long,Area> townAreaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_TOWN);
            for(ServicePointStation item:list){
                if(item.getArea() !=null && item.getArea().getId()!=null && item.getArea().getId()>0){
                    Area area =  townAreaMap.get(item.getArea().getId());
                    if(area !=null){
                        item.setArea(area);
                        returnList.add(item);
                    }
                }
            }
        }
        */
        return returnList;
    }

    /**
     * 根据服务网点获取数据  add on 2020-8-18
     * @param servicePointStation
     * @return id,sub_area_id
     */
    public List<ServicePointStation> findSpecList(ServicePointStation servicePointStation){
        // 改成分页从DB中获取数据
        List<ServicePointStation> returnList = Lists.newArrayList();
        int pageNo = 1;
        Page<ServicePointStation> servicePointStationPage = new Page<>();
        servicePointStationPage.setPageSize(500);
        servicePointStationPage.setPageNo(pageNo);

        Page<ServicePointStation> returnPage = MDUtils.findListForPage(servicePointStationPage, servicePointStation, ServicePointStation.class, MDServicePointStation.class, msServicePointStationFeign::findSpecList);
        if (returnPage != null && returnPage.getList() != null) {
            returnList.addAll(returnPage.getList());
        }

        while (pageNo < returnPage.getPageCount()) {
            pageNo++;
            servicePointStationPage.setPageNo(pageNo);
            Page<ServicePointStation> whileReturnPage = MDUtils.findListForPage(servicePointStationPage, servicePointStation, ServicePointStation.class, MDServicePointStation.class, msServicePointStationFeign::findSpecList);
            if (whileReturnPage != null && whileReturnPage.getList() != null) {
                returnList.addAll(whileReturnPage.getList());
            }
        }

        return returnList;
    }

    /**
     * 保存网点服务点
     * @param servicePointStation
     * @param isNew
     * @return
     */
    public MSErrorCode insert(ServicePointStation servicePointStation, boolean isNew){
        return MDUtils.genericSave(servicePointStation, MDServicePointStation.class,isNew,isNew?msServicePointStationFeign::insert:msServicePointStationFeign::update);
    }


    /**
     * 删除网点服务点
     * @param servicePointStation
     * @return
     */
    public MSErrorCode delete(ServicePointStation servicePointStation){
        return MDUtils.genericSave(servicePointStation, MDServicePointStation.class, false, msServicePointStationFeign::delete);
    }


    /**
     * 批量添加或者修改
     * @param list
     * @return
     */
    public List<ServicePointStation> batchSave(List<ServicePointStation> list){
        if(list==null || list.size()<=0){
            //return new MSErrorCode(99999,"要保存的网点服务点为空,请检查!");
            return null;
        }
        List<MDServicePointStation> mdServicePointStationList = mapper.mapAsList(list,MDServicePointStation.class);
        List<List<MDServicePointStation>> saveList = Lists.partition(mdServicePointStationList,12);
        List<ServicePointStation> servicePointStationList = Lists.newArrayList();
        /*
        StringBuffer sb = new StringBuffer("");
        String errorMsg = "";
        int errorCode = 0;
        */
        for(List<MDServicePointStation> item:saveList){
            if(item !=null && item.size()>0){
                MSResponse<List<MDServicePointStation>> msResponse = msServicePointStationFeign.batchInsertOrUpdate(item);
                if (MSResponse.isSuccess(msResponse)) {
                    servicePointStationList.addAll(mapper.mapAsList(msResponse.getData(),ServicePointStation.class));
                }
            }
        }
        return servicePointStationList;
        /*
        MSErrorCode msErrorCode;
        if(StringUtils.isNotBlank(sb)){
            msErrorCode = new MSErrorCode(errorCode,sb.toString()+"保存失败.失败原因:" + errorMsg);
        }else{
            msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
        }
        return msErrorCode;
        */
    }

    /*
    // 原方法  2020-1-20
    public MSErrorCode batchSave(List<ServicePointStation> list){
        if(list==null || list.size()<=0){
            return new MSErrorCode(99999,"要保存的网点服务点为空,请检查!");
        }
        List<MDServicePointStation> mdServicePointStationList = mapper.mapAsList(list,MDServicePointStation.class);
        List<List<MDServicePointStation>> saveList = Lists.partition(mdServicePointStationList,12);
        MSResponse<Integer> msResponse = new MSResponse<>(MSErrorCode.FAILURE);
        StringBuffer sb = new StringBuffer("");
        String errorMsg = "";
        int errorCode = 0;
        for(List<MDServicePointStation> item:saveList){
            if(item !=null && item.size()>0){
                msResponse = msServicePointStationFeign.batchInsertOrUpdate(item);
                if(msResponse.getCode()>0){
                    errorMsg = msResponse.getMsg();
                    errorCode = msResponse.getCode();
                    for(int i=0;i<item.size();i++){
                        sb.append(item.get(i).getName() + ",");
                    }
                }
            }
        }
        MSErrorCode msErrorCode;
        if(StringUtils.isNotBlank(sb)){
            msErrorCode = new MSErrorCode(errorCode,sb.toString()+"保存失败.失败原因:" + errorMsg);
        }else{
            msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
        }
        return msErrorCode;
    }
    */


    /**
     * 批量删除
     * @param list
     * @return
     */
    public MSErrorCode batchDelete(List<ServicePointStation> list){
        if(list == null || list.size()<=0){
            return new MSErrorCode(99999,"要删除的网点服务点为空,请检查!");
        }
        List<MDServicePointStation> mdServicePointStationList = mapper.mapAsList(list,MDServicePointStation.class);
        List<List<MDServicePointStation>> deleteList = Lists.partition(mdServicePointStationList,12);
        MSResponse<Integer> msResponse = new MSResponse<>(MSErrorCode.FAILURE);
        for(List<MDServicePointStation> item:deleteList){
            msResponse = msServicePointStationFeign.batchDelete(item);
        }
        MSErrorCode msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
        return msErrorCode;
    }


    /**
     * 根据网点id从缓存中获取数据
     * @param servicePointId
     * @return
     */
    public List<ServicePointStation> findListFromCacheByServicePointId(Long servicePointId){
        MSResponse<List<MDServicePointStation>> msResponse = msServicePointStationFeign.findListFromCacheByServicePointId(servicePointId);
        List<ServicePointStation> servicePointStationList = Lists.newArrayList();
        if(MSResponse.isSuccess(msResponse)) {
            List<ServicePointStation> list = mapper.mapAsList(msResponse.getData(), ServicePointStation.class);
            if (list != null && list.size() > 0) {
                List<Long> countyAreaId = list.stream().map(ServicePointStation::getArea).map(Area::getParentId).distinct().collect(Collectors.toList());
                Map<Long,Area> townAreaMap = listToMap(countyAreaId);
                for (ServicePointStation item : list) {
                    if (item.getArea() != null && item.getArea().getId() != null && item.getArea().getId() > 0) {
                       // List<Area> townAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN, item.getArea().getParent().getId());
                        Area area = townAreaMap.get(item.getArea().getId());
                        if(area !=null){
                            item.setArea(area);
                            servicePointStationList.add(item);
                        }
                    }
                }
            }
        }
        return servicePointStationList;
    }

    /**
     * 根据区域id返回街道map
     * @param countAreaIdList
     * @return
     */
    public Map<Long,Area> listToMap(List<Long> countAreaIdList){
        List<Area> allTownAreaList = Lists.newArrayList();
        if(countAreaIdList !=null && countAreaIdList.size()>0){
            for(Long areaId:countAreaIdList){
                List<Area> townAreaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,areaId);
                if(townAreaList!=null && townAreaList.size()>0){
                    allTownAreaList.addAll(townAreaList);
                }
            }
        }
        Map<Long, Area> townAreaMap = allTownAreaList.stream().collect(Collectors.toMap(Area::getId, a -> a, (k1, k2) -> k1));
        if(townAreaMap !=null && townAreaMap.size()>0){
            return townAreaMap;
        }else{
            return Maps.newHashMap();
        }
    }

    public Long  autoPlanByServicePointId(Long servicePointId) {
        MSResponse<Long> msResponse = msServicePointStationFeign.autoPlanByServicePointId(servicePointId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    public List<MDServicePointStation> findSpecListByServicePointId(Long servicePointId) {
        List<MDServicePointStation> servicePointStationList = Lists.newArrayList();
        int pageNo = 1;
        int pageSize = 500;
        MSResponse<MSPage<MDServicePointStation>>  returnResponse = msServicePointStationFeign.findSpecListByServicePointId(servicePointId, pageNo, pageSize);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<MDServicePointStation> msPage = returnResponse.getData();
            Optional.ofNullable(msPage.getList()).ifPresent(servicePointStationList::addAll);

            int pageCount = msPage.getPageCount();
            pageNo++;
            while (pageNo < pageCount) {
                MSResponse<MSPage<MDServicePointStation>> whileResponse = msServicePointStationFeign.findSpecListByServicePointId(servicePointId, pageNo, pageSize);
                if (MSResponse.isSuccess(whileResponse)) {
                    MSPage<MDServicePointStation> whilePage = whileResponse.getData();
                    Optional.ofNullable(whilePage.getList()).ifPresent(servicePointStationList::addAll);
                }
            }
        }
        return servicePointStationList;
    }

    public List<Map<String,Object>> findSpecListByServicePointIdToMapList(Long servicePointId) {
        List<MDServicePointStation> servicePointStationList = findSpecListByServicePointId(servicePointId);
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            Map<Long, List<Long>> maps = servicePointStationList.stream().collect(Collectors.groupingBy(r -> r.getAreaId(), Collectors.mapping(r -> r.getSubAreaId(), Collectors.toList())));
            List<Map<String,Object>> mapList = Lists.newArrayList();
            maps.forEach((k,v)->{
                Map<String,Object> map = Maps.newHashMap();
                map.put("areaId", k);
                map.put("subAreaIds", v);
                mapList.add(map);
            });
            return mapList;
        }
        return Lists.newArrayList();
    }
}
