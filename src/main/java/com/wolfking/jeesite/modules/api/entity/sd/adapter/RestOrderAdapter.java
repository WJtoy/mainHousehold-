package com.wolfking.jeesite.modules.api.entity.sd.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrder;

import java.io.IOException;

/**
 * 订单列表中订单Gson序列化实现
 */
public class RestOrderAdapter extends TypeAdapter<RestOrder> {

    @Override
    public RestOrder read(final JsonReader in) throws IOException {
        return null;
    }

    @Override
    public void write(final JsonWriter out, final RestOrder order) throws IOException {
        out.beginObject();

        out.name("dataSource").value(order.getDataSource())
                .name("orderId").value(order.getOrderId().toString())
                .name("quarter").value(order.getQuarter())
                .name("orderNo").value(order.getOrderNo())
                .name("userName").value(order.getUserName())
                .name("servicePhone").value(order.getServicePhone())
                .name("serviceAddress").value(order.getServiceAddress())
                .name("status")
                    .beginObject()
                    .name("label").value(order.getStatus()==null?"":order.getStatus().getLabel())
                    .name("value").value(order.getStatus()==null?"":order.getStatus().getValue())
                    .endObject();
        out.name("engineer").beginObject()
                .name("id").value(order.getEngineer()==null?"0":order.getEngineer().getId().toString())
                .name("name").value(order.getEngineer()==null?"":order.getEngineer().getName())
                .endObject();
        out.name("appointDate").value(order.getAppointDate()==null?0:order.getAppointDate().getTime())
                .name("acceptDate").value(order.getAcceptDate()==null?0:order.getAcceptDate().getTime())
                .name("remarks").value(order.getRemarks())
                .name("orderServiceType").value(order.getOrderServiceType())
                .name("orderServiceTypeName").value(order.getOrderServiceTypeName())
                .name("areaId").value(order.getAreaId())
                .name("appCompleteType").value(order.getAppCompleteType())
                .name("isAppCompleted").value(order.getIsAppCompleted())
                .name("appAbnormalyFlag").value(order.getAppAbnormalyFlag())
                .name("pendingFlag").value(order.getPendingFlag())
                .name("isComplained").value(order.getIsComplained())//18/01/24
                .name("isNewOrder").value(order.getIsNewOrder())
                .name("urgentLevelId").value(order.getUrgentLevelId())
                .name("reminderFlag").value(order.getReminderFlag())//催单标志 19/07/09
                .name("reminderItemNo").value(order.getReminderItemNo())
                .name("reminderTimeoutAt").value(order.getReminderTimeoutAt())
                .name("suspendType").value(order.getSuspendType())
                .name("suspendFlag").value(order.getSuspendFlag());
        out.endObject();
    }

}
