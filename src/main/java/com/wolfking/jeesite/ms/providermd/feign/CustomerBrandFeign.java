package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.wolfking.jeesite.ms.providermd.fallback.CustomerBrandFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 客户品牌服务
 * 调用微服务：providerMD
 */
@FeignClient(name = "provider-md", fallbackFactory = CustomerBrandFeignFallbackFactory.class)
public interface CustomerBrandFeign {

    @GetMapping("/customerBrand/get/{id}")
    MSResponse<CustomerBrand> getById(@PathVariable("id") Long id);

    @PostMapping("/customerBrand/getList")
    MSResponse<MSPage<CustomerBrand>> getList(@RequestBody CustomerBrand customerBrand);

    @PostMapping("/customerBrand/save")
    MSResponse<CustomerBrand> insert(@RequestBody CustomerBrand customerBrand);

    @PostMapping("/customerBrand/update")
    MSResponse<Integer> update(@RequestBody CustomerBrand customerBrand);

    @PostMapping("/customerBrand/delete")
    MSResponse<Integer> delete(@RequestBody CustomerBrand customerBrand);

    @GetMapping("/customerBrand/getListByCustomer")
    MSResponse<List<CustomerBrand>> getListByCustomer(@RequestParam("customerId") Long customerId);

    @GetMapping("/customerBrand/findAllList")
    MSResponse<List<CustomerBrand>> findAllList(@RequestBody CustomerBrand customerBrand);


}
