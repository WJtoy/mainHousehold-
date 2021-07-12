package com.wolfking.jeesite.ms.b2bcenter.md.web;


import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BWarrantyMapping;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BWarrantyMappingService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/warranty/")
public class B2BWarrantyMappingController extends BaseController {

    @Autowired
    private B2BWarrantyMappingService warrantyMappingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MicroServicesProperties msProperties;

    @Autowired
    private B2BCustomerMappingService customerMappingService;


    /**
     * 分页查询
     *
     * @param warrantyMapping
     * @return
     */
    @RequiresPermissions("md:b2bwarranty:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BWarrantyMapping warrantyMapping, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BWarrantyMapping> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = warrantyMappingService.getList(page, warrantyMapping);
            List<B2BCustomerMapping> list = customerMappingService.findAllList();
            List<B2BWarrantyMapping> warrantyMappingList = page.getList();
            B2BCustomerMapping customerMapping;
            for (B2BWarrantyMapping entity : warrantyMappingList) {
                Customer customer = customerService.getFromCache(entity.getCustomerId());
                entity.setB2bWarrantyCode(entity.getB2bWarrantyCode().replace(" ", "").trim());
                if(customer!=null){
                    String customerName = customerService.getFromCache(entity.getCustomerId()).getName();
                    if (customerName != null && customerName != "") {
                        entity.setCustomerName(customerName);
                    }
                }
                if(list!=null && list.size()>0){
                    customerMapping = list.stream().filter(t-> t.getShopId().equals(entity.getShopId()) &&
                                                           t.getDataSource()==entity.getDataSource() &&
                                                           t.getCustomerId().equals(entity.getCustomerId())).findFirst().orElse(null);
                    if(customerMapping!=null){
                        entity.setShopName(customerMapping.getShopName());
                    }
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("b2bWarrantyMapping", warrantyMapping);
        return "modules/b2bcenter/md/b2bWarrantyMappingList";
    }

    @RequiresPermissions("md:b2bwarranty:view")
    @RequestMapping(value = "form")
    public String form(B2BWarrantyMapping warrantyMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (warrantyMapping.getId() != null && warrantyMapping.getId() > 0) {
                String shopName=warrantyMapping.getShopName();
                warrantyMapping = warrantyMappingService.getById(warrantyMapping.getId());
                warrantyMapping.setShopName(shopName);
                if(warrantyMapping.getCustomerId()!=null && warrantyMapping.getCustomerId()>0){
                    warrantyMapping.setCustomerName(customerService.getFromCache(warrantyMapping.getCustomerId()).getName());
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("b2bWarrantyMapping", warrantyMapping);
        return "modules/b2bcenter/md/b2bWarrantyMappingForm";
    }


    /**
     * 添加或者修改数据
     *
     * @param warrantyMapping
     * @return
     */
    @RequiresPermissions("md:b2bwarranty:edit")
    @RequestMapping("save")
    public String save(B2BWarrantyMapping warrantyMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, warrantyMapping)) {
                return form(warrantyMapping, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                warrantyMapping.setCreateById(user.getId());
                warrantyMapping.setUpdateById(user.getId());
                MSErrorCode mSResponse = warrantyMappingService.save(warrantyMapping);
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

        return "redirect:" + adminPath + "/b2bcenter/md/warranty/getList?repage";
    }

    /**
     * 删除数据
     * @param entity
     * @return
     */
    @RequiresPermissions("md:b2bwarranty:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BWarrantyMapping entity, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = warrantyMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                addMessage(redirectAttributes, "删除成功");
            } else {
                addMessage(redirectAttributes, msResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/warranty/getList?repage";
    }

    /**
     * 删除数据
     */
    @RequiresPermissions("md:b2bwarranty:edit")
    @ResponseBody
    @PostMapping(value = "ajaxDelete")
    public AjaxJsonEntity delete(Long id,Integer dataSource, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        if(id == null || id <= 0 || dataSource == null || dataSource <= 0){
            return AjaxJsonEntity.fail("参数错误",null);
        }
        AjaxJsonEntity ajaxResponse = new AjaxJsonEntity(true);
        try {
            if (msProperties.getB2bcenter().getEnabled()) {
                B2BWarrantyMapping entity = new B2BWarrantyMapping();
                entity.setId(id);
                entity.setDataSource(dataSource);
                MSResponse<Integer> msResponse = warrantyMappingService.delete(entity);
                if(!MSResponse.isSuccessCode(msResponse)){
                    ajaxResponse.setSuccess(false);
                    ajaxResponse.setMessage(msResponse.getMsg());
                }
            } else {
                ajaxResponse.setSuccess(false);
                ajaxResponse.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
            }
        } catch (Exception e) {
            ajaxResponse.setSuccess(false);
            ajaxResponse.setMessage("修改网点价格类型失败：" + e.getMessage());
        }
        return ajaxResponse;
    }

}
