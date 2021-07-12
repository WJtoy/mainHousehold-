package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePointEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.ObjectUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePrices;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.DictUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointPriceService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * add on 2020-2-25
 */
@Service
@Slf4j
public class ServicePointPriceService {
    @Autowired
    private MSServicePointPriceService msServicePointPriceService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private ProductPriceService productPriceService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private MSDictService msDictService;


    /**
     * 获取网点价格列表
     *
     * @param servicePointIds
     * @param productId
     * @param serviceTypeId
     * @return
     */
    public List<ServicePrice> findPricesList(List<Long> servicePointIds, Long productId, Long serviceTypeId) {
        //1. 根据网点id列表查询是否要使用自定义价格的网点。
        //2. 使用默认价格的网点id列表
        return Lists.newArrayList();
    }


    public List<ServicePrice> getPricesNew(Long servicePointId, Long productId, Integer delFlag,Integer priceType) {
        // add on 2019-12-20  // 用来取代getPrices()方法
        if (servicePointId == null || servicePointId <= 0){
            throw new RuntimeException("读取网点价格异常：无网点id-" + (servicePointId==null?"null":servicePointId.toString()));
        }
        ServicePrice servicePrice = new ServicePrice();
        servicePrice.setServicePoint(new ServicePoint(servicePointId));
        servicePrice.setProduct(new Product(productId));
        servicePrice.setDelFlag(delFlag);
        servicePrice.setPriceType(new Dict(String.valueOf(priceType)));

        //return msServicePointPriceService.findPricesList(servicePrice);

        List<ServicePrice> servicePriceList = msServicePointPriceService.findPricesList(servicePrice);
        // add on 2020-3-13 beign
//        ServicePoint servicePoint = servicePointService.getSimple(servicePointId);
//        if (servicePoint != null) {
//            int customizePriceFlag = servicePoint.getCustomizePriceFlag();
//            boolean startWithYH = servicePoint.getServicePointNo().toUpperCase().startsWith("YH");
//            if ( !startWithYH && customizePriceFlag != ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED ) {
//                List<ServicePrice> standardServicePriceList = msServicePointPriceService.findStandardPricesByProductIdAndPriceType(servicePointId, productId, servicePoint.getUseDefaultPrice());
//                servicePriceListCompare(servicePriceList, standardServicePriceList, "getPricesNew");
//            }
//        }
        // add on 2020-3-13 end
        return servicePriceList;
    }

