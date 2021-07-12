package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ProductPriceService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSProductPriceService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/productprice")
public class ProductPriceController extends BaseController
{
	@Autowired
	private ProductService productService;

	@Autowired
	private ServiceTypeService serviceTypeService;

	@Autowired
	private ProductPriceService productPriceService;

	@Autowired
	private MSProductPriceService msProductPriceService;

	@RequiresPermissions("md:product:view")
	@RequestMapping(value = { "list", "" })
	public String list(Long productCategoryId, ProductPrice productPrice, @RequestParam int type, HttpServletRequest request, HttpServletResponse response, Model model) {
		//产品更改为下拉方式方法
		//List<Product> products = productService.findAllList();
		//model.addAttribute("products", products);

		//List<ServiceType> serviceTypes = serviceTypeService.findAllList();

		//调用微服务 服务类型对象只有id和名称有值 start 2019-10-11
		List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
		// end

		model.addAttribute("serviceTypes", serviceTypes);
		//按产品分组读取不同服务类型的产品价格
		Product pd = new Product();
		if (productPrice.getProduct() != null && productPrice.getProduct().getId() != null){
			pd.setId(productPrice.getProduct().getId());
		}
		if (productCategoryId != null) {
			pd.setCategory(new ProductCategory(productCategoryId));
			model.addAttribute("productCategoryId", productCategoryId);
		}
		Page<Product> productPage = productService.findPageForPrice(new Page<>(request, response), pd);
		List<Long> productIds = Lists.newArrayList();
		List<Product> productList = productPage.getList();
		for (Product product : productList){
			productIds.add(product.getId());
		}
		//List<ProductPrice> productPriceList = productPriceService.findAllGroupList(productIds, null, type, null, null);
		// update on 2020-06-09
		List<ProductPrice> productPriceList = productPriceService.findAllPriceList(productIds, null, type, null, null);

		//生成分组显示数据
		List<HashMap<String, Object>> groupedProductPriceList = Lists.newArrayList();
		for (Product product : productList){
			HashMap<String, Object> productPriceMap = new HashMap<>();
			productPriceMap.put("productId", product.getId());
			productPriceMap.put("productName", product.getName());
			List<HashMap<String, Object>> serviceTypePriceList = Lists.newArrayList();
			for (ServiceType serviceType : serviceTypes){
				HashMap<String, Object> serviceTypePriceMap = new HashMap<>();
				boolean isInserted = false;
				for (int index = productPriceList.size()-1; index >= 0; index--){
					ProductPrice pp = productPriceList.get(index);
					if (pp.getProduct().getId().equals(product.getId()) &&
						pp.getServiceType().getId().equals(serviceType.getId())){
						serviceTypePriceMap.put("productPriceId", pp.getId());
						serviceTypePriceMap.put("engineerStandardPrice", pp.getEngineerStandardPrice());
						serviceTypePriceMap.put("engineerDiscountPrice", pp.getEngineerDiscountPrice());
						serviceTypePriceMap.put("customerStandardPrice", pp.getCustomerStandardPrice());
						serviceTypePriceMap.put("customerDiscountPrice", pp.getCustomerDiscountPrice());
						serviceTypePriceMap.put("delFlag", pp.getDelFlag());
						serviceTypePriceList.add(serviceTypePriceMap);
						productPriceList.remove(index);
						isInserted = true;
						break;
					}
				}
				if (!isInserted){
					serviceTypePriceMap.put("engineerStandardPrice", 0D);
					serviceTypePriceMap.put("engineerDiscountPrice", 0D);
					serviceTypePriceMap.put("customerStandardPrice", 0D);
					serviceTypePriceMap.put("customerDiscountPrice", 0D);
					serviceTypePriceMap.put("delFlag", 0);
					serviceTypePriceList.add(serviceTypePriceMap);
				}
				serviceTypePriceMap.put("serviceTypeId", serviceType.getId());
				serviceTypePriceMap.put("serviceTypeName", serviceType.getName());
			}
			productPriceMap.put("itemlist", serviceTypePriceList);
			groupedProductPriceList.add(productPriceMap);
		}

		Page<HashMap<String, Object>> page = new Page<>(productPage.getPageNo(), productPage.getPageSize(), productPage.getCount());
		page.setList(groupedProductPriceList);
		model.addAttribute("page", page);
		model.addAttribute("priceType", type);

//		return "modules/md/productPriceList";
		// update on 2020-06-09
		return "modules/md/productPriceListNew";
	}

