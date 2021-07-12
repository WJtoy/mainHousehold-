package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.utils.ServiceTypeListAdapter;
import com.wolfking.jeesite.modules.md.utils.ServiceTypeSimpleAdapter;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 客户产品-服务数据模型
 * 使用：下单
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProductVM implements Serializable {

    private Long id;//product
    private String name;
    //品类/类目
    private Long categoryId;
    private String categoryName;
    private String model;
    private String brand;
    private int sort;

    @JsonAdapter(ServiceTypeListAdapter.class)
    private List<ServiceType> services = Lists.newArrayList();

    private List<CustomerProductModel> models;
    private List<Brand> brands;
    private List<String> b2bProductCodes;
}

