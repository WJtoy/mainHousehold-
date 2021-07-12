package com.wolfking.jeesite.modules.sd.web;

import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointModel;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ryan Lu
 * @version 1.0
 * 订单模组共用控制器
 * 如网点选择列表等
 * @date 2020/6/1 3:48 下午
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/sd/common")
public class OrderCommonController {

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private AreaService areaService;

    //region Selector

    /**
     * 网点选择列表
     * @param searchTag 1:不查数据 2:根据条件分页查询
     */
    @RequiresUser
    @RequestMapping(value = "/selectServicePoint")
    public String selectServicePoint(ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model,Integer searchTag) {
        Page<ServicePoint> page = new Page<>(request, response);
        if(searchTag==2){
            if (servicePointModel.getArea() != null && servicePointModel.getArea().getId() != null && StringUtils.isBlank(servicePointModel.getArea().getName())) {
                Area area = areaService.getFromCache(servicePointModel.getArea().getId());
                servicePointModel.getArea().setName(area.getFullName());
            }
            ServicePoint servicePoint = new ServicePoint();
            BeanUtils.copyProperties(servicePointModel, servicePoint);
            servicePoint.setInvoiceFlag(-1);
            if (servicePointModel.getSearchType() == 0) {
                servicePoint.setName(servicePointModel.getName());
                servicePoint.setServicePointNo("");
            } else {
                servicePoint.setServicePointNo(servicePointModel.getName());
                servicePoint.setName("");
            }
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
        String showArea = request.getParameter("showArea");
        model.addAttribute("showArea", StringUtils.trimToEmpty(showArea));
        model.addAttribute("searchTag", searchTag);
        return "modules/sd/common/servicePointSelect";
    }

    //endregion Selector

}
