package com.wolfking.jeesite.modules.mq.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.entity.OrderReport;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 下单消息队列
 * Created by Ryan on 2017/4/19.
 */
@Mapper
public interface OrderCreateMessageDao extends LongIDCrudDao<OrderCreateBody> {

    OrderCreateBody getByOrderId(@Param("quarter") String quarter,@Param("orderId") long orderId);

    //待处理列表
    List<OrderCreateBody> getResendList(@Param("num") Integer number,@Param("logType") Integer logType);

    //报表重新统计
    //List<OrderCondition> getReportResendMessage(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("topNum") Integer topNum);//mark on 2020-2-11

    //报表重新统计
    //List<OrderCondition> getReportResendMessageByOrderIds(@Param("quarter") String quarter, @Param("ids") List<Long> ids);  //mark on 2020-2-11
}
