package com.wolfking.jeesite.test.sd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.es.ServicePointStation;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.service.OrderAutoPlanMessageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by yanshenglu on 2017/4/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class TestAutoPlan {

    @Autowired
    private OrderAutoPlanMessageService planMessageService;

    private static final int PAGE_SIZE = 10;

    public List<ServicePointStation> esStations = Lists.newArrayList();

    public Map<Long,com.wolfking.jeesite.modules.md.entity.ServicePointStation> stations = Maps.newHashMap();

    //region local test

    //@Before
    public void initStations(){
        //es服务点
        ServicePointStation esStation = new ServicePointStation();
        esStation.setStationId(1001l);
        esStation.setServicePointId(1l);
        esStation.setLevel(2);
        esStation.setDistance(310.0);
        esStations.add(esStation);

        esStation = new ServicePointStation();
        esStation.setStationId(1002l);
        esStation.setServicePointId(1l);
        esStation.setLevel(2);
        esStation.setDistance(501.0);
        esStations.add(esStation);

        esStation = new ServicePointStation();
        esStation.setStationId(1003l);
        esStation.setServicePointId(2l);
        esStation.setLevel(2);
        esStation.setDistance(580.0);
        esStations.add(esStation);

        esStation = new ServicePointStation();
        esStation.setStationId(1004l);
        esStation.setServicePointId(3l);
        esStation.setLevel(1);
        esStation.setDistance(610.0);
        esStations.add(esStation);

        esStation = new ServicePointStation();
        esStation.setStationId(1005l);
        esStation.setServicePointId(4l);
        esStation.setLevel(2);
        esStation.setDistance(630.0);
        esStations.add(esStation);

        esStation = new ServicePointStation();
        esStation.setStationId(1006l);
        esStation.setServicePointId(5l);
        esStation.setLevel(1);
        esStation.setDistance(630.0);
        esStations.add(esStation);

        esStation = new ServicePointStation();
        esStation.setStationId(1007l);
        esStation.setServicePointId(6l);
        esStation.setLevel(1);
        esStation.setDistance(630.0);
        esStations.add(esStation);

        //服务点
        com.wolfking.jeesite.modules.md.entity.ServicePointStation station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1001l);
        station.setRadius(300);
        stations.put(station.getId(),station);

        station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1002l);
        station.setRadius(500);
        stations.put(station.getId(),station);

        station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1003l);
        station.setRadius(500);
        stations.put(station.getId(),station);

        station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1004l);
        station.setRadius(500);
        stations.put(station.getId(),station);

        station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1005l);
        station.setRadius(1000);
        stations.put(station.getId(),station);

        station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1006l);
        station.setRadius(1000);
        stations.put(station.getId(),station);

        station = new com.wolfking.jeesite.modules.md.entity.ServicePointStation();
        station.setId(1007l);
        station.setRadius(1000);
        stations.put(station.getId(),station);

    }

    private MSResponse<MSPage<ServicePointStation>> getESNearByServicePoint(int pageSize,int pageNo,double distance){
        MSResponse msResponse = new MSResponse();
        List<ServicePointStation> s = esStations.stream().filter(t->t.getDistance()<=distance).sorted(Comparator.comparing(ServicePointStation::getLevel).reversed().thenComparing(Comparator.comparing(ServicePointStation::getDistance))).collect(Collectors.toList());
        List<List<ServicePointStation>> lists = Lists.partition(s,pageSize);
        MSPage<ServicePointStation> page = new MSPage<>();
        page.setPageSize(pageSize);
        page.setPageNo(pageNo);
        page.setRowCount(s.size());
        if(s.size()==0){
            page.setPageCount(0);
        }else{
            page.setPageCount((int)(s.size() / pageSize));
            if (s.size() % pageSize != 0 || page.getPageCount() == 0) {
                page.setPageCount(page.getPageCount()+1);
            }
        }
        if(lists.size()>=pageNo){
            page.setList(lists.get(pageNo-1));
        }else{
            page.setList(Lists.newArrayList());
        }
        msResponse.setData(page);
        return msResponse;
    }

    @Test
    public void testGetESNearByServicePoint(){
        int pageSize = 2;
        int pageNo = 3;
        double[] radiuses = DoubleStream.of(550,1000,0).toArray();
        MSResponse<MSPage<ServicePointStation>> msResponse = getESNearByServicePoint(pageSize,pageNo,radiuses[1]);
        MSPage<ServicePointStation> msPage = msResponse.getData();
        log.warn("pageSize:{} ,pageNo:{} ,pageCount:{} ,rowCount:{}",msPage.getPageSize(),msPage.getPageNo(),msPage.getPageCount(),msPage.getRowCount());
        if(msPage.getList().size()>0){
            msPage.getList().forEach(t ->{
                System.out.printf("\n stationId:%d serviceId:%d distance:%f",
                        t.getStationId(),
                        t.getServicePointId(),
                        t.getDistance()
                );
            });
        }else{
            log.error("无数据返回:pageNo:{} , pageSize:{}",pageNo,pageSize);
        }
    }

    /**
     * 自动派单时筛选符合的网点
     */
    @Test
    public void testGetNearByServicePoint(){
        int[] radiuses = IntStream.of(1000,0,0).toArray();
        ServicePointStation station = getNearServicePoint(1l,radiuses,113.220886d,22.866232);
    }


    /**
     * 自动派单时读取可派单网点列表
     */
    private ServicePointStation getNearServicePoint(long areaId, int[] distances,double longitude,double latitude){
        //不同半径返回的服务点有重复，重复服务点不重复计数
        Map<Long,Double> stationIdMaps = Maps.newHashMap();
        int remain = 10;
        return getNearServicePointRecursion(stationIdMaps,remain,areaId,0,distances,1,longitude,latitude);
    }

    /**
     * 递归获得网点
     * @param remain    剩余检索服务点数量
     * @param areaId    区县id
     * @param index     当前半径索引
     * @param distances 半径列表
     * @param longitude 经度
     * @param latitude  维度
     * @return
     */
    private ServicePointStation getNearServicePointRecursion(Map<Long,Double> stationIdMaps,int remain,long areaId, int index, int[] distances,int pageNo,double longitude,double latitude) {
        if(index >= distances.length || remain <= 0){
            return null;
        }
        int distance = distances[index];
        log.warn("====================================================================================");
        log.info("index:{} , distance: {} ,remain: {}",index,distance,remain);
        MSResponse<MSPage<ServicePointStation>> msResponse = getESNearByServicePoint(PAGE_SIZE,pageNo, distance);
        if (!MSResponse.isSuccessCode(msResponse)) {
            log.error("[getESNearByServicePoint]获取自动派单网点列表错误：{}", msResponse.getMsg());
            return null;
        }
        MSPage<ServicePointStation> msPage = msResponse.getData();
        log.info("ES返回列表数量:{} , distance: {}",msPage.getList().size(),distance);
        if (msPage == null || msPage.getList() == null || msPage.getList().size() == 0) {
            // 下个半径检索
            if(index+1 >= distances.length){
                return null;
            }
            int nextDistance = distances[index+1];
            log.info("下一半径： index:{} , distance: {}",index+1,nextDistance);
            //下个半径为0 或范围比当前小，中断递归
            if(nextDistance <= 0 || nextDistance <= distance){
                return null;
            }
            return getNearServicePointRecursion(stationIdMaps,remain,areaId,index+1,distances,pageNo,longitude, latitude);
        } else {
            log.info("返回列表:{}", GsonUtils.getInstance().toGson(msPage));
            List<ServicePointStation> list = msPage.getList();
            list.stream().forEach(t -> t.setDistance(t.getDistance() + ThreadLocalRandom.current().nextDouble(1,10)));
            Collections.sort(list, Comparator.comparing(ServicePointStation::getLevel).reversed()
                    .thenComparing(ServicePointStation::getDistance));
            ServicePointStation station;
            com.wolfking.jeesite.modules.md.entity.ServicePointStation cacheStation = null;
            int rowCount = list.size();
            for(int i=0;i<rowCount;i++){
                station = list.get(i);
                if(stationIdMaps.containsKey(station.getStationId())){
                    continue;
                }
                remain--;
                stationIdMaps.put(station.getStationId(),station.getDistance());
                // 从缓存读取服务点的服务范围(半径)
                cacheStation = stations.get(station.getStationId());
                if(station.getDistance() <= cacheStation.getRadius()){
                    log.warn("remain: {}",remain);
                    log.info("stationIdMaps:{}",StringUtils.join(stationIdMaps.keySet(),","));
                    log.warn("符合的服务点 - index:{} serviePoint Id:{} level:{} station:{} distance:{} radius:{}",i,station.getServicePointId(),station.getLevel(),station.getStationId(),station.getDistance(),cacheStation.getRadius());
                    return station;
                }else{
                    continue;
                }
            }
            //本次查询没有符合服务点
            //剩余检索次数
            if(remain<=0){
                return null;
            }
            //下一页
            log.info("pageCount:{} ,pageNo: {} ,remain: {}",msPage.getPageCount(),pageNo,remain);
            if(msPage.getPageCount() > pageNo){
                return getNearServicePointRecursion(stationIdMaps,remain,areaId,index,distances,pageNo+1,longitude, latitude);
            }
            // 下个半径检索
            if(index+1 >= distances.length){
                return null;
            }
            int nextDistance = distances[index+1];
            log.info("下一半径：index:{} , distance: {}",index+1,nextDistance);
            //下个半径为0 或范围比当前小，中断递归
            if(nextDistance <= 0 || nextDistance <= distance){
                return null;
            }
            //因下个半径比当前半径大，所以查询数据会重复
            int nextPageNo = pageNo;
            if(rowCount == msPage.getPageSize()){
                nextPageNo++;
            }
            return getNearServicePointRecursion(stationIdMaps,remain,areaId,index+1,distances,nextPageNo,longitude, latitude);
        }
    }

    @Test
    public void testMessaeFormat(){
        String msg = MessageFormat.format("订单:{0} 自动派单失败 - 当前状态：{1} 不能自动派单","K2019042966993","已派单");
        System.out.println(msg);
    }
    //endregion

    //region es

    @Test
    public void testGetESNearServicePoint(){
        long areaId = 3403;
        int[] distances = IntStream.of(20000,0,0).toArray();
        //114.045846,22.600856
        ServicePointStation station = planMessageService.getNearServicePoint(areaId,distances,114.037627,22.676551);
    }

    //endregion
}
