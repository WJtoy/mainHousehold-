package com.wolfking.jeesite.test.sd;

import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 测试网点订单处理
 * @date 2020/2/14 9:33 上午
 */
public class OrderServicePointTest {


    @Test
    public void mqToMessage() {
        OrderServicePointMessageMapper messageMapper = Mappers.getMapper(OrderServicePointMessageMapper.class);
        MQOrderServicePointMessage.ServicePointMessage message = MQOrderServicePointMessage.ServicePointMessage.newBuilder()
                .setOperationType(MQOrderServicePointMessage.OperationType.Create)
                .setId(1581644252496L)//id
                .setOrderId(1L)
                .setQuarter("20201")
                .setSubStatus(30)
                .setOperationAt(1581642032000L)
                .setOperationBy(1)
                .setResetAppointmentDate(1)//重置null
                .setOrderInfo(MQOrderServicePointMessage.OrderInfo.newBuilder()
                        .setOrderNo("K2020021400001")
                        .setOrderServiceType(1)//安装
                        .setAreaId(3403)
                        .setAreaName("广东省 深圳市 龙华区")
                        .setStatus(30)
                        .build())
                .setServicePointInfo(MQOrderServicePointMessage.ServicePointInfo.newBuilder()
                        .setServicePointId(1)
                        .setEngineerId(1)
                        .setPrevServicePointId(0)
                        .setPlanOrder(0)
                        .setPlanType(OrderServicePoint.PlanType.KEFU.ordinal())
                        .build())
                .setUserInfo(MQOrderServicePointMessage.UserInfo.newBuilder()
                        .setUserName("李小兵")
                        .setPhone("13760468200")
                        .setAddress("龙华街道测试地址")
                        .build())
                .setPlanDate(1581642032000L)
                .setReservationDate(1581642032000L)
                .setAppointmentDate(1581642032000L)
                .setPendingType(1)

                .setOrderChannel(1)
                .setDataSource(7)
                .setMasterFlag(1)
                .setAbnormalyFlag(1)
                .setUrgentLevelId(3)
                .setComplainFlag(1)
                .setAppCompleteType("complete_all")
                .setReminderFlag(2)
                .setChargeDate(1581645600000L)
                .setCloseDate(1581645300000L)
                .build();
        OrderServicePoint entity = messageMapper.mqToModel(message);
        String json = GsonUtils.getInstance().toGson(entity);
        System.out.println("json:" + json);
    }
}
