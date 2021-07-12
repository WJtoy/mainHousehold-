package com.wolfking.jeesite.ms.b2bcenter.sd.dao;

import com.wolfking.jeesite.common.persistence.BaseDao;
import com.wolfking.jeesite.modules.sd.entity.MaterialItem;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface B2BOrderDao extends BaseDao {

    /**
     * 按B2B工单号、数据源读取已转换工单系统工单信息
     *
     * @return id、order_no、quarter
     */
    Order getOrderInfo(@Param("dataSource") int dataSource,
                       @Param("b2bOrderNo") String b2bOrderNo,
                       @Param("quarter") String quarter);

    Order getOrderInfoByB2BOrderId(@Param("dataSource") int dataSource, @Param("b2bOrderId") Long b2bOrderId, @Param("quarter") String quarter);

    /**
     * 根据工单ID获取工单的数据源ID
     */
    Integer getDataSourceIdByOrderId(@Param("orderId") Long orderId,
                                     @Param("quarter") String quarter);


    /**
     * 根据工单ID获取工单的客户ID
     */
    Long getCustomerIdByOrderId(@Param("orderId") Long orderId,
                                @Param("quarter") String quarter);

    /**
     * B2B工单退单待审核列表
     */
    List<Order> getB2BOrderReturnApproveList(OrderSearchModel searchModel);

    List<MaterialItem> getOrderMaterials(@Param("orderId") Long orderId,
                                         @Param("quarter") String quarter);

    List<OrderDetail> getOrderErrors(@Param("orderId") Long orderId,
                                     @Param("quarter") String quarter);

    Order getOrderAdditionalInfo(@Param("orderId") Long orderId,
                                  @Param("quarter") String quarter);
}
