package com.wolfking.jeesite.modules.sd.utils;

import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 订单客评自定义Gson序列化/序列化
 */
@Slf4j
public class OrderGradeModelAdapter extends TypeAdapter<OrderGradeModel> {

    private final String  dateFormat = new String("yyyy-MM-dd HH:mm:ss");

    @Override
    public OrderGradeModel read(final JsonReader in) throws IOException {
        final OrderGradeModel gradeModel = new OrderGradeModel();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    if(in.peek() == JsonToken.NULL){
                        gradeModel.setId(null);
                        in.nextNull();
                    }else{
                        gradeModel.setId(in.nextLong());
                    }
                    break;
                case "quarter":
                    gradeModel.setQuarter(in.nextString());
                    break;
                case "orderId":
                    gradeModel.setOrderId(in.nextLong());
                    break;
                case "orderNo":
                    gradeModel.setOrderNo(in.nextString());
                    break;
                case "servicePoint":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                    }else {
                        in.beginObject();
                        ServicePoint servicePoint = new ServicePoint();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "id":
                                    servicePoint.setId(in.nextLong());
                                    break;
                                case "name":
                                    servicePoint.setName(in.nextString());
                                    break;
                            }
                        }
                        gradeModel.setServicePoint(servicePoint);
                        in.endObject();
                    }
                    break;
                case "engineer":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                    }else {
                        in.beginObject();
                        Engineer engineer = new Engineer();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "id":
                                    engineer.setId(in.nextLong());
                                    break;
                                case "name":
                                    engineer.setName(in.nextString());
                                    break;
                            }
                        }
                        gradeModel.setEngineer(engineer);
                        in.endObject();
                    }
                    break;
                case "point":
                    gradeModel.setPoint(in.nextInt());
                    break;
                case "autoGradeFlag":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                    }else {
                        in.nextInt();
                    }
                    break;
                case "timeLiness":
                    gradeModel.setTimeLiness(in.nextDouble());
                    break;
                case "rushCloseFlag":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        gradeModel.setRushCloseFlag(0);
                    }else {
                        gradeModel.setRushCloseFlag(in.nextInt());
                    }
                    break;
                case "items":
                    in.beginArray();
                    final List items = Lists.newArrayList();
                    while (in.hasNext()) {
                        items.add(OrderGradeItemAdapter.getInstance().read(in));//调用OrderItem的序列化类
                    }
                    gradeModel.setGradeList(items);
                    in.endArray();
                    break;
                case "createBy":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        gradeModel.setCreateBy(null);
                    }else{
                        User createBy = new User();
                        in.beginObject();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "id":
                                    createBy.setId(in.nextLong());
                                    break;
                                case "name":
                                    createBy.setName(in.nextString());
                                    break;
                            }
                        }
                        gradeModel.setCreateBy(createBy);
                        in.endObject();
                    }
                    break;
                case "createDate":
                    if(in.peek() == JsonToken.NULL){
                        gradeModel.setCreateDate(new Date());
                    }else{
                        gradeModel.setCreateDate(DateUtils.longToDate(in.nextLong()));
                    }
                    break;
                case "processLog":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        gradeModel.setProcessLog(null);
                    }else{
                        OrderProcessLog processLog = new OrderProcessLog();
                        in.beginObject();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "id":
                                    if(in.peek() == JsonToken.NULL){
                                        in.nextNull();
                                    }else {
                                        processLog.setId(in.nextLong());
                                    }
                                    break;
                                case "quarter":
                                    if(in.peek() == JsonToken.NULL){
                                        in.nextNull();
                                    }else {
                                        processLog.setQuarter(in.nextString());
                                    }
                                    break;
                                case "orderId":
                                    processLog.setOrderId(in.nextLong());
                                    break;
                                case "action":
                                    processLog.setAction(in.nextString());
                                    break;
                                case "actionComment":
                                    processLog.setActionComment(in.nextString());
                                    break;
                                case "status":
                                    processLog.setStatus(in.nextString());
                                    break;
                                case "statusValue":
                                    processLog.setStatusValue(in.nextInt());
                                    break;
                                case "statusFlag":
                                    processLog.setStatusFlag(in.nextInt());
                                    break;
                                case "closeFlag":
                                    if(in.peek() == JsonToken.NULL){
                                        in.nextNull();
                                    }else {
                                        processLog.setCloseFlag(in.nextInt());
                                    }
                                    break;
                                case "remarks":
                                    if(in.peek() == JsonToken.NULL){
                                        in.nextNull();
                                    }else {
                                        processLog.setRemarks(in.nextString());
                                    }
                                    break;
                                case "createBy":
                                    if(in.peek() == JsonToken.NULL){
                                        in.nextNull();
                                        processLog.setCreateBy(null);
                                    }else{
                                        User createBy = new User();
                                        in.beginObject();
                                        while (in.hasNext()) {
                                            switch (in.nextName()) {
                                                case "id":
                                                    createBy.setId(in.nextLong());
                                                    break;
                                                case "name":
                                                    createBy.setName(in.nextString());
                                                    break;
                                            }
                                        }
                                        processLog.setCreateBy(createBy);
                                        in.endObject();
                                    }
                                    break;
                                case "createDate":
                                    if(in.peek() == JsonToken.NULL){
                                        processLog.setCreateDate(new Date());
                                    }else{
                                        processLog.setCreateDate(DateUtils.longToDate(in.nextLong()));
                                    }
                                    break;
                            }
                        }
                        gradeModel.setProcessLog(processLog);
                        in.endObject();
                    }
                    break;
            }
        }
        in.endObject();
        return gradeModel;
    }

    @Override
    public void write(final JsonWriter out, final OrderGradeModel gradeModel) throws IOException {
        out.beginObject()
            .name("id").value(gradeModel.getId()==null? gradeModel.getOrderId():gradeModel.getId())
            .name("quarter").value(gradeModel.getQuarter())
            .name("orderId").value(gradeModel.getOrderId())
            .name("orderNo").value(gradeModel.getOrderNo());
        //servicePoint
        if(gradeModel.getServicePoint() != null){
            out.name("servicePoint")
                    .beginObject()
                    .name("id").value(gradeModel.getServicePoint().getId())
                    .name("name").value(gradeModel.getServicePoint().getName())
                    .endObject();
        }
        //engineer
        if(gradeModel.getEngineer() != null){
            out.name("engineer")
                    .beginObject()
                    .name("id").value(gradeModel.getEngineer().getId())
                    .name("name").value(gradeModel.getEngineer().getName())
                    .endObject();
        }

        out.name("point").value(gradeModel.getPoint())
            //.name("autoGradeFlag").value(gradeModel.getAutoGradeFlag())
            .name("timeLiness").value(gradeModel.getTimeLiness())
            .name("rushCloseFlag").value(gradeModel.getRushCloseFlag());

        //gradList(items)
        if(gradeModel.getGradeList() != null && !gradeModel.getGradeList().isEmpty()) {
            out.name("items")
                    .beginArray();
            for (final OrderGrade item : gradeModel.getGradeList()) {
                OrderGradeItemAdapter.getInstance().write(out, item);
            }
            out.endArray();
        }
        //createBy
        if (gradeModel.getCreateBy() != null){
            out.name("createBy")
                    .beginObject()
                    .name("id").value(gradeModel.getCreateBy().getId())
                    .name("name").value(gradeModel.getCreateBy().getName())
                    .endObject();
        }
        out.name("createDate").value(DateUtils.formatDate(gradeModel.getCreateDate(),dateFormat));
        if(gradeModel.getProcessLog() != null){
            OrderProcessLog processLog = gradeModel.getProcessLog();
            out.name("processLog")
                    .beginObject()
                    .name("id").value(processLog.getId())
                    .name("quarter").value(processLog.getQuarter())
                    .name("orderId").value(processLog.getOrderId())
                    .name("action").value(processLog.getAction())
                    .name("actionComment").value(processLog.getActionComment())
                    .name("status").value(processLog.getStatus())
                    .name("statusValue").value(processLog.getStatusValue())
                    .name("statusFlag").value(processLog.getStatusFlag())
                    .name("closeFlag").value(processLog.getCloseFlag())
                    .name("remarks").value(processLog.getRemarks())
                    .name("createBy")
                        .beginObject()
                        .name("id").value(processLog.getCreateBy()==null?1l:processLog.getCreateBy().getId())
                        .name("name").value(processLog.getCreateBy() == null ?"系统管理员":processLog.getCreateBy().getName())
                        .endObject()
                    .name("createDate").value(DateUtils.formatDate(processLog.getCreateDate(),dateFormat))
                    .endObject();
        }
        out.endObject();
    }

    private static OrderGradeModelAdapter adapter;
    public OrderGradeModelAdapter() {}
    public static OrderGradeModelAdapter getInstance() {
        if (adapter == null){
            adapter = new OrderGradeModelAdapter();
        }
        return adapter;
    }
}
