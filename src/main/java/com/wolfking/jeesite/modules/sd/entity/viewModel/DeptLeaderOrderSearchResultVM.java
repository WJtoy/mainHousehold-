package com.wolfking.jeesite.modules.sd.entity.viewModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 事业部订单列表查询结果
 * 用于返回订单id及分片
 * @date 2019-12-13 09:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeptLeaderOrderSearchResultVM {
    //订单id
    private long orderId;
    //分片
    private String quarter;
}
