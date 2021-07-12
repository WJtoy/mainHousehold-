package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.CacheDataTypeEnum;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.md.utils.ServiceTypeSimpleAdapter;
import com.wolfking.jeesite.modules.sd.dao.OrderItemDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class OrderItemService {

    @Resource
    private OrderItemDao orderItemDao;

    @Autowired
    private ProductService productService;

    /**
     * 更新工单的orderitem
     *
     * @param quarter    分片标志，允许为null
     * @param orderId    工单id
     * @param orderItems orderitem列表

    @Transactional(readOnly = false)
    public Integer updateOrderItems(String quarter, long orderId, List<OrderItem> orderItems) {
        if (orderItems != null && orderItems.size() > 0) {
            String json = OrderItemUtils.toOrderItemsJson(orderItems);
            return orderItemDao.updateOrderItemJson(quarter, orderId, json);
        }
        return 0;
    }*/

    /**
     * 删除工单的orderitem
     *
     * @param quarter 分片标志，允许为null
     * @param orderId 工单id
     * @return

    @Transactional(readOnly = false)
    public Integer deleteOrderItems(String quarter, long orderId) {
        return orderItemDao.updateOrderItemJson(quarter, orderId, null);
    }*/

    /**
     * 查询工单的orderitem列表
     */
    public Order getOrderItems(String quarter, long orderId) {
        Order order = orderItemDao.getOrderItems(quarter, orderId);
        if (order != null) {
            //order.setItems(OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson()));
            order.setItems(OrderItemUtils.pbToItems(order.getItemsPb()));//2020-12-17 sd_order -> sd_order_head
        }
        return order;
    }

    /**
     * 计算图片数量
     */
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


}
