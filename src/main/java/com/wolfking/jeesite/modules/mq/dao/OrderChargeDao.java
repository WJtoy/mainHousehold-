package com.wolfking.jeesite.modules.mq.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.mq.entity.OrderCharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface OrderChargeDao extends LongIDCrudDao<OrderCharge> {

    List<OrderCharge> selectRetryList(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate,
                                             @Param("count") Integer count);
}
