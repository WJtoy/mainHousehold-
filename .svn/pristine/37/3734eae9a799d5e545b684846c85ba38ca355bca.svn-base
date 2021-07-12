package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrice;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrices;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.TimeLinessPriceService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSProductTimeLinessService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "${adminPath}/md/timelinessPriceNew")
public class TimelinessPriceNewController extends BaseController {
    @Autowired
    private TimeLinessPriceService timeLinessPriceService;
    @Autowired
    private MSProductTimeLinessService msProductTimeLinessService;
    @Autowired
    private ProductCategoryService productCategoryService;

    @RequiresPermissions("md:timelinessprice:view")
    @RequestMapping(value = {"list", ""})
    public String list(TimeLinessPrice insurancePrice, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<Dict> levelList = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);

        if (insurancePrice.getCategory().getId() == null) {
            insurancePrice.getCategory().setId(0L);
        }
        Long productCategoryId = insurancePrice.getCategory().getId();
        List<TimeLinessPrice> list;
        if (productCategoryId == 0) {
            list = timeLinessPriceService.findAllList();

        } else {
            list = msProductTimeLinessService.getPrices(productCategoryId);
        }
        if(list != null){
            list = list.stream().sorted(Comparator.comparing(i -> i.getTimeLinessLevel().getValue())).collect(Collectors.toList());
        }else {
            return "modules/md/timelinessPriceNewList";
        }
        for (TimeLinessPrice timeLinessPrice : list) {
            timeLinessPrice.setCategory(productCategoryService.getFromCache(timeLinessPrice.getCategory().getId()));
        }

        List<HashMap<String, Object>> listmap = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        HashSet<HashMap<String, Object>> set = new HashSet<>();

        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                map = new HashMap<>();
                map.put("categoryId", list.get(i).getCategory().getId());
                map.put("categoryName", list.get(i).getCategory().getName());
                set.add(map);
            }

            List<TimeLinessPrice> timeLinessPrices;
            for (HashMap<String, Object> map1 : set) {
                map = new HashMap<>();
                map.put("categoryId", map1.get("categoryId"));
                map.put("categoryName", map1.get("categoryName"));
                timeLinessPrices = new LinkedList<>();
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).getCategory().getId() == Long.valueOf(map1.get("categoryId").toString())) {
                        timeLinessPrices.add(list.get(j));
                    }
                }
                map.put("timeLinessPriceList", timeLinessPrices);
                listmap.add(map);
            }
        }

        model.addAttribute("listmap", listmap);
        model.addAttribute("levelList", levelList);
        return "modules/md/timelinessPriceNewList";
    }

    @RequiresPermissions("md:timelinessprice:edit")
    @RequestMapping(value = "forms")
    public String forms(TimeLinessPrices timeLinessPrices, Model model) {
        String viewModel = "modules/md/timelinessPriceNewForms";
        List<Dict> status = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
        if (status == null || status.size() == 0) {
            addMessage(model, "请先设置时效等级");
            model.addAttribute("timeLinessPrices", timeLinessPrices);
            model.addAttribute("canAction", false);
            return viewModel;
        }
        if (timeLinessPrices.getCategory() != null && timeLinessPrices.getCategory().getId() != null) {
            Long categoryId = timeLinessPrices.getCategory().getId();
            ProductCategory category = ProductUtils.getProductCategory(categoryId);
            if (category == null) {
                addMessage(model, "读取产品类别错误，请重试!");
                model.addAttribute("timeLinessPrices", timeLinessPrices);
                model.addAttribute("canAction", false);
                return viewModel;
            }
            List<TimeLinessPrice> prices = timeLinessPriceService.getTimeLinessPrices(categoryId);
            prices = prices.stream().sorted(Comparator.comparing(i -> i.getTimeLinessLevel().getValue())).collect(Collectors.toList());
            if (prices != null && prices.size() > 0) {
                timeLinessPrices.setCategory(category);
                timeLinessPrices.setList(prices);
                model.addAttribute("timeLinessPrices", timeLinessPrices);
                model.addAttribute("canAction", true);
                return viewModel;
            }
        }

        List<TimeLinessPrice> list = new ArrayList<>();
        for (Dict dict : status) {
            TimeLinessPrice entity = new TimeLinessPrice();
            entity.setAmount(0.0);
            entity.setTimeLinessLevel(dict);
            list.add(entity);
        }
        timeLinessPrices.setList(list);

        model.addAttribute("timeLinessPrices", timeLinessPrices);
        model.addAttribute("canAction", true);
        return viewModel;
    }



    @RequiresPermissions("md:timelinessprice:edit")
    @RequestMapping(value = "saveTimelinessPricesNew")
    @ResponseBody
    public AjaxJsonEntity saveTimeLinessPrices(TimeLinessPrices timeLinessPrices, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            if (timeLinessPrices.getCategory() != null && timeLinessPrices.getCategory().getId() != null) {
                timeLinessPriceService.save(timeLinessPrices);
                ajaxJsonEntity.setSuccess(true);
            } else {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage("保存失败，产品类别有误。请重试。");

            }

        } catch (Exception e) {
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }

        return ajaxJsonEntity;
    }

    /**
     * @param categoryId         产品分类ID
     * @param redirectAttributes
     * @return
     */
    @RequiresPermissions("md:timelinessprice:edit")
    @RequestMapping(value = "delete")
    public String delete(Long categoryId, RedirectAttributes redirectAttributes) {

        timeLinessPriceService.deleteByCategoryId(categoryId);
        addMessage(redirectAttributes, "删除成功");
        return "redirect:" + adminPath + "/md/timelinessPriceNew?repage";
    }

}
