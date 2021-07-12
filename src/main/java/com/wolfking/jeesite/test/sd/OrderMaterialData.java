package com.wolfking.jeesite.test.sd;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.ms.material.mq.entity.mapper.B2BMaterialMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单配件数据处理
 * @author Ryan
 * @date  2019/08/26
 */
@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class OrderMaterialData {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SequenceIdService sequenceIdService;

    /**
     * 按分片写入配件产品表
     * 1.主数据读取sd_materialmaster
     * 2.读取订单sd_order.item_json
     * 3.读取sd_materialitem
     * 更新:
     * 1.update apply_time
     * 2.insert sd_material_product
     * 3.update sd_materialitem.material_product_id

    @Test
    public void reWriteMaterialProducts() throws InterruptedException {
        //SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(31,31);
        List<String> quarters = Lists.newArrayList("20193");
        //List<String> quarters = Lists.newArrayList("20181","20182","20183","20184");
        System.out.println("start quaters:" + String.join(",",quarters));
        //List<String> quarters = Lists.newArrayList("20191","20192","20193");
        Long maxId = null;
        Long id = 1166534838127214592L;
        List<MaterialMaster> list = orderMaterialService.findToFixMaterialForms(quarters,id,maxId,50000);
        if(ObjectUtils.isEmpty(list)){
            System.out.println("no data return");
            return;
        }else{
            System.out.println("return data count:" + list.size());
        }
        Order order;
        List<OrderItem> orderItems;
        Map<Long,List<OrderItem>> orderItemMap;
        Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
        Map<Long, Product> singleProductMap = productService.getSingleProductList().stream().collect(Collectors.toMap(LongIDBaseEntity::getId, p->p));
        Map<Long,Product> setProductMap = productService.getSetProductList().stream().collect(Collectors.toMap(LongIDBaseEntity::getId, p->p));
        int size = list.size();
        MaterialMaster master;
        MaterialItem item;
        MaterialProduct materialProduct;
        List<MaterialItem> items;

        Customer customer;
        Product product,sProduct;
        ServiceType serviceType;
        MaterialProduct.MaterialProductBuilder builder;
        Long preOrderId =0L;
        Long pid;
        int applyTime = 0;
        Long productItemId;
        OrderItem orderItem;
        Map<Long,Product> itemProductGroup;
        List<String> singleProductIds;
        int materialProductItemNo = 0;
        for(int i=0;i<size;i++){
            master = list.get(i);
            //读取订单单头
            order = orderService.getOrderItems(master.getOrderId(),master.getQuarter());
            if(order == null){
                log.error("[error]order:{} quarter:{} not found",master.getOrderId(),master.getQuarter());
                continue;
            }
            if(preOrderId.equals(master.getOrderId())){
                applyTime++;
                orderMaterialService.updateMaterialFormApplyTime(master.getQuarter(),master.getId(),applyTime);
            }else{
                applyTime = 1;
                preOrderId = master.getOrderId();
            }
            //下单项目
            orderItemMap = order.getItems().stream().collect(Collectors.groupingBy(OrderItem::getProductId));
            builder = MaterialProduct.builder()
                    .quarter(master.getQuarter())
                    //.itemNo(applyTime)
                    .materialMasterId(master.getId());
            itemProductGroup = master.getItems().stream().map(t->t.getProduct()).distinct().collect(Collectors.toMap(Product::getId,p->p));
            materialProductItemNo = 0;
            //配件产品列表
            for(Map.Entry<Long,Product> entry:itemProductGroup.entrySet()){
                pid = entry.getKey();
                productItemId = sequenceIdService.nextId();
                materialProductItemNo++;
                builder = builder
                        .itemNo(materialProductItemNo)
                        .id(productItemId);

                //product
                product = singleProductMap.getOrDefault(pid,null);
                if(product == null){
                    //set
                    product = setProductMap.getOrDefault(pid,null);
                }
                if(product == null){
                    log.error("[error]order:{} prodict:{} not found",master.getOrderId(),pid);
                    continue;
                }else{
                    builder = builder.product(product);
                }
                //item
                orderItem = null;
                sProduct = null;
                orderItems = orderItemMap.getOrDefault(pid,null);
                if(!ObjectUtils.isEmpty(orderItems)){
                    //单品
                    orderItem = orderItems.get(0);
                }else{
                    //set
                    sProduct = setProductMap.getOrDefault(pid,null);
                    if(sProduct == null){
                        log.error("[error]order:{} prodict:{} not found",master.getOrderId(),pid);
                        continue;
                    }
                    orderItems = orderItemMap.getOrDefault(pid,null);
                    if(!ObjectUtils.isEmpty(orderItems)){
                        orderItem = orderItems.get(0);
                    }
                }
                if(orderItem != null && sProduct != null){
                    serviceType = serviceTypeMap.get(orderItem.getServiceType().getId());
                    materialProduct = builder
                            .product(sProduct)
                            .brand(orderItem.getBrand())
                            .productSpec(orderItem.getProductSpec())
                            .serviceType(serviceType)
                            .warrantyType(serviceType.getWarrantyStatus().getValue())
                            .build();
                    orderMaterialService.insertMaterialProduct(materialProduct);
                    orderMaterialService.updateMaterialItemProductItemId(master.getQuarter(),master.getId(),pid,materialProduct.getId());
                }
            }
            serviceTypeMap = null;
            singleProductMap = null;
            setProductMap = null;
            list= null;
            //sequenceIdUtils = null;
        }


    }
     */

    @Test
    public void testJoyoungMaterialFormMapper(){
        Long id = 1166534838127214592L;
        String quarter = "20193";
        MaterialMaster materialMaster = orderMaterialService.getMaterialMasterById(id,quarter,true);
        List<MaterialAttachment> attachments = orderMaterialService.findAttachementsByMasterId(id,quarter);
        if(!ObjectUtils.isEmpty(attachments)){
            materialMaster.setAttachments(attachments);
        }
        B2BMaterialMapper mapper = Mappers.getMapper(B2BMaterialMapper.class);
        B2BMaterial joyoung = mapper.toB2BMaterialForm(materialMaster);
        System.out.println("joyoung materkal:" + GsonUtils.getInstance().toGson(joyoung));
    }

}
