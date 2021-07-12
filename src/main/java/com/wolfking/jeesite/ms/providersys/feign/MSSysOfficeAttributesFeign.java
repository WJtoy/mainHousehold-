package com.wolfking.jeesite.ms.providersys.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysOfficeAttributes;
import com.wolfking.jeesite.ms.providersys.fallback.MSSysOfficeAttributesFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "provider-sys", fallbackFactory = MSSysOfficeAttributesFeignFallbackFactory.class)
public interface MSSysOfficeAttributesFeign {

    /**
     * 根据部门id,属性id获取列表
     *
     * @param officeId,attributesId
     * @return
     */
    @GetMapping("/officeAttributes/getByOfficeIdAndAttributeId")
    MSResponse<SysOfficeAttributes> getByOfficeIdAndAttributeId(@RequestParam("officeId") Long officeId, @RequestParam("attributeId") Long attributeId);
}
