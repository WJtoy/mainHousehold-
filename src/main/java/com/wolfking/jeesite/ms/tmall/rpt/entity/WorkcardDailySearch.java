package com.wolfking.jeesite.ms.tmall.rpt.entity;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.rpt.entity.BaseRptEntity;

import java.util.List;

public class WorkcardDailySearch {

    private static final long serialVersionUID = 1L;

    public static final int IS_SEARCHING_NO     = 0;
    public static final int IS_SEARCHING_YES    = 1;

    private Integer statisticType;   //统计类型
    private Integer dataSource;     //数据源
    private Integer selectedYear;   //选中的年
    private Integer selectedMonth;  //选中的月
    private Integer isSearching; //是否从数据库中搜索数据
    private int days; //天数
    private List<?> list = Lists.newArrayList();//要显示的报表数据
    public Integer getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(Integer statisticType) {
        this.statisticType = statisticType;
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(Integer selectedYear) {
        this.selectedYear = selectedYear;
    }

    public Integer getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(Integer selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public int getRowsCount() {
        return list.size();
    }
    public Integer getIsSearching() {
        return isSearching;
    }

    public void setIsSearching(Integer isSearching) {
        this.isSearching = isSearching;
    }

    public boolean isSearching() {
        if (isSearching != null && isSearching == IS_SEARCHING_YES) {
            return true;
        }
        else {
            return false;
        }
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public List<?> getList() {
        return list;
    }

    public void setList(List<?> list) {
        this.list = list;
    }

    public int getSumRowNumber() {
        return BaseRptEntity.RPT_ROW_NUMBER_SUMROW;
    }

    public int getPerRowNumber() {
        return BaseRptEntity.RPT_ROW_NUMBER_PERROW;
    }

    public int getIsSearchingYes() {
        return IS_SEARCHING_YES;
    }
}
