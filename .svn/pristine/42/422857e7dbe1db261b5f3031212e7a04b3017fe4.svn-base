package com.wolfking.jeesite.modules.sales.sd.controller;


import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderItem;
import com.kkl.kklplus.entity.cc.vm.RereminderCheckRespone;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.Exceptions;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderViewModelMapper;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * 网点工单信息
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/sales/sd/reminder/")
public class SalesReminderController extends BaseController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private OrderService orderService;

    /**
     * [Ajax]订单详情页-催单列表(tab)
     * @param orderId	订单id
     * @param quarter 	分片
     * @param detailType 详情页类型 1:客服 如有待回复催单，显示催单回复界面 2:客户，只显示催单列表
     * @return Map<String,Object>
     *     isWaitReply:1 待回复 ，显示待回复催单内容；0：已回复，显示催单内容，不可回复
     *     value: isWait=1，待回复/待确认的催单实例；0:最新的催单实例
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/list")
    public AjaxJsonEntity ajaxOrderReminderList(@RequestParam String orderId, @RequestParam String quarter, @RequestParam Integer detailType, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try
        {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId == null || lorderId <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单参数错误");
                return jsonEntity;
            }
            Map<String,Object> data = Maps.newHashMapWithExpectedSize(5);
            data.put("reminderCheckFlag",1);
            data.put("reminderCheckMsg","");
            data.put("needConfirm",1);
            Reminder reminder = reminderService.getLastReminderByOrderId(lorderId,quarter);
            if(reminder == null){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("读取催单信息失败，请重试");
                return jsonEntity;
            }
            if(reminder.getItemId()>0 && !CollectionUtils.isEmpty(reminder.getItems())){
                ReminderItem item = reminder.getItems().stream().filter(t->t.getId() == reminder.getItemId()).findFirst().orElse(null);
                if(item != null){
                    reminder.setReminderRemark(item.getCreateRemark());
                    reminder.setReminderReason(item.getReminderReason());
                }
            }
            /*if(reminder.getStatus() == ReminderStatus.Replied.getCode()){*/
            //检查是否可以再次催单
            RereminderCheckRespone checkRespone = reminderService.reReminderCheck(reminder.getOrderId(),reminder.getQuarter(),System.currentTimeMillis());
            if(checkRespone.getCode() != 1){
                data.put("reminderCheckMsg",checkRespone.getMsg());
                data.put("reminderCheckFlag",0);
            }
            //}
            ReminderModel model = Mappers.getMapper(ReminderViewModelMapper.class).toViewModel(reminder);
            if((user.isAdmin() || user.isCustomer() || user.isSaleman() || user.isMerchandiser()) && detailType == 2){
                ReminderItem item = reminder.getItems().stream().filter(t->t.getStatus()==2).findFirst().orElse(null);
                if(item!=null){
                    model.setProcessRemark(item.getProcessRemark());
                    model.setReminderReason(item.getReminderReason());
                    model.setItemId(item.getId());
                    data.put("isWait",1);
                    if(item.getStatus() == 2 && (user.isMerchandiser() || user.isSaleman())){
                        data.put("needConfirm",1); //确认按钮权限
                    }
                }else{
                    data.put("isWait",0);
                }

            }else if((user.isKefu() || user.isInnerAccount() || user.isAdmin() || user.isSystemUser()) && detailType == 1){
                ReminderItem item = reminder.getItems().stream().filter(t->t.getStatus()==1).findFirst().orElse(null);
                if(item != null){
                    model.setCreateRemark(item.getCreateRemark());
                    model.setReminderReason(item.getReminderReason());
                    model.setItemId(item.getId());
                    data.put("isWait",1);
                }else{
                    data.put("isWait",0);
                }
                model.setProcessRemark("已与【" + reminder.getUserPhone() + "】联系，沟通方案为【】，预计完成时间【】");
            }else{
                data.put("isWait",0);
            }
            model.setProcessRemark(StringUtils.toString(model.getProcessRemark()));
            //获取催单类型
            List<Dict> dicts = MSDictUtils.getDictList("reminder_reason");
            data.put("value",model);
            data.put("reminderReasons",dicts);
            jsonEntity.setData(data);
            return jsonEntity;

        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(Exceptions.getRootCauseMessage(e));
        }
        return jsonEntity;
    }

    /**
     * 保存确认意见
     */
    //@RequiresPermissions("sd:reminder:complete")
    @ResponseBody
    @RequestMapping(value = "/saveConfirm")
    public AjaxJsonEntity saveConfirm(ReminderModel reminder, HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setContentType("application/json; charset=UTF-8");
        if(reminder == null){
            return AjaxJsonEntity.fail("提交内容为空，请确认",null);
        }
        Integer actionCode = reminder.getAction();
        if(actionCode == null || actionCode <=0 || actionCode >2){
            return AjaxJsonEntity.fail("提交操作错误，请确认",null);
        }
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            return AjaxJsonEntity.fail("登录超时，请重新登录。",null);
        }
        if(reminder.getId() <=0){
            return AjaxJsonEntity.fail("提交内容无催单id",null);
        }
        if(StringUtils.isBlank(reminder.getQuarter())){
            return AjaxJsonEntity.fail("提交内容无分片数据",null);
        }
        if(StringUtils.isBlank(reminder.getProcessRemark())){
            return AjaxJsonEntity.fail("请输入催单意见",null);
        }
        user.setName(StringUtils.left(user.getName(),30));
        Order order = null;
        order = orderService.getOrderById(reminder.getOrderId(),reminder.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
        if(order == null || order.getOrderCondition() == null){
            return AjaxJsonEntity.fail("确认订单信息失败",null);
        }
        try{
            reminder.setProcessRemark(StringUtils.left(reminder.getProcessRemark(),250));
            OrderCondition condition = order.getOrderCondition();
            Long servicePointId = Optional.ofNullable(condition.getServicePoint()).map(t->t.getId()).orElse(0L);
            if(actionCode == 1) {//确认
                reminderService.confirmReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), reminder.getProcessRemark(), user,servicePointId,reminder.getItemId());
            }else {//再次催单
                reminderService.rejectReminder(reminder.getId(), reminder.getOrderId(), reminder.getQuarter(), reminder.getProcessRemark(), user,reminder.getStatus(),servicePointId,reminder.getReminderReason());
                if(order != null && order.getOrderCondition() != null) {
                    OrderCondition orderCondition = order.getOrderCondition();
                    PushMessageUtils.pushReminderMessage(orderCondition.getServicePoint(), orderCondition.getEngineer(), order.getOrderNo());
                }
            }
            //淘汰订单orderStatus缓存
            OrderCacheParam.Builder cacheBuilder = new OrderCacheParam.Builder();
            cacheBuilder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(reminder.getOrderId())
                    .setDeleteField(OrderCacheField.ORDER_STATUS);
            OrderCacheUtils.update(cacheBuilder.build());
            return AjaxJsonEntity.success("保存成功",null);
        }catch (Exception e){
            log.error("reminderNo:{} ,userId:{}",reminder.getReminderNo(),user.getId(),e);
            LogUtils.saveLog("确认催单","ReminderController.saveConfirm",reminder.getReminderNo(),e,user);
            return AjaxJsonEntity.fail("保存失败:" + Exceptions.getRootCauseMessage(e),null);
        }
    }
}

