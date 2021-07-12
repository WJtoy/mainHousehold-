package com.wolfking.jeesite.ms.b2bcenter.sd.mapper;

import cn.hutool.core.util.StrUtil;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderConvertVModel;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Order <-> B2BOrderConvertVModel
 */
@Component
public class OrderToB2BOrderConvertVModelMapper extends CustomMapper<Order, B2BOrderConvertVModel> {

    @Autowired
    private AreaService areaService;

    @Override
    public void mapAtoB(Order a, B2BOrderConvertVModel b, MappingContext context) {
        b.setDataSource(a.getDataSource());
        b.setId(a.getId());
        b.setQuarter(a.getQuarter());
        b.setB2bOrderId(a.getB2bOrderId());
        b.setB2bOrderNo(a.getWorkCardId());
        b.setParentBizOrderId(a.getParentBizOrderId());
        b.setOrderNo(a.getOrderNo());
        OrderCondition condition = a.getOrderCondition();
        b.setCustomer(condition.getCustomer());
        b.setArea(condition.getArea());
        b.setSubArea(condition.getSubArea());  // add on 2019-6-3
        b.setAddress(condition.getAddress());
        b.setServiceAddress(condition.getAddress());
        b.setUserName(condition.getUserName());
        b.setPhone1(condition.getPhone1());
        b.setPhone2(condition.getPhone2());
        b.setServicePhone(condition.getServicePhone());
        b.setFullAddress(condition.getFullAddress());
        b.setRepeateNo(a.getRepeateNo());
        if(a.getOrderLocation()!=null) {
            b.setLongitude(a.getOrderLocation().getLongitude());
            b.setLatitude(a.getOrderLocation().getLatitude());
        }
        b.setOrderServiceType(a.getOrderCondition().getOrderServiceType());
        if (a.getB2bShop() != null) {
            B2BCustomerMapping customerMapping = new B2BCustomerMapping();
            customerMapping.setDataSource(a.getB2bShop().getDataSource());
            customerMapping.setShopId(a.getB2bShop().getShopId());
            customerMapping.setShopName(a.getB2bShop().getShopName());
            customerMapping.setCustomerId(a.getB2bShop().getCustomerId());
            customerMapping.setCustomerName(a.getB2bShop().getCustomerName());
            customerMapping.setSaleChannel(a.getOrderChannel().getIntValue());//2020-05-04
            b.setCustomerMapping(customerMapping);
        }
        b.setDescription(a.getDescription());
        if (a.getOrderAdditionalInfo() != null) {
            b.setEstimatedReceiveDate(a.getOrderAdditionalInfo().getEstimatedReceiveDate());
            b.setBuyDate(a.getOrderAdditionalInfo().getBuyDate());
            b.setExpectServiceTime(a.getOrderAdditionalInfo().getExpectServiceTime());
            b.setSiteCode(StringUtils.toString(a.getOrderAdditionalInfo().getSiteCode()));
            b.setSiteName(StringUtils.toString(a.getOrderAdditionalInfo().getSiteName()));
            b.setEngineerName(StringUtils.toString(a.getOrderAdditionalInfo().getEngineerName()));
            b.setEngineerMobile(StringUtils.toString(a.getOrderAdditionalInfo().getEngineerMobile()));
            b.setOrderDataSource(StrUtil.trimToEmpty(a.getOrderAdditionalInfo().getOrderDataSource()));
        }

        int qty = 0;
        double charge = 0.0;
        double blockCharge = 0.0;
        if (a.getItems() != null && a.getItems().size() > 0) {
            OrderItemModel model;
            int idx =0;
            for (OrderItem item : a.getItems()) {
                if(idx==0){
                    b.setCategory(item.getProduct().getCategory());
                }
                model = super.mapperFacade.map(item, OrderItemModel.class);
                qty = qty + model.getQty();
                charge = charge + model.getCharge();
                blockCharge = blockCharge + model.getBlockedCharge();
                b.getItems().add(model);
                idx++;
            }
        }else{
            b.setCategory(new ProductCategory(0L));
        }

        b.setOrderPaymentType(a.getOrderFee().getOrderPaymentType());
        b.setTotalQty(qty);
        b.setExpectCharge(charge + blockCharge);
        b.setBlockedCharge(blockCharge);
        b.setCreateBy(a.getCreateBy());
        b.setCreateDate(a.getCreateDate());
        b.setCreateDt(a.getCreateDate() == null ? 0 : a.getCreateDate().getTime());
        b.setCustomerOwner(condition.getCustomerOwner());
        if (condition.getCreateBy() != null && condition.getCreateBy().getId() != null && condition.getCreateBy().getId() > 0) {
            b.setCreateById(condition.getCreateBy().getId());
        }
    }

