package com.wolfking.jeesite.modules.rpt.web;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointModel;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/rpt/common")
public class RptCommonController extends BaseController {

    @Autowired
    private ServicePointService servicePointService;

    /**
     * 服务网点选择列表
     */
    @RequiresUser
    @RequestMapping(value = "servicePointSelector")
    public String select(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model,Integer searchTag) {
        Page<ServicePoint> page = new Page<>(request, response);
        ServicePoint servicePoint = new ServicePoint();
        if(searchTag==2){
            BeanUtils.copyProperties(servicePointModel, servicePoint);
            servicePoint.setInvoiceFlag(-1);

            servicePoint.setName(servicePointModel.getName());
            servicePoint.setServicePointNo(servicePointModel.getServicePointNo());

            if (servicePoint.getFinance() != null) {
                servicePoint.getFinance().setInvoiceFlag(-1);
                servicePoint.getFinance().setDiscountFlag(-1);
            }
            page.setPageSize(8);
            servicePoint.setOrderBy("s.order_count desc,s.servicepoint_no");//sort
            servicePoint.setAutoPlanFlag(-1);    //自动派单
            servicePoint.setInsuranceFlag(-1);   //购买保险
            servicePoint.setTimeLinessFlag(-1);  //快可立补贴
            servicePoint.setCustomerTimeLinessFlag(-1);  //客户时效
            servicePoint.setUseDefaultPrice(-1); //结算标准
            page = servicePointService.findPage(page, servicePoint);
        }
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePointModel);
        model.addAttribute("searchTag", searchTag);
        return "modules/rpt/servicePointSelector";
    }
}