	@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "form")
	public String form(Long qProductId, Long qServiceTypeId, String qPriceType, ProductPrice productPrice, Model model)
	{
		if (productPrice.getId() != null){
			productPrice = productPriceService.get(productPrice.getId());
		}else {
			if (qProductId != null && productPrice.getProduct() == null) {
				productPrice.setProduct(productService.get(qProductId));
			}
			if (qServiceTypeId != null && productPrice.getServiceType() == null) {
				productPrice.setServiceType(serviceTypeService.get(qServiceTypeId));
			}
			if (qPriceType != null) {
				productPrice.setPriceType(new Dict(qPriceType));
			}
		}

		model.addAttribute("productPrice", productPrice);
//		return "modules/md/productPriceForm";
		// update on 2020-06-09
		return "modules/md/productPriceFormNew";
	}

	/*@RequiresPermissions("md:customerprice:edit")
	@RequestMapping(value = "forms")
	public String forms(ProductPrices productPrices, Model model)
	{
		List<ServiceType> serviceTypes = serviceTypeService.findAllList();
		List<ProductPrice> tempCustomerPrices = Lists.newArrayList();
		for (ServiceType serviceType : serviceTypes)
		{
			ProductPrice productprice = new ProductPrice();
			productprice.setServiceType(serviceType);
			productprice.setCustomerStandardPrice(serviceType.getPrice());
			productprice.setCustomerDiscountPrice(serviceType.getDiscountPrice());
			productprice.setEngineerStandardPrice(serviceType.getEngineerPrice());
			productprice.setEngineerDiscountPrice(serviceType.getEngineerDiscountPrice());
			tempCustomerPrices.add(productprice);
		}

		productPrices.setListProductPrice(tempCustomerPrices);

		model.addAttribute("productPrices", productPrices);
		return "modules/md/productPriceForms";
	}*/

    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "formsNew")
    public String formsNew(ProductPrices productPrices, Model model) {
    	final String remotePriceTypeValue = "40";
        List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        List<ProductPrice> tempCustomerPrices = Lists.newArrayList();
        for (ServiceType serviceType : serviceTypes) {
            ProductPrice productprice = new ProductPrice();
            productprice.setServiceType(serviceType);
            productprice.setCustomerStandardPrice(serviceType.getPrice());
            productprice.setCustomerDiscountPrice(serviceType.getDiscountPrice());
            productprice.setEngineerStandardPrice(serviceType.getEngineerPrice());
            productprice.setEngineerDiscountPrice(serviceType.getEngineerDiscountPrice());
            tempCustomerPrices.add(productprice);
        }
        productPrices.setListProductPrice(tempCustomerPrices);
        if (!remotePriceTypeValue.equalsIgnoreCase(productPrices.getPriceType().getValue())) {
			productPrices.setPriceType(new Dict());
		}
        model.addAttribute("productPrices", productPrices);
        return "modules/md/productPriceFormsNew";
    }

	/*@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "save")
	// @Valid
	public String save(ProductPrice productPrice, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, productPrice))
		{
			return form(productPrice.getProduct().getId(),productPrice.getServiceType().getId(),productPrice.getPriceType().getValue(),productPrice, model);
		}
//		try
//		{
		productPriceService.save(productPrice);
//		} catch (Exception e){
//			addMessage(model, "保存产品'"
//					+ productPrice.getProduct().getName() + "'的默认价失败"+e.getMessage());
//			return form(productPrice.getProduct().getId(),productPrice.getServiceType().getId(),productPrice, model);
//		}
		addMessage(redirectAttributes, "保存产品'"
				+ productPrice.getProduct().getName() + "'的参考成功");
		return "redirect:" + adminPath + "/md/productprice/list?repage&type="+productPrice.getPriceType().getValue();
	}*/

    @RequiresPermissions("md:product:edit")
    @RequestMapping(value = "saveNew")
    @ResponseBody
    public AjaxJsonEntity saveNew(ProductPrice productPrice, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		String msg = getPriceTypeNameByValue(Optional.ofNullable(productPrice.getPriceType()).map(Dict::getValue).orElse(""));
    	AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		try {
            productPriceService.save(productPrice);
            ajaxJsonEntity.setMessage("保存产品'" + productPrice.getProduct().getName() + "'的"+msg+"成功");
		} catch (Exception e){
		    ajaxJsonEntity.setSuccess(false);
		    ajaxJsonEntity.setMessage("保存产品'" + productPrice.getProduct().getName() + "'的默认价失败,原因：" + e.getMessage());
		}
		return ajaxJsonEntity;
    }

	/*@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "delete")
	public String delete(Long id, Long priceType, RedirectAttributes redirectAttributes)
	{
		ProductPrice productPrice = new ProductPrice(id);
		productPrice.setDelFlag(1);
		productPriceService.delete(productPrice);
		addMessage(redirectAttributes, "停用参考成功");
		return "redirect:" + adminPath + "/md/productprice?repage&type="+priceType;
	}*/

	@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "deleteNew")
	@ResponseBody
	public AjaxJsonEntity deleteNew(Long id, Long priceType, RedirectAttributes redirectAttributes) {
		String strPriceTypeValue = Optional.ofNullable(priceType).map(Object::toString).orElse("");
		String msg = getPriceTypeNameByValue(strPriceTypeValue);
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		ProductPrice productPrice = new ProductPrice(id);
		productPrice.setDelFlag(1);
		try {
			productPriceService.delete(productPrice);
			ajaxJsonEntity.setMessage("停用"+msg+"成功");
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("停用"+msg+"失败，失败原因：" + e.getMessage());
		}
		return ajaxJsonEntity;
	}

	/*@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "active")
	public String active(Long id, Long priceType, RedirectAttributes redirectAttributes)
	{
		ProductPrice productPrice = new ProductPrice(id);
		productPrice.setDelFlag(0);
		productPriceService.delete(productPrice);
		addMessage(redirectAttributes, "停用参考成功");
		return "redirect:" + adminPath + "/md/productprice?repage&type="+priceType;
	}*/

	@RequiresPermissions("md:product:edit")
	@RequestMapping(value = "activeNew")
	@ResponseBody
	public AjaxJsonEntity activeNew(Long id, Long priceType, RedirectAttributes redirectAttributes) {
		String strPriceTypeValue = Optional.ofNullable(priceType).map(Object::toString).orElse("");
		String msg = getPriceTypeNameByValue(strPriceTypeValue);
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		ProductPrice productPrice = new ProductPrice(id);
		productPrice.setDelFlag(0);
		try {
			productPriceService.delete(productPrice);
			ajaxJsonEntity.setMessage("启用"+msg+"成功");
		} catch (Exception e) {
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("启用"+msg+"失败，失败原因：" + e.getMessage());
		}
		return ajaxJsonEntity;
	}

	/*@RequiresPermissions("md:customerprice:edit")
	@RequestMapping(value = "saveproductprices")
	public String saveProductPrices(ProductPrices productPrices, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
	{
		if (!beanValidator(model, productPrices))
		{
			return forms(productPrices, model);
		}
		try{
			Product product=productService.get(productPrices.getProduct().getId());
			productPrices.setProduct(product);
			productPriceService.saveProductPrices(productPrices);
			addMessage(redirectAttributes, "保存'" + "产品("
					+ productPrices.getProduct().getName() + "）'参考价格成功");
//			return "redirect:" + adminPath + "/md/productprice/forms?repage";
		} catch (Exception e){
			addMessage(model, "保存'" + productPrices.getProduct().getName()
					+ "'参考价格失败:" + e.getMessage());
//			return "modules/md/customerPriceForms";
		}
		return "redirect:" + adminPath + "/md/productprice/forms?repage&type="+productPrices.getPriceType().getValue();
	}*/

    @RequiresPermissions("md:customerprice:edit")
    @RequestMapping(value = "saveproductpricesNew")
    @ResponseBody
    public AjaxJsonEntity saveProductPricesNew(ProductPrices productPrices, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		String msg = getPriceTypeNameByValue(Optional.ofNullable(productPrices.getPriceType()).map(Dict::getValue).orElse(""));
    	AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
	    try{
            Product product=productService.get(productPrices.getProduct().getId());
            productPrices.setProduct(product);
            productPriceService.saveProductPrices(productPrices);
            ajaxJsonEntity.setMessage("保存'" + "产品(" + productPrices.getProduct().getName() + "）'"+msg+"成功");
        } catch (Exception e){
	        ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存'" + productPrices.getProduct().getName()+ "'"+msg+"失败:" + e.getMessage());
        }
        return ajaxJsonEntity;
    }


	@ResponseBody
	@RequestMapping(value={"ajax/getPrice"})
	public AjaxJsonEntity getPriceByProductIdAndServiceTypeIdAndPriceType(Long productId, Long serviceTypeId, Integer priceType, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
		jsonEntity.setSuccess(true);
		try{
			Double price = msProductPriceService.getPriceByProductIdAndServiceTypeIdAndPriceType(priceType, productId,serviceTypeId);
			jsonEntity.setSuccess(true);
			jsonEntity.setData(price);
		}catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	private  String getPriceTypeNameByValue(String priceTypeValue) {
		final String remotePriceType = "40";  // 偏远价格
		String msg = "服务价格";
		String strPriceType = Optional.ofNullable(priceTypeValue).map(Object::toString).orElse("");
		if (remotePriceType.equals(strPriceType)) {
			msg = MSDictUtils.getDictLabel(strPriceType, "PriceType", "");
		}
		return msg;
	}
}
