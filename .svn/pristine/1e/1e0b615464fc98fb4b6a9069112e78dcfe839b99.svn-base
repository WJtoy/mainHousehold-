package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAreaTimeLiness;
import com.wolfking.jeesite.ms.providermd.fallback.MSAreaTimeLinessFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="provider-md", fallbackFactory = MSAreaTimeLinessFeignFallbackFactory.class)
public interface MSAreaTimeLinessFeign {

    /**
     * 通过多个areaId获取-->基础资料
     */
    @PostMapping("/areaTimeLiness/findListByAreaIdsForMD")
    MSResponse<List<MDAreaTimeLiness>> findListByAreaIdsForMD(@RequestBody List<Long> areaIdList);

    /**
     * 通过多个areaId分页获取有效品类的时效-->基础资料
     */
    @PostMapping("/areaTimeLiness/findListByAreaIdsAndProductCategoryForMD")
    MSResponse<MSPage<MDAreaTimeLiness>> findListByAreaIdsAndProductCategoryForMD(@RequestBody List<Long> areaIdList,@RequestParam("pageNo") int pageNo, @RequestParam("pageSize") int pageSize);

    /**
     * 批量操作-->基础资料
     * @param mdAreaTimeLinessList
     * @return
     */
    @PostMapping("/areaTimeLiness/batchSaveForMD")
    MSResponse<Integer> batchSaveForMD(@RequestBody List<MDAreaTimeLiness> mdAreaTimeLinessList);

    /**
     * 根据区域id获取isOpen标识-->工单
     */
    @GetMapping("/areaTimeLiness/getIsOpenByAreaIdFromCacheForSD")
    MSResponse<Integer> getIsOpenByAreaIdFromCacheForSD(@RequestParam("areaId") Long areaId);

    /**
     * 根据区域id获取isOpen标识-->工单
     */
    @GetMapping("/areaTimeLiness/getIsOpenByAreaIdAndCategoryFromCacheForSD")
    MSResponse<Integer> getIsOpenByAreaIdAndCategoryFromCacheForSD(@RequestParam("areaId") Long areaId, @RequestParam("productCategoryId") Long productCategoryId);
}
