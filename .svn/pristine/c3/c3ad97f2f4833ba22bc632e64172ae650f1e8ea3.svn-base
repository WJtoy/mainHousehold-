package com.wolfking.jeesite.modules.mq.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.mq.entity.MQRecharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 支付通知处理失败记录表
 * @date 2019-07-29 16:21
 */
@Mapper
public interface MQRechargeDao extends LongIDCrudDao<MQRecharge> {

    /**
     * 读取处理失败的记录
     */
    List<MQRecharge> getRetryList(@Param("startAt") long startAt,
                                      @Param("endAt") long endAt,
                                      @Param("pageSize") Integer pageSize);

    void updateRechargeMessage(MQRecharge rechargeMessage);

    //消费成功
    int mqConsumeSuccess(@Param("customerId")long customerId,@Param("rechargeId")long rechargeId,@Param("updateAt")long updateAt);

    //消费失败
    int mqConsumeFail(@Param("customerId")long customerId,@Param("rechargeId")long rechargeId,@Param("updateAt")long updateAt,@Param("remarks") String remarks);
}
