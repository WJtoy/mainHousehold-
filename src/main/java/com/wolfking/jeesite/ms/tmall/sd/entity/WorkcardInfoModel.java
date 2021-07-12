package com.wolfking.jeesite.ms.tmall.sd.entity;

import com.kkl.kklplus.entity.b2b.order.WorkcardInfo;
import com.wolfking.jeesite.modules.md.entity.Customer;
import lombok.Data;

/**
 * 天猫工单转换数据模型
 */
@Data
public class WorkcardInfoModel extends WorkcardInfo{
    //客户
    private Customer customer;
    //工单状态
    private String taskStatusLabel;
    //处理进度
    private String processLogLabel;
    //服务类型
    private String serviceLabel;
    //数据源
    private Integer dataSource = 0;

}
