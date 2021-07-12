package com.wolfking.jeesite.ms.providerrpt.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerComplainChartEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTDataDrawingListSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.MSDataDrawingListChartFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-rpt", fallbackFactory = MSDataDrawingListChartFallbackFactory.class)
public interface MSDataDrawingListChartFeign {

    /**
     * 获取客服完工时效图表数据
     */
    @PostMapping("/dataDrawingList/getKeFuCompleteTimeInstallChartList")
    MSResponse<Map<String, Object>> getKeFuCompleteTimeChartList(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取下单图表数据
     */
    @PostMapping("/dataDrawingList/getOrderDataChartList")
    MSResponse<Map<String, Object>> getOrderDataChartList(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取工单图表数据
     */
    @PostMapping("/dataDrawingList/getOrderQtyDailyChartData")
    MSResponse<Map<String, Object>> getOrderQtyDailyChartData(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取催单图表数据
     */
    @PostMapping("/dataDrawingList/getCustomerReminderChart")
    MSResponse<RPTCustomerReminderEntity> getCustomerReminderChart(@RequestBody RPTDataDrawingListSearch search);


    /**
     * 获取客诉图表数据
     */
    @PostMapping("/dataDrawingList/getCustomerComplainChart")
    MSResponse<RPTCustomerComplainChartEntity> getCustomerComplainChart(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取网点数量图表数据
     */
    @PostMapping("/dataDrawingList/getServicePointQtyChart")
    MSResponse<Map<String, Object>> getServicePointQtyChart(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取网点数量图表数据
     */
    @PostMapping("/dataDrawingList/getServicePointStreetQtyChart")
    MSResponse<Map<String, Object>> getServicePointStreetQtyChart(@RequestBody RPTDataDrawingListSearch search);
    /**
     * 获取支出费用图表数据
     */
    @PostMapping("/dataDrawingList/getIncurExpenseChart")
    MSResponse<List<Double>> getIncurExpenseChart(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取突击单数量图表数据
     */
    @PostMapping("/dataDrawingList/getOrderCrushQtyChart")
    MSResponse<Map<String, Object>> getOrderCrushQtyChart(@RequestBody RPTDataDrawingListSearch search);

    /**
     * 获取突击单数量图表数据
     */
    @PostMapping("/dataDrawingList/getOrderPlanDailyChart")
    MSResponse<Map<String, Object>> getOrderPlanDailyChart(@RequestBody RPTDataDrawingListSearch search);
}
