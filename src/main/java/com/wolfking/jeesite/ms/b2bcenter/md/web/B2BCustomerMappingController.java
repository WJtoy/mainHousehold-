package com.wolfking.jeesite.ms.b2bcenter.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Controller
@RequestMapping(value = "${adminPath}/b2bcenter/md/customer/")
public class B2BCustomerMappingController extends BaseController {

    @Autowired
    private B2BCustomerMappingService b2BCustomerMappingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MicroServicesProperties msProperties;


    /**
     * 分页查询
     *
     * @param b2BCustomerMapping
     * @return
     */
    @RequiresPermissions("md:b2bcustomer:view")
    @RequestMapping(value = {"getList", ""})
    public String getList(B2BCustomerMapping b2BCustomerMapping, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<B2BCustomerMapping> page = new Page<>(request, response);
        if (msProperties.getB2bcenter().getEnabled()) {
            page = b2BCustomerMappingService.getList(page, b2BCustomerMapping);
            List<B2BCustomerMapping> customerMappingList = page.getList();

            // 调整批量分页优化获取
            List<Long> customerIds = customerMappingList.stream().distinct().map(p -> p.getCustomerId()).collect(Collectors.toList());
            List<Long> customerAccountIds = customerMappingList.stream().filter(t->t.getCustomerAccountId()>0).map(p -> p.getCustomerAccountId()).distinct().collect(Collectors.toList());
            Map<Long,User> userMap = Maps.newHashMap();
            if(customerAccountIds!=null && customerAccountIds.size()>0){
                List<User> users = b2BCustomerMappingService.findUserByIds(customerAccountIds);
                if(users!=null && users.size()>0){
                    userMap = users.stream().collect(Collectors.toMap(User::getId, a -> a,(k1,k2)->k1));
                }
            }
            User user;
            if (!CollectionUtils.isEmpty(customerIds)) {
                Map<Long, String> map = CustomerUtils.findAllCustomerMap(customerIds);
                for (B2BCustomerMapping entity : customerMappingList) {
                    entity.setCustomerName(Optional.ofNullable(map.get(entity.getCustomerId())).orElse(""));
                    user = userMap.get(entity.getCustomerAccountId());
                    if(user!=null){
                        entity.setCustomerAccountName(user.getName());
                    }
                }
            }

            /*for (B2BCustomerMapping entity : customerMappingList) {
                String customerName = customerService.getFromCache(entity.getCustomerId()).getName();
                if (customerName != null && customerName != "") {
                    entity.setCustomerName(customerName);
                }
            }*/
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("page", page);
        model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
        return "modules/b2bcenter/md/b2bCustomerMappingList";
    }

    @RequiresPermissions("md:b2bcustomer:view")
    @RequestMapping(value = "form")
    public String form(B2BCustomerMapping b2BCustomerMapping, Model model) {
        if (msProperties.getB2bcenter().getEnabled()) {
            if (b2BCustomerMapping.getId() != null && b2BCustomerMapping.getId() > 0) {
                MSResponse<B2BCustomerMapping> msResponse = b2BCustomerMappingService.getById(b2BCustomerMapping.getId());
                b2BCustomerMapping = msResponse.getData();
                if (msResponse.getCode() == 0) {
                    b2BCustomerMapping.setCustomerName(customerService.getFromCache(b2BCustomerMapping.getCustomerId()).getName());
                }
            }
        } else {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
        model.addAttribute("b2BCustomerMapping", b2BCustomerMapping);
        return "modules/b2bcenter/md/b2bCustomerMappingForm";
    }


    /**
     * 添加或者修改数据
     *
     * @param b2BCustomerMapping
     * @return
     */
    @RequiresPermissions("md:b2bcustomer:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity save(B2BCustomerMapping b2BCustomerMapping, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (!beanValidator(model, b2BCustomerMapping)) {
//                return form(b2BCustomerMapping, model);
                ajaxJsonEntity.setSuccess(false);
                return ajaxJsonEntity;
            }
            User user = UserUtils.getUser();
            if (user.getId() != null) {
                b2BCustomerMapping.setCreateById(user.getId());
                b2BCustomerMapping.setUpdateById(user.getId());
                b2BCustomerMapping.setShopId(StringUtils.trim(b2BCustomerMapping.getShopId()));
                b2BCustomerMapping.setShopName(StringUtils.trim(b2BCustomerMapping.getShopName()));
                MSErrorCode mSResponse = b2BCustomerMappingService.save(b2BCustomerMapping);
                if (mSResponse.getCode() == 0) {
//                    addMessage(redirectAttributes, "保存成功");
                    ajaxJsonEntity.setSuccess(true);
                    ajaxJsonEntity.setMessage("保存成功");
                } else {
//                    addMessage(redirectAttributes, mSResponse.getMsg());
                    ajaxJsonEntity.setSuccess(false);
                    ajaxJsonEntity.setMessage(mSResponse.getMsg());
                }
            } else {
//                addMessage(redirectAttributes, "当前用户不存在");
                ajaxJsonEntity.setMessage("当前用户不存在");
            }
        } else {
//            addMessage(redirectAttributes, MSErrorCode.MICROSERVICE_DISABLED.msg);
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
//        return "redirect:" + adminPath + "/b2bcenter/md/customer/getList?repage";
        return ajaxJsonEntity;
    }

    /**
     * 删除数据
     *
     * @param entity
     * @return
     */
    @RequiresPermissions("md:b2bcustomer:edit")
    @RequestMapping(value = "delete")
    @ResponseBody
    public AjaxJsonEntity delete(B2BCustomerMapping entity, RedirectAttributes redirectAttributes) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Integer> msResponse = b2BCustomerMappingService.delete(entity);
            if (msResponse.getCode() == 0) {
                ajaxJsonEntity.setSuccess(true);
                ajaxJsonEntity.setMessage("删除成功");
            } else {
                ajaxJsonEntity.setMessage(msResponse.getMsg());
            }
        } else {
            ajaxJsonEntity.setMessage(MSErrorCode.MICROSERVICE_DISABLED.msg);
        }
//        return "redirect:" + adminPath + "/b2bcenter/md/customer/getList?repage";
        return ajaxJsonEntity;
    }

    /**
     * 检查店铺id是否以及存在
     *
     * @param id
     * @param dataSoruce
     * @param shopId
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "checkShopId")
    public String checkShopId(Long id, String shopId, Integer dataSoruce, HttpServletResponse response) {
        String result = "true";
        response.setContentType("application/json; charset=UTF-8");
        if (msProperties.getB2bcenter().getEnabled()) {
            MSResponse<Long> msResponse = b2BCustomerMappingService.checkShopId(shopId, dataSoruce);
            if (msResponse.getData() != null && msResponse.getData() > 0) {
                if (id == -1) {
                    result = "该店铺id已存在";
                } else if (id != msResponse.getData()) {
                    result = "该店铺id已存在";
                }
            }
        } else {
            result = MSErrorCode.MICROSERVICE_DISABLED.msg;
        }
        return result;
    }

    /**
     * 根据来源返回所有店铺列表
     * @param dataSource 来源
     */
    @ResponseBody
    @RequestMapping(value = "getShopList")
    public AjaxJsonEntity getShopList(@RequestParam int dataSource, @RequestParam(required = false) Long customerId, HttpServletResponse response) {

        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setMessage("OK");
        if (dataSource < B2BDataSourceEnum.KKL.id) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("来源错误");
            return jsonEntity;
        }

        try {
            List<B2BCustomerMapping> shopList;
            B2BDataSourceEnum dataSourceEnum = dataSource == 1 ? B2BDataSourceEnum.KKL : B2BDataSourceEnum.valueOf(dataSource);
//            shopList = b2BCustomerMappingService.getShopListByCustomer(B2BDataSourceEnum.get(dataSource), customerId);
            shopList = b2BCustomerMappingService.getShopListByCustomer(dataSourceEnum, customerId);
            if (shopList == null) {
                shopList = Lists.newArrayList();
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(shopList);
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage().toString());
        }
        return jsonEntity;
    }

    /**
     * 根据来源返回客户集合
     * @param dataSource 数据源
     */
    @ResponseBody
    @RequestMapping(value = "ajax/getCustomerListByDataSource")
    public AjaxJsonEntity getCustomerListByDataSource(@RequestParam int dataSource, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        jsonEntity.setMessage("OK");
        if (dataSource <= B2BDataSourceEnum.KKL.id) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("数据源错误");
            return jsonEntity;
        }
        try{
            List<Customer> list =Lists.newArrayList();
            List<B2BCustomerMapping> customerMappingList = b2BCustomerMappingService.getListByDataSource(B2BDataSourceEnum.get(dataSource));
            if(customerMappingList!=null &&customerMappingList.size()>0){
                //根据客户id去重
                List<B2BCustomerMapping> distinctList = customerMappingList.stream().collect(collectingAndThen(
                        toCollection(() -> new TreeSet<>(comparingLong(B2BCustomerMapping::getCustomerId))), ArrayList::new));
                if(distinctList!=null && distinctList.size()>0){
                    Map<Long,Customer> map = CustomerUtils.getAllCustomerBasicMap();
                    if(map!=null && map.size()>0){
                        Customer customer;
                        for(B2BCustomerMapping item:distinctList){
                            customer = map.get(item.getCustomerId());
                            if(customer!=null){
                                list.add(customer);
                            }
                        }
                    }
                }
            }
            jsonEntity.setSuccess(true);
            jsonEntity.setData(list);
        }catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage().toString());
        }
        return jsonEntity;
    }


    /**
     * 工单转换列表一键配置客户店铺
     * @param b2BCustomerMapping 数据源
     */
    @RequestMapping("addCustomerMappingForm")
    public String addCustomerMappingForm(B2BCustomerMapping b2BCustomerMapping, Model model){
        model.addAttribute("b2BCustomerMapping",b2BCustomerMapping);
        return "modules/b2bcenter/sd/addCustomerMappingForm";
    }

    /**
     * 根据客户id获取
     * @param strCustomerId 数据源
     */
    @RequestMapping("findCustomerAccountListByCustomerId")
    @ResponseBody
    public AjaxJsonEntity findCustomerAccountListByCustomerId(String strCustomerId){
          Long customerId = 0L;
          AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
          try {
              customerId = Long.valueOf(strCustomerId);
              List<User> list = b2BCustomerMappingService.findCustomerAccountListByCustomerId(customerId);
              if(list!=null){
                  ajaxJsonEntity.setData(list);
              }else{
                  ajaxJsonEntity.setData(Lists.newArrayList());
              }
          }catch (Exception e){
              ajaxJsonEntity.setSuccess(false);
              ajaxJsonEntity.setMessage("读取客户账号错误:"+e.getMessage());
          }
          return ajaxJsonEntity;
    }

}
