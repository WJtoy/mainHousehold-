package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.wolfking.jeesite.ms.providermd.feign.CustomerBrandFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CustomerBrandFeignFallbackFactory implements FallbackFactory<CustomerBrandFeign> {

    @Override
    public CustomerBrandFeign create(Throwable throwable) {
        return new CustomerBrandFeign() {

            @Override
            public MSResponse<CustomerBrand> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<CustomerBrand>> getList(CustomerBrand customerBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<CustomerBrand> insert(CustomerBrand customerBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(CustomerBrand customerBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(CustomerBrand customerBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<CustomerBrand>> getListByCustomer(Long customerId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<CustomerBrand>> findAllList(CustomerBrand customerBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
