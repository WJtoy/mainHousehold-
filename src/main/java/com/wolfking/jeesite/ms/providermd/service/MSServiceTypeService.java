package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDServiceType;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.feign.MSServiceTypeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MSServiceTypeService {

    @Autowired
    private MSServiceTypeFeign msServiceTypeFeign;

    @Autowired
    private MapperFacade mapper;


    /**
     * 根据id服务类型信息
     * @param id
     * @return
     */
    public ServiceType getById(Long id) {
        //return MDUtils.getById(id, ServiceType.class, msServiceTypeFeign::getById);
        return MDUtils.getObjNecessaryConvertType(ServiceType.class, ()->msServiceTypeFeign.getById(id));
    }

    /**
     * 根据ID获取是否扣点和平台信息费开关标识-->财务
     *
     * @param id
     * @return
     */
    public ServiceType getTaxAndInfoFlagByIdForFI(Long id) {
        return MDUtils.getObjNecessaryConvertType(ServiceType.class, ()->msServiceTypeFeign.getTaxAndInfoFlagByIdForFI(id));
    }

    /**
     * 根据ID列表获取是否扣点和平台信息费开关标识列表-->财务
     *
     * @param ids
     * @return
     */
    public List<ServiceType> findTaxAndInfoFlagListByIdsForFI(List<Long> ids) {
        if (!org.springframework.util.ObjectUtils.isEmpty(ids)) {
            List<ServiceType> serviceTypeList = Lists.newArrayList();
            Lists.partition(ids, 50).forEach(partIds->{
                List<ServiceType> partServiceTypeList = MDUtils.findListNecessaryConvertType(ServiceType.class, ()->msServiceTypeFeign.findTaxAndInfoFlagListByIdsForFI(partIds));
                if (!org.springframework.util.ObjectUtils.isEmpty(partServiceTypeList)) {
                    serviceTypeList.addAll(partServiceTypeList);
                }
            });
            return serviceTypeList;
        }
        return Lists.newArrayList();
    }

    /**
     * 根据ID列表获取是否扣点和平台信息费开关标识列表-->财务
     * @param ids
     * @return
     */
    public Map<Long, ServiceType> findTaxAndInfoFlagMapByIdsForFI(List<Long> ids) {
        Map<Long, ServiceType> serviceTypeMap = Maps.newHashMap();

        List<ServiceType> serviceTypeList = findTaxAndInfoFlagListByIdsForFI(ids);
        if (serviceTypeList.size() == 0) {
            return serviceTypeMap;
        }
        for (ServiceType serviceType : serviceTypeList) {
            serviceTypeMap.put(serviceType.getId(), serviceType);
        }
        return serviceTypeMap;
    }

    /**
     * 获取所有数据
     * @return
     */
    public List<ServiceType> findAllList() {
        return MDUtils.findAllList(ServiceType.class, msServiceTypeFeign::findAllList);
    }


    /**
     * 获取分页数据
     * @param serviceTypePage
     * @param serviceType
     * @return
     */
    public Page<ServiceType> findList(Page<ServiceType> serviceTypePage, ServiceType serviceType) {
        return MDUtils.findListForPage(serviceTypePage, serviceType, ServiceType.class, MDServiceType.class, msServiceTypeFeign::findList);
    }


    /**
     * 添加/更新
     * @param serviceType
     * @param isNew
     * @return
     */
    public MSErrorCode save(ServiceType serviceType, boolean isNew) {
        return MDUtils.genericSave(serviceType, MDServiceType.class, isNew, isNew?msServiceTypeFeign::insert:msServiceTypeFeign::update);
    }

    /**
     * 删除
     * @param serviceType
     * @return
     */
    public MSErrorCode delete(ServiceType serviceType) {
        return MDUtils.genericSave(serviceType, MDServiceType.class, false, msServiceTypeFeign::delete);
    }

    /**
     * 根据id从缓存中读取服务类型
     * @param id
     * @return
     */
    public ServiceType getFromCache(Long id){
        MSResponse<MDServiceType> msResponse = msServiceTypeFeign.getFromCache(id);
        if(MSResponse.isSuccess(msResponse)){
           ServiceType serviceType = mapper.map(msResponse.getData(),ServiceType.class);
           if(serviceType!=null){
               Map<String, Dict> warrantyStatusMap = MSDictUtils.getDictMap("warrantyStatus");//切换为微服务
               serviceType.setWarrantyStatus(warrantyStatusMap.get(serviceType.getWarrantyStatus().getValue()));
               return serviceType;
           }else{
               return null;
           }
        }else{
            return null;
        }
    }


    /**
     * 获取所有的服务类型
     * @return map<Long,String> key为id,value为服务类型名称</>
     */
     public Map<Long,String> findAllIdsAndNames(){
         MSResponse<Map<Long,String>> msResponse = msServiceTypeFeign.findAllIdsAndNames();
         if(MSResponse.isSuccess(msResponse)){
             Map<Long,String> map = msResponse.getData();
             return map;
         }else{
             return Maps.newHashMap();
         }
     }

    /**
     * 获取所有的服务类型
     * @return map<Long,String> key为id,value为服务类型编码(code)</>
     */
    public Map<Long,String> findIdsAndCodes(){
        MSResponse<Map<Long,String>> msResponse = msServiceTypeFeign.findIdsAndCodes();
        if(MSResponse.isSuccess(msResponse)){
            Map<Long,String> map = msResponse.getData();
            return map;
        }else{
            return Maps.newHashMap();
        }
    }


    /**
     * 根据对象属性名,返回相对应的数据
     * @param fieldList 需要返回数据的对象的属性名(如果需要返回id跟名称，即fieldList.add("id")和fieldList.add("name"))
     * @return list
     */
    public List<ServiceType> findAllListWithCondition(List<String> fieldList){
        MSResponse<List<MDServiceType>> msResponse = msServiceTypeFeign.findAllListWithCondition(fieldList);
        if(MSResponse.isSuccess(msResponse)){
            List<ServiceType> list= mapper.mapAsList(msResponse.getData(),ServiceType.class);
            if(list!=null && list.size()>0){
                return list;
            }else{
                return Lists.newArrayList();
            }
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 获取工单类型为维修的服务类型列表  //add on 2019-11-26
     * @return
     */
    public List<ServiceType> findListByMaintenance() {
        return MDUtils.findAllList(ServiceType.class, msServiceTypeFeign::findListByMaintenance);
     }
}
