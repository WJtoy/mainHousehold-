package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerTimeliness;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaTimelinessModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.TimelinessChargeModel;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.CustomerTimelinessService;
import com.wolfking.jeesite.modules.md.service.TimelinessLevelService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerTimelinessService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/customertimeliness")
public class CustomerTimelinessController extends BaseController
{
	@Autowired
	private CustomerTimelinessService customerTimelinessService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private TimelinessLevelService timelinessLevelService;

	@Autowired
	private MSCustomerTimelinessService msCustomerTimelinessService;

	@Autowired
	private MSCustomerService msCustomerService;

	@RequiresPermissions("md:customertimeliness:view")
	@RequestMapping(value = {"list", ""})
	public String list(CustomerTimeliness customerTimeliness, HttpServletRequest request, HttpServletResponse response, Model model) {
//		Page<CustomerTimeliness> page = customerTimelinessService.findPage(new Page<>(request, response), customerTimeliness);   //mark on 2019-8-1
		if (customerTimeliness.getCustomer() == null || customerTimeliness.getCustomer().getId() == null) {
			addMessage(model, "请选择客户!");
			return "modules/md/customerTimelinessNewList";
		}
		Page<CustomerTimeliness> page = msCustomerTimelinessService.findList(new Page<>(request, response), customerTimeliness); //add on 2019-8-1 微服务调用

		List<HashMap<String,Object>> listmap=new ArrayList<>();
		if (page.getList()!=null && page.getList().size()>0){
			List<CustomerTimeliness> list=page.getList();
			Set<Long> customerIds = list.stream().map(r->r.getCustomer().getId()).collect(Collectors.toSet());
			Map<Long, Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(Lists.newArrayList(customerIds));
			HashMap<String,Object> map=new HashMap<>();
			for (CustomerTimeliness entity:list) {
				Long customerId=entity.getCustomer().getId();
				// Customer customer = customerService.getFromCache(customerId);  // mark on 2019-7-31
				Customer customer = customerMap.get(customerId);            // 改成调用微服务  //2019-7-31
				entity.setCustomer(customer);

				map=new HashMap<>();
				map.put("customerId", entity.getCustomer() == null ? customerId:entity.getCustomer().getId());  // update on 2019-7-31
				map.put("customerName", entity.getCustomer() == null? "":entity.getCustomer().getName());       // update on 2019-7-31

				List<AreaTimelinessModel> list1=customerTimelinessService.findListByCustomerId(customerId);
				map.put("areaTimelinessModelList",list1);
				listmap.add(map);
			}

			//按照客户名称排序
			listmap.sort((HashMap map1,HashMap map2)->map1.get("customerName").toString().compareTo(map2.get("customerName").toString()));
		}
		List<TimelinessLevel> timelineList = timelinessLevelService.findAllList();
		model.addAttribute("page", page);
		model.addAttribute("customerTimeliness", customerTimeliness);
		model.addAttribute("timelineList", timelineList);
		model.addAttribute("listmap", listmap);
		return "modules/md/customerTimelinessNewList";
	}

