package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerNewFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSCustomerNewFeignFallbackFactory implements FallbackFactory<MSCustomerNewFeign> {
    @Override
    public MSCustomerNewFeign create(Throwable cause) {
        return new MSCustomerNewFeign(){

            @Override
            public MSResponse<NameValuePair<Long, Long>> insertCustomerUnion(MDCustomer mdCustomer) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> updateCustomerUnion(MDCustomer mdCustomer) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerAddress> getByCustomerIdAndType(Long customerId, Integer addressType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerAddress> getById(Long addressId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
            @Override
            public MSResponse<Integer> delete(Long addressId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
            @Override
            public MSResponse<List<MDCustomerAddress>> findListByCustomerId(Long customerId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(MDCustomerAddress mdCustomerAddress) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> insert(MDCustomerAddress mdCustomerAddress) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> existByName(String customerName) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerAddress> getByCustomerIdAndTypeFromCache(Long customerId, Integer addressType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomer> getCustomerByIdFromCache(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse reloadCustomerCacheById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
