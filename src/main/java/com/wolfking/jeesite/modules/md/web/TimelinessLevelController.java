package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.modules.md.service.TimelinessLevelService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "${adminPath}/md/timelinesslevel")
public class TimelinessLevelController extends BaseController
{
	@Autowired
	private TimelinessLevelService timelinessLevelService;

	@ModelAttribute
	public TimelinessLevel get(@RequestParam(required = false) Long id)
	{
		if (id != null)
		{
			return timelinessLevelService.get(id);
		} else
		{
			return new TimelinessLevel();
		}
	}

	@RequiresPermissions("md:timelinesslevel:view")
	@RequestMapping(value = { "list", "" })
	public String list(TimelinessLevel timelinessLevel, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<TimelinessLevel> page = timelinessLevelService.findPage(new Page<>(request, response), timelinessLevel);
		model.addAttribute("page", page);
		return "modules/md/timelinessLevelList";
	}

	@RequiresPermissions("md:timelinesslevel:view")
	@RequestMapping(value = "form")
	public String form(TimelinessLevel timelinessLevel, Model model)
	{
		model.addAttribute("timelinessLevel", timelinessLevel);
		return "modules/md/timelinessLevelForm";
	}

	@RequiresPermissions("md:timelinesslevel:edit")
	@RequestMapping(value = "save")
	public String save(TimelinessLevel timelinessLevel, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, timelinessLevel))
		{
			return form(timelinessLevel, model);
		}
		timelinessLevelService.save(timelinessLevel);
		addMessage(redirectAttributes, "保存成功");
		return "redirect:" + adminPath + "/md/timelinesslevel/list?repage";
	}

	@RequiresPermissions("md:timelinesslevel:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, RedirectAttributes redirectAttributes)
	{
		timelinessLevelService.delete(new TimelinessLevel(id));
		addMessage(redirectAttributes, "删除成功");
		return "redirect:" + adminPath + "/md/timelinesslevel/list?repage";
	}

}
