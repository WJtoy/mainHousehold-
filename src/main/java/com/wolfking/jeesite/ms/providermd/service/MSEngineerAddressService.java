package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDAreaTypeEnum;
import com.kkl.kklplus.entity.md.MDEngineerAddress;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSEngineerAddressFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MSEngineerAddressService {
    @Autowired
    private MSEngineerAddressFeign msEngineerAddressFeign;

    @Autowired
    private AreaService areaService;

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public MDEngineerAddress getByEngineerId(Long id) {
        if (id == null) {
            return null;
        }
        MDEngineerAddress engineerAddress = msEngineerAddressFeign.getByEngineerId(id).getData();
        if (engineerAddress == null) {
            engineerAddress = new MDEngineerAddress();
        }
        return engineerAddress;
    }

    public MDEngineerAddress getById(Long id) {
        if (id == null) {
            return null;
        }

        MDEngineerAddress engineerAddress = msEngineerAddressFeign.getById(id).getData();
        if (engineerAddress == null) {
            engineerAddress = new MDEngineerAddress();
        }
        if(engineerAddress.getAreaId() != null){
            Area area = areaService.get(engineerAddress.getAreaId());
            if(area != null){
                engineerAddress.setAddress(engineerAddress.getAddress().replace(area.getFullName(),""));
            }
        }
        return engineerAddress;
    }

    @Transactional(readOnly = false)
    public void save(MDEngineerAddress engineerAddress){
        if (engineerAddress.getAreaName() != null && !("").equals(engineerAddress.getAreaName())) {
            String address = engineerAddress.getAddress();
            String areaName = engineerAddress.getAreaName();
            String newAddress = address.replace(areaName, "");
            engineerAddress.setAddress(engineerAddress.getAreaName() + newAddress);
        }
        boolean isNew = engineerAddress.getIsNewRecord();
        MSResponse<Integer> msResponse = new MSResponse<>();
        if(engineerAddress.getAreaId() != null){
            Area city = areaService.getFromCache(engineerAddress.getAreaId());
            if(city != null){
                engineerAddress.setCityId(city.getParentId());
                Area province = areaService.getFromCache(city.getParentId());
                if(province != null){
                    engineerAddress.setProvinceId(province.getParentId());
                }
            }
        }
        Long userId = Optional.ofNullable(UserUtils.getUser()).map(User::getId).orElse(0L);
        if (isNew) {
            engineerAddress.preInsert();
            engineerAddress.setCreateById(userId);
            engineerAddress.setUpdateById(userId);
            msResponse = msEngineerAddressFeign.insert(engineerAddress);
            // 当插入时从微服务中获取生成ID
            if (MSResponse.isSuccess(msResponse)) {
                engineerAddress.setId(msResponse.getData().longValue());
            }
        } else {
            engineerAddress.preUpdate();
            engineerAddress.setUpdateById(userId);
            msResponse = msEngineerAddressFeign.update(engineerAddress);
        }
        if (msResponse.getCode() > 0) {
            throw new RuntimeException("保存收货地址到微服务中出错。出错原因:" + msResponse.getMsg());
        }
    }


    /**
     * 根据网点id和师傅id从缓存中获取师傅收件地址
     * @param servicePointId
     * @return
     */
    public MDEngineerAddress getFromCache(Long servicePointId,Long engineerId){
        MSResponse<MDEngineerAddress> msResponse = msEngineerAddressFeign.getFromCache(servicePointId,engineerId);
        if(MSResponse.isSuccess(msResponse)){
             return msResponse.getData();
        }else{
            return null;
        }
    }

}
