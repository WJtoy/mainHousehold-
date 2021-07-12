package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderItemCompleteDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 完成工单上传图片
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderItemCompleteService extends LongIDCrudService<OrderItemCompleteDao, OrderItemComplete> {

    @Resource
    private OrderItemCompleteDao orderItemCompleteDao;

    @Autowired
    private OrderService orderService;

    @Resource
    private OrderDao orderDao;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ProductService productService;

    /**
     * 上传的附件/
     *
     * @param orderItemComplete
     * @return
     */
    @Transactional()
    public void save(OrderItemComplete orderItemComplete) {
        if (orderItemComplete == null) {
            throw new RuntimeException("参数值未空。");
        }
        if (orderItemComplete.getOrderId() == null || orderItemComplete.getOrderId() <= 0) {
            throw new RuntimeException("未关联订单:无法保存附件。");
        }

        Order order = orderService.getOrderById(orderItemComplete.getOrderId(), "", OrderUtils.OrderDataLevel.CONDITION, true);

        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取关联订单信息失败");
        }
        if (orderItemComplete.getId() != null && orderItemComplete.getId() > 0) {
            orderItemCompleteDao.update(orderItemComplete);

        } else {
            orderItemCompleteDao.insert(orderItemComplete);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("quarter", order.getQuarter());
        map.put("orderId", orderItemComplete.getOrderId());
        map.put("finishPhotoQty", 1);//+1
        orderDao.updateCondition(map);

       /* String key = String.format(RedisConstant.SD_ORDER, orderItemComplete.getOrderId());
        try {
            if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB, key)) {
                //cache
                Long finishPhtoQty = redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_SD_DB, key, "finishPhotoQty", 1l);
            }
        } catch (Exception e) {
            log.error("[OrderAttachementService.save] hdel redis key:{}", key, e);
        }*/
        //调用订单公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(orderItemComplete.getOrderId())
                .incrFinishPhotoQty(1L);
        OrderCacheUtils.update(builder.build());

    }

    /**
     * 获取上传的附件数据/
     *
     * @param orderId
     * @param quarter
     * @return
     */
    public List<OrderItemComplete> getByOrderId(Long orderId, String quarter) {
        return orderItemCompleteDao.getByOrderId(orderId, quarter);
    }

    /**
     * 删除上传的附件/
     *
     * @param orderItemComplete
     * @return
     */
    @Transactional()
    public int deletePic(OrderItemComplete orderItemComplete) {
        int result = 0;
        orderItemComplete.preUpdate();
        if (orderItemComplete.getItemList() == null || orderItemComplete.getItemList().size() <= 0) {
            orderItemCompleteDao.delete(orderItemComplete);
            result = 0;
        } else {
            orderItemCompleteDao.update(orderItemComplete);
            result = 1;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("quarter", orderItemComplete.getQuarter());
        map.put("orderId", orderItemComplete.getOrderId());
        map.put("finishPhotoQty", -1);//-1
        orderDao.updateCondition(map);
       /* String key = String.format(RedisConstant.SD_ORDER, orderItemComplete.getOrderId());
        try {
            //cache
            if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB, key)) {
                redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_SD_DB, key, "finishPhotoQty", -1l);
            }
        } catch (Exception e) {
            log.error("[OrderItemComplete.delete] redis key:{}", key, e);
        }*/
        //调用订单公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(orderItemComplete.getOrderId())
                .incrFinishPhotoQty(-1L);
        OrderCacheUtils.update(builder.build());
        return result;
    }

    /**
     * 删除上传的附件(整条数据)/
     *
     * @param orderItemComplete
     * @return
     */
    public void delete(OrderItemComplete orderItemComplete) {
        orderItemCompleteDao.delete(orderItemComplete);
        HashMap<String, Object> map = new HashMap<>();
        map.put("quarter", orderItemComplete.getQuarter());
        map.put("orderId", orderItemComplete.getOrderId());
        map.put("finishPhotoQty", -1);//-1
        orderDao.updateCondition(map);
    }

    /**
     * 编辑产品条码
     *
     * @param orderItemComplete
     * @return
     */
    @Transactional()
    public void updateBarcode(OrderItemComplete orderItemComplete) {
        if (orderItemComplete == null) {
            throw new RuntimeException("参数值未空。");
        }
        orderItemComplete.preUpdate();
        orderItemCompleteDao.updateBarCode(orderItemComplete);
    }

    public int getProductQty(Long orderId, String quarter, Long productId) {
        return orderItemCompleteDao.getProductQty(orderId, quarter, productId);
    }

    @Transactional()
    public void deleteOrderCompletePics(Long orderId, String quarter) {
        if (orderId != null && StringUtils.isNotBlank(quarter)) {
            List<OrderItemComplete> completeList = getByOrderId(orderId, quarter);
            if (completeList != null && !completeList.isEmpty()) {
                int picQty = 0;
                for (OrderItemComplete item : completeList) {
                    picQty = picQty + item.getUploadQty();
                    item.preUpdate();
                    orderItemCompleteDao.delete(item);
                }
                HashMap<String, Object> params = new HashMap<>();
                params.put("orderId", orderId);
                params.put("quarter", quarter);
                params.put("finishPhotoQty", -picQty);
                orderDao.updateCondition(params);
           /*     String key = String.format(RedisConstant.SD_ORDER, orderId);
                try {
                    if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB, key)) {
                        redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_SD_DB, key, "finishPhotoQty", -picQty);
                        redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SD_DB, key, "orderItemCompletes");
                    }
                } catch (Exception e) {
                    log.error("[AppOrderService.updateOrderPicCache] hdel redis key:{}", key, e);
                    try {
                        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, String.format(RedisConstant.SD_ORDER, orderId));
                    } catch (Exception e1) {

                    }
                }*/
                //调用订单公共缓存
                OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
                builder.setOpType(OrderCacheOpType.UPDATE)
                        .setOrderId(orderId)
                        .incrFinishPhotoQty(-((long)picQty));
                OrderCacheUtils.update(builder.build());
            }


        }
    }

    /**
     * 删除完成工单图片
     */
    @Transactional()
    public void deleteOrderCompletePics(Long orderId, String quarter, List<OrderItem> orderItems) {
        if (orderId != null && StringUtils.isNotBlank(quarter) && orderItems != null) {
            List<OrderItemComplete> completeList = getByOrderId(orderId, quarter);
            if (completeList != null && !completeList.isEmpty()) {
                List<Long> productIds = orderItems.stream().filter(i -> i.getProduct() != null && i.getProduct().getId() != null)
                        .map(OrderItem::getProductId).collect(Collectors.toList());
                Map<Long, Integer> productQtyMap = Maps.newHashMap();
                if (!orderItems.isEmpty()) {
                    Map<Long, Product> productMap = productService.getProductMap(productIds);
                    List<TwoTuple<Long, Integer>> productQtyList = Lists.newArrayList();
                    Product product = null;
                    Long productId = null;
                    for (OrderItem item : orderItems) {
                        product = productMap.get(item.getProductId());
                        if (product != null) {
                            if (product.getSetFlag() == 1) {
                                final String[] setIds = product.getProductIds().split(",");
                                for (String id : setIds) {
                                    productId = StringUtils.toLong(id);
                                    if (productId > 0) {
                                        productQtyList.add(new TwoTuple<>(productId, item.getQty()));
                                    }
                                }
                            } else {
                                productQtyList.add(new TwoTuple<>(item.getProductId(), item.getQty()));
                            }
                        }
                    }

                    if (!productQtyList.isEmpty()) {
                        Integer productQty = null;
                        for (TwoTuple<Long, Integer> item : productQtyList) {
                            productQty = productQtyMap.get(item.getAElement());
                            if (productQty != null) {
                                productQty = productQty + item.getBElement();
                                productQtyMap.put(item.getAElement(), productQty);
                            } else {
                                productQtyMap.put(item.getAElement(), item.getBElement());
                            }
                        }
                    }
                }

                int deletePicQty = 0;
                for (OrderItemComplete item : completeList) {
                    item.preUpdate();
                    Integer qty = productQtyMap.get(item.getProduct().getId());
                    if (qty != null && qty > 0) {
                        if (qty >= item.getUploadQty()) {
                            productQtyMap.put(item.getProduct().getId(), qty - item.getUploadQty());
                        }
                        else {
                            productQtyMap.put(item.getProduct().getId(), 0);
                            deletePicQty = deletePicQty + item.getUploadQty();
                            orderItemCompleteDao.delete(item);
                        }
                    }
                    else {
                        deletePicQty = deletePicQty + item.getUploadQty();
                        orderItemCompleteDao.delete(item);
                    }
                }
                HashMap<String, Object> params = new HashMap<>();
                params.put("orderId", orderId);
                params.put("quarter", quarter);
                params.put("finishPhotoQty", -deletePicQty);
                orderDao.updateCondition(params);
           /*     String key = String.format(RedisConstant.SD_ORDER, orderId);
                try {
                    if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB, key)) {
                        redisUtils.hIncrBy(RedisConstant.RedisDBType.REDIS_SD_DB, key, "finishPhotoQty", -deletePicQty);
                        redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SD_DB, key, "orderItemCompletes");
                    }
                } catch (Exception e) {
                    log.error("[AppOrderService.updateOrderPicCache] hdel redis key:{}", key, e);
                    try {
                        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, String.format(RedisConstant.SD_ORDER, orderId));
                    } catch (Exception e1) {

                    }
                }*/
                //调用订单公共缓存
                OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
                builder.setOpType(OrderCacheOpType.UPDATE)
                        .setOrderId(orderId)
                        .incrFinishPhotoQty(-((long)deletePicQty));
                OrderCacheUtils.update(builder.build());
            }

        }
    }

    /**
     * 获取已删除的照片记录
     * @param orderId
     * @param quarter
     * @return
     */
    public List<OrderItemComplete> getDelListByOrderId(Long orderId,String quarter){
        return orderItemCompleteDao.getDelListByOrderId(orderId,quarter);
    }

    public List<OrderItemComplete> findItemCompleteByOrderId(Long orderId,String quarter){
        return orderItemCompleteDao.findItemCompleteByOrderId(orderId,quarter);
    }

    /**
     * 根据工单id获取上传图片的总数量
     */
    public int getUploadCountByOrderId(Long orderId,String quarter){
        Integer updateCount = orderItemCompleteDao.getUploadCountByOrderId(orderId,quarter);
        if(updateCount==null){
            updateCount = 0;
        }
        return updateCount;
    }

    /**
     * 根据id获取产品条码
     */
    public String getUnitBarcodeById(Long id, String quarter){
        String unitBarcode = orderItemCompleteDao.getUnitBarcodeById(id,quarter);
        if(unitBarcode==null){
            unitBarcode = "";
        }
        return unitBarcode;
    }


    /**
     * 根据工单Id产品id从主库中读取已上传的完工组数(用于判断上传图片由于并发生成多笔记录)
     */
    public Integer getProductQtyByMasterDB(Long orderId,String quarter,Long productId){
        Integer productQty = orderItemCompleteDao.getProductQtyByMasterDB(orderId,quarter,productId);
        if(productQty==null){
            productQty = 0;
        }
        return productQty;
    }

}
