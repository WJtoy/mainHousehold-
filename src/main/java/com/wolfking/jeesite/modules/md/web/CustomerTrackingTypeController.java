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

/**
 * 客户进度跟踪项目
 * @author ryan
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/md/trackingtype")
public class CustomerTrackingTypeController extends BaseController {
//	@Autowired
//	private DictService dictService;

	/**
	 * 切换为微服务
	 */
	@Autowired
	private MSDictService dictService;
	
	@ModelAttribute
	public Dict get(@RequestParam(required=false) Long id) {
		if (id != null){
			return dictService.get(id);
		}else{
			Dict dict = new Dict();
			java.util.Date dt = new java.util.Date();
			dict.setValue(Long.toString(dt.getTime()));
			return dict;
		}
	}
	
	@RequiresPermissions("md:trackingtype:view")
	@RequestMapping(value = {"list", ""})
	public String list(Dict dict, HttpServletRequest request, HttpServletResponse response, Model model) {
        dict.setType("TrackingType");
		Page<Dict> page = dictService.findPage(new Page<>(request, response), dict);
        model.addAttribute("page", page);
		return "modules/md/trackingTypeList";
	}

	@RequiresPermissions("md:trackingtype:view")
	@RequestMapping(value = "form")
	public String form(Dict dict, Model model) {
		model.addAttribute("dict", dict);
		return "modules/md/trackingTypeForm";
	}

	@RequiresPermissions("md:trackingtype:edit")
	@RequestMapping(value = "save")//@Valid
	public String save(Dict dict, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		dict.setType("TrackingType");
		dict.setAloneManagement(1);
		if (!beanValidator(model, dict)){
			return form(dict, model);
		}
		if(dict.getId() != null){
			java.util.Date dt = new java.util.Date();
			dict.setValue(Long.toString(dt.getTime()));
		}
		dictService.save(dict);
		addMessage(redirectAttributes, "保存客户跟踪项目'" + dict.getLabel() + "'成功");
		return "redirect:"+Global.getAdminPath()+"/md/trackingtype?repage&type="+dict.getType();
	}
	
	@RequiresPermissions("md:trackingtype:edit")
	@RequestMapping(value = "delete")
	public String delete(Dict dict, RedirectAttributes redirectAttributes) {
		dictService.delete(dict);
		addMessage(redirectAttributes, "删除客户跟踪项目成功");
		return "redirect:"+Global.getAdminPath()+"/md/trackingtype?repage";
	}
}
