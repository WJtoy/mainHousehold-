package com.wolfking.jeesite.ms.cc.entity;

import com.wolfking.jeesite.modules.sd.entity.Order;
import lombok.Data;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 订单催单视图模型
 * @date 2019-11-22 15:22
 */
@Data
public class OrderReminderVM extends Order {

    // 催单日期
    private String reminderDate;

    // 创建或再次催单距离现在的时效
    private double cutOffTimeLiness;
    private String cutOffLabel;

    // 最后一次客服处理催单时效
    private double latestProcessTimeLiness;
    //催单单据id
    private Long reminderId;
}
