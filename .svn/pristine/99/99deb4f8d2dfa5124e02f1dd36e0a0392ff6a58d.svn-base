package com.wolfking.jeesite.modules.sys.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.*;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 用户财务Controller
 *
 * @author ThinkGem
 * @version 2021-3-5
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/userFinance")
@Slf4j
public class UserFinanceController extends BaseController {
    @Autowired
    private SystemService systemService;
    @Autowired
    private UserSalesAccountService userSalesAccountService;
    @Autowired
    private UserFinanceService userFinanceService;
    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private OfficeService officeService;


    @RequiresPermissions("sys:userFinance:view")
    @RequestMapping(value = {"list", ""})
    public String list(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        user.setUserType(User.USER_TYPE_FINANCE);
        Page<User> page = userFinanceService.findUser(new Page<User>(request, response), user);
        model.addAttribute("page", page);
        model.addAttribute("userType", user.getUserType());
        return "modules/sys/userFinanceList";
    }

    @RequiresPermissions("sys:userFinance:view")
    @RequestMapping(value = "form")
    public String form(User user, Model model) {

        List<Role> allRoles = Lists.newArrayList();
        if (user.getId() != null) {
            user = UserUtils.get(user.getId());
            if(user != null){
                HashMap<String, Object> loginInfo = systemService.getLoginInfo(user.getId());
                if (loginInfo != null && loginInfo.size() == 2) {
                    user.setLoginIp(loginInfo.get("login_ip").toString());
                    if (loginInfo.get("login_date") != null) {
                        user.setLoginDate((Date) loginInfo.get("login_date"));
                    }
                }
            }
        } else {
            user.setUserType(User.USER_TYPE_FINANCE);
        }
        List<Office> offices = userKeFuService.orderByOffice("财务", User.USER_TYPE_SERVICE);//获取客服部门
        if (offices != null && offices.size() > 0) {
            for (Office office : offices) {
                allRoles.addAll(userSalesAccountService.getUserRolesByOfficeId(office.getId()));
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("offices", offices);
        return "modules/sys/userFinanceFrom";
    }

    @RequiresPermissions("sys:userFinance:edit")
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity save(User user, HttpServletRequest request, Model model) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (Global.isDemoMode()) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("演示模式，不允许操作！");
        }
        String officeId = request.getParameter("office.id");
        if (StringUtils.isBlank(officeId)) {
            user.setOffice(new Office(0L));
        } else {
            user.setOffice(officeService.get(Long.parseLong(officeId))); //add on 2017-4-13
        }
        // 如果新密码为空，则不更换密码
        if (StringUtils.isNotBlank(user.getNewPassword())) {
            user.setPassword(SystemService.entryptPassword(user.getNewPassword()));
        }
        if (!beanValidator(model, user)) {
            ajaxJsonEntity.setSuccess(false);
        }
        // 角色数据有效性验证，过滤不在授权内的角色
        List<Role> roleList = Lists.newArrayList();
        List<Long> roleIdList = user.getRoleIdList();
        for (Role r : systemService.findAllRole()) {
            if (roleIdList.contains(r.getId())) {
                roleList.add(r);
            }
        }
        user.setRoleList(roleList);

        try {
            userFinanceService.saveUser(user); // 保存用户信息
            ajaxJsonEntity.setMessage("用户信息已保存");
        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存用户信息失败,失败原因:" + ex.getMessage());
        }
        // 清除用户缓存
        try {
            UserUtils.clearCache(user);
        } catch (Exception e) {
            log.error("[UserFinanceController.save]clear cache,userId:{},loginName:{}", user.getId(), user.getLoginName(), e);
        }
        return ajaxJsonEntity;
    }
}
