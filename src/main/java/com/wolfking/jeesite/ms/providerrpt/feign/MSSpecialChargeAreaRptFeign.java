package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.search.RPTSpecialChargeSearchCondition;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSSpecialChargeAreaRptFeignFallbackFactory;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSSpecialChargeAreaRptFeignFallbackFactory.class)
public interface MSSpecialChargeAreaRptFeign {


    @GetMapping("/specialChargeArea/getList")
    MSResponse<List<RPTSpecialChargeAreaEntity>> getSpecialChargeList(@RequestBody RPTSpecialChargeSearchCondition searchCondition);

}
