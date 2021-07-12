package com.wolfking.jeesite.modules.mq.entity.mapper;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.mq.dto.MQCommon;
import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.mq.dto.MQOrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderGradeItemMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderProcessLogMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 导入订单消息与模型之间相互转换
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrderImportMessageMapper {

    @Mappings({
            @Mapping(target = "id",source = "id"),
            @Mapping(target = "customer.id",source = "customerId"),
            @Mapping(target = "customer.name",source = "customerName"),
            @Mapping(target = "repeateOrderNo",source = "repeateOrderNo"),
            @Mapping(target = "userName",source = "userName"),
            @Mapping(target = "phone",source = "phone"),
            @Mapping(target = "tel",source = "tel"),
            @Mapping(target = "address",source = "address"),
            @Mapping(target = "product.id",source = "productId"),
            @Mapping(target = "product.name",source = "productName"),
            @Mapping(target = "brand",source = "brand"),
            @Mapping(target = "productSpec",source = "productSpec"),
            @Mapping(target = "description",source = "description"),
            @Mapping(target = "serviceType.id",source = "serviceTypeId"),
            @Mapping(target = "serviceType.name",source = "serviceTypeName"),
            @Mapping(target = "qty",source = "qty"),
            @Mapping(target = "errorMsg",source = "errorMsg"),
            @Mapping(target = "expressCompany.value",source = "expressCompanyValue"),
            @Mapping(target = "expressCompany.label",source = "expressCompanyLabel"),
            @Mapping(target = "expressNo",source = "expressNo"),
            @Mapping(target = "createBy.id",source = "createById"),
            @Mapping(target = "createBy.name",source = "createByName"),
            @Mapping(target = "createBy.userType",source = "createByType"),
            @Mapping(target = "createDate",expression = "java(com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getCreateDate()))"),
            @Mapping(target = "retryTimes",source = "retryTimes"),
            @Mapping(target = "thdNo",source = "workCardId"),
            @Mapping(target = "b2bShop.shopId",source = "shopId"),

            @Mapping(target = "createTimeMillis",ignore = true),
            @Mapping(target = "canSave",ignore = true),
            @Mapping(target = "lineNumber",ignore = true),
            @Mapping(target = "orderNo",ignore = true),
            @Mapping(target = "successFlag",ignore = true),
            @Mapping(target = "orgServiceType",ignore = true),
            @Mapping(target = "orgProduct",ignore = true),
            @Mapping(target = "orgProductSpec",ignore = true),
            @Mapping(target = "orgExpressCompany",ignore = true),
            @Mapping(target = "orgDesription",ignore = true),
            @Mapping(target = "define1",ignore = true),
            @Mapping(target = "define2",ignore = true),
            @Mapping(target = "define3",ignore = true),
            @Mapping(target = "orgQty",ignore = true),
            @Mapping(target = "sort",ignore = true),
    })
    public abstract TempOrder mqToModel(MQOrderImportMessage.OrderImportMessage message);

    public TempOrder manualMqToModel(MQOrderImportMessage.OrderImportMessage msg){
        if(msg == null){
            return null;
        }
        TempOrder order = new TempOrder();
        order.setId(msg.getId());
        order.setCustomer(new Customer(msg.getCustomerId(),msg.getCustomerName()));
        order.setRepeateOrderNo(msg.getRepeateOrderNo());
        order.setUserName(msg.getUserName());
        order.setPhone(msg.getPhone());
        order.setTel(msg.getTel());
        order.setErrorMsg(msg.getErrorMsg());
        order.setAddress(msg.getAddress());
        order.setBrand(msg.getBrand());
        order.setProductSpec(msg.getProductSpec());
        order.setProduct(new Product(msg.getProductId(),msg.getProductName()));
        order.setServiceType(new ServiceType(msg.getServiceTypeId(),"",msg.getServiceTypeName()));
        order.setDescription(msg.getDescription());
        order.setQty(msg.getQty());
        order.setExpressCompany(new Dict(msg.getExpressCompanyValue(),msg.getExpressCompanyLabel()));
        order.setExpressNo(msg.getExpressNo());
        order.setCreateBy( new User(msg.getCreateById(),msg.getCreateByName()));
        order.setCreateDate(DateUtils.longToDate(msg.getCreateDate()));
        order.setRetryTimes(msg.getRetryTimes());
        order.setThdNo(msg.getWorkCardId());//第三方单号 2018/12/19
        order.setB2bShop(new B2bCustomerMap(msg.getShopId(),""));
        return order;
    }

    public MQOrderImportMessage.OrderImportMessage modelToMq(TempOrder model){
        if(model == null){
            return null;
        }
        MQOrderImportMessage.OrderImportMessage.Builder builder = modelToMqBuilder(model);
        return builder.build();
    }

    public MQOrderImportMessage.OrderImportMessage.Builder modelToMqBuilder(TempOrder model){
        if(model == null){
            return null;
        }
        MQOrderImportMessage.OrderImportMessage.Builder builder = MQOrderImportMessage.OrderImportMessage.newBuilder()
                .setId(model.getId()==null?0:model.getId())
                .setCustomerId(model.getCustomer().getId())
                .setCustomerName(model.getCustomer().getName())
                .setRepeateOrderNo(model.getRepeateOrderNo())
                .setUserName(model.getUserName())
                .setPhone(model.getPhone())
                .setTel(model.getTel())
                .setAddress(model.getAddress())
                .setProductId(model.getProduct().getId())
                .setProductName(model.getProduct().getName())
                .setBrand(model.getBrand())
                .setProductSpec(model.getProductSpec())
                .setDescription(model.getDescription())
                .setServiceTypeId(model.getServiceType().getId())
                .setServiceTypeName(model.getServiceType().getName())
                .setQty(model.getQty())
                .setErrorMsg(model.getErrorMsg())
                .setExpressCompanyValue(model.getExpressCompany()==null?"":model.getExpressCompany().getValue())
                .setExpressCompanyLabel(model.getExpressCompany()==null?"":model.getExpressCompany().getLabel())
                .setExpressNo(model.getExpressNo())
                .setCreateById(model.getCreateBy().getId())
                .setCreateByName(model.getCreateBy().getName())
                .setCreateByType(model.getCreateBy().getUserType())
                .setCreateDate(model.getCreateDate().getTime())
                .setRetryTimes(model.getRetryTimes())
                .setWorkCardId(model.getThdNo())//第三方单号 2018/12/19
                .setShopId(model.getB2bShop()==null?"":model.getB2bShop().getShopId()); //购买店铺
        return builder;
    }

    public Order mqToOrder(MQOrderImportMessage.OrderImportMessage message){
        if(message == null){
            return null;
        }
        Order order = new Order();
        if(message.getId()>0) {
            order.setId(message.getId());
        }
        String description = StringEscapeUtils.unescapeHtml4(message.getDescription())
                .replace("\"", "")
                .replace(":", "|")
                .replace("http|", "http:")
                .replace("https|", "https:")
                .replace("\\\\", "")
                .replace("\\", "");
        order.setDescription(StringUtils.left(description,255));
        User user = new User(message.getCreateById(),message.getCreateByName(),"");
        user.setUserType(message.getCreateByType());
        order.setCreateBy(user);
        order.setCreateDate(DateUtils.longToDate(message.getCreateDate()));
        order.setQuarter(QuarterUtils.getSeasonQuarter(order.getCreateDate()));
        order.setTotalQty(message.getQty());
        order.setRepeateNo(message.getRepeateOrderNo());
        order.setOrderType(new Dict(Order.ORDER_ORDERTYPE_DSXD.toString(),"电商下单"));
        order.setWorkCardId(message.getWorkCardId());//第三方单号 2018/12/19
        order.setParentBizOrderId(message.getWorkCardId());//第三方单号 2018/12/19

        //Item
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(new Product(message.getProductId(),message.getProductName()));
        orderItem.setServiceType(new ServiceType(message.getServiceTypeId(),"",message.getServiceTypeName()));
        orderItem.setItemNo(10);
        orderItem.setBrand(message.getBrand().trim());
        orderItem.setProductSpec(message.getProductSpec().trim());
        orderItem.setQty(message.getQty());
        if(StringUtils.isNotBlank(message.getExpressCompanyValue())) {
            orderItem.setExpressCompany(new Dict(message.getExpressCompanyValue(),message.getExpressCompanyLabel()));
        }
        orderItem.setExpressNo(message.getExpressNo());
        order.setItems(Lists.newArrayList(orderItem));

        //Status
        OrderStatus ostatus = new OrderStatus();
        ostatus.setQuarter(order.getQuarter());
        //ostatus.setOrderId(order.getId());//未处理
        ostatus.setUrgentDate(order.getCreateDate());
        order.setOrderStatus(ostatus);

        //Location 2019-04-24
        OrderLocation location = new OrderLocation();
        location.setQuarter(order.getQuarter());
        order.setOrderLocation(location);

        //Condition
        OrderCondition condition = new OrderCondition();
        condition.setQuarter(order.getQuarter());
        //condition.setQuarter(order.getQuarter());
        //condition.setOrderId(order.getId());
        //condition.setOrderNo(order.getOrderNo());

        condition.setUserName(message.getUserName().trim());
        condition.setPhone1(message.getPhone().trim());
        if(StringUtils.isNotBlank(message.getTel().trim())) {
            condition.setPhone2(message.getTel().trim());
        }
        condition.setServicePhone(StringUtils.isBlank(condition.getPhone1()) ? condition.getPhone2() : condition.getPhone1());
        //未做检查
        condition.setAddress(StringEscapeUtils.unescapeHtml4(message.getAddress().replace("null", "")).replace("\"", "").replace(":", "|"));//详细地址
        condition.setServiceAddress(condition.getAddress());
        //condition.setArea(order.getArea());

        condition.setDelFlag(0);
        condition.setCreateDate(order.getCreateDate());
        condition.setCreateBy(order.getCreateBy());
        condition.setCustomerOwner(message.getCreateByName());//客户负责人

        condition.setTotalQty(order.getTotalQty());
        condition.setCustomer(new Customer(message.getCustomerId(),message.getCustomerName()));

        //condition.setStatus();
        UrgentLevel urgentLevel = new UrgentLevel(0l, "不加急");
        condition.setUrgentLevel(urgentLevel);
        order.setOrderCondition(condition);

        //fee
        OrderFee fee = new OrderFee();
        fee.setQuarter(order.getQuarter());
        order.setOrderFee(fee);
        if(StringUtils.isNotBlank(message.getShopId())){
            B2bCustomerMap b2bShop = new B2bCustomerMap(message.getShopId());
            order.setB2bShop(b2bShop);
        }
        return order;
    }

}
