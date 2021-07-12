/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderImportMessageMapper;
import com.wolfking.jeesite.modules.sd.dao.OrderImportDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderConvertVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单导入服务类
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderImportService extends LongIDBaseService {

    public static int ERRORCODE_DUPLICATE_ENTRY = 999999;
    /**
     * 持久层对象
     */
    @Autowired
    protected OrderImportDao dao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderMQService orderMQService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;

    //region 订单导入

    /**
     * 保存导入订单
     * 产生订单错误时，才保存
     * @param order
     */
    @Transactional(readOnly = false)
    public void insertTempOrder(TempOrder order) {
        dao.insertTempOrder(order);
    }

    public TempOrder getTempOrder(Long id) {
        TempOrder order = dao.getTempOrder(id);
        if (order.getExpressCompany() != null && StringUtils.isNoneBlank(order.getExpressCompany().getValue())) {
            final List<Dict> expresses = MSDictUtils.getDictList("express_type");//切换为微服务
            Dict express = expresses.stream().filter(t -> t.getValue().equalsIgnoreCase(order.getExpressCompany().getValue())).findFirst().orElse(null);
            if (express != null) {
                order.setExpressCompany(express);
            }
        }
        ServiceType serviceType = serviceTypeService.getFromCache(order.getServiceType().getId());
        if (serviceType != null) {
            order.getServiceType().setName(serviceType.getName());
        }
        //user微服务
        if (order.getCreateBy()!= null && order.getCreateBy().getId()!= null) {
            String name = MSUserUtils.getName(order.getCreateBy().getId());
            order.getCreateBy().setName(name);
        }
        return order;
    }

    /**
     * 分页查询导入失败的订单列表
     *
     * @param entity
     * @return
     */
    public Page<TempOrder> findRetryTempOrder(Page<TempOrder> page, TempOrder entity) {
        entity.setPage(page);
        List<TempOrder> list = dao.findRetryTempOrder(entity);
        final StringBuffer loadCustomer = new StringBuffer(5);
        if (entity.getCustomer() != null && entity.getCustomer().getId() != null) {
            loadCustomer.append("false");
        } else {
            loadCustomer.append("true");
        }
        if (list != null && list.size() > 0) {
            Map<Long,Customer> customerMap = Maps.newHashMap();
            //快递公司
            Map<String,Dict> expressesMap = MSDictUtils.getDictMap("express_type");//切换为微服务
            Map<Long, ServiceType> serviceTypesMap = serviceTypeService.getAllServiceTypeMap();
            Map<String,String> shopNamesMap = Maps.newHashMap();
            ServiceType serviceType;
            Dict express;
            TempOrder order;
            Long cid;
            Customer customer;
            String shopName;
            StringBuilder key = new StringBuilder(30);
            for(int i=0,size=list.size();i<size;i++){
                order = list.get(i);
                express = expressesMap.get(order.getExpressCompany().getValue());
                if (express != null) {
                    order.setExpressCompany(express);
                }
                //customer
                if (loadCustomer.toString().equalsIgnoreCase("true")) {
                    cid = order.getCustomer().getId();
                    customer = customerMap.get(cid);
                    if(customer == null) {
                        customer = customerService.getFromCache(cid);
                        if (customer == null) {
                            customer = new Customer(cid,"");
                        }
                        customerMap.put(cid, customer);
                        order.setCustomer(customer);
                    }else {
                        order.setCustomer(customer);
                    }
                } else {
                    order.setCustomer(entity.getCustomer());
                }
                serviceType = serviceTypesMap.get(order.getServiceType().getId());
                if (serviceType != null) {
                    order.getServiceType().setName(serviceType.getName());
                }
                //店铺名称
                if(order.getB2bShop()!=null && StringUtils.isNotBlank(order.getB2bShop().getShopId())){
                    key.setLength(0);
                    key.append(order.getCustomer().getId()).append(":").append(order.getB2bShop().getShopId());
                    shopName = shopNamesMap.get(key.toString());
                    if(shopName == null) {
                        shopName = B2BMDUtils.getShopName(order.getCustomer().getId(), order.getB2bShop().getShopId());
                        if(shopName == null) {
                            shopName = "";
                        }
                        shopNamesMap.put(key.toString(),shopName);
                        order.getB2bShop().setShopName(shopName);
                    }else{
                        order.getB2bShop().setShopName(shopName);
                    }
                }
            }
            expressesMap.clear();
            shopNamesMap.clear();
            customerMap.clear();
            serviceTypesMap.clear();
        }
        loadCustomer.setLength(0);
        page.setList(list);
        return page;
    }

    /**
     * 客户取消导入订单
     *
     * @param orderId 订单id
     * @param comment 取消说明
     */
    @Transactional(readOnly = false)
    public void cancelTempOrder(Long orderId, User user, String comment) {
        TempOrder o = dao.getTempOrderStatus(orderId);
        if (o.getSuccessFlag() == 1 || o.getDelFlag() == TempOrder.DEL_FLAG_DELETE) {
            throw new OrderException(String.format("该订单已%s", o.getSuccessFlag() == 1 ? "转成正式订单" : "取消"));
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.clear();
        params.put("id", orderId);
        params.put("delFlag", TempOrder.DEL_FLAG_DELETE);
        params.put("errorMsg", comment);
        params.put("updateBy", user);
        params.put("updateDate", new Date());
        dao.updateTempOrder(params);
    }

    @Transactional(readOnly = false)
    public void retryError(Long id,String msg, User user,Date updateDate,Integer successFlag) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.clear();
        params.put("id", id);
        params.put("errorMsg", StringUtils.left(msg,250));
        if(successFlag != null && successFlag == 1) {
            params.put("successFlag", successFlag);
        }
        if(user != null) {
            params.put("updateBy", user);
        }
        params.put("updateDate", updateDate);
        dao.retryError(params);
    }

    /**
     * 自动转换导入订单(消息队列处理)
     * 正式订单id与导入id相同
     */
    public MSResponse<Order> transferOrder(MQOrderImportMessage.OrderImportMessage mqMessage) {
        MSResponse<Order> responseEntity = new MSResponse<>(MSErrorCode.SUCCESS);
        Boolean saveTrue = false;
        Order order = null;
        try {
            responseEntity = checkOrderAuto(mqMessage);
            if (!MSResponse.isSuccessCode(responseEntity)) {
                return responseEntity;
            }
            order = responseEntity.getData();
            //check repeate no
            OrderCondition orderCondition = order.getOrderCondition();
            String repeateOrderNo = orderService.getRepeateOrderNo( orderCondition.getCustomer().getId(),orderCondition.getPhone1());
            if (StringUtils.isNotBlank(repeateOrderNo)) {
                order.setRepeateNo(repeateOrderNo);
            }
            //save to db
            Long tmpId = mqMessage.getTmpId()>0?mqMessage.getTmpId():null;
            if(tmpId == null &&  mqMessage.getRetryTimes() >= 1){
                tmpId = mqMessage.getId();
            }
            orderService.createOrder_v2_1(order, tmpId);
            saveTrue = true;
            responseEntity.setCode(MSErrorCode.SUCCESS.getCode());
        }catch (Exception e) {
            saveTrue = false;
            if(StringUtils.contains(e.getMessage(),"Duplicate")) {
                log.error(MessageFormat.format("导入订单自动转单失败:同一个订单重复提交,id:{0},user:{1},phone:{2}",mqMessage.getId(),mqMessage.getUserName(),mqMessage.getPhone()), e);
                responseEntity.setCode(ERRORCODE_DUPLICATE_ENTRY);//Duplicate
                responseEntity.setMsg("已转换，同一个订单重复提交");
            }else {
                //log.error(MessageFormat.format("导入订单自动转单失败,user:{0},phone:{1}",mqMessage.getUserName(),mqMessage.getPhone()), e);
                responseEntity.setCode(MSErrorCode.FAILURE.getCode());
                responseEntity.setMsg(StringUtils.left(e.getMessage(), 200));
            }
        }finally {
            if(saveTrue == true){
                orderMQService.sendCreateOrderMessage(order, "OrderImportService.transferOrder");
            }
            return responseEntity;
        }
    }

    /**
     * 订单检查及转换为正式订单
     * @version 1.0
     * @return
     */
    public MSResponse<Order> checkOrderAuto(MQOrderImportMessage.OrderImportMessage message){
        MSResponse<Order> response = new MSResponse<>(MSErrorCode.FAILURE);
        Order order = null;
        if(message == null){
            response.setMsg("无订单信息");
            log.error("导入订单信息转换失败,message:null");
            return response;
        }

        try {
            order = Mappers.getMapper(OrderImportMessageMapper.class).mqToOrder(message);
        }catch (Exception e){
            response.setMsg("导入订单信息转换失败");
            log.error("导入订单信息转换失败,message:{}",new JsonFormat().printToString(message),e);
            return response;
        }
        Customer customer = customerService.getFromCache(message.getCustomerId());
        if (customer == null){
            response.setMsg("确认客户类型失败");
            return response;
        }
        if(order.getCreateDate() == null){
            order.setCreateDate(new Date());
            order.setQuarter(QuarterUtils.getSeasonQuarter(order.getCreateDate()));
        }
        User user = order.getCreateBy();
        String quarter = order.getQuarter();
        if(StringUtils.isBlank(quarter)){
            quarter = QuarterUtils.getSeasonQuarter(order.getCreateDate());
            order.setQuarter(quarter);
        }
        Long orderId = order.getId();
        if(orderId == null || orderId<=0) {
            orderId = SeqUtils.NextIDValue(SeqUtils.TableName.Order);
        }
        CustomerFinance finance = customerService.getFinanceForAddOrder(message.getCustomerId());
        if (finance == null || finance.getPaymentType() == null || StringUtils.isBlank(finance.getPaymentType().getValue())) {
            response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_INVALID_PAYMENTTYPE);
            response.setMsg(String.format("厂商：%s 未设置结算方式", message.getCustomerName()));
            return response;
        }
        OrderCondition condition = order.getOrderCondition();
        customer.setFinance(finance);
        //condition.getCustomer().setFinance(finance);
        condition.setCustomer(customer);
        // 订单项次处理
        List<OrderItem> items = order.getItems();
        List<CustomerPrice> prices = customerService.getPricesFromCache(message.getCustomerId());
        Optional<CustomerPrice> price ;
        Product p;
        Set<String> pids = Sets.newHashSet();//产品
        Long categoryId = null; //产品类别
        Set<String> sids = Sets.newHashSet();//服务项目
        Integer hasSet = 0;
        List<Dict> expressCompanys = MSDictUtils.getDictList("express_type");//切换为微服务
        Dict expressCompany;
        int orderServiceType = 0;
        //服务类型
        Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
        ServiceType serviceType;
        StringBuilder content = new StringBuilder();
        content.append("师傅，在您附近有一张  ");
        // 移除产品为空的项目,并读取最新价格
        for (Iterator<OrderItem> it = items.iterator(); it.hasNext();)
        {
            OrderItem item = it.next();
            if(item.getProduct() == null || item.getServiceType() == null){
                it.remove();
                continue;
            }
            //价格
            price = prices.stream().filter(t->
                    Objects.equals(t.getProduct().getId(),item.getProduct().getId()) && Objects.equals(t.getServiceType().getId(),item.getServiceType().getId())).findFirst();
            if(!price.isPresent()){
                response.setMsg(String.format("产品:%s 未定义服务项目:%s 的服务价格",item.getProduct().getName(),item.getServiceType().getName()));
                return response;
            }
            p = productService.getProductByIdFromCache(item.getProduct().getId());
            //品类检查 2019-09-25
            if(categoryId == null){
                categoryId = p.getCategory().getId();
            }else if(!categoryId.equals(p.getCategory().getId())){
                response.setMsg("订单中产品属不同品类，无法保存。");
                return response;
            }
            item.setProduct(p);
            item.setStandPrice(price.get().getPrice());
            item.setDiscountPrice(price.get().getDiscountPrice());
            if(expressCompanys!=null && expressCompanys.size()>0){
                expressCompany = expressCompanys.stream().filter(t->t.getValue().equalsIgnoreCase(item.getExpressCompany().getValue())).findFirst().orElse(item.getExpressCompany());
                item.setExpressCompany(expressCompany);
            }
            if(p.getSetFlag()==1){
                hasSet = 1;
            }
            pids.add(String.format(",%s,",p.getId()));
            sids.add(String.format(",%s,",item.getServiceType().getId()));
            //工单类型按服务项目设定为准
            serviceType = serviceTypeMap.get(item.getServiceType().getId());
            if(serviceType == null){
                response.setMsg("确认服务项目的工单类型错误，无法保存。");
                return response;
            }
            //除维修(2)外，值最大的优先
            if(orderServiceType == 0){
                orderServiceType = serviceType.getOrderServiceType();
            }else if (serviceType.getOrderServiceType() == 2){
                orderServiceType = serviceType.getOrderServiceType();
            }else if(orderServiceType < serviceType.getOrderServiceType()){
                orderServiceType = serviceType.getOrderServiceType();
            }
            content.append(item.getServiceType().getName())
                    .append(item.getBrand())
                    .append(item.getProduct().getName());
        }
        content.append("的工单，请尽快登陆APP接单~");
        order.setAppMessage(content.toString());//app&短信通知内容

        if(orderServiceType==0){
            orderServiceType = 2;
        }

        OrderLocation location = order.getOrderLocation();// 2019-04-24
        location.setOrderId(orderId);
        String[] areaParseResult = null;
        Area area = null;
        Area subArea = null;
        int canRush = 0;
        int kefuType = 0;
        OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = null;
        try {
            //areaParseResult = AreaUtils.decodeAddressGaode(condition.getAddress().replace(" ", ""));  //mark on 2020-8-5
            areaParseResult = AreaUtils.decodeAddressGaodeFromMS(condition.getAddress().replace(" ", ""));  //add on 2020-8-5
            if (areaParseResult != null && areaParseResult.length > 0) {
                if(ObjectUtil.isEmpty(areaParseResult[0]) || areaParseResult[0].trim().equals("0")){
                    response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_ADDRESS_PARSE_FAILURE);
                    return response;
                }
                area = new Area(Long.valueOf(areaParseResult[0]));
                location.setArea(area);
                if (StringUtils.isNotEmpty(areaParseResult[1])) {
                    subArea = new Area(Long.valueOf(areaParseResult[1]));   // 获取街道id  //2019-5-21
                }
                //经纬度处理 2019-04-15
                if(areaParseResult.length == 9 && StringUtils.isNotBlank(areaParseResult[7]) && StringUtils.isNotBlank(areaParseResult[8])){
                    location.setLongitude(StringUtils.toDouble(areaParseResult[7]));
                    location.setLatitude(StringUtils.toDouble(areaParseResult[8]));
                }
                /* 省/市id 2019-09-25 */

                Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(area.getId());
                Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
                Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
                condition.setProvinceId(province.getId());
                condition.setCityId(city.getId());
                //vip客户，不检查客服类型 ， 街道id小于等于3也不检查客服类型 2020-12-8
                long subAreaId = Optional.ofNullable(subArea).map(t->t.getId()).orElse(0l);
               /* if(customer.getVipFlag()==1){
                    kefuType = OrderCondition.VIP_KEFU_TYPE;
                }else{
                    canRush = orderService.isCanRush(categoryId,city.getId(),area.getId(),subAreaId);
                    kefuType = orderService.getKefuType(categoryId,city.getId(),area.getId(),subAreaId);
                }*/
                orderKefuTypeRuleEnum  = orderService.getKefuType(categoryId,city.getId(),area.getId(),subAreaId,customer.getVipFlag(),customer.getVip());
                kefuType = orderKefuTypeRuleEnum.getCode();
                if(kefuType==OrderCondition.RUSH_KEFU_TYPE){
                    canRush = 1;
                }
                condition.setCanRush(canRush);
                condition.setKefuType(kefuType);
            } else {
                response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_ADDRESS_PARSE_FAILURE);
                return response;
            }

        } catch (Exception e) {
            log.error("解析地址失败:{}",condition.getAddress(),e);
            response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_ADDRESS_PARSE_FAILURE);
            return response;
        }

        if(areaParseResult != null && areaParseResult.length> 2 && areaParseResult[2].length()> B2BOrderConvertVModel.ADDRESS_MAX_LENGTH){
            response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_ADDRESS_PARSE_FAILURE);
            response.setMsg("详细地址长度超过数据库设定:"+String.valueOf(B2BOrderConvertVModel.ADDRESS_MAX_LENGTH));
            return response;
        }

        //【重新计算价格】
        OrderUtils.rechargeOrder2(items);
        Double totalCharge = 0.00;
        Double blockedCharge = 0.00;
        for (OrderItem item : items) {
            totalCharge = totalCharge + item.getCharge();
            blockedCharge = blockedCharge + item.getBlockedCharge();
        }

        //check余额
        if (finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) - finance.getBlockAmount() - totalCharge - blockedCharge < 0) {
            response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_BALANCE_INSUFFICIENT);
            response.setMsg(message.getCustomerName() + " : 账户余额不足");
            return response;
        }

        User kefu = orderService.getRandomKefu(message.getCustomerId(), area.getId(), categoryId,kefuType,condition.getCityId(),condition.getProvinceId());
        if (kefu == null) {
            response.setErrorCode(B2BOrderVModel.ERROR_CODE_B2BORDER_AREA_HAS_NO_KEFU);
            //String failReason=orderService.findKefuFail(kefuType,categoryId);
            response.setMsg("此区域暂未分配跟进客服，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
            return response;
        }

        String orderNo;
        try {
            orderNo = orderService.getNewOrderNo();
            if (StringUtils.isBlank(orderNo)) {
                orderNo = orderService.getNewOrderNo();
                if (StringUtils.isBlank(orderNo)) {
                    response.setErrorCode(MSErrorCode.FAILURE);
                    response.setMsg("生成订单号失败，请重试");
                    return response;
                }
            }
        } catch (Exception e) {
            response.setErrorCode(MSErrorCode.FAILURE);
            response.setMsg(String.format("转换系统工单错误:%s", e.getMessage()));
            log.error("生成订单号失败",e);
            return response;
        }

        //Order
        order.setId(orderId);
        order.setOrderNo(orderNo);
        order.setOrderChannel(new Dict(1,"线下单"));//2020-05-05 销售渠道
        // add on 2019-5-21 begin
        if (area != null && areaParseResult != null && areaParseResult.length > 2) {
            area.setFullName(areaParseResult[2].trim());
            area.setName(areaParseResult[2].trim());
        }
        // add on 2019-5-21 end

        //OrderCondition
        condition.setOrderId(orderId);
        condition.setOrderNo(orderNo);
        condition.setArea(area);
        condition.setSubArea(subArea);  // 4级区域  //2019-5-21

        // add on 2019-5-21 begin
        if (areaParseResult != null && areaParseResult.length > 2) {
            condition.setAreaName(areaParseResult[2].trim());
        }
        if (areaParseResult != null && areaParseResult.length > 3) {
            condition.setAddress(areaParseResult[3]);
        }
        // add on 2019-5-21 end

        condition.setServiceAddress(condition.getAddress());
        condition.setOrderServiceType(orderServiceType);
        condition.setHasSet(hasSet);
        condition.setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,", ",,"));
        condition.setProductCategoryId(categoryId);//2019-09-25
        condition.setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,", ",,"));
        condition.setTotalQty(message.getQty());
        condition.setKefu(kefu);
        Dict status;
        if(user.isCustomer()) {
            user = UserUtils.get(user.getId());
        }
        if (user.isSystemUser()) {
            //不需审核
            status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");
        } else if (user.isCustomer() && user.getCustomerAccountProfile().getOrderApproveFlag() == 0) {
            //不需审核
            status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
        } else {
            status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_NEW), "order_status");//切换为微服务
        }
        condition.setStatus(status);

        //OrderItem
        for (OrderItem item : items) {
            item.setOrderId(orderId);
            item.setQuarter(quarter);
        }
        order.setItems(items);

        //OrderFee
        OrderFee fee = order.getOrderFee()==null?new OrderFee():order.getOrderFee();
        fee.setOrderId(orderId);
        fee.setQuarter(quarter);
        fee.setExpectCharge(totalCharge);
        fee.setBlockedCharge(blockedCharge);
        fee.setOrderPaymentType(finance.getPaymentType());
        order.setOrderFee(fee);

        //OrderStatus
        OrderStatus ostatus = order.getOrderStatus()==null?new OrderStatus():order.getOrderStatus();
        ostatus.setQuarter(quarter);
        ostatus.setOrderId(order.getId());
        order.setOrderStatus(ostatus);

        response.setErrorCode(MSErrorCode.SUCCESS);
        response.setData(order);
        return response;
    }


    //endregion

}
