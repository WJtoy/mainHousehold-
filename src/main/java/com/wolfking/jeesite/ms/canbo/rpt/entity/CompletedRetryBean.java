package com.wolfking.jeesite.ms.canbo.rpt.entity;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


public class CompletedRetryBean implements Serializable {
    private List<Items> items;
    private String appKey;
    private Long processLogId;
    private String orderNo;
    private String appSecret;
    private int dataSource;
    private int b2bInterfaceId;
    private Long completesdate;
    private List<String> pic1s;
    private List<String> pic2s;
    private List<String> pic3s;
    private List<String> pic4s;
    private List<String> barcodes = Lists.newArrayList();
    private List<String> itemCodes = Lists.newArrayList();
    private List<String> outBarcodes = Lists.newArrayList();
    private String kklOrderNo;
    private String quarter;
    private Long orderId;
    private String processComment;

    public Long getCompletesdate() {
        return completesdate;
    }

    public void setCompletesdate(Long completesdate) {
        this.completesdate = completesdate;
    }

    public String getProcessComment() {
        return processComment;
    }

    public void setProcessComment(String processComment) {
        this.processComment = processComment;
    }

    public String getKklOrderNo() {
        return kklOrderNo;
    }

    public void setKklOrderNo(String kklOrderNo) {
        this.kklOrderNo = kklOrderNo;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public List<String> getPic1s() {
        return pic1s;
    }

    public Long getProcessLogId() {
        return processLogId;
    }

    public void setProcessLogId(Long processLogId) {
        this.processLogId = processLogId;
    }

    public void setPic1s(List<String> pic1s) {
        this.pic1s = pic1s;
    }

    public List<String> getPic2s() {
        return pic2s;
    }

    public void setPic2s(List<String> pic2s) {
        this.pic2s = pic2s;
    }

    public List<String> getPic3s() {
        return pic3s;
    }

    public void setPic3s(List<String> pic3s) {
        this.pic3s = pic3s;
    }

    public List<String> getPic4s() {
        return pic4s;
    }

    public void setPic4s(List<String> pic4s) {
        this.pic4s = pic4s;
    }

    public int getDataSource() {
        return dataSource;
    }

    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    public int getB2bInterfaceId() {
        return b2bInterfaceId;
    }

    public void setB2bInterfaceId(int b2bInterfaceId) {
        this.b2bInterfaceId = b2bInterfaceId;
    }

    public List<String> getItemCodes() {
        return itemCodes;
    }

    public void setItemCodes(List<String> itemCodes) {
        this.itemCodes = itemCodes;
    }

    public List<String> getOutBarcodes() {
        return outBarcodes;
    }

    public void setOutBarcodes(List<String> outBarcodes) {
        this.outBarcodes = outBarcodes;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public List<String> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(List<String> barcodes) {
        this.barcodes = barcodes;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

   public static class Items{

         private String pic1;
         private String pic2;
         private String pic3;
         private String pic4;
         private String barcode;
         private String itemCode;
         private String outBarcode;

       public String getPic1() {
           return pic1;
       }

       public void setPic1(String pic1) {
           this.pic1 = pic1;
       }

       public String getPic2() {
           return pic2;
       }

       public void setPic2(String pic2) {
           this.pic2 = pic2;
       }

       public String getPic3() {
           return pic3;
       }

       public void setPic3(String pic3) {
           this.pic3 = pic3;
       }

       public String getPic4() {
           return pic4;
       }

       public void setPic4(String pic4) {
           this.pic4 = pic4;
       }

       public String getBarcode() {
           return barcode;
       }

       public void setBarcode(String barcode) {
           this.barcode = barcode;
       }

       public String getItemCode() {
           return itemCode;
       }

       public void setItemCode(String itemCode) {
           this.itemCode = itemCode;
       }

       public String getOutBarcode() {
           return outBarcode;
       }

       public void setOutBarcode(String outBarcode) {
           this.outBarcode = outBarcode;
       }
   }

}
