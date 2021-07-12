package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDActionCode;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSActionCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "${adminPath}/provider/md/actionCode")
@Slf4j
public class ActionCodeController {
    @Autowired
    private MSActionCodeService msActionCodeService;

    @ResponseBody
    @RequestMapping(value={"checkAnalysis"})
    public String checkName(Long productId, Long serviceTypeId, String analysis) {
        // 此方法不用了 //2019-11-27
        /*User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            return "false";
        }
        if (productId == null || productId.intValue() == 0) {
            return "请选择产品";
        }
        if (serviceTypeId == null || serviceTypeId.intValue() == 0) {
            return "请选择服务类型";
        }
        if (StringUtils.isBlank(analysis)) {
            return "true";
        }
        try {
            MDActionCode mdActionCode = new MDActionCode();
            mdActionCode.setProductId(productId);
            mdActionCode.setServiceTypeId(serviceTypeId);
            mdActionCode.setAnalysis(analysis);
            String result = msActionCodeService.checkAnalysis(mdActionCode);
            return result.equalsIgnoreCase("true") ? result : "故障处理已存在.";
        } catch (Exception ex) {
            log.error("error,", ex);
            return "false";
        }*/
        return "";
    }
}
