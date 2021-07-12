package com.wolfking.jeesite.modules.sys.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import lombok.Data;

@Data
public class UserProductCategory extends LongIDDataEntity<UserProductCategory> {

    private Long userId;

    private Long productCategoryId;
}
