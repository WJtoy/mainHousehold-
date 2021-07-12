/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.entity.md.MDEngineerAddress;
import com.kkl.kklplus.entity.md.MDEngineerCert;
import com.kkl.kklplus.entity.md.MDEngineerEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.persistence.zTreeEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.viewModel.EngineerAreaManageVM;
import com.wolfking.jeesite.modules.md.service.EngineerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerAddressService;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安维人员Controller
 * @author Ryan Lu
 * @version 2017-04-26
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/engineer")
public class EngineerController extends BaseController {

	@Autowired
	private ServicePointService servicePointService;

	@Autowired
	private EngineerService engineerService;

	@Autowired
	private SystemService systemService;

	@Autowired
	GsonRedisSerializer gsonRedisSerializer;

	@Autowired
	private MSEngineerAddressService engineerAddressService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private ServicePointService service;

	@Autowired
	private MSEngineerService msEngineerService;


	@ModelAttribute
	public Engineer get(@RequestParam(required=false) Long id) {
		Engineer engineer;
		if (id != null){
			//engineer = servicePointService.getEngineer(id);  // mark on 2019-10-17
			engineer = engineerService.getEngineer(id);		   // add on 2019-10-17
//			List<Area> areaList = servicePointService.getEngineerAreaList(id);
//			if(areaList == null){
//				areaList = Lists.newArrayList();
//			}
//			engineer.setAreaList(areaList);
			ServicePoint s;
			if (engineer.getServicePoint().getId() != null) {
				s = service.getFromCache(engineer.getServicePoint().getId());
				engineer.setServicePoint(s);
			}
		}else{
			engineer = new Engineer();
			//engineer.setAreaList(Lists.newArrayList());
		}
		return engineer;
	}

	/**
	 * 安维人员列表
     */
	//@RequiresPermissions(value = "md:engineer:view")
	@RequestMapping(value = {"list", ""})
	public String list(Engineer engineer, HttpServletRequest request, HttpServletResponse response, Model model) {
		if (!SecurityUtils.getSubject().isPermitted("md:engineer:view")) {
			addMessage(model, "未开通浏览权限");
			model.addAttribute("page", new Page<>());
			model.addAttribute("engineer", engineer);
			return "modules/md/engineerList";
		}
		//安维网点主帐号管理
		User user = UserUtils.getUser();
		if(user.isEngineer()) {
			Engineer e = servicePointService.getEngineer(user.getEngineerId());
			engineer.setServicePoint(e.getServicePoint());
		}
		if(engineer.getFirstSearch()==1){
			engineer.setMasterFlag(-1);
			engineer.setAppFlag(-1);
			engineer.setFirstSearch(0);
		}
		//engineer.setDelFlag(null);//包含停用的安维   //mark on 2019-11-13
		engineer.setDelFlag(-1);  // 改成调用Engineer微服务后，基类MSBase已默认设置DelFlag为0，所以设置DelFlag为-1时包含停用的安维  // add on 2019-11-13
		String spNo = StringUtils.EMPTY;
		String spName = StringUtils.EMPTY;
		// add on 2020-6-10 begin
		// 网点管理中的人员管理中只传了网点id
		if(engineer.getServicePoint() != null && engineer.getServicePoint().getId() != null){
			ServicePoint servicePoint = servicePointService.getSimple(engineer.getServicePoint().getId());
			if (servicePoint != null) {
				engineer.getServicePoint().setServicePointNo(servicePoint.getServicePointNo());
			}
		}
		// add on 2020-6-10 end
		if(engineer.getServicePoint() != null && StringUtils.isNotBlank(engineer.getServicePoint().getServicePointNo())){
			spNo = engineer.getServicePoint().getServicePointNo();
		}
		if(engineer.getServicePoint() != null && StringUtils.isNotBlank(engineer.getServicePoint().getName())){
			spName = engineer.getServicePoint().getName();
		}
		if(StringUtils.isBlank(spNo) && StringUtils.isBlank(engineer.getName()) &&StringUtils.isBlank(engineer.getContactInfo()) && StringUtils.isBlank(spName)){
			addMessage(model, "注:请选择网点编号，师傅，师傅手机中至少一项进行查询!");
			model.addAttribute("page", new Page<>());
			model.addAttribute("engineer", engineer);
			return "modules/md/engineerList";
		}
		Page<Engineer> page = servicePointService.findPage(new Page<Engineer>(request, response), engineer);
        model.addAttribute("page", page);
		model.addAttribute("engineer", engineer);
		return "modules/md/engineerList";
	}

