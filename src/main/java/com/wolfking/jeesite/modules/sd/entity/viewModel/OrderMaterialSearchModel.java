package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.Area;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单配件查询模型
 */
@Data
@NoArgsConstructor
public class OrderMaterialSearchModel extends RegionSearchModel<OrderMaterialSearchModel> {

    private Boolean valid = true;
    //分片
    private String quarter;
    private String orderNo;
    private int orderNoSearchType = 0;//工单单号搜索类型
    //申请日期
    private Date beginDate;
    private Date endDate;
    private Customer customer;
    //店铺id
    private List<String> shopIds;
    //区域
    private Area area;
    private Area subArea;

    //配件状态
    //1：New - 待确认
    //2：Approved - 待发货
    //3：Sended - 已发货
    //4：Closed - 已完成
    //5：Reject - 已驳回
    private Integer status;

    private List<Integer> statuses;

    // 配件类型(MaterialType)
    //1:配件申请
    //2:返件申请
    private Integer materialType;

    //申请类型(material_apply_type)：
    //1-向师傅购买
    //2-厂家寄发
    private Integer applyType;
    //驳回原因
    private String rejectReason;
    //是否将驳回原因及详细描述合并在closeRemark供前端显示
    private boolean merchRjectReasonAndRemark = false;
    //用户电话(手机号)
    private String userPhone = "";
    private int isPhone = 0; //是否是合法的手机号码
    // 跟踪状态
    //1:缺货
    private Integer pendingType;

    //客服
    private Long kefu;

    private Long sales;
    private Integer subUserType = 0;//子账号类型
    //快递单号
    private String expressNo;

    private Integer customerType;//客户类型
    private Integer rushType;//突击区域类型

    private Integer kefuType;

    //线下单客户列表，跟单及业务查询使用
    private List<Long> offlineCustomerList;

    public int getOrderNoSearchType(){
        if (StringUtils.isNotBlank(this.orderNo)){
            this.orderNo = this.orderNo.trim().toUpperCase();
            String orderNoPrefix = Global.getConfig("OrderPrefix");
            if (orderNo.length() == 14 && orderNo.startsWith(orderNoPrefix)){
                orderNoSearchType = 1;
                String quarter = QuarterUtils.getOrderQuarterFromNo(orderNo);
                if(StringUtils.isNotBlank(quarter)){
                    this.quarter = quarter;
                }
            }else if (orderNo.startsWith(orderNoPrefix)){
                orderNoSearchType = 2;
            }
        }
        return this.orderNoSearchType;
    }

    public int getIsPhone(){
        if (StringUtils.isNotBlank(this.userPhone)){
            if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(this.userPhone))){
                this.isPhone = 1;
            }
        }
        return this.isPhone;
    }
}

