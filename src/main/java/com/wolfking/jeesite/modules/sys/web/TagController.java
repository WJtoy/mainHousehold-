/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wolfking.jeesite.common.web.BaseController;

/**
 * 标签Controller
 * @author ThinkGem
 * @version 2013-3-23
 */
@Controller
@RequestMapping(value = "${adminPath}/tag")
public class TagController extends BaseController {
	
	/**
	 * 树结构选择标签（treeselect.tag）
	 */
	@RequiresPermissions("user")
	@RequestMapping(value = "treeselect")
	public String treeselect(HttpServletRequest request, Model model) {
		model.addAttribute("url", request.getParameter("url")); 	// 树结构数据URL
		model.addAttribute("extId", request.getParameter("extId")); // 排除的编号ID
		model.addAttribute("checked", request.getParameter("checked")); // 是否可复选
		model.addAttribute("selectIds", request.getParameter("selectIds")); // 指定默认选中的ID
		model.addAttribute("isAll", request.getParameter("isAll")); 	// 是否读取全部数据，不进行权限过滤
		model.addAttribute("module", request.getParameter("module"));	// 过滤栏目模型（仅针对CMS的Category树）
		return "modules/sys/tagTreeselect";
	}

	/**
	 * 树结构选择标签（treeselectarea.tag）
	 * 对已选择街道特殊处理，因街道层级是动态加载
	 * 1.先根据selectParentId选中指定节点（区）
	 * 2.再装载街道数据
	 * 3.根据selectIds的值，选中街道
	 */
	@RequiresPermissions("user")
	@RequestMapping(value = "treeselectarea")
	public String treeselectarea(HttpServletRequest request, Model model) {
		model.addAttribute("url", request.getParameter("url")); 	// 树结构数据URL
		model.addAttribute("extId", request.getParameter("extId")); // 排除的编号ID
		model.addAttribute("checked", request.getParameter("checked")); // 是否可复选
		model.addAttribute("selectIds", request.getParameter("selectIds")); // 指定默认选中的ID
		model.addAttribute("selectParentId", request.getParameter("selectParentId")); // 选择街道时，指定区县id
		model.addAttribute("isAll", request.getParameter("isAll")); 	// 是否读取全部数据，不进行权限过滤
		model.addAttribute("module", request.getParameter("module"));	// 过滤栏目模型（仅针对CMS的Category树）
		model.addAttribute("areaLevel", request.getParameter("areaLevel"));	// 已选择区域类型 0-省 1-市 2-区 3-街道
		return "modules/sys/tagTreeselectarea";
	}


	/**
	 * 因区域改成微服务调用后，原来一次加载所有的省,市,区/县不再适用(需要动态加载区/县)
	 * 树结构选择标签（treeselectareanew.tag）
	 * 对已选择街道特殊处理，因街道层级是动态加载
	 * 1.先根据selectParentId选中指定节点（市）
	 * 2.再装载区县数据
	 * 3.根据selectIds的值，选中区/县
	 */
	@RequiresPermissions("user")
	@RequestMapping(value = "treeselectareanew")
	public String treeselectareanew(HttpServletRequest request, Model model) {
		model.addAttribute("url", request.getParameter("url")); 	// 树结构数据URL
		model.addAttribute("extId", request.getParameter("extId")); // 排除的编号ID
		model.addAttribute("checked", request.getParameter("checked")); // 是否可复选
		model.addAttribute("selectIds", request.getParameter("selectIds")); // 指定默认选中的ID
		model.addAttribute("selectParentId", request.getParameter("selectParentId")); // 选择街道时，指定区县id
		model.addAttribute("isAll", request.getParameter("isAll")); 	// 是否读取全部数据，不进行权限过滤
		model.addAttribute("module", request.getParameter("module"));	// 过滤栏目模型（仅针对CMS的Category树）
		model.addAttribute("areaLevel", request.getParameter("areaLevel"));	// 已选择区域类型 0-省 1-市 2-区 3-街道
		model.addAttribute("loadFourLevel", request.getParameter("loadFourLevel"));	// 是否加载4级区域
		model.addAttribute("nameLevel", request.getParameter("nameLevel"));	        // 是否加载4级区域
		return "modules/sys/tagTreeselectareanew";
	}

	/**
	 * 图标选择标签（iconselect.tag）
	 */
	@RequiresPermissions("user")
	@RequestMapping(value = "iconselect")
	public String iconselect(HttpServletRequest request, Model model) {
		model.addAttribute("value", request.getParameter("value"));
		return "modules/sys/tagIconselect";
	}
	
}
