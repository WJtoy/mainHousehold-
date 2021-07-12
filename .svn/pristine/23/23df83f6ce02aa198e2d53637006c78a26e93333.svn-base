package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerPrice;
import com.kkl.kklplus.entity.md.dto.MDCustomerPriceDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerPriceFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@FeignClient(name="provider-md", fallbackFactory = MSCustomerPriceFeignFallbackFactory.class)
public interface MSCustomerPriceFeign {
    /**
     * 获得某客户的生效价格清单用于缓存
     * @param id 客户id
     * @return
     */
    @GetMapping("/customerPrice/findCustomerPriceWithAssociated")
    MSResponse<List<MDCustomerPriceDto>> findCustomerPriceWithAssociated(@RequestParam("id") Long id);

    /**
     * 按id列表获得价格列表(for cache)
     * @param ids 客户价格id列表
     * @return
     */
    @PostMapping("/customerPrice/findPricesByPriceIds")
    MSResponse<List<MDCustomerPriceDto>> findPricesByPriceIds(@RequestBody List<Long> ids);

    /**
     * 获得待审核价格清单
     * @param customerPriceDto 查询条件
     * @return
     */
    @PostMapping("/customerPrice/findApprovePriceList")
    MSResponse<MSPage<MDCustomerPriceDto>> findApprovePriceList(@RequestBody MDCustomerPriceDto customerPriceDto);

    /**
     * 根据客户id从缓存中获取客户价格
     * @param id 客户id
     * @return
     */
    @GetMapping("customerPrice/findCustomerPriceWithAssociatedFromCache")
    MSResponse<List<MDCustomerPriceDto>> findCustomerPriceWithAssociatedFromCache(@RequestParam("id") Long id);

    /**
     * 审核价格
     * @param ids 客户价格id
     * @param updateById 审核人
     * @param updateDate 审核时间
     * @return
     */
    @PutMapping("/customerPrice/approvePrices")
    MSResponse<Integer> approvePrices(@RequestParam("ids") List<Long> ids,@RequestParam("updateById") Long updateById,
                                      @RequestParam("updateDate") Long updateDate);

    /**
     * 删除客户产品价格
     * @param customerId
     * @param productIds
     */
    @DeleteMapping("/customerPrice/deletePricesByCustomerAndProducts")
    MSResponse<Integer> deletePricesByCustomerAndProducts(@RequestParam("customerId") Long customerId,@RequestParam("productIds") List<Long> productIds);

    /**
     * 批量添加
     * @param customerPriceList
     */
    @PostMapping("customerPrice/batchInsert")
    MSResponse<Integer> batchInsert(@RequestBody List<MDCustomerPrice> customerPriceList);
}
