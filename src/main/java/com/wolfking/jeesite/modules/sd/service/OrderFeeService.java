/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.kkl.kklplus.entity.md.ReceivablePayableItemEnum;
import com.kkl.kklplus.entity.sys.SysUser;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderCharge;
import com.wolfking.jeesite.modules.sd.dao.OrderFeeDao;
import com.wolfking.jeesite.modules.sd.dao.OrderStatusFlagDao;
import com.wolfking.jeesite.modules.sd.entity.OrderPayable;
import com.wolfking.jeesite.modules.sd.entity.OrderReceivable;
import com.wolfking.jeesite.modules.sd.entity.OrderServicepointReceivable;
import com.wolfking.jeesite.modules.sd.entity.OrderStatusFlag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 订单费用表服务层
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderFeeService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderFeeDao dao;

    @Autowired
    private OrderServicePointFeeService servicePointFeeService;

    @Autowired
    private OrderPayableService payableService;

    @Autowired
    private OrderServicepointReceivableService servicepointReceivableService;

    /**
     * 对账后更新财务网点其他扣费
     * @param orderId 订单id
     * @param quarter 分片
     * @param engineerTaxFee 下游扣点费(汇总)
     * @param engineerInfoFee 下游平台费(汇总)
     */
    @Transactional
    public int updateFeeAfterCharge(long orderId, String quarter,Long triggerBy, Long triggerAt, double engineerTaxFee, double engineerInfoFee,double deposit, List<MQOrderCharge.FeeUpdateItem> items) {
        if (orderId <= 0 || StringUtils.isBlank(quarter) || CollectionUtils.isEmpty(items)) {
            return 0;
        }
        dao.updateFeeAfterCharge(orderId, quarter, engineerTaxFee, engineerInfoFee,deposit);
        servicePointFeeService.updateFeeAfterCharge(orderId, quarter, items);
        SysUser user = new SysUser(triggerBy);
        OrderPayable payable;
        //订单应付记录
        //扣点
        if(engineerTaxFee != 0) {
            payable = OrderPayable.builder()
                    .orderId(orderId)
                    .quarter(quarter)
                    .itemNo(ReceivablePayableItemEnum.Tax.code)
                    .formNo("")
                    .amount(engineerTaxFee)
                    .remark("")
                    .createBy(user)
                    .createAt(triggerAt)
                    .build();
            payableService.insert(payable);
        }
        //平台费
        if(engineerInfoFee != 0) {
            payable = OrderPayable.builder()
                    .orderId(orderId)
                    .quarter(quarter)
                    .itemNo(ReceivablePayableItemEnum.Platform.code)
                    .formNo("")
                    .amount(engineerInfoFee)
                    .remark("")
                    .createBy(user)
                    .createAt(triggerAt)
                    .build();
            payableService.insert(payable);
        }
        //网点应收应付记录
        OrderServicepointReceivable servicePointReceivable;
        MDServicePoint servicePoint;
        for (MQOrderCharge.FeeUpdateItem item : items) {
            servicePoint = new MDServicePoint(Long.valueOf(item.getServicePointId()));
            //扣点
            if(item.getTaxFee() != 0) {
                servicePointReceivable = OrderServicepointReceivable.builder()
                        .orderId(orderId)
                        .quarter(quarter)
                        .servicePoint(servicePoint)
                        .itemNo(ReceivablePayableItemEnum.Tax.code)
                        .formNo("")
                        .amount(item.getTaxFee())
                        .remark("")
                        .createBy(user)
                        .createAt(triggerAt)
                        .build();
                servicepointReceivableService.insert(servicePointReceivable);
            }
            //服务费
            if(item.getInfoFee() != 0) {
                servicePointReceivable = OrderServicepointReceivable.builder()
                        .orderId(orderId)
                        .quarter(quarter)
                        .servicePoint(servicePoint)
                        .itemNo(ReceivablePayableItemEnum.Platform.code)
                        .formNo("")
                        .amount(item.getInfoFee())
                        .remark("")
                        .createBy(user)
                        .createAt(triggerAt)
                        .build();
                servicepointReceivableService.insert(servicePointReceivable);
            }
        }

        return 1;
    }

    /**
     * 更新好评费
     */
    @Transactional
    public int updatePraiseFee(long orderId,String quarter,Double praiseFee,Double engineerPraiseFee){
        if(orderId <= 0 || StringUtils.isBlank(quarter)){
            return 0;
        }
        if(praiseFee == null && engineerPraiseFee == null){
            return 0;
        }
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(5);
        params.put("orderId",orderId);
        params.put("quarter",quarter);
        if(praiseFee != null) {
            params.put("praiseFee", praiseFee);
        }
        if(engineerPraiseFee != null) {
            params.put("engineerPraiseFee", engineerPraiseFee);
        }
        return dao.updatePraiseFee(params);
    }

}
