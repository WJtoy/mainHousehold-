package com.wolfking.jeesite.modules.mq.service;


import com.kkl.kklplus.entity.rpt.mq.MQRPTOrderProcessMessage;
import com.kkl.kklplus.entity.rpt.mq.MQRPTUpdateOrderComplainMessage;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderComplainModel;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderProcessModel;
import com.wolfking.jeesite.modules.mq.sender.RPTOrderComplainSender;
import com.wolfking.jeesite.modules.mq.sender.RPTOrderProcessSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class RPTOrderComplainService {

    @Autowired
    private RPTOrderComplainSender orderComplainSender;

    public void sendRPTOrderComplain(RPTOrderComplainModel orderComplainModel){
        MQRPTUpdateOrderComplainMessage.MQOrderComplainMessage msg = null;
        msg = MQRPTUpdateOrderComplainMessage.MQOrderComplainMessage.newBuilder()
                .setId(orderComplainModel.getId())
                .setJudgeItem(Optional.ofNullable(orderComplainModel.getJudgeItem()).orElse(0))
                .setJudgeObject(Optional.ofNullable(orderComplainModel.getJudgeObject()).orElse(0))
                .setStatus(Optional.ofNullable(orderComplainModel.getStatus()).orElse(0))
                .setComplainDt(Optional.ofNullable(orderComplainModel.getComplainDt()).orElse(0l))
                .build();
        orderComplainSender.send(msg);
    }
}
