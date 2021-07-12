package com.wolfking.jeesite.modules.fi.entity.viewModel;

import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.Page;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 网点质保金查询视图模型
 * @author Ryan
 * @date 2021-02-22 15:12
 */
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class EngineerDepositVM {

    /**
     * 当前实体分页对象
     */
    @GsonIgnore
    protected Page page;

    /**
     * 网点
     */
    private Long servicePointId;

    private String servicePointName;

    private String servicePointNo;

    /**
     * 质保金等级
     */
    private long depositLevel = -1;

    /**
     * 流水类型 参考：FIEnums.DepositCurrencyTypeENum
     */
    private Integer currencyType;

    /**
     * 相关单号
     */
    private String currencyNo;
    /**
     * 变更类型
     */
    private Integer actionType;
    /**
     * 查询日期
     */
    private Date startDate;
    private Date endDate;
    /**
     * 数据库分片
     */
    private List<String> quarters;
    /**
     * 网点电话
     */
    private String contactInfo;

}
