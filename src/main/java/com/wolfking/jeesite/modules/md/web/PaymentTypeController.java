package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
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

//切换为微服务
@Controller
@RequestMapping(value = "${adminPath}/md/paymenttype")
public class PaymentTypeController extends BaseController {

	@Autowired
	private MSDictService msDictService;

	@ModelAttribute
	public Dict get(@RequestParam(required=false) Long id) {
		if (id != null){
			return msDictService.get(id);
		}else{
			return new Dict();
		}
	}
	
	@RequiresPermissions("md:paymenttype:view")
	@RequestMapping(value = {"list", ""})
	public String list(Dict dict, HttpServletRequest request, HttpServletResponse response, Model model) {
		dict.setType("PaymentType");
		Page<Dict> page = msDictService.findPage(new Page<Dict>(request, response), dict);
        model.addAttribute("page", page);
		return "modules/md/paymentTypeList";
	}

	@RequiresPermissions("md:paymenttype:view")
	@RequestMapping(value = "form")
	public String form(Dict dict, Model model) {
		model.addAttribute("dict", dict);
		return "modules/md/paymentTypeForm";
	}

	@RequiresPermissions("md:paymenttype:edit")
	@RequestMapping(value = "save")//@Valid
	public String save(Dict dict, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if(Global.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:"+Global.getAdminPath()+"/md/paymenttype?repage&type="+dict.getType();
		}
		dict.setType("PaymentType");
		dict.setAloneManagement(1);
		if (!beanValidator(model, dict)){
			return form(dict, model);
		}
		msDictService.save(dict);
		addMessage(redirectAttributes, "保存结算方式'" + dict.getLabel() + "'成功");
		return "redirect:"+Global.getAdminPath()+"/md/paymenttype?repage&type="+dict.getType();
	}
	
	@RequiresPermissions("md:paymenttype:edit")
	@RequestMapping(value = "delete")
	public String delete(Dict dict, RedirectAttributes redirectAttributes) {
		if(Global.isDemoMode()){
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:"+Global.getAdminPath()+"/md/paymenttype?repage";
		}
		msDictService.delete(dict);
		addMessage(redirectAttributes, "删除结算方式成功");
		return "redirect:"+Global.getAdminPath()+"/md/paymenttype?repage";
	}
}
