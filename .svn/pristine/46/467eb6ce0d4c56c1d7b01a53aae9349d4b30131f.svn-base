package com.wolfking.jeesite.modules.customer.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerAccountService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.entity.CustomerShop;
import com.wolfking.jeesite.modules.md.entity.viewModel.CustomerShopModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Controller
@RequestMapping(value="${adminPath}/customer/md/customerAccount")
public class CtCustomerAccountController extends BaseController {
    @Autowired
    private CtCustomerAccountService ctCustomerAccountService;

    @Autowired
    private SystemService systemService;

    @RequiresPermissions("md:customeraccount:view")
    @RequestMapping(value = "list")
    public String accountList(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            User cuser = UserUtils.getUser();
            if (cuser.isCustomer()) {
                CustomerAccountProfile profile = new CustomerAccountProfile();
                profile.setCustomer(cuser.getCustomerAccountProfile().getCustomer());
                profile.setOrderApproveFlag(cuser.getCustomerAccountProfile().getOrderApproveFlag());
                user.setCustomerAccountProfile(profile);
            } else if (cuser.isSalesPerson()) {
                user.setSalesId(cuser.getId());
            }
            user.setDelFlag(null);
            Page<User> page = ctCustomerAccountService.findCustomerAccountProfile(new Page<>(request, response), user);
            model.addAttribute("page", page);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("CtCustomerAccountController.accountList", e);
        }