	/**
	 * 专门开给客户看的时效费用
	 * @param customerTimeliness
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "customerList")
	public String customerlist(CustomerTimeliness customerTimeliness, HttpServletRequest request, HttpServletResponse response, Model model) {

		//是客户账号则设置查询条件
		User user=UserUtils.getUser();
		if (user.isCustomer()){
			customerTimeliness.setCustomer(user.getCustomerAccountProfile().getCustomer());
		}
		//Page<CustomerTimeliness> page = customerTimelinessService.findPage(new Page<>(request, response), customerTimeliness);  //mark on 2019-8-1
		Page<CustomerTimeliness> page = msCustomerTimelinessService.findList(new Page<>(request, response), customerTimeliness); //add on 2019-8-1 微服务调用
		List<HashMap<String,Object>> listmap=new ArrayList<>();
		if (page.getList()!=null && page.getList().size()>0){
			List<CustomerTimeliness> list=page.getList();
			HashMap<String,Object> map=new HashMap<>();
			Set<Long> customerIds = list.stream().map(r->r.getCustomer().getId()).collect(Collectors.toSet());
			Map<Long, Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(Lists.newArrayList(customerIds));
			Customer customer;
			for (CustomerTimeliness entity:list) {
				Long customerId=entity.getCustomer().getId();
				// Customer customer = customerService.getFromCache(customerId);  // mark on 2019-7-31
				customer = customerMap.get(customerId);            // 改成调用微服务  //2019-7-31
				entity.setCustomer(customer);

				map=new HashMap<>();
//				map.put("customerId",entity.getCustomer().getId());      // mark on 2019-8-1
//				map.put("customerName",entity.getCustomer().getName());  // mark on 2019-8-1
				map.put("customerId", entity.getCustomer() == null ? customerId:entity.getCustomer().getId());  // update on 2019-7-31
				map.put("customerName", entity.getCustomer() == null? "":entity.getCustomer().getName());       // update on 2019-7-31

				List<AreaTimelinessModel> list1=customerTimelinessService.findListByCustomerId(customerId);
				map.put("areaTimelinessModelList",list1);
				listmap.add(map);
			}

			//按照客户名称排序
			listmap.sort((HashMap map1,HashMap map2)->map1.get("customerName").toString().compareTo(map2.get("customerName").toString()));
		}
		List<TimelinessLevel> timelineList =timelinessLevelService.findAllList();
		model.addAttribute("page", page);
		model.addAttribute("customerTimeliness", customerTimeliness);
		model.addAttribute("timelineList", timelineList);
		model.addAttribute("listmap", listmap);
		return "modules/md/customerTimelinessListForCustomer";
	}


	@RequiresPermissions("md:customertimeliness:edit")
	@RequestMapping(value = "forms")
	public String forms(CustomerTimeliness customerTimeliness, Model model) {
		String viewModel = "modules/md/customerTimelinessNewForms";

		List<TimelinessLevel> timelineList = timelinessLevelService.findAllList();
		if (timelineList == null || timelineList.size() == 0){
			addMessage(model,"请先设置时效等级");
			model.addAttribute("customerTimeliness", customerTimeliness);
			model.addAttribute("canAction", false);
			return viewModel;
		}

		if(customerTimeliness.getCustomer() != null && customerTimeliness.getCustomer().getId() != null) {
			Long customerId = customerTimeliness.getCustomer().getId();
			List<AreaTimelinessModel> list = customerTimelinessService.findListByCustomerId(customerId);
			if (list != null && list.size() > 0) {
				customerTimeliness.setAreaTimelinessModelList(list);
				model.addAttribute("timelineList", timelineList);
				model.addAttribute("customerTimeliness", customerTimeliness);
				model.addAttribute("canAction", true);
				return viewModel;
			}
			model.addAttribute("customerTimeliness", customerTimeliness);
			model.addAttribute("canAction", true);
			return viewModel;
		}
		List<Customer> customerList=CustomerUtils.getMyCustomerList();
		List<Area> areaList=areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
		//customerTimeliness.setAreaList(areaList);

		List<TimelinessChargeModel> timelinessChargeModelList=new ArrayList<>();
		for (TimelinessLevel  timelinessLevel:timelineList) {
			TimelinessChargeModel chargeModel=new TimelinessChargeModel();
			chargeModel.setTimelinessLevel(timelinessLevel);
			chargeModel.setChargeIn(timelinessLevel.getChargeIn());
			chargeModel.setChargeOut(timelinessLevel.getChargeOut());
			timelinessChargeModelList.add(chargeModel);
		}

		List<AreaTimelinessModel> areaTimelinessModels=new ArrayList<>();
		for (Area area:areaList) {
			AreaTimelinessModel areaTimelinessModel=new AreaTimelinessModel();
			areaTimelinessModel.setArea(area);
			areaTimelinessModel.setList(timelinessChargeModelList);
			areaTimelinessModels.add(areaTimelinessModel);
		}
		customerTimeliness.setAreaTimelinessModelList(areaTimelinessModels);
		model.addAttribute("timelineList", timelineList);
		model.addAttribute("customerList", customerList);
		model.addAttribute("customerTimeliness", customerTimeliness);
		model.addAttribute("canAction", true);
		return viewModel;
	}

	@RequiresPermissions("md:customertimeliness:edit")
	@RequestMapping(value = "saveCustomerTimelinesss")
	public String saveCustomerTimelinesss(CustomerTimeliness customerTimeliness, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, customerTimeliness))
		{
			return forms(customerTimeliness, model);
		}

		if (customerTimeliness.getCustomer()!=null && customerTimeliness.getCustomer().getId()!=null){
			customerTimelinessService.saveCustomerTimelinesss(customerTimeliness);
			addMessage(redirectAttributes, "保存成功");
		}else
		{
			addMessage(model, "保存失败，客户Id有误。请重试。");
			return forms(customerTimeliness, model);
		}
		return "redirect:"+ adminPath+"/md/customertimeliness?repage";
	}

	@RequiresPermissions("md:urgentcustomer:edit")
	@RequestMapping(value = "save")
	@ResponseBody
	public AjaxJsonEntity save(CustomerTimeliness customerTimeliness, Model model) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if (!beanValidator(model, customerTimeliness)){
			ajaxJsonEntity.setSuccess(false);
			return ajaxJsonEntity;
		}
		if (customerTimeliness.getCustomer()!=null && customerTimeliness.getCustomer().getId()!=null){
			try {
				customerTimelinessService.saveCustomerTimelinesss(customerTimeliness);
				ajaxJsonEntity.setMessage("保存成功");
			}catch (Exception e){
				ajaxJsonEntity.setSuccess(false);
				ajaxJsonEntity.setMessage(e.getMessage());
			}

		}else{
			ajaxJsonEntity.setMessage("保存失败，客户Id有误。请重试。");
			ajaxJsonEntity.setSuccess(false);
			return ajaxJsonEntity;
		}
		return ajaxJsonEntity;
	}
	/**
	 *
	 * @param customerId 客户ID
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("md:customertimeliness:edit")
	@RequestMapping(value = "delete")
	public String delete(Long customerId, RedirectAttributes redirectAttributes) {

		customerTimelinessService.deleteByCustomerId(customerId);
		addMessage(redirectAttributes, "删除成功");
		return "redirect:"+adminPath+"/md/customertimeliness?repage";
	}

}
