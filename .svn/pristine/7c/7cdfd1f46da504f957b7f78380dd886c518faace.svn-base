package com.wolfking.jeesite.ms.tmall.md.controller;

import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "${adminPath}/tmall/md/b2bcustomer")
public class B2bCustomerMapController extends BaseController {

    @Autowired
    private B2bCustomerMapService customerMapService;

    @Autowired
    private CustomerService customerService;

//    @ModelAttribute
//    public B2bCustomerMap get(@RequestParam(required = false) Long id) {
//        if (id != null) {
//            return customerMapService.get(id);
//        } else {
//            return new B2bCustomerMap();
//        }
//    }

//    @RequiresPermissions("md:b2bcustomer:view")
//    @RequestMapping(value = { "list", "" })
//    public String list(B2bCustomerMap b2bCustomerMap, HttpServletRequest request, HttpServletResponse response, Model model)
//    {
//        Page<B2bCustomerMap> page = customerMapService.findPage(new Page<>(request, response), b2bCustomerMap);
//
//
//        List<B2bCustomerMap> list=page.getList();
//        for (B2bCustomerMap entity:list) {
//            entity.setCustomerName(customerService.getFromCache(entity.getCustomerId()).getName());
//        }
//
//        model.addAttribute("page", page);
//        return "modules/tmall/md/b2bCustomerMapList";
//    }

//    @RequiresPermissions("md:b2bcustomer:view")
//    @RequestMapping(value = "form")
//    public String form(B2bCustomerMap b2bCustomerMap, Model model)
//    {
//        model.addAttribute("b2bCustomerMap", b2bCustomerMap);
//        return "modules/tmall/md/b2bCustomerMapForm";
//    }

//    @RequiresPermissions("md:b2bcustomer:edit")
//    @RequestMapping(value = "save")
//    public String save(B2bCustomerMap b2bCustomerMap, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
//    {
//        if (!beanValidator(model, b2bCustomerMap))
//        {
//            return form(b2bCustomerMap, model);
//        }
//        b2bCustomerMap.setCustomerName(customerService.getFromCache(b2bCustomerMap.getCustomerId()).getName());
//        customerMapService.save(b2bCustomerMap);
//        addMessage(redirectAttributes, "保存成功");
//        return "redirect:" + adminPath + "/tmall/md/b2bcustomer/list?repage";
//    }

//    @RequiresPermissions("md:b2bcustomer:edit")
//    @RequestMapping(value = "delete")
//    public String delete(B2bCustomerMap b2bCustomerMap, RedirectAttributes redirectAttributes)
//    {
//        customerMapService.delete(b2bCustomerMap);
//        addMessage(redirectAttributes, "删除成功");
//        return "redirect:" + adminPath + "/tmall/md/b2bcustomer/list?repage";
//    }
//    /**
//     * 检查数据源下 数据源ID是否重复
//     * @param dataSoruce
//     * @param shopId
//     * @param id
//     * @param response
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "checkShopId")
//    public String checkShopId(Integer dataSoruce, String shopId,Long id, HttpServletResponse response)
//    {
//        response.setContentType("application/json; charset=UTF-8");
//        //false ：配件名字已经存在,true:不存在
//        return customerMapService.checkShopId(id,dataSoruce,shopId) ? "店铺ID已经存在" : "true";
//    }
}
