package com.wolfking.jeesite.modules.sd.entity.mapper;

import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.B2BOrderTransferModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.apache.commons.lang3.StringEscapeUtils;
import com.kkl.kklplus.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order <-> TempOrder
 */
@Component
public class OrderToB2BOrderModelMapper extends CustomMapper<Order, B2BOrderTransferModel> {

    @Override
    public void mapAtoB(Order a, B2BOrderTransferModel b, MappingContext context) {
        b.setDataSource(a.getDataSource());
        b.setId(a.getId());
        b.setQuarter(a.getQuarter());
        b.setWorkcardId(a.getWorkCardId());
        b.setOrderNo(a.getOrderNo());
        OrderCondition condition = a.getOrderCondition();
        b.setCustomer(condition.getCustomer());
        b.setArea(condition.getArea());
        b.setAddress(condition.getAddress());
        b.setServiceAddress(condition.getAddress());
        b.setUserName(condition.getUserName());
        b.setPhone1(condition.getPhone1());
        b.setPhone2(condition.getPhone2());
        b.setServicePhone(condition.getServicePhone());
        b.setFullAddress(condition.getFullAddress());
        b.setRepeateNo(a.getRepeateNo());
        b.setB2bShop(a.getB2bShop());
        b.setDescription(a.getDescription());

        //List<CustomerPrice> prices = customerService.getPricesFromCache(order.getCustomer().getId());
        //items
        OrderItemModel model;
        List<OrderItem> items = a.getItems();
        int qty = 0;
        double charge = 0.0;
        double blockCharge = 0.0;
        OrderItem item;
        for(int i=0,size=items.size();i<size;i++){
            //item = items.get(i);
            model = super.mapperFacade.map(items.get(i),OrderItemModel.class);
            //model = (OrderItemModel)items.get(i);
            qty = qty + model.getQty();
            charge = charge + model.getCharge();
            blockCharge = blockCharge + model.getBlockedCharge();
            b.getItems().add(model);
        }
        b.setOrderPaymentType(a.getOrderFee().getOrderPaymentType());
        b.setTotalQty(qty);
        b.setExpectCharge(charge + blockCharge);
        b.setBlockedCharge(blockCharge);
        b.setCreateBy(a.getCreateBy());
        b.setCreateDate(a.getCreateDate());
    }

    @Override
    public void mapBtoA(B2BOrderTransferModel b, Order a, MappingContext context) {
        a.setDataSource(b.getDataSource());
        a.setId(b.getId());
        a.setQuarter(b.getQuarter());
        a.setWorkCardId(b.getWorkcardId());
        a.setOrderNo(b.getOrderNo());
        a.setCreateBy(b.getCreateBy());// 创建者
        a.setCreateDate(b.getCreateDate());// 创建日期

        a.setOrderType(MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_B2B),"order_type"));//切换为微服务
        a.setTotalQty(b.getTotalQty());
        String description = StringEscapeUtils.unescapeHtml4(b.getDescription())
                .replace("\"","")
                .replace(":","|")
                .replace("http|","http:")
                .replace("https|","https:")
                .replace("\\\\","")
                .replace("\\","");
        a.setDescription(description);
        a.setRepeateNo(b.getRepeateNo().trim());
        a.setB2bShop(b.getB2bShop());
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

        condition.setUserName(b.getUserName());
        condition.setPhone1(b.getPhone1());
        condition.setServicePhone(StringUtils.isBlank(b.getPhone1())?b.getPhone2():b.getPhone1());
        condition.setPhone2(b.getPhone2().trim());
        //condition.setAddress(StringEscapeUtils.unescapeHtml4(b.getAddress().replace("null","")).replace("\"","").replace(":","|"));//详细地址
        condition.setAddress(StringUtils.filterAddress(b.getAddress()));//详细地址
        condition.setServiceAddress(condition.getAddress());
        condition.setFullAddress(b.getFullAddress());
        condition.setDelFlag(0);
        condition.setCreateDate(b.getCreateDate());
        condition.setCreateBy(b.getCreateBy());

        condition.setTotalQty(b.getTotalQty());
        condition.setArea(b.getArea());
        condition.setStatus(MSDictUtils.getDictByValue(String.valueOf(b.getStatus()),"order_status"));//切换为微服务
        condition.setKefu(b.getKefu());
        condition.setCustomer(b.getCustomer());

        a.setOrderCondition(condition);

        //fee
        OrderFee fee = new OrderFee();
        fee.setOrderId(b.getId());
        fee.setExpectCharge(b.getExpectCharge());
        fee.setBlockedCharge(b.getBlockedCharge());
        fee.setOrderPaymentType(b.getOrderPaymentType());
        fee.setQuarter(b.getQuarter());

        // 安维
        fee.setEngineerPaymentType(new Dict("0",""));
        a.setOrderFee(fee);
        //items
        for(OrderItemModel item:b.getItems()){
            if(!item.getFlag().equalsIgnoreCase("del")) {
                OrderItem m = (OrderItem)item;
                m.setQuarter(b.getQuarter());
                m.setOrderId(b.getId());
                a.getItems().add(m);
            }
        }

    }
}
