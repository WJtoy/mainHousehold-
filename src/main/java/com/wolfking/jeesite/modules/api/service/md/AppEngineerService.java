package com.wolfking.jeesite.modules.api.service.md;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDEngineerAddress;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.api.entity.common.RestAppException;
import com.wolfking.jeesite.modules.api.entity.md.*;
import com.wolfking.jeesite.modules.api.util.RestEnum;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSEngineerAddressFeign;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * APP安维
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AppEngineerService extends LongIDBaseService {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ServicePointService servicePointService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private MSEngineerService msEngineerService;
    @Autowired
    private MSEngineerAddressFeign msEngineerAddressFeign;

    private static final String LOCK_EDIT_SERVICE_POINT_KEY = "lock:engineer:%s";

    /**
     * 获取师傅信息
     */
    public RestEngineerInfo getEngineerInfo(RestEnum.UserInfoType userInfoType, RestLoginUserInfo loginUserInfo) {
        RestEngineerInfo restEngineer = new RestEngineerInfo();
        restEngineer.setPhoto("");
        if (userInfoType == RestEnum.UserInfoType.All || userInfoType == RestEnum.UserInfoType.Base) {
            Engineer engineer = msEngineerService.getBaseInfoFromCache(loginUserInfo.getEngineerId());
            restEngineer.setName(engineer.getName());
            restEngineer.setContactInfo(engineer.getContactInfo());
            restEngineer.setPlanCount(engineer.getPlanCount());
            restEngineer.setOrderCount(engineer.getOrderCount());
            restEngineer.setReminderCount(engineer.getReminderCount() == null ? 0 : engineer.getReminderCount());
            restEngineer.setComplainCount(engineer.getComplainCount() == null ? 0 : engineer.getComplainCount());
            if (loginUserInfo.getPrimary()) {
                ServicePointFinance servicePointFinance = servicePointService.getAmounts(loginUserInfo.getServicePointId());
                restEngineer.setBalance(servicePointFinance.getBalance());
            }
        }
        if (userInfoType == RestEnum.UserInfoType.All || userInfoType == RestEnum.UserInfoType.Detail) {
            Engineer engineer = msEngineerService.getDetailInfoFromCache(loginUserInfo.getServicePointId(), loginUserInfo.getEngineerId());
            restEngineer.setName(engineer.getName());
            restEngineer.setContactInfo(engineer.getContactInfo());
            restEngineer.setAddress(engineer.getAddress());
            Area area;
            List<String> areaNames = Lists.newArrayList();
            for (Long areaId : engineer.getAreaIds()) {
                area = areaService.getFromCache(areaId);
                if (area != null && StringUtils.isNotBlank(area.getName())) {
                    areaNames.add(area.getName());
                }
                restEngineer.setAreaNames(StringUtils.join(areaNames, "、"));
            }
//            ServicePoint servicePoint = servicePointService.getFromCache(engineer.getServicePoint().getId());
            if (engineer.getServicePoint() != null && StringUtils.isNotBlank(engineer.getServicePoint().getName())) {
                restEngineer.setServicePointName(engineer.getServicePoint().getName());
            }
        }
        return restEngineer;
    }

    /**
     * 保存用户地址信息
     */
    public void saveEngineerAddress(Engineer engineer, RestEngineerAddress addressInfo, User user) {
        if (addressInfo == null || StringUtils.isBlank(addressInfo.getAreaName()) || StringUtils.isBlank(addressInfo.getAddress())) {
            throw new RestAppException("参数不全，请确认后重试。");
        }

        Long areaId;
        String areaName = "";
        String detailAddress = "";
        String[] result;
        try {
            //result = AreaUtils.parseAddress(addressInfo.getAreaName() + addressInfo.getAddress().replace(" ", ""));  //mark on 2020-8-5
            result = AreaUtils.parseAddressFromMS(addressInfo.getAreaName() + addressInfo.getAddress().replace(" ", ""));  //add on 2020-8-5
        } catch (Exception e) {
            throw new RestAppException("用户地址解析失败");
        }
        if (result != null && result.length > 1) {
            areaId = StringUtils.toLong(result[0]);
            areaName = result[1];
            if (result.length > 2) {
                detailAddress = result[2];
            }
        } else {
            throw new RestAppException("用户地址解析失败");
        }
        String lockKey = String.format(LOCK_EDIT_SERVICE_POINT_KEY, engineer.getId());
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);
        if (!locked) {
            throw new RestAppException("用户地址正在修改中，请稍候重试。");
        }
        try {

            Engineer params = new Engineer();
            params.setId(engineer.getId());
            params.setArea(new Area(areaId));
            params.setAddress(areaName + " " + (StringUtils.isNotBlank(detailAddress) ? detailAddress : addressInfo.getAddress()));
            params.setUpdateBy(user);
            params.setUpdateDate(new Date());
            msEngineerService.updateAddress(params);
        } catch (Exception e) {
            LogUtils.saveLog("APP保存用户地址", "AppEngineerService.saveEngineerAddress", engineer.getId().toString(), e, user);
            throw new RestAppException("保存用户地址失败");
        } finally {
            if (lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    /**
     * 保存用户收货地址信息
     */
    public void saveEngineerConsigneeAddress(Long servicePointId, Long engineerId, AppSaveEngineerConsigneeAddressRequest addressInfo, User user) {
        if (addressInfo == null || StringUtils.isBlank(addressInfo.getAreaName()) || StringUtils.isBlank(addressInfo.getAddress())) {
            throw new RestAppException("参数不全，请确认后重试。");
        }
        Long areaId;
        String[] result;
        try {
            result = AreaUtils.parseAddress(addressInfo.getAreaName() + addressInfo.getAddress().replace(" ", ""));
        } catch (Exception e) {
            throw new RestAppException("用户收货地址解析失败");
        }
        if (result != null && result.length > 1) {
            areaId = StringUtils.toLong(result[0]);
        } else {
            throw new RestAppException("用户地址解析失败");
        }
        String lockKey = String.format(LOCK_EDIT_SERVICE_POINT_KEY, engineerId);
        boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey, 1, 60);
        if (!locked) {
            throw new RestAppException("用户收货地址正在修改中，请稍候重试。");
        }
        try {
            MDEngineerAddress params = new MDEngineerAddress();
            params.setServicePointId(servicePointId);
            params.setEngineerId(engineerId);
            params.setUserName(addressInfo.getUserName());
            params.setContactInfo(addressInfo.getUserPhone());
            params.setAreaId(areaId);
            Area area = areaService.getFromCache(areaId);
            if (area != null) {
                List<String> ids = Splitter.onPattern(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(area.getParentIds());
                if (ids.size() >= 2) {
                    params.setCityId(StringUtils.toLong(ids.get(ids.size() - 1)));
                    params.setProvinceId(StringUtils.toLong(ids.get(ids.size() - 2)));
                }
            }
            Date now = new Date();
            params.setAddress(addressInfo.getAddress());
            params.setAddressFlag(addressInfo.getAddressType());
            params.setCreateBy(user);
            params.setUpdateBy(user);
            params.setCreateDate(now);
            params.setUpdateDate(now);

            MSResponse<Integer> msResponse;
            MDEngineerAddress oldConsigneeAddress = getEngineerConsigneeAddress(engineerId);
            if (oldConsigneeAddress != null && oldConsigneeAddress.getId() != null) {
                params.setId(oldConsigneeAddress.getId());
                msResponse = msEngineerAddressFeign.update(params);
            } else {
                msResponse = msEngineerAddressFeign.insert(params);
            }
            if (!MSResponse.isSuccessCode(msResponse)) {
                throw new RuntimeException("调用微服务更新用户收货地址失败.失败原因:" + msResponse.getMsg());
            }
        } catch (Exception e) {
            LogUtils.saveLog("APP保存用户收货地址", "AppEngineerService.saveEngineerConsigneeAddress", engineerId.toString(), e, user);
            throw new RestAppException("APP保存用户收货地址失败");
        } finally {
            if (lockKey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey);
            }
        }
    }

    /**
     * 根据网点id和师傅id从缓存中获取师傅收件地址
     */
    public AppGetEngineerConsigneeAddressResponse getEngineerConsigneeAddressFromCache(Long servicePointId, Long engineerId){
        AppGetEngineerConsigneeAddressResponse result = new AppGetEngineerConsigneeAddressResponse();
        MSResponse<MDEngineerAddress> response = msEngineerAddressFeign.getFromCache(servicePointId,engineerId);
        if(MSResponse.isSuccess(response)){
            result.setHasConsigneeAddress(1);
            result.setUserName(response.getData().getUserName());
            result.setUserPhone(response.getData().getContactInfo());
            result.setAddress(response.getData().getAddress());
            Area area = areaService.getFromCache(response.getData().getAreaId());
            result.setAreaName(area.getFullName());
        }
        return result;
    }

    /**
     * 根据id获取安维人员信息
     */
    private MDEngineerAddress getEngineerConsigneeAddress(Long engineerId) {
        MDEngineerAddress address = null;
        if (engineerId != null && engineerId > 0) {
            MSResponse<MDEngineerAddress> response = msEngineerAddressFeign.getByEngineerId(engineerId);
            if (MSResponse.isSuccess(response)) {
                address = response.getData();
            }
        }
        return address;
    }

    /**
     * 获取师傅信息
     */
    public AppGetEngineerAndServicePointAddressResponse getEngineerAndServicePointAddress(Long servicePointId, Long engineerId) {
        AppGetEngineerAndServicePointAddressResponse result = new AppGetEngineerAndServicePointAddressResponse();
        Engineer engineer = msEngineerService.getDetailInfoFromCache(servicePointId, engineerId);
        if (engineer != null) {
            result.getEngineer().setName(engineer.getName());
            result.getEngineer().setContactInfo(engineer.getContactInfo());
            String fullAddress = engineer.getAddress();
            if (StringUtils.isNotBlank(fullAddress)) {
                String[] addressArr = fullAddress.split(" ");
                if (addressArr.length > 2) {
                    List<String> areaNameArr = Lists.newArrayList();
                    List<String> subAddressArr = Lists.newArrayList();
                    for (int i = 0; i<addressArr.length; i++) {
                        if (i <= 2) {
                            areaNameArr.add(addressArr[i]);
                        } else {
                            subAddressArr.add(addressArr[i]);
                        }
                    }
                    String areaFullName = StringUtils.join(areaNameArr, "");
                    String subAddress = StringUtils.join(subAddressArr, "");
                    result.getEngineer().setAreaName(areaFullName);
                    result.getEngineer().setAddress(subAddress);
                }
            }
//            result.getEngineer().setAddress(engineer.getAddress());
//            Area area = areaService.getFromCache(engineer.getArea().getId());
//            if (area != null) {
//                result.getEngineer().setAreaName(area.getFullName());
//            }
        }
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        if (servicePoint != null) {
            result.getServicePoint().setAddress(servicePoint.getSubAddress());
            Area area = areaService.getFromCache(servicePoint.getArea().getId());
            if (area != null) {
                result.getServicePoint().setAreaName(area.getFullName());
            }
            if (servicePoint.getPrimary() != null && servicePoint.getPrimary().getId() != null) {
                Engineer primary = msEngineerService.getDetailInfoFromCache(servicePointId, engineerId);
                if (primary != null) {
                    result.getServicePoint().setName(primary.getName());
                    result.getServicePoint().setContactInfo(primary.getContactInfo());
                }
            }
        }
        return result;
    }

}
