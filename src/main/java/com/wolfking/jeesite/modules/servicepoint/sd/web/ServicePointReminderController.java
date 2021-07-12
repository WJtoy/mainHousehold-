package com.wolfking.jeesite.modules.servicepoint.sd.web;

import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderItem;
import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCacheField;
import com.wolfking.jeesite.modules.sd.entity.OrderCacheOpType;
import com.wolfking.jeesite.modules.sd.entity.OrderCacheParam;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderCacheReadService;
import com.wolfking.jeesite.modules.servicepoint.sd.service.ServicePointReminderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderViewModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;


/**
 * 网点催单
 */
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/sd/reminder")
@Slf4j
public class ServicePointReminderController extends BaseController {

    @Autowired
    private SpOrderCacheReadService orderCacheReadService;

    @Autowired
    private ServicePointReminderService reminderService;

    private static final String VIEW_NAME_SERVICEPOINT_REPLY_REMINDER_FORM = "modules/servicePoint/sd/reminder/servicePointReplyReminderForm";
    private static final String VIEW_NAME_SERVICEPOINT_VIEW_REMINDER_FORM = "modules/servicePoint/sd/reminder/servicePointViewReminderForm";


    /**
     * 处理窗口
     *
     * @param reminderId 催单id
     * @param quarter    分片
     */
    @RequiresPermissions(value = "sd:reminder:process")
    @RequestMapping(value = "servicePointReplyReminderForm", method = RequestMethod.GET)
    public String servicePointReplyReminderForm(@RequestParam String reminderId, @RequestParam(required = false) String quarter, Model model) {
        String formView = VIEW_NAME_SERVICEPOINT_REPLY_REMINDER_FORM;
        User user = UserUtils.getUser();
        Long lid = null;
        ReminderModel reminder = new ReminderModel();
        try {
            lid = Long.parseLong(reminderId);
        } catch (Exception e) {
            return reminderCheckFail(model, formView, reminder, "传入催单id类型不符");
        }
        if (StringUtils.isBlank(quarter)) {
            return reminderCheckFail(model, formView, reminder, "传入参数：数据分片 无内容");
        }
        Reminder reminderModel = reminderService.getReminderById(lid, quarter, 1);
        if (reminderModel == null) {
            return reminderCheckFail(model, formView, reminder, "读取催单失败，或催单单据不存在。");
        }
        ReminderItem reminderItem =null;
        if(reminderModel.getItems()!=null && reminderModel.getItems().size()>0){
            reminderItem = reminderModel.getItems().stream().filter(t->t.getStatus()==ReminderStatus.WaitReply.getCode()).findFirst().orElse(null);
        }
        //判断状态
        /*if (reminderModel.getStatus() != ReminderStatus.WaitReply.getCode()) {
            ReminderStatus reminderStatus = ReminderStatus.fromCode(reminderModel.getStatus());
            return reminderCheckFail(model, formView, reminder, "当前催单状态:" + (reminderStatus == null ? "" : reminderStatus.getMsg()) + ",不能进行处理操作。");
        }*/
        if(reminderItem==null){
            return reminderCheckFail(model, formView, reminder, "该催单无待回复的催单项,请检查");
        }
        reminder = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminderModel);
        if (reminder == null) {
            return reminderCheckFail(model, formView, reminder, "单据转换失败，请重试");
        }
        reminder.setItemId(reminderItem.getId());
        reminder.setCreateRemark(reminderItem.getCreateRemark());
        reminder.setCreateName(reminderItem.getCreateName());
        reminder.setCreateDate(DateFormatUtils.format(reminderItem.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
        /*if (reminderModel.getItemId() > 0 && !CollectionUtils.isEmpty(reminderModel.getItems())) {
            ReminderItem item = reminderModel.getItems().stream().filter(t -> t.getId() == reminderModel.getItemId()).findFirst().orElse(null);
            if (item != null) {
                reminder.setCreateRemark(item.getCreateRemark());
                reminder.setCreateName(item.getCreateName());
                reminder.setCreateDate(DateFormatUtils.format(item.getCreateAt(), "yyyy-MM-dd HH:mm:ss"));
            }
        }*/
        //reminder.setProcessRemark("您好，已回电客户【】，针对客户问题已与通方案为【】，用户认可，此问题预计【】处理完成，谢谢");
        //已与【联系电话】联系，沟通方案为【沟通方案】，预计完成时间【 完成时间】
        reminder.setProcessRemark("已与【" + reminder.getUserPhone() + "】联系，沟通方案为【】，预计完成时间【】");
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminder);
        return formView;
    }

