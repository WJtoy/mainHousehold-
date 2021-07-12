package com.wolfking.jeesite.modules.sys.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.md.dto.MDRegionPermissionDto;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.*;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 用户客服Controller
 *
 * @author ThinkGem
 * @version 2020-12-10
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/userKeFu")
@Slf4j
public class UserKeFuController extends BaseController {
    @Autowired
    private SystemService systemService;

    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private MSSysOfficeService msSysOfficeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OfficeService officeService;

    @Autowired
    private UserRegionService userRegionService;

    @Autowired
    private GsonRedisSerializer gsonRedisSerializer;

    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = {"list"})
    public String list(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        user.setUserType(User.USER_TYPE_SERVICE);
        if(user.getStatusFlag() == null){
            user.setStatusFlag(0);
        }
        Page<User> page = userKeFuService.findUser(new Page<User>(request, response), user);
        List<Office> offices = userKeFuService.orderByOffice("客服", User.USER_TYPE_SERVICE);//获取客服部门\
        model.addAttribute("page", page);
        model.addAttribute("offices", offices);
        model.addAttribute("userType", user.getUserType());
        model.addAttribute("user", user);
        return "modules/sys/userKeFuList";
    }


    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = "form")
    public String form(Long userId, Model model) {

        java.util.Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        User user = new User();
        user.setUserType(User.USER_TYPE_SERVICE);
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
                if (user.getId() != null && (user.getCustomerList() == null || user.getCustomerList().size() == 0)) {
                    user.setCustomerList(customerService.findListByUserIdOrCustomerId(paramMap));
                    if (user.getUserType().intValue() == User.USER_TYPE_SERVICE) {
                        List<Long> customerIds = systemService.findCustomerIdList(paramMap);
                        user.setCustomerIds(Sets.newHashSet(customerIds));
                    }
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
        List<Office> offices = userKeFuService.orderByOffice("客服", User.USER_TYPE_SERVICE);//获取客服部门
        List<Role> roles = userKeFuService.getUserRolesByNameNew(38L);//获取客服角色

        model.addAttribute("user", user);
        model.addAttribute("offices", offices);
        model.addAttribute("roles", roles);
        return "modules/sys/userKeFuForm";
    }


    @RequiresPermissions("sys:user:edit")
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity save(User user, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
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
        if (!"true".equals(checkLoginName(user.getOldLoginName(), user.getLoginName()))) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存用户'" + user.getLoginName() + "'失败，登录名已存在");
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
            userKeFuService.saveUser(user); // 保存用户信息
            ajaxJsonEntity.setMessage("用户信息已保存");
        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存用户失败,失败原因:" + ex.getMessage());
        }
        // 清除用户缓存
        try {
            UserUtils.clearCache(user);
        } catch (Exception e) {
            log.error("[UserController.save]clear cache,userId:{},loginName:{}", user.getId(), user.getLoginName(), e);
        }
        return ajaxJsonEntity;
    }

    @RequiresPermissions("sys:user:edit")
    @RequestMapping(value = "userRegion")
    public String userRegion(Long userId, Integer subFlag, Model model) {
        List<MDRegionPermissionDto> userRegionAreaList = userKeFuService.getUserRegionAreaList(subFlag);
        model.addAttribute("userRegionAreaList", userRegionAreaList);
        model.addAttribute("userId", userId);
        model.addAttribute("subFlag", subFlag);
        return "modules/sys/userRegion";
    }


    /**
     * 获取已设置的权限区域
     *
     * @param
     * @return
     */
    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = "/getUserRegion", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity findRegionPermission(Long userId) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<UserRegion> data;
        try {
            data = userRegionService.getUserRegionsFromDB(userId);
            ajaxJsonEntity.setData(data);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 获取已设置的权限区域名称
     *
     * @param
     * @return
     */
    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = "/selectUserRegionNames", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity selectUserRegionNames(Long userId, String userRegionJson, Integer subFlag) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<String> data = Lists.newArrayList();
        int type = 0;
        try {
            if (userId != null && userId > 0) {
                List<UserRegion> userRegionsFromDB = userRegionService.getUserRegionsFromDB(userId);
                if(subFlag == 3){
                    type = 1;
                }
                if(subFlag == 4){
                    type = 3;
                }
                data = userKeFuService.selectUserRegionNames(userRegionsFromDB, type);
            } else {
                StringBuilder json = new StringBuilder(userRegionJson);
                List<UserRegion> list = Lists.newArrayList();
                if (json.length() > 0) {
                    json = new StringBuilder(json.toString().replace("&quot;", "\""));
                    UserRegionViewModel userRegion = (UserRegionViewModel) gsonRedisSerializer.fromJson(json.toString(), UserRegionViewModel.class);
                    if (userRegion != null && userRegion.getRegionList() != null) {
                        list = userKeFuService.contrastUserRegion(userRegion.getRegionList(), subFlag, 0L);
                    }
                }
                if(subFlag == 3){
                    type = 1;
                }
                if(subFlag == 4){
                    type = 3;
                }
                data = userKeFuService.selectUserRegionNames(list, type);
            }

            ajaxJsonEntity.setData(data);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }


    @RequiresPermissions("sys:user:edit")
    @RequestMapping(value = "saveUserRegion")
    @ResponseBody
    public AjaxJsonEntity saveUserRegion(UserRegionViewModel userRegion) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        boolean saveUserRegion = userKeFuService.saveUserRegion(userRegion);

        if (Global.isDemoMode()) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("演示模式，不允许操作！");
        }
        if (saveUserRegion) {
            ajaxJsonEntity.setMessage("保存成功");
            ajaxJsonEntity.setData(userRegion);
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存失败");
        }
        return ajaxJsonEntity;
    }


    @RequiresPermissions("sys:user:edit")
    @RequestMapping(value = "delete")
    @ResponseBody
    public AjaxJsonEntity delete(User user, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (Global.isDemoMode()) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("演示模式，不允许操作！");
        }
        if (UserUtils.getUser().getId().equals(user.getId())) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除用户失败, 不允许删除当前用户");
        } else if (User.isAdmin(user.getId())) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除用户失败, 不允许删除超级管理员用户");
        } else {
            userKeFuService.deleteUser(user);
            ajaxJsonEntity.setMessage("删除用户成功");
        }
        return ajaxJsonEntity;
    }

    /**
     * 验证登录名是否有效
     *
     * @param oldLoginName
     * @param loginName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkLoginName")
    public String checkLoginName(String oldLoginName, String loginName) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if (loginName != null && loginName.equals(oldLoginName)) {
            return "true";
        } else if (loginName != null && systemService.getUserByLoginName(loginName) == null) {
            return "true";
        }
        return "登录名已存在。";
    }
}