    @Override
    public void mapBtoA(B2BOrderConvertVModel b, Order a, MappingContext context) {
        a.setDataSource(b.getDataSource());
        a.setId(b.getId());
        a.setQuarter(b.getQuarter());
        a.setB2bOrderId(b.getB2bOrderId());
        a.setWorkCardId(b.getB2bOrderNo());
        a.setParentBizOrderId(b.getParentBizOrderId());
        a.setOrderNo(b.getOrderNo());
        a.setCreateBy(b.getCreateBy());// 创建者
        a.setCreateDate(b.getCreateDate());// 创建日期

        //地理信息表 2019-04-24
        OrderLocation location = new OrderLocation(b.getId(),b.getQuarter());
        location.setArea(b.getArea());
        location.setLongitude(b.getLongitude());
        location.setLatitude(b.getLatitude());
        a.setOrderLocation(location);
        a.setOrderType(MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_B2B), "order_type"));//切换为微服务
        a.setTotalQty(b.getTotalQty());
        String description = StringEscapeUtils.unescapeHtml4(b.getDescription())
                .replace("\"", "")
                .replace(":", "|")
                .replace("http|", "http:")
                .replace("https|", "https:")
                .replace("\\\\", "")
                .replace("\\", "");
        a.setDescription(description);
        a.setRepeateNo(b.getRepeateNo().trim());
        if (b.getCustomerMapping() != null) {
            B2bCustomerMap b2bShop = new B2bCustomerMap();
            b2bShop.setDataSource(StringUtils.toInteger(b.getCustomerMapping().getDataSource()));
            b2bShop.setShopId(b.getCustomerMapping().getShopId());
            b2bShop.setShopName(b.getCustomerMapping().getShopName());
            b2bShop.setCustomerId(b.getCustomerMapping().getCustomerId());
            b2bShop.setCustomerName(b.getCustomerMapping().getCustomerName());
            a.setB2bShop(b2bShop);
            a.setOrderChannel(new Dict(b.getCustomerMapping().getSaleChannel(),""));
        }else{
            a.setOrderChannel(new Dict(1,"线下"));
        }
        a.setDescription(b.getDescription());

        //Status
        OrderStatus ostatus = new OrderStatus();
        ostatus.setQuarter(b.getQuarter());
        ostatus.setOrderId(b.getId());
        a.setOrderStatus(ostatus);

        //Condition
        OrderCondition condition = new OrderCondition();
        condition.setQuarter(b.getQuarter());
        condition.setOrderId(b.getId());
        condition.setQuarter(b.getQuarter());
        condition.setOrderNo(b.getOrderNo());
        condition.setOrderServiceType(b.getOrderServiceType());
        condition.setUserName(b.getUserName());
        condition.setPhone1(b.getPhone1());
        condition.setServicePhone(StringUtils.isBlank(b.getPhone1()) ? b.getPhone2() : b.getPhone1());
        condition.setPhone2(b.getPhone2().trim());
        //condition.setAddress(StringEscapeUtils.unescapeHtml4(b.getAddress().replace("null", "")).replace("\"", "").replace(":", "|"));//详细地址
        condition.setAddress(StringUtils.filterAddress(b.getAddress()));//详细地址
        condition.setServiceAddress(condition.getAddress());
        condition.setFullAddress(b.getFullAddress());
        condition.setDelFlag(0);
        condition.setCreateDate(b.getCreateDate());
        condition.setCreateBy(b.getCreateBy());

        condition.setTotalQty(b.getTotalQty());
        condition.setArea(b.getArea());
        condition.setSubArea(b.getSubArea());  // add  on 2019-6-3
        /* 省/市id 2019-09-25 */
        Map<Integer, Area> areas = areaService.getAllParentsWithDistrict(b.getArea().getId());
        Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
        Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
        condition.setProvinceId(province.getId());
        condition.setCityId(city.getId());
        condition.setStatus(MSDictUtils.getDictByValue(String.valueOf(b.getStatus()), "order_status"));//切换为微服务
        condition.setKefu(b.getKefu());
        condition.setCustomer(b.getCustomer());
        condition.setCustomerOwner(b.getCustomerOwner());

        a.setOrderCondition(condition);

        //fee
        OrderFee fee = new OrderFee();
        fee.setOrderId(b.getId());
        fee.setExpectCharge(b.getExpectCharge());
        fee.setBlockedCharge(b.getBlockedCharge());
        fee.setOrderPaymentType(b.getOrderPaymentType());
        fee.setQuarter(b.getQuarter());

        // 安维
        fee.setEngineerPaymentType(new Dict("0", ""));
        a.setOrderFee(fee);
        OrderAdditionalInfo orderAdditionalInfo = new OrderAdditionalInfo();
        orderAdditionalInfo.setEstimatedReceiveDate(StringUtils.toString(b.getEstimatedReceiveDate()));
        orderAdditionalInfo.setBuyDate(b.getBuyDate() == null ? 0 : b.getBuyDate());
        orderAdditionalInfo.setExpectServiceTime(StringUtils.toString(b.getExpectServiceTime()));
        orderAdditionalInfo.setSiteCode(StringUtils.toString(b.getSiteCode()));
        orderAdditionalInfo.setSiteName(StringUtils.toString(b.getSiteName()));
        orderAdditionalInfo.setEngineerName(StringUtils.toString(b.getEngineerName()));
        orderAdditionalInfo.setEngineerMobile(StringUtils.toString(b.getEngineerMobile()));
        orderAdditionalInfo.setOrderDataSource(StrUtil.trimToEmpty(b.getOrderDataSource()));
        a.setOrderAdditionalInfo(orderAdditionalInfo);

        //items
        Long categoryId = null;
        for (OrderItemModel item : b.getItems()) {
            if (item.getFlag().equalsIgnoreCase("del") || item.getProduct() == null
                    || item.getServiceType() == null ) {
                continue;
            }
            OrderItem m = (OrderItem) item;
            m.setQuarter(b.getQuarter());
            m.setOrderId(b.getId());
            if(categoryId == null){
                categoryId = m.getProduct().getCategory().getId();
                condition.setProductCategoryId(categoryId);
            }
            a.getItems().add(m);
        }
        if (b.getCreateById() != null && b.getCreateById() > 0) {
            User createBy = MSUserUtils.get(b.getCreateById());
            if (createBy != null && createBy.getId() != null && createBy.getId() > 0) {
                a.setCreateBy(createBy);
                a.getOrderCondition().setCreateBy(createBy);
            }
        }

    }
}
