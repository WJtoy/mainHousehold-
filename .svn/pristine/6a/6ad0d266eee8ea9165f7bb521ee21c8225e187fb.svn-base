package com.wolfking.jeesite.ms.providersys.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysOffice;
import com.wolfking.jeesite.ms.providersys.fallback.MSSysOfficeFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-sys", fallbackFactory = MSSysOfficeFallbackFactory.class)
public interface MSSysOfficeFeign {

    /**
     * 根据部门id获取部门信息
     * @param id
     * @return
     */
    @GetMapping("/office/get/{id}")
    MSResponse<SysOffice> get(@PathVariable("id") Long id);

    /**
     * 插入部门信息
     * @param office
     * @return
     */
    @PostMapping("/office/insert")
    MSResponse<Integer> insert(@RequestBody  SysOffice office);

    /**
     * 更新部门信息
     * @param office
     * @return
     */
    @PutMapping("/office/update")
    MSResponse<Integer> update(@RequestBody  SysOffice office);

    /**
     * 更新部门的上级部门id
     * @param office
     * @return
     */
    @PutMapping("/office/updateParentIds")
    MSResponse<Integer> updateParentIds(@RequestBody SysOffice office);

    /**
     *
     * @param office
     * @return
     */
    @DeleteMapping("/office/delete")
    MSResponse<Integer> delete(@RequestBody SysOffice office);

    /**
     * 获取所有的部门
     * @return
     */
    @GetMapping("/office/findAllList")
    MSResponse<List<SysOffice>> findAllList();

    /**
     * 根据部门id获取部门列表
     * @param id
     * @return
     */
    @GetMapping("/office/findList/{id}")
    MSResponse<List<SysOffice>> findList(@PathVariable("id") Long id);

    /**
     * 根据父id串查询部门列表
     * @param parentIds
     * @return
     */
    @GetMapping("/office/findByParentIdsLike")
    MSResponse<List<SysOffice>> findByParentIdsLike(@RequestParam("parentIds") String parentIds);

    /**
     * 根据部门父编码查询所属的下级部门
     * @param parentCode
     * @return
     */
//    @GetMapping("/office/findSubListByParentCode")
//    MSResponse<List<SysOffice>> findSubListByParentCode(@RequestParam("parentCode") String parentCode);

    /**
     * 根据id获取id，name，code属性
     * @param id
     * @return id,name,code
     */
    @GetMapping("office/getSpecColumnById/{id}")
    MSResponse<SysOffice> getSpecColumnById(@PathVariable("id") Long id);

    /**
     * 根据部门id列表获取部门的(id,name,code)
     * @param ids
     * @return
     */
    @PostMapping("office/findSpecColumnListByIds")
    MSResponse<List<SysOffice>> findSpecColumnListByIds(@RequestBody List<Long> ids);

    /**
     * 查询当前部门及下属部门id列表
     * @param id
     * @return
     */
    @GetMapping("office/findIdListById/{id}")
    MSResponse<List<Long>> findIdListById(@PathVariable("id") Long id);

    /**
     * 根据名称和类型获取部门本身级子级
     * @param name,userType
     * @return
     */
    @PostMapping("office/findListByNameAndType")
    MSResponse<List<SysOffice>> findListByNameAndType(@RequestParam("name") String name,@RequestParam("type") Integer userType);

    /**
     * 根据名称和类型获取部门最上级
     * @param name,userType
     * @return
     */
    @PostMapping("office/findParentListByNameAndType")
    MSResponse<List<Long>> findParentListByNameAndType(@RequestParam("name") String name,@RequestParam("type") Integer userType);

    @GetMapping("office/findIdListByParentId/{parentId}")
    MSResponse<List<Long>> findIdListByParentId(@PathVariable("parentId") Long parentId);
}