	/**
	 * 修改安维
	 */
	@RequiresPermissions(value = {"md:engineer:view","md:engineer:edit"},logical = Logical.OR)
	@RequestMapping(value = "form")
	public String form(Engineer engineer, Model model) {
		User user = UserUtils.getUser();
		//新增
		List<Long> areaIds = Lists.newArrayList();
		if(engineer.getId()==null) {
			if(user.isEngineer()){
				//Engineer e = servicePointService.getEngineer(user.getEngineerId());  //mark on 2020-5-23
				//engineer.setServicePoint(e.getServicePoint());      //mark on 2020-5-23
				ServicePoint servicePoint = servicePointService.get(user.getCompany().getId()); //add on 2020-5-23
				engineer.setServicePoint(servicePoint);
				engineer.setMasterFlag(0);//只能添加子帐号
			}else if (engineer.getServicePoint() != null && engineer.getServicePoint().getId() !=null) {
				ServicePoint point = servicePointService.get(engineer.getServicePoint().getId());
				engineer.setServicePoint(point);
			}
			engineer.setLevel(new Dict("1","等级一"));
		}else {
			areaIds = servicePointService.getEngineerAreaIds(engineer.getId());
		}
		Long areaId = null;
		// 调用微服务

		if(!engineer.getAttachment().equals("")){
			Gson gson = new GsonBuilder().create();
			List<MDEngineerCert> list = gson.fromJson(engineer.getAttachment(),new TypeToken<ArrayList<MDEngineerCert>>(){}.getType());
			engineer.setEngineerCerts(list);
		}

		MDEngineerAddress engineerAddress = engineerAddressService.getByEngineerId(engineer.getId());
		if (engineerAddress != null) {
			engineer.setEngineerAddress(engineerAddress);
			areaId = engineerAddress.getAreaId() != null ? engineerAddress.getAreaId() : null;
		}
		Area area = areaId == null ? null : areaService.getFromCache(areaId);
		if (area != null) {
			model.addAttribute("engineerAddressArea", area);
		}

		ArrayList<MDEngineerEnum.EngineerCertPicOrder> list = new ArrayList<>(Arrays.asList(MDEngineerEnum.EngineerCertPicOrder.values()));
		List<MDEngineerCert> mdEngineerCerts = org.assertj.core.util.Lists.newArrayList();
		for (MDEngineerEnum.EngineerCertPicOrder item : list) {
			MDEngineerCert mdEngineerCert = new MDEngineerCert();
			mdEngineerCert.setNo(item.getValue());
			mdEngineerCert.setPicUrl(String.valueOf(item.getLabel()));
			mdEngineerCerts.add(mdEngineerCert);
		}

		if(engineer.getArea() != null && engineer.getArea().getId() != null){
			engineer.setAddress(engineer.getAddress().replace(engineer.getArea().getFullName(),""));
		}

		List<String> areaNames = engineerService.getServiceAreaNames(areaIds);
		engineer.setOrgMasterFlag(engineer.getMasterFlag());//important
		String areas = StringUtils.join(areaIds, ",");;
		model.addAttribute("engineer", engineer);
		model.addAttribute("userType", user.getUserType());
		model.addAttribute("mdEngineerCerts", mdEngineerCerts);
		model.addAttribute("areaNum", areaIds.size());
		model.addAttribute("areaNames", areaNames);
		model.addAttribute("areas", areas);
//		//网点-区域
//		model.addAttribute("areaList", servicePointService.getAreas(engineer.getServicePoint().getId());
		return "modules/md/engineerFormNew";
	}

