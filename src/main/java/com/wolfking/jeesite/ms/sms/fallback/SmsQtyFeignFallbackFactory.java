package com.wolfking.jeesite.ms.sms.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.ms.sms.feign.SmsQtyFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Component
public class SmsQtyFeignFallbackFactory implements FallbackFactory<SmsQtyFeign> {

    @Override
    public SmsQtyFeign create(Throwable throwable) {
        return new SmsQtyFeign() {

            @Override
            public MSResponse<Map<Integer, Long>> shortMessageCache(@RequestBody String date) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
