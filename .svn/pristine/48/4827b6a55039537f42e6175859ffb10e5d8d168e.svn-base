package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialCategory;
import com.wolfking.jeesite.ms.providermd.fallback.AuxiliaryMaterialCategoryFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * B2BCenter微服务接口调用
 */
@FeignClient(name = "provider-md", fallbackFactory = AuxiliaryMaterialCategoryFeignFallbackFactory.class)
public interface AuxiliaryMaterialCategoryFeign {

    @GetMapping("/auxiliaryMaterialCategory/get/{id}")
    MSResponse<MDAuxiliaryMaterialCategory> get(@PathVariable("id") Long id);

    @PostMapping("/auxiliaryMaterialCategory/getList")
    MSResponse<MSPage<MDAuxiliaryMaterialCategory>> getList(@RequestBody MDAuxiliaryMaterialCategory auxiliaryMaterialCategory);

    @PostMapping("/auxiliaryMaterialCategory/insert")
    MSResponse<MDAuxiliaryMaterialCategory> insert(@RequestBody MDAuxiliaryMaterialCategory auxiliaryMaterialCategory);

    @PutMapping("/auxiliaryMaterialCategory/update")
    MSResponse<Integer> update(@RequestBody MDAuxiliaryMaterialCategory auxiliaryMaterialCategory);

    @DeleteMapping("/auxiliaryMaterialCategory/delete")
    MSResponse<Integer> delete(@RequestBody MDAuxiliaryMaterialCategory auxiliaryMaterialCategory);

    @GetMapping("/auxiliaryMaterialCategory/findAllList")
    MSResponse<List<MDAuxiliaryMaterialCategory>> findAllList();
}