	/**
	 * 保存安维
	 */
	@RequiresPermissions("md:engineer:edit")
	@RequestMapping(value = "save")
	public String save(Engineer engineer, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, engineer)){
			return form(engineer, model);
		}
		engineer.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);
		//level.label
//		List<Dict> dicts = dictService.getDictListByIdFromCache("ServicePointLevel");
		List<Dict> dicts = MSDictUtils.getDictList("ServicePointLevel");//切换为微服务
		if(dicts !=null && dicts.size()>0){
			Dict dict = dicts.stream().filter(t->t.getValue().equalsIgnoreCase(engineer.getLevel().getValue())).findFirst().orElse(null);
			if(dict !=null){
				engineer.setLevel(dict);
			}
		}
		//servicepoint
		ServicePoint servicePoint = servicePointService.getFromCache(engineer.getServicePoint().getId());
		if(servicePoint!=null) {
			engineer.setServicePoint(servicePoint);
		}
		//区域列表
		StringBuilder json = new StringBuilder(engineer.getAreas());
		List<Area> areaList = Lists.newArrayList();
		if(json.length()>0){
			json = new StringBuilder(json.toString().replace("&quot;","\""));
			areaList = Arrays.asList((Area[])gsonRedisSerializer.fromJson(json.toString(),Area[].class));
		}
		engineer.setAreaList(areaList);
		try {
			engineerService.save(engineer);
			addMessage(redirectAttributes, "保存安维人员'" + engineer.getName() + "'成功");
			//主帐号更改需同步网点缓存 2018/08/04
			try {
				servicePoint.setPrimary(engineer);
				if (engineer.getMasterFlag() == 1 && servicePoint.getPrimary().getId().equals(engineer.getId())) {
					//servicePointService.updateServicePointCache(servicePoint);  //mark on 2020-1-14  web端去servicePoint
				}
			}catch (Exception e){
				log.error("[EngineerController.save]sync servicepoint error",e);
			}
			return String.format("redirect:%s/md/engineer/list?servicePoint.id=%s&servicePoint.name=%s&servicePoint.servicePointNo=%s&repage",adminPath,engineer.getServicePoint().getId(), URLEncoder.encode(engineer.getServicePoint().getName()),URLEncoder.encode(engineer.getServicePoint().getServicePointNo()));
		}catch (Exception ex){
			return form(engineer,model);
		}

	}

	@RequiresPermissions("md:engineer:edit")
	@RequestMapping(value = "saveEngineer")
	@ResponseBody
	public AjaxJsonEntity saveEngineer(Engineer engineer, Model model, RedirectAttributes redirectAttributes) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if (!beanValidator(model, engineer)){
			ajaxJsonEntity.setSuccess(false);
			return ajaxJsonEntity;
		}
		engineer.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);
		List<Dict> dicts = MSDictUtils.getDictList("ServicePointLevel");//切换为微服务
		if(dicts !=null && dicts.size()>0){
			Dict dict = dicts.stream().filter(t->t.getValue().equalsIgnoreCase(engineer.getLevel().getValue())).findFirst().orElse(null);
			if(dict !=null){
				engineer.setLevel(dict);
			}
		}
		//servicepoint
		ServicePoint servicePoint = servicePointService.getFromCache(engineer.getServicePoint().getId());
		if(servicePoint!=null) {
			engineer.setServicePoint(servicePoint);
		}
		//区域列表
		List<Area> areaList = Lists.newArrayList();
		List<Long> areaIds = Arrays.asList(engineer.getAreas().split(",")).stream().map(a -> Long.valueOf(a)).collect(Collectors.toList());
		for (Long areaId : areaIds) {
			Area area = new Area();
			area.setId(Long.valueOf(areaId));
			areaList.add(area);
		}
		engineer.setAreaList(areaList);
		if (engineer.getEngineerAddress() != null) {
			engineer.getEngineerAddress().setServicePointId(engineer.getServicePoint().getId());
		}
		if (engineer.getAddress() != null && !"".equals(engineer.getAddress()) && engineer.getAddress().indexOf(",") == 0) {
			String address = engineer.getAddress().substring(1, engineer.getAddress().length() - 1);
			engineer.setAddress(address);
		}

		if(!engineer.getAttachment().equals("")){
			List<MDEngineerCert> engineerCerts = new ArrayList();
			MDEngineerCert entity;
			String[] arr  =  engineer.getAttachment().split(":");
			for(int i = 0;i<arr.length;i++){
				if(arr[i] != null && !arr[i].equals("")){
					String[] starr = arr[i].split(",");
					entity = new MDEngineerCert();
					entity.setNo(Integer.valueOf(starr[0]));
					entity.setPicUrl(starr[1]);
					engineerCerts.add(entity);
				}
			}
			engineer.setEngineerCerts(engineerCerts);
		}
		try {
			engineerService.save(engineer);
			ajaxJsonEntity.setMessage("保存安维人员'" + engineer.getName() + "'成功");
			//主帐号更改需同步网点缓存 2018/08/04
			try {
				servicePoint.setPrimary(engineer);
			}catch (Exception e){
				log.error("[EngineerController.save]sync servicepoint error",e);
			}
			return ajaxJsonEntity;

		}catch (Exception ex){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(ex.getMessage());
			return ajaxJsonEntity;
		}

	}

	/**
	 * 编辑或新增安维时，装载区域
	 * 1.网点的区域
	 * 2.安维已分配的区域
	 * @param sid 		网点id
	 * @param eid 		安维人员id
	 */
	@ResponseBody
	@RequestMapping(value = "loadEngineerAreas", method = RequestMethod.GET )
	public AjaxJsonEntity loadEngineerAreas(Long sid,Long eid,HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		if(sid==null){
//			result.setSuccess(false);
			result.setMessage("未设置网点。");
			return result;
		}

		try{
			List<Area> serviceAreas = servicePointService.getAreas(sid);
			if(serviceAreas == null || serviceAreas.size()==0){
				result.setSuccess(false);
				result.setMessage("网点未授权负责的区域。");
				return result;
			}
			/*查询父节点
			List<String> pidList = serviceAreas.stream().map(t->t.getParentIds()).distinct().collect(Collectors.toList());
			String sids = String.join(",",pidList);
			String[] arrIds = sids.split(",");
			Set<String> set = new HashSet<>();
			for(int i=0;i<arrIds.length;i++){
				set.add(arrIds[i]);
			}
			String[] arrayResult = (String[]) set.toArray(new String[set.size()]);
			String id = new String("");
			Area parent;
			for(int i=0,size=arrayResult.length;i<size;i++){
				id = arrayResult[i];
				if(StringUtils.isNoneBlank(id) && !"0".equals(id.trim()) && !"1".equals(id.trim()) ){
					parent = areaService.getFromCache(Long.valueOf(id));
					if(parent!=null){
						serviceAreas.add(0,parent);
					}
				}
			}
			*/
			/*
			List<Area> areaList = new ArrayList<>();
			// 去重
			serviceAreas.stream().forEach(
					p -> {
						if (!areaList.contains(p)) {
							areaList.add(p);
						}
					}
			);
			serviceAreas = areaList.stream()
					.sorted(Comparator.comparing(Area::getType)
					.thenComparing(Area::getSort))
					.collect(Collectors.toList());
			*/
			serviceAreas = serviceAreas.stream()
					.filter(t->t.getId() >1)
					.sorted(Comparator.comparing(Area::getType)
					.thenComparing(Area::getParentId)
					.thenComparing(Area::getSort))
					.collect(Collectors.toList());

			if(serviceAreas.stream().filter(t->t.getType()==1).count()==0){
				serviceAreas.add(0,new Area(1l,"区域列表",1));
			}
			EngineerAreaManageVM data = new EngineerAreaManageVM();
			List<zTreeEntity> treeList = Lists.newArrayList();
			treeList = serviceAreas.stream().map(t-> new zTreeEntity(t.getId(),t.getParentId(),t.getName(),t.getType())).collect(Collectors.toList());

			data.setServiceAreas(treeList);

			if(eid !=null && eid>0) {
				List<Area> areas = servicePointService.getEngineerAreaList(eid);
				if (areas != null && areas.size() > 0) {
					List<Long> ids = areas.stream()
							.map(t -> t.getId())
							.collect(Collectors.toList());
					data.setAreaIds(ids);
				}
			}
			result.setData(data);
		} catch (Exception ex){
			log.error("[EngineerController.loadEngineerAreas]",ex);
			//return "false";
			result.setSuccess(false);
			result.setMessage("读取网点及安维区域错误");
		}
		return result;
	}

	/**
	 * 停用
	 * 逻辑删除安维人员
	 */
	@RequiresPermissions("md:engineer:stop")
	@RequestMapping(value = "delete")
	@ResponseBody
	public AjaxJsonEntity delete(Long id, Long pointId,HttpServletRequest request, RedirectAttributes redirectAttributes)
	{
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}

		Engineer engineer = servicePointService.getEngineer(id);
		if(engineer == null){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("停用用户失败, 该安维不存在");
		}else if(engineer.getDelFlag() == Engineer.DEL_FLAG_DELETE){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("停用用户失败, 该安维已停用");
		}else{
			servicePointService.delete(engineer);
			if(engineer.getMasterFlag()==1){
				ajaxJsonEntity.setMessage("停用安维人员成功，网点已无可用主帐号。");
			}else{
				ajaxJsonEntity.setMessage("停用安维人员成功");
			}
		}

		return ajaxJsonEntity;
	}

	/**
	 * 启用
	 * @param id
	 * @param contactInfo
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("md:engineer:stop")
	@RequestMapping(value = "enable")
	@ResponseBody
	public AjaxJsonEntity enable(Long id, String contactInfo, HttpServletRequest request, RedirectAttributes redirectAttributes)
	{
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(Global.isDemoMode()){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("演示模式，不允许操作！");
		}

		Engineer engineer = servicePointService.getEngineer(id);
		if(engineer == null){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("该安维人员不存在");
		}else if(engineer.getDelFlag().equals(Engineer.DEL_FLAG_NORMAL)){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("该安维人员已启用");
		}else if(servicePointService.checkEngineerMobile(id, contactInfo.trim()).equalsIgnoreCase("false")){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("该联系电话已被使用");
		}else{
			servicePointService.enable(engineer);
			ajaxJsonEntity.setMessage("启用安维人员成功");
		}

		return ajaxJsonEntity;
	}

	/**
	 * 检查网点编号是否有多个主帐号(一个网点只允许有一个主帐号)
	 * @param servicePointId 		网点id
	 * @param expectId 	排除安维人员id
	 */
	@ResponseBody
	@RequiresPermissions("md:engineer:edit")
	@RequestMapping(value = "checkPrimary", method = RequestMethod.POST )
	public AjaxJsonEntity checkPrimary(Long servicePointId,Long expectId,HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
		try{
			int cnt = servicePointService.checkMasterEngineer(servicePointId,expectId);
			//无主帐号：false
			result.setSuccess(cnt>0?false:true);
//			return cnt>0?"false":"true";
		} catch (Exception ex){
			log.error("[EngineerController.checkPrimary]", ex);
			//return "false";
			result.setSuccess(false);
			result.setMessage("检查网点时候有多主帐号错误");
		}
		return result;
	}

	/**
	 * 验证登录名是否有效
	 * @param contactInfo   手机号
	 * @return
	 */
	@ResponseBody
