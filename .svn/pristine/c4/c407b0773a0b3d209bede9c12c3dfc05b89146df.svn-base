package com.wolfking.jeesite.modules.sales.sd.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sd.entity.Feedback;
import com.wolfking.jeesite.modules.sd.entity.FeedbackItem;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.service.FeedbackService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


/**
 * 网点工单信息
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/sales/sd/feedBack/")
public class SalesFeedBackController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private FeedbackService feedbackService;


    /**
     * 查看反馈内容
     * @param id	问题反馈id
     */
    @RequiresPermissions(value = { "sd:order:edit", "sd:order:add", "sd:feedback:add", "sd:feedback:edit" }, logical = Logical.OR)
    @RequestMapping(value = "replylist")
    public String replylist(@RequestParam String id, @RequestParam String quarter , String orderId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
    {
        User user = UserUtils.getUser();
        if (StringUtils.isBlank(id) && StringUtils.isBlank(orderId))
        {
            addMessage(model, "错误：问题反馈参数错误");
            model.addAttribute("handleFeedback",false);
            return "modules/sales/sd/feedback/replylist";
        }

        Feedback feedback = null;
        if(StringUtils.isNoneBlank(id)) {
            feedback = feedbackService.getWithItemsById(Long.valueOf(id), quarter);
        }else{
            Order order = orderService.getOrderById(Long.valueOf(orderId),quarter, OrderUtils.OrderDataLevel.CONDITION,true);
            if(order == null){
                addMessage(model, "错误：订单不存在");
                model.addAttribute("handleFeedback",false);
            }else{
                feedback = feedbackService.getWithItemsById(order.getOrderCondition().getFeedbackId(), quarter);
            }
        }
        if (feedback == null)
        {
            addMessage(model, "错误：读取问题反馈错误");
            model.addAttribute("handleFeedback",false);
        }else{
            //get replyFlag,replyFlagKefu,replyFlagCustomer from db
            Integer flag = 0;//1:对方回复，我方还未回复
            try {
                Order order = orderService.getOrderById(feedback.getOrder().getId(),feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                if(order == null || order.getOrderCondition()==null){
                    addMessage(model, "错误：读取订单失败，请重试");
                    model.addAttribute("handleFeedback",false);
                }else {
                    feedback.setReplyFlag(order.getOrderCondition().getReplyFlag());
                    if(user.isCustomer() || user.isSaleman()) {
                        flag = order.getOrderCondition().getReplyFlagKefu();

                    }else if(user.isKefu()) {
                        flag = order.getOrderCondition().getReplyFlagCustomer();
                    }
                    model.addAttribute("handleFeedback",flag == 1);
                }
            }catch (Exception e){
                log.error("[FeedbackController.replylist] id:{}",id,e);
            }
        }
        model.addAttribute("feedback", feedback);
        return "modules/sales/sd/feedback/replylist";
    }


    /**
     * 回复问题反馈
     * **/
    @ResponseBody
    @RequiresPermissions(value = { "sd:feedback:edit", "sd:order:add" }, logical = Logical.OR)
    @RequestMapping(value = "reply")
    public AjaxJsonEntity reply(FeedbackItem item, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if(item==null || StringUtils.isBlank(item.getRemarks())){
            result.setMessage("请输入回复内容");
            result.setSuccess(false);
        }else if(item.getFeedbackId()==null || item.getFeedbackId()<=0){
            result.setMessage("反馈主键为空");
            result.setSuccess(false);
        }else if(StringUtils.isBlank(item.getQuarter())){
            result.setMessage("数据库分片为空");
            result.setSuccess(false);
        }
        if(!result.getSuccess()){
            return result;
        }
        item.setCreateBy(user);
        item.setCreateDate(new Date());
        item.setContentType(0);//文本
        try {
            feedbackService.addItem(item);
            item.setCreateName(item.getCreateBy().getName());
            item.setCreateDateString(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));
            result.setData(item);
        }catch (Exception e){
            result.setMessage("保存回复失败："+e.getMessage());
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 上传图片
     */
    @RequiresPermissions(value =
            { "sd:order:edit", "sd:order:add", "sd:feedback:add", "sd:feedback:edit" }, logical = Logical.OR)
    @RequestMapping(value = "replyAttach")
    public String replyAttach(@RequestParam Long id,@RequestParam String quarter, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
    {
        if (id==null || id == 0)
        {
            addMessage(model, "客诉编号不存在");
            return "modules/sales/sd/feedback/replyAttach";
        }

        model.addAttribute("feedbackId", id);
        model.addAttribute("quarter", quarter);
        return "modules/sales/sd/feedback/replyAttach";
    }

    /**
     * 上传附件成功后，添加内容
     * 问题反馈，始终保留一个附件，存储在单头的attachment1
     * @param feedbackId 反馈id
     * @param remarks 上传图片地址
     * @return
     */
    @ResponseBody
    @RequiresPermissions(value =
            { "sd:order:edit", "sd:order:add", "sd:feedback:add", "sd:feedback:edit" }, logical = Logical.OR)
    @RequestMapping(value = "addImageItem")
    public AjaxJsonEntity addImageItem(@RequestParam Long feedbackId,
                                       @RequestParam String quarter,
                                       @RequestParam String remarks, HttpServletRequest request,
                                       HttpServletResponse response, Model model)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (feedbackId == null)
        {
            return result;
        }
        try
        {
            FeedbackItem item = new FeedbackItem();
            User user = item.getCurrentUser();
            item.setQuarter(quarter);
            item.setFeedbackId(feedbackId);
            item.setRemarks(remarks);
            item.setCreateBy(user);
            item.setCreateDate(new Date());
            if (user.isCustomer() || user.isSaleman()){
                item.setUserType(0);//customer
            }else{
                item.setUserType(1);//kkl
            }
            feedbackService.addImageItem(item);
            item.setCreateName(item.getCreateBy().getName());
            item.setCreateDateString(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));
            result.setData(item);
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage("上传图片错误:" + e.getMessage());
            //e.printStackTrace();
        }
        return result;
    }

    /**
     * 订单反馈异常处理
     * 标记反馈的异常已经处理
     * @param orderId 订单id
     */
    @ResponseBody
    @RequestMapping(value = "handled", method = RequestMethod.POST)
    public AjaxJsonEntity exceptionHandled(@RequestParam("orderId") Long orderId,@RequestParam("quarter") String quarter, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (orderId == null || orderId <=0){
            result.setSuccess(false);
            result.setMessage("订单参数传递错误");
            return result;
        }
        if (StringUtils.isBlank(quarter)){
            result.setSuccess(false);
            result.setMessage("数据分片参数传递错误");
            return result;
        }
        try
        {
            orderService.feedbackHandled(orderId,quarter,user);
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            //e.printStackTrace();
        }
        return result;
    }

    /**
     * 标记回复已读
     * 更改reply_flag=0
     * @param id 反馈id
     */
    @ResponseBody
    @RequestMapping(value = "read")
    public AjaxJsonEntity read(@RequestParam("id") Long id,@RequestParam("quarter") String quarter, HttpServletRequest request,HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (id == null)
        {
            return result;
        }

        try
        {
            Feedback feedback = feedbackService.get(id,quarter);
            Order order = orderService.getOrderById(feedback.getOrder().getId(),quarter, OrderUtils.OrderDataLevel.CONDITION,true);
            // 要增加消息队列补偿处理
            if(order == null || order.getOrderCondition() == null){
                return result;
            }
            OrderCondition condition = order.getOrderCondition();
            if(condition.getReplyFlag()==0){
                return result;
            }
            if(condition.getReplyFlag()==1 && !user.isCustomer()){
                return result;
            }
            if(condition.getReplyFlag()==2 && !user.isKefu()){
                return result;
            }
            feedback.setUpdateBy(user);
            feedback.setUpdateDate(new Date());
            feedbackService.read(feedback,condition);
        } catch (Exception e)
        {
            log.error("[SalesFeedBackController.read] id:{}",id,e);
            //e.printStackTrace();
        }
        return result;
    }

    /**
     * 添加问题反馈
     */
    @RequiresPermissions(value = { "sd:feedback:edit", "sd:feedback:add" }, logical = Logical.OR)
    @RequestMapping(value = "form")
    public String form(Feedback feedback, HttpServletRequest request, Model model)
    {
        User u = UserUtils.getUser();
        if(feedback == null || feedback.getOrder() == null || feedback.getOrder().getId() == null){
            addMessage(model, "错误:订单参数未传递。");
            model.addAttribute("canAction",false);
            model.addAttribute("feedback", feedback);
            return "modules/sales/sd/feedback/feedbackForm";
        }
        if(u.isCustomer()){
            feedback.setFeedFrom("厂商");
        }else{
            feedback.setFeedFrom("用户");
        }
        if (u.isCustomer() && u.getCustomerAccountProfile() == null)
        {
            addMessage(model, "错误:您未指定客户，不能保存投诉内容。");
            model.addAttribute("canAction",false);
            model.addAttribute("feedback", feedback);
            return "modules/sales/sd/feedback/feedbackForm";
        }
        Order o = orderService.getOrderById(feedback.getOrder().getId(),feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
        if(o == null || o.getOrderCondition() == null){
            addMessage(model, "错误:读取订单失败，请重试。");
            model.addAttribute("canAction",false);
        }else {
            feedback.setOrder(o);
            feedback.setCustomer(o.getOrderCondition().getCustomer());
            model.addAttribute("canAction",true);
        }
        model.addAttribute("feedback", feedback);
        return "modules/sales/sd/feedback/feedbackForm";
    }

    /**
     * 保存投诉
     */
    @RequiresPermissions(value = { "sd:feedback:edit", "sd:feedback:add" }, logical = Logical.OR)
    @ResponseBody
    @RequestMapping(value = "save")
    public AjaxJsonEntity save(Feedback feedback, HttpServletRequest request,HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (StringUtils.isBlank(feedback.getTitle()))
        {
            result.setSuccess(false);
            result.setMessage("请填写标题");
            return result;
        }
        if (feedback.getOrder() == null || feedback.getOrder().getId() == null)
        {
            result.setSuccess(false);
            result.setMessage("未关联订单:无法保存问题反馈。");
            return result;
        }

        if (StringUtils.isBlank(feedback.getQuarter()))
        {
            result.setSuccess(false);
            result.setMessage("数据库分片参数错误。");
            return result;
        }

        try
        {
            feedback.setCreateBy(user);
            feedback.setCreateDate(new Date());
            feedbackService.save(feedback);
            result.setData(feedback.getId().toString());
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage("保存问题反馈时错误:" + e.getMessage());
            //e.printStackTrace();
        }
        return result;
    }

    /**
     * [Ajax]订单日志-问题反馈
     * @param id	问题反馈id
     */
    @ResponseBody
    @RequestMapping(value = "feedbackLog")
    public AjaxJsonEntity feedbackLog(@RequestParam String id,@RequestParam String quarter, HttpServletResponse response)
    {
        User user = UserUtils.getUser();
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try
        {
            Long lorderId = Long.valueOf(id);
            if(lorderId == null || lorderId <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单参数错误");
                return jsonEntity;
            }
            List<FeedbackItem> items = feedbackService.getFeedbackItems(lorderId,quarter);
            if(items ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                jsonEntity.setData(items);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

}

