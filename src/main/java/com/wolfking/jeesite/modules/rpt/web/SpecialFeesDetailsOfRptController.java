package com.wolfking.jeesite.modules.rpt.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.rpt.entity.SpecialFeesDetailsOfRptEntity;
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
@RequestMapping(value = "${adminPath}/rpt/order/specialExpenses")
public class SpecialFeesDetailsOfRptController extends BaseRptController {
    @Autowired
    SpecialFeesDetailsOfRptService specialFeesDetailsOfRptService;

    @Autowired
    private SystemService systemService;


    @SuppressWarnings("deprecation")
    @RequestMapping(value = "specialFeesDetailsOfRpt")
    public String SpecialFeesDetailsOfRpt(Integer year, Integer month, Integer day, Long provinceId, Integer chargeFlag, Integer productCategoryId, Model model) {
        List<SpecialFeesDetailsOfRptEntity> list = new ArrayList<>();
        User user = UserUtils.getUser();
        List<Long> productCategoryIds = Lists.newArrayList();
        if (user.getUserType() == User.USER_TYPE_SERVICE.intValue() || user.getUserType() == User.USER_TYPE_INNER.intValue()) {
            productCategoryIds = systemService.getAuthorizedProductCategoryIds(user.getId());
        }
        if (productCategoryId != null && productCategoryId != 0) {
            productCategoryIds = Lists.newArrayList();
            productCategoryIds.add(Long.valueOf(productCategoryId));
        }
        list = specialFeesDetailsOfRptService.getSpecialOrderFee(year, month, day, provinceId, chargeFlag, productCategoryIds);
        model.addAttribute("chargeFlag", chargeFlag);
        model.addAttribute("list", list);
        return "modules/rpt/specialFeesDetailsOfRpt";
    }

    @RequestMapping(value = "specialFeesDetailsOfRptChargeDate")
    public String SpecialFeesDetailsOfRptChargeDate(Integer year, Integer month, Integer day, Long provinceId, Integer chargeFlag, Integer productCategoryId, Model model) {
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
        list = specialFeesDetailsOfRptService.getSpecialOrderFeeOfChargeDate(year, month, day, provinceId, chargeFlag, productCategoryIds);
        model.addAttribute("chargeFlag", chargeFlag);
        model.addAttribute("list", list);
        return "modules/rpt/specialFeesDetailsOfRpt";
    }

}
