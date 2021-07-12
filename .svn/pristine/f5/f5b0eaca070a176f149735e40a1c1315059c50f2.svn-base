/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sys.web;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Sequence;
import com.wolfking.jeesite.modules.sys.service.SequenceService;
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
 * 字典单据编号规则Controller
 * @author ThinkGem
 * @version 2013-3-23
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/sequence")
public class SequenceController extends BaseController {

	@Autowired
	private SequenceService seqService;
	
	@ModelAttribute
	public Sequence get(@RequestParam(required=false) Long id) {
		if (id != null){
			return seqService.get(id);
		}else{
			return new Sequence();
		}
	}
	
	@RequiresPermissions("sys:sequence:view")
	@RequestMapping(value = {"list", ""})
	public String list(Sequence seq, HttpServletRequest request, HttpServletResponse response, Model model) {
		model.addAttribute("seq", seq);
		Page<Sequence> page = seqService.findPage(new Page<Sequence>(request, response), seq);
        model.addAttribute("page", page);

		//SeqUtils.NextSequenceNo("OrderNo");
		return "modules/sys/seqList";
	}

	@RequiresPermissions("sys:sequence:view")
	@RequestMapping(value = "form")
	public String form(Sequence seq, Model model) {
		model.addAttribute("seq", seq);
		return "modules/sys/seqForm";
	}

	@RequiresPermissions("sys:sequence:edit")
	@RequestMapping(value = "save")
	public String save(Sequence seq, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, seq)){
			return form(seq, model);
		}
		seqService.save(seq);
		addMessage(redirectAttributes, "保存单据编号'" + seq.getCode() + "'成功");
		return "redirect:"+ Global.getAdminPath()+"/sys/sequence?repage";
	}
	
	@RequiresPermissions("sys:sequence:edit")
	@RequestMapping(value = "delete")
	public String delete(Sequence sequence, RedirectAttributes redirectAttributes) {
		seqService.delete(sequence);
		addMessage(redirectAttributes, "删除单据编号成功");
		return "redirect:"+Global.getAdminPath()+"/sys/sequence?repage";
	}
}
