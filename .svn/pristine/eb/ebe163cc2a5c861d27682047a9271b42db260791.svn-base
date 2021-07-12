package com.wolfking.jeesite.ms.providersys.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysOfficeAttributes;
import com.wolfking.jeesite.ms.providersys.feign.MSSysOfficeAttributesFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MSSysOfficeAttributesFeignFallbackFactory implements FallbackFactory<MSSysOfficeAttributesFeign> {
    @Override
    public MSSysOfficeAttributesFeign create(Throwable cause) {
        return new MSSysOfficeAttributesFeign() {

            @Override
            public MSResponse<SysOfficeAttributes> getByOfficeIdAndAttributeId(Long officeId, Long attributesId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
