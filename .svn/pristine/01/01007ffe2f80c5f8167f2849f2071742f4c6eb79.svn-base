package com.wolfking.jeesite.ms.providersys.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.ms.providersys.fallback.MSSysUserWhiteListFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 用户白名单管理
 * */
@FeignClient(name="provider-sys", fallbackFactory = MSSysUserWhiteListFallbackFactory.class)
public interface MSSysUserWhiteListFeign {

    @PostMapping("/userWhiteList/findList")
    MSResponse<MSPage<SysUserWhiteList>> findList(@RequestBody SysUserWhiteList sysUserWhiteList);

    @PostMapping("/userWhiteList/batchInsert")
    MSResponse<Integer> batchInsert(@RequestBody List<SysUserWhiteList> sysUserWhiteList);

    /**
     * 获取所有白名单Id集合
     * */
    @GetMapping("/userWhiteList/findAllUserIdList")
    MSResponse<List<Long>> findAllIdList();

    /**
     * 根据id获取白名单
     * */
    @GetMapping("/userWhiteList/getById/{id}")
    MSResponse<SysUserWhiteList> getById(@PathVariable(value="id") Long id);

    /**
     *  根据userId从缓存中获取
     */
    @GetMapping("/userWhiteList/getByUserIdFromCache/{userId}")
    MSResponse<SysUserWhiteList> getByUserIdFromCache(@PathVariable(value="userId") Long userId);

    /**
     * 修改
     * */
    @PostMapping("/userWhiteList/update")
    MSResponse<Integer> update(@RequestBody SysUserWhiteList sysUserWhiteList);

    /**
     * 根据id物理删除
     * */
    @GetMapping("/userWhiteList/delete/{id}")
    MSResponse<Integer> delete(@PathVariable(value="id") Long id);


}
