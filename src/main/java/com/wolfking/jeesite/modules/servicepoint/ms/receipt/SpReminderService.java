package com.wolfking.jeesite.modules.servicepoint.ms.receipt;

import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderCreatorType;
import com.kkl.kklplus.entity.cc.ReminderLog;
import com.kkl.kklplus.entity.cc.vm.BulkRereminderCheckModel;
import com.kkl.kklplus.entity.cc.vm.ReminderTimeLinessModel;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * 网点催单
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpReminderService {


    @Autowired
    private ReminderService reminderService;


    /**
     * 根据Id+quarter读取催单信息
     *
     * @param itemFlag 是否读取催单项目列表 1:是
     */
    public Reminder getReminderById(Long id, String quarter, Integer itemFlag) {
        return reminderService.getReminderById(id, quarter, itemFlag);
    }

    /**
     * 根据订单Id+quarter读取最后一次催单信息
     */
    public Reminder getLastReminderByOrderId(Long orderId, String quarter) {
        return reminderService.getLastReminderByOrderId(orderId, quarter);
    }

    /**
     * 回复催单
     */
    @Transactional()
    public void replyReminder(Long id, Long orderId, String quarter, String remark, User user, Long servicePointId,Long itemId) {
        reminderService.replyReminder(id, orderId, quarter, remark, user, servicePointId,itemId);
    }

    /**
     * 按订单id批量读取订单催单时效信息
     * processTimeLiness:时效 ，创建或再次催单距离现在的时效
     * createDt: 创建日期时间戳
     */
    public Map<Long, ReminderTimeLinessModel> bulkGetReminderTimeLinessByOrders(BulkRereminderCheckModel searchModel){
        return reminderService.bulkGetReminderTimeLinessByOrders(searchModel);
    }

}
