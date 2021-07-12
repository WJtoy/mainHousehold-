package com.wolfking.jeesite.modules.md.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sys.entity.User;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by yanshenglu on 2017/5/3.
 */
public class CustomerPrices implements Serializable {

    public CustomerPrices(){}

    private Customer customer = new Customer();
    private Product product;
    private List<CustomerPrice> prices = Lists.newArrayList();
    private List<CustomerPrice> newPrices = Lists.newArrayList();
    private boolean reload = true;
    private User createBy;
    private Date createDate;

    public List<CustomerPrice> getNewPrices() {
        return newPrices;
    }

    public void setNewPrices(List<CustomerPrice> newPrices) {
        this.newPrices = newPrices;
    }

    @NotNull(message = "客户不能为空")
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @NotNull(message = "价格不能为空")
    public List<CustomerPrice> getPrices() {
        return prices;
    }

    public void setPrices(List<CustomerPrice> prices) {
        this.prices = prices;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
