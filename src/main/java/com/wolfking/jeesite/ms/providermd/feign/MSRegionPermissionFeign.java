package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.kkl.kklplus.entity.md.MDRegionPermission;
import com.kkl.kklplus.entity.md.dto.MDRegionAttributesDto;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionDto;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionSummaryDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSRegionPermissionFeignFallbackFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-md", fallbackFactory = MSRegionPermissionFeignFallbackFactory.class)
public interface MSRegionPermissionFeign {
    /**
     * 根据品类和城市获取区域设置信息
     * @return
     */
    @PostMapping("/regionPermission/findListWithCategory")
    MSResponse<List<MDRegionPermission>> findListWithCategory(@RequestBody MDRegionPermission regionPermission);

    /**
     * 根据品类和城市获取区域设置信息
     * @return
     */
    @PostMapping("/regionPermission/findListWithCategoryNew")
    MSResponse<List<MDRegionPermission>> findListWithCategoryNew(@RequestBody MDRegionPermission regionPermission);

    @PostMapping("/regionPermission/findListByAreaIdAndCategory")
    MSResponse<List<MDRegionPermission>> findListByAreaIdAndCategory(@RequestBody MDRegionPermission regionPermission);

    /**
     * 批量操作
     * @param regionPermissions
     * @return
     */
    @PostMapping("/regionPermission/batchSave")
    MSResponse<Integer> batchSave(@RequestBody List<MDRegionPermission> regionPermissions);
    /**
     * 批量操作
     * @param regionPermissions
     * @return
     */
    @PostMapping("/regionPermission/batchSaveNew")
    MSResponse<Integer> batchSaveNew(@RequestBody List<MDRegionPermission> regionPermissions);

    /**
     * 根据城市和产品品类获取启用区域
     * @param regionPermission
     * @return
     */
    @PostMapping("/regionPermission/findListByCategoryAndCityId")
    MSResponse<List<MDRegionPermission>> findListByCategoryAndCityId(@RequestBody MDRegionPermission regionPermission);
    /**
     * 根据城市和产品品类获取启用区域
     * @param regionPermission
     * @return
     */
    @PostMapping("/regionPermission/findListByCategoryAndCityIdNew")
    MSResponse<List<MDRegionPermission>> findListByCategoryAndCityIdNew(@RequestBody MDRegionPermission regionPermission);
    /**
     * 根据省市区街道判断是否有可突击区域
     * @param mdRegionPermission
     * @return
     */
    @PostMapping("regionPermission/getSubAreaStatusFromCacheForSD")
    MSResponse<Integer> getSubAreaStatusFromCacheForSD(@RequestBody MDRegionPermission mdRegionPermission);


    /**
     * 根据市区街道街道是否有远程费
     * @param mdRegionPermission
     * @return
     */
    @PostMapping("/regionPermission/getRemoteFeeStatusFromCacheForSD")
    MSResponse<Integer> getRemoteFeeStatusFromCacheForSD(@RequestBody MDRegionPermission mdRegionPermission);


    /**
     * 根据市区街道街道是否有远程费
     * @param mdRegionPermission
     * @return
     */
    @PostMapping("regionPermission/getRemoteFeeStatusFromCacheNewForSD")
    MSResponse<Integer> getRemoteFeeStatusFromCacheNewForSD(@RequestBody MDRegionPermission mdRegionPermission);


    /**
     * 根据省市区街道判断客服类型
     * @param mdRegionPermission
     * @return
     */
    @PostMapping("/regionPermission/getSubAreaTypeFromCacheNewForSD")
    MSResponse<Integer> getSubAreaTypeFromCacheNewForSD(@RequestBody MDRegionPermission mdRegionPermission);

    /**
     * 根据客服类型获取省市区街道dto列表
     * @param groupType,type
     * @return
     */
    @GetMapping("regionPermission/findDtoListByGroupTypeAndType")
    MSResponse<List<MDRegionPermissionDto>> findDtoListByGroupTypeAndType(@RequestParam("groupType") Integer groupType, @RequestParam("type") Integer type);


    /**
     * 根根据客服类型获取省市区街道列表
     * @param groupType,type
     * @return
     */
    @GetMapping("regionPermission/findAreaListByGroupTypeAndType")
    MSResponse<List<MDRegionPermission>> findAreaListByGroupTypeAndType(@RequestParam("groupType") Integer groupType, @RequestParam("type") Integer type);

    /**
     * 根据客服类型获取省市区三级列表
     * @param groupType,type
     * @return
     */
    @GetMapping("regionPermission/findRegionPermissionDtoList")
    MSResponse<List<MDRegionPermissionDto>> findRegionPermissionDtoList(@RequestParam("groupType") Integer groupType, @RequestParam("type") Integer type);


    /**
     * 根据省市区街道判断客服类型(同时支持有街道和无街道)
     * @param mdRegionPermission
     * @return
     */
    @PostMapping("/regionPermission/getAreaTypeFromCacheVerFourthForSD")
    MSResponse<MDRegionAttributesDto> getAreaTypeFromCacheForSD(@RequestBody MDRegionPermission mdRegionPermission);


    /**
     * 根据品类汇总（突击街道，自动区/县，大客服区/县）
     * @return
     */
    @GetMapping("/regionPermission/getAreaCountByProductCategoryForRPT")
    MSResponse<List<MDRegionPermissionSummaryDto>> getAreaCountByProductCategoryForRPT();


    /**
     * 获取客服VIP最小等级
     * @return
     */
    @GetMapping("customerVipLevel/getMinStartVipLevel")
    MSResponse<MDCustomerVipLevel> getMinStartVipLevel();


    /**
     * 获取所有vip等级列表
     * @return
     */
    @GetMapping("customerVipLevel/findAllIdAndNameList")
    MSResponse<List<MDCustomerVipLevel>> findAllIdAndNameList();

    /**
     * 修改vip等级状态
     * @return
     */
    @PutMapping("customerVipLevel/updateStatusFlag")
    MSResponse<Integer> updateStatusFlag(MDCustomerVipLevel mdCustomerVipLevel);

    /**
     * 根据品类,市,区(县),街道(productCategoryId,cityId,subAreaId)获取远程区域状态->工单（远程区域）
     * @param regionPermission
     * @return
     */
    @PostMapping("regionPermission/getRemoteAreaStatusFromCacheForSD")
    MSResponse<Integer> getRemoteAreaStatusFromCacheForSD(@RequestBody MDRegionPermission regionPermission);
}
