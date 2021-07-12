package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.BrandCategory;
import com.wolfking.jeesite.modules.md.entity.BrandsCaterotyModel;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.BrandCategoryService;
import com.wolfking.jeesite.modules.md.service.BrandService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/brandCategory")
public class BrandCategoryController extends BaseController
{
	@Autowired
	private BrandCategoryService brandCategoryService;

	@Autowired
	private BrandService brandService;

	@Autowired
	private ProductCategoryService productCategoryService;

	@ModelAttribute
	public BrandsCaterotyModel get(@RequestParam(required = false) Long categoryId)
	{
		if (categoryId != null) {
			//ProductCategory category = productCategoryService.get(categoryId);  //mark on 2020-3-16
			ProductCategory category = productCategoryService.getFromCache(categoryId);   //add on 2020-4-1
			List<Brand> brandIds = brandCategoryService.getBrandListByCategory(categoryId);

			String brandIdsstr = brandIds.stream().map(i->i.getId().toString()).collect(Collectors.joining(","));

			BrandsCaterotyModel model=new BrandsCaterotyModel();
			model.setCategory(category);
			model.setBrandIds(brandIdsstr);
			return model;
		} else {
			return new BrandsCaterotyModel();
		}
	}

	@RequiresPermissions("md:brandcategory:view")
	@RequestMapping(value = { "list", "" })
	public String list(BrandCategory brandCategory, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<BrandCategory> page = brandCategoryService.findPage(new Page<>(request, response), brandCategory);

		List<BrandCategory> list=page.getList();
		/*
		// mark on 2019-9-5
		for (BrandCategory entity:list) {
			entity.setBrand(brandService.getFromCache(entity.getBrand().getId()));
			entity.setCategory(productCategoryService.getFromCache(entity.getCategory().getId()));

		}
		*/
		List<ProductCategory> categoryList=productCategoryService.findAllList();
		List<Brand> brandList=brandService.findAllList();

		// add on 2019-9-5 begin
		Map<Long, Brand> brandMap = Maps.newHashMap();
		if (brandList != null && !brandList.isEmpty()) {
			brandMap = brandList.stream().collect(Collectors.toMap(Brand::getId, Function.identity()));
		}

		Map<Long, ProductCategory> productCategoryMap = Maps.newHashMap();
		if (categoryList != null && !categoryList.isEmpty()) {
			productCategoryMap = categoryList.stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
		}

		for (BrandCategory entity:list) {
			entity.setBrand(brandMap.get(entity.getBrand().getId()));
			entity.setCategory(productCategoryMap.get(entity.getCategory().getId()));
		}
		// add on 2019-9-5 end

		model.addAttribute("brandList",brandList);
		model.addAttribute("categoryList",categoryList);
		model.addAttribute("page", page);
		return "modules/md/brandCategoryList";
	}

	@RequiresPermissions("md:brandcategory:view")
	@RequestMapping(value = "form")
	public String form(BrandsCaterotyModel brandsCaterotyModel, Model model)
	{
		//model.addAttribute("categoryList", productCategoryService.findAllList());  //发现界面没有用到此属性 //mark on 2020-3-18
		model.addAttribute("brandList", brandService.findAllList());
		model.addAttribute("brandsCaterotyModel", brandsCaterotyModel);
		return "modules/md/brandCategoryForm";
	}

	@RequiresPermissions("md:brandcategory:edit")
	@RequestMapping(value = "save")
	public String save(BrandsCaterotyModel brandsCaterotyModel, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		brandCategoryService.saveBrandsCaterotyModel(brandsCaterotyModel);
		addMessage(redirectAttributes, "保存成功");
		return "redirect:" + adminPath + "/md/brandCategory/list?repage";
	}

	@RequiresPermissions("md:brandcategory:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, RedirectAttributes redirectAttributes)
	{
		brandCategoryService.delete(new BrandCategory(id));
		addMessage(redirectAttributes, "删除成功");
		return "redirect:" + adminPath + "/md/brandCategory/list?repage";
	}

	/**
	 * 通过产品分类ID获取品牌列表
	 * @param productCategoryId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "ajax/getBrandListByCategory")
	public AjaxJsonEntity getBrandListByCustomer(Long productCategoryId)
	{
		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
		jsonEntity.setSuccess(false);
		if (productCategoryId != null && productCategoryId > 0){
			List<Brand> brandList=brandCategoryService.getBrandListByCategory(productCategoryId);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(brandList);
			return jsonEntity;
		}else
		{
			return jsonEntity;
		}
	}
}
