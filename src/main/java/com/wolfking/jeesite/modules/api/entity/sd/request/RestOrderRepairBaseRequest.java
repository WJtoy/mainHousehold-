package com.wolfking.jeesite.modules.api.entity.sd.request;

import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderItem;
import com.wolfking.jeesite.modules.api.entity.sd.adapter.RestOrderGrabAdapter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.util.Lists;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 完工维修基本请求参数
 */
@Getter
@Setter
public class RestOrderRepairBaseRequest implements Serializable {
    // 订单类型id
    private Integer orderTypeId;
    // 名称
    private String orderTypeName= "";

    // 产品id
    private Long productId;

    // 故障分类id
    private Long errorTypeId;

    // 故障现象id
    private Long errorCodeId;

}
