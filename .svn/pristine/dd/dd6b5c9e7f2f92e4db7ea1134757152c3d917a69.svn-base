package com.wolfking.jeesite.ms.providersys.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.sys.SysArea;
import com.wolfking.jeesite.ms.providersys.feign.MSSysAreaFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MSSysAreaFallbackFactory implements FallbackFactory<MSSysAreaFeign> {
    @Override
    public MSSysAreaFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("MSSysAreaFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new MSSysAreaFeign() {
            /**
             * 新增区域
             *
             * @param sysArea
             * @return
             */
            @Override
            public MSResponse<Integer> insert(SysArea sysArea) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 修改区域
             *
             * @param sysArea
             * @return
             */
            @Override
            public MSResponse<Integer> update(SysArea sysArea) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据id从DB中获取数据
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<SysArea> get(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据id从缓存中获取数据
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<SysArea> getFromCache(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据id从缓存中获取数据
             *
             * @param id
             * @param type
             * @return
             */
            @Override
            public MSResponse<SysArea> getFromCache(Long id, Integer type) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 分页查询
             *
             * @param sysArea
             * @return
             */
            @Override
            public MSResponse<MSPage<SysArea>> findSpecList(SysArea sysArea) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域类型获取区域列表
             *
             * @param type
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findListByType(Integer type) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域类型从缓存中获取区域列表
             *
             * @param type
             * @param pageNo
             * @param pageSize
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findListByTypeFromCache(Integer type, Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域类型从缓存中获取省市区域列表
             *
             * @param pageNo
             * @param pageSize
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findListExcludeTownFromCache(Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域类型和父区域id从缓存中获取区域列表
             *
             * @param type
             * @param parentId
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findListByTypeAndParentFromCache(Integer type, Long parentId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域类型和父区域id从缓存中获取(statusFlag=0)区域列表
             *
             * @param type
             * @param parentId
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findListByTypeAndParentNewFromCache(Integer type, Long parentId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id删除区域
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<Integer> delete(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id删除区域
             *
             * @param id
             * @param updateById
             * @param updateDate
             * @return
             */
            @Override
            public MSResponse<Integer> deleteNew(Long id, Long updateById, Long updateDate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
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
            @Override
            public MSResponse<String[]> decodeAddress(String province, String city, String district, String street) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 地址解析
             *
             * @param province 省
             * @param city     市
             * @param district 区
             * @return
             */
            @Override
            public MSResponse<String[]> decodeDistrictAddress(String province, String city, String district) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 通过区Id获取区，市，省id及名称
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<SysArea> getThreeLevelAreaById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 通过区Id获取区，市，省id及名称
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<SysArea> getThreeLevelAreaByIdFromCache(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区县id，街道id从缓存中获取街道信息
             *
             * @param areaId
             * @param townId
             * @return
             */
            @Override
            public MSResponse<SysArea> getTownFromCache(Long areaId, Long townId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 通过父级id串获取
             *
             * @param parentIds
             * @param pageNo
             * @param pageSize
             * @return
             */
            @Override
            public MSResponse<MSPage<SysArea>> findByParentIdsLike(String parentIds, Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id列表查询区域详细信息列表
             *
             * @param areaIds
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findListByAreaIdList(List<Long> areaIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            /**
             * 根据id列表获取区县集合
             *
             * @param areaList
             * @param pageNo
             * @param pageSize
             * @return
             */
            @Override
            public MSResponse<MSPage<SysArea>> findDistrictListByAreas(List<SysArea> areaList, Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据父id串和区域类型分页返回区域Id列表
             *
             * @param sysArea
             * @return
             */
            @Override
            public MSResponse<MSPage<Long>> findIdByParentIdsAndType(SysArea sysArea) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 为网点或师傅获取区域信息
             *
             * @param ids
             * @param pageNo
             * @param pageSize
             * @return
             */
            @Override
            public MSResponse<MSPage<SysArea>> findAreasForServicePointOrEngineer(List<Long> ids, Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * @param areaIds
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findSpecListByIds(List<Long> areaIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 从缓存中获取省,市区域列表数据
             *
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findProvinceAndCityListFromCache() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id获取三个级别区域id列表
             *
             * @param ids
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findThreeLevelAreaIdByIds(List<Long> ids) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域id列表返回区县的id，name
             *
             * @param areaIds
             * @return
             */
            @Override
            public MSResponse<List<SysArea>> findDistrictNameListByAreaIds(List<Long> areaIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据区域类型和父级区域id查询区域列表
             *
             * @param type
             * @param parentIds
             * @return 返回属性为：id，name，parentId
             */
            @Override
            public MSResponse<List<SysArea>> findListByTypeAndParentIds(Integer type, List<Long> parentIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 分页获取省，市，区/县 的id，parentId， name， type 数据  2020-11-13
             *
             * @param pageNo
             * @param pageSize
             * @return
             */
            @Override
            public MSResponse<MSPage<SysArea>> findList(Integer pageNo, Integer pageSize) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getSubAreaCountByAreaId(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 更新区域状态
             *
             * @param sysArea
             * @return
             */
            @Override
            public MSResponse<Integer> updateStatus(SysArea sysArea) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<NameValuePair<Integer, Integer>> getAllAreaCountForRPT() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
