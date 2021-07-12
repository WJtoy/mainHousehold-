package com.wolfking.jeesite.ms.providermd.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerAction;
import com.kkl.kklplus.entity.md.MDCustomerProductType;
import com.kkl.kklplus.entity.md.MDCustomerProductTypeMapping;
import com.kkl.kklplus.entity.md.dto.MDCustomerActionDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerProductTypeFeignFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-md", fallbackFactory = MSCustomerProductTypeFeignFactory.class)
public interface MSCustomerProductTypeFeign {

    /**
     * 更新客户产品分类
     *
     * @return
     */
    @PostMapping("/customerAction/updateCustomerActionList")
    MSResponse<Integer> updateCustomerActionList(@RequestBody List<MDCustomerAction> mdCustomerActionList, @RequestParam("beginFlag") Integer beginFlag, @RequestParam("endFlag") Integer endFlag);

    /**
     * 获取客户产品分类故障详情
     *
     * @return
     */
    @GetMapping("/customerAction/findIdAndNameByCustomerId")
    MSResponse<List<MDCustomerAction>> findIdAndNameByCustomerId(@RequestParam("customerId") Long customerId, @RequestParam("customerProductTypeId") Long customerProductTypeId);

    /**
     * 获取客户产品分类关联的产品
     *
     * @return
     */
    @GetMapping("/customerProductType/findProductIds")
    MSResponse<List<Long>> findProductIds(@RequestParam("customerId") Long customerId, @RequestParam("customerProductTypeId") Long customerProductTypeId);

    /**
     * 获取客户产品分类列表
     *
     * @return
     */
    @GetMapping("/customerProductType/findCustomerProductTypeListByCustomerId/{customerId}")
    MSResponse<List<MDCustomerProductType>> findCustomerProductTypeListByCustomerId(@PathVariable("customerId") Long customerId);

    /**
     * 批量保存客户产品映射
     *
     * @return
     */
    @PostMapping("/customerProductTypeMapping/batchInsert")
    MSResponse<Integer> batchInsert(@RequestParam("customerProductTypeId") Long customerProductTypeId, @RequestBody List<Long> productIds);

    /**
     * 获取客户产品分类关联的平台产品列表
     *
     * @return
     */
    @GetMapping("/customerProductTypeMapping/findProductTypeMappingByCustomerId")
    MSResponse<List<MDCustomerProductTypeMapping>> findProductTypeMappingByCustomerId(@RequestParam("customerId") Long customerId);

    /**
     * 根据客户ID获取客户产品分类关联的平台产品
     *
     * @return
     */
    @GetMapping("/customerProductTypeMapping/findListByCustomerId")
    MSResponse<List<MDCustomerProductTypeMapping>> findListByCustomerId(@RequestParam("customerId") Long customerId,
                                                                        @RequestParam("customerProductTypeId") Long customerProductTypeId);

    /**
     * 获取客户产品分类故障列表
     *
     * @return
     */
    @PostMapping("/customerAction/findCustomerActionDtoList")
    MSResponse<MSPage<MDCustomerActionDto>> findCustomerActionDtoList(@RequestBody MDCustomerActionDto mdCustomerActionDto);

    /**
     * 删除客户产品分类故障列表
     *
     * @return
     */
    @DeleteMapping("/customerAction/deleteAction")
    MSResponse<Integer> deleteAction(@RequestBody MDCustomerAction mdCustomerAction);

    /**
     * 根据客户Id,客户产品分类ID,一级,二级获取故障详
     *
     * @return
     */
    @PostMapping("/customerAction/getCustomerActionDto")
    MSResponse<MDCustomerActionDto> getCustomerActionDto(@RequestBody MDCustomerActionDto mdCustomerActionDto);

    /**
     * 删除客户故障分析和故障处理
     *
     * @return
     */
    @DeleteMapping("/customerAction/delete")
    MSResponse<Integer> delete(@RequestBody MDCustomerAction mdCustomerAction);

    @PutMapping("/customerAction/saveCustomerActionDto")
    MSResponse<Integer> saveCustomerActionDto(@RequestBody MDCustomerAction mdCustomerAction);

    @GetMapping("/customerAction/findErrorTypeNameList")
    MSResponse<List<String>> findErrorTypeNameList(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId);

    /**
     * 新增客户产品分类
     *
     * @return
     */
    @PostMapping("/customerProductType/insert")
    MSResponse<Long> customerProductTypeInsert(@RequestBody MDCustomerProductType mdCustomerProductType);

    /**
     * 修改客户产品分类
     *
     * @return
     */
    @PutMapping("/customerProductType/update")
    MSResponse<Integer> customerProductTypeUpdate(@RequestBody MDCustomerProductType mdCustomerProductType);

    /**
     * 分页获取客户产品分类列表
     *
     * @return
     */
    @GetMapping("/customerProductType/findList")
    MSResponse<MSPage<MDCustomerProductType>> customerProductTypeFindList(@RequestParam("customerId") Long customerId, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    /**
     * 根据ID获取客户产品分类详情
     *
     * @return
     */
    @GetMapping("/customerProductType/getById/{id}")
    MSResponse<MDCustomerProductType> customerProductTypeGetById(@PathVariable("id") Long id);

    /**
     * 删除客户产品分类
     *
     * @return 1 删除成功  -1 下面有关联产品 不能删除
     */
    @DeleteMapping("/customerProductType/delete")
    MSResponse<Integer> customerProductTypeDelete(@RequestBody MDCustomerProductType mdCustomerProductType);


    /**
     * 判断客户产品分类名称是否存在
     *
     * @return
     */
    @PostMapping("/customerProductType/getByCustomerIdAndName")
    MSResponse<MDCustomerProductType> getByCustomerIdAndName(@RequestParam("customerId") Long customerId, @RequestParam("name") String name);
}
