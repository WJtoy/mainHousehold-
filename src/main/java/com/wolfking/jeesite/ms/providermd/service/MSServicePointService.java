package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.*;
import com.kkl.kklplus.entity.md.dto.MDServicePointDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointSearchDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointTimeLinessSummaryDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointUnionDto;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointAddressFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kkl.kklplus.utils.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MSServicePointService {
    @Autowired
    private MSServicePointFeign msServicePointFeign;

    @Autowired
    private MSServicePointAddressFeign msServicePointAddressFeign;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private AreaService areaService;

    /**
     * 根据网点id获取网点信息
     * @param id  网点Id
     * @return
     */
    public ServicePoint getById(Long id) {
        return MDUtils.getObjNecessaryConvertType(ServicePoint.class, ()->msServicePointFeign.getById(id));
    }

    /**
     * @param id 网点id
     * @return
     *     id, servicepointNo, name, useDefaultPrice, primaryId, customizePriceFlag
     */
    public ServicePoint getSimpleById(Long id) {
        return MDUtils.getObjNecessaryConvertType(ServicePoint.class, ()->msServicePointFeign.getSimpleById(id));
    }


    /**
     * 从缓存中获取网点数据
     * @param id 网点id
     * @return
     */
    public ServicePoint getCacheById(Long id) {
        return MDUtils.getObjNecessaryConvertType(ServicePoint.class, ()->msServicePointFeign.getCacheById(id));
    }

    /**
     * 从缓存中获取网点数据(为了网点付款，付款确认中快速返回有限网点字段信息(id,servicepointno,name,primaryId)
     * @param id 网点id
     * @return
     *   id,servicePointNo,name,primaryId,customizePriceFlag
     */
    public ServicePoint getSimpleCacheById(Long id) {
        return MDUtils.getObjNecessaryConvertType(ServicePoint.class, ()->msServicePointFeign.getSimpleCacheById(id));
    }

    /**
     * 按银行账号返回网点id
     * @param bankNo
     * @return
     */
    public Long getServicePointIdByBankNo(String bankNo, Long exceptId) {
        MSResponse<Long> msResponse = msServicePointFeign.getServicePointIdByBankNo(bankNo, exceptId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     *按网点手机号返回网点id
     * @param contactInfo1
     * @return
     */
    public Long getServicePointIdByContact(String contactInfo1, Long exceptId) {
        MSResponse<Long> msResponse = msServicePointFeign.getServicePointIdByContact(contactInfo1, exceptId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 通过网点编号获取网点信息
     * @param servicePointNo
     * @return
     */
    public Long getServicePointNo(String servicePointNo, Long exceptId) {
        MSResponse<Long> msResponse = msServicePointFeign.getServicePointNo(servicePointNo, exceptId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取网点容量 (B2B)
     * @param servicePointId
     * @return
     */
    public Long getServicePointCapacity(Long servicePointId) {
        MSResponse<Long> msResponse = msServicePointFeign.getServicePointCapacity(servicePointId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取网点的结算标准
     * @param servicePointId
     * @return
     */
    public int getUseDefaultPrice(Long servicePointId) {
        MSResponse<Integer> msResponse = msServicePointFeign.getUseDefaultPrice(servicePointId);
        if (MSResponse.isSuccess(msResponse)) {
            Integer useDefaultPrice = msResponse.getData();
            if (useDefaultPrice == null) {
                useDefaultPrice = 0;
            }
            return useDefaultPrice;
        }
        return 0;

    }



    /**
     * 获取区域对应的网点数量
     * @return
     */
    /*public List<ServicePointProvinceBatch> findAreaAndServicePointCountList() {
        MSResponse<List<ServicePointProvinceBatch>> msResponse = msServicePointFeign.findAreaAndServicePointCountList();
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }*/


    /**
     * 获取20000网点数据，输出字段；
     * @return
     */

    public List<ServicePoint> findPagingData() {
        //return MDUtils.findAllList(ServicePoint.class, msServicePointFeign::findPagingData);
        return null;
    }

    /**
     * 分页获取网点信息
     * @param servicePointPage
     * @param servicePoint
     * @return
     */
    public Page<ServicePoint> findList(Page<ServicePoint> servicePointPage, ServicePoint servicePoint, List<Long> areaIds) {
       //return MDUtils.findListForPage(servicePointPage, servicePoint, ServicePoint.class, MDServicePoint.class, msServicePointFeign::findList);

        MDServicePoint mdServicePoint = mapper.map(servicePoint, MDServicePoint.class);
        if (mdServicePoint.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(servicePointPage);
        }

        Page<MDServicePoint> tPage = new Page<>();
        tPage.setPageSize(servicePointPage.getPageSize());
        tPage.setPageNo(servicePointPage.getPageNo());
        mdServicePoint.setPage(new MSPage<>(tPage.getPageNo(), tPage.getPageSize()));
        MSResponse<MSPage<MDServicePoint>> returnResponse = msServicePointFeign.findList(mdServicePoint, areaIds);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<MDServicePoint>  msPage = returnResponse.getData();
            servicePointPage.setList(mapper.mapAsList(msPage.getList(), ServicePoint.class));
            servicePointPage.setCount(msPage.getRowCount());
            //log.warn("微服务方法:{};获取的分页数据:{}", strMethodName, GsonUtils.toGsonString(msPage.getList()));
        } else {
            servicePointPage.setCount(0);
            servicePointPage.setList(new ArrayList<>());
            //log.warn("微服务方法:{}; 获取的分页数据为空", strMethodName);
        }
        return servicePointPage;
    }

    /**
     * 为报表分页获取网点信息 // add on 2019-9-30
     * @param servicePointPage
     * @param servicePoint
     * @param areaIds
     * @param servicePointIds
     * @return
     */
    public Page<ServicePoint> findListForReport(Page<ServicePoint> servicePointPage, ServicePoint servicePoint, List<Long> areaIds, List<Long> servicePointIds) {
        MDServicePoint mdServicePoint = mapper.map(servicePoint, MDServicePoint.class);
        if (mdServicePoint.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(servicePointPage);
        }

        Page<MDServicePoint> tPage = new Page<>();
        tPage.setPageSize(servicePointPage.getPageSize());
        tPage.setPageNo(servicePointPage.getPageNo());
        mdServicePoint.setPage(new MSPage<>(tPage.getPageNo(), tPage.getPageSize()));
        MSResponse<MSPage<MDServicePoint>> returnResponse = msServicePointFeign.findListReport(mdServicePoint, areaIds, servicePointIds);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<MDServicePoint>  msPage = returnResponse.getData();
            servicePointPage.setList(mapper.mapAsList(msPage.getList(), ServicePoint.class));
            servicePointPage.setCount(msPage.getRowCount());
            //log.warn("微服务方法:{};获取的分页数据:{}", strMethodName, GsonUtils.toGsonString(msPage.getList()));
        } else {
            servicePointPage.setCount(0);
            servicePointPage.setList(new ArrayList<>());
            //log.warn("微服务方法:{}; 获取的分页数据为空", strMethodName);
        }
        return servicePointPage;
    }

    /**
     * 根据网点id列表获取网点列表
     * @param servicePointIds
     * @return
     */
    public List<ServicePoint> findListByIds(List<Long> servicePointIds) {
        //List<ServicePoint> servicePoints =  MDUtils.findListByCustomCondition(servicePointIds, ServicePoint.class, msServicePointFeign::findListByIds);
        List<ServicePoint> servicePointList = MDUtils.findAllList(ServicePoint.class, ()->msServicePointFeign.findListByIds(servicePointIds));
        return servicePointList;
    }

    /**
     * 根据网点id列表获取网点编号，自定义价格，价格轮次
     * @param servicePointIds
     * @return
     */
    public List<ServicePoint> findServicePointNoAndCustomizePriceFlagAndUseDefaultPriceListByIds(List<Long> servicePointIds) {
        List<ServicePoint> servicePointList = MDUtils.findAllList(ServicePoint.class, ()->msServicePointFeign.findServicePointNoAndCustomizePriceFlagAndUseDefaultPriceListByIds(servicePointIds));
        return servicePointList;
    }


    /**
     * 根据条件查询网点id列表
     * @param servicePoint
     * @return
     */
    public List<Long> findIdList(ServicePoint servicePoint, List<Long> servicePointIds) {
        List<Long>  ids = Lists.newArrayList();

        MDServicePointSearchDto mdServicePointSearchDto = convertServicePointToMDServicePointSearchDto(servicePoint, servicePointIds);
        MSResponse<MSPage<Long>>  returnPage = msServicePointFeign.findIdList(mdServicePointSearchDto);
        if (MSResponse.isSuccess(returnPage)) {
            MSPage<Long> msPage = returnPage.getData();
            if (msPage != null) {
                ids.addAll(msPage.getList());
                if (servicePoint.getPage()!= null) {
                    servicePoint.getPage().setPageNo(msPage.getPageNo());
                    servicePoint.getPage().setPageSize(msPage.getPageSize());
                    servicePoint.getPage().setCount(msPage.getRowCount());
                }
            }
        }

        return ids;
    }

    public List<Long> findIdListWithPrice(ServicePoint servicePoint, List<Long> servicePointIds) {
        List<Long>  ids = Lists.newArrayList();

        MDServicePointSearchDto mdServicePointSearchDto = convertServicePointToMDServicePointSearchDto(servicePoint, servicePointIds);
        MSResponse<MSPage<Long>>  returnPage = msServicePointFeign.findIdListWithPrice(mdServicePointSearchDto);
        if (MSResponse.isSuccess(returnPage)) {
            MSPage<Long> msPage = returnPage.getData();
            if (msPage != null) {
                ids.addAll(msPage.getList());
                if (servicePoint.getPage()!= null) {
                    servicePoint.getPage().setPageNo(msPage.getPageNo());
                    servicePoint.getPage().setPageSize(msPage.getPageSize());
                    servicePoint.getPage().setCount(msPage.getRowCount());
                }
            }
        }

        return ids;
    }

    /**
     * 按区县/街道/品类 分页查询可派单列表
     * @param servicePoint
     * @param servicePointIds
     * @return
     */
    public List<Long> findServicePointIdsForPlan(ServicePoint servicePoint, List<Long> servicePointIds) {
        List<Long>  ids = Lists.newArrayList();
        if (servicePoint.getProductCategoryId() >0 ) {
            servicePoint.setProductCategory(servicePoint.getProductCategoryId().intValue());
        }
        MDServicePointSearchDto mdServicePointSearchDto = convertServicePointToMDServicePointSearchDto(servicePoint, servicePointIds);
        MSResponse<MSPage<Long>>  returnPage = msServicePointFeign.findServicePointIdsForPlan(mdServicePointSearchDto);
        if (MSResponse.isSuccess(returnPage)) {
            MSPage<Long> msPage = returnPage.getData();
            if (msPage != null) {
                ids.addAll(msPage.getList());
                if (servicePoint.getPage()!= null) {
                    servicePoint.getPage().setPageNo(msPage.getPageNo());
                    servicePoint.getPage().setPageSize(msPage.getPageSize());
                    servicePoint.getPage().setCount(msPage.getRowCount());
                }
            }
        }

        return ids;
    }

    public MDServicePointSearchDto convertServicePointToMDServicePointSearchDto(ServicePoint servicePoint, List<Long> servicePointIds) {
        MDServicePointSearchDto  mdServicePointSearchDto = new MDServicePointSearchDto();

        mdServicePointSearchDto.setServicePointNo(servicePoint.getServicePointNo());
        mdServicePointSearchDto.setName(servicePoint.getName());
        mdServicePointSearchDto.setContactInfo1(servicePoint.getContactInfo1());
        mdServicePointSearchDto.setAreaId(Optional.ofNullable(servicePoint.getArea()).map(Area::getId).orElse(null));
        mdServicePointSearchDto.setSubAreaId(Optional.ofNullable(servicePoint.getSubArea()).map(Area::getId).orElse(null));
        mdServicePointSearchDto.setDeveloper(servicePoint.getDeveloper());
        mdServicePointSearchDto.setSignFlag(servicePoint.getSignFlag());
        mdServicePointSearchDto.setStatus(Optional.ofNullable(servicePoint.getStatus()).map(r-> StringUtils.toInteger(r.getValue())).orElse(null));
        mdServicePointSearchDto.setLevel(Optional.ofNullable(servicePoint.getLevel()).map(r-> StringUtils.toInteger(r.getValue())).orElse(null));
        mdServicePointSearchDto.setLevelRangeStart(Optional.ofNullable(servicePoint.getLevelRange()).map(IntegerRange::getStart).orElse(null));
        mdServicePointSearchDto.setLevelRangeEnd(Optional.ofNullable(servicePoint.getLevelRange()).map(IntegerRange::getEnd).orElse(null));
        //mdServicePointSearchDto.setBankIssue(Optional.ofNullable(servicePoint.getBankIssue()).map(r->StringUtils.toInteger(r.getValue())).orElse(null));
        mdServicePointSearchDto.setBankIssue(Optional.ofNullable(servicePoint.getFinance()).map(ServicePointFinance::getBankIssue).map(r->StringUtils.toInteger(r.getValue())).orElse(null));

        //mdServicePointSearchDto.setBank(Optional.ofNullable(servicePoint.getBank()).map(r->StringUtils.toInteger(r.getValue())).orElse(null));
        mdServicePointSearchDto.setBank(Optional.ofNullable(servicePoint.getFinance()).map(ServicePointFinance::getBank).map(r->StringUtils.toInteger(r.getValue())).orElse(null));

        //mdServicePointSearchDto.setBankNo(servicePoint.getBankNo());
        mdServicePointSearchDto.setBankNo(Optional.ofNullable(servicePoint.getFinance()).map(ServicePointFinance::getBankNo).orElse(null));

        //mdServicePointSearchDto.setPaymentType(Optional.ofNullable(servicePoint.getPaymentType()).map(r->StringUtils.toInteger(r.getValue())).orElse(null));
        mdServicePointSearchDto.setPaymentType(Optional.ofNullable(servicePoint.getFinance()).map(ServicePointFinance::getPaymentType).map(r->StringUtils.toInteger(r.getValue())).orElse(null));

        mdServicePointSearchDto.setProductCategory(servicePoint.getProductCategory());
        mdServicePointSearchDto.setAppFlag(servicePoint.getAppFlag());
        mdServicePointSearchDto.setAutoCompleteOrder(servicePoint.getAutoCompleteOrder());
        mdServicePointSearchDto.setAutoPlanFlag(servicePoint.getAutoPlanFlag());
        mdServicePointSearchDto.setInsuranceFlag(servicePoint.getInsuranceFlag());
        mdServicePointSearchDto.setTimeLinessFlag(servicePoint.getTimeLinessFlag());
        mdServicePointSearchDto.setCustomerTimeLinessFlag(servicePoint.getCustomerTimeLinessFlag());
        mdServicePointSearchDto.setUseDefaultPrice(servicePoint.getUseDefaultPrice());
        mdServicePointSearchDto.setOrderCount(servicePoint.getOrderCount());
        mdServicePointSearchDto.setServicePointIds(servicePointIds);
        mdServicePointSearchDto.setOrderBy(servicePoint.getOrderBy());
        mdServicePointSearchDto.setDegree(servicePoint.getDegree());
        mdServicePointSearchDto.setCustomizePriceFlag(Optional.ofNullable(servicePoint.getCustomizePriceFlag()).orElse(null));

        if (servicePoint.getPage() != null) {
            MSPage<MDServicePointSearchDto> dtoPage = new MSPage<>();
            dtoPage.setPageNo(servicePoint.getPage().getPageNo());
            dtoPage.setPageSize(servicePoint.getPage().getPageSize());
            mdServicePointSearchDto.setPage(dtoPage);
        }

        return mdServicePointSearchDto;
    }

    /**
     * 根据id批量返回网点数据
     * @param ids
     * @return
     */
    /*
    public List<ServicePoint> findBatchByIds(List<Long> ids) {
        //return MDUtils.findListByCustomCondition(ids, ServicePoint.class, msServicePointFeign::findBatchByIds);

        List<ServicePoint> servicePointList = Lists.newArrayList();
        List<List<Long>> servicePointIds = Lists.partition(ids, 1000);
        servicePointIds.stream().forEach(longList -> {
            List<ServicePoint> returnList = MDUtils.findListByCustomCondition(longList, ServicePoint.class, msServicePointFeign::findBatchByIds);
            Optional.ofNullable(returnList).ifPresent(servicePointList::addAll);
        });

        return servicePointList;
    }
    */

    /**
     * 通过网点id列表获取网点ids及网点名称列表  //add on 2019-10-12
     * @param servicePointIds
     * @return
     *  网点id,Name
     */
    /*public List<MDServicePointViewModel> findBatchIdsAndNamesByIds(List<Long> servicePointIds) {
        return doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesByIds);
    }*/


    /*public List<MDServicePointViewModel> findBatchIdsAndNamesWithPointNoByIds(List<Long> servicePointIds) {
        return doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesWithPointNoByIds);
    }*/

    /**
     * 通过网点id列表获取网点id及网点名称,网点编号哈希对象 // add on 2019-10-12
     * @param servicePointIds
     * @return
     * id,servicePointNo,name
     */
    /*public Map<Long, MDServicePointViewModel> findBatchIdsAndNamesWithPointNoByIdsToMap(List<Long> servicePointIds) {
        List<MDServicePointViewModel> mdServicePointViewModelList = doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesWithPointNoByIds);
        return mdServicePointViewModelList != null && !mdServicePointViewModelList.isEmpty()?mdServicePointViewModelList.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, Function.identity())):Maps.newHashMap();
    }*/

    /**
     * 获取网点列表数据  // add on 2019-10-12
     * @param servicePointIds
     * @return
     * id,servicePointNo,name,contractInfo1,contractInfo2,primaryId
     */
    /*
    public List<MDServicePointViewModel> findBatchIdsAndNamesWithPointNoAndInfosAndPrimaryId(List<Long> servicePointIds) {
       return doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesWithPointNoAndInfosAndPrimaryId);
    }
    */

    /*public Map<Long, MDServicePointViewModel> findBatchIdsAndNamesWithPointNoAndInfosAndPrimaryIdToMap(List<Long> servicePointIds, Consumer<List<Long>> primaryCallback) {
        List<MDServicePointViewModel> mdServicePointViewModelList = doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesWithPointNoAndInfosAndPrimaryId);

        Map<Long, MDServicePointViewModel> mdServicePointViewModelMap = Maps.newHashMap();
        List<Long> engineerIds = Lists.newArrayList();
        if (mdServicePointViewModelList != null && !mdServicePointViewModelList.isEmpty()) {
            mdServicePointViewModelMap = mdServicePointViewModelList.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, Function.identity()));
            engineerIds = mdServicePointViewModelList.stream().filter(r->r.getPrimaryId()!=null).map(MDServicePointViewModel::getPrimaryId).distinct().collect(Collectors.toList());
        }
        List<Long> finalEngineerIds = engineerIds;
        Optional.ofNullable(primaryCallback).ifPresent(c->c.accept(finalEngineerIds));
        return  mdServicePointViewModelMap;
    }*/

    /**
     *  获取网点列表数据  // add on 2019-10-12
     * @param servicePointIds
     * @return
     *       id,servicePointNo,name,contactInfo1,bank,bankOwner,bankNo,paymentType
     */
    /*
    public List<MDServicePointViewModel> findBatchIdsAndNamesWithPointNoAndInfosAndBankInfoesAndPayType(List<Long> servicePointIds) {
        return doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesWithPointNoAndInfosAndBankInfoesAndPayType);
    }
    */

    /**
     *  获取网点列表数据  // add on 2019-10-12
     * @param servicePointIds
     * @return
     *       id,servicePointNo,name,contactInfo1,bank,bankOwner,bankNo,paymentType
     */
    /*public Map<Long, MDServicePointViewModel> findBatchIdsAndNamesWithPointNoAndInfosAndBankInfoesAndPayTypeToMap(List<Long> servicePointIds) {
        List<MDServicePointViewModel> mdServicePointViewModelList = doFindBatchByIds(servicePointIds, msServicePointFeign::findBatchIdsAndNamesWithPointNoAndInfosAndBankInfoesAndPayType);
        return mdServicePointViewModelList != null && !mdServicePointViewModelList.isEmpty()?mdServicePointViewModelList.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, Function.identity())):Maps.newHashMap();
    }*/

    /**
     * 获取网点列表数据  // add on 2019-10-12
     * @param servicePointIds
     * @param fun
     * @return
     */
    /*public List<MDServicePointViewModel> doFindBatchByIds(List<Long> servicePointIds, Function<List<Long>, MSResponse<List<MDServicePointViewModel>>> fun) {
        List<MDServicePointViewModel> mdServicePointViewModelList = Lists.newArrayList();
        if (servicePointIds == null || servicePointIds.isEmpty()) {
            return mdServicePointViewModelList;
        }
        if (servicePointIds.size() < 200) {  //小于200 一次调用,
            MSResponse<List<MDServicePointViewModel>> msResponse = fun.apply(servicePointIds);
            if (MSResponse.isSuccess(msResponse)) {
                mdServicePointViewModelList = msResponse.getData();
            }
        } else { // 大于等于200 分批次调用
            List<MDServicePointViewModel> mdServicePointViewModels = Lists.newArrayList();
            List<List<Long>> servicePointIdList = Lists.partition(servicePointIds, 1000); //测试验证一次取1000笔数据比较合理
            servicePointIdList.stream().forEach(longList -> {
                MSResponse<List<MDServicePointViewModel>> msResponse = fun.apply(longList);
                if (MSResponse.isSuccess(msResponse)) {
                    Optional.ofNullable(msResponse.getData()).ifPresent(mdServicePointViewModels::addAll);
                }
            });
            if (!mdServicePointViewModels.isEmpty()) {
                mdServicePointViewModelList.addAll(mdServicePointViewModels);
            }
        }

        return mdServicePointViewModelList;
    }*/


    /**
     * 通过网点id列表及要获取的字段列表获取网点列表 // add on 2019-10-14
     * @param servicePointIds
     * @return
     *  要返回的字段跟参数fields中相同
     */
    public List<MDServicePointViewModel> findBatchByIdsByCondition(List<Long> servicePointIds, List<String> fields, Integer delFlag) {
        Class<?> cls = MDServicePointViewModel.class;
        Field[] fields1 = cls.getDeclaredFields();

        Long icount = Arrays.asList(fields1).stream().filter(r->fields.contains(r.getName())).count();
        if (icount.intValue() != fields.size()) {
            throw new RuntimeException("按条件获取网点列表数据要求返回的字段有问题，请检查");
        }

        List<MDServicePointViewModel> mdServicePointViewModelList = Lists.newArrayList();
        if (servicePointIds == null || servicePointIds.isEmpty()) {
            return mdServicePointViewModelList;
        }
        if (servicePointIds.size() < 200) {  //小于200 一次调用,
            MSResponse<List<MDServicePointViewModel>> msResponse = msServicePointFeign.findBatchByIdsByCondition(servicePointIds,fields,delFlag);
            if (MSResponse.isSuccess(msResponse)) {
                mdServicePointViewModelList = msResponse.getData();
            }
        } else { // 大于等于200 分批次调用
            List<MDServicePointViewModel> mdServicePointViewModels = Lists.newArrayList();
            List<List<Long>> servicePointIdList = Lists.partition(servicePointIds, 1000); //测试验证一次取1000笔数据比较合理
            servicePointIdList.stream().forEach(longList -> {
                MSResponse<List<MDServicePointViewModel>> msResponse = msServicePointFeign.findBatchByIdsByCondition(longList,fields,delFlag);
                if (MSResponse.isSuccess(msResponse)) {
                    Optional.ofNullable(msResponse.getData()).ifPresent(mdServicePointViewModels::addAll);
                }
            });
            if (!mdServicePointViewModels.isEmpty()) {
                mdServicePointViewModelList.addAll(mdServicePointViewModels);
            }
        }

        return mdServicePointViewModelList;
    }

    /**
     * 通过网点id列表及要获取的字段列表获取网点列表 // add on 2019-10-14
     * @param servicePointIds
     * @param fields
     * @param delFlag
     * @return
     */
    public Map<Long, MDServicePointViewModel> findBatchByIdsByConditionToMap(List<Long> servicePointIds,List<String> fields, Integer delFlag) {
        List<MDServicePointViewModel> mdServicePointViewModelList = findBatchByIdsByCondition(servicePointIds, fields, delFlag);
        return mdServicePointViewModelList != null && !mdServicePointViewModelList.isEmpty() ? mdServicePointViewModelList.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, Function.identity())):Maps.newHashMap();
    }

    /**
     * 通过网点id列表及要获取的字段列表获取网点列表 // add on 2019-10-14
     * @param servicePointIds
     * @param fields
     * @param delFlag
     * @param primaryCallback
     * @return
     */
    public Map<Long, MDServicePointViewModel> findBatchByIdsByConditionToMap(List<Long> servicePointIds,List<String> fields, Integer delFlag,  Consumer<List<Long>> primaryCallback) {
        List<MDServicePointViewModel> mdServicePointViewModelList = findBatchByIdsByCondition(servicePointIds, fields, delFlag);
        Map<Long, MDServicePointViewModel> mdServicePointViewModelMap = Maps.newHashMap();
        List<Long> engineerIds = Lists.newArrayList();
        if (mdServicePointViewModelList != null && !mdServicePointViewModelList.isEmpty()) {
            mdServicePointViewModelMap = mdServicePointViewModelList.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, Function.identity()));
            engineerIds = mdServicePointViewModelList.stream().filter(r->r.getPrimaryId()!=null).map(MDServicePointViewModel::getPrimaryId).distinct().collect(Collectors.toList());
            if (engineerIds != null && !engineerIds.isEmpty()) {
                engineerIds = engineerIds.stream().distinct().collect(Collectors.toList());
            }
        }
        List<Long> finalEngineerIds = engineerIds;
        Optional.ofNullable(primaryCallback).ifPresent(c->c.accept(finalEngineerIds));
        return  mdServicePointViewModelMap;
    }

    /**
     * 通过网点id列表及要获取的字段列表获取网点列表 // add on 2019-10-14
     * @param servicePointIds
     * @param fields
     * @param delFlag
     * @param primaryCallback
     * @return
     */
    public List<MDServicePointViewModel> findBatchByIdsByCondition(List<Long> servicePointIds,List<String> fields, Integer delFlag,  Consumer<List<Long>> primaryCallback) {
        List<MDServicePointViewModel> mdServicePointViewModelList = findBatchByIdsByCondition(servicePointIds, fields, delFlag);
        List<Long> engineerIds = Lists.newArrayList();
        if (mdServicePointViewModelList != null && !mdServicePointViewModelList.isEmpty()) {
            engineerIds = mdServicePointViewModelList.stream().filter(r->r.getPrimaryId()!=null).map(MDServicePointViewModel::getPrimaryId).distinct().collect(Collectors.toList());
            if (engineerIds != null && !engineerIds.isEmpty()) {
                engineerIds = engineerIds.stream().distinct().collect(Collectors.toList());
            }
        }
        List<Long> finalEngineerIds = engineerIds;
        Optional.ofNullable(primaryCallback).ifPresent(c->c.accept(finalEngineerIds));
        return  mdServicePointViewModelList;
    }


    /**
     * 添加/更新
     * @param servicePoint
     * @param isNew
     * @return
     */
    public MSErrorCode save(ServicePoint servicePoint, boolean isNew) {
        return MDUtils.genericSaveShouldReturnId(servicePoint, MDServicePoint.class, isNew, isNew?msServicePointFeign::insert:msServicePointFeign::update,true);
        //return MDUtils.genericSave(servicePoint, MDServicePoint.class, isNew, isNew?msServicePointFeign::insert:msServicePointFeign::update);
    }

    /**
     * 更新备注信息
     * @param servicePointId
     * @param remark
     * @return
     */
    public MSErrorCode updateRemark(Long servicePointId, String remark) {
        MSResponse<Integer> msResponse = msServicePointFeign.updateRemark(servicePointId, remark);
        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }

    /**
     * 更新派单备注
     * @param servicePointId
     * @param planRemark
     * @return
     */
    public MSErrorCode updatePlanRemark(Long servicePointId, String planRemark) {
        MSResponse<Integer> msResponse = msServicePointFeign.updatePlanRemark(servicePointId, planRemark);
        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }

    /**
     * 有条件更新网点信息
     * @param maps
     * @return
     */
    public MSErrorCode updateServicePointByMap(HashMap<String,Object>  maps) {
        return MDUtils.genericCustomConditionSave(maps, msServicePointFeign::updateServicePointByMap);
    }

    /**
     * 更新网点的关键设定值
     * @param servicePoint
     * @return
     */
    public MSErrorCode updateServicePointForKeySetting(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updateServicePointForKeySetting);
    }

    /**
     * 网点审核
     * @param servicePointIds
     * @param updateById
     * @return
     */
    public MSErrorCode approve(java.util.List<Long> servicePointIds, Long updateById) {
        return MDUtils.customSave((()->msServicePointFeign.approve(servicePointIds, updateById)));
    }

    /**
     * 更新网点付款失败原因
     * @param servicePointId
     * @param bankIssue
     * @return
     */
    public MSErrorCode updateBankIssue(Long servicePointId, String bankIssue) {
        return MDUtils.customSave((()->msServicePointFeign.updateBankIssue(servicePointId, bankIssue)));
    }

    /**
     * 是否启用网点保险扣除
     * @param id
     * @param appInsuranceFlag
     * @param updateBy
     * @param updateDate
     * @return
     */
    public MSErrorCode appReadInsuranceClause(Long id, Integer appInsuranceFlag, Long updateBy, Date updateDate) {
        long date = updateDate != null?updateDate.getTime():new Date().getTime();
        return MDUtils.customSave((()->
            msServicePointFeign.appReadInsuranceClause(id, appInsuranceFlag, updateBy, date)
        ));
    }


    /**
     * 更新网点地址信息
     * @param servicePoint
     * @return
     */
    public MSErrorCode updateServicePointAddress(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updateServicePointAddress);
    }


    /**
     * 更新网点的账号信息
     * @param servicePoint
     * @return
     */
    public MSErrorCode updateServicePointBankAccountInfo(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updateServicePointBankAccountInfo);
    }

    /**
     * 更新网点的自动派单标志
     * @param servicePoint
     * @return
     */
    public MSErrorCode updateAutoPlanFlag(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updateAutoPlanFlag);
    }

    /**
     * 更新网点等级
     * @param servicePoint
     * @return
     */
    public MSErrorCode updateLevel(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updateLevel);
    }

    /**
     * 更新网点的主账号信息
     * @param servicePoint
     * @return
     */
    public MSErrorCode updatePrimaryAccount(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updatePrimaryAccount);
    }

    /**
     * 升级网点信息
     * @param servicePoint
     * @return
     */
    public MSErrorCode upgradeServicePoint(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::upgradeServicePoint);
    }

    /**
     * 更新网点-产品类型映射
     */
    public MSErrorCode updateProductCategoryServicePointMapping(Long servicePointId, List<Long> productCategoryIds) {
        MSResponse<Integer> msResponse = msServicePointFeign.updateProductCategoryServicePointMapping(servicePointId, productCategoryIds);
        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }

    /**
     * 逻辑删除
     * @param servicePoint
     * @return
     */
    public MSErrorCode delete(ServicePoint servicePoint) {
        return MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::delete);
    }

    /**
     * 更新网点是否自定义网点价格
     * @param servicePoint
     */
    public void updateCustomizePriceFlag(ServicePoint servicePoint) {
        MSErrorCode msErrorCode = MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointFeign::updateCustomizePriceFlag);
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("调用微服务更新网点自定义使用价格.");
        }
    }

    public NameValuePair<Long,Long> saveServicePointAndEngineer(ServicePoint servicePoint, Engineer engineer) {
        NameValuePair<Long, Long> nameValuePair = null;
        MDServicePoint mdServicePoint = mapper.map(servicePoint, MDServicePoint.class);
        MDEngineer  mdEngineer = mapper.map(engineer, MDEngineer.class);
        User user = UserUtils.getUser();
        if (user != null) {
            mdEngineer.setCreateById(user.getId());
            mdEngineer.getEngineerAddress().setCreateById(user.getId());
        }
        Date currentDate = new Date();
        mdEngineer.setCreateDate(currentDate);
        mdEngineer.getEngineerAddress().setCreateDate(currentDate);

        MDServicePointUnionDto servicePointUnionDto = new MDServicePointUnionDto();
        servicePointUnionDto.setServicePoint(mdServicePoint);
        servicePointUnionDto.setEngineer(mdEngineer);
        MSResponse<NameValuePair<Long,Long>> msResponse = msServicePointFeign.insertServicePointAndEngineer(servicePointUnionDto);
        if (msResponse.getCode() >0) {
            throw new RuntimeException("调用微服务保存网点及师傅信息错误.出错原因:"+msResponse.getMsg());
        } else {
            nameValuePair = msResponse.getData();
        }
        return nameValuePair;
    }

    //region API

    /**
     * 添加网点的收货地址
     * @param mdServicePointAddress
     */
    public void saveAddress(MDServicePointAddress mdServicePointAddress) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointAddressFeign.save(mdServicePointAddress));
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("调用微服务添加网点收货地址失败.失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 添加网点的收货地址
     * @param servicePointId
     */
    public MDServicePointAddress getAddressByServicePointIdFromCache(Long servicePointId) {
       //MDServicePointAddress mdServicePointAddress = MDUtils.getByCustomCondition(()->msServicePointAddressFeign.getByServicePointIdFromCache(servicePointId));
       //return mdServicePointAddress;
        MDServicePointAddress mdServicePointAddress = MDUtils.getObjUnnecessaryConvertType(()->msServicePointAddressFeign.getByServicePointIdFromCache(servicePointId));
        return mdServicePointAddress;
    }

    //endregion API

    /**
     * 获取分页数据
     * @param page
     * @param mdServicePointDto
     * @return
     */
    public Page<MDServicePointDto> findServicePointTimeliness(Page<MDServicePointDto> page, MDServicePointDto mdServicePointDto) {
        Page<MDServicePointDto> returnPage = new Page<>();
        returnPage.setPageSize(page.getPageSize());
        returnPage.setPageNo(page.getPageNo());
        mdServicePointDto.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        MSResponse<MSPage<MDServicePointDto>> msPageMSResponse = msServicePointFeign.findServicePointTimeliness(mdServicePointDto);
        if (MSResponse.isSuccess(msPageMSResponse)) {
            MSPage<MDServicePointDto> data = msPageMSResponse.getData();
            returnPage.setCount(data.getRowCount());
            returnPage.setList(data.getList());
        }else{
            returnPage.setCount(0);
            returnPage.setList(Lists.newArrayList());
        }
        return returnPage;
    }


    public MDServicePointDto getServicePointTimeliness(Long id){
        MSResponse<MDServicePointDto> msResponse = msServicePointFeign.getServicePointTimeliness(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 修改网点时效信息
     * @param mdServicePointDto
     * @return
     */
    public void updateTimeliness(MDServicePointDto mdServicePointDto){
        MSResponse<Integer> msResponse = msServicePointFeign.updateTimeliness(mdServicePointDto);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("保存网点时效失败.失败原因:" + msResponse.getMsg());
        }
    }

    /**
     * 按省获取网点时效信息
     * @param entity
     * @return
     */
    public List<MDServicePointTimeLinessSummaryDto> servicePointAreaTimelinessList(MDServicePointTimeLinessSummaryDto entity){
        List<MDServicePointTimeLinessSummaryDto> list = Lists.newArrayList();
        List<Area> cityList = areaService.findListByParent(Area.TYPE_VALUE_CITY,entity.getAreaId());
        Map<Long,Area> map = cityList.stream().collect(Collectors.toMap(Area::getId, Function.identity(), (key1, key2) -> key2));
        List<Area> countyList = Lists.newArrayList();
        MDServicePointTimeLinessSummaryDto servicePointTimeLinessSummaryDto;
        List<Area> allCountyList = areaService.findListByType(Area.TYPE_VALUE_COUNTY);
        for (Area city:cityList){
            //countyList = areaService.findListByParent(Area.TYPE_VALUE_COUNTY,city.getId());
            countyList = allCountyList.stream().filter(t->t.getParentId()==city.getId()).collect(Collectors.toList());
            servicePointTimeLinessSummaryDto = new MDServicePointTimeLinessSummaryDto();
            servicePointTimeLinessSummaryDto.setAreaId(city.getId());
            servicePointTimeLinessSummaryDto.setAreaIds(countyList.stream().map(Area::getId).collect(Collectors.toList()));
            servicePointTimeLinessSummaryDto.setAreaName(city.getName());
            list.add(servicePointTimeLinessSummaryDto);
        }
        MSResponse<List<MDServicePointTimeLinessSummaryDto>> msResponse = msServicePointFeign.findTimeLinessFlagListByAreaIds(list);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return list;
        }
    }

    /**
     * 根据市开启或关闭快可立补贴
     * @param mdServicePointDto
     * @return
     */
    public void  updateTimelinessByArea(MDServicePointDto mdServicePointDto){
        MSResponse<Integer> msResponse = msServicePointFeign.updateTimelinessByArea(mdServicePointDto);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("保存网点快可立时效失败.失败原因：" + msResponse.getMsg());
        }
    }

    /**
     * 根据市开启或关闭网点客户时效
     * @param mdServicePointDto
     * @return
     */
    public void  updateCustomerTimelinessByArea(MDServicePointDto mdServicePointDto){
        MSResponse<Integer> msResponse = msServicePointFeign.updateCustomerTimelinessByArea(mdServicePointDto);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("保存网点客户时效.失败原因：" + msResponse.getMsg());
        }
    }

    /**
     * 根据市获取签约非返现网点，返回网点名称和网点编号
     * @param areaIds
     * @return
     */
     public List<MDServicePointDto> findIdAndPointNoByAreaIds(List<Long> areaIds){
         MSResponse<List<MDServicePointDto>> msResponse = msServicePointFeign.findIdAndPointNoByAreaIds(areaIds);
         if(MSResponse.isSuccess(msResponse)){
             return msResponse.getData();
         }else{
             return Lists.newArrayList();
         }
     }

     public NameValuePair<Long,Long> insertServicePointUnionDto(MDServicePointUnionDto servicePointUnionDto){
        MSResponse<NameValuePair<Long,Long>> msResponse = msServicePointFeign.insertServicePointUnionDto(servicePointUnionDto);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            throw new RuntimeException("调用微服务保存网点失败.失败原因："+ msResponse.getMsg());
        }
     }

    /**
     * 按区县/街道/品类 分页查询可派单列表
     * @param servicePoint
     * @return
     */
    public List<Long> findUnbleSelectForPlan(ServicePoint servicePoint) {
        List<Long>  ids = Lists.newArrayList();
        if (servicePoint.getProductCategoryId() >0 ) {
            servicePoint.setProductCategory(servicePoint.getProductCategoryId().intValue());
        }
        MDServicePointSearchDto mdServicePointSearchDto = convertServicePointToMDServicePointSearchDto(servicePoint, Lists.newArrayList());
        MSResponse<MSPage<Long>>  returnPage = msServicePointFeign.findServicePointIdsByAreaWithCategory(mdServicePointSearchDto);
        if (MSResponse.isSuccess(returnPage)) {
            MSPage<Long> msPage = returnPage.getData();
            if (msPage != null) {
                ids.addAll(msPage.getList());
                if (servicePoint.getPage()!= null) {
                    servicePoint.getPage().setPageNo(msPage.getPageNo());
                    servicePoint.getPage().setPageSize(msPage.getPageSize());
                    servicePoint.getPage().setCount(msPage.getRowCount());
                }
            }
        }
        return ids;
    }

    /**
     * 突击客服恢复网点
     * @param mdServicePoint
     * @return
     */
    public void updateStatusForPlan(MDServicePoint mdServicePoint){
        MSResponse<Integer> msResponse = msServicePointFeign.updateStatus(mdServicePoint);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("恢复网点调用微服务失败.失败原因:" +msResponse.getMsg());
        }
    }

    /**
     * 同步网点以及自动派单区域到ES
     *
     * @param id
     * @return
     */
    public void pushServicePointAndStationToES(Long id) {
        MSResponse<Integer> msResponse = msServicePointFeign.pushServicePointAndStationToES(id);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("同步网点信息到微服务失败.失败原因:" +msResponse.getMsg());
        }
    }

    /**
     * 根据网点Id列表从缓存中获取网点信息  2020-11-13
     *
     * @param servicePointIds 网点ids列表
     * @return
     */
    public List<ServicePoint> findListByIdsFromCache(List<Long> servicePointIds) {
        if (ObjectUtils.isEmpty(servicePointIds)) {
            return Lists.newArrayList();
        }
        List<ServicePoint> servicePointList = MDUtils.findListNecessaryConvertType(ServicePoint.class, ()->msServicePointFeign.findListByIdsFromCache(servicePointIds));
        return servicePointList;
    }

    /**
     * 根据网点Id列表从缓存中获取网点信息  2020-11-13
     *
     * @param servicePointIds 网点ids列表
     * @return
     */
    public Map<Long, ServicePoint> findListByIdsFromCacheToMap(List<Long> servicePointIds) {
        List<ServicePoint> servicePointList = findListByIdsFromCache(servicePointIds);
        Map<Long, ServicePoint> servicePointMap = Maps.newHashMap();
        if (!ObjectUtils.isEmpty(servicePointList)) {
            servicePointMap = servicePointList.stream().collect(Collectors.toMap(ServicePoint::getId, Function.identity()));
        }

        return servicePointMap;
    }

    /**
     * 通过网点编号获取网点信息
     * @param servicePointNo
     * @return
     */
    public Long getIdByServicePointNoForMD(String servicePointNo) {
        MSResponse<Long> msResponse = msServicePointFeign.getIdByServicePointNoForMD(servicePointNo);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }


    public int reloadServicePointCacheById(Long id) {
        MSResponse<Integer> msResponse = msServicePointFeign.reloadServicePointCacheById(id);
        if (MSResponse.isSuccess(msResponse)) {
            return Optional.ofNullable(msResponse.getData()).orElse(0);
        }
        return 0;
    }

    /**
     *提供根据网点编号获取对应是否收质保金，上限，每单扣除金额--财务
     * @param servicePointNo
     * @return
     */
    public MDDepositLevel getSpecFieldsByServicePointNoForFI(String servicePointNo){
        MDDepositLevel mdDepositLevel = new MDDepositLevel();
        MSResponse<MDDepositLevel> msResponse = msServicePointFeign.getSpecFieldsByServicePointNoForFI(servicePointNo);
        if (MSResponse.isSuccess(msResponse)) {
            mdDepositLevel = msResponse.getData();
        }
        return mdDepositLevel;
    }

    /**
     *按网点名称，编号及电话号码查询，并分页显示；排除返现网点，有质保金等级的网点id列表--工单
     * @param page,servicePoint
     * @return
     */
    public Page<Long> findIdsByServicePointWithDepositLevelForSD(Page<Long> page,ServicePoint servicePoint){
        MDServicePoint mdServicePoint = mapper.map(servicePoint, MDServicePoint.class);
        if (mdServicePoint.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(page);
        }
        Page<MDServicePoint> tPage = new Page<>();
        tPage.setPageSize(page.getPageSize());
        tPage.setPageNo(page.getPageNo());
        mdServicePoint.setPage(new MSPage<>(tPage.getPageNo(), tPage.getPageSize()));
        MSResponse<MSPage<Long>> returnResponse = msServicePointFeign.findIdsByServicePointWithDepositLevelForSD(mdServicePoint);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<Long>  msPage = returnResponse.getData();
            page.setList(msPage.getList());
            page.setCount(msPage.getRowCount());
        } else {
            page.setCount(0);
            page.setList(new ArrayList<>());
        }
        return page;
    }

    public Integer updateInsuranceFlagForMD(ServicePoint servicePoint){
        Integer num = 0;
        MDServicePoint mdServicePoint = mapper.map(servicePoint, MDServicePoint.class);
        MSResponse<Integer> msResponse = msServicePointFeign.updateInsuranceFlagForMD(mdServicePoint);
        if (MSResponse.isSuccess(msResponse)) {
            num = msResponse.getData();
        }
        return num;
    }

    /**
     * 更新网点未完工单数量
     * @param paramMap
     */
    public void updateUnfinishedOrderCountByMapForSD(Map<String,Object> paramMap){
        MSResponse<Integer> msResponse = msServicePointFeign.updateUnfinishedOrderCountByMapForSD(paramMap);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("更新网点未完工单数量调用微服务失败.失败原因:" +msResponse.getMsg());
        }
    }
}
