package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/md/productapprove")
public class ProductApproveController extends BaseController {
	@Autowired
	private ProductService productService;

	@ModelAttribute
	public Product get(@RequestParam(required = false) Long id) {
		if (id != null) {
			return productService.get(id);
		} else {
			return new Product();
		}
	}

	@RequiresPermissions("md:productapprove:view")
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
		product.setApproveFlag(Product.APPROVE_FLAG_NEW);
		Page<Product> page = productService.findPage(new Page<>(request, response), product);
		model.addAttribute("page", page);
		model.addAllAttributes(paramMap);
		return "modules/md/productApproveList";
	}

	@RequiresPermissions("md:productapprove:approve")
	@ResponseBody
	@RequestMapping(value = { "approve" })
	public AjaxJsonEntity approve(@RequestParam Long id, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try {
			/*
			productService.approveProduct(id);
			productService.delProductCache();
			jsonEntity.setSuccess(true);
			*/
			Product product = productService.get(id);
			if(product == null){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("系统中无该产品，请确认产品是否已删除。");
			}else{
				productService.approveProduct(id);
				//productService.delProductCache();
				jsonEntity.setSuccess(true);
			}
		} catch (Exception e) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

}
