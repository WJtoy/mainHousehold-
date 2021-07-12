package com.wolfking.jeesite.modules.finance.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.entity.md.MDServicePointEnum;
import com.kkl.kklplus.entity.md.dto.MDProductDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointProductDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.dao.ServicePointFinanceDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePrices;
import com.wolfking.jeesite.modules.md.service.ProductPriceService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FiServicePointPriceService {

    @Resource
    private ServicePointFinanceDao servicePointFinanceDao;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSServicePointPriceService msServicePointPriceService;

    @Autowired
    private MSProductPriceService msProductPriceService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private MSServicePointProductService msServicePointProductService;

    @Autowired
    private MSDictService msDictService;

    @Autowired
    private FiServicePointService fiServicePointService;

    public Page<ServicePoint> findPricePage(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);
        List<Long> ids = findIdListFromMSWithPrice(entity);

        page.initialize();
        ServicePoint s;
        for (Long id : ids) {
            s = msServicePointService.getById(id);
            Engineer engineer = msEngineerService.getById(s.getPrimary().getId());
            s.getPrimary().setName(Optional.ofNullable(engineer).map(Engineer::getName).orElse(""));
            if (s != null) {
                page.getList().add(s);
            }
        }
        for (ServicePoint item : page.getList()) {
            if(item.getRemotePriceFlag() == null){
                item.setRemotePriceFlag(0);
            }
            if(item.getRemotePriceType() == null){
                item.setRemotePriceType(0);
            }
            if(item.getRemotePriceEnabledFlag() == null){
                item.setRemotePriceEnabledFlag(0);
            }
        }

        return page;
    }

    private List<Long> findIdListFromMSWithPrice(ServicePoint entity) {
        // 从微服务中获取网点id
        List<Long> ids = Lists.newArrayList();
        if (entity.getFinance() != null) {
            if ((entity.getFinance().getInvoiceFlag() != null && entity.getFinance().getInvoiceFlag() >=0) ||
                    (entity.getFinance().getDiscountFlag() != null && entity.getFinance().getDiscountFlag()>=0)) {
                ids = servicePointFinanceDao.findServicePointIdsFromFinance(entity.getFinance());
            }
        }
        return msServicePointService.findIdListWithPrice(entity, ids);
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
        List<ServicePointProduct> servicePointProductsAll = getServicePointProductsByIds(entity);      //add on 2019-12-18

        List<Long> servicePointIds = servicePointProductsAll.stream()
                .map(t -> t.getServicePoint().getId())
                .distinct()
                .collect(Collectors.toList());

        // 通过网点列表获取自定义价格列表
        List<ServicePrice> prices;
        if(serviceRemotePriceFlag == 0){
            prices = msServicePointPriceService.findPricesByPointsAndPriceType(servicePointIds, entity.getProduct().getId(), null,entity.getServicePoint().getUseDefaultPrice()); // add on 2019-12-23
        }else {
            prices = msServicePointPriceService.findPricesByPointsAndPriceType(servicePointIds, entity.getProduct().getId(), null,entity.getServicePoint().getRemotePriceType()); // add on 2019-12-23
        }
        //产品参考价格
        // 该网点下的产品
        List<Long> productIds = servicePointProductsAll.stream()
                .map(t -> t.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        // 通过产品id和网点id获取标准价
        List<ProductPrice> productPrices;
        if(serviceRemotePriceFlag == 0){
            productPrices = msProductPriceService.findGroupList(entity.getServicePoint().getUseDefaultPrice(), productIds, null, entity.getServicePoint().getId(), null);
        }else {
            productPrices = msProductPriceService.findGroupList(entity.getServicePoint().getRemotePriceType(), productIds, null, entity.getServicePoint().getId(), null);
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

    public List<ServicePointProduct>  getServicePointProductsByIds(ServicePrice servicePrice) {
        // add on 2019-12-18
        MDServicePointProductDto mdServicePointProductDto = new MDServicePointProductDto();
        List<ServicePointProduct> servicePointProductList = Lists.newArrayList();
        if (servicePrice.getServicePoint()!= null) {
            MDServicePointDto servicePointDto = new MDServicePointDto();
            servicePointDto.setId(servicePrice.getServicePoint().getId());
            mdServicePointProductDto.setServicePoint(servicePointDto);
        }
        if (servicePrice.getProduct()!= null) {
            MDProductDto productDto = new MDProductDto();
            productDto.setId(servicePrice.getProduct().getId());
            if (servicePrice.getProductCategory()!= null) {
                productDto.setProductCategoryId(servicePrice.getProductCategory().getId());
            }
            mdServicePointProductDto.setProduct(productDto);
        }

        if (servicePrice.getPage() != null) {
            mdServicePointProductDto.setPage(new MSPage<>(servicePrice.getPage().getPageNo(), servicePrice.getPage().getPageSize()));
            servicePointProductList = msServicePointProductService.findList(mdServicePointProductDto);
            servicePrice.getPage().setPageNo(mdServicePointProductDto.getPage().getPageNo());
            servicePrice.getPage().setPageSize(mdServicePointProductDto.getPage().getPageSize());
            servicePrice.getPage().setCount(mdServicePointProductDto.getPage().getRowCount());
        }
        return servicePointProductList;
    }

    public List<ServicePrice> getPrices(Long servicePointId, Long productId, Integer delFlag,Integer priceType) {
        if (servicePointId == null || servicePointId <= 0){
            throw new RuntimeException("读取网点价格异常：无网点id-" + (servicePointId==null?"null":servicePointId.toString()));
        }
        ServicePrice servicePrice = new ServicePrice();
        servicePrice.setServicePoint(new ServicePoint(servicePointId));
        servicePrice.setProduct(new Product(productId));
        servicePrice.setDelFlag(delFlag);
        servicePrice.setPriceType(new Dict(String.valueOf(priceType)));

        List<ServicePrice> servicePriceList = msServicePointPriceService.findPricesList(servicePrice);
//        // add on 2020-3-13 beign
//        ServicePoint servicePoint = msServicePointService.getSimpleById(servicePointId);
//        if (servicePoint != null) {
//            int customizePriceFlag = servicePoint.getCustomizePriceFlag();
//            boolean startWithYH = servicePoint.getServicePointNo().toUpperCase().startsWith("YH");
//            if ( !startWithYH && customizePriceFlag != ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED ) {
//                List<ServicePrice> standardServicePriceList = msServicePointPriceService.findStandardPricesByProductIdAndPriceType(servicePointId, productId, servicePoint.getUseDefaultPrice());
//                servicePriceListCompare(servicePriceList, standardServicePriceList, "getPricesNew");
//            }
//        }
//        // add on 2020-3-13 end
        return servicePriceList;
    }

    /**
     * 保存网点某产品的所有安维价格（后台）
     *
     * @param servicePrices
     */
    @Transactional(readOnly = false)
    public void saveProductPrices(ServicePrices servicePrices,Integer serviceRemotePriceFlag) {
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
        List<ProductPrice> allPrices = msProductPriceService.findGroupList(null, Lists.newArrayList(servicePrices.getProduct().getId()), null, servicePoint.getId(), null);
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
        if (customizePriceFlag == ServicePoint.AUTO_PLAN_FLAG_DISABLED
                && customizePriceFlagAtLive == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED
                && !degree) {
            throw new RuntimeException( "<br>网点现在使用[ "+priceTypeDict.getLabel()+" ]价格，不能修改。" + stringBuilder.toString());
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
        // add on 2019-12-20 en
        if(serviceRemotePriceFlag == 0){
            fiServicePointService.updateCustomizePriceFlag(customizePriceFlagAtLive, servicePoint.getId(), priceType);// add on 2020-2-24
        }
    }

}
