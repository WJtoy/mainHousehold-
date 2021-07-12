package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerGallery;
import com.kkl.kklplus.entity.md.dto.MDCustomerGalleryStreamLineDto;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerGalleryFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@Slf4j
public class MSCustomerGalleryFeignFallbackFactory implements FallbackFactory<MSCustomerGalleryFeign> {

    @Override
    public MSCustomerGalleryFeign create(Throwable throwable) {
        return new MSCustomerGalleryFeign() {

            /**
             * 新增图库
             */
            @Override
            public MSResponse<Long> add(@RequestBody MDCustomerGallery gallery){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            /**
             * 删除图库
             */
            @Override
            public MSResponse<Integer> deleteById(@RequestBody MDCustomerGallery gallery){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 按客户+二级分类逻辑删除图库
             */
            @Override
            public MSResponse<Integer> deleteByCustomerAndTypeItem(@RequestBody MDCustomerGallery entity){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 按客户+二级分类重载到缓存
             * @param customerId    客户id
             * @param productTypeItemId 产品二级分类
             */
            @Override
            public MSResponse<Integer> loadToRedisByProductTypeItem(@PathVariable("customerId") Long customerId, @PathVariable("productTypeItemId") Long productTypeItemId){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 按客户+二级分类读取图库精简列表
             * @param customerId    客户id
             * @param productTypeItemId 产品二级分类
             */
            @Override
            public MSResponse<List<MDCustomerGalleryStreamLineDto>> findByProductTypeItem(@PathVariable("customerId") Long customerId,@PathVariable("productTypeItemId") Long productTypeItemId){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }
}
