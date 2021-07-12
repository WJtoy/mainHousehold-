package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDProductAttributes;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.viewModel.ProductSortModel;
import com.wolfking.jeesite.modules.md.service.MaterialCategoryService;
import com.wolfking.jeesite.modules.md.service.MaterialService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ProductVerSecondService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/product")
public class ProductController extends BaseController {
	@Autowired
	private ProductService productService;
	@Autowired
	private MaterialService materialService;
	@Autowired
	private MaterialCategoryService materialCategoryService;
	@Autowired
	private MSProductService msProductService;

	@Autowired
	private ProductVerSecondService productVerSecondService;

	@ModelAttribute
	public Product get(@RequestParam(required = false) Long id) {
		if (id != null) {
//			Product product = productService.get(id);
			Product product = productVerSecondService.getSpecColumnByIdForMD(id);
			//TODO 改为延迟加载
			if (product != null){
				product.setMaterialList(materialService.getMaterialListByProductId(id));
			}
			return product;
		} else {
			Product product=new Product();
			product.setSetFlag(0);
			product.setApproveFlag(Product.APPROVE_FLAG_APPROVED);
			return product;
		}
	}

	@RequiresPermissions("md:product:view")
	@RequestMapping(value = { "list", "" })
	public String list(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		Product product = new Product();
		if (paramMap.containsKey("customerId") && paramMap.get("customerId").toString().length() > 0){
			product.setCustomerId(Long.parseLong(paramMap.get("customerId").toString()));
		}
		if (paramMap.containsKey("categoryId") && paramMap.get("categoryId").toString().length() > 0){
			product.setCategory(new ProductCategory(Long.parseLong(paramMap.get("categoryId").toString())));
		}
		if (paramMap.containsKey("name")){
			product.setName(paramMap.get("name").toString());
		}
		if (paramMap.containsKey("brand")){
			product.setBrand(paramMap.get("brand").toString());
		}
		if (paramMap.containsKey("model")){
			product.setModel(paramMap.get("model").toString());
		}

		Page<Product> page = productVerSecondService.findPage(new Page<>(request,
				response), product);
		for(Product item:page.getList()){
			List<Material> materialList = materialService.findMaterialsByProductIdMS(item.getId());
			List<String> materialNames  = materialList.stream().map(Material::getName).collect(Collectors.toList());
			item.setMaterialNames(StringUtils.join(materialNames,","));
			if(item.getProductTypeSpecList()!=null && item.getProductTypeSpecList().size()>0){
				//处理分类规格
				List<MDProductAttributes> list = item.getProductTypeSpecList();
				if(StringUtils.isNotBlank(list.get(0).getProductTypeName()) &&
						StringUtils.isNotBlank(list.get(0).getProductTypeItemName())){
					item.setProductTypeInfo(item.getProductTypeSpecList().get(0).getProductTypeName()+"-"+item.getProductTypeSpecList().get(0).getProductTypeItemName());
				}
				Map<Long, List<MDProductAttributes>> attributeMap = list.stream().filter(r->r.getProductSpecId()!= null).collect(Collectors.groupingBy(MDProductAttributes::getProductSpecId));
				String productSpecInfo = "";
				for(Map.Entry<Long, List<MDProductAttributes>> map:attributeMap.entrySet()){
					String specInfo = "";
					List<MDProductAttributes> attributeList = map.getValue();
					if(StringUtils.isNotBlank(attributeList.get(0).getProductSpecName())){
                       String spec = attributeList.get(0).getProductSpecName();
                       List<String> specItemNameList = attributeList.stream().map(MDProductAttributes::getProductSpecItemName).collect(Collectors.toList());
                       String specItem = StringUtils.join(specItemNameList,",");
                       if(StringUtils.isNotBlank(specItem));{
							specInfo = spec + ":"+specItem;
						}
					}
					if(StringUtils.isNotBlank(specInfo)){
						if(StringUtils.isNotBlank(productSpecInfo)){
							productSpecInfo = productSpecInfo+ "--"+specInfo;
						}else{
							productSpecInfo = specInfo;
						}
					}
				}
				item.setProductSpecInfo(productSpecInfo);
			}
		}
		model.addAttribute("page", page);
		model.addAllAttributes(paramMap);
		return "modules/md/productList";
	}

