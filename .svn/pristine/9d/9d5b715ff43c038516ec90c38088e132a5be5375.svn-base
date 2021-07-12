package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.service.ProductCompletePicService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 产品完成图片管理
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/product/pic")
public class ProductCompletePicController extends BaseController
{
	@Autowired
	private ProductCompletePicService service;

	@RequiresPermissions("md:productpic:view")
	@RequestMapping(value = { "list", "" })
	public String list(ProductCompletePic entity, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		if(entity == null){
			entity = new ProductCompletePic();
		}
		Page<ProductCompletePic> page = service.findPage(new Page<>(request, response), entity);
		model.addAttribute("page", page);
		model.addAttribute("entity",entity);
		return "modules/md/productPicList";
	}

	@RequiresPermissions("md:productpic:view")
	@RequestMapping(value = "form")
	public String form(ProductCompletePic entity, Model model,HttpServletRequest request)
	{
		boolean canAction = true;
		String id = request.getParameter("id");
		String productId = request.getParameter("productId");
		if (StringUtils.isNotBlank(id) && StringUtils.isNumeric(id) && StringUtils.isNotBlank(productId) && StringUtils.isNumeric(productId))
		{
			entity = service.getFromCache(Long.valueOf(productId));
		} else
		{
			entity = new ProductCompletePic();
			entity.setItems(Lists.newArrayList());
		}
		List<Dict> picTypes = MSDictUtils.getDictList(ProductCompletePic.DICTTYPE);
		if(picTypes == null || picTypes.isEmpty()){
			addMessage(model,"未设定产品完成图片类型");
			canAction = false;
		}else{
			entity.parseItemsFromJson();
			List<ProductCompletePicItem> items = entity.getItems();
			items = service.mergeAllItems(items,picTypes);
			entity.setItems(items);
		}
		model.addAttribute("canAction",canAction);
		model.addAttribute("entity", entity);
		return "modules/md/productPicForm";
	}
	/*
	@RequiresPermissions("md:productpic:edit")
	@RequestMapping(value = "save")
	public String save(ProductCompletePic entity, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, entity))
		{
			return form(entity, model,request);
		}
		entity.toJsonInfo();//list to json
		service.save(entity);
		addMessage(redirectAttributes, "保存成功");
		return "redirect:" + adminPath + "/md/product/pic/list?repage";
	}*/

	@ResponseBody
	@RequiresPermissions("md:productpic:edit")
	@RequestMapping(value ="save",method = RequestMethod.POST)
	public AjaxJsonEntity save(ProductCompletePic entity, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		if(entity == null || entity.getProduct() == null || entity.getProduct().getId() == null || entity.getProduct().getId().longValue() == 0l
				|| entity.getItems() == null || entity.getItems().isEmpty()){
			result.setSuccess(false);
			result.setMessage("传入内容有误，请检查。");
			return result;
		}
		try
		{
			entity.toJsonInfo();
			service.save(entity);
		} catch (Exception e)
		{
			if(StringUtils.contains(e.getMessage(),"Duplicate")) {
				result.setSuccess(false);
				result.setMessage("数据库中数据重复定义，请确认");
			}else{
				result.setSuccess(false);
				result.setMessage(e.getMessage());
			}
		}
		return result;
	}

	@RequiresPermissions("md:productpic:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id,Long productId, RedirectAttributes redirectAttributes)
	{
		try {
			if (id == null || id.longValue() == 0l || productId == null || productId.longValue() == 0l) {
				addMessage(redirectAttributes, "删除错误:参数不合法");
			} else {
				service.delete(new ProductCompletePic(id,productId));
				addMessage(redirectAttributes, "删除成功");
			}
		}catch (Exception e){
			log.error("[ProductCompletePicController.delete]",e);
			addMessage(redirectAttributes, "删除错误:" + ExceptionUtils.getMessage(e));
		}finally {
			return "redirect:" + adminPath + "/md/product/pic/list?repage";
		}
	}

}
