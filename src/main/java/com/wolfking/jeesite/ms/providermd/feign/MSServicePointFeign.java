package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.kkl.kklplus.entity.md.dto.MDServicePointDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointSearchDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointTimeLinessSummaryDto;
import com.kkl.kklplus.entity.md.dto.MDServicePointUnionDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSServicePointFeignFallbackFactory;
import com.wolfking.jeesite.ms.tmall.md.entity.ServicePointProvinceBatch;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FeignClient(name="provider-md", fallbackFactory = MSServicePointFeignFallbackFactory.class)
public interface MSServicePointFeign {
    @GetMapping("/servicePoint/getById/{id}")
    MSResponse<MDServicePoint> getById(@PathVariable("id") Long id);

    /**
     *
     * @param id
     * @return
     * id,servicepointNo,name,useDefaultPrice,primaryId,customizePriceFlag
     */
    @GetMapping("/servicePoint/getSimpleById/{id}")
    MSResponse<MDServicePoint> getSimpleById(@PathVariable("id") Long id);

    /**
     * 从缓存中获取网点信息
     * @param id
     * @return
     */
    @GetMapping("/servicePoint/getCacheById/{id}")
    MSResponse<MDServicePoint> getCacheById(@PathVariable("id") Long id);

    /**
     * 从缓存中获取网点信息(返回有限网点字段信息(id,servicepointno,name,primaryId))
     * @param id
     * @return
     */
    @GetMapping("/servicePoint/getSimpleCacheById/{id}")
    MSResponse<MDServicePoint> getSimpleCacheById(@PathVariable("id") Long id);

    @GetMapping("/servicePoint/getServicePointIdByBankNo/{bankNo}")
    MSResponse<Long> getServicePointIdByBankNo(@PathVariable("bankNo") String bankNo, @RequestParam("exceptId") Long exceptId);

    @GetMapping("/servicePoint/getServicePointIdByContact/{contactInfo1}")
    MSResponse<Long> getServicePointIdByContact(@PathVariable("contactInfo1") String contactInfo1, @RequestParam("exceptId") Long exceptId);

    @GetMapping("/servicePoint/getServicePointNo/{servicePointNo}")
    MSResponse<Long> getServicePointNo(@PathVariable("servicePointNo") String servicePointNo, @RequestParam("exceptId") Long exceptId);

    @GetMapping("/servicePoint/getServicePointCapacity")
    MSResponse<Long> getServicePointCapacity(@RequestParam("servicePointId") Long servicePointId);

    /**
     * 获取网点的结算标准/价格
     * @param id
     * @return
     */
    @GetMapping("/servicePoint/getUseDefaultPrice")
    MSResponse<Integer> getUseDefaultPrice(@RequestParam("id") Long id);

    /**
     * 返回网点数据
     * @param ids
     * fields
     * delFlag
     * @return
     * 网点id,Name,ServicePointNo
     */
    @PostMapping("/servicePoint/findBatchByIdsByCondition")
    MSResponse<List<MDServicePointViewModel>> findBatchByIdsByCondition(@RequestBody List<Long> ids, @RequestParam("fields") List<String> fields, @RequestParam("delFlag") Integer delFlag);

    @PostMapping("/servicePoint/findList")
    MSResponse<MSPage<MDServicePoint>> findList(@RequestBody MDServicePoint mdServicePoint,@RequestParam("areaIds") List<Long> areaIds);

    @PostMapping("/servicePoint/findListReport")
    MSResponse<MSPage<MDServicePoint>> findListReport(@RequestBody MDServicePoint mdServicePoint,
                                                      @RequestParam("areaIds") List<Long> areaIds,
                                                      @RequestParam("engineerIds") List<Long> engineerIds);


    /**
     * 查询网点id列表
     * @param mdServicePointSearchDto
     * @return
     */
    @PostMapping("/servicePoint/findIdList")
    MSResponse<MSPage<Long>> findIdList(@RequestBody MDServicePointSearchDto mdServicePointSearchDto);

    @PostMapping("/servicePoint/findIdListWithPrice")
    MSResponse<MSPage<Long>> findIdListWithPrice(@RequestBody MDServicePointSearchDto mdServicePointSearchDto);

    /**
     * 按区县/街道/品类 分页查询可派单列表
     * @param mdServicePointSearchDto
     * @return
     */
    @PostMapping("/servicePoint/findServicePointIdsForPlan")
    MSResponse<MSPage<Long>> findServicePointIdsForPlan(@RequestBody MDServicePointSearchDto mdServicePointSearchDto);

    @GetMapping("/servicePoint/findListByIds")
    MSResponse<List<MDServicePoint>> findListByIds(@RequestParam("servicePointIds") List<Long> servicePointIds);