	@RequiresPermissions("md:product:view")
	@RequestMapping(value = "form")
	public String form(Product product, Model model) {
		model.addAttribute("productList", productVerSecondService.getSingleProductListForMD());
		model.addAttribute("materialList", materialService.findAllList());
		model.addAttribute("product", product);
		model.addAttribute("materialCategoryList",materialCategoryService.findAllList());
		return "modules/md/productForm";
	}

	@RequiresPermissions("md:product:view")
	@RequestMapping(value = "sort")
	public String sort(ProductSortModel productSortModel, Model model) {
		if(productSortModel == null || productSortModel.getProducts()==null || productSortModel.getProducts().size() == 0) {
//			model.addAttribute("productList", productService.findAllList()); //add 2020-11-26
			model.addAttribute("productList", productVerSecondService.findAllList());//add 2020-11-26
		}else{
			model.addAttribute("productList",productSortModel.getProducts());
		}
		return "modules/md/productSortForm";
	}

	/**
	 *
	 */
	@RequestMapping(value = "updateSort",method = RequestMethod.POST)
	public String updateSort(ProductSortModel productSortModel, RedirectAttributes redirectAttributes,Model model) {
		String view =  "redirect:" + adminPath + "/md/product/sort";
		if(productSortModel == null || productSortModel.getProducts()==null || productSortModel.getProducts().size() == 0){
			addMessage(model, "错误：传入参数无值!");
			return sort(productSortModel,model);
		}

		Map<Integer, Long> sortCount = productSortModel.getProducts().stream().collect(Collectors.groupingBy(Product::getSort,Collectors.counting()));
		StringBuilder msg = new StringBuilder(200);
		if(sortCount.size() < productSortModel.getProducts().size()){
			msg.append("错误：排序值重复，请确认:").append("<br>");
			for (Map.Entry<Integer,Long> entry : sortCount.entrySet()) {
				if(entry.getValue().intValue()>1) {
					msg.append(entry.getKey()).append("<br>");
				}
			}
			addMessage(model, msg.toString());
			return sort(productSortModel,model);
		}
		productVerSecondService.updateSortForMD(productSortModel.getProducts());
		addMessage(model, "保存产品排序成功!");
		return sort(null,model);
		//return view;
	}


