package com.wolfking.jeesite.modules.md.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.sys.entity.User;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by yanshenglu on 2017/5/3.
 */
public class ServicePrices implements Serializable {

    public ServicePrices(){}

    private ServicePoint servicePoint = new ServicePoint();
    private Product product;
    private List<ServicePrice> prices = Lists.newArrayList();
    private User createBy;
    private Date createDate;

    @NotNull(message = "网点不能为空")
    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    @NotNull(message = "价格不能为空")
    public List<ServicePrice> getPrices() {
        return prices;
    }

    public void setPrices(List<ServicePrice> prices) {
        this.prices = prices;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getCreateBy() {
        return createBy;
    }

    public void setCreateBy(User createBy) {
        this.createBy = createBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
