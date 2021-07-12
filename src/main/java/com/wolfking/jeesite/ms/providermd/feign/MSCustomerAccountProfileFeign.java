package com.wolfking.jeesite.ms.providermd.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDCustomerAccountProfile;
import com.wolfking.jeesite.ms.providermd.fallback.MSCustomerAccountProfileFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-md", fallbackFactory = MSCustomerAccountProfileFallbackFactory.class)
public interface MSCustomerAccountProfileFeign {
    /**
     * 根据id获取单个客户账户资料
     * @param id
     * @return
     */
    @GetMapping("/customerAccountProfile/getById/{id}")
    MSResponse<MDCustomerAccountProfile> getById(@PathVariable("id") Long id);

    /**
     * 根据CustomerId和OrderApproveFlag获取客户账户资料
     * @param mdCustomerAccountProfile
     * @return
     */
    @PostMapping("/customerAccountProfile/findByCustomerIdAndOrderApproveFlag")
    MSResponse<List<MDCustomerAccountProfile>> findByCustomerIdAndOrderApproveFlag(@RequestBody MDCustomerAccountProfile mdCustomerAccountProfile);

    /**
     * 新增客户账号资料
     * @param mdCustomerAccountProfile
     * @return
     */
    @PostMapping("/customerAccountProfile/insert")
    MSResponse<Integer> insert(@RequestBody MDCustomerAccountProfile mdCustomerAccountProfile);

    /**
     * 修改客户账号资料
     * @param mdCustomerAccountProfile
     * @return
     */
    @PutMapping("/customerAccountProfile/update")
    MSResponse<Integer> update(@RequestBody MDCustomerAccountProfile mdCustomerAccountProfile);

    /**
     * 删除客户账号资料
     * @param mdCustomerAccountProfile
     * @return
     */
    @DeleteMapping("/customerAccountProfile/delete")
    MSResponse<Integer> delete(@RequestBody MDCustomerAccountProfile mdCustomerAccountProfile);
}
