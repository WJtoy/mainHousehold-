package com.wolfking.jeesite.modules.sys.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserRegionViewModel;
import com.wolfking.jeesite.modules.sys.service.*;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
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
 * 用户客户投诉Controller
 *
 * @author ThinkGem
 * @version 2021-3-16
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/customerComplaint")
@Slf4j
public class UserCustomerComplaintController extends BaseController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private MSSysOfficeService msSysOfficeService;

    @Autowired
    private UserSalesAccountService userSalesAccountService;

    @Autowired
    private GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private OfficeService officeService;

    @Autowired
    private UserServicePointExploitService userServicePointExploitService;

    @Autowired
    private UserCustomerComplaintService userCustomerComplaintService;

    @RequiresPermissions("sys:customerComplaint:view")
    @RequestMapping(value = {"list", ""})
    public String list(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        user.setUserType(User.USER_TYPE_INNER);
        Page<User> page = userCustomerComplaintService.findUser(new Page<User>(request, response), user);
        model.addAttribute("page", page);
        model.addAttribute("userType", user.getUserType());
        return "modules/sys/userCustomerComplaintList";
    }


    @RequiresPermissions("sys:customerComplaint:view")
    @RequestMapping(value = "form")
    public String form(Long userId, Model model) {

        java.util.Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        User user = new User();
        user.setUserType(User.USER_TYPE_INNER);
        if (userId != null) {
            user = UserUtils.get(userId);
            if (user != null) {
                user.setManagerFlag(userKeFuService.getManagerFlag(user.getId()));
                HashMap<String, Object> loginInfo = systemService.getLoginInfo(userId);
                if (loginInfo != null && loginInfo.size() == 2) {
                    user.setLoginIp(loginInfo.get("login_ip").toString());
                    if (loginInfo.get("login_date") != null) {
                        user.setLoginDate((Date) loginInfo.get("login_date"));
                    }
                }
                user.setProductCategoryIds(systemService.getProductCategoryIds(user.getId()));
                if (user.getOffice() == null) {
                    user.setOffice(msSysOfficeService.getSpecColumnById(user.getId()));
                }
                int type = 0;
                if (user.getSubFlag() == 4) {
                    type = 3;
                }
                if (user.getSubFlag() == 3) {
                    type = 1;
                }
                List<String> userRegionNames = userKeFuService.getUserRegionNameList(userId, type);
                model.addAttribute("userRegionNames", userRegionNames);
            }
        }
        List<Role> allRoles = Lists.newArrayList();
        List<Office> offices = userKeFuService.orderByOffice("客诉", User.USER_TYPE_SERVICE);//获取网点开发部门
        if (offices != null && offices.size() > 0) {
            for (Office office : offices) {
                allRoles.addAll(userSalesAccountService.getUserRolesByOfficeId(office.getId()));
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("offices", offices);
        model.addAttribute("allRoles", allRoles);
        return "modules/sys/userCustomerComplaintForm";
    }


    @RequiresPermissions("sys:customerComplaint:edit")
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

        // regions

        StringBuilder json = new StringBuilder(user.getUserRegionJson());

        if (json.length() > 0) {
            json = new StringBuilder(json.toString().replace("&quot;", "\""));
            UserRegionViewModel userRegion = (UserRegionViewModel) gsonRedisSerializer.fromJson(json.toString(), UserRegionViewModel.class);
            user.setUserRegion(userRegion);
        }
        try {
            userServicePointExploitService.saveUser(user); // 保存用户信息
            ajaxJsonEntity.setMessage("用户信息已保存");
        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存用户失败,失败原因:" + ex.getMessage());
        }
        // 清除用户缓存
        try {
            UserUtils.clearCache(user);
        } catch (Exception e) {
            log.error("[UserCustomerComplaintController.save]clear cache,userId:{},loginName:{}", user.getId(), user.getLoginName(), e);
        }
        return ajaxJsonEntity;
    }
}
