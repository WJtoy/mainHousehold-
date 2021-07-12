package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerPrice;
import com.kkl.kklplus.entity.md.dto.MDCustomerPriceDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerPriceNewFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@FeignClient(name="provider-md", fallbackFactory = MSCustomerPriceNewFeignFallbackFactory.class)
public interface MSCustomerPriceNewFeign {
    /**
     * 获得某客户的所有价格清单
     * @param customerId 客户id
     * @param delFlag 0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
     * @return
     */
    @GetMapping("/customerPriceNew/findPrices")
    MSResponse<List<MDCustomerPriceDto>> findPricesNew(@RequestParam("customerId") Long customerId, @RequestParam("delFlag") Integer delFlag);

    /**
     * 按多个id获得客户下价格
     * @param customerIds 客户id列表
     * @param productId 产品id
     * @param serviceTypeId 服务类型id
     * @return
     */
    @PostMapping("/customerPriceNew/findPricesByCustomers")
    MSResponse<List<MDCustomerPrice>> findPricesByCustomersNew(@RequestParam("customerIds") List<Long> customerIds, @RequestParam("productId") Long productId,
                                                               @RequestParam("serviceTypeId") Long serviceTypeId);

    /**
     * 添加客户价格New
     * @param mdCustomerPrice
     * @return
     */
    @PostMapping("/customerPriceNew/insert")
    MSResponse<Integer> insert(@RequestBody MDCustomerPrice mdCustomerPrice);

    /**
     * 修改价格
     * @param mdCustomerPrice
     * @return
     */
    @PutMapping("/customerPriceNew/update")
    MSResponse<Integer> update(@RequestBody MDCustomerPrice mdCustomerPrice);

    /**
     * 修改价格
     * @param paramMap
     * @return
     */
    @PutMapping("/customerPriceNew/updatePriceByMap")
    MSResponse<Integer> updatePriceByMapNew(@RequestBody HashMap<String,Object> paramMap);

    /**
     * 批量添加或者修改
     * @param customerPriceList
     */
    @PostMapping("customerPriceNew/batchInsertOrUpdate")
    MSResponse<Integer> insertOrUpdateBatchNew(@RequestBody List<MDCustomerPrice> customerPriceList);

    /**
     * 修改客户价格为标准价
     * @return
     */
    @PutMapping("/customerPriceNew/updateCustomizePriceFlag")
    MSResponse<Integer> updateCustomizePriceFlag(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId, @RequestParam("serviceTypeIds") List<Long> serviceTypeIds,
                                                 @RequestParam("updateById") Long updateById, @RequestParam("updateDate") String updateDate);

    /**
     * 获得某客户的所有价格清单
     * @param id 客户价格id
     * @param delFlag 0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
     * @return
     */
    @GetMapping("/customerPriceNew/getPrice")
    MSResponse<MDCustomerPriceDto> getPriceNew(@RequestParam("id") Long id,@RequestParam("delFlag") Integer delFlag);

    /**
     * 根据产品和服务类型获取客户的服务价格
     * @param customerId  客户id
     * @param paramMap key 为产品id ,value为服务类型id
     * @return
     */
    @PostMapping("/customerPriceNew/findPricesByProductsAndServiceTypesFromCache")
    MSResponse<List<MDCustomerPriceDto>> findPricesByProductsAndServiceTypesFromCache(@RequestParam("customerId") Long customerId, @RequestBody HashMap<Long, Long> paramMap);

    /**
     * 根据客户id从缓存中获取客户价格
     * @param id 客户id
     * @return
     */
    @GetMapping("customerPriceNew/findCustomerPriceWithAssociatedFromCache")
    MSResponse<List<MDCustomerPriceDto>> findCustomerPriceWithAssociatedFromCache(@RequestParam("id") Long id);

    /**
     * 审核价格
     * @param ids 客户价格id
     * @param updateById 审核人
     * @param updateDate 审核时间
     * @return
     */
    @PutMapping("/customerPriceNew/approvePrices")
    MSResponse<Integer> approvePrices(@RequestParam("ids") List<Long> ids,@RequestParam("updateById") Long updateById,
                                      @RequestParam("updateDate") Long updateDate);

    /**
     * 获得待审核价格清单
     * @param customerPriceDto 查询条件
     * @return
     */
    @PostMapping("/customerPriceNew/findApprovePriceList")
    MSResponse<MSPage<MDCustomerPriceDto>> findApprovePriceList(@RequestBody MDCustomerPriceDto customerPriceDto);
}
