package com.wolfking.jeesite.ms.tmall.md.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BActionType;
import com.kkl.kklplus.entity.b2b.common.B2BTmallConstant;
import com.kkl.kklplus.entity.b2b.common.B2BTmallErrorCode;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStore;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStoreCapacity;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStoreCoverService;
import com.kkl.kklplus.entity.b2b.servicestore.Worker;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryBrandService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.tmall.md.dao.B2BServicePointBatchLogDao;
import com.wolfking.jeesite.ms.tmall.md.dao.B2BServicePointDao;
import com.wolfking.jeesite.ms.tmall.md.entity.*;
import com.wolfking.jeesite.ms.tmall.md.feign.MSServiceStoreFeign;
import com.wolfking.jeesite.ms.tmall.md.utils.MSBaseUtils;
import org.assertj.core.util.Lists;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2BServicePointService {

    @Resource
    private B2BServicePointDao b2BServicePointDao;

    @Resource
    private B2BServicePointBatchLogDao b2BServicePointBatchLogDao;

    @Autowired
    private MSServiceStoreFeign serviceStoreFeign;

    @Autowired
    private MdB2bTmallService mdB2bTmallService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSProductCategoryBrandService msProductCategoryBrandService;

    @Autowired
    private MSServicePointService msServicePointService; // add 2019-9-21


    //region 网点资料

    /**
     * 查询网点资料
     *
     * @param servicePointId 网点ID
     * @return B2B网点资料
     */
    public ServiceStore getServiceStore(Long servicePointId) {
        ServiceStore serviceStore = null;
        if (servicePointId != null) {
            //ServicePoint servicePoint = b2BServicePointDao.getServicePoint(servicePointId); // mark on 2019-9-20
            ServicePoint servicePoint = servicePointService.getServicePointAndTripleAreaById(servicePointId); // add on 2019-9-20
            if (servicePoint != null) {
                serviceStore = new ServiceStore();
                serviceStore.setServiceStoreCode(servicePoint.getId().toString());
                serviceStore.setServiceStoreName(servicePoint.getName());
                serviceStore.setPhone(servicePoint.getContactInfo1());
                serviceStore.setAddressDetail(servicePoint.getAddress());
                serviceStore.setAddressProvince(servicePoint.getArea().getParent().getParent().getName());
                serviceStore.setAddressCity(servicePoint.getArea().getParent().getName());
                serviceStore.setAddressDistrict(servicePoint.getArea().getName());
                serviceStore.setAddressCode(StringUtils.toLong(servicePoint.getArea().getCode()));
                MSBaseUtils.preInsert(serviceStore);
            }
        }
        return serviceStore;
    }

    /**
     * 插入网点
     *
     * @param servicePointId 网点ID
     * @return
     */
    private MSResponse<String> insertServiceStore(Long servicePointId) {
        ServiceStore serviceStore = getServiceStore(servicePointId);
        MSResponse<String> responseEntity = null;
        if (serviceStore != null) {
            serviceStore.setActionType(B2BActionType.ACTION_TYPE_CREATE.value);
            responseEntity = serviceStoreFeign.insertServiceStore(serviceStore);
        }
        return responseEntity;
    }

    /**
     * 更新网点
     *
     * @param servicePointId
     */
    private MSResponse<String> updateServiceStore(Long servicePointId) {
        ServiceStore serviceStore = getServiceStore(servicePointId);
        MSResponse<String> responseEntity = null;
        if (serviceStore != null) {
            serviceStore.setActionType(B2BActionType.ACTION_TYPE_UPDATE.value);
            responseEntity = serviceStoreFeign.updateServiceStore(serviceStore);
        }
        return responseEntity;
    }

    /**
     * 删除网点
     *
     * @param servicePointId
     */
    private MSResponse<String> deleteServiceStore(Long servicePointId) {
        MSResponse<String> responseEntity = null;
        if (servicePointId != null) {
            ServiceStore serviceStore = new ServiceStore();
            serviceStore.setServiceStoreCode(servicePointId.toString());
            serviceStore.setActionType(B2BActionType.ACTION_TYPE_DELETE.value);
            MSBaseUtils.preInsert(serviceStore);
            responseEntity = serviceStoreFeign.deleteServiceStore(serviceStore);
        }
        return responseEntity;
    }

    //endregion

    //region 网点覆盖服务

    /**
     * 查询网点覆盖服务
     *
     * @param servicePointId
     * @return
     */
    private ServiceStoreCoverService getServiceStoreCoverService(Long servicePointId) {
        ServiceStoreCoverService coverService = null;
        if (servicePointId != null) {
            coverService = new ServiceStoreCoverService();
            coverService.setServiceStoreCode(servicePointId.toString());
            coverService.setBizType(B2BTmallConstant.FIXED_VALUE_BIZTYPE);
            coverService.setServiceCodes(B2BTmallConstant.FIXED_VALUE_SERVICECODES);

//            List<B2BProductCategory> productCategorys = b2BServicePointDao.getB2BProductCategoryMap(servicePointId); //mark on 2019-8-22
            List<B2BProductCategory> productCategorys = getB2BProductCategoryMap(servicePointId);   //add on 2019-8-22 //product微服务
//            List<B2BCategoryBrand> categoryBrands = b2BServicePointDao.getCategoryBrandMap(servicePointId);   //mark on 2019-8-22
            List<B2BCategoryBrand> categoryBrands = getCategoryBrandMap(servicePointId);   //add on 2019-8-22 //product微服务

            Set<Long> categoryIdSet = Sets.newSet();
            Map<Long, List<B2BProductCategory>> productCategoryMap = Maps.newConcurrentMap();
            for (B2BProductCategory item : productCategorys) {
                categoryIdSet.add(item.getProductCategoryId());
                List<B2BProductCategory> temp = null;
                if (productCategoryMap.containsKey(item.getProductCategoryId())) {
                    temp = productCategoryMap.get(item.getProductCategoryId());
                } else {
                    temp = Lists.newArrayList();
                    productCategoryMap.put(item.getProductCategoryId(), temp);
                }
                temp.add(item);
            }

            Map<Long, List<B2BCategoryBrand>> categoryBrandMap = Maps.newConcurrentMap();
            for (B2BCategoryBrand item : categoryBrands) {
                categoryIdSet.add(item.getProductCategoryId());
                List<B2BCategoryBrand> temp = null;
                if (categoryBrandMap.containsKey(item.getProductCategoryId())) {
                    temp = categoryBrandMap.get(item.getProductCategoryId());
                } else {
                    temp = Lists.newArrayList();
                    categoryBrandMap.put(item.getProductCategoryId(), temp);
                }
                temp.add(item);
            }

            List<CategoryIdsAndBrandIds> categoryIdsAndBrandIdsList = Lists.newArrayList();
            for (Long categoryId : categoryIdSet) {
                List<B2BProductCategory> tmpProductCategorys = productCategoryMap.get(categoryId);
                List<B2BCategoryBrand> tmpCategoryBrands = categoryBrandMap.get(categoryId);
                if (tmpProductCategorys != null && tmpProductCategorys.size() > 0 &&
                        tmpCategoryBrands != null && tmpCategoryBrands.size() > 0) {
                    Set<Long> customerCategorySet = tmpProductCategorys.stream().
                            map(B2BProductCategory::getCustomerCategoryId).
                            collect(Collectors.toSet());
                    Set<String> brandCodeSet = tmpCategoryBrands.stream().
                            map(B2BCategoryBrand::getBrandCode).
                            collect(Collectors.toSet());
                    ;
                    CategoryIdsAndBrandIds categoryIdsAndBrandIds = new CategoryIdsAndBrandIds();
                    categoryIdsAndBrandIds.setCategoryIds(StringUtils.join(customerCategorySet, "|"));
                    categoryIdsAndBrandIds.setBrandIds(StringUtils.join(brandCodeSet, "|"));
                    categoryIdsAndBrandIdsList.add(categoryIdsAndBrandIds);
                }
            }

            String categoryIdsAndBrandIdsJson = GsonUtils.getInstance().toGson(categoryIdsAndBrandIdsList);
            coverService.setCategoryIdsAndBrandIds(categoryIdsAndBrandIdsJson);
            MSBaseUtils.preInsert(coverService);
        }
        return coverService;
    }

    /**
     * 插入网点覆盖的服务
     *
     * @param servicePointId
     */
    private MSResponse<String> insertServiceStoreCoverService(Long servicePointId) {
        ServiceStoreCoverService coverService = getServiceStoreCoverService(servicePointId);
        MSResponse<String> responseEntity = null;
        if (coverService != null) {
            if (StringUtils.isNotBlank(coverService.getCategoryIdsAndBrandIds())) {
                coverService.setActionType(B2BActionType.ACTION_TYPE_CREATE.value);
                responseEntity = serviceStoreFeign.insertServiceStoreCoverService(coverService);
            } else {
                responseEntity = new MSResponse<>(MSErrorCode.FAILURE);
                responseEntity.setMsg("缺失天猫类目、品牌");
            }
        }
        return responseEntity;
    }

    /**
     * 更新网点覆盖的服务
     *
     * @param servicePointId
     */
    private MSResponse<String> updateServiceStoreCoverService(Long servicePointId) {
        ServiceStoreCoverService coverService = getServiceStoreCoverService(servicePointId);
        MSResponse<String> responseEntity = null;
        if (coverService != null) {
            if (StringUtils.isNotBlank(coverService.getCategoryIdsAndBrandIds())) {
                coverService.setActionType(B2BActionType.ACTION_TYPE_UPDATE.value);
                responseEntity = serviceStoreFeign.updateServiceStoreCoverService(coverService);
            } else {
                responseEntity = new MSResponse<>(MSErrorCode.FAILURE);
                responseEntity.setMsg("缺失天猫类目、品牌");
            }

        }
        return responseEntity;
    }

    /**
     * 删除网点覆盖的服务
     *
     * @param servicePointId
     */
    private MSResponse<String> deleteServiceStoreCoverService(Long servicePointId) {
        MSResponse<String> responseEntity = null;
        if (servicePointId != null) {
            ServiceStoreCoverService coverService = new ServiceStoreCoverService();
            coverService.setServiceStoreCode(servicePointId.toString());
            coverService.setBizType(B2BTmallConstant.FIXED_VALUE_BIZTYPE);
            coverService.setActionType(B2BActionType.ACTION_TYPE_DELETE.value);
            MSBaseUtils.preInsert(coverService);

            responseEntity = serviceStoreFeign.deleteServiceStoreCoverService(coverService);
        }
        return responseEntity;
    }

    //endregion

    //region 网点容量

    /**
     * 查询网点容量
     *
     * @param servicePointId
     * @return
     */
    private ServiceStoreCapacity getServiceStoreCapacity(Long servicePointId) {
        ServiceStoreCapacity serviceStoreCapacity = null;
        if (servicePointId != null) {
            serviceStoreCapacity = new ServiceStoreCapacity();
            serviceStoreCapacity.setServiceStoreCode(servicePointId.toString());
            serviceStoreCapacity.setBizType(B2BTmallConstant.FIXED_VALUE_BIZTYPE);
            serviceStoreCapacity.setServiceCodes(B2BTmallConstant.FIXED_VALUE_SERVICECODES);

//            List<B2BProductCategory> productCategorys = b2BServicePointDao.getB2BProductCategoryMap(servicePointId);  // mark on 2019-8-22
            List<B2BProductCategory> productCategorys = getB2BProductCategoryMap(servicePointId);   // add on 2019-8-22 //product微服务
            List<Area> areas = b2BServicePointDao.getServicePointServiceAreas(servicePointId);
//          Long capacity = b2BServicePointDao.getServicePointCapacity(servicePointId);     // mark on 2019-9-21
            Long capacity = msServicePointService.getServicePointCapacity(servicePointId);  // add on 2019-9-21  //ServicePoint微服务
            List<CategoryIdsAndAreaCodesAndCapacity> categoryIdsAndAreaCodesAndCapacities = Lists.newArrayList();
            if (productCategorys != null && productCategorys.size() > 0 &&
                    areas != null && areas.size() > 0 &&
                    capacity != null) {

                Set<Long> customerCategorySet = productCategorys.stream().
                        map(B2BProductCategory::getCustomerCategoryId).
                        collect(Collectors.toSet());
                //天猫只支持6位的区域编码
                Set<String> areaCodeSet = areas.stream()
                        .filter(x -> x.getCode() != null && !x.getCode().equals(""))
                        .map(x -> x.getCode().substring(0, 6))
                        .collect(Collectors.toSet());

                CategoryIdsAndAreaCodesAndCapacity categoryIdsAndAreaCodesAndCapacity = new CategoryIdsAndAreaCodesAndCapacity();
                categoryIdsAndAreaCodesAndCapacity.setCategoryIds(StringUtils.join(customerCategorySet, "|"));
                categoryIdsAndAreaCodesAndCapacity.setAreaCodes(StringUtils.join(areaCodeSet, "|"));
                categoryIdsAndAreaCodesAndCapacity.setCapacity(capacity);
                categoryIdsAndAreaCodesAndCapacities.add(categoryIdsAndAreaCodesAndCapacity);
            }
            Gson gson = new Gson();
            String categoryIdsAndAreaCodesAndCapacityJson = gson.toJson(categoryIdsAndAreaCodesAndCapacities);
            serviceStoreCapacity.setCategoryIdsAndAreaCodesAndCapacity(categoryIdsAndAreaCodesAndCapacityJson);
            MSBaseUtils.preInsert(serviceStoreCapacity);
        }

        return serviceStoreCapacity;
    }

    /**
     * 插入网点容量
     *
     * @param servicePointId
     */
    private MSResponse<String> insertServiceStoreCapacity(Long servicePointId) {
        ServiceStoreCapacity serviceStoreCapacity = getServiceStoreCapacity(servicePointId);
        MSResponse<String> responseEntity = null;
        if (serviceStoreCapacity != null) {
            if (StringUtils.isNotBlank(serviceStoreCapacity.getCategoryIdsAndAreaCodesAndCapacity())) {
                serviceStoreCapacity.setActionType(B2BActionType.ACTION_TYPE_CREATE.value);
                responseEntity = serviceStoreFeign.insertServiceStoreCapacity(serviceStoreCapacity);
            } else {
                responseEntity = new MSResponse<>(MSErrorCode.FAILURE);
                responseEntity.setMsg("缺少天猫类目、覆盖区域及对应的容量");
            }
        }
        return responseEntity;
    }

    /**
     * 更新网点容量
     *
     * @param servicePointId
     */
    private MSResponse<String> updateServiceStoreCapacity(Long servicePointId) {
        ServiceStoreCapacity serviceStoreCapacity = getServiceStoreCapacity(servicePointId);
        MSResponse<String> responseEntity = null;
        if (serviceStoreCapacity != null) {
            if (StringUtils.isNotBlank(serviceStoreCapacity.getCategoryIdsAndAreaCodesAndCapacity())) {
                serviceStoreCapacity.setActionType(B2BActionType.ACTION_TYPE_UPDATE.value);
                responseEntity = serviceStoreFeign.updateServiceStoreCapacity(serviceStoreCapacity);
            } else {
                responseEntity = new MSResponse<>(MSErrorCode.FAILURE);
                responseEntity.setMsg("缺少天猫类目、覆盖区域及对应的容量");
            }
        }
        return responseEntity;
    }

    /**
     * 删除网点容量
     *
     * @param servicePointId
     */
    private MSResponse<String> deleteServiceStoreCapacity(Long servicePointId) {
        MSResponse<String> responseEntity = null;
        if (servicePointId != null) {
            ServiceStoreCapacity serviceStoreCapacity = new ServiceStoreCapacity();
            serviceStoreCapacity.setServiceStoreCode(servicePointId.toString());
            serviceStoreCapacity.setBizType(B2BTmallConstant.FIXED_VALUE_BIZTYPE);
            serviceStoreCapacity.setActionType(B2BActionType.ACTION_TYPE_DELETE.value);
            MSBaseUtils.preInsert(serviceStoreCapacity);

            responseEntity = serviceStoreFeign.deleteServiceStoreCapacity(serviceStoreCapacity);
        }
        return responseEntity;
    }

    //endregion

    //region 服务商工人信息

    /**
     * 查询工人信息
     *
     * @param engineerId
     * @return
     */
    private Worker getWorker(Long engineerId) {
        Worker worker = null;
        if (engineerId != null) {
            Engineer engineer = b2BServicePointDao.getEngineer(engineerId);
            if (engineer != null) {
                worker = new Worker();
                worker.setBizType(B2BTmallConstant.FIXED_VALUE_BIZTYPE);
                worker.setServiceCodes(B2BTmallConstant.FIXED_VALUE_SERVICECODES);

                worker.setName(engineer.getName());
                worker.setPhone(engineer.getContactInfo());
                worker.setIdentityId(engineer.getId().toString());
                worker.setServiceStoreCode(engineer.getServicePoint().getId().toString());

//                List<B2BProductCategory> productCategorys = b2BServicePointDao.getB2BProductCategoryMap(engineer.getServicePoint().getId()); // mark on 2019-8-22
                List<B2BProductCategory> productCategorys = getB2BProductCategoryMap(engineer.getServicePoint().getId());  // add on 2019-8-22 //product微服务
                if (productCategorys != null && productCategorys.size() > 0) {
                    Set<Long> customerCategorySet = productCategorys.stream().
                            map(B2BProductCategory::getCustomerCategoryId).
                            collect(Collectors.toSet());
                    worker.setCoverCategoryIds(StringUtils.join(customerCategorySet, "|"));
                }

                MSBaseUtils.preInsert(worker);
            }
        }
        return worker;
    }

    /**
     * 插入服务商工人信息
     *
     * @param engineerId
     */
    private MSResponse<String> insertWorker(Long engineerId) {
        Worker worker = getWorker(engineerId);
        MSResponse<String> responseEntity = null;
        if (worker != null) {
            if (StringUtils.isNotBlank(worker.getCoverCategoryIds())) {
                worker.setActionType(B2BActionType.ACTION_TYPE_CREATE.value);
                responseEntity = serviceStoreFeign.insertWorker(worker);
            } else {
                responseEntity = new MSResponse<>(MSErrorCode.FAILURE);
                responseEntity.setMsg("缺少天猫类目");
            }
        }
        return responseEntity;
    }

    /**
     * 更新服务商工人信息
     *
     * @param engineerId
     */
    private MSResponse<String> updateWorkder(Long engineerId) {
        Worker worker = getWorker(engineerId);
        MSResponse<String> responseEntity = null;
        if (worker != null) {
            if (StringUtils.isNotBlank(worker.getCoverCategoryIds())) {
                worker.setActionType(B2BActionType.ACTION_TYPE_UPDATE.value);
                responseEntity = serviceStoreFeign.updateWorker(worker);
            } else {
                responseEntity = new MSResponse<>(MSErrorCode.FAILURE);
                responseEntity.setMsg("缺少天猫类目");
            }
        }
        return responseEntity;
    }

    /**
     * 删除服务商工人信息
     *
     * @param engineerId 师傅ID
     */
    private MSResponse<String> deleteWorkder(Long engineerId) {
        MSResponse<String> responseEntity = null;
        if (engineerId != null) {
            Engineer engineer = b2BServicePointDao.getEngineer(engineerId);
            if (engineer != null) {
                Worker worker = new Worker();
                worker.setName(engineer.getName());
                worker.setPhone(engineer.getContactInfo());
                worker.setBizType(B2BTmallConstant.FIXED_VALUE_BIZTYPE);
                worker.setIdentityId(engineer.getId().toString());
                worker.setActionType(B2BActionType.ACTION_TYPE_DELETE.value);
                MSBaseUtils.preInsert(worker);
                responseEntity = serviceStoreFeign.deleteWorker(worker);
            }
        }
        return responseEntity;
    }

    //endregion

    //region 网点资料批量操作

    /**
     * 按市统计网点数量与师傅数量
     *
     * @return
     */
    public List<ServicePointProvinceBatch> getServicePointProvinceBatchList(Long provinceId) {
        List<ServicePointProvinceBatch> servicePointCityList = b2BServicePointDao.getCityServicePointCount(provinceId);
        List<ServicePointProvinceBatch> engineerCityList = b2BServicePointDao.getCityEngineerCount(provinceId);
        List<B2BServicePointBatchLog> batchLogList = b2BServicePointBatchLogDao.getAllServicePointBatchLogs();

        Map<Long, ServicePointProvinceBatch> servicePointCityMap = Maps.newHashMap();
        for (ServicePointProvinceBatch item : servicePointCityList) {
            servicePointCityMap.put(item.getCity().getId(), item);
        }
        Map<Long, ServicePointProvinceBatch> engineerCityMap = Maps.newHashMap();
        for (ServicePointProvinceBatch item : engineerCityList) {
            engineerCityMap.put(item.getCity().getId(), item);
        }
        Map<Long, B2BServicePointBatchLog> batchLogMap = Maps.newHashMap();
        for (B2BServicePointBatchLog item : batchLogList) {
            batchLogMap.put(item.getCity().getId(), item);
        }

        List<Area> cityList = areaService.findListByType(Area.TYPE_VALUE_CITY);
        if (provinceId != null) {
            cityList = cityList.stream().filter(i -> i.getParentId() == provinceId).collect(Collectors.toList());
        }
        Map<Long, List<ServicePointProvinceBatch>> provinceBatchMap = Maps.newHashMap();
        List<ServicePointProvinceBatch> tempList = null;
        ServicePointProvinceBatch tempItem = null;
        ServicePointProvinceBatch servicePointCity = null;
        ServicePointProvinceBatch engineerCity = null;
        B2BServicePointBatchLog batchLog = null;
        for (Area city : cityList) {
            tempItem = new ServicePointProvinceBatch();
            tempItem.setCity(city);
            servicePointCity = servicePointCityMap.get(city.getId());
            tempItem.setServicePointCount(servicePointCity != null ? servicePointCity.getServicePointCount() : 0);
            engineerCity = engineerCityMap.get(city.getId());
            tempItem.setEngineerCount(engineerCity != null ? engineerCity.getEngineerCount() : 0);
            batchLog = batchLogMap.get(city.getId());
            if (batchLog != null) {
                tempItem.setBatchLog(batchLog);
            }

            if (provinceBatchMap.containsKey(city.getParentId())) {
                tempList = provinceBatchMap.get(city.getParentId());
            } else {
                tempList = Lists.newArrayList();
                provinceBatchMap.put(city.getParentId(), tempList);
            }
            tempList.add(tempItem);
        }

        List<Area> provinceList = areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
        if (provinceId != null) {
            provinceList = provinceList.stream().filter(i -> i.getId().equals(provinceId)).collect(Collectors.toList());
        }
        provinceList = provinceList.stream().sorted(Comparator.comparing(Area::getId)).collect(Collectors.toList());
        List<ServicePointProvinceBatch> provinceBatchList = Lists.newArrayList();
        for (Area province : provinceList) {
            tempItem = new ServicePointProvinceBatch();
            tempItem.setProvince(province);
            tempList = provinceBatchMap.get(province.getId());
            if (tempList != null && tempList.size() > 0) {
                tempItem.setSubItemlist(tempList);
            }
            provinceBatchList.add(tempItem);
        }

        return provinceBatchList;
    }


    /**
     * 批量上传网点资料
     *
     * @param cityId
     */
    public List<Integer> uploadServicePointsToTmallByCityId(long cityId) {
        List<Long> servicePointIdList = b2BServicePointDao.getServicePointIdsByCityId(cityId);
        List<B2BServicePointBatchLog.BatchProcessComment> processCommentList = Lists.newArrayList();
        MSResponse<String> response = null;
        int serviceStoreFailureCount = 0;
        int coverServiceFailureCount = 0;
        int capacityFailureCount = 0;
        int workerFailureCount = 0;
        for (Long id : servicePointIdList) {
            response = insertServiceStore(id);
            if (!MSResponse.isSuccess(response)) {
                if (response != null && response.getCode() == B2BTmallErrorCode.SERVICESTORE_EXISTED.code) {
                    response = updateServiceStore(id);
                    if (!MSResponse.isSuccess(response)) {
                        processCommentList.add(getBatchProcessComment(id, null, B2BActionType.ACTION_TYPE_UPDATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORE, response));
                        serviceStoreFailureCount++;
                    }
                } else {
                    processCommentList.add(getBatchProcessComment(id, null, B2BActionType.ACTION_TYPE_CREATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORE, response));
                    serviceStoreFailureCount++;
                }
            }

            response = insertServiceStoreCoverService(id);
            if (!MSResponse.isSuccess(response)) {
                if (response != null && response.getCode() == B2BTmallErrorCode.SERVICESTORE_COVERSERVICE_EXISTED.code) {
                    response = updateServiceStoreCoverService(id);
                    if (!MSResponse.isSuccess(response)) {
                        processCommentList.add(getBatchProcessComment(id, null, B2BActionType.ACTION_TYPE_UPDATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORECOVERSERVICE, response));
                        coverServiceFailureCount++;
                    }
                } else {
                    processCommentList.add(getBatchProcessComment(id, null, B2BActionType.ACTION_TYPE_CREATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORECOVERSERVICE, response));
                    coverServiceFailureCount++;
                }
            }

            response = insertServiceStoreCapacity(id);
            if (!MSResponse.isSuccess(response)) {
                if (response != null && response.getCode() == B2BTmallErrorCode.SERVICESTORE_CAPACITY_EXISTED.code) {
                    response = updateServiceStoreCapacity(id);
                    if (!MSResponse.isSuccess(response)) {
                        processCommentList.add(getBatchProcessComment(id, null, B2BActionType.ACTION_TYPE_UPDATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORECAPACITY, response));
                        capacityFailureCount++;
                    }
                } else {
                    processCommentList.add(getBatchProcessComment(id, null, B2BActionType.ACTION_TYPE_CREATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORECAPACITY, response));
                    capacityFailureCount++;
                }
            }
        }

        List<Engineer> engineerList = b2BServicePointDao.getEngineerIdsByCityId(cityId);
        for (Engineer item : engineerList) {
            response = insertWorker(item.getId());
            if (!MSResponse.isSuccess(response)) {
                if (response != null && response.getCode() == B2BTmallErrorCode.WORKER_EXISTED.code) {
                    response = updateWorkder(item.getId());
                    if (!MSResponse.isSuccess(response)) {
                        processCommentList.add(getBatchProcessComment(item.getServicePoint().getId(), item.getId(), B2BActionType.ACTION_TYPE_UPDATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_WORKER, response));
                        workerFailureCount++;
                    }
                } else {
                    processCommentList.add(getBatchProcessComment(item.getServicePoint().getId(), item.getId(), B2BActionType.ACTION_TYPE_CREATE, MdB2bTmall.InterfaceType.INTERFACE_TYPE_WORKER, response));
                    workerFailureCount++;
                }
            }
        }
        int servicePointCount = servicePointIdList.size();
        int engineerCount = engineerList.size();
        mdB2bTmallService.updateServicePointBatchLog(cityId, servicePointCount - serviceStoreFailureCount, serviceStoreFailureCount,
                servicePointCount - coverServiceFailureCount, coverServiceFailureCount, servicePointCount - capacityFailureCount, capacityFailureCount,
                engineerCount - workerFailureCount, workerFailureCount, processCommentList);

        return Lists.newArrayList(serviceStoreFailureCount, coverServiceFailureCount, capacityFailureCount, workerFailureCount);
    }

    /**
     * 批量删除网点资料
     *
     * @param cityId
     */
    public List<Integer> deleteServicePointsToTmallByCityId(long cityId) {
        List<Long> servicePointIdList = b2BServicePointDao.getServicePointIdsByCityId(cityId);
        MSResponse<String> response = null;
        int serviceStoreFailureCount = 0;
        int coverServiceFailureCount = 0;
        int capacityFailureCount = 0;
        int workerFailureCount = 0;
        for (Long id : servicePointIdList) {
            response = deleteServiceStore(id);
            if (!MSResponse.isSuccess(response)) {
                serviceStoreFailureCount++;
            }

            response = deleteServiceStoreCoverService(id);
            if (!MSResponse.isSuccess(response)) {
                coverServiceFailureCount++;
            }

            response = deleteServiceStoreCapacity(id);
            if (!MSResponse.isSuccess(response)) {
                capacityFailureCount++;
            }
        }

        List<Engineer> engineerList = b2BServicePointDao.getEngineerIdsByCityId(cityId);
        for (Engineer item : engineerList) {
            response = deleteWorkder(item.getId());
            if (!MSResponse.isSuccess(response)) {
                workerFailureCount++;
            }
        }

        return Lists.newArrayList(serviceStoreFailureCount, coverServiceFailureCount, capacityFailureCount, workerFailureCount);
    }

    /**
     * 获取批处理操作的备注信息
     *
     * @param response
     * @return
     */
    private B2BServicePointBatchLog.BatchProcessComment getBatchProcessComment(Long servicePointId, Long engineerId, B2BActionType actionType, MdB2bTmall.InterfaceType interfaceType, MSResponse response) {
        B2BServicePointBatchLog.BatchProcessComment processComment = new B2BServicePointBatchLog.BatchProcessComment();
        processComment.setServicePointId(servicePointId);
        processComment.setEngineerId(engineerId);
        processComment.setActionType(actionType.value);
        processComment.setInterfaceType(interfaceType.value);
        if (response != null) {
            processComment.setErrorCode(response.getCode());
            processComment.setErrorMsg(response.getMsg());
        }
        return processComment;
    }
    //endregion

    public List<B2BProductCategory> getB2BProductCategoryMap(Long servicePointId) {
        // add on 2019-8-22
        List<B2BProductCategory> productCategorys = b2BServicePointDao.getB2BProductCategoryMap(servicePointId);

        if (productCategorys == null || productCategorys.isEmpty()) {
            return productCategorys;
        }
        String productIds = productCategorys.stream().map(g -> g.getProductId().toString()).distinct().collect(Collectors.joining(","));

        Product product = new Product();
        product.setProductIds(productIds);
        List<Product> productList = msProductService.findListByConditions(product);

        Map<Long, Long> categoryMap = null;
        if (productList != null && !productList.isEmpty()) {
            categoryMap = productList.stream().collect(Collectors.toMap(g ->g.getId(), g -> g.getCategory().getId()));
        }

        final Map<Long, Long> finalCategoryMap = categoryMap;
        productCategorys.stream().forEach(r->{
            r.setProductCategoryId(finalCategoryMap==null?null:finalCategoryMap.get(r.getProductId()));
        });

        return productCategorys;
    }

    public List<B2BCategoryBrand> getCategoryBrandMap(Long servicePointId) {
        // add on 2019-8-22
        List<Product> productList = servicePointService.getProducts(servicePointId);
        String categoryIds = "";
        List<Long> categoryIdList = Lists.newArrayList();
        if (productList != null && !productList.isEmpty()) {
            //categoryIds = productList.stream().map(p->p.getCategory().getId().toString()).distinct().collect(Collectors.joining(","));  //mark on 2020-1-6
            categoryIdList = productList.stream().map(p->p.getCategory().getId()).distinct().collect(Collectors.toList());
        }
        List<B2BCategoryBrand> b2BCategoryBrandList = Lists.newArrayList();

        /*
        // mark on 2020-1-7
        if (categoryIds != null && !categoryIds.equals("")) {
            //b2BCategoryBrandList = b2BServicePointDao.getCategoryBrandMap(categoryIds);  // mark on 2019-9-6
            //b2BCategoryBrandList = msProductCategoryBrandService.getCategoryBrandMap(categoryIds);
            b2BCategoryBrandList = msProductCategoryBrandService.findCategoryBrandMap(categoryIds);
        }
        */

        if (categoryIdList != null && !categoryIdList.isEmpty()) {
            b2BCategoryBrandList = msProductCategoryBrandService.findCategoryBrandMap(categoryIdList);
        }

        return b2BCategoryBrandList;
    }
}
