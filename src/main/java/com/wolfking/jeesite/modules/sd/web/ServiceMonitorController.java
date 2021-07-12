package com.wolfking.jeesite.modules.sd.web;

import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallServiceMonitorService;
import feign.FeignException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 天猫预警
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/serviceMonitor")
public class ServiceMonitorController extends BaseController
{
	@Autowired
	private TmallServiceMonitorService monitorService;

	/**
	 * 列表显示
	 * @param tmallServiceMonitor
	 * @param request
	 * @param response
	 * @param  model
	 * @returno
	 */
	@RequiresPermissions(value =
			{"sd:order:accept", "sd:order:plan", "sd:order:service",
					"sd:order:complete", "sd:order:return", "sd:order:grade",
					"sd:order:feedback"}, logical = Logical.OR)
	@RequestMapping(value = {"list", ""})
	public String list(TmallServiceMonitor tmallServiceMonitor, HttpServletRequest request, HttpServletResponse response, Model model) {
		if(tmallServiceMonitor.getStatus()==null || tmallServiceMonitor.getStatus()<0){
			tmallServiceMonitor.setStatus(1);
		}
		Page<TmallServiceMonitor> page = monitorService.findPage(new Page<>(request, response), tmallServiceMonitor);
		model.addAttribute("entity", tmallServiceMonitor);
		model.addAttribute("page", page);
		return "modules/sd/tmall/serviceMonitorList";
	}

	/**
	 * 跳转反馈页面
	 * @param tmallServiceMonitor
	 * @returno
	 */
	@RequiresPermissions("sd:servicemonitor:feedback")
	@RequestMapping(value="feedbackFrom")
	public String feedbackFrom(TmallServiceMonitor tmallServiceMonitor, Model model){
		model.addAttribute("entity", tmallServiceMonitor);
		return "modules/sd/tmall/monitorFeedbackForm";
	}

	/**
	 * 保存反馈信息
	 * @param serviceMonitor
	 * @returno
	 */
	@RequiresPermissions("sd:servicemonitor:feedback")
	@RequestMapping(value="updateFeedback")
	@ResponseBody
	public AjaxJsonEntity updateFeedback(TmallServiceMonitor serviceMonitor,HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user==null){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("超时,重新登录");
		}
		if(serviceMonitor.getId() ==null || serviceMonitor.getId()<=0
				|| serviceMonitor.getMonitorId() ==null || serviceMonitor.getMonitorId()<=0){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("参数错误");
		}
		serviceMonitor.setReplierId(user.getId());
		serviceMonitor.setReplierName(user.getName());
		serviceMonitor.setStatus(2);
		serviceMonitor.setReplyDate(new Date());
		try {
			monitorService.updateFeedback(serviceMonitor);
		} catch (OrderException oe){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(oe.getMessage());
		} catch (FeignException fe){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("因网络或其它原因，调用B2B接口错误,请稍后重试。");
		} catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 根据订到id获取数据
	 * @param serviceMonitor
	 * @returno
	 */
	@RequestMapping("/ajax/list")
	@ResponseBody
	public AjaxJsonEntity getListByOrderId(TmallServiceMonitor serviceMonitor,HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		List<TmallServiceMonitor> list = monitorService.getListByOrderId(serviceMonitor.getOrderId(),serviceMonitor.getQuarter());
		if(list!=null && list.size()>0){
			jsonEntity.setData(list);
		}else{
			list = new ArrayList<>();
			jsonEntity.setData(list);
		}
		return jsonEntity;
	}

}
