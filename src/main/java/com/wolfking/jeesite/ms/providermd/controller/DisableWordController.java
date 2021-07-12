package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDDisableWord;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.providermd.service.MSDisableWordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/provider/md/disableWord")
public class DisableWordController {

    @Autowired
    private MSDisableWordService msDisableWordService;

    @RequiresPermissions("md:disableWord:view")
    @RequestMapping(value = {"list"})
    public String list(MDDisableWord mdDisableWord, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<MDDisableWord> page = msDisableWordService.findList(new Page<>(request, response), mdDisableWord);
        model.addAttribute("page", page);
        model.addAttribute("mdDisableWord", mdDisableWord);
        return "modules/providermd/disableWordList";
    }

    @RequiresPermissions("md:disableWord:edit")
    @RequestMapping(value = "form")
    public String form(Model model) {
        MDDisableWord mdDisableWord = new MDDisableWord();
        model.addAttribute("mdDisableWord", mdDisableWord);
        return "modules/providermd/disableWordFrom";
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param mdDisableWord
     * @return
     */
    @RequiresPermissions("md:disableWord:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(MDDisableWord mdDisableWord) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        try {
            msDisableWordService.save(mdDisableWord);
            ajaxJsonEntity.setSuccess(true);
            ajaxJsonEntity.setMessage("保存成功");
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @RequiresPermissions("md:disableWord:edit")
    @RequestMapping("delete")
    @ResponseBody
    public AjaxJsonEntity delete(Long id) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(true);
        ajaxJsonEntity.setMessage("删除成功");
        try {
            msDisableWordService.delete(id);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }
}
