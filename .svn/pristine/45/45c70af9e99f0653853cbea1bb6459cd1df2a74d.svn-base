/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.sys.SysArea;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域Controller
 * @author ThinkGem
 * @version 2013-5-15
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/area")
public class AreaController extends BaseController {

	@Autowired
	private AreaService areaService;

	/*
	// mark on 2020-7-31
	@Autowired
	private RedisUtils redisUtils;
	*/

	@Autowired
	private MSSysAreaService msSysAreaService;


	/**
	 * 控制器前置处理
	 * 增加过滤，只有增/删/改时按id查询
	 */
	@ModelAttribute("area")
	public Area get(@RequestParam(required = false) Long id,HttpServletRequest request) {
		if (id != null) {
			String uri =request.getRequestURI();
			//编辑时才从数据库读取
			if(uri.indexOf("/sys/area/form") >= 0 || uri.indexOf("/sys/area/save") >= 0
                    || uri.indexOf("/sys/area/delete") >= 0) {
				return areaService.get(id);
			}else{
				return new Area();
			}
		} else {
			return new Area();
		}
	}

	@RequiresPermissions("sys:area:view")
	@RequestMapping(value = {"list", ""})
	public String list(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		model.addAllAttributes(paramMap);
		Page<Area> page = areaService.find(new Page<Area>(request, response), paramMap);
		model.addAttribute("page", page);

		return "modules/sys/areaList";
	}

	@RequiresPermissions("sys:area:view")
	@RequestMapping(value = "form")
	public String form(Area area, Model model) {
		if (area.getId() == null && area.getParent() != null && area.getParent().getId() != null) {
			//Area parent = areaService.getFromCache(area.getParent().getId());
			Area parent = areaService.get(area.getParent().getId());
			area.setParent(parent);
		}
		model.addAttribute("area", area);
		return "modules/sys/areaForm";
	}

	@RequiresPermissions("sys:area:edit")
	@RequestMapping(value = "save")
	public String save(Area area, Model model, RedirectAttributes redirectAttributes,HttpServletRequest request) {
		if (Global.isDemoMode()) {
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + adminPath + "/sys/area";
		}
		if (!beanValidator(model, area)) {
			return form(area, model);
		}

		String strRenew = request.getParameter("renew");

		Area parentArea = area.getParent();
		if (parentArea != null) 	{
			parentArea = areaService.get(area.getParent().getId());
			if (area.getType() > 2) {
				area.setFullName(parentArea.getFullName() + " " + area.getName());
			} else {
				//省直接写省名称
				area.setFullName(area.getName());
			}
			String parentIds = parentArea.getParentIds()+parentArea.getId()+",";
			area.setParentIds(parentIds);
		} else {
			area.setFullName(area.getName());
		}

		areaService.save(area);
		addMessage(redirectAttributes, "保存区域'" + area.getName() + "'成功");
		String strParentName = parentArea!= null? parentArea.getName():"";
		try {
			strParentName = URLEncoder.encode(strParentName, "UTF-8");
		} catch(Exception e) {
		}

		if (StringUtils.isNotBlank(strRenew) && strRenew.equalsIgnoreCase("1")) {
			Area renewArea = new Area();
			renewArea.setParent(area.getParent());
			addMessage(model, "保存区域'" + area.getName() + "'成功.");
			return form(renewArea,model);
		} else {
			return "redirect:" + adminPath + "/sys/area/?repage&name=" + strParentName;
		}
	}

	@RequiresPermissions("sys:area:edit")
	@RequestMapping(value = "delete")
	public String delete(Area area, RedirectAttributes redirectAttributes) {
		if (Global.isDemoMode()) {
			addMessage(redirectAttributes, "演示模式，不允许操作！");
			return "redirect:" + adminPath + "/sys/area";
		}
		areaService.delete(area);
		addMessage(redirectAttributes, "删除区域成功");

		return "redirect:" + adminPath + "/sys/area/";
	}

	/**
	 * 返回区域（json格式）
	 *
	 * @param kefu  客服id
	 * @param extId 排除id
	 */
	@RequiresPermissions("user")
	@ResponseBody
	//@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required = false) Long kefu, @RequestParam(required = false) String extId, HttpServletResponse response) {

