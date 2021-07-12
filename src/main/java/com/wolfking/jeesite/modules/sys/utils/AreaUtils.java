/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 区域工具类
 *
 * @author Ryan
 * @version 2018-5-17
 */
public class AreaUtils {

    private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);
    private static AreaService areaService = SpringContextHolder.getBean(AreaService.class);
    private static MSSysAreaService msSysAreaService = SpringContextHolder.getBean(MSSysAreaService.class);

    //region 高得接口

    /**
     * 0:651 (具体的区/县id)
     * 1:浙江省 杭州市 西湖区 (省市区名称)
     * 2:灵隐街道 (详细地址)
     * 3:解析结果 1:成功
     * 4:手机号
     * 5:姓名
     * 6:精度/longitude
     * 7:维度/latitude
     * @param fullAddress
     * @return
     */
    public static String[] parseAddress(String fullAddress) {
        try {
            fullAddress = fullAddress.replaceAll("&nbsp;", "").replaceAll("&lt;", "")
                    .replaceAll("&gt;", "").replaceAll("&amp;", "")
                    .replaceAll("&quot;", "").replaceAll("&mdash;", "");
            String returnStr = "";
            StringBuffer sb = new StringBuffer("https://restapi.amap.com/v3/geocode/geo?key=37b238e75a3097696daf4a81498f1399&address=");
            sb.append(fullAddress.replaceAll(" ", "").replaceAll("#", "")
                    .replaceAll("　", "").replaceAll("\\|", "")
                    .replaceAll(",", "").replaceAll("\r", "")
                    .replaceAll("\n", "").replaceAll("\t", ""));
            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Host", "restapi.amap.com");
            if (200 == connection.getResponseCode()) {
                //得到输入流
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while (-1 != (len = is.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                returnStr = baos.toString("utf-8");
                baos.close();
                is.close();
                connection.disconnect();
                if (returnStr != null && returnStr.length() > 0) {
                    JSONObject jsonObj = JSONObject.fromObject(returnStr);
                    if (jsonObj.get("info").toString().toUpperCase().equals("OK")) {
                        JSONArray jsonArray = (JSONArray) jsonObj.get("geocodes");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String province = jsonObject.get("province").toString();
                            String city = jsonObject.get("city").toString();
                            String district = jsonObject.get("district").toString();
                            if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("2:1:%s", province.substring(0, 2)))) {
                                Long provinceId = (Long) redisUtils.get(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("2:1:%s", province.substring(0, 2)), Long.class);
                                if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("3:%d:%s", provinceId, city))) {
                                    Long cityId = (Long) redisUtils.get(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("3:%d:%s", provinceId, city), Long.class);
                                    if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("4:%d:%s", cityId, district))) {
                                        String districtString = (String) redisUtils.getString(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("4:%d:%s", cityId, district), String.class);
                                        String[] districtStrings = districtString.split("=");
                                        String detailAddress = fullAddress;
                                        //at 2019/04/10 数组容量由6->8,新增 6:longitude/经度 7:latitude/维度
                                        String[] returnString = new String[8];
                                        returnString[3] = "0";
                                        returnString[4] = StringUtils.getCellphone(fullAddress);
                                        returnString[5] = "";
                                        if (returnString[4].length() > 0) {
                                            returnString[5] = StringUtils.getChineseName(fullAddress.replaceAll("姓名", "")
                                                    .replaceAll("收货人", "").replaceAll("联系人", "")
                                                    .replaceAll("收件人", ""));
                                        }
                                        int ipos = fullAddress.indexOf(city);
                                        if (ipos >= 0) {
                                            detailAddress = fullAddress.substring(ipos + district.length()).trim();
                                            returnString[3] = "1";
                                        }
                                        ipos = detailAddress.indexOf(district);
                                        if (ipos >= 0) {
                                            detailAddress = detailAddress.substring(ipos + district.length()).trim();
                                            returnString[3] = "1";
                                        }
                                        returnString[0] = districtStrings[0];
                                        returnString[1] = districtStrings[1];
                                        returnString[2] = detailAddress;
                                        //经纬度
                                        String location = jsonObject.getString("location");
                                        if(StringUtils.isNotBlank(location)){
                                            String[] locations =  StringUtils.split(location,",");
                                            if(locations.length == 2){
                                                returnString[6] = locations[0];
                                                returnString[7] = locations[1];
                                            }
                                        }
                                        return returnString;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 0:651 (具体的区/县id)
     * 1:浙江省 杭州市 西湖区 (省市区名称)
     * 2:灵隐街道 (详细地址)
     * 3:解析结果 1:成功
     * 4:手机号
     * 5:姓名
     * 6:精度/longitude
     * 7:维度/latitude
     * @param fullAddress
     * @return
     */
    public static String[] parseAddressFromMS(String fullAddress) {
        try {
            fullAddress = fullAddress.replaceAll("&nbsp;", "").replaceAll("&lt;", "")
                    .replaceAll("&gt;", "").replaceAll("&amp;", "")
                    .replaceAll("&quot;", "").replaceAll("&mdash;", "");
            String returnStr = "";
            StringBuffer sb = new StringBuffer("https://restapi.amap.com/v3/geocode/geo?key=37b238e75a3097696daf4a81498f1399&address=");
            sb.append(fullAddress.replaceAll(" ", "").replaceAll("#", "")
                    .replaceAll("　", "").replaceAll("\\|", "")
                    .replaceAll(",", "").replaceAll("\r", "")
                    .replaceAll("\n", "").replaceAll("\t", ""));
            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Host", "restapi.amap.com");
            if (200 == connection.getResponseCode()) {
                //得到输入流
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while (-1 != (len = is.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                returnStr = baos.toString("utf-8");
                baos.close();
                is.close();
                connection.disconnect();
                if (returnStr != null && returnStr.length() > 0) {
                    JSONObject jsonObj = JSONObject.fromObject(returnStr);
                    if (jsonObj.get("info").toString().toUpperCase().equals("OK")) {
                        JSONArray jsonArray = (JSONArray) jsonObj.get("geocodes");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String province = jsonObject.get("province").toString();
                            String city = jsonObject.get("city").toString();
                            String district = jsonObject.get("district").toString();

                            String[]  arrayStr = msSysAreaService.decodeDistrictAddress(province, city, district);
                            if (arrayStr != null && arrayStr.length >= 2) {
                                String[] returnString = new String[8];
                                String detailAddress = fullAddress;

                                returnString[3] = "0";
                                returnString[4] = StringUtils.getCellphone(fullAddress);
                                returnString[5] = "";
                                if (returnString[4].length() > 0){
                                    returnString[5] = StringUtils.getChineseName(fullAddress.replaceAll("姓名","")
                                            .replaceAll("收货人","").replaceAll("联系人","")
                                            .replaceAll("收件人",""));
                                }

                                int ipos = fullAddress.indexOf(city);
                                if (ipos >= 0) {
                                    detailAddress = fullAddress.substring(ipos + district.length()).trim();
                                    returnString[3] = "1";
                                }
                                ipos = detailAddress.indexOf(district);
                                if (ipos >= 0) {
                                    detailAddress = detailAddress.substring(ipos + district.length()).trim();
                                    returnString[3] = "1";
                                }

                                returnString[0] = arrayStr[0];
                                returnString[1] = arrayStr[1];
                                returnString[2] = detailAddress;
                                //经纬度
                                String location = jsonObject.getString("location");
                                if(StringUtils.isNotBlank(location)){
                                    String[] locations =  StringUtils.split(location,",");
                                    if(locations.length == 2){
                                        returnString[6] = locations[0];
                                        returnString[7] = locations[1];
                                    }
                                }

                                return returnString;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 解析省市区街道
     * @param fullAddress
     * @return
     * 0 - 区县id
     * 1 - 街道id,如果成功匹配街道
     * 2 - 广东省 深圳市 龙华区 龙华街道
     * 3 - 除去第2项之后的剩余地址
     * 4 - 0:没有匹配成功,1:成功匹配区县/街道
     * 5 - 手机号
     * 6 - 姓名
     * 7 - 精度/longitude
     * 8 - 维度/latitude
     */
    public static String[] decodeAddressGaode(String fullAddress) {
        try {
            fullAddress = HtmlUtil.escape(fullAddress).replaceAll("&nbsp;", "").replaceAll("&lt;","")
                    .replaceAll("&gt;", "").replaceAll("&amp;","")
                    .replaceAll("&quot;", "").replaceAll("&mdash;","")
                    .replaceAll(" ", "").replaceAll("#", "")
                    .replaceAll("　", "").replaceAll("\\|", "")
                    .replaceAll(",", "").replaceAll("\r","")
                    .replaceAll("\n","").replaceAll("\t","");
            String pointUrl = String.format("https://restapi.amap.com/v3/geocode/geo?address=%s&key=37b238e75a3097696daf4a81498f1399",
                    fullAddress);
            String pointResult = HttpUtil.get(pointUrl);
            JSON pointResultJson = JSONUtil.parse(pointResult);
            if (pointResultJson.getByPath("info").toString().toUpperCase().equals("OK")) {
                cn.hutool.json.JSONArray pointArray = (cn.hutool.json.JSONArray) pointResultJson.getByPath("geocodes");
                if (pointArray.size() > 0) {
                    JSON pointJson = (JSON) pointArray.get(0);
                    String province = pointJson.getByPath("province").toString();
                    String city = pointJson.getByPath("city").toString();
                    String district = pointJson.getByPath("district").toString();
                    String pointString = pointJson.getByPath("location").toString();
                    city = city.replace("[]", "");
                    if (StrUtil.isEmpty(city)) {
                        city = district;
                    }
                    String street = "";
                    String longitude = "";
                    String latitude = "";
                    if (StrUtil.isNotEmpty(pointString)) {
                        String[] locations =  StringUtils.split(pointString,",");
                        if(locations.length == 2){
                            longitude = locations[0];
                            latitude = locations[1];
                        }
                        try {
                            String streetUrl = String.format("https://restapi.amap.com/v3/geocode/regeo?location=%s&key=37b238e75a3097696daf4a81498f1399",
                                    pointString);
                            String streetResult = HttpUtil.get(streetUrl);
                            JSON streetResultJson = JSONUtil.parse(streetResult);
                            cn.hutool.json.JSONObject streetObject = (cn.hutool.json.JSONObject) streetResultJson.getByPath("regeocode");
                            if (streetObject.size() == 2) {
                                JSON streetJson = (JSON) streetObject.get("addressComponent");
                                String streetString = streetJson.getByPath("township").toString();
                                if (StrUtil.isNotEmpty(streetString) && !streetString.equals("[]")) {
                                    street = streetString;
                                    district = district.replace("[]","");  // add on 2019-5-22
                                    if (StrUtil.isEmpty(district)) {
                                        district = street;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                        }
                    }
                    if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("2:1:%s", province.substring(0, 2)))) {
                        Long provinceId = (Long) redisUtils.get(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("2:1:%s", province.substring(0, 2)), Long.class);
                        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("3:%d:%s", provinceId, city))) {
                            Long cityId = (Long) redisUtils.get(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("3:%d:%s", provinceId, city), Long.class);
                            if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("4:%d:%s", cityId, district))) {
                                String districtString = (String) redisUtils.getString(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("4:%d:%s", cityId, district), String.class);
                                String[] districtStrings = districtString.split("=");
                                String districtIdString = districtStrings[0];
                                String streetIdString = "";
                                if (StrUtil.isNotEmpty(street)) {
                                    Long districtId = Long.parseLong(districtStrings[0]);
                                    if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("5:%d:%s", districtId, street))) {
                                        String streetString = (String) redisUtils.getString(RedisConstant.RedisDBType.REDIS_SYS_AREA, String.format("5:%d:%s", districtId, street), String.class);
                                        districtStrings = streetString.split("=");
                                        streetIdString = districtStrings[0];
                                    }
                                    // add on  2019-5-29 begin
                                    else {
                                        streetIdString = "3";  // 高德地图找到街道,但在我们系统中无法找到街道/乡镇
                                    }
                                    // add on  2019-5-29 end
                                }
                                // add on  2019-5-29 begin
                                else {
                                    streetIdString = "2";  // 高德地图无法找到街道/乡镇
                                }
                                // add on  2019-5-29 end
                                String detailAddress = fullAddress;
                                String[] returnString = new String[9];
                                returnString[4] = "0";
                                returnString[5] = StringUtils.getCellphone(fullAddress);
                                returnString[6] = "";
                                if (returnString[5].length() > 0){
                                    returnString[6] = StringUtils.getChineseName(fullAddress.replaceAll("姓名","")
                                            .replaceAll("收货人","").replaceAll("联系人","")
                                            .replaceAll("收件人",""));
                                }

                                if (StrUtil.isNotEmpty(street) && fullAddress.indexOf(street) > 0) {
                                    detailAddress =  fullAddress.substring(fullAddress.indexOf(street) + street.length()).trim();
                                    returnString[4] = "1";
                                }
                                else if (fullAddress.indexOf(district) > 0) {
                                    detailAddress = fullAddress.substring(fullAddress.indexOf(district) + district.length()).trim();
                                    returnString[4] = "1";
                                }

                                returnString[0] = districtIdString;
                                returnString[1] = streetIdString;
                                returnString[2] = districtStrings[1];
                                returnString[3] = (detailAddress==null || detailAddress.length()==0)?".":detailAddress;
                                returnString[7] = longitude;
                                returnString[8] = latitude;
                                return returnString;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 解析省市区街道
     * @param fullAddress
     * @return
     * 0 - 区县id
     * 1 - 街道id,如果成功匹配街道
     * 2 - 广东省 深圳市 龙华区 龙华街道
     * 3 - 除去第2项之后的剩余地址
     * 4 - 0:没有匹配成功,1:成功匹配区县/街道
     * 5 - 手机号
     * 6 - 姓名
     * 7 - 精度/longitude
     * 8 - 维度/latitude
     */
    public static String[] decodeAddressGaodeFromMS(String fullAddress) {
        try {
            fullAddress = HtmlUtil.escape(fullAddress).replaceAll("&nbsp;", "").replaceAll("&lt;","")
                    .replaceAll("&gt;", "").replaceAll("&amp;","")
                    .replaceAll("&quot;", "").replaceAll("&mdash;","")
                    .replaceAll(" ", "").replaceAll("#", "")
                    .replaceAll("　", "").replaceAll("\\|", "")
                    .replaceAll(",", "").replaceAll("\r","")
                    .replaceAll("\n","").replaceAll("\t","");
            String pointUrl = String.format("https://restapi.amap.com/v3/geocode/geo?address=%s&key=37b238e75a3097696daf4a81498f1399",
                    fullAddress);
            String pointResult = HttpUtil.get(pointUrl);
            JSON pointResultJson = JSONUtil.parse(pointResult);
            if (pointResultJson.getByPath("info").toString().toUpperCase().equals("OK")) {
                cn.hutool.json.JSONArray pointArray = (cn.hutool.json.JSONArray) pointResultJson.getByPath("geocodes");
                if (pointArray.size() > 0) {
                    JSON pointJson = (JSON) pointArray.get(0);
                    String province = pointJson.getByPath("province").toString();
                    String city = pointJson.getByPath("city").toString();
                    String district = pointJson.getByPath("district").toString();
                    String pointString = pointJson.getByPath("location").toString();
                    city = city.replace("[]", "");
                    if (StrUtil.isEmpty(city)) {
                        city = district;
                    }
                    String street = "";
                    String longitude = "";
                    String latitude = "";
                    if (StrUtil.isNotEmpty(pointString)) {
                        String[] locations =  StringUtils.split(pointString,",");
                        if(locations.length == 2){
                            longitude = locations[0];
                            latitude = locations[1];
                        }
                        try {
                            String streetUrl = String.format("https://restapi.amap.com/v3/geocode/regeo?location=%s&key=37b238e75a3097696daf4a81498f1399",
                                    pointString);
                            String streetResult = HttpUtil.get(streetUrl);
                            JSON streetResultJson = JSONUtil.parse(streetResult);
                            cn.hutool.json.JSONObject streetObject = (cn.hutool.json.JSONObject) streetResultJson.getByPath("regeocode");
                            if (streetObject.size() == 2) {
                                JSON streetJson = (JSON) streetObject.get("addressComponent");
                                String streetString = streetJson.getByPath("township").toString();
                                if (StrUtil.isNotEmpty(streetString) && !streetString.equals("[]")) {
                                    street = streetString;
                                    district = district.replace("[]","");  // add on 2019-5-22
                                    if (StrUtil.isEmpty(district)) {
                                        district = street;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                        }
                    }

                    String[] returnString = new String[9];
                    String[] arrayStr = msSysAreaService.decodeAddress(province, city, district, street);
                    if (arrayStr != null && arrayStr.length >=3) {
                        String detailAddress = fullAddress;

                        returnString[4] = "0";
                        returnString[5] = StringUtils.getCellphone(fullAddress);
                        returnString[6] = "";
                        if (returnString[5].length() > 0) {
                            returnString[6] = StringUtils.getChineseName(fullAddress.replaceAll("姓名", "")
                                    .replaceAll("收货人", "").replaceAll("联系人", "")
                                    .replaceAll("收件人", ""));
                        }

                        if (StrUtil.isNotEmpty(street) && fullAddress.indexOf(street) > 0) {
                            detailAddress = fullAddress.substring(fullAddress.indexOf(street) + street.length()).trim();
                            returnString[4] = "1";
                        } else if (fullAddress.indexOf(district) > 0) {
                            detailAddress = fullAddress.substring(fullAddress.indexOf(district) + district.length()).trim();
                            returnString[4] = "1";
                        }
                        if( arrayStr[0]==null || arrayStr[1]==null || arrayStr[2]==null){
                            returnString[4] = "0";
                        }
                        returnString[0] = arrayStr[0]==null?"0":arrayStr[0];  //districtIdString;
                        returnString[1] = arrayStr[1]==null?"0":arrayStr[1];  //streetIdString;
                        returnString[2] = arrayStr[2]==null?"":arrayStr[2];  //districtStrings[1];
                        returnString[3] = (detailAddress==null || detailAddress.length()==0)?".":detailAddress;
                        returnString[7] = longitude;
                        returnString[8] = latitude;
                        return returnString;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 根据地址获得经纬度
     * 0:精度/longitude
     * 1:维度/latitude
     * @param fullAddress 完整地址，包含省市区县街道门牌号等
     * @return
     */
    public static String[] getLocation(String fullAddress) {
        try {
            fullAddress = fullAddress.replaceAll("&nbsp;", "").replaceAll("&lt;", "")
                    .replaceAll("&gt;", "").replaceAll("&amp;", "")
                    .replaceAll("&quot;", "").replaceAll("&mdash;", "");
            String returnStr = "";
            StringBuffer sb = new StringBuffer("https://restapi.amap.com/v3/geocode/geo?key=37b238e75a3097696daf4a81498f1399&address=");
            sb.append(fullAddress.replaceAll(" ", "").replaceAll("#", "")
                    .replaceAll("　", "").replaceAll("\\|", "")
                    .replaceAll(",", "").replaceAll("\r", "")
                    .replaceAll("\n", "").replaceAll("\t", ""));
            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Host", "restapi.amap.com");
            if (200 == connection.getResponseCode()) {
                //得到输入流
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while (-1 != (len = is.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                returnStr = baos.toString("utf-8");
                baos.close();
                is.close();
                connection.disconnect();
                if (returnStr != null && returnStr.length() > 0) {
                    JSONObject jsonObj = JSONObject.fromObject(returnStr);
                    if (jsonObj.get("info").toString().toUpperCase().equals("OK")) {
                        JSONArray jsonArray = (JSONArray) jsonObj.get("geocodes");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String[] returnString = new String[2];
                            //经纬度
                            String location = jsonObject.getString("location");
                            if(StringUtils.isNotBlank(location)){
                                String[] locations =  StringUtils.split(location,",");
                                if(locations.length == 2){
                                    returnString[0] = locations[0];
                                    returnString[1] = locations[1];
                                    return returnString;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            //throw new RuntimeException(e.getMessage());
            return null;
        }
    }
    //endregion

    /**
     * 按区域类型返回所有区域信息
     *
     * @param type
     * @return
     */
    public static Map<Long, Area> getAreaMap(int type) {
        List<Area> areaList = areaService.findListByType(type);
        Map<Long, Area> areaMap = Maps.newHashMap();
        for (Area item : areaList) {
            areaMap.put(item.getId(), item);
        }
        return areaMap;
    }

    /**
     * 返回省份列表
     *
     * @return
     */
    public static List<Area> getProvinceList() {
        List<Area> areaList = areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
        return areaList != null && areaList.size() > 0 ? areaList : Collections.EMPTY_LIST;
    }

    /**
     * 根据区域获得省份
     * @param id 区域id
     * @param type id所属区域类型 1:国家 2:省 3:市 4:区/县
     * @return 区域所属省份
     */
    public static Area getProvinceByArea(Long id,int type){
        Area area = areaService.getFromCache(id,type);
        if(area == null || area.getId() == null || area.getId().longValue() <= 0){
            return null;
        }
        //province
        if(type == 2){
            return area;
        }
        //city
        if(type == 3){
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if(ids.size()>0){
                String strId = ids.get(ids.size()-1);
                area = areaService.getFromCache(Long.valueOf(strId),2);
                return area;
            }
        }
        //area
        if(type == 4){
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if(ids.size()>2){
                String strId = ids.get(ids.size()-2);
                area = areaService.getFromCache(Long.valueOf(strId),2);
                return area;
            }
        }
        return null;
    }

    /**
     * 根据id查询区县的名称
     */
    public static String getCountyName(Long areaId) {
        String countyName = null;
        if (areaId != null) {
            Area area = areaService.getFromCache(areaId, Area.TYPE_VALUE_COUNTY);
            if (area != null) {
                countyName = area.getName();
            }
        }
        return StringUtils.toString(countyName);
    }

    public static String getCountyFullName(Long areaId) {
        String countyFullName = null;
        if (areaId != null) {
            Area area = areaService.getFromCache(areaId, Area.TYPE_VALUE_COUNTY);
            if (area != null) {
                countyFullName = area.getFullName();
            }
        }
        return StringUtils.toString(countyFullName);
    }

    /**
     * 根据区域id、乡镇id查询镇的名称
     */
    public static String getTownName(Long areaId, Long subAreaId) {
        String townName = null;
        if (subAreaId != null && subAreaId > 3) {
            Area subArea = areaService.getTownFromCache(areaId, subAreaId);
            if (subArea != null) {
                townName = subArea.getName();
            }
        }
        return StringUtils.toString(townName);
    }
}
