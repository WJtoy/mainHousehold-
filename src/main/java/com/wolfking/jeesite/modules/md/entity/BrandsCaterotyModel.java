package com.wolfking.jeesite.modules.md.entity;

import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import lombok.Data;

import java.util.List;

@Data
public class BrandsCaterotyModel extends LongIDDataEntity<BrandsCaterotyModel> {
    private ProductCategory category;
    @GsonIgnore
    private String brandIds = "";
    List<Brand> brandList;
}
