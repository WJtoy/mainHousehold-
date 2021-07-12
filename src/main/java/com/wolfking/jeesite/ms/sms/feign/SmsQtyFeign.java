package com.wolfking.jeesite.ms.sms.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.ms.sms.fallback.SmsQtyFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@FeignClient(name = "provider-sms", fallbackFactory = SmsQtyFeignFallbackFactory.class)
public interface SmsQtyFeign {

    @PostMapping("/smsQty/shortMessageCache")
    MSResponse<Map<Integer, Long>> shortMessageCache(@RequestParam("date") String date);
}
