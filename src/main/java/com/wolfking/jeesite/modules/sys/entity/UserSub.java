package com.wolfking.jeesite.modules.sys.entity;

import lombok.Data;

@Data
public class UserSub {

    private Long userId;

    private Long subUserId;

    private String subUserName;

    private Integer type;
}
