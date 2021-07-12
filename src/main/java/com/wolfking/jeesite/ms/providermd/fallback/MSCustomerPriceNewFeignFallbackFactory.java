package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerPrice;
import com.kkl.kklplus.entity.md.dto.MDCustomerPriceDto;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerPriceNewFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class MSCustomerPriceNewFeignFallbackFactory implements FallbackFactory<MSCustomerPriceNewFeign> {
    @Override
    public MSCustomerPriceNewFeign create(Throwable throwable) {
        return new MSCustomerPriceNewFeign() {
            /**
             * 根据产品和服务类型获取客户的服务价格
             *
             * @param customerId 客户id
             * @param paramMap   key 为产品id ,value为服务类型id
             * @return
             */
            @Override
            public MSResponse<List<MDCustomerPriceDto>> findPricesByProductsAndServiceTypesFromCache(Long customerId, HashMap<Long, Long> paramMap) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据客户id从缓存中获取客户价格
             *
             * @param id 客户id
             * @return
             */
            @Override
            public MSResponse<List<MDCustomerPriceDto>> findCustomerPriceWithAssociatedFromCache(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 审核价格
             *
             * @param ids        客户价格id
             * @param updateById 审核人
             * @param updateDate 审核时间
             * @return
             */
            @Override
            public MSResponse<Integer> approvePrices(List<Long> ids, Long updateById, Long updateDate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 获得某客户的所有价格清单
             *
             * @param customerId 客户id
             * @param delFlag    0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
             * @return
             */
            @Override
            public MSResponse<List<MDCustomerPriceDto>> findPricesNew(Long customerId, Integer delFlag) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 按多个id获得客户下价格
             *
             * @param customerIds   客户id列表
             * @param productId     产品id
             * @param serviceTypeId 服务类型id
             * @return
             */
            @Override
            public MSResponse<List<MDCustomerPrice>> findPricesByCustomersNew(List<Long> customerIds, Long productId, Long serviceTypeId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 添加客户价格New
             *
             * @param mdCustomerPrice
             * @return
             */
            @Override
            public MSResponse<Integer> insert(MDCustomerPrice mdCustomerPrice) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 修改价格
             *
             * @param mdCustomerPrice
             * @return
             */
            @Override
            public MSResponse<Integer> update(MDCustomerPrice mdCustomerPrice) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 批量添加或者修改
             *
             * @param customerPriceList
             */
            @Override
            public MSResponse<Integer> insertOrUpdateBatchNew(List<MDCustomerPrice> customerPriceList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 修改客户价格为标准价
             *
             * @param customerId
             * @param productId
             * @param serviceTypeIds
             * @param updateById
             * @param updateDate
             * @return
             */
            @Override
            public MSResponse<Integer> updateCustomizePriceFlag(Long customerId, Long productId, List<Long> serviceTypeIds, Long updateById, String updateDate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 获得某客户的所有价格清单
             *
             * @param id      客户价格id
             * @param delFlag 0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
             * @return
             */
            @Override
            public MSResponse<MDCustomerPriceDto> getPriceNew(Long id, Integer delFlag) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 修改价格
             *
             * @param paramMap
             * @return
             */
            @Override
            public MSResponse<Integer> updatePriceByMapNew(HashMap<String, Object> paramMap) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 获得待审核价格清单
             *
             * @param customerPriceDto 查询条件
             * @return
             */
            @Override
            public MSResponse<MSPage<MDCustomerPriceDto>> findApprovePriceList(MDCustomerPriceDto customerPriceDto) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
