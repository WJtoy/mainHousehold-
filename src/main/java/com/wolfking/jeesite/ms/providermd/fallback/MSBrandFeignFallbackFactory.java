package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDBrand;
import com.wolfking.jeesite.ms.providermd.feign.MSBrandFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSBrandFeignFallbackFactory implements FallbackFactory<MSBrandFeign> {
    @Override
    public MSBrandFeign create(Throwable throwable) {
        return new MSBrandFeign() {
            @Override
            public MSResponse<MDBrand> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getIdByCode(MDBrand mdBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> getIdByName(MDBrand mdBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDBrand>> findAllList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<MDBrand>> findList(MDBrand mdBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> insert(MDBrand mdBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(MDBrand mdBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(MDBrand mdBrand) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
