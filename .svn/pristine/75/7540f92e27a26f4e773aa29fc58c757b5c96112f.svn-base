package com.wolfking.jeesite.ms.providerrpt.feign;



import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerNewOrderDailyRptEntity;
import com.kkl.kklplus.entity.rpt.RPTSearchCondtion;


import com.wolfking.jeesite.ms.providerrpt.fallback.CustomerNewOrderDailyRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;




/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = CustomerNewOrderDailyRptFeignFallbackFactory.class)
public interface CustomerNewOrderDailyRptFeign {
    /**
     * 获取客户每日下单列表数据
     * @return
     */
    @GetMapping("/customerNewOrderDailyRpt/getList")
    MSResponse<MSPage<RPTCustomerNewOrderDailyRptEntity>> getCustomerNewOrderDailyRptList(@RequestBody RPTSearchCondtion rptSearchCondtion);
}
