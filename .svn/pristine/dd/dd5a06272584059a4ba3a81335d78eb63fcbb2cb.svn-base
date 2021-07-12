package com.wolfking.jeesite.modules.customer.md.web;


import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerTimelinessService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerTimeliness;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaTimelinessModel;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerTimelinessService;
import com.wolfking.jeesite.ms.providermd.service.MSTimelinessLevelService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/customer/md/customertimeliness")
public class CtCustomerTimelinessController extends BaseController {
    @Autowired
    private MSCustomerTimelinessService msCustomerTimelinessService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private CtCustomerTimelinessService ctCustomerTimelinessService;

    @Autowired
    private MSTimelinessLevelService msTimelinessLevelService;

    /**
     * 专门开给客户看的时效费用
     * @param customerTimeliness
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("customer:md:customertimeliness:view")
    @RequestMapping(value = "customerList")
    public String customerlist(CustomerTimeliness customerTimeliness, HttpServletRequest request, HttpServletResponse response, Model model) {
        //是客户账号则设置查询条件
        User user= UserUtils.getUser();
        if (user.isCustomer()){
            customerTimeliness.setCustomer(user.getCustomerAccountProfile().getCustomer());
        }
        Page<CustomerTimeliness> page = msCustomerTimelinessService.findList(new Page<>(request, response), customerTimeliness); //add on 2019-8-1 微服务调用
        List<HashMap<String,Object>> listmap = new ArrayList<>();
        if (page.getList()!=null && page.getList().size()>0){
            List<CustomerTimeliness> list = page.getList();
            HashMap<String,Object> map = new HashMap<>();
            for (CustomerTimeliness entity:list) {
                Long customerId=entity.getCustomer().getId();
                Customer customer = msCustomerService.get(customerId);            // 改成调用微服务  //2019-7-31
                entity.setCustomer(customer);

                map = new HashMap<>();
                map.put("customerId", entity.getCustomer() == null ? customerId:entity.getCustomer().getId());  // update on 2019-7-31
                map.put("customerName", entity.getCustomer() == null? "":entity.getCustomer().getName());       // update on 2019-7-31

                List<AreaTimelinessModel> list1 = ctCustomerTimelinessService.findListByCustomerId(customerId);
                map.put("areaTimelinessModelList",list1);
                listmap.add(map);
            }

            //按照客户名称排序
            // listmap.sort((HashMap map1,HashMap map2)->map1.get("customerName").toString().compareTo(map2.get("customerName").toString()));
            listmap = listmap.stream().sorted(Comparator.comparing(t->t.get("customerName").toString())).collect(Collectors.toList());
        }
        List<TimelinessLevel> timelineList = msTimelinessLevelService.findAllList();
        if (timelineList == null) {
            timelineList = Lists.newArrayList();
        }
        model.addAttribute("page", page);
        model.addAttribute("customerTimeliness", customerTimeliness);
        model.addAttribute("timelineList", timelineList);
        model.addAttribute("listmap", listmap);
        return "modules/customer/md/ctCustomerTimelinessListForCustomer";
    }
}
