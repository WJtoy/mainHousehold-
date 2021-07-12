package com.wolfking.jeesite.ms.providersys.service;

import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.sys.SysArea;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.providersys.feign.MSSysAreaFeign;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
public class MSSysAreaService {
    @Autowired
    MSSysAreaFeign msSysAreaFeign;

    @Autowired
    MapperFacade mapper;


    /**
     * 保存区域信息
     * @param area
     */
    public void save(Area area) {
        boolean isNew = area.getIsNewRecord();
        MSErrorCode msErrorCode = MDUtils.genericSave(area, SysArea.class, isNew, isNew?msSysAreaFeign::insert:msSysAreaFeign::update);
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("保存区域信息失败,失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 根据id从DB中获取数据
     *
     * @param id
     * @return
     */
    public Area get(Long id) {
        return MDUtils.getObjNecessaryConvertType(Area.class, ()->msSysAreaFeign.get(id));
    }

    /**
     * 根据id从缓存中获取数据
     *
     * @param id
     * @return
     */
    public Area getFromCache(Long id) {
        return MDUtils.getObjNecessaryConvertType(Area.class, ()->msSysAreaFeign.getFromCache(id));
    }

    /**
     * 根据id从缓存中获取数据
     *
     * @param id
     * @param type
     * @return
     */
    public Area getFromCache(Long id, Integer type) {
        return MDUtils.getObjNecessaryConvertType(Area.class, ()->msSysAreaFeign.getFromCache(id, type));
    }

    /**
     * 分页查询
     *
     * @param area
     * @return
     */
    public List<Area> findSpecList(Area area) {
        SysArea sysArea = mapper.map(area, SysArea.class);
        if (area.getPage() != null) {
            sysArea.setPage(new MSPage<>(area.getPage().getPageNo(), area.getPage().getPageSize()));
        }
        MSResponse<MSPage<SysArea>> response = msSysAreaFeign.findSpecList(sysArea);
        if (MSResponse.isSuccess(response)) {
            MSPage<SysArea> page = response.getData();
            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                area.getPage().setPageNo(page.getPageNo());
                area.getPage().setPageSize(page.getPageSize());
                area.getPage().setCount(page.getRowCount());
                return mapper.mapAsList(page.getList(), Area.class);
            }
        } else {
            return Lists.newArrayList();
        }
        return Lists.newArrayList();
    }

    /**
     * 根据区域类型获取区域列表
     *
     * @param type
     * @return
     */
    public List<Area> findListByType(Integer type) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findListByType(type));
    }

    /**
     * 根据区域类型从缓存中获取区域列表
     *
     * @param type
     * @return
     */
    public List<Area> findListByTypeFromCache(Integer type) {
        int pageNo = 0;
        int pageSize = 499;
        List<Area>  areaList = Lists.newArrayList();

        while (true) {
            MSResponse<List<SysArea>> msResponse =  msSysAreaFeign.findListByTypeFromCache(type, pageNo, pageSize);
            if (MSResponse.isSuccess(msResponse)) {
                List<SysArea> sysAreaList = msResponse.getData();
                if (sysAreaList == null || sysAreaList.isEmpty()) {
                    break;
                } else {
                    areaList.addAll(mapper.mapAsList(sysAreaList, Area.class));
                }
            } else {
                break;
            }
            pageNo ++;
        }
        log.warn("按类型：{} 从缓存中获取的区域数量:{}", type, areaList.size());
        return areaList;
    }

    /**
     * 根据区域类型从缓存中省,市区域
     * @return
     */
    public List<Area>  findListExcludeTownFormCache()  {
        int pageNo = 0;
        int pageSize = 499;
        List<Area>  areaList = Lists.newArrayList();

        long start = System.currentTimeMillis();
        while (true) {
            MSResponse<List<SysArea>> msResponse =  msSysAreaFeign.findListExcludeTownFromCache(pageNo, pageSize);
            if (MSResponse.isSuccess(msResponse)) {
                List<SysArea> sysAreaList = msResponse.getData();
                if (sysAreaList == null || sysAreaList.isEmpty()) {
                    break;
                } else {
                    areaList.addAll(mapper.mapAsList(sysAreaList, Area.class));
                }
            } else {
                break;
            }
            pageNo++;
        }
        long end = System.currentTimeMillis();
        log.warn("从缓存中获取所有的省,市，县数量:{};耗时:{} 毫秒", areaList.size(), end-start);
        return areaList;
    }

    /**
     * 根据区域类型和父区域id从缓存中获取区域列表
     *
     * @param type
     * @param parentId
     * @return
     */
    public List<Area> findListByTypeAndParentFromCache(Integer type, Long parentId) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findListByTypeAndParentFromCache(type, parentId));
    }


    /**
     * 根据区域类型和父区域id从缓存中获取(statusFlag=0)区域列表
     *
     * @param type
     * @param parentId
     * @return
     */
    public List<Area> findNormalStatusListByTypeAndParentFromCache(Integer type, Long parentId) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findListByTypeAndParentNewFromCache(type, parentId));
    }


    /**
     * 根据区域id删除区域
     *
     * @param id
     * @return
     */
    public void delete(Long id) {
        //MSErrorCode msErrorCode = MDUtils.customSave(()->msSysAreaFeign.delete(id));
        User currentUser = UserUtils.getUser();
        MSErrorCode msErrorCode = MDUtils.customSave(()->msSysAreaFeign.deleteNew(id, Optional.ofNullable(currentUser).map(r->r.getId()).orElse(0L), System.currentTimeMillis()));
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("调用微服务删除区域失败,原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 地址解析
     *
     * @param province 省
     * @param city     市
     * @param district 区
     * @param street   街道
     * @return
    */
    public String[] decodeAddress(String province, String city, String district, String street) {
        MSResponse<String[]> msResponse = msSysAreaFeign.decodeAddress(province, city, district, street);
        String[] strArray = new String[]{};
        if (MSResponse.isSuccess(msResponse)) {
            strArray = msResponse.getData();
        }
        return strArray;
    }

    /**
     * 地址解析
     *
     * @param province 省
     * @param city     市
     * @param district 区
     * @return
     */
    public String[] decodeDistrictAddress(String province, String city, String district) {
        MSResponse<String[]> msResponse = msSysAreaFeign.decodeDistrictAddress(province, city, district);
        String[] strArray = new String[]{};
        if (MSResponse.isSuccess(msResponse)) {
            strArray = msResponse.getData();
        }
        return strArray;
    }


    /**
     * 通过区Id获取区，市，省id及名称
     *
     * @param id
     * @return
    */
    public Area getThreeLevelAreaById(Long id) {
        return MDUtils.getObjNecessaryConvertType(Area.class, ()-> msSysAreaFeign.getThreeLevelAreaById(id));
    }

    /**
     * 通过区Id获取区，市，省id及名称
     *
     * @param id
     * @return
     */
    public Area getThreeLevelAreaByIdFromCache(Long id) {
        return MDUtils.getObjNecessaryConvertType(Area.class, ()-> msSysAreaFeign.getThreeLevelAreaByIdFromCache(id));
    }


    /**
     * 根据区县id，街道id从缓存中获取街道信息
     * @param districtId   区/县
     * @param townId       镇/街道
     * @return
     */
    public Area getTownFromCache(Long districtId, Long townId) {
        return MDUtils.getObjNecessaryConvertType(Area.class, ()->msSysAreaFeign.getTownFromCache(districtId, townId));
    }

    /**
     * 根据父级id串获取区域数据
     * @param parentIds
     * @return
     */
    public List<Area> findByParentIdsLike(String parentIds) {
        int pageNo = 1;
        int pageSize = 200;

        List<Area> areaList = Lists.newArrayList();
        MSResponse<MSPage<SysArea>> response = msSysAreaFeign.findByParentIdsLike(parentIds, pageNo, pageSize);
        if (MSResponse.isSuccess(response)) {
            MSPage<SysArea> page = response.getData();
            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                List<Area> firstAreaList = mapper.mapAsList(page.getList(), Area.class);
                if (firstAreaList != null && !firstAreaList.isEmpty()) {
                    areaList.addAll(firstAreaList);
                }

                pageNo++;
                while (pageNo <= page.getPageCount()) {
                    MSResponse<MSPage<SysArea>> whileResponse = msSysAreaFeign.findByParentIdsLike(parentIds, pageNo, pageSize);
                    if (MSResponse.isSuccess(whileResponse)) {
                        MSPage<SysArea> whilePage = whileResponse.getData();
                        if (whilePage != null && whilePage.getList() != null && !whilePage.getList().isEmpty()) {
                            List<Area> whileAreaList = mapper.mapAsList(whilePage.getList(), Area.class);
                            if (whileAreaList != null && !whileAreaList.isEmpty()) {
                                areaList.addAll(whileAreaList);
                            }
                        }
                    }
                    pageNo++;
                }
            }
        } else {
            return Lists.newArrayList();
        }
        return areaList;
    }

    /**
     * 根据区域ids获取区域列表
     * @param areaIds
     * @return
     */
    public List<Area> findListByAreaIdList(List<Long> areaIds) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findListByAreaIdList(areaIds));
    }

    /**
     * 根据父id串和区域类型分页返回区域Id列表
     *
     * @param area
     * @return
     */
    public List<Long> findIdByParentIdsAndType(Area area) {
        List<Long> areaIds = Lists.newArrayList();

        int pageNo = 1;
        int pageSize = 200;

        SysArea sysArea = mapper.map(area, SysArea.class);
        MSPage<SysArea> page = sysArea.getPage();
        if (page == null) {
            page = new MSPage<>();
        }
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        sysArea.setPage(page);

        MSResponse<MSPage<Long>> msResponse = msSysAreaFeign.findIdByParentIdsAndType(sysArea);
        if (MSResponse.isSuccess(msResponse)) {
            MSPage<Long> firstPage= msResponse.getData();
            if (firstPage != null && firstPage.getList() != null && !firstPage.getList().isEmpty()) {
                List<Long> firstAreaIds = firstPage.getList();
                if (firstAreaIds != null && !firstAreaIds.isEmpty()) {
                    areaIds.addAll(firstAreaIds);
                }

                while (pageNo < firstPage.getPageCount()) {
                    pageNo++;
                    sysArea.getPage().setPageNo(pageNo);
                    MSResponse<MSPage<Long>> whileResponse = msSysAreaFeign.findIdByParentIdsAndType(sysArea);
                    if (MSResponse.isSuccess(whileResponse)) {
                        MSPage<Long> whilePage = whileResponse.getData();
                        if (whilePage != null && whilePage.getList() != null && !whilePage.getList().isEmpty()) {
                            List<Long> whileAreaIds = whilePage.getList();
                            if (whileAreaIds != null && !whileAreaIds.isEmpty()) {
                                areaIds.addAll(whileAreaIds);
                            }
                        }
                    }
                }
            }
        }

        return areaIds;
    }

    /**
     * 根据id列表获取区县集合
     *
     * @param paramAreaList
     * @return
     */
    public List<Area> findDistrictListByAreas(List<Area> paramAreaList) {
        List<Area> areaList = Lists.newArrayList();

        int pageNo = 1;
        int pageSize = 500;

        List<SysArea>  paramSysAreaList = mapper.mapAsList(paramAreaList, SysArea.class);
        MSResponse<MSPage<SysArea>> msResponse = msSysAreaFeign.findDistrictListByAreas(paramSysAreaList, pageNo, pageSize);
        if (MSResponse.isSuccess(msResponse)) {
            MSPage<SysArea> firstPage = msResponse.getData();
            if (firstPage != null && firstPage.getList() != null && !firstPage.getList().isEmpty()) {
                List<SysArea> firstSysAreaList = firstPage.getList();
                if (firstSysAreaList != null && !firstSysAreaList.isEmpty()) {
                    List<Area> firstAreaList = mapper.mapAsList(firstSysAreaList, Area.class);
                    areaList.addAll(firstAreaList);
                }

                while (pageNo < firstPage.getPageCount()) {
                    pageNo++;
                    MSResponse<MSPage<SysArea>> whileResponse = msSysAreaFeign.findDistrictListByAreas(paramSysAreaList, pageNo, pageSize);
                    if (MSResponse.isSuccess(whileResponse)) {
                        MSPage<SysArea> whilePage = whileResponse.getData();
                        if (whilePage != null && whilePage.getList() != null && !whilePage.getList().isEmpty()) {
                            List<SysArea> whileSysAreaList = whilePage.getList();
                            if (whileSysAreaList != null && !whileSysAreaList.isEmpty()) {
                                List<Area> whileAreaList = mapper.mapAsList(whileSysAreaList, Area.class);
                                areaList.addAll(whileAreaList);
                            }
                        }
                    }
                }
            }
        }

        return areaList;
    }


    /**
     * 为网点或师傅获取区域信息
     *
     * @param ids
     * @return
     */
    public List<Area> findAreasForServicePointOrEngineer(List<Long> ids) {
        int pageNo = 1;
        int pageSize = 200;

        List<Area> areaList = Lists.newArrayList();
        MSResponse<MSPage<SysArea>> response = msSysAreaFeign.findAreasForServicePointOrEngineer(ids, pageNo, pageSize);
        if (MSResponse.isSuccess(response)) {
            MSPage<SysArea> page = response.getData();
            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                List<Area> firstAreaList = mapper.mapAsList(page.getList(), Area.class);
                if (firstAreaList != null && !firstAreaList.isEmpty()) {
                    areaList.addAll(firstAreaList);
                }

                pageNo++;
                while (pageNo <= page.getPageCount()) {
                    MSResponse<MSPage<SysArea>> whileResponse = msSysAreaFeign.findAreasForServicePointOrEngineer(ids, pageNo, pageSize);
                    if (MSResponse.isSuccess(whileResponse)) {
                        MSPage<SysArea> whilePage = whileResponse.getData();
                        if (whilePage != null && whilePage.getList() != null && !whilePage.getList().isEmpty()) {
                            List<Area> whileAreaList = mapper.mapAsList(whilePage.getList(), Area.class);
                            if (whileAreaList != null && !whileAreaList.isEmpty()) {
                                areaList.addAll(whileAreaList);
                            }
                        }
                    }
                    pageNo++;
                }
            }
        } else {
            return Lists.newArrayList();
        }
        return areaList;
    }


    /**
     * 根据区域id获取区域名称，parentIds属性
     * @param areaIds
     * @return
     */
    public List<Area> findSpecListByIds(List<Long> areaIds) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findSpecListByIds(areaIds));
    }

    /**
     * 从缓存中获取省,市区域列表数据
     *
     * @return
    */
    public List<Area> findProvinceAndCityListFromCache() {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findProvinceAndCityListFromCache());
    }

    /**
     * 根据区域id获取三个级别区域id列表
     *
     * @param ids
     * @return
     */
    public List<Area> findThreeLevelAreaIdByIds(List<Long> ids) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findThreeLevelAreaIdByIds(ids));
    }

    /**
     * 根据区域id列表返回区县的id，name
     *
     * @param areaIds
     * @return
     */
    public List<Area> findDistrictNameListByAreaIds(List<Long> areaIds) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findDistrictNameListByAreaIds(areaIds));
    }


    /**
     * 根据区域类型和父级区域id查询区域列表
     *
     * @param type
     * @param parentIds
     * @return 返回属性为：id，name，parentId
    */
    public List<Area> findListByTypeAndParentIds(Integer type, List<Long> parentIds) {
        return MDUtils.findListNecessaryConvertType(Area.class, ()->msSysAreaFeign.findListByTypeAndParentIds(type, parentIds));
    }


    /**
     * 分页获取省，市，区/县 的id，parentId， name， type 数据  2020-11-13
     *
     * @return
     */
    public List<Area> findAllListExcludeTown() {
        int pageNo = 1;
        int pageSize = 1000;

        List<Area> areaList = Lists.newArrayList();
        MSResponse<MSPage<SysArea>> response = msSysAreaFeign.findList( pageNo, pageSize);
        if (MSResponse.isSuccess(response)) {
            MSPage<SysArea> page = response.getData();
            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                List<Area> firstAreaList = mapper.mapAsList(page.getList(), Area.class);
                if (firstAreaList != null && !firstAreaList.isEmpty()) {
                    areaList.addAll(firstAreaList);
                }

                pageNo++;
                while (pageNo <= page.getPageCount()) {
                    MSResponse<MSPage<SysArea>> whileResponse = msSysAreaFeign.findList(pageNo, pageSize);
                    if (MSResponse.isSuccess(whileResponse)) {
                        MSPage<SysArea> whilePage = whileResponse.getData();
                        if (whilePage != null && whilePage.getList() != null && !whilePage.getList().isEmpty()) {
                            List<Area> whileAreaList = mapper.mapAsList(whilePage.getList(), Area.class);
                            if (whileAreaList != null && !whileAreaList.isEmpty()) {
                                areaList.addAll(whileAreaList);
                            }
                        }
                    }
                    pageNo++;
                }
            }
        } else {
            return Lists.newArrayList();
        }
        return areaList;
    }


    /**
     * 根据区/县id获取街道的数量2020-11-13
     * @return
     */
    public Integer getSubAreaCountByAreaId(Long id){
       MSResponse<Integer> msResponse = msSysAreaFeign.getSubAreaCountByAreaId(id);
       if(MSResponse.isSuccess(msResponse)){
           return msResponse.getData();
       }else{
           return 0;
       }
    }

    /**
     * 更新区域状态
     *
     * @param area
     * @return
    */
    public void updateStatus(Area area) {
        SysArea sysArea = mapper.map(area, SysArea.class);
        MSErrorCode msErrorCode = MDUtils.customSave(()->msSysAreaFeign.updateStatus(sysArea));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("更新区域状态失败,失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 返回全国区县，街道数量
     * @return
     */
    public NameValuePair<Integer,Integer> findAllAreaCountForRPT() {
        MSResponse<NameValuePair<Integer,Integer>> responseArea = msSysAreaFeign.getAllAreaCountForRPT();
        NameValuePair nameValuePair;
        if (MSResponse.isSuccessCode(responseArea)) {
            nameValuePair = responseArea.getData();
        } else {
            throw new RuntimeException("调用微服务获取全国区域街道失败.失败原因" + responseArea.getMsg());
        }
        return nameValuePair;
    }
}
