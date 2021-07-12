package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAppFeedbackType;
import com.kkl.kklplus.entity.md.dto.MDAppFeedbackTypeDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSAppFeedbackTypeeFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App反馈类型
 */
@FeignClient(name = "provider-md", fallbackFactory= MSAppFeedbackTypeeFeignFallbackFactory.class)
public interface MSAppFeedbackTypeFeign {

    /**
     * 获取所有app反馈类型
     */
    @GetMapping("/appFeedbackType/findAllList")
    MSResponse<List<MDAppFeedbackType>> findAllList();

    /**
     * 根据反馈类型标识获取指定app反馈类型
     */
    @GetMapping("/appFeedbackType/findListByFeedbackType")
    MSResponse<List<MDAppFeedbackType>> findListByFeedbackType(@RequestParam("feedbackType") Integer feedbackType);

    /**
     * 根据id从缓存读取
     */
    @GetMapping("/appFeedbackType/getByIdFromCache")
    MSResponse<MDAppFeedbackType> getByIdFromCache(@RequestParam("id") Long id);

    /**
     * DB查询根据id
     */
    @GetMapping("/appFeedbackType/getById")
    MSResponse<MDAppFeedbackType> getById(@RequestParam("id") Long id);


    /**
     * 分页查询
     */
    @PostMapping("appFeedbackType/findList")
    MSResponse<MSPage<MDAppFeedbackTypeDto>> findList(@RequestBody MDAppFeedbackType mdAppFeedbackType);


    /**
     * 保存数据
     */
    @PostMapping("appFeedbackType/insert")
    MSResponse<Integer> insert(@RequestBody MDAppFeedbackType appFeedbackType);

    /**
     * 修改数据
     */
    @PutMapping("appFeedbackType/update")
    MSResponse<Integer> update(@RequestBody MDAppFeedbackType appFeedbackType);

    /**
     * 获取最大的排序
     */
    @GetMapping("appFeedbackType/getMaxSortBy")
    MSResponse<Integer> getMaxSortBy();

    /**
     * 启动或者停用
     */
    @DeleteMapping("appFeedbackType/disableOrEnable")
    MSResponse<Integer> disableOrEnable(@RequestBody MDAppFeedbackType appFeedbackType);

    /**
     * 根据parentId和label判断是否已近存在
     */
    @GetMapping("appFeedbackType/existsWithParentIdAndLabel")
    MSResponse<Long> checkLabel(@RequestParam("parentId") Long parentId,@RequestParam("label") String label);


    /**
     * 根据parentId和label,value判断是否已近存在(用于操作反馈类型)
     */
    @GetMapping("appFeedbackType/checkValue")
    MSResponse<Long> checkValue(@RequestParam("parentId") Long parentId,@RequestParam("value") Integer value);

}
