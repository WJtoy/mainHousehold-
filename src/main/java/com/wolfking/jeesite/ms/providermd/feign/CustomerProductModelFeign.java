package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.ms.providermd.fallback.CustomerProductModelFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户产品型号服务
 * 调用微服务：providerMD
 */
@FeignClient(name = "provider-md", fallbackFactory = CustomerProductModelFeignFallbackFactory.class)
public interface CustomerProductModelFeign {

    @GetMapping("/customerProductModel/get/{id}")
    MSResponse<CustomerProductModel> getById(@PathVariable("id") Long id);

    @PostMapping("/customerProductModel/getList")
    MSResponse<MSPage<CustomerProductModel>> getList(@RequestBody CustomerProductModel customerProductModel);

    @PostMapping("/customerProductModel/save")
    MSResponse<CustomerProductModel> insert(@RequestBody CustomerProductModel customerProductModel);

    @PostMapping("/customerProductModel/update")
    MSResponse<Integer> update(@RequestBody CustomerProductModel customerProductModel);

    @PostMapping("/customerProductModel/delete")
    MSResponse<Integer> delete(@RequestBody CustomerProductModel customerProductModel);

    @GetMapping("/customerProductModel/getListByField")
    MSResponse<List<CustomerProductModel>> getListByField(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId);

    /**
     * 根据customerId和ProductId从客户产品型号中获取客户产品型号，产品名称，客户产品id，及ID
     * @param customerId
     * @param productId
     * @return
     */
    @GetMapping("/customerProductModel/findListByCustomerAndProduct")
    MSResponse<List<CustomerProductModel>> findListByCustomerAndProduct(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId);
}
