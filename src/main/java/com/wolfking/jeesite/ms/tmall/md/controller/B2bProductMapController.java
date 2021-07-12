package com.wolfking.jeesite.ms.tmall.md.controller;

import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.tmall.md.service.B2bProductMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "${adminPath}/tmall/md/b2bproduct")
public class B2bProductMapController extends BaseController {

    @Autowired
    private B2bProductMapService b2bProductMapService;

    @Autowired
    private ProductService productService;

    @Autowired
    private B2bCustomerMapService b2bCustomerMapService;

    @Autowired
    private CustomerService customerService;

//    @ModelAttribute
//    public B2bProductMap get(@RequestParam(required = false) Long id) {
//        if (id != null) {
//            B2bProductMap b2bProductMap = b2bProductMapService.get(id);
//            return b2bProductMap;
//        } else {
//            return new B2bProductMap();
//        }
//    }

//    @RequiresPermissions("md:b2bproduct:view")
//    @RequestMapping(value = { "list", "" })
//    public String list(B2bProductMap b2bProductMap, HttpServletRequest request, HttpServletResponse response, Model model)
//    {
//        Page<B2bProductMap> page = b2bProductMapService.findPage(new Page<>(request, response), b2bProductMap);
//
//
//        List<B2bProductMap> list=page.getList();
//        for (B2bProductMap entity: list) {
//            entity.setCustomerName(customerService.getFromCache(entity.getCustomerId()).getName());
//            entity.setProduct(productService.getProductByIdFromCache(entity.getProduct().getId()));
//            entity.setShopName(b2bCustomerMapService.getShopName(entity.getDataSource(), entity.getCustomerId(),entity.getShopId()));
//        }
//
//
//        model.addAttribute("page", page);
//        return "modules/tmall/md/b2bProductMapList";
//    }

//    @RequiresPermissions("md:b2bproduct:view")
//    @RequestMapping(value = "form")
//    public String form(B2bProductMap b2bProductMap, Model model)
//    {
//
//        if(b2bProductMap.getDataSource() > 0 && b2bProductMap.getCustomerId()> 0 && StringUtils.isNotBlank(b2bProductMap.getShopId()))
//        {
//            //编辑
//            List<B2BProductModel> dblist=b2bProductMapService.getProductModelListByShop(b2bProductMap.getDataSource(),b2bProductMap.getShopId());
//
//            List<Product> productList=productService.getCustomerProductList(b2bProductMap.getCustomerId());
//            List<B2BProductModel> b2BProductModelList =new LinkedList<>();
//            for (Product product:productList) {
//                B2BProductModel b2BProductModel =new B2BProductModel();
//
//                //查找已经保存的产品关联信息 加载已经设置好的数据源产品ID
//                for (B2BProductModel p: dblist) {
//                    if (p.getProductId()==product.getId()){
//                        b2BProductModel.setCustomerCategoryId(p.getCustomerCategoryId());
//                    }
//                }
//
//                b2BProductModel.setProductId(product.getId());
//                b2BProductModel.setProductName(product.getName());
//                b2BProductModelList.add(b2BProductModel);
//            }
//
//            b2bProductMap.setList(b2BProductModelList);
//
//        }
//
//        model.addAttribute("shopList", b2bCustomerMapService.getAllShopList(2));
//        return "modules/tmall/md/b2bProductMapForm";
//    }

//    @ResponseBody
//    @RequestMapping(value = "ajax/shopList")
//    public AjaxJsonEntity shopList(Integer dataSource,Long customerId)
//    {
//        AjaxJsonEntity jsonEntity=new AjaxJsonEntity();
//        jsonEntity.setSuccess(false);
//        if (dataSource!=null && dataSource > 1){
//            List<B2bCustomerMap> list=b2bCustomerMapService.getShopListByCustomer(dataSource,customerId);
//            jsonEntity.setSuccess(true);
//            jsonEntity.setData(list);
//            return jsonEntity;
//        }else
//        {
//            return jsonEntity;
//        }
//
//    }

//    @RequiresPermissions("md:b2bproduct:edit")
//    @RequestMapping(value = "save")
//    public String save(B2bProductMap b2bProductMap, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes)
//    {
//        if (!beanValidator(model, b2bProductMap))
//        {
//            return form(b2bProductMap, model);
//        }
//        b2bProductMapService.save(b2bProductMap);
//        addMessage(redirectAttributes, "保存成功");
//        return "redirect:" + adminPath + "/tmall/md/b2bproduct/list?repage";
//    }

//    @RequiresPermissions("md:b2bproduct:edit")
//    @RequestMapping(value = "delete")
//    public String delete(B2bProductMap entity, RedirectAttributes redirectAttributes)
//    {
//        b2bProductMapService.delete(entity);
//        addMessage(redirectAttributes, "删除成功");
//        return "redirect:" + adminPath + "/tmall/md/b2bproduct/list?repage";
//    }
}
