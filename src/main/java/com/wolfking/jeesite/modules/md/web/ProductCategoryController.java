package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.rpt.web.RPTDict;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/md/productcategory")
public class ProductCategoryController extends BaseController {
	@Autowired
	private ProductCategoryService productCategoryService;

//	@Autowired
//	private MSProductCategoryService msProductCategoryService;

	@Autowired
	private MSProductCategoryNewService msProductCategoryNewService;

	
	@ModelAttribute
	public ProductCategory get(@RequestParam(required=false) Long id) {
		if (id != null){
			//return productCategoryService.get(id);  //mark on 2020-3-16
			//return msProductCategoryService.getById(id); //add on 2020-3-16  //mark on 2020-4-1
			return msProductCategoryNewService.getByIdForMD(id); //add on 2020-4-1
		}else{
			return new ProductCategory();
		}
	}
	
	@RequiresPermissions("md:productcategory:view")
	@RequestMapping(value = {"list", ""})
	public String list(ProductCategory productCategory, HttpServletRequest request, HttpServletResponse response, Model model) {
        //Page<ProductCategory> page = productCategoryService.findPage(new Page<>(request, response), productCategory); //mark on 2019-8-13
		//Page<ProductCategory> page = msProductCategoryService.findList(new Page<>(request, response), productCategory); //add on 2019-8-13  //mark on 2020-4-1
		Map<String, Dict> dictList = MSDictUtils.getDictMap("groupCategory");

		Page<ProductCategory> page   = msProductCategoryNewService.findListForMD(new Page<>(request, response),productCategory); //add on 2020-4-1
		for(ProductCategory entity : page.getList()){
			if(entity.getGroupCategory() != null && entity.getGroupCategory()>0 ){
				Dict dict  = dictList.get(entity.getGroupCategory().toString());
				entity.setGroupCategoryName(dict != null ? dict.getLabel() : "");
			}
		}
        model.addAttribute("page", page);
		model.addAttribute("ProductCategory",productCategory);
		return "modules/md/productCategoryNewList";
	}

	@RequiresPermissions("md:productcategory:view")
	@RequestMapping(value = "form")
	public String form(ProductCategory productCategory, Model model) {
		model.addAttribute("productcategory", productCategory);
		return "modules/md/productCategoryNewForm";
	}

	@RequiresPermissions("md:productcategory:edit")
	@RequestMapping(value = "save")
	@ResponseBody
	public AjaxJsonEntity save(ProductCategory productCategory, Model model) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

		if (!beanValidator(model, productCategory)){
			ajaxJsonEntity.setSuccess(false);
			return ajaxJsonEntity;
		}

		try {
			if(productCategory.getGroupCategory() == null){
				productCategory.setGroupCategory(0);
			}
			productCategoryService.save(productCategory);
			ajaxJsonEntity.setMessage("保存成功");
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}

		return ajaxJsonEntity;
	}
	
	@RequiresPermissions("md:productcategory:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public AjaxJsonEntity delete(Long id, RedirectAttributes redirectAttributes) {
		//检查该分类下所有有效产品数量，如大于0，不能删除分类，必须先删除产品
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}
		if(productCategoryService.isExistProductByCategoryId(id)){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("该品类下已有产品，不能删除。");
		}
		else{
			productCategoryService.delete(new ProductCategory(id));
			ajaxJsonEntity.setMessage("删除产品分类成功");
		}
		return ajaxJsonEntity;
	}


	@ResponseBody
	@RequiresPermissions("md:productcategory:edit")
	@RequestMapping(value = "check")
	public AjaxJsonEntity checkExportTask(Long id) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			if(productCategoryService.isExistProductByCategoryId(id)){
				ajaxJsonEntity.setSuccess(false);
				ajaxJsonEntity.setMessage("该品类下已有产品，不能删除。");
			}

		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}

	@ResponseBody
	@RequestMapping(value = "checkProductCategoryCode")
	public String checkProductCategoryCode(Long id, String code, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		ProductCategory productCategory = new ProductCategory();
		productCategory.setId(id);
		productCategory.setCode(code);
		//false ：产品分类编码已经存在,true:不存在
		return productCategoryService.isExistProductCategoryCode(productCategory) ? "产品分类编码已经存在" : "true";
//		return productCategoryService.isExistProductCategoryCode(productCategory) ? "false" : "true";
	}

	@ResponseBody
	@RequestMapping(value = "checkProductCategoryName")
	public String checkProductCategoryName(Long id, String name, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		ProductCategory productCategory = new ProductCategory();
		productCategory.setId(id);
		productCategory.setName(name);
		//false ：产品分类名字已经存在,true:不存在
		return productCategoryService.isExistProductCategoryName(productCategory) ? "产品分类名字已经存在" : "true";
//		return productCategoryService.isExistProductCategoryName(productCategory) ? "false" : "true";
	}

	//TODO 改为缓存读取
	/**
	 * 获得产品分类json数据
	 * 包含id,pid,name三个节点内容
	 * @param type
	 * @param response
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required=false) Long type, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<ProductCategory> list = productCategoryService.findAllList();
		for (int i=0; i<list.size(); i++){
			ProductCategory e = list.get(i);
			Map<String, Object> map = Maps.newHashMap();
			map.put("id", e.getId());
			map.put("pId", "");
			map.put("name", e.getName());
			mapList.add(map);
		}
		return mapList;
	}

	@ResponseBody
	@RequestMapping(value = "ajax/getListByCustomer")
	public AjaxJsonEntity getListByCustomer(Long customerId)
	{
		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
		jsonEntity.setSuccess(false);
		if (customerId != null && customerId > 0){
			HashSet list=productCategoryService.getListByCustomer(customerId);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(list);
			return jsonEntity;
		}else
		{
			return jsonEntity;
		}

	}
	
}
