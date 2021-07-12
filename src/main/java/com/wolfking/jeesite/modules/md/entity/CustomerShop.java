package com.wolfking.jeesite.modules.md.entity;

import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.modules.md.utils.CustomerShopAdapter;
import lombok.Data;

@Data
@JsonAdapter(CustomerShopAdapter.class)
public class CustomerShop {

    private Long userId;

    private String id;

    private String name;

    private Integer dataSource;
}
