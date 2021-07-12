package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerVipLevelFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSDepositLevelFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MSDepositLevelFeignFallbackFactory implements FallbackFactory<MSDepositLevelFeign> {
    @Override
    public MSDepositLevelFeign create(Throwable cause) {
        return new MSDepositLevelFeign(){

            @Override
            public MSResponse<MSPage<MDDepositLevel>> findList(int pageNo, int pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            @Override
            public MSResponse<Integer> insert(MDDepositLevel mdDepositLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(MDDepositLevel mdDepositLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(MDDepositLevel mdDepositLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getStatusByDepositLevelId(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDDepositLevel> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getByName(String name) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getByCode(String code) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDDepositLevel>> findAllLevelList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDDepositLevel>> findAllListFromCache() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDDepositLevel> getByIdFromCache(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<Long,Map<String, Object>>> getDepositLevelByServicePointIdsForFI(List<Long> servicePointIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
