package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.entity.md.MDEngineerAddress;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.kkl.kklplus.entity.md.dto.MDRegionAttributesDto;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionDto;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionSummaryDto;
import com.wolfking.jeesite.ms.providermd.feign.MSEngineerAddressFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSRegionPermissionFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSRegionPermissionFeignFallbackFactory implements FallbackFactory<MSRegionPermissionFeign> {
    @Override
    public MSRegionPermissionFeign create(Throwable throwable) {

        return new MSRegionPermissionFeign() {

            @Override
            public MSResponse<List<MDRegionPermission>> findListWithCategory(MDRegionPermission regionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermission>> findListWithCategoryNew(MDRegionPermission regionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermission>> findListByAreaIdAndCategory(MDRegionPermission regionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> batchSave(List<MDRegionPermission> regionPermissions) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> batchSaveNew(List<MDRegionPermission> regionPermissions) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermission>> findListByCategoryAndCityId(MDRegionPermission regionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermission>> findListByCategoryAndCityIdNew(MDRegionPermission regionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getSubAreaStatusFromCacheForSD(MDRegionPermission mdRegionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getRemoteFeeStatusFromCacheForSD(MDRegionPermission mdRegionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getRemoteFeeStatusFromCacheNewForSD(MDRegionPermission mdRegionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getSubAreaTypeFromCacheNewForSD(MDRegionPermission mdRegionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDRegionAttributesDto> getAreaTypeFromCacheForSD(MDRegionPermission mdRegionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermissionSummaryDto>> getAreaCountByProductCategoryForRPT() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MDCustomerVipLevel> getMinStartVipLevel() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDCustomerVipLevel>> findAllIdAndNameList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> updateStatusFlag(MDCustomerVipLevel mdCustomerVipLevel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermissionDto>> findDtoListByGroupTypeAndType(Integer groupType, Integer type) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermission>> findAreaListByGroupTypeAndType(Integer groupType, Integer type) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<MDRegionPermissionDto>> findRegionPermissionDtoList(Integer groupType, Integer type) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据品类,市,区(县),街道(productCategoryId,cityId,subAreaId)获取远程区域状态->工单（远程区域）
             *
             * @param regionPermission
             * @return
             */
            @Override
            public MSResponse<Integer> getRemoteAreaStatusFromCacheForSD(MDRegionPermission regionPermission) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
