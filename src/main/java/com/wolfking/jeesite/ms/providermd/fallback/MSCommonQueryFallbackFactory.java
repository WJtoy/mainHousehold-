package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.ms.providermd.feign.MSCommonQueryFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class MSCommonQueryFallbackFactory implements FallbackFactory<MSCommonQueryFeign> {
    @Override
    public MSCommonQueryFeign create(Throwable throwable) {
        return new MSCommonQueryFeign() {
            /**
             * 检查数据库连接是否可用
             *
             * @return
             */
            @Override
            public MSResponse<Integer> checkConnection() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
