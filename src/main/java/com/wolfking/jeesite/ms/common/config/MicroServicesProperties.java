package com.wolfking.jeesite.ms.common.config;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhoucy
 * @date 2018/7/31 14:34
 **/
@ConfigurationProperties(prefix = "ms")
public class MicroServicesProperties {

    @Getter
    private final B2BCenterProperties b2bcenter = new B2BCenterProperties();

    @Getter
    private final TmallProperties tmall = new TmallProperties();

    @Getter
    private final CanboProperties canbo = new CanboProperties();

    @Getter
    private final WeberProperties weber = new WeberProperties();

    @Getter
    private final MBOProperties mbo = new MBOProperties();

    @Getter
    private final SuporProperties supor = new SuporProperties();

    @Getter
    private final JinJingProperties jinjing = new JinJingProperties();

    @Getter
    private final UsatonGaProperties usatonGa = new UsatonGaProperties();

    @Getter
    private final MqiProperties mqi = new MqiProperties();

    @Getter
    private final JinRanProperties jinran = new JinRanProperties();

    @Getter
    private final JDUEProperties jdue = new JDUEProperties();

    @Getter
    private final JDUEPlusProperties jduePlus = new JDUEPlusProperties();

    @Getter
    private final JdProperties jd = new JdProperties();

    @Getter
    private final InseProperties inse = new InseProperties();

    @Getter
    private final XYingYanProperties xYingYan = new XYingYanProperties();

    @Getter
    private final XYYProperties xyy = new XYYProperties();

    @Getter
    private final XYYPlusProperties xyyPlus = new XYYPlusProperties();

    @Getter
    private final LbProperties lb = new LbProperties();

    @Getter
    private final KonkaProperties konka = new KonkaProperties();

    @Getter
    private final JoyoungProperties joyoung = new JoyoungProperties();

    @Getter
    private final VoiceServiceProperties voiceService = new VoiceServiceProperties();

    @Getter
    private final UMProperties um = new UMProperties();

    @Getter
    private final SuningProperties suning = new SuningProperties();

    @Getter
    private final OtlanProperties otlan = new OtlanProperties();

    @Getter
    private final PddProperties pdd = new PddProperties();

    @Getter
    private final KegProperties keg = new KegProperties();

    @Getter
    private final VioMiProperties vioMi = new VioMiProperties();

    @Getter
    private final SFProperties sf = new SFProperties();

    @Getter
    private final VattiProperties vatti = new VattiProperties();

    @Getter
    private final PhilipsProperties philips = new PhilipsProperties();

    @Getter
    private final MSReportProperties report = new MSReportProperties();

    @Getter
    private final WebProperties b2bCreateOrderNo = new WebProperties();

    /**
     * B2B基础资料微服务配置
     */
    public static class B2BCenterProperties {
        @Getter
        @Setter
        private Boolean enabled = false;

        @Getter
        @Setter
        private Map<Integer,String> material = new HashMap<>();
        @Getter
        @Setter
        private String defaultEngineerName = "快可立";
        @Getter
        @Setter
        private String defaultEngineerPhone = "0757-29966188";
    }

    public static class B2BCustomerProperties {

        /**
         * 是否启用微服务
         */
        @Getter
        @Setter
        private Boolean enabled = false;

        /**
         * 启用路由
         */
        @Getter
        @Setter
        private Boolean routingEnabled = false;

        /**
         * 启用日志
         */
        @Getter
        @Setter
        private Boolean logEnabled = false;

        /**
         * 启用催单
         */
        @Getter
        @Setter
        private Boolean reminderEnabled = false;

        /**
         * 启用直接取消
         */
        @Getter
        @Setter
        private Boolean directlyCancelEnabled = false;

        /**
         * 启用忽略
         */
        @Getter
        @Setter
        private Boolean ignoreOrder = false;

        @Getter
        @Setter
        private Boolean searchByCustomerEnabled = false;

        /**
         * 默认电话号码
         */
        @Getter
        @Setter
        private String defaultPhoneNumber = "";

        @Getter
        @Setter
        private Boolean canProcessKKLOrder = false;

        /**
         * 是否允许更新B2B工单的安装标识
         */
        @Getter
        @Setter
        private Boolean canUpdateInstallFlag = false;

        /**
         * 启用直接取消
         */
        @Getter
        @Setter
        private Boolean salesmanTransferOrderEnabled  = false;
    }

    /**
     * 天猫微服务配置
     */
    public static class TmallProperties extends B2BCustomerProperties {
    }

    /**
     * 同望微服务配置
     */
    public static class CanboProperties extends B2BCustomerProperties {
    }

    /**
     * 威博微服务配置
     */
    public static class WeberProperties extends B2BCustomerProperties {
    }

    /**
     * 美博微服务配置
     */
    public static class MBOProperties extends B2BCustomerProperties {
    }

