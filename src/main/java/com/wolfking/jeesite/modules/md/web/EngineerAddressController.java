/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.web;

import com.kkl.kklplus.entity.md.MDEngineerAddress;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerAddressService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * 安维人员Controller
 * @author Ryan Lu
 * @version 2017-04-26
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/engineerAddress")
public class EngineerAddressController extends BaseController {

	@Autowired
	GsonRedisSerializer gsonRedisSerializer;

	@Autowired
	private MSEngineerAddressService engineerAddressService;

	@Autowired
	private AreaService areaService;


	@RequiresPermissions(value = {"md:engineer:view","md:engineer:edit"},logical = Logical.OR)
	@RequestMapping(value = "addressForm")
	public String addressForm(@RequestParam(required=false) Long id, MDEngineerAddress engineerAddress, Model model) {
		User user = UserUtils.getUser();
		Long areaId = null;
        if (id != null){
            engineerAddress = engineerAddressService.getById(id);
        }else{
            engineerAddress = new MDEngineerAddress();
        }

		if (engineerAddress != null) {
			areaId = Optional.ofNullable(engineerAddress.getAreaId()).orElse(null);
		}
		Area area = areaId == null ? null : areaService.getFromCache(areaId);

		if (area != null) {
			model.addAttribute("engineerAddressArea", area);
		}
		model.addAttribute("engineerAddress", engineerAddress);
		model.addAttribute("userType", user.getUserType());
		return "modules/md/engineerAddressForm";
	}


	@RequiresPermissions("md:engineer:edit")
	@RequestMapping(value = "saveEngineerAddress")
	@ResponseBody
	public AjaxJsonEntity saveEngineerAddress(MDEngineerAddress engineerAddress, Model model, RedirectAttributes redirectAttributes) {
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if (!beanValidator(model, engineerAddress)){
			ajaxJsonEntity.setSuccess(false);
			return ajaxJsonEntity;
		}
		engineerAddress.setDelFlag(ServicePoint.DEL_FLAG_NORMAL);
		try {
			engineerAddressService.save(engineerAddress);
			ajaxJsonEntity.setData(engineerAddress.getId());
			ajaxJsonEntity.setMessage("保存收货地址成功");
			return ajaxJsonEntity;

		}catch (Exception ex){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(ex.getMessage());
			return ajaxJsonEntity;
		}

	}

}
