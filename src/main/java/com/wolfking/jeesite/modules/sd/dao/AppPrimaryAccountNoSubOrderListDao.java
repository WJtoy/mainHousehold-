package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AppPrimaryAccountNoSubOrderListDao extends LongIDCrudDao<Order> {


}
