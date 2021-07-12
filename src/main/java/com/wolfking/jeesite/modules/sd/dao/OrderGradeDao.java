package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrder;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单客评访问接口
 * Created on 2018-10-09
 */
@Mapper
public interface OrderGradeDao extends LongIDCrudDao<OrderGradeModel> {

    //region 客评

    public OrderGradeModel getOrderGradeByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    public void insertOrderGrade(OrderGradeModel grade);

    //endregion 客评

}
