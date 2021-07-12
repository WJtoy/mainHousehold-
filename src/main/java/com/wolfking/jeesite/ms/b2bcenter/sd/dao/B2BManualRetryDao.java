package com.wolfking.jeesite.ms.b2bcenter.sd.dao;

import com.wolfking.jeesite.common.persistence.BaseDao;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


@Mapper
public interface B2BManualRetryDao extends BaseDao {

    /**
     * 获取客户的工单
     */
    List<OrderCondition> listOrders(@Param("quarter") String quarter,
                                    @Param("customerIds") List<Long> customerId,
                                    @Param("beginCreateDate")Date beginCreateDate,
                                    @Param("endCreateDate") Date endCreateDate);


}
