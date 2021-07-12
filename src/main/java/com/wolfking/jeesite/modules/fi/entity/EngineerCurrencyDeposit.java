package com.wolfking.jeesite.modules.fi.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 网点质保金流水
 */
@Accessors(chain = true)
@NoArgsConstructor
@Data
@ToString
public class EngineerCurrencyDeposit extends LongIDDataEntity<EngineerCurrencyDeposit> {

    /**
     * 网点
     */
    private ServicePoint servicePoint;

    /**
     * 流水类型 参考：FIEnums.DepositCurrencyTypeENum
     */
    private Integer currencyType;
    private String  currencyTypeName;
    /**
     * 单据号
     */
    private String currencyNo;
    /**
     * 之前余额
     */
    private Double beforeBalance;
    /**
     * 当前余额
     */
    private Double balance;
    /**
     * 金额
     */
    private Double amount;

    /**
     * 付款方式  参考:FIEnums.PaymentTypeENum
     */
    private Integer paymentType;
    private String  paymentTypeName;

    /**
     * 处理类型 参考:FIEnums.DepositActionTypeENum
     */
    private Integer actionType;
    private String actionTypeName;
    private String quarter;

}
