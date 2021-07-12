package com.wolfking.jeesite.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("web")
public class WebProperties {

    /**
     * 网点配置
     */
    @Getter
    private final ServicePointProperties servicePoint = new ServicePointProperties();

    public static class ServicePointProperties {

        /**
         * 启用网点保险
         */
        @Getter
        @Setter
        private Boolean insuranceEnabled = false;

        /**
         * 强制网点保险（该配置会强制APP主账号必须同意保险条款）
         */
        @Getter
        @Setter
        private Boolean insuranceForced = false;

        /**
         * 启用网点质保金
         */
        @Getter
        @Setter
        private Boolean depositEnabled = false;
    }

}
