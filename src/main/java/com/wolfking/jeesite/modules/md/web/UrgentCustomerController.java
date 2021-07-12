package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.UrgentCustomer;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.UrgentCustomerService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerUrgentService;
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
@RequestMapping(value = "${adminPath}/md/urgentcustomer")
public class UrgentCustomerController extends BaseController
{
	@Autowired
	private UrgentCustomerService urgentCustomerService;

	@Autowired
	private UrgentLevelService urgentLevelService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private MSCustomerUrgentService msCustomerUrgentService;

	@Autowired
	private MSCustomerService msCustomerService;

	@RequiresPermissions("md:urgentcustomer:view")
	@RequestMapping(value = {"list", ""})
	public String list(UrgentCustomer urgentCustomer, HttpServletRequest request, HttpServletResponse response, Model model) {
		if (urgentCustomer.getCustomer() == null || urgentCustomer.getCustomer().getId() == null) {
			addMessage(model, "请选择客户!");
			return "modules/md/urgentCustomerNewList";
		}
		List<UrgentLevel> urgentLevelList= urgentLevelService.findAllList();
//		Page<UrgentCustomer> page = urgentCustomerService.findPage(new Page<>(request, response), urgentCustomer);   // mark on 2019-8-1
		Page<UrgentCustomer> page = msCustomerUrgentService.findList(new Page<>(request, response), urgentCustomer); // add on 2019-8-1 微服务调用
		List<HashMap<String,Object>> listmap=new ArrayList<>();
		if (page.getList()!=null && page.getList().size()>0){
			List<UrgentCustomer> list=page.getList();
			Set<Long> customerIds = list.stream().map(r->r.getCustomer().getId()).collect(Collectors.toSet());
			Map<Long, Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(Lists.newArrayList(customerIds));
			HashMap<String,Object> map=new HashMap<>();
			Long customerId;
			Customer customer;
			List<AreaUrgentModel> areaUrgentModels;
			for (UrgentCustomer entity:list) {
				customerId=entity.getCustomer().getId();
//				Customer custoemr=customerService.getFromCache(customerId);   // mark on 2019-8-1
				customer = customerMap.get(customerId);        // add on 2019-8-1  微服务调用
				entity.setCustomer(customer);

				map=new HashMap<>();
//				map.put("customerId",entity.getCustomer().getId());        //mark on 2019-8-1
//				map.put("customerName",entity.getCustomer().getName());	   //mark on 2019-8-1
				map.put("customerId",entity.getCustomer()==null? customerId:entity.getCustomer().getId());  //update on 2019-8-1
				map.put("customerName",entity.getCustomer()==null? "":entity.getCustomer().getName());      //update on 2019-8-1

				areaUrgentModels=urgentCustomerService.findListByCustomerId(customerId);
				map.put("AreaUrgentModelList",areaUrgentModels);
				listmap.add(map);
			}
			//按客户名称排序
			listmap=listmap.stream().sorted(Comparator.comparing(t->t.get("customerName").toString())).collect(Collectors.toList());
		}
		model.addAttribute("page", page);
		model.addAttribute("listmap", listmap);
		model.addAttribute("urgentLevelList",urgentLevelList);

		return "modules/md/urgentCustomerNewList";
	}
	@RequestMapping(value = "customerList")
	public String customerList(UrgentCustomer urgentCustomer, HttpServletRequest request, HttpServletResponse response, Model model) {

		List<UrgentLevel> urgentLevelList= urgentLevelService.findAllList();
		//是客户账号则设置查询条件
		User user=UserUtils.getUser();
		if (user.isCustomer()){
			urgentCustomer.setCustomer(user.getCustomerAccountProfile().getCustomer());
		}
		Page<UrgentCustomer> page = urgentCustomerService.findPage(new Page<>(request, response), urgentCustomer);
		List<HashMap<String,Object>> listmap=new ArrayList<>();

		if (page.getList()!=null && page.getList().size()>0){
			List<UrgentCustomer> list=page.getList();
			Set<Long> customerIds = list.stream().map(r->r.getCustomer().getId()).collect(Collectors.toSet());
			Map<Long, Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(Lists.newArrayList(customerIds));
			HashMap<String,Object> map=new HashMap<>();
			Customer customer;
			for (UrgentCustomer entity:list) {
				Long customerId=entity.getCustomer().getId();
//				Customer custoemr=customerService.getFromCache(customerId);   // mark on 2019-8-1
				customer = customerMap.get(customerId);        // add on 2019-8-1  微服务调用
				entity.setCustomer(customer);

				map=new HashMap<>();
//				map.put("customerId",entity.getCustomer().getId());        //mark on 2019-8-1
//				map.put("customerName",entity.getCustomer().getName());	   //mark on 2019-8-1
				map.put("customerId",entity.getCustomer()==null? customerId:entity.getCustomer().getId());  //update on 2019-8-1
				map.put("customerName",entity.getCustomer()==null? "":entity.getCustomer().getName());      //update on 2019-8-1

				List<AreaUrgentModel> areaUrgentModels=urgentCustomerService.findListByCustomerId(customerId);
				map.put("AreaUrgentModelList",areaUrgentModels);
				listmap.add(map);
			}
			//按客户名称排序
			listmap=listmap.stream().sorted(Comparator.comparing(t->t.get("customerName").toString())).collect(Collectors.toList());
		}
		model.addAttribute("page", page);
		model.addAttribute("listmap", listmap);
		model.addAttribute("urgentLevelList",urgentLevelList);

		return "modules/md/urgentCustomerListForCustomer";
	}