		// 本方法在sys_area微服务化后将不提供服务 // mark on 2020-11-13
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Area> list;
		if (kefu == null || kefu <= 0) {
			list = areaService.findAll(2);
		} else {
			list = areaService.getFullAreaListOfKefu(kefu);
			if (list == null || list.size() == 0) {
				list = areaService.findAll();
			}
		}
		for (int i = 0; i < list.size(); i++) {
			Area e = list.get(i);
			if (StringUtils.isBlank(extId) || (extId != null && !extId.equals(e.getId()) && e.getParentIds().indexOf("," + extId + ",") == -1)) {
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", e.getParentId());
				map.put("pIds", e.getParentIds());
				map.put("name", e.getName());
				map.put("type",e.getType());
				mapList.add(map);
			}
		}
		return mapList;
	}

	/**
	 * 返回区域（json格式）
	 *
	 * @param kefu  客服id
	 * @param extId 排除id
	 */
	@RequiresPermissions("user")
	@ResponseBody
	@RequestMapping(value = "treeDataNew")
	public List<Map<String, Object>> treeDataNew(@RequestParam(required = false) Long kefu, @RequestParam(required = false) String extId, HttpServletResponse response) {
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Area> list;
		if (kefu == null || kefu <= 0) {
			list = areaService.findProvinceAndCityListFromCache();
		} else {
			list = areaService.getFullAreaListOfKefu(kefu);
			if (list == null || list.size() == 0) {
				list = areaService.findProvinceAndCityListFromCache();
			}
		}
		for (int i = 0; i < list.size(); i++) {
			Area e = list.get(i);
			if (StringUtils.isBlank(extId) || (extId != null && !extId.equals(e.getId()) && e.getParentIds().indexOf("," + extId + ",") == -1)) {
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", e.getParentId());
				map.put("pIds", e.getParentIds());
				map.put("name", e.getName());
				map.put("type",e.getType());
				mapList.add(map);
			}
		}
		return mapList;
	}

	@ResponseBody
	@RequestMapping(value = {"clearcache"}, method = RequestMethod.POST)
	public AjaxJsonEntity clearcache(HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try {
			// Boolean result = UserUtils.loadAreas();    // mark on 2020-7-31
			// jsonEntity.setSuccess(result);             // mark on 2020-7-31
			jsonEntity.setSuccess(true);   // add on 2020-7-31
		} catch (Exception e) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}

	/**
	 * 返回省份列表 For区域联动选择 返回字段:id,pid,name
	 */
	@ResponseBody
	@RequestMapping(value = "service/arealist")
	public List<Area> arealist(@RequestParam(required = false) int type, @RequestParam(required = false) Long id, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		if (id == null || id == 0) {
			//查询当前type的所有区域
			return areaService.findListByType(2);
		} else {
			//查询当前type,且属于id的下属区域
			return areaService.findListByParent(type, id);
		}
	}

	/**
	 * 返回(stautsFlag为0)区域列表 For 区域联动选择 返回字段:id,pid,name
	 */
	@ResponseBody
	@RequestMapping(value = "service/normalAreaList")
	public List<Area> normalAreaList(@RequestParam(required = false) int type, @RequestParam(required = false) Long id, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		if (id == null || id == 0) {
			//查询当前type的所有区域
			List<Area>  areaList = areaService.findListByType(2);
			if (!ObjectUtils.isEmpty(areaList)) {  // 排除停用区域
				areaList = areaList.stream().filter(area -> area.getStatusFlag().equals(Area.STATUS_ENABLE)).collect(Collectors.toList());
			}
			return areaList;
		} else {
			//查询当前type,且属于id的非停用下属区域
			return msSysAreaService.findNormalStatusListByTypeAndParentFromCache(type, id);
		}
	}

	/**
	 * 用于只知道街道/乡/镇，从下而上获取 当前街道的兄弟节点；获取父级节点的兄弟节点
	 * add on 2020-8-7
	 */
	@ResponseBody
	@RequestMapping(value = "service/twoGroupAreaList")
	public AjaxJsonEntity twoGroupAreaList(int type, Long parentId, int parentType, Long grandpaId, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try{
			Map<String,Object> map = Maps.newHashMap();
			List<Area> parentAreaList =  areaService.findListByParent(parentType, grandpaId);
			map.put("parent", parentAreaList);

			List<Area> currentAreaList =  areaService.findListByParent(type, parentId);
			map.put("current", currentAreaList);

			jsonEntity.setSuccess(true);
			jsonEntity.setData(map);
		}
		catch (Exception ex) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(ex.getMessage());
		}
		return jsonEntity;
	}

	/**
	 * 从缓存中获取任意两级区域
	*/
	@ResponseBody
	@RequestMapping(value = "service/threeLevelArea/{id}")
	public AjaxJsonEntity twoLevelArea(@PathVariable Long id, HttpServletResponse response) {
		// add on 2020-8-6  用于地址控件获取单个区域的父id，祖父id
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try{
			Area area = areaService.getThreeLevelAreaByIdFromCache(id);
			Map<String,Object> map = Maps.newHashMap();
			map.put("id", area.getId());
			map.put("parentId", area.getParent().getId());
			map.put("grandpaId", area.getParent().getParentId());
			jsonEntity.setSuccess(true);
			jsonEntity.setData(map);
		}
		catch (Exception ex) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(ex.getMessage());
		}
		return jsonEntity;
	}


	@ResponseBody
	@RequestMapping(value = "service/arealistbyids")
	public AjaxJsonEntity arealistbyids(String ids, HttpServletResponse response) {
		// 此函数主要用来获取同一父级id下的所有4级区域数据
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try{
			if (ids == null || ids.equals("")) {
				throw new Exception("传入id列表为空.");
			}
			String[] idsArr = ids.split(",");
			List<Long> areaParentIds = Lists.newArrayList();
			List<Area> areaList = Lists.newArrayList();
			for (int i = 0; i < idsArr.length; i++) {
				long lareaId = StringUtils.toLong(idsArr[i]);
				if (lareaId < 1) {
					continue;
				}
				Area area = areaService.getFromCache(lareaId);  // 从缓存中获取4级(乡镇)的数据
				if (area.getType().equals(Area.TYPE_VALUE_TOWN)) {
					Long parentId = area.getParentId();  // 获取父级id
					long idsCount = areaParentIds.stream().filter(r->r.longValue() == parentId).count();  // 检查父级列表中是否已包含此id
					if (idsCount < 1) {
						// 若没有找到则从缓存中获取数据
						areaList.addAll(areaService.findListByParent(Area.TYPE_VALUE_TOWN, parentId));
						areaParentIds.add(parentId);
					}
				}
			}
			jsonEntity.setSuccess(true);
			jsonEntity.setData(areaList);
		}
		catch (Exception ex) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(ex.getMessage().toString());
		}
		return jsonEntity;
	}

	@ResponseBody
	@RequestMapping(value = {"da"}, method = RequestMethod.POST)
	public AjaxJsonEntity decodeDistrictAddressGaode(@RequestParam("fullAddress") String fullAddress, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try {
			fullAddress = fullAddress.replaceAll("&nbsp;", "").replaceAll("&lt;","")
					.replaceAll("&gt;", "").replaceAll("&amp;","")
					.replaceAll("&quot;", "").replaceAll("&mdash;","");
			jsonEntity.setSuccess(false);
			String returnStr = "";
			StringBuffer sb = new StringBuffer("https://restapi.amap.com/v3/geocode/geo?key=37b238e75a3097696daf4a81498f1399&address=");
			sb.append(fullAddress.replaceAll(" ", "").replaceAll("#", "")
					.replaceAll("　", "").replaceAll("\\|", "")
					.replaceAll(",", "").replaceAll("\r","")
					.replaceAll("\n","").replaceAll("\t",""));
			URL url = new URL(sb.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Host", "restapi.amap.com");
			if (200 == connection.getResponseCode()) {
				//得到输入流
				InputStream is = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len;
				while (-1 != (len = is.read(buffer))) {
					baos.write(buffer, 0, len);
					baos.flush();
				}
				returnStr = baos.toString("utf-8");
				baos.close();
				is.close();
				connection.disconnect();
				if (returnStr != null && returnStr.length() > 0) {
					JSONObject jsonObj = JSONObject.fromObject(returnStr);
					if (jsonObj.get("info").toString().toUpperCase().equals("OK")) {
						JSONArray jsonArray = (JSONArray) jsonObj.get("geocodes");
						if (jsonArray != null && jsonArray.size() > 0) {
							JSONObject jsonObject = jsonArray.getJSONObject(0);
							String province = jsonObject.get("province").toString();
							String city = jsonObject.get("city").toString();
							String district = jsonObject.get("district").toString();

							String[]  arrayStr = msSysAreaService.decodeDistrictAddress(province, city, district);
							if (arrayStr != null && arrayStr.length >= 2) {
								String[] returnString = new String[8];
								String detailAddress = fullAddress;

								returnString[3] = "0";
								returnString[4] = StringUtils.getCellphone(fullAddress);
								returnString[5] = "";
								if (returnString[4].length() > 0){
									returnString[5] = StringUtils.getChineseName(fullAddress.replaceAll("姓名","")
											.replaceAll("收货人","").replaceAll("联系人","")
											.replaceAll("收件人",""));
								}
								if (fullAddress.indexOf(district) > 0) {
									detailAddress = fullAddress.substring(fullAddress.indexOf(district) + district.length()).trim();
									returnString[3] = "1";
								}
								returnString[0] = arrayStr[0];
								returnString[1] = arrayStr[1];
								returnString[2] = detailAddress;
								//经纬度
								String location = jsonObject.getString("location");
								if(StringUtils.isNotBlank(location)){
									String[] locations =  StringUtils.split(location,",");
									if(locations.length == 2){
										returnString[6] = locations[0];
										returnString[7] = locations[1];
									}
								}
								jsonEntity.setData(returnString);
								jsonEntity.setSuccess(true);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
		return jsonEntity;
	}


	@ResponseBody
	@RequestMapping(value = {"new_da"}, method = RequestMethod.POST)
	public AjaxJsonEntity newDecodeAddressGaode(@RequestParam("fullAddress") String fullAddress, HttpServletResponse response) {
		/**
		 * 解析省市区街道
		 */
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
		try {
			// String[] decodeAddressGaode = AreaUtils.decodeAddressGaode(fullAddress);  //mark on 2020-7-28
			String[] decodeAddressGaode = AreaUtils.decodeAddressGaodeFromMS(fullAddress);  //add on 2020-7-28

			jsonEntity.setData(decodeAddressGaode);
			jsonEntity.setSuccess(true);
		} catch (Exception e) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}

	@ResponseBody
	@RequestMapping(value = "service/area/{id}")
	public AjaxJsonEntity arealist(@PathVariable Long id, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity();
		User user = UserUtils.getUser();
		if (user == null || user.getId() == null) {
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}
		try {
			Area area = areaService.getFromCache(id);
			result.setSuccess(true);
			result.setData(area);
		} catch(Exception ex) {
			result.setSuccess(false);
			result.setMessage(ex.getMessage().toString());
		}
		return result;
	}

	@RequestMapping(value = "enable")
	public String enable(Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Area AreaFromDb = msSysAreaService.get(id);
		if(AreaFromDb == null){
			addMessage(redirectAttributes, "区域不存在");
			return "redirect:" + Global.getAdminPath() + "/sys/area/list?repage";
		}else if(AreaFromDb.getStatusFlag() == Area.STATUS_ENABLE){
			addMessage(redirectAttributes, "该区域已启用");
			return "redirect:" + Global.getAdminPath() + "/sys/area/list?repage";
		}
		Area area = new Area();
		area.setId(id);
		area.setUpdateBy(UserUtils.getUser());
		area.setUpdateDate(new java.util.Date());
		area.setStatusFlag(Area.STATUS_ENABLE);
		msSysAreaService.updateStatus(area);

		addMessage(redirectAttributes, "启用区域成功");
		return "redirect:" + Global.getAdminPath() + "/sys/area/list?repage";
	}

	@RequestMapping(value = "disable")
	public String disable(Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Area AreaFromDb = msSysAreaService.get(id);
		if(AreaFromDb == null){
			addMessage(redirectAttributes, "区域不存在");
			return "redirect:" + Global.getAdminPath() + "/sys/area/list?repage";
		}else if(AreaFromDb.getStatusFlag() == Area.STATUS_DISABLE){
			addMessage(redirectAttributes, "该区域已停用");
			return "redirect:" + Global.getAdminPath() + "/sys/area/list?repage";
		}
		Area area = new Area();
		area.setId(id);
		area.setUpdateBy(UserUtils.getUser());
		area.setUpdateDate(new java.util.Date());
		area.setStatusFlag(Area.STATUS_DISABLE);

		msSysAreaService.updateStatus(area);
		addMessage(redirectAttributes, "停用区域成功");

		return "redirect:" + Global.getAdminPath() + "/sys/area/list?repage";
	}
}