package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerGallery;
import com.kkl.kklplus.entity.md.dto.MDCustomerGalleryStreamLineDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerFeignFallbackFactory;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerGalleryFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户产品图库
 * 按客户+产品二级分来维护
 */
@FeignClient(name = "provider-md", fallbackFactory = MSCustomerGalleryFeignFallbackFactory.class)
public interface MSCustomerGalleryFeign {


    /**
     * 新增图库
     */
    @PostMapping(value="/customer/gallery/add")
    MSResponse<Long> add(@RequestBody MDCustomerGallery gallery);


    /**
     * 删除图库
     */
    @DeleteMapping("/customer/gallery/delete")
    MSResponse<Integer> deleteById(@RequestBody MDCustomerGallery gallery);

    /**
     * 按客户+二级分类逻辑删除图库
     */
    @DeleteMapping("/customer/gallery/deleteByCustomerAndTypeItem")
    MSResponse<Integer> deleteByCustomerAndTypeItem(@RequestBody MDCustomerGallery entity);

    /**
     * 按客户+二级分类重载到缓存
     * @param customerId    客户id
     * @param productTypeItemId 产品二级分类
     */
    @GetMapping("/customer/gallery/loadToRedis/{customerId}/{productTypeItemId}")
    MSResponse<Integer> loadToRedisByProductTypeItem(@PathVariable("customerId") Long customerId,@PathVariable("productTypeItemId") Long productTypeItemId);

    /**
     * 按客户+二级分类读取图库精简列表
     * @param customerId    客户id
     * @param productTypeItemId 产品二级分类
     */
    @GetMapping("/customer/gallery/findByProductTypeItem/{customerId}/{productTypeItemId}")
    MSResponse<List<MDCustomerGalleryStreamLineDto>> findByProductTypeItem(@PathVariable("customerId") Long customerId, @PathVariable("productTypeItemId") Long productTypeItemId);
}
