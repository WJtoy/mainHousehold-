package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.OrderItemComplete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 完成工单上传图片
 */
@Mapper
public interface OrderItemCompleteDao extends LongIDCrudDao<OrderItemComplete> {

    List<OrderItemComplete> getByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    List<OrderItemComplete> getByOrderIdAndProductId(@Param("orderId") Long orderId,
                                                     @Param("quarter") String quarter,
                                                     @Param("productId") Long productId);

    Integer getProductQty(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("productId") Long productId);

    OrderItemComplete getById(@Param("id") Long id, @Param("quarter") String quarter);

    void updateBarCode(OrderItemComplete entity);

    List<OrderItemComplete> findItemCompleteByOrderId(@Param("orderId") Long orderId,
                                                      @Param("quarter") String quarter);

    /**
     * 根据订单Id获取已经删除的记录
     */
    List<OrderItemComplete> getDelListByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 根据工单id获取上传图片的总数量
     */
    Integer getUploadCountByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);


    /**
     * 根据id获取产品条码
     */
    String getUnitBarcodeById(@Param("id") Long id,@Param("quarter") String quarter);


    /**
     * 根据工单Id产品id从主库中读取已上传的完工组数(用于判断上传图片由于并发生成多笔记录)
     */
    Integer getProductQtyByMasterDB(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("productId") Long productId);


}
