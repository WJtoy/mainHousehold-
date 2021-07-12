package com.wolfking.jeesite.ms.recharge.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.ms.providersys.feign.MSSysUserWhiteListFeign;
import com.wolfking.jeesite.ms.recharge.feign.CustomerOfflineRechargeFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author WSJ
 * @date 2018/9/25 10:12
 **/
@Component
public class CustomerOfflineRechargeFallbackFactory implements FallbackFactory<CustomerOfflineRechargeFeign> {

    @Override
    public CustomerOfflineRechargeFeign create(Throwable throwable) {
        return new CustomerOfflineRechargeFeign() {

            @Override
            public MSResponse<Integer> save(CustomerOfflineRecharge customerOfflineRecharge) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<CustomerOfflineRecharge>> findPendingList(CustomerOfflineRechargeSearch customerOfflineRechargeSearch) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<CustomerOfflineRecharge>> findHasReviewList(CustomerOfflineRechargeSearch customerOfflineRechargeSearch) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> approved(CustomerOfflineRecharge customerOfflineRecharge) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> invalid(CustomerOfflineRecharge customerOfflineRecharge) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<CustomerOfflineRecharge>> findListForCustomer(CustomerOfflineRechargeSearch customerOfflineRechargeSearch) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
