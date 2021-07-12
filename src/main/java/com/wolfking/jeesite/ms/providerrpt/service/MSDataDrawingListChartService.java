package com.wolfking.jeesite.ms.providerrpt.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.rpt.RPTCustomerComplainChartEntity;
import com.kkl.kklplus.entity.rpt.RPTCustomerReminderEntity;
import com.kkl.kklplus.entity.rpt.search.RPTDataDrawingListSearch;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providerrpt.feign.MSDataDrawingListChartFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class MSDataDrawingListChartService {

    @Autowired
    private MSDataDrawingListChartFeign msDataDrawingListChartFeign;

    @Autowired
    private MicroServicesProperties microServicesProperties;

    /**
     * 从rpt微服务中获取客服时效(安装)图表数据
     */
    public Map<String, Object> getKeFuCompleteTimeInstallChartList(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            search.setOrderServiceType(1);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getKeFuCompleteTimeChartList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }


    /**
     * 从rpt微服务中获取客服时效(维修)图表数据
     */
    public Map<String, Object> getKeFuCompletionTimeMaintainChartList(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            search.setOrderServiceType(2);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getKeFuCompleteTimeChartList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }

    /**
     * 从rpt微服务中获取下单图表数据
     */
    public Map<String, Object> getOrderDataChartList(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(endDate);
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getOrderDataChartList(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }

    /**
     * 从rpt微服务中获取工单图表数据
     */
    public Map<String, Object> getOrderQtyDailyChartData(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getOrderQtyDailyChartData(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }
    /**
     * 从rpt微服务中获取催单图表数据
     */
    public RPTCustomerReminderEntity getCustomerReminderChart(Long endDate) {
        RPTCustomerReminderEntity entity = new RPTCustomerReminderEntity();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<RPTCustomerReminderEntity> msResponse = msDataDrawingListChartFeign.getCustomerReminderChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    entity = msResponse.getData();
                }
            }
        }
        return entity;
    }


    /**
     * 从rpt微服务中获取客诉图表数据
     */
    public RPTCustomerComplainChartEntity getCustomerComplainChart(Long endDate) {
        RPTCustomerComplainChartEntity entity = new RPTCustomerComplainChartEntity();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<RPTCustomerComplainChartEntity> msResponse = msDataDrawingListChartFeign.getCustomerComplainChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    entity = msResponse.getData();
                }
            }
        }
        return entity;
    }


    /**
     * 从rpt微服务中获取网点数量图表数据
     */
    public Map<String, Object> getServicePointQtyChart(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getServicePointQtyChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }

    /**
     * 从rpt微服务中获取网点数量图表数据
     */
    public Map<String, Object> getServicePointStreetQtyChart(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getServicePointStreetQtyChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }

    /**
     * 从rpt微服务中获取支付费用图表数据
     */
    public List<Double> getIncurExpenseChart(Long endDate) {
        List<Double> list = Lists.newArrayList();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<List<Double>> msResponse = msDataDrawingListChartFeign.getIncurExpenseChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    list = msResponse.getData();
                }
            }
        }
        return list;
    }


    /**
     * 从rpt微服务中获取网点数量图表数据
     */
    public Map<String, Object> getOrderCrushQtyChart(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getOrderCrushQtyChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }


    /**
     * 从rpt微服务中获取日下单明细图表数据
     */
    public Map<String, Object> getOrderPlanDailyChart(Long endDate) {
        Map<String, Object> map = new HashMap<>();
        if (endDate != null) {
            RPTDataDrawingListSearch search = new RPTDataDrawingListSearch();
            search.setEndDate(DateUtils.addDays(new Date(endDate), -1).getTime());
            if (microServicesProperties.getReport().getEnabled()) {
                MSResponse<Map<String, Object>> msResponse = msDataDrawingListChartFeign.getOrderPlanDailyChart(search);
                if (MSResponse.isSuccess(msResponse)) {
                    map = msResponse.getData();
                }
            }
        }
        return map;
    }
}
