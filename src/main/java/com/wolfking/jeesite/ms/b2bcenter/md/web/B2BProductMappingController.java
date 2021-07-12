package com.wolfking.jeesite.ms.b2bcenter.md.web;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BProductMapping;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BProductMappingService;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.providermd.service.ProductModelService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/product/")
public class B2BProductMappingController extends BaseController {

    @Autowired
    private B2BProductMappingService b2BProductMappingService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MicroServicesProperties msProperties;

    @Autowired
    private B2BCustomerMappingService customerMappingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductModelService productModelService;

    /**
     * 分页查询
     *
     * @param b2BProductMapping
     * @return
     */
    @RequiresPermissions("md:b2bproduct:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BProductMapping b2BProductMapping, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BProductMapping> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = b2BProductMappingService.getList(new Page<>(request, response), b2BProductMapping);
            Product product;
            Customer customer;
            B2BCustomerMapping customerMapping;
            List<B2BCustomerMapping> customerMappingList = customerMappingService.findAllList();
            for (B2BProductMapping entity : page.getList()) {
                product = productService.getProductByIdFromCache(entity.getProductId());
                if (product != null && StrUtil.isNotBlank(product.getName())) {
                    entity.setProductName(product.getName());
                }
                if(entity.getCustomerId()!=null){
                    customer = customerService.getFromCache(entity.getCustomerId());
                    if(customer!=null){
                        entity.setCustomerName(customer.getName());
                    }
                }
                if(customerMappingList!=null && customerMappingList.size()>0){
                    customerMapping = customerMappingList.stream().filter(t-> t.getShopId().equals(entity.getShopId()) &&
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
        model.addAttribute("b2BCustomerMapping", b2BProductMapping);
        return "modules/b2bcenter/md/b2bProductMappingList";
    }

    @RequiresPermissions("md:b2bproduct:view")
    @RequestMapping(value = "form")
    public String form(B2BProductMapping productMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (productMapping.getId() != null && productMapping.getId() > 0) {
                B2BProductMapping entity = b2BProductMappingService.getById(productMapping.getId());
                if(entity !=null){
                    productMapping.setCustomerCode(entity.getCustomerCode());
                    productMapping.setCustomerProductModel(entity.getCustomerProductModel());
                }
                if (productMapping.getProductId() != null && productMapping.getProductId() > 0) {
                    String productName = productService.getProductByIdFromCache(productMapping.getProductId()).getName();
                    if (productName != null && productName != "") {
                        productMapping.setProductName(productName);
                    }
                }
                if(productMapping.getCustomerId() !=null && productMapping.getCustomerId()>0){
                    Customer customer = customerService.getFromCache(productMapping.getCustomerId());
                    if(customer!=null){
                        productMapping.setCustomerName(customer.getName());
                    }
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("productMapping", productMapping);
        return "modules/b2bcenter/md/b2bProductMappingForm";
    }


    /**
     * 保存数据
     *
     * @param productMapping
     * @return
     */
    @RequiresPermissions("md:b2bproduct:edit")
    @RequestMapping("save")
    public String save(B2BProductMapping productMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, productMapping)) {
                return form(productMapping, model);
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                productMapping.setCreateById(user.getId());
                productMapping.setUpdateById(user.getId());
                MSErrorCode mSResponse = b2BProductMappingService.save(productMapping);
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
        return "redirect:" + adminPath + "/b2bcenter/md/product/getList?repage";
    }


    /**
     * 删除数据
     *
     * @param entity
     * @return
     */
    @RequiresPermissions("md:b2bproduct:edit")
    @RequestMapping(value = "delete")
    public String delete(B2BProductMapping entity, RedirectAttributes redirectAttributes) {
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = b2BProductMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                addMessage(redirectAttributes, "删除成功");
            } else {
                addMessage(redirectAttributes, msResponse.getMsg());
            }
        } else {
            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        return "redirect:" + adminPath + "/b2bcenter/md/product/getList?repage";
    }


    /**
     * 根据客户id获取产品和店铺id
     * @param customerId,dataSource
     * @return
     */
    @RequestMapping(value="ajax/getByCustomerId")
    @ResponseBody
    public AjaxJsonEntity getByCustomerId(Long customerId, Integer dataSource){
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(false);
        if(customerId != null && customerId > 0 && dataSource !=null && dataSource>0) {
            Map<String,Object> map = new HashMap<>();
            List<Product> productList = productService.getCustomerProductList(customerId);
            if (productList != null && productList.size() > 0) {
                jsonEntity.setSuccess(true);
                map.put("productList",productList);
            }
            List<B2BCustomerMapping> customerMappingList = customerMappingService.getShopListByCustomer(B2BDataSourceEnum.valueOf(dataSource),customerId);
            if(customerMappingList !=null && customerMappingList.size()>0){
                jsonEntity.setSuccess(true);
                map.put("customerMappingList",customerMappingList);
            }
            jsonEntity.setData(map);

        }
        return jsonEntity;
    }

    /**
     * 根据客户id获取产品id获取产品信号
     * @param customerId,productId
     * @return
     */
    @RequestMapping(value="ajax/getModelList")
    @ResponseBody
    public AjaxJsonEntity getModelList(Long customerId,Long productId,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        try {
             List<String> modelList = productModelService.getModelNamesFromCache(customerId,productId);
             jsonEntity.setData(modelList);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    @RequiresPermissions("md:b2bproduct:edit")
    @RequestMapping(value = "syncRoutingConfig")
    public String syncRoutingConfig(Model model) {
        List<Dict> dataSources = Lists.newArrayList();
        if (msProperties.getB2bcenter().getEnabled()) {
            dataSources = B2BMDUtils.getRoutingEnabledDataSourceDicts();
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("dataSources", dataSources);
        return "modules/b2bcenter/md/b2bRoutingConfigSyncForm";
    }


    /**
     * 一键同步
     */
    @RequestMapping(value="ajax/syncByDataSource")
    @ResponseBody
    public AjaxJsonEntity syncByDataSource(@RequestParam("dataSourceId") Integer dataSourceId, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
        jsonEntity.setSuccess(true);
        User user = UserUtils.getUser();
        if(user ==null || user.getId()==null || user.getId()<0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("用户不存在,请重新登录");
            return jsonEntity;
        }
        try{
            B2BProductMapping productMapping = new B2BProductMapping();
            productMapping.setDataSource(dataSourceId);
            MSResponse<Integer> msResponse = b2BProductMappingService.syncByDataSource(productMapping,user.getId());
            if(msResponse.getCode()==0){
                jsonEntity.setMessage("同步成功");
            }else{
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage(msResponse.getMsg());
            }
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }


    /**
     * 工单转换列表一键配置
     */
    @RequestMapping(value="addProductMapping")
    public String addProductMapping(HttpServletRequest request,B2BProductMapping b2BProductMapping,Model model){
        model.addAttribute("productMapping",b2BProductMapping);
      return "modules/b2bcenter/sd/addProductMappingForm";
    }

    /**
     * 异步保存产品关联
     */
    @RequestMapping(value="ajax/saveProductMapping")
    @ResponseBody
    public AjaxJsonEntity saveProductMapping(B2BProductMapping b2BProductMapping){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null && user.getId()<=0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
        }
        try {
            b2BProductMapping.setCreateById(user.getId());
            b2BProductMapping.setUpdateById(user.getId());
            MSErrorCode msErrorCode = b2BProductMappingService.save(b2BProductMapping);
            if(msErrorCode.getCode()>0){
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存失败:"+ msErrorCode.getMsg());
            }
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存失败:"+ e.getMessage());
        }
        return ajaxJsonEntity;
    }
}
