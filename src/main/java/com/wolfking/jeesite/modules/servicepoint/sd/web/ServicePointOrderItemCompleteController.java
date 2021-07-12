package com.wolfking.jeesite.modules.servicepoint.sd.web;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderItemService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpCustomerProductCompletePicService;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpProductCompletePicService;
import com.wolfking.jeesite.modules.servicepoint.ms.md.SpProductService;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * 网点完工图片
 */
@Controller
@RequestMapping(value = "${adminPath}/servicePoint/sd/orderItemComplete")
@Slf4j
public class ServicePointOrderItemCompleteController extends BaseController {

    @Autowired
    private SpOrderCacheReadService orderCacheReadService;
    @Autowired
    private SpOrderService orderService;
    @Autowired
    private SpOrderAttachmentService attachmentService;
    @Autowired
    private SpProductService productService;
    @Autowired
    private SpProductCompletePicService productCompletePicService;
    @Autowired
    private SpOrderItemCompleteService orderItemCompleteService;
    @Autowired
    private SpCustomerProductCompletePicService customerPicService;
    @Autowired
    private OrderItemService orderItemService;

    private static final String VIEW_NAME_VIEW_ORDER_PRODUCT_ATTACHEMENT_FORM = "modules/servicePoint/sd/orderItemComplete/viewOrderProductAttachmentForm";
    private static final String VIEW_NAME_PIC_BARCODE_FORM = "modules/servicePoint/sd/orderItemComplete/picBarcodeForm";
    private static final String VIEW_NAME_VIEW_ORDER_PRODUCT_ATTACHMENT_RECORD = "modules/servicePoint/sd/orderItemComplete/viewOrderProductAttachmentRecord";
    private static final String VIEW_NAME_VIEW_ORDER_ATTACHEMENT_FORM = "modules/servicePoint/sd/orderItemComplete/viewOrderAttachmentForm";
    private static final String VIEW_NAME_VIEW_BROWSE_ORDER_ATTACHMENT_FORM = "modules/servicePoint/sd/orderItemComplete/browseOrderAttachmentForm";


    /**
     * 跳转完成工单上传附件页面
     */
    @RequestMapping("orderAttachmentForm")
    public String orderAttachmentForm(@RequestParam Long orderId, @RequestParam String quarter, Model model) {
        String viewForm = VIEW_NAME_VIEW_ORDER_PRODUCT_ATTACHEMENT_FORM;
        Order order = new Order();
        Long lorderId = Long.valueOf(orderId);
        if (lorderId != null || lorderId > 0) {
            order = orderCacheReadService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
                model.addAttribute("order", new Order());
                return viewForm;
            }

            //获取产品完成照片上传配置
            List<Product> productList = new ArrayList<>();
            List<ProductCompletePic> completePicList = new ArrayList<>();
            Long customerId = order.getOrderCondition().getCustomer().getId();
            if (order.getItems() != null && order.getItems().size() > 0) {
                for (OrderItem item : order.getItems()) {
                    Product product = null;
                    ProductCompletePic completePic = null;
                    if (item.getProductId() != null && item.getProductId() > 0) {
                        Product entity = productService.getProductByIdFromCache(item.getProductId());
                        //判断是否是套组
                        if (entity.getSetFlag() == 1) {
                            String[] productIds = entity.getProductIds().split(",");
                            for (int i = 0; i < productIds.length; i++) {
                                //查看客户上传附件配置
                                completePic = customerPicService.getFromCache(Long.valueOf(productIds[i]), customerId);
                                if (completePic == null) {
                                    //如果客户没有配置,再从产品配置获取
                                    completePic = productCompletePicService.getFromCache(Long.valueOf(productIds[i]));
                                }
                                if (completePic != null) {
                                    completePic.parseItemsFromJson();
                                    product = new Product();
                                    product.setName(productService.getProductByIdFromCache(Long.valueOf(productIds[i])).getName());
                                    product.setId(Long.valueOf(productIds[i]));
                                    completePic.setProduct(product);
                                    for (int j = 0; j < item.getQty(); j++) {
                                        productList.add(product);
                                        completePicList.add(completePic);
                                    }
                                } else {
                                    product = new Product();
                                    completePic = new ProductCompletePic();
                                    product.setName(productService.getProductByIdFromCache(Long.valueOf(productIds[i])).getName());
                                    product.setId(Long.valueOf(productIds[i]));
                                    completePic.setProduct(product);
                                    productList.add(product);
                                    completePicList.add(completePic);
                                }
                            }
                        } else {
                            completePic = customerPicService.getFromCache(item.getProductId(), customerId);
                            if (completePic == null) {
                                completePic = productCompletePicService.getFromCache(item.getProductId());
                            }
                            if (completePic != null) {
                                completePic.parseItemsFromJson();
                                product = new Product();
                                product.setName(productService.getProductByIdFromCache(item.getProductId()).getName());
                                product.setId(item.getProductId());
                                completePic.setProduct(product);
                                for (int j = 0; j < item.getQty(); j++) {
                                    productList.add(product);
                                    completePicList.add(completePic);
                                }
                            } else {
                                product = new Product();
                                completePic = new ProductCompletePic();
                                product.setName(productService.getProductByIdFromCache(item.getProductId()).getName());
                                product.setId(item.getProductId());
                                completePic.setProduct(product);
                                productList.add(product);
                                completePicList.add(completePic);
                            }
                        }
                    }
                }
            }
            order.setProducts(productList);

            if (completePicList == null || completePicList.size() <= 0) {
                addMessage(model, "此工单产品没有配置上传附件配置");
                model.addAttribute("order", order);
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
            } else {
                itemCompleteList = Lists.newArrayList();
            }
           /* model.addAttribute("itemCompleteList",itemCompleteList);
            model.addAttribute("completePicList", completePicList);*/
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = null;
            for (OrderItemComplete itemComplete : itemCompleteList) {
                Iterator<ProductCompletePic> iterator = completePicList.iterator();
                while (iterator.hasNext()) {
                    ProductCompletePic completePic = iterator.next();
                    if (itemComplete.getProduct().getId().equals(completePic.getProduct().getId()) && completePic.getItems() != null) {
                        map = new HashMap<>();
                        map.put("completePic", completePic);
                        map.put("itemComplete", itemComplete);
                        list.add(map);
                        iterator.remove();
                        break;
                    }
                }
            }
            if (completePicList != null && completePicList.size() > 0) {
                for (ProductCompletePic completePic : completePicList) {
                    //还没有上传过照片的产品
                    map = new HashMap<>();
                    map.put("completePic", completePic);
                    map.put("itemComplete", null);
                    list.add(map);
                }
            }
            model.addAttribute("list", list);
        }
        model.addAttribute("order", order);


