package com.wolfking.jeesite.modules.api.entity.sd.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.entity.md.RestProductCompletePic;
import com.wolfking.jeesite.modules.api.entity.receipt.praise.AppPraisePicItem;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderDetail;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderDetailInfo;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderItem;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.sd.entity.OrderAttachment;
import com.wolfking.jeesite.modules.sd.utils.OrderAttachmentAdapter;

import java.io.IOException;

/**
 * 订单详情Gson序列化实现
 */
public class RestOrderDetailInfoAdapter extends TypeAdapter<RestOrderDetailInfo> {

    @Override
    public RestOrderDetailInfo read(final JsonReader in) throws IOException {
        return null;
    }

    @Override
    public void write(final JsonWriter out, final RestOrderDetailInfo order) throws IOException {
        out.beginObject();
        out.name("dataSource").value(order.getDataSource());
        out.name("orderId").value(order.getOrderId().toString());
        out.name("quarter").value(order.getQuarter());
        out.name("orderNo").value(order.getOrderNo());
        out.name("userName").value(order.getUserName());
        out.name("servicePhone").value(order.getServicePhone());
        out.name("serviceAddress").value(order.getServiceAddress());
        out.name("areaName").value(order.getAreaName());
        out.name("subAddress").value(order.getSubAddress());
        out.name("status")
                .beginObject()
                .name("label").value(order.getStatus() == null ? "" : order.getStatus().getLabel())
                .name("value").value(order.getStatus() == null ? "" : order.getStatus().getValue())
                .endObject();
        out.name("engineer").beginObject()
                .name("id").value(order.getEngineer() == null ? "0" : order.getEngineer().getId().toString())
                .name("name").value(order.getEngineer() == null ? "" : order.getEngineer().getName())
                .endObject();
        out.name("appointDate").value(order.getAppointDate() == null ? 0 : order.getAppointDate().getTime());
        out.name("acceptDate").value(order.getAcceptDate() == null ? 0 : order.getAcceptDate().getTime());
        out.name("remarks").value(order.getRemarks());//厂商说明
        out.name("description").value(order.getDescription());//服务描述
        out.name("partsFlag").value(order.getPartsFlag());
        out.name("partsStatus").value(order.getPartsStatus());//配件申请单状态 0:无配件申请 1:处理中 2:完成
        out.name("finishPhotoQty").value(order.getFinishPhotoQty());
        out.name("serviceTimes").value(order.getServiceTimes());
        out.name("serviceFlag").value(order.getServiceFlag());//上门服务标志(有上门服务)
        out.name("orderServiceType").value(order.getOrderServiceType());
        out.name("orderServiceTypeName").value(order.getOrderServiceTypeName());
        out.name("appAbnormalyFlag").value(order.getAppAbnormalyFlag());
        out.name("pendingFlag").value(order.getPendingFlag());
        out.name("areaId").value(order.getAreaId());
        out.name("appCompleteType").value(order.getAppCompleteType());
        out.name("isAppCompleted").value(order.getIsAppCompleted());
        out.name("kefuPhone").value(order.getKefuPhone());
        out.name("urgentLevelId").value(order.getUrgentLevelId());
        out.name("reminderFlag").value(order.getReminderFlag());//催单标识 0：无催单 19/07/09
        //2019-12-02
        out.name("reminderItemNo").value(order.getReminderItemNo())
                .name("reminderTimeoutAt").value(order.getReminderTimeoutAt());
        //items
        out.name("items").beginArray();
        for (final RestOrderItem item : order.getItems()) {
            RestOrderItemAdapter.getInstance().write(out, item);
        }
        out.endArray();
        //services
        out.name("services").beginArray();
        for (final RestOrderDetail item : order.getServices()) {
            RestOrderDetailAdapter.getInstance().write(out, item);
        }
        out.endArray();
        //photos
        out.name("photos").beginArray();
        for (final OrderAttachment attachment : order.getPhotos()) {
            OrderAttachmentAdapter.getInstance().write(out, attachment);
        }
        out.endArray();
        out.name("photoMinQty").value(order.getPhotoMinQty());
        out.name("photoMaxQty").value(order.getPhotoMaxQty());

        //fee
        out.name("engineerServiceCharge").value(order.getEngineerServiceCharge());
        out.name("engineerTravelCharge").value(order.getEngineerTravelCharge());
        out.name("engineerExpressCharge").value(order.getEngineerExpressCharge());
        out.name("engineerMaterialCharge").value(order.getEngineerMaterialCharge());
        out.name("engineerOtherCharge").value(order.getEngineerOtherCharge());
        out.name("engineerCharge").value(order.getEngineerCharge());
        //网点预估服务费 18/01/24
        out.name("estimatedServiceCost").value(order.getEstimatedServiceCost());
        out.name("isComplained").value(order.getIsComplained());//18/01/24
        out.name("hasAuxiliaryMaterials").value(order.getHasAuxiliaryMaterials());
        out.name("auxiliaryMaterialsTotalCharge").value(order.getAuxiliaryMaterialsTotalCharge());
        out.name("auxiliaryMaterialsActualTotalCharge").value(order.getAuxiliaryMaterialsActualTotalCharge());
        out.name("estimatedReceiveDate").value(order.getEstimatedReceiveDate());
        out.name("arrivalDate").value(order.getArrivalDate() == null ? 0 : order.getArrivalDate());
        out.name("expectServiceTime").value(StringUtils.toString(order.getExpectServiceTime()));

        //products
        out.name("products").beginArray();
        for (Product product : order.getProducts()) {
            out.beginObject()
                    .name("id").value(product.getId().toString())
                    .name("name").value(product.getName())
                    .endObject();
        }
        out.endArray();

        out.name("picRules").beginArray();
        for (RestProductCompletePic picRule : order.getPicRules()) {
            RestProductPicAdapter.getInstance().write(out, picRule);
        }
        out.endArray();
        out.name("orderPics").beginArray();
        for (RestProductCompletePic orderPic : order.getOrderPics()) {
            RestProductPicAdapter.getInstance().write(out, orderPic);
        }
        out.endArray();

        out.name("praiseStatus").value(order.getPraiseStatus() == null ? 0 : order.getPraiseStatus());
        out.name("praisePics").beginArray();
        for (AppPraisePicItem praisePicItem : order.getPraisePics()) {
            out.beginObject()
                    .name("code").value(praisePicItem.getCode())
                    .name("name").value(praisePicItem.getName())
                    .name("url").value(praisePicItem.getUrl())
                    .endObject();
        }
        out.endArray();
        out.name("suspendFlag").value(order.getSuspendFlag() == null ? 0 : order.getSuspendFlag());
        out.name("suspendType").value(order.getSuspendType() == null ? 0 : order.getSuspendType());
        out.endObject();
    }


    private static RestOrderDetailInfoAdapter adapter;

    public RestOrderDetailInfoAdapter() {
    }

    public static RestOrderDetailInfoAdapter getInstance() {
        if (adapter == null) {
            adapter = new RestOrderDetailInfoAdapter();
        }
        return adapter;
    }

}
