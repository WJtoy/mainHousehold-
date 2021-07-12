package com.wolfking.jeesite.ms.tmall.sd.entity;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.entity.b2b.order.WorkcardInfo;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 天猫一键求助数据模型
 */
@Data
@NoArgsConstructor
public class TmallAnomalyRecourse implements Serializable {

    //主键
    private Long id;
    //分片
    private String quarter;
    //订单id
    Long orderId;
    //订单号
    String orderNo;
    //天猫求助单id
    Long anomalyRecourseId;
    //区域id(区县级)
    Long areaId;
    //区域名称(包含省市区)
    String areaName;
    //问题分类
    @JsonAdapter(DictSimpleAdapter.class)
    Dict questionType;
    //状态,0-初始，未反馈，1-进行中，已反馈，2-已关闭
    int status = 0;
    //服务名称
    String serviceCode;
    //预警时间
    Date submitDate;
    //图片地址列表
    List<TmallAnomalyRecourseImage> recourseList = Lists.newArrayList();
    //图片地址列表json文本
    @GsonIgnore
    String recourseJson;
    //反馈人id
    Long replierId = 0l;
    //反馈人姓名
    String  replierName = "";
    //客服反馈时间
    Date replyDate;
    //客服反馈内容
    String replyContent = "";
    //创建时间
    Date createDate;

    private Long customerId = 0L;

    private Long provinceId = 0L;

    private Long cityId = 0L;

    private Integer canRush = 0;

    private  Long productCategoryId = 0L;

    private Integer kefuType = 0;
}