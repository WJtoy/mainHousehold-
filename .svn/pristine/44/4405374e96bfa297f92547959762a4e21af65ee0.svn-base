package com.wolfking.jeesite.ms.providermd.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.ms.providermd.fallback.MSCommonQueryFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="provider-md", fallbackFactory = MSCommonQueryFallbackFactory.class)
public interface MSCommonQueryFeign {
    /**
     * 检查数据库连接是否可用
     * @return
     */
    @GetMapping("/commonQuery/checkConnection")
    MSResponse<Integer> checkConnection();
}
