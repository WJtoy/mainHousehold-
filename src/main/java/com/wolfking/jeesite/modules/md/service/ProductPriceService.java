/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.ProductPriceDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductPriceService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 产品价格
 * 
 * @author ThinkGem
 * @version 2013-5-29
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class ProductPriceService extends LongIDCrudService<ProductPriceDao, ProductPrice>
{
	@Autowired
	private MSProductService msProductService;

	@Autowired
	private MSProductPriceService msProductPriceService;

//	@Autowired
//	private ServicePointService servicePointService;
//
//	@Autowired
//	private CustomerService customerService;

	@Autowired
	private ServiceTypeService serviceTypeService;

	@Override
	public ProductPrice get(long id) {
		// add on 2019-8-19
		//ProductPrice productPrice = super.get(id);  // mark on 2019-8-24
		ProductPrice productPrice = msProductPriceService.getById(id);  // add on 2019-8-24 //productPrice微服务
		if (productPrice != null) {
			// 调用product微服务获取
			Product product = msProductService.getById(productPrice.getProduct().getId());
			productPrice.getProduct().setName(product != null?product.getName():"");

			ServiceType serviceType = serviceTypeService.get(productPrice.getServiceType().getId());
			productPrice.getServiceType().setName(Optional.ofNullable(serviceType).map(ServiceType::getName).orElse(""));
			productPrice.getServiceType().setCode(Optional.ofNullable(serviceType).map(ServiceType::getCode).orElse(""));
			productPrice.getServiceType().setWarrantyStatus(Optional.ofNullable(serviceType).map(ServiceType::getWarrantyStatus).orElse(null));
		}
		return productPrice;
	}

	@Override
	public List<ProductPrice> findAllList() {
		//return super.findAllList();
		// add on 2019-8-19
		// 调用product微服务
		//List<ProductPrice> productPriceList = super.findAllList();  // mark on 2019-8-24
		List<ProductPrice> productPriceList = msProductPriceService.findAllList(); // add on 2019-8-24 //ProductPrice微服务
		if (productPriceList != null && !productPriceList.isEmpty()) {
			String productIds = productPriceList.stream().map(r->r.getProduct().getId().toString()).collect(Collectors.joining(","));
			Map<Long, String> finalMap = findProductListByProductIds(productIds);

			//mark on 2019-10-11
			/*List<ServiceType> serviceTypeList = serviceTypeService.findAllList();
			Map<Long, String> serviceTypeMap = serviceTypeList.stream().collect(Collectors.toMap(ServiceType::getId, ServiceType::getName));*/

            //调用微服务获取服务类型 只返回id和名称 start on 2019-10-11
			Map<Long, String> serviceTypeMap = serviceTypeService.findAllIdsAndNames();
            //end
			productPriceList.stream().forEach(productPrice -> {
				productPrice.getProduct().setName(finalMap==null?"":finalMap.get(productPrice.getProduct().getId()));
				productPrice.getServiceType().setName(Optional.ofNullable(serviceTypeMap.get(productPrice.getServiceType().getId())).orElse(""));
			});
		}
		return productPriceList;
	}

	/**
	 * 通过产品id获取产品名Map
	 * @param productIds
	 * @return
	 */
	public Map<Long, String> findProductListByProductIds(String productIds) {
		Map<Long,String> map = null;
		Page<Product> productPage = new Page<>();
		productPage.setPageSize(5000);
		Product product = new Product();
		product.setProductIds(productIds);
		Page<Product> page = msProductService.findList(productPage, product);
		if (page != null && page.getList() != null && !page.getList().isEmpty()) {
			map = page.getList().stream().collect(Collectors.toMap(Product::getId, Product::getName));
		}

		return map;
	}

	/**
	 * 获取某产品某项服务项目的默认价格
	 * 
	 * @param productId
	 * @param serviceTypeId
	 * @return
	 */
	/*
	// mark on 2020-2-13 begin
	// 无处调用，代码废弃
	public ProductPrice getByProductIDAndServiceTypeId(Long productId, Long serviceTypeId)
	{
		return dao.getByProductIDAndServiceTypeId(productId, serviceTypeId);

	}
	// mark on 2020-2-13 end
	 */

	/**
	 * 获取分组可用参考价格列表
	 * @param productIds
	 * @return
	 */
	public List<ProductPrice> findGroupList(List<Long> productIds, List<Long> serviceTypeIds, Integer priceType, Long servicePointId, Long customerId){
		//return dao.findGroupList(productIds, serviceTypeIds, priceType, servicePointId, customerId);  // mark on 2019-8-27
		/*
		// mark on 2019-12-21 begin
		Long customerProductExists = null;
		if (customerId != null) {
			List<CustomerProduct> customerProductList = customerService.getListByCustomer(customerId);
			if (customerProductList != null) {
				customerProductExists = customerProductList.stream().filter(t -> productIds.contains(t.getProduct().getId())).count();
			}
		}

		Long servicePointProductExists = null;
		if (servicePointId != null) {
			List<Integer> servicePointProductIds = servicePointService.getProductIds(servicePointId);
			if (servicePointProductIds != null) {
				servicePointProductExists = servicePointProductIds.stream().filter(t -> servicePointProductIds.contains(t)).count();
			}
		}

		// 微服务已按product_id, service_type_id排序
		List<ProductPrice> productPriceList = msProductPriceService.findGroupList(priceType, productIds, serviceTypeIds, servicePointProductExists, customerProductExists);
		return productPriceList;
		// mark on 2019-12-21 end
		*/

		// add on 2019-12-21 begin
		//List<ProductPrice> productPriceList = dao.findGroupList(productIds, serviceTypeIds, priceType, servicePointId, customerId);
		List<ProductPrice> productPriceListFromMS = msProductPriceService.findGroupList(priceType, productIds, serviceTypeIds, servicePointId, customerId);
		//findGroupListCompare(productPriceList, productPriceListFromMS);
		return productPriceListFromMS;
		// add on 2019-12-21 end
	}

	/**
	 * 获取分组所有参考价格列表
	 * @param productIds
	 * @return
	 */
	public List<ProductPrice> findAllGroupList(List<Long> productIds, List<Long> serviceTypeIds, Integer priceType, Long servicePointId, Long customerId){
		//return dao.findAllGroupList(productIds, serviceTypeIds, priceType, servicePointId, customerId);  // mark on 2019-8-26
		List<ProductPrice> productPriceList = msProductPriceService.findAllGroupList(priceType, productIds.stream().map(r->r.intValue()).collect(Collectors.toList())); // add on 2019-8-26
		return productPriceList;
	}

	public List<ProductPrice> findAllPriceList(List<Long> productIds, List<Long> serviceTypeIds, Integer priceType, Long servicePointId, Long customerId){
		return msProductPriceService.findAllPriceList(priceType, productIds.stream().map(r->r.intValue()).collect(Collectors.toList()));
	}

	/*
	// mark on 2020-2-13 begin
	// 无处调用代码废弃
	public List<ProductPrice> getProductPricesByProduct(Long productId){
		ProductPrice productPrice = new ProductPrice();
		productPrice.setProduct(new Product(productId));
		//切换为微服务
//		List<ProductPrice> productPriceList = dao.findList(productPrice);  // mark on 2019-8-24
		List<ProductPrice> productPriceList = null;						   // add on 2019-8-24
		// add on 2019-8-24 begin
		// productPrice微服务
		Page<ProductPrice> productPricePage = new Page<>();
		productPricePage.setPageSize(1000);
		Page<ProductPrice> page = msProductPriceService.findList(productPricePage, productPrice);
		if (page != null && page.getList() != null && !page.getList().isEmpty()) {
			productPriceList = page.getList();
		}
		// add on 2019-8-24 end

		if (productPriceList != null && productPriceList.size() > 0) {
			// add on 2019-8-19 begin
			String productIds = productPriceList.stream().map(r->r.getProduct().getId().toString()).collect(Collectors.joining(","));
			Map<Long,String> map = findProductListByProductIds(productIds);

			List<ServiceType> serviceTypeList = serviceTypeService.findAllList();
			Map<Long, String> serviceTypeMap = serviceTypeList.stream().collect(Collectors.toMap(ServiceType::getId, ServiceType::getName));
			// add on 2019-8-19 end

			Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
			for(ProductPrice price : productPriceList){
				if (price.getPriceType() != null && Integer.parseInt(price.getPriceType().getValue()) > 0) {
					Dict priceTypeDict = priceTypeMap.get(price.getPriceType().getValue());
					if (priceTypeDict != null){
						price.setPriceType(priceTypeDict);
					}
				}
				price.getProduct().setName(map==null?"":map.get(price.getProduct().getId())); // add on 2019-8-19
				price.getServiceType().setName(Optional.ofNullable(serviceTypeMap.get(price.getServiceType().getId())).orElse(""));  // add on 2019-8-28
			}
		}
		return productPriceList;
	}
	// mark on 2020-2-13 end
	 */

	@Transactional()
	public void save(ProductPrice productPrice) {
//		mark on 2019-8-24
//		Long existProductPriceId = dao.getIdByProductIDAndServiceTypeIdAndPriceType(
//				productPrice.getProduct().getId(),
//				productPrice.getServiceType().getId(),
//				Integer.parseInt(productPrice.getPriceType().getValue()));
		// add on 2019-8-24 //ProductPrice微服务
		Long existProductPriceId = msProductPriceService.getIdByProductIdAndServiceTypeIdAndPriceType(
				productPrice.getProduct().getId(),
				productPrice.getServiceType().getId(),
				Integer.parseInt(productPrice.getPriceType().getValue()));
		if (existProductPriceId != null) {
			productPrice.setId(existProductPriceId);
		}
		boolean isNew = productPrice.getIsNewRecord(); // add on 2019-8-26
		//super.save(productPrice); //mark on 2020-2-13 //web端去md_product_price
		// add on 2019-8-26 begin
		// ProductPrice微服务
		MSErrorCode msErrorCode = msProductPriceService.save(productPrice, isNew);
		if (msErrorCode.getCode() >0) {
			throw new RuntimeException("调用产品价格微服务失败.失败原因:" + msErrorCode.getMsg());
		}
		// add on 2019-8-26 end
	}

	@Transactional()
	public void saveProductPrices(ProductPrices productPrices){
		User user= UserUtils.getUser();
		List<ProductPrice> productPriceList = productPrices.getListProductPrice();
		List<ProductPrice> savedProductPriceList = Lists.newArrayList();
		for (ProductPrice productprice : productPriceList)
		{
			if (productprice.getServiceType() != null && productPrices.getProduct() != null)
			{
				if (productprice.getCustomerStandardPrice() != 0
						|| productprice.getCustomerDiscountPrice() != 0
						|| productprice.getEngineerStandardPrice() != 0
						|| productprice.getEngineerDiscountPrice() != 0){

					productprice.setProduct(productPrices.getProduct());
					productprice.setPriceType(productPrices.getPriceType());
					// mark on 2019-8-24
//					Long existProductPriceId = dao.getIdByProductIDAndServiceTypeIdAndPriceType(
//							productprice.getProduct().getId(),
//							productprice.getServiceType().getId(),
//							Integer.parseInt(productprice.getPriceType().getValue()));
					// add on 2019-8-24  // ProductPrice微服务
					Long existProductPriceId = msProductPriceService.getIdByProductIdAndServiceTypeIdAndPriceType(
							productprice.getProduct().getId(),
							productprice.getServiceType().getId(),
							Integer.parseInt(productprice.getPriceType().getValue()));
					if (existProductPriceId != null) {
						productprice.setId(existProductPriceId);
					}
					//super.save(productprice);  // mark on 2020-2-13 web端去md_product_price
					savedProductPriceList.add(productprice);  // 保存已存入DB后的productPrice // add on 2019-8-28
				}
			}
		}
		// add on 2019-8-28 begin
		// ProductPrice微服务
		if (savedProductPriceList != null && !savedProductPriceList.isEmpty()) {
			MSErrorCode msErrorCode = msProductPriceService.batchInsert(savedProductPriceList);
			if (msErrorCode.getCode() >0) {
				throw new RuntimeException("调用产品价格微服务出错。出错原因:" + msErrorCode.getMsg());
			}
		}
		// add on 2019-8-28 end
	}

	@Override
	@Transactional()
	public void delete(ProductPrice entity) {
		//super.delete(entity);  //mark on 2020-2-13 web端去md_product_price
		MSErrorCode msErrorCode = msProductPriceService.delete(entity);
		if (msErrorCode.getCode() >0) {
			throw new RuntimeException("调用产品价格微服务出错,出错原因:" + msErrorCode.getMsg());
		}
	}

	//	public List<ProductPrice> findProductPricelist(Product p)
//	{
//		DetachedCriteria dc = productPriceDao.createDetachedCriteria();
//		dc.add(Restrictions.ne(EngineerPrice.FIELD_DEL_FLAG,
//				EngineerPrice.DEL_FLAG_DELETE));
//		if (p != null
//				&& StringUtils.isNotBlank(p.getId()))
//		{
//			dc.add(Restrictions.eq("product.id", p.getId()));
//		}
//		dc.createAlias("serviceType", "serviceType");
//
//		dc.addOrder(Order.asc("serviceType.sort"));
//
//		return productPriceDao.find(dc);
//	}
}
