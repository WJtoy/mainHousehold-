package com.wolfking.jeesite.modules.rpt.entity;

/**
 * Created on 2017-05-15.
 */
public abstract class BaseRptEntity {

    private static final long serialVersionUID = 1L;

    public static final int RPT_ROW_NUMBER_SUMROW  = 99999;
    public static final int RPT_ROW_NUMBER_PERROW  = 100000;

    private int rowNumber = 0;
    private Long id = 0L;
    private String quarter;

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}
