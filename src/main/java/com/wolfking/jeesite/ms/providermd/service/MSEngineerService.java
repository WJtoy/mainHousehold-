package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDEngineer;
import com.kkl.kklplus.entity.md.dto.MDEngineerDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSEngineerFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.HTMLDocument;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MSEngineerService {
    @Autowired
    private MSEngineerFeign msEngineerFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public Engineer getById(Long id) {
        if (id == null) {
            return null;
        }
        Engineer engineer = MDUtils.getById(id, Engineer.class, msEngineerFeign::getById);
        if (engineer == null) {
            engineer = new Engineer(id);
        }
        return engineer;
    }

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public Engineer getByIdFromCache(Long id) {
        if (id == null) {
            return null;
        }
        Engineer engineer = MDUtils.getById(id, Engineer.class, msEngineerFeign::getByIdByFromCache);
        if (engineer == null) {
            engineer = new Engineer(id);
        }
        return engineer;
    }

    public int reloadEngineerCacheById(Long id) {
        MSResponse<Integer> msResponse = msEngineerFeign.reloadEngineerCacheById(id);
        if (MSResponse.isSuccess(msResponse)) {
            return Optional.ofNullable(msResponse.getData()).orElse(0);
        }
        return 0;
    }

    /**
     * 根据id获取安维人员名字
     * @param id
     * @return
     */
    public String getNameByIdFromCache(Long id) {
        if (id == null) {
            return null;
        }
       /* Engineer engineer = MDUtils.getById(id, Engineer.class, msEngineerFeign::getNameByIdByFromCache);
        if (engineer == null) {
            engineer = new Engineer(id);
        }
        return engineer;*/

        MSResponse<String> response = msEngineerFeign.getNameByIdByFromCache(id);
        if (MSResponse.isSuccess(response)) {
            return response.getData();
        }
        return "";
    }

    // region API
    /**
     * 基本信息：姓名、电话、派单、完成、催单、投诉单(API)
     * @param engineerId
     * @return
     */
    public Engineer getBaseInfoFromCache(Long engineerId) {
        //return MDUtils.getEntity(Engineer.class, ()->msEngineerFeign.getBaseInfoFromCache(engineerId));
        return MDUtils.getObjNecessaryConvertType(Engineer.class, ()->msEngineerFeign.getBaseInfoFromCache(engineerId));
    }

    /**
     * 基本信息：姓名、电话、派单、完成、催单、投诉单(API)
     * @param engineerId
     * @return
     */
    public Engineer getDetailInfoFromCache(Long servicePointId, Long engineerId) {
        //return MDUtils.getEntity(Engineer.class, ()->msEngineerFeign.getDetailInfoFromCache(servicePointId, engineerId));
        return MDUtils.getObjNecessaryConvertType(Engineer.class, ()->msEngineerFeign.getDetailInfoFromCache(servicePointId, engineerId));
    }

    /**
     * 更新安维地址（API)
     * @param engineer
     */
    public void updateAddress(Engineer engineer) {
        MSErrorCode msErrorCode = MDUtils.genericSave(engineer, MDEngineer.class,false, msEngineerFeign::updateAddress);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新安维地址失败.失败原因:" + msErrorCode.getMsg());
        }
    }
    // endregion API

    /**
     * 根据网点id和安维id从缓存中获取安维信息
     * @param servicePointId
     * @param engineerId
     * @return
     */
    public Engineer getEngineerFromCache(Long servicePointId, Long engineerId) {
        //return MDUtils.getEntity(Engineer.class,(()->msEngineerFeign.getEngineerFromCache(engineerId, servicePointId)));
        return MDUtils.getObjNecessaryConvertType(Engineer.class,(()->msEngineerFeign.getEngineerFromCache(engineerId, servicePointId)));
    }

    /**
     * 根据手机号获取安维人员id
     * @param mobile
     * @param exceptId
     * @return
     */
    public Long getEngineerIdByMobile(String mobile, Long exceptId) {
        MSResponse<Long> msResponse = msEngineerFeign.getEngineerIdByMobile(mobile, exceptId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 根据id列表获取安维人员列表
     * @param ids
     * @return
     */
    public List<Engineer> findEngineersByIds(List<Long> ids, List<String> fields) {
        List<Field> fieldList = Lists.newArrayList();
        Class<?> cls = Engineer.class;
        while(cls != null) {
            Field[] fields1 = cls.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields1));
            cls = cls.getSuperclass();
        }
        Long icount = fieldList.stream().filter(r->fields.contains(r.getName())).count();
        if (icount.intValue() != fields.size()) {
            throw new RuntimeException("按条件获取安维列表数据要求返回的字段有问题，请检查");
        }

        List<Engineer> engineerList = Lists.newArrayList();
        if (ids != null && !ids.isEmpty()) {
            Lists.partition(ids,1000).forEach(longList -> {
                MSResponse<List<MDEngineer>> msResponse = msEngineerFeign.findEngineersByIds(longList, fields);
                if (MSResponse.isSuccess(msResponse)) {
                    List<Engineer> engineerListFromMS = mapper.mapAsList(msResponse.getData(), Engineer.class);
                    if (engineerListFromMS != null && !engineerListFromMS.isEmpty()) {
                        engineerList.addAll(engineerListFromMS);
                    }
                }
            });
        }
        return engineerList;
    }

    /**
     * 根据id列表获取安维人员map对象
     * @param ids
     * @return
     */
    public Map<Long, Engineer> findEngineersByIdsToMap(List<Long> ids, List<String> fields) {
        List<Engineer> engineerList = findEngineersByIds(ids, fields);
        return engineerList != null && !engineerList.isEmpty() ? engineerList.stream().collect(Collectors.toMap(Engineer::getId, Function.identity())):Maps.newHashMap();
    }

    /**
     * 根据网点id从缓存中获取安维信息 //2019-11-2
     * @param servicePointId
     * @return
     */
    public List<Engineer> findEngineerByServicePointIdFromCache(Long servicePointId) {
       return MDUtils.findListNecessaryConvertType(Engineer.class, ()->msEngineerFeign.findEngineerByServicePointIdFromCache(servicePointId));
    }

    /**
     * 检查网点下是否存在其他的主帐号
     * @param servicePointId
     * @param exceptEngineerId
     * @return
     */
    public Integer checkMasterEngineer(Long servicePointId,Long exceptEngineerId) {
        MSResponse<Integer> msResponse = msEngineerFeign.checkMasterEngineer(servicePointId, exceptEngineerId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return 0;
    }

    /**
     *  获取当前网点下所有的非当前人员的安维人员id列表
     * @param engineerId
     * @param servicePointId
     * @return
     */
    public List<Long> findSubEngineerIds(Long engineerId, Long servicePointId) {
        MSResponse<List<Long>> msResponse = msEngineerFeign.findSubEngineerIds(engineerId, servicePointId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return Lists.newArrayList();
    }

    /**
     * 查询师傅列表
     * @param engineer
     * @return
     */
    public Page<Engineer> findEngineerForKeFu(Page<Engineer> page, Engineer engineer) {
        return MDUtils.findListForPage(page,engineer, Engineer.class, MDEngineer.class, msEngineerFeign::findEngineerForKeFu);
    }

    /**
     *
     * @param page
     * @param engineer
     * @return
     */
    public Page<Long> findAppFlagEngineer(Page<Engineer> page, Engineer engineer) {
        return findPageIdsList(page,engineer,msEngineerFeign::findAppFlagEngineer);
    }

    public Page<Long> findPagingIdWithNameOrPhone(Page<Engineer> page, Engineer engineer) {
        return findPageIdsList(page,engineer,msEngineerFeign::findPagingIdWithNameOrPhone);
    }

    public Page<Long> findPageIdsList(Page<Engineer> page, Engineer engineer, Function<MDEngineer,MSResponse<MSPage<Long>>> fun) {
        MDEngineer mdEngineer = mapper.map(engineer, MDEngineer.class);

        Page<Long> longPage = new Page<>();
        longPage.setPageSize(page.getPageSize());

        mdEngineer.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        MSResponse<MSPage<Long>> returnResponse = fun.apply(mdEngineer);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<Long>  msPage = returnResponse.getData();
            longPage.setList(msPage.getList());
            longPage.setCount(msPage.getRowCount());
        } else {
            longPage.setCount(0);
            longPage.setList(new ArrayList<>());
        }
        return longPage;
    }

    /**
     * 根据安维服务区域id获取能手机接单的网点主账号安维人员列表信息  //2019-11-8
     * @param areaId
     * @return
     */
    public List<Engineer> findEngineerListByServiceAreaId(Long areaId) {
        return MDUtils.findListNecessaryConvertType(Engineer.class, ()->msEngineerFeign.findEngineerListByServiceAreaId(areaId));
    }

    /**
     * 分页查询安维数据
     * @param page
     * @param engineer
     * @return
     */
    public Page<Engineer> findEngineerList(Page<Engineer> page, Engineer engineer) {
        return MDUtils.findListForPage(page, engineer, Engineer.class, MDEngineerDto.class, msEngineerFeign::findEngineerList);
    }


    /**
     * 保存安维信息
     * @param engineer
     * @param isNew
     * @return
     */
    public MSErrorCode save(Engineer engineer, boolean isNew) {
        return MDUtils.genericSaveShouldReturnId(engineer, MDEngineer.class, isNew, isNew?msEngineerFeign::save:msEngineerFeign::update, true);
    }

    /**
     * 保存安维姓名信息
     * @param engineer
     * @return
     */
    public MSErrorCode updateEngineerName(Engineer engineer) {
        return MDUtils.genericSave(engineer, MDEngineer.class, false,msEngineerFeign::updateEngineerName);
    }

    /**
     * 更新安维人员单数与评分
     * @param paramMap
     * @return
     */
    public void updateEngineerByMap(Map<String,Object> paramMap) {
        if (paramMap.isEmpty()) {
            return;
        }
        log.warn("updateEngineerByMap 传入的数据:{}", paramMap);

        MDEngineer mdEngineer = new MDEngineer();
        mdEngineer.setId(Long.valueOf(paramMap.get("id").toString()));
        if (paramMap.get("orderCount") != null) {
            mdEngineer.setOrderCount(Integer.valueOf(paramMap.get("orderCount").toString()));
        }
        if (paramMap.get("planCount") != null) {
            mdEngineer.setPlanCount(Integer.valueOf(paramMap.get("planCount").toString()));
        }
        if (paramMap.get("breakCount") != null) {
            mdEngineer.setBreakCount(Integer.valueOf(paramMap.get("breakCount").toString()));
        }
        if (paramMap.get("grade") != null) {
            mdEngineer.setGrade(Double.valueOf(paramMap.get("grade").toString()));
        }
        MSErrorCode msErrorCode =  MDUtils.customSave(()->msEngineerFeign.updateEngineerByMap(mdEngineer));

        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务修改安维人员单数及评分失败,失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 升级安维人员
     * @param engineer
     */
    public void upgradeEngineer(Engineer engineer) {
        MSErrorCode msErrorCode = MDUtils.genericSave(engineer, MDEngineer.class, false, msEngineerFeign::updateServicePointId);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务升级安维出错.错误原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 重置安维人员的主账号标志
     * @param engineerIds
     * @return
     */
    public MSErrorCode resetEngineerMasterFlag(List<Long> engineerIds) {
        return MDUtils.customSave(()->msEngineerFeign.resetEngineerMasterFlag(engineerIds));
    }

    /**
     * 删除安维人员
     *
     * @param engineer
     * @return
     */
    public void delete(Engineer engineer) {
        MSErrorCode msErrorCode = MDUtils.genericSave(engineer, MDEngineer.class, false, msEngineerFeign::enableOrDisable);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务停用或激活安维人员出错.错误原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 分页查询师傅数据
     * @param page
     * @param engineer
     * @return
     */
    public Page<Engineer> findEngineerListForSD(Page<Engineer> page, Engineer engineer) {
        return MDUtils.findListForPage(page, engineer, Engineer.class, MDEngineerDto.class, msEngineerFeign::findEngineerListForSD);
    }

}
