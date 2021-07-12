package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceTypeMapping;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
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
@RequestMapping(value = "${adminPath}/b2bcenter/md/serviceType/")
public class B2BServiceTypeMappingController extends BaseController {

    @Autowired
    private B2BServiceTypeMappingService b2BServiceTypeMappingService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private MicroServicesProperties msProperties;

    /**
     * 分页查询
     *
     * @param b2BServiceTypeMapping
     * @return
     */
    @RequiresPermissions("md:b2bservicetype:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BServiceTypeMapping b2BServiceTypeMapping, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BServiceTypeMapping> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = b2BServiceTypeMappingService.getList(new Page<B2BServiceTypeMapping>(request, response), b2BServiceTypeMapping);
            List<B2BServiceTypeMapping> list = page.getList();
            for (B2BServiceTypeMapping entity : list) {
                ServiceType serviceType = serviceTypeService.get(entity.getServiceTypeId());
                if (serviceType != null) {
                    entity.setServiceTypeName(serviceType.getName());
                    entity.setServiceTypeCode(serviceType.getCode());
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("b2BServiceTypeMapping", b2BServiceTypeMapping);
        return "modules/b2bcenter/md/b2bServiceTypeMappingList";
    }

    @RequiresPermissions("md:b2bservicetype:view")
    @RequestMapping(value = "form")
    public String form(B2BServiceTypeMapping b2BServiceTypeMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (b2BServiceTypeMapping.getId() != null && b2BServiceTypeMapping.getId() > 0) {
                MSResponse<B2BServiceTypeMapping> msResponse = b2BServiceTypeMappingService.getById(b2BServiceTypeMapping.getId());
                b2BServiceTypeMapping = msResponse.getData();
                if (msResponse.getCode() == 0) {
                    b2BServiceTypeMapping.setServiceTypeName(serviceTypeService.get(b2BServiceTypeMapping.getServiceTypeId()).getName());
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("b2BServiceTypeMapping", b2BServiceTypeMapping);
        return "modules/b2bcenter/md/b2bServiceTypeMappingForm";
    }


    /**
     * 保存数据(添加或修改)
     *
     * @param b2BServiceTypeMapping
     * @return
     */
    @RequiresPermissions("md:b2bservicetype:edit")
    @RequestMapping("save")
    public String save(B2BServiceTypeMapping b2BServiceTypeMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, b2BServiceTypeMapping)) {
                return form(b2BServiceTypeMapping, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                b2BServiceTypeMapping.setCreateById(user.getId());
                b2BServiceTypeMapping.setUpdateById(user.getId());
                MSErrorCode mSResponse = b2BServiceTypeMappingService.save(b2BServiceTypeMapping);
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

        return "redirect:" + adminPath + "/b2bcenter/md/serviceType/getList?repage";
    }

    /**
     * 删除
     *
     * @param entity
     * @return
     */
    @RequiresPermissions("md:b2bservicetype:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BServiceTypeMapping entity, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = b2BServiceTypeMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                addMessage(redirectAttributes, "删除成功");
            } else {
                addMessage(redirectAttributes, msResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/serviceType/getList?repage";
    }

    /**
     * 检查服务类型是否已经存在
     *
     * @param id
     * @param b2BServiceTypeMapping
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkIsExist")
    public String checkIsExist(Long id, B2BServiceTypeMapping b2BServiceTypeMapping, HttpServletResponse response) {
        String result = "true";
        response.setContentType("application/json; charset=UTF-8");
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Long> msResponse = b2BServiceTypeMappingService.checkIsExist(b2BServiceTypeMapping);
            if (msResponse.getData() != null && msResponse.getData() > 0) {
                if (id == -1) {
                    result = "该服务类型关联已存在";
                } else if (id != msResponse.getData()) {
                    result = "该服务类型关联已存在";
                }
            }
        } else {
            result = MSErrorCode.MICROSERVICE_DISABLED.msg;
        }
        return result;
    }
}
