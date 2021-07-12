package com.wolfking.jeesite.modules.sys.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserKeFuService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.sys.service.UserServicePointService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
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
 * 用户网点Controller
 *
 * @author ThinkGem
 * @version 2021-01-05
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/userServicePoint")
@Slf4j
public class UserServicePointController extends BaseController {

    @Autowired
    private UserServicePointService userServicePointService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private UserRegionService userRegionService;
    @Autowired
    private GsonRedisSerializer gsonRedisSerializer;

    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = {"list"})
    public String list(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        user.setUserType(User.USER_TYPE_ENGINEER);
        user.setSubFlag(2);
        Page<User> page = userServicePointService.findUser(new Page<User>(request, response), user);
        model.addAttribute("page", page);
        model.addAttribute("userType", user.getUserType());
        return "modules/sys/userServicePointList";
    }

    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = "form")
    public String form(Long userId, Model model) {
        User user = new User();
        user.setUserType(User.USER_TYPE_ENGINEER);
        user.setSubFlag(2);
        if (userId != null) {
            user = UserUtils.get(userId);
            if (user != null) {
                HashMap<String, Object> loginInfo = systemService.getLoginInfo(userId);
                if (loginInfo != null && loginInfo.size() == 2) {
                    user.setLoginIp(loginInfo.get("login_ip").toString());
                    if (loginInfo.get("login_date") != null) {
                        user.setLoginDate((Date) loginInfo.get("login_date"));
                    }
                }
                user.setProductCategoryIds(systemService.getProductCategoryIds(user.getId()));

                List<String> userRegionNames = userKeFuService.getUserRegionNameList(userId,user.getSubFlag());
                model.addAttribute("userRegionNames", userRegionNames);
            }

        }
        List<Role> roles = userKeFuService.getUserRolesByName("网点");//获取网点角色
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        return "modules/sys/userServicePointForm";
    }

    /**
     * 获取已设置的权限区域名称
     * @param
     * @return
     */
    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = "/selectUserRegionNames", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity selectUserRegionNames(Long userId, String userRegionJson, Integer subFlag){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        List<String> data = Lists.newArrayList();
        int type = 0;
        try {
            if(userId != null && userId > 0){
                List<UserRegion> userRegionsFromDB = userRegionService.getUserRegionsFromDB(userId);

                data = userKeFuService.selectUserRegionNames(userRegionsFromDB,type);
            }else {
                StringBuilder json = new StringBuilder(userRegionJson);
                List<UserRegion> list = Lists.newArrayList();
                if(json.length()>0){
                    json = new StringBuilder(json.toString().replace("&quot;","\""));
                    UserRegionViewModel userRegion = (UserRegionViewModel) gsonRedisSerializer.fromJson(json.toString(),UserRegionViewModel.class);
                    if(userRegion != null && userRegion.getRegionList() != null){
                        list = userKeFuService.contrastUserRegion(userRegion.getRegionList(),subFlag,0L);
                    }
                }
                data = userKeFuService.selectUserRegionNames(list,type);
            }

            ajaxJsonEntity.setData(data);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
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

    /**
     * 验证登录名是否有效
     *
     * @param oldLoginName
     * @param loginName
     * @return
     */
    @ResponseBody
    @RequiresPermissions("sys:user:edit")
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

    @RequiresPermissions("sys:user:view")
    @RequestMapping(value = {"accountList"})
    public String accountList(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        user.setUserType(User.USER_TYPE_ENGINEER);
        user.setSubFlag(2);
        Page<User> page = userServicePointService.findUser(new Page<User>(request, response), user);
        model.addAttribute("page", page);
        model.addAttribute("userType", user.getUserType());
        return "modules/sys/userServicePointAccountList";
    }
}
