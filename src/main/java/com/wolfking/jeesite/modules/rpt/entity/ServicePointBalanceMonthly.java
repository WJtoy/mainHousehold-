package com.wolfking.jeesite.modules.rpt.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;

public class ServicePointBalanceMonthly extends LongIDDataEntity<ServicePointBalanceMonthly> {

    private ServicePoint    servicePoint;           //网点
    private Integer         paymentType;            //付款類型(日结月结)
    private Integer         year;                   //年
    private Double          m1 = 0.0;                     //1月余额
    private Double          m2 = 0.0;                     //2月余额
    private Double          m3 = 0.0;                     //3月余额
    private Double          m4 = 0.0;                     //4月余额
    private Double          m5 = 0.0;                     //5月余额
    private Double          m6 = 0.0;                     //6月余额
    private Double          m7 = 0.0;                     //7月余额
    private Double          m8 = 0.0;                     //8月余额
    private Double          m9 = 0.0;                     //9月余额
    private Double          m10 = 0.0;                    //10月余额
    private Double          m11 = 0.0;                    //11月余额
    private Double          m12 = 0.0;                    //12月余额



    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getM1() {
        return m1;
    }

    public void setM1(Double m1) {
        this.m1 = m1;
    }

    public Double getM2() {
        return m2;
    }

    public void setM2(Double m2) {
        this.m2 = m2;
    }

    public Double getM3() {
        return m3;
    }

    public void setM3(Double m3) {
        this.m3 = m3;
    }

    public Double getM4() {
        return m4;
    }

    public void setM4(Double m4) {
        this.m4 = m4;
    }

    public Double getM5() {
        return m5;
    }

    public void setM5(Double m5) {
        this.m5 = m5;
    }

    public Double getM6() {
        return m6;
    }

    public void setM6(Double m6) {
        this.m6 = m6;
    }

    public Double getM7() {
        return m7;
    }

    public void setM7(Double m7) {
        this.m7 = m7;
    }

    public Double getM8() {
        return m8;
    }

    public void setM8(Double m8) {
        this.m8 = m8;
    }

    public Double getM9() {
        return m9;
    }

    public void setM9(Double m9) {
        this.m9 = m9;
    }

    public Double getM10() {
        return m10;
    }

    public void setM10(Double m10) {
        this.m10 = m10;
    }

    public Double getM11() {
        return m11;
    }

    public void setM11(Double m11) {
        this.m11 = m11;
    }

    public Double getM12() {
        return m12;
    }

    public void setM12(Double m12) {
        this.m12 = m12;
    }
}
