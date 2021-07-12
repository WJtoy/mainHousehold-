package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.wolfking.jeesite.modules.sd.entity.OrderItemComplete;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderItemCompleteService {

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;


    /**
     * 获取上传的附件数据
     */
    public List<OrderItemComplete> getByOrderId(Long orderId, String quarter) {
        return orderItemCompleteService.getByOrderId(orderId, quarter);
    }

    public OrderItemComplete get(long id) {
        return orderItemCompleteService.get(id);
    }

    public int getProductQty(Long orderId, String quarter, Long productId) {
        return orderItemCompleteService.getProductQty(orderId, quarter, productId);
    }

    /**
     * 上传的附件
     */
    @Transactional()
    public void save(OrderItemComplete orderItemComplete) {
        orderItemCompleteService.save(orderItemComplete);
    }

    /**
     * 删除上传的附件
     */
    @Transactional()
    public int deletePic(OrderItemComplete orderItemComplete) {
        return orderItemCompleteService.deletePic(orderItemComplete);
    }

    /**
     * 删除上传的附件(整条数据)/
     */
    public void delete(OrderItemComplete orderItemComplete) {
        orderItemCompleteService.delete(orderItemComplete);
    }

    /**
     * 编辑产品条码
     */
    @Transactional()
    public void updateBarcode(OrderItemComplete orderItemComplete) {
        orderItemCompleteService.updateBarcode(orderItemComplete);
    }

    /**
     * 获取已删除的照片记录
     */
    public List<OrderItemComplete> getDelListByOrderId(Long orderId, String quarter) {
        return orderItemCompleteService.getDelListByOrderId(orderId, quarter);
    }

    /**
     * 获取id获取条码
     */
    public String getUnitBarcodeById(Long id,String quarter){
        return orderItemCompleteService.getUnitBarcodeById(id,quarter);
    }


}
