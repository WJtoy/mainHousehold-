package com.wolfking.jeesite.modules.fi.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.modules.fi.entity.EngineerCharge;

import java.io.IOException;

/**
 * 订单自定义Gson序列化/序列化
 */
public class EngineerChargeAdapter extends TypeAdapter<EngineerCharge> {

    @Override
    public EngineerCharge read(final JsonReader in) throws IOException {
        final EngineerCharge engineerCharge = new EngineerCharge();
        String strd;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    engineerCharge.setId(in.nextLong());
                    break;
                case "orderDetailId":
                    engineerCharge.setOrderDetailId(in.nextLong());
                    break;
                case "chargeOrderType":
                    engineerCharge.setChargeOrderType(in.nextInt());
                    break;
                case "serviceCharge":
                    engineerCharge.setServiceCharge(in.nextDouble());
                    break;
                case "expressCharge":
                    engineerCharge.setExpressCharge(in.nextDouble());
                    break;
                case "travelCharge":
                    engineerCharge.setTravelCharge(in.nextDouble());
                    break;

                case "materialCharge":
                    engineerCharge.setMaterialCharge(in.nextDouble());
                    break;
                case "otherCharge":
                    engineerCharge.setOtherCharge(in.nextDouble());
                    break;
            }
        }

        in.endObject();

        return engineerCharge;
    }

    @Override
    public void write(final JsonWriter out, final EngineerCharge engineerCharge) throws IOException {
        out.beginObject();
        out.name("id").value(engineerCharge.getId());
        out.name("orderDetailId").value(engineerCharge.getOrderDetailId());
        out.name("chargeOrderType").value(engineerCharge.getChargeOrderType());
        out.name("serviceCharge").value(engineerCharge.getServiceCharge());
        out.name("travelCharge").value(engineerCharge.getTravelCharge());
        out.name("expressCharge").value(engineerCharge.getExpressCharge());
        out.name("materialCharge").value(engineerCharge.getMaterialCharge());
        out.name("otherCharge").value(engineerCharge.getOtherCharge());

        out.endObject();
    }

    private static EngineerChargeAdapter adapter;

    public EngineerChargeAdapter() {}

    public static EngineerChargeAdapter getInstance() {
        if (adapter == null){
            adapter = new EngineerChargeAdapter();
        }
        return adapter;
    }
}
