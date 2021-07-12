package com.wolfking.jeesite.ms.viomi.rpt.entity;

import com.kkl.kklplus.entity.viomi.sd.VioMiOrderExceptionSearchModel;

import java.util.Date;

public class ViomiFailLogSearchModel extends VioMiOrderExceptionSearchModel {
    private Date beginDate;
    private Date endDate;


    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