	@RequiresPermissions("md:urgentcustomer:edit")
	@RequestMapping(value = "forms")
	public String forms(UrgentCustomer urgentCustomer, Model model) {
		String viewModel = "modules/md/urgentCustomerNewForms";
		List<UrgentLevel> urgentLevelList= urgentLevelService.findAllList();
		if (urgentLevelList == null || urgentLevelList.size() == 0){
			addMessage(model,"请先设置加急等级");
			model.addAttribute("urgentCustomer", urgentCustomer);
			model.addAttribute("canAction", false);
			return viewModel;
		}

		if(urgentCustomer.getCustomer() != null && urgentCustomer.getCustomer().getId() != null) {
			Long customerId = urgentCustomer.getCustomer().getId();
			List<AreaUrgentModel> list = urgentCustomerService.findListByCustomerId(customerId);
			if (list != null && list.size() > 0) {
				urgentCustomer.setList(list);
				model.addAttribute("urgentCustomer", urgentCustomer);
				model.addAttribute("urgentLevelList", urgentLevelList);
				model.addAttribute("canAction", true);
				return viewModel;
			}
		}
		List<Customer> customerList=CustomerUtils.getMyCustomerList();
		List<Area> areaList=areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
		List<UrgentChargeModel> list=new ArrayList<>();
		for (UrgentLevel urgentLevel:urgentLevelList) {


			UrgentChargeModel urgentLevel1=new UrgentChargeModel();
			urgentLevel1.setChargeIn(urgentLevel.getChargeIn());
			urgentLevel1.setChargeOut(urgentLevel.getChargeOut());
			urgentLevel1.setUrgentLevel(urgentLevel);
			list.add(urgentLevel1);
		}

		List<AreaUrgentModel> areaTimelinessModels=new ArrayList<>();
		for (Area area:areaList) {
			AreaUrgentModel areaUrgentModel=new AreaUrgentModel();
			areaUrgentModel.setArea(area);
			areaUrgentModel.setList(list);
			areaTimelinessModels.add(areaUrgentModel);
		}

		urgentCustomer.setList(areaTimelinessModels);
		model.addAttribute("customerList", customerList);
		model.addAttribute("urgentCustomer", urgentCustomer);
		model.addAttribute("urgentLevelList", urgentLevelList);
		model.addAttribute("canAction", true);
		return viewModel;
	}

	@RequiresPermissions("md:urgentcustomer:edit")
	@RequestMapping(value = "saveUrgentCustomers")
	public String saveUrgentCustomers(UrgentCustomer urgentCustomer, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, urgentCustomer))
		{
			return forms(urgentCustomer, model);
		}

		if (urgentCustomer.getCustomer()!=null && urgentCustomer.getCustomer().getId()!=null){
			urgentCustomerService.saveUrgentCustomers(urgentCustomer);
			addMessage(redirectAttributes, "保存成功");
		}else
		{
			addMessage(model, "保存失败，客户Id有误。请重试。");
			return forms(urgentCustomer, model);
		}
		return "redirect:"+ adminPath+"/md/urgentcustomer?repage";
	}

	@RequiresPermissions("md:urgentcustomer:edit")
	@RequestMapping(value = "save")
	@ResponseBody
	public AjaxJsonEntity save(UrgentCustomer urgentCustomer, Model model) {

		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

		if (!beanValidator(model, urgentCustomer)){
			ajaxJsonEntity.setSuccess(false);
			return ajaxJsonEntity;
		}

		if (urgentCustomer.getCustomer()!=null && urgentCustomer.getCustomer().getId()!=null){
			try {
				urgentCustomerService.saveUrgentCustomers(urgentCustomer);
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
	@RequiresPermissions("md:urgentcustomer:edit")
	@RequestMapping(value = "delete")
	public String delete(Long customerId, RedirectAttributes redirectAttributes) {

		urgentCustomerService.deleteByCustomerId(customerId);
		addMessage(redirectAttributes, "删除成功");
		return "redirect:"+adminPath+"/md/urgentcustomer?repage";
	}

}
