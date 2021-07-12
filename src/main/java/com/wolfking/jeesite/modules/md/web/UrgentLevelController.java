package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
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
@RequestMapping(value = "${adminPath}/md/urgentlevel")
public class UrgentLevelController extends BaseController
{
	@Autowired
	private UrgentLevelService urgentLevelService;

	@ModelAttribute
	public UrgentLevel get(@RequestParam(required = false) Long id)
	{
		if (id != null)
		{
			return urgentLevelService.get(id);
		} else
		{
			return new UrgentLevel();
		}
	}

	@RequiresPermissions("md:urgentlevel:view")
	@RequestMapping(value = { "list", "" })
	public String list(UrgentLevel urgentLevel, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<UrgentLevel> page = urgentLevelService.findPage(new Page<>(request, response), urgentLevel);
		model.addAttribute("page", page);
		return "modules/md/urgentLevelList";
	}

	@RequiresPermissions("md:urgentlevel:view")
	@RequestMapping(value = "form")
	public String form(UrgentLevel urgentLevel, Model model)
	{
		model.addAttribute("urgentLevel", urgentLevel);
		return "modules/md/urgentLevelForm";
	}

	@RequiresPermissions("md:urgentlevel:edit")
	@RequestMapping(value = "save")
	public String save(UrgentLevel urgentLevel, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, urgentLevel))
		{
			return form(urgentLevel, model);
		}
		urgentLevelService.save(urgentLevel);
		addMessage(redirectAttributes, "保存成功");
		return "redirect:" + adminPath + "/md/urgentlevel/list?repage";
	}

	@RequiresPermissions("md:urgentlevel:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, RedirectAttributes redirectAttributes)
	{
		urgentLevelService.delete(new UrgentLevel(id));
		addMessage(redirectAttributes, "删除成功");
		return "redirect:" + adminPath + "/md/urgentlevel/list?repage";
	}

}
