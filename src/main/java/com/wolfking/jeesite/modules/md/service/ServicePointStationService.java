package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointStationMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncType;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.ServicePointStationDao;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.mq.sender.ServicePointStationSender;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointStationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 网点服务点
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointStationService extends LongIDCrudService<ServicePointStationDao,ServicePointStation> {

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private ServicePointStationSender servicePointStationSender;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private AreaService areaService;

    @Value("${SyncServicePoint2ES}")
    private boolean syncServicePoint2ES;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSServicePointStationService msServicePointStationService;

    /**
     * 通过id获取网点服务点
     * @param
     * @return
     */
    /*
    // 没有地方要调用 //mark on 2019-12-26
    public ServicePointStation get(Long id) {
        //return dao.get(id); //mark on 2019-11-14改为调用微服务 getById方法
        //return msServicePointStationService.getById(id);
        // add on 2019-12-24 begin
        ServicePointStation servicePointStation = dao.get(id);
        ServicePointStation servicePointStationFromMS = msServicePointStationService.getById(id);
        getCompare("get", servicePointStation, servicePointStationFromMS);
        // add on 2019-12-24 end
        return servicePointStation;
    }


    @Deprecated
    public void getCompare(String methodName, ServicePointStation db, ServicePointStation ms) {
        // 辅助方法 //add on 2019-12-24
        try {
            String msg = "";
            if (ms == null) {
                if (db != null) {
                    msg = "ms=null,db:"+db.toString();
                }
            } else {
                if (db != null) {
                    if (!db.getServicePoint().getId().equals(ms.getServicePoint().getId())
                        || !db.getArea().getParent().getId().equals(ms.getArea().getParent().getId())
                        || !db.getArea().getId().equals(ms.getArea().getId())
                        || !db.getAutoPlanFlag().equals(ms.getAutoPlanFlag())
                    ) {
                        msg = "ms:"+ms.toString()+",db:"+db.toString();
                    }
                } else {
                    msg = "db=null,ms:" + ms.toString();
                }
            }
            if (msg !="") {
                msg = "服务区域DB与MS不一致," + msg;
                LogUtils.saveLog("基础资料","ServicePointStationService.getCompare()."+methodName, msg, null, UserUtils.getUser());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
   */
    /*
    // 此方法没有地方调用被注释 // mark on 2019-12-25 begin
    public ServicePointStation get(Long servicePointId,Long ServicePointStationId) {
        //return Optional.ofNullable(getServicePointStationCache(servicePointId,ServicePointStationId)).orElse(dao.get(ServicePointStationId));  //mark on 2019-6-19
        return getServicePointStationCache(servicePointId,ServicePointStationId);
    }
    // mark on 2019-12-25 end
    */

    public ServicePointStation getByServicePointIdAndAreaId(ServicePointStation servicePointStation) {
        //return dao.getByServicePointIdAndAreaId(servicePointStation);
        /*
        // mark on 2019-12-24 begin
        List<ServicePointStation> servicePointStationList = findListFromCache(servicePointStation.getServicePoint().getId());
        ServicePointStation retServicePointStation = null;
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            retServicePointStation = servicePointStationList.stream().filter(r->r.getArea().getId().equals(servicePointStation.getArea().getId())).findFirst().orElse(null);
        }
        if (retServicePointStation == null) {
            //retServicePointStation = dao.getByServicePointIdAndAreaId(servicePointStation); mark on 2019-11-21
            retServicePointStation = msServicePointStationService.getByServicePointIdAndAreaId(servicePointStation.getServicePoint().getId(),servicePointStation.getArea().getId());
            if (retServicePointStation != null){
                Area area = retServicePointStation.getArea();
                if (area != null) {
                    Area areaFromCache = areaService.getFromCache(area.getId());
                    area.setType(areaFromCache.getType());
                    area.setName(areaFromCache.getName());
                }
            }
        }else{
            ServicePoint servicePoint = new ServicePoint(servicePointStation.getServicePoint().getId());
            retServicePointStation.setServicePoint(servicePoint);
        }
        return retServicePointStation;
        // mark on 2019-12-24 end
        */

        // add on 2019-12-24 begin
        // TODO: 此方法将作废.因为:generateServicePointStation()方法无处调用,后续此方法包括微服务方法将作废  2019-12-27
        //ServicePointStation retServicePointStation = dao.getByServicePointIdAndAreaId(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station
        ServicePointStation retServicePointStationFromMS = msServicePointStationService.getByServicePointIdAndAreaId(servicePointStation.getServicePoint().getId(),servicePointStation.getArea().getId());
        return retServicePointStationFromMS;
        /*
        //mark on 2020-1-20 web端去md_servicepoint_station
        getCompare("getByServicePointIdAndAreaId", retServicePointStation, retServicePointStationFromMS);
        if (retServicePointStation != null){
            Area area = retServicePointStation.getArea();
            if (area != null) {
                Area areaFromCache = areaService.getFromCache(area.getId());
                area.setType(areaFromCache.getType());
                area.setName(areaFromCache.getName());
            }
        }
        return retServicePointStation;
         */
        // add on 2019-12-24 end
    }

    public List<ServicePointStation> findList(ServicePointStation servicePointStation) {
        /*
        // mark on 2019-12-24
        List<ServicePointStation> servicePointStationList = findListFromCache(servicePointStation.getServicePoint().getId());
        return servicePointStationList;
        */
        // add on 2019-12-24 begin
        //List<ServicePointStation> servicePointStationList = dao.findList(servicePointStation);  //mark on 2020-1-20   web端去md_servicepoint_station
        List<ServicePointStation> servicePointStationListFromMS = msServicePointStationService.findList(servicePointStation);
        //findListCompare("servicePointStationService.findList", servicePointStationList, servicePointStationListFromMS);  //mark on 2020-1-20
        //return servicePointStationList;   //mark on 2020-1-20
        return servicePointStationListFromMS;
        // add on 2019-12-24 end
    }

    @Deprecated
    public void findListCompare(String methodName, List<ServicePointStation> dbList, List<ServicePointStation> msList) {
        //  辅助方法  // add on 2019-12-24
        try{
            String msg = "";
            if (org.springframework.util.ObjectUtils.isEmpty(msList)) {
                if (!org.springframework.util.ObjectUtils.isEmpty(dbList)) {
                    Map<Long,List<Long>> dbMap = dbList.stream().collect(Collectors.groupingBy(r->r.getServicePoint().getId(), Collectors.mapping(r->r.getId(), Collectors.toList())));
                    msg = "db:" + dbMap + ",ms为空.";
                }
            } else {
                boolean isEquals = true;
                List<Long> msIds = msList.stream().map(r->r.getId()).sorted().collect(Collectors.toList());
                Map<Long,List<Long>> msMap = msList.stream().collect(Collectors.groupingBy(r->r.getServicePoint().getId(), Collectors.mapping(r->r.getId(), Collectors.toList())));
                if (!org.springframework.util.ObjectUtils.isEmpty(msList)) {
                    List<Long> dbIds = dbList.stream().map(r->r.getId()).sorted().collect(Collectors.toList());
                    Map<Long,List<Long>> dbMap = dbList.stream().collect(Collectors.groupingBy(r->r.getServicePoint().getId(), Collectors.mapping(r->r.getId(), Collectors.toList())));
                    if (dbIds.size() != msIds.size()) {
                        isEquals = false;
                    }
                    if (isEquals) {
                        if (dbIds.stream().filter(x->!msIds.contains(x)).count() >0) {
                            isEquals = false;
                        }
                    }
                    if (isEquals) {
                        if (msIds.stream().filter(x->!dbIds.contains(x)).count() >0) {
                            isEquals = false;
                        }
                    }
                    if (!isEquals) {
                        msg = "ms:" + msMap.toString()+",DB："+ dbMap.toString();
                    }
                } else {
                    msg = "ms:" + msMap.toString()+",DB为空";
                }
            }
            if (msg != "") {
                msg = "服务区域DB与MS返回不一致," + msg;
                LogUtils.saveLog("基础资料", "ServicePointStationService.findListCompare." + methodName, msg, null, UserUtils.getUser());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }


    /*
    //mark on 2020-1-20 web端去md_servicepoint_station
    public List<ServicePointStation> findAutoPlanList(ServicePointStation servicePointStation) {
        List<ServicePointStation> servicePointStationList = dao.findAutoPlanList(servicePointStation);
        return servicePointStationList;
    }
    */

    public List<ServicePointStation>  findAutoPlanListNew(List<Long> countyAreaIdList) {
        // add on 2019-10-9
        //List<ServicePointStation> servicePointStationList = dao.findAutoPlanList(servicePointStation); // mark on 2019-11-13 调用微服务 findAutoPlanList方法
        //调用微服务 add on 2019-11-21
        List<ServicePointStation> servicePointStationList = msServicePointStationService.findAutoPlanList(countyAreaIdList);
        if (servicePointStationList != null && !servicePointStationList.isEmpty()) {
            // 过滤数据
            List<Long> servicePointIds = servicePointStationList.stream().map(t->t.getServicePoint().getId()).distinct().collect(Collectors.toList());
            Map<Long,ServicePoint> servicePointMap = Maps.newHashMap();

            // region 获取网点信息
            List<ServicePoint> servicePointList = Lists.newArrayList();
            if (servicePointIds != null && !servicePointIds.isEmpty()) {
                List<MDServicePointViewModel> servicePointViewModelList = msServicePointService.findBatchByIdsByCondition(servicePointIds, Arrays.asList("id","name"),null);
                if (servicePointViewModelList != null && !servicePointViewModelList.isEmpty()) {
                    servicePointList=  servicePointViewModelList.stream().map(r->{
                        ServicePoint servicePoint = new ServicePoint();
                        servicePoint.setId(r.getId());
                        servicePoint.setName(r.getName());
                        return servicePoint;
                    }).collect(Collectors.toList());
                }
                servicePointMap = servicePointList.stream().collect(Collectors.toMap(ServicePoint::getId, Function.identity()));
            }
            // endregion 获取网点信息

            Map<Long,ServicePoint> finalServicePointMap = servicePointMap;
            //Map<Long,Area> townAreaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_TOWN);
            List<Long> areaIdList =  servicePointStationList.stream().map(ServicePointStation::getArea).map(Area::getParentId).distinct().collect(Collectors.toList());
            Map<Long,Area> townAreaMap = msServicePointStationService.listToMap(areaIdList);
            Map<Long,Area> finalTownAreaMap = townAreaMap;
            servicePointStationList.stream().forEach(entity->{
                ServicePoint servicePoint = finalServicePointMap.get(entity.getServicePoint().getId());
                if (servicePoint != null){
                    entity.setServicePoint(servicePoint);
                }
                if(entity.getArea()!=null && entity.getArea().getId()!=null && entity.getArea().getId()>0){
                    Area area = finalTownAreaMap.get(entity.getArea().getId());
                    if(area!=null){
                        entity.setArea(area);
                    }
                }
//                log.warn("区域：{}", entity.getArea().getName());
            });
        }
        return servicePointStationList;
    }

    public List<ServicePointStation> findServicePointStationAreaList(Long areaId) {
        //
        // 通过市获取区县 及乡/镇/街道下对应的服务点列表
        //
        List<ServicePointStation> servicePointStationList = Lists.newArrayList();

        Area area = areaService.getFromCache(areaId);
        List<Area> countyAreaList = Lists.newArrayList();
        if (area != null && area.getType().equals(Area.TYPE_VALUE_CITY)){
            countyAreaList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,areaId);   //通过城市id获取县/区列表
        } else if (area != null && area.getType().equals(Area.TYPE_VALUE_COUNTY)) {
            countyAreaList.add(area);
        } else {
            return servicePointStationList;
        }

        List<Long> countyAreaIdList = countyAreaList.stream().map(Area::getId).distinct().collect(Collectors.toList());
        List<ServicePointStation> autoPlanServicePointStations = findAutoPlanListNew(countyAreaIdList);
        Map<Long,List<ServicePointStation>> servicePointStationMap = autoPlanServicePointStations.stream().collect(Collectors.groupingBy(t->t.getArea().getId()));
        if (!ObjectUtils.isEmpty(countyAreaList)) {
            countyAreaList.stream().forEach(countyArea->{
                List<Area>  areaList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,countyArea.getId());   //通过区县id获取乡/镇/街道列表

                if (!ObjectUtils.isEmpty(areaList)) {
                    areaList.stream().forEach(townArea -> {
                        ServicePoint servicePoint = new ServicePoint();
                        servicePoint.setAutoPlanFlag(1);   // 自动派单
                        ServicePointStation servicePointStation = ServicePointStation.builder()
                                .area(new Area(townArea.getId()))
                                .servicePoint(servicePoint)
                                .build();

                        List<ServicePointStation> autoPlanServicePointStationList = servicePointStationMap.get(townArea.getId());
                        if (autoPlanServicePointStationList != null && !autoPlanServicePointStationList.isEmpty()) {
                            autoPlanServicePointStationList.stream().forEach(r->{
                                r.getArea().setParent(countyArea);
                            });
                            servicePointStationList.addAll(autoPlanServicePointStationList);
                        } else {
                            //  生成一笔有区域的空数据
                            Area cachedArea = areaService.getFromCache(townArea.getId());
                            servicePointStation.setArea(cachedArea);
                            servicePointStation.getArea().setParent(countyArea);
                            servicePointStationList.add(servicePointStation);
                        }
                    });
                }
            });
        }
        return servicePointStationList;
    }

    /*public List<ServicePoint> findNoConfigServicePointStationList(Page<ServicePoint> page,ServicePoint servicePoint,Area area) {
        //
        // 查找没有配置当前区域的网点
        //
        page = servicePointService.findPage(page, servicePoint);
        List<ServicePoint> servicePointList = page.getList();

        //剔除已配置的服务点的网点
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .area(new Area(area.getId()))
                .build();
        List<ServicePointStation> servicePointStationList = findAutoPlanList(servicePointStation);
        List<ServicePoint> existedServicePointList = null;
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            existedServicePointList = servicePointStationList.stream().map(ServicePointStation::getServicePoint).collect(Collectors.toList());
        }

        if (!ObjectUtils.isEmpty(existedServicePointList)) {
            servicePointList.removeAll(existedServicePointList);  //从servicePointList中剔除existedServicePointList
        }

        return servicePointList;
    }*/


    /*@Deprecated
    public Page<ServicePointStation> findPage(Page<ServicePointStation> page, ServicePointStation servicePointStationEntity) {
        page.setPageSize(2);
        String strAreaFullName = "广东省佛山市顺德区";
        servicePointStationEntity.setPage(page);

        List<ServicePointStation> servicePointStationList = findAutoPlanList(servicePointStationEntity);

        if (!org.springframework.util.ObjectUtils.isEmpty(servicePointStationList)) {
            // 对过滤的数据看看是否有坐标
            servicePointStationList.stream().forEach(entity->{
                //System.out.println(String.format("检查服务点:%s的坐标,经度:%s,纬度:%s",entity.getName(),entity.getLongtitude(),entity.getLatitude()));
                if (entity.getLatitude().equals(0D) || entity.getLatitude().equals(0D)) {
                    String[] stationAreaArray = AreaUtils.getLocation(strAreaFullName.concat(entity.getAddress()));

                    if (ObjectUtils.isEmpty(stationAreaArray) && stationAreaArray.length == 2) {
                        entity.setLongtitude(Double.valueOf(stationAreaArray[0]));
                        entity.setLatitude(Double.valueOf(stationAreaArray[1]));
                        //System.out.println(String.format("重新获取服务点:%s的坐标,经度:%s,纬度:%s",entity.getName(),entity.getLongtitude(),entity.getLatitude()));
                    }
                }
            });
        }
        page.setList(servicePointStationList);
        return page;
    }*/

    /*public List<Area> getStationAreaList(Long id) {
        return dao.getStationAreas(id);
    }*/

    @Transactional(readOnly = false)
    public void save(ServicePointStation servicePointStation) {
        boolean isNewRecord = servicePointStation.getIsNewRecord();
        //super.save(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station
        MSErrorCode msErrorCode = msServicePointStationService.insert(servicePointStation,isNewRecord);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务保存区域服务点失败.失败原因:" + msErrorCode.getMsg());
        }
        if (syncServicePoint2ES) {
            if (isNewRecord) {
                pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.ADD);
            } else {
                pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.UPDATE);
            }
        }
        //updateServicePointStationCache(servicePointStation);  //mark on 2020-1-20
    }

    @Transactional(readOnly = false)
    public void saveForWeb(ServicePointStation servicePointStation) {
        boolean isNewRecord = servicePointStation.getIsNewRecord();
        //super.save(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station
        //servicePointStation.setIsNewRecord(isNewRecord);   //mark on 2020-1-20

        if (syncServicePoint2ES) {
            if (isNewRecord) {
                pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.ADD);
            } else {
                pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.UPDATE);
            }
        }
        //updateServicePointStationCache(servicePointStation); //mark on 2020-1-20
    }

    @Transactional(readOnly = false)
    public TwoTuple<MQSyncType.SyncType,ServicePointStation> simpleSave(ServicePointStation servicePointStation) {
        boolean isNewRecord = servicePointStation.getIsNewRecord();
        //super.save(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station
        //servicePointStation.setIsNewRecord(isNewRecord); // add on 2019-12-26  //mark on 2020-1-20

        TwoTuple<MQSyncType.SyncType,ServicePointStation> twoTuple = new TwoTuple<>();
        if (isNewRecord) {
            twoTuple.setAElement(MQSyncType.SyncType.ADD);
        } else {
            twoTuple.setAElement(MQSyncType.SyncType.UPDATE);
        }
        twoTuple.setBElement(servicePointStation);

        //updateServicePointStationCache(servicePointStation);  //mark on 2020-1-20
        return twoTuple;
    }

    public void handlerMessageAndCache(List<TwoTuple<MQSyncType.SyncType,ServicePointStation>> twoTupleList) {
        if (twoTupleList != null && !twoTupleList.isEmpty()) {
            twoTupleList.stream().forEach(r->{
                if (syncServicePoint2ES) {
                    pushServicePointStationToEs(r.getBElement(),r.getAElement());
                }
                //updateServicePointStationCache(r.getBElement()); //mark on 2020-1-20
            });
//            if (syncServicePoint2ES) {
//                //pushServicePointStationListToES(twoTupleList);  //保留，用于批量发送
//            }
        }
    }



    @Transactional(readOnly = false)
    public void delete(ServicePointStation servicePointStation) {
        servicePointStation.preUpdate();
        servicePointStation.setDelFlag(ServicePointStation.DEL_FLAG_DELETE);
        //dao.delete(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station
        //调用微服务 add On 2019-11-14
        MSErrorCode msErrorCode = msServicePointStationService.delete(servicePointStation);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务删除网点服务点失败.失败原因" + msErrorCode.getMsg());
        }
        // end
        if (syncServicePoint2ES) {
            pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.DELETE);
        }
        //removeServicePointStationCache(servicePointStation); //mark on 2020-2-2
    }

    @Transactional(readOnly = false)
    public void deleteForWeb(ServicePointStation servicePointStation) {
        servicePointStation.preUpdate();
        servicePointStation.setDelFlag(ServicePointStation.DEL_FLAG_DELETE);
        //dao.delete(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station

        if (syncServicePoint2ES) {
            pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.DELETE);
        }
        //removeServicePointStationCache(servicePointStation);  //mark on 2020-2-2
    }

    @Transactional(readOnly = false)
    public void enable(ServicePointStation servicePointStation) {
        servicePointStation.preUpdate();
        servicePointStation.setDelFlag(ServicePointStation.DEL_FLAG_NORMAL);
        //dao.delete(servicePointStation);  //mark on 2020-1-20 web端去md_servicepoint_station
        // 调用微服务 add on 2019-11-14
        MSErrorCode msErrorCode = msServicePointStationService.delete(servicePointStation);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("调用微服务启动网点服务点失败.失败原因"+ msErrorCode.getMsg());
        }
        //end
        //updateServicePointStationCache(servicePointStation);  //mark on 2020-1-20
        if (syncServicePoint2ES) {
            pushServicePointStationToEs(servicePointStation, MQSyncType.SyncType.ADD);
        }
    }


    /**
     * 通过网点id查找该网点是否有自动派单(即autoPlanFlag 为1)
     * @param servicePointId
     * @return
     */
    public Long autoPlanByServicePointId(Long servicePointId){
        //return dao.autoPlanByServicePointId(servicePointId);
        //Long id = dao.autoPlanByServicePointId(servicePointId); //mark on 2020-1-20 web端去md_servicepoint_station
        Long idFromMS = msServicePointStationService.autoPlanByServicePointId(servicePointId);
        return idFromMS;
        /*
        //mark on 2020-1-20
        try {
            if (idFromMS != null && idFromMS.equals(id)) {
                try {
                    String msg = "网点服务区域DB与MS不一致,DB:" + id + ",ms:" + idFromMS;
                    LogUtils.saveLog("基础资料", "ServicePointStationService.autoPlanByServicePointId", msg, null, UserUtils.getUser());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex){}
        return id;*/
    }


    /**
     * 生成新的服务点
     * @param val  区域id
     * @param inputAutoPlanIds  自动派单id列表
     * @return
     */
    public ServicePointStation generateServicePointStation(Long val, ServicePointStation servicePointStation, List<Long> inputAutoPlanIds) {
        ServicePointStation servicePointStationEntity = ServicePointStation.builder().build();
        BeanUtils.copyProperties(servicePointStation, servicePointStationEntity);

        Area area = areaService.getFromCache(val);
        servicePointStationEntity.setArea(area);
        // 检查服务点在缓存或者DB中是否存在
        ServicePointStation servicePointStationDb = getByServicePointIdAndAreaId(servicePointStationEntity);
        if (!ObjectUtils.isEmpty(servicePointStationDb)) {
            servicePointStationEntity = servicePointStationDb;
            if (!ObjectUtils.isEmpty(servicePointStationEntity.getArea()) && ObjectUtils.isEmpty(servicePointStationEntity.getArea().getParent())) {
                servicePointStationEntity.setArea(area);
            }
        }
        servicePointStationEntity.setName(area.getName());  //默认地名为区域简称
        servicePointStationEntity.setAddress("");           //服务区域默认为空

        Long autoPlanFlag = inputAutoPlanIds.stream().filter(r->r.equals(val)).findFirst().orElse(null);
        autoPlanFlag = autoPlanFlag == null?0:1L;
        servicePointStationEntity.setAutoPlanFlag(autoPlanFlag.intValue());
        servicePointStationEntity.setLongtitude(0D);
        servicePointStationEntity.setLatitude(0D);
        servicePointStationEntity.setRadius(0);

//        if (autoPlanFlag.intValue() == ServicePoint.AUTO_PLAN_FLAG_ENABLED) {   // 1 -- 自动派单
//            // add on 2019-6-26 begin
//            if (servicePointStationEntity.getServicePoint().getAutoPlanFlag().equals(ServicePoint.AUTO_PLAN_FLAG_DISABLED)) {
//                servicePointStationEntity.getServicePoint().setAutoPlanFlag(ServicePoint.AUTO_PLAN_FLAG_ENABLED);
//            }
        // add on 2019-6-26 end
            /*
            // 暂时注释如下代码,后续可能会用到 // mark on 2019-6-26
            String strFullAddressName = area.getFullName();
            String[] areaArray = AreaUtils.getLocation(strFullAddressName);
            Double dblLongtitude = 0D;
            Double dblLatitude = 0D;
            if (!org.springframework.util.ObjectUtils.isEmpty(areaArray)) {
                if (areaArray.length == 2) {
                    dblLongtitude = Double.valueOf(areaArray[0]);
                    dblLatitude = Double.valueOf(areaArray[1]);

                    System.out.println(String.format("街道名称:[%s] 转换成坐标为{longtitude:%s,latitude:%s}", strFullAddressName,dblLongtitude, dblLatitude));
                    servicePointStationEntity.setLongtitude(dblLongtitude);
                    servicePointStationEntity.setLatitude(dblLatitude);
                }
            } else {
            }
            */
//        }
        return servicePointStationEntity;
    }

    /**
     * 生成新的服务点
     * @param subAreaId  4级区域id
     * @param inputAutoPlanIds  自动派单id列表
     * @return
     */
    public ServicePointStation generateServicePointStationNew(Long subAreaId, ServicePointStation servicePointStation, List<Long> inputAutoPlanIds, List<ServicePointStation> servicePointStationList) {
        // add on 2019-12-26  //与generateServicePointStation()相比降低访问次数。
        ServicePointStation servicePointStationEntity = ServicePointStation.builder().build();
        BeanUtils.copyProperties(servicePointStation, servicePointStationEntity);

        Area area = areaService.getFromCache(subAreaId);
        servicePointStationEntity.setArea(area);
        // 检查服务点在缓存或者DB中是否存在
        ServicePointStation finalServicePointStation = servicePointStationEntity;
        ServicePointStation servicePointStationDb = ObjectUtils.isEmpty(servicePointStationList)?
                null: servicePointStationList.stream().filter(x->x.getServicePoint().getId().equals(finalServicePointStation.getServicePoint().getId()) && x.getArea().getId().equals(finalServicePointStation.getArea().getId())).findFirst().orElse(null);

        User user = UserUtils.getUser();
        if (!ObjectUtils.isEmpty(servicePointStationDb)) {
            servicePointStationEntity = servicePointStationDb;
            if (!ObjectUtils.isEmpty(servicePointStationEntity.getArea()) && ObjectUtils.isEmpty(servicePointStationEntity.getArea().getParent())) {
                servicePointStationEntity.setArea(area);
            }
            servicePointStationEntity.setUpdateBy(user);  //add on 2020-2-2
            servicePointStationEntity.setUpdateDate(new Date()); //add on 2020-2-2
        } else { //add on 2020-2-2 begin
            servicePointStationEntity.setCreateBy(user);
            servicePointStationEntity.setCreateDate(new Date());
            servicePointStationEntity.setUpdateBy(user);
            servicePointStationEntity.setUpdateDate(new Date());
            //add on 2020-2-2 end
        }
        servicePointStationEntity.setName(area.getName());  //默认地名为区域简称
        servicePointStationEntity.setAddress("");           //服务区域默认为空

        Long autoPlanFlag = inputAutoPlanIds.stream().filter(r->r.equals(subAreaId)).findFirst().orElse(null);
        autoPlanFlag = autoPlanFlag == null?0:1L;
        servicePointStationEntity.setAutoPlanFlag(autoPlanFlag.intValue());
        servicePointStationEntity.setLongtitude(0D);
        servicePointStationEntity.setLatitude(0D);
        servicePointStationEntity.setRadius(0);

        return servicePointStationEntity;
    }

    //region push data to ElasticSearch
    public void pushServicePointStationListToES(List<TwoTuple<MQSyncType.SyncType,ServicePointStation>> list) {
        // 保留，一次批量发送多个服务点消息
        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage = null;

        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
                .setMessageId(1121l)
                .build();

        MQSyncServicePointStationMessage.SyncServicePointStationMessage.Builder syncServicePointStationMessageBuilder = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder();

        if (!org.springframework.util.ObjectUtils.isEmpty(list)) {
            list.stream().forEach(r->{
                ServicePointStation servicePointStation = r.getBElement();
                MQSyncServicePointStationMessage.SyncStationMessage syncStationMessage = MQSyncServicePointStationMessage.SyncStationMessage.newBuilder()
                        .setStationId(servicePointStation.getId())
                        .setAreaId(servicePointStation.getArea().getParent().getId())
                        .setSubAreaId(servicePointStation.getArea().getId())
                        .setStationName(servicePointStation.getName())
                        .setAutoPlanFlag(servicePointStation.getAutoPlanFlag())
                        .build();
                syncServicePointStationMessageBuilder.addStationMessage(syncStationMessage);
            });
        }

        syncServicePointStationMessage = syncServicePointStationMessageBuilder.setMessageId(1121l)
                .setServicePointMessage(syncServicePointMessage)
                .setSyncType(MQSyncType.SyncType.UPDATE)
                .build();

        servicePointStationSender.send(syncServicePointStationMessage);
        log.warn("批量Es-ServicePointStation:{}", syncServicePointStationMessage);
    }

    public void pushServicePointStationToEs(ServicePointStation servicePointStation,MQSyncType.SyncType syncType) {
        ServicePoint servicePoint = servicePointService.get(servicePointStation.getServicePoint().getId());
        if (org.springframework.util.ObjectUtils.isEmpty(servicePoint)) {
            log.warn("通过servicepoint.id获取到的servicePoint对象为空.");
            servicePoint = servicePointStation.getServicePoint();
            if (org.springframework.util.ObjectUtils.isEmpty(servicePoint)) {
                log.warn("通过从servicePointStation中获取的servicepoint对象也为空.");
                return;
            }
        }

        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = null;
        MQSyncServicePointStationMessage.SyncStationMessage syncStationMessage = null;
        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder()
                //.setMessageId(generateMessageId())
                .setMessageId(sequenceIdService.nextId())
                .build();

        syncStationMessage = transServicePointStation(servicePointStation,syncType);      //add on 2019-9-14
        if (syncType.equals(MQSyncType.SyncType.ADD) || syncType.equals(MQSyncType.SyncType.UPDATE)) {
            syncServicePointMessage = transServicePointMessage(servicePoint,syncType);  // add on 2019-9-14

            syncServicePointStationMessage = syncServicePointStationMessage.toBuilder()
                    .setSyncType(syncType)
                    .setServicePointMessage(syncServicePointMessage)
                    .addStationMessage(syncStationMessage)
                    .build();
        } else {
            syncServicePointStationMessage = syncServicePointStationMessage.toBuilder()
                    .setSyncType(syncType)
                    .addStationMessage(syncStationMessage)
                    .build();
        }
        String json ="";
        if (!ObjectUtils.isEmpty(syncServicePointMessage )) {
            json = new JsonFormat().printToString(syncServicePointMessage);
            //log.warn("ServicePointMessage:" + json);
            //log.warn("ServicePointMessage-{}",syncServicePointMessage);
        }
        json = new JsonFormat().printToString(syncServicePointStationMessage);
        log.warn("ServicePointStationMessage:"+json);
        //log.warn("ServicePointStationMessage-{}",syncServicePointStationMessage);

        servicePointStationSender.sendRetry(syncServicePointStationMessage);
    }

    public MQSyncServicePointMessage.SyncServicePointMessage transServicePointMessage(ServicePoint servicePoint,MQSyncType.SyncType syncType) {
        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = null;

        if (syncType.equals(MQSyncType.SyncType.ADD) || syncType.equals(MQSyncType.SyncType.UPDATE)) {
            int servicePointLevel = Optional.ofNullable(servicePoint.getLevel()).map(Dict::getValue).map(r -> {
                if (!r.isEmpty()) {
                    return StringUtils.toInteger(r.trim());
                }
                return 0;
            }).orElse(0);

            int servicePointPaymentType = Optional.ofNullable(servicePoint.getFinance()).map(ServicePointFinance::getPaymentType).map(Dict::getValue).map(r -> {
                if (!r.isEmpty()) {
                    return StringUtils.toInteger(r.trim());
                }
                return 0;
            }).orElse(0);

            syncServicePointMessage = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
                    //.setMessageId(generateMessageId())
                    .setMessageId(sequenceIdService.nextId())
                    .setServicePointId(servicePoint.getId())
                    .setServicePointNo(servicePoint.getServicePointNo())
                    .setName(servicePoint.getName())
                    .setContactInfo1(servicePoint.getContactInfo1())
                    .setPaymentType(servicePointPaymentType)
                    .setLevel(servicePointLevel)
                    .setAutoPlanFlag(servicePoint.getAutoPlanFlag())
                    .build();
        }

        return syncServicePointMessage;
    }

    public MQSyncServicePointStationMessage.SyncStationMessage transServicePointStation(ServicePointStation servicePointStation,MQSyncType.SyncType syncType) {
        MQSyncServicePointStationMessage.SyncStationMessage.Builder builder = MQSyncServicePointStationMessage.SyncStationMessage.newBuilder();
        builder.setStationId(servicePointStation.getId());
        if (syncType.equals(MQSyncType.SyncType.ADD) || syncType.equals(MQSyncType.SyncType.UPDATE)) {
            //builder.setAreaId(servicePointStation.getArea().getId());  //mark on 2019-5-16
            builder.setAreaId(servicePointStation.getArea().getParent().getId());  // add on 2019-5-16
            builder.setSubAreaId(servicePointStation.getArea().getId());              // add on 2019-5-16
            builder.setStationName(servicePointStation.getName());
            builder.setStationAddress(Optional.ofNullable(servicePointStation.getAddress()).orElse(""));
            builder.setLongitude(servicePointStation.getLongtitude()==null?0:servicePointStation.getLongtitude());
            builder.setLatitude(servicePointStation.getLatitude()==null?0:servicePointStation.getLatitude());
            builder.setAutoPlanFlag(servicePointStation.getAutoPlanFlag());   // add on 2019-5-29
        }
        return builder.build();
    }
    /*
    public Long generateMessageId() {
        //随机，防止同用户产生重复id
        int workerId = ThreadLocalRandom.current().nextInt(32);
        int datacenterId = ThreadLocalRandom.current().nextInt(32);
        SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
        return sequence.nextId();
    }*/
    // endregion push data to ElasticSearch

    // region operate redis
    /**
     * 从缓存中获取网点的服务区域列表
     * @param servicePointId
     * @return
     */
    /*
    // mark on 2020-1-20
    public List<ServicePointStation> findListFromCache(Long servicePointId) {
        List<ServicePointStation> servicePointStationList = Lists.newArrayList();
        if (ObjectUtils.isEmpty(servicePointId)) {
            return null;
        }
        //调用微服务 add on 2019-11-25
        servicePointStationList = msServicePointStationService.findListFromCacheByServicePointId(servicePointId);
        if(servicePointStationList !=null && servicePointStationList.size()>0){
            return servicePointStationList;
        }
        // end
        String key = String.format(RedisConstant.MD_SERVICEPOINT_STATION, servicePointId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, key)) {
            servicePointStationList = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,ServicePointStation.class);
        }
        if (ObjectUtils.isEmpty(servicePointStationList)) {
            servicePointStationList = syncServicePointStationListCache(servicePointId);
        }
        return servicePointStationList;
    }
    */

    /**
     * 根据网点id同步所有的网点服务点列表数据到缓存中
     * @param servicePointId
     * @return
     */
    /*
    // mark on 2020-2-2 begin
    public List<ServicePointStation> syncServicePointStationListCache(Long servicePointId) {
        if (ObjectUtils.isEmpty(servicePointId)) {
            return null;
        }
        String key = String.format(RedisConstant.MD_SERVICEPOINT_STATION, servicePointId);
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .servicePoint(new ServicePoint(servicePointId))
                .build();
        //List<ServicePointStation> servicePointStationList = dao.findList(servicePointStation); //mark on 2019-11-14 调用我服务findList方法
        // add on 2019-11-14
        List<ServicePointStation> servicePointStationList = msServicePointStationService.findList(servicePointStation);
        // end
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            Set<RedisZSetCommands.Tuple> sets = servicePointStationList.stream()
                    .filter(t->t.getDelFlag().intValue() == ServicePointStation.DEL_FLAG_NORMAL)
                    .map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getId().doubleValue()))
                    .collect(Collectors.toSet());
            redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, key, sets, 0L);
        }

        return servicePointStationList;
    }
    // mark on 2020-2-2 end
     */


    public ServicePointStation getServicePointStationCache(Long servicePointId,Long servicePointStationId) {
        //ServicePointStation servicePointStation = null;  //mark on 2020-1-20
        if (ObjectUtils.isEmpty(servicePointId)) {
            return null;
        }
        if (ObjectUtils.isEmpty(servicePointStationId)) {
            return null;
        }
        return msServicePointStationService.getFromCacheByPointIdAndStationId(servicePointId, servicePointStationId);
        /*
        // mark on 2020-1-20
        String key = String.format(RedisConstant.MD_SERVICEPOINT_STATION, servicePointId);
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, key)) {
            servicePointStation = (ServicePointStation) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB,key,servicePointStationId,servicePointStationId,ServicePointStation.class);
        }

        if (ObjectUtils.isEmpty(servicePointStation)) {
            // 如果没有找到则找出当前servicepointId下的所有station写入缓存
            //List<ServicePointStation> servicePointStationList = syncServicePointStationListCache(servicePointId);  //mark on 2020-1-20
            List<ServicePointStation> servicePointStationList = msServicePointStationService.findList(servicePointStation);
            if (!ObjectUtils.isEmpty(servicePointStationList)) {
                servicePointStation = servicePointStationList.stream().filter(r->r.getId().equals(servicePointStationId)).findFirst().orElse(null);
            }
        }
        return servicePointStation;
        */
    }

    /**
     * 更新网点服务点
     * 没有:新增，有：覆盖
     *
     * @param servicePointStation
     */
    /*
    //mark on 2020-1-20
    public void updateServicePointStationCache(ServicePointStation servicePointStation) {
        Long servicePointId = Optional.ofNullable(servicePointStation)
                .map(ServicePointStation::getServicePoint)
                .map(ServicePoint::getId)
                .orElse(0L);

        String key = String.format(RedisConstant.MD_SERVICEPOINT_STATION,servicePointId);
        try {
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB,key,servicePointStation,servicePointStation.getId(),0L);
        } catch (Exception ex) {
            try {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
            } catch (Exception e1) {
                log.error("[ServiePointStationService.updateCache] update servicePointStation:{}", servicePointStation.getId(), ex);
            }
        }
    }
    */

    /**
     * 从网点中移除服务点
     *
     * @param servicePointStation
     * @return
     */
    /*
    // mark on 2020-2-2 begin
    public Boolean removeServicePointStationCache(ServicePointStation servicePointStation) {
        Long servicePointId = Optional.ofNullable(servicePointStation)
                .map(ServicePointStation::getServicePoint)
                .map(ServicePoint::getId)
                .orElse(null);

        if (ObjectUtils.isEmpty(servicePointId)){
            return false;
        }

        String key = String.format(RedisConstant.MD_SERVICEPOINT_STATION, servicePointId);
        try {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key, servicePointStation.getId(), servicePointStation.getId());
        } catch (Exception e) {
            try {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
            } catch (Exception e1) {
                log.error("removeServicePointStation", e1);
                return false;
            }
        }
        return true;
    }
    // mark on 2020-2-2 end
    */
    // endregion operate redis



    /**
     * 保存网点区域服务(客服在派单选择网点时操作)
     * @param servicePoint
     * @param areaIds
     * @return
     */
    @Transactional(readOnly = false)
    public void saveServicePointStationByAreaId(ServicePoint servicePoint,String areaIds){
        ServicePointStation servicePointStation = ServicePointStation.builder().build();
        servicePointStation.setDelFlag(ServicePointStation.DEL_FLAG_NORMAL);
        servicePointStation.setServicePoint(servicePoint);
        if (servicePointStation.getRadius() == null) {
            servicePointStation.setRadius(0);
        }
        // 处理从前端返回有效区域id列表
        String[] idsArr = areaIds.split(",");
        List<Long> inputIds = Arrays.asList(idsArr).stream().map(r->Long.valueOf(r)).collect(Collectors.toList());

        List<Long> currentAreaIds = Lists.newArrayList();
        Optional.ofNullable(inputIds).ifPresent(currentAreaIds::addAll);

        // add on 2020-5-6 begin
        // 此功能只修改一个区/县
        Long countyAreaId = null;
        if (!ObjectUtils.isEmpty(inputIds)) {
            countyAreaId = inputIds.get(0);
            Area townArea = areaService.getFromCache(countyAreaId);
            if (townArea != null && townArea.getParent() != null) {
                countyAreaId = townArea.getParent().getId();
            }
        }
        // add on 2020-5-6 end

        // 从db中获取当前网点下所有的有效的服务点
        ServicePointStation servicePointStationEnable = ServicePointStation.builder()
                .servicePoint(servicePoint)
                .build();
        List<ServicePointStation> servicePointStationList = findList(servicePointStationEnable);
        List<Long> areaIdArray = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(servicePointStationList)) {
            //areaIdArray = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
            Long finalCountyAreaId = countyAreaId;
            areaIdArray = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL) && r.getArea().getParent().getId().equals(finalCountyAreaId))
                    .map(ServicePointStation::getArea)
                    .map(Area::getId)
                    .collect(Collectors.toList());
        }

        List<Long> pastAreaIds = Lists.newArrayList();
        Optional.ofNullable(areaIdArray).ifPresent(pastAreaIds::addAll);

        //比较集合
        //输入的ids有,数据库areaIdArray没有,对数据进行插入操作
        List<Long> retainIdsCopy = Lists.newArrayList();    // 用来保存取交集和取差集的结果
        retainIdsCopy.addAll(inputIds);
        retainIdsCopy.removeAll(areaIdArray);

        List<TwoTuple<MQSyncType.SyncType,ServicePointStation>> twoTupleList = Lists.newArrayList();
        if (retainIdsCopy !=null && retainIdsCopy.size()>0) {
            List<ServicePointStation> saveServicePointStationLit = Lists.newArrayList(); // add on 2019-11-23
            for (Long val : retainIdsCopy) {
                //ServicePointStation servicePointStationEntity = generateServicePointStation(val,servicePointStation,Lists.newArrayList());  //mark on 2019-12-26
                ServicePointStation servicePointStationEntity = generateServicePointStationNew(val,servicePointStation,Lists.newArrayList(), servicePointStationList); //mark on 2019-12-26
                log.warn("生成的服务点对象:{}",servicePointStationEntity);
                saveServicePointStationLit.add(servicePointStationEntity); // add on 2019-11-23
            }

            // add on 2020-1-20 begin
            List<ServicePointStation> servicePointStations = msServicePointStationService.batchSave(saveServicePointStationLit);
            if (servicePointStations != null && !servicePointStations.isEmpty()) {
                servicePointStations.stream().forEach(servicePointStationEntity->{
                    TwoTuple<MQSyncType.SyncType,ServicePointStation> twoTuple = simpleSave(servicePointStationEntity);
                    saveServicePointStationLit.add(servicePointStationEntity);
                    twoTupleList.add(twoTuple);
                });
            }
            // add on 2020-1-20 end
        }
        // 删除
        List<Long> dbIdsCopy = Lists.newArrayList();
        dbIdsCopy.addAll(areaIdArray);
        dbIdsCopy.removeAll(inputIds);
        if (dbIdsCopy !=null && dbIdsCopy.size()>0) {
            List<ServicePointStation> deleteList = Lists.newArrayList();
            for(Long val : dbIdsCopy) {
                Area area = areaService.getFromCache(val);
                ServicePointStation entity = ServicePointStation.builder()
                        .servicePoint(servicePoint)
                        .area(area)
                        .build();
                // add on 2019-12-26 begin
                ServicePointStation retServicePointStation = ObjectUtils.isEmpty(servicePointStationList)?
                        null: servicePointStationList.stream().filter(x->x.getServicePoint().getId().equals(entity.getServicePoint().getId()) && x.getArea().getId().equals(entity.getArea().getId())).findFirst().orElse(null);
                // add on 2019-12-26 end
                if (retServicePointStation !=null) {
                    //delete(retServicePointStation);  // mark on 2019-12-28
                    deleteForWeb(retServicePointStation); // add on 2019-12-28
                    deleteList.add(retServicePointStation); // add on 2019-11-25
                }
            }
            // add on 2019-11-25 调用微服务
            if (!ObjectUtils.isEmpty(deleteList)) {
                MSErrorCode msErrorCode = msServicePointStationService.batchDelete(deleteList);
                if (msErrorCode.getCode() > 0) {
                    throw new RuntimeException("删除网点服务点失败.失败原因" + msErrorCode.getMsg());
                }
            }
            //end
        }
        // 获取该网点是否还存在自动派单的街道
        Long autoPlan = autoPlanByServicePointId(servicePoint.getId());
        if((autoPlan== null || autoPlan<=0) && servicePoint.getAutoPlanFlag()==1){
            servicePointService.updateAutoPlanFlag(servicePoint.getId(),ServicePoint.AUTO_PLAN_FLAG_DISABLED);
            log.warn("将当前网点:{} 自动派单标志置为{}.",servicePoint.getName(),ServicePoint.AUTO_PLAN_FLAG_DISABLED);
        }
        //add on 2019-10-4 begin
        //ServicePoint微服务
        ServicePoint servicePointFromMS = null;
        if (servicePointStation.getServicePoint() != null && servicePointStation.getServicePoint().getId() != null) {
            servicePointFromMS = msServicePointService.getById(servicePointStation.getServicePoint().getId());
        }
        //add on 2019-10-4 end
        if (twoTupleList != null && !twoTupleList.isEmpty()) {
            int lambdaAutoPlanFlag = ServicePoint.AUTO_PLAN_FLAG_DISABLED;
            final ServicePoint finalServicePoint = servicePointFromMS;
            twoTupleList.stream().forEach(r->{
                // add on 2019-10-4 begin
                //ServicePoint微服务
                if (r.getBElement().getServicePoint() ==null) {
                    r.getBElement().setServicePoint(finalServicePoint);
                }
                // add on 2019-10-4 end
                r.getBElement().getServicePoint().setAutoPlanFlag(lambdaAutoPlanFlag);
            });
            handlerMessageAndCache(twoTupleList);
        }

        // add on 2020-10-13 begin
        StringBuilder strLog = new StringBuilder();
        strLog.append("servicePointId:")
                .append(servicePoint.getId())
                .append(",派单界面修改区域，保存前4级区域id：")
                .append((CollectionUtils.isEmpty(pastAreaIds)?"":pastAreaIds.stream().sorted().collect(Collectors.toList()).toString()))
                .append(",保存后4级区域id：")
                .append((CollectionUtils.isEmpty(currentAreaIds)?"":currentAreaIds.stream().sorted().collect(Collectors.toList()).toString()));

        LogUtils.saveLog("基础资料-修改网点区域", "ServicePointStationService.saveServicePointStationByAreaId", strLog.toString(), null, UserUtils.getUser());
        // add on 2020-10-13 end
    }
}
