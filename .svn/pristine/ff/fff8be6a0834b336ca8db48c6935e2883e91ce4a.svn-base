package com.wolfking.jeesite.modules.sd.web;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.OrderReturnComplete;
import com.wolfking.jeesite.modules.sd.entity.viewModel.B2BSNValidModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.KefuCompleteModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ReturnCompleteModel;
import com.wolfking.jeesite.modules.sd.service.OrderReturnCompleteService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Ryan
 * @date: 2020/10/16 10:38
 * @Description: 退换货订单处理
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/return/")
@Slf4j
public class OrderReturnController extends BaseController {

    @Autowired
    private OrderService orderService;
    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;
    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    @Autowired
    private OrderReturnCompleteService returnCompleteService;

    //region 退换货

    /**
     * 换货-确认收货
     */
    @ResponseBody
    @PostMapping(value = "confirmReceived")
    public AjaxJsonEntity confirmReceived(KefuCompleteModel requestModel, HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        if (requestModel == null || requestModel.getOrderId() == null || requestModel.getOrderId() <=0
                || StringUtils.isBlank(requestModel.getQuarter()))
        {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("错误：传入参数内容错误。");
            return jsonEntity;
        }
        try {
            User user = UserUtils.getUser();
            Order order = orderService.getOrderById(requestModel.getOrderId(), requestModel.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true, true);
            if(order == null || order.getOrderCondition() == null){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单不存在或读取错误。");
                return jsonEntity;
            }
            int status = order.getOrderCondition().getStatusValue();
            if (status == Order.ORDER_STATUS_COMPLETED.intValue() || status == Order.ORDER_STATUS_CHARGED.intValue()) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单已客评。");
                return jsonEntity;
            } else if (status > Order.ORDER_STATUS_CHARGED.intValue()) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单已取消或已退单。");
                return jsonEntity;
            }
            //TODO: APP完工[55]
//            else if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType())){
            else if(StringUtils.isNotBlank(order.getOrderCondition().getAppCompleteType()) || status == Order.ORDER_STATUS_APP_COMPLETED){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("订单已完工操作。");
                return jsonEntity;
            }
            if(order.getOrderCondition().getOrderServiceType() != OrderUtils.OrderTypeEnum.EXCHANGE.getId()){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("该订单不是[换货服务]，不能[确认收货]操作。");
                return jsonEntity;
            }
            int dataSource = order.getDataSourceId();
            if(dataSource != B2BDataSourceEnum.VIOMI.getId()){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("该订单不需要[确认收货]操作。");
                return jsonEntity;
            }
            if(order.getOrderCondition().getArrivalDate() != null){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("该订单货品已收货。");
                return jsonEntity;
            }
            requestModel.setUser(user);
            requestModel.setOrder(order);
            requestModel.setDataSourceId(order.getDataSourceId());
            requestModel.setOperateDate(new Date());
            StringBuilder logContent = new StringBuilder(64);
            orderService.confirmReceived(requestModel,logContent);
            Map<String,Object> rtnMap = Maps.newHashMapWithExpectedSize(3);
            rtnMap.put("date",DateUtils.formatDate(requestModel.getOperateDate(),"yyyy-MM-dd HH:mm"));
            rtnMap.put("user",user.getName());
            if(logContent.length() > 0) {
                rtnMap.put("log", logContent.toString());
            }
            jsonEntity.setData(rtnMap);
        } catch (OrderException oe){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(ExceptionUtils.getRootCauseMessage(oe));
            log.error("[OrderController.receivingGoods] {}",requestModel,oe);
        } catch (Exception e){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(ExceptionUtils.getRootCauseMessage(e));
            log.error("[OrderController.receivingGoods] {}",requestModel,e);
        }
        return jsonEntity;
    }

    /**
     * 退货-完工(窗口)
     */
    @GetMapping(value="completeFrom")
    public String completeFromForReturn(@RequestParam Long orderId, @RequestParam String quarter, Model model, HttpServletRequest request, HttpServletResponse respons) {
        String viewForm = "modules/sd/returnProcess/returnCompleteForm";
        ReturnCompleteModel completeModel = new ReturnCompleteModel();
        Integer dataSource = 0;
        int errorFlag = 0;
        if (orderId == null || orderId <= 0) {
            return formFail(model, viewForm, "订单编号错误.", null, null);
        }
        Order order;
        try {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                return formFail(model, viewForm, "错误：系统繁忙，读取订单失败，请重试。", null, null);
            }
            completeModel.setCompleteType(order.getOrderCondition().getAppCompleteType());
            //读取已提交的数据
            List<OrderReturnComplete> items = returnCompleteService.getByOrderId(orderId,order.getQuarter());
            if(CollectionUtils.isEmpty(items)) {
                //未保存过
                loadEmptyDismountCompleteItem(model,OrderUtils.OrderTypeEnum.BACK);
                loadEmptyLogisticsCompleteItem(model);
                List<OrderItem> orderItems = order.getItems();
                if(CollectionUtils.isEmpty(orderItems)){
                    return formFail(model, viewForm, "错误：读取订单服务产品错误，请重试。", null, null);
                }
                Product product = orderItems.get(0).getProduct();
                completeModel.setProductId(product.getId());
                completeModel.setProductName(product.getName());
            }else{
                String msg = assemblyCompleteItems(model,items);
                if(!msg.equals("OK")){
                    return formFail(model, viewForm, msg, null, null);
                }
                OrderReturnComplete item = items.get(0);
                completeModel.setProductId(item.getProductId());
                completeModel.setProductName(item.getProductName());
            }
            completeModel.setOrderId(orderId);
            completeModel.setQuarter(order.getQuarter());
            completeModel.setDataSource(order.getDataSourceId());
            completeModel.setB2bOrderId(order.getB2bOrderId());
            completeModel.setB2bOrderNo(order.getWorkCardId());
            completeModel.setOrderServiceType(order.getOrderCondition().getOrderServiceType());
        } catch (Exception e) {
            return formFail(model, viewForm, "", null, e);
        }
        model.addAttribute("dataSource", dataSource);
        model.addAttribute("errorFlag", errorFlag);
        model.addAttribute("completeModel", completeModel);

        return viewForm;
    }


    /**
     * 换货-完工(窗口)
     */
    @GetMapping(value="exchange/completeFrom")
    public String completeFromForExchange(@RequestParam Long orderId, @RequestParam String quarter, Model model,HttpServletRequest request, HttpServletResponse respons) {
        String viewForm = "modules/sd/returnProcess/exchangeCompleteForm";
        ReturnCompleteModel completeModel = new ReturnCompleteModel();
        Integer dataSource = 0;
        int errorFlag = 0;
        if (orderId == null || orderId <= 0) {
            return formFail(model, viewForm, "订单编号错误.", null, null);
        }
        Order order;
        try {
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                return formFail(model, viewForm, "错误：系统繁忙，读取订单失败，请重试。", null, null);
            }
            completeModel.setCompleteType(order.getOrderCondition().getAppCompleteType());
            //读取已提交的数据
            List<OrderReturnComplete> items = returnCompleteService.getByOrderId(orderId,order.getQuarter());
            if(CollectionUtils.isEmpty(items)) {
                //未保存过
                loadEmptyDismountCompleteItem(model,OrderUtils.OrderTypeEnum.EXCHANGE);
                loadEmptyLogisticsCompleteItem(model);
                List<OrderItem> orderItems = order.getItems();
                if(CollectionUtils.isEmpty(orderItems)){
                    return formFail(model, viewForm, "错误：读取订单服务产品错误，请重试。", null, null);
                }
                Product product = orderItems.get(0).getProduct();
                completeModel.setProductId(product.getId());
                completeModel.setProductName(product.getName());
            }else{
                String msg = assemblyCompleteItems(model,items);
                if(!msg.equals("OK")){
                    return formFail(model, viewForm, msg, null, null);
                }
                OrderReturnComplete item = items.get(0);
                completeModel.setProductId(item.getProductId());
                completeModel.setProductName(item.getProductName());
            }
            completeModel.setOrderId(orderId);
            completeModel.setQuarter(order.getQuarter());
            completeModel.setDataSource(order.getDataSourceId());
            completeModel.setB2bOrderId(order.getB2bOrderId());
            completeModel.setB2bOrderNo(order.getWorkCardId());
            completeModel.setOrderServiceType(order.getOrderCondition().getOrderServiceType());

        } catch (Exception e) {
            return formFail(model, viewForm, "", null, e);
        }
        model.addAttribute("dataSource", dataSource);
        model.addAttribute("errorFlag", errorFlag);
        model.addAttribute("completeModel", completeModel);

        return viewForm;
    }

    //region 私有方法

    private String formFail(Model model,String viewForm,String msg,Object data,Exception ex){
        model.addAttribute("errorFlag",1);
        if(StringUtils.isNotBlank(msg)){
            addMessage(model, msg);
        }
        if(data != null) {
            model.addAttribute("order", data);
        }
        if(ex != null){
            log.error("装载退单完工窗口错误,",ex);
            if(StringUtils.isBlank(msg)){
                addMessage(model, ExceptionUtil.getRootCauseMessage(ex));
            }
        }
        return viewForm;
    }

    /**
     * 装载空白退货寄回物流项目
     */
    private void loadEmptyLogisticsCompleteItem(Model model){
        List<OrderReturnComplete.PicSubItem> items = Lists.newArrayListWithCapacity(5);
        items.add(
                OrderReturnComplete.PicSubItem.builder()
                        .code("backPhone")
                        .title("寄回产品照片")
                        .required(1)
                        .sort(1)
                        .url("")
                        .updateDate("")
                        .build()
        );
        OrderReturnComplete.JsonItem logisticItem = new OrderReturnComplete.JsonItem()
                .setCompany("")
                .setCompanyCode("")
                .setNumber("")
                .setPhotos(items);

        OrderReturnComplete item = new OrderReturnComplete()
                .setItemType(OrderReturnComplete.ItemTypeEnum.LOGISTICS.getId())
                .setId(0L)
                .setJsonItem(logisticItem);
        model.addAttribute("logisticsItem", item);
    }

    /**
     * 装载空白换货项目
     * @param model
     */
    private void loadEmptyDismountCompleteItem(Model model,OrderUtils.OrderTypeEnum orderType) {
        List<OrderReturnComplete.PicSubItem> items = Lists.newArrayListWithCapacity(10);
        if (orderType == OrderUtils.OrderTypeEnum.EXCHANGE) {
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("package")
                            .title("产品打包照片")
                            .required(1)
                            .sort(1)
                            .url("")
                            .createDate("")
                            .build()
            );
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("oldSn")
                            .title("旧产品SN码照片")
                            .required(1)
                            .sort(2)
                            .url("")
                            .updateDate("")
                            .build()
            );
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("install")
                            .title("新产品安装照片")
                            .required(1)
                            .sort(3)
                            .url("")
                            .updateDate("")
                            .build()
            );
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("newSn")
                            .title("新产品SN码照片")
                            .required(1)
                            .sort(4)
                            .url("")
                            .build()
            );
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("signForm")
                            .title("服务单签字照片")
                            .required(1)
                            .sort(5)
                            .url("")
                            .build()
            );
        } else {
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("package")
                            .title("产品打包照片")
                            .required(1)
                            .sort(1)
                            .url("")
                            .build()
            );
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("oldSn")
                            .title("产品SN码照片")
                            .required(1)
                            .sort(3)
                            .url("")
                            .build()
            );
            items.add(
                    OrderReturnComplete.PicSubItem.builder()
                            .code("signForm")
                            .title("服务单签字照片")
                            .required(1)
                            .sort(5)
                            .url("")
                            .build()
            );
        }
        OrderReturnComplete.JsonItem dismountItem = new OrderReturnComplete.JsonItem().setPhotos(items);
        OrderReturnComplete item = new OrderReturnComplete()
                .setItemType(OrderReturnComplete.ItemTypeEnum.DISMOUNT.getId())
                .setId(0L)
                .setJsonItem(dismountItem);
        model.addAttribute("dismountItem", item);
    }

    /**
     * 装配完工项目
     * @param model
     * @param items
     * @return
     */
    private String assemblyCompleteItems(Model model,List<OrderReturnComplete> items){
        try {
            for (OrderReturnComplete item : items) {
                if (item.getItemType() == OrderReturnComplete.ItemTypeEnum.DISMOUNT.getId()) {
                    OrderReturnComplete.JsonItem dismountItem = (OrderReturnComplete.JsonItem) gsonRedisSerializer.fromJson(item.getJson(), OrderReturnComplete.JsonItem.class);
                    //List<OrderReturnComplete.PicSubItem> photos = Arrays.asList((OrderReturnComplete.PicSubItem[]) gsonRedisSerializer.fromJson(item.getJson(), OrderReturnComplete.PicSubItem[].class));
                    item.setJsonItem(dismountItem);
                    model.addAttribute("dismountItem", item);
                } else {
                    OrderReturnComplete.JsonItem logisticItem = (OrderReturnComplete.JsonItem) gsonRedisSerializer.fromJson(item.getJson(), OrderReturnComplete.JsonItem.class);
                    item.setJsonItem(logisticItem);
                    model.addAttribute("logisticsItem", item);
                }
            }
            return "OK";
        }catch (Exception e){
            log.error("装配完工项目错误,items:{}",items,e);
            return "装配完工项目错误";
        }
    }

    //endregion 私有方法

    /**
     * 退货-完工(提交)
     */
    @ResponseBody
    @RequestMapping(value="saveCompleteFrom",method = RequestMethod.POST)
    public AjaxJsonEntity saveCompleteFrom(@RequestBody ReturnCompleteModel completeModel, HttpServletRequest request, HttpServletResponse respons){
        if(completeModel == null || com.wolfking.jeesite.common.utils.StringUtils.longIsNullOrLessSpecialValue(completeModel.getOrderId(),0)
                || com.wolfking.jeesite.common.utils.StringUtils.longIsNullOrLessSpecialValue(completeModel.getProductId(),0)
                || StringUtils.isBlank(completeModel.getProductName())
                || CollectionUtils.isEmpty(completeModel.getItems())
                || completeModel.getItems().size() <2){
            return AjaxJsonEntity.fail("提交内容错误",null);
        }
        Map<String,Object> responseMap = Maps.newHashMapWithExpectedSize(10);
        //step1:save to db
        try {
            User user = UserUtils.getUser();
            completeModel.setUser(user);
            completeModel.setOperateDate(new Date());
            orderService.saveCompleteItemsForReturn(completeModel);
        }catch (OrderException e){
            return AjaxJsonEntity.fail(e.getMessage(),null);
        }
        catch (Exception e){
            return AjaxJsonEntity.fail(ExceptionUtil.getMessage(e),null);
        }

        //step2:send to b2b
        try{
            MSResponse response = orderService.sendB2BCompleteReturnMessage(completeModel);
            if(MSResponse.isSuccessCode(response)){
                completeModel.getItems().forEach(t->t.setUploadFlag(1));
            }else{
                completeModelToMap(completeModel,responseMap);
                return AjaxJsonEntity.fail(response.getMsg(),responseMap);
            }
        }catch (Exception e){
            completeModelToMap(completeModel,responseMap);
            return AjaxJsonEntity.fail(ExceptionUtil.getMessage(e),responseMap);
        }
        //step3:kefu complete
        completeModelToMap(completeModel,responseMap);
        try{
            KefuCompleteModel kefuCompleteModel = KefuCompleteModel.builder()
                    .orderId(completeModel.getOrderId())
                    .quarter(completeModel.getQuarter())
                    .dataSourceId(completeModel.getDataSource())
                    .completeType(new Dict("compeled_kefu","客服完工"))
                    .user(completeModel.getUser())
                    .build();
            orderService.kefuCompleteReturn(kefuCompleteModel);
        }catch (Exception e){
            return AjaxJsonEntity.fail(ExceptionUtil.getMessage(e),responseMap);
        }
        return AjaxJsonEntity.success("完工成功",responseMap);
    }

    // 将完工对象转为Map对象，返回给前端
    private void completeModelToMap(ReturnCompleteModel completeModel,Map<String,Object> map){
        map.clear();
        if(completeModel == null || CollectionUtils.isEmpty(completeModel.getItems())){
            return;
        }
        completeModel.getItems().forEach(t->{
            OrderReturnComplete.ItemTypeEnum itemType = OrderReturnComplete.ItemTypeEnum.get(t.getItemType());
            if(itemType != null) {
                map.put(itemType.getCode(), t);
            }
        });
    }

    /**
     * 验证B2B条码SN
     */
    @ResponseBody
    @PostMapping(value="validSN")
    public AjaxJsonEntity validSN(B2BSNValidModel validModel, HttpServletRequest request, HttpServletResponse respons){
        if(validModel == null
                || validModel.getDataSourceId() == null
                || validModel.getDataSourceId() <=0
                || StringUtils.isBlank(validModel.getSn())){
            return AjaxJsonEntity.fail("验证产品SN失败: 提交验证信息不完整",null);
        }
        User user = UserUtils.getUser();
        MSResponse msResponse = b2BCenterOrderService.checkProductSN(validModel.getDataSourceId(),validModel.getB2bOrderNo(),validModel.getSn(),user);
        if(MSResponse.isSuccessCode(msResponse)){
            return AjaxJsonEntity.success("",null);
        }
        return 	AjaxJsonEntity.fail("验证产品SN失败: " + msResponse.getMsg(),null);
    }
    //endregion 退换货
}