    /**
     * 苏泊尔微服务配置
     */
    public static class SuporProperties extends B2BCustomerProperties {
    }

    /**
     * 津晶微服务配置
     */
    public static class JinJingProperties extends B2BCustomerProperties {
    }

    /**
     * 阿斯丹顿燃热微服务配置
     */
    public static class UsatonGaProperties extends B2BCustomerProperties {
    }

    /**
     * 名气微服务配置
     */
    public static class MqiProperties extends B2BCustomerProperties {
    }

    /**
     * 金燃微服务配置
     */
    public static class JinRanProperties extends B2BCustomerProperties {
    }

    /**
     * 京东优易微服务配置
     */
    public static class JDUEProperties extends B2BCustomerProperties {
    }

    /**
     * 京东优易+微服务配置
     */
    public static class JDUEPlusProperties extends B2BCustomerProperties {
    }

    /**
     * 京东微服务配置
     */
    public static class JdProperties extends B2BCustomerProperties {
//        @Getter
//        @Setter
//        private Boolean canProcessKKLOrder = false;
    }

    /**
     * 樱雪微服务配置
     */
    public static class InseProperties extends B2BCustomerProperties {
//        @Getter
//        @Setter
//        private Boolean canProcessKKLOrder = false;
    }

    /**
     * 新迎燕微服务配置
     */
    public static class XYingYanProperties extends B2BCustomerProperties {

        @Getter
        @Setter
        private String appKey;

        @Getter
        @Setter
        private String appSecret;

        @Getter
        @Setter
        private List<String> methods;

        @Getter
        @Setter
        private String defaultPhoneNumber = "";
    }

    /**
     * 新的新迎燕微服务对接
     */
    public static class XYYProperties extends B2BCustomerProperties {
    }

    /**
     * 新的新迎燕微服务对接
     */
    public static class XYYPlusProperties extends B2BCustomerProperties {
        @Getter
        @Setter
        private Boolean canModifyKKLOrder = false;

//        @Getter
//        @Setter
//        private Boolean canProcessKKLOrder = false;
    }

    /**
     * 乐邦微服务对接
     */
    public static class LbProperties extends B2BCustomerProperties {
        @Getter
        @Setter
        private Boolean canModifyKKLOrder = false;
    }

    /**
     * 康佳微服务配置
     */
    public static class KonkaProperties extends B2BCustomerProperties {
    }

    /**
     * 九阳微服务配置
     */
    public static class JoyoungProperties extends B2BCustomerProperties {
    }

    /**
     * 优盟微服务配置
     */
    public static class UMProperties extends B2BCustomerProperties {
        /**
         * 优盟的客户ID
         */
        @Getter
        @Setter
        private List<Long> customerIds = Lists.newArrayList();

        @Getter
        @Setter
        private Boolean orderInfoEnabled = false;

        @Getter
        @Setter
        private Boolean orderStatusUpdateEnabled = false;
    }

    /**
     * 苏宁微服务配置
     */
    public static class SuningProperties extends B2BCustomerProperties {
        @Getter
        @Setter
        private Boolean canModifyB2BOrder = false;

        @Getter
        @Setter
        private Boolean canModifyKKLOrder = false;
    }

    /**
     * 奥特朗微服务配置
     */
    public static class OtlanProperties extends B2BCustomerProperties {

    }

    /**
     * 奥特朗微服务配置
     */
    public static class PddProperties extends B2BCustomerProperties {

    }

    /**
     * 优盟微服务配置
     */
    public static class KegProperties extends B2BCustomerProperties {
        /**
         * 优盟的客户ID
         */
        @Getter
        @Setter
        private List<Long> customerIds = Lists.newArrayList();

        @Getter
        @Setter
        private Boolean pushOrderInfoEnabled = false;
    }

    /**
     *云米微服务配置
     */
    public static class VioMiProperties extends B2BCustomerProperties {

    }

    /**
     *顺丰微服务配置
     */
    public static class SFProperties extends B2BCustomerProperties {

    }

    /**
     *华帝微服务配置
     */
    public static class VattiProperties extends B2BCustomerProperties {

    }

    /**
     *飞利浦微服务配置
     */
    public static class PhilipsProperties extends B2BCustomerProperties {

    }

    /**
     * 智能客服微服务配置
     */
    public static class VoiceServiceProperties {
        @Getter
        @Setter
        private Boolean enabled = false;
    }

    /**
     * 报表微服务
     */
    public static class MSReportProperties {
        @Getter
        @Setter
        private Boolean enabled = false;
    }

    /**
     * 2b2调用web生成工单单号
     */
    public static class WebProperties{
        @Getter
        @Setter
        private String appKey = "";

        @Getter
        @Setter
        private String appSecret = "";

        @Getter
        @Setter
        private List<String> methods = Lists.newArrayList();
    }


}
