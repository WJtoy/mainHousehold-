package com.wolfking.jeesite.ms.tmall.md.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.util.Lists;

import java.util.List;

public class ServicePointProvinceBatch extends LongIDDataEntity<ServicePointProvinceBatch> {

    @Getter
    @Setter
    private Area province;

    @Getter
    @Setter
    private Area city;

    @Getter
    @Setter
    private Long servicePointCount;

    @Getter
    @Setter
    private Long engineerCount;

    @Getter
    @Setter
    private B2BServicePointBatchLog batchLog = new B2BServicePointBatchLog();

    @Getter
    @Setter
    List<ServicePointProvinceBatch> subItemlist = Lists.newArrayList();

    /**
     * 计算子项数据
     * @return
     */
    public int getMaxRow() {
        return subItemlist.size();
    }

    public long getTotalServicePointCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(ServicePointProvinceBatch::getServicePointCount).reduce(Long::sum).orElse(0L);
        }
        return totalCount;
    }

    public long getTotalEngineerCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(ServicePointProvinceBatch::getEngineerCount).reduce(Long::sum).orElse(0L);
        }
        return totalCount;
    }

    public long getTotalServicePointSuccessCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getServicePointSuccessCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public long getTotalServicePointFailureCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getServicePointFailureCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public long getTotalCoverServiceSuccessCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getCoverServiceSuccessCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public long getTotalCoverServiceFailureCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getCoverServiceFailureCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public long getTotalCapacitySuccessCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getCapacitySuccessCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public long getTotalCapacityFailureCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getCapacityFailureCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public long getTotalWorkerSuccessCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getWorkerSuccessCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }
    public long getTotalWorkerFailureCount() {
        long totalCount = 0;
        if (subItemlist != null && subItemlist.size() > 0) {
            totalCount = subItemlist.stream().map(x->x.getBatchLog().getWorkerFailureCount()).reduce(Integer::sum).orElse(0);
        }
        return totalCount;
    }

    public String getLastUpdateRemark() {
        StringBuilder remark = new StringBuilder();
        if (batchLog != null) {
            remark.append("上次批量上传时间").append(" ").append(DateUtils.formatDate(batchLog.getUpdateDate(), "yyyy-MM-dd HH:mm:ss")).append("；");
            remark.append("成功上传网点").append(batchLog.getServicePointSuccessCount()).append("个").append("，")
                    .append("失败").append(batchLog.getServicePointFailureCount()).append("个").append("；");
            remark.append("成功上传网点的覆盖服务").append(batchLog.getServicePointSuccessCount()).append("个").append("，")
                    .append("失败").append(batchLog.getServicePointFailureCount()).append("个").append("；");
            remark.append("成功上传网点容量").append(batchLog.getServicePointSuccessCount()).append("个").append("，")
                    .append("失败").append(batchLog.getServicePointFailureCount()).append("个").append("；");
            remark.append("成功上传网点师傅").append(batchLog.getServicePointSuccessCount()).append("个").append("，")
                    .append("失败").append(batchLog.getServicePointFailureCount()).append("个").append("。");
        }
        return remark.toString();
    }

    public String getLastUpdateErrorMsg() {
        String errorMsg = "";
        if (batchLog != null) {
            errorMsg = batchLog.getProcessComment();
        }
        return errorMsg;
    }



}
