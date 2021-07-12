package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/customerMaterial")
public class CustomerMaterialController extends BaseController
{

	@Autowired
	private CustomerMaterialService customerMaterialService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ProductService productService;

	@Autowired
	private MaterialService materialService;

	/**
	 * 分页查询
	 *
	 * @param customerMaterial
	 * @return
	 */
	@RequiresPermissions("md:customermaterial:view")
	@RequestMapping(value = { "list", "" })
	public String list(CustomerMaterial customerMaterial, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<CustomerMaterial> page = new Page<>();
		User user = UserUtils.getUser();
		Boolean errorFlag = false;
		if(user.isCustomer()){
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				errorFlag = true;
			}
		}else if(user.isSaleman()){
		    if(customerMaterial.getCustomer() == null ||
                    customerMaterial.getCustomer().getId()==null || customerMaterial.getCustomer().getId()<=0){
                model.addAttribute("page", page);
                model.addAttribute("customerMaterial", customerMaterial);
                return "modules/md/customerMaterialList";
            }
		}
		if(errorFlag){
			if(errorFlag){
				model.addAttribute("page", page);
				model.addAttribute("customerMaterial", customerMaterial);
				return "modules/md/customerMaterialList";
			}
		}
		page = customerMaterialService.findPage(new Page<>(request, response), customerMaterial);
        List<Material> materialList = materialService.findAllList();
		if(page.getList() !=null && page.getList().size()>0){
            Map<Long,Material> materialMap = materialList.stream().collect(Collectors.toMap(Material::getId,material -> material));
           for(CustomerMaterial item:page.getList()){
               Customer customer = customerService.getFromCache(item.getCustomer().getId());
               if(customer !=null){
				   item.getCustomer().setName(customer.getName());
			   }
			   Product product = productService.getProductByIdFromCache(item.getProduct().getId());
               if(product!=null){
				   item.getProduct().setName(product.getName());
			   }
			   Material material = materialMap.get(item.getMaterial().getId());
               if(material !=null){
				   item.getMaterial().setName(material.getName());
			   }
		   }
		}
		model.addAttribute("materialList",materialList);
		model.addAttribute("page", page);
		model.addAttribute("customerMaterial", customerMaterial);
		return "modules/md/customerMaterialList";
	}

	@RequiresPermissions("md:customermaterial:view")
	@RequestMapping("form")
	public String form(CustomerMaterial customerMaterial,Model model){
		User user = UserUtils.getUser();
		if(user.isCustomer()){
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				customerMaterial.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("customerMaterial", customerMaterial);
				return "modules/md/customerMaterialForm";
			}
		}
       if(customerMaterial.getId()!=null && customerMaterial.getId()>0){
		   customerMaterial = customerMaterialService.get(customerMaterial.getId());
		   if(customerMaterial !=null){
			   Customer customer = customerService.getFromCache(customerMaterial.getCustomer().getId());
			   if(customer !=null){
				   customerMaterial.getCustomer().setName(customer.getName());
			   }
			   Product product = productService.getProductByIdFromCache(customerMaterial.getProduct().getId());
			   if(product!=null){
				   customerMaterial.getProduct().setName(product.getName());
			   }
			   Material material = materialService.getFromCache(customerMaterial.getMaterial().getId());
			   if(material !=null){
				   customerMaterial.getMaterial().setName(material.getName());
			   }
		   }
	   }
	   model.addAttribute("customerMaterial",customerMaterial);
		model.addAttribute("canAction",true);
       return "modules/md/customerMaterialForm";
	}


	/**
	 * 添加或者修改
	 *
	 * @param customerMaterial
	 * @return
	 */
	@RequiresPermissions("md:customermaterial:edit")
	@RequestMapping("save")
	@ResponseBody
	public AjaxJsonEntity save(CustomerMaterial customerMaterial, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try {
			customerMaterialService.save(customerMaterial);
			jsonEntity.setSuccess(true);
		}catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
        return jsonEntity;
	}

	/**
	 * 删除
	 *
	 * @param customerMaterial
	 * @return
	 */
	@RequiresPermissions("md:customermaterial:edit")
	@RequestMapping(value = "delete")
	public String delete(CustomerMaterial customerMaterial, RedirectAttributes redirectAttributes)
	{
		customerMaterialService.delete(customerMaterial);
		addMessage(redirectAttributes, "删除品牌成功");
		return "redirect:" + adminPath + "/md/customerMaterial/list?repage";
	}



	/**
	 * 根据产品Id查询配件列表
	 *
	 * @param productId
	 * @return
	 */
	@RequestMapping("ajax/getMaterialListByProductId")
	@ResponseBody
	public AjaxJsonEntity getMaterialListByProductId(Long productId,Long customerId,HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity  = new AjaxJsonEntity(true);
		try {
			 Map<String,Object> map = Maps.newHashMap();
             List<Material> list = materialService.getMaterialListByProductId(productId);
             if(list !=null && list.size()>0){
				 jsonEntity.setSuccess(true);
			 }else{
				 list = Lists.newArrayList();
			 }
			map.put("materialList",list);
            List<CustomerMaterial> customerMaterialList = customerMaterialService.getByCustomerAndProduct(customerId,productId);
			map.put("customerMaterialList",customerMaterialList);
			jsonEntity.setData(map);
		}catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 根据配件Id获取配件信息
	 *
	 * @param materialId
	 * @return
	 */
	@RequestMapping("ajax/getMaterial")
	@ResponseBody
	public AjaxJsonEntity getMaterial(Long materialId,HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity  = new AjaxJsonEntity(true);
		try {
			Material material = materialService.getFromCache(materialId);
			if(material==null){
				material = new Material();
			}
			jsonEntity.setSuccess(true);
			jsonEntity.setData(material);
		}catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}
}
