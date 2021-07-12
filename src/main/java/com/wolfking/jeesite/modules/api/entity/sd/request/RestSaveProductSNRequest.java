package com.wolfking.jeesite.modules.api.entity.sd.request;

import lombok.Data;

@Data
public class RestSaveProductSNRequest {

    /**
     * 产品完工图片表的主键id
     */
    private Long uniqueId;

    /**
     * 产品条码
     */
    private String productSN;
}
