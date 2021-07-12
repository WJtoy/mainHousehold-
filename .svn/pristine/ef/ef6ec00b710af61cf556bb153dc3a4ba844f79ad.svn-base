package com.wolfking.jeesite.modules.md.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;

import java.util.List;

/**
 * 自定義類
 * 
 * @author Kody Huang
 * @version 2017-07-07
 */

public class ProductServiceType extends LongIDDataEntity<ProductServiceType>
{

	private ProductCategory productCategory;
	private Product product;
	private ServiceType	serviceType;
	private String serviceTypeIds = "";
	private int count; //saveorupdate时用到

	@JsonIgnore
	private Long productId;

	public ProductServiceType()
	{
	}

	public ProductServiceType(Long id, Product product,ProductCategory productCategory,ServiceType serviceType)
	{
		this.id = id;
		this.product = product;
		this.productCategory = productCategory;
		this.serviceType=serviceType;
	}


	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public Product getProduct() {

		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public ProductCategory getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(ProductCategory productCategory) {
		this.productCategory = productCategory;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getServiceTypeIds() {
		return serviceTypeIds;
	}

	public void setServiceTypeIds(String serviceTypeIds) {
		this.serviceTypeIds = serviceTypeIds;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}