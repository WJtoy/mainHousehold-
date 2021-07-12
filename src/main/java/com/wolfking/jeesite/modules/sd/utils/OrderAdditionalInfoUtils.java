package com.wolfking.jeesite.modules.sd.utils;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.OrderAdditionalInfo;
import com.wolfking.jeesite.modules.sd.entity.dto.OrderPbDto;
import com.wolfking.jeesite.modules.sd.entity.dto.OrderPbDto.OrderInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单附加信息(OrderAdditionalInfo)工具类
 */
@Slf4j
public class OrderAdditionalInfoUtils {

    private static final Gson orderAdditionalInfoGson = new GsonBuilder().registerTypeAdapter(OrderAdditionalInfo.class, OrderAdditionalInfoAdapter.getInstance()).create();

    /**
     * OrderAdditionalInfo转成json字符串
     */
    public static String toOrderAdditionalInfoJson(OrderAdditionalInfo info) {
        String json = null;
        if (info != null) {
            json = orderAdditionalInfoGson.toJson(info, new TypeToken<OrderAdditionalInfo>() {
            }.getType());
            /**
             * 因为myCat1.6不支持在json或text类型的字段中存储英文括号，故将所有的英文括号替换成中文括号.
             */
            json = json.replace("(", "（");
            json = json.replace(")", "）");
        }
        return json;
    }

    /**
     * json字符串转成OrderAdditionalInfo列表
     */
    public static OrderAdditionalInfo fromOrderAdditionalInfoJson(String json) {
        OrderAdditionalInfo info = null;
        if (StringUtils.isNotEmpty(json)) {
            info = orderAdditionalInfoGson.fromJson(json, new TypeToken<OrderAdditionalInfo>() {
            }.getType());
        }
        return info;
    }

    //region pb

    /**
     * pb二进制数组转附加信息实例
     *
     * @param bytes
     * @return
     */
    public static OrderAdditionalInfo pbBypesToAdditionalInfo(byte[] bytes) {
        OrderAdditionalInfo info = new OrderAdditionalInfo();
        if(bytes == null || bytes.length ==0){
            return info;
        }
        try{
            OrderInfo orderInfo = OrderInfo.parseFrom(bytes);
            info.setExpectServiceTime(orderInfo.getExpectServiceTime());
            info.setEstimatedReceiveDate(orderInfo.getEstimatedReceiveDate());
            info.setBuyDate(orderInfo.getBuyDate());
            info.setSiteCode(orderInfo.getSiteCode());
            info.setSiteName(orderInfo.getSiteName());
            info.setEngineerName(orderInfo.getEngineerName());
            info.setEngineerMobile(orderInfo.getEngineerMobile());
            info.setOrderDataSource(orderInfo.getOrderDataSource());
        } catch (InvalidProtocolBufferException e) {
            log.error("pb to OrderPbDto.OrderInfo error",e);
        }
        return info;
    }

    /**
     * 附加信息实例转pb二进制数组
     * @param orderInfo
     * @return
     */
    public static byte[] additionalInfoToPbBytes(OrderAdditionalInfo orderInfo){
        if(orderInfo == null) {
            return null;
        }
        OrderPbDto.OrderInfo.Builder builder = OrderPbDto.OrderInfo.newBuilder()
                .setEstimatedReceiveDate(orderInfo.getEstimatedReceiveDate())
                .setExpectServiceTime(orderInfo.getExpectServiceTime())
                .setBuyDate(orderInfo.getBuyDate()==null?0:orderInfo.getBuyDate())
                .setSiteCode(StrUtil.trimToEmpty(orderInfo.getSiteCode()))
                .setSiteName(StrUtil.trimToEmpty(orderInfo.getSiteName()))
                .setEngineerName(StrUtil.trimToEmpty(orderInfo.getEngineerName()))
                .setEngineerMobile(StrUtil.trimToEmpty(orderInfo.getEngineerMobile()))
                .setOrderDataSource(StrUtil.trimToEmpty(orderInfo.getOrderDataSource()));
        OrderInfo info = builder.build();
        if(StringUtils.isBlank(info.toString())){
            return null;
        }
        return info.toByteArray();
    }

    //endregion pb
}
