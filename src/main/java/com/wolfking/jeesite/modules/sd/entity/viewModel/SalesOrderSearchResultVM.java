package com.wolfking.jeesite.modules.sd.entity.viewModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 业务订单列表查询结果
 * 用于返回订单id及分片
 * @date 2019-11-07 15:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderSearchResultVM {
    //订单id
    private long orderId;
    //分片
    private String quarter;
}
