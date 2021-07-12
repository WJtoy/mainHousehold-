package com.wolfking.jeesite.ms.providersys.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.sys.SysArea;
import com.wolfking.jeesite.ms.providersys.fallback.MSSysAreaFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-sys", fallbackFactory = MSSysAreaFallbackFactory.class)
public interface MSSysAreaFeign {

    /**
     * 新增区域
     * @param sysArea
     * @return
     */
    @PostMapping("/area/insert")
    MSResponse<Integer> insert(@RequestBody SysArea sysArea);

    /**
     * 修改区域
     * @param sysArea
     * @return
     */
    @PutMapping("/area/update")
    MSResponse<Integer> update(@RequestBody SysArea sysArea);

    /**
     * 根据id从DB中获取数据
     * @param id
     * @return
     */
    @GetMapping("/area/get/{id}")
    MSResponse<SysArea> get(@PathVariable("id") Long id);

    /**
     * 根据id从缓存中获取数据
     * @param id
     * @return
     */
    @GetMapping("/area/getFromCache/{id}")
    MSResponse<SysArea> getFromCache(@PathVariable("id") Long id);

    /**
     * 根据id从缓存中获取数据
     * @param id
     * @return
     */
    @GetMapping("/area/getFromCache")
    MSResponse<SysArea> getFromCache(@RequestParam("id") Long id, @RequestParam("type") Integer type);

    /**
     * 分页查询
     * @param sysArea
     * @return
     */
    @PostMapping("/area/findSpecList")
    MSResponse<MSPage<SysArea>> findSpecList(@RequestBody SysArea sysArea);

    /**
     * 根据区域类型获取区域列表
     * @param type
     * @return
     */
    @GetMapping("/area/findListByType")
    MSResponse<List<SysArea>> findListByType(@RequestParam("type") Integer type);

