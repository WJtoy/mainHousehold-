package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDMaterialRequirement;
import com.kkl.kklplus.entity.viomi.sd.ProductParts;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.MaterialCategory;
import com.wolfking.jeesite.modules.md.entity.MaterialRequire;
import com.wolfking.jeesite.modules.md.service.MaterialCategoryService;
import com.wolfking.jeesite.modules.md.service.MaterialService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/material")
public class MaterialController extends BaseController
{
	@Autowired
	private MaterialService materialService;

	@Autowired
	private MSMaterialService msMaterialService;

	@Autowired
	private ProductService productService;

	@Autowired
	private MaterialCategoryService materialCategoryService;

	@ModelAttribute
	public Material get(@RequestParam(required = false) Long id)
	{
		if (id != null)
		{
			return materialService.get(id);
		} else
		{
			return new Material();
		}
	}

	@RequiresPermissions("md:material:view")
	@RequestMapping(value = { "list", "" })
	public String list(Material material, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<Material> page = materialService.findPage(new Page<>(request, response), material);
		List<MaterialCategory> materialCategoryList = materialCategoryService.findAllList();
		Map<Long,MaterialCategory> map = materialCategoryList.stream().collect(Collectors.toMap(MaterialCategory::getId, materialCategory ->materialCategory));
		if(page.getList()!=null && page.getList().size()>0){
            for(Material item:page.getList()){
                MaterialCategory materialCategory = map.get(item.getMaterialCategory().getId());
                if(materialCategory!=null){
					item.getMaterialCategory().setName(materialCategory.getName());
				}
			}
	    }
	    model.addAttribute("materialCategoryList",materialCategoryList);
		model.addAttribute("page", page);
		//return "modules/md/materialList";
		return "modules/md/materialListNew";
	}

	@RequiresPermissions("md:material:view")
	@RequestMapping(value = "form")
	public String form(Material material, Model model)
	{
		List<MaterialCategory> list = materialCategoryService.findAllList();
		model.addAttribute("material", material);
		model.addAttribute("materialCategoryList",list);
		//return "modules/md/materialForm";
		return "modules/md/materialFormNew";
	}

	@RequiresPermissions("md:material:edit")
	@RequestMapping(value = "save")
	public String save(Material material, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, material))
		{
			return form(material, model);
		}
		materialService.save(material);
		addMessage(redirectAttributes, "保存配件'" + material.getName() + "'成功");
		return "redirect:" + adminPath + "/md/material/list?repage";
	}

	@RequiresPermissions("md:material:edit")
	@ResponseBody
	@RequestMapping(value = "ajaxSave")
	public AjaxJsonEntity ajaxSave(Material material)
	{
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			materialService.save(material);
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}

	@RequiresPermissions("md:material:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, RedirectAttributes redirectAttributes)
	{
		HashMap<String, Object> map = productService.getProductByMaterialId(id);
		if (map != null) {
			addMessage(redirectAttributes, "删除配件失败，该配件已被产品 [" + map.get("name") + "] 使用，不能删除。");
		} else {
			materialService.delete(new Material(id));
			addMessage(redirectAttributes, "删除配件成功");
		}
		return "redirect:" + adminPath + "/md/material/list?repage";
	}

	@RequiresPermissions("md:material:edit")
	@RequestMapping("ajax/delete")
	@ResponseBody
	public AjaxJsonEntity delete(Long id, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try {
			HashMap<String, Object> map = productService.getProductByMaterialId(id);
			if (map != null) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("删除配件失败，该配件已被产品 [" + map.get("name") + "] 使用，不能删除。");
			}else {
				materialService.delete(new Material(id));
				jsonEntity.setMessage("删除成功");
			}
		} catch (Exception e) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Material> list = materialService.findAllList();
		for (int i = 0; i < list.size(); i++)
		{
			Material e = list.get(i);
			Map<String, Object> map = Maps.newHashMap();
			map.put("id", e.getId());
			map.put("name", e.getName());
			mapList.add(map);
		}
		return mapList;
	}

	@ResponseBody
	@RequestMapping(value = "checkMaterialName")
	public String checkMaterialName(Long id, String name, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		Material material = new Material();
		material.setId(id);
		material.setName(name);
		//false ：配件名字已经存在,true:不存在
		return materialService.isExistMaterialName(material) ? "配件名字已经存在" : "true";
//		return materialService.isExistMaterialName(material) ? "false" : "true";
	}

	@RequiresPermissions("md:material:view")
	@RequestMapping(value="requirement")
	public String requirement(HttpServletRequest request, HttpServletResponse response,Model model) {
		List<MDMaterialRequirement> materialRequirementList = materialService.findMaterialRequirementList();
		model.addAttribute("list", materialRequirementList);
		return "modules/md/materialRequirementList";
	}

	@ResponseBody
	@RequestMapping("ajaxSaveRequirement")
	@RequiresPermissions("md:material:edit")
	public AjaxJsonEntity  ajaxSaveRequirement(MaterialRequire materialRequire) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			List<MDMaterialRequirement> materialRequirementList = materialRequire.getRequirementList();
			materialService.saveMaterialRequirementList(materialRequirementList);
		} catch (Exception ex) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(ex.getMessage());
		}
		return ajaxJsonEntity;
	}

	@ResponseBody
	@RequestMapping("getProductParts/{product69Code}")
	public String getProductParts(@PathVariable String product69Code, HttpServletResponse response) throws IOException {
		List<ProductParts> productPartsList = msMaterialService.getProductParts(product69Code);

		List<String> stringList = Lists.newArrayList();
		StringBuilder stringBuilder = new StringBuilder();
		for (ProductParts productParts:productPartsList ) {
			stringBuilder.setLength(0);
			stringBuilder.append("insert into md_customer_material (customer_id, product_id, material_id, is_return, price, customer_part_code, customer_part_name, warranty_day, create_by, create_date, update_by, update_date, del_flag)");
			stringBuilder.append("values('cid',");
			stringBuilder.append("'pid',");
			stringBuilder.append("'mid',");
			stringBuilder.append(productParts.getReturnFactory().equals("是")?"'1'":"'0',");
			stringBuilder.append(productParts.getPriceNew()+",'");
			stringBuilder.append(productParts.getYunmiCode()+"','");
			stringBuilder.append(productParts.getPartsName()+"',");
			stringBuilder.append(productParts.getWarrantyDay()+",");
			stringBuilder.append("1,");
			stringBuilder.append("'2020-10-20 12:00:00',");
			stringBuilder.append("1,");
			stringBuilder.append("'2020-10-20 12:00:00',");
			stringBuilder.append("0);");
			stringBuilder.append("   ");
			stringList.add(stringBuilder.toString());
		}

		response.setContentType("application/octet-stream; charset=utf-8");
		String fileName = "customerMaterial".concat(".sql");
		response.setHeader("Content-Disposition", " attachment; filename=" +  fileName);

		OutputStreamWriter charArrayWriter = new OutputStreamWriter(response.getOutputStream());
		for(String str1: stringList) {
			charArrayWriter.append(str1+"\r\n");
		}
		charArrayWriter.flush();
		charArrayWriter.close();

		return "OK!";
	}
}