        return viewForm;
    }

    /**
     * 上传的附件/
     */
    @ResponseBody
    @RequestMapping("saveOrderCompletePic")
    public AjaxJsonEntity saveOrderCompletePic(OrderItemComplete orderItemComplete, @RequestParam String filePath, @RequestParam String field, @RequestParam Long productId, @RequestParam Long customerId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            Long lorderId = Long.valueOf(orderItemComplete.getOrderId());
            if (lorderId == null || lorderId <= 0 || customerId == null || customerId <= 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
                return jsonEntity;
            }
            Order order = orderCacheReadService.getOrderById(lorderId, "", OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("系统繁忙，读取订单失败，请重试");
                return jsonEntity;
            }
            if (!StringUtils.isNotBlank(filePath)) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("文件路径为空,请确认");
                return jsonEntity;
            }
            if (!StringUtils.isNotBlank(field)) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("图片类型为空,请确认");
                return jsonEntity;
            }

            ProductCompletePic productCompletePic;
            //该订单客户配置了完工图片规格
            productCompletePic = customerPicService.getFromCache(productId, customerId);
            if (productCompletePic == null) {
                productCompletePic = productCompletePicService.getFromCache(productId);
            }
            if (productCompletePic != null && StringUtils.isNotBlank(productCompletePic.getJsonInfo())) {
                productCompletePic.parseItemsFromJson();
                if (productCompletePic.getItems() == null && productCompletePic.getItems().size() <= 0) {
                    jsonEntity.setSuccess(false);
                    jsonEntity.setMessage("产品完成工单上传照片配置参数不完整,请确认");
                    return jsonEntity;
                }
                ProductCompletePicItem item = productCompletePic.getItems().stream().filter(t -> t.getPictureCode().equalsIgnoreCase(field))
                        .findFirst().orElse(null);
                if (item == null) {
                    jsonEntity.setSuccess(false);
                    jsonEntity.setMessage("产品完成工单上传照片配置没有相应的图片code,请确认");
                    return jsonEntity;
                }
                item.setUrl(filePath);
                item.setUploadDate(new Date());//上传时间 2019-06-25
                List<ProductCompletePicItem> picItemList = new ArrayList<>();
                if (orderItemComplete.getId() != null && orderItemComplete.getId() > 0) {
                    OrderItemComplete entity = orderItemCompleteService.get(orderItemComplete.getId());
                    if (entity != null) {
                        orderItemComplete.setUploadQty(entity.getUploadQty() + 1);
                        picItemList = OrderUtils.fromProductCompletePicItemsJson(entity.getPicJson());
                        if (picItemList != null && picItemList.size() > 0) {
                            Iterator<ProductCompletePicItem> iterator = picItemList.iterator();
                            while (iterator.hasNext()) {
                                ProductCompletePicItem picItem = iterator.next();
                                //判断数据库中是否已经上传了这张图片
                                if (picItem.getPictureCode().equals(field.toLowerCase())) {
                                    iterator.remove();
                                    orderItemComplete.setUploadQty(orderItemComplete.getUploadQty() - 1);
                                }
                            }
                        }
                    }
                } else {
                    Integer uploadedProductQty = orderItemCompleteService.getProductQty(orderItemComplete.getOrderId(), orderItemComplete.getQuarter(), productId);
                    Order orderIem = orderItemService.getOrderItems(orderItemComplete.getQuarter(), orderItemComplete.getOrderId());
                    int productQty = 0;
                    if (orderIem != null && !orderIem.getItems().isEmpty()) {
                        productQty = orderItemService.getProductQty(order.getItems(), productId);
                    }
                    if (productQty <= uploadedProductQty) {
                        jsonEntity.setSuccess(false);
                        jsonEntity.setMessage("图片组数不能大于产品数量");
                        return jsonEntity;
                    }
                    orderItemComplete.setItemNo(0);
                    orderItemComplete.setUploadQty(1);
                    Product product = new Product();
                    product.setId(productId);
                    orderItemComplete.setProduct(product);
                }
                picItemList.add(item);
                orderItemComplete.preInsert();
                orderItemComplete.setPicJson(new Gson().toJson(picItemList));
                orderItemCompleteService.save(orderItemComplete);
                jsonEntity.setData(orderItemComplete);

            } else {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("该产品还没配置工单完成上传照片的配置");
                return jsonEntity;
            }

        } catch (OrderException oe) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(oe.getMessage());
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        // 更新订单信息
        return jsonEntity;
    }

    /**
     * 删除上传的附件/
     */
    @ResponseBody
    @RequestMapping("deletePic")
    public AjaxJsonEntity deletePic(@RequestParam String id, @RequestParam String field, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            Long completePicId = Long.valueOf(id);
            if (completePicId == null || completePicId < 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
                return jsonEntity;
            }
            if (!StringUtils.isNotBlank(field)) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
                return jsonEntity;
            }
            OrderItemComplete entity = orderItemCompleteService.get(completePicId);
            if (entity == null) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("数据不存在");
                return jsonEntity;
            }
            List<ProductCompletePicItem> orderPicItemList = new ArrayList<>();
            orderPicItemList = OrderUtils.fromProductCompletePicItemsJson(entity.getPicJson());
            if (orderPicItemList == null || orderPicItemList.size() <= 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("删除照片失败");
            }
            if (orderPicItemList != null && orderPicItemList.size() > 0) {
                Iterator<ProductCompletePicItem> iterator = orderPicItemList.iterator();
                while (iterator.hasNext()) {
                    ProductCompletePicItem picItem = iterator.next();
                    if (picItem.getPictureCode().equals(field.toLowerCase())) {
                        iterator.remove();
                        entity.setItemList(orderPicItemList);
                        entity.setPicJson(GsonUtils.toGsonString(orderPicItemList));
                        entity.setUploadQty(entity.getUploadQty() - 1);
                        break;
                    }
                }
            }
            int result = orderItemCompleteService.deletePic(entity);
            jsonEntity.setData(result);
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 删除上传的附件(整条数据)
     */
    @ResponseBody
    @RequestMapping("deleteItemComplete")
    public AjaxJsonEntity deleteItemComplete(@RequestParam String itemCompleteId, @RequestParam String orderId, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            Long lattachmentid = Long.valueOf(itemCompleteId);
            Long lorderId = Long.valueOf(orderId);
            if (lattachmentid == null || lattachmentid <= 0 || lorderId == null || lorderId <= 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
            } else {
                String quarter = orderService.getOrderQuarterFromCache(lorderId);
                OrderItemComplete orderItemComplete = new OrderItemComplete();
                orderItemComplete.setId(lattachmentid);
                orderItemComplete.setOrderId(lorderId);
                orderItemComplete.setQuarter(quarter);
                orderItemComplete.preUpdate();
                orderItemCompleteService.delete(orderItemComplete);
            }
            return jsonEntity;
        } catch (OrderException oe) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(oe.getMessage());
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("删除附件错误:" + e.getMessage());
            log.error("[OrderController.deleteAttach] orderId:{} , attachmentId:{}", orderId, itemCompleteId, e);
        }
        return jsonEntity;
    }

    /**
     * 跳转编辑产品条码页面
     */
    @RequestMapping("editBarcode")
    public String editBarCode(OrderItemComplete orderItemComplete, String productIndex, Long productId, Model model) {
        if (productId != null && productId > 0) {
            orderItemComplete.setProduct(new Product(productId));
        }
        String unitBarCode = orderItemCompleteService.getUnitBarcodeById(orderItemComplete.getId(),orderItemComplete.getQuarter());
        orderItemComplete.setUnitBarcode(unitBarCode);
        model.addAttribute("productIndex", productIndex);
        model.addAttribute("orderItemComplete", orderItemComplete);
        return VIEW_NAME_PIC_BARCODE_FORM;
    }

    /**
     * 修改产品条码
     */
    @RequestMapping("updateBarcode")
    @ResponseBody
    public AjaxJsonEntity updateBarcode(OrderItemComplete orderItemComplete, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        try {
            if (orderItemComplete == null) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("数据不存在");
                return jsonEntity;
            }

            if (orderItemComplete.getId() != null && orderItemComplete.getId() <= 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("参数错误");
                return jsonEntity;
            }
            orderItemCompleteService.updateBarcode(orderItemComplete);
            jsonEntity.setData(orderItemComplete);
        } catch (Exception e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage(e.getMessage());
        }
        return jsonEntity;
    }

    /**
     * 获取已删除的照片记录
     */
    @RequestMapping("getDelListByOrderId")
    public String getDelListByOrderId(@RequestParam Long orderId, @RequestParam String quarter, Model model) {
        if (orderId == null || orderId <= 0) {
            addMessage(model, "参数错误");
            return VIEW_NAME_VIEW_ORDER_PRODUCT_ATTACHMENT_RECORD;
        }
        List<OrderItemComplete> list = orderItemCompleteService.getDelListByOrderId(orderId, quarter);
        if (list != null && list.size() > 0) {
            List<ProductCompletePicItem> picItems;
            Product product;
            for (OrderItemComplete entity : list) {
                picItems = OrderUtils.fromProductCompletePicItemsJson(entity.getPicJson());
                product = productService.getProductByIdFromCache(entity.getProduct().getId());
                entity.setItemList(picItems);
                if (product != null) {
                    entity.setProduct(product);
                }
            }
        }
        model.addAttribute("picCompleteList", list);
        return VIEW_NAME_VIEW_ORDER_PRODUCT_ATTACHMENT_RECORD;
    }

    /**
     * 订单附件的查看(老的完工图片查看)
     *
     * @param orderId 订单id
     */
    @RequestMapping("viewDetailAttachment")
    public String viewDetailAttachment(@RequestParam String orderId, @RequestParam String quarter, HttpServletRequest request, Model model) {
        Order order = new Order();
        Long lorderId = Long.valueOf(orderId);
        if (lorderId != null || lorderId > 0) {
            order = orderCacheReadService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                addMessage(model, "错误：系统繁忙，读取订单失败，请重试。");
                model.addAttribute("order", new Order());
                return VIEW_NAME_VIEW_ORDER_ATTACHEMENT_FORM;
            }
            if (order.getOrderCondition().getFinishPhotoQty() == 0) {
                addMessage(model, "此单上门服务没有添加附件");
                model.addAttribute("order", order);
                return VIEW_NAME_VIEW_ORDER_ATTACHEMENT_FORM;
            }

            List<OrderAttachment> attachments = order.getAttachments();
            if (attachments == null || attachments.size() == 0) {
                attachments = attachmentService.getAttachesByOrderId(lorderId, order.getQuarter());
                if (attachments == null || attachments.size() == 0) {
                    attachments = Lists.newArrayList();
                } else {
                    //调用公共缓存
                    OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
                    builder.setOpType(OrderCacheOpType.UPDATE)
                            .setOrderId(lorderId)
                            .setAttachments(attachments);
                    OrderCacheUtils.update(builder.build());
                }
            }
            order.setAttachments(attachments);
        }
        model.addAttribute("order", order);
        return VIEW_NAME_VIEW_ORDER_ATTACHEMENT_FORM;
    }

    /**
     * 查看完成照片
     * 只有浏览权限
     */
    @RequestMapping("browseOrderAttachment")
    public String browseOrderAttachment(@RequestParam Long orderId, @RequestParam String quarter, Model model) {
        String viewForm = VIEW_NAME_VIEW_BROWSE_ORDER_ATTACHMENT_FORM;
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

}
