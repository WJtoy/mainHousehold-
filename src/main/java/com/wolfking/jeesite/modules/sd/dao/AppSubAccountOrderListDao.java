package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrder;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface AppSubAccountOrderListDao extends LongIDCrudDao<Order> {




}
