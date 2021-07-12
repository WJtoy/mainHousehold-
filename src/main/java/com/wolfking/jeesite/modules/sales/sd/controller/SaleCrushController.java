package com.wolfking.jeesite.modules.sales.sd.controller;


import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
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
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 网点工单信息
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/sales/sd/crush/")
public class SaleCrushController extends BaseController {

    @Autowired
    private OrderCrushService crushService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ServicePointService servicePointService;

    /**
     * [Ajax]客服订单详情-突击单列表
     * @param orderId	订单id
     */
    @ResponseBody
    @RequestMapping(value = "ajax/list")
    public AjaxJsonEntity orderCrushList(@RequestParam String orderId, @RequestParam String quarter, HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        User user = UserUtils.getUser();
        if(user == null || user.getId()==null){
            AjaxJsonEntity jsonEntity =  AjaxJsonEntity.fail("登录超时，请重新登录。",null);
            jsonEntity.setLogin(false);
            return jsonEntity;
        }
        try
        {
            Long lorderId = Long.valueOf(orderId);
            if(lorderId == null || lorderId <=0){
                return AjaxJsonEntity.fail("订单参数错误",null);
            }
            List<OrderCrush> list = crushService.findCrushListOfOrder(lorderId,"",quarter);
            return AjaxJsonEntity.success("",list==null?Lists.newArrayList():list);
        } catch (Exception e)
        {
            return AjaxJsonEntity.fail(ExceptionUtils.getRootCauseMessage(e),null);
        }
    }

    /**
     * 突击单浏览窗口
     * @param id		突击单id
     * @param quarter 	分片
     */
    @RequestMapping(value = "view", method = RequestMethod.GET)
    public String viewForm(@RequestParam String id, @RequestParam(required = false) String quarter, Model model) {
        User user = UserUtils.getUser();
        OrderCrush orderCrush = new OrderCrush();
        String formView = "modules/sales/sd/crush/viewForm";
        if(StringUtils.isBlank(id)){
            return crushResult(orderCrush,model,"参数为空。",formView);
        }
        Long lid = null;
        try {
            lid = Long.valueOf(id);
        }catch (Exception e){
            lid = 0l;
        }
        if (lid == null || lid <= 0){
            return crushResult(orderCrush,model,"突击单参数类型错误。",formView);
        }
        try {
            orderCrush = crushService.getOrderCrush(lid,quarter,false);
        }catch (Exception e){
            return crushResult(orderCrush,model,"突击单读取错误。",formView);
        }
        if(orderCrush ==null){
            return crushResult(orderCrush,model,"读取突击单失败或不存在，请重试。",formView);
        }
        if (orderCrush.getOrderId()>0) {
            Order order = orderService.getOrderById(orderCrush.getOrderId(), quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                return crushResult(orderCrush,model,"系统繁忙：读取订单读取失败，稍后请重试。",formView);
            } else {
                orderCrush.setUserName(order.getOrderCondition().getUserName());
                orderCrush.setUserPhone(order.getOrderCondition().getServicePhone());
                orderCrush.setUserAddress(order.getOrderCondition().getArea().getName() + order.getOrderCondition().getServiceAddress());
                orderCrush.setCustomer(order.getOrderCondition().getCustomer());
            }
        }
        Map<Integer, Area> areaMap = areaService.getAllParentsWithDistrict(orderCrush.getArea().getId());
        if(areaMap.containsKey(Area.TYPE_VALUE_PROVINCE)){
            orderCrush.setProvince(areaMap.get(Area.TYPE_VALUE_PROVINCE));
        }
        if(areaMap.containsKey(Area.TYPE_VALUE_CITY)){
            orderCrush.setCity(areaMap.get(Area.TYPE_VALUE_CITY));
        }
        if(areaMap.containsKey(Area.TYPE_VALUE_COUNTY)){
            orderCrush.setArea(areaMap.get(Area.TYPE_VALUE_COUNTY));
        }
        //servicepoint list
        int degree = 0;
        List<ServicePoint> servicePoints;
        try {
            Long productCategoryId = orderService.getOrderProductCategoryId(orderCrush.getQuarter(), orderCrush.getOrderId());
            //servicePoints = getCrushServicePointList(orderCrush.getArea(), null, productCategoryId);
            List<Dict> dictList = MSDictUtils.getDictList("degreeType");
            if(dictList!=null && dictList.size()>0){
                degree = dictList.get(0).getIntValue();
            }
            //servicePoints = getCrushServicePointList(orderCrush.getArea(), null, productCategoryId);
            servicePoints = findCrushServicePointByDegree(orderCrush.getArea(),productCategoryId,degree);
        }catch (Exception e){
            return crushResult(orderCrush, model, "系统繁忙，读取网点信息失败，请稍后重试。", formView);
        }
        orderCrush.setServicePoints(servicePoints);
        model.addAttribute("degreeType",degree);
        return crushResult(orderCrush,model,"",formView);
    }


    /**
     * 突击错误
     * @param orderCrush
     * @param model
     * @param errorMsg
     * @param formView
     * @return
     */
    private String crushResult(OrderCrush orderCrush, Model model, String errorMsg, String formView) {
        if(StringUtils.isNotBlank(errorMsg)) {
            addMessage(model, errorMsg);
            model.addAttribute("canAction", false);
        }else{
            model.addAttribute("canAction", true);
        }
        model.addAttribute("orderCrush", orderCrush==null?new OrderCrush():orderCrush);
        return formView;
    }


    /**
     * 根据区id，品类,网点分类获取网点
     * @param area
     * @param productCategoryId
     * @param degree
     * @return
     */
    private List<ServicePoint> findCrushServicePointByDegree(Area area,long productCategoryId,int degree){
        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setArea(area);
        servicePoint.setProductCategoryId(productCategoryId);
        servicePoint.setDegree(degree);
        Page<ServicePoint> page = new Page<ServicePoint>(1,500);
        page = servicePointService.findServicePointListForPlanNew(page, servicePoint);
        List<ServicePoint> servicePoints = Lists.newArrayList();
        if (!org.springframework.util.ObjectUtils.isEmpty(page.getList())){
            servicePoints = page.getList();
        }
        return servicePoints;
    }

}

