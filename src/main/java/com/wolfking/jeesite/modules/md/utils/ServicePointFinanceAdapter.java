package com.wolfking.jeesite.modules.md.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * 客户服务价格自定义Gson序列化/序列化
 */
public class ServicePointFinanceAdapter extends TypeAdapter<ServicePointFinance> {

    private final String  dateFormat = new String("yyyy-MM-dd HH:mm:ss");

    @Override
    public ServicePointFinance read(final JsonReader in) throws IOException {
        final ServicePointFinance finance = new ServicePointFinance();
        String lastPayDateString;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        finance.setId(null);
                    }else {
                        finance.setId(in.nextLong());
                    }
                    break;
                case "paymentType":
                    finance.setPaymentType(DictSimpleAdapter.getInstance().read(in));
                    break;
                case "bank":
                    in.beginObject();
                    Dict bank = new Dict();
                    while (in.hasNext()) {
                        switch (in.nextName()) {
                            case "label":
                                bank.setLabel(in.nextString());
                                break;
                            case "value":
                                bank.setValue(in.nextString());
                                break;
                        }
                    }
                    finance.setBank(bank);
                    in.endObject();
                    break;
                case "branch":
                    finance.setBranch(in.nextString());
                    break;
                case "bankNo":
                    finance.setBankNo(in.nextString());
                    break;
                case "bankOwner":
                    finance.setBankOwner(in.nextString());
                    break;
                case "bankIssue":
                    finance.setBankIssue(DictSimpleAdapter.getInstance().read(in));
                    break;
                case "invoiceFlag":
                    finance.setInvoiceFlag(in.nextInt());
                    break;
                case "discountFlag":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        finance.setDiscountFlag(0);
                    }else {
                        finance.setDiscountFlag(in.nextInt());
                    }
                    break;
                case "discount":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        finance.setDiscount(0);
                    }else {
                        finance.setDiscount(in.nextDouble());
                    }
                    break;
                case "lastPayDate":
                    lastPayDateString = in.nextString();
                    if(StringUtils.isBlank(lastPayDateString)){
                        finance.setLastPayDate(null);
                    }else{
                        try{
                            Date date = DateUtils.parse(lastPayDateString,dateFormat);
                            finance.setLastPayDate(date);
                        } catch (ParseException e) {
                            finance.setLastPayDate(null);
                            try {
                                LogUtils.saveLog("日期格式错误:", "ServicePointFinanceAdapter.read", String.format("id:%s,lastPayDate:%s", finance.getId(), lastPayDateString), e, null);
                            }catch (Exception e1){}

                        }
                    }
                    break;
                case "lastPayAmount":
                    finance.setLastPayAmount(in.nextDouble());
                    break;
                case "balance":
                    finance.setBalance(in.nextDouble());
                    break;
                case "debtsAmount":
                    if(in.peek()==JsonToken.NULL){
                        finance.setDebtsAmount(0.0d);
                    }else {
                        finance.setDebtsAmount(in.nextDouble());
                    }
                    break;
                case "debtsDescrption":
                    if(in.peek()==JsonToken.NULL){
                        finance.setDebtsDescrption("");
                    }else {
                        finance.setDebtsDescrption(in.nextString());
                    }
                    break;
                case "insuranceAmount":
                    if(in.peek() == JsonToken.NULL){
                        finance.setInsuranceAmount(0);
                    }else{
                        finance.setInsuranceAmount(in.nextDouble());
                    }
                    break;
                case "taxFee":
                    if (in.peek() == JsonToken.NULL) {
                        finance.setTaxFee(0);
                    } else {
                        finance.setTaxFee(in.nextDouble());
                    }
                    break;
                case "infoFee":
                    if (in.peek() == JsonToken.NULL) {
                        finance.setInfoFee(0);
                    } else {
                        finance.setInfoFee(in.nextDouble());
                    }
                    break;
                case "deposit":
                    if (in.peek() == JsonToken.NULL) {
                        finance.setDeposit(0);
                    } else {
                        finance.setDeposit(in.nextDouble());
                    }
                    break;
            }
        }
        in.endObject();
        return finance;
    }

    @Override
    public void write(final JsonWriter out, final ServicePointFinance finance) throws IOException {
        out.beginObject();
        out.name("id").value(finance.getId());
        if(finance.getPaymentType() != null){
            out.name("paymentType");
            DictSimpleAdapter.getInstance().write(out,finance.getPaymentType());
        }
        if(finance.getBank() != null){
            out.name("bank");
            DictSimpleAdapter.getInstance().write(out,finance.getBank());
        }
        out.name("branch").value(finance.getBranch())
            .name("bankNo").value(finance.getBankNo())
            .name("bankOwner").value(finance.getBankOwner());
        if(finance.getBankIssue() != null){
            out.name("bankIssue");
            DictSimpleAdapter.getInstance().write(out,finance.getBankIssue());
        }
        out.name("invoiceFlag").value(finance.getInvoiceFlag())
                .name("discountFlag").value(finance.getDiscountFlag())
                .name("discount").value(finance.getDiscount())
                .name("lastPayDate").value(finance.getLastPayDate()==null?"":DateUtils.formatDate(finance.getLastPayDate(),dateFormat))
                .name("lastPayAmount").value(finance.getLastPayAmount())
                .name("balance").value(finance.getBalance())
                .name("debtsAmount").value(finance.getDebtsAmount())
                .name("debtsDescrption").value(finance.getDebtsDescrption())
                .name("insuranceAmount").value(finance.getInsuranceAmount())
                .name("taxFee").value(finance.getTaxFee())
                .name("infoFee").value(finance.getInfoFee())
                .name("deposit").value(finance.getDeposit());
        out.endObject();
    }

    private static ServicePointFinanceAdapter adapter;
    public ServicePointFinanceAdapter() {}
    public static ServicePointFinanceAdapter getInstance() {
        if (adapter == null){
            adapter = new ServicePointFinanceAdapter();
        }
        return adapter;
    }
}
