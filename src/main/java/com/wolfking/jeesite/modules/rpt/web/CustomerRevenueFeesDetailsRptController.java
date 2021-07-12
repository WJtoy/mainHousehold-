package com.wolfking.jeesite.modules.rpt.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.rpt.entity.CustomerRevenueFeesRptEntity;
import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import com.wolfking.jeesite.modules.rpt.service.CustomerRevenueFeesRptService;
import com.wolfking.jeesite.modules.rpt.service.CustomerSpecialFeesDetailsOfRptService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/rpt/order/customerRevenueExpenses")
public class CustomerRevenueFeesDetailsRptController  extends BaseRptController{

    @Autowired
    private SystemService systemService;

    @Autowired
    CustomerRevenueFeesRptService customerRevenueFeesRptService;

    @RequestMapping(value = "customerRevenueDetailsOfRptChargeDate")
    public String SpecialFeesDetailsOfRptChargeDate(Integer year, Integer month, Long customerId, Integer productCategoryId, Integer finishOrder,Double receivableCharge, Double payableCharge,Double orderGrossProfit,Double everySingleGrossProfit ,Model model) {
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());

        }
        if (productCategoryId != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(productCategoryId.longValue());
        }
        CustomerRevenueFeesRptEntity entity = customerRevenueFeesRptService.getCustomerRevenueFeeOfChargeDate(year, month,productCategoryIds,customerId);
        entity.setFinishOrder(finishOrder);
        entity.setReceivableCharge(receivableCharge);
        entity.setPayableCharge(payableCharge);
        entity.setOrderGrossProfit(orderGrossProfit);
        entity.setEverySingleGrossProfit(everySingleGrossProfit);
        model.addAttribute("entity", entity);
        return "modules/rpt/customerRevenueFeeDetails";
    }
}
