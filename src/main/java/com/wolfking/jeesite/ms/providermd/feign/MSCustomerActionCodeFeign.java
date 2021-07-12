package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.entity.md.dto.MDErrorActionDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerActionCodeFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "provider-md", fallbackFactory= MSCustomerActionCodeFeignFallbackFactory.class)
public interface MSCustomerActionCodeFeign {
    /**
     * 添加客户处理代码
     * @param mdErrorActionDto
     * @return
     */
    @PostMapping("/actionCode/insertNew")
    MSResponse<Integer> insertCustomerActionCode(@RequestBody MDErrorActionDto mdErrorActionDto);

    /**
     * 根据客户id+产品id+id获取故障处理(缓存中获取)
     * @param customerId
     * @param productId
     * @param id
     * @return
     */
    @PostMapping("/actionCode/getByProductIdAndCustomerIdFromCache")
    MSResponse<MDActionCode> getByProductIdAndCustomerIdFromCache(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId,
                                                                  @RequestParam("id") Long id);

    /**
     * 根据客户id,产品id,故障分类ids获取故障处理(从缓存获取)
     * @param customerId
     * @param nameValuePairs key:产品id,value:故障处理id
     * @return
     */
    @PostMapping("/actionCode/findListByProductIdAndIdsFromCache")
    MSResponse<List<MDActionCode>> findListByProductIdAndIdsFromCache(@RequestParam("customerId") Long customerId, @RequestBody List<NameValuePair<Long,Long>> nameValuePairs);
}
