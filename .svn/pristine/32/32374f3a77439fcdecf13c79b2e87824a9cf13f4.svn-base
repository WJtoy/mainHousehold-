package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.wolfking.jeesite.ms.providermd.fallback.MSDepositLevelFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-md", fallbackFactory = MSDepositLevelFeignFallbackFactory.class)
public interface MSDepositLevelFeign {

    /**
     * 分页查询质保金等级
     *
     * @return
     */
    @GetMapping("/depositLevel/findAllList")
    MSResponse<MSPage<MDDepositLevel>> findList(@RequestParam("pageNo") int pageNo, @RequestParam("pageSize") int pageSize);

    /**
     * 新增质保金等级
     *
     */
    @PostMapping("/depositLevel/insert")
    MSResponse<Integer> insert(@RequestBody MDDepositLevel mdDepositLevel);

    /**
     * 修改质保金等级
     *
     */
    @PutMapping("/depositLevel/update")
    MSResponse<Integer> update(@RequestBody MDDepositLevel mdDepositLevel);

    /**
     * 删除质保金等级
     *
     */
    @DeleteMapping("/depositLevel/delete")
    MSResponse<Integer> delete(@RequestBody MDDepositLevel mdDepositLevel);

    /**
     * 是否为启用质保金等级（-1未启用，0启用）
     *
     */
    @GetMapping("/depositLevel/getStatusByDepositLevelId/{id}")
    MSResponse<Integer> getStatusByDepositLevelId(@PathVariable("id") Long id);

    /**
     * 通过id查询质保金信息
     *
     */
    @GetMapping("/depositLevel/getById/{id}")
    MSResponse<MDDepositLevel> getById(@PathVariable("id") Long id);

    /**
     * 检查质保金等级名称是否存在
     *
     */
    @GetMapping("/depositLevel/getByName")
    MSResponse<Long> getByName(@RequestParam("name") String name);

    /**
     * 检查质保金编号是否存在
     *
     */
    @GetMapping("/depositLevel/getByCode")
    MSResponse<Long> getByCode(@RequestParam("code") String code);

    /**
     * 查询质保金等级列表
     *
     */
    @GetMapping("/depositLevel/findAllLevelList")
    MSResponse<List<MDDepositLevel>> findAllLevelList();


    /**
     * 查询质保金等级列表--缓存
     *
     */
    @GetMapping("/depositLevel/findAllListFromCache")
    MSResponse<List<MDDepositLevel>> findAllListFromCache();

    /**
     * 通过id查询质保金信息--缓存
     *
     */
    @GetMapping("/depositLevel/getByIdFromCache/{id}")
    MSResponse<MDDepositLevel> getByIdFromCache(@PathVariable("id") Long id);

    /**
     * 网点id为key, value -- (字段名 -value)
     * @param servicePointIds
     * @return
     */
    @PostMapping("/depositLevel/getDepositLevelByServicePointIdsForFI")
    MSResponse<Map<Long,Map<String, Object>>> getDepositLevelByServicePointIdsForFI(@RequestBody List<Long> servicePointIds);
}