//	@RequiresPermissions("md:engineer:edit")
	@RequestMapping(value = "checkLoginName")
	public String checkLoginName(String contactInfo,Long expectId,HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
//        User cuser = UserUtils.getUser();
//        if(cuser==null || cuser.getId()==null){
//            return "false";
//        }
		if(StringUtils.isBlank(contactInfo)) {
			return "true";
		}
		User user = servicePointService.getEngineerByPhoneExpect(contactInfo,expectId);
		if( user== null || user.getId() == null) {
			return "true";
		}
		if(Objects.equals(user.getId(),expectId)){
			return "true";
		}
		return "该手机号已存在，请确认输入是否正确";
//		return "false";
	}

	/**
	 * 重置密码
	 * @param id  安维id
	 * @return
	 */
	@ResponseBody
	@RequiresPermissions("md:engineer:resetpassword")
	@RequestMapping(value = "resetPassword")
	public AjaxJsonEntity resetPassword(String id,HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(StringUtils.isBlank(id)){
			result.setSuccess(false);
			result.setMessage("参数错误");
			return result;
		}
		Long lid = Long.valueOf(id);

        User cuser = UserUtils.getUser();
        if(cuser==null || cuser.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
		Engineer engineer = servicePointService.getEngineer(lid);
		if( engineer== null || engineer.getId() == null) {
			result.setSuccess(false);
			result.setMessage("安维人员不存在或已停用");
			return result;
		}
		try {
			User user = systemService.getUserByEngineerId(lid);
//			User user = new User();
//			user.setEngineerId(id);
			user.preUpdate();
//			user.setPassword(SystemService.entryptPassword(engineer.getContactInfo()));
			String pwd = new String("");
			pwd = StringUtils.right(engineer.getContactInfo().trim(),6);
			//手机号后6位
			user.setPassword(SystemService.entryptPassword(pwd));
			servicePointService.resetPassword(user);
			//清除该帐号登录内容
			try {
				UserUtils.clearCache(user);
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}catch (Exception e){
			result.setSuccess(false);
			result.setMessage("更改密码时发生错误："+e.getMessage());
		}
		return  result;
	}

	/**
	 * 升级为网点
	 */
	@RequiresPermissions(value = "md:engineer:upgrade")
	@RequestMapping(value = "upgrade")
	public String upgrade(Engineer engineer, Model model) {
		//新增
		if(engineer.getId()==null) {
			model.addAttribute("engineer", engineer);
			addMessage(model,"参数错误");
			return "modules/md/engineerUpgradeForm";
		}
		model.addAttribute("engineer", engineer);
		return "modules/md/engineerUpgradeForm";
	}

	/**
	 * 保存安维
	 */
	@RequiresPermissions("md:engineer:upgrade")
	@RequestMapping(value = "saveUpgrade")
	public String saveUpgrade(Engineer engineer, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, engineer)){
			return form(engineer, model);
		}
		engineer.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);
		engineerService.save(engineer);
		addMessage(redirectAttributes, "升级安维[" + engineer.getName() + "]为网点成功");
		return "redirect:" + adminPath + "/md/engineer/list?servicePoint.id=" + engineer.getServicePoint().getId() + "&repage";
	}

	/**
	 * 安维网点选择列表
	 */
	@RequiresUser
	@RequestMapping(value = "select")
	public String select(Engineer engineer, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<Engineer> page = servicePointService.findPage(new Page<Engineer>(request, response), engineer);
		model.addAttribute("page", page);
		model.addAttribute("engineer", engineer);
		return "modules/md/engineerSelect";
	}

	/**
	 * 安维网点选择列表
	 * @param searchType 1:不查数据 2:根据条件分页查询
	 */
	@RequiresUser
	@RequestMapping(value = "selectForKefu")
	public String selectForKefu(Engineer engineer, HttpServletRequest request, HttpServletResponse response, Model model,Integer searchType)
	{
		Page<Engineer> page = new Page<>(request, response);
		if(searchType==2){
			 page = engineerService.getEngineersForKefu(new Page<>(request, response), engineer);
		}
		model.addAttribute("page", page);
		model.addAttribute("engineer", engineer);
		model.addAttribute("searchType", searchType);
		return "modules/md/engineerSelectForKefu";
	}

	@RequiresPermissions("md:engineer:edit")
	@ResponseBody
	@RequestMapping("reloadEngineerCacheById/{id}")
	public String reloadEngineerCacheById(@PathVariable Long id){
		if (id <= 0 ) {
			return "fail.";
		}
		int i = msEngineerService.reloadEngineerCacheById(id);
		return "success.count="+i;
	}

	@RequiresPermissions("md:engineer:edit")
	@ResponseBody
	@RequestMapping("getFromCache/{id}")
	public Engineer getFromCache(@PathVariable Long id){
		if (id <= 0 ) {
			return null;
		}
		return msEngineerService.getByIdFromCache(id);
	}
}
