/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.common.config;

import com.ckfinder.connector.ServletContextFactory;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.utils.PropertiesLoader;
import com.kkl.kklplus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 全局配置类
 *
 * @author ThinkGem
 * @version 2014-06-25
 *
 * @date 2019-06-27
 * @author ryan
 * 取消getSiteCode()及getSiteName()方法
 */
@Slf4j
public class Global {

    public static final int ONE_SECOND = 1 * 1000;

    public static final int ONE_MINUTE = 1 * 60 * 1000;

    static RelaxedPropertyResolver resolver;
    /**
     * 当前对象实例
     */
    private static Global global = new Global();

    /**
     * 保存全局属性值
     */
    private static Map<String, String> map = Maps.newHashMap();

    /**
     * 属性文件加载对象
     */
//    private static PropertiesLoader loader = new PropertiesLoader("bootstrap.yml","application.yml");
    private static PropertiesLoader loader = new PropertiesLoader("bootstrap.yml");

    /**
     * 是/否
     */
    public static final String YES = "1";
    public static final String NO = "0";

    /**
     * 对/错
     */
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    /**
     * 上传文件基础虚拟路径
     */
    public static final String USERFILES_BASE_URL = "/uploads/";//userfiles

    /**
     * 获取当前对象实例
     */
    public static Global getInstance() {
        return global;
    }

    /**
     * 获取配置
     * ${fns:getConfig('adminPath')}
     */
    public static String getConfig(String key) {
        String value = map.get(key);
        if (value == null) {
            try {
                value = resolver.getProperty(key);
//                value = loader.getProperty(key);
                if (StringUtils.isBlank(value))
                    throw new RuntimeException("value null");
                map.put(key, value);
            } catch (Exception e) {
                value = loader.getProperty(key);
                map.put(key, value != null ? value : StringUtils.EMPTY);
            }
        }
        return value;
    }

    /**
     * 获取管理端根路径
     */
    public static String getAdminPath() {
        return getConfig("adminPath");
    }

    /**
     * 是否是演示模式，演示模式下不能修改用户、角色、密码、菜单、授权
     */
    public static Boolean isDemoMode() {
        String dm = getConfig("demoMode");
        return "true".equals(dm) || "1".equals(dm);
    }

    /**
     * 获取上传文件的根目录(CKEditor)
     *
     * @return
     */
    public static String getUserfilesBaseDir() {
        String dir = getConfig("userfiles.basedir");
        if (StringUtils.isBlank(dir)) {
            try {
                dir = ServletContextFactory.getServletContext().getRealPath("/");
            } catch (Exception e) {
                return "";
            }
        }
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        return dir;
    }

    /**
     * 获取上传文件的根目录(/servlet/Upload/)
     * 绝对路径
     * @return
     */
    public static String getUploadfilesDir() {
        String dir = getConfig("userfiles.uploaddir");
        if (StringUtils.isBlank(dir)) {
            return "";
        }
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        return dir;
    }

    public static String getJdbcType() {
//        if (map.containsKey("spring.datasource.url") && StringUtils.isNotBlank(map.get("spring.datasource.url")) ) {
//            return map.get("spring.datasource.url");
//        }
//        try {
//            String url = new String();
//            if(resolver != null) {
//                url = resolver.getProperty("spring.datasource.url");
//            }
////            String url = loader.getProperty("spring.datasource.url");
//            String type = getDbType(url);
//            map.put("spring.datasource.url", type);
//            return type;
//        } catch (Exception e) {
//            log.error("get jdbcType error", e);
//        }
//        log.error("return the defaut jdbc type is mysql");
        return "mysql";
    }