    /**
     * 分页查询（后台管理）所有产品，即使没有维护价格
     * 先从数据库返回网点id,再根据id从数据库读取
     * 保存在map属性中
    */
    public Page<ServicePrice> findPage(Page<ServicePrice> page, ServicePrice entity,Integer serviceRemotePriceFlag) {
        HashMap<String, List<HashMap<String, Object>>> servicePointPriceListMap = new HashMap<>();
        List<HashMap<String, Object>> servicePointProductPriceList = Lists.newArrayList();
        HashMap<String, Object> servicePointProductPriceMap;
        List<HashMap<String, Object>> servicePointPriceList;
        HashMap<String, Object> servicePointPriceMap;
        List<ServicePrice> servicePriceList;

        entity.setPage(page);
        final String useDefaultPrice = String.valueOf(entity.getServicePoint().getUseDefaultPrice());

        final String remotePriceType = String.valueOf(entity.getServicePoint().getRemotePriceType());

//        List<ServicePointProduct> servicePointProductsAll = dao.getServicePointProductsByIds(entity); // mark on 2019-8-22
        //List<ServicePointProduct> servicePointProductsAll = getServicePointProductsByIds(entity);      //add on 2019-8-22  // mark on 2020-1-2
        // add on 2019-12-18 begin
        List<ServicePointProduct> servicePointProductsAll = servicePointService.getServicePointProductsByIdsNew(entity);      //add on 2019-12-18
        //getServicePointProductsByIdsCompare(servicePointProductsAll, servicePointProductsAllNew);       //mark on 2020-1-2
        // add on 2019-12-18 end

        List<Long> servicePointIds = servicePointProductsAll.stream()
                .map(t -> t.getServicePoint().getId())
                .distinct()
                .collect(Collectors.toList());

        //List<ServicePrice> prices = dao.getPricesByPoints(servicePointIds, entity.getProduct().getId(), null); //mark on 2020-3-4 web端去md_servicepoint_price
        // 通过网点列表获取自定义价格列表
        List<ServicePrice> prices;
        if(serviceRemotePriceFlag == 0){
            prices = msServicePointPriceService.findPricesByPointsAndPriceType(servicePointIds, entity.getProduct().getId(), null,entity.getServicePoint().getUseDefaultPrice()); // add on 2019-12-23
        }else {
            prices = msServicePointPriceService.findPricesByPointsAndPriceType(servicePointIds, entity.getProduct().getId(), null,entity.getServicePoint().getRemotePriceType()); // add on 2019-12-23
        }

        //getPricesCompare("servicePointService.findPage", 0L, prices, pricesFromMS);   // add on 2019-12-23  //mark on 2020-3-4


        //产品参考价格
        // 该网点下的产品
        List<Long> productIds = servicePointProductsAll.stream()
                .map(t -> t.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        // 通过产品id和网点id获取标准价
        List<ProductPrice> productPrices;
        if(serviceRemotePriceFlag == 0){
            productPrices = productPriceService.findGroupList(productIds, null, entity.getServicePoint().getUseDefaultPrice(), entity.getServicePoint().getId(), null);
        }else {
            productPrices = productPriceService.findGroupList(productIds, null, entity.getServicePoint().getRemotePriceType(), entity.getServicePoint().getId(), null);
        }

        // add on 2020-3-13 begin
        //findServicePointPricesByServicePointId(servicePointIds, entity.getProduct().getId(), productIds, prices);
        // add on 2020-3-13 end
        //mark on 2019-10-11
        //List<ServiceType> serviceTypes = typeService.findAllList();
        //调用微服务获取服务类型,只返回id 和服务名称 start 2019-10-11
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
        //end
        serviceTypes = ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes);//服务类型

        ServicePoint servicePoint = null;
        Product product;
        ProductPrice productPrice;
        ServicePrice price;

        for (Long servicePointId : servicePointIds) {
            final Long spi = servicePointId;
            servicePoint = servicePointProductsAll.stream()
                    .filter(spp -> Objects.equals(spp.getServicePoint().getId(), spi))
                    .findFirst().orElse(null).getServicePoint();

            servicePointProductPriceMap = new HashMap<>();
            servicePointPriceList = Lists.newArrayList();

            servicePointProductPriceMap.put("servicePointId", servicePoint.getId());
            servicePointProductPriceMap.put("servicePointNo", servicePoint.getServicePointNo());
            servicePointProductPriceMap.put("servicePointName", servicePoint.getName());

            List<ServicePointProduct> servicePointProducts = servicePointProductsAll.stream()
                    .filter(t -> Objects.equals(t.getServicePoint().getId(), spi))
                    .collect(Collectors.toList());

            for (ServicePointProduct servicePointProduct : servicePointProducts) {
                product = servicePointProduct.getProduct();
                servicePointPriceMap = new HashMap<>();
                servicePriceList = Lists.newArrayList();
                final Long productId = product.getId();

                servicePointPriceMap.put("productId", product.getId());
                servicePointPriceMap.put("productName", product.getName());

                for (ServiceType serviceType : serviceTypes) {// 遍历服务项目
                    final Long serviceTypeId = serviceType.getId();
                    // 如果该网点使用的是自定义价格
                        if(serviceRemotePriceFlag == 0){
                            if (entity.getServicePoint().getCustomizePriceFlag() == 1) {
                                //已有价格
                                price = prices.stream()
                                        .filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                                && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                                && Objects.equals(t.getServicePoint().getId(), spi))
                                        .findFirst().orElse(null);
                                //标准价格
                                productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                        && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                        && Objects.equals(t.getPriceType().getValue(), useDefaultPrice))
                                        .findFirst().orElse(null);
                                // 筛选的自定义价不为空跳出循环
                                if (price != null) { //维护
                                    price.setFlag(0);
                                    price.setReferPrice(Optional.ofNullable(productPrice).map(ProductPrice :: getEngineerStandardPrice).orElse(0.0));
                                    price.setReferDiscountPrice(Optional.ofNullable(productPrice).map(ProductPrice :: getEngineerDiscountPrice).orElse(0.0));
                                    servicePriceList.add(price);
                                    continue;
                                }

                                // 没有自定义价格则获取产品的标准价
                                if (productPrice != null) {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setReferPrice(Optional.ofNullable(productPrice.getEngineerStandardPrice()).orElse(0.0));
                                    price.setReferDiscountPrice(Optional.ofNullable(productPrice.getEngineerDiscountPrice()).orElse(0.0));
                                    price.setFlag(1);//有参考价格
                                } else {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setFlag(2);//无参考价格
                                }
                                servicePriceList.add(price);
                            } else {
                                //标准价格
                                productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                        && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                        && Objects.equals(t.getPriceType().getValue(), useDefaultPrice))
                                        .findFirst().orElse(null);
                                if (productPrice != null) {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setReferPrice(Optional.ofNullable(productPrice.getEngineerStandardPrice()).orElse(0.0));
                                    price.setReferDiscountPrice(Optional.ofNullable(productPrice.getEngineerDiscountPrice()).orElse(0.0));
                                    price.setFlag(1);//有参考价格
                                } else {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setFlag(2);//无参考价格
                                }
                                servicePriceList.add(price);
                            }
                        }else {
                            if (entity.getServicePoint().getRemotePriceFlag() == 1) {
                                //已有价格
                                price = prices.stream()
                                        .filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                                && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                                && Objects.equals(t.getServicePoint().getId(), spi))
                                        .findFirst().orElse(null);
                                //标准价格
                                productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                        && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                        && Objects.equals(t.getPriceType().getValue(), remotePriceType))
                                        .findFirst().orElse(null);
                                // 筛选的自定义价不为空跳出循环
                                if (price != null) { //维护
                                    price.setFlag(0);
                                    price.setReferPrice(Optional.ofNullable(productPrice).map(ProductPrice :: getEngineerStandardPrice).orElse(0.0));
                                    price.setReferDiscountPrice(Optional.ofNullable(productPrice).map(ProductPrice :: getEngineerDiscountPrice).orElse(0.0));
                                    servicePriceList.add(price);
                                    continue;
                                }

                                // 没有自定义价格则获取产品的标准价
                                if (productPrice != null) {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setReferPrice(Optional.ofNullable(productPrice.getEngineerStandardPrice()).orElse(0.0));
                                    price.setReferDiscountPrice(Optional.ofNullable(productPrice.getEngineerDiscountPrice()).orElse(0.0));
                                    price.setFlag(1);//有参考价格
                                } else {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setFlag(2);//无参考价格
                                }
                                servicePriceList.add(price);
                            } else {
                                //标准价格
                                productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                        && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                        && Objects.equals(t.getPriceType().getValue(), remotePriceType))
                                        .findFirst().orElse(null);
                                if (productPrice != null) {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setReferPrice(Optional.ofNullable(productPrice.getEngineerStandardPrice()).orElse(0.0));
                                    price.setReferDiscountPrice(Optional.ofNullable(productPrice.getEngineerDiscountPrice()).orElse(0.0));
                                    price.setFlag(1);//有参考价格
                                } else {
                                    price = new ServicePrice();
                                    price.setServiceType(serviceType);
                                    price.setFlag(2);//无参考价格
                                }
                                servicePriceList.add(price);
                            }
                        }

                }
                servicePointPriceMap.put("servicePriceList", servicePriceList);
                servicePointPriceList.add(servicePointPriceMap);
            }

            servicePointProductPriceMap.put("servicePointPriceList", servicePointPriceList);
            servicePointProductPriceList.add(servicePointProductPriceMap);
        }
        servicePointPriceListMap.put("list", servicePointProductPriceList);
        page.setMap(servicePointPriceListMap);
        return page;
    }

    public List<ProductPrice>  findServicePointPricesByServicePointId(List<Long> servicePointIds, Long productId, List<Long> productIds, List<ServicePrice> sourceServicePriceList) {
        //  用来获取网点价格数据  // add on 2020-3-13
        List<ServicePoint> servicePointList = Lists.newArrayList();
        if (!org.springframework.util.ObjectUtils.isEmpty(servicePointIds)){
            servicePointList = msServicePointService.findServicePointNoAndCustomizePriceFlagAndUseDefaultPriceListByIds(servicePointIds);
        }
        List<Long> customizePriceServicePointIds = Lists.newArrayList();
        List<ServicePoint> standardPriceServicePoint = Lists.newArrayList();
        if (servicePointList != null && !servicePointList.isEmpty()) {
            for(ServicePoint servicePoint: servicePointList) {
                if (servicePoint.getServicePointNo().toUpperCase().startsWith("YH") || servicePoint.getCustomizePriceFlag().intValue() == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED) {
                    customizePriceServicePointIds.add(servicePoint.getId());
                } else {
                    standardPriceServicePoint.add(servicePoint);
                }
            }
        }

        // 获取自定义价格或YH开头的网点列表
        List<ServicePrice> customizeServicePirceList = msServicePointPriceService.findPricesByPoints(servicePointIds, productId, null);
        List<ServicePrice> standardServicePriceList = Lists.newArrayList();
        if (standardPriceServicePoint != null && !standardPriceServicePoint.isEmpty()) {
            standardPriceServicePoint.stream().forEach(p->{
                List<ServicePrice> singleServicePointPrices = msServicePointPriceService.findStandardServicePointPricesByPoints(p.getId(), p.getUseDefaultPrice(),productIds);
                if (singleServicePointPrices != null && !singleServicePointPrices.isEmpty()) {
                    standardServicePriceList.addAll(singleServicePointPrices);
                }
            });

            // 过滤要比较的数据 begin
            if (sourceServicePriceList != null && !sourceServicePriceList.isEmpty()) {
                List<ServicePrice> filterSourceServicePrices = Lists.newArrayList();
                for(ServicePoint servicePoint: standardPriceServicePoint) {
                    List<ServicePrice> queryServicePriceList  = sourceServicePriceList.stream().filter(p->p.getServicePoint().getId().longValue() == servicePoint.getId() && p.getPriceType().getIntValue().intValue() == servicePoint.getUseDefaultPrice() && productIds.contains(p.getId())).collect(Collectors.toList());
                    //List<ServicePrice> queryServicePriceList  = sourceServicePriceList.stream().filter(p->p.getServicePoint().getId().longValue() == servicePoint.getId() && p.getPriceType().getIntValue().intValue() == servicePoint.getUseDefaultPrice()).collect(Collectors.toList());
                    if (queryServicePriceList != null && !queryServicePriceList.isEmpty()) {
                        filterSourceServicePrices.addAll(queryServicePriceList);
                    }
                }
                servicePriceListCompare(filterSourceServicePrices, standardServicePriceList, "findServicePointPricesByServicePointId");
            }
            // 过滤要比较的数据 end
        }

        return Lists.newArrayList();
    }

    public void servicePriceListCompare(List<ServicePrice> sourceList, List<ServicePrice> standardList, String methodName) {
        try {
            // 数据比较   // add on 2020-3-13
            StringBuilder stringBuilder = new StringBuilder();
            // 以网点价格表中的数据为准
            for(ServicePrice sourceServicePrice :sourceList) {
                ServicePrice standardServicePrice= standardList.stream().filter(p->p.getServicePoint().getId().longValue() == sourceServicePrice.getServicePoint().getId().longValue()
                && p.getProduct().getId().longValue() == sourceServicePrice.getProduct().getId().longValue()
                && p.getPriceType().getIntValue().intValue() == sourceServicePrice.getPriceType().getIntValue().intValue()
                && p.getServiceType().getId().longValue() == sourceServicePrice.getServiceType().getId().longValue()).findFirst().orElse(null);

                if (standardServicePrice != null) {
                    if (standardServicePrice.getPrice() != sourceServicePrice.getPrice()
                            || standardServicePrice.getDiscountPrice() != sourceServicePrice.getDiscountPrice()
                            || standardServicePrice.getDelFlag().intValue() != sourceServicePrice.getDelFlag().intValue() ) {
                        stringBuilder.append("servicePointId：").append(sourceServicePrice.getServicePoint().getId())
                                .append(",productId:").append(sourceServicePrice.getProduct().getId())
                                .append(",serviceTypeId:").append(sourceServicePrice.getServiceType().getId())
                                .append(",priceType:").append(sourceServicePrice.getPriceType().getValue())
                                .append(",servicePointPrice:").append(sourceServicePrice.getPrice())
                                .append(",servicePointDiscountPrice:").append(sourceServicePrice.getDiscountPrice())
                                .append(",engineerPrice:").append(standardServicePrice.getPrice())
                                .append(",engineerDiscountPrice:").append(standardServicePrice.getDiscountPrice())
                                .append(",servicePointDelFlag:").append(sourceServicePrice.getDelFlag())
                                .append(",engineerDelFlag:").append(standardServicePrice.getDelFlag())
                                .append(";\n");
                    }
                } else {
                    stringBuilder.append("servicePointId：").append(sourceServicePrice.getServicePoint().getId())
                            .append(",productId:").append(sourceServicePrice.getProduct().getId())
                            .append(",serviceTypeId:").append(sourceServicePrice.getServiceType().getId())
                            .append(",priceType:").append(sourceServicePrice.getPriceType().getValue())
                            .append(",servicePointPrice:").append(sourceServicePrice.getPrice())
                            .append(",servicePointDiscountPrice:").append(sourceServicePrice.getDiscountPrice())
                            .append(",servicePointDelFlag:").append(sourceServicePrice.getDelFlag())
                            .append(",参考价格不存在;\n");
                }
            }

            for(ServicePrice standardServicePrice :standardList) {
                ServicePrice sourceServicePrice = sourceList.stream().filter(p->p.getServicePoint().getId().longValue() == standardServicePrice.getServicePoint().getId().longValue()
                        && p.getProduct().getId().longValue() == standardServicePrice.getProduct().getId().longValue()
                        && p.getPriceType().getIntValue().intValue() == standardServicePrice.getPriceType().getIntValue().intValue()
                        && p.getServiceType().getId().longValue() == standardServicePrice.getServiceType().getId().longValue()).findFirst().orElse(null);
                if (sourceServicePrice != null) {
                    if (sourceServicePrice.getPrice() != standardServicePrice.getPrice()
                            || sourceServicePrice.getDiscountPrice() != standardServicePrice.getDiscountPrice()
                            || sourceServicePrice.getDelFlag().intValue() != standardServicePrice.getDelFlag().intValue() ) {
                        stringBuilder.append("servicePointId：").append(standardServicePrice.getServicePoint().getId())
                                .append(",productId:").append(standardServicePrice.getProduct().getId())
                                .append(",serviceTypeId:").append(standardServicePrice.getServiceType().getId())
                                .append(",priceType:").append(standardServicePrice.getPriceType().getValue())
                                .append(",engineerPrice:").append(standardServicePrice.getPrice())
                                .append(",engineerDiscountPrice:").append(standardServicePrice.getDiscountPrice())
                                .append(",servicePointPrice:").append(sourceServicePrice.getPrice())
                                .append(",servicePointDiscountPrice:").append(sourceServicePrice.getDiscountPrice())
                                .append(",servicePointDelFlag:").append(sourceServicePrice.getDelFlag())
                                .append(",engineerDelFlag:").append(standardServicePrice.getDelFlag())
                                .append(";\n");
                    }
                } else {
                    stringBuilder.append("servicePointId：").append(standardServicePrice.getServicePoint().getId())
                            .append(",productId:").append(standardServicePrice.getProduct().getId())
                            .append(",serviceTypeId:").append(standardServicePrice.getServiceType().getId())
                            .append(",priceType:").append(standardServicePrice.getPriceType().getValue())
                            .append(",engineerPrice:").append(standardServicePrice.getPrice())
                            .append(",engineerDiscountPrice:").append(standardServicePrice.getDiscountPrice())
                            .append(",engineerDelFlag:").append(standardServicePrice.getDelFlag())
                            .append(",网点价格不存在;\n");
                }
            }
            if (stringBuilder.length() >0 ){
                LogUtils.saveLog("基础资料", methodName, stringBuilder.toString(), null, UserUtils.getUser());
                //log.warn("价格比较：{}", stringBuilder.toString() );
            }
        } catch (Exception ex) {
        }
    }


    /**
     * 保存网点某产品的所有安维价格（后台）
     *
     * @param servicePrices
     */
    @Transactional(readOnly = false)
    public void saveProductPrices(ServicePrices servicePrices ,Integer serviceRemotePriceFlag) {
        ServicePoint servicePoint = servicePrices.getServicePoint();
        // add on 2020-3-12 begin
        ServicePoint servicePointFromMS = msServicePointService.getSimpleById(servicePoint.getId());
        if (servicePointFromMS == null) {
            throw new RuntimeException("获取网点信息失败");
        }
        int priceType;
        int customizePriceFlag;
        int priceTypeFlag;
        if(serviceRemotePriceFlag == 0){
            priceType = servicePointFromMS.getUseDefaultPrice();
            customizePriceFlag  = servicePointFromMS.getCustomizePriceFlag();
            priceTypeFlag = MDServicePointEnum.PriceTypeFlag.SERVICEPRICE.getValue();
        }else {
            priceType = servicePointFromMS.getRemotePriceType();
            customizePriceFlag  = servicePointFromMS.getRemotePriceFlag();
            priceTypeFlag = MDServicePointEnum.PriceTypeFlag.REMOTEPRICE.getValue();
        }
        String strServicepointNo = servicePointFromMS.getServicePointNo();
        boolean startWithYH = strServicepointNo.toUpperCase().startsWith("YH");
        boolean degree = servicePointFromMS.getDegree().equals(30);
        //Dict priceType = DictUtils.getDictByValue(useDefaultPrice+"","PriceType");  // mark on 2020-6-3  //这里取不到数据了
        Dict priceTypeDict =   msDictService.getDictByValue(String.valueOf(priceType), "PriceType");     // add on 2020-6-3
        int customizePriceFlagAtLive = servicePointFromMS.getCustomizePriceFlag();
        // add on 2020-3-12 end

        ProductPrice productPrice;
        // add on 2019-12-20 begin
        List<ServicePrice> delServicePriceList = Lists.newArrayList();
        List<ServicePrice> saveServicePriceList = Lists.newArrayList();
        // add on 2019-12-20 end

        StringBuilder stringBuilder = new StringBuilder();
        List<ProductPrice> allPrices = productPriceService.findGroupList(Lists.newArrayList(servicePrices.getProduct().getId()), null, null, servicePoint.getId(), null);
        for (ServicePrice p : servicePrices.getPrices()) {
            if (p.getId() != null) {
                if (p.getDelFlag() == 1) {
                    //不会运行下面代码，因为界面传过来的数据没有delFlag等于1的数据。//comment on 2019-12-21
                    //dao.deletePrice(p.getId());  //mark on 2020-3-4
                    p.setServicePoint(servicePoint); //add on 2019-12-20
                    delServicePriceList.add(p); // add on 2019-12-20
                } else {
                    // add on 2020-2-24 begin
                    p.setPriceType(priceTypeDict);

                    productPrice = allPrices.stream().filter(t -> Objects.equals(t.getServiceType().getId(), p.getServiceType().getId())
                            && Objects.equals(t.getPriceType().getIntValue(), p.getPriceType().getIntValue())).findFirst().orElse(null);
                    if (productPrice != null) {
                        if (p.getPrice() != productPrice.getEngineerStandardPrice().doubleValue() || p.getDiscountPrice() != productPrice.getEngineerDiscountPrice().doubleValue()) {
                            p.setCustomizeFlag(ServicePrice.CUSTOMIZE_FLAG_ENABLED);
                            customizePriceFlagAtLive = ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED;
                            ServiceType serviceType = serviceTypeService.getFromCache(p.getServiceType().getId());
                            if (serviceType != null) {
                                stringBuilder.append("<br>")
                                        .append(serviceType.getName())
                                        .append(" 的网点价:")
                                        .append(p.getPrice())
                                        .append(",网点优惠价：")
                                        .append(p.getDiscountPrice())
                                        .append(";")
                                        .append("参考价：")
                                        .append(productPrice.getEngineerStandardPrice())
                                        .append(",参考优惠价：")
                                        .append(productPrice.getEngineerDiscountPrice());
                            }
                        }
                    }
                    // add on 2020-2-24 end
                    p.setProduct(servicePrices.getProduct());
                    //p.setDelFlag(2);//待审核
                    p.setUpdateBy(servicePrices.getCreateBy());
                    p.setUpdateDate(servicePrices.getCreateDate());
                    //dao.updatePrice(p);  //mark on 2020-3-4
                    p.setServicePoint(servicePoint); //add on 2019-12-20
                    saveServicePriceList.add(p); // add on 2019-12-20
                }
            } else {
                //new
                if (p.getDelFlag() == 1) {//忽略
                    continue;
                }
                //productPrice = allPrices.stream().filter(t -> Objects.equals(t.getServiceType().getId(), p.getServiceType().getId())).findFirst().orElse(null); //mark on 2020-2-25
                // 判断是否存在相同服务类型与价格轮次的参考价格
                p.setPriceType(priceTypeDict);   // add on 2019-12-21
                if (degree || customizePriceFlag == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED) {  //网点编号是YH开头或者是自定义网点价格的网点
                    List<ProductPrice> productPriceList = allPrices.stream().filter(t -> Objects.equals(t.getServiceType().getId(), p.getServiceType().getId())).collect(Collectors.toList()); //add on 2020-2-25
                    if (productPriceList != null && !productPriceList.isEmpty()) {
                        p.setProduct(servicePrices.getProduct());
                        p.setServicePoint(servicePoint);
                        p.setCreateBy(servicePrices.getCreateBy());
                        p.setCreateDate(servicePrices.getCreateDate());
                        // add on 2020-2-24 begin
                        ProductPrice filterProductPrice = productPriceList.stream().filter(t ->Objects.equals(t.getPriceType().getIntValue(), p.getPriceType().getIntValue())).findFirst().orElse(null);
                        if (filterProductPrice != null) {
                            if (p.getPrice() != filterProductPrice.getEngineerStandardPrice().doubleValue() || p.getDiscountPrice() != filterProductPrice.getEngineerDiscountPrice().doubleValue()) {
                                p.setCustomizeFlag(ServicePrice.CUSTOMIZE_FLAG_ENABLED);
                                customizePriceFlagAtLive = ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED;
                            }
                        }
                        // add on 2020-2-24 end
                        //dao.insertPrice(p);        // mark on 2020-3-4 web端去md_servicepoint_
                        p.setIsNewRecord(true);      // add on 2019-12-20
                        saveServicePriceList.add(p); // add on 2019-12-20
                    }
                } else  {
                    ProductPrice filterProductPrice = allPrices.stream().filter(t -> t.getServiceType().getId().longValue() == p.getServiceType().getId().longValue() && t.getPriceType().getIntValue().intValue() == p.getPriceType().getIntValue().intValue()).findFirst().orElse(null); //add on 2020-2-25
                    if (filterProductPrice != null) {
                        p.setProduct(servicePrices.getProduct());
                        p.setServicePoint(servicePoint);
                        p.setCreateBy(servicePrices.getCreateBy());
                        p.setCreateDate(servicePrices.getCreateDate());
                        p.setIsNewRecord(true);      // add on 2019-12-20
                        saveServicePriceList.add(p); // add on 2019-12-20

                        if (p.getPrice() != filterProductPrice.getEngineerStandardPrice().doubleValue() || p.getDiscountPrice() != filterProductPrice.getEngineerDiscountPrice().doubleValue()) {
                            p.setCustomizeFlag(ServicePrice.CUSTOMIZE_FLAG_ENABLED);
                            customizePriceFlagAtLive = ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED;

                            ServiceType serviceType = serviceTypeService.getFromCache(p.getServiceType().getId());
                            if (serviceType != null) {
                                stringBuilder.append("<br>")
                                        .append(serviceType.getName())
                                        .append(" 的网点价:")
                                        .append(p.getPrice())
                                        .append(",网点优惠价：")
                                        .append(p.getDiscountPrice())
                                        .append(";")
                                        .append("参考价：")
                                        .append(filterProductPrice.getEngineerStandardPrice())
                                        .append(",参考优惠价：")
                                        .append(filterProductPrice.getEngineerDiscountPrice());
                            }
                        }
                    }
                }
            }
        }

        // add on 2019-12-20 begin
        if (customizePriceFlag == ServicePoint.CUSTOMIZE_PRICE_FLAG_DISABLED   // 标准价
                && customizePriceFlagAtLive == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED  // 自定义价
                && !degree) {  // 非返现网点
            throw new RuntimeException( "<br>网点现在使用[ "+ priceTypeDict.getLabel()+" ]价格，不能修改。" + stringBuilder.toString());
        }
        if (!saveServicePriceList.isEmpty()) {
            if (degree && customizePriceFlag == ServicePoint.AUTO_PLAN_FLAG_DISABLED) {
                saveServicePriceList.forEach(p -> {
                    p.setIsNewRecord(true);
                    p.setId(null);
                    p.setCreateBy(servicePrices.getCreateBy());
                    p.setCreateDate(servicePrices.getCreateDate());
                });
            }
            msServicePointPriceService.batchInsertOrUpdateThreeVer(saveServicePriceList,priceTypeFlag);
        }
        if (!delServicePriceList.isEmpty()) {
            msServicePointPriceService.batchDelete(delServicePriceList);
        }
        // add on 2019-12-20 end
        if(serviceRemotePriceFlag == 0){
            servicePointService.updateCustomizePriceFlag(customizePriceFlagAtLive, servicePoint.getId(),priceType);// add on 2020-4-24
        }
    }
}
