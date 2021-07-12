package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.kkl.kklplus.entity.md.MDServicePointPrice;
import com.kkl.kklplus.entity.md.dto.MDProductDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointPriceDto;
import com.kkl.kklplus.entity.md.dto.MDServiceTypeDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointPriceFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class MSServicePointPriceService {
    @Autowired
    private MSServicePointPriceFeign msServicePointPriceFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据网点价格id获取单笔网点价格信息
     * @param id
     * @return
     */
    public ServicePrice getPrice(Long id) {
        return MDUtils.getById(id, ServicePrice.class, msServicePointPriceFeign::getPrice );
    }

    /**
     * 获取价格列表
     *
     * @param servicePrice
     * @return
     */
    public List<ServicePrice> findPricesList(ServicePrice servicePrice) {
        List<ServicePrice> servicePriceList = Lists.newArrayList();

        int pageNo = 1;
        Page<ServicePrice> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(500);

        Page<ServicePrice> returnPage = MDUtils.findListForPage(page, servicePrice, ServicePrice.class, MDServicePointPriceDto.class, msServicePointPriceFeign::findPricesList);
        servicePriceList.addAll(returnPage.getList());
        while(pageNo < returnPage.getPageCount()) {
            pageNo++;
            page.setPageNo(pageNo);
            Page<ServicePrice> whileReturnPage = MDUtils.findListForPage(page, servicePrice, ServicePrice.class, MDServicePointPriceDto.class, msServicePointPriceFeign::findPricesList);
            servicePriceList.addAll(whileReturnPage.getList());
        }
        return servicePriceList;
    }

    /**
     * 从缓存中获取价格列表
     *
     * @param servicePrice
     * @return

    public List<ServicePrice> findPricesListFromCache(ServicePrice servicePrice) {
        return MDUtils.findList(servicePrice, ServicePrice.class, MDServicePointPriceDto.class, msServicePointPriceFeign::findPricesListFromCache);
    }*/

    /**
     * 按需读取网点服务价格  (Ryan:此方法淘汰 2020-04-20)
     * @param servicePointId   网点id
     * @param products NameValuePair<产品id,服务项目id> 列表
     * @return
    public List<ServicePrice> findPricesByProductsFromCache(Long servicePointId,List<NameValuePair<Long,Long>> products) {
        MSResponse<List<MDServicePointPriceDto>> msResponse = msServicePointPriceFeign.findPricesByProductsFromCache(servicePointId,products);
        if(!MSResponse.isSuccessCode(msResponse)){
            return null;
        }
        List<MDServicePointPriceDto> list = msResponse.getData();
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }else{
            List<ServicePrice> prices = mapper.mapAsList(list, ServicePrice.class);
            list.clear();
            return prices;
        }
    }
     */

    /**
     * 按需,按网点价格类型读取网点价格
     *
     * @param servicePointId 网点id
     * @param products       NameValuePair<产品id,服务项目id>
     * @return
     */
    public List<ServicePrice> findPricesListByCustomizePriceFlagFromCache(Long servicePointId, List<NameValuePair<Long, Long>> products) {
        MSResponse<List<MDServicePointPriceDto>> msResponse = msServicePointPriceFeign.findPricesListByCustomizePriceFlagFromCache(servicePointId, products);
        if(!MSResponse.isSuccessCode(msResponse)){
            return null;
        }
        List<MDServicePointPriceDto> list = msResponse.getData();
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }else{
            List<ServicePrice> prices = mapper.mapAsList(list, ServicePrice.class);
            list.clear();
            return prices;
        }
    }

    /**
     * 按需读取网点的远程价格
     *
     * @param servicePointId 网点id
     * @param products       NameValuePair<产品id,服务项目id>
     * @return
     */
    public List<ServicePrice> findPricesListByRemotePriceFlagFromCacheForSD(Long servicePointId, List<NameValuePair<Long, Long>> products) {
        MSResponse<List<MDServicePointPriceDto>> msResponse = msServicePointPriceFeign.findPricesListByRemotePriceFlagFromCacheForSD(servicePointId, products);
        if(!MSResponse.isSuccessCode(msResponse)){
            return null;
        }
        List<MDServicePointPriceDto> list = msResponse.getData();
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }else{
            List<ServicePrice> prices = mapper.mapAsList(list, ServicePrice.class);
            list.clear();
            return prices;
        }
    }

    /**
     * 批量获取网点价格(偏远价格或服务价格) 2021-7-9
     *
     * @param productServiceTypeList
     * @param productCategoryId
     * @param cityId
     * @param subAreaId
     * @param servicePointId
     * @return
     */
    public List<ServicePrice> findListByCategoryAndAreaAndServiceTypeAndProductFromCacheForSD(List<NameValuePair<Long, Long>> productServiceTypeList, Long productCategoryId, Long cityId, Long subAreaId, Long servicePointId) {
        MSResponse<List<MDServicePointPriceDto>> msResponse = msServicePointPriceFeign.findListByCategoryAndAreaAndServiceTypeAndProductFromCacheForSD(productServiceTypeList, productCategoryId, cityId, subAreaId, servicePointId);
        if(!MSResponse.isSuccessCode(msResponse)){
            return null;
        }
        List<MDServicePointPriceDto> list = msResponse.getData();
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }else{
            List<ServicePrice> prices = mapper.mapAsList(list, ServicePrice.class);
            list.clear();
            return prices;
        }
    }

    /**
     * 通过网点列表获取价格列表
     * @param ids
     * @param productId
     * @param serviceTypeId
     * @return
     */
    public List<ServicePrice> findPricesByPoints(List<Long> ids, Long productId, Long serviceTypeId) {
        List<ServicePrice> servicePriceList = Lists.newArrayList();
        if (ObjectUtils.isEmpty(ids)) {
            return servicePriceList;
        }
        MDServicePointPriceDto mdServicePointPriceDto = new MDServicePointPriceDto();
        mdServicePointPriceDto.setServicePointIds(ids);
        if (productId != null) {
            MDProductDto mdProductDto = new MDProductDto();
            mdProductDto.setId(productId);
            mdServicePointPriceDto.setProduct(mdProductDto);
        }
        if (serviceTypeId != null) {
            MDServiceTypeDto mdServiceTypeDto = new MDServiceTypeDto();
            mdServiceTypeDto.setId(serviceTypeId);
            mdServicePointPriceDto.setServiceType(mdServiceTypeDto);
        }

        int pageNo =1;
        Page<MDServicePointPrice> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(500);
        Page<MDServicePointPrice> returnPage = MDUtils.findMDEntityListForPage(page, mdServicePointPriceDto, msServicePointPriceFeign::findPricesByPoints);
        servicePriceList.addAll(mapper.mapAsList(returnPage.getList(), ServicePrice.class));

        while(pageNo < returnPage.getPageCount()) {
            pageNo++;
            page.setPageNo(pageNo);
            Page<MDServicePointPrice> whileReturnPage = MDUtils.findMDEntityListForPage(page, mdServicePointPriceDto, msServicePointPriceFeign::findPricesByPoints);
            servicePriceList.addAll(mapper.mapAsList(whileReturnPage.getList(), ServicePrice.class));
        }
        return servicePriceList;
    }

    /**
     * 通过网点列表获取价格列表
     * @param ids
     * @param productId
     * @param serviceTypeId
     * @return
     */
    public List<ServicePrice> findPricesByPointsAndPriceType(List<Long> ids, Long productId, Long serviceTypeId,Integer priceType) {
        List<ServicePrice> servicePriceList = Lists.newArrayList();
        if (ObjectUtils.isEmpty(ids)) {
            return servicePriceList;
        }
        MDServicePointPriceDto mdServicePointPriceDto = new MDServicePointPriceDto();
        mdServicePointPriceDto.setServicePointIds(ids);
        if (productId != null) {
            MDProductDto mdProductDto = new MDProductDto();
            mdProductDto.setId(productId);
            mdServicePointPriceDto.setProduct(mdProductDto);
        }
        if (serviceTypeId != null) {
            MDServiceTypeDto mdServiceTypeDto = new MDServiceTypeDto();
            mdServiceTypeDto.setId(serviceTypeId);
            mdServicePointPriceDto.setServiceType(mdServiceTypeDto);
        }
        mdServicePointPriceDto.setPriceType(priceType);
        int pageNo =1;
        Page<MDServicePointPrice> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(500);
        Page<MDServicePointPrice> returnPage = MDUtils.findMDEntityListForPage(page, mdServicePointPriceDto, msServicePointPriceFeign::findPricesByPointsAndPriceType);
        servicePriceList.addAll(mapper.mapAsList(returnPage.getList(), ServicePrice.class));

        while(pageNo < returnPage.getPageCount()) {
            pageNo++;
            page.setPageNo(pageNo);
            Page<MDServicePointPrice> whileReturnPage = MDUtils.findMDEntityListForPage(page, mdServicePointPriceDto, msServicePointPriceFeign::findPricesByPointsAndPriceType);
            servicePriceList.addAll(mapper.mapAsList(whileReturnPage.getList(), ServicePrice.class));
        }
        return servicePriceList;
    }
    /**
     * 通过网点列表获取价格列表
     * @param servicePointId
     * @param priceType
     * @return
     */
    public List<ServicePrice> findStandardServicePointPricesByPoints(Long servicePointId, Integer priceType, List<Long> productIds) {
        List<ServicePrice> servicePriceList = MDUtils.findListNecessaryConvertType(ServicePrice.class, ()->msServicePointPriceFeign.findStandardServicePointPricesByServicePoints(servicePointId, priceType, productIds));
        if (servicePriceList != null && !servicePriceList.isEmpty()){
            servicePriceList.stream().forEach(r->{
                r.getServicePoint().setId(servicePointId);
                r.getPriceType().setValue(priceType.toString());
            });
        }
        return servicePriceList;
    }

    public List<ServicePrice> findStandardPricesByProductIdAndPriceType(Long servicePointId, Long productId, Integer priceType) {
        List<ServicePrice> servicePriceList = MDUtils.findListNecessaryConvertType(ServicePrice.class, ()->msServicePointPriceFeign.findStandardPricesByProductIdAndPriceType(productId, priceType));
        if (servicePriceList != null && !servicePriceList.isEmpty()){
            servicePriceList.stream().forEach(r->{
                r.getServicePoint().setId(servicePointId);
                r.getPriceType().setValue(priceType.toString());
            });
        }
        return servicePriceList;
    }


    /**
     * 启用网点价格
     *
     * @param servicePrice
     * @return
     */
    public void activePrice(ServicePrice servicePrice) {
        MSErrorCode msErrorCode = MDUtils.genericSave(servicePrice, MDServicePointPrice.class, false, msServicePointPriceFeign::activePrice);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务启用网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }

    /**
     * 停用价格
     *
     * @param servicePrice
     * @return
     */
    public void stopPrice(ServicePrice servicePrice) {
        MSErrorCode msErrorCode = MDUtils.genericSave(servicePrice, MDServicePointPrice.class, false, msServicePointPriceFeign::stopPrice);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务停用网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }

    /**
     * 修改价格
     *
     * @param servicePrice
     * @return
     */
    public void updatePrice(ServicePrice servicePrice) {
        MSErrorCode msErrorCode = MDUtils.genericSave(servicePrice, MDServicePointPrice.class, false, msServicePointPriceFeign::updatePrice);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务修改网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }


    /**
     * 批量修改/添加
     * @param servicePrices
     */
    public void batchInsertOrUpdate(List<ServicePrice> servicePrices) {
        MSErrorCode msErrorCode = MDUtils.genericBatchSave(servicePrices, MDServicePointPrice.class, msServicePointPriceFeign::batchInsertOrUpdateNew);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务批量修改/添加网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }

    /**
     * 批量修改/添加
     * @param servicePrices
     */
    public void batchInsertOrUpdateThreeVer(List<ServicePrice> servicePrices,Integer priceTypeFlag) {
        List<MDServicePointPrice> servicePointPrices = mapper.mapAsList(servicePrices, MDServicePointPrice.class);
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointPriceFeign.batchInsertOrUpdateThreeVer(servicePointPrices, priceTypeFlag));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务批量修改/添加网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }
    /**
     * 批量删除
     *
     * @param servicePrices
     * @return
     */
    public void batchDelete(List<ServicePrice> servicePrices) {
        MSErrorCode msErrorCode = MDUtils.genericBatchSave(servicePrices, MDServicePointPrice.class, msServicePointPriceFeign::batchDelete);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务批量修改/添加网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }

    /**
     * 删除网点的所有价格
     *
     * @param servicePointId
     * @return

    public void deletePrices(Long servicePointId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointPriceFeign.deletePrices(servicePointId));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务删除网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }
     */

    /**
     * 按网点和产品删除价格
     *
     * @param servicePointId
     * @param products
     * @return
     */

    /*
    // 此方法没有地方调用，将删除2021-4-28
    public void deletePricesByPointAndProducts(Long servicePointId, List<Long> products) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointPriceFeign.deletePricesByPointAndProducts(servicePointId, products));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务删除网点价格失败，失败原因：" +msErrorCode.getMsg());
        }
    }
    */

    /**
     * 重载网点价格到缓存
     */
    public MSResponse<Integer> reloadPointPriceWithCache(Long servicePointId){
        return msServicePointPriceFeign.reloadPointPriceWithCache(servicePointId);
    }

    /**
     * 修改网点是否使用标准价
     * @param servicePoint
     */
    public void updateCustomizePriceFlag(ServicePoint servicePoint){
        MSErrorCode msErrorCode = MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointPriceFeign::updateCustomizePriceFlag);
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("调用微服务修改网点是否使用标准价失败，失败原因：" +msErrorCode.getMsg());
        }
    }
    /**
     * 修改网点是否使用标准价
     * @param servicePoint
     */
    public void updateRemotePriceFlag(ServicePoint servicePoint){
        MSErrorCode msErrorCode = MDUtils.genericSave(servicePoint, MDServicePoint.class, false, msServicePointPriceFeign::updateRemotePriceFlag);
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("调用微服务修改网点偏远价格标准价失败，失败原因：" +msErrorCode.getMsg());
        }
    }
}
