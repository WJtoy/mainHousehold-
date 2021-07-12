package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDServicePointAutoPlan;
import com.kkl.kklplus.entity.md.dto.MDServicePointAutoPlanDto;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.ms.providermd.feign.MSServicePointAutoPlanFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class MSServicePointAutoPlanFallbackFactory implements FallbackFactory<MSServicePointAutoPlanFeign> {

    @Override
    public MSServicePointAutoPlanFeign create(Throwable throwable) {
        return new MSServicePointAutoPlanFeign() {
            /**
             * 分页获取网点自动派单区域
             *
             * @param mdServicePointAutoPlan
             * @return
             */
            @Override
            public MSResponse<MSPage<MDServicePointAutoPlan>> findList(MDServicePointAutoPlan mdServicePointAutoPlan) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 保存服务区域和自动派单区域数据
             *
             * @param mdServicePointAutoPlanDto
             * @return
             */
            @Override
            public MSResponse<Integer> saveServicePointAutoPlanDto(MDServicePointAutoPlanDto mdServicePointAutoPlanDto) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 将所有自动派单服务区域的网点都同步到ES
             *
             * @return
             */
            @Override
            public MSResponse<Integer> pushAllServicePointStationMessageToES() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
