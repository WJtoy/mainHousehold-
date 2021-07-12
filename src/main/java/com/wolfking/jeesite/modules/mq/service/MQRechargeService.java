package com.wolfking.jeesite.modules.mq.service;

import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.mq.dao.MQRechargeDao;
import com.wolfking.jeesite.modules.mq.entity.MQRecharge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 支付通知处理失败记录表
 * @date 2019-07-29 16:21
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MQRechargeService extends LongIDCrudService<MQRechargeDao, MQRecharge> {

    /**
     * 读取处理失败的消息
     */
    public List<MQRecharge> getRetryList(long startAt, long endAt, Integer count){
        return dao.getRetryList(startAt,endAt,count);
    }

    /**
     * 新增消息记录
     */
    @Transactional()
    public int insertRechargeMessage(MQRecharge message){
        return dao.insert(message);
    }

    @Transactional()
    public int mqConsumeSuccess(long customerId,long rechargeId){
        return dao.mqConsumeSuccess(customerId,rechargeId,System.currentTimeMillis());
    }

    @Transactional()
    public int mqConsumeFail(long customerId,long rechargeId,String remarks){
        return dao.mqConsumeFail(customerId,rechargeId,System.currentTimeMillis(),remarks);
    }
}
