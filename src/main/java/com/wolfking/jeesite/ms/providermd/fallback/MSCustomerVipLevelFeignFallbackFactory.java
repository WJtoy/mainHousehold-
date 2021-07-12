package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerVipLevelFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSCustomerVipLevelFeignFallbackFactory implements FallbackFactory<MSCustomerVipLevelFeign> {
    @Override
    public MSCustomerVipLevelFeign create(Throwable cause) {
        return new MSCustomerVipLevelFeign(){

            @Override
            public MSResponse<MSPage<MDCustomerVipLevel>> findList(MDCustomerVipLevel mdCustomerVipLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDCustomerVipLevel>> findAllIdAndNameList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> insert(MDCustomerVipLevel mdCustomerVipLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(MDCustomerVipLevel mdCustomerVipLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(MDCustomerVipLevel mdCustomerVipLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerVipLevel> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getByName(String name) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getByValue(Integer value) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
