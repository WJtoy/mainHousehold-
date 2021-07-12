package com.wolfking.jeesite.modules.md.service;

import cn.hutool.db.Entity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.CustomerProductCompletePicDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.utils.ProductCompletePicItemMapper;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductPicMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 品牌Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerProductCompletePicNewService extends LongIDCrudService<CustomerProductCompletePicDao, ProductCompletePic> {


    @Autowired
    private ProductService productService;

    @Autowired
    private MSCustomerService msCustomerService;


    @Autowired
    private MSCustomerProductPicMappingService msCustomerProductPicMappingService;


    /**
     * 分页查询
     */
    public Page<ProductCompletePic> findPage(Page<ProductCompletePic> page, ProductCompletePic entity) {
        entity.setPage(page);
        Page<ProductCompletePic> productCompletePicPage = msCustomerProductPicMappingService.findList(page,entity);
        List<ProductCompletePic> list = productCompletePicPage.getList();
        if (list == null) {
            list = Lists.newArrayList();
        }
        if (list.isEmpty()) {
            page.setList(list);
            return page;
        }
        Product product;
        Set<Long> productIds = list.stream().map(r->r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));
        for (ProductCompletePic customerCompletePic : list) {
            customerCompletePic.parseItemsFromJson();//json to list
            product = productMap.get(customerCompletePic.getProduct().getId());
            if (product != null) {
                customerCompletePic.setProduct(product);
            }
        }
        page.setCount(productCompletePicPage.getCount());
        page.setList(list);
        return page;
    }

    /**
     * 从数据字典中同步标题，排序，说明等信息,并读取产品信息
     *
     * @param list
     */
    public void syncItemInfoFromDict(List<ProductCompletePic> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Dict> dicts = MSDictUtils.getDictList(ProductCompletePic.DICTTYPE);
        if (dicts == null || dicts.isEmpty()) {
            return;
        }
        List<ProductCompletePicItem> items = Mappers.getMapper(ProductCompletePicItemMapper.class).listToPicItem(dicts);
        Product product;
        Customer customer;
        Set<Long> customerIds = list.stream().map(r->r.getCustomer().getId()).collect(Collectors.toSet());
        Map<Long, Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(Lists.newArrayList(customerIds));
        Set<Long> productIds = list.stream().map(r->r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));
        for (ProductCompletePic customerCompletePic : list) {
            customerCompletePic.parseItemsFromJson();//json to list
            product = productMap.get(customerCompletePic.getProduct().getId());
            customer = customerMap.get(customerCompletePic.getCustomer().getId());
            if (product != null) {
                customerCompletePic.setProduct(product);
            }
            if (customer != null) {
                customerCompletePic.setCustomer(customer);
            }
            if (!customerCompletePic.getItems().isEmpty()) {
                Map<String, ProductCompletePicItem> itemMaps = customerCompletePic.getItems().stream().collect(Collectors.toMap(ProductCompletePicItem::getPictureCode, item -> item));
                for (ProductCompletePicItem itm : items) {
                    if (!itemMaps.containsKey(itm.getPictureCode())) {
                       customerCompletePic.getItems().add(itm);
                    }else {
                        itemMaps.get(itm.getPictureCode()).setSort(itm.getSort());
                    }
                }
                customerCompletePic.setItems(customerCompletePic.getItems().stream().sorted(Comparator.comparing(ProductCompletePicItem::getSort)).collect(Collectors.toList()));
            }
        }
    }

}
