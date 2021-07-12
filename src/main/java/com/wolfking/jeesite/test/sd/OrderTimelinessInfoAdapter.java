package com.wolfking.jeesite.test.sd;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sd.entity.FeedbackItem;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * Gson序列化
 */
public class OrderTimelinessInfoAdapter extends TypeAdapter<OrderTimelinessInfo> {

    private final String  dateFormat = new String("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(final JsonWriter out, final OrderTimelinessInfo item) throws IOException {
        out.beginObject()
                .name("orderId").value(item.getOrderId())
                .name("orderNo").value(item.getOrderNo())
                .name("createDate").value(DateUtils.formatDate(item.getCreateDate(),dateFormat))
                .name("planDate").value(DateUtils.formatDate(item.getPlanDate(),dateFormat))
                .name("arrivalDate").value(DateUtils.formatDate(item.getArrivalDate(),dateFormat))
                .name("closeDate").value(DateUtils.formatDate(item.getCloseDate(),dateFormat))
                .name("appCompleteType").value(item.getAppCompleteType())
                .name("appCompleteDate").value(DateUtils.formatDate(item.getAppCompleteDate(),dateFormat))
                .name("timeLiness").value(item.getTimeLiness())
                .endObject();
    }

    @Override
    public OrderTimelinessInfo read(JsonReader jsonReader) throws IOException {
        return null;
    }

    private static OrderTimelinessInfoAdapter adapter;
    public OrderTimelinessInfoAdapter() {}
    public static OrderTimelinessInfoAdapter getInstance() {
        if (adapter == null){
            adapter = new OrderTimelinessInfoAdapter();
        }
        return adapter;
    }

}
