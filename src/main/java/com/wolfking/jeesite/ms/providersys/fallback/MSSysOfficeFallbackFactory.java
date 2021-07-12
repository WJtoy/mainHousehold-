package com.wolfking.jeesite.ms.providersys.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysOffice;
import com.wolfking.jeesite.ms.providersys.feign.MSSysOfficeFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MSSysOfficeFallbackFactory implements FallbackFactory<MSSysOfficeFeign> {
    @Override
    public MSSysOfficeFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSSysOfficeFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new MSSysOfficeFeign() {
            /**
             * 根据部门id获取部门信息
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<SysOffice> get(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 插入部门信息
             *
             * @param office
             * @return
             */
            @Override
            public MSResponse<Integer> insert(SysOffice office) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 更新部门信息
             *
             * @param office
             * @return
             */
            @Override
            public MSResponse<Integer> update(SysOffice office) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 更新部门的上级部门id
             *
             * @param office
             * @return
             */
            @Override
            public MSResponse<Integer> updateParentIds(SysOffice office) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * @param office
             * @return
             */
            @Override
            public MSResponse<Integer> delete(SysOffice office) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 获取所有的部门
             *
             * @return
             */
            @Override
            public MSResponse<List<SysOffice>> findAllList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据部门id获取部门列表
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<List<SysOffice>> findList(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据父id串查询部门列表
             *
             * @param parentIds
             * @return
             */
            @Override
            public MSResponse<List<SysOffice>> findByParentIdsLike(String parentIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据部门父编码查询所属的下级部门
             *
             * @param parentCode
             * @return
             */
//            @Override
//            public MSResponse<List<SysOffice>> findSubListByParentCode(String parentCode) {
//                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
//            }


            /**
             * 根据id获取id，name，code属性
             *
             * @param id
             * @return id, name, code
             */
            @Override
            public MSResponse<SysOffice> getSpecColumnById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据部门id列表获取部门的(id,name,code)
             *
             * @param ids
             * @return
             */
            @Override
            public MSResponse<List<SysOffice>> findSpecColumnListByIds(List<Long> ids) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 查询当前部门及下属部门id列表
             *
             * @param id
             * @return
             */
            @Override
            public MSResponse<List<Long>> findIdListById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<SysOffice>> findListByNameAndType(String name, Integer userType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<Long>> findParentListByNameAndType(String name, Integer userType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<Long>> findIdListByParentId(Long parentId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
