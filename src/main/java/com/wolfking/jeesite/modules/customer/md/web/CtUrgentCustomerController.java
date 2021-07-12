package com.wolfking.jeesite.modules.customer.md.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.md.service.CtUrgentCustomerService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.UrgentCustomer;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerUrgentService;
import com.wolfking.jeesite.ms.providermd.service.MSUrgentLevelService;
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
@RequestMapping(value = "${adminPath}/customer/md/urgentcustomer")
public class CtUrgentCustomerController extends BaseController {

    @Autowired
    private MSUrgentLevelService msUrgentLevelService;

    @Autowired
    private MSCustomerUrgentService msCustomerUrgentService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private CtUrgentCustomerService ctUrgentCustomerService;


    @RequiresPermissions("customer:md:urgentcustomer:view")
    @RequestMapping(value = "customerList")
    public String customerList(UrgentCustomer urgentCustomer, HttpServletRequest request, HttpServletResponse response, Model model) {

        List<UrgentLevel> urgentLevelList = msUrgentLevelService.findAllList();
        //是客户账号则设置查询条件
        User user= UserUtils.getUser();
        if (user.isCustomer()){
            urgentCustomer.setCustomer(user.getCustomerAccountProfile().getCustomer());
        }
        Page<UrgentCustomer> page = msCustomerUrgentService.findList(new Page<>(request, response), urgentCustomer);
        List<HashMap<String,Object>> listmap=new ArrayList<>();
        if (page.getList()!=null && page.getList().size()>0){
            List<UrgentCustomer> list=page.getList();
            HashMap<String,Object> map=new HashMap<>();
            for (UrgentCustomer entity:list) {
                Long customerId=entity.getCustomer().getId();
                Customer customer = msCustomerService.get(customerId);
                entity.setCustomer(customer);

                map=new HashMap<>();
                map.put("customerId",entity.getCustomer()==null? customerId:entity.getCustomer().getId());
                map.put("customerName",entity.getCustomer()==null? "":entity.getCustomer().getName());

                List<AreaUrgentModel> areaUrgentModels = ctUrgentCustomerService.findListByCustomerId(customerId);
                map.put("AreaUrgentModelList",areaUrgentModels);
                listmap.add(map);
            }
            //按客户名称排序
            listmap = listmap.stream().sorted(Comparator.comparing(t->t.get("customerName").toString())).collect(Collectors.toList());
        }
        model.addAttribute("page", page);
        model.addAttribute("listmap", listmap);
        model.addAttribute("urgentLevelList",urgentLevelList);

        return "modules/customer/md/ctUrgentCustomerListForCustomer";
    }
}
