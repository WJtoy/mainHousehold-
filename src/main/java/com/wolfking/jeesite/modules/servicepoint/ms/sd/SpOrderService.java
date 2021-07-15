package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.kkl.kklplus.entity.common.NameValuePair;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderService {

    @Autowired
    private OrderService orderService;

    /**
     * 保存工单日志（计算日志可见性标记值）
     */
    @Transactional()
    public void saveOrderProcessLog(OrderProcessLog processLog) {
        orderService.saveOrderProcessLogNew(processLog);
    }

    /**
     * 保存工单日志（不计算日志可见性标记值）
     */
    @Transactional()
    public void saveOrderProcessLogWithNoCalcVisibility(OrderProcessLog processLog) {
        orderService.saveOrderProcessLogWithNoCalcVisibility(processLog);
    }

    /**
     * 读取订单分片
     */
    public String getOrderQuarterFromCache(Long orderId) {
        return orderService.getOrderQuarterFromCache(orderId);
    }


    /**
     * 发送统计消息
     * 包含：问题反馈，反馈处理,app异常
     */
    public void sendNoticeMessage(Integer noticeType, Long orderId, String quarter, Customer customer, User
            kefu, Long areaId, User user, Date date) {
        orderService.sendNoticeMessage(noticeType, orderId, quarter, customer, kefu, areaId, user, date);
    }

    /**
     * 发送自动完工消息(app触发)
     */
    public void sendAutoCompleteMessage(Long orderId, String quarter, User user, Date date) {
        orderService.sendAutoCompleteMessage(orderId, quarter, user, date);
    }

    /**
     * 新建智能回访任务
     *
     * @param order 订单
     * @param kefu  客服
     */
    public void sendNewVoiceTaskMessage(String site, Order order, String kefu, Date date) {
        orderService.sendNewVoiceTaskMessage(site, order, kefu, date);
    }

    /**
     * 继续任务
     */
    public void keepOnVoiceOperateMessage(String site, Long orderId, String quarter, String user, Date date) {
        orderService.keepOnVoiceOperateMessage(site, orderId, quarter, user, date);
    }

    /**
     * 检查是否可以自动完工
     *
     * @return String, 检查结果，空字符串代表：可以
     * <p>
     * (x)2018/06/04 Ryan (2018/06/06 cancel)
     * 有保险费，时效奖金，不能自动完工
     */
    public String checkAutoComplete(Order order) {
        return orderService.checkAutoComplete(order);
    }

    public boolean checkOrderProductBarCode(Long orderId, String quarter, Long customerId, List<OrderDetail> orderDetails) {
        return orderService.checkOrderProductBarCode(orderId, quarter, customerId, orderDetails);
    }

    /**
     * 返回产品及其服务项目组成的键值对列表
     *
     * @param items 服务项目列表
     */
    public List<NameValuePair<Long, Long>> getOrderDetailProductAndServiceTypePairs(List<OrderDetail> items) {
        return orderService.getOrderDetailProductAndServiceTypePairs(items);
    }

    /**
     * 派单及接单时，计算网点预估服务费用
     * 循环计价，取最低价
     */
    public Double calcServicePointCost(OrderCondition orderCondition,ServicePoint servicePoint, List<OrderItem> list) {
        return orderService.calcServicePointCost(orderCondition,servicePoint, list);
    }

    /**
     * 根据上门服务重新统计费用和上门次数
     */
    public HashMap<String, Object> recountFee(List<OrderDetail> details) {
        return orderService.recountFee(details);
    }

    /**
     * 重新计算费用，只计算本次上门服务项目
     * 循环计价，厂商取最高价，网点取最低价
     */
    public void rechargeOrder(List<OrderDetail> list, OrderDetail detail) {
        orderService.rechargeOrder(list, detail);
    }

    /**
     * 检查街道是否是偏远区域
     * @param orderCondition    订单信息表
     * @return  data: true 是；false 不是
     */
    public RestResult<Boolean> checkServicePointRemoteArea(OrderCondition orderCondition){
        return orderService.checkServicePointRemoteArea(orderCondition);
    }

    /**
     * 批量获取网点价格(偏远价格或服务价格)
     * @param orderCondition    订单信息表
     * @param servicePointId     网点
     * @param products           产品id和服务类型id键值对
     * @return  d
     */
    public Map<String, ServicePrice> getServicePriceFromCacheNew(OrderCondition orderCondition, Long servicePointId, List<NameValuePair<Long,Long>> products){
        return orderService.getServicePriceFromCacheNew(orderCondition,servicePointId,products);
    }
}
