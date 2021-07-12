package com.wolfking.jeesite.modules.servicepoint.sd.web;


import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpServicePointService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderAuxiliaryMaterialService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderCacheReadService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;


/**
 * 网点工单信息
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/sd/orderInfo/")
public class ServicePointOrderInfoController extends BaseController {

    @Autowired
    private SpServicePointService servicePointService;

    @Autowired
    private SpOrderCacheReadService orderCacheReadService;

    @Autowired
    private SpOrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

    /**
     * 查看工单详情
     *
     * @param orderId 订单id
     */
    @RequestMapping("showOrderDetailInfo")
    public String showOrderDetailInfo(String orderId, String quarter, HttpServletRequest request, Model model) {
        boolean errorFlag = false;
        Order order = new Order();
        Long lid = Long.valueOf(orderId);
        boolean hasAuxiliaryMaterils = false;
        if (lid == null || lid <= 0) {
            errorFlag = true;
            addMessage(model, "订单参数错误");
        } else {
            order = orderCacheReadService.getOrderById(lid, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            } else {
                ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
                if (servicePoint != null && servicePoint.getId() != null & servicePoint.getId() > 0) {
                    Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
                hasAuxiliaryMaterils = orderAuxiliaryMaterialService.hasAuxiliaryMaterials(order.getId(), order.getQuarter());
            }
        }
        model.addAttribute("order", order);
        model.addAttribute("hasAuxiliaryMaterils", hasAuxiliaryMaterils ? 1 : 0);
        model.addAttribute("errorFlag", errorFlag);
        if (!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        } else {
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        return "modules/servicePoint/sd/orderInfo/orderDefailInfoForm";
    }

}

