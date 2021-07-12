package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerAction;
import com.kkl.kklplus.entity.md.MDCustomerProductType;
import com.kkl.kklplus.entity.md.MDCustomerProductTypeMapping;
import com.kkl.kklplus.entity.md.dto.MDCustomerActionDto;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerProductTypeFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSCustomerProductTypeFeignFactory implements FallbackFactory<MSCustomerProductTypeFeign> {
    @Override
    public MSCustomerProductTypeFeign create(Throwable throwable) {
        return new MSCustomerProductTypeFeign() {


            @Override
            public MSResponse<Integer> updateCustomerActionList(List<MDCustomerAction> mdCustomerActionList, Integer beginFlag, Integer endFlag) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDCustomerAction>> findIdAndNameByCustomerId(Long customerId, Long customerProductTypeId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<Long>> findProductIds(Long customerId, Long customerProductTypeId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDCustomerProductType>> findCustomerProductTypeListByCustomerId(Long customerId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> batchInsert(Long customerProductTypeId, List<Long> productIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDCustomerProductTypeMapping>> findProductTypeMappingByCustomerId(Long customerId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDCustomerProductTypeMapping>> findListByCustomerId(Long customerId,Long customerProductTypeId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<MDCustomerActionDto>> findCustomerActionDtoList(MDCustomerActionDto mdCustomerActionDto) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> deleteAction(MDCustomerAction mdCustomerAction) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerActionDto> getCustomerActionDto(MDCustomerActionDto mdCustomerActionDto) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(MDCustomerAction mdCustomerAction) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> saveCustomerActionDto(MDCustomerAction mdCustomerAction) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<String>> findErrorTypeNameList(Long customerId, Long productId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> customerProductTypeInsert(MDCustomerProductType mdCustomerProductType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> customerProductTypeUpdate(MDCustomerProductType mdCustomerProductType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<MDCustomerProductType>> customerProductTypeFindList(Long customerId, Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerProductType> customerProductTypeGetById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> customerProductTypeDelete(MDCustomerProductType mdCustomerProductType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerProductType> getByCustomerIdAndName(Long customerId, String name) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

}
