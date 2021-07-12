package com.wolfking.jeesite.modules.finance.md.web;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.finance.md.service.FiServicePointPriceService;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePrices;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSProductPriceService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointPriceService;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/fi/md/servicePointPrice")
public class FiServicePointPriceController  extends BaseController {

    @Autowired
    private FiServicePointPriceService fiServicePointPriceService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSProductPriceService msProductPriceService;

    @Autowired
    private MSServicePointPriceService msServicePointPriceService;


    @RequiresPermissions("fi:md:servicepointprice:view")
    @RequestMapping(value = { "list" }, method = RequestMethod.GET)
    public String getList(ServicePoint servicePoint, ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        addMessage(model, "注：请选择网点名称，网点编号，网点电话中至少一项进行查询！");
        model.addAttribute("page", null);
        model.addAttribute("getRequest", true);
        return "modules/finance/md/fiServicePointPriceList";
    }

    @RequiresPermissions("fi:md:servicepointprice:view")
    @RequestMapping(value = { "list"}, method = RequestMethod.POST)
    public String postList(ServicePoint servicePoint, ServicePointModel servicePointModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ServicePoint> page = new Page<>(request, response);
        BeanUtils.copyProperties(servicePointModel, servicePoint);
        if (StringUtils.isBlank(servicePoint.getServicePointNo()) && StringUtils.isBlank(servicePoint.getName()) && StringUtils.isBlank(servicePoint.getContactInfo1())) {
            addMessage(model, "注：请选择网点名称，网点编号，网点电话中至少一项进行查询！");
            model.addAttribute("page", page);
            model.addAttribute("servicePoint", servicePointModel);
            return "modules/finance/md/fiServicePointPriceList";
        }
        servicePoint.setInvoiceFlag(-1);
        if(servicePoint.getFinance() != null){
            servicePoint.getFinance().setInvoiceFlag(-1);
            servicePoint.getFinance().setDiscountFlag(-1);
        }
        servicePoint.setOrderBy("s.order_count desc,s.servicepoint_no");//sort
        servicePoint.setAutoPlanFlag(-1);    //自动派单
        servicePoint.setInsuranceFlag(-1);   //购买保险
        servicePoint.setTimeLinessFlag(-1);  //快可立补贴
        servicePoint.setUseDefaultPrice(-1); //结算标准
        page = fiServicePointPriceService.findPricePage(page, servicePoint);
        model.addAttribute("page", page);
        model.addAttribute("servicePoint", servicePointModel);
        return "modules/finance/md/fiServicePointPriceList";
    }

    @RequiresPermissions("fi:md:servicepointprice:view")
    @RequestMapping(value = "selectPrice")
    public String selectPrice(ServicePrice servicePrice, Long id, String servicePointNo, String servicePointName, String primaryName, String contactInfo, String customizePriceFlag, String useDefaultPrice, Integer degree,Integer serviceRemotePriceFlag,String remotePriceFlag,String remotePriceType,
             Long productCategoryId, Long productId,HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("servicePointNo", servicePointNo);
        model.addAttribute("servicePointName", servicePointName);
        model.addAttribute("primaryName", primaryName);
        model.addAttribute("contactInfo", contactInfo);
        model.addAttribute("customizePriceFlag", customizePriceFlag);// 是否标准价 0是 1否
        model.addAttribute("useDefaultPrice", useDefaultPrice);
        model.addAttribute("serviceRemotePriceFlag", serviceRemotePriceFlag);//价格类型  0服务价格 1远程价格
        model.addAttribute("remotePriceFlag", remotePriceFlag);
        model.addAttribute("remotePriceType", remotePriceType);
        model.addAttribute("servicePointId", id);
        model.addAttribute("degree", degree);
        model.addAttribute("productId", productId);

        Page<ServicePrice> page = new Page<>(request, response);

        if (servicePrice.getProduct() == null) {
            if (productId == null || productId <= 0) {
                servicePrice.setProduct(new Product());
            } else {
                servicePrice.setProduct(new Product(productId));
            }
        }
        if (productCategoryId != null && productCategoryId >0) {
            servicePrice.setProductCategory(new ProductCategory(productCategoryId));
            model.addAttribute("productCategoryId", productCategoryId);
        }
        ServicePoint servicePoint = new ServicePoint(id);
        servicePoint.setCustomizePriceFlag(Integer.valueOf(customizePriceFlag));
        servicePoint.setRemotePriceFlag(Integer.parseInt(remotePriceFlag));
        servicePoint.setUseDefaultPrice(Integer.parseInt(useDefaultPrice));
        if(serviceRemotePriceFlag == 1){
            servicePoint.setRemotePriceType(Integer.parseInt(remotePriceType));
        }
        servicePrice.setServicePoint(servicePoint);
        page = fiServicePointPriceService.findPage(page, servicePrice,serviceRemotePriceFlag);
        model.addAttribute("page", page);
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
        //end
        model.addAttribute("serviceTypes", ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes));
        model.addAttribute("servicePrice",servicePrice);
        model.addAttribute("firstSearch","1");

