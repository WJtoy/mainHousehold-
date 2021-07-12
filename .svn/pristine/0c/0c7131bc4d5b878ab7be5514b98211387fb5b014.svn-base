package com.wolfking.jeesite.modules.sd.web;


import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderCrush;
import com.wolfking.jeesite.modules.sd.entity.OrderFee;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderPendingSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.service.KefuOrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallAnomalyRecouseService;
import feign.FeignException;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//import com.wolfking.jeesite.common.web.FormToken;

/**
 * 天猫一键求助控制器
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/anomaly/")
@Slf4j
public class AnomalyRecourseController extends BaseController {

    //锁
    public static final String ANOMALY_LOCK = "anomaly:lock:%s";

    @Autowired
    private TmallAnomalyRecouseService anomalyRecouseService;

    @Autowired
    private RedisUtils redisUtils;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;


    //region 订单列表

    /**
     * 订单处理之一键预警列表
     */
    @RequiresPermissions(value =
            {"sd:order:accept", "sd:order:plan", "sd:order:complete","sd:order:anomaly",
                    "sd:order:return", "sd:order:grade", "sd:order:feedback"}, logical = Logical.OR)
    @RequestMapping(value = "list")
    public String list(TmallAnomalyRecourseSearchVM searchEntity, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<TmallAnomalyRecourse> page = new Page<TmallAnomalyRecourse>();
        User user = UserUtils.getUser();

        //date
        if (searchEntity.getSubmitStartDate() == null) {
            searchEntity.setSubmitEndDate(DateUtils.getDateEnd(new Date()));
            searchEntity.setSubmitStartDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -1)));
        } else {
            searchEntity.setSubmitEndDate(DateUtils.getDateEnd(searchEntity.getSubmitEndDate()));
        }
        try {
            //查询
            page = anomalyRecouseService.findList(new Page<TmallAnomalyRecourseSearchVM>(request, response), searchEntity);
        } catch (Exception e) {
            log.error("[AnomalyRecourseController.list] ", e);
            addMessage(model, "错误：" + e.getMessage());
        }
        model.addAttribute("page", page);
        model.addAttribute("searchEntity", searchEntity);
        return "modules/sd/tmall/anomalyList";

    }

    //region 订单详情页

    /**
     * [Ajax]客服订单详情-求助单列表
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/list")
    public AjaxJsonEntity ajaxList(@RequestParam String orderId, @RequestParam String quarter, HttpServletRequest request, HttpServletResponse response)
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
            List<TmallAnomalyRecourse> list = anomalyRecouseService.getListByOrder(lorderId,"",quarter);
            if(list ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else{
                jsonEntity.setData(list);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    //endregion 订单详情页

    //region 反馈

    /**
     * form
     */
    @RequiresPermissions(value = "sd:order:anomaly")
    @RequestMapping(value = "reply", method = RequestMethod.GET)
    //@FormToken(save = true)
    public String reply(String id,String anomalyId,String quarter, String refreshType,HttpServletRequest request, Model model) {
        String viewForm = "modules/sd/tmall/anomalyReplyForm";
        TmallAnomalyRecourse anomaly = new TmallAnomalyRecourse();
        Long lid = Long.valueOf(id);
        if (lid == null || lid <= 0) {
            addMessage(model, "错误：主键丢失");
            model.addAttribute("canSave", false);
            model.addAttribute("anomaly", anomaly);
            return viewForm;
        }
        Long lanomalyId = Long.valueOf(anomalyId);
        if (lanomalyId == null || lanomalyId <= 0) {
            addMessage(model, "错误：求助单号丢失");
            model.addAttribute("canSave", false);
            model.addAttribute("anomaly", anomaly);
            return viewForm;
        }
        String lockkey = String.format(ANOMALY_LOCK,anomalyId);
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB,lockkey)){
            addMessage(model,"错误:此求助单正在处理中，请稍候重试，或刷新页面。");
            model.addAttribute("canSave", false);
            model.addAttribute("anomaly", anomaly);
            return viewForm;
        }
        anomaly.setId(lid);
        anomaly.setAnomalyRecourseId(lanomalyId);
        anomaly.setQuarter(quarter);

        model.addAttribute("canSave", true);
        model.addAttribute("anomaly", anomaly);
        model.addAttribute("refreshType", refreshType);
        return viewForm;
    }

    /**
     * ajax提交
     */
    @RequiresPermissions("sd:order:anomaly")
    @ResponseBody
    @RequestMapping(value = "reply", method = RequestMethod.POST)
    public AjaxJsonEntity reply(TmallAnomalyRecourse entity, HttpServletResponse response)
    {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user == null || user.getId() == null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }

        if (entity == null || entity.getId() == null)
        {
            result.setSuccess(false);
            result.setMessage("反馈时发生错误：求助单号丢失");
            return result;
        }

        if (StringUtils.isBlank(entity.getReplyContent()))
        {
            result.setSuccess(false);
            result.setMessage("反馈时发生错误：请输入反馈内容，内容不超过250个汉字");
            return result;
        }

        TmallAnomalyRecourse anomaly = anomalyRecouseService.get(entity.getId());
        if (anomaly==null)
        {
            result.setSuccess(false);
            result.setMessage("系统中无此求助单，请确认！");
            return result;
        }

        if (anomaly.getStatus() != 0)
        {
            result.setSuccess(false);
            result.setMessage("求助单已反馈，请刷新求助列表!");
            return result;
        }

        try
        {
            entity.setStatus(1);
            entity.setReplierId(user.getId());
            entity.setReplierName(user.getName());
            entity.setReplyDate(new Date());
            entity.setReplyContent(StringUtils.left(entity.getReplyContent(),250));
            anomalyRecouseService.feedback(entity);
            result.setData(entity);
        } catch (OrderException oe){
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (FeignException fe){
            result.setSuccess(false);
            result.setMessage("因网络或其它原因，调用B2B接口错误,请稍后重试。");
            log.error("[AnomalyRecourseController.reply] anomalyId:{}",anomaly.getAnomalyRecourseId(),fe);
        } catch (Exception e){
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            log.error("[AnomalyRecourseController.reply] anomalyId:{}",anomaly.getAnomalyRecourseId(),e);
        }
        return result;
    }

    //endregion

    //endregion 订单列表
}