        return "modules/customer/md/ctCustomerAccountList";
    }

    @RequiresPermissions("md:customeraccount:view")
    @RequestMapping(value = "form")
    public String accountForm(User user, Model model) {
        User currentUser = UserUtils.getUser();
        String customerShopNames = "";
        if (user.getId() != null && user.getId() != 0) {  //edit
            user = ctCustomerAccountService.getAccount(user.getId());
            List<CustomerShop> customerShopList = ctCustomerAccountService.getCustomerShopList(user.getId());
            if(customerShopList != null){
                customerShopNames = customerShopList.stream().map(CustomerShop::getName).collect(Collectors.joining("，"));
            }
        } else {
            if (StringUtils.isBlank(user.getName())) {
                //new
                CustomerAccountProfile profile = new CustomerAccountProfile(0);
                if (currentUser.isCustomer()) {
                    profile.setCustomer(currentUser.getCustomerAccountProfile().getCustomer());
                }
                profile.setOrderApproveFlag(0);
                user.setUserType(User.USER_TYPE_SUBCUSTOMER);
                user.setCustomerAccountProfile(profile);
            }
        }

        model.addAttribute("currentuser", currentUser);
        model.addAttribute("customerShopNames", customerShopNames);
        model.addAttribute("user", user);
        return "modules/customer/md/ctCustomerAccountForm";
    }

    @ResponseBody
    @RequiresPermissions("md:customeraccount:edit")
    @RequestMapping(value = "save")
    public AjaxJsonEntity saveAccount(User user, String oldLoginName, String newPassword, Model model, HttpServletRequest request) {
        // 如果新密码为空，则不更换密码
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        String customerShops = request.getParameter("customerShops");
        if (StringUtils.isNotBlank(newPassword)) {
            user.setPassword(SystemService.entryptPassword(newPassword));

        }
        if (!beanValidator(model, user)) {
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        String userId = null;
        if(user.getId() != null){
            userId = user.getId().toString();
        }
        if (!"true".equals(checkLoginName(oldLoginName, user.getLoginName(), userId))) {
            ajaxJsonEntity.setMessage("保存客户" + (user.getUserType().equals(User.USER_TYPE_CUSTOMER) ? "" : (user.getUserType().equals(User.USER_TYPE_SEARCH_CUSTOMER) ? "查询" : "子")) + "帐号'" + user.getLoginName() + "'失败，登录名已存在");
            ajaxJsonEntity.setSuccess(false);
            return ajaxJsonEntity;
        }
        if (user.getCustomerAccountProfile().getOrderApproveFlag() < 0) {
            user.getCustomerAccountProfile().setOrderApproveFlag(0);
        }

        // 角色数据有效性验证，过滤不在授权内的角色
        Role r;
        if (user.getUserType().equals(User.USER_TYPE_SEARCH_CUSTOMER)) {
            r = systemService.getRoleByEnname("SearchCustomer");
        } else {

            r = systemService.getRoleById(Objects.equals(user.getUserType(), User.USER_TYPE_CUSTOMER) ? 4L : 5L);
        }
        List<Role> roleList = systemService.findUserRoles(user);
        long count = roleList.stream().filter(i -> i.getId().equals(r.getId())).count();
        if(count == 0){
            roleList.add(r);
        }
        user.setRoleList(roleList);
        //save
        ctCustomerAccountService.save(user,customerShops);
        ajaxJsonEntity.setMessage("保存客户".concat(user.getUserType().equals(User.USER_TYPE_CUSTOMER) ? "" : "子").concat("账号人员'").concat(user.getLoginName()).concat("'成功"));
        return ajaxJsonEntity;
    }

    @ResponseBody
    @RequiresPermissions("md:customeraccount:edit")
    @RequestMapping(value = "delete")
    public AjaxJsonEntity delete(User user, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (UserUtils.getUser().getId().equals(user.getId())) {
            ajaxJsonEntity.setMessage("停用帐号失败, 不允许停用当前帐号");
        } else if (User.isAdmin(user.getId())) {
            ajaxJsonEntity.setMessage("停用帐号失败, 不允许停用超级管理员帐号");
        } else {
            try {
                systemService.deleteUser(user);
                ajaxJsonEntity.setMessage("停用帐号[ " + user.getLoginName() + " ]成功");
            } catch (Exception e) {
                ajaxJsonEntity.setMessage("停用帐号[ " + user.getLoginName() + " ]失败");
            }
        }

        return ajaxJsonEntity;
    }

    @ResponseBody
    @RequiresPermissions("md:customeraccount:edit")
    @RequestMapping(value = "enable")
    public AjaxJsonEntity enableAccount(User user, RedirectAttributes redirectAttributes) {
        systemService.enableUser(user);
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            ctCustomerAccountService.getCustomerAccountProfileByUserId(user.getId());
            ajaxJsonEntity.setMessage("启用[ " + user.getLoginName() + " ]成功");
        } catch (Exception e) {
            ajaxJsonEntity.setMessage("启用[ " + user.getLoginName() + " ]失败");
        }
        return ajaxJsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "checkLoginName")
    public String checkLoginName(String oldLoginName, String loginName, @RequestParam(required = false) String expectId) {
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        Long id = null;
        if (StringUtils.isNoneBlank(expectId)) {
            try {
                id = Long.valueOf(expectId);
            } catch (Exception e) {
            }
        }
        if (loginName != null && loginName.equals(oldLoginName)) {
            return "true";
        } else if (loginName != null
                && systemService.checkLoginName(id, loginName) == 0) {
            return "true";
        }

        return "客户账号人员登录名已存在";
    }

    /**
     * 检查客户帐号手机号是否注册
     * @param id    帐号id
     * @param phone    手机号
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkMasterPhone")
    public AjaxJsonEntity checkMasterPhone(String id, String phone) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (StringUtils.isBlank(phone)) {
            return result;
        }
        Long userId = systemService.checkLoginName(StringUtils.isBlank(id) ? null : Long.valueOf(id), phone);
        if (id.isEmpty()) {
            if (userId > 0) {
                result.setMessage("手机号已注册");
                result.setSuccess(false);
            }
        } else {
            if (userId != 0 && !userId.equals(Long.valueOf(id))) {
                result.setMessage("手机号已注册");
                result.setSuccess(false);
            }
        }
        return result;
    }

    /**
     * 重置密码
     * @param id  账户id
     * @return
     */
    @ResponseBody
    @RequiresPermissions("md:customeraccount:edit")
    @RequestMapping(value = "resetPassword")
    public AjaxJsonEntity resetPassword(String id, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (StringUtils.isBlank(id)) {
            result.setSuccess(false);
            result.setMessage("参数错误");
            return result;
        }
        Long lid = Long.valueOf(id);
        if (lid <= 0l) {
            result.setSuccess(false);
            result.setMessage("参数错误");
            return result;
        }

        User cuser = UserUtils.getUser();
        if (cuser == null || cuser.getId() == null) {
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        User user = ctCustomerAccountService.getAccount(lid);
        if (user == null || user.getId() == null) {
            result.setSuccess(false);
            result.setMessage("客户帐号不存在或已停用");
            return result;
        }
        if (user.getMobile().trim().length() < 6) {
            result.setSuccess(false);
            result.setMessage("手机号长度不到6，");
            return result;
        }
        try {
            user.preUpdate();
            String pwd = new String("");
            pwd = StringUtils.right(user.getMobile().trim(), 6);
            //手机号后6位
            user.setPassword(SystemService.entryptPassword(pwd));
            ctCustomerAccountService.resetPassword(user);
            //清除该帐号登录内容
            try {
                UserUtils.clearCache(user);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("更改密码时发生错误：" + e.getMessage());
        }
        return result;
    }


    @RequestMapping(value = "customerShop")
    public String customerShop(Long userId,Long customerId,String name, Model model) {

        List<CustomerShop> shops = B2BMDUtils.getCustomerKKLShopListNewForMD(customerId);
        List<CustomerShop> customerShopList = Lists.newArrayList();
        if(userId != null){
            customerShopList = ctCustomerAccountService.getCustomerShopList(userId);
        }

        model.addAttribute("name", name);
        model.addAttribute("userId", userId);
        model.addAttribute("customerId", customerId);
        model.addAttribute("shops", shops);
        model.addAttribute("customerShopList", customerShopList);

        return "modules/md/customerShop";
    }

    @RequestMapping(value = "saveCustomerShop")
    @ResponseBody
    public AjaxJsonEntity saveCustomerShop(CustomerShopModel customerShopModel) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            if(customerShopModel != null && customerShopModel.getUserId() != null){
                ctCustomerAccountService.saveCustomerShop(customerShopModel);
            }

            assert customerShopModel != null;
            String customerShopNames = ctCustomerAccountService.getCustomerShopNames(customerShopModel);
            ajaxJsonEntity.setMessage("保存成功");
            Map<String, Object> map = new HashMap<>();
            map.put("customerShopNames",customerShopNames);
            map.put("customerShops",customerShopModel.getCustomerShops());
            ajaxJsonEntity.setData(map);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存失败");
        }
        return ajaxJsonEntity;
    }
}
