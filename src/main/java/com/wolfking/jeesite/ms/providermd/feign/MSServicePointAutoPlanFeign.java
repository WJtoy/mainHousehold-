package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDServicePointAutoPlan;
import com.kkl.kklplus.entity.md.dto.MDServicePointAutoPlanDto;
import com.wolfking.jeesite.ms.providermd.fallback.MSServicePointAutoPlanFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="provider-md", fallbackFactory = MSServicePointAutoPlanFallbackFactory.class)
public interface MSServicePointAutoPlanFeign {

    /**
     * 分页获取网点自动派单区域
     * @param mdServicePointAutoPlan
     * @return
     */
    @PostMapping("/servicePointAutoPlan/findList")
    MSResponse<MSPage<MDServicePointAutoPlan>> findList(@RequestBody MDServicePointAutoPlan mdServicePointAutoPlan);

    /**
     * 保存服务区域和自动派单区域数据
     * @param mdServicePointAutoPlanDto
     * @return
     */
    @PostMapping("/servicePointAutoPlan/saveServicePointAutoPlanDto")
    MSResponse<Integer> saveServicePointAutoPlanDto(@RequestBody MDServicePointAutoPlanDto mdServicePointAutoPlanDto);

    /**
     * 将所有自动派单服务区域的网点都同步到ES
     * @return
     */
    @GetMapping("/servicePointAutoPlan/pushAllServicePointStationMessageToES")
    MSResponse<Integer> pushAllServicePointStationMessageToES();
}
