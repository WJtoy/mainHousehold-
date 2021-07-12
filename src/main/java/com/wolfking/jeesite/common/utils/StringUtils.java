/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 字符串工具类
 *
 * @author ThinkGem
 * @version 2013-05-22
 *
 * @date 2020-05-05
 * 将公用方法迁移到com.kkl.utils.StringUtils
 */
@Slf4j
public class StringUtils {

    private static final char SEPARATOR = '_';
    private static final String CHARSET_NAME = "UTF-8";

    /**
     * 替换为手机识别的HTML，去掉样式及属性，保留回车。
     *
     * @param txt
     * @return
     */
    public static String toHtml(String txt) {
        if (txt == null) {
            return "";
        }
        return StrUtil.replace(StrUtil.replace(Encodes.escapeHtml(txt), "\n", "<br/>"), "\t", "&nbsp; &nbsp; ");
    }

    /**
     * 转换为JS获取对象值，生成三目运算返回结果
     *
     * @param objectString 对象串
     *                     例如：row.user.id
     *                     返回：!row?'':!row.user?'':!row.user.id?'':row.user.id
     */
    public static String jsGetVal(String objectString) {
        StringBuilder result = new StringBuilder();
        StringBuilder val = new StringBuilder();
        String[] vals = StrUtil.split(objectString, ".");
        for (int i = 0; i < vals.length; i++) {
            val.append("." + vals[i]);
            result.append("!" + (val.substring(1)) + "?'':");
        }
        result.append(val.substring(1));
        return result.toString();
    }

    /**
     * Gson将json以字符方式存储redis时，进行了转换
     * 在头和尾增加了“"”,且加了转义"\"
     * @param json
     * @return
     */
    public static String fromGsonString(String json) {
        if (StrUtil.isBlank(json)) {
            return "";
        }
        StringBuffer jsonsb = new StringBuffer(2000);
        jsonsb.append(json);
        if (jsonsb.substring(0, 1).equalsIgnoreCase("\"")) {
            jsonsb.deleteCharAt(0);
            jsonsb.deleteCharAt(jsonsb.length() - 1);
        }
        return jsonsb.toString().replace("\\", "");
    }

    /**
     * 判断Long类型是否null或小于等于0
     * @param value
     * @return
     */
    public static boolean longIsNullOrLessSpecialValue(Long value, long value2){
        if(value == null){
            return true;
        }
        if(value <= value2){
            return true;
        }
        return false;
    }

    /*
    public static void main(String[] args) {
        //if(StrUtil.equals("a","A")){
        //    System.out.println("equals");
        //}else{
        //    System.out.println("no equals");
        //}
        String name = "特价烟机";
        System.out.println(StringUtils.getStandardProductName(name));
        name = "T烟机";
        System.out.println(StringUtils.getStandardProductName(name));
        name = "TT烟机";
        System.out.println(StringUtils.getStandardProductName(name));
        name = "S烟机";
        System.out.println(StringUtils.getStandardProductName(name));
    }*/

    /**
     * 读取产品标准名称，去除"T","S"
     * @param name
     * @return
     */
    public static String getStandardProductName(String name){
        if(StrUtil.isBlank(name)){
            return name;
        }
        return StrUtil.removePrefix(StrUtil.removeAll(name.toUpperCase(),'T','S'),"VIP");
    }

}
