package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.wolfking.jeesite.ms.providermd.fallback.CustomerBrandCategoryFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * providerMD微服务接口调用
 */
@FeignClient(name = "provider-md", fallbackFactory = CustomerBrandCategoryFeignFallbackFactory.class)
public interface CustomerBrandCategoryFeign {


    @GetMapping("/customerBrandCategory/get/{id}")
    MSResponse<CustomerBrandCategory> getById(@PathVariable("id") Long id);

    @PostMapping("/customerBrandCategory/getList")
    MSResponse<MSPage<CustomerBrandCategory>> getList(@RequestBody CustomerBrandCategory customerBrandCategory);

    @PostMapping("/customerBrandCategory/save")
    MSResponse<CustomerBrandCategory> insert(@RequestBody CustomerBrandCategory customerBrandCategory);

    @PostMapping("/customerBrandCategory/delete")
    MSResponse<Integer> delete(@RequestBody CustomerBrandCategory customerBrandCategory);

    @GetMapping("/customerBrandCategory/findListByCustomerAndCagtegory")
    MSResponse<List<CustomerBrandCategory>> findListByCustomerAndCagtegory(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId);

    @GetMapping("/customerBrandCategory/findListByBrand")
    MSResponse<List<CustomerBrandCategory>> findListByBrand(@RequestParam("customerId") Long customerId, @RequestParam("brandId") Long brandId);

}
