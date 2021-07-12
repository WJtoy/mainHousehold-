package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.AreaTimeLinessService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "${adminPath}/md/areaTimeLiness")
public class AreaTimeLinessController extends BaseController
{

	@Autowired
	private AreaTimeLinessService areaTimeLinessService;

	@Autowired
	private AreaService areaService;



	@RequiresPermissions("md:areatimeliness:view")
	@RequestMapping(value = {""})
	public String index(Model model){
       return "modules/md/areaTimeLinessIndex";
	}


	/**
	 * 查看列表
	 * */
	@RequiresPermissions("md:areatimeliness:view")
	@RequestMapping("list")
	public String list(AreaTimeLiness areaTimeLiness, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		List<AreaTimeLiness> list = areaTimeLinessService.findList(areaTimeLiness);
	    if(list==null){
			list = Lists.newArrayList();
		}
		model.addAttribute("areaTimeLiness",areaTimeLiness);
		model.addAttribute("areaTimeLinessList",list);
		return "modules/md/areaTimeLinessList";
	}

	/**
	 * 区域时效启用/停用
	 * */
	@RequiresPermissions("md:areatimeliness:edit")
	@RequestMapping("enable")
	@ResponseBody
	public AjaxJsonEntity enable(@RequestBody List<AreaTimeLiness> areaTimeLinessList, HttpServletResponse response){
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);
        try {
        	if(areaTimeLinessList!=null && areaTimeLinessList.size()>0){
				areaTimeLinessService.saveBatch(areaTimeLinessList);
				jsonEntity.setSuccess(true);
			}else{
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("参数集合为空");
			}
		}catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 获取所有的省份
	 * */
	@ResponseBody
	@RequestMapping("ajax/areaTreeDate")
	public List<Map<String,Object>> areaTreeDate(){
	   List<Map<String,Object>> mapList = Lists.newArrayList();
       List<Area> list = areaService.findListByType(2);
       for(Area entity:list){
		   Map<String, Object> map = Maps.newHashMap();
		   map.put("id",entity.getId());
		   map.put("name",entity.getName());
		   map.put("pId",0);
		   mapList.add(map);
	   }
	   return mapList;
	}

}
