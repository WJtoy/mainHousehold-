package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTSpecialChargeAreaEntity;
import com.kkl.kklplus.entity.rpt.search.RPTSpecialChargeSearchCondition;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSCustomerSpecialChargeAreaRptFeignFallbackFactory;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSSpecialChargeAreaRptFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = MSCustomerSpecialChargeAreaRptFeignFallbackFactory.class)
public interface MSCustomerSpecialChargeAreaRptFeign {


    @GetMapping("/customerSpecialChargeArea/getList")
    MSResponse<List<RPTSpecialChargeAreaEntity>> getSpecialChargeList(@RequestBody RPTSpecialChargeSearchCondition searchCondition);

}
