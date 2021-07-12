package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.ms.providermd.feign.CustomerProductModelFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerProductModelFeignFallbackFactory implements FallbackFactory<CustomerProductModelFeign> {

    @Override
    public CustomerProductModelFeign create(Throwable throwable) {
        return new CustomerProductModelFeign() {

            @Override
            public MSResponse<CustomerProductModel> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<CustomerProductModel>> getList(CustomerProductModel customerProductModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<CustomerProductModel> insert(CustomerProductModel customerProductModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(CustomerProductModel customerProductModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(CustomerProductModel customerProductModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<CustomerProductModel>> getListByField(Long customerId, Long productId) {
                return  new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据customerId和ProductId从客户产品型号中获取客户产品型号，产品名称，客户产品id，及ID
             *
             * @param customerId
             * @param productId
             * @return
             */
            @Override
            public MSResponse<List<CustomerProductModel>> findListByCustomerAndProduct(Long customerId, Long productId) {
                return  new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
