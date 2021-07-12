/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.modules.md.service.GradeService;
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

/**
 * 项目客评Controller
 * @author
 * @version
 */
@Controller
@RequestMapping(value = "${adminPath}/md/grade")
public class GradeController extends BaseController {

	@Autowired
	private GradeService gradeService;
	
	@ModelAttribute
	public Object get(@RequestParam(required=false) Long id, @RequestParam(required=false) String type) {
		if(StringUtils.isBlank(type) || StringUtils.equals(type, "grade") ){
			if (id != null){
				return gradeService.get(id);
			}else{
				return new Grade();
			}
		}
		else
		{
			if (id != null){
				return gradeService.getItem(id);
			}else{
				return new GradeItem();
			}
		}
	}
	
	@RequiresPermissions("md:grade:view")
	@RequestMapping(value = {"list", ""})
	public String list(Grade grade, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<Grade> page = gradeService.find(new Page<Grade>(request, response), grade); 
        model.addAttribute("page", page);
		return "modules/md/gradeList";
	}
	
	@RequiresPermissions("md:grade:view")
	@RequestMapping(value = "form")
	public String form(Grade grade, Model model) {
		model.addAttribute("grade", grade);
		return "modules/md/gradeForm";
	}
	
	@RequiresPermissions("md:grade:edit")
	@RequestMapping(value = "save")
	public String save(Grade grade, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, grade)){
			return form(grade, model);
		}
		gradeService.save(grade);
		addMessage(redirectAttributes, "保存客评项目'" + grade.getName() + "'成功");
		return "redirect:"+Global.getAdminPath()+"/md/grade";
	}
	
	@RequiresPermissions("md:grade:edit")
	@RequestMapping(value = "delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {
		gradeService.deleteGrade(StringUtils.toLong(id));
		addMessage(redirectAttributes, "删除客评项目成功");
		return "redirect:"+Global.getAdminPath()+"/md/grade?repage";
	}
	
	//编辑评分标准
	@RequiresPermissions("md:grade:edit")
	@RequestMapping(value = "itemform")
	public String itemform(GradeItem item, HttpServletRequest request, Model model) {
		if(item == null)
			item = new GradeItem();
		item.setType("item");
		if(item.getId() == null){
			String gradeId = request.getParameter("gradeId");
			if (StringUtils.isNotBlank(gradeId)){
				Grade grade= gradeService.get(StringUtils.toLong(gradeId));
				item.setGrade(grade);
			}
		}
		model.addAttribute("item", item);
		return "modules/md/gradeItemForm";
	}
	
	
	
	@RequiresPermissions("md:grade:edit")
	@RequestMapping(value = "saveitem")
	public String saveitem(GradeItem item, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, item)){
			return itemform(item,request, model);
		}
		Grade grade = gradeService.get(item.getGrade().getId());
		//客评标准分值不能大于项目分值
		if(item.getPoint()>grade.getPoint()){
			model.addAttribute("message", "客评标准分值不能大于项目分值:"+grade.getPoint().toString());
			return itemform(item,request, model);
		}
		
		gradeService.save(item);
		addMessage(redirectAttributes, "保存客评标准成功");
		return "redirect:"+Global.getAdminPath()+"/md/grade";
	}
	
	@RequiresPermissions("md:grade:edit")
	@RequestMapping(value = "deleteitem")
	public String delete(String gradeId,String id, RedirectAttributes redirectAttributes) {
		
		gradeService.deleteGradeItem(StringUtils.toLong(gradeId), StringUtils.toLong(id));
		addMessage(redirectAttributes, "删除客评标准成功");
		return "redirect:"+Global.getAdminPath()+"/md/grade?repage";
	}

}
