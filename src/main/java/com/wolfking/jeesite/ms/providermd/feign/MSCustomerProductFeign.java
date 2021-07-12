package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerProduct;
import com.kkl.kklplus.entity.md.MDProduct;
import com.kkl.kklplus.entity.md.dto.MDCustomerProductDto;
import com.kkl.kklplus.entity.md.dto.MDProductDto;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerProduct;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerProductFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name="provider-md", fallbackFactory = MSCustomerProductFeignFallbackFactory.class)
public interface MSCustomerProductFeign {

    /**
     * 从缓存中获取安装规范(API)
     * @param customerId
     * @param productId
     * @return
     */
    @GetMapping("/customerProduct/getFixSpecFromCache")
    MSResponse<MDCustomerProduct> getFixSpecFromCache(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId);

    /**
     * 根据客户id获取
     * @param customerId
     * @return
     */
    @GetMapping("/customerProduct/findByCustomer")
    MSResponse<List<MDCustomerProduct>> findByCustomer(@RequestParam("customerId") Long customerId);

    /**
     * 根据客户id获取产品Id集合
     * @param customerId
     * @return
     */
    @GetMapping("/customerProduct/findProductIdsById/{customerId}")
    MSResponse<List<Long>> findProductIdsById(@PathVariable("customerId") Long customerId);


    /**
     * 根据客户Id,或者产品id，或者产品id集合获取客户产品
     * @param mdCustomerProduct
     * @param productIds
     * @return
     */
    @PostMapping("/customerProduct/findCustomerProductsByIdsWithoutCustomerAndProduct")
    MSResponse<MSPage<MDCustomerProduct>> findCustomerProductsByIdsWithoutCustomerAndProduct(@RequestBody MDCustomerProduct mdCustomerProduct,
                                                                                                   @RequestParam("productIds") List<Long> productIds);

    /**
     * 根据客户id删除
     * @param customerId
     * @return
     */
    @DeleteMapping("/customerProduct/deleteByCustomer")
    MSResponse<Integer> deleteByCustomer(@RequestParam("customerId") Long customerId);

    /**
     * 根据客户id,产品id客户配件
     * @param customerId,productId
     * @return
     */
    @PostMapping("/customerProduct/batchInsert")
    MSResponse<Integer> batchInsert(@RequestParam("customerId") Long customerId,@RequestParam("productIds") List<Long> productIds);

    /**
     * 获取客户和产品相关信息(返回客户id，name，code和产品id,name,sort,product_category_id),根据产品sort排序
     * @param mdCustomerProductDto
     * @return
     */
    @PostMapping("customerProduct/findCustomerProductsByPaging")
    MSResponse<MSPage<MDCustomerProductDto>> findCustomerProductList(@RequestBody MDCustomerProductDto mdCustomerProductDto);


    /**
     * 分页查询
     * @param customerProduct
     * @return
     */
    @PostMapping("customerProduct/findList")
    MSResponse<MSPage<MDCustomerProductDto>> findList(@RequestBody MDCustomerProduct customerProduct);


    /**
     * 根据id获取数据
     * @param id
     * @return
     */
    @GetMapping("customerProduct/getById")
    MSResponse<MDCustomerProductDto> getById(@RequestParam("id") Long id);

    /**
     * 添加单条数据
     * @param mdCustomerProduct
     * @return
     */
    @PostMapping("customerProduct/insert")
    MSResponse<Integer> insert(@RequestBody MDCustomerProduct mdCustomerProduct);

    /**
     * 修改单条数据
     * @param mdCustomerProduct
     * @return
     */
    @PutMapping("customerProduct/update")
    MSResponse<Integer> update(@RequestBody MDCustomerProduct mdCustomerProduct);


    /**
     * 删除单条数据
     * @param mdCustomerProduct
     * @return
     */
    @DeleteMapping("customerProduct/delete")
    MSResponse<Integer> delete(@RequestBody MDCustomerProduct mdCustomerProduct);

    /**
     * 批量添加
     * @param mdCustomerProduct
     * @return
     */
    @PostMapping("customerProduct/newBatchInsert")
    MSResponse<Integer> newBatchInsert(@RequestBody MDCustomerProduct mdCustomerProduct,@RequestParam("productIds") List<Long> productIds);

    /**
     * 批量删除
     * @param mdCustomerProduct
     * @return
     */
    @DeleteMapping("customerProduct/batchDelete")
    MSResponse<Integer> batchDelete(@RequestBody MDCustomerProduct mdCustomerProduct,@RequestParam("productIds") List<Long> productIds);

    /**
     * 根据客户Id和产品id获取
     * @param customerId
     * @param productId
     * @return
     */
    @GetMapping("customerProduct/checkExistWithCustomerProduct")
    MSResponse<MDCustomerProduct> checkExistWithCustomerProduct(@RequestParam("customerId") Long customerId,@RequestParam("productId") Long productId);


    /**
     * 根据客户Id重缓存获取产品
     * @param customerId
     * @return
     */
    @GetMapping("customerProduct/findProductsByCustomerId")
    MSResponse<List<MDProduct>> findProductByCustomerIdFromCache(@RequestParam("customerId") Long customerId);


    /**
     * 查询客户的产品列表，供“客户信息”报表使用
     * 结果集中的MDProductDto对象使用name属性存储客户的产品列表
     * @param customerId
     * @param paymentType
     * @return
     */
    @GetMapping("customerProduct/findCustomerProductsWithOutCustomer")
    MSResponse<List<MDProductDto>> getCustomerProducts(@RequestParam("customerId") Long customerId,@RequestParam("paymentType") Integer paymentType);

    /**
     * 根据客户和产品获取远程费标识
     */
    @GetMapping("customerProduct/getRemoteFeeFlag")
    MSResponse<Integer> getRemoteFeeFlag(@RequestParam("customerId") Long customerId,@RequestParam("productIds") List<Long> productIds);

    /**
     * 根据客户id+二级分类id获取产品规格，属性及产品，服务
     * @param customerId    客户
     * @param productTypeItemId 二级分类
     * @return
     */
    @GetMapping("/customerProduct/getProductSpecAndInfoForCreateOrder")
    MSResponse<String> getProductSpecAndInfoForCreateOrder(@RequestParam("customerId") Long customerId,@RequestParam("productTypeItemId") Long productTypeItemId);

    /**
     * 读取客户下品类ID集合
     * @param customerId
     * @return
     */
    @GetMapping("/customerProduct/getCustomerCategories")
    MSResponse<List<Long>> getCustomerCategories(@RequestParam("customerId") Long customerId);

    /**
     * 根据客户id和产品id获取客户产品
     * @param customerId
     * @param productId
     * @return
     */
    @GetMapping("/customerProduct/getByCustomerIdAndProductId")
    MSResponse<MDCustomerProduct>  getByCustomerIdAndProductId(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId );

    /**
     * 根据客户id及产品id列表返回对应的产品列表是否有安装规范
     * @param customerId
     * @param productIds
     * @return
     */
    @PostMapping("/customerProduct/findFixSpecByCustomerIdAndProductIdsFromCacheForSD")
    MSResponse<Map<Long,Integer>> findFixSpecByCustomerIdAndProductIdsFromCacheForSD(@RequestParam("customerId") Long customerId, @RequestBody List<Long> productIds);

    /**
     * 清空安装规范
     * @param mdCustomerProduct
     * @return
     */
    @PutMapping("/customerProduct/emptyFixSpecById")
    MSResponse<Integer> emptyFixSpecById(@RequestBody MDCustomerProduct mdCustomerProduct);
}
