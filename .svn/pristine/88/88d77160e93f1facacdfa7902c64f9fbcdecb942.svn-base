package com.wolfking.jeesite.modules.sys.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.sys.SysOfficeRegion;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserServiceRegion;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.OfficeRegionService;
import com.wolfking.jeesite.modules.sys.service.OfficeService;
import com.wolfking.jeesite.modules.sys.service.UserKeFuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "${adminPath}/sys/officeRegion")
@Slf4j
public class OfficeRegionController extends BaseController {
    @Autowired
    private OfficeRegionService officeRegionService;

    @Autowired
    private OfficeService officeService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private UserKeFuService userKeFuService;

    @RequiresPermissions("sys:officeRegion:view")
    @RequestMapping(value = "list")
    public String list(SysOfficeRegion sysOfficeRegion, Model model) {

        List<Office> officeList = userKeFuService.orderByOffice("客服", User.USER_TYPE_SERVICE);//获取客服部门
        List<ProductCategory> productCategoryList = Lists.newArrayList();
        List<UserServiceRegion> provinceRegionList = Lists.newArrayList();
        if (sysOfficeRegion.getOfficeId() != null) {
            productCategoryList = officeRegionService.getProductCategoryList(sysOfficeRegion.getOfficeId());
            provinceRegionList = officeRegionService.getOfficeRegion(sysOfficeRegion.getOfficeId());
        }

        model.addAttribute("officeList", officeList);
        model.addAttribute("sysOfficeRegion", sysOfficeRegion);
        model.addAttribute("productCategoryList", productCategoryList);
        model.addAttribute("provinceRegionList", provinceRegionList);
        return "modules/sys/officeRegionList";
    }

    @RequiresPermissions("sys:officeRegion:edit")
    @RequestMapping(value = "form")
    public String form(Long officeId, Long productCategoryId, Long provinceId, Long cityId, String beGrantedAreaIds, String unauthorizedAreaIds, Model model) {
        String officeName = "";
        String productCategoryName = "";
        String areaName = "";
        Office office = officeService.get(officeId);
        if (office != null) {
            officeName = office.getName();
        }
        ProductCategory productCategory = productCategoryService.getFromCache(productCategoryId);
        if (productCategory != null) {
            productCategoryName = productCategory.getName();
        }
        Area city = areaService.get(cityId);
        if (city != null) {
            areaName = city.getParent().getName() + city.getName();
        }
        Map<String, List<Area>> regionArea = officeRegionService.getRegionArea(beGrantedAreaIds, unauthorizedAreaIds);
        List<User> userKeFuList = officeRegionService.getUserKeFuList(officeId, productCategoryId);
        model.addAttribute("officeName", officeName);
        model.addAttribute("officeId", officeId);
        model.addAttribute("productCategoryName", productCategoryName);
        model.addAttribute("areaName", areaName);
        model.addAttribute("beGrantedArea", regionArea.get("beGrantedArea"));
        model.addAttribute("unauthorizedArea", regionArea.get("unauthorizedArea"));
        model.addAttribute("userKeFuList", userKeFuList);
        model.addAttribute("cityId", cityId);
        model.addAttribute("provinceId", provinceId);
        return "modules/sys/officeRegionForm";
    }


    @RequiresPermissions("sys:officeRegion:edit")
    @RequestMapping(value = "save")
    @ResponseBody
    public AjaxJsonEntity save(Long keFuId, Long provinceId, Long cityId, String areaRegion) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (Global.isDemoMode()) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("演示模式，不允许操作！");
        }
        try {
            officeRegionService.save(keFuId, provinceId, cityId, areaRegion); // 保存信息
            ajaxJsonEntity.setMessage("保存成功");
        } catch (Exception ex) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存失败,失败原因:" + ex.getMessage());
        }
        return ajaxJsonEntity;
    }
}
