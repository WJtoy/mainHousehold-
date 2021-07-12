package com.wolfking.jeesite.ms.cc.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.vm.ReminderListModel;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.kkl.kklplus.entity.cc.vm.ReminderSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.cc.feign.fallback.CCKefuReminderFeignFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 催单(客诉功能其一)
 * 调用微服务：provider-customer-compliant
 */
@FeignClient(name = "provider-customer-compliant", fallbackFactory = CCKefuReminderFeignFactory.class)
public interface CCKefuReminderFeign {

    //region 催单

    //region 列表及读取

    /**
     * 待回复列表
     */
    @RequestMapping("/kefu//reminders/waitReplyList")
    MSResponse<MSPage<ReminderListModel>> waitReplyList(@RequestBody ReminderPageSearchModel serachModel);

    /**
     * 查询已回复列表
     */
    @PostMapping("/kefu/reminders/haveRepliedList")
    MSResponse<MSPage<ReminderListModel>> haveRepliedList(@RequestBody ReminderPageSearchModel reminderPageSearchModel);

    /**
     * 已处理列表(跟单/客户)
     */
    @RequestMapping("/kefu/reminders/processedList")
    MSResponse<MSPage<ReminderListModel>> processedList(@RequestBody ReminderPageSearchModel serachModel);

    /**
     * 查询工单完成的催单列表
     */
    @PostMapping("/kefu/reminders/finishList")
    MSResponse<MSPage<ReminderListModel>> finishList(@RequestBody ReminderPageSearchModel reminderPageSearchModel);


    /**
     * 分页查询所有催单列表
     */
    @PostMapping("/kefu/reminders/search")
    MSResponse<MSPage<ReminderListModel>> getReminderPage(@RequestBody ReminderPageSearchModel serachModel);

    /**
     * 查询订单下催单列表
     */
    @RequestMapping("/kefu/reminders/getListByOrderId")
    MSResponse<MSPage<Reminder>> getListByOrderId(@RequestBody ReminderSearchModel serachModel);

    /**
     * 超过24小时未完成(工单未完成)催单
     */
    @PostMapping("/kefu/reminders/moreThan24HoursUnfinishedList")
    MSResponse<MSPage<ReminderListModel>> moreThan24HoursUnfinishedList(@RequestBody ReminderPageSearchModel reminderPageSearchModel);

    /**
     * 超过48小时未完成(工单未完成)催单
     */
    @PostMapping("/kefu/reminders/moreThan48HoursUnfinishedList")
    MSResponse<MSPage<ReminderListModel>> moreThan48HoursUnfinishedList(@RequestBody ReminderPageSearchModel reminderPageSearchModel);

    //endregion

}
