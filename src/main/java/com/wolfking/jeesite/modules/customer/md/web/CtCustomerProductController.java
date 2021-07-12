package com.wolfking.jeesite.modules.customer.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.dto.MDCustomerDto;
import com.kkl.kklplus.entity.md.dto.MDCustomerProductDto;
import com.kkl.kklplus.entity.md.dto.MDProductDto;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerProduct;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
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
import java.util.Optional;

@Controller
@RequestMapping(value="${adminPath}/customer/md/customerProduct")
public class CtCustomerProductController extends BaseController {
    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private ProductService productService;

    @ResponseBody
    @RequestMapping(value = "ajax/customerProductList")
    public AjaxJsonEntity getCustomerProductList(Long customerId) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        if (customerId != null && customerId > 1){
            List<Product> list = msCustomerProductService.findProductByCustomerIdFromCache(customerId);
            if (list == null) {
                list = Lists.newArrayList();
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
            return jsonEntity;
        }else
        {
            return jsonEntity;
        }
    }

    /**
     * 分页查询
     * @param customerProduct
     * @return
     */
    @RequiresPermissions("customer:md:customerproduct:view")
    @RequestMapping(value = {"findList", ""})
    public String findList(CustomerProduct customerProduct, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<CustomerProduct> customerPage = new Page<>(request, response);
        // add on 2020-6-2 begin
        User cuser = UserUtils.getUser();
        if (cuser.isCustomer()) {
            Customer customer = customerProduct.getCustomer();
            if (customer == null) {
                customer = new Customer();
            }
            if (cuser.getCompany() != null && cuser.getCompany().getId() != null) {
                customer.setId(cuser.getCompany().getId());
                Customer tempCustomer = msCustomerService.get(cuser.getCompany().getId());
                customer.setName(Optional.ofNullable(tempCustomer).map(Customer::getName).orElse(""));
            }
            customerProduct.setCustomer(customer);
        } else if (cuser.isSaleman()) {
            Customer customer = customerProduct.getCustomer();
            if (customer == null) {
                customer = new Customer();
            }
            customer.setSales(cuser);
            customerProduct.setCustomer(customer);
        }
        // add on 2020-6-2 end
        if (customerProduct.getCustomer() == null || customerProduct.getCustomer().getId() == null ||  customerProduct.getCustomer().getId()<= 0) {
            //addMessage(model, "请选择客户");
            //return "modules/customer/md/ctCustomerProductList";
            return "modules/customer/md/ctCustomerProductListVerSecond";
        }
        Page<MDCustomerProductDto> page= msCustomerProductService.findList(customerPage,customerProduct);
        model.addAttribute("customerProduct",customerProduct);
        model.addAttribute("page", page);

        List<Product> productList = productService.getCustomerProductList(customerProduct.getCustomer().getId());
        model.addAttribute("productList", productList);

        return "modules/customer/md/ctCustomerProductListVerSecond";
    }

    @RequiresPermissions("customer:md:customerproduct:edit")
    @RequestMapping(value = "form")
    public String form(Long id,Model model) {
        MDCustomerProductDto customerProduct = new MDCustomerProductDto();
        if (id != null && id > 0) {
            customerProduct = msCustomerProductService.getById(id);
            if(customerProduct == null){
                customerProduct = new MDCustomerProductDto();
            }
        } else {
            User cuser = UserUtils.getUser();
            if (cuser.isCustomer()) {
                if (cuser.getCompany() != null && cuser.getCompany().getId() != null) {
                    MDCustomerDto customer = new MDCustomerDto();
                    customer.setId(cuser.getCompany().getId());

                    Customer tempCustomer = msCustomerService.get(cuser.getCompany().getId());
                    customer.setName(Optional.ofNullable(tempCustomer).map(Customer::getName).orElse(""));
                    customerProduct.setCustomerDto(customer);
                }
            }
        }
        model.addAttribute("customerProduct", customerProduct);
        //return "modules/customer/md/ctCustomerProductForm";
        return "modules/customer/md/ctCustomerProductFormVerSecond";
    }


