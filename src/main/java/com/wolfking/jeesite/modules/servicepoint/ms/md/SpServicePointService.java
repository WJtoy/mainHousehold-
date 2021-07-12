/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDEngineerArea;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerAreaService;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 网点工单
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpServicePointService {


    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private MSEngineerAreaService msEngineerAreaService;

    @Autowired
    private AreaService areaService;

    /**
     * 获得网点下某个安维人员信息
     *
     * @param servicePointId 网点id
     * @param engineerId     安维id
     */
    public Engineer getEngineerFromCache(Long servicePointId, Long engineerId) {
        return servicePointService.getEngineerFromCache(servicePointId, engineerId);
    }

    /**
     * 获得网点下安维列表
     *
     * @param servicePointId 网点Id
     */
    public List<Engineer> getEngineerListOfServicePoint(Long servicePointId) {
        return servicePointService.getEngineerListOfServicePoint(servicePointId);
    }

    public Engineer getEngineer(Long id) {
        return servicePointService.getEngineer(id);
    }

    /**
     * 按id获得网点信息
     * 优先从缓存中取
     */
    public ServicePoint getFromCache(Long id) {
        return servicePointService.getFromCache(id);
    }

    /**
     * 按需读取网点价格
     *
     * @param servicePointId 网点id
     * @param products       NameValuePair<产品id,服务项目id>
     */
    public Map<String, ServicePrice> getPriceMapByProductsFromCache(Long servicePointId, List<NameValuePair<Long, Long>> products) {
        return servicePointService.getPriceMapByProductsFromCache(servicePointId, products);
    }

    /**
     * 按区县/街道/品类 分页查询可派单列表(以完成单数量倒序排序)
     * 只查询level 1 ~ 5的,且status=10
     */
    public Page<ServicePoint> findServicePointListForPlanNew(Page<ServicePoint> page, ServicePoint entity) {
        return servicePointService.findServicePointListForPlanNew(page, entity);
    }

    /**
     * 分页查询
     * 先从数据库返回id,再根据id从缓存中读取，缓存不存在则再从数据库读取并更新至缓存
     */
    public Page<Engineer> findPage(Page<Engineer> page, Engineer entity) {
        // return servicePointService.findPage(page, entity);
        return findPageForSD(page, entity);
    }

    public Page<Engineer> findPageForSD(Page<Engineer> page, Engineer entity) {
        entity.setPage(page);
        return findEngineerListForPage(page, entity);
    }

    public Page<Engineer> findEngineerListForPage(Page<Engineer> page, Engineer engineer) {
        Page<Engineer> engineerPage = msEngineerService.findEngineerListForSD(page, engineer);
        List<Engineer> engineerList = engineerPage.getList();

        if (engineerList != null && engineerList.size() > 0) {
            List<Long> engineerIds = engineerList.stream().map(engineer1 -> engineer1.getId()).distinct().collect(Collectors.toList());
            List<User> userList = systemService.findEngineerAccountList(engineerIds, null); //subFlag 1：是子帐号　0：不是子帐号
            Map<Long,User> userMap = userList!= null&& !userList.isEmpty()?userList.stream().collect(Collectors.toMap(User::getEngineerId, Function.identity())): Maps.newHashMap();
            Map<Long, String> serviceAreaMap = getEngineerServiceAreas(engineerIds); // add on 2019-11-8

            Map<String, Dict> levelMap = MSDictUtils.getDictMap("ServicePointLevel");
            for (Engineer eng : engineerList) {
                if (eng.getLevel() != null && Integer.parseInt(eng.getLevel().getValue()) > 0) {
                    eng.setLevel(levelMap.get(eng.getLevel().getValue()));
                }
                if (eng.getArea()!= null && eng.getArea().getId() != null) {
                    Area area = areaService.getFromCache(eng.getArea().getId());
                    eng.setArea(area);
                }

                User user = userMap.get(eng.getId());
                if (user != null) {
                    eng.setAccountId(user.getId());
                    eng.setAppLoged(user.getAppLoged());
                }
                eng.setAreas(serviceAreaMap.get(eng.getId()));
            }
        }

        engineerPage.setList(engineerList);
        return engineerPage;
    }

    public Map<Long, String> getEngineerServiceAreas(List<Long> engineerIds) {
        List<MDEngineerArea> mdEngineerAreaList = msEngineerAreaService.findEngineerAreasWithIds(engineerIds);

        Map<Long, String> serviceAreaMap = Maps.newHashMap();
        if (mdEngineerAreaList != null && !mdEngineerAreaList.isEmpty()) {
            // 获取区域id列表，再通过区域id列表获取区域哈希对象
            List<Long> areaIds = mdEngineerAreaList.stream().map(MDEngineerArea::getAreaId).distinct().collect(Collectors.toList());
            Map<Long,String> areaMap = Maps.newHashMap();
            if (areaIds != null && !areaIds.isEmpty()) {
                areaIds.stream().forEach(r->{
                    Area area = areaService.getFromCache(r,Area.TYPE_VALUE_COUNTY);
                    if (area != null) {
                        areaMap.put(r, area.getName());
                    }
                });
            }
            // 生成key为安维id,value为区域名称哈希对象
            Map<Long, List<MDEngineerArea>> engineerAreaMap = mdEngineerAreaList.stream().collect(Collectors.groupingBy(MDEngineerArea::getEngineerId));
            engineerAreaMap.forEach((k,v)->{
                if (v != null && !v.isEmpty()) {
                    String areaNames = v.stream().filter(mdEngineerArea -> areaMap.get(mdEngineerArea.getAreaId()) != null).map(mdEngineerArea -> areaMap.get(mdEngineerArea.getAreaId())).collect(Collectors.joining(","));
                    serviceAreaMap.put(k, areaNames);
                }
            });
        }
        return serviceAreaMap;
    }

    /**
     * 按需读取网点偏远区域价格
     *
     * @param servicePointId 网点id
     * @param products       NameValuePair<产品id,服务项目id>
     * @return
     */
    public Map<String, ServicePrice> getRemotePriceMapByProductsFromCache(Long servicePointId, List<NameValuePair<Long, Long>> products) {
        return servicePointService.getRemotePriceMapByProductsFromCache(servicePointId,products);
    }

}
