package com.wolfking.jeesite.ms.providerrpt.servicepoint.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.rpt.RPTExportTaskEntity;
import com.kkl.kklplus.entity.rpt.RPTExportTaskSearch;
import com.wolfking.jeesite.ms.providerrpt.feign.RptExportTaskFeign;
import com.wolfking.jeesite.ms.providerrpt.servicepoint.feign.SpRptExportTaskFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpRptExportTaskFeignFallbackFactory implements FallbackFactory<SpRptExportTaskFeign> {


    @Override
    public SpRptExportTaskFeign create(Throwable throwable) {

        if(throwable != null){
            log.error("SpRptExportTaskFeignFallbackFactory:{}",throwable.getMessage());
        }

       return new SpRptExportTaskFeign() {
           @Override
           public MSResponse<String> checkRptExportTask(RPTExportTaskEntity taskEntity) {
               return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
           }

           @Override
           public MSResponse<String> createRptExportTask(RPTExportTaskEntity taskEntity) {
               return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
           }

           @Override
           public MSResponse<MSPage<RPTExportTaskEntity>> getRptExportTaskList(RPTExportTaskSearch search) {
               return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
           }

           @Override
           public MSResponse<String> getRptExcelDownloadUrl(RPTExportTaskEntity taskEntity) {
               return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
           }
       };

    }
}

