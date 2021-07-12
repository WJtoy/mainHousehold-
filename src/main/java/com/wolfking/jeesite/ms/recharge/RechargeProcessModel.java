package com.wolfking.jeesite.ms.recharge;

import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 充值处理实体类
 * @date 2019-07-25 21:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeProcessModel implements Serializable {
    private Long id;
    private String tradeNo;
    private CustomerFinance customerFinance;
    private User createBy;
    private Date createDate;
    private String quarter;
}