    /**
     * 根据区域类型从缓存中获取区域列表
     * @param type
     * @return
     */
    @GetMapping("/area/findListByTypeFromCache")
    MSResponse<List<SysArea>> findListByTypeFromCache(@RequestParam("type") Integer type, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    /**
     * 根据区域类型从缓存中获取省市区域列表
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/area/findListExcludeTownFromCache")
    MSResponse<List<SysArea>> findListExcludeTownFromCache(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    /**
     * 根据区域类型和父区域id从缓存中获取区域列表
     * @param type
     * @param parentId
     * @return
     */
    @PostMapping("/area/findListByTypeAndParentFromCache")
    MSResponse<List<SysArea>> findListByTypeAndParentFromCache(@RequestParam("type") Integer type, @RequestParam("parentId") Long parentId);

    /**
     * 根据区域类型和父区域id从缓存中获取(statusFlag=0)区域列表
     * @param type
     * @param parentId
     * @return
     */
    @PostMapping("/area/findListByTypeAndParentNewFromCache")
    MSResponse<List<SysArea>> findListByTypeAndParentNewFromCache(@RequestParam("type") Integer type, @RequestParam("parentId") Long parentId);

    /**
     * 根据区域id删除区域
     * @param id
     * @return
     */
    @DeleteMapping("/area/delete/{id}")
    MSResponse<Integer> delete(@PathVariable("id") Long id);

    /**
     * 根据区域id删除区域
     * @param id
     * @return
     */
    @DeleteMapping("/area/delete")
    MSResponse<Integer> deleteNew(@RequestParam("id") Long id, @RequestParam("updateById") Long updateById, @RequestParam("updateDate") Long updateDate);

    /**
     * 地址解析
     * @param province 省
     * @param city  市
     * @param district  区
     * @param street  街道
     * @return
     */
    @PostMapping("/area/decodeAddress")
    MSResponse<String[]> decodeAddress(@RequestParam("province") String province, @RequestParam("city") String city, @RequestParam("district") String district,  @RequestParam("street") String street);

    /**
     * 地址解析
     * @param province 省
     * @param city  市
     * @param district  区
     * @return
     */
    @PostMapping("/area/decodeDistrictAddress")
    MSResponse<String[]> decodeDistrictAddress(@RequestParam("province") String province, @RequestParam("city") String city, @RequestParam("district") String district);

    /**
     * 通过区Id获取区，市，省id及名称
     * @param id
     * @return
     */
    @GetMapping("/area/getThreeLevelAreaById/{id}")
    MSResponse<SysArea> getThreeLevelAreaById(@PathVariable("id") Long id);

    /**
     * 通过区Id获取区，市，省id及名称
     * @param id
     * @return
     */
    @GetMapping("/area/getThreeLevelAreaByIdFromCache/{id}")
    MSResponse<SysArea> getThreeLevelAreaByIdFromCache(@PathVariable("id") Long id);

    /**
     * 根据区县id，街道id从缓存中获取街道信息
     * @param areaId
     * @param townId
     * @return
     */
    @GetMapping("/area/getTownFromCache")
    MSResponse<SysArea> getTownFromCache(@RequestParam("areaId") Long areaId, @RequestParam("townId") Long townId);

    /**
     * 通过父级id串获取
     * @param parentIds
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PostMapping("/area/findByParentIdsLike")
    MSResponse<MSPage<SysArea>> findByParentIdsLike(@RequestParam("parentIds") String parentIds, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    /**
     * 根据区域id列表查询区域详细信息列表
     * @param areaIds
     * @return
     */
    @PostMapping("/area/findListByAreaIdList")
    MSResponse<List<SysArea>> findListByAreaIdList(@RequestBody List<Long> areaIds);

    /**
     * 根据id列表获取区县集合
     * @param areaList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PostMapping("/area/findDistrictListByAreas")
    MSResponse<MSPage<SysArea>> findDistrictListByAreas(@RequestBody List<SysArea> areaList, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);


    /**
     * 根据父id串和区域类型分页返回区域Id列表
     * @param sysArea
     * @return
     */
    @PostMapping("/area/findIdByParentIdsAndType")
    MSResponse<MSPage<Long>> findIdByParentIdsAndType(@RequestBody SysArea sysArea);

    /**
     * 为网点或师傅获取区域信息
     * @param ids
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PostMapping("/area/findAreasForServicePointOrEngineer")
    MSResponse<MSPage<SysArea>> findAreasForServicePointOrEngineer(@RequestBody List<Long> ids, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    /**
     *
     * @param areaIds
     * @return
     */
    @PostMapping("/area/findSpecListByIds")
    MSResponse<List<SysArea>> findSpecListByIds(@RequestBody List<Long> areaIds);

    /**
     * 从缓存中获取省,市区域列表数据
     * @return
     */
    @GetMapping("/area/findProvinceAndCityListFromCache")
    MSResponse<List<SysArea>> findProvinceAndCityListFromCache();

    /**
     * 根据区域id获取三个级别区域id列表
     * @param ids
     * @return
     */
    @GetMapping("/area/findThreeLevelAreaIdByIds")
    MSResponse<List<SysArea>> findThreeLevelAreaIdByIds(@RequestBody List<Long> ids);

    /**
     * 根据区域id列表返回区县的id，name
     * @param areaIds
     * @return
     */
    @PostMapping("/area/findDistrictNameListByAreaIds")
    MSResponse<List<SysArea>> findDistrictNameListByAreaIds(@RequestBody List<Long> areaIds);

    /**
     * 根据区域类型和父级区域id查询区域列表
     * @param type
     * @param parentIds
     * @return
     *  返回属性为：id，name，parentId
     */
    @PostMapping("/area/findListByTypeAndParentIds")
    MSResponse<List<SysArea>> findListByTypeAndParentIds(@RequestParam("type") Integer type, @RequestBody List<Long> parentIds);

    /**
     * 分页获取省，市，区/县 的id，parentId， name， type 数据  2020-11-13
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/area/findList")
    MSResponse<MSPage<SysArea>> findList(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    /**
     * 根据区/县id获取街道的数量2020-11-13
     * @return
     */
    @GetMapping("/area/getIdCountByParentId/{id}")
    MSResponse<Integer> getSubAreaCountByAreaId(@PathVariable("id") Long id);

    /**
     * 更新区域状态
     * @param sysArea
     * @return
     */
    @PutMapping("/area/updateStatus")
    MSResponse<Integer> updateStatus(SysArea sysArea);


    /**
     * 获取全国街道，区县数量
     * @return
     */
    @GetMapping("/area/getAllAreaCountForRPT")
    MSResponse<NameValuePair<Integer,Integer>> getAllAreaCountForRPT();

}
