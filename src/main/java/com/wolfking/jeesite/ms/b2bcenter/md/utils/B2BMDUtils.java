package com.wolfking.jeesite.ms.b2bcenter.md.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.b2bcenter.md.*;
import com.kkl.kklplus.entity.common.MSBase;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerShop;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.b2bcenter.md.service.*;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.utils.MSDictUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class B2BMDUtils {

    private static B2BCustomerMappingService customerMappingService = SpringContextHolder.getBean(B2BCustomerMappingService.class);
    private static B2BProductMappingService productMappingService = SpringContextHolder.getBean(B2BProductMappingService.class);
    private static B2BServiceTypeMappingService serviceTypeMappingService = SpringContextHolder.getBean(B2BServiceTypeMappingService.class);
    private static B2BCancelTypeMappingService cancelTypeMappingService = SpringContextHolder.getBean(B2BCancelTypeMappingService.class);
    private static MicroServicesProperties microServicesProperties = SpringContextHolder.getBean(MicroServicesProperties.class);
    private static B2BServiceFeeCategoryService serviceFeeCategoryService = SpringContextHolder.getBean(B2BServiceFeeCategoryService.class);
    private static B2BServiceFeeItemService serviceFeeItemService = SpringContextHolder.getBean(B2BServiceFeeItemService.class);

    private static B2BSurchargeItemMappingService surchargeItemMappingService = SpringContextHolder.getBean(B2BSurchargeItemMappingService.class);
    private static B2BSurchargeCategoryMappingService surchargeCategoryMappingService = SpringContextHolder.getBean(B2BSurchargeCategoryMappingService.class);

    private static CustomerService customerService = SpringContextHolder.getBean(CustomerService.class);

    public static final User B2B_USER = new User(3L, "B2B帐号", "");

    //region B2B店铺与工单系统客户的映射关系

    /**
     * 查询数据源中所有的店铺与客户的对应关系
     *
     * @param dataSource B2BDataSourceEnum
     * @return key为shopId
     */
    public static Map<String, B2BCustomerMapping> getCustomerMappingMap(B2BDataSourceEnum dataSource) {
        List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(dataSource);
        Map<String, B2BCustomerMapping> map = Maps.newHashMap();
        if (list.size() > 0) {
            for (B2BCustomerMapping item : list) {
                map.put(item.getShopId(), item);
            }
        }
        return map;
    }

    /**
     * 获取快可立客户店铺
     */
    public static List<Dict> getCustomerKKLShopList(Long customerId) {
        List<Dict> result = Lists.newArrayList();
        if (customerId != null && customerId > 0) {
            List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(B2BDataSourceEnum.KKL);
            if (!list.isEmpty()) {
                for (B2BCustomerMapping item : list) {
                    if (item.getCustomerId().equals(customerId)) {
                        result.add(new Dict(item.getShopId(), item.getShopName()));
                    }
                }
            }
        }
        return result;
    }

    public static List<Dict> getCustomerKKLShopListNew(Long customerId) {
        List<Dict> result = Lists.newArrayList();
        if (customerId != null && customerId > 0) {
            List<B2BDataSourceEnum> dataSourceList = B2BDataSourceEnum.getAllB2BDataSource();
            dataSourceList.add(0, B2BDataSourceEnum.KKL);
            List<B2BCustomerMapping> list;
            Set<String> keySets = Sets.newHashSet();
            String key;
            for (B2BDataSourceEnum dataSource : dataSourceList) {
                list = customerMappingService.getListByDataSource(dataSource);
                if (list.size() > 0) {
                    for (B2BCustomerMapping item : list) {
                        key = String.format("%s:%s", item.getShopId(), item.getShopName());
                        if (item.getCustomerId().equals(customerId) && !keySets.contains(key)) {
                            keySets.add(key);
                            Dict dict = new Dict(item.getShopId(), item.getShopName());
                            dict.setSort(item.getSaleChannel());//使用sort存储销售渠道 2020-04-27
                            result.add(dict);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<CustomerShop> getCustomerKKLShopListNewForMD(Long customerId) {
        List<CustomerShop> result = Lists.newArrayList();
        if (customerId != null && customerId > 0) {
            List<B2BDataSourceEnum> dataSourceList = B2BDataSourceEnum.getAllB2BDataSource();
            dataSourceList.add(0, B2BDataSourceEnum.KKL);
            List<B2BCustomerMapping> list;
            Set<String> keySets = Sets.newHashSet();
            String key;
            for (B2BDataSourceEnum dataSource : dataSourceList) {
                list = customerMappingService.getListByDataSource(dataSource);
                if (list.size() > 0) {
                    for (B2BCustomerMapping item : list) {
                        key = String.format("%s:%s", item.getShopId(), item.getShopName());
                        if (item.getCustomerId().equals(customerId) && !keySets.contains(key)) {
                            keySets.add(key);
                            CustomerShop customerShop = new CustomerShop();
                            customerShop.setId(item.getShopId());
                            customerShop.setName(item.getShopName());
                            customerShop.setDataSource(item.getDataSource());
                            result.add(customerShop);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static String getShopName(Long customerId, String shopId) {
        String shopName = "";
        if (customerId != null && customerId > 0 && StringUtils.isNotBlank(shopId)) {
            shopName = customerMappingService.getshopName(customerId, shopId);
        }
        return shopName;
    }

    /**
     * 查询所有的数据源中的店铺与客户的对应关系
     *
     * @return key为“dataSourceId:shopId”
     */
    public static Map<String, B2BCustomerMapping> getAllCustomerMappingMap() {
        List<B2BDataSourceEnum> dataSourceList = B2BDataSourceEnum.getAllB2BDataSource();
        dataSourceList.add(B2BDataSourceEnum.KKL);
        List<B2BCustomerMapping> list = null;
        Map<String, B2BCustomerMapping> map = Maps.newHashMap();
        for (B2BDataSourceEnum dataSource : dataSourceList) {
            list = customerMappingService.getListByDataSource(dataSource);
            if (list.size() > 0) {
                for (B2BCustomerMapping item : list) {
                    map.put(String.format("%d:%s", dataSource.id, item.getShopId()), item);
                }
            }
        }
        return map;
    }

    /**
     * 返回元祖对象，包含两个Map：
     * 第一个Map的key为shopId，value为B2BCustomerMapping；
     * 第二个Map的key为dataSourceId:shopId，value为B2BCustomerMapping
     */
    public static TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> getAllCustomerMappingMaps() {
        Map<String, B2BCustomerMapping> resultA = Maps.newHashMap();
        Map<String, B2BCustomerMapping> resultB = Maps.newHashMap();
        List<B2BCustomerMapping> list = customerMappingService.getAllCustomerMapping();
        if (list.size() > 0) {
            for (B2BCustomerMapping item : list) {
                if (item != null && StringUtils.isNotBlank(item.getShopId())) {
                    resultA.put(item.getShopId(), item);
                    if (B2BDataSourceEnum.isB2BDataSource(item.getDataSource())) {
                        resultB.put(String.format("%d:%s", item.getDataSource(), item.getShopId()), item);
                    }
                }
            }
        }
        return new TwoTuple<>(resultA, resultB);
    }

    /**
     * 获取店铺名称
     *
     * @param allShopMap 元祖对象，包含两个Map：第一个Map的key为shopId，value为B2BCustomerMapping；第二个Map的key为dataSourceId:shopId，value为B2BCustomerMapping
     */
    public static String getShopName(Integer dataSourceId, String shopId, TwoTuple<Map<String, B2BCustomerMapping>, Map<String, B2BCustomerMapping>> allShopMap) {
        String shopName = "";
        if (B2BDataSourceEnum.isDataSource(dataSourceId) && StringUtils.isNotBlank(shopId)) {
            B2BCustomerMapping customerMapping;
            if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
                customerMapping = allShopMap.getBElement().get(String.format("%s:%s", dataSourceId, shopId));
            } else {
                customerMapping = allShopMap.getAElement().get(shopId);
            }
            if (customerMapping != null && StringUtils.isNotBlank(customerMapping.getShopName())) {
                shopName = customerMapping.getShopName();
            }
        }
        return shopName;
    }

    /**
     * 查询所有的数据源中的店铺与客户的对应关系
     *
     * @return key为“shopId”
     */
    public static Map<String, B2BCustomerMapping> getAllCustomerMappingMapForKKL() {
        Map<String, B2BCustomerMapping> result = Maps.newHashMap();
        List<B2BCustomerMapping> list = customerMappingService.getAllCustomerMapping();
        if (list.size() > 0) {
            for (B2BCustomerMapping item : list) {
                if (item != null && StringUtils.isNotBlank(item.getShopId())) {
                    result.put(item.getShopId(), item);
                }
            }
        }
        return result;
    }

    /**
     * 获取所有的默认店铺ID
     */
    public static Map<Integer, String> getDefaultShopMap() {
        Map<Integer, String> result = customerMappingService.getDefaultShopMap();
        return result;
    }

    /**
     * 获取指定数据源的默认店铺ID
     */
    public static String getDefaultShopId(Integer dataSourceId) {
        String shopId = "";
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            Map<Integer, String> map = customerMappingService.getDefaultShopMap();
            if (!map.isEmpty() && map.containsKey(dataSourceId)) {
                shopId = map.get(dataSourceId);
            }
        }
        return shopId;
    }

    //endregion B2B店铺与工单系统客户的映射关系

    //region B2B产品与工单系统产品的映射关系

    /**
     * 查询产品关联实例
     *
     * @param dataSource  B2BDataSourceEnum
     * @param category    类目/类型
     * @param productCode 规格/型号
     * @return B2BProductMapping
     */
    public static B2BProductMapping getProductMapping(B2BDataSourceEnum dataSource, String shopId, String category, String productCode) {
        Map<String, Map<String, B2BProductMapping>> map = getProductMappingMap(dataSource, shopId, Lists.newArrayList(category));
        return getProductMapping(map, category, productCode);
    }

    /**
     * 查询客户料号对应的产品，自己店铺找不到，则去默认店铺查找
     */
    public static B2BProductMapping getProductMappingNew(B2BDataSourceEnum dataSource, String shopId, String defaultShopId, String category, String productCode) {
        B2BProductMapping productMapping = null;
        if (StringUtils.isBlank(defaultShopId) || defaultShopId.equalsIgnoreCase(shopId)) {
            productMapping = getProductMapping(dataSource, shopId, category, productCode);
        } else {
            Map<String, List<B2BProductMapping>> map = productMappingService.getListByCustomerCategoryIds(dataSource, Lists.newArrayList(shopId, defaultShopId), Lists.newArrayList(category));
            if (!map.isEmpty()) {
                List<B2BProductMapping> list = map.get(shopId);
                Map<String, Map<String, B2BProductMapping>> tempMap;
                if (list != null && !list.isEmpty()) {
                    tempMap = list.stream().collect(Collectors.groupingBy(B2BProductMapping::getCustomerCategoryId, Collectors.toMap(B2BProductMapping::getProductCode, item -> item)));
                    productMapping = getProductMapping(tempMap, category, productCode);
                }
                if (productMapping == null) {
                    list = map.get(defaultShopId);
                    if (list != null && !list.isEmpty()) {
                        tempMap = list.stream().collect(Collectors.groupingBy(B2BProductMapping::getCustomerCategoryId, Collectors.toMap(B2BProductMapping::getProductCode, item -> item)));
                        productMapping = getProductMapping(tempMap, category, productCode);
                    }
                }
            }
        }
        return productMapping;
    }

    /**
     * 查询产品关联实例
     *
     * @param map         数据集
     * @param category    类目/类型
     * @param productCode 规格/型号
     */
    private static B2BProductMapping getProductMapping(Map<String, Map<String, B2BProductMapping>> map, String category, String productCode) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        if (category == null) {
            category = "";
        }
        if (productCode == null) {
            productCode = "";
        }
        Map<String, B2BProductMapping> subMap;
        //1.category
        subMap = map.get(category);
        if (subMap == null) {
            return null;
        }
        //2.productCode
        B2BProductMapping product = subMap.get(productCode);
        if (product == null && StringUtils.isNotBlank(productCode)) {
            product = subMap.get("");
        }
        return product;
    }

    /**
     * 从产品映射中读取店铺ID
     */
    public static List<String> getShopIdsFromProductMapping(B2BDataSourceEnum dataSource, List<String> customerCategoryIds) {
        List<String> shopIds = Lists.newArrayList();
        List<B2BProductMapping> list = productMappingService.getListByCustomerCategoryIds(dataSource, customerCategoryIds);
        if (list != null && !list.isEmpty()) {
            shopIds = list.stream().filter(i -> StringUtils.isNotBlank(i.getShopId())).map(B2BProductMapping::getShopId).distinct().collect(Collectors.toList());
        }
        return shopIds;
    }

    /**
     * 根据产品+型号来获取店铺ID
     */
    public static List<String> getShopIdsByProductCodeAndSpecs(B2BDataSourceEnum dataSource, List<TwoTuple<String, String>> b2bProductCodeAndSpecs) {
        List<String> shopIds = Lists.newArrayList();
        if (b2bProductCodeAndSpecs != null && !b2bProductCodeAndSpecs.isEmpty()) {
            List<String> customerCategoryIds = b2bProductCodeAndSpecs.stream().map(TwoTuple::getAElement).distinct().collect(Collectors.toList());
            List<B2BProductMapping> list = productMappingService.getListByCustomerCategoryIds(dataSource, customerCategoryIds);
            if (list != null && !list.isEmpty()) {
                List<String> temp;
                for (TwoTuple<String, String> item : b2bProductCodeAndSpecs) {
                    temp = list.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getShopId()))
                            .filter(i -> i.getCustomerCategoryId().equals(item.getAElement()) && i.getProductCode().equals(item.getBElement()))
                            .map(B2BProductMapping::getShopId).distinct().collect(Collectors.toList());
                    if (!temp.isEmpty()) {
                        shopIds.addAll(temp);
                    }
                }
                if (shopIds.size() > 1) {
                    shopIds = shopIds.stream().distinct().collect(Collectors.toList());
                }
            }
        }
        return shopIds;
    }

    private static Map<String, Map<String, B2BProductMapping>> getProductMappingMap(B2BDataSourceEnum dataSource, String shopId, List<String> customerCategoryIds) {
        List<B2BProductMapping> list = productMappingService.getListByCustomerCategoryIds(dataSource, shopId, customerCategoryIds);
        if (list == null || list.isEmpty()) {
            list = Lists.newArrayList();
        }
        return list.stream().collect(Collectors.groupingBy(B2BProductMapping::getCustomerCategoryId, Collectors.toMap(B2BProductMapping::getProductCode, item -> item)));
    }

    //endregion B2B产品与工单系统产品的映射关系

    //region B2B服务类型与工单系统服务类型的映射关系

    /**
     * 查询数据源中所有B2B服务类型与工单系统服务类型的映射关系
     *
     * @param dataSource B2BDataSourceEnum
     * @return key为b2bServiceTypeCode:b2bWarrantyType
     */
    public static Map<String, B2BServiceTypeMapping> getServiceTypeMappingMap(B2BDataSourceEnum dataSource) {
        List<B2BServiceTypeMapping> list = serviceTypeMappingService.getListByDataSource(dataSource);
        Map<String, B2BServiceTypeMapping> map = Maps.newHashMap();
        if (list.size() > 0) {
            for (B2BServiceTypeMapping item : list) {
                map.put(String.format("%s:%s", item.getB2bServiceTypeCode(), item.getB2bWarrantyType()), item);
            }
        }
        return map;
    }

    //endregion 查询数据源中所有B2B服务类型与工单服务类型的映射关系


    //region 查询kkl取消类型与第三方取消类型的映射关系

    /**
     * 查询指定数据源的取消类型映射关系
     */
    public static Map<Integer, B2BCancelTypeMapping> getCancelTypeMappingMap(B2BDataSourceEnum dataSource) {
        List<B2BCancelTypeMapping> list = cancelTypeMappingService.getListByDataSource(dataSource);
        Map<Integer, B2BCancelTypeMapping> map = Maps.newHashMap();
        if (list.size() > 0) {
            for (B2BCancelTypeMapping item : list) {
                map.put(item.getCancelCode(), item);
            }
        }
        return map;
    }

    /**
     * 获取京东的取消类型
     */
    public static Integer getJdCancelType(Integer kklCancelType) {
        Integer jdCancelType = null;
        if (kklCancelType != null) {
            Map<Integer, B2BCancelTypeMapping> map = B2BMDUtils.getCancelTypeMappingMap(B2BDataSourceEnum.JD);
            B2BCancelTypeMapping mapping = map.get(kklCancelType);
            if (mapping != null && mapping.getB2bCancelCode() != null) {
                jdCancelType = mapping.getB2bCancelCode();
            }
        }
        return jdCancelType;
    }

    /**
     * 获取云米的取消原因
     */
    public static String getVioMiCancelReason(Integer kklCancelType) {
        String cancelReason = null;
        if (kklCancelType != null) {
            Map<Integer, B2BCancelTypeMapping> map = B2BMDUtils.getCancelTypeMappingMap(B2BDataSourceEnum.VIOMI);
            B2BCancelTypeMapping mapping = map.get(kklCancelType);
            if (mapping != null && mapping.getB2bCancelCode() != null) {
                cancelReason = mapping.getB2bCancelName();
            }
        }
        return cancelReason;
    }

    public static Integer getPhilipsCancelType(Integer kklCancelType) {
        Integer jdCancelType = null;
        if (kklCancelType != null) {
            Map<Integer, B2BCancelTypeMapping> map = B2BMDUtils.getCancelTypeMappingMap(B2BDataSourceEnum.PHILIPS);
            B2BCancelTypeMapping mapping = map.get(kklCancelType);
            if (mapping != null && mapping.getB2bCancelCode() != null) {
                jdCancelType = mapping.getB2bCancelCode();
            }
        }
        return jdCancelType;
    }

    public static Integer getSuporCancelType(Integer kklCancelType) {
        Integer jdCancelType = null;
        if (kklCancelType != null) {
            Map<Integer, B2BCancelTypeMapping> map = B2BMDUtils.getCancelTypeMappingMap(B2BDataSourceEnum.SUPOR);
            B2BCancelTypeMapping mapping = map.get(kklCancelType);
            if (mapping != null && mapping.getB2bCancelCode() != null) {
                jdCancelType = mapping.getB2bCancelCode();
            }
        }
        return jdCancelType;
    }

    //endregion

    private static List<B2BDataSourceEnum> getEnabledDataSourceEnums() {
        List<B2BDataSourceEnum> dataSourceEnums = Lists.newArrayList();
        if (microServicesProperties.getTmall().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.TMALL);
        }
        if (microServicesProperties.getCanbo().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.CANBO);
            dataSourceEnums.add(B2BDataSourceEnum.USATON);
            dataSourceEnums.add(B2BDataSourceEnum.FEIYU);
        }
        if (microServicesProperties.getWeber().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.WEBER);
        }
        if (microServicesProperties.getMbo().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.MBO);
        }
        if (microServicesProperties.getSupor().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SUPOR);
        }
        if (microServicesProperties.getJinjing().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JINJING);
        }
        if (microServicesProperties.getUsatonGa().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.USATON_GA);
        }
        if (microServicesProperties.getMqi().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.MQI);
        }
        if (microServicesProperties.getJinran().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JINRAN);
        }
        if (microServicesProperties.getJd().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JD);
        }
        if (microServicesProperties.getInse().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.INSE);
        }
        if (microServicesProperties.getKonka().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.KONKA);
        }
        if (microServicesProperties.getJoyoung().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JOYOUNG);
        }
        if (microServicesProperties.getSuning().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SUNING);
        }
        if (microServicesProperties.getJdue().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JDUE);
        }
        if (microServicesProperties.getJduePlus().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JDUEPLUS);
        }
        if (microServicesProperties.getXyyPlus().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.XYINGYAN);
        }
        if (microServicesProperties.getLb().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.LB);
        }
        if (microServicesProperties.getUm().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.UM);
        }
        if (microServicesProperties.getOtlan().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.OTLAN);
        }
        if (microServicesProperties.getPdd().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.PDD);
        }
        if (microServicesProperties.getVioMi().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.VIOMI);
        }
        if (microServicesProperties.getSf().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SF);
        }
        if (microServicesProperties.getVatti().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.VATTI);
        }
        if (microServicesProperties.getPhilips().getEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.PHILIPS);
        }
        return dataSourceEnums;
    }

    private static List<B2BDataSourceEnum> getRoutingEnabledDataSourceEnums() {
        List<B2BDataSourceEnum> dataSourceEnums = Lists.newArrayList();
        if (microServicesProperties.getTmall().getEnabled() && microServicesProperties.getTmall().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.TMALL);
        }
        if (microServicesProperties.getCanbo().getEnabled() && microServicesProperties.getCanbo().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.CANBO);
            dataSourceEnums.add(B2BDataSourceEnum.USATON);
            dataSourceEnums.add(B2BDataSourceEnum.FEIYU);
        }
        if (microServicesProperties.getWeber().getEnabled() && microServicesProperties.getWeber().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.WEBER);
        }
        if (microServicesProperties.getMbo().getEnabled() && microServicesProperties.getMbo().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.MBO);
        }
        if (microServicesProperties.getSupor().getEnabled() && microServicesProperties.getSupor().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SUPOR);
        }
        if (microServicesProperties.getJinjing().getEnabled() && microServicesProperties.getJinjing().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JINJING);
        }
        if (microServicesProperties.getUsatonGa().getEnabled() && microServicesProperties.getUsatonGa().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.USATON_GA);
        }
        if (microServicesProperties.getMqi().getEnabled() && microServicesProperties.getMqi().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.MQI);
        }
        if (microServicesProperties.getJinran().getEnabled() && microServicesProperties.getJinran().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JINRAN);
        }
        if (microServicesProperties.getJd().getEnabled() && microServicesProperties.getJd().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JD);
        }
        if (microServicesProperties.getInse().getEnabled() && microServicesProperties.getInse().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.INSE);
        }
        if (microServicesProperties.getXYingYan().getEnabled() && microServicesProperties.getXYingYan().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.XYINGYAN);
        }
        if (microServicesProperties.getKonka().getEnabled() && microServicesProperties.getKonka().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.KONKA);
        }
        if (microServicesProperties.getJoyoung().getEnabled() && microServicesProperties.getJoyoung().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JOYOUNG);
        }
        if (microServicesProperties.getSuning().getEnabled() && microServicesProperties.getSuning().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SUNING);
        }
        if (microServicesProperties.getXyy().getEnabled() && microServicesProperties.getXyy().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.XYINGYAN);
        }
        if (microServicesProperties.getJdue().getEnabled() && microServicesProperties.getJdue().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JDUE);
        }
        if (microServicesProperties.getJduePlus().getEnabled() && microServicesProperties.getJduePlus().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JDUEPLUS);
        }
        if (microServicesProperties.getPdd().getEnabled() && microServicesProperties.getPdd().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.PDD);
        }
        if (microServicesProperties.getSf().getEnabled() && microServicesProperties.getSf().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SF);
        }
        if (microServicesProperties.getPhilips().getEnabled() && microServicesProperties.getPhilips().getRoutingEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.PHILIPS);
        }
        return dataSourceEnums;
    }

    private static List<B2BDataSourceEnum> getSalesmanTransferOrderEnabledDataSourceEnums() {
        List<B2BDataSourceEnum> dataSourceEnums = Lists.newArrayList();
        if (microServicesProperties.getTmall().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.TMALL);
        }
        if (microServicesProperties.getCanbo().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.CANBO);
            dataSourceEnums.add(B2BDataSourceEnum.USATON);
            dataSourceEnums.add(B2BDataSourceEnum.FEIYU);
        }
        if (microServicesProperties.getWeber().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.WEBER);
        }
        if (microServicesProperties.getMbo().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.MBO);
        }
        if (microServicesProperties.getSupor().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SUPOR);
        }
        if (microServicesProperties.getJinjing().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JINJING);
        }
        if (microServicesProperties.getUsatonGa().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.USATON_GA);
        }
        if (microServicesProperties.getMqi().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.MQI);
        }
        if (microServicesProperties.getJinran().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JINRAN);
        }
        if (microServicesProperties.getJd().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JD);
        }
        if (microServicesProperties.getInse().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.INSE);
        }
        if (microServicesProperties.getKonka().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.KONKA);
        }
        if (microServicesProperties.getJoyoung().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JOYOUNG);
        }
        if (microServicesProperties.getSuning().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SUNING);
        }
        if (microServicesProperties.getJdue().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JDUE);
        }
        if (microServicesProperties.getJduePlus().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.JDUEPLUS);
        }
        if (microServicesProperties.getXyyPlus().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.XYINGYAN);
        }
        if (microServicesProperties.getLb().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.LB);
        }
        if (microServicesProperties.getUm().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.UM);
        }
        if (microServicesProperties.getOtlan().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.OTLAN);
        }
        if (microServicesProperties.getPdd().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.PDD);
        }
        if (microServicesProperties.getVioMi().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.VIOMI);
        }
        if (microServicesProperties.getSf().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.SF);
        }
        if (microServicesProperties.getVatti().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.VATTI);
        }
        if (microServicesProperties.getPhilips().getSalesmanTransferOrderEnabled()) {
            dataSourceEnums.add(B2BDataSourceEnum.PHILIPS);
        }
        return dataSourceEnums;
    }

    private static List<Dict> toDataSourceDicts(List<B2BDataSourceEnum> dataSources) {
        List<Dict> result = Lists.newArrayList();
        if (dataSources != null && !dataSources.isEmpty()) {
            Set<String> ids = dataSources.stream().map(i -> Integer.toString(i.getId())).collect(Collectors.toSet());
            List<Dict> dataSourceDicts = MSDictUtils.getDictList(Dict.DICT_TYPE_DATA_SOURCE);
            for (Dict item : dataSourceDicts) {
                if (ids.contains(item.getValue())) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    public static List<Dict> getEnabledDataSourceDicts() {
        List<B2BDataSourceEnum> dataSourceEnums = getEnabledDataSourceEnums();
        return toDataSourceDicts(dataSourceEnums);
    }

    public static List<Dict> getRoutingEnabledDataSourceDicts() {
        List<B2BDataSourceEnum> dataSourceEnums = getRoutingEnabledDataSourceEnums();
        return toDataSourceDicts(dataSourceEnums);
    }

    public static List<Dict> getEnabledDataSourceDictsBySales(User user) {
        List<B2BDataSourceEnum> intersectedDataSources = Lists.newArrayList();
        List<B2BDataSourceEnum> dataSources = getB2BDataSourceListBySales(user);
        List<B2BDataSourceEnum> enabledDataSources = getSalesmanTransferOrderEnabledDataSourceEnums();
        B2BDataSourceEnum intersectedDataSource;
        for (B2BDataSourceEnum item : dataSources) {
            intersectedDataSource = enabledDataSources.stream().filter(i -> i.getId() == item.getId()).findFirst().orElse(null);
            if (intersectedDataSource != null) {
                intersectedDataSources.add(intersectedDataSource);
            }
        }
        return toDataSourceDicts(intersectedDataSources);
    }

    /**
     * 是否可以直接取消订单/驳回订单
     */
    public static boolean canDirectlyCancel(Integer dataSourceId) {
        boolean result = false;
        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
        if (dataSource != null) {
            switch (dataSource) {
                case TMALL:
                    result = microServicesProperties.getTmall().getEnabled() && microServicesProperties.getTmall().getDirectlyCancelEnabled();
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public static boolean canIgnoreOrder(Integer dataSourceId) {
        boolean result = false;
        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
        if (dataSource != null) {
            switch (dataSource) {
                case TMALL:
                    result = microServicesProperties.getTmall().getEnabled() && microServicesProperties.getTmall().getIgnoreOrder();
                    break;
                case JD:
                    result = microServicesProperties.getJd().getEnabled() && microServicesProperties.getJd().getIgnoreOrder();
                    break;
                default:
                    break;
            }
        }
        return result;
    }


    //region 产品辅材或服务项目

    public static Map<Long, B2BSurchargeItemMapping> getB2BSurchargeItemMap(Integer dataSourceId) {
        Map<Long, B2BSurchargeItemMapping> map = Maps.newHashMap();
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            List<B2BSurchargeItemMapping> list = surchargeItemMappingService.getListByDataSource(dataSourceId);
            map = list.stream().collect(Collectors.toMap(B2BSurchargeItemMapping::getAuxiliaryMaterialItemId, i -> i));
        }
        return map;
    }

    public static Map<Long, B2BSurchargeCategoryMapping> getB2BSurchargeCategoryMap(Integer dataSourceId) {
        Map<Long, B2BSurchargeCategoryMapping> map = Maps.newHashMap();
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            List<B2BSurchargeCategoryMapping> list = surchargeCategoryMappingService.getListByDataSource(dataSourceId);
            map = list.stream().collect(Collectors.toMap(B2BSurchargeCategoryMapping::getAuxiliaryMaterialCategoryId, i -> i));
        }
        return map;
    }

    public static Map<Long, List<B2BServiceFeeItem>> getB2BServiceItemAndCategoryMap(Integer dataSourceId, List<Long> productIds) {
        Map<Long, List<B2BServiceFeeItem>> map = Maps.newHashMap();
        if (dataSourceId != null && productIds != null && !productIds.isEmpty()) {
            List<B2BServiceFeeItem> items = serviceFeeItemService.getListByDataSource(dataSourceId);
            if (items != null && !items.isEmpty()) {
                List<B2BServiceFeeCategory> categories = serviceFeeCategoryService.getListByDataSource(dataSourceId);
                Map<Long, B2BServiceFeeCategory> categoryMap = categories.stream().collect(Collectors.toMap(MSBase::getId, i -> i));
                Long productId;
                B2BServiceFeeCategory category;
                for (B2BServiceFeeItem item : items) {
                    productId = item.getProductId();
                    if (item.getCategory() != null && item.getCategory().getId() != null && productId != null && productIds.contains(productId)) {
                        category = categoryMap.get(item.getCategory().getId());
                        if (category != null) {
                            item.setCategory(category);
                            if (map.containsKey(productId)) {
                                map.get(productId).add(item);
                            } else {
                                map.put(productId, Lists.newArrayList(item));
                            }
                        }
                    }
                }
            }
        }
        return map;
    }


    //endregion 产品辅材或服务项目


    public static List<Dict> getCustomers(Integer dataSourceId) {
        List<Dict> result = Lists.newArrayList();
        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
        if (dataSource != null) {
            List<B2BCustomerMapping> list = customerMappingService.getListByDataSource(dataSource);
            if (list != null && !list.isEmpty()) {
                Map<Long, List<String>> map = Maps.newHashMap();
                for (B2BCustomerMapping item : list) {
                    if (map.containsKey(item.getCustomerId())) {
                        map.get(item.getCustomerId()).add(item.getShopId());
                    } else {
                        map.put(item.getCustomerId(), Lists.newArrayList(item.getShopId()));
                    }
                }
                if (!map.isEmpty()) {
                    Customer customer;
                    String shopIds;
                    Dict dict;
                    for (Map.Entry<Long, List<String>> item : map.entrySet()) {
                        customer = customerService.getFromCache(item.getKey());
                        if (customer != null) {
                            shopIds = StringUtils.join(item.getValue(), ",");
                            dict = new Dict(shopIds, customer.getName());
                            result.add(dict);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean canSearchB2BOrderByCustomer(Integer dataSourceId) {
        boolean result = false;
        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
        if (dataSource != null) {
            switch (dataSource) {
                case TMALL:
                    result = microServicesProperties.getTmall().getEnabled() && microServicesProperties.getTmall().getSearchByCustomerEnabled();
                    break;
                case PDD:
                    result = microServicesProperties.getPdd().getEnabled() && microServicesProperties.getPdd().getSearchByCustomerEnabled();
                    break;
                default:
                    break;
            }
        }
        return result;
    }


    public static boolean isB2BMicroServiceEnabled(Integer dataSourceId) {
        boolean result = false;
        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
        if (dataSource != null) {
            switch (dataSource) {
                case XYINGYAN:
                    result = microServicesProperties.getXyyPlus().getEnabled();
                    break;
                case INSE:
                    result = microServicesProperties.getInse().getEnabled();
                    break;
                case VIOMI:
                    result = microServicesProperties.getVioMi().getEnabled();
                    break;
                case SF:
                    result = microServicesProperties.getSf().getEnabled();
                    break;
                default:
                    break;
            }
        }
        return result;
    }


    /**
     * 判断是否需要启用微服务的工单状态更新功能
     */
    public static boolean isOrderStatusUpdateEnabled(B2BDataSourceEnum dataSource, Long customerId) {
        boolean result = false;
        if (dataSource != null && customerId != null && customerId > 0) {
            switch (dataSource) {
                case UM:
                    List<Long> customerIds = microServicesProperties.getUm().getCustomerIds();
                    if (microServicesProperties.getUm().getEnabled()
                            && microServicesProperties.getUm().getOrderStatusUpdateEnabled()
                            && customerIds != null && !customerIds.isEmpty()) {
                        result = customerIds.contains(customerId);
                    }
                    break;
            }
        }
        return result;
    }

    /**
     * 获取数据源关联的客户ID
     */
    public static Map<Integer, List<Long>> getB2BDataSourceRelatedCustomerIdsMap() {
        Map<Integer, List<Long>> result = Maps.newHashMap();
        List<B2BCustomerMapping> list = customerMappingService.getAllCustomerMapping();
        for (B2BCustomerMapping mapping : list) {
            if (B2BDataSourceEnum.isB2BDataSource(mapping.getDataSource())) {
                if (result.containsKey(mapping.getDataSource())) {
                    result.get(mapping.getDataSource()).add(mapping.getCustomerId());
                } else {
                    result.put(mapping.getDataSource(), Lists.newArrayList(mapping.getCustomerId()));
                }
            }
        }
        return result;
    }


    public static List<B2BDataSourceEnum> getB2BDataSourceListBySales(User user) {
        List<B2BDataSourceEnum> dataSourceList = Lists.newArrayList();
        if (user != null && user.isSaleman()) {
            List<Customer> customers = CustomerUtils.getCustomerListBySales(user);
            if (!customers.isEmpty()) {
                List<Long> customerIds = customers.stream().map(LongIDBaseEntity::getId).distinct().collect(Collectors.toList());
                Map<Integer, List<Long>> relatedCustomerIdsMap = getB2BDataSourceRelatedCustomerIdsMap();
                Long containedCustomerId;
                for (Map.Entry<Integer, List<Long>> entry : relatedCustomerIdsMap.entrySet()) {
//                    if (entry.getValue().size() == 1 && customerIds.contains(entry.getValue().get(0))) {
                    containedCustomerId = entry.getValue().stream().filter(i -> customerIds.contains(i)).findFirst().orElse(null);
                    if (containedCustomerId != null) {
                        dataSourceList.add(B2BDataSourceEnum.valueOf(entry.getKey()));
                    }
                }
            }
        }
        return dataSourceList;
    }
}
