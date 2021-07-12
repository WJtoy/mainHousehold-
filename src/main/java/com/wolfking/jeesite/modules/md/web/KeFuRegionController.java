package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.KeFuRegionService;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.UserKeFuService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 客服区域Controller
 *
 * @author ThinkGem
 * @version 2020-12-18
 */
@Controller
@RequestMapping(value = "${adminPath}/md/keFuRegion")
@Slf4j
public class KeFuRegionController extends BaseController {
    @Autowired
    private KeFuRegionService keFuRegionService;
    @Autowired
    private UserKeFuService userKeFuService;


    @RequiresPermissions("md:keFuRegion:view")
    @RequestMapping(value = "list")
    public String list(User user, Model model) {
        User u = UserUtils.getUser();
        for(Role role : u.getRoleList()){
            if(role.getId() == 1){
                u.setOffice(new Office(0L));
            }
        }
        List<Office> officeList = Lists.newArrayList();
        List<User> keFuAreaList = Lists.newArrayList();
        if(u.getOffice() != null && u.getOfficeId() != null){
            officeList = userKeFuService.orderByOfficeNew("客服",u.getOfficeId(),User.USER_TYPE_SERVICE);//获取客服部门

            if(user.getStatusFlag() == null){
                user.setStatusFlag(0);
            }
            if(user.getAppLoged() == 1){
                user.setUserType(User.USER_TYPE_SERVICE);
                keFuAreaList = keFuRegionService.findUser(user,u.getOfficeId());
            }
        }
        model.addAttribute("officeList", officeList);
        model.addAttribute("list", keFuAreaList);
        model.addAttribute("user", user);
        return "modules/md/keFuRegionList";
    }

}
