package com.wolfking.jeesite.modules.sd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * 下单配置
 * @date 2020/3/12 8:06 下午
 */
@Component
@Data
@ConfigurationProperties("createOrder")
public class CreateOrderConfig {
    /**
     * 旧下单
     */
    private final FirstType firstType = new FirstType();
    /**
     * 新下单
     */
    private final SecondType secondType = new SecondType();


    /**
     * 旧下单配置
     */
    @Data
    public static class FirstType {
        //标题
        private String title = "下单";
        //版本
        private int version = 1;
        //排除品类列表
        private Set<Long> exceptCategories;
    }

    /**
     * 新下单配置
     */
    @Data
    public static class SecondType {
        //标题
        private String title;
        //版本
        private int version = 2;
        //下单相对地址
        private String url;
        //涵盖品类列表
        private Set<Long> categories;
        //分类下可最多上传图片数量
        private int maxUploadQty = 5;
        //每个产品最多选择图片数量
        private int maxSelectQty = 3;
    }

}
