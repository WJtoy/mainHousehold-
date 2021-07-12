package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 切换为微服务
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/appExceptionType")
public class APPExceptionTypeController extends BaseController {
	@Autowired
//	private DictService dictService;
	private MSDictService dictService;

	@ModelAttribute
	public Dict get(@RequestParam(required = false) Long id) {
		if (id != null) {
			return dictService.get(id);
		}
		else {
			return new Dict();
		}
	}

	@RequiresPermissions("md:paymenttype:view")
	@RequestMapping(value ={ "list", "" })
	public String list(Dict dict, HttpServletRequest request,
                       HttpServletResponse response, Model model) {
		dict.setType("order_abnormal_reason");
//		Page<Dict> page = dictService.findList(new Page<Dict>(request,response), dict, 1);
		Page<Dict> page = dictService.findPage(new Page<Dict>(request,response), dict);
		model.addAttribute("page", page);
		return "modules/md/appExceptionTypeList";
	}

	@RequiresPermissions("md:paymenttype:view")
	@RequestMapping(value = "form")
	public String form(Dict dict, Model model) {
		model.addAttribute("dict", dict);
		return "modules/md/appExceptionTypeForm";
	}

	@RequiresPermissions("md:paymenttype:edit")
	@RequestMapping(value = "save")
	// @Valid
	public String save(Dict dict, HttpServletRequest request,
					   Model model,RedirectAttributes redirectAttributes) {
		if (Global.isDemoMode()) {
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + Global.getAdminPath()
					+ "/md/appExceptionType?repage&type=" + dict.getType();
		}
		dict.setType("order_abnormal_reason");
		if (!beanValidator(model, dict)) {
			return form(dict, model);
		}
		dictService.save(dict);
		addMessage(redirectAttributes, "保存异常原因'" + dict.getLabel() + "'成功");
		return "redirect:" + Global.getAdminPath() + "/md/appExceptionType?repage&type=" + dict.getType();
	}

	@RequiresPermissions("md:paymenttype:edit")
	@RequestMapping(value = "delete")
	public String delete(Dict dict, RedirectAttributes redirectAttributes) {
		if (Global.isDemoMode()) {
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + Global.getAdminPath()
					+ "/md/appExceptionType?repage";
		}
		dictService.delete(dict);
		addMessage(redirectAttributes, "删除异常原因成功");
		return "redirect:" + Global.getAdminPath()
				+ "/md/appExceptionType?repage";
	}

	@ResponseBody
	@RequestMapping(value = "testseqno")
	public AjaxJsonEntity testseqno(HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
		try {
			//String no = seqnoService.NextSequrenceNo("Customer");
			String no = SeqUtils.NextSequenceNo("Customer");
			result.setMessage(no);
		}
		catch (Exception e) {
			result.setSuccess(false);
			result.setMessage("产生单号错误:" + e.getMessage());
			log.error("产生单号错误:", e);
		}
		return result;
	}
}
