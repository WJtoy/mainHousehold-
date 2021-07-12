package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.test.sd.OrderTimelinessInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface TestDao extends LongIDCrudDao<Order> {
    public List<OrderCondition> findOrderConditionList(@Param("pageIndex")int pageIndex, @Param("pageSize")int pageSize);

    public List<OrderTimelinessInfo> batchCheckTimeliness();
}