	@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "save")
	public String save(Product product, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, product)) {
			return form(product, model);
		}
		if (product.getSetFlag() == 1) {
			if (product.getProductIds().length() > 255) {
				addMessage(redirectAttributes, "保存产品'" + product.getName()
						+ "'失败,组合产品太多.");
				return form(product, model);
			}
			//套组忽略配件，配件直接从下级产品获取
			product.setMaterialList(null);
		}else{
			//非套组忽略下级产品
			product.setProductIds("");
		}
		// 设置为待审核状态
		product.setApproveFlag(Product.APPROVE_FLAG_NEW);
		productVerSecondService.saveForMD(product);
		addMessage(redirectAttributes, "保存产品'" + product.getName() + "'成功,请审核产品.");
		return "redirect:" + adminPath + "/md/product?repage";
	}

	@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, Integer sf, RedirectAttributes redirectAttributes) {
		HashMap<String, Object> map = productVerSecondService.getSetProductByProductId(id);
		if (map != null) {
			addMessage(redirectAttributes, "删除产品失败，该产品已被套组 [" + map.get("name") + "] 使用，不能删除。");
		} else {
			Product product = get(id);
			if(product == null){
				addMessage(redirectAttributes, "产品已被删除");
			}else {
				productVerSecondService.deleteForMD(product);
				addMessage(redirectAttributes, "删除产品成功");
			}
		}
		return "redirect:" + adminPath + "/md/product?repage";
	}

	@ResponseBody
	@RequestMapping(value = "checkProductName")
	public String checkProductName(Long id, String name, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		Product product = new Product();
		product.setId(id);
		product.setName(name);
		//false ：产品名字已经存在,true:不存在
		return productVerSecondService.isExistProductName(product) ? "产品名字已经存在" : "true";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(
			HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Product> list = productVerSecondService.findAllList();
		for (int i = 0; i < list.size(); i++) {
			Product e = list.get(i);
			Map<String, Object> map = Maps.newHashMap();
			map.put("id", e.getId());
			map.put("name", e.getName());
			mapList.add(map);
		}
		return mapList;
	}

	@ResponseBody
	@RequestMapping(value = "ajax/customerProductList")
	public AjaxJsonEntity getCustomerProductList(Long customerId)
	{
		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
		jsonEntity.setSuccess(false);
		if (customerId != null && customerId > 1){
			List<Product> list=productVerSecondService.getCustomerProductList(customerId);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(list);
			return jsonEntity;
		}else
		{
			return jsonEntity;
		}
	}

	@ResponseBody
	@RequestMapping(value = "ajax/singleProductList")
	public AjaxJsonEntity findSingleProductListByProductCategoryId(Long productCategoryId) {
		// 根据产品类别id获取产品列表  //add on 2019-12-12
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		jsonEntity.setSuccess(false);
		if (productCategoryId != null && productCategoryId > 0){
			List<Product> list = msProductService.findSingleListByProductCategoryId(productCategoryId);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(list);
			return jsonEntity;
		}else
		{
			return jsonEntity;
		}
	}


	@RequiresPermissions("md:product:view")
	@RequestMapping(value = "formNew")
	public String formNew(Product product, Model model) {
	    if(product !=null && product.getProductTypeSpecList()!=null && product.getProductTypeSpecList().size()>0){
	    	MDProductAttributes productAttributes = product.getProductTypeSpecList().get(0);
			product.setProductTypeId(productAttributes.getProductTypeId());
			product.setProductTypeItemId(productAttributes.getProductTypeItemId());
			product.setProductTypeInfo(productAttributes.getProductTypeName());
			product.setProductSpecInfo(productAttributes.getProductTypeItemName());
			List<Long> specItemList = product.getProductTypeSpecList().stream().map(MDProductAttributes::getProductSpecItemId).collect(Collectors.toList());
			product.setSpecItemIds(StringUtils.join(specItemList,","));
		}
		model.addAttribute("product", product);
		model.addAttribute("productList", productVerSecondService.getSingleProductListForMD());
		model.addAttribute("materialList", materialService.findAllList());
		model.addAttribute("materialCategoryList",materialCategoryService.findAllList());
		return "modules/md/productFormNew";
	}


	@RequiresPermissions("md:product:edit")
	@ResponseBody
	@RequestMapping(value = "saveNew")
	public AjaxJsonEntity saveNew(Product product) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
			if (product.getSetFlag() == 1) {
				if (product.getProductIds().length() > 255) {
					ajaxJsonEntity.setSuccess(false);
					ajaxJsonEntity.setMessage("保存产品'" + product.getName() + "'失败,组合产品太多.");
				}
				//套组忽略配件，配件直接从下级产品获取
				product.setMaterialList(null);
			}else{
				//非套组忽略下级产品
				product.setProductIds("");
			}
			// 设置为待审核状态
			product.setApproveFlag(Product.APPROVE_FLAG_NEW);
			List<MDProductAttributes> productAttributes = Lists.newArrayList();
			if(product.getProductTypeId()!=null && product.getProductTypeId()>0 &&
					product.getProductTypeItemId() !=null && product.getProductTypeItemId()>0 ) {
					// && StringUtils.isNotBlank(product.getSpecItemIds())){  // mark on 2020-3-27
				String[] specItemIdArray = StringUtils.isNotBlank(product.getSpecItemIds())?product.getSpecItemIds().split(","):null; //add on 2020-3-27
				if(specItemIdArray!=null && specItemIdArray.length>0){
					//保存产品规格参数
					MDProductAttributes productAttributes1;
					for(int i=0;i<specItemIdArray.length;i++){
						productAttributes1 = new MDProductAttributes();
						productAttributes1.setProductTypeId(product.getProductTypeId());
						productAttributes1.setProductTypeItemId(product.getProductTypeItemId());
						productAttributes1.setProductSpecItemId(Long.valueOf(specItemIdArray[i]));
						productAttributes.add(productAttributes1);
					}
				}  else {
					// add on 2020-3-26 begin
					MDProductAttributes productAttributes1 = new MDProductAttributes();
					productAttributes1.setProductTypeId(product.getProductTypeId());
					productAttributes1.setProductTypeItemId(product.getProductTypeItemId());
					productAttributes.add(productAttributes1);
					// add on 2020-3-26 end
				}
			}
			product.setProductTypeSpecList(productAttributes);
			productVerSecondService.saveForMD(product);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}

}
