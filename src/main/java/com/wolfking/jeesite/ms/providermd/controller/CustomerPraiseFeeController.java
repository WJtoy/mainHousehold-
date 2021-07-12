package com.wolfking.jeesite.ms.providermd.controller;

import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerPraiseFeeService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("${adminPath}/provider/md/customerPraiseFee")
@Slf4j
public class CustomerPraiseFeeController extends BaseController {
    public static String DICTTYPE = "PraiseStandardType";
    @Autowired
    private MSCustomerPraiseFeeService msCustomerPraiseFeeService;

    @Autowired
    private MSCustomerService msCustomerService;

    /**
     * 分页查询
     * @param customerPraiseFee
     * @return
     */
    @RequiresPermissions("md:customerpraisefee:view")
    @RequestMapping(value = {"findList",""})
    public String findList(MDCustomerPraiseFee customerPraiseFee, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (customerPraiseFee.getCustomerId() == null) {
            addMessage(model, "请选择客户!");
            return "modules/providermd/customerPraiseFeeList";
        }
        Page<MDCustomerPraiseFee> page = msCustomerPraiseFeeService.findList(new Page<>(request, response), customerPraiseFee);
        if  (page != null && page.getList() != null && !page.getList().isEmpty()) {
            // 补充客户名称
            List<MDCustomerPraiseFee> customerPraiseFeeList = page.getList();
            List<Long> customerIds = customerPraiseFeeList.stream().map(c->c.getCustomerId()).distinct().collect(Collectors.toList());
            List<Customer> customerList = msCustomerService.findIdAndNameListByIds(customerIds);
            Map<Long, String> nameMap = customerList.stream().collect(Collectors.toMap(r->r.getId(),r->r.getName()));
            for(MDCustomerPraiseFee mdCustomerPraiseFee: customerPraiseFeeList) {
                mdCustomerPraiseFee.setCustomerName(nameMap.get(mdCustomerPraiseFee.getCustomerId()));
                Double discount = mdCustomerPraiseFee.getDiscount()*100;
                mdCustomerPraiseFee.setDiscount(discount);
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("customerPraiseFee", customerPraiseFee);

        return "modules/providermd/customerPraiseFeeList";
    }

    @RequiresPermissions("md:customerpraisefee:edit")
    @RequestMapping(value = "form")
    public String form(MDCustomerPraiseFee customerPraiseFee, Model model) {
        boolean canAction = true;
        if (customerPraiseFee.getId() != null) {
            customerPraiseFee = msCustomerPraiseFeeService.getById(customerPraiseFee.getId());
            if (customerPraiseFee != null) {
                Double discount = customerPraiseFee.getDiscount() * 100;
                customerPraiseFee.setDiscount(discount);
            }
            Customer customer = msCustomerService.getByIdToCustomer(customerPraiseFee.getCustomerId());
            customerPraiseFee.setCustomerName(Optional.ofNullable(customer).map(Customer::getName).orElse(""));
        } else {
            customerPraiseFee = new MDCustomerPraiseFee();
        }

        List<Dict> picTypes = MSDictUtils.getDictList(DICTTYPE);

        if(picTypes == null || picTypes.isEmpty()){
            addMessage(model,"未设定好评费标准");
            canAction = false;
        }else{
            List<MDCustomerPraiseFeePraiseStandardItem> items = customerPraiseFee.getPraiseStandardItems();
            List<MDCustomerPraiseFeePraiseStandardItem> returnItems = msCustomerPraiseFeeService.mergeAllItems(items, picTypes);
            customerPraiseFee.setPraiseStandardItems(returnItems);
        }
        List<Customer> customerList = CustomerUtils.getMyCustomerListFromMS();

        model.addAttribute("canAction",canAction);
        model.addAttribute("customerList", customerList);
        model.addAttribute("customerPraiseFee", customerPraiseFee);
        return "modules/providermd/customerPraiseFeeForm";
    }

    @RequiresPermissions("md:customerpraisefee:edit")
    @ResponseBody
    @RequestMapping(value = "save")
    public AjaxJsonEntity save(MDCustomerPraiseFee customerPraiseFee) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            if (customerPraiseFee.getIsNewRecord()) {
                // 判断是否重复加载
                boolean isExists = msCustomerPraiseFeeService.isExistsByCustomerId(customerPraiseFee.getCustomerId());
                if (isExists) {
                    throw new RuntimeException("该客户的好评费已存在了，请不要重复添加。");
                }
            }
            Double discount = customerPraiseFee.getDiscount()/100.0;
            customerPraiseFee.setDiscount(discount);
            customerPraiseFee.setPicCount(0);
            msCustomerPraiseFeeService.save(customerPraiseFee);
        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

    /**
     * 删除
     * @param
     * @return
     */
    @RequiresPermissions("md:customerpraisefee:edit")
    @RequestMapping(value = "delete")
    public String delete(MDCustomerPraiseFee customerPraiseFee, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if(user !=null && user.getId()!=null && user.getId()>0){
            try {
                msCustomerPraiseFeeService.delete(customerPraiseFee);
                addMessage(redirectAttributes, "删除成功" );
            }catch (Exception e){
                addMessage(redirectAttributes, e.getMessage());
            }
        }else{
            addMessage(redirectAttributes, "删除失败.失败原因:当前用户不存在" );
        }

        return "redirect:" + adminPath + "/provider/md/customerPraiseFee/findList?repage";
    }
}
