package com.wolfking.jeesite.ms.recharge.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.wolfking.jeesite.ms.recharge.fallback.CustomerOfflineRechargeFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 客户线下充值
 * */
@FeignClient(name="provider-fi-recharge", fallbackFactory = CustomerOfflineRechargeFallbackFactory.class)
public interface CustomerOfflineRechargeFeign {

    @PostMapping("/customerOfflineRecharge/save")
    MSResponse<Integer> save(@RequestBody CustomerOfflineRecharge customerOfflineRecharge);

    /**
     * 财务查看客户线下待审核充值明细
     * @param customerOfflineRechargeSearch
     * */
    @PostMapping("/customerOfflineRecharge/findPendingList")
    MSResponse<MSPage<CustomerOfflineRecharge>> findPendingList(@RequestBody CustomerOfflineRechargeSearch customerOfflineRechargeSearch);

    /**
     * 财务查看客户线下充值已审核明细
     * @param customerOfflineRechargeSearch
     * */
    @PostMapping("/customerOfflineRecharge/findHasReviewList")
    MSResponse<MSPage<CustomerOfflineRecharge>> findHasReviewList(@RequestBody CustomerOfflineRechargeSearch customerOfflineRechargeSearch);

    /**
     *  审核通过
     * @param customerOfflineRecharge
     * */
    @PutMapping("/customerOfflineRecharge/approved")
    MSResponse<Integer> approved(@RequestBody CustomerOfflineRecharge customerOfflineRecharge);

    /**
     * 查看客户线下充值明细
     * @param customerOfflineRechargeSearch
     * @return
     */
    @PostMapping("/customerOfflineRecharge/findListForCustomer")
    MSResponse<MSPage<CustomerOfflineRecharge>> findListForCustomer(@RequestBody CustomerOfflineRechargeSearch customerOfflineRechargeSearch);

    /**
     *  审核无效
     * @param customerOfflineRecharge
     * */
    @PutMapping("/customerOfflineRecharge/invalid")
    MSResponse<Integer> invalid(@RequestBody CustomerOfflineRecharge customerOfflineRecharge);


}
