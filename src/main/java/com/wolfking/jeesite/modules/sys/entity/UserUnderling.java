package com.wolfking.jeesite.modules.sys.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import lombok.Data;

import java.util.List;

@Data
public class UserUnderling extends LongIDDataEntity<UserUnderling> {

    private Long userId;

    private Integer type;

    private List<Long> subUserIds; //属员id
}
