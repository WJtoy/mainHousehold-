/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.modules.api.service.sd.AppBaseService;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * App工单
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AppOrderService extends AppBaseService {


    //region 工单图片



    /*计算图片数量
    public Integer getProductQty(List<OrderItem> items, Long productId) {
        int productQty = 0;
        if (items != null && !items.isEmpty()) {
            List<Long> productIds = items.stream().map(i -> i.getProduct().getId()).collect(Collectors.toList());
            Map<Long, Product> productMap = productService.getProductMap(productIds);
            Product product = null;
            for (OrderItem item : items) {
                product = productMap.get(item.getProductId());
                if (product != null) {
                    if (product.getSetFlag() == 1) {
                        final String[] setIds = product.getProductIds().split(",");
                        for (String id : setIds) {
                            if (productId.equals(Long.valueOf(id))) {
                                productQty = productQty + item.getQty();
                                break;
                            }
                        }
                    } else {
                        if (productId.equals(item.getProductId())) {
                            productQty = productQty + item.getQty();
                        }
                    }
                }
            }
        }

        return productQty;
    }
    */

    //endregion 工单图片


}
