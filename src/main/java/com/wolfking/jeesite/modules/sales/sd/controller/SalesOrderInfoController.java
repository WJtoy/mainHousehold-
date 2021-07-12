package com.wolfking.jeesite.modules.sales.sd.controller;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.utils.NumberUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.entity.viewModel.CustomerCurrencyModel;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpServicePointService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderAuxiliaryMaterialService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderCacheReadService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.praise.entity.ViewPraiseModel;
import com.wolfking.jeesite.ms.praise.service.CustomerPraiseService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 网点工单信息
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/sales/sd/orderInfo/")
public class SalesOrderInfoController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderAuxiliaryMaterialService orderAuxiliaryMaterialService;

    @Autowired
    private OrderStatusFlagService orderStatusFlagService;

    @Autowired
    private OrderItemCompleteService orderItemCompleteService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;

    @Autowired
    private CustomerPraiseService customerPraiseService;

    @Autowired
    private MapperFacade mapper;

    public static String CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset=UTF-8";

    /**
     * 查看订单明细 for 业务
     * @param id	订单id
     * @return
     */
    @RequestMapping(value = { "showOrderDetailInfo" })
    public String orderDetailInfo(String id,String quarter, HttpServletRequest request, Model model)
    {
        Boolean errorFlag = false;
        Order order = new Order();
        Long lid = Long.valueOf(id);
        boolean hasAuxiliaryMaterils = false;
        if (lid == null || lid <= 0)
        {
            errorFlag = true;
            addMessage(model, "订单参数错误");
        } else
        {
            order = orderService.getOrderById(lid, quarter,OrderUtils.OrderDataLevel.DETAIL,true);
            if(order == null || order.getOrderCondition() == null){
                errorFlag = true;
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
            }else {
                ServicePoint servicePoint = order.getOrderCondition().getServicePoint();
                if (servicePoint != null && servicePoint.getId() != null & servicePoint.getId() > 0) {
                    Engineer engineer = servicePointService.getEngineerFromCache(servicePoint.getId(), order.getOrderCondition().getEngineer().getId());
                    if (engineer != null) {
                        User engineerUser = new User(engineer.getId());
                        engineerUser.setName(engineer.getName());
                        engineerUser.setMobile(engineer.getContactInfo());
                        engineerUser.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
                        order.getOrderCondition().setEngineer(engineerUser);
                    }
                }
                hasAuxiliaryMaterils = orderAuxiliaryMaterialService.hasAuxiliaryMaterials(order.getId(), order.getQuarter());
            }
        }
        model.addAttribute("order", order);
        model.addAttribute("hasAuxiliaryMaterils", hasAuxiliaryMaterils ? 1 : 0);
        model.addAttribute("errorFlag",errorFlag);
        if(!errorFlag) {
            model.addAttribute("fourServicePhone", MSDictUtils.getDictSingleValue("400ServicePhone", "400-666-3653"));
        }else{
            model.addAttribute("fourServicePhone", "400-666-3653");
        }
        //好评单是否可见
        int praiseFlag = 0;
        OrderStatusFlag orderStatusFlag = orderStatusFlagService.getByOrderId(lid,quarter);
        if(orderStatusFlag !=null && orderStatusFlag.getPraiseStatus()== PraiseStatusEnum.APPROVE.code){
            praiseFlag = 1;
        }
        model.addAttribute("praiseFlag",praiseFlag);
        return "modules/sales/sd/orderInfo/orderDefailInfoForm";
    }

    /**
     * 查看完成照片
     * 只有浏览权限
     */
    @RequestMapping("browseOrderAttachment")
    public String browseOrderAttachment(@RequestParam Long orderId, @RequestParam String quarter, Model model) {
        String viewForm = "modules/sales/sd/orderItemComplete/browseOrderAttachmentForm";
        Order order = new Order();
        Long lorderId = Long.valueOf(orderId);
        if (lorderId == null || lorderId <= 0) {
            model.addAttribute("list", Lists.newArrayList());
            return viewForm;
        }

        //获取已经上传照片的数据
        List<OrderItemComplete> itemCompleteList = null;
        itemCompleteList = orderItemCompleteService.getByOrderId(orderId, quarter);
        if (itemCompleteList != null && itemCompleteList.size() > 0) {
            for (OrderItemComplete entity : itemCompleteList) {
                entity.setProduct(productService.getProductByIdFromCache(entity.getProduct().getId()));
                List<ProductCompletePicItem> picItemList = null;
                picItemList = OrderUtils.fromProductCompletePicItemsJson(entity.getPicJson());
                entity.setItemList(picItemList);
            }
        }
        if (itemCompleteList == null) {
            itemCompleteList = Lists.newArrayList();
        }
        model.addAttribute("list", itemCompleteList);
        return viewForm;
    }


    /**
     * [Ajax]订单日志-跟踪进度
     * 按日期顺序
     * @param orderId	订单id
     */
    @ResponseBody
    @RequestMapping(value = "trackingLog")
    public AjaxJsonEntity trackingLog(@RequestParam String orderId,@RequestParam String quarter ,String isCustomer,HttpServletResponse response)
    {
        User user = UserUtils.getUser();
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try
        {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId == null || lorderId<=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
                return jsonEntity;
            }
            Boolean bIsCustomer = false;
            //读取跟踪进度,statusFlag 0:未更改订单状态 1:订单状态变更 4：进度跟踪
            List<Integer> statusFlags = Lists.newArrayList();
            statusFlags.add(4);
            statusFlags.add(1);
            if(StringUtils.isNoneBlank(isCustomer) && isCustomer.equalsIgnoreCase("true")){
                bIsCustomer = true;
            }else{
                statusFlags.add(0);
            }
            List<OrderProcessLog> trackings = orderService.getOrderLogsByFlags(lorderId,quarter,statusFlags,null);
            if(CollectionUtils.isEmpty(trackings)){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                if(bIsCustomer){
                    List<OrderProcessLog> news = Lists.newArrayList();
                    int customer_visable_flag = 2;
                    trackings.stream().filter(t-> (t.getVisibilityFlag().intValue()&customer_visable_flag) == customer_visable_flag)
                            .forEach(t->{
                                t.setRemarks(t.getActionComment());
                                news.add(t);
                            });
                    List<OrderProcessLog> logs = news.stream()
                            .sorted(Comparator.comparing(OrderProcessLog::getId).reversed()).collect(Collectors.toList());
                    jsonEntity.setData(logs);
                    /*正常日志（订单状态变更的）,将actionComment -> remarks
                    List<OrderProcessLog> news = Lists.newArrayList();
                    trackings.stream().filter(t->t.getStatusFlag() == 1)
                            .forEach(t->{
                                t.setRemarks(t.getActionComment());
                                news.add(t);
                            });
                    //跟踪进度只显示remarks不为空的记录
                    trackings.stream().filter(t-> t.getStatusFlag()==4 && StringUtils.isNoneBlank(t.getRemarks()))
                            .forEach(t->news.add(t));
                    trackings = news.stream().sorted(Comparator.comparing(OrderProcessLog::getId)).collect(Collectors.toList());
                    */
                }else {
                    int customer_visable_flag = 2;
                    //trackings = trackings.stream().filter(t->t.getVisibilityFlag().intValue()!=5 && t.getStatusValue().intValue()!=80).collect(Collectors.toList());
                    trackings.stream().filter(t-> (t.getVisibilityFlag().intValue()&customer_visable_flag) == customer_visable_flag)
                            .forEach(t->{
                                t.setRemarks(t.getActionComment());
                            });
                    List<OrderProcessLog> logs = trackings.stream().sorted(Comparator.comparing(OrderProcessLog::getId))
                            .collect(Collectors.toList());
                    jsonEntity.setData(logs);
                }
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }


    /**
     * [Ajax]订单日志-异常处理
     * 对账时，财务标记异常
     * @param orderId	订单id
     */
    @ResponseBody
    @RequestMapping(value = "exceptLog")
    public AjaxJsonEntity exceptLog(@RequestParam String orderId, @RequestParam String quarter, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if(user == null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try
        {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId == null || lorderId <=0){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单参数错误");
                return jsonEntity;
            }
            //读取异常处理,statusFlag 2:生成对账单时标记异常 6：订单异常处理
            List<OrderProcessLog> excepts = orderService.getOrderLogsByFlags(lorderId,quarter, Arrays.asList(new Integer[] {2,6}),null);
            if(excepts ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                jsonEntity.setData(excepts);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * [Ajax]厂商退补单列表
     */
    @ResponseBody
    @RequestMapping(value = "customerReturnAndAdditionalList")
    public AjaxJsonEntity customerReturnAndAdditionalList(@RequestParam String orderNo,@RequestParam String quarter,HttpServletResponse response)
    {
        User user = UserUtils.getUser();
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try
        {

            if(StringUtils.isBlank(orderNo)){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单号为空");
                return jsonEntity;
            }
            //if (!user.isCustomer()) {
            //	jsonEntity.setSuccess(false);
            //	jsonEntity.setMessage("您没有权限查看厂商退补");
            //	return jsonEntity;
            //}
            List<CustomerCurrency> list = customerCurrencyService.getByOrderNoAndActionTypes(orderNo,new Integer[]{30,40});
            if(list ==null){
                jsonEntity.setData(Lists.newArrayList());
            }else {
                final Map<String,Dict> dicts = MSDictUtils.getDictMap("ServicePointActionType");//切换为微服务
                List<CustomerCurrencyModel> rtnList = Lists.newArrayList();
                CustomerCurrencyModel model;
                CustomerCurrency customerCurrency;
                Dict dict;
                Dict actionType;
                for(int i=0,size=list.size();i<size;i++){
                    customerCurrency = list.get(i);
                    model = mapper.map(customerCurrency,CustomerCurrencyModel.class);
                    actionType = new Dict(customerCurrency.getActionType().toString());
                    if(dicts != null){
                        dict = dicts.get(customerCurrency.getActionType().toString());
                        if(dict !=null){
                            actionType.setLabel(dict.getDescription());
                        }else{
                            actionType.setLabel("读取失败，请刷新");
                        }
                    }
                    model.setActionType(actionType);
                    rtnList.add(model);
                }
                jsonEntity.setData(rtnList);
            }
        } catch (Exception e)
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 工单辅材详情
     */
    @ResponseBody
    @RequestMapping(value = "/auxiliaryMaterialDetailInfo")
    public AjaxJsonEntity orderAuxiliaryMaterialInfo(@RequestParam String orderId, @RequestParam String quarter, HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF8);
        User user = UserUtils.getUser();
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if (user == null || user.getId() == null) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录已超时");
            return jsonEntity;
        }
        try {
            long orderIdLong = StringUtils.toLong(orderId);
            if (orderIdLong <= 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单参数错误");
                return jsonEntity;
            }
            AuxiliaryMaterialMaster master = orderAuxiliaryMaterialService.getOrderAuxiliaryMaterialsV2(orderIdLong, quarter);
            if(master!=null){
                if(master.getFormType()==AuxiliaryMaterialMaster.FormTypeEnum.HAS_MATERIAL_ITEM.getValue()){//有辅材项
                    if (master.getItems() != null && !master.getItems().isEmpty()) {
                        Map<String, Object> map = Maps.newHashMap();
                        List<Map<String, String>> itemMapList = Lists.newArrayList();
                        Map<String, String> itemMap;
                        for (AuxiliaryMaterial item : master.getItems()) {
                            itemMap = Maps.newHashMap();
                            itemMap.put("productName", item.getProduct() != null ? StringUtils.toString(item.getProduct().getName()) : "");
                            itemMap.put("categoryName", item.getCategory() != null ? StringUtils.toString(item.getCategory().getName()) : "");
                            itemMap.put("materialName", item.getMaterial() != null ? StringUtils.toString(item.getMaterial().getName()) : "");
                            itemMap.put("materialPrice", item.getMaterial() != null && item.getMaterial().getPrice() != null ? NumberUtils.formatNum(item.getMaterial().getPrice()) : "0.00");
                            itemMap.put("materialUnit", item.getMaterial() != null ? StringUtils.toString(item.getMaterial().getUnit()) : "");
                            itemMap.put("type", item.getMaterial().getType().toString());
                            itemMap.put("qty", item.getQty() != null ? item.getQty().toString() : "0");
                            itemMap.put("subtotal", item.getSubtotal() != null ? NumberUtils.formatNum(item.getSubtotal()) : "0.00");
                            itemMapList.add(itemMap);
                        }
                        map.put("items", itemMapList);
                        map.put("totalCharge", master.getTotal() != null ? NumberUtils.formatNum(master.getTotal()) : "0.00");
                        map.put("actualTotalCharge", master.getActualTotalCharge() != null ? NumberUtils.formatNum(master.getActualTotalCharge()) : "0.0");
                        map.put("remarks", StringUtils.toString(master.getRemarks()));
                        map.put("filePath",master.getFilePath());
                        map.put("formType",master.getFormType());
                        jsonEntity.setData(map);
                    }
                }else{
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("actualTotalCharge", master.getActualTotalCharge() != null ? NumberUtils.formatNum(master.getActualTotalCharge()) : "0.0");
                    map.put("filePath",master.getFilePath());
                    map.put("remarks", StringUtils.toString(master.getRemarks()));
                    map.put("formType",master.getFormType());
                    jsonEntity.setData(map);
                }
            }
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 业务订单详情查看查看好评单
     * @param orderId
     * @param quarter
     */
    @RequestMapping(value = "ajax/getPraiseInfo")
    @ResponseBody
    public AjaxJsonEntity getPraiseForCustomer(Long orderId,String quarter,Long servicePointId){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
        try {
            ViewPraiseModel praiseModel = customerPraiseService.getPraiseForCustomer(orderId,quarter,servicePointId);
            ajaxJsonEntity.setData(praiseModel);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage(e.getMessage());
        }
        return ajaxJsonEntity;
    }

}

