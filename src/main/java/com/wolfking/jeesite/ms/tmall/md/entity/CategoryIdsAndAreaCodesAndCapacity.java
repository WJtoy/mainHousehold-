package com.wolfking.jeesite.ms.tmall.md.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class CategoryIdsAndAreaCodesAndCapacity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String categoryIds;

    @Getter
    @Setter
    private String areaCodes;

    @Getter
    @Setter
    private Long capacity;
}
