package com.wolfking.jeesite.ms.providersys.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysUserCustomer;
import com.sun.tracing.ProviderName;
import com.wolfking.jeesite.ms.providersys.fallback.MSSysUserCustomerFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="provider-sys", fallbackFactory = MSSysUserCustomerFallbackFactory.class)
public interface MSSysUserCustomerFeign {

    /**
     * 根据客户id获取用户id列表
     * @param customerId
     * @return
     */
    @GetMapping("/sysUserCustomer/findUserIdListByCustomerId")
    MSResponse<List<Long>> findUserIdListByCustomerId(@RequestParam("customerId") Long customerId);

    /**
     * 查询客户id列表
     * @param sysUserCustomer
     * @return
     */
    @PostMapping("/sysUserCustomer/findCustomerIdList")
    MSResponse<List<Long>> findCustomerIdList(@RequestBody SysUserCustomer sysUserCustomer);

    /**
     * 获取所有的customerId列表
     * @return
     */
    @GetMapping("/sysUserCustomer/findAllCustomerIdList")
    public MSResponse<List<Long>> findAllCustomerIdList();

    /**
     * 批量插入用户客户
     * @param userId
     * @param customerIds
     * @return
     */
    @PostMapping("/sysUserCustomer/batchInsert")
    MSResponse<Integer> batchInsert(@RequestParam("userId") Long userId, @RequestBody List<Long> customerIds);

    /**
     * 根据用户id删除用户客户
     * @param userId
     * @return
     */
    @DeleteMapping("/sysUserCustomer/deleteByUserId")
    MSResponse<Integer> deleteByUserId(@RequestParam("userId") Long userId);

}
