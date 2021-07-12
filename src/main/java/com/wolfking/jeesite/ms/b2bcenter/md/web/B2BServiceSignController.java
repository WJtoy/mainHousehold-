package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BServiceTypeMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSign;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BServiceSignService;
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
import java.util.Date;

@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/serviceSign/")
public class B2BServiceSignController extends BaseController {

    @Autowired
    private B2BCustomerMappingService b2BCustomerMappingService;

    @Autowired
    private B2BServiceTypeMappingService b2BServiceTypeMappingService;

    @Autowired
    private B2BServiceSignService b2BServiceSignService;

    @Autowired
    private MicroServicesProperties msProperties;


    /**
     * 分页查询待处理
     *
     * @param b2BSign
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BSign b2BSign, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BSign> page = new Page<>(request, response);
        Date now = new Date();
        if (b2BSign.getCreateDate() == null) {
            b2BSign.setCreateDate(DateUtils.getStartDayOfMonth(now));
        }
        if (b2BSign.getUpdateDate() == null) {
            b2BSign.setUpdateDate(now);
        }

        b2BSign.setBeginApplyTime(DateUtils.getStartOfDay(b2BSign.getCreateDate()).getTime());
        b2BSign.setEndApplyTime(DateUtils.getEndOfDay(b2BSign.getUpdateDate()).getTime());


        if (msProperties.getB2bcenter().getEnabled()) {
            page = b2BServiceSignService.getList(page, b2BSign);

        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }

        model.addAttribute("page", page);
        model.addAttribute("b2BSign", b2BSign);
        return "modules/b2bcenter/md/b2bServiceSignList";
    }
    /**
     * 分页查询已同意
     *
     * @param b2BSign
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:view")
    @RequestMapping(value = {"getAgreeList"})
    public String getAgreeList(B2BSign b2BSign, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BSign> page = new Page<>(request, response);
        Date now = new Date();
        if (b2BSign.getCreateDate() == null) {
            b2BSign.setCreateDate(DateUtils.getStartDayOfMonth(now));
        }
        if (b2BSign.getUpdateDate() == null) {
            b2BSign.setUpdateDate(now);
        }

        b2BSign.setBeginApplyTime(DateUtils.getStartOfDay(b2BSign.getCreateDate()).getTime());
        b2BSign.setEndApplyTime(DateUtils.getEndOfDay(b2BSign.getUpdateDate()).getTime());
        if (msProperties.getB2bcenter().getEnabled()) {
            b2BSign.setSignStatus(10);
            page = b2BServiceSignService.getList(page, b2BSign);

        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("b2BSign", b2BSign);
        return "modules/b2bcenter/md/b2bServiceSignAgreeList";
    }
    /**
     * 分页查询已拒绝
     *
     * @param b2BSign
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:view")
    @RequestMapping(value = {"getRefuseList"})
    public String getRefuseList(B2BSign b2BSign, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BSign> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            b2BSign.setSignStatus(20);
            page = b2BServiceSignService.getList(page, b2BSign);

        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("b2BSign", b2BSign);
        return "modules/b2bcenter/md/b2bServiceSignRefuseList";
    }

    @RequiresPermissions("md:b2bservicesign:view")
    @RequestMapping(value = "form")
    public String form(B2BCustomerMapping b2BCustomerMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (b2BCustomerMapping.getId() != null && b2BCustomerMapping.getId() > 0) {
                MSResponse<B2BSign> msResponse = b2BServiceSignService.getById(b2BCustomerMapping.getId());
                if (msResponse.getData() != null) {
                    B2BSign b2BSign = msResponse.getData();
                    b2BCustomerMapping.setShopId(b2BSign.getMallId().toString());
                    b2BCustomerMapping.setShopName(b2BSign.getMallName());
                    b2BCustomerMapping.setDataSource(16);
                    b2BCustomerMapping.setSaleChannel(4);
                } else {
                    addMessage(model, msResponse.getMsg());
                    return "modules/b2bcenter/md/b2bServiceSignList";
                }

            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
            return "modules/b2bcenter/md/b2bServiceSignList";
        }
        model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
        return "modules/b2bcenter/md/b2bServiceSignForm";
    }

    @RequiresPermissions("md:b2bservicesign:view")
    @RequestMapping(value = "serviceTypeForm")
    public String serviceTypeForm(B2BServiceTypeMapping b2BServiceTypeMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (b2BServiceTypeMapping.getId() != null && b2BServiceTypeMapping.getId() > 0) {
                MSResponse<B2BSign> msResponse = b2BServiceSignService.getById(b2BServiceTypeMapping.getId());
                if (msResponse.getData() != null) {
                    B2BSign b2BSign = msResponse.getData();
                    b2BServiceTypeMapping.setB2bWarrantyType("保内");
                    b2BServiceTypeMapping.setB2bServiceTypeCode(b2BSign.getServType());
                    b2BServiceTypeMapping.setDataSource(16);
                    b2BServiceTypeMapping.setB2bServiceTypeName(b2BSign.getServName());
                } else {
                    addMessage(model, msResponse.getMsg());
                    return "modules/b2bcenter/md/b2bServiceSignList";
                }

            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
            return "modules/b2bcenter/md/b2bServiceSignList";
        }
        model.addAttribute("b2BServiceTypeMapping", b2BServiceTypeMapping);
        return "modules/b2bcenter/md/b2bServiceSignServiceTypeForm";
    }

    /**
     * 检查店铺id是否以及存在
     *
     * @param dataSource
     * @param mallId
     * @param response
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:view")
    @ResponseBody
    @RequestMapping(value = "checkShopId")
    public AjaxJsonEntity checkShopId(Long mallId, Integer dataSource, HttpServletResponse response) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        response.setContentType("application/json; charset=UTF-8");
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Long> msResponse = b2BServiceSignService.checkShopId(mallId, dataSource);
            if (msResponse.getData() != null && msResponse.getData() > 0) {
                ajaxJsonEntity.setMessage("该店铺已存在");
            } else {
                ajaxJsonEntity.setData(1);
            }
        } else {
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return ajaxJsonEntity;
    }


    /**
     * 检查服务类型是否已经存在
     *
     * @param servType
     * @param dataSource
     * @param response
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:view")
    @ResponseBody
    @RequestMapping(value = "checkIsExist")
    public AjaxJsonEntity checkIsExist(String servType, Integer dataSource, HttpServletResponse response) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        response.setContentType("application/json; charset=UTF-8");
        B2BServiceTypeMapping b2BServiceTypeMapping = new B2BServiceTypeMapping();
        if (msProperties.getB2bcenter().getEnabled()) {
            b2BServiceTypeMapping.setDataSource(dataSource);
            b2BServiceTypeMapping.setB2bServiceTypeCode(servType);
            b2BServiceTypeMapping.setB2bWarrantyType("保内");
            MSResponse<Long> msResponse = b2BServiceTypeMappingService.checkIsExist(b2BServiceTypeMapping);

            if (msResponse.getData() != null && msResponse.getData() > 0) {
                ajaxJsonEntity.setMessage("该服务类型已存在");
            } else {
                ajaxJsonEntity.setData(1);
            }

        } else {
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return ajaxJsonEntity;
    }

    /**
     * 签约
     *
     * @param b2BSign
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:edit")
    @RequestMapping("audit")
    @ResponseBody
    public AjaxJsonEntity audit(B2BSign b2BSign, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, b2BSign)) {
                ajaxJsonEntity.setSuccess(false);
                return ajaxJsonEntity;
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                b2BSign.setCreateById(user.getId());
                b2BSign.setUpdateById(user.getId());
                b2BSign.setUpdateDt(new Date().getTime());
                MSResponse<Boolean> mSResponse = b2BServiceSignService.audit(b2BSign);
                if (mSResponse.getCode() == 0) {
                    ajaxJsonEntity.setSuccess(true);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                } else {
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                }
            } else {

                ajaxJsonEntity.setMessage("登录超时,请重新登录");
            }
        } else {

            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }

        return ajaxJsonEntity;
    }

    /**
     * 客户保存
     *
     * @param b2BCustomerMapping
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(B2BCustomerMapping b2BCustomerMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, b2BCustomerMapping)) {

                ajaxJsonEntity.setSuccess(false);
                return ajaxJsonEntity;
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                b2BCustomerMapping.setId(null);
                b2BCustomerMapping.setCreateById(user.getId());
                b2BCustomerMapping.setUpdateById(user.getId());
                b2BCustomerMapping.setShopId(StringUtils.trim(b2BCustomerMapping.getShopId()));
                b2BCustomerMapping.setShopName(StringUtils.trim(b2BCustomerMapping.getShopName()));
                MSErrorCode mSResponse = b2BCustomerMappingService.save(b2BCustomerMapping);
                if (mSResponse.getCode() == 0) {
                    ajaxJsonEntity.setSuccess(true);
                    ajaxJsonEntity.setMessage("保存成功");
                } else {

                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                }
            } else {

                ajaxJsonEntity.setMessage("当前用户不存在");
            }
        } else {

            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return ajaxJsonEntity;
    }

    /**
     * 服务类型保存
     *
     * @param b2BServiceTypeMapping
     * @return
     */
    @RequiresPermissions("md:b2bservicesign:edit")
    @RequestMapping("serviceTypeSave")
    @ResponseBody
    public AjaxJsonEntity save(B2BServiceTypeMapping b2BServiceTypeMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, b2BServiceTypeMapping)) {
                ajaxJsonEntity.setSuccess(false);
                return ajaxJsonEntity;
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                b2BServiceTypeMapping.setId(null);
                b2BServiceTypeMapping.setCreateById(user.getId());
                b2BServiceTypeMapping.setUpdateById(user.getId());
                MSErrorCode mSResponse = b2BServiceTypeMappingService.save(b2BServiceTypeMapping);
                if (mSResponse.getCode() == 0) {
                    ajaxJsonEntity.setSuccess(true);
                    ajaxJsonEntity.setMessage("保存成功");
                } else {
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                }
            } else {
                ajaxJsonEntity.setMessage("当前用户不存在");
            }
        } else {
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }

        return ajaxJsonEntity;
    }

}
