package com.wolfking.jeesite.modules.rpt.entity;

import lombok.Data;
import lombok.Getter;

@Data
public class CustomerRevenueFeesRptEntity {

    private Integer finishOrder; //订单编号

    private Integer finishSum = 0; //订单编号

    private Double receivableCharge = 0.0; //应收

    private Double payableCharge = 0.0; //应付

    private Double orderGrossProfit = 0.0; //工单毛利

    private Double everySingleGrossProfit = 0.0; //每单毛利

    private Double customerServiceCharge = 0.0; //服务费

    private Double customerExpressCharge = 0.0; //快递费

    private Double customerTravelCharge = 0.0; //远程费

    private Double customerMaterialCharge = 0.0; //配件费

    private Double customerTimelinessCharge  = 0.0; //时效费用

    private Double customerUrgentCharge = 0.0; //加急费

    private Double customerOtherCharge = 0.0; //其他费用

    private Double customerPraiseFee = 0.0; //好评费

    private Double engineerServiceCharge = 0.0; //服务费

    private Double engineerExpressCharge = 0.0; //快递费

    private Double engineerTravelCharge = 0.0; //远程费

    private Double engineerMaterialCharge = 0.0; //配件费

    private Double engineerUrgentCharge = 0.0; //加急费

    private Double engineerOtherCharge = 0.0; //其他费用

    private Double engineerPraiseFee = 0.0; //好评费

    private Double engineerTimelinessCharge  = 0.0; //时效费用

    private Double engineerCustomerTimelinessCharge = 0.0; //厂商时效补贴

    private Double engineerInsuranceCharge = 0.0; //保险费用

    private Double taxFee = 0.0; //税费

    private Double infoFee = 0.0; //平台费

    private Double engineerDeposit = 0.0; //质保金

    private Double customerWriteOffCharge = 0.0;
    private Double engineerWriteOffCharge = 0.0;


    public Double getReceivableTotalCharge() {
        return this.customerServiceCharge + this.customerExpressCharge + this.customerTravelCharge + this.customerMaterialCharge + this.customerTimelinessCharge + this.customerUrgentCharge + this.customerOtherCharge + this.customerPraiseFee + this.customerWriteOffCharge;
    }

    public Double getPayableChargeTotalCharge() {
        return this.engineerServiceCharge + this.engineerExpressCharge + this.engineerTravelCharge + this.engineerMaterialCharge + this.engineerUrgentCharge + this.engineerOtherCharge + this.engineerPraiseFee +
                this.engineerTimelinessCharge + this.engineerCustomerTimelinessCharge + this.taxFee + this.infoFee +  this.engineerWriteOffCharge;
    }
}
