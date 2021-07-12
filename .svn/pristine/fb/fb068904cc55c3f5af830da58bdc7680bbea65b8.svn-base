package com.wolfking.jeesite.ms.cc.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.vm.ReminderListModel;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.cc.feign.fallback.CCCustomerReminderFeignFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 客户催单列表
 * 催单(客诉功能其一)
 * 调用微服务：provider-customer-compliant
 */
@FeignClient(name = "provider-customer-compliant", fallbackFactory = CCCustomerReminderFeignFactory.class)
public interface CCCustomerReminderFeign {

    //region 催单

    //region 列表及读取

    /**
     * 待回复列表
     */
    @RequestMapping("/customer//reminders/waitReplyList")
    MSResponse<MSPage<ReminderListModel>> waitReplyList(@RequestBody ReminderPageSearchModel serachModel);

    /**
     * 查询已回复列表
     */
    @PostMapping("/customer/reminders/haveRepliedList")
    MSResponse<MSPage<ReminderListModel>> haveRepliedList(@RequestBody ReminderPageSearchModel reminderPageSearchModel);

    /**
     * 分页查询所有催单列表
     */
    @PostMapping("/customer/reminders/search")
    MSResponse<MSPage<ReminderListModel>> getReminderPage(@RequestBody ReminderPageSearchModel serachModel);

    //endregion

}