    /**
     * 保存回复内容
     */
    //@RequiresPermissions("sd:reminder:process")
    @ResponseBody
    @RequestMapping(value = "servicePointReplyReminder")
    public AjaxJsonEntity servicePointReplyReminder(ReminderModel reminder, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        if (!SecurityUtils.getSubject().isPermitted("sd:reminder:process")) {
            return AjaxJsonEntity.fail("无[回复]权限，请联系管理员", null);
        }
        if (reminder == null) {
            return AjaxJsonEntity.fail("提交内容为空，请确认", null);
        }
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return AjaxJsonEntity.fail("登录超时，请重新登录。", null);
        }
        if (reminder.getId() <= 0) {
            return AjaxJsonEntity.fail("提交内容无催单id", null);
        }
        if (StringUtils.isBlank(reminder.getQuarter())) {
            return AjaxJsonEntity.fail("提交内容无分片数据", null);
        }
        if (StringUtils.isBlank(reminder.getProcessRemark())) {
            return AjaxJsonEntity.fail("请输入处理意见", null);
        }
        user.setName(StringUtils.left(user.getName(), 30));
        try {
            Long servicePointId = null;
            Order order = orderCacheReadService.getOrderById(reminder.getOrderId(), reminder.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            servicePointId = Optional.ofNullable(order).map(t -> t.getOrderCondition()).map(t -> t.getServicePoint()).map(t -> t.getId()).orElse(0L);
            reminder.setProcessRemark(StringUtils.left(reminder.getProcessRemark(), 250));
            reminderService.replyReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), reminder.getProcessRemark(), user, servicePointId,reminder.getItemId());
            //淘汰订单orderStatus缓存
            OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
            cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(reminder.getOrderId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(cacheBuilder.build());
            return AjaxJsonEntity.success("保存成功", null);
        } catch (Exception e) {
            log.error("[ReminderController.saveProcess] reminderNo:{} ,userId:{}", reminder.getReminderNo(), user.getId(), e);
            LogUtils.saveLog("回复催单", "ReminderController.saveProcess", reminder.getReminderNo(), e, user);
            return AjaxJsonEntity.fail("保存失败:" + ExceptionUtils.getRootCauseMessage(e), null);
        }
    }


    /**
     * 查看催单表单
     *
     * @param reminderId 催单id
     * @param quarter    分片
     */
    @RequestMapping(value = "servicePointViewReminder", method = RequestMethod.GET)
    public String servicePointViewReminder(@RequestParam String reminderId, @RequestParam String orderId, @RequestParam(required = false) String quarter, Model model) {
        User user = UserUtils.getUser();
        ReminderModel reminderModel = new ReminderModel();
        String formView = VIEW_NAME_SERVICEPOINT_VIEW_REMINDER_FORM;
        if (StringUtils.isBlank(reminderId) && StringUtils.isBlank(orderId)) {
            return reminderCheckFail(model, formView, reminderModel, "参数为空。");
        }

        Long lid = null;
        Long lorderId = null;
        if (StringUtils.isNotBlank(reminderId)) {
            try {
                lid = Long.valueOf(reminderId);
            } catch (Exception e) {
                lid = 0l;
            }
        } else {
            try {
                lorderId = Long.valueOf(orderId);
            } catch (Exception e) {
                lorderId = 0l;
            }
        }
        if ((lorderId == null || lorderId <= 0) && (lid == null || lid <= 0)) {
            return reminderCheckFail(model, formView, reminderModel, "催单参数类型错误。");
        }
        Reminder reminder = null;
        try {
            if (lid != null) {
                //按催单id读取
                reminder = reminderService.getReminderById(lid, quarter, 1);
            } else {
                reminder = reminderService.getLastReminderByOrderId(lorderId, quarter);
            }
            if (reminder != null && reminder.getServicepointId() == 0) {
                //get service point
                Order order = orderCacheReadService.getOrderById(reminder.getOrderId(), reminder.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
                Long servicePointId = Optional.ofNullable(order).map(o -> o.getOrderCondition()).map(o -> o.getServicePoint()).map(s -> s.getId()).orElse(null);
                if (servicePointId != null && servicePointId > 0) {
                    reminder.setServicepointId(servicePointId);
                }
            }
        } catch (Exception e) {
            return reminderCheckFail(model, formView, reminderModel, "读取催单错误。");
        }
        if (reminder == null) {
            return reminderCheckFail(model, formView, reminderModel, "读取催单失败,或催单不存在，请重试。");
        }

        reminderModel = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminder);
        if (reminderModel == null) {
            return reminderCheckFail(model, formView, new ReminderModel(), "转换催单失败,请重试。");
        }
        model.addAttribute("canAction", true);
        model.addAttribute("reminder", reminderModel);
        return formView;
    }

    private String reminderCheckFail(Model model, String formView, ReminderModel reminder, String s) {
        addMessage(model, s);
        model.addAttribute("canAction", false);
        model.addAttribute("reminder", reminder);
        return formView;
    }


}
