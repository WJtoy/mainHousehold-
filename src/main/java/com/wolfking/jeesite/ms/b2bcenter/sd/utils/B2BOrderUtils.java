package com.wolfking.jeesite.ms.b2bcenter.sd.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;

import java.util.List;

public class B2BOrderUtils {

    private static MicroServicesProperties microServicesProperties = SpringContextHolder.getBean(MicroServicesProperties.class);

    /**
     * 是否允许调用微服务
     */
    public static boolean canInvokeB2BMicroService(Integer dataSourceId) {
        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSourceId);
        boolean result = false;
        if (dataSourceEnum != null) {
            switch (dataSourceEnum) {
                case FEIYU://TODO: 飞羽的只有接单功能，不需要发送工单状态变更消息
                    break;
                case XYINGYAN:
                    result = microServicesProperties.getXyyPlus().getEnabled();
                    break;
                case LB:
                    result = microServicesProperties.getLb().getEnabled();
                    break;
                case TMALL:
                    result = microServicesProperties.getTmall().getEnabled();
                    break;
                case CANBO:
                case USATON:
                    result = microServicesProperties.getCanbo().getEnabled();
                    break;
                case WEBER:
                    result = microServicesProperties.getWeber().getEnabled();
                    break;
                case MBO:
                    result = microServicesProperties.getMbo().getEnabled();
                    break;
                case SUPOR:
                    result = microServicesProperties.getSupor().getEnabled();
                    break;
                case JINJING:
                    result = microServicesProperties.getJinjing().getEnabled();
                    break;
                case USATON_GA:
                    result = microServicesProperties.getUsatonGa().getEnabled();
                    break;
                case MQI:
                    result = microServicesProperties.getMqi().getEnabled();
                    break;
                case JINRAN:
                    result = microServicesProperties.getJinran().getEnabled();
                    break;
                case JD:
                    result = microServicesProperties.getJd().getEnabled();
                    break;
                case INSE:
                    result = microServicesProperties.getInse().getEnabled();
                    break;
                case KONKA:
                    result = microServicesProperties.getKonka().getEnabled();
                    break;
                case JOYOUNG:
                    result = microServicesProperties.getJoyoung().getEnabled();
                    break;
                case UM:
                    result = microServicesProperties.getUm().getEnabled();
                    break;
                case SUNING:
                    result = microServicesProperties.getSuning().getEnabled();
                    break;
                case JDUE:
                    result = microServicesProperties.getJdue().getEnabled();
                    break;
                case JDUEPLUS:
                    result = microServicesProperties.getJduePlus().getEnabled();
                    break;
                case PDD:
                    result = microServicesProperties.getPdd().getEnabled();
                    break;
                case VIOMI:
                    result = microServicesProperties.getVioMi().getEnabled();
                    break;
                case SF:
                    result = microServicesProperties.getSf().getEnabled();
                    break;
                case PHILIPS:
                    result = microServicesProperties.getPhilips().getEnabled();
                    break;
            }
        }
        return result;
    }

    /**
     * 是否允许发送工单日志
     */
    public static boolean canSendOrderProcessLog(Integer dataSourceId) {
        if (canInvokeB2BMicroService(dataSourceId)) {
            if (dataSourceId == B2BDataSourceEnum.UM.id && microServicesProperties.getUm().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.JOYOUNG.id && microServicesProperties.getJoyoung().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.INSE.id && microServicesProperties.getInse().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id && microServicesProperties.getVioMi().getLogEnabled()) {
                return true;
            }
            if (B2BDataSourceEnum.isTooneDataSourceId(dataSourceId) && microServicesProperties.getCanbo().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.WEBER.id && microServicesProperties.getWeber().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.MBO.id && microServicesProperties.getMbo().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.SUPOR.id && microServicesProperties.getSupor().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.JINJING.id && microServicesProperties.getJinjing().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.USATON_GA.id && microServicesProperties.getUsatonGa().getLogEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.JDUEPLUS.id && microServicesProperties.getJduePlus().getLogEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许催单
     */
    public static boolean canReminderOrder(Integer dataSourceId) {
        if (canInvokeB2BMicroService(dataSourceId)) {
            if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id && microServicesProperties.getXyyPlus().getReminderEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.LB.id && microServicesProperties.getLb().getReminderEnabled()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id && microServicesProperties.getVioMi().getReminderEnabled()) {
                return true;
            }
            if(dataSourceId == B2BDataSourceEnum.JOYOUNG.id && microServicesProperties.getJoyoung().getReminderEnabled()){
                return true;
            }
            if(dataSourceId == B2BDataSourceEnum.MQI.id && microServicesProperties.getMqi().getReminderEnabled()){
                return true;
            }
            if(dataSourceId == B2BDataSourceEnum.JINRAN.id && microServicesProperties.getJinran().getReminderEnabled()){
                return true;
            }
            if(dataSourceId == B2BDataSourceEnum.PHILIPS.id && microServicesProperties.getPhilips().getReminderEnabled()){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许修改B2B工单
     */
    public static boolean canModifyB2BOrder(Integer dataSourceId) {
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            if (dataSourceId == B2BDataSourceEnum.SUNING.id && microServicesProperties.getSuning().getEnabled() && microServicesProperties.getSuning().getCanModifyB2BOrder()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许修改B2B工单
     */
    public static boolean canModifyKKLOrder(Integer dataSourceId) {
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            if (dataSourceId == B2BDataSourceEnum.SUNING.id && microServicesProperties.getSuning().getEnabled() && microServicesProperties.getSuning().getCanModifyKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id && microServicesProperties.getXyyPlus().getEnabled() && microServicesProperties.getXyyPlus().getCanModifyKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.LB.id && microServicesProperties.getLb().getEnabled() && microServicesProperties.getLb().getCanModifyKKLOrder()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许第三方系统直接处理快可立工单
     */
    public static boolean canProcessKKLOrder(Integer dataSourceId) {
        if (B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            if (dataSourceId == B2BDataSourceEnum.XYINGYAN.id && microServicesProperties.getXyyPlus().getEnabled() && microServicesProperties.getXyyPlus().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.LB.id && microServicesProperties.getLb().getEnabled() && microServicesProperties.getLb().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.INSE.id && microServicesProperties.getInse().getEnabled() && microServicesProperties.getInse().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.JD.id && microServicesProperties.getJd().getEnabled() && microServicesProperties.getJd().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.UM.id && microServicesProperties.getUm().getEnabled() && microServicesProperties.getUm().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.PDD.id && microServicesProperties.getPdd().getEnabled() && microServicesProperties.getPdd().getCanProcessKKLOrder()) {
                return true;
            }
            if ((dataSourceId == B2BDataSourceEnum.CANBO.id || dataSourceId == B2BDataSourceEnum.USATON.id) && microServicesProperties.getCanbo().getEnabled() && microServicesProperties.getCanbo().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.WEBER.id && microServicesProperties.getWeber().getEnabled() && microServicesProperties.getWeber().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.MBO.id && microServicesProperties.getMbo().getEnabled() && microServicesProperties.getMbo().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.SUPOR.id && microServicesProperties.getSupor().getEnabled() && microServicesProperties.getSupor().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.JINJING.id && microServicesProperties.getJinjing().getEnabled() && microServicesProperties.getJinjing().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.USATON_GA.id && microServicesProperties.getUsatonGa().getEnabled() && microServicesProperties.getUsatonGa().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.MQI.id && microServicesProperties.getMqi().getEnabled() && microServicesProperties.getMqi().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.JINRAN.id && microServicesProperties.getJinran().getEnabled() && microServicesProperties.getJinran().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.VIOMI.id && microServicesProperties.getPdd().getEnabled() && microServicesProperties.getVioMi().getCanProcessKKLOrder()) {
                return true;
            }
            if (dataSourceId == B2BDataSourceEnum.SF.id && microServicesProperties.getSf().getEnabled() && microServicesProperties.getSf().getCanProcessKKLOrder()) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * 允许调用优盟微服务
//     */
//    public static boolean canInvokeUMMicroService(Long customerId) {
//        List<Long> customerIds = microServicesProperties.getUm().getCustomerIds();
//        if (microServicesProperties.getUm().getEnabled()
//                && customerIds != null && !customerIds.isEmpty()
//                && customerId != null && customerId > 0) {
//            for (Long id : customerIds) {
//                if (customerId.longValue() == id) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    /**
     * 是否需要发送工单数据给优盟
     */
    public static boolean canSendOrderDataToMS(Long customerId) {
        List<Long> customerIds = microServicesProperties.getUm().getCustomerIds();
        if (microServicesProperties.getUm().getEnabled()
                && microServicesProperties.getUm().getOrderInfoEnabled()
                && customerIds != null && !customerIds.isEmpty()
                && customerId != null && customerId > 0) {
            return customerIds.contains(customerId);
        }
        return false;
    }

    public static long getBrandId(String brandName, List<Brand> brandList) {
        long brandId = 0;
        if (StrUtil.isNotBlank(brandName) && CollectionUtil.isNotEmpty(brandList)) {
            for (Brand brand : brandList) {
                if (brand != null && brandName.equals(brand.getName())) {
                    brandId = brand.getId();
                    break;
                }
            }
        }
        return brandId;
    }

}
