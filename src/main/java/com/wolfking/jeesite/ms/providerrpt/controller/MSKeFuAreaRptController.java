package com.wolfking.jeesite.ms.providerrpt.controller;

import com.kkl.kklplus.entity.rpt.RPTCrushCoverageEntity;
import com.kkl.kklplus.entity.rpt.RPTKeFuAreaEntity;
import com.kkl.kklplus.entity.rpt.exception.RPTBaseException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.modules.rpt.web.BaseRptController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providerrpt.service.MSCrushCoverageRptService;
import com.wolfking.jeesite.ms.providerrpt.service.MSKeFuAreaRptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/rpt/provider/keFuArea/")
public class MSKeFuAreaRptController {

        @Autowired
        private MSKeFuAreaRptService msKeFuAreaRptService;

        /**
         * 客服区域报表
         * @param model
         * @return
         */
        @SuppressWarnings("deprecation")
        @RequiresPermissions("rpt:keFuArea:view")
        @RequestMapping(value = "keFuAreaReport")
        public String KeFuAreaReport(@RequestParam Map<String, Object> paramMap, Model model) {
            List<RPTKeFuAreaEntity> list = msKeFuAreaRptService.getKefuAreasRptData();
            model.addAttribute("list", list);
            model.addAllAttributes(paramMap);
            return "modules/providerrpt/keFuAreaReport";
        }

        @ResponseBody
        @RequiresPermissions("rpt:keFuArea:export")
        @RequestMapping(value = "export", method = RequestMethod.POST)
        public AjaxJsonEntity export() {
            AjaxJsonEntity result = new AjaxJsonEntity(true);
            try {
                User user = UserUtils.getUser();
                msKeFuAreaRptService.createRptExportTask(user);
                result.setMessage("报表导出任务创建成功，请前往'报表中心->报表下载'功能下载");

            } catch (RPTBaseException e) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                result.setSuccess(false);
                result.setMessage("创建报表导出任务失败，请重试");
            }
            return result;
        }
}
