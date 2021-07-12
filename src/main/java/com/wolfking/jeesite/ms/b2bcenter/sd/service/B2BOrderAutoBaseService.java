package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BProductMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceTypeMapping;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.b2bcenter.exception.AddressParseFailureException;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderTranserFailureException;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BProductNotSupportedException;
import com.wolfking.jeesite.ms.b2bcenter.exception.IncompleteB2BOrderException;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.mapper.B2BOrderMapper;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.assertj.core.util.Lists;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2BOrderAutoBaseService extends B2BOrderBaseService {

    //region 内部方式

    /**
     * 获取店铺ID
     */
    protected final String getShopId(Integer dataSourceId, String shopId, List<TwoTuple<String, String>> b2bProductCodeAndSpecs) {
        String returnShopId = "";
        if (dataSourceId == B2BDataSourceEnum.JD.id) {
            if (StringUtils.isNotBlank(shopId)) {
                returnShopId = shopId;
            } else {
                //TODO：京东自营的工单需要特殊处理
                if (b2bProductCodeAndSpecs != null && !b2bProductCodeAndSpecs.isEmpty()) {
                    List<String> b2bProductCodes = b2bProductCodeAndSpecs.stream().map(TwoTuple::getAElement).distinct().collect(Collectors.toList());
                    if (!b2bProductCodes.isEmpty()) {
                        List<String> productCodeSet = b2bProductCodes.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
                        if (!productCodeSet.isEmpty()) {
                            List<String> temp = B2BMDUtils.getShopIdsFromProductMapping(B2BDataSourceEnum.JD, productCodeSet);
                            if (temp.size() == 1) {
                                returnShopId = temp.get(0);
                            }
                        }
                    }
                }
            }
        } else if (dataSourceId == B2BDataSourceEnum.PHILIPS.id) {
            //TODO：飞利浦的工单需要根据产品获取店铺ID
            List<String> productCodeSet = b2bProductCodeAndSpecs.stream().map(TwoTuple::getAElement).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(productCodeSet)) {
                List<String> shopIds = B2BMDUtils.getShopIdsFromProductMapping(B2BDataSourceEnum.PHILIPS, productCodeSet);
                if (CollectionUtil.isNotEmpty(shopIds)) {
                    returnShopId = shopIds.get(0);
                }
            }
        } else {
            returnShopId = StringUtils.toString(shopId);
        }
        return returnShopId;
    }

    /**
     * B2BOrder转B2BOrderVModel，并根据shopId解析出Customer
     */
    protected List<B2BOrderVModel> toB2BOrderVModels(List<B2BOrder> b2BOrders) {
        List<B2BOrderVModel> list = Lists.newArrayList();
        if (b2BOrders != null && !b2BOrders.isEmpty()) {
            Map<Integer, Map<String, B2BCustomerMapping>> customerMappings = Maps.newHashMap();
            Map<String, B2BCustomerMapping> b2BCustomerMappingMap;
            HashMap<String, Customer> customerMap = Maps.newHashMap();
            Map<String, Dict> dataSourceMap = MSDictUtils.getDictMap("order_data_source");
            Map<Integer, String> defaultShopMap = B2BMDUtils.getDefaultShopMap();
            String shopId;
            String defaultShopId;
            Long customerId;
            Customer customer;
            B2BCustomerMapping customerMapping;
            List<TwoTuple<String, String>> b2bProductCodeAndSpes;
            B2BOrderVModel orderVModel;
            for (B2BOrder item : b2BOrders) {
                b2bProductCodeAndSpes = item.getItems().stream().filter(i -> StringUtils.isNotBlank(i.getProductCode()))
                        .map(i -> new TwoTuple<>(i.getProductCode(), i.getProductSpec())).collect(Collectors.toList());
                orderVModel = Mappers.getMapper(B2BOrderMapper.class).toB2BOrderVModel(item);
                //TODO: 客户产品型号转换成大写
                String productSpec;
                for (B2BOrder.B2BOrderItem b2BOrderItem : orderVModel.getItems()) {
                    productSpec = StringUtils.toString(b2BOrderItem.getProductSpec()).toUpperCase();
                    b2BOrderItem.setProductSpec(productSpec);
                }
                shopId = getShopId(item.getDataSource(), item.getShopId(), b2bProductCodeAndSpes);
                if (item.getDataSource() == B2BDataSourceEnum.TMALL.id) {
                    if (StringUtils.isNotBlank(item.getShopName())) {
                        orderVModel.setShopName(item.getShopName());
                    }
                }
                orderVModel.setShopId(shopId);
                defaultShopId = defaultShopMap.get(item.getDataSource());
                if (StringUtils.isNotBlank(defaultShopId)) {
                    orderVModel.setDefaultShopId(defaultShopId);
                }
                orderVModel.setIsReadOnly(isReadOnly(item));
                orderVModel.setDataSourceName(getDataSourceName(item, dataSourceMap));

                if (!customerMappings.containsKey(item.getDataSource())) {
                    b2BCustomerMappingMap = B2BMDUtils.getCustomerMappingMap(B2BDataSourceEnum.get(item.getDataSource()));
                    customerMappings.put(item.getDataSource(), b2BCustomerMappingMap);
                }
                b2BCustomerMappingMap = customerMappings.get(item.getDataSource());
                if (b2BCustomerMappingMap != null && !b2BCustomerMappingMap.isEmpty()) {
                    customerMapping = b2BCustomerMappingMap.get(shopId);
                    if (customerMapping != null) {
                        orderVModel.setCustomerMapping(customerMapping);
                        if (StringUtils.isBlank(orderVModel.getShopName())) {
                            orderVModel.setShopName(customerMapping.getShopName());
                        }
                        if (customerMap.containsKey(shopId)) {
                            orderVModel.setCustomer(customerMap.get(shopId));
                        } else {
                            customerId = customerMapping.getCustomerId();
                            if (customerId != null && customerId > 0) {
                                customer = customerService.getFromCache(customerId);
                                if (customer != null) {
                                    customerMap.put(shopId, customer);
                                } else {
                                    customer = new Customer(customerId);
                                }
                                orderVModel.setCustomer(customer);
                            }
                        }
                    }
                }
                Long b2bCreateById = getB2BOrderCreateBy(orderVModel.getDataSource(), orderVModel.getShopId(), orderVModel.getCreateById());
                orderVModel.setCreateById(b2bCreateById);
                list.add(orderVModel);
            }
        }
        return list;
    }

    /**
     * 检查B2B工单是否是只读的
     */
    private boolean isReadOnly(B2BOrder order) {
        boolean isReadOnly = false;
//        if (order != null && order.getDataSource() != null) {
//            B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(order.getDataSource());
//            if (dataSourceEnum != B2BDataSourceEnum.XYINGYAN) {
//                isReadOnly = false;
//            }
//        }
        return isReadOnly;
    }

    /**
     * 获取数据源名称
     */
    private String getDataSourceName(B2BOrder order, Map<String, Dict> dataSourceMap) {
        String dataSourceName = "";
        if (order != null && order.getDataSource() != null) {
            Dict dataSourceDict = dataSourceMap.get(order.getDataSource().toString());
            if (dataSourceDict != null && StringUtils.isNotBlank(dataSourceDict.getLabel())) {
                dataSourceName = dataSourceDict.getLabel();
            }
        }
        return dataSourceName;
    }

    /**
     * 检查厂商及结算方式
     */
    protected Customer getCustomer(B2BOrderVModel orderVModel) {
        Customer customer = orderVModel.getCustomer();
        if (customer == null || customer.getId() == null || customer.getId() <= 0) {
            throw new B2BOrderTranserFailureException("未发现关联的厂商信息");
        }
        CustomerFinance finance = customerService.getFinanceForAddOrder(customer.getId());
        if (finance == null || finance.getPaymentType() == null || StringUtils.isBlank(finance.getPaymentType().getValue())) {
            throw new B2BOrderTranserFailureException(String.format("厂商：%s 未设置结算方式", customer.getName()));
        }
        customer.setFinance(finance);
        return customer;
    }

    /**
     * 检查是否只有一个工单项
     */
    protected void validateOnlyOneOrderItem(B2BOrderVModel orderVModel) {
        if (orderVModel.getOrderItemQty() != 1) {
            throw new IncompleteB2BOrderException("工单有多个工单子项，请人工处理");
        }
    }

    /**
     * 创建OrderItem
     * 并根据订单项目获得订单类型(安装/维修)
     */
    protected Map<String,Object> createOrderItems(B2BOrderVModel orderVModel, Long customerId) {
        Map<String,Object> map = Maps.newHashMapWithExpectedSize(2);
        B2BDataSourceEnum dataSource = B2BDataSourceEnum.get(orderVModel.getDataSource());
        Map<String, B2BServiceTypeMapping> serviceTypeMappingMap = B2BMDUtils.getServiceTypeMappingMap(dataSource);
        Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
        Map<String, CustomerPrice> priceMap = customerService.getCustomerPriceMap(customerId);
        // 以客户现有上架产品为准
        List<Product> productList = productService.getCustomerProductList(customerId);
        if(CollectionUtils.isEmpty(productList)){
            throw new B2BOrderTranserFailureException("读取客户现有产品列表失败");
        }
        Set<Long> productIdSet = productList.stream().map(t->t.getId()).collect(Collectors.toSet());

        List<OrderItem> orderItemList = Lists.newArrayList();
        OrderItem orderItem = null;
        Long categoryId = null;
        int orderServiceType = 0;
        ServiceType serviceType;
        for (int i = 0; i < orderVModel.getItems().size(); i++) {
            B2BOrder.B2BOrderItem b2BOrderItem = orderVModel.getItems().get(i);
            orderItem = new OrderItem();
            orderItem.setDelFlag(0);
            orderItem.setItemNo(10 * (i + 1));
            orderItem.setQty(b2BOrderItem.getQty());
            orderItem.setPics(b2BOrderItem.getPics());//图片 2020-08-06
            String brand = StringUtils.isNotBlank(b2BOrderItem.getBrand()) ? b2BOrderItem.getBrand() : StringUtils.toString(orderVModel.getBrand());
//            orderItem.setBrand(StringUtils.left(StringUtils.toString(orderVModel.getBrand()), 20));//截取品牌长度，sd_orderdetail的brand字段为varchar(20)
            orderItem.setBrand(StringUtils.left(brand, 20));//截取品牌长度，sd_orderdetail的brand字段为varchar(20)
            orderItem.setProductSpec(B2BOrderVModel.leftSubString(b2BOrderItem.getProductSpec(), B2BOrderVModel.FIELD_LENGTH_PRODUCT_SPEC));
            if (b2BOrderItem.getQty() <= 0) {
                throw new B2BOrderTranserFailureException(String.format("工单子项产品:%s 的数量小于1 请确认", b2BOrderItem.getProductCode()));
            }

            String warrantyType = b2BOrderItem.getWarrantyType();
            if (warrantyType == null || StringUtils.isBlank(warrantyType)) {
                throw new B2BProductNotSupportedException("工单没有指定质保类型，请确认");
            }
            String serviceTypeMappingKey = String.format("%s:%s", b2BOrderItem.getServiceType(), warrantyType);

            B2BServiceTypeMapping serviceTypeMapping = serviceTypeMappingMap.get(serviceTypeMappingKey);
            Long serviceTypeId = serviceTypeMapping != null ? serviceTypeMapping.getServiceTypeId() : null;
            if (serviceTypeId == null) {
                throw new B2BProductNotSupportedException(String.format("工单系统中未找到对应的服务类型:%s 请确认", b2BOrderItem.getServiceType()));
            }
            serviceType = serviceTypeMap.get(serviceTypeId);
            if (serviceType == null) {
                throw new B2BProductNotSupportedException(String.format("工单系统中未找到对应的服务类型:%s 请确认", b2BOrderItem.getServiceType()));
            }
            orderItem.setServiceType(serviceType);
            //工单类型按服务项目设定为准
            //除维修(2)外，值最大的优先
            if(orderServiceType == 0){
                orderServiceType = serviceType.getOrderServiceType();
            }else if (serviceType.getOrderServiceType() == 2){
                orderServiceType = serviceType.getOrderServiceType();
            }else if(orderServiceType < serviceType.getOrderServiceType()){
                orderServiceType = serviceType.getOrderServiceType();
            }
            B2BProductMapping  productMapping = B2BMDUtils.getProductMappingNew(dataSource, orderVModel.getShopId(), orderVModel.getDefaultShopId(), b2BOrderItem.getProductCode(), orderItem.getProductSpec());
            if (productMapping == null || productMapping.getProductId() == null || productMapping.getProductId() == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("无类目:").append(b2BOrderItem.getProductCode());
                if (StringUtils.isNotBlank(orderItem.getProductSpec())) {
                    stringBuilder.append(" 型号/规格:").append(orderItem.getProductSpec());
                }
                stringBuilder.append(" 匹配的产品");
                throw new B2BProductNotSupportedException(stringBuilder.toString());
            }
            //已客户现有上架产品为准
            if(!productIdSet.contains(productMapping.getProductId())){
                throw new B2BProductNotSupportedException(String.format("产品:%s 已下架，请确认客户运营的产品", b2BOrderItem.getProductName()));
            }
            Product product = productService.getProductByIdFromCache(productMapping.getProductId());
            if (product == null) {
                throw new B2BProductNotSupportedException(String.format("未获得产品:%s 信息", b2BOrderItem.getProductName()));
            }
            /* 检查品类 2019-09-26 */
            if(categoryId == null){
                categoryId = product.getCategory().getId();
            }else if(!categoryId.equals(product.getCategory().getId())){
                throw new B2BProductNotSupportedException("工单中产品属不同品类，无法保存。");
            }
            orderItem.setProduct(product);
            orderItem.setB2bProductCode(StringUtils.toString(b2BOrderItem.getProductCode()));/* orderItem增加B2B产品编码 */

            CustomerPrice price = priceMap.get(String.format("%d:%d", product.getId(), serviceType.getId()));
            if (price == null) {
                throw new B2BProductNotSupportedException(String.format("产品:%s 未定义服务项目:%s 的服务价格", product.getName(), serviceType.getName()));
            }
            orderItem.setStandPrice(price.getPrice());
            orderItem.setDiscountPrice(price.getDiscountPrice());
            orderItem.setBlockedCharge(price.getBlockedPrice() * b2BOrderItem.getQty());

            orderItem.setExpressCompany(new Dict("", b2BOrderItem.getExpressCompany()));
            orderItem.setExpressNo(b2BOrderItem.getExpressNo());
            orderItemList.add(orderItem);
        }
        if(orderServiceType==0){
            orderServiceType = 2;
        }
        OrderUtils.rechargeOrder2(orderItemList);//【重新计算价格】
        //return orderItemList;
        map.put("orderServiceType",orderServiceType);
        map.put("items",orderItemList);
        return map;
    }

    /**
     * 创建Order
     */
    protected Order createOrder(B2BOrderVModel orderVModel, double totalCharge, double blockedCharge, Date createDate) {
        Order order = new Order();
        String quarter = QuarterUtils.getSeasonQuarter(createDate);
        order.setId(SeqUtils.NextIDValue(SeqUtils.TableName.Order));
        order.setQuarter(quarter);
        order.setDataSource(new Dict(String.valueOf(orderVModel.getDataSource())));
        order.setB2bOrderId(orderVModel.getB2bOrderId());
        order.setWorkCardId(orderVModel.getOrderNo());
        if (StringUtils.isBlank(orderVModel.getParentBizOrderId())) {
            order.setParentBizOrderId(orderVModel.getOrderNo());
        } else {
            order.setParentBizOrderId(orderVModel.getParentBizOrderId());
        }
        order.setTotalQty(orderVModel.getProductQty());
        order.setCreateDate(createDate);
        order.setOrderType(new Dict(Order.ORDER_ORDERTYPE_B2B, ""));
        String description = "";
        if (StringUtils.isNotBlank(orderVModel.getDescription())) {
            description = orderVModel.getDescription() + "——" + B2BDataSourceEnum.get(orderVModel.getDataSource()).name + "B2B工单，请务必规范操作！";
        } else {
            description = B2BDataSourceEnum.get(orderVModel.getDataSource()).name + "B2B工单，请务必规范操作！";
        }
        if (StringUtils.isNotBlank(orderVModel.getAllB2bWarrantyCodes())) {
            description = description + "（" + "outerIdSKU: " + orderVModel.getAllB2bWarrantyCodes() + "）";
        }
        order.setDescription(description);
        B2bCustomerMap b2bShop = new B2bCustomerMap(orderVModel.getShopId());
        if (orderVModel.getCustomerMapping() != null) {
            b2bShop.setDataSource(orderVModel.getCustomerMapping().getDataSource());
            b2bShop.setShopName(orderVModel.getCustomerMapping().getShopName());
            b2bShop.setCustomerId(orderVModel.getCustomerMapping().getCustomerId());
            b2bShop.setCustomerName(orderVModel.getCustomerMapping().getCustomerName());
        }
        order.setB2bShop(b2bShop);
        //销售渠道 2020-04-28
        if (orderVModel.getSaleChannel() != null && orderVModel.getSaleChannel() > 0) {
            order.setOrderChannel(new Dict(orderVModel.getSaleChannel(), StringUtils.EMPTY));
        } else {
            int channel = Optional.ofNullable(orderVModel.getCustomerMapping()).map(t -> t.getSaleChannel()).orElse(1);
            if (channel == 0) {
                channel = 1;
            }
            order.setOrderChannel(new Dict(channel, StringUtils.EMPTY));
        }
        //销售渠道 End
        OrderAdditionalInfo additionalInfo = new OrderAdditionalInfo();
        additionalInfo.setEstimatedReceiveDate(StringUtils.toString(orderVModel.getEstimatedReceiveDate()));
        additionalInfo.setBuyDate(orderVModel.getBuyDate() == null ? 0 : orderVModel.getBuyDate());
        additionalInfo.setExpectServiceTime(StringUtils.toString(orderVModel.getExpectServiceTime()));
        additionalInfo.setSiteCode(StringUtils.toString(orderVModel.getSiteCode()));
        additionalInfo.setSiteName(StringUtils.toString(orderVModel.getSiteName()));
        additionalInfo.setEngineerName(StringUtils.toString(orderVModel.getEngineerName()));
        additionalInfo.setEngineerMobile(StringUtils.toString(orderVModel.getEngineerMobile()));
        additionalInfo.setOrderDataSource(StrUtil.trimToEmpty(orderVModel.getOrderDataSource()));
        order.setOrderAdditionalInfo(additionalInfo);

        //OrderFee
        OrderFee orderFee = new OrderFee();
        orderFee.setOrderId(order.getId());
        orderFee.setQuarter(quarter);
        orderFee.setOrderPaymentType(orderVModel.getCustomer().getFinance().getPaymentType());
        orderFee.setExpectCharge(totalCharge);
        orderFee.setBlockedCharge(blockedCharge);
        order.setOrderFee(orderFee);

        //OrderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setQuarter(quarter);
        orderStatus.setOrderId(order.getId());
        order.setOrderStatus(orderStatus);
        return order;
    }

    /**
     * 创建OrderCondition
     */
    protected OrderCondition createOrderCondition(B2BOrderVModel orderVModel, List<OrderItem> orderItemList,
                                                  String[] areaParseResult,User user, Date createDate) {
        OrderCondition condition = new OrderCondition();
        condition.setUserName(orderVModel.getUserName());
        String userMobile = StringUtils.isNotBlank(orderVModel.getUserMobile()) ? orderVModel.getUserMobile() : orderVModel.getUserPhone();
        condition.setPhone1(userMobile);
        condition.setPhone2(StringUtils.isNotBlank(orderVModel.getUserPhone()) ? orderVModel.getUserPhone() : "");
        condition.setServicePhone(userMobile);
        condition.setCustomer(orderVModel.getCustomer());
        condition.setCreateDate(createDate);
        condition.setTotalQty(orderVModel.getProductQty());
        Area area = new Area(0L);
        Area subArea = new Area(0L);
        if (areaParseResult != null && areaParseResult.length > 0) {
//            if (areaParseResult[0] != null) {
//                area = new Area(StringUtils.toLong(areaParseResult[0]));
                area.setId(StringUtils.toLong(areaParseResult[0]));
//            }
        /*
        // mark on 2019-5-21
        if (areaParseResult.length > 1) {
            area.setFullName(areaParseResult[1].trim());
            area.setName(areaParseResult[1].trim());
            condition.setAreaName(areaParseResult[1].trim());
        }
        if (areaParseResult.length > 2) {
            condition.setAddress(areaParseResult[2]);
        }
        */
            // add on 2019-5-21 begin

            if (areaParseResult.length > 1) {
//                if (StringUtils.isNotEmpty(areaParseResult[1])) {
//                    subArea = new Area(StringUtils.toLong(areaParseResult[1]));  // 获取4级街道   //2019-5-21
//                }
                subArea.setId(StringUtils.toLong(areaParseResult[1]));
            }
            if (areaParseResult.length > 2) {
                area.setFullName(StringUtils.toString(areaParseResult[2]).trim());
                area.setName(StringUtils.toString(areaParseResult[2]).trim());
                condition.setAreaName(StringUtils.toString(areaParseResult[2]).trim());
            }
            if (areaParseResult.length > 3) {
                condition.setAddress(StringUtils.toString(areaParseResult[3]));
            }

        }
        // add on 2019-5-21 end
        condition.setArea(area);
        condition.setSubArea(subArea);
        /* 省/市id 2019-09-25 */
        Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(area.getId());
        Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
        Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
        condition.setProvinceId(province.getId());
        condition.setCityId(city.getId());

        //condition.setServiceAddress(condition.getAddress());
        condition.setServiceAddress(StringUtils.filterAddress(condition.getAddress()));//详细地址
        condition.setAddress(condition.getServiceAddress());
        condition.setFullAddress(orderVModel.getUserAddress());
//        condition.setCreateBy(user);
        User createBy = MSUserUtils.get(orderVModel.getCreateById());
        if (createBy != null && createBy.getId() != null && createBy.getId() > 0) {
            condition.setCreateBy(createBy);
        } else {
            condition.setCreateBy(user);
        }
        //condition.setKefu(kefu);
        Dict status = getOrderStatus(user);
        condition.setStatus(status);
        if (StringUtils.isNotBlank(orderVModel.getIssueBy())) {
            condition.setCustomerOwner(orderVModel.getIssueBy());
        } else {
            if (createBy != null && createBy.getId() != null && createBy.getId() > 0) {
                condition.setCustomerOwner(createBy.getName());
            } else {
                condition.setCustomerOwner(user.getName());
            }
        }
        // 在外部赋值
        //int orderServiceType = Order.isInstallation(orderItemList) ? Order.ORDER_SERVICE_TYPE_INSTALLATION : Order.ORDER_SERVICE_TYPE_MAINTENANCE;
        //condition.setOrderServiceType(orderServiceType);

        int hasSet = 0;
        Set<String> productIds = Sets.newHashSet();
        Long categoryId = null;
        Set<String> serviceTypeIds = Sets.newHashSet();
        for (OrderItem item : orderItemList) {
            if (item.getProduct().getSetFlag() == 1) {
                hasSet = 1;
            }
            serviceTypeIds.add(String.format(",%s,", item.getServiceType().getId()));
            productIds.add(String.format(",%s,", item.getProduct().getId()));
            if(categoryId == null){
                categoryId = item.getProduct().getCategory().getId();
            }
        }
        condition.setHasSet(hasSet);
        condition.setProductIds(String.join(",", productIds).replace(",,,", ",,"));
        condition.setProductCategoryId(categoryId==null?0L:categoryId);
        condition.setServiceTypes(String.join(",", serviceTypeIds).replace(",,,", ",,"));

        return condition;
    }
    //endregion 内部方法

    //region 公开方法

    /**
     * 将B2B工单转成快可立工单
     *
     * @throws IncompleteB2BOrderException
     * @throws com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderExistsException
     * @throws AddressParseFailureException
     * @throws B2BOrderTranserFailureException
     */
    public Order toOrderAuto(B2BOrderVModel orderVModel, User user) {
        Assert.notNull(orderVModel, "没有需要转换的工单");
        Assert.isTrue(user != null && user.getId() != null && user.getId() > 0, "操作人为空，请检查是否登录超时");

        validateBasicProperties(orderVModel); /* 检查B2B工单的必要属性是否有值 */
//        validateB2BOrderIsExists(orderVModel.getOrderNo(), orderVModel.getDataSource()); /* 检查B2B工单是否已被转入系统 */
        validateB2BOrderIsExistsNew(orderVModel.getB2bOrderId(), orderVModel.getOrderNo(), orderVModel.getDataSource()); /* 检查B2B工单是否已被转入系统 */
        String[] areaParseResult = parseAddressNew(orderVModel.getUserAddress());   /* 解析B2B工单的服务地址 */  //add on 2019-5-21
        Customer customer = getCustomer(orderVModel); /* 获取B2B工单所属客户的财务信息 */
        Map<String,Object> map = createOrderItems(orderVModel, customer.getId());/* 创建工单的OrderItem */
        int orderServiceType = (Integer)map.get("orderServiceType");
        List<OrderItem> orderItemList = (List<OrderItem>)map.get("items");
        StringBuilder content = new StringBuilder();
        content.append("师傅，在您附近有一张  ");
        Double totalCharge = 0.00;
        Double blockedCharge = 0.00;
        Long categoryId = null;
        for (OrderItem item : orderItemList) {
            if(categoryId == null){
                categoryId = item.getProduct().getCategory().getId();
            }
            totalCharge = totalCharge + item.getCharge();
            blockedCharge = blockedCharge + item.getBlockedCharge();
            content.append(item.getServiceType().getName()).append(item.getBrand()).append(item.getProduct().getName());
        }
        content.append("的工单，请尽快登陆APP接单~");

        validateCustomerBalance(orderVModel, totalCharge, blockedCharge);/* 检查客户的账户余额 */
        Area area = new Area(Long.valueOf(areaParseResult[0]));

//        String orderNo = generateOrderNo();
        String orderNo = generateOrderNoNew(orderVModel.getDataSource(), orderVModel.getKklOrderNo());

        //Order
//        Date createDate = new Date();
        Date createDate = generateOrderCreateDate(orderVModel.getDataSource(), orderVModel.getCreateDt());
        Order order = createOrder(orderVModel, totalCharge, blockedCharge, createDate); /* 创建工单的Order、OrderStatus、OrderFee */
        order.setAppMessage(content.toString());
        order.setOrderNo(orderNo);
        User createBy = MSUserUtils.get(orderVModel.getCreateById());
        if (createBy != null && createBy.getId() != null && createBy.getId() > 0) {
            order.setCreateBy(createBy);
        } else {
            order.setCreateBy(user);
        }
        //OrderCondition
        OrderCondition condition = createOrderCondition(orderVModel, orderItemList, areaParseResult, user, createDate);/* 创建工单的OrderCondition */
        int canRush = 0;
        int kefuType = 0;
        //vip客户，不检查突击区域 ， 街道id小于等于3也不检查突击区域 2020-06-20 Ryan
        long subAreaId = Optional.ofNullable(condition.getSubArea()).map(t->t.getId()).orElse(0l);
        /*if(customer.getVipFlag()==1){
            kefuType = OrderCondition.VIP_KEFU_TYPE;
        }else{ //有街道
            canRush = orderService.isCanRush(categoryId==null?0L:categoryId,condition.getCityId(),area.getId(),subAreaId);
            kefuType = orderService.getKefuType(categoryId==null?0L:categoryId,condition.getCityId(),area.getId(),subAreaId);
        }*/
        OrderKefuTypeRuleEnum orderKefuTypeRuleEnum = orderService.getKefuType(categoryId==null?0L:categoryId,condition.getCityId(),area.getId(),subAreaId,customer.getVipFlag(),customer.getVip());
        kefuType = orderKefuTypeRuleEnum.getCode();
        if(kefuType==OrderCondition.RUSH_KEFU_TYPE){
            canRush = 1;
        }
        User kefu = getRandomKefu(orderVModel.getCustomer().getId(), area.getId(),categoryId==null?0L:categoryId,kefuType,condition.getCityId(),condition.getProvinceId());/* 随机获取客服 */
        condition.setKefu(kefu);
        condition.setOrderServiceType(orderServiceType);
        condition.setOrderId(order.getId());
        condition.setOrderNo(orderNo);
        condition.setQuarter(order.getQuarter());
        condition.setCanRush(canRush);
        condition.setKefuType(kefuType);
        order.setOrderCondition(condition);
        //地理信息 2019-04-15
        OrderLocation location = new OrderLocation(order.getId(), order.getQuarter());
        location.setArea(condition.getArea());
        // add on 2019-5-21 begin
        if (areaParseResult != null && areaParseResult.length == 9 && areaParseResult[4].equals("1")) {
            location.setLongitude(StringUtils.toDouble(areaParseResult[7])); //经度
            location.setLatitude(StringUtils.toDouble(areaParseResult[8])); //维度
        }
        // add on 2019-5-21 end
        order.setOrderLocation(location);
        //OrderItem
        for (OrderItem item : orderItemList) {
            item.setOrderId(order.getId());
            item.setQuarter(order.getQuarter());
        }
        order.setItems(orderItemList);
        return order;
    }

    //endregion 公开方法
}
