/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.api.service.sd;

import com.kkl.kklplus.entity.md.MDEngineerEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.modules.api.entity.md.RestProduct;
import com.wolfking.jeesite.modules.api.entity.sd.RestMaterialMasterNew;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * App工单配件
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AppOrderMaterialService extends LongIDBaseService {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    /**
     * 获取工单子项中的产品ID列表，如产品为套组，则拆分出具体的产品ID
     */
    private List<Long> getSpecificProductIds(List<Long> productIds) {
        List<Long> result = Lists.newArrayList();
        if (productIds != null && !productIds.isEmpty()) {
            productIds = productIds.stream().filter(i -> i != null && i > 0).distinct().collect(Collectors.toList());
            Map<Long, Product> productMap = productService.getProductMap(productIds);
            Product product;
            Long productId;
            String[] setIds;
            for (Long pId : productIds) {
                product = productMap.get(pId);
                if (product != null) {
                    if (product.getSetFlag() == 1) {
                        setIds = product.getProductIds().split(",");
                        for (String setId : setIds) {
                            productId = StringUtils.toLong(setId);
                            if (productId > 0) {
                                result.add(productId);
                            }
                        }
                    } else {
                        result.add(pId);
                    }
                }
            }
            result = result.stream().distinct().collect(Collectors.toList());
        }
        return result;
    }

    private List<Long> getSpecificProductIdsByOrderItems(List<OrderItem> orderItems) {
        List<Long> result = Lists.newArrayList();
        if (orderItems != null && !orderItems.isEmpty()) {
            List<Long> pIds = orderItems.stream()
                    .filter(i -> i.getProduct() != null && i.getProduct().getId() != null)
                    .map(OrderItem::getProductId).distinct().collect(Collectors.toList());
            result = getSpecificProductIds(pIds);
        }
        return result;
    }

    public Long getOrderDetailId(List<OrderDetail> details, Set<Long> productIdSet) {
        Long orderDetailId = 0L;
        if (details != null && !details.isEmpty() && productIdSet != null && !productIdSet.isEmpty()) {
            OrderDetail detail = details.stream().filter(i -> productIdSet.contains(i.getProductId())).findFirst().orElse(null);
            if (detail == null) {
                for (OrderDetail item : details) {
                    List<Long> tempList = getSpecificProductIds(Lists.newArrayList(item.getProductId()));
                    Long pId = tempList.stream().filter(productIdSet::contains).findFirst().orElse(null);
                    if (pId != null && pId > 0) {
                        orderDetailId = item.getId();
                        break;
                    }
                }
            } else {
                orderDetailId = detail.getId();
            }
        }

        return orderDetailId;
    }


    /**
     * 获取产品的配件列表
     */
    public List<RestProduct> getProductMaterialList(List<OrderItem> orderItems) {
        List<Long> productIds = getSpecificProductIdsByOrderItems(orderItems);
        List<RestProduct> result = Lists.newArrayList();
        Map<Long, Product> productMap = productService.getProductMap(productIds);
        Product product;
        String productName;
        for (Long pId : productIds) {
            product = productMap.get(pId);
            productName = product != null && StringUtils.isNotBlank(product.getName()) ? product.getName() : "";
            RestProduct restProduct = new RestProduct();
            restProduct.setId(pId.toString());
            restProduct.setName(productName);
            restProduct.setMaterials(productService.getMaterialByProductId(pId));
            result.add(restProduct);
        }
        return result;
    }

    public List<RestMaterialMasterNew> getOrderMaterials(Long orderId, String quarter) {
        List<RestMaterialMasterNew> result = Lists.newArrayList();
        if (orderId != null && orderId > 0 && StringUtils.isNotBlank(quarter)) {
            List<MaterialMaster> materialMasters = orderMaterialService.findMaterialMastersByOrderIdMS(orderId, quarter);
            Map<Long, Area> areaMap = AreaUtils.getAreaMap(Area.TYPE_VALUE_COUNTY);
            Area area;
            for (MaterialMaster master : materialMasters) {
                RestMaterialMasterNew restMaster = new RestMaterialMasterNew();
                restMaster.setId(master.getId().toString());
                restMaster.setQuarter(master.getQuarter());
                restMaster.setOrderId(master.getOrderId().toString());
                restMaster.setOrderDetailId(master.getOrderDetailId().toString());
                restMaster.setApplyType(master.getApplyType().getLabel());
                restMaster.setApplyTypeValue(master.getApplyType().getValue());
                restMaster.setStatus(master.getStatus().getValue());
                restMaster.setStatusName(master.getStatus().getLabel());
                restMaster.setExpressCompany(master.getExpressCompany().getLabel());
                restMaster.setExpressNo(master.getExpressNo());
                restMaster.setRemarks(master.getRemarks());
                restMaster.setTotalPrice(master.getTotalPrice());
                restMaster.setCreateDate(master.getCreateDate().getTime());

                //收件人信息
                restMaster.setReceiver(StringUtils.toString(master.getReceiver()));
                restMaster.setReceiverPhone(StringUtils.toString(master.getReceiverPhone()));
                if (master.getReceiverAreaId() != null && master.getReceiverAreaId() > 0) {
                    area = areaMap.get(master.getReceiverAreaId());
                    if (area != null) {
                        restMaster.setReceiverAddress(area.getFullName());
                    }
                }
                restMaster.setReceiverAddress(restMaster.getReceiverAddress() + StringUtils.toString(master.getReceiverAddress()));
                //TODO: 收件人地址类型暂时未加表字段
                restMaster.setReceiverType(master.getReceiverType());

                if (master.getStatus().getIntValue().equals(MaterialMaster.STATUS_APPROVED)
                        || master.getStatus().getIntValue().equals(MaterialMaster.STATUS_SENDED)
                        || master.getStatus().getIntValue().equals(MaterialMaster.STATUS_CLOSED)
                        || master.getStatus().getIntValue().equals(MaterialMaster.STATUS_ABNORMAL)
                ) {
                    restMaster.setReturnFlag(master.getReturnFlag());
                } else {
                    restMaster.setReturnFlag(0);
                }
                if (master.getAttachments() != null && !master.getAttachments().isEmpty()) {
                    for (MaterialAttachment attachment : master.getAttachments()) {
                        RestMaterialMasterNew.Photo photo = new RestMaterialMasterNew.Photo();
                        photo.setPhotoId(attachment.getId().toString());
                        photo.setFilePath(OrderPicUtils.getOrderPicHostDir() + attachment.getFilePath());
                        photo.setRemarks(attachment.getRemarks());
                        restMaster.getPhotos().add(photo);
                    }
                }
                if (master.getItems() != null && !master.getItems().isEmpty()) {
                    Map<Long, List<MaterialItem>> itemMap = master.getItems().stream().collect(Collectors.groupingBy(i -> i.getProduct().getId()));
                    List<Long> productIds = itemMap.keySet().stream().distinct().collect(Collectors.toList());
                    Map<Long, Product> productMap = productService.getProductMap(productIds);
                    for (Map.Entry<Long, List<MaterialItem>> entry : itemMap.entrySet()) {
                        RestMaterialMasterNew.Product restProduct = new RestMaterialMasterNew.Product();
                        Product product = productMap.get(entry.getKey());
                        if (product != null) {
                            restProduct.setProductId(product.getId());
                            restProduct.setProductName(product.getName());
                        }
                        if (!entry.getValue().isEmpty()) {
                            for (MaterialItem item : entry.getValue()) {
                                RestMaterialMasterNew.Material restMaterial = new RestMaterialMasterNew.Material();
                                restMaterial.setMaterialId(item.getId().toString());
                                restMaterial.setMaterialName(item.getMaterial().getName());
                                restMaterial.setQty(item.getQty());
                                restMaterial.setPrice(item.getPrice());
                                restMaterial.setTotalPrice(item.getTotalPrice());
                                restProduct.getItems().add(restMaterial);
                            }
                        }
                        restMaster.getProducts().add(restProduct);
                    }
                }
                result.add(restMaster);
            }
        }
        return result;
    }

}