    /**
     * 通过网点ID获取网点编号和自定义价格标志
     * @param servicePointIds
     * @return
     */
    @GetMapping("/servicePoint/findServicePointNoAndCustomizePriceFlagAndUseDefaultPriceListByIds")
    MSResponse<List<MDServicePoint>> findServicePointNoAndCustomizePriceFlagAndUseDefaultPriceListByIds(@RequestParam("servicePointIds") List<Long> servicePointIds);

    @PostMapping("/servicePoint/insert")
    MSResponse<Integer> insert(@RequestBody MDServicePoint mdServicePoint);

    /**
     * 调用微服务保存网点和师傅及师傅地址   2020-05-20
     * @param mdServicePointUnionDto
     * @return
     */
    @PostMapping("/servicePoint/insertServicePointAndEngineer")
    MSResponse<NameValuePair<Long,Long>> insertServicePointAndEngineer(@RequestBody MDServicePointUnionDto mdServicePointUnionDto);

    @PutMapping("/servicePoint/update")
    MSResponse<Integer> update(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/updateRemark/{servicePointId}")
    MSResponse<Integer> updateRemark(@PathVariable("servicePointId") Long servicePointId, @RequestParam("remarks") String remarks);

    @PutMapping("/servicePoint/updatePlanRemark/{servicePointId}")
    MSResponse<Integer> updatePlanRemark(@PathVariable("servicePointId") Long servicePointId, @RequestParam("planRemark") String planRemark);

    @PutMapping("/servicePoint/updateServicePointByMap")
    MSResponse<Integer> updateServicePointByMap(@RequestBody HashMap<String,Object> hashMap);

    @PutMapping("/servicePoint/updateServicePointForKeySetting")
    MSResponse<Integer> updateServicePointForKeySetting(@RequestBody MDServicePoint mdServicePoint);

    /**
     * 更新网点是否使用自定义价格标志  2020-2-24
     * @param mdServicePoint
     * @return
     */
    @PutMapping("/servicePoint/updateCustomizePriceFlag")
    MSResponse<Integer> updateCustomizePriceFlag(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/approve")
    MSResponse<Integer> approve(@RequestParam("servicePointIds") List<Long> servicePointIds, @RequestParam("updateById") Long updateById);

    @PutMapping("/servicePoint/updateBankIssue")
    MSResponse<Integer> updateBankIssue(@RequestParam("servicePointId") Long servicePointId, @RequestParam("bankIssue") String bankIssue);

    @PutMapping("/servicePoint/appReadInsuranceClause")
    MSResponse<Integer> appReadInsuranceClause(@RequestParam("id") Long id,
                                               @RequestParam("appInsuranceFlag") Integer appInsuranceFlag,
                                               @RequestParam("updateBy") Long updateBy,
                                               @RequestParam("updateDate") Long updateDate);

    @PutMapping("/servicePoint/updateServicePointAddress")
    MSResponse<Integer> updateServicePointAddress(@RequestBody MDServicePoint servicePoint);

    @PutMapping("/servicePoint/updateServicePointBankAccountInfo")
    MSResponse<Integer> updateServicePointBankAccountInfo(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/updateAutoPlanFlag")
    MSResponse<Integer> updateAutoPlanFlag(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/updateLevel")
    MSResponse<Integer> updateLevel(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/updatePrimaryAccount")
    MSResponse<Integer> updatePrimaryAccount(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/upgradeServicePoint")
    MSResponse<Integer> upgradeServicePoint(@RequestBody MDServicePoint mdServicePoint);

    @PutMapping("/servicePoint/updateProductCategoryServicePointMapping")
    MSResponse<Integer> updateProductCategoryServicePointMapping(@RequestParam("servicePointId") Long servicePointId,
                                                                 @RequestParam("productCategoryIds") List<Long> productCategoryIds);

    @DeleteMapping("/servicePoint/delete")
    MSResponse<Integer> delete(@RequestBody MDServicePoint mdServicePoint);

    /**
     * 分页获取网点时效信息
     * @param mdServicePointDto
     * @return
     */
    @PostMapping("/servicePoint/findServicePointTimeliness")
    MSResponse<MSPage<MDServicePointDto>> findServicePointTimeliness(@RequestBody MDServicePointDto mdServicePointDto);

    /**
     * 根据id获取网点时效信息
     * @param id
     * @return
     */
    @GetMapping("/servicePoint/getServicePointTimeliness")
    MSResponse<MDServicePointDto> getServicePointTimeliness(@RequestParam("id") Long id);
    /**
     * 修改网点时效信息
     * @param mdServicePointDto
     * @return
     */
    @PostMapping("/servicePoint/updateTimeliness")
    MSResponse<Integer> updateTimeliness(@RequestBody MDServicePointDto mdServicePointDto);

    /**
     * 获取某个省下所有网点时效总量
     * @param servicePointTimeLinessSummaryDtoList
     * @return
     */
    @PostMapping("/servicePoint/findTimeLinessFlagListByAreaIds")
    MSResponse<List<MDServicePointTimeLinessSummaryDto>> findTimeLinessFlagListByAreaIds(@RequestBody List<MDServicePointTimeLinessSummaryDto> servicePointTimeLinessSummaryDtoList);

    /**
     * 根据市开启或关闭快可立补贴
     * @param mdServicePointDto
     * @return
     */
    @PostMapping("/servicePoint/updateTimelinessByArea")
    MSResponse<Integer> updateTimelinessByArea(@RequestBody MDServicePointDto mdServicePointDto);

    @PostMapping("/servicePoint/updateCustomerTimelinessByArea")
    MSResponse<Integer> updateCustomerTimelinessByArea(@RequestBody MDServicePointDto mdServicePointDto);

    /**
     * 根据市获取签约非返现网点，返回网点名称和网点编号
     * @param areaIds
     * @return
     */
    @PostMapping("/servicePoint/findIdAndPointNoByAreaIds")
    MSResponse<List<MDServicePointDto>> findIdAndPointNoByAreaIds(@RequestBody List<Long> areaIds);

    /**
     * 突击客服添加网点
     * @param servicePointUnionDto
     * @return
     */
    @PostMapping("/servicePoint/insertServicePointUnionDto")
    MSResponse<NameValuePair<Long,Long>> insertServicePointUnionDto(@RequestBody MDServicePointUnionDto servicePointUnionDto);


    /**
     * 按区县/街道/品类 分页查询可派单列表
     * @param servicePointSearchDto
     * @return
     */
    @PostMapping("/servicePoint/findServicePointIdsByAreaWithCategory")
    MSResponse<MSPage<Long>> findServicePointIdsByAreaWithCategory(@RequestBody MDServicePointSearchDto servicePointSearchDto);

    /**
     * 突击客服恢复网点使用
     * @param mdServicePoint
     * @return
     */
    @PutMapping("/servicePoint/updateStatus")
    MSResponse<Integer> updateStatus(@RequestBody MDServicePoint mdServicePoint);

    /**
     * 根据id加载单个网点缓存
     * @param id
     * @return
     */
    @GetMapping("/servicePoint/reloadServicePointCacheById/{id}")
    MSResponse<Integer> reloadServicePointCacheById(@PathVariable("id") Long id);

    /**
     * 同步网点以及自动派单区域到ES
     * @param id
     * @return
     */
    @GetMapping("/servicePoint/pushServicePointAndStationToES/{id}")
    MSResponse<Integer> pushServicePointAndStationToES(@PathVariable("id") Long id);

    /**
     * 根据网点Id列表从缓存中获取网点信息  2020-11-13
     * @param ids 网点ids列表
     * @return
     */
    @PostMapping("servicePoint/findListByIdsFromCache")
    MSResponse<List<MDServicePoint>> findListByIdsFromCache(@RequestBody List<Long> ids);

    /**
     *  此方法跟getServicePointNo()方法有点相重了，
     * @param servicePointNo
     * @return
     */
    @GetMapping("servicePoint/getIdByServicePointNoForMD")
    MSResponse<Long> getIdByServicePointNoForMD(@RequestParam("servicePointNo") String servicePointNo);

    /**
     *提供根据网点编号获取对应是否收质保金，上限，每单扣除金额--财务
     * @param servicePointNo
     * @return
     */
    @GetMapping("servicePoint/getSpecFieldsByServicePointNoForFI")
    MSResponse<MDDepositLevel> getSpecFieldsByServicePointNoForFI(@RequestParam("servicePointNo") String servicePointNo);

    /**
     *按网点名称，编号及电话号码查询，并分页显示；排除返现网点，有质保金等级的网点id列表--工单
     * @param mdServicePoint
     * @return
     */
    @PostMapping("servicePoint/findIdsByServicePointWithDepositLevelForSD")
    MSResponse<MSPage<Long>> findIdsByServicePointWithDepositLevelForSD(@RequestBody MDServicePoint mdServicePoint);

    /**
     * 修改网点是否开启互助基金标志(购买保险)
     * @param mdServicePoint
     * @return
     */
    @PutMapping("servicePoint/updateInsuranceFlagForMD")
    MSResponse<Integer> updateInsuranceFlagForMD(@RequestBody MDServicePoint mdServicePoint);

    /**
     * 更新网点未完工单数量
     * @param paramMap
     * @return
     */
    @PutMapping("servicePoint/updateUnfinishedOrderCountByMapForSD")
    MSResponse<Integer> updateUnfinishedOrderCountByMapForSD(@RequestBody Map<String,Object> paramMap);
}
