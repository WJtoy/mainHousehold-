package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDServicePointProduct;
import com.kkl.kklplus.entity.md.dto.MDServicePointProductDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ServicePointProduct;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointProductFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MSServicePointProductService {
    @Autowired
    private MSServicePointProductFeign msServicePointProductFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 读取网点与产品分类的产品列表 (用来替换ServicePointDao.getServicePointProductsByIds方法)
     * @param mdServicePointProductDto
     * @return
     */
    public List<ServicePointProduct> findList(MDServicePointProductDto mdServicePointProductDto) {
        List<ServicePointProduct> servicePointProductList = Lists.newArrayList();

        Page<MDServicePointProductDto> servicePointProductPage = new Page<>();
        servicePointProductPage.setPageSize(mdServicePointProductDto.getPage().getPageSize());
        servicePointProductPage.setPageNo(mdServicePointProductDto.getPage().getPageNo());

        Page<MDServicePointProductDto> returnPage = MDUtils.findMDEntityListForPage(servicePointProductPage, mdServicePointProductDto, msServicePointProductFeign::findList);
        if (!ObjectUtils.isEmpty(returnPage.getList())) {
            servicePointProductList.addAll(mapper.mapAsList(returnPage.getList(), ServicePointProduct.class));
            mdServicePointProductDto.getPage().setPageSize(returnPage.getPageSize());
            mdServicePointProductDto.getPage().setPageNo(returnPage.getPageNo());
            mdServicePointProductDto.getPage().setRowCount(returnPage.getCount());
        }

        return servicePointProductList;
    }

    /**
     * 通过网点id查询对应的产品id列表
     * @param mdServicePointProduct
     * @return
     */
    public List<Long> findProductIds(MDServicePointProduct mdServicePointProduct) {
        List<Long> productIds = Lists.newArrayList();

        int pageNo = 1;
        Page<MDServicePointProduct> servicePointProductPage = new Page<>();
        servicePointProductPage.setPageSize(500);
        servicePointProductPage.setPageNo(pageNo);

        List<MDServicePointProduct> mdServicePointProductList = Lists.newArrayList();
        Page<MDServicePointProduct> returnPage = MDUtils.findMDEntityListForPage(servicePointProductPage, mdServicePointProduct, msServicePointProductFeign::findProductIds);
        mdServicePointProductList.addAll(returnPage.getList());

        while(pageNo < returnPage.getPageCount()) {
            pageNo++;
            servicePointProductPage.setPageNo(pageNo);
            Page<MDServicePointProduct> whileReturnPage = MDUtils.findMDEntityListForPage(servicePointProductPage, mdServicePointProduct, msServicePointProductFeign::findProductIds);
            mdServicePointProductList.addAll(whileReturnPage.getList());
        }
        if (!ObjectUtils.isEmpty(mdServicePointProductList)) {
            productIds = mdServicePointProductList.stream().map(MDServicePointProduct::getProductId).collect(Collectors.toList());
        }

        return productIds;
    }

    /**
     * 给网点配置产品
     *
     * @param products
     * @param servicePointId
     * @return
     */
    public void assignProducts(List<Long> products, Long servicePointId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointProductFeign.assignProducts(products, servicePointId));
        if (msErrorCode.getCode() >0 ) {
            throw new RuntimeException("调用微服务保存网点产品失败.失败原因:" + msErrorCode.getMsg());
        }
    }


    /**
     * 移除网点负责的产品id
     *
     * @param servicePointId
     * @return
     */
    public void removeProducts(Long servicePointId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msServicePointProductFeign.removeProducts(servicePointId));
        if (msErrorCode.getCode() >0 ) {
            throw new RuntimeException("调用微服务删除网点产品失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 根据网点id，判读产品id列表是否都存在于网点产品中
     *
     * @param servicePointId 网点id
     * @param productIds     产品id列表
     * @return
     *     false-不能满足都存在条件,true-都存在
     */
    public boolean existProductsForSD(Long servicePointId, List<Long> productIds) {
        Integer iAllExists = MDUtils.getObjUnnecessaryConvertType(()->msServicePointProductFeign.existProductsForSD(servicePointId, productIds));
        if (iAllExists != null && iAllExists.intValue() == 1) {
            return true;
        }
        return false;
    }
}
