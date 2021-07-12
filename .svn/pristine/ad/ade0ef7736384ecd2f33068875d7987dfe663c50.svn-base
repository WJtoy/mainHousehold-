package com.wolfking.jeesite.ms.providerrpt.customer.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;
import com.wolfking.jeesite.ms.providerrpt.customer.fallback.CtCustomerNewOrderMonthRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.CustomerNewOrderMonthRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = CtCustomerNewOrderMonthRptFeignFallbackFactory.class)
public interface CtCustomerNewOrderMonthRptFeign {
    /**
     * 获取客户每月下单列表数据
     * @return
     */
    @GetMapping("/customer/customerNewOrderMonthRpt/getMonthOrderList")
    MSResponse<MSPage<RPTCustomerNewOrderDailyRptEntity>> getCustomerNewOrderMonthRptList(@RequestBody RPTSearchCondtion rptSearchCondtion);
}
