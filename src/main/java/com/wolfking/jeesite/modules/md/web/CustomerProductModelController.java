package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerProductModel;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.CustomerProductModelService;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.ProductModelService;
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
 * 客户产品型号
 */
@Controller
@RequestMapping(value = "${adminPath}/md/customerproductmodel")
public class CustomerProductModelController extends BaseController
{
	@Autowired
	private CustomerProductModelService customerProductModelService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ProductModelService productModelService;

	@Autowired
	private ProductService productService;

	@ModelAttribute
	public CustomerProductModel get(@RequestParam(required = false) Long id)
	{
		if (id != null)
		{
			return customerProductModelService.get(id);
		} else
		{
			return new CustomerProductModel();
		}
	}


	/**
	 * 列表显示
	 * @param customerProductModel
	 * @param request
	 * @param response
	 * @param  model
	 * @returno
	 */
	@RequiresPermissions("md:customerproductmodel:view")
	@RequestMapping(value = {"list", ""})
	public String list(CustomerProductModel customerProductModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<CustomerProductModel> page = customerProductModelService.findPage(new Page<>(request, response), customerProductModel);
		List<CustomerProductModel> list=page.getList();
		for (CustomerProductModel entity:list) {
			Long customerId=entity.getCustomer().getId();
			entity.setCustomer(customerService.getFromCache(customerId));

			Long productId=entity.getProduct().getId();
			entity.setProduct(productService.getProductByIdFromCache(productId));
		}
		model.addAttribute("page", page);
		return "modules/md/customerProductModelList";
	}

	@RequiresPermissions("md:customerproductmodel:edit")
	@RequestMapping(value = "form")
	public String form(CustomerProductModel customerProductModel, Model model) {
		String viewModel = "modules/md/customerProductModelForm";

		List<Customer> customerList =CustomerUtils.getMyCustomerList();
		List<Product> customerProductList =productService.findAllList();

		if(customerProductModel.getId() != null && customerProductModel.getId()>0) {
			model.addAttribute("customerList", customerList);
			model.addAttribute("customerProducModel", customerProductModel);
			model.addAttribute("canAction", true);
			return viewModel;
		}
		model.addAttribute("modelAddIndex", "2");
		model.addAttribute("customerList", customerList);
		model.addAttribute("customerProductList", customerProductList);
		model.addAttribute("customerProducModel", customerProductModel);
		model.addAttribute("canAction", true);
		return viewModel;
	}


	/**
	 * 保存数据
	 * @param customerProductModel
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @returno
	 */
	@RequiresPermissions("md:customerproductmodel:edit")
	@RequestMapping(value = "save")
	public String saveMuti(CustomerProductModel customerProductModel, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, customerProductModel))
		{
			return form(customerProductModel, model);
		}
		Long result = customerProductModelService.checkIsExist(customerProductModel);
		if(customerProductModel.getId()==null && result !=null){
			addMessage(redirectAttributes, "该型号已经存在");
		}else if(customerProductModel.getId() !=null && result!=null && !(customerProductModel.getId().equals(result))){
			addMessage(redirectAttributes, "该型号已经存在");
		}else{
			customerProductModelService.save(customerProductModel);
			addMessage(redirectAttributes, "保存成功");
		}
		return "redirect:"+ adminPath+"/md/customerproductmodel?repage";
	}

	/**
	 * 删除数据
	 * @param customerProductModel
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("md:customerproductmodel:edit")
	@RequestMapping(value = "delete")
	public String delete(CustomerProductModel customerProductModel, RedirectAttributes redirectAttributes) {
		customerProductModelService.delete(customerProductModel);
		addMessage(redirectAttributes, "删除成功");
		return "redirect:"+adminPath+"/md/customerproductmodel?repage";
	}


	/**
	 *根据客户获取产品列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "ajax/customerProductList")
	public AjaxJsonEntity getCustomerProductList(Long customerId)
	{
		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
		jsonEntity.setSuccess(false);
		if (customerId != null && customerId > 0){
			List<Product> list=productService.getCustomerProductList(customerId);
			if(list!=null && list.size()>0){
				jsonEntity.setSuccess(true);
				jsonEntity.setData(list);
				return jsonEntity;
			}else{
				return jsonEntity;
			}
		}else{
			return jsonEntity;
		}

	}

	/**
	 *根据客户列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "ajax/getMyCustomerList")
	public AjaxJsonEntity getMyCustomerList(){
		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
		jsonEntity.setSuccess(false);
		List<Customer> customerList =CustomerUtils.getMyCustomerList();
		if(customerList!=null && customerList.size()>0){
			jsonEntity.setSuccess(true);
			jsonEntity.setData(customerList);
		}
		return jsonEntity;
	}

	@ResponseBody
	@RequestMapping(value="ajax/findListByCustomerAndProduct")
	public AjaxJsonEntity ajaxFindListByCustomerAndProduct(Long customerId, Long productId) {
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		jsonEntity.setSuccess(false);

		User user = UserUtils.getUser();
		if(user == null || user.getId()<=0){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("用户不存在,请重新登录");
			return jsonEntity;
		}

		if (customerId == null) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("请选择客户");
			return jsonEntity;
		}

		if (productId == null) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("请选择产品");
			return jsonEntity;
		}

		List<com.kkl.kklplus.entity.md.CustomerProductModel> customerProductModelList = productModelService.findListByCustomerAndProduct(customerId, productId);
		if(customerProductModelList != null && customerProductModelList.size()>0){
			jsonEntity.setSuccess(true);
			jsonEntity.setData(customerProductModelList);
		}
		return jsonEntity;
	}

}
