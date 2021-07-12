package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderInsurance;
import com.wolfking.jeesite.modules.sd.entity.OrderPlan;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePointFee;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author Ryan Lu
 * @version 1.0
 * 派单操作实体类
 * 定义派单需要的实体
 * @date 2020/4/30 8:58 下午
 */
@Slf4j
@NoArgsConstructor
@Data
public class PlanActionEntity {
    //用户
    private User user;
    //日期
    private Date date;
    //界面出入参数接收实例
    private Order order;
    //订单实例
    private Order o;
    //派单记录
    private OrderPlan orderPlan;
    //派单最新次序
    //private Integer nextPlanTimes;
    //网点结算方式
    private Dict engineerPaymentType;
    //保险单
    private OrderInsurance orderInsurance;
    //网点订单表
    private OrderServicePointFee servicePointFee;
    //客户短信通知内容
    private String userMsg;
    private String userPhone;

    //网点短信通知内容
    private String engineerMsg;
    private String engineerPhone;
    //网点app推送内容
    private AppPushMessage appPushMessage;

}
