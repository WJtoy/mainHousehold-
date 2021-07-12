package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerNewFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-md", fallbackFactory = MSCustomerNewFeignFallbackFactory.class)
public interface MSCustomerNewFeign {


    @PostMapping("/customer/insertCustomerUnion")
    MSResponse<NameValuePair<Long,Long>> insertCustomerUnion(@RequestBody MDCustomer mdCustomer);


    @PutMapping("/customer/updateCustomerUnion")
    MSResponse<Integer> updateCustomerUnion(@RequestBody MDCustomer mdCustomer);

    /**
     * 根据ID,tyoe获取客户地址信息
     *
     * @param customerId,
     * @return customerId
     * addressType
     */
    @GetMapping("/customerAddress/getByCustomerIdAndType")
    MSResponse<MDCustomerAddress> getByCustomerIdAndType(@RequestParam("customerId") Long customerId, @RequestParam("addressType") Integer addressType);

    /**
     * 根据ID,获取客户地址信息
     *
     * @return customerId
     * addressType
     */
    @GetMapping("/customerAddress/getById/{id}")
    MSResponse<MDCustomerAddress> getById(@PathVariable("id") Long id);

    /**
     * 根据ID,删除客户地址信息
     *
     * @return customerId
     * addressType
     */
    @DeleteMapping("/customerAddress/delete/{id}")
    MSResponse<Integer> delete(@PathVariable("id") Long id);
    /**
     * 根据ID,获取客户所有地址信息
     *
     * @param customerId,
     * @return customerId
     * addressType
     */
    @GetMapping("/customerAddress/findListByCustomerId")
    MSResponse<List<MDCustomerAddress>> findListByCustomerId(@RequestParam("customerId") Long customerId);

    /**
     * 更新客户地址信息
     *
    * @param mdCustomerAddress
     */
    @PutMapping("/customerAddress/update")
    MSResponse<Integer> update(@RequestBody MDCustomerAddress mdCustomerAddress);

    /**
     * 插入客户地址信息
     *
    * @param mdCustomerAddress
     */
    @PostMapping("/customerAddress/insert")
    MSResponse<Long> insert(@RequestBody MDCustomerAddress mdCustomerAddress);

    /**
     * 检查客户名称是否存在
     * @param name
     * @return
     */
    @GetMapping("/customer/existByName")
    MSResponse<Long> existByName(@RequestParam("name") String name);

    /**
     * 根据客户id和地址类型从缓存中获取地址信息
     * @param customerId
     * @param
     */
    @GetMapping("/customerAddress/getByCustomerIdAndTypeFromCache")
    MSResponse<MDCustomerAddress> getByCustomerIdAndTypeFromCache(@RequestParam("customerId") Long customerId,@RequestParam("addressType") Integer addressType);





    /**
     * 根据ID,获取客户缓存
     *
     * @return customerId
     */
    @GetMapping("customer/reloadCustomerCacheById/{id}")
    MSResponse reloadCustomerCacheById(@PathVariable("id") Long id);

    @GetMapping("customer/getCustomerByIdFromCache/{id}")
    MSResponse<MDCustomer> getCustomerByIdFromCache(@PathVariable("id") Long id);
}
