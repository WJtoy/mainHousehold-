package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.Page;
import lombok.Data;

import java.util.List;

/**
 * 进度跟踪 of 订单详情
 */
@Data
public class OrderTrackingSearchModel {

    private Long orderId=0L;

    private String quarter = "";

    private String closeFlag;

    private List<Integer> statusFlags = Lists.newArrayList();

    Page<OrderTrackingSearchModel> page;

}

