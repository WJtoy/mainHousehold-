package com.wolfking.jeesite.modules.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePointProduct;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointProductService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/md/servicePointProduct")
public class ServicePointProductController extends BaseController {

    @Autowired
    private MSProductCategoryService productCategoryService;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private MSServicePointProductService msServicePointProductService;

    @Autowired
    private GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private ServicePointProductService servicePointProductService;


    /**
     * 返回网点产品页面
     * @param servicePointId
     * @return
     */
    @RequestMapping("showServicePointProductByPointId")
    public String showServicePointProductByPointId(Long servicePointId, Model model) {
        System.out.println(servicePointId+"!");
        String view = "modules/md/servicePointProductForm";
        if (servicePointId == null || servicePointId <= 0) {
            addMessage(model, "错误：网点id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }
        ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
        List<ProductCategory> productCategories = productCategoryService.findAllList();

        Map<Long, List<Product>> map = null;
        if (!CollectionUtils.isEmpty(productCategories)) {
            map = Maps.newHashMapWithExpectedSize(productCategories.size());
            for (ProductCategory productCategory : productCategories) {
                if (!map.containsKey(productCategory.getId())) {
                    List<Product> products = msProductService.findListByProductCategoryId(productCategory.getId());
                    map.put(productCategory.getId(), products);
                }
            }
        }
        MDServicePointProduct servicePointProduct = new MDServicePointProduct();
        servicePointProduct.setServicePointId(servicePointId);
        List<Long> productIds = msServicePointProductService.findProductIds(servicePointProduct);

        model.addAttribute("servicePoint", servicePoint);
        model.addAttribute("products", map);
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("productIds", gsonRedisSerializer.toJson(productIds));
        return view;
    }

    @RequestMapping("saveServicePointProduct")
    @ResponseBody
    public AjaxJsonEntity saveServicePointProduct(Long servicePointId, String productIds) {
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);

        ServicePoint servicePoint = null;
        if(servicePointId == null || servicePointId <= 0){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("参数错误,网点不存在");
            return ajaxJsonEntity;
        } else {
            servicePoint = new ServicePoint();
            servicePoint.setId(servicePointId);
            servicePoint.setProductIds(productIds);
            try {
                servicePointProductService.save(servicePoint);
            } catch (Exception e) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
                return ajaxJsonEntity;
            }
        }
        return ajaxJsonEntity;
    }


    /**
     * 派单停用网点查看产品
     * @param servicePointId
     * @return
     */
    @RequestMapping("showServicePointProductForRushPlan")
    public String showServicePointProductForRushPlan(Long servicePointId, Long productCategoryId,Model model) {
        String view = "modules/md/showServicePointProductForRushPlan";
        if (servicePointId == null || servicePointId <= 0) {
            addMessage(model, "错误：网点id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }

        if (productCategoryId == null || productCategoryId <= 0) {
            addMessage(model, "错误：品类id参数不存在");
            model.addAttribute("canSave",false);
            return view;
        }

        String categoryName = "";
        MDServicePointProduct servicePointProduct = new MDServicePointProduct();
        servicePointProduct.setServicePointId(servicePointId);
        List<Long> productIds = msServicePointProductService.findProductIds(servicePointProduct);
        ProductCategory productCategory = productCategoryService.getFromCache(productCategoryId);
        List<Product> products = msProductService.findListByProductCategoryId(productCategoryId);
        Map<Long,Product> productMap =products.stream().collect(Collectors.toMap(Product::getId, Function.identity(), (key1, key2) -> key2));
        List<Product> productList = Lists.newArrayList();
        Product product;
        for(Long item:productIds){
            product = productMap.get(item);
            if(product!=null){
                productList.add(product);
            }
        }
        if(productCategory!=null){
            categoryName = productCategory.getName();
        }


        model.addAttribute("productList", productList);
        model.addAttribute("categoryName", categoryName);
        return view;
    }

}
