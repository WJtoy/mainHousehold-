package com.wolfking.jeesite.modules.mq.service;


import com.kkl.kklplus.entity.rpt.mq.MQRPTOrderProcessMessage;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderProcessModel;
import com.wolfking.jeesite.modules.mq.sender.RPTOrderProcessSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class RPTOrderProcessService {

    @Autowired
    private RPTOrderProcessSender orderProcessSender;

    public void sendRPTOrderProcess(RPTOrderProcessModel orderProcessModel){
        MQRPTOrderProcessMessage.RPTOrderProcessMessage msg = null;
        msg = MQRPTOrderProcessMessage.RPTOrderProcessMessage.newBuilder()
                .setProcessType(orderProcessModel.getProcessType())
                .setOrderId(orderProcessModel.getOrderId())
                .setQuarter(Optional.ofNullable(orderProcessModel.getQuarter()).orElse(""))
                .setProvinceId(Optional.ofNullable(orderProcessModel.getProvinceId()).orElse(0L))
                .setCityId(Optional.ofNullable(orderProcessModel.getCityId()).orElse(0L))
                .setCountyId(Optional.ofNullable(orderProcessModel.getCountId()).orElse(0L))
                .setProductCategoryId(Optional.ofNullable(orderProcessModel.getProductCategoryId()).orElse(0L))
                .setCustomerId(orderProcessModel.getCustomerId())
                .setKeFuId(orderProcessModel.getKeFuId())
                .setDataSource(Optional.ofNullable(orderProcessModel.getDataSource()).orElse(0))
                .setOrderCreateDate(Optional.ofNullable(orderProcessModel.getOrderCreateDate()).orElse(0L))
                .setOrderCloseDate(orderProcessModel.getOrderCloseDate())
                .setOrderStatus(orderProcessModel.getOrderStatus())
                .setOrderServiceType(Optional.ofNullable(orderProcessModel.getOrderServiceType()).orElse(0))
                .setServicePointId(Optional.ofNullable(orderProcessModel.getServicePointId()).orElse(0L))
                .build();
        orderProcessSender.send(msg);
    }
}
