package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.kkl.kklplus.entity.rpt.RPTReminderResponseTimeEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerReminderSearch;
import com.kkl.kklplus.entity.rpt.search.RPTReminderResponseTimeSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerReminderRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerReminderRptFeignFallbackFactory.class)
public interface MSCustomerReminderRptFeign {

    /**
     * 获取客户每日催单明细
     */
    @PostMapping("/customerReminder/getCustomerReminderList")
    MSResponse<List<RPTCustomerReminderEntity>> getCustomerReminderList(@RequestBody RPTCustomerReminderSearch search);


    /**
     * 分页获取催单回复时效明细
     */
    @PostMapping("/customerReminder/getReminderResponseTimeList")
    MSResponse<MSPage<RPTReminderResponseTimeEntity>> getReminderResponseTimeList(@RequestBody RPTReminderResponseTimeSearch search);
}
