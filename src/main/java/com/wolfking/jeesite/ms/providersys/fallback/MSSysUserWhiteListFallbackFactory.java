package com.wolfking.jeesite.ms.providersys.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.ms.providersys.feign.MSSysUserWhiteListFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author WSJ
 * @date 2018/9/25 10:12
 **/
@Slf4j
@Component
public class MSSysUserWhiteListFallbackFactory implements FallbackFactory<MSSysUserWhiteListFeign> {

    @Override
    public MSSysUserWhiteListFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("MSSysUserWhiteListFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new MSSysUserWhiteListFeign() {
            @Override
            public MSResponse<MSPage<SysUserWhiteList>> findList(SysUserWhiteList sysUserWhiteList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> batchInsert(List<SysUserWhiteList> sysUserWhiteList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<Long>> findAllIdList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<SysUserWhiteList> getById(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(SysUserWhiteList sysUserWhiteList) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> delete(Long id) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<SysUserWhiteList> getByUserIdFromCache(Long userId){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }
}
