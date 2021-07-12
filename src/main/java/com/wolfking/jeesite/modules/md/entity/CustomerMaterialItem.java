package com.wolfking.jeesite.modules.md.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerMaterialItem implements Serializable {

    private Long materialId = 0L;
    /**
     * (是否反件)
     * */
    private Integer isReturn = 0;

    private Double price = 0.00;

    private Integer recycleFlag = 0;

    private Double recyclePrice;
    //客户配件编码
    private String customerPartCode = "";

    //客户配件名称
    private String customerPartName = "";

    //质保期
    private Integer warrantyDay = 0;

    private String remarks = "";

    private String name = "";

}
