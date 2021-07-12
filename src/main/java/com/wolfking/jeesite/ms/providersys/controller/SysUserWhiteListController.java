package com.wolfking.jeesite.ms.providersys.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.entity.SysUserWhiteListView;
import com.wolfking.jeesite.ms.providersys.entity.mapper.SysUserWhiteListViewMapper;
import com.wolfking.jeesite.ms.providersys.service.MSSysUserWhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 白名单
 */
@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/provider/sys/userWhitelist")
public class SysUserWhiteListController extends BaseController {


    @Autowired
    private MSSysUserWhiteListService sysUserWhiteListService;

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询
     * @param sysUserWhiteList
     * @return
     */
    @RequiresPermissions("sys:whitelist:view")
    @RequestMapping(value = {"findList", ""})
    public String findList(SysUserWhiteList sysUserWhiteList, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<SysUserWhiteListView> sysUserWhiteListPage = new Page<>(request, response);
        Page<SysUserWhiteListView> page= sysUserWhiteListService.findListForPage(sysUserWhiteListPage,sysUserWhiteList);
        model.addAttribute("sysUserWhiteList",sysUserWhiteList);
        model.addAttribute("page", page);
        return "modules/providersys/sysUserWhitelistList";
    }

    /**
     * 添加白名单
     * @return
     */
    @RequiresPermissions("sys:whitelist:edit")
    @RequestMapping(value = "addForm")
    public String addForm(SysUserWhiteList sysUserWhiteList,HttpServletRequest request,Model model){
        Page<User> page = systemService.findUser(new Page<User>(1, 5000), new User());
        List<User> users = page.getList();
        List<Long> whitelistUserIds = sysUserWhiteListService.findAllIdList();
        users = users.stream()
                .filter(item -> !whitelistUserIds.contains(item.getId()))
                .collect(Collectors.toList());
        model.addAttribute("userList", users);
        model.addAttribute("sysUserWhiteList",sysUserWhiteList);
        return "modules/providersys/sysUserWhitelistAddForm";
    }

    /**
     * 添加白名单
     * @return
     */
    @RequiresPermissions("sys:whitelist:edit")
    @RequestMapping(value = "addWhitelist")
    @ResponseBody
    public AjaxJsonEntity addWhitelist(SysUserWhiteList sysUserWhiteList,HttpServletRequest request){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("超时,请重新登录");
            return ajaxJsonEntity;
        }
        String userIds = request.getParameter("userIds");
        if(org.apache.commons.lang3.StringUtils.isBlank(userIds)){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("请至少选择一名用户");
            return ajaxJsonEntity;
        }
        if(sysUserWhiteList.getEndDate().getTime()<System.currentTimeMillis()){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("到期日期不能小于当前时间");
            return ajaxJsonEntity;
        }
        try {
            String[] userIdsArray = userIds.split(",");
            List<SysUserWhiteList> sysUserWhiteListList = Lists.newArrayList();
            SysUserWhiteList entity;
            sysUserWhiteList.setEndDate(DateUtils.getDateEnd(sysUserWhiteList.getEndDate()));
            for(int i=0;i<userIdsArray.length;i++){
                entity = new SysUserWhiteList();
                entity.setEndDate(sysUserWhiteList.getEndDate());
                entity.setUserId(Long.valueOf(userIdsArray[i]));
                entity.preInsert();
                entity.setCreateById(user.getId());
                entity.setUpdateById(user.getId());
                sysUserWhiteListList.add(entity);

            }
            sysUserWhiteListService.batchInsert(sysUserWhiteListList);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    @ResponseBody
    @RequestMapping(value = "findUserTree")
    public List<Map<String, Object>> findUserTree(Long officeId, HttpServletResponse response) {
        List<Map<String, Object>> mapList = Lists.newArrayList();
        User user = new User();
        user.setOffice(new Office(officeId));
        Page<User> page = systemService.findUser(new Page<User>(1, -1), user);
        List<User> users = page.getList();
        List<Long> whitelistUserIds = sysUserWhiteListService.findAllIdList();
        users = users.stream()
                .filter(item -> !whitelistUserIds.contains(item.getId()))
                .collect(Collectors.toList());
        for (User e : users) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("id", e.getId());
            map.put("pId", 0);
            map.put("name", e.getName());
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 修改白名单
     * @return
     */
    @RequiresPermissions("sys:whitelist:edit")
    @RequestMapping(value = "editForm")
    public String editForm(SysUserWhiteList sysUserWhiteList,HttpServletRequest request,Model model){
        sysUserWhiteList = sysUserWhiteListService.getById(sysUserWhiteList.getId());
        model.addAttribute("canSave",true);
        if(sysUserWhiteList==null){
            addMessage(model,"错误:获取白名单用户失败,数据不见了");
            model.addAttribute("canSave",false);
        }
        SysUserWhiteListView sysUserWhiteListView = Mappers.getMapper(SysUserWhiteListViewMapper.class).toViewModel(sysUserWhiteList);
        model.addAttribute("sysUserWhiteList",sysUserWhiteListView);
        return "modules/providersys/sysUserWhitelistEditForm";
    }

    /**
     * 修改白名单
     * @return
     */
    @RequiresPermissions("sys:whitelist:edit")
    @RequestMapping(value = "update")
    @ResponseBody
    public AjaxJsonEntity update(SysUserWhiteList sysUserWhiteList){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("超时,请重新登录");
            return ajaxJsonEntity;
        }
        if(sysUserWhiteList.getEndDate().getTime()<System.currentTimeMillis()){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("到期日期不能小于当前时间");
            return ajaxJsonEntity;
        }
        try {
            sysUserWhiteList.preUpdate();
            sysUserWhiteList.setUpdateById(user.getId());
            sysUserWhiteList.setEndDate(DateUtils.getDateEnd(sysUserWhiteList.getEndDate()));
            sysUserWhiteListService.update(sysUserWhiteList);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 修改白名单
     * @return
     */
    @RequiresPermissions("sys:whitelist:edit")
    @RequestMapping(value = "delete")
    public String delete(Long id,RedirectAttributes redirectAttributes){
        try {
            sysUserWhiteListService.delete(id);
            addMessage(redirectAttributes,"删除成功");
        }catch (Exception e){
            addMessage(redirectAttributes,"删除白名单失败.失败原因："+ e.getMessage());
        }
        return "redirect:" + adminPath + "/provider/sys/userWhitelist/findList?repage";
    }
}
