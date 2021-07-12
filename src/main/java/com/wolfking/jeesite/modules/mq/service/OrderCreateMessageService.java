package com.wolfking.jeesite.modules.mq.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.mq.dao.OrderCreateMessageDao;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ryan on 2017/08/1.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderCreateMessageService extends LongIDBaseService {

    @Resource
    private OrderCreateMessageDao dao;

    public OrderCreateBody get(long id) {
        return dao.get(id);
    }

    public OrderCreateBody getByOrderId(String quarter,long orderId) {
        return dao.getByOrderId(quarter,orderId);
    }

    @Transactional()
    public int insert(OrderCreateBody entity) {
        return dao.insert(entity);
    }

    @Transactional()
    public void update(OrderCreateBody entity) {
        dao.update(entity);
    }

    /**
     * 读取待重送的列表
     *
     * @param number 返回的记录数量（null 或<0时，返回所有的记录）
     * @return
     */
    public List<OrderCreateBody> getResendList(Integer number,Integer logType) {
        List<OrderCreateBody> orderCreateBodyList = dao.getResendList(number,logType);
        if (orderCreateBodyList != null && orderCreateBodyList.size() > 0) {
            List<Long> triggerByIdList = orderCreateBodyList.stream().map(orderCreateBody -> orderCreateBody.getTriggerBy().getId()).collect(Collectors.toList());
            Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(triggerByIdList);
            orderCreateBodyList.forEach(orderCreateBody -> {
                if (nameMap.get(orderCreateBody.getTriggerBy().getId()) != null) {
                    orderCreateBody.getTriggerBy().setName(nameMap.get(orderCreateBody.getTriggerBy().getId()));
                }
            });
        }
        return orderCreateBodyList;
    }

    public List<OrderCondition> getReportResendMessage(Date startDate, Date endDate, Integer topNum) {

        List<OrderCondition> list = Lists.newArrayList(); //mark on 2020-2-11 web端去md_customer //dao.getReportResendMessage(startDate, endDate, topNum);
        setPaymentTypeAndNames(list);

        return list;
    }

    public List<OrderCondition> getReportResendMessageByOrderIds(String quarter, List<Long> ids) {

        List<OrderCondition> list = Lists.newArrayList();//mark on 2020-2-11 web端去md_customer  //dao.getReportResendMessageByOrderIds(quarter, ids);
        setPaymentTypeAndNames(list);

        return list;
    }

    private void setPaymentTypeAndNames(List<OrderCondition> list){
        if (list != null && list.size() > 0) {
            List<Long> kefuIdList = list.stream().map(orderCondition -> orderCondition.getKefu().getId()).collect(Collectors.toList());
            List<Long> salesIdList = list.stream().map(orderCondition -> orderCondition.getCustomer().getSales().getId()).collect(Collectors.toList());
            Map<Long, String> kefuNameMap = MSUserUtils.getNamesByUserIds(kefuIdList);
            Map<Long, String> salesNameMap = MSUserUtils.getNamesByUserIds(salesIdList);
            //切换为微服务
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            for (OrderCondition item : list) {
                if (item.getCustomer() != null &&
                        item.getCustomer().getFinance() != null && item.getCustomer().getFinance().getPaymentType() != null
                        && StringUtils.isNotBlank(item.getCustomer().getFinance().getPaymentType().getValue())) {
                    item.getCustomer().getFinance().setPaymentType(paymentTypeMap.get(item.getCustomer().getFinance().getPaymentType().getValue()));
                }
                if (kefuNameMap.get(item.getKefu().getId()) != null){
                    item.getKefu().setName(kefuNameMap.get(item.getKefu().getId()));
                }
                if (salesNameMap.get(item.getCustomer().getSales().getId()) != null){
                    item.getCustomer().getSales().setName(kefuNameMap.get(item.getCustomer().getSales().getId()));
                }
            }
        }
    }

}
