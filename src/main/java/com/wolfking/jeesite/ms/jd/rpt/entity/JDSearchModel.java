package com.wolfking.jeesite.ms.jd.rpt.entity;

import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;

import java.util.Date;

public class JDSearchModel extends B2BProcessLogSearchModel {

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
