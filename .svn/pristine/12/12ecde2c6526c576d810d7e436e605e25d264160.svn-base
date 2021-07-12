package com.wolfking.jeesite.modules.md.web;

import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrice;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrices;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.TimeLinessPriceService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping(value = "${adminPath}/md/timeLinessPrice")
public class TimeLinessPriceController extends BaseController {
	@Autowired
	private TimeLinessPriceService timeLinessPriceService;

	@Autowired
	private ProductCategoryService productCategoryService;

	@RequiresPermissions("md:timelinessprice:view")
	@RequestMapping(value = {"list", ""})
	public String list(TimeLinessPrice insurancePrice, HttpServletRequest request, HttpServletResponse response, Model model) {
		List<Dict> levelList = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
		List<TimeLinessPrice> list= timeLinessPriceService.findAllList();

		for (TimeLinessPrice timeLinessPrice:list) {
			timeLinessPrice.setCategory(productCategoryService.getFromCache(timeLinessPrice.getCategory().getId()));
		}

		List<HashMap<String,Object>> listmap=new ArrayList<>();
		HashMap<String,Object> map=new HashMap<>();
		HashSet<HashMap<String,Object>> set=new HashSet<>();

		if (list!=null && list.size()>0)
		{
			for (int i=0;i<list.size();i++){
				map=new HashMap<>();
				map.put("categoryId",list.get(i).getCategory().getId());
				map.put("categoryName",list.get(i).getCategory().getName());
				set.add(map);
			}

			List<TimeLinessPrice> timeLinessPrices;
			for (HashMap<String,Object> map1 : set) {
				map=new HashMap<>();
				map.put("categoryId",map1.get("categoryId"));
				map.put("categoryName",map1.get("categoryName"));
				timeLinessPrices=new LinkedList<>();
				for (int j=0;j<list.size();j++){
					if (list.get(j).getCategory().getId()==Long.valueOf(map1.get("categoryId").toString()))
					{
						timeLinessPrices.add(list.get(j));
					}
				}
				map.put("timeLinessPriceList",timeLinessPrices);
				listmap.add(map);
			}
		}

        model.addAttribute("listmap", listmap);
		model.addAttribute("levelList",levelList);
		return "modules/md/timeLinessPriceList";
	}

	@RequiresPermissions("md:timelinessprice:edit")
	@RequestMapping(value = "forms")
	public String forms(TimeLinessPrices timeLinessPrices, Model model) {
		String viewModel = "modules/md/timeLinessPriceForms";
		List<Dict> status = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
		if (status == null || status.size() == 0){
			addMessage(model,"请先设置时效等级");
			model.addAttribute("timeLinessPrices", timeLinessPrices);
			model.addAttribute("canAction", false);
			return viewModel;
		}
		if(timeLinessPrices.getCategory() != null && timeLinessPrices.getCategory().getId() != null) {
			Long categoryId = timeLinessPrices.getCategory().getId();
			ProductCategory category = ProductUtils.getProductCategory(categoryId);
			if (category == null) {
				addMessage(model, "读取产品类别错误，请重试!");
				model.addAttribute("timeLinessPrices", timeLinessPrices);
				model.addAttribute("canAction", false);
				return viewModel;
			}
			List<TimeLinessPrice> prices = timeLinessPriceService.getTimeLinessPrices(categoryId);
			if (prices != null && prices.size() > 0) {
				timeLinessPrices.setCategory(category);
				timeLinessPrices.setList(prices);
				model.addAttribute("timeLinessPrices", timeLinessPrices);
				model.addAttribute("canAction", true);
				return viewModel;
			}
		}

		List<TimeLinessPrice> list=new ArrayList<>();
		for (Dict dict:status) {
			TimeLinessPrice entity=new TimeLinessPrice();
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
	@RequestMapping(value = "saveTimeLinessPrices")
	public String saveTimeLinessPrices(TimeLinessPrices timeLinessPrices, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (timeLinessPrices.getCategory()!=null && timeLinessPrices.getCategory().getId()!=null){
			timeLinessPriceService.save(timeLinessPrices);
			addMessage(redirectAttributes, "保存时效价格成功");
		}else
		{
			addMessage(model, "保存失败，产品类别有误。请重试。");
			return forms(timeLinessPrices, model);
		}

		return "redirect:"+ adminPath+"/md/timeLinessPrice?repage";
	}

	/**
	 *
	 * @param categoryId 产品分类ID
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("md:timelinessprice:edit")
	@RequestMapping(value = "delete")
	public String delete(Long categoryId, RedirectAttributes redirectAttributes) {

		timeLinessPriceService.deleteByCategoryId(categoryId);
		addMessage(redirectAttributes, "删除成功");
		return "redirect:"+adminPath+"/md/timeLinessPrice?repage";
	}

}
