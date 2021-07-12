/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.sd.dao.OrderFeeDao;
import com.wolfking.jeesite.modules.sd.dao.OrderServicePointFeeDao;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePointFee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 网点订单费用表服务层
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderServicePointFeeService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderServicePointFeeDao dao;

    public OrderServicePointFee getByPrimaryKeys(long orderId,String quarter,long servicePointId){
        return dao.getByPrimaryKeys(orderId,quarter,servicePointId);
    }

    /**
     * 对账后更新财务网点其他扣费
     */
    @Transactional
    public int updateFeeAfterCharge(long orderId,String quarter, List<MQOrderCharge.FeeUpdateItem> items){
        if(orderId <= 0 || StringUtils.isBlank(quarter) || CollectionUtils.isEmpty(items)){
            return 0;
        }
        for(MQOrderCharge.FeeUpdateItem item:items) {
            dao.updateFeeAfterCharge(orderId, quarter, item.getServicePointId(),item.getTaxFee(),item.getInfoFee(),item.getDeposit());
        }
        return items.size();
    }

    /**
     * 更新好评费
     */
    @Transactional
    public int updatePraiseFee(long orderId,String quarter,long servicePointId,Double engineerPraiseFee){
        if(orderId <= 0 || StringUtils.isBlank(quarter)){
            return 0;
        }
        if(engineerPraiseFee == null){
            return 0;
        }
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(5);
        params.put("orderId",orderId);
        params.put("quarter",quarter);
        params.put("servicePointId",servicePointId);
        params.put("praiseFee", engineerPraiseFee);
        return dao.updatePraiseFee(params);
    }

}
