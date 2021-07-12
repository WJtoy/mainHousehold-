package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderCacheReadService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderCacheReadService {


    @Autowired
    private OrderCacheReadService orderCacheReadService;


    /**
     * 读取订单信息
     *
     * @param orderId    订单id
     * @param level      读取订单内容类别（见OrderUtils.OrderDataLevel）
     * @param cacheFirst 缓存优先
     */
    public Order getOrderById(Long orderId, String quarter, OrderUtils.OrderDataLevel level, boolean cacheFirst) {
        return getOrderById(orderId, quarter, level, cacheFirst, false);
    }

    /**
     * 读取订单信息
     *
     * @param orderId      订单id
     * @param level        读取订单内容类别（见OrderUtils.OrderDataLevel）
     * @param cacheFirst   缓存优先
     * @param fromMasterDb 数据读取主库(包含condition部分，fee，details)
     */
    public Order getOrderById(Long orderId, String quarter, OrderUtils.OrderDataLevel level, boolean cacheFirst, boolean fromMasterDb) {
        return orderCacheReadService.getOrderById(orderId, quarter, level, cacheFirst, fromMasterDb);
    }

}
