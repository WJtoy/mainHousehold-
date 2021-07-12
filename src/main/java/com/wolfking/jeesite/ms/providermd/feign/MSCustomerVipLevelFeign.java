package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerVipLevelFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-md", fallbackFactory = MSCustomerVipLevelFeignFallbackFactory.class)
public interface MSCustomerVipLevelFeign {


    @PostMapping("/customerVipLevel/findList")
    MSResponse<MSPage<MDCustomerVipLevel>> findList(@RequestBody MDCustomerVipLevel mdCustomerVipLevel);

    @GetMapping("/customerVipLevel/findAllIdAndNameList")
    MSResponse<List<MDCustomerVipLevel>> findAllIdAndNameList();

    @PostMapping("/customerVipLevel/insert")
    MSResponse<Integer> insert(@RequestBody MDCustomerVipLevel mdCustomerVipLevel);


    @PutMapping("/customerVipLevel/update")
    MSResponse<Integer> update(@RequestBody MDCustomerVipLevel mdCustomerVipLevel);

    @DeleteMapping("/customerVipLevel/delete")
    MSResponse<Integer> delete(@RequestBody MDCustomerVipLevel mdCustomerVipLevel);

    @GetMapping("/customerVipLevel/getById/{id}")
    MSResponse<MDCustomerVipLevel> getById(@PathVariable("id") Long id);

    @GetMapping("/customerVipLevel/getByName")
    MSResponse<Long> getByName(@RequestParam("name") String name);

    @GetMapping("/customerVipLevel/getByValue/{value}")
    MSResponse<Long> getByValue(@PathVariable("value") Integer value);
}
