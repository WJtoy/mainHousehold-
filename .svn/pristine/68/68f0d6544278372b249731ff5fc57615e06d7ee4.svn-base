/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.BrandService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.ThreeTuple;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CustomerProductVM;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BProductMappingService;
import com.wolfking.jeesite.ms.providermd.service.CustomerBrandCategoryService;
import com.wolfking.jeesite.ms.providermd.service.ProductModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 编辑工单Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderEditFormService extends LongIDBaseService {

    @Autowired
    private CustomerBrandCategoryService msCustomerBrandService;

    @Autowired
    private B2BProductMappingService b2BProductMappingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private BrandService brandService;

    /**
     * 获取客户产品及对应的服务类型
     * 2019-12-22
     * 增加筛选，客户设定产品中未设定的产品价格不包含在内
     */
    public List<CustomerProductVM> getCustomerProducts(Long customerId) {
        List<CustomerProductVM> vmList = Lists.newArrayList();
        if (customerId != null && customerId > 0) {
            //以客户现有产品为准
            List<Product> productList = productService.getCustomerProductList(customerId);
            if(CollectionUtils.isEmpty(productList)){
                return vmList;
            }
            Set<Long> productIdSet = productList.stream().map(t->t.getId()).collect(Collectors.toSet());

            List<CustomerPrice> prices = customerService.getPricesFromCache(customerId);
            if (prices != null && !prices.isEmpty()) {
                Map<Product, List<CustomerPrice>> priceMap = prices.stream().collect(Collectors.groupingBy(CustomerPrice::getProduct));
                List<Long> productIds = priceMap.keySet().stream().map(LongIDBaseEntity::getId).collect(Collectors.toList());
                Map<Long, Product> productMap = productService.getProductMap(productIds);
                Map<Long, ServiceType> allServiceTypeMap = serviceTypeService.getAllServiceTypeMap();
                Product product;
                List<ServiceType> serviceTypes;
                ServiceType serviceType;
                CustomerProductVM.CustomerProductVMBuilder builder;
                ProductCategory productCategory;
                for (Map.Entry<Product, List<CustomerPrice>> item : priceMap.entrySet()) {
                    //筛选，必须是客户现在设定的产品
                    if(!productIdSet.contains(item.getKey().getId())){
                        continue;
                    }
                    builder = CustomerProductVM.builder()
                            .id(item.getKey().getId())
                            .name(item.getKey().getName())
                            .brand(item.getKey().getBrand())
                            .model(item.getKey().getModel());
                    //Product & Category
                    product = productMap.get(item.getKey().getId());
                    if (product == null) {
                        return Lists.newArrayList();
                    }
                    productCategory = product.getCategory();
                    builder.sort(product.getSort());
                    builder.categoryId(productCategory.getId());
                    builder.categoryName(productCategory.getName());
                    //service Type
                    serviceTypes = Lists.newArrayList();
                    for (CustomerPrice price : item.getValue()) {
                        serviceType = allServiceTypeMap.get(price.getServiceType().getId());
                        if (serviceType != null && serviceType.getOpenForCustomer() == ServiceType.OPEN_FOR_CUSTOMER_YES) {
                            serviceTypes.add(serviceType);
                        }
                    }
                    builder.services(serviceTypes.stream().sorted(Comparator.comparing(ServiceType::getSort)).collect(Collectors.toList()));
                    vmList.add(builder.build());
                }
                if (vmList.size() > 1) {
                    vmList = vmList.stream().sorted(Comparator.comparing(CustomerProductVM::getSort)).collect(Collectors.toList());
                }
            }
        }
        return vmList;
    }

    /**
     * 获取客户产品对应的品牌（包括客户的默认品牌）
     */
    public List<String> getBrandNames(Long customerId, Long productId) {
        //Customer customer = customerService.getFromCache(customerId);//不需要订单其他信息，没必要读取缓存
        Customer customer = new Customer(customerId);
        return getBrandNames(customer, productId);
    }

    /**
     * 获取客户产品对应的品牌（包括客户的默认品牌）
    public List<Brand> getBrands(Customer customer, Long productId) {
        List<Brand> brands = Lists.newArrayList();
        List<Brand> prodBrands = null;
        //客户的产品所对应的品牌
        if (customer != null && customer.getId() != null && customer.getId() > 0 && productId != null) {
            prodBrands = msCustomerBrandService.getBranList(customer.getId(), productId);
            if(prodBrands == null){
                prodBrands = Lists.newArrayList();
            }
            if(!prodBrands.isEmpty()) {
                brands.addAll(prodBrands);
            }
        }
        //添加客户的默认品牌
        if (customer != null && StringUtils.isNotBlank(customer.getDefaultBrand())) {
            List<Brand> allBrands = brandService.findAllList();
            Map<String,Brand> allBrandMaps = allBrands.stream().collect(Collectors.toMap(Brand::getName, item -> item));;

            Map<String,Brand> prodBrandMaps;
            if(prodBrands.isEmpty()){
                prodBrandMaps = Maps.newHashMap();
            }else{
                prodBrandMaps = prodBrands.stream().collect(Collectors.toMap(Brand::getName, item -> item));
            }
            List<Brand> temp = Lists.newArrayList();
            Arrays.stream(customer.getDefaultBrand().replace("，", ",").split(","))
                    .forEach(i -> {
                        if (StringUtils.isNoneBlank(i)) {
                            //根据品牌名找品牌
                            if(!prodBrandMaps.containsKey(i.trim())) {
                                if(allBrandMaps.containsKey(i.trim())) {
                                    temp.add(allBrandMaps.get(i.trim()));
                                    //temp.add(i.trim());
                                }
                            }
                        }
                    });
            if(!temp.isEmpty()) {
                brands.addAll(temp);
            }
        }
        if (brands.size() > 1) {
            brands = brands.stream().distinct().collect(Collectors.toList());
        }
        return brands;
    }*/

    /**
     * 获取客户产品对应的品牌
     */
    public List<Brand> getBrands(Customer customer, Long productId) {
        List<Brand> brands = Lists.newArrayList();
        //客户的产品所对应的品牌
        if (customer != null && customer.getId() != null && customer.getId() > 0 && productId != null) {
            brands = msCustomerBrandService.getBranList(customer.getId(), productId);
            if(brands == null){
                brands = Lists.newArrayList();
            }
        }
        //sort
        if(!brands.isEmpty()){
            brands = brands.stream().sorted(Comparator.comparing(Brand::getName)).sorted(Comparator.comparing(Brand::getSort)).collect(Collectors.toList());
        }
        return brands;
    }


    /**
     * 获取客户产品对应的品牌
     * x（包括客户的默认品牌）废弃客户默认品牌
     */
    public List<String> getBrandNames(Customer customer, Long productId) {
        List<String> brandNames = Lists.newArrayList();
        //客户的产品所对应的品牌
        if (customer != null && customer.getId() != null && customer.getId() > 0 && productId != null) {
            List<String> temp = msCustomerBrandService.getBrandNames(customer.getId(), productId);
            brandNames.addAll(temp);
        }
        /*添加客户的默认品牌
        if (customer != null && StringUtils.isNotBlank(customer.getDefaultBrand())) {
            List<String> temp = Lists.newArrayList();
            Arrays.stream(customer.getDefaultBrand().replace("，", ",").split(","))
                    .forEach(i -> {
                        if (StringUtils.isNoneBlank(i)) {
                            temp.add(i.trim());
                        }
                    });
            brandNames.addAll(temp);
        }
        if (brandNames.size() > 1) {
            brandNames = brandNames.stream().distinct().collect(Collectors.toList());
        }*/
        if(!brandNames.isEmpty()){
            return brandNames.stream().sorted().collect(Collectors.toList());
        }
        return brandNames;
    }

    /**
     * 获取客户产品对应的B2B产品编码
     */
    public List<String> getB2BProductCodes(Integer dataSourceId, Long productId) {
        List<String> b2bProductCodes = Lists.newArrayList();
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId) && productId != null && productId > 0) {
            b2bProductCodes = b2BProductMappingService.getB2BProductCodesByProductId(dataSourceId, productId);
        }
        if(b2bProductCodes!=null && !b2bProductCodes.isEmpty()){
            return b2bProductCodes.stream().distinct().collect(Collectors.toList());
        }else {
            return b2bProductCodes;
        }
    }

    /**
     * 获取客户产品对应的型号
     */
    public List<String> getProductSpecs(Long customerId, Long productId) {
        List<String> productSpecs = Lists.newArrayList();
        if (customerId != null && customerId > 0 && productId != null && productId > 0) {
            productSpecs = productModelService.getModelNamesFromCache(customerId, productId);
        }
        return productSpecs;
    }

    /**
     * 获取客户产品对应的型号
     */
    public List<CustomerProductModel> getProductSpecModels(Long customerId, Long productId) {
        List<CustomerProductModel> productSpecs = Lists.newArrayList();
        if (customerId != null && customerId > 0 && productId != null && productId > 0) {
            productSpecs = productModelService.getModelListFromCache(customerId, productId);
        }
        return productSpecs;
    }

    /**
     * 根据客户、数据源、产品来查询产品对应的品牌、型号、B2B产品编码
     * @return <产品ID, ThreeTuple<品牌列表, 型号列表, B2B产品编码列表>>
    */
    public Map<Long, ThreeTuple<List<String>, List<String>, List<String>>> getProductProperties(Long customerId, Integer dataSourceId, List<Long> productIds) {
        Map<Long, ThreeTuple<List<String>, List<String>, List<String>>> map = Maps.newHashMap();
        if (customerId != null && customerId > 0 && productIds != null && !productIds.isEmpty()) {
            Map<Long, List<String>> brandMap = Maps.newHashMap();
            Map<Long, List<String>> productSpecMap = Maps.newHashMap();
            Map<Long, List<String>> b2bProductCodeMap = Maps.newHashMap();
            List<String> brands;
            List<String> productSpecs;
            List<String> b2bProductCodes;

            for (Long productId : productIds) {
                if (brandMap.containsKey(productId)) {
                    brands = brandMap.get(productId);
                } else {
                    brands = getBrandNames(customerId, productId);
                    brandMap.put(productId, brands);
                }
                if (productSpecMap.containsKey(productId)) {
                    productSpecs = productSpecMap.get(productId);
                } else {
                    productSpecs = getProductSpecs(customerId, productId);
                    productSpecMap.put(productId, productSpecs);
                }
                if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
                    if (b2bProductCodeMap.containsKey(productId)) {
                        b2bProductCodes = b2bProductCodeMap.get(productId);
                    } else {
                        b2bProductCodes = getB2BProductCodes(dataSourceId, productId);
                        b2bProductCodeMap.put(productId, b2bProductCodes);
                    }
                } else {
                    b2bProductCodes = Lists.newArrayList();
                }
                map.put(productId, new ThreeTuple<>(brands, productSpecs, b2bProductCodes));
            }
        }
        return map;
    }

    /**
     * 根据客户、数据源、产品来查询产品对应的品牌、型号、B2B产品编码
     * @return <产品ID, ThreeTuple<品牌列表, 型号列表, B2B产品编码列表>>
     */
    public Map<Long, ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>>> getProductPropertyEntris(Long customerId, Integer dataSourceId, List<Long> productIds) {
        Map<Long, ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>>> map = Maps.newHashMap();
        if (customerId != null && customerId > 0 && productIds != null && !productIds.isEmpty()) {
            Map<Long, List<Brand>> brandMap = Maps.newHashMap();
            Map<Long, List<CustomerProductModel>> productSpecMap = Maps.newHashMap();
            Map<Long, List<String>> b2bProductCodeMap = Maps.newHashMap();
            List<Brand> brands;
            List<CustomerProductModel> productSpecs;
            List<String> b2bProductCodes;

            for (Long productId : productIds) {
                if (brandMap.containsKey(productId)) {
                    brands = brandMap.get(productId);
                } else {
                    Customer customer = new Customer(customerId);
                    brands = getBrands(customer, productId);
                    brandMap.put(productId, brands);
                }
                if (productSpecMap.containsKey(productId)) {
                    productSpecs = productSpecMap.get(productId);
                } else {
                    productSpecs = getProductSpecModels(customerId, productId);
                    productSpecMap.put(productId, productSpecs);
                }
                if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
                    if (b2bProductCodeMap.containsKey(productId)) {
                        b2bProductCodes = b2bProductCodeMap.get(productId);
                    } else {
                        b2bProductCodes = getB2BProductCodes(dataSourceId, productId);
                        b2bProductCodeMap.put(productId, b2bProductCodes);
                    }
                } else {
                    b2bProductCodes = Lists.newArrayList();
                }
                map.put(productId, new ThreeTuple<>(brands, productSpecs, b2bProductCodes));
            }
        }
        return map;
    }

}
