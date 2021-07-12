package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAreaTimeLiness;
import com.wolfking.jeesite.ms.providermd.feign.MSAreaTimeLinessFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSAreaTimeLinessFeignFallbackFactory  implements FallbackFactory<MSAreaTimeLinessFeign> {
    @Override
    public MSAreaTimeLinessFeign create(Throwable throwable) {
        return new MSAreaTimeLinessFeign() {
            /**
             * 通过多个areaId获取-->基础资料
             *
             * @param areaIdList
             */
            @Override
            public MSResponse<List<MDAreaTimeLiness>> findListByAreaIdsForMD(List<Long> areaIdList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 通过多个areaId分页获取有效品类的时效-->基础资料
             *
             * @param areaIdList
             * @param pageNo
             * @param pageSize
             */
            @Override
            public MSResponse<MSPage<MDAreaTimeLiness>> findListByAreaIdsAndProductCategoryForMD(List<Long> areaIdList, int pageNo, int pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 批量操作-->基础资料
             *
             * @param mdAreaTimeLinessList
             * @return
             */
            @Override
            public MSResponse<Integer> batchSaveForMD(List<MDAreaTimeLiness> mdAreaTimeLinessList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id获取isOpen标识-->工单
             *
             * @param areaId
             */
            @Override
            public MSResponse<Integer> getIsOpenByAreaIdFromCacheForSD(Long areaId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id获取isOpen标识-->工单
             *
             * @param areaId
             * @param productCategoryId
             */
            @Override
            public MSResponse<Integer> getIsOpenByAreaIdAndCategoryFromCacheForSD(Long areaId, Long productCategoryId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
