package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialCategoryService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping(value = "${adminPath}/md/materialCategory")
public class MaterialCategoryController extends BaseController
{

	@Autowired
	private MaterialCategoryService materialCategoryService;

	@Autowired
	private MSMaterialCategoryService msMaterialCategoryService;

	@Autowired
	private MaterialService materialService;


	/**
	 * 分页查询
	 *
	 * @param materialCategory
	 * @return
	 */
	@RequiresPermissions("md:materialcategory:view")
	@RequestMapping(value = { "list", "" })
	public String list(MaterialCategory materialCategory, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<MaterialCategory> page = materialCategoryService.findPage(new Page<>(request, response), materialCategory);
		model.addAttribute("page", page);
		model.addAttribute("materialCategory", materialCategory);
		model.addAttribute("materialCategoryList", msMaterialCategoryService.findAllListWithIdAndName());
		//return "modules/md/materialCategoryList";
		return "modules/md/materialCategoryListNew";
	}

	@RequiresPermissions("md:materialcategory:view")
	@RequestMapping("form")
	public String form(MaterialCategory materialCategory,Model model){
		if(materialCategory.getId()!=null && materialCategory.getId()>0){
			materialCategory = materialCategoryService.get(materialCategory.getId());
			if(materialCategory==null){
				materialCategory = new MaterialCategory();
			}
		}
	    model.addAttribute("materialCategory",materialCategory);
		model.addAttribute("canAction",true);
       return "modules/md/materialCategoryForm";
	}

	@RequiresPermissions("md:materialcategory:view")
	@RequestMapping("newForm")
	public String newForm(MaterialCategory materialCategory,Model model){
		if(materialCategory.getId()!=null && materialCategory.getId()>0){
			materialCategory = materialCategoryService.get(materialCategory.getId());
			if(materialCategory==null){
				materialCategory = new MaterialCategory();
			}
		}
		model.addAttribute("materialCategory",materialCategory);
		model.addAttribute("canAction",true);
		return "modules/md/materialCategoryFormNew";
	}


	/**
	 * 添加或者修改
	 *
	 * @param materialCategory
	 * @return
	 */
	@RequiresPermissions("md:materialcategory:edit")
	@RequestMapping("save")
	public String save(MaterialCategory materialCategory, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, materialCategory))
		{
			return form(materialCategory, model);
		}
       /* Long result = materialCategoryService.checkIsExist(materialCategory.getName());
		if((materialCategory.getId()==null || materialCategory.getId()<=0) && result!=null && result>0){
			model.addAttribute("message","配件类别已存在");
			return form(materialCategory,model);
		}else if(materialCategory.getId()!=null && result!=null && result>0 && !materialCategory.getId().equals(result)){
			model.addAttribute("message","配件类别已存在");
			return form(materialCategory,model);
		}else{
			materialCategoryService.save(materialCategory);
			addMessage(redirectAttributes, "保存成功");
		}*/
		materialCategoryService.save(materialCategory);
		addMessage(redirectAttributes, "保存成功");
		return "redirect:" + adminPath + "/md/materialCategory/list?repage";
	}

	@RequiresPermissions("md:materialcategory:edit")
	@RequestMapping("ajaxSave")
	@ResponseBody
	public AjaxJsonEntity ajaxSave(MaterialCategory materialCategory)
	{
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			materialCategoryService.save(materialCategory);
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}


	@RequiresPermissions("md:materialcategory:view")
	@RequestMapping("ajax/findAllList")
	@ResponseBody
	public AjaxJsonEntity ajaxFindAllList()
	{
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			List<MaterialCategory> materialCategoryList = msMaterialCategoryService.findAllListWithIdAndName();
			ajaxJsonEntity.setData(materialCategoryList);
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}



	/**
	 * 删除
	 *
	 * @param materialCategory
	 * @return
	 */
	@RequiresPermissions("md:materialcategory:edit")
	@RequestMapping(value = "delete")
	public String delete(MaterialCategory materialCategory, RedirectAttributes redirectAttributes)
	{
		Long result = materialService.getByMaterialCategoryId(materialCategory.getId());
		if(result!=null && result>0){
			addMessage(redirectAttributes, "已经关联配件不允许删除");
		}else{
			materialCategoryService.delete(materialCategory);
			addMessage(redirectAttributes, "删除成功");
		}
		return "redirect:" + adminPath + "/md/materialCategory/list?repage";
	}

	@ResponseBody
	@RequestMapping(value = "checkMaterialCategoryName")
	public String checkMaterialCategoryName(Long id, String name, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		MaterialCategory materialCategory = new MaterialCategory();
		materialCategory.setId(id);
		materialCategory.setName(name);
		//false ：品牌名字已经存在,true:不存在
		return materialCategoryService.isExistName(materialCategory) ? "类别名称已经存在" : "true";
	}

}
