package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrder;
import com.wolfking.jeesite.modules.sd.entity.*;
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
 * 导入订单数据访问接口
 * Created on 2018-10-22
 */
@Mapper
public interface OrderImportDao extends LongIDCrudDao<TempOrder> {

    /**
     * 按客户查询未转临时订单列表（不分页）
     * @param order
     * @return
     */
    public List<TempOrder> findRetryTempOrder(TempOrder order);

    public void insertTempOrder(TempOrder order);

    public TempOrder getTempOrder(@Param("id") Long id);

    TempOrder getTempOrderStatus(@Param("id") Long id);

    /**
     * 修改临时订单
     * @param order
     */
    public void updateTempOrder(HashMap<String, Object> order);

    public void retryError(HashMap<String,Object> map);

}
