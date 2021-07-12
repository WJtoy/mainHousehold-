/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.servicepoint.common.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JSP Tag标记控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/common/tag")
public class ServicePointTagController extends BaseController {

    @Autowired
    private SpServicePointService servicePointService;


    /**
     * 派单安维人员选择器
     */
    @RequestMapping(value = "engineerSelectorForPlan")
    public String select(Engineer engineer, HttpServletRequest request, HttpServletResponse response, Model model) {
        engineer.setMasterFlag(null);
        engineer.setAppFlag(null);
        Page<Engineer> page = servicePointService.findPage(new Page<>(request, response), engineer);
        model.addAttribute("page", page);
        model.addAttribute("engineer", engineer);
        return "modules/servicePoint/common/tag/engineerSelectorForPlan";
    }


}
