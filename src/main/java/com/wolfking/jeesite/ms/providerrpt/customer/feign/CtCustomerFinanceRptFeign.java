package com.wolfking.jeesite.ms.providerrpt.customer.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTCustomerFinanceEntity;
import com.kkl.kklplus.entity.rpt.search.RPTCustomerFinanceSearch;
import com.wolfking.jeesite.ms.providerrpt.customer.fallback.CtCustomerFinanceRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = CtCustomerFinanceRptFeignFallbackFactory.class)
public interface CtCustomerFinanceRptFeign {
    /**
     * 获取数据
     * @return
     */
    @GetMapping("/customer/customerFinance/getCustomerFinanceRptList")
    MSResponse<MSPage<RPTCustomerFinanceEntity>> getCustomerFinanceRptList(@RequestBody RPTCustomerFinanceSearch search);
}
