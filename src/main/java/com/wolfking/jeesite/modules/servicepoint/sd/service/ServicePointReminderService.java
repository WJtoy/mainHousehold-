package com.wolfking.jeesite.modules.servicepoint.sd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderOrderModel;
import com.kkl.kklplus.entity.cc.vm.ReminderTimeLinessModel;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.servicepoint.ms.receipt.SpReminderService;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import com.wolfking.jeesite.ms.cc.entity.mapper.OrderReminderVMMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 网点催单服务层
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointReminderService extends SpReminderService {

    /**
     * 读取催单时效
     */
    public List<OrderReminderVM> getReminderTimeliness(List<Order> list) {
        List<OrderReminderVM> vmList = Lists.newArrayList();
        try {
            List<ReminderOrderModel> orderModelList = Lists.newArrayList();
            list.stream().forEach(t -> {
                orderModelList.add(ReminderOrderModel.builder()
                        .orderId(t.getId())
                        .quarter(t.getQuarter()).build()
                );
            });
            OrderReminderVM vm;
            if (!orderModelList.isEmpty()) {
                BulkRereminderCheckModel bulkRereminderCheckModel = new BulkRereminderCheckModel(orderModelList);
                Map<Long, ReminderTimeLinessModel> reminderModels = bulkGetReminderTimeLinessByOrders(bulkRereminderCheckModel);
                if (reminderModels != null && reminderModels.size() > 0) {
                    Order order;
                    ReminderTimeLinessModel model;
                    Date date = new Date();
                    TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                    long startDt = twoTuple.getAElement();
                    long endDt = twoTuple.getBElement();
                    for (int i = 0, size = list.size(); i < size; i++) {
                        order = list.get(i);
                        vm = OrderReminderVMMapper.INSTANCE.toReminderModel(order);
                        if (reminderModels.containsKey(order.getId())) {
                            model = reminderModels.get(order.getId());
                            if (model != null) {
                                vm.setReminderId(model.getId());
                                if (model.getCreateAt() > 0) {
                                    vm.setReminderDate(DateFormatUtils.format(model.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
                                }
                                double praiseTimeliness = DateUtils.calculateTimeliness(date,model.getTimeoutAt(),startDt,endDt);
                                vm.setCutOffTimeLiness(model.getCutOffTimeLiness());
                                vm.setCutOffLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                            }
                        }
                        vmList.add(vm);
                    }
                }
            }
        } catch (Exception e) {
            log.error("读取催单时效错误", e);
        }
        return vmList;
    }


    /**
     * 根据催单时效显示不同文本
     *
     * @param timeliness 时效(小时)
     */
    private String getCutOffTimelinessLabel(double timeliness) {
        int minutes = (int) (60 * timeliness);
        if (minutes < 40) {
            return (60 - minutes) + "分钟后超时";
        }
        if (minutes < 60) {
            return (60 - minutes) + "分钟后超时";
        }
        return "超时:" + DateUtils.minuteToTimeString(minutes - 60, "小时", "分");
    }

}