        return  "modules/finance/md/fiSingleServicePointPriceList";
    }

    @RequiresPermissions("fi:md:servicepointprice:edit")
    @RequestMapping(value = "product")
    public String productForm(ServicePrices servicePrices, String qServicePointId, String qServicePointName,
                                 String qProductCategoryId, String qProductCategoryName, String qProductId,
                                 String qProductName,Integer serviceRemotePriceFlag, Model model) {
        Boolean canAction = true;
        model.addAttribute("servicePointId", qServicePointId);
        model.addAttribute("servicePointName", qServicePointName);
        model.addAttribute("productCategoryId", qProductCategoryId);
        model.addAttribute("productCategoryName", qProductCategoryName);
        model.addAttribute("productId", qProductId);
        model.addAttribute("productName", qProductName);
        model.addAttribute("serviceRemotePriceFlag", serviceRemotePriceFlag);//价格类型  0服务价格 1远程价格
        String view = "modules/finance/md/fiServicePointPriceProductForm";

        if(servicePrices.getServicePoint()==null || servicePrices.getServicePoint().getId()==null ||
                servicePrices.getProduct()==null || servicePrices.getProduct().getId() == null){
            addMessage(model,"参数：网点或产品错误.");
            model.addAttribute("canAction",false);
            return view;
        }

        Long sid = servicePrices.getServicePoint().getId();
        ServicePoint servicePoint = msServicePointService.getSimpleById(sid);
        if(servicePoint == null){
            addMessage(model,"读取网点信息失败，请关闭窗口，重新打开。");
            model.addAttribute("canAction",false);
            return view;
        }

        servicePrices.setServicePoint(servicePoint);

        Long pid = servicePrices.getProduct().getId();
        Product product = productService.get(pid);
        if(product == null){
            addMessage(model,"读取产品信息失败，请关闭窗口，重新打开。");
            model.addAttribute("canAction",false);
            return view;
        }
        servicePrices.setProduct(product);
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNames();
        // end
        if(serviceTypes == null || serviceTypes.size()==0){
            addMessage(model,"读取服务类型信息失败，请关闭窗口，重新打开。");
            model.addAttribute("canAction",false);
            return view;
        }
        //默认价格

        List<ProductPrice> allPrices = msProductPriceService.findGroupList(null, Lists.newArrayList(pid),  null, servicePoint.getId(), null);
        List<Long> allServiceTypeIdList = allPrices.stream().map(t->t.getServiceType().getId()).distinct().collect(Collectors.toList());
        if(allPrices == null || allPrices.size()==0){
            addMessage(model,"产品参考价格为空，请先维护产品参考价格。");
            model.addAttribute("canAction",false);
            return view;
        }

        List<ServicePrice> prices = null;
        try{
            if(serviceRemotePriceFlag == 0){
                prices = fiServicePointPriceService.getPrices(sid, product.getId(), null,servicePoint.getUseDefaultPrice());  // add on 2020-3-13
            }else {
                prices = fiServicePointPriceService.getPrices(sid, product.getId(), null,servicePoint.getRemotePriceType());  // add on 2020-3-13
            }
        }catch (Exception e){
            log.error("[getPrices]读取网点价格错误:{}",sid,e);
            addMessage(model,"读取网点已维护价格错误，请重试。");
            model.addAttribute("canAction",false);
            return view;
        }
        if (prices == null){
            prices = Lists.newArrayList();
        }
        final List<ServicePrice> hasprices = prices.stream().filter(t-> Objects.equals(t.getProduct().getId(), pid))
                .collect(Collectors.toList());
        List<ServicePrice> list = Lists.newArrayList();
        int priceType;
        if(serviceRemotePriceFlag == 0){
            priceType = servicePoint.getUseDefaultPrice();
        }else {
            priceType = servicePoint.getRemotePriceType();  // add on 2020-3-5
        }
        allServiceTypeIdList.forEach(t->{
            List<ProductPrice> pp = allPrices.stream().filter(p->Objects.equals(p.getServiceType().getId(),t)).collect(Collectors.toList());
            ServiceType st = serviceTypes.stream().filter(m->Objects.equals(m.getId(),t)).findFirst().orElse(null);
            if(st !=null) {
                ServicePrice price = hasprices.stream().filter(s -> Objects.equals(s.getServiceType().getId(), t)).findFirst().orElse(null);
                if (price == null) {
                    price = new ServicePrice();
                }

                price.setServiceType(st);
                List<HashMap<String, Object>> productPriceList = Lists.newArrayList();
                //切换为微服务
                Map<String, Dict> priceTypeMap = MSDictUtils.getDictMap("PriceType");
                for (ProductPrice productPrice : pp){
                    HashMap<String, Object> productPriceMap = new HashMap<>();
                    productPriceMap.put("priceType", productPrice.getPriceType().getValue());
                    productPriceMap.put("priceTypeName", priceTypeMap.get(productPrice.getPriceType().getValue()).getLabel());
                    productPriceMap.put("standPrice", productPrice.getEngineerStandardPrice());
                    productPriceMap.put("discountPrice", productPrice.getEngineerDiscountPrice());
                    productPriceList.add(productPriceMap);
                    Collections.sort(productPriceList, new Comparator<Map<String, Object>>(){

                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            String name1 =(String)o1.get("priceType");
                            String name2= (String)o2.get("priceType");
                            return name1.compareTo(name2);
                        }

                    });
                    if (priceType == productPrice.getPriceType().getIntValue()) {
                        if (price.getId() == null && price.getServiceType().getId().longValue() == productPrice.getServiceType().getId().longValue()) {
                            price.setPrice(productPrice.getEngineerStandardPrice());
                            price.setDiscountPrice(productPrice.getEngineerDiscountPrice());
                        }
                    }
                }
                price.setProductPriceList(productPriceList);

                list.add(price);
            }
        });

        servicePrices.getPrices().addAll(list);

        model.addAttribute("canAction",true);
        model.addAttribute("servicePrices", servicePrices);
        return view;
    }

    @RequiresPermissions("fi:md:servicepointprice:edit")
    @RequestMapping(value = "saveProductPrices")
    @ResponseBody
    public AjaxJsonEntity saveProductPrices(ServicePrices entity, String qServicePointId, String qServicePointName,
                                            String qProductCategoryId, String qProductCategoryName, String qProductId,
                                            String qProductName,Integer serviceRemotePriceFlag, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        if (!beanValidator(model, entity)) {
            try {
                productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName,serviceRemotePriceFlag, model);
            } catch (Exception e) {
                ajaxJsonEntity.setSuccess(false);
                ajaxJsonEntity.setMessage(e.getMessage());
            }
        }

        List<ServicePrice> prices = entity.getPrices().stream().filter(t->t.getServiceType()!=null).collect(Collectors.toList());
        if(prices==null || prices.size()==0){
            ajaxJsonEntity.setSuccess(false);
            if(serviceRemotePriceFlag == 0){
                ajaxJsonEntity.setMessage("错误：请设定服务价格");
            }else {
                ajaxJsonEntity.setMessage("错误：请设定偏远价格");
            }
            productForm(entity, qServicePointId, qServicePointName, qProductCategoryId, qProductCategoryName, qProductId, qProductName,serviceRemotePriceFlag,model);
        }
        final User user = UserUtils.getUser();
        entity.setCreateBy(user);
        entity.setCreateDate(new Date());
        entity.setPrices(prices);
        try {
            fiServicePointPriceService.saveProductPrices(entity,serviceRemotePriceFlag);
            model.addAttribute("servicePoint", entity.getServicePoint());
            ajaxJsonEntity.setMessage("保存服务网点：" + entity.getServicePoint().getName() + " 价格成功");
        } catch (Exception e) {
            log.error("保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage(), e);
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存服务网点：" + entity.getServicePoint().getName() + " 价格失败:" + e.getMessage());
        }
        return ajaxJsonEntity;
    }

    @RequestMapping(value = "recover")
    public String recover(){
        return "modules/finance/md/fiServicePointPriceRecover";
    }

    /**
     * 修改网点是否使用标准价
     * @param servicePoint
     * @param response
     * @return
     */
    @ResponseBody
    @RequiresPermissions("fi:md:servicepointprice:edit")
    @RequestMapping(value = "updateCustomizePriceFlag",method = RequestMethod.POST)
    public AjaxJsonEntity updateCustomizePriceFlag(ServicePoint servicePoint, Integer serviceRemotePriceFlag ,HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity ajaxResponse = new AjaxJsonEntity(true);
        try {
            if(serviceRemotePriceFlag == 0){
                msServicePointPriceService.updateCustomizePriceFlag(servicePoint);
            }else {
                servicePoint.setRemotePriceFlag(0);
                msServicePointPriceService.updateRemotePriceFlag(servicePoint);
            }
            return ajaxResponse;
        } catch (Exception e){
            ajaxResponse.setSuccess(false);
            ajaxResponse.setMessage("修改网点价格类型失败：" + e.getMessage());
            return ajaxResponse;
        }
    }
}
