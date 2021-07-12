package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialItem;
import com.wolfking.jeesite.ms.providermd.fallback.AuxiliaryMaterialItemFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * B2BCenter微服务接口调用
 */
@FeignClient(name = "provider-md", fallbackFactory = AuxiliaryMaterialItemFeignFallbackFactory.class)
public interface AuxiliaryMaterialItemFeign {

    @GetMapping("/auxiliaryMaterialItem/get/{id}")
    MSResponse<MDAuxiliaryMaterialItem> get(@PathVariable("id") Long id);

    @PostMapping("/auxiliaryMaterialItem/getList")
    MSResponse<MSPage<MDAuxiliaryMaterialItem>> getList(@RequestBody MDAuxiliaryMaterialItem auxiliaryMaterialItem);

    @PostMapping("/auxiliaryMaterialItem/getListByProductId")
    MSResponse<List<MDAuxiliaryMaterialItem>> getListByProductId(@RequestBody List<String> productIds);

    @PostMapping("/auxiliaryMaterialItem/insert")
    MSResponse<MDAuxiliaryMaterialItem> insert(@RequestBody MDAuxiliaryMaterialItem auxiliaryMaterialItem);

    @PutMapping("/auxiliaryMaterialItem/update")
    MSResponse<Integer> update(@RequestBody MDAuxiliaryMaterialItem auxiliaryMaterialItem);

    @DeleteMapping("/auxiliaryMaterialItem/delete")
    MSResponse<Integer> delete(@RequestBody MDAuxiliaryMaterialItem auxiliaryMaterialItem);

    @GetMapping("/auxiliaryMaterialItem/findAllList")
    MSResponse<List<MDAuxiliaryMaterialItem>> findAllList();

}