    private static String getDbType(String rawUrl) {
        return rawUrl == null ? null : (!rawUrl.startsWith("jdbc:derby:") && !rawUrl.startsWith("jdbc:log4jdbc:derby:") ? (!rawUrl.startsWith("jdbc:mysql:") && !rawUrl.startsWith("jdbc:cobar:") && !rawUrl.startsWith("jdbc:log4jdbc:mysql:") ? (rawUrl.startsWith("jdbc:mariadb:") ? "mariadb" : (!rawUrl.startsWith("jdbc:oracle:") && !rawUrl.startsWith("jdbc:log4jdbc:oracle:") ? (rawUrl.startsWith("jdbc:alibaba:oracle:") ? "AliOracle" : (!rawUrl.startsWith("jdbc:microsoft:") && !rawUrl.startsWith("jdbc:log4jdbc:microsoft:") ? (!rawUrl.startsWith("jdbc:sqlserver:") && !rawUrl.startsWith("jdbc:log4jdbc:sqlserver:") ? (!rawUrl.startsWith("jdbc:sybase:Tds:") && !rawUrl.startsWith("jdbc:log4jdbc:sybase:") ? (!rawUrl.startsWith("jdbc:jtds:") && !rawUrl.startsWith("jdbc:log4jdbc:jtds:") ? (!rawUrl.startsWith("jdbc:fake:") && !rawUrl.startsWith("jdbc:mock:") ? (!rawUrl.startsWith("jdbc:postgresql:") && !rawUrl.startsWith("jdbc:log4jdbc:postgresql:") ? (rawUrl.startsWith("jdbc:edb:") ? "edb" : (!rawUrl.startsWith("jdbc:hsqldb:") && !rawUrl.startsWith("jdbc:log4jdbc:hsqldb:") ? (rawUrl.startsWith("jdbc:odps:") ? "odps" : (rawUrl.startsWith("jdbc:db2:") ? "db2" : (rawUrl.startsWith("jdbc:sqlite:") ? "sqlite" : (rawUrl.startsWith("jdbc:ingres:") ? "ingres" : (!rawUrl.startsWith("jdbc:h2:") && !rawUrl.startsWith("jdbc:log4jdbc:h2:") ? (rawUrl.startsWith("jdbc:mckoi:") ? "mckoi" : (rawUrl.startsWith("jdbc:cloudscape:") ? "cloudscape" : (!rawUrl.startsWith("jdbc:informix-sqli:") && !rawUrl.startsWith("jdbc:log4jdbc:informix-sqli:") ? (rawUrl.startsWith("jdbc:timesten:") ? "timesten" : (rawUrl.startsWith("jdbc:as400:") ? "as400" : (rawUrl.startsWith("jdbc:sapdb:") ? "sapdb" : (rawUrl.startsWith("jdbc:JSQLConnect:") ? "JSQLConnect" : (rawUrl.startsWith("jdbc:JTurbo:") ? "JTurbo" : (rawUrl.startsWith("jdbc:firebirdsql:") ? "firebirdsql" : (rawUrl.startsWith("jdbc:interbase:") ? "interbase" : (rawUrl.startsWith("jdbc:pointbase:") ? "pointbase" : (rawUrl.startsWith("jdbc:edbc:") ? "edbc" : (rawUrl.startsWith("jdbc:mimer:multi1:") ? "mimer" : (rawUrl.startsWith("jdbc:dm:") ? "dm" : (rawUrl.startsWith("jdbc:kingbase:") ? "kingbase" : (rawUrl.startsWith("jdbc:log4jdbc:") ? "log4jdbc" : (rawUrl.startsWith("jdbc:hive:") ? "hive" : (rawUrl.startsWith("jdbc:hive2:") ? "hive" : (rawUrl.startsWith("jdbc:phoenix:") ? "phoenix" : null)))))))))))))))) : "informix"))) : "h2"))))) : "hsql")) : "postgresql") : "mock") : "jtds") : "sybase") : "sqlserver") : "sqlserver")) : "oracle")) : "mysql") : "derby");
    }

    /**
     * 获取CKFinder上传文件的根目录
     * @return
     */
    public static String getCkBaseDir() {
        String dir = getConfig("userfiles.basedir");
        Assert.hasText(dir, "配置文件里没有配置userfiles.basedir属性");
        if(!dir.endsWith("/")) {
            dir += "/";
        }
        return dir;
    }
    /* comment at 2019-06-27 ryan
    public static String getSiteCode(){
        String code = getConfig("site.code");
        //Assert.hasText(code, "配置文件里没有配置site.code属性");
        return code;
    }

    public static String getSiteName(){
        String name = getConfig("site.name");
        return name;
    }
    */
}
