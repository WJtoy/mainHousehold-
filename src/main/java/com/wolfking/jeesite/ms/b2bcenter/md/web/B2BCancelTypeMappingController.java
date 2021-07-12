package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCancelTypeMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceTypeMapping;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCancelTypeMappingService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BServiceTypeMappingService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/cancelType/")
public class B2BCancelTypeMappingController extends BaseController {

    @Autowired
    private B2BCancelTypeMappingService cancelTypeMappingService;

    @Autowired
    private MicroServicesProperties msProperties;

    /**
     * 分页查询
     *
     * @param cancelTypeMapping
     * @return
     */
    @RequiresPermissions("md:b2bcanceltype:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BCancelTypeMapping cancelTypeMapping, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BCancelTypeMapping> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = cancelTypeMappingService.getList(new Page<B2BCancelTypeMapping>(request, response), cancelTypeMapping);
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("cancelTypeMapping", cancelTypeMapping);
        return "modules/b2bcenter/md/b2bCancelTypeMappingList";
    }

    @RequiresPermissions("md:b2bcanceltype:view")
    @RequestMapping(value = "form")
    public String form(B2BCancelTypeMapping cancelTypeMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (cancelTypeMapping.getId() != null && cancelTypeMapping.getId() > 0) {
                MSResponse<B2BCancelTypeMapping> msResponse = cancelTypeMappingService.getById(cancelTypeMapping.getId());
                cancelTypeMapping = msResponse.getData();
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("cancelTypeMapping", cancelTypeMapping);
        return "modules/b2bcenter/md/b2bCancelTypeMappingForm";
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param cancelTypeMapping
     * @return
     */
    @RequiresPermissions("md:b2bcanceltype:edit")
    @RequestMapping("save")
    public String save(B2BCancelTypeMapping cancelTypeMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, cancelTypeMapping)) {
                return form(cancelTypeMapping, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                cancelTypeMapping.setCreateById(user.getId());
                cancelTypeMapping.setUpdateById(user.getId());
                MSErrorCode mSResponse = cancelTypeMappingService.save(cancelTypeMapping);
                if (mSResponse.getCode() == 0) {
                    addMessage(redirectAttributes, "保存成功");
                } else {
                    addMessage(redirectAttributes, mSResponse.getMsg());
                }
            } else {
                addMessage(redirectAttributes, "当前用户不存在");
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }

        return "redirect:" + adminPath + "/b2bcenter/md/cancelType/getList?repage";
    }

    /**
     * 删除
     *
     * @param entity
     * @return
     */
    @RequiresPermissions("md:b2bcanceltype:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BCancelTypeMapping entity, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = cancelTypeMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                addMessage(redirectAttributes, "删除成功");
            } else {
                addMessage(redirectAttributes, msResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/cancelType/getList?repage";
    }
}
