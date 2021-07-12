package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.wolfking.jeesite.ms.providermd.feign.CustomerBrandCategoryFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
public class CustomerBrandCategoryFeignFallbackFactory implements FallbackFactory<CustomerBrandCategoryFeign> {

    @Override
    public CustomerBrandCategoryFeign create(Throwable throwable) {
        return new CustomerBrandCategoryFeign() {

            @Override
            public MSResponse<CustomerBrandCategory> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<CustomerBrandCategory>> getList(CustomerBrandCategory customerBrandCategory) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<CustomerBrandCategory> insert(CustomerBrandCategory customerBrandCategory) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(CustomerBrandCategory customerBrandCategory) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<CustomerBrandCategory>> findListByCustomerAndCagtegory(Long customerId,Long productId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<CustomerBrandCategory>> findListByBrand(Long customerId, Long brandId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
