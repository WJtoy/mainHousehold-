package com.wolfking.jeesite.modules.fi.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.fi.entity.CustomerCharge;
import com.wolfking.jeesite.modules.md.utils.GradeAdapter;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * 订单自定义Gson序列化/序列化
 */
public class CustomerChargeAdapter extends TypeAdapter<CustomerCharge> {

    private final String  dateFormat = new String("yyyy-MM-dd HH:mm:ss");

    @Override
    public CustomerCharge read(final JsonReader in) throws IOException {
        final CustomerCharge customerCharge = new CustomerCharge();
        String strd;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    customerCharge.setId(in.nextLong());
                    break;
                case "chargeOrderType":
                    customerCharge.setChargeOrderType(in.nextInt());
                    break;
                case "serviceCharge":
                    customerCharge.setServiceCharge(in.nextDouble());
                    break;
                case "expressCharge":
                    customerCharge.setExpressCharge(in.nextDouble());
                    break;
                case "travelCharge":
                    customerCharge.setTravelCharge(in.nextDouble());
                    break;

                case "materialCharge":
                    customerCharge.setMaterialCharge(in.nextDouble());
                    break;
                case "otherCharge":
                    customerCharge.setOtherCharge(in.nextDouble());
                    break;
                case "createDate":
                    strd = in.nextString();
                    if(StringUtils.isBlank(strd)){
                        customerCharge.setCreateDate(null);
                    }else{
                        try{
                            Date date = DateUtils.parse(strd,dateFormat);
                            customerCharge.setCreateDate(date);
                        } catch (ParseException e) {
                            customerCharge.setCreateDate(null);
                            try {
                                LogUtils.saveLog("日期格式错误:", "CustomerChargeAdapter.read", String.format("id:%s,createDate:%s", customerCharge.getId(), strd), e, null);
                            }catch (Exception e1){}
                        }
                    }
                    break;
                case "remarks":
                    customerCharge.setRemarks(in.nextString());
                    break;
                case "updateDate":
                    if(in.peek()== JsonToken.NULL){
                        in.nextNull();
                        customerCharge.setUpdateDate(null);
                    }else {
                        strd = in.nextString();
                        if (StringUtils.isBlank(strd)) {
                            customerCharge.setUpdateDate(null);
                        } else {
                            try {
                                Date date = DateUtils.parse(strd,dateFormat);
                                customerCharge.setUpdateDate(date);
                            } catch (ParseException e) {
                                customerCharge.setUpdateDate(null);
                                try {
                                    LogUtils.saveLog("日期格式错误:", "CustomerChargeAdapter.read", String.format("id:%s,updateDate:%s", customerCharge.getId(), strd), e, null);
                                }catch (Exception e1){}
                            }
                        }
                    }
                    break;
            }
        }

        in.endObject();

        return customerCharge;
    }

    @Override
    public void write(final JsonWriter out, final CustomerCharge customerCharge) throws IOException {
        out.beginObject();
        out.name("id").value(customerCharge.getId());
        out.name("chargeOrderType").value(customerCharge.getChargeOrderType());

        out.name("serviceCharge").value(customerCharge.getServiceCharge());
        out.name("travelCharge").value(customerCharge.getTravelCharge());
        out.name("expressCharge").value(customerCharge.getExpressCharge());
        out.name("materialCharge").value(customerCharge.getMaterialCharge());
        out.name("otherCharge").value(customerCharge.getOtherCharge());
        out.name("createDate").value(customerCharge.getCreateDate()==null?"":DateUtils.formatDate(customerCharge.getCreateDate(),dateFormat));
        out.name("remarks").value(customerCharge.getRemarks());
        if(customerCharge.getUpdateDate() != null){
            out.name("updateDate").value(DateUtils.formatDate(customerCharge.getUpdateDate(),dateFormat));
        }

        out.endObject();
    }

    private static CustomerChargeAdapter adapter;

    public CustomerChargeAdapter() {}

    public static CustomerChargeAdapter getInstance() {
        if (adapter == null){
            adapter = new CustomerChargeAdapter();
        }
        return adapter;
    }
}
