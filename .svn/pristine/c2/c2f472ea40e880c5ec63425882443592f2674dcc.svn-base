package com.wolfking.jeesite.modules.finance.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.CustomerPrices;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.entity.CustomerPriceModel;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPriceService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductService;
import com.wolfking.jeesite.ms.providermd.service.MSProductPriceService;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FiCustomerPriceService {

    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private MSCustomerPriceService msCustomerPriceService;

    @Autowired
    private MSProductPriceService msProductPriceService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    /**
     * 分页查询
     * 先从数据库返回客户id,再根据id从缓存中读取，缓存不存在则再从数据库读取并更新至缓存
     * 保存在map属性中
     */
    public Page<CustomerPrice> findPage(Page<CustomerPrice> page, CustomerPrice entity) {
        HashMap<String, List<HashMap<String, Object>>> customerPriceListMap = new HashMap<>();
        List<HashMap<String, Object>> customerProductPriceList = Lists.newArrayList();
        HashMap<String, Object> customerProductPriceMap;
        List<HashMap<String, Object>> customerPriceMapList;
        HashMap<String, Object> customerPriceMap;
        List<CustomerPrice> customerPriceList;
        entity.setPage(page);

        final String useDefaultPrice = String.valueOf(entity.getCustomer().getUseDefaultPrice());
        boolean notUsePrice = Objects.equals(useDefaultPrice, "0");
        final String customizePriceFlag = String.valueOf(entity.getCustomer().getCustomizePriceFlag());


        Page<CustomerProduct> customerProductPage = msCustomerProductService.findCustomerProductList(entity);
        List<CustomerProduct> customerProductsAll = customerProductPage.getList();
        page.setCount(customerProductPage.getCount());
        //end
        List<Long> customerIds = customerProductsAll.stream()
                .map(t -> t.getCustomer().getId())
                .distinct()
                .collect(Collectors.toList());

        // 获取客户价格
        List<CustomerPrice> prices = msCustomerPriceService.findPricesByCustomersNew(customerIds,entity.getProduct().getId(),null);
        final String priceType = prices.stream().map(p -> p.getPriceType().getValue()).findFirst().orElse(null);

        List<Long> productIds = customerProductsAll.stream()
                .map(t -> t.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        // 产品参考价格
        List<ProductPrice> productPrices = msProductPriceService.findGroupList(null, productIds,  null, null, entity.getCustomer().getId());

        List<ServiceType> serviceTypes = serviceTypeService.findListIdAndNameAndWarrantyStatus();
        //end
        serviceTypes = ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes);//服务类型

        Customer customer = null;
        Product product;
        ProductPrice productPrice;
        CustomerPrice price;

        for (Long customerId : customerIds) {
            final Long ci = customerId;
            customer = customerProductsAll.stream()
                    .filter(cpp -> Objects.equals(cpp.getCustomer().getId(), ci))
                    .findFirst().orElse(null).getCustomer();

            customerProductPriceMap = new HashMap<>();
            customerPriceMapList = Lists.newArrayList();

            customerProductPriceMap.put("customerId", customer.getId());
            customerProductPriceMap.put("customerCode", customer.getCode());
            customerProductPriceMap.put("customerName", customer.getName());

            List<CustomerProduct> customerProducts = customerProductsAll.stream()
                    .filter(t -> Objects.equals(t.getCustomer().getId(), ci))
                    .collect(Collectors.toList());

            for (CustomerProduct customerProduct : customerProducts) {
                product = customerProduct.getProduct();
                customerPriceMap = new HashMap<>();
                customerPriceList = Lists.newArrayList();
                final Long productId = product.getId();

                customerPriceMap.put("productId", product.getId());
                customerPriceMap.put("productName", product.getName());

                for (ServiceType serviceType : serviceTypes) {
                    final Long serviceTypeId = serviceType.getId();
                    // 客户价格
                    price = prices.stream()
                            .filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                    && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                    && Objects.equals(t.getCustomer().getId(), ci))
                            .findFirst().orElse(null);

                    // 客户价格不为空
                    if (price != null) {
                        price.setFlag(0);
                        price.setServiceType(serviceType);
                        //customerPriceList.add(price);
                        //continue;
                    }

                    // 产品参考价格 用作页面比对
                    productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                            && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                            && Objects.equals(t.getPriceType().getValue(), useDefaultPrice))
                            .findFirst().orElse(null);

                    // 有参考价格
                    if (productPrice != null) {
                        // 客户服务价格为空
                        if (price == null) {
                            price = new CustomerPrice();
                            price.setFlag(1);
                        }
                        price.setServiceType(serviceType);
                        price.setReferPrice(productPrice.getCustomerStandardPrice());
                        price.setReferDiscountPrice(productPrice.getCustomerDiscountPrice());

                    } else {
                        // 无参考价格但客户有维护自己的价格
                        if (price != null) {
                            customerPriceList.add(price);
                            continue;
                        }
                        // 无参考价格也没有客户价格 页面显示产品价格未维护
                        price = new CustomerPrice();
                        price.setServiceType(serviceType);
                        price.setFlag(2);
                    }
                    customerPriceList.add(price);
                }
                customerPriceMap.put("customerPriceList", customerPriceList);
                customerPriceMapList.add(customerPriceMap);
            }

            customerProductPriceMap.put("customerPriceMapList", customerPriceMapList);
            customerProductPriceList.add(customerProductPriceMap);
        }
        customerPriceListMap.put("list", customerProductPriceList);
        page.setMap(customerPriceListMap);
        return page;
    }

    /**
     * 保存客户某产品的所有安维价格（后台）
     * 修改价格：要审核
     * 新增价格：与参考价格对比，不同：要审核 相同：不审核
     *
     * @param customerPrices
     */
    @Transactional(readOnly = false)
    public void saveProductPrices(CustomerPrices customerPrices) {
        Customer customer = customerPrices.getCustomer();
        Product product = customerPrices.getProduct();
        User user = customerPrices.getCreateBy();
        Date date = customerPrices.getCreateDate();
        ProductPrice productPrice;
        List<ProductPrice> allPrices = msProductPriceService.findGroupList(null, Lists.newArrayList(product.getId()), null, null, customer.getId());
        List<CustomerPrice> list = Lists.newArrayList();
        boolean isNull = false;
        for (CustomerPrice p : customerPrices.getNewPrices()) {

            if (p == null || p.getServiceType() == null) {
                continue;
            }

            if (p.getServiceType().getWarrantyStatus().getValue().equalsIgnoreCase("IW")
                    && (p.getPrice() <= 0 || p.getDiscountPrice() <= 0)) {
                // 保内，无论新增、修改价格不大于0的都不更新数据库
                isNull = true;
                continue;
            }
            if (p.getId() != null) {//待审核
                p.setProduct(product);
                p.setDelFlag(2);//待审核
                p.setUpdateBy(user);
                p.setUpdateDate(date);
                p.setCustomer(customer);
                p.setIsNewRecord(false);
                list.add(p);
            } else {
                //new
                productPrice = allPrices.stream()
                        .filter(t -> Objects.equals(t.getServiceType().getId(), p.getServiceType().getId()))
                        .findFirst()
                        .orElse(null);
                if (productPrice != null) {
                    p.setProduct(product);
                    p.setCustomer(customer);
                    p.setCreateBy(user);
                    p.setCreateDate(date);
                    if (!(Objects.equals(productPrice.getCustomerStandardPrice(), p.getPrice())
                            && Objects.equals(productPrice.getCustomerDiscountPrice(), p.getDiscountPrice()))) {
                        //待审核
                        p.setDelFlag(2);
                    }
                    p.setIsNewRecord(true);
                    list.add(p);
                }
            }
        }

        MSErrorCode msErrorCode = msCustomerPriceService.insertOrUpdateBatchNewTwo(list, isNull);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 启用价格
     * 与参考价格比对，
     * 相同  ：不审核
     * 不相同：要审核
     *
     * @param p    价格
     * @param user
     */
    @Transactional(readOnly = false)
    public void startPrice(CustomerPrice p, User user) {
        if (p == null) {
            throw new RuntimeException("停用的价格不存在");
        }
        p.setDelFlag(0);

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", p.getId());
        map.put("delFlag", 2);
        map.put("updateBy", user);
        map.put("updateDate", new Date());

        MSErrorCode msErrorCode = msCustomerPriceService.updatePriceByMapNew(map);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("启用价格失败.失败原因" + msErrorCode.getMsg());
        }
    }

    /**
     * 按主键id停用单个价格
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void deletePrice(Long id) {
        List<Long> ids = Lists.newArrayList(id);
        User user = UserUtils.getUser();
        CustomerPrice price = msCustomerPriceService.getPriceNew(id, 0);   // update on 2020-06-04
        if (price == null) {
            throw new RuntimeException("停用价格不存在");
        }
        HashMap<String, Object> maps = new HashMap<String, Object>();
        maps.put("id", id);
        maps.put("delFlag", 1);
        maps.put("updateBy", user);
        maps.put("updateDate", new Date());

        MSErrorCode msErrorCode = msCustomerPriceService.updatePriceByMapNew(maps);

        if(msErrorCode.getCode()>0){
            throw new RuntimeException("停用价格失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 更新为标准价
     * @param customerPriceModel
     */
    @Transactional(readOnly = false)
    public void updateCustomizePriceFlag(CustomerPriceModel customerPriceModel) {
        User user = UserUtils.getUser();

        MSErrorCode errorCode = msCustomerPriceService.updateCustomizePriceFlag(customerPriceModel.getCustomerId(), customerPriceModel.getProductId(), customerPriceModel.getServiceTypeId(),
                user.getId(), DateUtils.formatDate(new Date()));
        if (errorCode.getCode() > 0) {
            throw new RuntimeException("更新客户价格为标准价失败.失败原因:" + errorCode.getMsg());
        }
    }

    /**
     * 分页获得待审核价格(后台)
     *
     * @param page
     * @param entity
     * @return
     */
    public Page<CustomerPrice> findApprovePricePage(Page<CustomerPrice> page, CustomerPrice entity) {
        entity.setPage(page);

        Page<CustomerPrice> customerPricePage = msCustomerPriceService.findApprovePriceList(page,entity);
        List<CustomerPrice> customerPriceList = customerPricePage.getList();

        Function<CustomerPrice,String> customerCodeSorted = customerPrice -> customerPrice.getCustomer().getCode();
        Function<CustomerPrice,String> productNameSorted = customerPrice -> customerPrice.getProduct().getName();
        Function<CustomerPrice,String> serviceTypeSorted = customerPrice -> customerPrice.getServiceType().getCode();
        List<CustomerPrice> sortedCustomerPriceList = customerPriceList != null && !customerPriceList.isEmpty()?customerPriceList.stream().sorted(Comparator.comparing(customerCodeSorted).thenComparing(productNameSorted).thenComparing(serviceTypeSorted)).collect(Collectors.toList()):null;
        page.setList(sortedCustomerPriceList);
        page.setCount(customerPricePage.getCount());

        return page;
    }

}
