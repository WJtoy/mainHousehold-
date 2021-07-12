package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.SystemNotice;
import com.wolfking.jeesite.modules.md.service.SystemNoticeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@RequestMapping(value = "${adminPath}/md/sysNotice")
public class SystemNoticeController extends BaseController
{
	@Autowired
	private SystemNoticeService sysNoticeService;


	/**
	 * 网点通知列表
	 * @param systemNotice
	 * @return
	 */
	//@RequestMapping(value = { "servicePointNoticeList", "" })  //mark on 2020-7-11
	public String servicePointNoticeList(SystemNotice systemNotice, HttpServletRequest request, HttpServletResponse response, Model model){
		Page<SystemNotice> page = new Page<>(request,response);

		if(systemNotice.getStartDate() == null){
			systemNotice.setEndDate(DateUtils.getDateEnd(new Date()));
			systemNotice.setStartDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
		}else {
			systemNotice.setEndDate(DateUtils.getDateEnd(systemNotice.getEndDate()));
		}
		systemNotice.setNoticeType(SystemNotice.NOTICE_TYPE_SERVICE_POINT);
		//page = sysNoticeService.findPage(page,systemNotice);  //mark on 2020-7-11
		page = null;  //add on 2020-7-11
		model.addAttribute("page",page);
		return "modules/md/servicepointNoticeList";
	}




	/**
	 * 客户通知列表
	 * @param systemNotice
	 * @return
	 */
	//@RequestMapping(value = { "customerNoticeList"}) //mark on 2020-7-11
	public String customerNoticeList(SystemNotice systemNotice, HttpServletRequest request, HttpServletResponse response, Model model){
		Page<SystemNotice> page = new Page<>(request,response);

		if(systemNotice.getStartDate() == null){
			systemNotice.setEndDate(DateUtils.getDateEnd(new Date()));
			systemNotice.setStartDate(DateUtils.getDateStart(DateUtils.addMonth(new Date(), -1)));
		}else {
			systemNotice.setEndDate(DateUtils.getDateEnd(systemNotice.getEndDate()));
		}
		systemNotice.setNoticeType(SystemNotice.NOTICE_TYPE_CUSTOMER);
		//page = sysNoticeService.findPage(page,systemNotice);  //mark on 2020-7-11
		page = null;  //add on 2020-7-11
		model.addAttribute("page",page);
		return "modules/md/customerNoticeList";
	}


	@RequiresPermissions("md:sysnotice:add")
	//@RequestMapping(value = "form")  //mark on 2020-7-11
	public String form(SystemNotice systemNotice, Model model)
	{
		model.addAttribute("canAction",true);
		model.addAttribute("systemNotice",systemNotice);
		return "modules/md/systemNoticeForm";
	}

	/**
	 * 保存数据
	 * @param systemNotice
	 * @return
	 */
	@RequiresPermissions("md:sysnotice:add")
	//@RequestMapping(value = "save")  //mark on 2020-7-11
	public String save(SystemNotice systemNotice, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, systemNotice))
		{
			return form(systemNotice, model);
		}
		try{
			//sysNoticeService.save(systemNotice);  //mark on 2020-7-11
			addMessage(model, "发布成功");
			model.addAttribute("canAction",true);
			systemNotice.setContent("");
			return "modules/md/systemNoticeForm";
		}catch (Exception e){
			addMessage(model,e.getMessage());
			model.addAttribute("canAction",true);
			return "modules/md/systemNoticeForm";
		}
	}

	/**
	 * 查看详情
	 * @param title,subtitle,content
	 * @return
	 */
	//@RequestMapping(value = "findDetails")  //mark on 2020-7-11
	public String findDetails(String title,String subtitle,String content,Model model){
		SystemNotice systemNotice = new SystemNotice();
		systemNotice.setTitle(title);
		systemNotice.setSubtitle(subtitle);
		systemNotice.setContent(content);
		model.addAttribute(systemNotice);
		return "modules/md/noticeDetailsForm";
	}
}
