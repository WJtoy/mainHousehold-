package com.wolfking.jeesite.modules.md.entity;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * 安维价格视图模型
 */
public class ServicePriceVM implements Serializable {

    public ServicePriceVM(){}

    private ServicePoint servicePoint;
    private List<ServicePrice> priceList = Lists.newArrayList();

    private ServicePrice servicePrice;//传递查询条件

    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public List<ServicePrice> getPriceList() {
        return priceList;
    }


    public ServicePrice getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(ServicePrice servicePrice) {
        this.servicePrice = servicePrice;
    }
}