    /**
     * 保存数据(添加或修改)
     * @param customerProduct
     * @return
     */
    @RequiresPermissions("customer:md:customerproduct:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(CustomerProduct customerProduct,HttpServletRequest request) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if(customerProduct.getCustomer() ==null || customerProduct.getCustomer().getId()==null || customerProduct.getCustomer().getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("客户不能为空");
            return ajaxJsonEntity;
        }
        if(customerProduct.getProduct() ==null || customerProduct.getProduct().getId()==null || customerProduct.getProduct().getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("产品不能为空");
            return ajaxJsonEntity;
        }
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerProduct.setCreateBy(user);
            customerProduct.setUpdateBy(user);
            try {
                msCustomerProductService.save(customerProduct);
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("保存成功");
            }catch (Exception e){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        } else {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在");
        }
        return ajaxJsonEntity;
    }

    /**
     * 删除
     * @param customerProduct
     * @return
     */
    /*
    @RequiresPermissions("customer:md:customerproduct:edit")
    @RequestMapping(value = "delete")
    public String delete(CustomerProduct customerProduct, RedirectAttributes redirectAttributes) {
        User user = UserUtils.getUser();
        if(user !=null && user.getId()!=null && user.getId()>0){
            try {
                customerProduct.setUpdateBy(user);
                msCustomerProductService.delete(customerProduct);
                addMessage(redirectAttributes, "删除成功" );
            }catch (Exception e){
                addMessage(redirectAttributes, e.getMessage());
            }
        }else{
            addMessage(redirectAttributes, "删除失败.失败原因:当前用户不存在" );
        }
        return "redirect:" + adminPath + "/customer/md/customerProduct/findList?repage";
    }
    */

    /**
     * 异步删除
     * @param customerProduct
     * @return
     */
    @RequiresPermissions("customer:md:customerproduct:edit")
    @RequestMapping(value = "ajax/remove")
    @ResponseBody
    public AjaxJsonEntity remove(CustomerProduct customerProduct){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user !=null && user.getId()!=null && user.getId()>0){
            try {
                customerProduct.setUpdateBy(user);
                msCustomerProductService.delete(customerProduct);
                ajaxJsonEntity.setMessage("删除成功");
            }catch (Exception e){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        }else{
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除失败.失败原因:当前用户不存在,请重新登录");
        }
        return ajaxJsonEntity;
    }

    /**
     * 异步删除
     * @param customerProduct
     * @return
     */
    @RequiresPermissions("customer:md:customerproduct:edit")
    @RequestMapping(value = "ajax/removeFixSpec")
    @ResponseBody
    public AjaxJsonEntity removeFixSpec(CustomerProduct customerProduct){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user !=null && user.getId()!=null && user.getId()>0){
            try {
                customerProduct.setUpdateBy(user);
                msCustomerProductService.removeFixSpec(customerProduct);
                ajaxJsonEntity.setMessage("删除成功");
            }catch (Exception e){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        }else{
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("删除失败.失败原因:当前用户不存在,请重新登录");
        }
        return ajaxJsonEntity;
    }

    @RequiresPermissions("customer:md:customerproduct:edit")
    @ResponseBody
    @RequestMapping(value = "ajax/form")
    public AjaxJsonEntity aJaxForm(Long customerId,Long productId) {
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        CustomerProduct customerProduct = msCustomerProductService.getByCustomerIdAndProductId(customerId, productId);
        if(customerProduct != null){
            MDCustomerProductDto customerProductDto = new MDCustomerProductDto();
            MDCustomerDto customerDto = new MDCustomerDto();
            customerDto.setId(customerProduct.getCustomer().getId());
            customerProductDto.setCustomerDto(customerDto);

            MDProductDto productDto = new MDProductDto();
            productDto.setId(customerProduct.getProduct().getId());
            customerProductDto.setProductDto(productDto);
            customerProductDto.setFixSpec(customerProduct.getFixSpec());
            customerProductDto.setId(customerProduct.getId());

            jsonEntity.setSuccess(true);
            jsonEntity.setData(customerProductDto);
        }
        return jsonEntity;
    }


}
