package com.wolfking.jeesite.ms.b2bcenter.sd.controller;


import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterAbnormalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * B2B异常订单Controller
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/b2b/b2bcenter/abnormalOrder/")
public class B2BCenterAbnormalOrderController extends BaseController {

    private static final String MODEL_ATTR_PAGE = "page";
    private static final String MODEL_ATTR_ORDER = "order";

    @Autowired
    private B2BCenterAbnormalOrderService b2BCenterAbnormalOrderService;


    /**
     * 待审核退单列表 (for 客服主管)
     * 客服提出退单申请，由客服主管审核
     */
    @RequiresPermissions("sd:b2bOrder:approveReturn")
    @RequestMapping(value = "b2bOrderReturnApproveList")
    public String orderReturnApproveList(OrderSearchModel order, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/b2bcenter/sd/b2bOrderReturnApproveList";
        Page<Order> page = new Page<>();
        User user = UserUtils.getUser();
        order = setSearchModel(user, order, 1, true, 90, false, 0);
        Boolean isValide = checkOrderNoAndPhone(order, model, page);
        if (!isValide) {
            model.addAttribute("canSearch", false);
            return viewForm;
        }
        try {
            Date[] dates = OrderUtils.getQuarterDates(order.getBeginDate(), order.getEndDate(), 0, 0);
            List<String> quarters = QuarterUtils.getQuarters(dates[0], dates[1]);
            if (quarters != null && quarters.size() > 0) {
                order.setQuarters(quarters);
            }
            page = b2BCenterAbnormalOrderService.getB2BOrderReturnApproveList(new Page<>(request, response), order);
        } catch (Exception e) {
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute(MODEL_ATTR_PAGE, page);
        model.addAttribute(MODEL_ATTR_ORDER, order);
        model.addAttribute("canSearch", true);
        return viewForm;
    }

    /**
     * 设置及初始化查询条件
     *
     * @param user                      当前帐号
     * @param searchModel               查询条件
     * @param initMonths                初始最小查询时间段(月)
     * @param searchByOrderDateRange    by下单日期查询开关
     * @param maxOrderDays              下单最大查询范围(天)
     * @param searchByCompleteDateRange by完成日期查询开关
     * @param maxCompleteDays           完成最大查询范围(天)
     */
    private OrderSearchModel setSearchModel(User user, OrderSearchModel searchModel, int initMonths, boolean searchByOrderDateRange, int maxOrderDays, boolean searchByCompleteDateRange, int maxCompleteDays) {
        if (searchModel == null) {
            searchModel = new OrderSearchModel();
        }
        Area area = searchModel.getArea();
        if (area == null) {
            area = new Area(0L);
            searchModel.setArea(area);
        }
        if (area.getParent() == null || area.getParent().getId() == null) {
            area.setParent(new Area(0L));
        }
        //客服主管
        boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");
        if (searchModel.getStatus() == null || StringUtils.isBlank(searchModel.getStatus().getValue())) {
            searchModel.setStatus(null);
        }

        Date now = new Date();
        //下单日期
        if (searchByOrderDateRange) {
            if (searchModel.getBeginDate() == null) {
                searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
                searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
            } else {
                searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
            }
            //检查最大时间范围
            if (maxOrderDays > 0) {
                Date maxDate = DateUtils.addDays(searchModel.getBeginDate(), maxOrderDays - 1);
                maxDate = DateUtils.getDateEnd(maxDate);
                if (DateUtils.pastDays(maxDate, searchModel.getEndDate()) > 0) {
                    searchModel.setEndDate(maxDate);
                }
            }
        }
        //完成日期
        if (searchByCompleteDateRange) {
            if (searchModel.getCompleteEnd() != null) {
                searchModel.setCompleteEnd(DateUtils.getDateEnd(searchModel.getCompleteEnd()));
            }
            //检查最大时间范围
            if (maxCompleteDays > 0 && searchModel.getCompleteBegin() != null) {
                Date maxDate = DateUtils.addDays(searchModel.getCompleteBegin(), maxCompleteDays - 1);
                maxDate = DateUtils.getDateEnd(maxDate);
                if (searchModel.getCompleteEnd() == null) {
                    searchModel.setCompleteEnd(DateUtils.getDateEnd(now));
                }
                if (DateUtils.pastDays(maxDate, searchModel.getCompleteEnd()) > 0) {
                    searchModel.setCompleteEnd(maxDate);
                }
            }
        }
        int subQueryUserArea = 1;
        //vip客服查询自己负责的单，by客户+区域+品类
        //1.by 客户，前端客户已按客服筛选了
        if (user.isKefu() && user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
            //vip客服
            searchModel.setSubQueryUserCustomer(1);//指派客户，关联sys_user_customer
        } else if (user.isKefu() && user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
            //普通客服，不能查询vip客户订单
            searchModel.setCustomerType(0);
        } else {
            searchModel.setSubQueryUserCustomer(0);//未指派客户，不关联sys_user_customer
        }
        //2.by 区域
        //如果是客服，要按其负责的区域过滤，前端区域选择已经按安维做筛选
        //其余系统帐号，不限定区域
        //如选择的区域是 [区/县]级，则直接查询订单的area_id与传入值相等
        //否则，需要关联sys_area表，根据parent_ids like查询
        if (isServiceSupervisor) {
            searchModel.setSubQueryUserArea(1);
            searchModel.setCreateBy(user);//*
        } else if (user.isKefu()) {
            searchModel.setSubQueryUserArea(subQueryUserArea);
            searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
        } else if (user.isInnerAccount()) { //内部帐号
            searchModel.setSubQueryUserArea(1);
            searchModel.setCreateBy(user);//*
        } else {
            searchModel.setSubQueryUserArea(0);//非客服可查询任何区域
        }
        return searchModel;
    }

    /**
     * 检查订单号，手机号输入
     *
     * @param searchModel
     * @param model
     * @return
     */
    private Boolean checkOrderNoAndPhone(OrderSearchModel searchModel, Model model, Page<Order> page) {
        if (searchModel == null) {
            return true;
        }
        //检查电话
        int orderSerchType = searchModel.getOrderNoSearchType();
        if (StringUtils.isNotBlank(searchModel.getOrderNo())) {
            if (orderSerchType != 1) {
                addMessage(model, "错误：请输入正确的订单号码");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            } else {
                //检查分片
                try {
                    Date goLiveDate = OrderUtils.getGoLiveDate();
                    String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
                    if (quarters.length == 2) {
                        int start = StringUtils.toInteger(quarters[0]);
                        int end = StringUtils.toInteger(quarters[1]);
                        if (start > 0 && end > 0) {
                            int quarter = StringUtils.toInteger(searchModel.getQuarter());
                            if (quarter < start || quarter > end) {
                                addMessage(model, "错误：请输入正确的订单号码");
                                model.addAttribute(MODEL_ATTR_PAGE, page);
                                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查分片错误,orderNo:{}", searchModel.getOrderNo(), e);
                }
            }
        }
        if (StringUtils.isNotBlank(searchModel.getPhone1())) {
            if (searchModel.getIsPhone() != 1) {
                addMessage(model, "错误：请输入正确的用户电话");
                model.addAttribute(MODEL_ATTR_PAGE, page);
                model.addAttribute(MODEL_ATTR_ORDER, searchModel);
                return false;
            }
        }
        return true;
    }
}

