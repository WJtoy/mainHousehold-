package com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RegionSearchModel;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 天猫一键求助查询数据模型
 */
@Data
@NoArgsConstructor
public class TmallServiceMonitorSearchVM  extends RegionSearchModel<TmallServiceMonitorSearchVM> {

    private static final int ORDER_LENGTH = 14;

    //分片
    private String quarter;

    //订单id
    Long orderId;

    //订单号
    String orderNo;
    /**
     * 等级 0-所有 1-预警 2-警告 3-严重
     */
    private Integer level;

    //状态,0-初始，未反馈，1-进行中，2-关闭
    Integer status = 1;

    //客服id
    Long kefu;

    //区域查询 0-说有区域 1-按传入createBy区域查询
    Integer subQueryUserArea = 0;
    private int orderNoSearchType = 0;//工单单号搜索类型

    //客户类型 2019/12/11
    private Integer customerType;
    //可突击订单 0:非突击区域订单 1:突击区域订单
    private Integer rushType;

    private Integer kefuType;

    public int getOrderNoSearchType(){
        if (StringUtils.isNotBlank(this.orderNo)){
            this.orderNo = this.orderNo.trim().toUpperCase();
            String orderNoPrefix = Global.getConfig("OrderPrefix");
            if (orderNo.length() == ORDER_LENGTH && orderNo.startsWith(orderNoPrefix)){
                orderNoSearchType = 1;
                String quarter = QuarterUtils.getOrderQuarterFromNo(orderNo);
                if(StringUtils.isNotBlank(quarter)){
                    this.quarter = quarter;
                }
            }else if (orderNo.startsWith(orderNoPrefix)){
                orderNoSearchType = 2;
            }
        }
        return this.orderNoSearchType;
    }
}