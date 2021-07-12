package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.modules.sd.dao.OrderLocationDao;
import com.wolfking.jeesite.modules.sd.entity.OrderLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 订单地理信息表
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@Slf4j
public class OrderLocationService {

    @Resource
    private OrderLocationDao dao;

    /**
     * 插入数据
     *
     * @param entity
     * @return
     */
    @Transactional
    public int insert(OrderLocation entity) {
        return dao.insert(entity);
    }

    /**
     * 更新
     *
     * @param params
     */
    @Transactional
    public int updateByMap(Map<String, Object> params) {
        return dao.updateByMap(params);
    }

    public OrderLocation getByOrderId(Long orderId, String quarter) {
        OrderLocation location = null;
        if (orderId != null) {
            location = dao.getByOrderId(orderId, quarter);
        }
        return location;
    }

}
