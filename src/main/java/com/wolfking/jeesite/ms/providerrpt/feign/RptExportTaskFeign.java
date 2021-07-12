package com.wolfking.jeesite.ms.providerrpt.feign;


import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTExportTaskEntity;
import com.kkl.kklplus.entity.rpt.RPTExportTaskSearch;
import com.wolfking.jeesite.ms.providerrpt.fallback.RptExportTaskFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * RPT微服务调用
 */
@FeignClient(name = "provider-rpt", fallbackFactory = RptExportTaskFeignFallbackFactory.class)
public interface RptExportTaskFeign {

    @PostMapping("/rptExportTask/checkRptExportTask")
    MSResponse<String> checkRptExportTask(@RequestBody RPTExportTaskEntity taskEntity);

    @PostMapping("/rptExportTask/createRptExportTask")
    MSResponse<String> createRptExportTask(@RequestBody RPTExportTaskEntity taskEntity);

    /**
     * 分页&条件查询
     */
    @PostMapping("/rptExportTask/getRptExportTaskList")
    MSResponse<MSPage<RPTExportTaskEntity>> getRptExportTaskList(@RequestBody RPTExportTaskSearch search);


    /**
     * 获取报表下载地址
     */
    @PostMapping("/rptExportTask/getRptExcelDownloadUrl")
     MSResponse<String> getRptExcelDownloadUrl(@RequestBody RPTExportTaskEntity taskEntity);
}
