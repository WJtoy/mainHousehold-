package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDProduct;
import com.wolfking.jeesite.ms.providermd.feign.MSProductVerSecondFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSProductVerSecondFeignFallbackFactory implements FallbackFactory<MSProductVerSecondFeign> {
    @Override
    public MSProductVerSecondFeign create(Throwable cause) {
        return new MSProductVerSecondFeign() {

            @Override
            public MSResponse<String> getNameByIdForMD(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<NameValuePair<Long, String>>> finAllIdAndNameListForMD() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDProduct> getSpecColumnByIdForMD(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDProduct>> findSpecListByProductIdsForMD(List<Long> ids) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<MDProduct>> findListForMD(MDProduct mdProduct) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDProduct>> findAllProductListForMD() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> insertForMD(MDProduct mdProduct) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> updateProductForMD(MDProduct mdProduct) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> updateSortForMD(List<MDProduct> mdProductList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> deleteForMD(MDProduct mdProduct) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getIdByNameForMD(String name) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDProduct> getSetProductByProductIdForMD(Long productId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
