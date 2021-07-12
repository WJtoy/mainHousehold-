package com.wolfking.jeesite.modules.rpt.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
import com.wolfking.jeesite.modules.rpt.service.CustomerSpecialFeesDetailsOfRptService;
import com.wolfking.jeesite.modules.rpt.service.SpecialFeesDetailsOfRptService;
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
@RequestMapping(value = "${adminPath}/rpt/order/customerSpecialExpenses")
public class CustomerSpecialFeesDetailsOfRptController extends BaseRptController {
    @Autowired
    CustomerSpecialFeesDetailsOfRptService customerSpecialFeesDetailsOfRptService;

    @Autowired
    private SystemService systemService;



    @RequestMapping(value = "specialFeesDetailsOfRptChargeDate")
    public String SpecialFeesDetailsOfRptChargeDate(Integer year, Integer month, Integer day, Long provinceId, Integer chargeFlag, Integer productCategoryId,Long customerId, Model model) {
        List<SpecialFeesDetailsOfRptEntity> list = new ArrayList<>();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());

        }
        if (productCategoryId != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(productCategoryId.longValue());
        }
        list = customerSpecialFeesDetailsOfRptService.getSpecialOrderFeeOfChargeDate(year, month, day, provinceId, chargeFlag, productCategoryIds,customerId);
        model.addAttribute("chargeFlag", chargeFlag);
        model.addAttribute("list", list);
        return "modules/rpt/specialFeesDetailsOfRpt";
    }

}
