package com.wolfking.jeesite.modules.md.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.sys.entity.Dict;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * 安维店服务价格表(弃用，旧版本)
 * Created on 2017-04-16.
 */
public class EngineerPrice extends LongIDDataEntity<EngineerPrice> {
    private ServicePoint servicePoint; //安维网点
    private Product product;     //产品
    private ServiceType serviceType;  //服务类型
    private double price = 0;   //标准价
    private double discountPrice = 0;//折扣价
    private Dict unit = new Dict("CNY","人民币");  //价格单位

    @NotNull(message = "安维网点不能为空")
    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    @NotNull(message = "产品不能为空")
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @NotNull(message = "服务类型不能为空")
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    @NotNull(message = "币别单位不能为空")
    public Dict getUnit() {
        return unit;
    }

    public void setUnit(Dict unit) {
        this.unit = unit;
    }

    @DecimalMin(value = "0.0",message = "标准价不能小于0")
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @DecimalMin(value = "0.0",message = "折扣价不能小于0")
    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

}
