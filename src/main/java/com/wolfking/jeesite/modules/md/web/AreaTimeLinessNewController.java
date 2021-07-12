package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.AreaTimeLiness;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.AreaTimeLinessService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "${adminPath}/md/areaTimelinessNew")
public class AreaTimeLinessNewController extends BaseController {

    @Autowired
    private AreaTimeLinessService areaTimeLinessService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;


    /**
     * 查看列表
     */
    @RequiresPermissions("md:timelinessprice:view")
    //@RequestMapping(value = {"list", ""})
    public String list(AreaTimeLiness areaTimeLiness, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<Area> provinceList = AreaUtils.getProvinceList();
        if (areaTimeLiness.isSearching()) {
            List<AreaTimeLiness> list = areaTimeLinessService.findListNew(areaTimeLiness);
            if (list == null) {
                list = Lists.newArrayList();
            }
            model.addAttribute("list", list);
        }
        model.addAttribute("areaTimeliness", areaTimeLiness);
        model.addAttribute("provinceList", provinceList);
        return "modules/md/areaTimelinessNewList";
    }

    /**
     * 查看列表
     */
    @RequiresPermissions("md:timelinessprice:view")
    @RequestMapping(value = {"list", ""})
    public String newlist(AreaTimeLiness areaTimeLiness, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<Area> provinceList = AreaUtils.getProvinceList();
        if (areaTimeLiness.isSearching()) {
            List<Map<String,Object>> list = areaTimeLinessService.findAllProductCategoryList(areaTimeLiness);
            model.addAttribute("list", list);
        }
        List<ProductCategory> productCategoryList = msProductCategoryNewService.findAllListForMDWithEntity();
        if (!ObjectUtils.isEmpty(productCategoryList)) {
            productCategoryList = productCategoryList.stream().sorted(Comparator.comparing(r->r.getId())).collect(Collectors.toList());
        }
        model.addAttribute("areaTimeliness", areaTimeLiness);
        model.addAttribute("provinceList", provinceList);
        model.addAttribute("productCategoryList", productCategoryList);

        return "modules/md/areaTimelinessNewerList";
    }

    /**
     * 保存
     */
    @RequiresPermissions("md:timelinessprice:edit")
    @RequestMapping("save")
    @ResponseBody
    public AjaxJsonEntity enable(@RequestBody List<AreaTimeLiness> areaTimeLinessList, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(false);
        try {
            if (areaTimeLinessList != null && areaTimeLinessList.size() > 0) {
                areaTimeLinessService.saveBatch(areaTimeLinessList);
                jsonEntity.setSuccess(true);
            } else {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数集合为空");
            }
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }


}
