package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProductPrice;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ProductPrice;
import com.wolfking.jeesite.ms.providermd.feign.MSProductPriceFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class MSProductPriceService {

    @Autowired
    private MSProductPriceFeign msProductPriceFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取产品价格
     * @param id
     * @return
     */
    public ProductPrice getById(Long id) {
        return MDUtils.getById(id, ProductPrice.class, msProductPriceFeign::getById);
    }

    /**
     * 根据产品id，服务id，价格类型
     * @param productId
     * @param serviceTypeId
     * @param priceType
     * @return
     */
    public Long getIdByProductIdAndServiceTypeIdAndPriceType(Long productId, Long serviceTypeId, Integer priceType) {
        MSResponse<Long> response = msProductPriceFeign.getIdByProductIdAndServiceTypeIdAndPriceType(productId, serviceTypeId, priceType);
        Long productPriceId = null;
        if (MSResponse.isSuccess(response)) {
            productPriceId = response.getData();
        }
        return productPriceId;
    }

    /**
     * 根据产品id，服务id，价格类型(第几轮价格)获取厂商指导价格// add on 2019-11-26
     * @param productId
     * @param serviceTypeId
     * @param priceType
     * @return
     */
    public Double getPriceByProductIdAndServiceTypeIdAndPriceType(Integer priceType, Long productId, Long serviceTypeId) {
        MSResponse<Double> response = msProductPriceFeign.getPriceByProductIdAndServiceTypeIdAndPriceType(priceType, productId, serviceTypeId);
        Double customerStandardPrice = 0.0;
        if (MSResponse.isSuccess(response)) {
            customerStandardPrice = response.getData();
        }
        return customerStandardPrice;
    }

    /**
     * 根据产品id，服务id，价格类型(第几轮价格)获取厂商指导价格// add on 2019-11-26
     * @param productId
     * @param serviceTypeId
     * @param priceType
     * @return
     */
    public ProductPrice getEngineerPriceByProductIdAndServiceTypeIdAndPriceType(Integer priceType, Long productId, Long serviceTypeId) {
        //return MDUtils.getEntity(ProductPrice.class,()->msProductPriceFeign.getEngineerPriceByProductIdAndServiceTypeIdAndPriceType(priceType, productId, serviceTypeId));
        return MDUtils.getObjNecessaryConvertType(ProductPrice.class,()->msProductPriceFeign.getEngineerPriceByProductIdAndServiceTypeIdAndPriceType(priceType, productId, serviceTypeId));
    }

    /**
     * 获取所有的产品价格信息
     * @return
     */
    public List<ProductPrice> findAllList() {
        return MDUtils.findAllList(ProductPrice.class, msProductPriceFeign::findAllList);
    }

    /**
     * 获取分组数据
     * @param priceType
     * @param productIds
     * @param servicePointId
     * @param customerId
     * @return
     */
    public List<ProductPrice> findGroupList(Integer priceType, List<Long> productIds, List<Long> serviceTypeIds, Long servicePointId,  Long customerId) {
        MSResponse<List<MDProductPrice>> listMSResponse = msProductPriceFeign.findGroupList(priceType, productIds, serviceTypeIds, servicePointId, customerId);
        List<ProductPrice> productPriceList = Lists.newArrayList();
        if (MSResponse.isSuccess(listMSResponse)) {
            productPriceList = mapper.mapAsList(listMSResponse.getData(), ProductPrice.class);
        }
        return productPriceList;
    }

    /**
     * 获取价格的分组信息
     * @param priceType
     * @param productIds
     * @return
     */
    public List<ProductPrice> findAllGroupList(Integer priceType, List<Integer> productIds) {
        MSResponse<List<MDProductPrice>> listMSResponse = msProductPriceFeign.findAllGroupList(priceType, productIds);
        List<ProductPrice> productPriceList = Lists.newArrayList();
        if (MSResponse.isSuccess(listMSResponse)) {
            productPriceList = mapper.mapAsList(listMSResponse.getData(), ProductPrice.class);
        }
        return productPriceList;
    }

    public List<ProductPrice> findAllPriceList(Integer priceType, List<Integer> productIds) {
        MSResponse<List<MDProductPrice>> listMSResponse = msProductPriceFeign.findAllPriceList(priceType, productIds);
        List<ProductPrice> productPriceList = Lists.newArrayList();
        if (MSResponse.isSuccess(listMSResponse)) {
            productPriceList = mapper.mapAsList(listMSResponse.getData(), ProductPrice.class);
        }
        return productPriceList;
    }

    /**
     * 获取产品价格数据
     * @param productPricePage
     * @param productPrice
     * @return
     */
    public Page<ProductPrice> findList(Page<ProductPrice> productPricePage, ProductPrice productPrice) {
        return MDUtils.findListForPage(productPricePage, productPrice, ProductPrice.class, MDProductPrice.class, msProductPriceFeign::findList);
    }

    /**
     * 批量插入产品价格数据
     * @param productPriceList
     * @return
     */
    public MSErrorCode batchInsert(List<ProductPrice> productPriceList) {
        return MDUtils.genericBatchSave(productPriceList, MDProductPrice.class, msProductPriceFeign::batchInsert);
    }

    /**
     * 添加/更新
     * @param productPrice
     * @param isNew
     * @return
     */
    public MSErrorCode save(ProductPrice productPrice, boolean isNew) {
        return MDUtils.genericSave(productPrice, MDProductPrice.class, isNew, isNew?msProductPriceFeign::insert:msProductPriceFeign::update);
    }

    /**
     * 删除
     * @param productPrice
     * @return
     */
    public MSErrorCode delete(ProductPrice productPrice) {
        return MDUtils.genericSave(productPrice, MDProductPrice.class, false, msProductPriceFeign::delete);
    }
}
