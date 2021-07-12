package com.wolfking.jeesite.modules.sd.service;

import com.google.common.base.Supplier;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RegionSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserRegion;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * @{NAME} 〈一句话功能简述〉<br>
 * @date 2019-10-26 17:49
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderRegionService extends LongIDBaseService {

    @Autowired
    private UserRegionService userRegionService;

    @Autowired
    private AreaService areaService;

    /**
     * 装载并检查账号负责区域
     */
    public String loadAndCheckUserRegions(RegionSearchModel searchModel, User user){
        //界面未选择区域
        if(searchModel.getArea() == null || searchModel.getArea().getId() == null){
            searchModel.setArea(new Area(0L));
            searchModel.setAreaLevel(null);
        }
        //客服及内部账号
        // 区域：sys_user_region
        // 品类: md_productcategory_user
        // subQueryUserCustomer:1 关联 sys_user_customer
        if(user.isKefu() || user.isInnerAccount()){
            searchModel.setCreateBy(user);//*
            List<UserRegion> regions = userRegionService.getUserRegions(user.getId());
            if(ObjectUtils.isEmpty(regions)){
                return "错误：您的账号未设定负责区域";
            }

            //1.选择了区域，判断是否有权限
            if(searchModel.getArea().getId() > 0 && searchModel.getAreaLevel() != null){
                if(searchModel.getArea().getParent() == null || searchModel.getArea().getParent() .getId() == null){
                    searchModel.getArea().setParent(new Area(0L));
                }
                Area area = areaService.getSelfAndParentList(searchModel.getArea().getId(),searchModel.getArea().getParent() .getId(),searchModel.getAreaLevel()+2);
                if (area == null){
                    area = new Area(searchModel.getArea().getId());
                }
                Boolean checkRegion = userRegionService.checkUserHasRegionPermission(searchModel,regions,area,searchModel.getAreaLevel());
                if(!checkRegion){
                    return "错误：您的账号无选择区域的权限";
                }
                return org.apache.commons.lang3.StringUtils.EMPTY;
            }
            //2.未选择区域
            Supplier<Stream<UserRegion>> streamSupplier = () -> regions.stream();
            long count = streamSupplier.get().filter(t->t.getAreaType() == 1).count();
            if(count>0){
                searchModel.setProvinceList(null);
                searchModel.setCityList(null);
                searchModel.setAreaList(null);
                return org.apache.commons.lang3.StringUtils.EMPTY;
            }
            List<Long> idList = null;
            //province
            idList = streamSupplier.get().filter(t->t.getAreaType() == 2).map(t->t.getProvinceId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                searchModel.setProvinceList(null);
            }else{
                searchModel.setProvinceList(idList);
            }
            //city
            idList = streamSupplier.get().filter(t->t.getAreaType() == 3).map(t->t.getCityId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                searchModel.setCityList(null);
            }else{
                searchModel.setCityList(idList);
            }
            //area
            idList = streamSupplier.get().filter(t->t.getAreaType() == 4).map(t->t.getAreaId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                searchModel.setAreaList(null);
            }else{
                searchModel.setAreaList(idList);
            }
        }else{
            searchModel.setProvinceList(null);
            searchModel.setCityList(null);
            searchModel.setAreaList(null);
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }

    /**
     * 装载并检查账号负责区域 for 催单
     */
    public String loadAndCheckReminderUserRegions(ReminderPageSearchModel searchModel, User user){
        //客服及内部账号
        // 区域：sys_user_region
        // 品类: md_productcategory_user
        // subQueryUserCustomer:1 关联 sys_user_customer
        if(user.isKefu() || user.isInnerAccount()){
            searchModel.setCreateBy(user);//*
            List<UserRegion> regions = userRegionService.getUserRegions(user.getId());
            if(ObjectUtils.isEmpty(regions)){
                return "错误：您的账号未设定负责区域";
            }

            //1.选择了区域，判断是否有权限
            if(searchModel.getAreaId() > 0 && searchModel.getAreaLevel() >= 0){
                Area area = areaService.getSelfAndParentList(searchModel.getAreaId(),searchModel.getArea().getParent().getId(),searchModel.getAreaLevel()+2);
                if (area == null){
                    area = new Area(searchModel.getArea().getId());
                }
                RegionSearchModel regionSearchModel = new RegionSearchModel<>();
                Boolean checkRegion = userRegionService.checkUserHasRegionPermission(regionSearchModel,regions,area,searchModel.getAreaLevel());
                if(!checkRegion){
                    return "错误：您的账号无选择区域的权限";
                }else{
                    searchModel.setProvinceList(regionSearchModel.getProvinceList());
                    searchModel.setCityList(regionSearchModel.getCityList());
                    searchModel.setAreaList(regionSearchModel.getAreaList());
                }
                return org.apache.commons.lang3.StringUtils.EMPTY;
            }
            //2.未选择区域
            Supplier<Stream<UserRegion>> streamSupplier = () -> regions.stream();
            long count = streamSupplier.get().filter(t->t.getAreaType() == 1).count();
            if(count>0){
                searchModel.setProvinceList(null);
                searchModel.setCityList(null);
                searchModel.setAreaList(null);
                return org.apache.commons.lang3.StringUtils.EMPTY;
            }
            List<Long> idList = null;
            //province
            idList = streamSupplier.get().filter(t->t.getAreaType() == 2).map(t->t.getProvinceId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                searchModel.setProvinceList(null);
            }else{
                searchModel.setProvinceList(idList);
            }
            //city
            idList = streamSupplier.get().filter(t->t.getAreaType() == 3).map(t->t.getCityId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                searchModel.setCityList(null);
            }else{
                searchModel.setCityList(idList);
            }
            //area
            idList = streamSupplier.get().filter(t->t.getAreaType() == 4).map(t->t.getAreaId()).distinct().collect(Collectors.toList());
            if(ObjectUtils.isEmpty(idList)){
                searchModel.setAreaList(null);
            }else{
                searchModel.setAreaList(idList);
            }
        }else{
            searchModel.setProvinceList(null);
            searchModel.setCityList(null);
            searchModel.setAreaList(null);
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }


}
