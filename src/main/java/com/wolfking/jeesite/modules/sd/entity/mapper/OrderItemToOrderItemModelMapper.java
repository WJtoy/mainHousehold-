package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

/**
 * OrderDetail <-> OrderItem
 */
@Component
public class OrderItemToOrderItemModelMapper extends CustomMapper<OrderItem, OrderItemModel>{

    @Override
    public void mapAtoB(OrderItem a, OrderItemModel b, MappingContext context) {
        b.setId(a.getId());
        b.setOrderId(a.getOrderId());
        b.setQuarter(a.getQuarter());
        b.setProduct(a.getProduct());
        b.setB2bProductCode(a.getB2bProductCode()); /* orderItem增加B2B产品编码 */
        b.setServiceType(a.getServiceType());
        b.setProductSpec(a.getProductSpec()==null?"":a.getProductSpec());
        b.setItemNo(a.getItemNo());
        b.setBrand(a.getBrand()==null?"":a.getBrand());
        b.setStandPrice(a.getStandPrice());
        b.setDiscountPrice(a.getDiscountPrice());
        b.setQty(a.getQty());
        b.setCharge(a.getCharge());
        b.setBlockedCharge(a.getBlockedCharge());
        b.setExpressCompany(a.getExpressCompany());
        b.setExpressNo(a.getExpressNo()==null?"":a.getExpressNo());
        b.setCreateBy(a.getCreateBy());
        b.setCreateDate(a.getCreateDate());
        b.setUpdateBy(a.getUpdateBy());
        b.setUpdateDate(a.getUpdateDate());
        b.setDelFlag(a.getDelFlag());

    }

    @Override
    public void mapBtoA(OrderItemModel b, OrderItem a, MappingContext context) {
        a.setId(b.getId());
        a.setOrderId(b.getOrderId());
        a.setQuarter(b.getQuarter());
        a.setProduct(b.getProduct());
        a.setB2bProductCode(b.getB2bProductCode()); /* orderItem增加B2B产品编码 */
        a.setServiceType(b.getServiceType());
        a.setProductSpec(b.getProductSpec()==null?"":b.getProductSpec());
        a.setItemNo(b.getItemNo());
        a.setBrand(b.getBrand()==null?"":b.getBrand());
        a.setStandPrice(b.getStandPrice());
        a.setDiscountPrice(b.getDiscountPrice());
        a.setQty(b.getQty());
        a.setCharge(b.getCharge());
        a.setBlockedCharge(b.getBlockedCharge());
        a.setExpressCompany(b.getExpressCompany());
        a.setExpressNo(b.getExpressNo()==null?"":b.getExpressNo());
        a.setCreateBy(b.getCreateBy());
        a.setCreateDate(b.getCreateDate());
        a.setUpdateBy(b.getUpdateBy());
        a.setUpdateDate(b.getUpdateDate());
        a.setDelFlag(b.getDelFlag());
    }
}
